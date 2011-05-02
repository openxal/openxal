/*
 * Created on Mar 4, 2004
 */
package xal.sim.latgen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import xal.model.Sector;
import xal.model.elem.IdealDrift;

import xal.sim.latgen.ptree.DriftSpace;
import xal.sim.latgen.ptree.IAssocTreeVisitor;
import xal.sim.latgen.ptree.AssocTree;
import xal.sim.latgen.ptree.ThickHardware;
import xal.sim.latgen.ptree.ThinHardware;
import xal.sim.latgen.ptree.TreeNode;

import xal.sim.sync.SynchronizationManager;

import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

/**
 * This class build model sequences (<code>Sector</code> objects) from 
 * the hardware representation given by <code>AcceleratorSeq</code> objects.
 * Note that <code>Sector</code> objects may be nested within each other
 * to arbitrary order.
 * 
 * A proxy tree is built (@see gov.sns.xal.model.gen.ptree) which breaks the
 * hardware into nested sequences hardware components and subcomponents
 * that have model representations.  The <code>SequenceGenerator</code> class
 * implements the <code>IAssocTreeVisitor</code> interface, which means it is
 * a visitor class of the proxy tree.  
 *   
 * @author Christopher K. Allen
 *
 */
public class SequenceGenerator implements IAssocTreeVisitor {

    
    /*
     * Global Attributes
     */
    /** mapping of SMF AcceleratorSeq types to MODEL Element types */
    private static HashMap  s_mapTypes;
    


    
    /*
     * Global Methods
     */
     
    
    /**
     * Build and return an <code>Sector</code> sequence object 
     * that models the given hardware represented by the 
     * <code>AcceleratorSeq</code> argument.
     * 
     * @param smfSeq    hardware reference to be modeled
     * 
     * @return          synchronized model of the hardware reference argument
     * 
     * @throws  GenerationException     unable to build model
     */
    public static Sector    buildModelSector(AcceleratorSeq smfSeq)
        throws GenerationException 
    {
        SequenceGenerator   genSeq = new SequenceGenerator();
        
        return genSeq.genModelSector(smfSeq);    
    }
    



    /*
     * Local Attributes
     */
     
    /** index of drift space for current Sector under construction */
    private int                     m_cntDrift;
    
    /** master model sequence under generation */
    private Sector                  m_secMaster;
    
    /** the sequence stack */
    private Stack                   m_stackSecs;

    /** synchronization manager for master sequence under construction */
    private SynchronizationManager  m_syncMgr;
     
     
     
    /**
     * Build and return an <code>Sector</code> sequence object 
     * that models the given hardware represented by the 
     * <code>AcceleratorSeq</code> argument.
     * 
     * @param smfSeq    hardware reference to be modeled
     * 
     * @return          synchronized model of the hardware reference argument
     * 
     * @throws  GenerationException     unable to build model
     */ 
    public Sector   genModelSector(AcceleratorSeq smfSeq)    
        throws GenerationException
    {
        this.m_cntDrift = 1;
        this.setMasterSector(null);
        this.m_stackSecs = new Stack();
        this.m_syncMgr = new SynchronizationManager();
        
        AssocTree       pxyModel = new AssocTree(smfSeq);
        
        pxyModel.disseminateVisitor(this);
        
        return this.getMasterSector();     
    }
     
     
     
    /*
     * IAssocTreeVisitor Interface
     */

    /**
     * Process a <code>ThinHardware</code> node of the proxy tree.  Check if 
     * node is a leaf in the tree then build a modeling element and initiate
     * synchronization if so.
     * 
     * @param pxyNode   <code>ThinHardware</code> proxy-tree node
     * 
     * @see gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor#entering(gov.sns.xal.model.gen.ptree.ThinHardware)
     */
    public void process(ThinHardware pxyNode) throws GenerationException {
       
        // If this node has children it does not represent a model element
        if (pxyNode.getChildCount() > 0) return;
            
        AcceleratorNode smfNode = pxyNode.getHardwareRef();
        
        prozessHardware(smfNode);
    }

    /**
     * Process a <code>ThickHardware</code> node of the proxy tree.  Check if 
     * node is a leaf in the tree then build a modeling element and initiate
     * synchronization if so.
     * 
     * @param pxyNode   <code>ThickHardware</code> proxy-tree node
     * 
     * @see gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor#entering(gov.sns.xal.model.gen.ptree.ThickHardware)
     */
    public void process(ThickHardware pxyNode) throws GenerationException {

        // If this node has children it does not represent a model element
        if (pxyNode.getChildCount() > 0) return;
            
        AcceleratorNode smfNode = pxyNode.getHardwareRef();
        
        prozessHardware(smfNode);
    }

    /**
     * @param pxyNode
     * 
     * @see gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor#entering(gov.sns.xal.model.gen.ptree.DriftSpace)
     */
    public void process(DriftSpace pxyNode) throws GenerationException {
        
        if (pxyNode.getChildCount() > 0) return;
            
        String  strId  = this.genDriftId();
        double  dblLen = pxyNode.getLength();
        
        IdealDrift elemDrift = new IdealDrift(strId, dblLen);
        this.getCurrentSector().addChild(elemDrift); 
    }

    /**
     * Begin building a (Sub)sequence of model elements.  We have encountered
     * a <code>AssocTree</code> node in the proxy tree object which indicates
     * a child sequence.  We must create a new <code>Sector</code> object
     * and push it onto the sequence stack as the current sequence under 
     * construction.  If this is the first time this method is called on the
     * proxy tree we must set the newly created <code>Sector</code> object
     * as the master sequence for the tree.
     * 
     * @param pxyNode   <code>AssocTree</code> object representing an <code>AcceleratorSeq</code> object
     * 
     * @see gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor#entering(gov.AssocTree.xal.model.gen.ptree.ProxyTree)
     */
    public void entering(AssocTree pxyNode) throws GenerationException {

        // Create a new model sequence for this tree 
        AcceleratorSeq  smfSeq = pxyNode.getHardwareRef();
        Sector      seqNew = new Sector( smfSeq.getId() );
        
        
        // Check for root node or child node
        if (this.getMasterSector() == null)    {   // first time through - create master sequence
            this.setMasterSector(seqNew);
            
        }   else    {                           // processing a subsequence
            Sector      seqPrev = this.getCurrentSector();
            seqPrev.addChild(seqNew);
            
        }
        
        // Set the new model sequence as the current sequence to build
        this.pushCurrentSector(seqNew);

    }

    /**
     * Catch the end of processing event for a <code>AssocTree</code> node.
     * We must pop the sequence stack to return to the building of the previous 
     * <code>Sector</code> object.
     * 
     * @param pxyNode       dummy argument indicatin we are leaving a <code>AssocTree</code> object
     * 
     * @throws GenerationException      must have been a sequence stack crash
     * 
     * @see gov.IAssocTreeVisitor.xal.model.gen.ptree.IProxyVisitor#leaving(gov.AssocTree.xal.model.gen.ptree.ProxyTree)
     */    
    public void leaving(AssocTree pxyNode) throws GenerationException {
        this.popSector();
    }
    
    
    /*
     * Local Support 
     */


    /**
     * Initialize the sequence generator to the new <code>AcceleratorSeq</code>
     * argument.  Once the generator is initialized it is ready to build a new
     * <code>Sector</code> object represent the argument.
     * 
     * @param   smfSeq  sequence of hardware components
     */
    private void    initializeGenerator(AcceleratorSeq smfSeq)    {
        this.m_cntDrift = 1;
        this.setMasterSector(null);
        this.m_syncMgr = new SynchronizationManager();
    }



    /**
     * Return the master model sequence, which is the root of all the modeling
     * sequences currently under construction.
     * 
     * @return  <code>Sector</code> object currently under construction
     */
    private Sector  getMasterSector()  {
        return this.m_secMaster;
    }
    
    /**
     * Set the master model sequence, the root of all modeling sequences under
     * construction.
     * 
     * @param seq       set the root <code>Sector</code> object
     */
    private void        setMasterSector(Sector seq)    {
        this.m_secMaster = seq;
    }



    /**
     * Pops the {@link #getCurrentSector current sector} off the sector stack 
     * returning it.  Consequently, after this method returns the result of
     * calling <code>getCurrentSector()</code> will be the previous sector on
     * the sector stack.
     * 
     * @return  the current model sector, an <code>Sector</code> object
     * 
     * @exception   GenerationException     stack frame underflow
     */
    private Sector  popSector()   throws GenerationException {
        if (this.m_stackSecs.size() <= 0)
            throw new GenerationException("SequenceGenerator#popSequence() - stack frame underflow");
            
        return (Sector)this.m_stackSecs.pop();
    }
    
    /**
     * Pushes <code>Sector</code> argument onto the sequence stack where
     * it becomes the current sequence.
     * 
     * @param sec   <code>Sector</code> object to become current sequence
     */
    private void        pushCurrentSector(Sector sec)   {
        this.m_stackSecs.push(sec);
    }
    
    /**
     * Returns the current (<code>Sector</code>) object, which is
     * the top of the sector stack.
     * 
     * @return  the current sector, an <code>Sector</code> object
     */
    private Sector  getCurrentSector() {
        return (Sector)this.m_stackSecs.peek();
    }
    
    
    

    /**
     * Return the current <code>SynchronizationManager</code> object for the
     * current master modeling sequence (@see #getMasterSeq) being constructed.
     * 
     * @return  <code>SynchronizationManager</code> object for the current master sequence
     */    
    private SynchronizationManager  getCurrentSyncMgr() {
        return this.m_syncMgr;
    }
    
    
    /**
     * Generate an (unique) identifier string for a drift space modeling 
     * element.  The drift space identifiers will be unique within each 
     * <code>Sector</code> root (given that <code>initializeGenerator()</code>
     * is called).
     * 
     * @return  unique string identifier for a drift space modeling element
     */
    private String  genDriftId()    {
        String      strId = this.getCurrentSector().getId();
        strId +=  this.m_cntDrift++;
        
        return strId;
    }
    
    

    /**
     * Generates a modeling element for the hardware reference argument 
     * and sets up the synchronization between the two.
     *  
     * @param smfNode               hardware object to be processed
     * 
     * @throws GenerationException  unknown hardware object
     */    
    private void    prozessHardware(AcceleratorNode smfNode) 
        throws GenerationException
    {
//    	ElementGenerator.generateElementFor(smfNode);
    }



    /**
     * @since May 2, 2011
     * @see xal.sim.latgen.ptree.IAssocTreeVisitor#process(xal.sim.latgen.ptree.TreeNode)
     */
    @Override
    public void process(TreeNode pxyNode) throws GenerationException {
        // TODO Auto-generated method stub
        
    }
}
