package lt.visma.javahub.autoqualifier.model;

import java.io.File;

public class PropertyAnnotationLocation {
	private File file;
	private String propertyClass;
	private String annotationName;
	private int line;
	private int column;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getPropertyClass() {
		return propertyClass;
	}

	public void setPropertyClass(String propertyClass) {
		this.propertyClass = propertyClass;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public String toString() {
		return "@"+annotationName + " " + propertyClass + " at " + line + ":" + column + " in " + file;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}
}