/**
 * FixedSizeBuffer.java
 * 
 * Created      : September, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.collections;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * <p>
 * Implementation of a linear, fixed-length data buffer.  The current version follows a
 * list implementation and, thus, the collection is naturally ordered according
 * to their relative point of insertion.
 * </p>
 * <p>
 * Objects are added to the head of the buffer.  Once the buffer becomes "full",
 * that is, the number of objects contained within equals the buffer size, any
 * additional additions will cause objects to fall off the tail of the buffer.
 * </p>
 * <p>
 * The primary difference between this class and the class <code>{@link CircularBuffer}</code>
 * are the iterators provided.  The <code>CircularBuffer</code> class uses a 
 * Last In First Out (LIFO) iteration policy
 * while this class uses a First In First Out (FIFO) policy.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  Sep 2007
 *
 */
public class LinearBuffer<T> implements IBoundedList<T> {

    
    /*
     * Global Constants
     */
    
    /** the default buffer length 
     *
     * @deprecated
     */
    @Deprecated
    public static final int     DEF_LEN_BUFFER = 100;
    
    
    
    
    /*
     * Local Attributes
     */
    
    
    /** length of the buffer */
    private int             szBuffer;
    
    /** buffer of objects */
    private LinkedList<T>   lstBuffer;
    

    
    /*
     * Initialization
     */
    
    /**
     * Create a new, empty <code>FixedSizeBuffer</code> instance with the 
     * specified length.
     * 
     * @param   szBuffLen       Length of the data buffer
     */
    public LinearBuffer(int szBuffLen) {
        this.szBuffer = szBuffLen;
        this.lstBuffer = new LinkedList<T>();
    }
    
    /**
     * (Re)set the size of the buffer length.  If the new size of the buffer is
     * less the previous size objects at the tail are lost.
     * 
     * @param   szBuffer   new length of the data buffer
     */
    public void setBufferLength(int szBuffer)    {
        
        // If the new length is shorter throw off those data at the end
        if (szBuffer < this.szBuffer)
            for (ListIterator<T> i = this.lstBuffer.listIterator(szBuffer); i.hasNext(); )
                this.lstBuffer.removeLast();
        
        this.szBuffer = szBuffer;
    }
    
    
    /*
     * IBoundedList Interface
     */
    
    /**
     * Returns the maximum number of objects that this collection can 
     * hold.
     * 
     * @return      capacity of the buffer
     *
     * @author Christopher K. Allen
     * @since  Apr 11, 2013
     */
    public int  size() {
        return this.szBuffer;
    }
    
    /**
     * Adds an object to the buffer, pushing off an old object
     * if the buffer is full.
     * 
     * @param   objNew      object to be inserted at buffer head
     */
    public void add(T objNew)    {
        this.lstBuffer.addFirst(objNew);
        
        // Push last reading off tail of list if necessary
        if (this.lstBuffer.size() > this.szBuffer)
            this.lstBuffer.removeLast();
    }

    /**
     * Return the object at the specified position in this buffer.  The 
     * buffer uses a last in first out (LIFO) policy.
     * 
     * @param index     index position from head
     * 
     * @return          object at position <var>index</var>
     * 
     * @throws IndexOutOfBoundsException    index value exceeds buffer size
     */
    public T    get(int index)  throws IndexOutOfBoundsException {
        return this.lstBuffer.get(index);
    }
    
    /**
     * Clear out the contents of the data buffer.
     */
    public void clear() {
        this.lstBuffer.clear();
    }


    
    /*
     * Iterable Interface
     */
    
    /**
     * Return an iterator object for this buffer which traverses the
     * buffer objects in Last In First Out order.
     * 
     * @return      iterator for  buffer
     */
    public Iterator<T> iterator()  {
        return this.lstBuffer.listIterator();
    }

    
    /*
     * Operations
     */
    
    /**
     * Return an iterator object for this buffer which starts with the
     * object at the given index and ends with the last object in the
     * buffer.
     * 
     * @return      offset iterator for the buffer
     */
    public Iterator<T>  iterator(int index) {
        return this.lstBuffer.listIterator(index);
    }
    
    
    /*
     * Debugging
     */
    
    /**
     * Print out the contents of the buffer as a string.
     * 
     * @return  buffer content in text form
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String  strBuffer = "";
        
        strBuffer += "buffer size = " + this.szBuffer + "\n";
        strBuffer += "contents : " + this.lstBuffer + "\n";
        
        return strBuffer;
    }
    
    
    
}
