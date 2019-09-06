package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

public class WarningAction extends AbstractAction{

	private File sourceFile;
	private String message;

	@Override
	public void perform() {
		System.err.println("WARNING: "+getMessage()+" in "+sourceFile.getAbsolutePath());
	}

	public WarningAction setSourceFile(File componentFile) {
		this.sourceFile = componentFile;
		return this;
	}

	public WarningAction setMessage(String msg) {
		this.message = msg;
		return this;
	}

	public String getMessage() {
		return message;
	}

}
