/*
 * Created on Mar 3, 2004
 */
package xal.sim.latgen.atree;

import xal.sim.latgen.GenerationException;

/**
 * This is the interface to the visitor design pattern of an association tree.  
 * Specifically, any class implementing this interface can be used as
 * a "visitor" for an association tree.
 * 
 * @author Christopher K. Allen
 * @since  Mar 3, 2004
 * 
 * @version Apr 27, 2011 (formally <code>IProxyVistor</code>)
 * 
 */
public interface IHwareTreeVisitor {
   
//    /**
//     * Accept an association-tree node of type <code>ThinHardware</code> and perform
//     * any visitor-specific processing of the node argument.
//     * 
//     * @param pxyNode                current association-tree node being visited
//     * 
//     * @throws GenerationException   unable to process node (visitor specific)
//     */ 
//    public void process(ThinHardware pxyNode) throws GenerationException;

    /**
     * Accept an association-tree node of and perform
     * any visitor specific processing of the node argument.
     * 
     * @param node                current association-tree node being visited
     * 
     * @throws GenerationException   unable to process node (visitor specific)
     */
    public void process(TreeNode node) throws GenerationException;
    
//    /**
//     * Accept an association-tree node of type <code>DriftSpaceNode</code> and perform
//     * any visitor specific processing of the node argument.
//     * 
//     * @param pxyNode                current association-tree node being visited
//     * 
//     * @throws GenerationException   unable to process node (visitor specific)
//     */
//    public void process(DriftSpaceNode pxyNode) throws GenerationException;
    
    
    
    /**
     * Accept an association-tree node of type <code>AssocTree</code> and perform
     * any visitor specific processing of the node argument.  Node that
     * <code>AssocTree</code> nodes are somewhat special in that they 
     * representing the node of designated sub-trees.
     * 
     * @param pxyNode                current association-tree node being visited
     * 
     * @throws GenerationException   unable to process node (visitor specific)
     */
    public void entering(HardwareTree pxyNode) throws GenerationException;
    
    /**
     * Event representing the completion of a association sub-tree indicated by
     * the argument.
     * 
     * @param pxyNode               association sub-tree completed processing
     * 
     * @throws GenerationException  visitor specific error in tree processing
     */
    public void leaving(HardwareTree pxyNode) throws GenerationException;
}
