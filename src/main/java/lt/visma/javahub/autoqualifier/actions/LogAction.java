package lt.visma.javahub.autoqualifier.actions;

import org.apache.maven.plugin.logging.Log;

public class LogAction extends AbstractAction{

	private Log log;
	private String message = "no message";
	
	public LogAction(String msg) {
		this.message = msg;
	}

	public LogAction setLog(Log log) {
		this.log = log;
		return this;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public void perform() throws Exception {
		if (log != null)
			log.info(message);
		else
			System.out.println(message);
	}

}
