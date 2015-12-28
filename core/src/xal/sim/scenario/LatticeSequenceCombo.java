/**
 * LatticeSequenceCombo.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jan 20, 2015
 */
package xal.sim.scenario;

import xal.model.IComposite;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;

/**
 * <p>
 * This class is an extension of the <code>LatticeSequence</code> class needed for
 * generating modeling lattices from <code>AcceleratorSeqCombo</code> hardware
 * structures.  We need an extra level of processing to account for the fact that
 * <code>AcceleratorSeqCombo</code> flattens out its constituent sequence objects
 * when requesting the accelerator nodes, that is, you must ask for the constituent
 * sequences explicitly.
 * </p>
 * <p>
 * Given a valid <code>ElementMapping</code> object, <code>AcceleratorSeqCombo</code> object, 
 * and <code>SynchronizationManager</code> for the online model, 
 * the <code>LatticeSequenceCombo</code> will create an online model <code>Lattice</code>
 * object.  This object is used by a <code>ScenarioGenerator</code> to create
 * a new <code>Scenario</code> object.
 * </p>
 * <p>
 * Thus, the actual "lattice generation" is done mostly in the <code>LatticeSequence</code>
 * class.  The peripheral resources such as
 * hardware node to model element mappings and synchronization managers are created outside
 * this class.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since  Jan 20, 2015
 */
public class LatticeSequenceCombo extends LatticeSequence {

    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Constructor for lattice combo sequences.  Instantiates a new <code>LatticeSequenceCombo</code>
     * object for the given accelerator combo sequence under the assumption that that sequence
     * is the top level (i.e., not a sub-sequence).
     * </p>
     *
     * @param smfSeqCmbRoot top level associated hardware accelerator sequence
     * @param mapNodeToElem the mapping of SMF hardware nodes to modeling element class types  
     *
     * @since  Jan 20, 2015   by Christopher K. Allen
     */
    public LatticeSequenceCombo(AcceleratorSeqCombo smfSeqCmbRoot, ElementMapping mapNodeToElem) {
        super(smfSeqCmbRoot, mapNodeToElem);
    }

    
    /*
     * Attributes
     */
    
    /**
     * Overrides the base class implementation to return a down class to 
     * <code>{@link AcceleratorSeqCombo}</code>.  Since this class can only be
     * instantiated with an <code>AcceleratorSeq</code> of this child type, this
     * is a save operation.
     *
     * @see xal.sim.scenario.LatticeSequence#getHardwareNode()
     *
     * @since  Jan 20, 2015   by Christopher K. Allen
     */
    @Override
    public AcceleratorSeqCombo getHardwareNode() {
        return (AcceleratorSeqCombo)super.getHardwareNode();
    }

    
    /*
     * Operations
     */
    
    /**
     *
     * @see xal.sim.scenario.LatticeSequence#createModelLattice(xal.sim.sync.SynchronizationManager)
     *
     * @since  Jan 20, 2015   by Christopher K. Allen
     */
    @Override
    public Lattice createModelLattice(SynchronizationManager mgrSync) throws ModelException {
        
        AcceleratorSeqCombo smfSeqCombo = this.getHardwareNode();
        ElementMapping      mapNod2Elem = this.getNodeToElementMap();
        Lattice             mdlLatRoot  = this.createParentLattice();
        
        for (AcceleratorSeq smfSeq : smfSeqCombo.getConstituents()) {
            LatticeSequence lemSeq = new LatticeSequence(smfSeq, mapNod2Elem);
            IComposite      mdlSeq = lemSeq.createModelSequence(mgrSync);
            
            mdlLatRoot.addChild(mdlSeq);
        }

        return mdlLatRoot;
    }

}
