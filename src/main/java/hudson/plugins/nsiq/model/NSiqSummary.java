package hudson.plugins.nsiq.model;

import java.util.Map;

import hudson.plugins.nsiq.NSiqUtil;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.lang.mutable.MutableInt;

/**
 * 
 * @author iceize at NHN Corporation
 * @author JunHo Yoon at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqSummary {
	private int total;
	private int high;
	private int low;
	private int complexity;
	private int totalLoc;
	private int codeLoc;
	private Map<FileType, MutableInt> locPerType;

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getHigh() {
		return high;
	}

	public void setHigh(int high) {
		this.high = high;
	}

	public int getLow() {
		return low;
	}

	public void setLow(int low) {
		this.low = low;
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

	public int getMaxComplexity() {
		return complexity;
	}

	public String getAverageComplexity() {
		return NSiqUtil.getFormattedComplexity(complexity / (total == 0 ? 1 : total));
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public void setLocPerType(Map<FileType, MutableInt> locPerType) {
		this.locPerType = locPerType;
	}

	public Map<FileType, MutableInt> getLocPerType() {
		return this.locPerType;
	}
	
	public boolean isLocPerTypeAvailable() {
		return (locPerType != null && this.locPerType.size() > 0);
	}
}
