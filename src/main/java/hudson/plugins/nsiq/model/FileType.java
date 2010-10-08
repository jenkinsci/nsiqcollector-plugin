/*
 * @(#)FileType.java $version $Date$
 * 
 * Copyright 2010 NHN Corp. All rights Reserved.
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package hudson.plugins.nsiq.model;

public enum FileType {
	JAVA("Java", "Java", new String[] { "java" }),
	CCPP("C/C++", "C/C++", new String[] { "cpp", "h", "c", "hxx", "cxx", "hpp" }),
	CSharp("C#", "C#", new String[] { "cs" }),
	ObjectiveC("ObjC", "ObjC", new String[] { "m" }),
	HTML("Html", "HTML", new String[] { "htm", "html" }),
	JSP_PHP("JSP/PHP", "JSP\nPHP", new String[] { "jsp", "php" }),
	JAVASCRIPT_ACTIONSCRIPT("JavaScript/ActionScript", "Java /\nAction\nScript", new String[] { "js", "as" }),
	SHELL("Shell", "Shell", null);

	private final String name;
	private final String[] extensions;

	private final String displayName;

	private FileType(String name, String displayName, String[] extensions) {
		this.name = name;
		this.displayName = displayName;
		this.extensions = extensions;
	}

	public String getFileType() {
		return this.toString();
	}

	public String getName() {
		return name;
	}

	public String[] getExtensions() {
		return extensions;
	}

	public static FileType getFileType(String name) {
		for (FileType fileType : FileType.values()) {
			if (fileType.getName().toLowerCase().equals(name.toLowerCase())) {
				return fileType;
			}
		}

		return null;
	}
	
	public String toString() {
		return name;
	}

	public String getDisplayName() {
		return displayName;
	}
}
