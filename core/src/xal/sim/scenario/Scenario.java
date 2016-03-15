/*
 * Created on Oct 15, 2003
 */
package xal.sim.scenario;

import xal.model.IAlgorithm;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.sync.SynchronizationException;
import xal.sim.sync.SynchronizationManager;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.Ring;

import java.util.List;
import java.util.Map;


/**
 * <p>
 * Packages an on-line model scenario, including accelerator node proxy manager,
 * lattice, probe, and synchronization manager.
 * </p>
 * <p>
 * It is not necessary for the <code>Scenario</code> object to maintain a back reference
 * to the original <code>AcceleratorSeq</code> object that it models.  In fact it is 
 * undesirable since this represents a dependency of the online model with the SMF
 * hardware representation component of Open XAL.  Its current uses here are not 
 * so requirements,
 * typically used to lookup hardware nodes from their IDs.  This can be done directly
 * on the hardware sequence itself without using this class as a proxy.
 * <p> 
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 */
public class Scenario {
    
    /*
     * Global Constants
     */
    
    /** Synchronization manager constant for live machine synchronization */
    public static final String SYNC_MODE_LIVE = "LIVE";

    /** Synchronization manager constant for design parameter synchronization */
    public static final String SYNC_MODE_DESIGN = "DESIGN";

    /** Synchronization manager constant for synchronization only to live magnet values */
    public static final String SYNC_MODE_RF_DESIGN = "RF_DESIGN";
    

    /*
     * Global Operations
     */
    
    /**
     * Creates a new Scenario for the supplied accelerator sequence.
     * 
     * @param smfSeq    the accelerator sequence to build a scenario for
     * @return          a new Scenario for the supplied accelerator sequence
     * 
     * @throws          ModelException error building Scenario
     */
    public static Scenario newScenarioFor( final AcceleratorSeq smfSeq ) throws ModelException {

        // We have a linear accelerator/transport line - process as such
        Accelerator         smfAccel      = smfSeq.getAccelerator();
        ElementMapping      mapNodeToElem = smfAccel.getElementMapping();
        ScenarioGenerator   mdlGenScnr    = new ScenarioGenerator(mapNodeToElem);

        return mdlGenScnr.generateScenario(smfSeq);
    }

    /**
     * Creates a new Scenario for the supplied accelerator sequence and element mapping.
     *   
     * @param smfSeq        the accelerator sequence to build a scenario for
     * @param mapNodeToElem the element mapping to build a scenario with
     * @return              a new model <code>Scenario</code> for the supplied accelerator sequence
     * @throws ModelException   general error building model lattice
     */
    public static Scenario newScenarioFor( final AcceleratorSeq smfSeq, ElementMapping mapNodeToElem ) throws ModelException {

        // We have a linear accelerator/transport line - process as such
        ScenarioGenerator mdlGenScnr = new ScenarioGenerator(mapNodeToElem);

        return mdlGenScnr.generateScenario(smfSeq);
    }


    /**
     * Creates a new <code>Scenario</code> object for the explicit case where
     * the <code>AcceleratorSeq</code> object is of type 
     * <code>xal.smf.Ring</code>.
     * 
     * @param   smfRing     target hardware (SMF) ring object 
     * @return              <code>Scenario</code> object encapsulating ring
     * 
     * @throws ModelException   unable to build modeling scenario
     */
    public static Scenario  newScenarioFor( final Ring smfRing ) throws ModelException {

        // We have a ring structure
        Accelerator         smfAccel      = smfRing.getAccelerator();
        ElementMapping      mapNodeToElem = smfAccel.getElementMapping();
        ScenarioGenerator   mdlGenScnr    = new ScenarioGenerator( mapNodeToElem );

        return mdlGenScnr.generateScenario( smfRing ); 
    }


    
    /*
     * Local Attributes
     */
    
    /** The model lattice that we are managing */
    private Lattice                         lattice;
    
    /** Synchronization manager synchronizing the model lattice parameters to the synchronization source */
    private final SynchronizationManager    mgrSync;
    
    /** Back reference to the hardware that this scenario model - CKA: I really want to eliminate this */
    private final AcceleratorSeq            smfSeq;

    
    /** Current probe driving the simulation through the model lattice */
    private Probe<?>                        probe;
    
    /** element from which to start propagation */
    private String idElemStart = null;
    
    /** element at which to stop propagation */
    private String idElemStop = null;

    
    /**
     * Flag indicating that propagation should stop at the entrance of
     * the stop element.
     */
    private boolean     bolInclStopElem = true;
    
    
    /** 
     * Constructor 
     */
    protected Scenario( final AcceleratorSeq smfSeq, final Lattice mdlLattice, final SynchronizationManager mgrSync ) {
        this.smfSeq = smfSeq;
        this.lattice = mdlLattice;
        this.mgrSync = mgrSync;
    }
    
    // Model Operations ========================================================
    
    /**
     * Sets the synchronization mode for the sync manager to a known sync mode,
     * such as SynchronizationManager.SYNC_MODE_LIVE or SYNC_MODE_DESIGN.
     * 
     * @param newMode String specifying mode to set to
     * @throws IllegalArgumentException if the specified mode is unknown
     */
    public void setSynchronizationMode( final String newMode ) {
        mgrSync.setSynchronizationMode(newMode);
    }

	
	/** get the synchronization mode */
	public String getSynchronizationMode() {
		return mgrSync.getSynchronizationMode();
	}


    /**
     * Synchronizes each lattice element to the appropriate data source.
     * @throws SynchronizationException if an error is encountered reading a data source
     */
    public void resync() throws SynchronizationException {
        mgrSync.resync();
    }
    
	
    /**
	 * Synchronizes each lattice element from the cache and applies the whatif model inputs.
     * @throws SynchronizationException if an error is encountered reading a data source
     */
    public void resyncFromCache() throws SynchronizationException {
        mgrSync.resyncFromCache();
    }
	
        
    /**
     * Sets the model to start propagation from the AcceleratorNode with the
     * specified id.  First get the AcceleratorNode for the id, find the first
     * element mapped to it, and then set it as the starting element in the
     * lattice.
     * 
     * @param nodeId ID of AcceleratorNode to start from
     * 
     * @throws ModelException if the node is not found, or no elements are mapped to it
     */
    public void setStartNode( final String nodeId ) throws ModelException {
        // find start node
        AcceleratorNode theNode = nodeWithId(nodeId);
        if (theNode == null)
            throw new ModelException("Node not found: " + nodeId);
            
        // get first element mapped to node
        List<IElement> mappedElems = elementsMappedTo(theNode);
        if (mappedElems.isEmpty())
            throw new ModelException("No model elements mapped to: " + nodeId);
            
        // set propagation to start from that element
        IElement elemStart = mappedElems.get(0);
        //System.out.println("Scenario.setStartNode start at element: " + elemStart.getId());
        setStartElement(elemStart);
        
    }
        
	
    /**
     * Sets the model to stop propagation the AcceleratorNode 
     * with the specified id. By default the model will stop propagation <b>AFTER</b> this node. 
		 * This behavior can be changed in the Tracker ( see setStopNodeInclusive(boolean) method. 
     * 
     * @param nodeId ID of the AcceleratorNode to stop after
     * 
     * @throws ModelException if the node is not found, or no elements are mapped to it
     */
    public void setStopNode( final String nodeId ) throws ModelException {
        
        // find stop node
        AcceleratorNode theNode = nodeWithId(nodeId);
        if (theNode == null)
            throw new ModelException("Node not found: " + nodeId);
            
        // get first element mapped to node
        List<IElement> mappedElems = elementsMappedTo(theNode);
        if (mappedElems.isEmpty())
            throw new ModelException("No model elements mapped to: " + nodeId);
            
        // set propagation to stop after that element
        IElement elemStop = mappedElems.get(0);
        //System.out.println("Scenario.setStopNode stop at element: " + elemStop.getId());
        setStopElement(elemStop);
        
    }
	
	
    /**
     * Sets the model to start propagation from the specified IElement.  If you
     * don't have a reference to an element but do have an AcceleratorNode, use
     * setStartNode(String).
     * 
     * @param start Element to start propagation from
     */
    public void setStartElement( final IElement start ) {
        idElemStart = start.getId();
    }
    
	
    /**
     * Sets the model to stop propagation (by default) <b>after</b> the specified IElement.  If you
     * don't have a reference to an element but do have an AcceleratorNode, use
     * setStopNode(String). 
		 * This "stop after" behavior can be changed in the Tracker ( see setStopNodeInclusive(boolean) method. 
     * 
     * @param stop Element to stop propagation after
     */
    public void setStopElement( final IElement stop ) {
        idElemStop = stop.getId();
    }
	
    /**
     * Sets the flag that determines whether or not the
     * propagation stops at the entrance of the stop element (if set),
     * or at the exit of the stop node.  The later case is the default.
     *  
     * @param bolInclStopElem    propagation stops after stop element if <code>true</code>,
     *                           before the stop element if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 20, 2014
     */
    public void setIncludeStopElement(boolean bolInclStopElem) {
        this.bolInclStopElem = bolInclStopElem; 
    }
    
	
	/**
	 * Convert the position of a location in the sequence to a position in a trajectory due to 
	 * a start element offset specified in the scenario.
	 * @param positionInSequence The position of a location in this scenario's sequence
	 * @return the corresponding position relative to this scenario's starting element
	 */
	public double getPositionRelativeToStart( final double positionInSequence ) {
		return ( idElemStart == null ) ? positionInSequence : smfSeq.getRelativePosition( positionInSequence, idElemStart );
	}
    
	
    /**
     * Runs the model (propagate probe through lattice).
     * 
     * @throws ModelException if there is an error propagating the probe
     * @throws IllegalStateException if the lattice or probe is not properly initialized
     */
    public void run() throws ModelException {
        if (lattice == null)
            throw new IllegalStateException(
                "must initialize lattice before running model");
        if (probe == null)
            throw new IllegalStateException(
                "must initialize probe before running model");
        
        // TODO - remove debugging code
//        System.out.println("HO HO HO I am in xal.sim.Scenario#run()");
//        System.out.println("  getStartElementId() = " + this.getStartElementId());
//        System.out.println("  getStopElementId() = " + this.getStopElementId());
//        System.out.println();
        
        // Set the starting and stopping elements
        IAlgorithm  alg = probe.getAlgorithm();
        
        if (this.getStartElementId() != null)
            alg.setStartElementId( this.getStartElementId() );
        else
            alg.unsetStartElementId();
            
        if (this.getStopElementId() != null) {
            alg.setStopElementId( this.getStopElementId() );
            alg.setIncludeStopElement(this.bolInclStopElem);
        } else
            alg.unsetStopElementId();
        
        // Propagate probe
        probe.initialize();
        probe.update();
        
        lattice.propagate(probe);

//        probe.performPostProcessing();
    }
    
	
    /**
     * Returns the lattice.  NOTE: I (Craig M) don't like this here.  I only
     * added it so that I could keep ModelProxy working as it presently does.
     * Let's figure out a way to change it.  It seems that the only thing that
     * gets the Lattice from ModelProxy is OrbitDisplay2.  If it were changed
     * to use Scenario, then this method could be removed!
     * 
     * @return the Lattice
     */
    public Lattice getLattice() {
        return lattice;
    }
    
    public void setLattice(Lattice aLattice) {
    	lattice = aLattice;
    }
	
    /**
     * <h3>NOTE</h3>
     * <p>
     * Returns the trajectory obtained by running the model.
     *
     * &middot; The type of the object returned is actually <code>Trajectory&lt;?&gt;</code>
     * since the actual type of the trajectory is not known.  Any type of probe
     * may be used to run the scenario.  
     * <br>
     * <br>
     * &middot; This is simply a convenient way to avoid the clumsy Java
     * type casting, however, it is essentially the same thing.
     * <br>
     * <br>
     * &middot; A runtime cast exception will be thrown if the trajectory does
     * not match the probe type currently run.  
     * </p>
     * 
     * @return the Trajectory obtained by running the model
     * @throws IllegalStateException if the probe or trajectory is null
     */
    public <S extends ProbeState<S>> Trajectory<S> getTrajectory() {
        if (probe == null)
            throw new IllegalStateException("scenario doesn't contain a probe");
        if (probe.getTrajectory() == null)
            throw new IllegalStateException("model not yet run");
        
        @SuppressWarnings("unchecked")
        Trajectory<S>   trj = (Trajectory<S>) probe.getTrajectory();
        return trj;
        
//        return  probe.getTrajectory();
    }
        
    
    // Queries =================================================================
    
    /**
     * Returns a map of property values (key = String property name, value =
     * double property value) for the supplied node.
     * 
     * @param aNode AcceleratorNode whose properties to get
     * @return a Map of property values for the supplied node
     * @throws SynchronizationException if error getting properties
     * @throws IllegalArgumentException if aNode is null
     */
    public Map<String,Double> propertiesForNode( final AcceleratorNode aNode ) throws SynchronizationException {
        if (aNode == null)  throw new IllegalArgumentException( "node cannot be null getting property values" );
		return mgrSync.propertiesForNode(aNode);
    }
    
	
    /**
     * Returns the accelerator node with the specified id, or null if there is
     * none.
     * 
     * @param id String id of the node to return
     * @return AcceleratorNode with specified id
     */
    public AcceleratorNode nodeWithId( final String id ) {
        return smfSeq.getNodeWithId(id);
    }
    
	
    /**
     * Returns a List of elements mapped to the specified node.
     * 
     * @param aNode node to get elements mapped to
     * @return a List of Elements mapped to the specified node
     */
    public List<IElement> elementsMappedTo( final AcceleratorNode aNode ) {
        return mgrSync.allElementsMappedTo(aNode);
    }
    
	
    /**
     * Returns an array of the trajectory states for the specified element id.
     * 
     * @param id element id to find states for
     * @return array of trajectory states for specified element id
     * @throws ModelException if the probe is not yet propagated
     */
    public List<? extends ProbeState<?>> trajectoryStatesForElement( final String id ) throws ModelException {
        if (probe == null)
            throw new ModelException("Probe is null");
        return probe.getTrajectory().statesForElement(id);
    }
    
	
    /**
     * Return the string identifier of the modeling element where propagation
     * starts.
     * 
     * @return      modeling element string identifier
     */
    public String getStartElementId() {
        return idElemStart;
    }
    
	
    /**
     * Set the "start" element by String id
     * @param elemId Start element Id
     */
    public void setStartElementId( final String elemId ) {
        idElemStart = elemId;
    }

	
    /**
     * Return the string identifier of the modeling element where propagation
     * stops.
     * 
     * @return      modeling element string identifier
     */
    public String getStopElementId() {
        return idElemStop;
    }
    
	
    /**
     * Set the "stop" element by String id
     * @param elemId Stop element Id
     */
    public void setStopElementId( final String elemId ){
        idElemStop = elemId;
    }
    
	
    /**
     * Sets the specified node's property to the specified value.  Replaces the
     * existing value if there is one.
     * 
     * @param aNode node whose property to set
     * @param propName name of property to set
     * @param val double value for property
     */
    public ModelInput setModelInput( final AcceleratorNode aNode, final String propName, final double val ) {
        return mgrSync.setModelInput(aNode, propName, val);
    }
    
	
    /**
     * Returns the ModelInput for the specified node's property.
     * 
     * @param aNode node whose property to get a ModelInput for
     * @param propName name of property to get a ModelInput for
     */
    public ModelInput getModelInput( final AcceleratorNode aNode, final String propName ) {
        return mgrSync.getModelInput(aNode, propName);
    }
    
	
    /**
     * Removes the model input for the specified property on the specified node,
     * if there is one.
     * 
     * @param aNode node whose input to remove
     * @param property name of property whose input to remove
     */
    public void removeModelInput( final AcceleratorNode aNode, final String property ) {
        mgrSync.removeModelInput(aNode, property);
    }
    
    
    /**
     * Sets the supplied probe for this scenario.
     * 
     * @param aProbe the probe to be used by the scenario
     */
    public void setProbe( final Probe<?> aProbe ) {
        probe = aProbe;
    }
    
	
    /**
     * Returns the scenario's current probe, or null if there is none.
     * 
     * @return the scenario's current probe or null
     */
    public Probe<?> getProbe() {
        return probe;
    }
    
	
    /**
     * Resets the probe to its initial state - before propagation (e.g., the
     * state specified in the probe xml file).
     */
    public void resetProbe() {
        if (probe != null) probe.reset();
    }
    
    
    /** Testing Support */
    public boolean checkSynchronization( final AcceleratorNode aNode, final Map<String,Double> values ) throws SynchronizationException {
        return mgrSync.checkSynchronization(aNode, values);
    }
    
	
    /**
     * remove previously set Start point
     */
    public void unsetStartNode() {
        idElemStart = null;
    }
    
	
    /**
     * remove previously set Stop point
     */
    public void unsetStopNode() {
        idElemStop = null;
    }
}
