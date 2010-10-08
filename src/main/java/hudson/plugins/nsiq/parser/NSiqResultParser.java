package hudson.plugins.nsiq.parser;

import hudson.plugins.nsiq.NSiqAware;
import hudson.plugins.nsiq.NSiqUtil;
import hudson.plugins.nsiq.model.Complexity;
import hudson.plugins.nsiq.model.FileType;
import hudson.plugins.nsiq.model.Loc;
import hudson.plugins.nsiq.model.NSiqResult;
import hudson.plugins.nsiq.model.NSiqSummary;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqResultParser implements NSiqAware {
	private final List<Loc> locList;
	private final List<Complexity> complexityList;
	private final NSiqSummary summary = new NSiqSummary();
	private final List<NSiqResult> result = new LinkedList<NSiqResult>();

	public NSiqResultParser(List<Loc> locList, List<Complexity> complexityList) {
		this.locList = locList;
		this.complexityList = complexityList;
	}

	/**
	 * 파일 경로에서 디렉토리 명을 리턴한다.
	 */
	private String getDir(String filename, String target) {
		File file = new File(filename);
		String dir = file.getParent();
		return StringUtils.isEmpty(dir) ? "[" + target + "]" : dir;
	}

	/**
	 * 파일 경로에서 파일 명을 리턴한다.
	 */
	private String getFile(String filename) {
		File file = new File(filename);
		return file.getName();
	}

	private int getIndex(String filename, String target) {
		String dir = getDir(filename, target);
		String file = getFile(filename);

		for (int i = 0; i < result.size(); i++) {
			if (dir.equals(result.get(i).getDir()) && file.equals(result.get(i).getFile())) {
				return i;
			}
		}

		return -1;
	}

	private NSiqResult getNSiqResult(Loc loc) {
		NSiqResult nsiq = new NSiqResult();
		nsiq.seteType(loc.getType());
		nsiq.setDir(getDir(loc.getFile(), loc.getTarget()));
		nsiq.setFile(getFile(loc.getFile()));
		nsiq.setTotalLoc(loc.getTotalLoc());
		nsiq.setCodeLoc(loc.getCodeLoc());
		return nsiq;
	}

	public List<NSiqResult> parse() {
		int total = 0;
		int high = 0;
		int low = 0;
		int comp = 0;
		int totalLoc = 0;
		int codeLoc = 0;
		Map<FileType, MutableInt> locPerType = new HashMap<FileType, MutableInt>();

		for (Loc loc : locList) {
			totalLoc += loc.getTotalLoc();
			codeLoc += loc.getCodeLoc();
			result.add(getNSiqResult(loc));
			
			MutableInt eachLocPerType = locPerType.get(loc.getType());
			if (eachLocPerType == null) {
				eachLocPerType = new MutableInt();
				locPerType.put(loc.getType(), eachLocPerType);
			}
			eachLocPerType.add(loc.getCodeLoc());
		}

		for (Complexity complexity : complexityList) {
			int index = getIndex(complexity.getFile(), complexity.getTarget());

			if (index < 0) {
				continue;
			}

			NSiqResult nsiq = result.get(index);
			List<Complexity> functions = nsiq.getFunctions() == null ? new LinkedList<Complexity>() : nsiq.getFunctions();

			if (nsiq.getComplexity() < complexity.getComplexity()) {
				nsiq.setComplexity(complexity.getComplexity());
			}

			total++;

			if (complexity.getComplexity() >= HIGH) {
				high++;
			}

			if (complexity.getComplexity() >= LOW) {
				functions.add(complexity);
				nsiq.setFunctions(functions);
				low++;
			}

			comp += complexity.getComplexity();
		}

		summary.setTotal(total);
		summary.setHigh(high);
		summary.setLow(low);
		summary.setComplexity(comp);
		summary.setTotalLoc(totalLoc);
		summary.setCodeLoc(codeLoc);
		summary.setLocPerType(NSiqUtil.convertLangDistMap(locPerType));
		Collections.sort(result);

		return result;
	}

	public NSiqSummary getSummary() {
		return summary;
	}
}
