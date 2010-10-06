/**
 * 
 */
package hudson.plugins.nsiq;


class CustomBuildLabel implements Comparable<CustomBuildLabel> {
	private final Integer value;

	public CustomBuildLabel(Integer value) {
		this.value = value;
	}

	public int compareTo(CustomBuildLabel o) {
		return getValue() - o.getValue();
	}

	public String toString() {
		if (value == 0 || value == Integer.MAX_VALUE - 100) {
			return "";
		}
		return "#" + getValue().toString();
	}

	public Integer getValue() {
		return value;
	}

}