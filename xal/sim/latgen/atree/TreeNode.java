/*
 * Created on Feb 25, 2004
 */
package xal.sim.latgen.atree;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xal.sim.latgen.GenerationException;
import xal.tools.math.Interval;



/**
 * <p>
 * Abstract base class for any association-tree node.  An association tree is part of the
 * lattice generation mechanism for the online model.  It is based upon a nested tree
 * representation of the machine hardware.  The leaves of the association tree
 * represent hardware components, or subcomponents, that may be modeled.
 * The derived class <code>HardwareTree</code> contains the root node of the
 * the association tree corresponding to a given <code>AcceleratorSeq</code>
 * object. Each node functions by maintaining an ordered list of any child nodes.
 * </p>
 * <p>
 * The class <code>HardwareNode</code> is the direct descendant of this class and the two
 * classes could easily be combined into one, since this class directly references
 * <code>HardwareNode</code>.  The reason for separating the classes is simply for
 * ease of maintenance.  The <code>TreeNode</code> class handles all the tree structure
 * operations while the <code>HardwareNode</code> handles operations specific to 
 * the SMF hardware references.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  Feb 25, 2004
 * 
 * @see HardwareNode
 *
 */
public abstract class TreeNode implements Comparable<TreeNode> {
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** The parent node in the tree */
    final private TreeNode          nodeParent;
    
    /** ordered list of child nodes */
    private final List<TreeNode>    lstChildren;

    /** The beamline interval occupied by this node */
    protected final Interval        ivlBmline;


 


    /*
     * Abstract Protocol
     */
     
     
//    /**
//     * Method from interface <code>Comparable</code> which needs to be
//     * overriden to support sorting of the child node list.  This method
//     * is implemented in <code>{@link HardwareNode}</code>.
//     * 
//     * @since Apr 28, 2011
//     * @see java.lang.Comparable#compareTo(java.lang.Object)
//     */
//    abstract public <T extends HardwareNode> int    compareTo(TreeNode node);
//    
//    /**
//     * Each concrete node must figure out what to do if, when adding 
//     * a child node, the child lands inside another node's path interval.  When 
//     * that event happens, during a call to <code>addChild()</code>, this method 
//     * is called to insert the node.
//     *
//     * @param  node    a TreeNode object occurring within this path interval
//     * 
//     * @see TreeNode#addChild
//     */
//    abstract public <T extends HardwareNode> void   insert(TreeNode node) throws GenerationException;
//    
//
    /**
     * Each concrete association node (derived type) must provide an action when 
     * presented with an object exposing the <code>IHwareTreeVisitor</code> 
     * interface. Specifically, the <code>IHwareTreeVisitor</code> interface exposes
     * a single method corresponding to each concrete class derived from
     * the <code>TreeNode</code> hierarchy.  Thus, each concrete class should
     * call that method upon itself (using itself as an argument).
     * 
     * @param iVisitor  object exposing <code>IHwareTreeVisitor</code> interface
     */
    public abstract void   processVisitor(IHwareTreeVisitor iVisitor) throws GenerationException;
    
    
    

    /*
     * Initialization
     */

    /**
     * Creates a new tree node object with the given parent
     * node which represents the specified portion of beamline.
     * 
     * @param nodeParent    the parent node of this node within the tree
     * @param ivlBeamline   the section of beamline represented by this node
     *
     * @author  Christopher K. Allen
     * @since   Apr 28, 2011
     */
    public TreeNode(TreeNode nodeParent, Interval ivlBeamline) {
        this.nodeParent = nodeParent;
        this.ivlBmline  = ivlBeamline;
        
        this.lstChildren = new LinkedList<TreeNode>();
    }
    
     

    /*
     * Attributes
     */


    /**
     * Return the parent node of this node within the
     * tree. 
     *
     * @return      parent node containing this node as a child
     *
     * @author Christopher K. Allen
     * @since  Apr 28, 2011
     */
    public final TreeNode getParent() {
        return this.nodeParent;
    }

    /**
     * Return the interval occupied by the hardware represented by this hardware
     * node object.
     * 
     * @return      beamline interval over which this node is defined.
     */
    public final Interval getInterval() {
        return this.ivlBmline;
    }

    /**
     * Returns the center position of that portion of beamline interval
     * represented by this node.  This can be different from
     * the value returned by <code>{@link HardwareNode#getHardwarePosition()}</code>
     * when this node represents only a portion of hardware and
     * not an entire hardware object.
     *
     * @return  central location of the hardware portion referenced by this node
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2011
     */
    public double getNodeLocation() {
        return this.ivlBmline.midpoint();
    }

    /**
     * Get the number of child <code>TreeNode</code> objects owned by this
     * node.
     * 
     * @return      number of children of this node
     */
    public int      getChildCount() {
        return this.getChildren().size();
    }
    
    /**
     * <p>
     * Convenience method for determining whether or not the tree node
     * has children.  We simply execute the statement
     * <br/>
     * <br/>
     * &nbsp; &nbsp;  return {@link #getChildCount()} > 0;
     * </p> 
     *
     * @return  <code>true</code> if this node has child nodes,
     *          <code>false</code> if none
     *
     * @author Christopher K. Allen
     * @since  Jul 22, 2011
     */
    public boolean  hasChildren() {
        return (this.getChildCount() > 0);
    }

    
    
    /*
     * Tree Operations
     */

    /**
     * Add a child node to this node of the tree.  The list of current children
     * is searched to determine where the child is inserted.  If the location
     * of the new node is within a child node, then the <code>insert()</code>
     * method of that child is called with <code>node</code> as the 
     * argument.
     * 
     * @param   nodeChild           new child node of this node object
     * 
     */
    public void  addChild(TreeNode  nodeChild)   {

        this.lstChildren.add(nodeChild);
        Collections.sort(this.lstChildren);
    }
    
    /**
     * Removes the given child node from this node's list of
     * children.
     *
     * @param nodeChild     node to be removed
     * 
     * @return      <code>true</code> if node was successfully removed,
     *              <code>false</code> if node was not in list or remove failed
     *
     * @author Christopher K. Allen
     * @since  Apr 28, 2011
     */
    public boolean  removeChild(TreeNode nodeChild) {
        return this.lstChildren.remove(nodeChild);
    }

    /**
     * Get the <code>List</code> interface for the container of direct children 
     * for this node.
     * 
     * @return      ordered list of direct child nodes 
     */
    public List<TreeNode> getChildren()   {
        return this.lstChildren;
    }

    /**
     * Get an iterator object which iterates through all the <b>direct</b> children
     * of this node.
     * 
     * @return      <code>Iterator</code> object for direct child nodes
     */
    public Iterator<? extends TreeNode>    iterator()     {
        return this.getChildren().iterator();
    }

    
    /*
     * IHwareTreeVisitor Operations
     */
    
    /**
     * Distributes the visitor object (argument implementing the <code>
     * IHwareTreeVisitor</code> interface) to all <b>nodes</b> of the tree.
     * The <code>processVisitor()</code> method is called on on the current
     * node first, then consecutively on each child node.
     * 
     * @param iVisitor  visitor object implementing <code>IHwareTreeVisitor</code>
     * 
     */
    public void distributeVisitor(IHwareTreeVisitor iVisitor) throws GenerationException  {
                
        this.processVisitor(iVisitor);
        
        for (TreeNode node : this.getChildren()) 
            node.distributeVisitor(iVisitor);
       
    }
    
    /**
     * Distributes the visitor object (argument implementing the 
     * <code>IHwareTreeVisitor</code>
     * interface) to all <b>leaves</b> of the proxy tree, whence 
     * <code>processVistor()</code> is called.  This action is implemented 
     * by distributing the visitor to all the child nodes of this node.  
     * If there are no children then we call the <code>processVisitor()</code>
     * method on this node, which must be a leaf.
     * 
     * @param iVisitor  visitor object implementing <code>IHwareTreeVisitor</code>
     * 
     */
    public void leafVisitor(IHwareTreeVisitor iVisitor) throws GenerationException {
        
        // If we have no children then  this is a leaf - process this node and return
        if (this.getChildCount() == 0)  {
            this.processVisitor(iVisitor);
            return;
        }
        
        
        // Pass along the visitor among my children 
        Iterator<? extends TreeNode>  iterChild = this.iterator();
            
        while (iterChild.hasNext()) {
            TreeNode   pxyNode = (TreeNode)iterChild.next();
                
            pxyNode.leafVisitor(iVisitor);
        }
    }

    
    /*
     * Comparable Interface
     */
    
    /**
     * This method is a requirement of the <code>Comparable</code> interface
     * allowing the list of child nodes to be sorted using the
     * <code>{@link Collections}</code> class.  The method was deferred 
     * by the base class <code>TreeNode</code> and we are implementing it
     * here.
     *
     * @param nodeThat  node which we are comparing this node
     * 
     * @return          -1 if the given node is less (goes downstream),
     *                   0 if the two nodes are equal (co-local),
     *                  +1 if the given node is greater (goes upstream)
     *
     * @author Christopher K. Allen
     * @since Apr 29, 2011
     * @see xal.sim.latgen.atree.TreeNode#compareTo(xal.sim.latgen.atree.TreeNode)
     */
    @Override
    public int compareTo(TreeNode nodeThat) {
        Interval    ivlThis = this.getInterval();
        Interval    ivlThat = nodeThat.getInterval();
        
        // Check if one interval contains another
        
        // Look at the left endpoints to order
        //  the intervals.
        if (ivlThat.getMin() < ivlThis.getMin())
            return +1;
        
        if (ivlThat.getMin() > ivlThis.getMin())
            return -1;
        
        // The interval left endpoints are co-located.
        //  The larger interval goes before the other
        if (ivlThat.measure() > ivlThis.measure())
            return +1;
        
        if (ivlThat.measure() < ivlThis.measure())
            return -1;
        
        // The intervals are identical
        return 0;
    }



    /*
     * Object Overrides
     */
    

    /**
     * Creates a string description of the contents of this node. String
     * contains the beamline interval occupied by node and all its child
     * nodes.
     * 
     * @return  string description  
     * 
     * @since May 3, 2011
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    bufText = new StringBuffer();
        
        bufText.append("Loc:");
        bufText.append(this.ivlBmline);
        bufText.append(" children{" );
        for (TreeNode nodeChild : this.getChildren()) 
            bufText.append(nodeChild.toString());
        bufText.append("} \n");
        
        return bufText.toString();
    }
    
    
}
