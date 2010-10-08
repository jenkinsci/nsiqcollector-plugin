package hudson.plugins.nsiq;

import hudson.FilePath;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class NSiqTargetTest {
	private FilePath getFilename(String filename) throws IOException {
		ClassPathResource resource = new ClassPathResource(filename);
		return new FilePath(resource.getFile());
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
//		LocParser locParser = new LocParser(getFilename("loc.csv"));
//		List<Loc> locList = locParser.parse();
//
//		ComplexityParser complexityParser = new ComplexityParser(getFilename("complexity.csv"));
//		List<Complexity> complexityList = complexityParser.parse();
//
//		NSiqResultParser parser = new NSiqResultParser(locList, complexityList);
//		List<NSiqResult> parseResult = parser.parse();
//
//		NSiqTarget target = NSiqTarget.load(null, parseResult);
//		System.out.println("summary: " + target.getSummary());
//
//		for (NSiqTarget result : target.getChild(join(new String[] { "com", "nhncorp", "lucy", "bloc", "container" }, File.separator))
//				.getChildrenList()) {
//			System.out.println(result);
//		}
	}
}
