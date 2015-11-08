package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.ca.Channel;
import xal.smf.AcceleratorNode;
/**
 * 
 * @author luxiaohan
 *get and set values of the specified property in an accelerator node
 */
public class NodePropertyRecord {
	/**the accelerator node*/
	final private AcceleratorNode NODE;
	/**the name of specified property*/
	final private String PROPERTY_NAME;
	/**the channel array*/
	final private Channel[] CHANNELS;
	/** channel monitor to monitor the value of the channel */
	private List<ChannelMonitor> channelMonitorList;
	/***/
	private Double testValue = null;
	
	/**Constructor*/
	public NodePropertyRecord( final AcceleratorNode node, final String propertyName ) {
		NODE = node;
		PROPERTY_NAME = propertyName;
		CHANNELS = NODE.getLivePropertyChannels( PROPERTY_NAME );
		channelMonitorList = new ArrayList<ChannelMonitor>();
		setupMonitors(CHANNELS);
	}
	
	/**setup monitors*/
	private void setupMonitors( final Channel[] channels ){
		for( Channel channel:channels){
			channelMonitorList.add( new ChannelMonitor( channel ) );
		}
	}
	
	/**get the accelerator node*/
	public String getNodeId(){
		return NODE.getId();
	}
	
	/**get the property name*/
	public String getPropertyName(){
		return PROPERTY_NAME;
	}
	
	/** get the magnet */
	public double getDesignValue(){
		return NODE.getDesignPropertyValue( PROPERTY_NAME );
	}
	
	/**get the live value if use live model and there is one*/
	public double getLiveValue() {
		double liveValue = 0;
		for( ChannelMonitor channelMonitor:channelMonitorList ){
			liveValue += NODE.getLivePropertyValue(PROPERTY_NAME, channelMonitor.getValueList() );
		}
		return liveValue;
	}
	
	/**get the test value which sets by ourself*/
	public Double getTestValue(){
		return testValue;
	}
	
	/**set the test value*/
	public void setTestValue( final Double vule ){
		testValue = vule;
	}	

}
