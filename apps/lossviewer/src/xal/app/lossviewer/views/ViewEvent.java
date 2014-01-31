package xal.app.lossviewer.views;

import xal.app.lossviewer.LossDetector;

public class ViewEvent {
	
	private String command;
	
	private View<LossDetector> source;

    private Object argument=null;

    public ViewEvent(String command, View<LossDetector> source) {
        this(command, source, null);
    }
	public ViewEvent(String command, View<LossDetector> source, String arg) {
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
	public View<LossDetector> getSource() {
		return source;
	}
}
