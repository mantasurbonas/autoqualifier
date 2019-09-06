package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

import com.github.javaparser.Position;

public class SetBeanNameAction extends AbstractAction {

	private String annotationName;
	private String beanName;
	private File sourceFile;
	private Position position;

	@Override
	public void perform() throws Exception {
		String newAnnotation = "@" + annotationName;

		if (getBeanName() != null && !getBeanName().trim().isEmpty())
			newAnnotation += "(\"" + getBeanName() + "\")";
		
		getFile(getSourceFile())
			.replaceLine(position.line-1, newAnnotation);
	}

	public SetBeanNameAction setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
		return this;
	}

	public SetBeanNameAction setBeanName(String str) {
		this.beanName = str;
		return this;
	}

	public SetBeanNameAction setSourceFile(File f) {
		this.sourceFile = f;
		return this;
	}

	public SetBeanNameAction setPosition(Position position) {
		this.position = position;
		return this;
	}

	public String getBeanName() {
		return beanName;
	}

	public File getSourceFile() {
		return sourceFile;
	}

	public Position getPosition() {
		return position;
	}
}
