/*
 * Created on May 24, 2004
 *
 */
package xal.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of an iterator class for <code>IComposite</code> objects.
 * Returns each interface in the sequence, leaf or branch.  Consequently
 * the returned interface should be typed to <code>IComponent</code>.
 * 
 * The iteration order for child composite elements is parent first 
 * then all its children.
 * 
 * @author  Christopher K. Allen
 * @author  Ikeda
 * 
 * @see xal.model.IComposite
 */
public class CompositeGlobalIterator implements Iterator<IComponent> {
    
    /*
     * Local Attributes
     */
    
    /** The wrapped iterator object */
    private final Iterator<IComponent> iterator;
    
    /**
     * Create a new <code>CompositeGlobalIterator</code> object connected to the 
     * specific <code>IComposite</code> interface.
     * 
     * @param   composite     interface to composite element to iterate
     */
    public CompositeGlobalIterator(IComposite  composite)   {
        List<IComponent> compList = new LinkedList<IComponent>();
        buildFlatList(composite, compList);
        iterator = compList.iterator();
     }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public IComponent next() {
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
    }
    
    /*
     * Support Methods
     */

    /**
     * Flatten the specified composite element and save it to the internal
     * sequential list to be iterated.  This is a recursive algorithm which
     * calls itself on any child object that are also composite. 
     * 
     * The flattened list is ordered with the parent first then all its children.
     * 
     * @param   composite     interface of current composite object
     * @param   compList     the flattened list
     */
    private static void buildFlatList(IComposite composite, List<IComponent> compList)  {
        for (int i = 0;  i < composite.getChildCount(); i++)   {
            IComponent child = composite.getChild(i);
            
            compList.add(child);    
            
            if (child instanceof IComposite)     
                buildFlatList((IComposite)child, compList); 
        }
        
    }

}
