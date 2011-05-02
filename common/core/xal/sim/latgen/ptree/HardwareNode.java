/*
 * Created on Feb 25, 2004
 */
package xal.sim.latgen.ptree;


import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;
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
 * @since  Feb 25, 2004
 *
 */
public class HardwareNode extends TreeNode {


    /*
     * Global Utilities
     */
    
    /**
     * Creates and returns an <code>Interval</code> object describing the
     * location of the given accelerator hardware in its beamline.
     *
     * @param smfHware   a hardware object
     * 
     * @return          interval representing the location of the hardware within beamline
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public static Interval occupiedBeamline(AcceleratorNode smfHware) {
        
        // Get the center position of the hardware and its length
        double  dblPos = smfHware.getPosition();
        double  dblLng = smfHware.getLength();
        
        // Create the beamline interval and return it
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
     * @param smfHware      hardware object (or portion thereof) referenced by this node
     * @param ivlBeamline   section of beamline we are referencing the hardware
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
        double      dblMidPt = smfHware.getPosition();
        double      dblLng   = smfHware.getLength();
        Interval    ivlHware = Interval.createFromMidpoint(dblMidPt, dblLng);
        
        if ( !ivlHware.containsAE(ivlBeamline) )
            throw new GenerationException("Interval " + ivlBeamline + 
                    " is invalid for device " + smfHware.getId()
                    );
    }
    
    
    /*
     * TreeNode Abstracts and Operations
     */
    
    /**
     * @since Apr 29, 2011
     * @see xal.sim.latgen.ptree.TreeNode#processVisitor(xal.sim.latgen.ptree.IAssocTreeVisitor)
     */
    @Override
    public void processVisitor(IAssocTreeVisitor iVisitor)
            throws GenerationException {
        // TODO Auto-generated method stub
        
    }
    
    /**
      * Return the hardware object represented by this node.
      * 
      * @return     <code>AcceleratorNode</code> object 
      */
     public AcceleratorNode getHardwareRef()  {
         return this.smfHware;
     }


    /**
     * Returns the position of the referenced hardware in
     * the beamline.
     *
     * @return  the design position of the hardware within its beamline sector
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public double getHardwarePosition() {
        return this.smfHware.getPosition();
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
     * @param   node    node that is inserted between the split components of this node
     * 
     * @return          the left-most half of the resulting split hardware node
     * 
     * @throws  GenerationException the given split position is not in the hardware element's domain
     * 
     * @author Christopher K. Allen
     * @since Apr 29, 2011
//     * @see xal.sim.latgen.ptree.TreeNode#insert(xal.sim.latgen.ptree.HardwareNode)
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

            super.removeChild(this);
            super.addChild(nodeRight);
            super.addChild(node);
            super.addChild(nodeLeft);
            
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
        
            HardwareNode    nodeLeft  = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlLeft);
            HardwareNode    nodeRight = new HardwareNode(this.getParent(), this.getHardwareRef(), ivlRight);
    
            this.removeChild(this);
            
            this.addChild(nodeRight);
            this.addChild(nodeLeft);
            
        } catch (MathException e) {
            throw new GenerationException("Split location " + dblPos + 
                                          " is not in interval " + this.getInterval()
                                            );
            
        }
    }

    
}
