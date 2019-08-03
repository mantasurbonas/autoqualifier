package lt.visma.javahub.autoqualifier.model;

import java.io.File;

public class ClassAnnotationLocation {
	private File file;
	private String shortClassName;
	private String fullClassName;
	private String annotationName;
	private int line;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String toString() {
		return "@"+annotationName+" "+fullClassName+ " at "+line+" in "+file;
	}

	public String getShortClassName() {
		return shortClassName;
	}

	public void setShortClassName(String shortClassName) {
		this.shortClassName = shortClassName;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public void setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
	}
}