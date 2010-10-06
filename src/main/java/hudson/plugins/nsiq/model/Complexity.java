package hudson.plugins.nsiq.model;

import hudson.plugins.nsiq.NSiqAware;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class Complexity implements Comparable<Complexity>, NSiqAware {
	private String target;
	private String type;
	private String file = null;
	private String function = null;
	private int complexity;
	private int totalLoc;
	private int codeLoc;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
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

	public boolean isHigh() {
		return complexity >= HIGH;
	}

	public boolean isLow() {
		return complexity >= LOW;
	}

	public int getHighDist() {
		if (isHigh()) {
			return complexity - HIGH;
		}

		return 0;
	}

	public int getNormalDist() {
		if (isHigh()) {
			return HIGH - LOW;
		} else if (isLow()) {
			return complexity - LOW;
		}

		return 0;
	}

	public int getLowDist() {
		if (isLow()) {
			return LOW;
		}

		return complexity;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public int compareTo(Complexity complexity) {
		if (complexity == null) {
			return -1;
		}

		if (!file.equals(complexity.getFile())) {
			return file.compareTo(complexity.getFile());
		}

		return function.compareTo(complexity.getFunction());
	}
}
