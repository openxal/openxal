package xal.app.machinesimulator;

import xal.ca.Channel;
import xal.sim.scenario.ModelInput;
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
	/**modelinput variable to set test value to scenario*/
	final private ModelInput MODEL_INPUT;
	/** channel monitor to monitor the value of the channel */
	final private ChannelMonitor[] CHANNEL_MONITORS;
	/**check-state of scanning*/
	private boolean checkState;
	/**the start value of scanning*/
	private double scanStartValue = Double.NaN;
	/**the end value of scanning*/
	private double scanEndValue = Double.NaN;
	/**the steps of scanning*/
	private int scanSteps;
	
	/**Constructor*/
	public NodePropertyRecord( final AcceleratorNode node, final String propertyName ) {
		NODE = node;
		PROPERTY_NAME = propertyName;
		Channel[] channels = NODE.getLivePropertyChannels( PROPERTY_NAME );
		CHANNEL_MONITORS = createMonitors(channels);		
		MODEL_INPUT = new ModelInput(node, PROPERTY_NAME, Double.NaN );
		
	}
	
	/**create monitors*/
	private ChannelMonitor[] createMonitors( final Channel[] channels ){
		ChannelMonitor[] monitors = new ChannelMonitor[channels.length];
		for( int channelIndex = 0; channelIndex < channels.length; channelIndex++ ){
			monitors[channelIndex] = new ChannelMonitor( channels[channelIndex] );
		}
		return monitors;
		
	}
	
	/**get the accelerator node*/
	public AcceleratorNode getAcceleratorNode(){
		return NODE;
	}
	
	/**get the property name*/
	public String getPropertyName(){
		return PROPERTY_NAME;
	}
	/**get the model input*/
	public ModelInput getModelInput(){
		return MODEL_INPUT;
	}
	
	/** get design value */
	public double getDesignValue(){
		return NODE.getDesignPropertyValue( PROPERTY_NAME );
	}
	
	/**get the live value if use live model and there is one*/
	public double getLiveValue() {
		double[] liveValueArray = new double[CHANNEL_MONITORS.length];
		for( int monitorIndex = 0; monitorIndex<CHANNEL_MONITORS.length; monitorIndex++ ){
			liveValueArray[monitorIndex] = CHANNEL_MONITORS[monitorIndex].getLatestValue();
		}
		return NODE.getLivePropertyValue(PROPERTY_NAME, liveValueArray);
	}
	
	/**get the test value */
	public double getTestValue(){
		return MODEL_INPUT.getDoubleValue();
	}
	
	/**set the test value*/
	public void setTestValue( final double value ){
		MODEL_INPUT.setDoubleValue(value);
	}
	
	/**get the lowerLimit*/
	public boolean getCheckState () {
		return checkState;
	}
	
	/**set the check-state*/
	public void setCheckState ( final boolean checkState ) {
		this.checkState = checkState;
	}
	
	/**get the scan start value*/
	public double getScanStartValue () {
		return scanStartValue;
	}
	
	/**set the scan start value*/
	public void setScanStartValue ( final double startValue ) {
		this.scanStartValue = startValue;
	}
	
	/**get the scan end value*/
	public double getScanEndValue () {
		return scanEndValue;
	}
	
	/**set the scan end value*/
	public void setScanEndValue ( final double endValue ) {
		this.scanEndValue = endValue;
	}
	
	/**get the steps*/
	public int getSteps () {
		return scanSteps;
	}
	
	/**set the steps*/
	public void setSteps ( final int steps ) {
		this.scanSteps = steps;
	}

}
