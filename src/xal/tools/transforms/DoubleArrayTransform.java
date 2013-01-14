/*
 * DoubleArrayTransform.java
 *
 * Created on October 16, 2002, 11:33 AM
 */

package xal.tools.transforms;

/**
 * Implementation of DataTransform for the <code>double</code> precision array type.
 * DoubleArrayTransform provides a convenient way to implement general transformations 
 * between double precision floating point arrays.
 *
 * @author  tap
 * @see DataTransformFactory
 */
public interface DoubleArrayTransform extends DataTransform {
    /**
     * Convert a raw array to a physical array.
     * @param rawArray      The raw array to be converted to a physical array.
     * @return  A physical equivalent of the raw array.
     */
    public double[] convertFromRaw(double[] rawArray);
    
    /**
     * Convert a physical value to a raw array.
     * @param physicalArray     The physical array to be converted to a raw array.
     * @return  A raw equivalent of the physical array.
     */
    public double[] convertToRaw(double[] physicalArray);
}
