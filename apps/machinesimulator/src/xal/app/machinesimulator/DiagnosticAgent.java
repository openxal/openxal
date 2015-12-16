/**
 * 
 */
package xal.app.machinesimulator;
import java.util.Vector;

import xal.ca.Channel;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

/**
 * @author luxiaohan
 *represents a live diagnostic device which may be connected and monitored.
 */
public class DiagnosticAgent {
	/**the sequence which is this node belongs to*/
	final private AcceleratorSeq SEQUENCE;
	/**the diagnostic node*/
	final private AcceleratorNode NODE;
	/**the monitors*/
	final private ChannelMonitor[] MONITORS;
	/**the channel handles*/
	final private Vector<String> HANDLE;
	/**the position of this node in the sequence*/
	final private double POSITION;
	/**check state of this diagnostic node*/
	private Boolean checkState;
	
	public DiagnosticAgent(  final AcceleratorSeq seq, final AcceleratorNode node ,
			final String xHandle, final String yHandle, final String zHandle ) {
		NODE = node;
		SEQUENCE = seq;
		HANDLE = new Vector<String>(3);
		HANDLE.add( xHandle );
		HANDLE.add( yHandle );
		HANDLE.add( zHandle );
		MONITORS = createMonitor( getChannel( HANDLE.toArray( new String[3] ) ) );
		checkState = true;
		POSITION = SEQUENCE.getPosition( NODE );
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
	private ChannelMonitor[] createMonitor( final Channel[] channels ) {
		ChannelMonitor[] channelMonitors = new ChannelMonitor[3];
		for( int channelIndex = 0; channelIndex < channels.length; channelIndex++ ){
			if ( channels[channelIndex] != null && channels[channelIndex].isValid() ) {
				channelMonitors[channelIndex] = new ChannelMonitor( channels[channelIndex] );
			}
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
	public Vector<String> getNames() {
		return HANDLE;
	}
	
	/**get the name of x plane*/
	public String getNameX() {
		return HANDLE.get(0);
	}
	/**get the name of y plane*/	
	public String getNameY() {
		return HANDLE.get(1);
	}
	/**get the name of z plane*/	
	public String getNameZ() {
		return HANDLE.get(2);
	}
 	/**get values of all the channels,divide 1000 to convert mm to meters，the same below*/
	public Vector<Double> getValues() {
		Vector<Double> values = new Vector<Double>( HANDLE.size() );
		for ( int index = 0; index < HANDLE.size(); index++ ){
			if ( MONITORS[index] == null ) values.add( Double.NaN );
			else values.add( MONITORS[index].getLatestValue()/1000 );
		}
		return values;
	}
	
	/**get the value of x plane*/
	public Double getValueX() {
		return ( MONITORS[0] == null ) ? Double.NaN : MONITORS[0].getLatestValue()/1000;
	}
	
	/**get the value of y plane*/
	public Double getValueY() {
		return ( MONITORS[1] == null ) ? Double.NaN : MONITORS[1].getLatestValue()/1000;
	}
	
	/**get the value of z plane*/
	public Double getValueZ() {
		return ( MONITORS[2] == null ) ? Double.NaN : MONITORS[2].getLatestValue()/1000;
	}

}
