package hudson.plugins.nsiq;

import hudson.FilePath;

import java.io.IOException;

public class NSiqSrc {

	private final FilePath srcFile;

	public NSiqSrc(FilePath srcFile) {
		this.srcFile = srcFile;
	}

	public FilePath getSrcFile() {
		return srcFile;
	}
	
	public String getFileType() {
		if (srcFile == null) {
			return "plain";
		} else if (srcFile.getName().endsWith(".java")) {
			return "java";
		} else {
			return "cpp";
		}
	}
	
	public String getFileContent(Encoding encoding) throws IOException {
		if (srcFile == null){
			return "No file is avaliable";
		}
		return new SourcePainter().paint(srcFile, encoding);		 
	}
	
	public String getBaseURL() {
		return NSiqUtil.getHudsonBaseURL();
	}
}
