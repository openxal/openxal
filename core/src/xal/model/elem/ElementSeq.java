/*
 * ElementSeq.java
 *
 * Created on February 20, 2003, 11:13 AM
 */

package xal.model.elem;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import xal.model.CompositeGlobalIterator;
import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.sim.scenario.LatticeElement;



/**
 * <p>
 * Convenience abstract base class for a composite modeling element.  The 
 * composite is represented as a sequence of modeling elements, perhaps other
 * composites.  Specifically we have an ordered list of child modeling elements.
 * </p>
 * <p>
 * Propagation of probes (objects exposing the <code>IProbe</code> interface)
 * is done by passing it sequentially to each child.  Of course if a child is 
 * also a composite the same is done there.
 * </p>
 *   
 * @author  Christopher K. Allen
 * @version 2.0 February, 2009
 */
public abstract class ElementSeq implements IComposite {

    
    /*
     *  Global Attributes
     */
    
    /** default number of element positions to reserve in list array */
    public static final int         s_szDefReserve = 10;
    
    
    
    
    /*
     *  Defining Attributes
     */
    
    /** the element type identifier */
    private String      m_strType;
    
    /** element instance identifier of element */
    private String      m_strId;
    
    /** Identifier string of the model hardware node */
    private String      strSmfId;
    

    /** user comments regarding this sequence */
    private String      m_strComment;

    
    /** indicates that the composite had been modified and on-demand parameters must be recomputed */
    private boolean     bolDirty;
    
    /** the current length of this composite on demand */
    private double      dblLen;
    
    /** the current position of this composite within its parent, on demand */
    private double      dblPos;
    
    
    //
    //  Structure
    //
    
    /** the parent composite structure that owns this composite element */
    private IComposite  cpsParent;
    
    /** 
     * List of IComponent objects composing composite sequence
     * order upstream to downstream 
     */
    private List<IComponent>        m_lstCompsForward;
    
    /** 
     * List of IComponent objects composing composite sequence
     * order downstream to upstream.
     */
    private List<IComponent>        m_lstCompsBackward;
    

    /*
     * Initialization
     */    
     
    /**
     *  Creates a new instance of <code>ElementSeq</code> without of the
     *  given soft type but without a sequence identifier.
     *  
     *  @param  strType     soft type of the sequence (defined by the child class)
     */
    public ElementSeq(String strType) {
        this(strType, null , s_szDefReserve);
    }
 
    /**
     *  Creates a new instance of <code>ElementSeq</code> with the
     *  given soft type and sequence identifier.
     *
     *  @param  strType     soft type of the sequence (defined by the child class)
     *  @param  strId       identifier of the sequence
     */
    public ElementSeq(String strType, String strId) {
        this(strType, strId, s_szDefReserve);
    }
 
    /**
     *  Creates a new instance of <code>ElementSeq</code> with the
     *  given soft type and sequence identifier.
     *  Also reserves space for a the sequence elements.
     *  This saves a little CPU time if the relative size of the
     *  sequence is known <i>a priori</i>.
     *
     *  @param  strType     soft type of the sequence (defined by the child class)
     *  @param  strId       identifier of the sequence
     *  @param  szReserve   number of Element spaces to reserve
     */
    public ElementSeq(String strType, String strId, int szReserve) {
        m_lstCompsForward = new ArrayList<IComponent>(szReserve);
        m_lstCompsBackward = new ArrayList<IComponent>(szReserve);
        m_strType = strType;
        m_strId = strId;
        strSmfId = "";
        bolDirty = true;
        dblLen = 0.0;
        dblPos = 0.0;
    }

    
    /**
     *  Set the string identifier for the element.
     *
     *  @param  strId       new string identifier for element
     */
    public void setId(String strId) {
        m_strId = strId;
    };

    /**
     * Sets the string identifier of the hardware node which this
     * element models.  Node that this sequence probably models an
     * accelerator sector, or logical unit.  Thus, this ID is likely
     * of the type "HEBT", "RING", "DTL", etc.
     * 
     * @param strSmfId  identifier for the modeled hardware node (SMF object)
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2014
     */
    public void setHardwareNodeId(String strSmfId) {
        this.strSmfId = strSmfId;
    }

    /**
     *  Sets any user comment associated with this sequence.
     *
     *  @param  strComment  string containing user comments
     */
    public void setComments(String strComment)  {
        m_strComment = strComment;
    }

    
    /*
     *  Sequence Properties
     */

    /**
     *  Get any user comments regarding this sequence.  Returns the null string if none.
     *
     *  @return         string containing user comments
     */
    public String   getComments()   { return m_strComment; };
    
    /**
     *  Get the number of <code>IElement</code> derived objects contained
     *  in this sequence.
     * 
     * @return          number of <code>Element</code> object w/in sequence
     */
    public int  getLeafCount()   {
        Iterator<IComponent> iter = this.childIterator();
        
        int cntElem = 0;
        while (iter.hasNext())  {
            IComponent    ifcComp = iter.next();
            
            if (ifcComp instanceof ElementSeq) {
                cntElem += ((ElementSeq)ifcComp).getLeafCount();

            } else if (ifcComp instanceof IElement)  {
                cntElem++;
                
            } else  {
                continue;
                
            }
        }
        
        return cntElem;
    }

    /**
     * Return the list of <code>IElement</code> objects contained
     * in this sequence.
     * 
     * @return  list of elements composing this sequence
     */
    public  List<IComponent> getElementList()    {
        return  this.m_lstCompsForward;
    }
    
    /**
     * Returns a list of <em>all</em> elements contained in this
     * sequence, more specifically, all leaf elements.
     * 
     * @return  list containing all <code>IComponent</code> class 
     *          elements in this sequence
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    public List<IComponent> getAllElements() {
        List<IComponent>        lstCmps = new ArrayList<>();
        Iterator<IComponent>    itrCmps =  this.globalIterator();
        
        while (itrCmps.hasNext()) {
            IComponent cmp = itrCmps.next();
            
            lstCmps.add(cmp);
        }
        
        return lstCmps;
    }
    
    
    /*
     *  Operations
     */
    
    /**
     * Return an <code>Iterator</code> object that iterates over the direct
     * descendants only of this composite element, in reverse order.
     * 
     * @return  interface to iterator object
     * 
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     * 
     * @see     java.util.Iterator
     */ 
    public Iterator<IComponent> localBackIterator() {
        return this.getReverseCompList().iterator();
    }
     
    /**
     * Return an <code>Iterator</code> object that iterates over <b>every</b> 
     * <code>IComponent</code> object in this composite.  For 
     * <code>IComponent</code> which are also composite the parent is 
     * returned first, then all its children.  This would be in reverse
     * order.
     * 
     * @return  <code>Iterator</code> interface to iterator object
     * 
     * @see     java.util.Iterator
     */
    public Iterator<IComponent> globalBackIterator()  {
        Iterator<IComponent> flatListIter = new CompositeGlobalIterator(this);
        List<IComponent> reverseFlatList = new ArrayList<IComponent>();
        
        while (flatListIter.hasNext()) {
            IComponent comp = flatListIter.next();
            
            reverseFlatList.add(0, comp);
        }
        
        return reverseFlatList.iterator();
    }
    
    /**
     *  Return an <code>Iterator</code> object that cycles through
     *  all the direct children of the sequence.  Note that any child
     *  may have children itself.
     * 
     * @return  iterator of <code>IElement</code> interfaces
     */
    public Iterator<IComponent> childIterator() {
        return this.getForwardCompList().iterator();
    }
    
    
    /**
     *  Concatenate the indicated <code>ElementSeq</code> object
     *  to the tail of this sequence.
     * 
     *  @param  seq     object to append to this one
     */
    public void concatenateEquals(ElementSeq seq)   {
        Iterator<IComponent> iter = seq.childIterator();
        
        while (iter.hasNext()) {
            IElement    ifcNext = (IElement)iter.next();
            
            this.addChild(ifcNext);
        }
    }
    
    
//  /**
//  *  Return total time to propagate through all elements in this sequence 
//  *  for the given probe up to the specified length.  If the specified
//  *  length is >= to the sequence's length, returns the elapsed time for 
//  *  the entire sequence.
//  * 
//  *  WARNING:
//  *  The probe is NOT propagated, no acceleration applied so this value may
//  *  not be the same if the probe was actually propagated.
//  * 
//  *  @param  probe   propagating probe
//  *  @param  dblLen  length of subsection to propagate through <b>meters</b>
//  *  
//  *  @return         total elapsed time through section<b>Units: seconds</b> 
//  */
// public double elapsedTime(IProbe probe, double dblLen)  {
//     double     dblTime;    // total energy gain of sequence
//     Iterator    iter;       // element iterator
//     double     remLength;  // remaining length of subsection to calculate energy gain for
//     
//     dblTime = 0.0;
//     iter = this.getArray().iterator();
//     remLength = dblLen;
//     
//     while ((iter.hasNext()) && (remLength > 0))  {
//         IElement elem = (IElement)iter.next();
//         double elemLength = elem.getLength();
//         double calcLength = Math.min(elemLength, remLength);
//         
//         dblTime += elem.elapsedTime(probe, calcLength);
//         
//         remLength = remLength - calcLength;
//     }
//     
//     return dblTime;
// }
// 
// /**
//  *  Returns total energy gain provided by all elements in this sequence 
//  *  for the particular probe up to the specified length.  If the specified
//  *  length is >= to the sequence's length, returns the energy gain for 
//  *  the entire sequence.
//  * 
//  *  WARNING:
//  *  The probe is NOT propagated, no acceleration applied so this value may
//  *  not be the same if the probe was actually propagated.
//  * 
//  *
//  *  @param  probe   determine energy gain for this probe
//  *  @param  dblLen  length of sequence subsection to calculate energy gain
//  *
//  *  @return         total energy gain provided by sequence <b>Units: eV</b>
//  */
// public double energyGain(IProbe probe, double dblLen) {
//     double     dblDelW;    // total energy gain of sequence
//     Iterator    iter;       // element iterator
//     double     remLength;  // remaining length of subsection to calculate energy gain for
//     
//     dblDelW = 0.0;
//     iter = this.getArray().iterator();
//     remLength = dblLen;
//     
//     while ((iter.hasNext()) && (remLength > 0))  {
//         IElement elem = (IElement)iter.next();
//         double elemLength = elem.getLength();
//         double calcLength = Math.min(elemLength, remLength);
//         
//         dblDelW += elem.energyGain(probe, calcLength);
//         
//         remLength = remLength - calcLength;
//     }
//     
//     return dblDelW;
// }
// 
// /**  
//  *  Compute the transfer map of the entire sequence for a given probe
//  *  up to the specified length.  If the length parameter is greater than
//  *  the sum of the lengths of the sequence's elements, return the transfer
//  *  matrix for the entire sequence.  If the length parameter is smaller than
//  *  the sums of the lengths of the sequence's elements, return the transfer
//  *  matrix for the sequence from its beginning up to the specified length.
//  *  The total transfer matrix is the product of all the transfer matrices
//  *  in the sequence.  
//  *
//  *  @param  probe   typically transfer matrices depend upon probe parameters
//  *  @param  dblLen  length of sequence subSection to calculate transfer map
//  *
//  *  @return         composite transfer map of sequence for probe
//  *
//  *  @exception  ModelException  the transfer map could not be computed
//  */
// public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException {
//     PhaseMap        mapPhi;     // composite transfer map of sequence
//     Iterator        iter;       // element iterator
//     double          remLength;   // remaining length to get Map for
//     
//     mapPhi = PhaseMap.identity();
//     iter = this.getArray().iterator();
//     remLength = dblLen;
//     
//     while ((iter.hasNext()) && (remLength > 0)) {
//         IElement elem = (IElement)iter.next();
//         double elemLength = elem.getLength();
//         double mapLength = Math.min(elemLength, remLength);
//         
//         mapPhi.composeEquals( elem.transferMap(probe, mapLength) );
//         
//         remLength = remLength - mapLength;
//     }
//     
//     return mapPhi;
// }
 
 
    /*
     *  IComponent Interface
     */
    
    /**
     * Initialization method to be provided by the user
     * 
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement)
    {
        String  strElemId = latticeElement.getModelingElementId();
        String  strSmfId  = latticeElement.getHardwareNode().getId();
        
        setId( strElemId != null ? strElemId : strSmfId);
        setHardwareNodeId(strSmfId);
//      setId(latticeElement.getNode().getId());
    }
    
    /**  
     *  Get the type identifier for the composite element.
     *
     *  @return     type identifier for ElementSeq
     */
    @Override
    public String getType() { return m_strType; }
    
    /**  
     *  Get the sequence identifier 
     *
     *  @return     sequence identifier
     */
    @Override
    public String getId() { return m_strId; }
    
    /**
     * Returns the string identifier of the hardware node which this
     * element models.  This value is likely a sector of an accelerator
     * structure, such as "HEBT", "RING", "MEBT", etc.
     * 
     * @return      the identifier string of the hardware this element models
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2014
     */
    @Override
    public String   getHardwareNodeId() {
        return this.strSmfId;
    }

    /**  
     * <p>
     * Return the length of the sequence.  The length of the sequence is determined but
     * summing the lengths of all the contained <code>IComponent</code> objects.
     * </p>
     *
     *  @return     total length of the sequence (in <b>meters</b>)
     */
    @Override
    public double getLength() {
        if (this.bolDirty)
            this.compDependParams();
        
        return this.dblLen;
    }

    /**
     *
     * @see xal.model.IComponent#getPosition()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public double getPosition() {
        if (this.bolDirty)
            this.compDependParams();
        
        return this.dblPos;
    }

    /**
     *
     * @see xal.model.IComponent#getLatticePosition()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public double getLatticePosition() {
        if (this.bolDirty)
            this.compDependParams();
        
        // If this returns it should return 0.0
        if (this.getParent() == null)
            return this.getPosition();
        
        // If parent is top level then our local position is the global position
        double  dblParPos = this.getParent().getLatticePosition();
        if (dblParPos == 0.0)
            return this.getPosition();
        
        // This is a nested sequence
        double  dblLocPos = this.getPosition();
        double  dblParLen = this.getParent().getLength();
        double  dblGblPos = (dblParPos - dblParLen/2.0) + dblLocPos;
         
        return dblGblPos;
    }
    
    /**
     * @return  returns the composite structure owning this composite structure, 
     *          or <code>null</code> if this structure is top level
     *
     * @see xal.model.IComponent#getParent()
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public IComposite getParent() {
        return this.cpsParent;
    }

    /**
     * Sets the parent structure containing this composite structure. 
     * The parent is assumed to be a composite structure built from component 
     * elements.
     * 
     * @return the composite structure built from this structure
     *
     * @see xal.model.IComponent#setParent(xal.model.IComposite)
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public void setParent(IComposite cpsParent) {
        this.cpsParent = cpsParent;
        this.setDirty(this);
        this.getParent().setDirty(this);
    }

    
    /** 
     * <p>Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}</p>
     *
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     *
     * @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    @Override
    public void propagate(IProbe probe, double pos) throws ModelException { 
    	propagate(probe);
    }

    /**
     *  Propagate probe through sequence
     *
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {
        for(IComponent comp : getForwardCompList()) {
            comp.propagate(probe);
        }
    }
    
    /** 
     * <p>Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}</p>
     *
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     *
     * @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    @Override
    public void backPropagate(IProbe probe, double pos) throws ModelException { 
    	backPropagate(probe);
    }

    /**
     * <p>
     *  Backward propagation of probe through sequence.
     * </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     */
    @Override
    public void backPropagate(IProbe probe) throws ModelException {
        for(IComponent comp : getReverseCompList()) {
            comp.backPropagate(probe);
        }
    }

    
    /*
     * IComposite Interface
     */
    
    /**
     * Return an <code>Iterator</code> object that iterates over the direct
     * descendants only of this composite element, in order.
     * 
     * @return  interface to iterator object
     * 
     * @see     java.util.Iterator
     */ 
    @Override
    public Iterator<IComponent> localIterator() {
        return this.getForwardCompList().iterator();
    }
     
    /**
     * Return an <code>Iterator</code> object that iterates over <b>every</b> 
     * <code>IComponent</code> object in this composite.  For 
     * <code>IComponent</code> which are also composite the parent is 
     * returned first, then all its children.  This would be in reverse
     * order.
     * 
     * @return  <code>Iterator</code> interface to iterator object
     * 
     * @see     java.util.Iterator
     */
    @Override
    public Iterator<IComponent> globalIterator()  {
        return new CompositeGlobalIterator(this);
    }
    
    /**
     * Get the number of direct children in this sequence.  Note that this is 
     * not the number of leaves in the sequence.
     *
     *  @return         number of direct descendants
     */
    @Override
    public int  getChildCount() { return this.getForwardCompList().size(); };
    
    /**
     *  Get the child IComponent interface at location 
     *  specified by index.
     *
     *  @param  indChild    position index within the sequence list
     *
     *  @return             child at position indChild
     */
    @Override
    public IComponent getChild(int indChild) {
        return this.getForwardCompList().get(indChild);
    }
    
    /**
     *  <p>
     *  Add a component object at the tail of the sequence list and at the
     *  head of the reverse sequence list.
     *  </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Added support for backward propagation
     * February, 2009
     * </p>
     *
     *  @param  iComp   new component object
     */
    @Override
    public void addChild(IComponent iComp)   {
        this.getForwardCompList().add(iComp);
        this.getReverseCompList().add(0, iComp);
        iComp.setParent(this);
        if (iComp instanceof IComposite)
            ((IComposite)iComp).setDirty(this);

        this.setDirty(this);
    }
    
    /**
     * Remove an element from the entire tree.  The element can be a single
     * leaf node or a composite node.
     *
     * @param  iCmp    IComponent object to be removed
     * 
     * @return  return true if element was found and removed, false otherwise
     */
    @Override
    public boolean remove(IComponent iCmp)    {
        
        // Inspect each child for specified element
        for (IComponent iChild : this)   {                   
            
            if (iChild == iCmp)   {                // is this child the one?
                this.getForwardCompList().remove(iCmp);
                this.getReverseCompList().remove(iCmp);
                
                iCmp.setParent(null);
                
                this.setDirty(this);
                
                if (iCmp instanceof IComposite) 
                    ((IComposite)iCmp).setDirty(this);
                    
                return true;                        
            }
            
            if (iChild instanceof IComposite)  {        // if child is composite
                if (  ((IComposite)iChild).remove(iCmp)  )  // true to remove it
                    return true;                            // it was a child of my child 
                                                            //    and was removed
            }
        }
        
        return false;       // did not encounter specified element
    };
    
    /**
     *
     * @see xal.model.IComposite#setDirty()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public void setDirty(IComponent cmpCaller) {
        
        // Set the dirty flag
        this.bolDirty = true;
        
//        // Let my parent know I have been changed
//        if (this.getParent() != null)
//            this.getParent().setDirty(this);
//        
//        // Let my children know I have been changed
//        for (IComponent cmp : this) {
//            if (cmp == cmpCaller)       // one of my children might have invoked this operation
//                break;
//            if (cmp instanceof IComposite)
//                ((IComposite)cmp).setDirty(this);
//        }
    }

    
    /*
     * Iterable<IComponent> Interface
     */
    
    /**
     * Returns a shallow iterator for the direct descendants of this composite
     * modeling element.  This is the same iterator returned by 
     * <code>{@link #localIterator()}</code>.  This method is needed by 
     * the <code>Iterable</code> interface to satisfy <code>foreach</code>
     * language constructions.
     * 
     * @return  a shallow iterator traversing the direct descendants of this container
     *
     * @since  Dec 2, 2015,   Christopher K. Allen
     */
    @Override
    public Iterator<IComponent>    iterator() {
        return this.localIterator();
    }
    
    
    /*
     * Object Overrides
     */
    
    /**
     * Writes out the modeling elements in this element sequence
     * structure.
     *
     * @see java.lang.Object#toString()
     *
     * @since  Jan 5, 2015   by Christopher K. Allen
     */
    @Override
    public String   toString() {

        StringBuffer         bufOutput = new StringBuffer();
        Iterator<IComponent> iterCmps  = this.getForwardCompList().iterator();
        
        bufOutput.append("Sequence ID: " + this.getId() + '\n');
        
        while (iterCmps.hasNext())  {
            IComponent iCmp = iterCmps.next();
            
            bufOutput.append( iCmp.toString() );
            bufOutput.append('\n');
        }
        
        return bufOutput.toString();
    }
    
    
    /*
     *  Testing and Debugging
     */

    /**
     *
     * @see java.lang.Object#toString()
     *
     * @since  Feb 3, 2015   by Christopher K. Allen
     */
    public String toStringLegacy() {

        StringPrinter   sprnOut = new StringPrinter();
        PrintWriter     pwtrOut = new PrintWriter(sprnOut);

        this.print(pwtrOut);

        return sprnOut.toString();
    }


    /**
     *  Dump contents to a text stream.
     *
     *  @param  os      output stream
     */
    public void print(PrintWriter os)    {
        os.println(this.m_strId + " modeling HWID=" + this.strSmfId);
        os.println("  type code=" + this.m_strType + ", class type=" + this.getClass().getName());
        os.println();
        
        Iterator<IComponent> iter = this.getForwardCompList().iterator();
        
        while (iter.hasNext())  {
            IElement ifc = (IElement)iter.next();
            
            if (ifc instanceof Element) {
                Element elem = (Element)ifc; 
                elem.print(os);
                os.println("");
            }
            
            if (ifc instanceof ElementSeq)   {
                ElementSeq seq = (ElementSeq)ifc;
                os.println("ElementSeq - " + seq.getId());
                os.println("  commments : " + seq.getComments() );
                os.println("  length    : " + seq.getLength());
                os.println("  children  : " + seq.getChildCount());
                os.println("  elements  : " + seq.getLeafCount());
                os.println("");
                
                seq.print(os);
                os.println("End ElementSeq - " + seq.getId() );
                os.println("");
            }
        }
    }

    
    /*
     * Child Class Support
     */
    
    /**
     *  Return the internal list of components
     */
    protected List<IComponent> getForwardCompList()  { 
        return m_lstCompsForward; 
    }
    
    /**
     * <p>
     * Return the reverse component list (downstream to upstream)
     * </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Added support for backward propagation
     * February, 2009
     * </p>
     * @return  <code>List</code> of sequence components in reverse order
     * 
     * @since Feb 27, 2009
     * @author Christopher K. Allen
     * 
     */
    protected List<IComponent> getReverseCompList() { 
        return this.m_lstCompsBackward;
    }
	
	/**
	 * <p>
	 * Set the comp list to the new list of elements.
	 * </p>
	 * <p>
	 * <strong>NOTES</strong>: CKA
	 * <br>
	 * &middot; Added support for backward propagation
	 * February, 2009
	 * </p>
	 * 
	 * @param elements the new list of elements
	 */
	protected void setCompList( final List<? extends IComponent> elements ) {
		m_lstCompsForward = new ArrayList<IComponent>( elements );
		
		// Create reverse list
		m_lstCompsBackward = new ArrayList<IComponent>();
		for (IComponent comp : elements) 
		    m_lstCompsBackward.add(0, comp);
	}

	/**
	 * Recompute all the dependent parameters. Right now that is the sequence length
	 * and the position of this sequence within the parent sequence.  The dirty flag
	 * is cleared once it's all done.
	 *
	 * @since  Dec 3, 2015,   Christopher K. Allen
	 */
	protected void compDependParams() {
	    
	    // Check if there is anything to do?
	    if (!this.bolDirty)
	        return;
	    
	    // Compute the total length
        double dblMyLen = 0.0;
        for(IComponent comp : this) {
            dblMyLen += comp.getLength();
        }
        this.dblLen = dblMyLen;

        // Check for parent - if none our position is zero
        if (this.getParent() == null) {
            this.dblPos = 0.0;
            this.bolDirty = false;
            
            return;
        }
        
        // Compute position within parent composite
        double  dblMyPos = 0.0;
        
        for (IComponent cmp : this.getParent()) {
            if (cmp.equals(this)) {
                this.bolDirty = false;
                this.dblPos = dblMyPos + this.dblLen/2.0;
                
                return;
            }
            
            dblMyPos += cmp.getLength();
        }
        
        // If we made it here, something went wrong.  Likely that we are
        //  have the wrong parent or our parent does not know us.
        //  I'm not clearing the dirty flag wo this message will swamp the console.
        System.err.println("#compDependParams: inconsistent parent-child relationship between " + this.getParent().getId() + " and " + this.getId());
	}
}



    
/*
 * Auxiliary Classes
 */

/**
 * String buffer that mimics the Java writer functions.
 * 
 *
 * @author Christopher K. Allen
 * @since  Feb 3, 2015
 */
class StringPrinter extends StringWriter {
    
    /**
     * The print line function.  Prints the given string and terminates with a 
     * new line character.
     * 
     * @param strLine   text line 
     *
     * @since  Feb 3, 2015   by Christopher K. Allen
     */
    public void println(String strLine) {
        this.append(strLine);
        this.append('\n');
    }
}
    
    
    

/*
 * Legacy
 */


///**
// *  Propagate probe through sequence
// *
// *  @param  probe   the state of the probe will be advance using the elements dynamics
// *
// *  @exception  ModelException    an error occurred while advancing the probe state
// */
//public void propagate(IProbe probe) throws ModelException {   
//    
//    Iterator iterElem = this.getElems().iterator();
//    
//    boolean     bolStarted = false;
//    boolean     bolStopped = false;
//        
//    if (this.getStartPropElement() == null)
//        bolStarted = true;
//        
//    while (iterElem.hasNext()) {
//        IElement elem = (IElement)iterElem.next();
//
//        // Starting gate
//        if (!bolStarted)    
//            if (elem == this.getStartPropElement()) {
//                bolStarted = true;
//            } else  { continue;  }
//            
//        // Through gate - check for last element
//        if (!bolStopped)    
//            if (elem == this.getStopPropElement())   {
//                bolStopped = true;
//            }
//
//        elem.propagate(probe);
//            
//        // Check if we have stopped
//        if (bolStopped) return;
//    }
//}



//    /**
//     *  Creates (but does not load parameters) an XAL modeling element based on 
//     *  the information in the DataAdaptor.
//     *  The DataAdaptor should contain an XML node contain the <b>Element</bd> tag
//     *  of the XAL Model DTD.
//     *  <p>
//     *  The element may be either a <code>ThickElement</code> or a <code>ThinElement
//     *  </code> derived element depending upon the value of the <b>fam</b> attribute
//     *  in the DataAdaptor.
//     * 
//     *  @param  daptElem    DataAdaptor contiaining attributes of an Element tag
//     *
//     *  @return             newly created XAL modeling element (no specific parameters are set)
//     *
//     *  @exception  DataFormatException     <b>fam</b> attribute not present in DataAdaptor
//     *  @exception  NumberFormatException   bad number format in numeric attribute
//     *  @exception  ClassNotFoundException  <b>type</b> value not recognized by ElementFactory
//     */
//    protected Element createElement(DataAdaptor daptElem)  
//        throws DataFormatException, NumberFormatException, ClassNotFoundException
//    {
//        String      strFam;         // element family
//        String      strType;        // element type
//        String      strId;          // element identifier
//        String      strPos;         // element lattice position
//        String      strSecs;        // number of thick element subsections
//        String      strLen;         // element length (ThickElement)
//        
//        
//        // Get all the element attributes
//        strFam  = daptElem.stringValue("fam");
//        strType = daptElem.stringValue("type");
//        strId   = daptElem.stringValue("id");
//        strPos  = daptElem.stringValue("pos");
//        strSecs = daptElem.stringValue("nsecs");
//        strLen  = daptElem.stringValue("len");
//        
//        
//        // Create element according to its family
//        Element         elem;       // new element to be created
//        
//        // Create a ThinElement type
//        if (strFam.equals("THIN")) {
//            Double  dblPos = Double.valueOf(strPos);
//            
//            elem = ElementFactory.createThinElement(strType, strId, dblPos);
//
//        // Create a ThickElement type
//        } else if (strFam.equals("THICK"))  {
//            Integer nSecs  = Integer.valueOf(strSecs);
//            Double  dblLen = Double.valueOf(strLen);
//            Double  dblPos = Double.valueOf(strPos);
//
//            elem = ElementFactory.createThickElement(strType, strId, nSecs, dblLen, dblPos);
//            
//        // There are no other element types, an error must have occurred
//        } else  {
//            throw new DataFormatException("ElementSeq#createElement - 'fam' attribute not found in Element " + strId);
//            
//        }
//        
//        
//        return elem;
//    }
    
