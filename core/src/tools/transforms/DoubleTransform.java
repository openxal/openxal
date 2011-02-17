/*
 * DoubleTransform.java
 *
 * Created on October 16, 2002, 11:33 AM
 */

package xal.tools.transforms;

/**
 * Implementation of DataTransform for the <code>double</code> primitive type.
 * DoubleTransform provides a convenient way to implement general transformations 
 * between double precision floating point values.
 *
 * @author  tap
 * @see DataTransformFactory
 */
public interface DoubleTransform extends DataTransform {
    /**
     * Convert a raw value to a physical value.
     * @param rawValue      The raw value to be converted to a physical value.
     * @return  A physical equivalent of the raw value.
     */
    public double convertFromRaw(double rawValue);
    
    /**
     * Convert a physical value to a raw value.
     * @param physicalValue     The physical value to be converted to a raw value.
     * @return  A raw value equivalent of the physical value.
     */
    public double convertToRaw(double physicalValue);
}
