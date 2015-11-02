package xal.app.machinesimulator;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.RfCavity;
import xal.smf.proxy.RfCavityPropertyAccessor;
/**
 * 
 * @author luxiaohan
 *get and set values of the specified property in an accelerator node
 */
public class AcceleratorNodeRecord {
	/**the accelerator node*/
	final private AcceleratorNode NODE;
	/**the specified scenario*/
	final private Scenario SCENARIO;
	final private String PROPERTY_NAME;
	/**magnet*/
	private Electromagnet magnet;
	/**rf cavity*/
	private RfCavity 	rfCavity;
	
	/**Constructor*/
	public AcceleratorNodeRecord( final AcceleratorNode node, final Scenario scenario, final String propertyName) {
		NODE = node;
		SCENARIO = scenario;
		PROPERTY_NAME = propertyName;
		if( node instanceof Electromagnet) magnet = (Electromagnet)node;
		if( node instanceof RfCavity ) rfCavity = (RfCavity)node;
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
		return NODE.getDesignPropertyValue(PROPERTY_NAME);
	}
	
	/**get the live value if use live model and there is one*/
	public double getLiveValue() throws ConnectionException, GetException{
		double liveValue = 0;
		if( SCENARIO.getSynchronizationMode().equals(Scenario.SYNC_MODE_LIVE) ){
			if( NODE instanceof Electromagnet ) liveValue = magnet.getField();
			if( NODE instanceof RfCavity ) {
				if( PROPERTY_NAME.equals(RfCavityPropertyAccessor.PROPERTY_AMPLITUDE ) ) liveValue = rfCavity.getCavAmpAvg();
				if( PROPERTY_NAME.equals(RfCavityPropertyAccessor.PROPERTY_PHASE ) ) liveValue = rfCavity.getCavPhaseAvg();
			}
		}
		return liveValue;
	}
	
	/**get the test value which sets by ourself*/
	public double getTestValue(){
		double testValue;
		if(SCENARIO.getModelInput(NODE, PROPERTY_NAME) == null) {
			testValue = getDesignValue();
		}
		else testValue = SCENARIO.getModelInput(NODE, PROPERTY_NAME).getDoubleValue();
		return testValue;
	}
	
	/**set the test value*/
	public void setTestValue( final double vule ){
		SCENARIO.setModelInput(NODE, PROPERTY_NAME, vule);
	}	

}
