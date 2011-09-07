/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xal.sim.scenario.ModelInput;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.RfCavity;
import xal.smf.impl.RfGap;


/**
 * @author Craig McChesney
 */
public class PrimaryPropertyAccessor {
	/** indicates the debugging status for diagnostic typeout */
	private static final boolean DEBUG = false;
	
	/** map of property accessors keyed by node */
	private static Map<Class,PropertyAccessor> nodeAccessorMap = new HashMap<Class,PropertyAccessor>();
	
	// key = accelerator node, value = list of inputs for that node
	private Map<AcceleratorNode,Map<String,ModelInput>> nodeInputMap = new HashMap<AcceleratorNode,Map<String,ModelInput>>();
	
	/** cache of values (excluding model inputs) for node properties keyed by node and the subsequent map is keyed by property to get the value */
	final private Map<AcceleratorNode, Map<String,Double>> PROPERTY_VALUE_CACHE;
	
	
	// static initializer
	static {
		// Accessor Registration
		registerAccessorInstance( Electromagnet.class, new ElectromagnetPropertyAccessor() );
		registerAccessorInstance( RfGap.class, new RfGapPropertyAccessor() );
		registerAccessorInstance( RfCavity.class, new RfCavityPropertyAccessor() );
	}
	
	
	/** Constructor */
	public PrimaryPropertyAccessor() {
		PROPERTY_VALUE_CACHE = new HashMap<AcceleratorNode, Map<String,Double>>();
	}
	
	
	/**
	 * Returns the double value for the specified property of the supplied node, using the specified mode.
	 * @param node AcceleratorNode to get property value for
	 * @param property name of node property to get value for
	 * @param mode either PropertyAccessor.SYNC_MODE_LIVE or
	 * PropertyAccessor.SYNC_MODE_Design
	 * @return a double value for the specified node property using mode
	 * @throws ProxyException if the node's accessor encounters an error getting a property value
	 */
	protected static double doubleValueFor( final AcceleratorNode node, final String property, final String mode ) throws ProxyException {
		if ((node == null) || (property == null) || (mode == null))
			throw new IllegalArgumentException(
				"null arguments not allowed by doubleValueFor");
		if (! (SynchronizationManager.syncModes().contains(mode)))
			throw new IllegalArgumentException(
				"illegal sync mode value");
		PropertyAccessor accessor = getAccessorFor(node);
		if (accessor == null)
			throw new IllegalArgumentException(
				"No accessor registered for: " + node.getClass().getName());
		return accessor.doubleValueFor(node, property, mode);
	}
	
	/**
	 * Returns a Map of property values for the supplied node.  The map's keys are the property names as defined by the node class' propertyNames
	 * method, values are the Double value for that property on aNode.
	 * @param objNode the AcclereatorNode whose properties to return
	 * @param mode synchronization mode
	 * @return a Map of node property values
	 * @throws ProxyException if the node's accessor encounters an error getting a property value
	 */
	public Map<String,Double> valueMapFor( final Object objNode, final String mode ) throws ProxyException {
		if ( (objNode == null) || (mode == null) ) {
			throw new IllegalArgumentException( "null arguments not allowed by doubleValueFor" );
		}
		if (! (objNode instanceof AcceleratorNode)) {
			throw new IllegalArgumentException( "expected instance of AcceleratorNode" );
		}
		if (! (SynchronizationManager.syncModes().contains(mode))) {
			throw new IllegalArgumentException( "illegal sync mode value" );
		}
		AcceleratorNode aNode = (AcceleratorNode) objNode;
		PropertyAccessor nodeAccessor = getAccessorFor(aNode);
		if (nodeAccessor == null) {
			throw new IllegalArgumentException( "unknown node type: " + aNode.getClass().getName() );
		}
		final List properties = nodeAccessor.propertyNames();
		final Map<String,Double> valueMap = new HashMap<String,Double>( properties.size() );
		final Iterator propertyIt = properties.iterator();
		while ( propertyIt.hasNext() ) {
			final String property = (String) propertyIt.next();
			valueMap.put( property, nodeAccessor.doubleValueFor( aNode, property, mode ) );
		}
		PROPERTY_VALUE_CACHE.put( aNode, new HashMap<String,Double>( valueMap ) );		// need to copy it so we don't override the raw values
		addInputOverrides( aNode, valueMap );
		if (DEBUG) printValueMap( aNode, valueMap );
		return valueMap;
	}
	
	
	/** Use the cache rather than other sources for the value map and then apply the model inputs */
	public Map<String,Double> getWhatifValueMapFromCache( final Object objNode ) {
		if ( objNode == null ) {
			throw new IllegalArgumentException( "null arguments not allowed by doubleValueFor" );
		}
		else if ( !(objNode instanceof AcceleratorNode) ) {
			throw new IllegalArgumentException( "expected instance of AcceleratorNode" );
		}
		final AcceleratorNode aNode = (AcceleratorNode)objNode;
		final Map<String,Double> valueMap = new HashMap<String,Double>( PROPERTY_VALUE_CACHE.get( aNode ) );		// need to copy it so we don't override the raw values
		addInputOverrides( aNode, valueMap );
		return valueMap;
	}
	
	
	/**
	 * Returns a List of property names for the supplied node.
	 * 
	 * @param aNode AcceleratorNode whose property names to return
	 * @return a List of property names for aNode
	 */
	public List propertyNamesFor(AcceleratorNode aNode) {
		if (aNode == null)
			throw new IllegalArgumentException("can't get property names for null node");
		PropertyAccessor nodeAccessor = getAccessorFor(aNode);
		if (nodeAccessor == null)
			throw new IllegalArgumentException(
				"unregistered node type: " + aNode.getClass().getName());
		return nodeAccessor.propertyNames();
	}
	
	
	// Node-Specific Factory Operations ========================================
	
	private static PropertyAccessor getAccessorFor(AcceleratorNode aNode) {
		Iterator nodeClassIt = nodeAccessorMap.keySet().iterator();
		while (nodeClassIt.hasNext()) {
			Class cl = (Class) nodeClassIt.next();
			if (cl.isInstance(aNode)) {
				return (PropertyAccessor) nodeAccessorMap.get(cl);
			}
		}
		return null;
	}
	
	/**
	 * Returns true if there is an accessor for the specified node type, false
	 * otherwise.
	 * 
	 * @param aNode AcceleratorNode whose type to find an accessor for
	 * @return true if there is an accessor for the supplied node, false otherwise
	 */
	public boolean hasAccessorFor(AcceleratorNode aNode) {
		return getAccessorFor(aNode) != null;
	}
	
	private static void registerAccessorInstance( final Class nodeClass, final PropertyAccessor accessor ) {
		nodeAccessorMap.put( nodeClass, accessor );
	}


	// Model Input Data: Node Property Overrides ===============================

	private void addInputOverrides( final AcceleratorNode aNode, final Map<String,Double> valueMap ) {
		Map inputs = inputsForNode(aNode);
		if (inputs == null) return;
		Iterator inputIt = inputs.values().iterator();
		while (inputIt.hasNext()) {
			ModelInput input = (ModelInput) inputIt.next();
			final String property = input.getProperty();
			valueMap.put( property, input.getDoubleValue() );
		}
	}
	
	/**
	 * Sets the specified node's property to the specified value.  Replaces the
	 * existing value if there is one.
	 * 
	 * @param aNode node whose property to set
	 * @param property name of property to set
	 * @param val double value for property
	 */
	public ModelInput setModelInput(AcceleratorNode aNode, String property, double val) {
		ModelInput existingInput = getInput( aNode, property );
		if (existingInput != null) {
			existingInput.setDoubleValue(val);
			if ( aNode instanceof RfCavity )  applyModelInputToRFGaps( (RfCavity)aNode, property, val );
			return existingInput;
		} else {
			ModelInput input = new ModelInput( aNode, property, val );
			addInput(input);
			if ( aNode instanceof RfCavity )  applyModelInputToRFGaps( (RfCavity)aNode, property, val );
			return input;
		}
	}
	
	
	/**
	 * Workaround to address a bug in the way that cavities are handled.  Whenever a cavity 
	 * input is changed its RF Gaps must be changed accordingly.
	 * @param cavity the cavity whose inputs are being set
	 * @param property the cavity's property being changed
	 * @param value the new value of the cavity's property
	 */
	private void applyModelInputToRFGaps( final RfCavity cavity, final String property, final double value ) {
		if ( property.equals( RfCavityPropertyAccessor.PROPERTY_PHASE ) ) {
			applyCavityPhaseToRFGaps( cavity, value );
		}
		else if ( property.equals( RfCavityPropertyAccessor.PROPERTY_AMPLITUDE ) ) {
			applyCavityAmplitudeToRFGaps( cavity, value );
		}
	}
	
	
	/**
	 * Workaround to address a bug in the way that cavities are handled.  Whenever a cavity's amplitude 
	 * input is changed its RF Gaps must be changed accordingly.
	 * @param cavity the cavity whose inputs are being set
	 * @param cavityAmp the new value of the cavity's amplitude
	 */
	private void applyCavityAmplitudeToRFGaps( final RfCavity cavity, final double cavityAmp ) {
		final Iterator gapIter = cavity.getGaps().iterator();
		while ( gapIter.hasNext() ) {
			final RfGap gap = (RfGap)gapIter.next();
			final double gapAmp = gap.toGapAmpFromCavityAmp( cavityAmp );
			setModelInput( gap, RfGapPropertyAccessor.PROPERTY_E0, RfGapPropertyAccessor.SCALE_E0 * gapAmp );
			setModelInput( gap, RfGapPropertyAccessor.PROPERTY_ETL, RfGapPropertyAccessor.SCALE_ETL * gap.toE0TLFromGapField( gapAmp ) );
		}
	}
	
	
	/**
	 * Workaround to address a bug in the way that cavities are handled.  Whenever a cavity's phase 
	 * input is changed its RF Gaps must be changed accordingly.
	 * @param cavity the cavity whose inputs are being set
	 * @param cavityPhase the new value of the cavity's phase
	 */
	private void applyCavityPhaseToRFGaps( final RfCavity cavity, final double cavityPhase ) {
		final Iterator gapIter = cavity.getGaps().iterator();
		while ( gapIter.hasNext() ) {
			final RfGap gap = (RfGap)gapIter.next();
			setModelInput( gap, RfGapPropertyAccessor.PROPERTY_PHASE, RfGapPropertyAccessor.SCALE_PHASE * gap.toGapPhaseFromCavityPhase( cavityPhase ) );
		}
	}	
	
	
	/**
	 * Returns the ModelInput for the specified node's property, or null if there
	 * is none.
	 * 
	 * @param aNode node whose property to get a ModelInput for
	 * @param propName name of property to get a ModelInput for
	 */
	public ModelInput getInput(AcceleratorNode aNode, String propName) {
		Map inputs = inputsForNode(aNode);
		if (inputs != null)
			return (ModelInput) inputs.get(propName);
		else return null;
	}
	
	protected void addInput( final ModelInput anInput ) {
		final AcceleratorNode node = anInput.getAcceleratorNode();
		Map<String,ModelInput> inputs = inputsForNode(node);
		if (inputs == null) {
			inputs = new HashMap<String,ModelInput>();
			nodeInputMap.put( node, inputs );
		}
		inputs.put( anInput.getProperty(), anInput );		
	}
	
	
	public void removeInput(AcceleratorNode aNode, String property) {
		Map inputs = inputsForNode(aNode);
		if (inputs != null) {
			inputs.remove(property);
			if ( aNode instanceof RfCavity )  removeRFCavityGapInputs( (RfCavity)aNode, property );
		}
	}
	
	
	/**
	 * Workaround to remove the cavity's associated gap inputs.
	 * @param cavity the cavity for which to remove the input
	 * @param property the property whose input is to be removed
	 */
	private void removeRFCavityGapInputs( final RfCavity cavity, final String property ) {
		if ( property.equals( RfCavityPropertyAccessor.PROPERTY_PHASE ) ) {
			removeRFCavityGapPhaseInputs( cavity );
		}
		else if ( property.equals( RfCavityPropertyAccessor.PROPERTY_AMPLITUDE ) ) {
			removeRFCavityGapAmplitudeInputs( cavity );
		}		
	}
	
	
	/**
	 * Workaround to remove an RF cavity's gap phase inputs.
	 * @param cavity the cavity for which to remove the gap's inputs
	 */
	private void removeRFCavityGapPhaseInputs( final RfCavity cavity ) {
		final Iterator gapIter = cavity.getGaps().iterator();
		while ( gapIter.hasNext() ) {
			final RfGap gap = (RfGap)gapIter.next();
			removeInput( gap, RfGapPropertyAccessor.PROPERTY_PHASE );
		}		
	}
	
	
	/**
	 * Workaround to remove an RF cavity's gap amplitude inputs.
	 * @param cavity the cavity for which to remove the gap's inputs
	 */
	private void removeRFCavityGapAmplitudeInputs( final RfCavity cavity ) {
		final Iterator gapIter = cavity.getGaps().iterator();
		while ( gapIter.hasNext() ) {
			final RfGap gap = (RfGap)gapIter.next();
			removeInput( gap, RfGapPropertyAccessor.PROPERTY_E0 );
			removeInput( gap, RfGapPropertyAccessor.PROPERTY_ETL );
		}		
	}
	
	
	private Map<String,ModelInput> inputsForNode( final AcceleratorNode aNode ) {
		return nodeInputMap.get( aNode );
	}
	
	
	// Testing and Debugging ===================================================
	
	private static void printValueMap(AcceleratorNode aNode, Map values) {
		System.out.println("Properties for node: " + aNode);
		Iterator propIt = values.keySet().iterator();
		while (propIt.hasNext()) {
			String property = (String) propIt.next();
			Double val = (Double) values.get(property);
			System.out.println("\t" + property + ": " + val); 
		}
		System.out.println();
	}
	
}
