/*
 * Created on Feb 25, 2004
 */
package xal.sim.latgen.atree;

import java.util.LinkedList;
import java.util.List;

import xal.sim.cfg.ModelConfiguration;
import xal.sim.latgen.GenerationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.math.Interval;
import xal.tools.math.MathException;

/**
 * <p>
 * This class is an <em>association tree</em>.  Specifically the nodes of
 * the tree are association classes between hardware objects in the <tt>SMF</tt>
 * hardware package, and the online model simulation package.
 * This class maintains a reference to an <code>AcceleratorSeq</code> 
 * object from the SMF hierarchy which is the root of the association.
 * The associate tree knows how to build itself from such an hardware object
 * and does so at construction time.
 * </p>
 * <p>
 * This class is also the entry point for any association-tree visitor.  That is,
 * association trees support the <i>Visitor</i> design pattern.  Here, a visitor
 * class is identified by one which implements the <code>IHwareTreeVisitor</code>
 * interface.
 * </p> 
 *  
 * @author Christopher K. Allen
 * @since   Feb 25, 2004
 * @version Apr 27, 2011 (formally <code>ProxyTree</code>
 *
 */
public class HardwareTree {
    
    
    /*
     * Global Constants
     */
    
    /** Tolerance used when determining co-local hardware devices */
    public static final double DBL_TOL_POS = 1.0e-4;
    
    
    
    /*
     * Global Operations
     */

    /**
     * Get the smallest possible drift space length
     * 
     * @return  smallest length of a drift to consider 
     */
    public static double   getDriftTolerance() {
        return HardwareTree.DBL_TOL_POS;
    }



    /*
     * Local Attributes
     */
     
    /** hardware sequence object represented by this tree */
    final private AcceleratorSeq        smfRoot;
    
    /** the parent accelerator object of the accelerator sequence */
    final private Accelerator           smfAccel;
    
    /** The online model configuration manager for the accelerator */
    final private ModelConfiguration    mgrMdlCfg;
    
    /** Root of the association tree */
    final private HardwareNode          nodeRoot;
    
    
    
//    /** ordered list of all subsequences */
//    private List                    m_lstSeq = new LinkedList();
//    
//    /** ordered list of all thick elements */
//    private List                    m_lstThick = new LinkedList();
//    
//    /** ordered list of all thin elements */
//    private List                    m_lstThin = new LinkedList(); 
    



    /*
     * TreeNode Protocol
     */

//    /**
//     * Since a <code>HardwareTree</code> represents a sequence of hardware object
//     * this method simply delegates the control to the superclass method
//     * <code>addChild()</code>.  The subsequent call may consequently invoke the
//     * <code>insert()</code> method on any child objects of this node.
//     * 
//     * @param node
//     * @throws GenerationException
//     * 
//     * @see gov.TreeNode.xal.model.gen.ptree.ProxyNode#insert(gov.TreeNode.xal.model.gen.ptree.ProxyNode)
//     * @see gov.TreeNode.xal.model.gen.ptree.ProxyNode#addChild(gov.TreeNode.xal.model.gen.ptree.ProxyNode)
//     */
//    void insert(TreeNode node) throws GenerationException {
//        super.addChild(node);
//    }


    /*
     * Initialization
     */
     
    /**
     * Build a <code>HardwareTree</code> object representing the provided 
     * <code>AcceleratorSeq</code> object.
     *  
     * @param smfSeq        SMF hardware sequence to be modeled 
     */
    public HardwareTree(AcceleratorSeq smfSeq)    throws GenerationException {
        
        // Assign the hardware reference and build tree
        double      dblMin = 0.0;
        double      dblLng = smfSeq.getLength();
        Interval    ivlSeq = Interval.createFromEndpoints(dblMin, dblLng);

        this.smfRoot   = smfSeq;
        this.smfAccel  = smfSeq.getAccelerator();
        this.mgrMdlCfg = smfSeq.getAccelerator().getModelConfiguration();
        this.nodeRoot  = new HardwareNode(null, smfSeq, ivlSeq);

        // Build association tree
        this.buildTree(this.nodeRoot, this.smfRoot);
        this.splitHardware(this.nodeRoot);
//        this.applyDriftSpaces(0, nodeRoot);
    }

    
    /*
     * Attribute Query
     */
     
     /**
      * Return the hardware sequence represented by this tree.
      * 
      * @return     SMF hardware sequence
      */
     public AcceleratorSeq  getHardwareRef()    {
         return this.smfRoot;
     }




    /*
     * IHwareTreeVisitor Interface
     */
     
     /**
      * Calls the appropriate (type sensitive) method of the visitor argument
      * upon this node.
      * 
      * @param iVisitor  visitor object implementing <code>IHwareTreeVisitor</code> interface
      * 
      * @see xal.sim.latgen.atree.IHwareTreeVisitor#process(TreeNode)
      */
     public void processVisitor(IHwareTreeVisitor iVisitor) throws GenerationException {
         
         iVisitor.entering(this);

     }

     /**
      * 
      * @param iVisitor  visitor object implementing <code>IHwareTreeVisitor</code> interface
      * 
      * @see xal.sim.latgen.atree.TreeNode#distributeVisitor(IHwareTreeVisitor)
      */
     public void distributVistor(IHwareTreeVisitor iVisitor) throws GenerationException {

         iVisitor.entering(this);
         
         this.nodeRoot.distributeVisitor(iVisitor);

         iVisitor.leaving(this);
     }

     
     /*
      * Object Overrides
      */

     /**
      * Return a text description of this tree.  The
      * description depends mostly on the implementation
      * of <code>toString()</code> in the tree nodes.
      * 
      * @return     string description of this tree
      * 
      * @since May 3, 2011
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         String     strBuf = "Tree Hardware Sequence: " + this.getHardwareRef() + "\n";
         
         strBuf += this.nodeRoot.toString();
         
         return strBuf;
     }





     /*
      * Local Support
      */


     /**
      * <p>
      * Builds up the hardware tree from the root <code>AcceleratorSeq</code>
      * object.  The location of the hardware nodes in relation to the 
      * SMF hardware position is given by the sorting algorithm in 
      * <code>TreeNode#compareTo(TreeNode)</code>.
      * </p>
      * <p>
      * Note this is a recursive function.  By calling initially with the root
      * node of the hardware tree and the top-level <code>AcceleratorSeq</code>
      * object being modeled, the hardware tree is built.
      * </p>
      * 
      * @param nodeTrunk    The current tree node - initially the root node
      * @param seqTrunk     The accelerator sequence beam modeled
      *
      * @author Christopher K. Allen
      * @since  May 3, 2011
      */
     private void buildTree(HardwareNode nodeTrunk, AcceleratorSeq seqTrunk) {

         List<AcceleratorNode>  lstSmfNodes = seqTrunk.getNodes();

         for (AcceleratorNode smfNode : lstSmfNodes) {
             HardwareNode       nodeBranch = new HardwareNode(nodeTrunk, smfNode);

             nodeTrunk.addChild(nodeBranch);

             if (smfNode instanceof AcceleratorSeq) {
                 AcceleratorSeq seqBranch = (AcceleratorSeq)smfNode;

                 this.buildTree(nodeBranch, seqBranch);
             }

         }
     }

     

     /**
      * <p>
      * Splits the hardware nodes representing thick hardware wherever
      * a thin hardware device is co-located (within tolerance).  This
      * action is in preparation for the generation of a model lattice
      * that contains thin elements within finite length elements.
      * </p>
      * <p>
      * This is a recursive function which propagations through the
      * tree calling itself on each valid tree node.
      * </p> 
      *
      * @param nodeParent           the tree root node upon initial call
      * 
      * @throws GenerationException failure occurred attempting to split a hardware node
      *
      * @author Christopher K. Allen
      * @since  May 25, 2011
      * 
      * TODO Currently this method does not work.
      */
     private void splitHardware(TreeNode nodeParent) throws GenerationException {

         // We need a double loop to do this - an order N^2 operation
         //     the outer loop: for each child node
         //     the inner loop: for each sibling of the current child
         for (TreeNode nodeChild : nodeParent.getChildren()) {

             // If this child also has children we need to process them
             //     This is done recursively
             if ( nodeChild.getChildCount() > 0 )
                 this.splitHardware(nodeChild);


             // Ignore non-hardware nodes
             if ( !(nodeChild instanceof HardwareNode) )
                 continue;

             HardwareNode                     hwnChild = (HardwareNode)nodeChild;
             AcceleratorNode                  smfChild = hwnChild.getHardwareRef();
             Class<? extends AcceleratorNode> clsChild = smfChild.getClass();


             // Only look at thick hardware
             if ( !this.mgrMdlCfg.isThickHardwareType(clsChild) )
                 continue;

             Interval   ivlChild = nodeChild.getInterval();


             // Inner loop now looks at all siblings of the current child node
             //     Siblings should be thin hardware to warrant action
             //     Since we will be modifying the siblings while traversing the
             //     sibling list, we need to snapshot the original list. 
             List<TreeNode> lstSiblings = new LinkedList<TreeNode>( nodeParent.getChildren() );

             for (TreeNode nodeSibling : lstSiblings ) {

                 // Ignore non-hardware nodes
                 if ( !(nodeSibling instanceof HardwareNode) )
                     continue;

                 HardwareNode                       hwnSibling = (HardwareNode)nodeSibling;
                 AcceleratorNode                    smfSibling = hwnSibling.getHardwareRef();
                 Class<? extends AcceleratorNode>   clsSibling = smfSibling.getClass();

                 // Only look at thin hardware
                 if ( !this.mgrMdlCfg.isThinHardwareType(clsSibling) ) 
                     continue;

                 double dblPos = smfSibling.getPosition();

                 // We apply a tolerance to avoid fine splitting
                 //     That is, we are not shaving off small sections of hardware at the ends
                 if ( !ivlChild.membership(dblPos + DBL_TOL_POS) )
                     continue;
                 if ( !ivlChild.membership(dblPos - DBL_TOL_POS) )
                     continue;

                 // Okay, we split the thick hardware in two so the thin one lives in between
                 //     Then we break the loop because nodeChild no longer exists, but its
                 //     two mitosis twins are children of the parent.
                 hwnSibling.split(dblPos);
                 break;
             }
         }
     }

     /**
      * <p>
      * Looks for regions of drift in the hardware tree (i.e., positions
      * of no hardware), then creates drift nodes of the appropriate length
      * and inserts them.  The result is a hardware tree which contains a 
      * contiguous collection of objects along the beamline, suitable for
      * generating a modeling lattice.
      * </p>
      * <p>
      * This is a recursive function which propagations through the
      * tree calling itself on each valid tree node.
      * </p>
      *
      * @param dblPosLt     the current left most position in the beamline (from where we are looking)
      * @param nodeParent   the root node of the tree on the initial call
      * 
      * @return             the left most position after processing 
      * 
      * @throws GenerationException   Failed to create an <code>{@link Interval}</code> object for the drift space
      *
      * @author Christopher K. Allen
      * @since  May 25, 2011
      */
     private double applyDriftSpaces(double dblPosLt, TreeNode nodeParent) throws GenerationException {
         // We need a single loop to do this with a recursive application over
         //     each tree level.
         for (TreeNode nodeChild : nodeParent.getChildren()) {

             
             // If this child also has children we need to process them
             //     This is done recursively
             if ( nodeChild.getChildCount() > 0 )
                 dblPosLt = this.applyDriftSpaces(dblPosLt, nodeChild);


             // Ignore non-hardware nodes
             if ( !(nodeChild instanceof HardwareNode) )
                 continue;

             Interval       ivlChild = nodeChild.getInterval();
             double         dblPosRt = ivlChild.getMin();
             
             
             // Check if the right-most point of the hardware is greater the
             //     the current left position.  If so, we have a drift space between.
             if (dblPosLt > dblPosRt - DBL_TOL_POS)
                 continue;
             
             try {
             Interval       ivlDrift  = new Interval(dblPosLt, dblPosRt);
             DriftSpaceNode nodeDrift = new DriftSpaceNode(nodeParent, ivlDrift);
             
             nodeParent.addChild(nodeDrift);
             dblPosLt = dblPosRt;
             
             } catch (MathException e) {
                 throw new GenerationException("Unable to create drift space interval ", e);
                 
             }

         }
         
         return dblPosLt;
         
     }


//     /**
//      * Parse the given <code>AcceleratorSeq</code> object identifying the 
//      * sequence nodes into subsequences, thick elements, and thin elements.
//      * After identification the nodes are placed into their respective
//      * container for subsequence processing.
//      * 
//      * @param   smfRoot      hardware object to be parsed
//      */
//     private void    parseSequence(AcceleratorSeq smfSeq)    {
//
//         Iterator        iterNode = smfSeq.getNodes().iterator();
//         while (iterNode.hasNext())  {
//             AcceleratorNode     smfNode = (AcceleratorNode)iterNode.next();
//
//             if (smfNode instanceof AcceleratorSeq) {        // check if node is a sequence
//                 m_lstSeq.add(smfNode);
//
//             } else if (smfNode.getLength() > 0.0   )    {   // check if node is thick
//                 m_lstThick.add(smfNode);
//
//             } else  {                                       // default - node must be thin
//                 m_lstThin.add(smfNode);
//
//             }
//         }                    
//     }
//
//
//     /**
//      * Creates <code>HardwareTree</code> nodes for hardware objects representing
//      * composite elements.  Populates the <code>HardwareTree</code> with these
//      * subtrees.
//      * 
//      * @throws  GenerationException     error occurred building the tree
//      */
//     private void    processSubsequences() throws GenerationException {
//         Iterator        iterHware = this.m_lstSeq.iterator();
//         while (iterHware.hasNext())  {
//             AcceleratorSeq      smfSeq = (AcceleratorSeq)iterHware.next();
//             HardwareTree           pxySeq = new HardwareTree(smfSeq);
//
//             this.addChild(pxySeq);        
//         }                    
//     }
//
//     /**
//      * Creates <code>ThickHardware</code> nodes for hardware objects having
//      * finite length.  Populates the <code>HardwareTree</code> with these objects.
//      * 
//      * @throws  GenerationException     error occurred building the tree
//      */
//
//     private void    processThickElements() throws GenerationException {
//         Iterator        iterHware = this.m_lstThick.iterator();
//         while (iterHware.hasNext())  {
//             AcceleratorNode     smfNode = (AcceleratorNode)iterHware.next();
//             ThickHardware        pxyThick = new ThickHardware(smfNode);
//
//             this.addChild(pxyThick);
//         }                    
//     }
//
//     /**
//      * Build the drift spaces in between thick elements.
//      * 
//      * @throws GenerationException
//      */
//     private void    processDriftSpaces()   throws GenerationException  {
//         double      sRight;                 // right endpoint of drift      
//         double      sLeft;                  // left endpoint of drift      
//         TreeNode   pxyChild;               // current child node under consideration
//
//
//         // Set up for the initial drift space between sequence entrance an first element
//         sLeft = this.getInterval().getMin();    // initilize to start of this sequence
//         sRight = 0.0;                           // irrelevant for now      
//
//
//         // Iterate through each immediate children putting drift spaces between
//         Iterator    iterChild = this.iterator();  
//         while (iterChild.hasNext()) {
//             pxyChild = (TreeNode)iterChild.next();
//             sRight = pxyChild.getInterval().getMin();  
//
//             if ((sRight-sLeft) > HardwareTree.getDriftTolerance()) {
//                 DriftSpaceNode  pxyDrift = new DriftSpaceNode(sLeft, sRight);
//
//                 this.addChild(pxyDrift);
//             }
//
//             sLeft = pxyChild.getInterval().getMax();
//         }
//
//
//         // Check for final drift between last element and exit of sequence
//         sRight = this.getInterval().getMax();
//         if ( (sRight-sLeft) > HardwareTree.getDriftTolerance())    {
//             DriftSpaceNode  pxyDrift = new DriftSpaceNode(sLeft, sRight);
//
//             this.addChild(pxyDrift);
//         }
//     }
//
//
//     /**
//      * Creates <code>ThinHardware</code> nodes for hardware objects having
//      * zero length.  Populates the <code>HardwareTree</code> with these objects.
//      * 
//      * @throws  GenerationException     error occurred building the tree
//      */
//
//     private void    processThinElements() throws GenerationException {
//         Iterator        iterHware = this.m_lstThin.iterator();
//         while (iterHware.hasNext())  {
//             AcceleratorNode     smfNode = (AcceleratorNode)iterHware.next();
//             ThinHardware        pxyThin = new ThinHardware(smfNode);
//
//             this.addChild(pxyThin);
//         }                    
//     }
//
//     /**
//      * Kluge for computing the true length of any <code>AcceleratorSeq</code> 
//      * object.  Apparently an <code>AcceleratorSeq</code> object that has
//      * children only of type <code>AcceleratorSeq</code> has a (mistaken)
//      * length of zero.
//      * 
//      * @param   seq     target <code>AcceleratorSeq</code> object
//      * @return          the length of <code>seq</code>
//      */    
//     double compSeqLen(AcceleratorSeq seq)   {
//         double len = seq.getLength();
//
//         if (len > 0.0)  return len;
//
//         Iterator    iterSub = seq.getSequences().iterator();
//         while (iterSub.hasNext())   {
//             AcceleratorSeq  seqSub = (AcceleratorSeq)iterSub.next();
//             len += this.compSeqLen(seqSub);
//         }
//         return len;
//
//     }

}





///**
// * Search the AcceleratorSeq object for AcceleratorNode objects with finite
// * length - these must be a subset of actual hardware objects.  Creates
// * <code>HardwareNode</code> nodes for these elements or other <code>
// * HardwareTree</code> objects if the <code>AcceleratorNode</code> is also an 
// * instance of <code>AcceleratorSeq</code>.
// * 
// * @param   smfRoot                  
// * @throws  GenerationException
// */
//
//private void    processThickElements(AcceleratorSeq smfRoot) throws GenerationException {
//    Iterator        iterNode = smfRoot.getNodes().iterator();
//    while (iterNode.hasNext())  {
//        AcceleratorNode     smfNode = (AcceleratorNode)iterNode.next();
//        
//        // Check if node is a sequence
//        if (smfNode instanceof AcceleratorSeq) {
//            AcceleratorSeq      smfComp = (AcceleratorSeq)smfNode;
//            HardwareTree           pxyComp = new HardwareTree(smfComp);
//                
//            this.addChild(pxyComp);        
//                
//        } else if (smfNode.getLength() > 0.0   )    {
//            ThickHardware   pxyThick = new ThickHardware(smfNode);
//                
//            this.addChild(pxyThick);
//        } 
//    }                    
//}
//    
///**
// * Build the drift spaces in between thick elements.
// * 
// * @param smfRoot
// * @throws GenerationException
// */
//private void    processDriftSpaces(AcceleratorSeq smfRoot)   throws GenerationException  {
//    double      sLeft = this.getInterval().getMin();
//        
//    Iterator    iterChild = this.childIterator();
//    while (iterChild.hasNext()) {
//        TreeNode   pxyChild = (TreeNode)iterChild.next();
//        double      sRight = pxyChild.getInterval().getMin();
//            
//        if ((sRight-sLeft) > HardwareTree.DBL_TOL_POS) {
//            DriftSpaceNode  pxyDrift = new DriftSpaceNode(sLeft, sRight);
//                
//            this.addChild(pxyDrift);
//        }
//            
//        sLeft = pxyChild.getInterval().getMax();
//    }
//}
//
