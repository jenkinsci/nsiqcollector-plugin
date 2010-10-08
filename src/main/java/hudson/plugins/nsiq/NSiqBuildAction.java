package hudson.plugins.nsiq;

import hudson.model.Build;
import hudson.model.HealthReport;
import hudson.model.HealthReportingAction;
import hudson.plugins.nsiq.model.FileType;
import hudson.plugins.nsiq.model.NSiqSummary;
import hudson.tasks.Builder;
import hudson.util.DataSetBuilder;
import hudson.util.Graph;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jfree.chart.JFreeChart;
import org.kohsuke.stapler.StaplerProxy;

/**
 * BuildAction은 Publisher의 perform 메소드 안에서 호출되며, 각 빌드마다의 정보를 저장한다.
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public final class NSiqBuildAction implements HealthReportingAction, StaplerProxy, NSiqAware {
	private static Logger logger = Logger.getLogger(NSiqBuildAction.class.getName());
	public final Build<?, ?> owner;
	private final double lowRatio;
	private final double highRatio;
	private final NSiqSummary summary;
	private transient WeakReference<NSiqTarget> target;
	private HealthReport healthReport = null;

	/**
	 * <b>생성자</b><br/>
	 * Build 및 N'SIQ Collector 결과와 health 설정에 대한 metric 정보를 파라미터로 받는다.
	 * 
	 * @param build
	 *            {@link Build}
	 * @param summary
	 *            N'SIQ Collector 요약
	 * @param target
	 *            N'SIQ Collector 결과
	 * @param lowRatio
	 * @param highRatio
	 */
	private NSiqBuildAction(Build<?, ?> build, NSiqSummary summary, NSiqTarget target, double lowRatio, double highRatio) {
		this.owner = build;
		this.summary = summary;
		this.target = new WeakReference<NSiqTarget>(target);
		this.lowRatio = lowRatio;
		this.highRatio = highRatio;
	}

	public static NSiqBuildAction load(Build<?, ?> build, NSiqSummary summary, NSiqTarget target, double lowRatio, double highRatio) {
		return new NSiqBuildAction(build, summary, target, lowRatio, highRatio);
	}

	private NSiqPublisher getPublisher() {
		return (NSiqPublisher) owner.getProject().getPublisher(NSiqPublisher.DESCRIPTOR);
	}

	private NSiqBuilder getBuilder() {
		for (Builder p : owner.getProject().getBuilders()) {
			if (p.getDescriptor() == NSiqBuilder.DESCRIPTOR)
				return (NSiqBuilder) p;
		}
		return null;
	}

	public String getSrc() {
		return getBuilder().getSrcDir();
	}

	public boolean isDailyView() {
		return getPublisher().isDailyView();
	}

	private double getLowValue() {
		return summary.getLow() * 100.0 / (summary.getTotal() == 0 ? 1 : summary.getTotal());
	}

	private double getHighValue() {
		return summary.getHigh() * 100.0 / (summary.getTotal() == 0 ? 1 : summary.getTotal());
	}

	public String getLowRatio() {
		return NSiqUtil.getFormattedComplexity(getLowValue());
	}

	public String getHighRatio() {
		return NSiqUtil.getFormattedComplexity(getHighValue());
	}

	private int getPercent(double value, double configured) {
		int percent = 100;

		if (Double.compare(value, configured) >= 0) {
			for (int i = 5; i > 0; i--) {
				if (value > configured * i) {
					percent = (5 - i) * 15;
					break;
				}
			}
		}

		return percent;
	}

	private int getPercent() {
		int lowPercent = getPercent(getLowValue(), lowRatio);
		int highPercent = getPercent(getHighValue(), highRatio);

		return lowPercent < highPercent ? lowPercent : highPercent;
	}

	/**
	 * 날씨 정보를 표시<br/> {@link HealthReport}는 두 개의 파라미터로 생성할 수 있는데, 첫번째 파라미터는
	 * minValue이며, 두번째 파라미터는 minValue에 대한 설명이다.<br/>
	 * minValue는 정수 값이며, 값의 크기에 따라 다른 날씨가 표시된다. 예를 들어, 80 이상의 값이면 <u>맑음</u>, 60
	 * 이상의 값이면 <u>구름 낀 맑음</u>, 40 이상의 값이면 <u>구름</u>, 20 이상의 값이면 <u>비</u>, 그 이하의
	 * 값이면 <u>번개</u>이다.<br/>
	 * <br/>
	 * Hudson의 프로젝트 대시보드에서의 날씨 정보는, 여러 개의 플러그인의 Health Report에서 가장 최소값을 기준으로 한다.
	 * 즉 어떤 프로젝트가 A 플러그인에서는 minValue가 80이고, B 플러그인에서는 50이라면, 날씨는 <b>구름</b>으로
	 * 나타난다.<br/>
	 * 만약 null을 리턴하면, 아무런 정보도 표시하지 않는다.<br/>
	 * <br/>
	 * 여기에서는 플러그인 설정에서 지정된 값보다 작으면 100%, 2배 이하이면 60%, 3배 이하이면 45%, 4배 이하이면 30%,
	 * 5배 이하이면 15%, 5배 이상이면 0%로 표시된다.<br/>
	 * 예를 들어, 기준 값이 1.0이고, 실제 complexity 값이 3.5라면 3배~4배 사이의 값이므로 30%(<u>비</u>)로
	 * 나타난다.
	 */
	@SuppressWarnings("deprecation")
	public HealthReport getBuildHealth() {
		if (healthReport != null) {
			return healthReport;
		}

		StringBuffer sb = new StringBuffer(Constant.DISPLAY_NAME + ": ");
		sb.append("over " + LOW + ": ");
		sb.append(getLowRatio());
		sb.append("(" + "metric=" + NSiqUtil.getFormattedComplexity(lowRatio) + ")");
		sb.append(", ");
		sb.append("over " + HIGH + ": ");
		sb.append(getHighRatio());
		sb.append("(" + "metric=" + NSiqUtil.getFormattedComplexity(highRatio) + ")");

		healthReport = new HealthReport(getPercent(), sb.toString());

		return healthReport;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 나타나는 플러그인의 이름을 리턴한다.
	 */
	public String getDisplayName() {
		return Constant.DISPLAY_NAME;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 나타나는 아이콘의 이름을 리턴한다.
	 */
	public String getIconFileName() {
		return Constant.ICON_FILENAME;
	}

	/**
	 * summary 페이지 (Build History를 클릭했을 때 나오는 페이지)에서 각 빌드로 연결되는 URL을 리턴한다.
	 */
	public String getUrlName() {
		return Constant.URL;
	}

	/**
	 * {@link NSiqBuildAction}의 N'SIQ Collector 결과를 리턴한다.
	 * 
	 * @return N'SIQ Collector 결과
	 */
	public NSiqTarget getTarget() {
		if (this.target != null) {
			NSiqTarget nsiqTarget = this.target.get();

			if (nsiqTarget != null) {
				return nsiqTarget;
			}
		}

		NSiqTarget nsiqTarget = null;

		synchronized (this) {
			try {
				nsiqTarget = (NSiqTarget) NSiqUtil.getDataFile(owner).read();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.toString());
			}

			if (nsiqTarget != null) {
				nsiqTarget.setOwner(owner);
				
				for (NSiqTarget file : nsiqTarget.getChildren().values()) {
					file.setOwner(owner);
					file.setParent(nsiqTarget);
					for (NSiqTarget function : file.getChildren().values()) {
						function.setOwner(owner);
						function.setParent(file);
					}
				}
				target = new WeakReference<NSiqTarget>(nsiqTarget);
				return nsiqTarget;
			} else {
				return null;
			}
		}
	}

	/**
	 * N'SIQ Collector 의 결과 요약 정보를 리턴한다.
	 */
	public NSiqSummary getSummary() {
		return summary;
	}

	/**
	 * 그래프(차트)를 표시하기 위해 이전 Build 정보를 리턴한다.
	 * 
	 * @return 이전 Build 정보를 갖는 {@link NSiqBuildAction} 인스턴스
	 */
	public NSiqBuildAction getPreviousResult() {
		return getPreviousResult(owner);
	}

	public boolean isOverView() {
		return getPublisher().isOverView();
	}

	/**
	 * 그래프(차트)를 표시하기 위해 build 파라미터의 이전 Build 정보를 리턴한다.
	 * 
	 * @param owner
	 *            {@link Build} 인스턴스
	 * @return 이전 Build 정보를 갖는 {@link NSiqBuildAction} 인스턴스
	 */
	static NSiqBuildAction getPreviousResult(Build<?, ?> owner) {
		Build<?, ?> build = owner;

		while (true) {
			build = build.getPreviousNotFailedBuild();

			if (build == null) {
				return null;
			}

			NSiqBuildAction action = build.getAction(NSiqBuildAction.class);

			if (action != null) {
				return action;
			}
		}
	}

	public long getTimestamp() {
		if (Constant.DISABLE_IMAGE_CACHE) {
			return 0;
		}
		return owner.getTimestamp().getTimeInMillis();
	}

	public Graph getLocDistGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {
				Map<FileType, Integer> locPerType = NSiqBuildAction.this.getSummary().getLocPerType();
				CustomBuildLabel numberOnlyBuildLabel = new CustomBuildLabel(owner.number);
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
//				dsb.add(0, "", new CustomBuildLabel(0));
				if (locPerType != null) {
					for (Map.Entry<FileType, Integer> eachType : locPerType.entrySet()) {
						dsb.add(eachType.getValue().intValue(), eachType.getKey().getDisplayName(), numberOnlyBuildLabel);
					}
				}
//				dsb.add(0, "", new CustomBuildLabel(Integer.MAX_VALUE - 100));

				return NSiqUtil.createDistrubutionChart(dsb.build(), "lines");
			}
		};
	}

	public Graph getLocGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {
				int lower = Integer.MAX_VALUE;
				int upper = Integer.MIN_VALUE;
				String previous = "";
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
				boolean dailyView = isDailyView();
				for (NSiqBuildAction build = NSiqBuildAction.this; build != null; build = build.getPreviousResult()) {
					Comparable<?> label = dailyView ? new BuildDateLabel(build.owner) : new NumberOnlyBuildLabel(build.owner);
					if (dailyView) {
						String date = ((BuildDateLabel) label).getDate();
						if (previous.equals(date)) {
							continue;
						}
						previous = date;
					}
					NSiqSummary nsiqSummary = build.getSummary();
					lower = Math.min(nsiqSummary.getCodeLoc(), lower);
					upper = Math.max(nsiqSummary.getTotalLoc(), upper);
					dsb.add(nsiqSummary.getTotalLoc(), "Total LOC", label);
					dsb.add(nsiqSummary.getCodeLoc(), "Code LOC", label);
				}
				return NSiqUtil.createLineChart(dsb.build(), "lines", lower, upper);
			}
		};
	}

	public Graph getComplexityGraph() {
		return new Graph(getTimestamp(), 500, 200) {
			@Override
			protected JFreeChart createGraph() {
				String previous = "";
				DataSetBuilder<String, Comparable<?>> dsb = new DataSetBuilder<String, Comparable<?>>();
				boolean dailyView = isDailyView();
				boolean overView = isOverView();
				for (NSiqBuildAction build = NSiqBuildAction.this; build != null; build = build.getPreviousResult()) {
					Comparable<?> label = dailyView ? new BuildDateLabel(build.owner) : new NumberOnlyBuildLabel(build.owner);
					if (dailyView) {
						String date = ((BuildDateLabel) label).getDate();
						if (previous.equals(date)) {
							continue;
						}
						previous = date;
					}
					NSiqSummary nsiqSummary = build.getSummary();
					dsb.add(nsiqSummary.getHigh(), "over " + HIGH, label);
					if (overView) {
						dsb.add(nsiqSummary.getLow() - nsiqSummary.getHigh(), "over " + LOW, label);
					}
				}
				return NSiqUtil.createStackChart(dsb.build(), "count");
			}
		};
	}
}
