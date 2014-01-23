package gov.sns.apps.lossviewer2.views;

public class ViewEvent {
	
	private String command;
	
	private Object source;

    private Object argument=null;

    public ViewEvent(String command, Object source) {
        this(command, source, null);
    }
	public ViewEvent(String command, Object source, String arg) {
		this.command = command;
		this.source = source;
        this.argument=arg;
	}


    public Object getArgument(){
        return argument;
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
