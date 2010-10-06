package hudson.plugins.nsiq.model;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public enum Level {
	Directory, File, Function;

	public String getName() {
		return this.toString();
	}
}
