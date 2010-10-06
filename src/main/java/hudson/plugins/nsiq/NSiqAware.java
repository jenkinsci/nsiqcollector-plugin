package hudson.plugins.nsiq;

import java.io.File;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public interface NSiqAware {
	public static final String SEPARATOR = File.separator;

	/**
	 * 복잡도 기준 값
	 */
	public static final int HIGH = 30;
	public static final int LOW = 10;
}
