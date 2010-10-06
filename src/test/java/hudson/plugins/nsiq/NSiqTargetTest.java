package hudson.plugins.nsiq;

import hudson.FilePath;
import hudson.plugins.nsiq.model.Complexity;
import hudson.plugins.nsiq.model.Loc;
import hudson.plugins.nsiq.model.NSiqResult;
import hudson.plugins.nsiq.parser.ComplexityParser;
import hudson.plugins.nsiq.parser.LocParser;
import hudson.plugins.nsiq.parser.NSiqResultParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqTargetTest {
	private FilePath getFilename(String filename) {
		return new FilePath(new File(this.getClass().getClassLoader().getResource(filename).getPath()));
	}

	private String join(String[] array, String delimiter) {
		StringBuffer sb = new StringBuffer();

		for (String a : array) {
			sb.append(a).append(delimiter);
		}

		String result = sb.toString();

		if (StringUtils.isEmpty(result)) {
			return result;
		}

		return result.substring(0, result.length() - delimiter.length());
	}

	@Test
	public void test() throws IOException {
		LocParser locParser = new LocParser(getFilename("loc.csv"));
//		locParser.setWriter(System.out);
		List<Loc> locList = locParser.parse();

		ComplexityParser complexityParser = new ComplexityParser(getFilename("complexity.csv"));
//		complexityParser.setWriter(System.out);
		List<Complexity> complexityList = complexityParser.parse();

		NSiqResultParser parser = new NSiqResultParser(locList, complexityList);
		List<NSiqResult> parseResult = parser.parse();

		NSiqTarget target = NSiqTarget.load(null, parseResult);
		System.out.println("summary: " + target.getSummary());

		for (NSiqResult result : target.getChild(join(new String[] {"com", "nhncorp", "lucy", "bloc", "container"}, File.separator)).getNSiqResultList()) {
			System.out.println(result);
		}
	}
}
