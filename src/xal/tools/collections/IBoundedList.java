/**
 * BoundedList.java
 *
 * @author Christopher K. Allen
 * @since  Apr 11, 2013
 */
package xal.tools.collections;

/**
 * <p>
 * Interface presented by class types representing bounded lists, that is, bounded buffers.
 * This interface is primarily indicative in nature, obviate the intention of the 
 * exposing type.  However, it does contain most of the methods typically needed to use
 * such a collection.
 * </p>
 * <p>
 * An important aspect of a bounded buffer is the iterator it provides.  For example, are the
 * contained elements traversed first in first out, last in first out, as a queue, as a 
 * dequeue, etc.  Thus, this interface inherits from the <code>{@link Iterable}</code>
 * interface in anticipation of such an iteration process.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since  Apr 11, 2013
 *
 */
public interface IBoundedList<T> extends Iterable<T> {
    
    /**
     * Returns the size of the bounded list.
     * 
     * @return  maximum number of objects that the list can manage
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2013
     */
    public int      size();
    
    /**
     * Adds an object to the list, potentially pushing off the oldest object
     * if the buffer is full.
     * 
     * @param   objNew      object to be inserted into the list
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2013
     */
    public void     add(T item);
    
    /**
     * Return the object at the specified index position in this list.  The 
     * relative meaning of the index may be interpreted differently for
     * each type of list.
     * 
     * @param index     index position into the list
     * 
     * @return          object at position <var>index</var>
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2013
     */
    public T        get(int index);
    
    /**
     * Clears the entire contents of the list.  That is,
     * empties the current list.
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2013
     */
    public void     clear();
    
}
