package xal.app.machinesimulator;

import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
/**
 * 
 * @author luxiaohan
 *get and set values of the specified property in an accelerator node
 */
public class NodePropertyRecord {
	/**the accelerator node*/
	final private AcceleratorNode NODE;
	/**the specified scenario*/
	final private Scenario SCENARIO;
	/**the name of specified property*/
	final private String PROPERTY_NAME;
	/** channel monitor to monitor the value of the channel */
	private ChannelMonitor channelMonitor;
	
	/**Constructor*/
	public NodePropertyRecord( final AcceleratorNode node, final Scenario scenario, final String propertyName ) {
		NODE = node;
		SCENARIO = scenario;
		PROPERTY_NAME = propertyName;
		if( NODE.getLivePropertyChannels( PROPERTY_NAME ).length != 0 ){
			channelMonitor = new ChannelMonitor( NODE.getLivePropertyChannels( PROPERTY_NAME )[0] );
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
		if( NODE.getLivePropertyChannels( PROPERTY_NAME).length != 0 ) {
		liveValue = NODE.getLivePropertyValue( PROPERTY_NAME, channelMonitor.getValueList() ) ;
		}
		return liveValue;
	}
	
	/**get the test value which sets by ourself*/
	public double getTestValue(){
		double testValue;
		if(SCENARIO.getModelInput(NODE, PROPERTY_NAME) == null) {
			testValue = getDesignValue();
		}
		else testValue = SCENARIO.getModelInput( NODE, PROPERTY_NAME ).getDoubleValue();
		return testValue;
	}
	
	/**set the test value*/
	public void setTestValue( final double vule ){
		SCENARIO.setModelInput( NODE, PROPERTY_NAME, vule );
	}	

}
