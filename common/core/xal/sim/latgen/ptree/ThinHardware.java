/*
 * Created on Mar 2, 2004
 *
 */
package xal.sim.latgen.ptree;

import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;

/**
 * Concrete subclass of the <code>TreeNode</code> hierarchy.  A 
 * <code>ThinHardware</code> object represents a section of hardware in a 
 * beamline that has zero length from a modeling perspective.  Thin element 
 * nodes in the proxy tree may <b>not</b> have child nodes.  
 * 
 * @author Christopher K. Allen
 *
 * @deprecated  I don't think this is really necessary any more.
 */
@Deprecated
public class ThinHardware extends HardwareNode {


    /*
     * Local Attributes
     */
    
    /** Position of thin element in its beamline */
    private double      dblPos;
    
    

    /*
     * TreeNode Protocol
     */

    /**
     * Currently it is impossible to insert any children into a <code>
     * ThinHardware</code> object.  So this method should not be called,
     * doing so throws a <code>GenerationException</code>.
     * 
     * @param   node                    dummy argument
     * @throws  GenerationException     this method was called
     * 
     * @see xal.sim.latgen.ptree.HardwareNode#insert(xal.sim.latgen.ptree.HardwareNode)
     */
    public void insert(TreeNode node) throws GenerationException {

        throw new GenerationException("ThinHardware#insert(TreeNode) - this method should not be called.");

    }

    /**
     * Calls the appropriate (type sensitive) method of the visitor argument
     * upon this node.
     * 
     * @param iVisitor  visitor object implement <code>IAssocTreeVisitor</code> interface
     * 
     * @see xal.sim.latgen.ptree.TreeNode#processVisitor(IAssocTreeVisitor)
     */
    public void processVisitor(IAssocTreeVisitor iVisitor) throws GenerationException {
        
        iVisitor.process(this);

    }


    /*
     * Initialization
     */

    /**
     * Initializing constructor.  
     * 
     * @param   smfNode             hardware object having zero effective length
     * @throws  GenerationException <code>smfNode</code> has finite length
     */
    ThinHardware(TreeNode nodeParent, AcceleratorNode smfNode) throws GenerationException {
        super(nodeParent, smfNode);
        this.setPosition( smfNode.getPosition() );
        
        if (smfNode.getLength() > 0.0)
            throw new GenerationException("ThinHardware(AcceleratorNode node) - node has finite length" );
    }
    
    /**
     * Set position of the <code>ThinHardware</code> object.  The interval
     * occupied by this element gets set to the zero length, single point
     * interval.
     *  
     * @param   dblPos      position along the beam path of this element
     */
    void    setPosition(double  dblPos) {
        this.dblPos = dblPos;
    }


    /*
     * Attribute Query
     */
     
     

}
