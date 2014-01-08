/*
 * Created on Feb 25, 2004
 *
 */
package xal.sim.latgen.atree;

import xal.sim.latgen.GenerationException;
import xal.tools.math.Interval;
import xal.tools.math.MathException;

/**
 * <p>
 * Concrete class of the <code>TreeNode</code> hierarchy representing a 
 * drift space within a beam line.  Note that a drift space has no direct
 * hardware representation, only a modeling one.  
 * </p>
 * <p>
 * Don't know about this one...
 * <br/>
 * Drift space nodes in the
 * association tree may have child nodes of type <code>ThinHardware</code> only.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  Feb 2004
 * @version Apr 2011
 */
public class DriftSpaceNode extends TreeNode {


    /*
     * Initialization
     */

    /**
     * Creates a new drift space along the given interval with the
     * given parent node.
     * 
     * @param ivlBeamline   beam path interval occupied by drift space
     */
    public DriftSpaceNode(TreeNode nodeParent, Interval ivlBeamline)  {
        super(nodeParent, ivlBeamline );
    }

    
    
    
    /*
     * Attribute Queries
     */
     
    /**
     * Return the length of the drift space
     * 
     * @return      length of drift space
     */
    public double   getLength() {
        
        return this.getInterval().measure();
    }


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
     * @since Apr 29, 2011
     * @author Christopher K. Allen
     */
    public void insert(TreeNode node) throws GenerationException {
                
        if (!this.getInterval().membership( node.getNodeLocation() ))
            throw new GenerationException("Failed operation: insertion of node into drift space"); 

        // Subdivide this node on either side of the given element center position 
        //      and add to child list
        double      dblPosMid = node.getNodeLocation();
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMax = this.getInterval().getMax();

        try {
            Interval  ivlLeft  = new Interval(dblPosMin, dblPosMid);
            Interval  ivlRight = new Interval(dblPosMid, dblPosMax);

            DriftSpaceNode      nodeLeft  = new DriftSpaceNode(this.getParent(), ivlLeft);
            DriftSpaceNode      nodeRight = new DriftSpaceNode(this.getParent(), ivlRight);

            this.removeChild(this);
            this.addChild(nodeRight);
            this.addChild(node);
            this.addChild(nodeLeft);

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

        DriftSpaceNode    nodeLeft  = new DriftSpaceNode(this.getParent(), ivlLeft);
        DriftSpaceNode    nodeRight = new DriftSpaceNode(this.getParent(), ivlRight);

        this.removeChild(this);

        this.addChild(nodeRight);
        this.addChild(nodeLeft);
    }

    /**
     * Replaces this <code>HardwareNode</code> object by two equal-length hardware
     * nodes reference to the same hardware object.
     * 
     * @param   dblPos      position to split hardware node
     * 
     * @throws GenerationException  split position is not inside hardware node 
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public void split(double dblPos) throws GenerationException {
        double      dblPosMin = this.getInterval().getMin();
        double      dblPosMid = dblPos;
        double      dblPosMax = this.getInterval().getMax();
        
        try {
            Interval    ivlLeft  = new Interval(dblPosMin, dblPosMid);
            Interval    ivlRight = new Interval(dblPosMid, dblPosMax);
        
            DriftSpaceNode    nodeLeft  = new DriftSpaceNode(this.getParent(), ivlLeft);
            DriftSpaceNode    nodeRight = new DriftSpaceNode(this.getParent(), ivlRight);
    
            this.removeChild(this);
            
            this.addChild(nodeRight);
            this.addChild(nodeLeft);
            
        } catch (MathException e) {
            throw new GenerationException("Split location " + dblPos + 
                    " is not in interval " + this.getInterval()
                      );
            
        }
    }

    
    
    /*
     * TreeNode Protocol
     */



    /**
     * Calls the appropriate (type sensitive) method of the visitor argument
     * upon this node.
     * 
     * @param iVisitor  visitor object implement <code>IHwareTreeVisitor</code> interface
     * 
     * @see TreeNode#processVisitor(IHwareTreeVisitor)
     */
    public void processVisitor(IHwareTreeVisitor iVisitor) throws GenerationException {
        
        iVisitor.process(this);

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
        String strBuf = "Drift Space: " + super.toString();

        return strBuf;
    }




}
