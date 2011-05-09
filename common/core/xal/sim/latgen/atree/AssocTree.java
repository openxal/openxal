/*
 * Created on Feb 25, 2004
 */
package xal.sim.latgen.atree;

import java.util.List;

import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.tools.math.Interval;

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
 * class is identified by one which implements the <code>IAssocTreeVisitor</code>
 * interface.
 * </p> 
 *  
 * @author Christopher K. Allen
 * @since   Feb 25, 2004
 * @version Apr 27, 2011 (formally <code>ProxyTree</code>
 *
 */
public class AssocTree {
    
    
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
        return AssocTree.DBL_TOL_POS;
    }



    /*
     * Local Attributes
     */
     
    /** hardware sequence object represented by this tree */
    final private AcceleratorSeq    smfRoot;
    
    /** Root of the association tree */
    final private HardwareNode      nodeRoot;
    
    
    
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
//     * Since a <code>AssocTree</code> represents a sequence of hardware object
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
     * Build a <code>AssocTree</code> object representing the provided 
     * <code>AcceleratorSeq</code> object.
     *  
     * @param smfSeq        SMF hardware sequence to be modeled 
     */
    public AssocTree(AcceleratorSeq smfSeq)    throws GenerationException {
        
        // Assign the hardware reference and build tree
        double      dblMin = 0.0;
        double      dblLng = smfSeq.getLength();
        Interval    ivlSeq = Interval.createFromEndpoints(dblMin, dblLng);

        this.smfRoot   = smfSeq;
        this.nodeRoot  = new HardwareNode(null, smfSeq, ivlSeq);

        // Build association tree
        this.buildTree(this.nodeRoot, this.smfRoot);
//        this.splitSiblings(this.nodeRoot);
        
//        this.parseSequence(smfSeq);
//        this.processSubsequences();
//        this.processThickElements();
//        this.processDriftSpaces();
//        this.processThinElements();        
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
     * IAssocTreeVisitor Interface
     */
     
     /**
      * Calls the appropriate (type sensitive) method of the visitor argument
      * upon this node.
      * 
      * @param iVisitor  visitor object implementing <code>IAssocTreeVisitor</code> interface
      * 
      * @see gov.TreeNode.xal.model.gen.ptree.ProxyNode#processVisitor(gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor)
      */
     void processVisitor(IAssocTreeVisitor iVisitor) throws GenerationException {
         
         iVisitor.entering(this);

     }

     /**
      * 
      * @param iVisitor  visitor object implementing <code>IAssocTreeVisitor</code> interface
      * 
      * @see xal.sim.latgen.atree.TreeNode#distributeVisitor(IAssocTreeVisitor)
      */
     public void distributVistor(IAssocTreeVisitor iVisitor) throws GenerationException {

         iVisitor.entering(this);
         
         this.nodeRoot.distributeVisitor(iVisitor);

         iVisitor.leaving(this);
     }


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
      *
      * @param nodeTrunk
      * @param seqTrunk
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
      *
      * @param nodeParent
      * @throws GenerationException
      *
      * @author Christopher K. Allen
      * @since  May 3, 2011
      */
     private void splitSiblings(HardwareNode nodeParent) throws GenerationException {

         for (TreeNode nodeChild : nodeParent.getChildren()) {

             // Ignore non-hardware nodes
             if ( !(nodeChild instanceof HardwareNode) )
                 continue;

             Interval       ivlChild = nodeChild.getInterval();

             for (TreeNode nodeSibling : nodeParent.getChildren()) {

                 if (nodeSibling.equals(nodeChild))
                     continue;

                 if ( !(nodeSibling instanceof HardwareNode) )
                     continue;

                 double     dblPosSblg   = ((HardwareNode) nodeSibling).getHardwarePosition();
                 Interval   ivlSibling = nodeSibling.getInterval();

                 if ( !ivlChild.containsAE(ivlSibling) )
                     continue;

                 if ( !ivlChild.membership(dblPosSblg) )
                     continue;

                 nodeChild = ((HardwareNode)nodeChild).insert(nodeSibling, dblPosSblg);
             }
         }
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
//      * Creates <code>AssocTree</code> nodes for hardware objects representing
//      * composite elements.  Populates the <code>AssocTree</code> with these
//      * subtrees.
//      * 
//      * @throws  GenerationException     error occurred building the tree
//      */
//     private void    processSubsequences() throws GenerationException {
//         Iterator        iterHware = this.m_lstSeq.iterator();
//         while (iterHware.hasNext())  {
//             AcceleratorSeq      smfSeq = (AcceleratorSeq)iterHware.next();
//             AssocTree           pxySeq = new AssocTree(smfSeq);
//
//             this.addChild(pxySeq);        
//         }                    
//     }
//
//     /**
//      * Creates <code>ThickHardware</code> nodes for hardware objects having
//      * finite length.  Populates the <code>AssocTree</code> with these objects.
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
//             if ((sRight-sLeft) > AssocTree.getDriftTolerance()) {
//                 DriftSpace  pxyDrift = new DriftSpace(sLeft, sRight);
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
//         if ( (sRight-sLeft) > AssocTree.getDriftTolerance())    {
//             DriftSpace  pxyDrift = new DriftSpace(sLeft, sRight);
//
//             this.addChild(pxyDrift);
//         }
//     }
//
//
//     /**
//      * Creates <code>ThinHardware</code> nodes for hardware objects having
//      * zero length.  Populates the <code>AssocTree</code> with these objects.
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
// * AssocTree</code> objects if the <code>AcceleratorNode</code> is also an 
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
//            AssocTree           pxyComp = new AssocTree(smfComp);
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
//        if ((sRight-sLeft) > AssocTree.DBL_TOL_POS) {
//            DriftSpace  pxyDrift = new DriftSpace(sLeft, sRight);
//                
//            this.addChild(pxyDrift);
//        }
//            
//        sLeft = pxyChild.getInterval().getMax();
//    }
//}
//
