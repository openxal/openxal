package xal.app.lossviewer.signals;

public class SignalEvent {
	private String command;
	
	private Object source;
	
	public SignalEvent(String command, Object source) {
		this.command = command;
		this.source = source;
	}
	
	
	/**
	 * Returns Command
	 *
	 * @return    a  String
	 */
	public String getCommand() {
		return command;
	}
	
	
	/**
	 * Returns Source
	 *
	 * @return    an Object
	 */
	public Object getSource() {
		return source;
	}
}
