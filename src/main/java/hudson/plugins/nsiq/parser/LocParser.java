package hudson.plugins.nsiq.parser;

import hudson.FilePath;
import hudson.plugins.nsiq.model.FileType;
import hudson.plugins.nsiq.model.Loc;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.csvreader.CsvReader;

/**
 * 
 * @author iceize at NHN Corporation
 * @version $Rev$, $Date$
 */
public class LocParser {
	private final FilePath file;
	private PrintStream writer = null;

	public LocParser(FilePath file) {
		this.file = file;
	}

	public void setWriter(PrintStream writer) {
		this.writer = writer;
	}

	public List<Loc> parse() throws IOException {
		List<Loc> result = new ArrayList<Loc>();
		CsvReader cr = new CsvReader(file.read(), Charset.defaultCharset());

		cr.skipLine();

		while (cr.readRecord()) {
			String[] vals = cr.getValues();

			Loc loc = new Loc();
			loc.setTarget(vals[0]);
			loc.setType(FileType.getFileType(vals[1]));
			loc.setFile(vals[2]);
			loc.setTotalLoc(Integer.parseInt(vals[3]));
			loc.setCodeLoc(Integer.parseInt(vals[4]));

			if (writer != null) {
				writer.println(loc);
			}

			result.add(loc);
		}

		cr.close();
		return result;
	}
}
