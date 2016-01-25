/**
 * 
 */
package xal.app.machinesimulator;

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
	final private String[] HANDLE;
	/**the position of this node in the sequence*/
	final private double POSITION;
	/**the vector of (x,y,z) scales*/
	final private double[] SCALES;
	/**check state of this diagnostic node*/
	private Boolean checkState;
	
	public DiagnosticAgent(  final AcceleratorSeq seq, final AcceleratorNode node ,
			final String xHandle, final String yHandle, final String zHandle ) {
		NODE = node;
		SEQUENCE = seq;
		HANDLE = new String[]{xHandle, yHandle, zHandle};
		SCALES = new double[]{ 1.0, 1.0, 1.0 };
		MONITORS = createMonitor( getChannel( HANDLE ) );
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
	
	/**set scales for three planes with common value*/
	public void setCommonScale( final double scale ) {
		SCALES[0] = scale;
		SCALES[1] = scale;
		SCALES[2] = scale;
	}
	
	/**set scales for three planes with different values*/
	public void setScales( final double xScale, final double yScale, final double zScale) {
		SCALES[0] = xScale;
		SCALES[1] = yScale;
		SCALES[2] = zScale;
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
	
	/**get the name of x plane*/
	public String getNameX() {
		return HANDLE[0];
	}
	/**get the name of y plane*/	
	public String getNameY() {
		return HANDLE[1];
	}
	/**get the name of z plane*/	
	public String getNameZ() {
		return HANDLE[2];
	}
 	/**get values of all the channels,divide 1000 to convert mm to meters, the same below*/
	public double[] getValues() {
		double[] values = new double[ HANDLE.length ];
		for ( int index = 0; index < HANDLE.length; index++ ){
			if ( MONITORS[index] == null ) values[index] = Double.NaN;
			else values[index] =  MONITORS[index].getLatestValue()*SCALES[index];
		}
		return values;
	}
	
	/**get the value of x plane*/
	public double getValueX() {
		return ( MONITORS[0] == null ) ? Double.NaN : MONITORS[0].getLatestValue()*SCALES[0];
	}
	
	/**get the value of y plane*/
	public double getValueY() {
		return ( MONITORS[1] == null ) ? Double.NaN : MONITORS[1].getLatestValue()*SCALES[1];
	}
	
	/**get the value of z plane*/
	public double getValueZ() {
		return ( MONITORS[2] == null ) ? Double.NaN : MONITORS[2].getLatestValue()*SCALES[2];
	}

}
