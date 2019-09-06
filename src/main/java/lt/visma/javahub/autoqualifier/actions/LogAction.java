package lt.visma.javahub.autoqualifier.actions;

public class LogAction extends AbstractAction{

	private String message = "no message";
	
	public LogAction(String msg) {
		this.message = msg;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public void perform() throws Exception {
		System.out.println(message);
	}

}
