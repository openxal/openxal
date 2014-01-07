/*
 * Created on Feb 25, 2004
 */
package xal.sim.latgen.atree;


import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.math.Interval;
import xal.tools.math.MathException;



/**
 * <p>
 * This class is a simple abstract extension of super-class 
 * <code>TreeNode</code> to indicate a direct reference to some hardware
 * component or part of a component.
 * </p>
 * <p>
 * Each node in the tree maintains an interval of the beam path for which
 * its represented hardware (by derived classes) occupies.  
 * </p>   
 * 
 * 
 * @author Christopher K. Allen
 * @since  Feb 2004
 * @version Apr 2011
 *
 */
public class HardwareNode extends TreeNode {


    /*
     * Global Utilities
     */

    /**
     * <p>
     * Creates and returns an <code>Interval</code> object describing the
     * location of the given accelerator hardware in its beamline.  
     * </p>
     * <p>
     * This operation requires a special method since the method
     * <code>{@link AcceleratorSeq#getPosition()}</code> (inherited from
     * <code>{@link AcceleratorNode}</code> always returns the value 0. So
     * it is necessary to check the type of the argument: if it is an <code>
     * AcceleratorSeq</code> object then we return the interval [0,<i>L</i>]
     * where <i>L</i> is the length of the sequence.
     * </p>
     * 
     * @param smfHware   a hardware object
     * 
     * @return          interval representing the location of the hardware within beamline
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public static Interval occupiedBeamline(AcceleratorNode smfHware) {


        // If the accelerator node is straight hardware the process is straight forward
        if ( !(smfHware instanceof AcceleratorSeq) ) {
            double  dblLng = smfHware.getLength();
            double  dblPos = smfHware.getPosition();
            
            Interval    I = Interval.createFromMidpoint(dblPos, dblLng);
            
            return I;
        }
        
        
        // Get the center position (the getPosition() method of AcceleratorSeq sometimes return zero)
        double      dblLng = smfHware.getLength();
        double      dblPos = smfHware.getPosition();
        
        if (dblPos == 0.0)          // if the position is zero we'll use half the length as the 
            dblPos = dblLng/2.0;    //      center of mass
            
        Interval    ivlBmline = Interval.createFromMidpoint(dblPos, dblLng);

        return ivlBmline;
    }



    /*
     * Local Attributes
     */

    /** Reference to hardware object which this association-tree node represents */
    final private AcceleratorNode         smfHware;


    /*
     * Initialization
     */

    /**
     * Initializing constructor.  Sets the hardware reference and beam path
     * interval occupied by this node directly from the position and length
     * of the hardware object.
     * 
     * @param nodeParent    the parent node of this node within the tree 
     * @param   smfHware     hardware object that this node represents  
     */
    public HardwareNode(TreeNode nodeParent, AcceleratorNode smfHware)   {
        super(nodeParent, occupiedBeamline(smfHware));

        this.smfHware  = smfHware;
    }

    /**
     * Create a new hardware node referencing the given SMF hardware object
     * long the given interval of beamline.  That is, it is assumed that we
     * are not referencing the entire hardware object, only a portion of it.
     * 
     * @param nodeParent    the parent node of this node within the tree 
     * @param smfHware      hardware object (or portion thereof) referenced by this node
     * @param ivlBeamline   section of beamline we are referencing the hardware
     * 
     * @throws  GenerationException     the given interval is not contained with the hardware location
     *
     * @author  Christopher K. Allen
     * @since   Apr 28, 2011
     */
    public HardwareNode(TreeNode nodeParent, AcceleratorNode smfHware, Interval ivlBeamline)
        throws GenerationException
    {
        super(nodeParent, ivlBeamline);

        this.smfHware  = smfHware;

        // Check that the interval is valid
        Interval    ivlHware = HardwareNode.occupiedBeamline(smfHware);

        if ( !ivlHware.containsAE(ivlBeamline) )
            throw new GenerationException("Interval " + ivlBeamline + 
                    " is invalid for device " + smfHware.getId()
            );
    }
    

    /*
     * Operations
     */
    
    /**
     * Returns a shallow copy of the current <code>HardwareNode</code>.  The
     * returned node shares all the same internal objects as this node. 
     * Copying the parent would require creation of a whole new tree and 
     * copying of the accelerator device leaves a dangerous redundancy.
     *
     * @return  shallow copy of the current <code>HardwareNode</code>
     *
     * @author Christopher K. Allen
     * @since  May 27, 2011
     */
    public HardwareNode copy()  {
        try {
            HardwareNode    hwnCpy = new HardwareNode(this.getParent(), this.getHardwareRef(), this.getInterval());

            return hwnCpy;
            
        } catch (GenerationException e) {
            System.err.println("Serious Error: This should never occur");
            
            return null;
        }
    }
    

    /*
     * TreeNode Abstracts and Operations
     */

    /**
     * @since Apr 29, 2011
     * @see xal.sim.latgen.atree.TreeNode#processVisitor(xal.sim.latgen.atree.IHwareTreeVisitor)
     */
    @Override
    public void processVisitor(IHwareTreeVisitor iVisitor)
    throws GenerationException {
        // TODO Auto-generated method stub

    }


    /*
     * Attributes
     */

    /**
     * Return the hardware object represented by this node.
     * 
     * @return     <code>AcceleratorNode</code> object 
     */
    public AcceleratorNode getHardwareRef()  {
        return this.smfHware;
    }

    /**
     * Returns the length of the hardware object which this node
     * represents, not the beamline length currently occupied by the
     * node. 
     *
     * @return  hardware length referenced by this node
     *
     * @author Christopher K. Allen
     * @since  May 4, 2011
     */
    public double  getHardwareLength() {
        return this.smfHware.getLength();
    }

    /**
     * Returns the position of the referenced hardware in
     * the beamline.  If the referenced hardware is an
     * <code>{@link AcceleratorSeq}</code> object, then the returned
     * value is half the length length.  If the referenced hardware
     * is a <code>{@link AcceleratorNode}</code> object, then
     * the returned value is <code>{@link AcceleratorNode#getPosition()}</code>.
     *
     * @return  the design position of the hardware within its beamline sector
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public double getHardwarePosition() {

        if (this.smfHware instanceof AcceleratorSeq)
            return this.smfHware.getLength()/2.0;

        return this.smfHware.getPosition();
    }


    /*
     * Operations
     */

    /**
     * <p>
     * The given node is inserted into this hardware node.  In doing so,
     * this hardware node is split in two nodes of equal length and the
     * given node is placed between them according to the ordering imposed
     * by method <code>{@link TreeNode#compareTo(TreeNode)}</code>.  Of course,
     * it is assumed that the given node is contained within this node, otherwise
     * a <code>GenerationException</code> is thrown.
     * </p>
     * <p>
     * It is important to note that this node is no longer a part of the association
     * tree after the insertion operation.  It is <em>removed from the tree</em>
     * and replaced by three nodes being the two split halves of this node plus
     * the given node.  This action is necessary so that the associate tree
     * mirror the tree of the SMF structure and, more importantly, that we do not
     * have multiple nodes referring to the same hardware object.
     * </p>
     * 
     * @param node    node that is inserted between the split components of this node
     * @param dblPos  beamline location where the node is to be inserted 
     * 
     * @return          the left-most half of the resulting split hardware node
     * 
     * @throws  GenerationException the given split position is not in the hardware element's domain
     * 
     * @author Christopher K. Allen
     * @since Apr 29, 2011
//     * @see xal.sim.latgen.atree.TreeNode#insert(xal.sim.latgen.atree.HardwareNode)
     */
    public HardwareNode insert(TreeNode node, double dblPos) throws GenerationException {

        if (!this.getInterval().membership( node.getNodeLocation() ))
            throw new GenerationException("Failed operation: insertion of node into SMF node " 
                    + this.getHardwareRef().getId()
            );

        // Subdivide this node on either side of the given element center position 
        //      and add to child list
        //        double      dblPosMid = node.getNodeLocation();
        double      dblPosMid = dblPos;
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMax = this.getInterval().getMax();

        try {
            Interval  ivlLeft  = new Interval(dblPosMin, dblPosMid);
            Interval  ivlRight = new Interval(dblPosMid, dblPosMax);

            HardwareNode    nodeLeft  = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlLeft);
            HardwareNode    nodeRight = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlRight);

            this.getParent().removeChild(this);
            this.getParent().addChild(nodeRight);
            this.getParent().addChild(node);
            this.getParent().addChild(nodeLeft);

            return nodeLeft;

        } catch (MathException e) {
            throw new GenerationException("The left and right intervals are bad", e);

        }
    }

    /**
     * Replaces this <code>HardwareNode</code> object by two equal-length hardware
     * nodes reference to the same hardware object.
     * 
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public void split() {
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMid = this.getInterval().midpoint();
        double      dblPosMax = this.getInterval().getMax();

        Interval    ivlLeft  = Interval.createFromEndpoints(dblPosMin, dblPosMid);
        Interval    ivlRight = Interval.createFromEndpoints(dblPosMid, dblPosMax);

        try {
            HardwareNode    nodeLeft  = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlLeft);
            HardwareNode    nodeRight = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlRight);

            this.removeChild(this);

            this.addChild(nodeRight);
            this.addChild(nodeLeft);

        } catch (GenerationException e) {
            System.err.println("Serious error, this should not occur");
            e.printStackTrace();

        }
    }

    /**
     * Replaces this <code>HardwareNode</code> object by two equal-length hardware
     * nodes reference to the same hardware object.
     * 
     * @param dblPos    position to split this hardware node
     * 
     * @throws GenerationException  the given split location is not within the hardware location
     *  
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public void split(double dblPos) throws GenerationException {
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMid = dblPos;
        double      dblPosMax = this.getInterval().getMax();

        try {
            // Create the new nodes
            Interval    ivlLeft  = new Interval(dblPosMin, dblPosMid);
            Interval    ivlRight = new Interval(dblPosMid, dblPosMax);

            TreeNode        nodeParent = this.getParent();
            HardwareNode    nodeLeft   = new HardwareNode(nodeParent, this.getHardwareRef(), ivlLeft);
            HardwareNode    nodeRight  = new HardwareNode(nodeParent, this.getHardwareRef(), ivlRight);
            

            // Remove the pre-divided node from the parent and replace with the two
            //  sub-divided ones.
            nodeParent.removeChild(this);

            nodeParent.addChild(nodeRight);
            nodeParent.addChild(nodeLeft);

        } catch (MathException e) {
            throw new GenerationException("Split location " + dblPos + 
                    " is not in interval " + this.getInterval()
            );

        }
    }



    /*
     * Object Overrides
     */


    /**
     * Adds the identifier of the hardware that this node represents
     * to the output string.
     * 
     * @return  text description of the node contents
     * 
     * @since May 3, 2011
     * @see xal.sim.latgen.atree.TreeNode#toString()
     */
    @Override
    public String toString() {
        String strBuf = "ID:" + this.getHardwareRef().getId() + " " + super.toString();

        return strBuf;
    }



}
