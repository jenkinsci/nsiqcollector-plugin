package hudson.plugins.nsiq;

import hudson.FilePath;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class StringTest {
	private static final String TMPDIR = System.getProperty("java.io.tmpdir");
	private static final File tmpDir = new File(TMPDIR);

	@Before
	public void before() {
		new File(tmpDir, "cppclient").mkdir();
		new File(tmpDir, "library").mkdir();
	}

	@After
	public void after() {
		new File(tmpDir, "cppclient").delete();
		new File(tmpDir, "library").delete();
	}

	@Test
	public void test() {
		FilePath[] moduleRoots = new FilePath[] {new FilePath(new File(tmpDir, "cppclient")), new FilePath(new File(tmpDir, "library"))};
		String[] srcDirs = new String[] {"cppclient/src", "library/src"};

		for (String srcDir : srcDirs) {
			for (FilePath moduleRoot : moduleRoots) {
				int index = srcDir.indexOf(moduleRoot.getName());

				if (index < 0) {
					continue;
				}

				System.out.println("moduleRoot=" + moduleRoot + ", srcDir=" + srcDir);
			}
		}
	}
}
