package hudson.plugins.nsiq.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class Loc {
	private String target;
	private FileType type;
	private String file = null;
	private int totalLoc;
	private int codeLoc;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public FileType getType() {
		return type;
	}

	public void setType(FileType type) {
		this.type = type;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
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

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}
}
