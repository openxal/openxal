/**
 * 
 */
package xal.app.machinesimulator;

import xal.ca.Channel;
import xal.smf.impl.BPM;

/**
 * @author luxiaohan
 *represents a live BPM which may be connected and monitored.
 */
public class DiagnosticAgent {
	/**the bpm node*/
	final private BPM NODE;
	/**the monitor for the channel of x plane*/
	final private ChannelMonitor MONITOR_X;
	/**the monitor for the channel of y plane*/
	final private ChannelMonitor MONITOR_Y;
	/**the position*/
	final private double POSITION;
	/**check state of this bpm*/
	private Boolean checkState;
	
	public DiagnosticAgent( final BPM node ) {
		NODE = node;
		Channel channelX = NODE.getChannel( BPM.X_AVG_HANDLE );
		MONITOR_X = createMonitor( channelX );
		Channel channelY = NODE.getChannel( BPM.Y_AVG_HANDLE );
		MONITOR_Y = createMonitor( channelY );
		checkState = true;
		POSITION = NODE.getPosition();
	}
	
	/**create monitor for the specified channel*/
	private ChannelMonitor createMonitor( final Channel channel ) {
		ChannelMonitor channelMonitor = new ChannelMonitor( channel );
		return channelMonitor;
	}
	
	/**get the BPM node*/
	public BPM getNode() {
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
		return BPM.X_AVG_HANDLE;
	}
	
	/**get the yAvg handle*/
	public String getYAvgName() {
		return BPM.Y_AVG_HANDLE;
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
