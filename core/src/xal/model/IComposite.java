/*
 * Created on May 24, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.model;

import java.util.Iterator;

/**
 * Represents a composite modeling structure.  Such a structure is typically
 * build from <code>IElement</code> objects and other composites exposing
 * the <code>IComposite</code> interface.  Both interfaces are derived from
 * the base interface <code>IComponent</code>.
 * 
 * @author Christopher K. Allen
 *
 */
public interface IComposite extends IComponent {
    /**
     * Return iterator over direct descendants only of this composite element
     * in sequence.
     * 
     * @return  interface to iterator object
     * 
     * @see java.util.Iterator
     */
    public Iterator<IComponent> localIterator();
    
    /**
     * Returns iterator over <b>all</b> the components in this composite 
     * element in proper sequence.  Parent objects should be returned first
     * than all the children.  This would be the order in
     * which the probe visits each component.
     * 
     * @return  interface to iterator object
     * 
     * @see java.util.Iterator
     * @see xal.model.CompositeGlobalIterator
     */
    public Iterator<IComponent> globalIterator();    
    
    /**
     * Return the number of direct descendants in this composite.  That is,
     * any child composite nodes only get a single count.
     * 
     * @return  number of direct child nodes
     */
    public int      getChildCount();
    
    /**
     * Return the direct descendant given by the index.
     * 
     * @param       index       index of child node (index origin 0)
     * @return      direct child node specified by index.
     * 
     * @throws IndexOutOfBoundsException    index was greater than child count
     */
    public IComponent getChild(int index) throws IndexOutOfBoundsException;
    
    /**
     * Add a direct child object to the current composite element.  The child
     * should be inserted at the tail.
     * 
     * @param iComp     interface to the new child element
     */
    public void     addChild(IComponent iComp);
    
    /**
     * Remove the specified child from the composite element
     * 
     * @param iComp     interface to the child to be removed
     * 
     * @return          true if child was found and removed, false otherwise
     */
    public boolean  remove(IComponent iComp);
    
    
    
    /*
     * Dynamics
     */
     
    /** 
     * Propagates the Probe object through this composite element 
     * sequentially element by element. 
     * 
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; This looks to be redundant!  It is overriding the 
     * same method in the super interface <code>IComponent</code>.
     * </p>
     *
     *  @param  probe   probe state will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error advancing probe state
     *  
     *  @see    IComponent#propagate(IProbe)
     */
    public void propagate(IProbe probe) throws ModelException;
    
    
}
