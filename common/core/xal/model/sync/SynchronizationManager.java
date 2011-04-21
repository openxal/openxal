/*
 * Created on Oct 21, 2003
 */
package xal.model.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xal.model.IComposite;
import xal.model.IElement;
import xal.model.elem.IElectromagnet;
import xal.model.elem.IRfCavity;
import xal.model.elem.IRfGap;
import xal.model.scenario.ModelInput;
import xal.model.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.proxy.PrimaryPropertyAccessor;
import xal.smf.proxy.ProxyException;

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
	
	private static HashMap nodeSynchronizerMap = new HashMap();
	// key = element class, value = synchronization factory class

	private static ArrayList syncModes = new ArrayList();
		
	
	// Static Initialization ===================================================
	
	static {
		// Synchronizer registration
		registerSynchronizer(IElectromagnet.class, new ElectromagnetSynchronizer());
		registerSynchronizer(IRfGap.class, new RfGapSynchronizer());
		registerSynchronizer(IRfCavity.class, new RfCavitySynchronizer());

		// Synch mode registration
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_LIVE);
		SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_DESIGN);
        SynchronizationManager.syncModes.add(Scenario.SYNC_MODE_RF_DESIGN);
	}
	
	
	// Static Accessors ========================================================
	
	public static List syncModes() {
		return syncModes;
	}
	
	
	// Instance Variables ======================================================

	private HashMap synchronizedNodeElems = new HashMap();
	// key = accelerator node, value = list of elements sync'ed to that node
	
	private HashMap allNodeElems = new HashMap();
	// a map of all accelerator nodes and a list of the elements corresponding
	// to them, whether synchronized or not
	
	private PrimaryPropertyAccessor propertyAccessor;

	private String syncMode = SynchronizationManager.DEFAULT_SYNC_MODE;
	

	// Public State ============================================================
	
	public void setSynchronizationMode(String newMode) {
		if (! SynchronizationManager.syncModes.contains(newMode))
			throw new IllegalArgumentException("unknown synchronization mode: " + newMode);
		syncMode = newMode;
	}
	
	
	// Constructors ============================================================
	
	public SynchronizationManager() {
		propertyAccessor = new PrimaryPropertyAccessor();
	}
	

	// Public Synchronization operations =======================================
	
	public void resync() throws SynchronizationException {
		Iterator nodeIt = synchronizedNodeElems.keySet().iterator();
		while (nodeIt.hasNext()) {
			AcceleratorNode node = (AcceleratorNode) nodeIt.next();
			Map valueMap;
			try {
				valueMap = propertyAccessor.valueMapFor(node, syncMode);
			}
			catch ( ProxyException exception ) {
				exception.printStackTrace();
				throw new SynchronizationException( "ProxyException getting properties for: " + node.getId() );
			}
			ArrayList elems = (ArrayList) synchronizedNodeElems.get(node);
			Iterator elemIt = elems.iterator();
			while (elemIt.hasNext()) {
				IElement elem = (IElement) elemIt.next();
				resync(elem, valueMap);
			}
		} 
	}
	
	
	/** use the cached values modified by the model inputs and resync the model */
	public void resyncFromCache() throws SynchronizationException {
		Iterator nodeIt = synchronizedNodeElems.keySet().iterator();
		while ( nodeIt.hasNext() ) {
			final AcceleratorNode node = (AcceleratorNode) nodeIt.next();
			final Map<String,Double> valueMap = propertyAccessor.getWhatifValueMapFromCache( node );
			final List elems = (List)synchronizedNodeElems.get( node );
			final Iterator elemIt = elems.iterator();
			while ( elemIt.hasNext() ) {
				IElement elem = (IElement)elemIt.next();
				resync( elem, valueMap );
			}
		} 
	}
	
	
	/**
	 * Synchronizes anElem to the property values contained in valueMap.
	 * @param anElem element to synchronize
	 * @param valueMap a Map whose keys are property names and values are String property values
	 */
	public static void resync(IElement anElem, Map valueMap) throws SynchronizationException {
		Synchronizer synchronizer = getSynchronizer(anElem);
		synchronizer.resync(anElem, valueMap);
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
	
	
    /**
     * <p>Creates a synchronization association between the specified composite
     * modeling element and accelerator hardware node.  A synchronization
     * association is created for each modeling element in the composite
     * element.  Thus, the method is implemented by iterating over the 
     * composite elements of the argument and calling the method
     * <code>synchronize(IElement,AcceleratorNode)</code>.
     * </p>
     * 
     * NOTE:
     * <p>Within the composite, a synchronization associate request is ignored 
     * if there is no synchronizer for the specified sub-element type.  Request 
     * is also ignored if there is no accessor for the specified hardware node 
     * type, because the system does not know how to access data from the hardware
     * object.
     * </p>
     * 
     * @param comp  composite modeling element requiring synchronization 
     * @param node  hardware node providing synchronization
     * 
     * @author  Christopher K. Allen
     * 
     * @see SynchronizationManager#synchronize(IElement, AcceleratorNode)
     */
    public void synchronize(IComposite comp, AcceleratorNode node) {
        Iterator iterComp = comp.globalIterator();
        while (iterComp.hasNext())  {
            IElement    elem = (IElement)iterComp.next();
            
            if ((hasSynchronizerFor(elem)) && (propertyAccessor.hasAccessorFor(node)))
                addSynchronizedElementMappedTo(elem, node);
            addElementMappedTo(elem, node);
            
        }
    }
    

// Queries =================================================================
	
	public Map propertiesForNode(AcceleratorNode aNode) throws ProxyException {
		return propertyAccessor.valueMapFor(aNode, syncMode);
	}
	
	// Node - Element Mapping ==================================================
	
	private void addElementMappedTo(IElement anElem, AcceleratorNode aNode) {
		List elems = allElementsMappedTo(aNode);
		if (elems == null) {
			elems = new ArrayList();
			allNodeElems.put(aNode, elems);
		}
		elems.add(anElem);
	}
	
	public List allElementsMappedTo(AcceleratorNode aNode) {
		return (List) allNodeElems.get(aNode);
	}
	
	private void addSynchronizedElementMappedTo(IElement anElem, AcceleratorNode aNode) {
		List elems = synchronizedElementsMappedTo(aNode);
		if (elems == null) {
			elems = new ArrayList();
			synchronizedNodeElems.put(aNode, elems);
		}
		elems.add(anElem);
	}
	
	protected List synchronizedElementsMappedTo(AcceleratorNode aNode) {
		return (List) synchronizedNodeElems.get(aNode);
	}
	
	
	// Private Synchronizer Support ============================================
	
	private static boolean hasSynchronizerFor(IElement anElem) {
		return getSynchronizer(anElem) != null;
	}
	
	private static Synchronizer getSynchronizer(IElement anElem) {
		Iterator elemClassIt = nodeSynchronizerMap.keySet().iterator();
		while (elemClassIt.hasNext()) {
			Class cl = (Class) elemClassIt.next();
			if (cl.isInstance(anElem)) {
				return (Synchronizer) nodeSynchronizerMap.get(cl);
			}
		}
		return null;
	}
	
	private static void registerSynchronizer(Class nodeClass, Synchronizer aSync) {
		nodeSynchronizerMap.put(nodeClass, aSync);
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
		Iterator nodeIt = allNodeElems.keySet().iterator();
		while (nodeIt.hasNext()) {
			AcceleratorNode node = (AcceleratorNode) nodeIt.next();
			System.out.println("\t" + node.getId());
			ArrayList elems = (ArrayList) allNodeElems.get(node);
			Iterator elemIt = elems.iterator();
			while (elemIt.hasNext()) {
				IElement elem = (IElement) elemIt.next();
				System.out.println("\t\t" + elem);
			}
		}
		
		System.out.println("Synchronized Node - Element Map:");
		nodeIt = synchronizedNodeElems.keySet().iterator();
		while (nodeIt.hasNext()) {
			AcceleratorNode node = (AcceleratorNode) nodeIt.next();
			System.out.println("\t" + node.getId());
			ArrayList elems = (ArrayList) synchronizedNodeElems.get(node);
			Iterator elemIt = elems.iterator();
			while (elemIt.hasNext()) {
				IElement elem = (IElement) elemIt.next();
				System.out.println("\t\t" + elem);
			}
		}
	}
	
	public boolean checkSynchronization(AcceleratorNode aNode, Map values) 
			throws SynchronizationException {
		List elems = synchronizedElementsMappedTo(aNode);
		if (elems == null) return true;
		Iterator elemIt = elems.iterator();
		while (elemIt.hasNext()) {
			IElement elem = (IElement) elemIt.next();
			getSynchronizer(elem).checkSynchronization(elem, values);
		}
		return true;
	}
	
}
