package lt.visma.javahub.autoqualifier.model;

import java.io.File;

import com.github.javaparser.Position;

public class ClassAnnotationLocation {
	private File file;
	private String shortClassName;
	private String fullClassName;
	private String annotationName;
	private String annotationValue;
	private Position position;

	public File getFile() {
		return file;
	}

	public ClassAnnotationLocation setFile(File file) {
		this.file = file;
		return this;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	public ClassAnnotationLocation setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
		return this;
	}

	public String getAnnotationValue() {
		return annotationValue;
	}

	public ClassAnnotationLocation setAnnotationValue(String annotationValue) {
		this.annotationValue = annotationValue;
		return this;
	}
	
	public Position getPosition() {
		return position;
	}

	public ClassAnnotationLocation setPosition(Position where) {
		this.position = where;
		return this;
	}

	public String getShortClassName() {
		return shortClassName;
	}

	public ClassAnnotationLocation setShortClassName(String shortClassName) {
		this.shortClassName = shortClassName;
		return this;
	}

	public String getFullClassName() {
		return fullClassName;
	}

	public ClassAnnotationLocation setFullClassName(String fullClassName) {
		this.fullClassName = fullClassName;
		return this;
	}

	public String toString() {
		return "@"+annotationName+" "+fullClassName+ " at "+position+" in "+file;
	}
}