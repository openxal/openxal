/*
 * ArrayUtil.java
 *
 * Created on August 19, 2002, 10:19 AM
 */

package xal.tools;

import xal.tools.StringJoiner;

import java.lang.reflect.Array;
import java.util.*;

/**
 * ArrayTool is a class that adds common convenience methods for dealing with arrays.
 * In particular, it has static methods for dealing with multi-dimensional arrays.
 *
 * @author  tap
 */
public class ArrayTool {
    
    /** Creates a new instance of ArrayUtil */
    protected ArrayTool() {
    }
    
    
    /** 
     *  Given a multi-dimensional array, return the double value at 
     *  the specified index.
     */
    public static double getDouble(Object array, int[] indices) {
        Object baseArray = getBaseArray(array, indices, 0);
        int index = indices[indices.length-1];

        return Array.getDouble(baseArray, index);
    }
    
    
    /** 
     *  Given a multi-dimensional array, set the double value at 
     *  the specified index.
     */
    public static void setDouble(Object array, int[] indices, double value) {
        Object baseArray = getBaseArray(array, indices, 0);
        int index = indices[indices.length-1];

        Array.setDouble(baseArray, index, value);
    }
    
    
    /** 
     *  Given a multi-dimensional array, return the Object at 
     *  the specified index.
     */
    public static Object getObject(Object array, int[] indices) {
        Object baseArray = getBaseArray(array, indices, 0);
        int index = indices[indices.length-1];

        return Array.get(baseArray, index);
    }
    
    
    /** 
     *  Given a multi-dimensional array, set the Object at 
     *  the specified index.
     */
    public static void setObject(Object array, int[] indices, Object value) {
        Object baseArray = getBaseArray(array, indices, 0);
        int index = indices[indices.length-1];

        Array.set(baseArray, index, value);
    }
    
    
    /**
     * Return the array at the base of the the specified array.
     */
    protected static Object getBaseArray(Object array, int[] indices, int indexPtr) {
        if ( indexPtr < indices.length - 1 ) {
            int index = indices[indexPtr];
            Object nextArray = Array.get(array, index);
            
            return getBaseArray(nextArray, indices, indexPtr+1);
        }
        else {
            return array;
        }
    }
    
    
    /**
     * Increment the indices in place.  The lower bounds of each index is 0. The upper bound of each index is given by the size of the associated 
     * dimension.  Indices are incremented in the same way that a multi-dimensional C array would be incremented.  Namely, the outer most 
     * index is the fast index.
     * @param indices       the indices are the set of indices to increment
     * @param dimensions    each element is the size of each dimension
     * @return <code>true</code> if the indices were incremented, or 
     *         <code>false</code> if the indices were not incremented thus 
     *         indicating that it has exhausted all possible indices.
     */
    public static boolean increment(int[] indices, final int[] dimensions) {
        for ( int indexPtr = indices.length - 1 ; indexPtr >= 0 ; indexPtr-- ) {
            if ( indices[indexPtr] < dimensions[indexPtr] - 1 ) {
                indices[indexPtr] += 1;
                return true;
            }
            else {
                indices[indexPtr] = 0;
            }
        }
        
        return false;
    }
    
    
    /** convenience method for getting a string description of an integer array */
    static public String asString( final byte[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
    
    
    /** convenience method for getting a string description of an integer array */
    static public String asString( final int[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
    
    
    /** convenience method for getting a string description of an integer array */
    static public String asString( final short[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
    
    
    /** convenience method for getting a string description of an integer array */
    static public String asString( final long[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
	
    
    /** convenience method for getting a string description of an integer array */
    static public String asString( final float[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
	
    
    /** convenience method for getting a string description of a double array */
    static public String asString( final double[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
    
    
    /** 
	 * convenience method for getting a string description of an Object array by joining each element with a comma character
	 * @param array The array of objects
	 * @return A string representation of the array
	 */
    static public String asString( final Object[] array ) {
        final StringJoiner joiner = new StringJoiner( ", " );
        joiner.append( array );
        return "{" + joiner.toString() + "}";
    }
    
    
    /** convenience method for getting a string description of a double two dimensional array */
    static public String asString( final double[][] array ) {
        final StringBuffer buffer = new StringBuffer();
        for ( int index = 0 ; index < array.length ; index++ ) {
            final StringJoiner joiner = new StringJoiner( ", " );
            joiner.append( array[index] );
            buffer.append( "{" );
            buffer.append( joiner.toString() );
            buffer.append( "}\n" );
        }
        return buffer.toString();
    }
}
