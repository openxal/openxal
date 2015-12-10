/**
 * 
 */
package xal.app.machinesimulator;

import xal.ca.Channel;
import xal.smf.AcceleratorNode;

/**
 * @author luxiaohan
 *represents a live diagnostic device which may be connected and monitored.
 */
public class DiagnosticAgent {
	/**the diagnostic node*/
	final private AcceleratorNode NODE;
	/**the monitor for the channel of x plane*/
	final private ChannelMonitor MONITOR_X;
	/**the monitor for the channel of y plane*/
	final private ChannelMonitor MONITOR_Y;
	/**the channel handle 1*/
	final private String HANDLE1;
	/**the channel handle 2*/
	final private String HANDLE2;
	/**the position*/
	final private double POSITION;
	/**check state of this bpm*/
	private Boolean checkState;
	
	public DiagnosticAgent( final AcceleratorNode node , final String handle1, final String handle2 ) {
		NODE = node;
		HANDLE1 = handle1;
		HANDLE2 = handle2;
		MONITOR_X = createMonitor( NODE.getChannel( handle1 ) );
		MONITOR_Y = createMonitor( NODE.getChannel( handle2 ) );
		checkState = true;
		POSITION = NODE.getPosition();
	}
	
	/**create monitor for the specified channel*/
	private ChannelMonitor createMonitor( final Channel channel ) {
		ChannelMonitor channelMonitor = new ChannelMonitor( channel );
		return channelMonitor;
	}
	
	/**get the node*/
	public AcceleratorNode getNode() {
		return NODE;
	}
	
	/**get the check state*/
	public Boolean getCheckState() {
		return checkState;
	}
	
	/**set the check state*/
	public void setCheckState( final Boolean state ) {
		checkState = state;
	}
	
	/**get the position of this node*/
	public double getPosition() {
		return POSITION;
	}
	
	/**get the xAvg handle*/
	public String getXAvgName() {
		return HANDLE1;
	}
	
	/**get the yAvg handle*/
	public String getYAvgName() {
		return HANDLE2;
	}
	
	/**returns average X position*/
	public double getXAvg() {		
		return MONITOR_X.getLatestValue();
	}
	
	/**returns average Y position*/
	public double getYAvg() {
		return MONITOR_Y.getLatestValue();
	}

}
