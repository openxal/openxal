/**
 * 
 */
package xal.app.machinesimulator;

import java.util.HashMap;
import java.util.Map;

import xal.ca.Channel;
import xal.smf.AcceleratorNode;

/**
 * @author luxiaohan
 *represents a live diagnostic device which may be connected and monitored.
 */
public class DiagnosticAgent {
	/**the diagnostic node*/
	final private AcceleratorNode NODE;
	/**the monitors map with channel handle*/
	final private Map<String, ChannelMonitor> MONITORS;
	/**the channel handle*/
	final private String[] HANDLE;
	/**the position of this node in the sequence*/
	final private double POSITION;
	/**check state of this diagnostic node*/
	private Boolean checkState;
	
	public DiagnosticAgent( final AcceleratorNode node , final String... handle ) {
		NODE = node;
		HANDLE = handle;
		MONITORS = createMonitor( getChannel( handle ) );
		checkState = true;
		POSITION = NODE.getPosition();
	}
	
	/**get the channels*/
	private Channel[] getChannel( final String[] handles ) {
		Channel[] channels = new Channel[handles.length];
		for ( int index = 0; index < handles.length; index++ ){
			channels[index] = NODE.findChannel( handles[index] );			
		}
		return channels;
	}
	
	/**create monitors for the specified channels*/
	private Map<String, ChannelMonitor> createMonitor( final Channel[] channels ) {
		Map<String, ChannelMonitor> channelMonitors = new HashMap<String, ChannelMonitor>();
		for( int channelIndex = 0; channelIndex < channels.length; channelIndex++ ){
			if ( channels[channelIndex] == null && !channels[channelIndex].isValid() ) channelMonitors.put( HANDLE[channelIndex], null );
			else channelMonitors.put( HANDLE[channelIndex], new ChannelMonitor( channels[channelIndex] ) );
		}
		return channelMonitors;
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
	
	/**get all the values' names which represented by the handle*/
	public String[] getNames() {
		return HANDLE;
	}
	
	/**get values of all the channels*/
	public Double[] getValues() {
		Double[] values = new Double[HANDLE.length];
		for ( int index = 0; index < values.length; index++ ){
			if ( MONITORS.get( HANDLE[index] ) == null ) values[index] = Double.NaN;
			else values[index] = MONITORS.get( HANDLE[index] ).getLatestValue();
		}
		return values;
	}

}
