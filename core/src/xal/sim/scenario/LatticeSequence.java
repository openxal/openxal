/**
 * LatticeSequence.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 8, 2014
 */
package xal.sim.scenario;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.sim.sync.SynchronizationManager;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Magnet;
import xal.smf.impl.Marker;
import xal.smf.impl.RfCavity;

/**
 * <p>
 * The Open XAL online model <em>lattice generator</em> is primarily contained here.  
 * </p>
 * <p>
 * Given a valid <code>ElementMapping</code> object, <code>AcceleratorSeq</code> object, 
 * and <code>SynchronizationManager</code> for the online model, 
 * the <code>LatticeSequence</code> will create an online model <code>Lattice</code>
 * object.  This object is used by a <code>ScenarioGenerator</code> to create
 * a new <code>Scenario</code> object.
 * </p>
 * <p>
 * Thus, the actual "lattice generation" is done here.  The peripheral resources such as
 * hardware node to model element mappings and synchronization managers are created outside
 * this class.
 * </p>  
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 8, 2014
 */
public class LatticeSequence extends LatticeElement implements Iterable<LatticeElement> {

    
    /*
     * Global Constants
     */
    
    /** Small number - usually the minimum drift length */
    public static final double EPS = 1.e-10d;
    
    
    /*
     * Local Attributes
     */
    
    /** container list of lattice element associations (some may be lattice sequences) */
    private       List<LatticeElement>       lstLatElems;
    
    /** convenience container of direct lattice subsequences */
    private final List<LatticeSequence>       lstSubSeqs;
    
    
    //
    // Generation Parameters
    //
    
    /** The set of hardware node to element mapping associations used to create model elements */
    private final ElementMapping        mapNodeToMdl;
    
    /** indicates whether or not axis coordinate have origin at sequence center */
    private boolean                     bolCtrOrigin;
    
    
    //
    // RF Cavity Parameters
    //
    
    /** The operating RF frequency if this sequence represents an RF cavity */
    private final double    dblCavFreq;
    
    /** The coupling constant between cavities in an RF coupled cavity structure */
    private final double    dblCavMode;


    //
    // Debugging 
    ///
      
    /** destination for debugging information */
    private PrintStream     ostrDebug;
    
    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for lattice sequences.  Instantiates a new <code>LatticeSequence</code>
     * object for the given accelerator sequence under the assumption that that sequence
     * is the top level (i.e., not a sub-sequence).
     * </p>
     * <p>
     * <h4>NOTES</h4>
     * &middot; The lattice element position and index parameters for the base class are both set
     * to zero since this is the root of the lattice structure.
     * </p>
     *
     * @param smfSeqRoot    top level associated hardware accelerator sequence
     * @param mapNodeToElem the mapping of SMF hardware nodes to modeling element class types  
//     * @param dblPos        position of the accelerator sequence within parent (if applicable)
//     * @param clsSeq        class type of the modeling element used to represent the hardware
//     * @param indOrgPos     index of the sequence within its parent sequence (if applicable)
     *
     * @since  Dec 8, 2014  @author Christopher K. Allen
     */
    public LatticeSequence(AcceleratorSeq smfSeqRoot, ElementMapping mapNodeToElem) {
        super(smfSeqRoot, smfSeqRoot.getLength()/2.0, mapNodeToElem.getModelSequenceType(smfSeqRoot), 0);
        
        this.lstLatElems = new LinkedList<>();
        this.lstSubSeqs  = new LinkedList<>();

        this.mapNodeToMdl = mapNodeToElem;        
        this.bolCtrOrigin = false;
                
        this.ostrDebug = System.out;
        
        if (smfSeqRoot instanceof RfCavity) {
            RfCavity    seqRfCav = (RfCavity)smfSeqRoot;
            
            this.dblCavFreq = seqRfCav.getCavFreq() * 1e6;
            this.dblCavMode = seqRfCav.getStructureMode();
        } else {
            
            this.dblCavFreq = 0.0;
            this.dblCavMode = 0.0;
        }
    }

    
    /**
     * <p>
     * Internal constructor for lattice sequences.  This constructor is called when making
     * subsequences within the root or parent sequences.  That is, this sequence is intended
     * to be the child of the provided lattice sequence.
     * </p>
     * <p>
     * This constructor needs the 
     * additional arguments of lattice position and index within its parent sequence, which
     * are necessary in the <em>lattice element</em> creations.  The lattice elements use
     * these values to order themselves, split themselves, and create drifts between them.  
     * </p>
     * <p>
     * We need to use a constructor when instantiating new lattice sequences in order to
     * call the base class constructor 
     * (i.e., <code>LatticeElement{@link #addLatticeElement(LatticeElement)}</code> to pass
     * in the appropriate accelerator hardware and hardware to element mappings.
     * </p> 
     *
     * @param latSeqParent  the parent lattice of this lattice
     * @param smfSeqChild  the associated hardware accelerator sequence for which this lattice 
     *                      is being created, i.e., the associated child sequence for the given parent 
     *                      lattice sequence 
     * @param dblPos        position of the accelerator sequence within parent (if applicable)
     * @param indOrgPos     index of the sequence within its parent sequence (if applicable)
     *
     * @since  Dec 12, 2014   @author Christopher K. Allen
     */
    private LatticeSequence(LatticeSequence latSeqParent, AcceleratorSeq smfSeqChild, double dblPos, int indOrgPos) {
        super(smfSeqChild, dblPos + smfSeqChild.getLength()/2.0, latSeqParent.getSequenceModelType(smfSeqChild), indOrgPos);

        this.lstLatElems = new LinkedList<>();
        this.lstSubSeqs  = new LinkedList<>();

        this.mapNodeToMdl = latSeqParent.mapNodeToMdl;        
        this.bolCtrOrigin = false;
        
        this.ostrDebug    = latSeqParent.ostrDebug;

        if (smfSeqChild instanceof RfCavity) {
            RfCavity    seqRfCav = (RfCavity)smfSeqChild;

            this.dblCavFreq = seqRfCav.getCavFreq() * 1e6;
            this.dblCavMode = seqRfCav.getStructureMode();
        } else {

            this.dblCavFreq = 0.0;
            this.dblCavMode = 0.0;
        }
    }
    
    /*
     * Attribute Queries
     */
    
    /**
     * Indicates whether or not the associated hardware accelerator sequence 
     * is an RF cavity structure.  Such structures are derived from the base
     * class <code>{@link xal.smf.impl.RfCavity}</code>.
     * 
     * @return  <code>true</code> if the accelerator sequence is and RF cavity,
     *          <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    public boolean isRfCavity() {
        
        return (super.getHardwareNode() instanceof RfCavity);
    }
    
    /**
     * Returns whether or the origin of the axis is at the center of the 
     * sequence, normally it is located at the entrance of the sequence.
     * 
     * @return  <code>true</code> if the axis origin is at the center of the sequence,
     *          <code>false</code> otherwise (likely at the sequence entrance)
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    public boolean isAxisOriginCentered() {
        return this.bolCtrOrigin;
    }
    
    /**
     * Convenience method for getting the modeling element type for the given
     * accelerator sequence according to the current node to element mapping.
     * 
     * @param smfSeq    accelerator sequence hardware object to be looked up in the map
     * 
     * @return          modeling class for the given accelerator sequence
     *
     * @since  Dec 12, 2014   @author Christopher K. Allen
     */
    public Class<? extends IComposite> getSequenceModelType(AcceleratorSeq smfSeq) {
        Class<? extends IComposite> clsSeqMdl = this.mapNodeToMdl.getModelSequenceType(smfSeq);
        
        return clsSeqMdl;
    }
    
    /**
     * Returns the map of SMF hardware nodes to online modeling elements, or more
     * specifically, modeling element class types.  This map is used when creating
     * a modeling element for any given hardware node.
     * 
     * @return  mapping between hardware nodes and modeling elements used to 
     *          generate the model lattice
     *
     * @since  Jan 21, 2015   by Christopher K. Allen
     */
    public ElementMapping   getNodeToElementMap() {
        return this.mapNodeToMdl;
    }
    

    /*
     * Operations
     */
    
    /**
     * Creates a new model lattice object according to the configuration of this lattice
     * sequence.  The given synchronization manager is populated with synchronization 
     * associations for the returned model lattice.  However, the synchronization manager
     * is still unbound to any scenario.
     *   
     * @param mgrSync       synchronization manager to receive synchronization associations 
     *                      for the model elements in the returned model lattice
     *                      
     * @return              new model lattice with the configuration provided by the 
     *                      accelerator sequence given to the public constructor
     *                      
     * @throws ModelException   problem instantiating modeling elements
     *
     * @author Christopher K. Allen
     * @since  Dec 11, 2014
     */
    public xal.model.Lattice createModelLattice(SynchronizationManager mgrSync) throws ModelException {
       
        // Create the online model element sequence corresponding to this lattice sequence
        IComposite  mdlSeq = this.createModelSequence(mgrSync);

        
        // Create new lattice modeling object then add the sequence 
        Lattice mdlLattice = this.createParentLattice();
        
        mdlLattice.addChild(mdlSeq);

        
        // Return the lattice object
        return mdlLattice;
    }
    
    /**
     * Returns an iterator that will visit all the direct subsequences of this 
     * sequence.
     * 
     * @return  iterator for all the <b>direct</b> subsequences of this sequence
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    public final List<LatticeSequence>    getSubSequences() {
        return this.lstSubSeqs;
    }
    
    
    /*
     * Iterable Interface
     */

    /**
     * Iterates element by element through the element lattice
     * from head to tail.
     *
     * @see java.lang.Iterable#iterator()
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    @Override
    public Iterator<LatticeElement> iterator() {
        return this.lstLatElems.iterator();
    }


    /*
     * LatticeElement Overrides
     */

    /**
     * Always returns <code>false</code>
     *
     * @see xal.sim.scenario.LatticeElement#isThin()
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    @Override
    public boolean isThin() {
        return false;
    }
    
    /**
     *
     * @see xal.sim.scenario.LatticeElement#getModelingClass()
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends IComposite> getModelingClass() {
        return (Class<? extends IComposite>) super.getModelingClass();
    }

    /**
     * Overrides the base class <code>{@link LatticeElement#getHardwareNode()}</code>
     * method so that it down casts the result to the tighter type of
     * <code>AcceleratorSeq</code> rather than <code>AcceleratorNode</code>. 
     * This class can be instantiated only with a <code>AcceleratorSeq</code> derived
     * hardware node, we are just enforcing it here.
     *
     * @return  the accelerator sequence hardware associated with this lattice sequence object
     *  
     * @see xal.sim.scenario.LatticeElement#getHardwareNode()
     *
     * @author Christopher K. Allen
     * @since  Dec 11, 2014
     */
    @Override 
    public AcceleratorSeq getHardwareNode() {
        AcceleratorSeq  smfSeq = (AcceleratorSeq)super.getHardwareNode();
        
        return smfSeq;
    }
    
    /**
     * <p>
     * Creates and initializes a new modeling composite sequence represented by this
     * object.  Java reflection is used to create a new instance from the sequence's
     * class type.  There must be a zero constructor for the element.
     * </p>  
     * <p>
     * <h4>CKA Notes</h4>
     * &middot; The parameter initialization is done by calling the 
     * <code>{@link IComponent#initializeFrom(LatticeElement)}</code>.  This design
     * effectively couples the <code>xal.model</code> online model subsystem to the 
     * <code>xal.smf</code> hardware representation subsystem.  These
     * systems should be independent, able to function without each other.
     * </p>
     * 
     * @return    a new modeling sequence for the hardware proxied by this object
     * 
     * @throws ModelException  Java reflection threw an <code>InstantiationException</code>
     *
     * @since  Jan 16, 2015
     */
    @Override
    public IComposite createModelingElement() throws ModelException {        
        IComponent component = super.createModelingElement();        
        IComposite mdlSeq = (IComposite)component;

        return mdlSeq;
    }

//    /**
//     *
//     * @see xal.sim.scenario.LatticeElement#createModelingElement()
//     *
//     * @author Christopher K. Allen
//     * @since  Dec 9, 2014
//     */
//    @Deprecated
//    @Override
//    public IComposite createModelingElement() throws ModelException {
//
//        Class<? extends IComposite> clsSeq = this.getModelingClass();
//        try {
//            IComposite modSeq = clsSeq.newInstance();        
//            modSeq.initializeFrom(this);
//
//            return modSeq;
//        } catch (InstantiationException | IllegalAccessException e) {
//            String  strMsg  = "Exception while instantiating class " + 
//                    clsSeq.getName() +
//                    " for node " + 
//                    this.getHardwareNode().getId();
//
//            throw new ModelException(strMsg, e);
//        }
//    }

    
    /*
     * Object Overrides
     */
    
    /**
     * Lists all the internal lattice elements line by line.
     *
     * @see xal.sim.scenario.LatticeElement#toString()
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    @Override
    public String toString() {
        StringBuffer    bufOutput = new StringBuffer();

        bufOutput.append(super.toString());
        bufOutput.append('\n');

        for (LatticeElement elem : this) {
            bufOutput.append(elem.toString());
            bufOutput.append('\n');
        }

        return bufOutput.toString();
    }


    /*
     * Child Class Support
     */
    
    /**
     * Builds an online model element sequence for the accelerator sequence provided to
     * the public constructor of this class.  This is done by calling the private methods
     * {@link #populateLatticeSeq()}, {@link #splitSequenceElements()}, and 
     * {@link #createModelElements(SynchronizationManager)} in that order.  When the
     * actual modeling elements are instantiated (via {@link #createModelElements(SynchronizationManager)})
     * they are connected to the given synchronization manager.
     *  
     * @param mgrSync       synchronization manager to receive synchronization associations 
     *                      for the new created model elements 
     *                      
     * @return              new online model sequence with the configuration provided by the 
     *                      accelerator sequence given to the public constructor
     *                      
     * @throws ModelException   problem instantiating modeling elements
     *
     * @since  Jan 21, 2015   by Christopher K. Allen
     */
    protected IComposite createModelSequence(SynchronizationManager mgrSync) throws ModelException {
        
        // Create the lattice elements to populate this sequence and any subsequences
        //  The sequence elements are also sorted in this step
        this.populateLatticeSeq();
        
        // Make sure the axis origin of this sequence is at the entrance.
        this.enforceAxisOrigin();
        
        // Move any orphaned elements to their direct parents.  Any hanging elements are
        //  pushed down to sequences where they live within the axial location of the 
        //  parent.
        this.placeOrphanedElements();
        
        // Sort the elements in this sequence and all subsequences recursively
        this.sortElements();
        
        // Split any thick lattice elements where a thin element intersects it.
        //  If two thick elements intersect then we bail out with a ModelException
        this.splitSequenceElements();
        
        // Create the modeling element representing this lattice sequence.
        //  Recall that this lattice sequence must be a top-level sequence
        //  since users do not have access to the sub-lattice constructor
        IComposite mdlSeq = this.createModelElements(mgrSync);
        
//        // Identify the first gaps in any RF Cavity and sub-cavities, then do any
//        //  processing as necessary.  Right now this method does nothing since the
//        //  only action, setting the isFirstGap flag, is accomplished via the XDXF
//        //  configuration file.
//        this.markFirstCavityGap(mdlSeq);
        
        return mdlSeq;
    }
    
    /**
     * Creates the actual model lattice object from the given element sequence.
     * Some of the cursory fields of the lattice are populated.
     * 
     * @param modSeqRoot    primary modeling element sequence
     * 
     * @return              model lattice object containing the given root sequence
     *
     * @since  Jan 20, 2015   by Christopher K. Allen
     */
    protected Lattice createParentLattice() {
        
        // Create new lattice modeling object
        Lattice mdlLattice = new Lattice();
        
        // Fill in the meta data and comments for the lattice
        AcceleratorSeq  smfSeqRoot = this.getHardwareNode();
        Accelerator     smfAccel   = smfSeqRoot.getAccelerator();
        
        String  strComment = "";
        if (smfAccel != null) strComment += "Accelerator ID:" + smfAccel.getId() + ", "; 
        strComment += "Sequence ID: " + smfSeqRoot.getId() + ", ";
        strComment += "Date: " + Calendar.getInstance().getTime() + ", ";
        strComment += "Version soft type: " + smfSeqRoot.getSoftType() + ", ";
        strComment += "Generated from : " + this.getClass().getName();

        mdlLattice.setId(smfSeqRoot.getId());
        mdlLattice.setHardwareNodeId(smfSeqRoot.getEntranceID());
        mdlLattice.setVersion("Version soft type: " + smfSeqRoot.getSoftType());
        mdlLattice.setComments(strComment);
        
        return mdlLattice;
    }
    
    
    /*
     * Internal Support 
     */

    /**
     * <p>
     * CKA : I have modified the original method so that it is now recursive.  It calls
     * itself whenever it finds a hardware node within the given accelerator sequence
     * which is actually another accelerator sequence.  In that case another 
     * lattice sequence is created (which is also a lattice element) and added to the
     * current lattice sequence as a lattice element.
     * </p>
     * <p>
     * Collects all elements in the smfSequence into a single list, recording element's original position.
     * </p>
     *  <p>
     *  Adds begin and end marker.
     *  </p>
     *  <p>
     *  If <code>bolDivMags</code> is set, also adds a center marker for each magnet.
     *  </p>
     *  
     * @return collected elements with begin, end, and possibly center markers
     */
    private void    populateLatticeSeq() {

        // Retrieve the accelerator sequence
        AcceleratorSeq  smfSeqRoot = this.getHardwareNode();
        
        // Loop variables
        int     indSeqPosition = 0;                        // used to record original position 
        double  dblLenSeq   = smfSeqRoot.getLength(); // workaround for sequences that don't have length set
                                                        //  This is also the location of the sequence end marker
        
        // The returned modeling element sequence
//        String                      strSeqId  = smfSeqParent.getId();
//        double                      dblSeqPos = smfSeqParent.getParent().getPosition(smfSeqParent);  
//        Class<? extends IComposite> clsSeqTyp = this.mapNodeToMdl.getModelSequenceType(smfSeqParent);
//
//        LatticeSequence latSeqParent = new LatticeSequence(smfSeqParent, dblSeqPos, clsSeqTyp, 0);
        
        

        
        // Now we generate lattice association objects for every hardware node 
        //  (including sequences) in the accelerator sequence which is marked as 
        //  having good status.
        for ( AcceleratorNode smfNodeCurr : smfSeqRoot.getNodes( true ) ) {

            
            // Recursively call this method to drill down and get any sub sequences
            if (smfNodeCurr instanceof AcceleratorSeq) {
                
                // Create a new lattice subsequence for this accelerator hardware node
                AcceleratorSeq    smfSeqChild = (AcceleratorSeq)smfNodeCurr;
                
                double          dblSubSeqPos = smfSeqRoot.getPosition(smfSeqChild);  
                LatticeSequence latSeqChild  = new LatticeSequence(this, smfSeqChild, dblSubSeqPos, indSeqPosition++);
                
                // Now generate the lattice structure for the child sequence and all
                //  its children
                latSeqChild.populateLatticeSeq();
                latSeqChild.bolCtrOrigin = mapNodeToMdl.isSubsectionAxisOriginCentered();
                
                // Added the populated child lattice to this lattice
                this.addLatticeElement(latSeqChild);
                
                // Skip the rest and continue on to the next hardware node
                continue;
            }
            
            // The current hardware node is atomic with no children.
            //  Create a new lattice element for the accelerator node and add it to the 
            //  this lattice sequence
            double                      dblNodePos = smfSeqRoot.getPosition(smfNodeCurr);
            Class<? extends IComponent> clsElemTyp = this.mapNodeToMdl.getModelElementType(smfNodeCurr);
            LatticeElement              latElem    = new LatticeElement(smfNodeCurr, dblNodePos, clsElemTyp, indSeqPosition);

            this.addLatticeElement(latElem);
            indSeqPosition++;
            
            if (mapNodeToMdl.isDebugging()) {
                this.ostrDebug.println("LatticeSequence#populateLatticeSeq(): " + 
                                        latElem.toString() + 
                                       ", thin=" + 
                                        latElem.isThin()
                                        );                      
            }
            
            // Check if the last lattice element extends beyond the current length of the 
            //  lattice sequence.  If so then extend the running length of the lattice sequence.
            if (latElem.getEndPosition() > dblLenSeq) 
                dblLenSeq = latElem.getEndPosition();
            
            
            // We need to add a thin element marker to the center of any magnet if the
            //  divide magnets flag is true.  Thus, check if the current hardware node
            //  is a magnet, the flag is true, and the hardware node modeling element is
            //  not a thin element.  If so, add the center marker.
            if (mapNodeToMdl.isMagnetDivided() && (smfNodeCurr instanceof Magnet) && !latElem.isThin()) {

                String                      strNodeId   = smfNodeCurr.getId();
                double                      dblPosCtr   = latElem.getCenterPosition();
                Class<? extends IComponent> clsMrkrTyp  = this.mapNodeToMdl.getDefaultElementType();
                AcceleratorNode             smfCntrMrkr = new Marker( strNodeId + "-Center" );
                
                LatticeElement  latCtrElem  = new LatticeElement(smfCntrMrkr, dblPosCtr, clsMrkrTyp, 0); 
                latCtrElem.setModelingElementId("CENTER:" + smfNodeCurr.getId());    // CKA Sep 5, 2014: dashes seem to break lookups
                this.addLatticeElement(latCtrElem);
            }
        }
        

    }
    
    /**
     * If this sequence has its axial origin at the center of the sequence
     * then all the elements are translated such that the new origin is at
     * the entrance of the sequence.
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    private void enforceAxisOrigin() {
        // First recursively translate all elements of all subsequences
        for (LatticeSequence lsq : this.lstSubSeqs) 
            lsq.enforceAxisOrigin();

        // Check if the origin is at the sequence center and if so move it to the entrance
        if (this.isAxisOriginCentered()) {

            // Compute the translation 
            double dblOffset = this.getLength()/2.0;

            // Translate all the non-artificial element along the sequence axis
            for (LatticeElement lem : this) { 
                lem.axialTranslation(dblOffset);
            }
            
            axialTranslation(-dblOffset);
            
            this.bolCtrOrigin = false;
        }
        
        // Following code checks that the subsequence lattice elements are within the limits.
        // If they weren't the length of the lattice magically grows!
        double dblBeginSeqChild = Double.POSITIVE_INFINITY, dblEndSeqChild = Double.NEGATIVE_INFINITY;
        for (LatticeElement el : this) {
            if (el.getStartPosition() < dblBeginSeqChild) dblBeginSeqChild = el.getStartPosition();
            if (el.getEndPosition() > dblEndSeqChild) dblEndSeqChild = el.getEndPosition();
        }        
        if (dblBeginSeqChild < -EPS || dblEndSeqChild > getLength() + EPS) {
            System.err.printf("Error: Elements do not fit sequence %s (element positions: [%f,%f], sequence position [0,%f])!\n", 
                    getHardwareNode().getId(), dblBeginSeqChild, dblEndSeqChild, getLength());
            
            
            // Now lets try to fix this. If we fail, some thick element will cover another.
            // Translate all the non-artificial element along the sequence axis
            for (LatticeElement lem : this) { 
                lem.axialTranslation(-dblBeginSeqChild);                
            }
            axialTranslation(dblBeginSeqChild);
            dblElemExitPos = dblElemEntrPos + (dblEndSeqChild - dblBeginSeqChild); 
            dblElemCntrPos = dblElemExitPos - dblElemEntrPos;
        }
    }


    /**
     * Looks for all the grand children of this sequence who are in direct ownership of
     * this sequence.  The method pushes them down the family tree until they are living with
     * their direct parents.  This is also a recursive method where each sequence pushes
     * the orphaned elements down one level until the recursion stops at the last 
     * lattice sequence in a branch.
     *
     * @since  Jan 28, 2015   by Christopher K. Allen
     */
    private void placeOrphanedElements() {

        List<LatticeElement>    lstElemsToRemove = new LinkedList<LatticeElement>();
        
        // Now check if any of my children are actually grand children
        for (LatticeSequence lsqChild : this.getSubSequences()) 

            // Looking through all my child sequences, see if the current child is
            //  contained in one.  If so we add it to the sequence and place it in the
            //  list of my child elements to be removed from my direct ownership 
            for (LatticeElement lemChild : this) {
                if ( lemChild != lsqChild && lemChild.isContainedIn(lsqChild) ) {

                    // Change coordinates to that of the new parent sequence
                    double  dblOffset = - lsqChild.getStartPosition();
                    lemChild.axialTranslation(dblOffset);

                    lsqChild.addLatticeElement(lemChild);
                    lstElemsToRemove.add(lemChild);

                    continue;
                }
            }

        // Remove all the lattice elements that have been moved to my child sequences
        this.removeAllLatticeElements(lstElemsToRemove);
        
//        // Now we recursively call this method on all my child sequences.
//        for (LatticeSequence lsqChild : this.getSubSequences())
//            lsqChild.placeOrphanedElements();
    }
    
    /**
     * <p>
     * Sorts the <code>LatticeElement</code> objects contained in this lattice 
     * sequence according to the natural ordering defined in <code>LatticeElement</code>.
     * </p>
     * <p>
     * Note that the natural ordering is defined by the 
     * <code>{@link LatticeElement#compareTo(LatticeElement)}</code> method required
     * by the <code>{@link java.lang.Comparable}</code> interface.
     * </p>
     * 
     * @since  Dec 10, 2014  @author Christopher K. Allen
     */
    private void sortElements() {
        Collections.sort(this.lstLatElems);
        
        for (LatticeSequence lsq : this.getSubSequences()) 
                lsq.sortElements();
    }
    
    /**
     * <p>Splits all thick elements by thin ones, keeping the order.</p>
     * <p>The method also checks if two thick elements cover each other, which should not happen.</p>
     * <p>Uses a simple scan line algorithm.</p>
     * <p>
     * <h4>CKA NOTES</h4>
     * &middot; The change to this sequence and its child lattice elements is irreversible. Once
     * lattice elements are split (via the <code>LatticeElement{@link #splitElementAt(LatticeElement)}</code>
     * method), the element cannot be reconstituted.
     * </p>
     *
     * @throws  ModelException  encountered a nonzero intersection of two thick elements (cannot process this) 
     */
    private void splitSequenceElements() throws ModelException {

        // This is the new list of lattice elements to replace our class attribute
        List<LatticeElement> lstSplitElems = new ArrayList<>();

        // Go through each lattice element contained in this lattice sequence
        //  and split thick elements that have thin elements juxtaposed.
        //  We if the lattice element is also a nested lattice sequence, then
        //  we call this method on it (recursion).

        // Initialize the loop by setting the last element of the sequence to null
        LatticeElement  lemLastThick = null;

        for (LatticeElement lemCurr : this) {

            // loop invariant: 
            //   scanline is at elemCurr.startPosition()
            //   all the intersections before scanline have already been accounted for
            //   variable elemLastThick has the thick element before or under the scanline, 
            //   if there is one.

            // First check if there is a thick element to be processed. 
            if (lemLastThick == null) {

                // If none and the current element is thin add it directly to  
                //  the list of split elements.
                if (lemCurr.isThin()) 
                    lstSplitElems.add(lemCurr);

                // If none and the current element is thick then set it up for processing 
                //  next round. 
                else 
                    lemLastThick = lemCurr;

                // Now skip everything else and continue on to the next lattice element in 
                //  the lattice sequence.
                continue;
            }


            //
            // There are four cases to process:

            // 1) The last thick element is actually a lattice sequence.
            if (lemLastThick instanceof LatticeSequence) {

                // Down cast it to its true type
                LatticeSequence lsqLast = (LatticeSequence)lemLastThick;

                // Check if there is a collision between the current element and 
                //  the lattice sequence.  This should not normally occur so we throw
                //  up an exception if we find this is so.
                if (lemCurr.getStartPosition() - lsqLast.getEndPosition() < -EPS)
                    throw new ModelException("Collision between a nested sequence " + 
                            lsqLast.getHardwareNode().getId() +
                            " and its parent child node " +
                            lemCurr.getHardwareNode().getId()
                            ); 

                // Recursively split up the elements of the child sequence then add it 
                //  to the list of split elements. Null out the last thick element.
                lsqLast.splitSequenceElements();
                lstSplitElems.add(lsqLast);
                lemLastThick = null;

                // If the current lattice element is thin we add it to the list of sequence
                //  elements.
                if (lemCurr.isThin())
                    this.addSplitElementTo(lstSplitElems, lemCurr);
                // Or if it is a thick element we set the last thick element to it.
                else
                    lemLastThick = lemCurr;

            // 2) The current element lives outside the last thick element.
            } else if (lemLastThick.getEndPosition() - lemCurr.getStartPosition() <= EPS) {

                // If so then add the last thick element to the list of split element and 
                //  zero out the last thick element reference.
                this.addSplitElementTo(lstSplitElems, lemLastThick);
                lemLastThick = null;

                // Also, if the current element is thin, it too goes in the list of 
                //  split elements,
                if (lemCurr.isThin())
                    this.addSplitElementTo(lstSplitElems, lemCurr);                     

                // Or if it is thick it becomes the new last lattice element.
                else
                    lemLastThick = lemCurr;

            // 3) The current element is inside the last thick element and it is a thin element.
            } else if (lemCurr.isThin()) { 

                if (mapNodeToMdl.isDebugging()) {
                    ostrDebug.println("splitElements: replacing " + lemLastThick.toString() + " with");
                }

                // Split the last thick element
                //  We split the last thick element at the current element's center position
                LatticeElement elemSplitPartEnd = lemLastThick.splitElementAt(lemCurr);

                if (mapNodeToMdl.isDebugging()) {
                    ostrDebug.println("\t" + lemLastThick.toString());
                    if (elemSplitPartEnd != null) ostrDebug.println("\t" + elemSplitPartEnd.toString());
                }                   

                // Add the first part of the newly split element to the list of split elements
                //  then null out the last thick element reference.
                if (lemLastThick.getEndPosition() <= lemCurr.getCenterPosition()) {
                    addSplitElementTo(lstSplitElems, lemLastThick);                      
                    lemLastThick = null;
                }

                // Add the current element to the list of split elements (remember, it is thin) 
                lstSplitElems.add(lemCurr);

                // The second part of the split thick element becomes the new last thick element. 
                if (elemSplitPartEnd != null)
                    lemLastThick = elemSplitPartEnd;

            // 4) There exists a non-zero intersection between two thick elements. 
            //  We cannot process this case so just throw up out hands.
            } else {               

                throw new ModelException("Two covering thick elements: " + lemLastThick.toString() + 
                        " and " + lemCurr.toString());                           
            }
        }

        // If we have a thick element left over we add it to the list of lattice elements.
        if (lemLastThick != null) {
        	if (lemLastThick instanceof LatticeSequence) {
                // Down cast it to its true type
                LatticeSequence lsqLast = (LatticeSequence)lemLastThick;

                // Recursively split up the elements of the child sequence then add it 
                //  to the list of split elements. Null out the last thick element.
                lsqLast.splitSequenceElements();
                lstSplitElems.add(lsqLast);
                lemLastThick = null;
            // 2) The current element lives outside the last thick element.
            } else  {
                // If so then add the last thick element to the list of split element and 
                //  zero out the last thick element reference.
                this.addSplitElementTo(lstSplitElems, lemLastThick);
                lemLastThick = null;
            }
        }

        // Replace our list of unsplit lattice elements with the new list of split ones.
        this.lstLatElems = lstSplitElems;
    }
    
    /**
     * <p>Visits each element and invokes conversion on it, using element mapper on it.</p>
     * <p>Hooks synchronization manager to each element.</p>
     * <p>Adds drifts between the elements.</p>
     * <p>
     * <h4>CKA NOTES</h4>
     * &middot; Here we differentiate between default drift spaces and RF cavity drift
     * spaces depending upon whether or not this sequence represents an RF cavity strucure.
     * <br/>
     * <br/>
     * &middot; This is a recursive algorithm.  It calls itself whenever it finds a lattice
     * element that is actually a lattice sequence.
     * </p>
     * 
     * @param mgrSync          manager used to synchronize modeling elements with live hardware
     * @param mapNodeToMdlCls  the mapping association between hardware node types and modeling element clas types
     * 
     * @throws ModelException  not sure why this is thrown 
     */
    private IComposite createModelElements(SynchronizationManager mgrSync) throws ModelException {

        //
        //  Need to set up the loop state variables and parameters
        //

        // Running position of the last processed element
//        double dblPosLast = this.getStartPosition();
        double dblPosLast = 0.0;

        //        double position = smfAccelSeq.getPosition(); // always 0.0
        // Running count of the number of drift spaces
        int cntDrifts = 1;

        // The new model sector to be returned
        IComposite mdlSeqRoot = this.createModelingElement();

        // Loop through each lattice element in this lattice sequence
        for (LatticeElement latElemCurr : this) {

            //
            // Add a drift space as necessary

            // Compute the length of the drift space between the current element and the last element
            double dblPosEntr  = latElemCurr.isThin() ? latElemCurr.getCenterPosition() : latElemCurr.getStartPosition();
            double dblLenDrift = dblPosEntr - dblPosLast;

            // Test if the length of the drift is numerically significant
            if (dblLenDrift > EPS) { 
                this.createAndAppendDrift(cntDrifts, dblLenDrift, mdlSeqRoot);
                cntDrifts++;
                
//                // Create the drift element identifier string
//                String       strDriftId = "DR" + (++cntDrifts);
//
//                // If this lattice sequence represents an RF cavity we need an RF cavity drift
//                if (this.isRfCavity()) {
//                    IComponent mdlDrift = this.mapNodeToMdl.createRfCavityDrift(strDriftId, dblLenDrift, this.dblCavFreq, this.dblCavMode);
//
//                    mdlSeqRoot.addChild(mdlDrift);
//
//                    // Else we use a regular drift space
//                } else {
//                    IComponent   modDrift   = this.mapNodeToMdl.createDefaultDrift(strDriftId, dblLenDrift);
//
//                    mdlSeqRoot.addChild(modDrift);
//                }
            }

            // Fetch the associated hardware node of the current element for later use 
            AcceleratorNode smfNodeCurr = latElemCurr.getHardwareNode();

            // 
            // Must check whether or not the current element is a lattice sequence or
            //  lattice element.
            // If the current element is actually a lattice sequence then we ask it to
            //  create a new model sector which we add to our parent sector and the
            //  synchronization manager.
            if (latElemCurr instanceof LatticeSequence) {
                LatticeSequence latSeqCurr = (LatticeSequence)latElemCurr;

                IComposite mdlSeqChild = latSeqCurr.createModelElements(mgrSync);

                mdlSeqRoot.addChild(mdlSeqChild);
//                mgrSync.synchronize((IComposite)mdlSeqChild, smfNodeCurr);

//                if (mdlSeqChild instanceof IElement) 
//                    syncMgr.synchronize((IElement) mdlSeqChild, smfNodeCurr);
//                
//                if (mdlSeqChild instanceof IComposite)
//                    syncMgr.synchronize((IComposite)mdlSeqChild, smfNodeCurr);
//                

                // Advance the position of the last processed element
                dblPosLast = latSeqCurr.getEndPosition();

            // The current element is just a lattice element.  
            //  Create a modeling element for it and add it to the parent sector and
            //  the synchronization manager
            } else {

                IComponent mdlElemCurr = latElemCurr.createModelingElement();
                mdlSeqRoot.addChild(mdlElemCurr);

                if (mdlElemCurr instanceof IElement) 
                    mgrSync.synchronize((IElement) mdlElemCurr, smfNodeCurr);
                
                // Advance the position of the last processed element
                dblPosLast = latElemCurr.getEndPosition();
            }

            // Debug type out
            if (mapNodeToMdl.isDebugging())
                ostrDebug.println(latElemCurr.getHardwareNode().getId() + ": ==mapped to==>\t" + smfNodeCurr.getType()
                        + ": s= " + latElemCurr.getCenterPosition());           
        }
        
        // Determine if there is a drift space between the last element and the end
        //  of this sequence.  If so create it.
        double dblLenDrift = this.getLength() - dblPosLast;

        if (dblLenDrift > EPS) 
            this.createAndAppendDrift(cntDrifts, dblLenDrift, mdlSeqRoot);
        
        
        return mdlSeqRoot;
    }

//    /**
//     * <p>
//     * Currently this method is just a skeleton, it is nothing but a placeholder for 
//     * future modifications and upgrades.
//     * </p>
//     * <p>  
//     * In many modeling applications it
//     * is necessary to identify the first gap in an RF cavity in order to synchronize
//     * incoming particle phases.  At the moment there is a flag in the Open XAL 
//     * XDXF configuration file that identifies a gap as the first gap in a cavity.
//     * This flag is not needed since it is self evident which gap is first simply by
//     * its position within the cavity.  This method can identify first gaps and do
//     * whatever processing is necessary.
//     * </p>
//     *  
//     * @param mdlSec    model sequence object to process RF Cavity first gaps
//     *
//     * @since  Dec 15, 2014   @author Christopher K. Allen
//     * @deprecated  The first gap in any cavity does not need a special flag, you can
//     *              identify it as the first cavity in the sequence (i.e., by it's index).
//     */
//    @Deprecated
//    private void markFirstCavityGap(IComposite mdlSec) {
//
//        // We are going to search every direct child element of this model sequence
//        Iterator<IComponent>    iterElems = mdlSec.localIterator();
//        while ( iterElems.hasNext() ) {
//            IComponent mdlElem = iterElems.next();
//            
//            // If we find a composite element then we must check all of its elements (recursively)
//            if ( mdlElem instanceof IComposite ) {
//                IComposite  mdlComp = (IComposite)mdlElem;
//                
//                this.markFirstCavityGap( mdlComp );
//                
//            // We have found an RF gap, and it must be the first one because
//            //  we have not returned yet
//            } else if ( mdlElem instanceof IdealRfGap ) {
//                IdealRfGap  mdlFirstRfGap = (IdealRfGap)mdlElem;
//
//                // TODO: Do something necessary with the first gap
//                // mdlFirstRfGap.set
//                return;
//            }
//        }
//    }


    //
    // These methods are called by the support methods above
    //
    
    /**
     * Adds a <code>LatticeElement</code> association class to the tail of this sequence
     * of lattices elements. 
     * 
     * @param lem   lattice element association class to become element in this sequence
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    private void addLatticeElement(LatticeElement lem) {
        
        // Add to the list of elements
        this.lstLatElems.add(lem);
        
        // Check if the element is actually a sequence, 
        //  if so add it to the list of subsequences
        if (lem instanceof LatticeSequence)
            this.lstSubSeqs.add( (LatticeSequence)lem );
        
        // check if we have an element position less than zero, indicating an axis origin
        //  at the center of the sequence
        if (lem.getStartPosition() < 0.0 || 
            lem.getCenterPosition() < 0.0 || 
            lem.getEndPosition() < 0
            )
            this.bolCtrOrigin = true;
    }
    
    /**
     * Removes all the elements in the given set of elements from the contained set
     * of lattice element in this sequence;
     * 
     * @param setLems   set of lattice elements to be removed from this sequence
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    private void removeAllLatticeElements(Collection<LatticeElement> setLems) {
        this.lstLatElems.removeAll(setLems);
    }

    /**
     * Called by <code>{@link #splitElements(List)}</code> to add the given lattice
     * element into the given list of split elements.  The given element must meet 1
     * of 2 criteria: 1) it has finite length or 2) it represents no more than one
     * modeling element.  Otherwise the operation is rejected.
     *  
     * @param lstSplitElems     list of elements being split
     * @param latElemAddend        element to be added to above list
     *
     * @since  Dec 5, 2014
     */
    private void addSplitElementTo(List<LatticeElement> lstSplitElems, LatticeElement latElemAddend) {
        
        if (latElemAddend.getLength() > EPS || (latElemAddend.isFirstSlice() && latElemAddend.isLastSlice()))
            lstSplitElems.add(latElemAddend);
    }
    
    /**
     * Creates a new drift space of the appropriate length and type 
     * (w.r.t. the hardware that this lattice sequence represents) 
     * and appends it to the end of the given modeling
     * composite structure.
     * 
     * @param indId             used as an identifier index for the drift, e.g., "DR-indId"
     * @param dblLen            length of the drift space
     * @param mdlSeqRoot        the model sequence to be appended
     * 
     * @throws ModelException   the drift space could not be created
     *
     * @since  Feb 3, 2015   by Christopher K. Allen
     */
    private void createAndAppendDrift(int indId, double dblLen, IComposite mdlSeqRoot) throws ModelException {
        
        // Create the drift element identifier string
        String       strDriftId = "DR" + (indId);

        // If this lattice sequence represents an RF cavity we need an RF cavity drift
        if (this.isRfCavity()) {
            IComponent mdlDrift = this.mapNodeToMdl.createRfCavityDrift(strDriftId, dblLen, this.dblCavFreq, this.dblCavMode);

            mdlSeqRoot.addChild(mdlDrift);

            // Else we use a regular drift space
        } else {
            IComponent   modDrift   = this.mapNodeToMdl.createDefaultDrift(strDriftId, dblLen);

            mdlSeqRoot.addChild(modDrift);
        }
    }
    
}
