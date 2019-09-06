package lt.visma.javahub.autoqualifier.actions;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

public class ErrorAction extends AbstractAction{

	private Log log;
	private File sourceFile;
	private String message;

	@Override
	public void perform() {
		String msg = "FATAL: "+getMessage()+" in "+sourceFile.getAbsolutePath();
		if (log != null)
			log.error(msg);
		else
			System.err.println(msg);
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

	public ErrorAction setLog(Log log) {
		this.log = log;
		return this;
	}

}
