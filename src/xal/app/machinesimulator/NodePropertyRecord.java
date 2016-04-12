package xal.app.machinesimulator;

import xal.ca.Channel;
import xal.sim.scenario.ModelInput;
import xal.smf.AcceleratorNode;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
/**
 * 
 * @author luxiaohan
 *get and set values of the specified property in an accelerator node
 */
public class NodePropertyRecord implements DataListener {
	
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "NodePropertyRecord";
	
	/**the accelerator node*/
	final private AcceleratorNode NODE;
	/**the name of specified property*/
	final private String PROPERTY_NAME;
	/**model Input variable to set test value to scenario*/
	final private ModelInput MODEL_INPUT;
	/** channel monitor to monitor the value of the channel */
	private ChannelMonitor[] channelMonitors;
	/**the logged value*/
	private double loggedValue;
	/**check-state of scanning*/
	private boolean checkState;
	/**the start value of scanning*/
	private double scanStartValue = Double.NaN;
	/**the end value of scanning*/
	private double scanEndValue = Double.NaN;
	/**the steps of scanning*/
	private int scanSteps;
	
	/**Constructor*/
	public NodePropertyRecord( final AcceleratorNode node, final String propertyName,
			final double loggedValue ) {
		NODE = node;
		PROPERTY_NAME = propertyName;
		this.loggedValue = loggedValue;
		Channel[] channels = NODE.getLivePropertyChannels( PROPERTY_NAME );
		channelMonitors = createMonitors(channels);		
		MODEL_INPUT = new ModelInput(node, PROPERTY_NAME, Double.NaN );
		
	}
	
	/**Constructor with adaptor*/
	public NodePropertyRecord( final AcceleratorNode node, final String propertyName,
			final double loggedValue, final DataAdaptor adaptor ) {
		NODE = node;
		PROPERTY_NAME = propertyName;
		this.loggedValue = loggedValue;
		Channel[] channels = NODE.getLivePropertyChannels( PROPERTY_NAME );
		channelMonitors = createMonitors( channels );
        double testValue = ( adaptor.hasAttribute( "testValue" ) ) ? adaptor.doubleValue( "testValue" ) : Double.NaN;
		MODEL_INPUT = new ModelInput(node, PROPERTY_NAME, testValue );
        if ( adaptor.hasAttribute( "checkState" ) ) checkState = adaptor.booleanValue( "checkState" );
        if ( adaptor.hasAttribute( "scanStartValue" ) ) scanStartValue = adaptor.doubleValue( "scanStartValue" );
        if ( adaptor.hasAttribute( "scanEndValue" ) ) scanEndValue = adaptor.doubleValue( "scanEndValue" );
        if ( adaptor.hasAttribute( "scanSteps" ) ) scanSteps = adaptor.intValue( "scanSteps" );
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
	public double getDesignValue() {
		return NODE.getDesignPropertyValue( PROPERTY_NAME );
	}
	
	/**get the live value if use live model and there is one*/
	public double getLiveValue() {
		double[] liveValueArray = new double[channelMonitors.length];
		for( int monitorIndex = 0; monitorIndex<channelMonitors.length; monitorIndex++ ){
			liveValueArray[monitorIndex] = channelMonitors[monitorIndex].getLatestValue();
		}
		return NODE.getLivePropertyValue(PROPERTY_NAME, liveValueArray);
	}
	
	/**get the logged value*/
	public double getLoggedValue() {
		return loggedValue;
	}
        
    public void refresh( final double loggedValue ) {
       Channel[] channels = NODE.getLivePropertyChannels( PROPERTY_NAME );
       channelMonitors = createMonitors( channels );
       this.loggedValue = loggedValue;
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
	
	/** provides the name used to identify the class in an external data source. */
        @Override
	public String dataLabel() {
		return DATA_LABEL;
	}
	
	/** Instructs the receiver to update its data based on the given adaptor. */
        @Override
	public void update( DataAdaptor adaptor ) {
	}
	
	/** Instructs the receiver to write its data to the adaptor for external storage. */
        @Override
	public void write( DataAdaptor adaptor ) {
        adaptor.setValue( "nodeId", NODE.getId() );
        adaptor.setValue( "propertyName", PROPERTY_NAME );
        adaptor.setValue( "testValue", MODEL_INPUT.getDoubleValue() );
        adaptor.setValue( "checkState", checkState );
        adaptor.setValue( "scanStartValue", scanStartValue );
        adaptor.setValue( "scanEndValue", scanEndValue );
        adaptor.setValue( "scanSteps", scanSteps );
	}
	
	

}
