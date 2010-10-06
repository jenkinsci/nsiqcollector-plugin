package hudson.plugins.nsiq;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import hudson.model.Build;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public final class BuildDateLabel implements Comparable<BuildDateLabel> {
	public final Build<?, ?> build;

	public BuildDateLabel(Build<?, ?> build) {
		this.build = build;
	}

	private String getDate(Calendar calendar) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd");
		return simpleDateFormat.format(calendar.getTime());
	}

	public String getDate() {
		return getDate(build.getTimestamp());
	}

	public int compareTo(BuildDateLabel buildDateLabel) {
		return this.build.number - buildDateLabel.build.number;
	}

	public boolean equals(Object object) {
		if (!(object instanceof BuildDateLabel)) {
			return false;
		}

		BuildDateLabel buildDateLabel = (BuildDateLabel) object;
		return build == buildDateLabel.build;
	}

	public int hashCode() {
		return build.hashCode();
	}

	public String toString() {
		return getDate();
	}
}
