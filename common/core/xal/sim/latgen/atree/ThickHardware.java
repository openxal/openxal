/*
 * Created on Mar 2, 2004
 *
 */
package xal.sim.latgen.atree;

import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;
import xal.tools.math.Interval;
import xal.tools.math.MathException;

/**
 * This concrete sub-class of the <code>TreeNode</code> hierarchy.  A 
 * <code>ThickHardware</code> represents a section of hardware in a beamline
 * that has finite length.  Thick elements in the proxy tree may have child
 * nodes which are of type <code>ThinHardware</code> only.  
 * 
 * @author Christopher K. Allen
 * 
 * @deprecated  I don't think this is really necessary any more
 *
 */

@Deprecated
public class ThickHardware extends HardwareNode {


    /*
     * TreeNode Protocol
     */
     
    /**
     * The method creates two child <code>ThickHardware</code>
     * objects, representing the upstream and downstream  portions 
     * (with respect to the argument) of the current beamline device.  The 
     * argument is inserted between these "partial devices" so that the 
     * result is the creation of three child nodes.
     * 
     * If this node already has children (it is previously be subdivided) then
     * the argument is given added to the child list.  Consequently it is 
     * possible that this method gets called on one of the children.
     * 
     * The argument of this method is assumed to be of type 
     * <code>ThinHardware</code>. If not a <code>GenerationException</code> 
     * exception is thrown.  
     * 
     * @param nodeThin              thin element to be inserted within this node
     * 
     * @throws GenerationException  the argument is not of type <code>ThinHardware</code>
     * 
     * @see xal.sim.latgen.atree.TreeNode#insert(xal.sim.latgen.atree.TreeNode)
     */
    public void insert(ThinHardware nodeThin) throws GenerationException {
        
        if (!this.getInterval().membership( nodeThin.getHardwarePosition() ))
            throw new GenerationException("The given hardware " + nodeThin.getHardwareRef().getId() 
                                       + " is not coincident with this hardware" 
                                       + this.getHardwareRef().getId()
                                       );
        
        // Subdivide this node on either side of the thin element and add to child list
        double      dblPosMid = nodeThin.getHardwarePosition();
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMax = this.getInterval().getMax();

        try {
            Interval  ivlLeft  = new Interval(dblPosMin, dblPosMid);
            Interval  ivlRight = new Interval(dblPosMid, dblPosMax);

            ThickHardware    nodeLeft  = new ThickHardware(this.getParent(), this.getHardwareRef(), ivlLeft);
            ThickHardware    nodeRight = new ThickHardware(this.getParent(), this.getHardwareRef(), ivlRight);

            super.removeChild(this);
            super.addChild(nodeRight);
            super.addChild(nodeThin);
            super.addChild(nodeLeft);

        } catch (MathException e) {
            throw new GenerationException("The left and right intervals are bad", e);
            
        }
    }


    /**
     * Calls the appropriate (type sensitive) method of the visitor argument
     * upon this node.
     * 
     * @param iVisitor  visitor object implement <code>IHwareTreeVisitor</code> interface
     * 
     * @see xal.sim.latgen.atree.TreeNode#processVisitor(xal.sim.latgen.atree.IHwareTreeVisitor)
     */
    public void processVisitor(IHwareTreeVisitor iVisitor) throws GenerationException {
        
        iVisitor.process(this);

    }


    /*
     * Initialization
     */
     
    /**
     * Initializing constructor - create thick element that represents the
     * given hardware reference.
     * 
     * The path interval is set to the path interval specified in the 
     * <code>AcceleratorNode</code> argument.
     * 
     * @param nodeParent    parent node of this node within tree
     * @param smfNode       hardware object that this node references
     */
    public ThickHardware(TreeNode nodeParent, AcceleratorNode smfNode) {
        super(nodeParent, smfNode);
    }
    
    /**
     * Initializing constructor - create a <code>ThickHardware</code> node
     * representing a subsection of the given <code>AcceleratorNode</code>
     * object.
     *  
     * @param nodeParent    parent node of this node within tree
     * @param smfNode       hardware object that this node represents
     * @param ivlSub        sub-interval of the hardware definition for which this
     *                      node represents 
     * 
     * @throws GenerationException  the subinterval is not located within the 
     *                              given hardware object
     */
    public ThickHardware(TreeNode nodeParent, AcceleratorNode smfNode, Interval ivlSub) 
        throws GenerationException  
    {
        super(nodeParent, smfNode, ivlSub);
    }



}
