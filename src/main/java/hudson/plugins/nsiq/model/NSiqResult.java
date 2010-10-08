package hudson.plugins.nsiq.model;

import hudson.plugins.nsiq.NSiqAware;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqResult implements Comparable<NSiqResult>, NSiqAware {
	private FileType eType;
	private String type;
	private String dir = null;
	private String file = null;
	private int complexity;
	private List<Complexity> functions = null;
	private int totalLoc;
	private int codeLoc;

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
	}

	public List<Complexity> getFunctions() {
		return functions;
	}

	public void setFunctions(List<Complexity> functions) {
		this.functions = functions;
	}

	public int getTotalLoc() {
		return totalLoc;
	}

	public void setTotalLoc(int totalLoc) {
		this.totalLoc = totalLoc;
	}

	public int getCodeLoc() {
		return codeLoc;
	}

	public void setCodeLoc(int codeLoc) {
		this.codeLoc = codeLoc;
	}

	public boolean isOnlyLoc() {
		return functions == null || functions.isEmpty();
	}

	private int getCount(int metric) {
		if (isOnlyLoc()) {
			return 0;
		}

		int count = 0;

		for (Complexity complexity : functions) {
			if (complexity.getComplexity() >= metric) {
				count++;
			}
		}

		return count;
	}

	public int getHighCount() {
		return getCount(HIGH);
	}

	public int getLowCount() {
		return getCount(LOW);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	@Override
	public int hashCode() {
		return dir.hashCode() * 31 + file.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}

		if (object instanceof NSiqResult && this.compareTo((NSiqResult) object) == 0) {
			return true;
		}

		return false;
	}

	public int compareTo(NSiqResult nsiqCollector) {
		if (nsiqCollector == null) {
			return -1;
		}

		if (!dir.equals(nsiqCollector.getDir())) {
			return dir.compareTo(nsiqCollector.getDir());
		}

		return file.compareTo(nsiqCollector.getFile());
	}

	public void seteType(FileType eType) {
		this.eType = eType;
	}

	public FileType geteType() {
		if (this.eType == null) {
			this.eType = FileType.getFileType(this.type);
		}
		return eType;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		if (type == null && eType != null) {
			type = eType.getName();
		}
		return type;
	}
}
