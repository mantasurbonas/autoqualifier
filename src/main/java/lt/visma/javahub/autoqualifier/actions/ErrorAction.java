package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

public class ErrorAction extends AbstractAction{

	private File sourceFile;
	private String message;

	@Override
	public void perform() {
		System.err.println("FATAL: "+getMessage()+" in "+sourceFile.getAbsolutePath());
	}

	public ErrorAction setSourceFile(File componentFile) {
		this.sourceFile = componentFile;
		return this;
	}

	public ErrorAction setMessage(String msg) {
		this.message = msg;
		return this;
	}

	public String getMessage() {
		return message;
	}

}
