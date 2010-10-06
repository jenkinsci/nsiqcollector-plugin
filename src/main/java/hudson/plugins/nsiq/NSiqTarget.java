package hudson.plugins.nsiq;

import hudson.FilePath;
import hudson.model.Build;
import hudson.model.Run;
import hudson.plugins.nsiq.model.Complexity;
import hudson.plugins.nsiq.model.FileType;
import hudson.plugins.nsiq.model.Level;
import hudson.plugins.nsiq.model.NSiqResult;
import hudson.plugins.nsiq.model.NSiqSummary;
import hudson.tasks.Builder;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;

import org.apache.commons.lang.mutable.MutableInt;
import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.springframework.beans.BeanUtils;

/**
 * N'SIQ Collector의 결과를 저장하고 표시하기 위한 클래스
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqTarget implements Serializable, NSiqAware {
	private static final long serialVersionUID = 2951637921403461196L;

	private Build<?, ?> owner = null;

	private final String name;
	private final Level level;
	private final NSiqSummary summary = new NSiqSummary();
	private final List<NSiqResult> nsiqResult;

	private final NSiqTarget parent;
	private final Map<String, NSiqTarget> children = new LinkedHashMap<String, NSiqTarget>();

	private NSiqTarget(Build<?, ?> owner, NSiqTarget parent, final String name, final Level level, final List<NSiqResult> nsiqResult) {
		this.owner = owner;
		this.parent = parent;
		this.name = name;
		this.level = level;
		this.nsiqResult = nsiqResult;

		parseSummary();

		if (level != Level.Function) {
			parseChildren();
		}
	}

	public static NSiqTarget load(Build<?, ?> owner, final List<NSiqResult> nsiqResult) {
		NSiqTarget target = new NSiqTarget(owner, null, null, Level.Directory, nsiqResult);
		return target;
	}

	public String getName() {
		return name;
	}

	public Build<?, ?> getOwner() {
		return owner;
	}

	public void setOwner(Build<?, ?> build) {
		this.owner = build;
	}

	public String getLevel() {
		return level.getName();
	}

	public NSiqTarget getParent() {
		return parent;
	}

	public Map<String, NSiqTarget> getChildren() {
		return children;
	}

	public NSiqTarget getChild(String name) {
		return children.get(name);
	}

	public NSiqSummary getSummary() {
		return summary;
	}

	public NSiqResult getNSiqResult() {
		if (level != Level.Function) {
			return null;
		}

		if (nsiqResult != null && !nsiqResult.isEmpty()) {
			return this.nsiqResult.get(0);
		}

		return null;
	}

	public List<Complexity> getComplexityList() {
		NSiqResult result = getNSiqResult();

		if (result != null) {
			return result.getFunctions();
		}

		return null;
	}

	public List<NSiqResult> getNSiqResultList() {
		if (level == Level.Function) {
			return null;
		}

		return nsiqResult;
	}

	private NSiqPublisher getPublisher() {
		return (NSiqPublisher) owner.getProject().getPublisher(NSiqPublisher.DESCRIPTOR);
	}

	public boolean isLocView() {
		return getPublisher().isLocView();
	}

	public boolean isComplexityView() {
		return getPublisher().isComplexityView();
	}

	public boolean isOverView() {
		return getPublisher().isOverView();
	}

	@SuppressWarnings("unchecked")
	private List<Complexity> getOverComplexityList() {
		List<Complexity> overComplexityList = new LinkedList<Complexity>();

		for (NSiqResult nsiq : nsiqResult) {
			if (nsiq.getFunctions() == null) {
				continue;
			}

			for (Complexity complexity : nsiq.getFunctions()) {
				if (isReportableComplexity(complexity)) {
					overComplexityList.add(complexity);
				}
			}
		}

		// reverse sort
		Collections.sort(overComplexityList, new Comparator() {
			public int compare(Object o1, Object o2) {
				if (o1 == null || o2 == null) {
					return 0;
				}

				if (o1 instanceof Complexity && o2 instanceof Complexity) {
					Complexity c1 = (Complexity) o1;
					Complexity c2 = (Complexity) o2;
					return Integer.valueOf(c2.getComplexity()).compareTo(c1.getComplexity());
				}

				return 0;
			}
		});

		return overComplexityList;
	}

	private boolean isReportableComplexity(Complexity complexity) {
		return (!isOverView() && complexity.getComplexity() >= LOW) || (isOverView() && complexity.getComplexity() >= HIGH);
	}

	public List<Complexity> getOverComplexity() {
		List<Complexity> result = new LinkedList<Complexity>();
		List<Complexity> overComplexity = getOverComplexityList();

		for (int i = 0; i < overComplexity.size() && i <= Constant.OVER_COUNT; i++) {
			result.add(overComplexity.get(i));
		}

		return result;
	}

	/**
	 * summary
	 */
	private void parseSummary() {
		int total = 0;
		int high = 0;
		int low = 0;
		int comp = 0;
		int totalLoc = 0;
		int codeLoc = 0;
		Map<FileType, MutableInt> locPerType = new HashMap<FileType, MutableInt>();

		for (NSiqResult nsiq : nsiqResult) {
			totalLoc += nsiq.getTotalLoc();
			codeLoc += nsiq.getCodeLoc();

			MutableInt eachLocPerType = locPerType.get(nsiq.getType());
			if (eachLocPerType == null) {
				eachLocPerType = new MutableInt();
				locPerType.put(nsiq.getType(), eachLocPerType);
			}
			eachLocPerType.add(nsiq.getCodeLoc());

			if (nsiq.getFunctions() == null) {
				continue;
			}

			for (Complexity complexity : nsiq.getFunctions()) {
				total++;

				if (complexity.getComplexity() >= HIGH) {
					high++;
				}

				if (complexity.getComplexity() >= LOW) {
					low++;
				}

				if (comp < complexity.getComplexity()) {
					comp = complexity.getComplexity();
				}
			}
		}

		summary.setTotal(total);
		summary.setHigh(high);
		summary.setLow(low);
		summary.setComplexity(comp);
		summary.setTotalLoc(totalLoc);
		summary.setCodeLoc(codeLoc);
		summary.setLocPerType(locPerType);

	}

	public long getTimestamp() {
		if (Constant.DISABLE_IMAGE_CACHE) {
			return 0;
		}
		return owner.getTimestamp().getTimeInMillis();
	}
	/**
	 * children
	 */
	private void parseChildren() {
		// 디렉토리의 unique 이름 목록을 저장한다.
		Set<String> treeSet = new TreeSet<String>();

		for (NSiqResult nsiq : nsiqResult) {
			treeSet.add(level == Level.Directory ? nsiq.getDir() : nsiq.getFile());
		}

		String[] sorted = treeSet.toArray(new String[treeSet.size()]);
		Arrays.sort(sorted, String.CASE_INSENSITIVE_ORDER);

		for (String name : sorted) {
			List<NSiqResult> childNSiqResult = getChildNSiqResult(name);
			NSiqTarget childTarget = new NSiqTarget(owner, this, name, level == Level.Directory ? Level.File : Level.Function, childNSiqResult);
			children.put(name, childTarget);
		}
	}

	/**
	 * 페이지에서 이름이 name인 정보를 리턴한다.
	 */
	private List<NSiqResult> getChildNSiqResult(String name) {
		List<NSiqResult> result = new LinkedList<NSiqResult>();

		for (NSiqResult nsiq : this.nsiqResult) {
			if (level == Level.Directory && name.equals(nsiq.getDir()) || (level == Level.File && name.equals(nsiq.getFile()))) {
				NSiqResult cloned = new NSiqResult();
				BeanUtils.copyProperties(nsiq, cloned);
				result.add(cloned);
			}
		}

		return result;
	}

	/**
	 * 디렉토리 별 페이지에서 파일 별 페이지로 이동하기 위해서 getDynamic 메소드를 사용한다.
	 * 
	 * @param token
	 *            서브 path의 이름 (예를 들어, /nsiq/com_nhncorp_lucy_common이 URL이라면,
	 *            token 값은 com_nhncorp_lucy_common이다)
	 * @throws ServletException
	 */
	public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
		if (level.equals(Level.Function) && "raw".equals(token)) {
			return new RedirectSrc();
		}

		for (String name : children.keySet()) {
			if (urlTransform(name).toLowerCase(Constant.LOCALE).equals(token.toLowerCase(Constant.LOCALE))) {
				return getChild(name);
			}
		}

		return null;
	}

	public class RedirectSrc {
		public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws ServletException, IOException, InterruptedException {
			rsp.setContentType("text/html");
			rsp.setCharacterEncoding(getPublisher().getEncoding().getEncodingKey());
			rsp.getWriter().write(new NSiqSrc(getSrcFile()).getFileContent(getPublisher().getEncoding()));
			rsp.flushBuffer();
		}
	}

	/**
	 * 그래프를 표시하기 위해 이전 결과 값을 리턴한다.
	 */
	public NSiqTarget getPreviousResult() {
		if (parent == null) {
			if (owner == null) {
				return null;
			}

			Run<?, ?> prevBuild = owner.getPreviousNotFailedBuild();

			if (prevBuild == null) {
				return null;
			}
			NSiqBuildAction action = null;

			while ((prevBuild != null) && (null == (action = prevBuild.getAction(NSiqBuildAction.class)))) {
				prevBuild = prevBuild.getPreviousNotFailedBuild();
			}

			if (action == null) {
				return null;
			}

			return action.getTarget();
		} else {
			NSiqTarget prevParent = parent.getPreviousResult();
			return prevParent == null ? null : prevParent.getChild(name);
		}
	}

	/**
	 * URL에서 숫자, 영어 대소문자가 아닌 경우 "_" 로 치환한다.
	 */
	public String urlTransform(String name) {
		StringBuilder buf = new StringBuilder(name.length());

		for (int i = 0; i < name.length(); i++) {
			final char c = name.charAt(i);

			if (('0' <= c && '9' >= c) || ('A' <= c && 'Z' >= c) || ('a' <= c && 'z' >= c)) {
				buf.append(c);
			} else {
				buf.append('_');
			}
		}

		return buf.toString();
	}

	private NSiqBuilder getBuilder() {
		for (Builder p : owner.getProject().getBuilders()) {
			if (p.getDescriptor() == NSiqBuilder.DESCRIPTOR)
				return (NSiqBuilder) p;
		}
		return null;
	}

	public String[] getSrc() {
		NSiqBuilder builder = getBuilder();
		if (builder == null) {
			return null;
		}
		return builder.getSrcDir().split(",");
	}

	public FilePath getSrcFile() throws InterruptedException, IOException {
		String dir = getNSiqResult().getDir();
		String file = getNSiqResult().getFile();
		for (FilePath moduleRoot : owner.getModuleRoots()) {
			for (String src : getSrc()) {
				FilePath path = moduleRoot.child(src.trim()).child(dir).child(file);
				if (path.exists())
					return path;
			}
		}
		return null;
	}

	public String getBaseURL() {
		return NSiqUtil.getHudsonBaseURL();
	}

	/**
	 * 디렉토리 별 페이지와 파일 별 페이지에서 복잡도 그래프를 표시한다.
	 */

	public Graph getLocGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {
				int i = 0;
				int lower = Integer.MAX_VALUE;
				int upper = Integer.MIN_VALUE;
				DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
				for (NSiqTarget target = NSiqTarget.this; (target != null && i++ < Constant.GRAPH_HISTORY_COUNT); target = target.getPreviousResult()) {
					ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(target.getOwner());
					NSiqSummary nsiqSummary = target.getSummary();
					lower = Math.min(lower, nsiqSummary.getCodeLoc());
					upper = Math.max(upper, nsiqSummary.getTotalLoc());
					dsb.add(nsiqSummary.getTotalLoc(), "Total LOC", label);
					dsb.add(nsiqSummary.getCodeLoc(), "Code LOC", label);
				}
				return NSiqUtil.createLineChart(dsb.build(), "lines", lower, upper);
			}

		};
	}

	/**
	 * 디렉토리 별 페이지와 파일 별 페이지에서 복잡도 그래프를 표시한다.
	 */

	public Graph getComplexityGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {
				DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dsb = new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
				int i = 0;
				for (NSiqTarget target = NSiqTarget.this; (target != null && i++ < Constant.GRAPH_HISTORY_COUNT); target = target.getPreviousResult()) {
					ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(target.getOwner());
					dsb.add(target.getSummary().getHigh(), "over " + HIGH, label);
					if (isOverView()) {
						dsb.add(target.getSummary().getLow() - target.getSummary().getHigh(), "over " + LOW, label);
					}
				}
				return NSiqUtil.createStackChart(dsb.build(), "count");
			}

		};
	}

	public Graph getLocDistGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {

				Map<FileType, MutableInt> locPerType = NSiqTarget.this.getSummary().getLocPerType();
				CustomBuildLabel numberOnlyBuildLabel = new CustomBuildLabel(owner.number);
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
				if (locPerType != null) {
					for (Map.Entry<FileType, MutableInt> eachType : locPerType.entrySet()) {
						dsb.add(eachType.getValue().intValue(), eachType.getKey().getDisplayName(), numberOnlyBuildLabel);
					}
				}
				return NSiqUtil.createDistrubutionChart(dsb.build(), "lines");
			}
		};
	}

}
