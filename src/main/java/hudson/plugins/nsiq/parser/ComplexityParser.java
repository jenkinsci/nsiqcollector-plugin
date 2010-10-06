package hudson.plugins.nsiq.parser;

import hudson.FilePath;
import hudson.plugins.nsiq.model.Complexity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.csvreader.CsvReader;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class ComplexityParser {
	private final FilePath file;

	public ComplexityParser(FilePath file) {
		this.file = file;
	}
	public List<Complexity> parse() throws IOException {
		List<Complexity> result = new ArrayList<Complexity>();
		CsvReader cr = new CsvReader(file.read(), Charset.defaultCharset());

		cr.skipLine();

		while (cr.readRecord()) {
			String[] vals = cr.getValues();
			Complexity complexity = new Complexity();
			int eachFunctionComplexity = Integer.parseInt(vals[4]);
			complexity.setTarget(vals[0]);
			complexity.setType(vals[1]);
			complexity.setFile(vals[2]);
			complexity.setFunction(vals[3]);
			complexity.setComplexity(eachFunctionComplexity);
			complexity.setTotalLoc(Integer.parseInt(vals[5]));
			complexity.setCodeLoc(Integer.parseInt(vals[6]));
			result.add(complexity);
		}
		Collections.sort(result);

		cr.close();
		return result;
	}
}
