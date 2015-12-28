/*
 * Created on Oct 21, 2003
 */
package xal.sim.sync;

import java.util.*;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.elem.IdealPermMagQuad;
import xal.model.elem.sync.IElectromagnet;
import xal.model.elem.sync.IRfCavity;
import xal.model.elem.sync.IRfGap;
import xal.sim.scenario.ModelInput;
import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.PrimaryPropertyAccessor;

/**
 * Manages synchronization mappings between accelerator node proxies and
 * lattice elements.
 * 
 * @author Craig McChesney
 */
public class SynchronizationManager {

	public static final String DEFAULT_SYNC_MODE = Scenario.SYNC_MODE_DESIGN;
	// key = accelerator node concrete class, value = access instance
	
	
	// Class Variables =========================================================
	
	private static Map<Class<?>,Synchronizer> nodeSynchronizerMap = new HashMap<Class<?>,Synchronizer>();
	// key = element class, value = synchronization factory class

	private static List<String> syncModes = new ArrayList<String>();
		
	
	// Static Initialization ===================================================
	
	static {
		// Synchronizer registration
		registerSynchronizer(IElectromagnet.class, new ElectromagnetSynchronizer());
		registerSynchronizer(IRfGap.class, new RfGapSynchronizer());
		registerSynchronizer(IRfCavity.class, new RfCavitySynchronizer());
		registerSynchronizer( IdealPermMagQuad.class, new PermanentMagnetSynchronizer() );

		// Synch mode registration
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_LIVE);
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_DESIGN);
        SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_RF_DESIGN);
	}
	
	
	// Static Accessors ========================================================
	
	public static List<String> syncModes() {
		return syncModes;
	}
	
	
	// Instance Variables ======================================================

	private Map<AcceleratorNode,List<IElement>> synchronizedNodeElems = new HashMap<AcceleratorNode,List<IElement>>();
	// key = accelerator node, value = list of elements sync'ed to that node
	
	private Map<AcceleratorNode,List<IElement>> allNodeElems = new HashMap<AcceleratorNode,List<IElement>>();
	// a map of all accelerator nodes and a list of the elements corresponding
	// to them, whether synchronized or not
	
	private PrimaryPropertyAccessor propertyAccessor;

	private String syncMode = SynchronizationManager.DEFAULT_SYNC_MODE;
	

	// Public State ============================================================
	
	/** Set the synchronization mode */
	public void setSynchronizationMode( final String newMode ) {
		if ( !SynchronizationManager.syncModes.contains( newMode ) )  throw new IllegalArgumentException( "unknown synchronization mode: " + newMode );
		syncMode = newMode;
	}


	/** Get the synchronization mode */
	public String getSynchronizationMode() {
		return syncMode;
	}

	
	// Constructors ============================================================
	
	public SynchronizationManager() {
		propertyAccessor = new PrimaryPropertyAccessor();
	}
	

	// Public Synchronization operations =======================================
	
	public void resync() throws SynchronizationException {
		final Collection<AcceleratorNode> nodes = synchronizedNodeElems.keySet();
		propertyAccessor.requestValuesForNodes( nodes, syncMode );
		
		for ( final AcceleratorNode node : nodes ) {
			final Map<String,Double> valueMap = propertyAccessor.valueMapFor( node );

			for ( final IElement elem : synchronizedNodeElems.get( node ) ) {
				resync(elem, valueMap);
			}
		} 
	}
	
	
	/** use the cached values modified by the model inputs and resync the model */
	public void resyncFromCache() throws SynchronizationException {
		for ( final AcceleratorNode node : synchronizedNodeElems.keySet() ) {
			final Map<String,Double> valueMap = propertyAccessor.getWhatifValueMapFromCache( node );
			for ( final IElement elem : synchronizedNodeElems.get( node ) ) {
				resync( elem, valueMap );
			}
		} 
	}
	
	
	/**
	 * Synchronizes anElem to the property values contained in valueMap.
	 * @param anElem element to synchronize
	 * @param valueMap a Map whose keys are property names and values are String property values
	 */
	public static void resync( final IElement anElem, final Map<String,Double> valueMap ) throws SynchronizationException {
		Synchronizer synchronizer = getSynchronizer( anElem );
		synchronizer.resync( anElem, valueMap );
	}
	
	/**
	 * Creates a synchronization between the specified element and accelerator
	 * node.  Request is ignored if there is no synchronizer for the specified
	 * element type.  Request is also ignored if there is no accessor for the
	 * specified node type, because the system doesn't know how to access data
	 * from that type of node.
	 * 
	 * @param anElem the lattice element to create synchronization for
	 * @param aNode the accelerator node to synchronize the element with
	 */
	public void synchronize(IElement anElem, AcceleratorNode aNode) {
		if ((hasSynchronizerFor(anElem)) && (propertyAccessor.hasAccessorFor(aNode)))
			addSynchronizedElementMappedTo(anElem, aNode);
		addElementMappedTo(anElem, aNode);
	}
	
	
//    /**
//     * <p>Creates a synchronization association between the specified composite
//     * modeling element and accelerator hardware node.  A synchronization
//     * association is created for each modeling element in the composite
//     * element.  Thus, the method is implemented by iterating over the 
//     * composite elements of the argument and calling the method
//     * <code>synchronize(IElement,AcceleratorNode)</code>.
//     * </p>
//     * 
//     * NOTE:
//     * <p>Within the composite, a synchronization associate request is ignored 
//     * if there is no synchronizer for the specified sub-element type.  Request 
//     * is also ignored if there is no accessor for the specified hardware node 
//     * type, because the system does not know how to access data from the hardware
//     * object.
//     * </p>
//     * 
//     * @param comp  composite modeling element requiring synchronization 
//     * @param node  hardware node providing synchronization
//     * 
//     * @author  Christopher K. Allen
//     * 
//     * @see SynchronizationManager#synchronize(IElement, AcceleratorNode)
//     */
//    public void synchronize( final IComposite comp, final AcceleratorNode node ) {
//       final Iterator<IComponent> iterComp = comp.globalIterator();
//        while (iterComp.hasNext())  {
//            final IElement elem = (IElement)iterComp.next();
//            
//            if ((hasSynchronizerFor(elem)) && (propertyAccessor.hasAccessorFor(node)))
//                addSynchronizedElementMappedTo(elem, node);
//            addElementMappedTo(elem, node);
//            
//        }
//    }
    

// Queries =================================================================
	
	public Map<String,Double> propertiesForNode( final AcceleratorNode aNode ) {
		propertyAccessor.requestValuesForNodes( Collections.singleton( aNode ), syncMode );
		return propertyAccessor.valueMapFor( aNode );
	}

	// Node - Element Mapping ==================================================
	
	private void addElementMappedTo(IElement anElem, AcceleratorNode aNode) {
		List<IElement> elems = allElementsMappedTo(aNode);
		if (elems == null) {
			elems = new ArrayList<IElement>();
			allNodeElems.put(aNode, elems);
		}
		elems.add(anElem);
	}
	
	public List<IElement> allElementsMappedTo(AcceleratorNode aNode) {
		return allNodeElems.get(aNode);
	}
	
	private void addSynchronizedElementMappedTo(IElement anElem, AcceleratorNode aNode) {
		List<IElement> elems = synchronizedElementsMappedTo(aNode);
		if (elems == null) {
			elems = new ArrayList<IElement>();
			synchronizedNodeElems.put(aNode, elems);
		}
		elems.add(anElem);
	}
	
	protected List<IElement> synchronizedElementsMappedTo(AcceleratorNode aNode) {
		return synchronizedNodeElems.get(aNode);
	}
	
	
	// Private Synchronizer Support ============================================
	
	private static boolean hasSynchronizerFor(IElement anElem) {
		return getSynchronizer(anElem) != null;
	}
	
	private static Synchronizer getSynchronizer(IElement anElem) {
		for ( final Class<?> cl : nodeSynchronizerMap.keySet() ) {
			if ( cl.isInstance( anElem ) ) {
				return nodeSynchronizerMap.get( cl );
			}
		}
		return null;
	}
	
	private static void registerSynchronizer( final Class<?> nodeClass, final Synchronizer aSync ) {
		nodeSynchronizerMap.put( nodeClass, aSync );
	}
	
	
	// ModelInput Management ===================================================
	
	/**
	 * Sets the specified node's property to the specified value.  Replaces the
	 * existing value if there is one.
	 * 
	 * @param aNode node whose property to set
	 * @param property name of property to set
	 * @param value double value for property
	 */
	public ModelInput setModelInput(AcceleratorNode aNode, String property, double value) {
		return propertyAccessor.setModelInput(aNode, property, value);
	}
	
	/**
	 * Returns the ModelInput for the specified node's property.
	 * 
	 * @param aNode node whose property to get a ModelInput for
	 * @param propName name of property to get a ModelInput for
	 */
	public ModelInput getModelInput(AcceleratorNode aNode, String propName) {
		return propertyAccessor.getInput(aNode, propName);
	}
	
	public void removeModelInput(AcceleratorNode aNode, String property) {
		propertyAccessor.removeInput(aNode, property);
	}
	
	
	// Testing and Debugging ===================================================
	
	protected void debugPrint() {
		System.out.println("Full Node - Element Map:");
		for ( final AcceleratorNode node : allNodeElems.keySet() ) {
			System.out.println("\t" + node.getId());
			for ( final IElement elem : allNodeElems.get( node ) ) {
				System.out.println("\t\t" + elem);
			}
		}
		
		System.out.println("Synchronized Node - Element Map:");
		for ( final AcceleratorNode node : synchronizedNodeElems.keySet() ) {
			System.out.println("\t" + node.getId());
			for ( final IElement elem : synchronizedNodeElems.get( node ) ) {
				System.out.println("\t\t" + elem);
			}
		}
	}

	
	public boolean checkSynchronization( final AcceleratorNode aNode, final Map<String,Double> values ) throws SynchronizationException {
		final List<IElement> elems = synchronizedElementsMappedTo( aNode );
		if (elems == null) return true;
		for ( final IElement elem : elems ) {
			getSynchronizer( elem ).checkSynchronization( elem, values );
		}
		return true;
	}
	
}
