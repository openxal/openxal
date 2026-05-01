/*
 * ChannelRecord.java
 *
 * Created on June 28, 2002, 2:08 PM
 */

package xal.ca;

import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;

import java.lang.reflect.Array;


/**
 * ChannelRecord is a wrapper for the value returned by a get operation on 
 * a Channel.  It a provides convenience methods for getting data back as 
 * one of many primitive types.
 *
 * @author  tap
 */
public class ChannelRecord {
	/** internal data storage */
    protected ArrayValue _store;

	
    /** 
	 * Constructor 
	 * @param adaptor from which to generate a record
	 */
    public ChannelRecord( final ValueAdaptor adaptor ) {
        _store = adaptor.getStore();
    }
    
    
    /**
     * Get the number of elements in the array value.
     * @return The length of the array.
     */
    public int getCount() {
        return _store.getCount();
    }
    
    
    /**
     * Get the native type of the data as a Java class.
     * @return The native type of the data.
     */
    public Class<?> getType() {
        return _store.getType();
    }
    
    
    /**
     * Get the data converted to a scalar byte.  If the data is an array the 
     * the value of the first element is converted to a byte and returned.
     * @return The data as a scalar byte.
     */
    public byte byteValue() {
        return _store.byteValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a byte.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar byte.
     */
    public byte byteValueAt( final int index ) {
        return _store.byteValueAt( index );
    }
    
    
    /**
     * Get the data converted to a byte array.
     * @return The data as a byte array.
     */
    public byte[] byteArray() {
        return _store.byteArray();
    }

    
    /**
     * Get the data converted to a scalar short.  If the data is an array the 
     * the value of the first element is converted to a short and returned.
     * @return The data as a scalar short.
     */
    public short shortValue() {
        return _store.shortValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a short.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar short.
     */
    public short shortValueAt( final int index ) {
        return _store.shortValueAt( index );
    }
    
    
    /**
     * Get the data converted to a short array.
     * @return The data as a short array.
     */
    public short[] shortArray() {
        return _store.shortArray();
    }

    
    /**
     * Get the data converted to a scalar int.  If the data is an array the 
     * the value of the first element is converted to a int and returned.
     * @return The data as a scalar int.
     */
    public int intValue() {
        return _store.intValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a int.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar int.
     */
    public int intValueAt( final int index ) {
        return _store.intValueAt( index );
    }
    
    
    /**
     * Get the data converted to a int array.
     * @return The data as a int array.
     */
    public int[] intArray() {
        return _store.intArray();
    }

    
    /**
     * Get the data converted to a scalar float.  If the data is an array the 
     * the value of the first element is converted to a float and returned.
     * @return The data as a scalar float.
     */
    public float floatValue() {
        return _store.floatValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a float.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar float.
     */
    public float floatValueAt( final int index ) {
        return _store.floatValueAt( index );
    }
    
    
    /**
     * Get the data converted to a float array.
     * @return The data as a float array.
     */
    public float[] floatArray() {
        return _store.floatArray();
    }

    
    /**
     * Get the data converted to a scalar double.  If the data is an array the 
     * the value of the first element is converted to a double and returned.
     * @return The data as a scalar double.
     */
    public double doubleValue() {
        return _store.doubleValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a double.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar double.
     */
    public double doubleValueAt( final int index ) {
        return _store.doubleValueAt( index );
    }
    
    
    /**
     * Get the data converted to a double array.
     * @return The data as a double array.
     */
    public double[] doubleArray() {
        return _store.doubleArray();
    }

    
    /**
     * Get the data converted to a scalar string.  If the data is an array the 
     * the value of the first element is converted to a string and returned.
     * @return The data as a scalar string.
     */
    public String stringValue() {
        return _store.stringValue();
    }
    
    
    /**
     * Get the value of the array element identified by the index and convert 
     * it to a string.
     * @param index The index of the array element to get.
     * @return The data element at the index as a scalar string.
     */
    public String stringValueAt( final int index ) {
        return _store.stringValueAt( index );
    }
    
    
    /**
     * Get the data converted to a string array.
     * @return The data as a string array.
     */
    public String[] stringArray() {
        return _store.stringArray();
    }
    
    
    /**
     * Override toString to return a representation of the data as an array.
     * @return A string representation of this object.
     */
    public String toString() {
        return "value: " + _store.toString();
    }
    
    
    /**
     * Convert the _store from a raw value to a processed value.
     * @param transform The transform used to convert the store.
     * @return this instance as a convenience.
     */
    ChannelRecord applyTransform( final ValueTransform transform ) {
        _store = transform.convertFromRaw( _store );
        return this;
    }
    
    
    /**
     * Get the internal storage.
     * @return The internal data storage.
     */
    ArrayValue arrayValue() {
        return _store;
    }
}
