/*
 * DoubleArrayTransformAdaptor.java
 *
 * Created on March 13, 2003, 10:34 AM
 */

package xal.tools.transforms;

import xal.tools.ArrayValue;

/**
 * An abstact class that provides a convenient base for classes implementing DoubleArrayTransform.
 * The developer need only implement the non-inherited methods of the DoubleArrayTransform interface.
 * Even if you do not want to subclass this class, it provides a static helper method from 
 * implementing the valueTransform() method required by DataTransform.
 *
 * @author  tap
 * @see DataTransformFactory
 */
public abstract class DoubleArrayTransformAdaptor implements DoubleArrayTransform {
    /** DoubleTransform interface */
    abstract public double[] convertFromRaw(double[] rawValue);
    
    /** DoubleTransform interface */
    abstract public double[] convertToRaw(double[] physicalValue);
    
    /** Implement DataTransform interface */
    final public ValueTransform valueTransform() {
        return implementValueTransform(this);
    }
    
    
    /** 
     * Helper method for implementing the valueTransform() DataTransform method 
     * for a DoubleArrayTransform implementation.
     */
    static public ValueTransform implementValueTransform(final DoubleArrayTransform transform) {
        return new ValueTransform() {
            final public ArrayValue convertFromRaw(ArrayValue rawArrayValue) {
                double[] rawArray = rawArrayValue.doubleArray();
                double[] array = transform.convertFromRaw(rawArray);
                
                return ArrayValue.doubleStore(array);
            }
            
            final public ArrayValue convertToRaw(ArrayValue physicalArrayValue) {
                double[] array = physicalArrayValue.doubleArray();
                double[] rawArray = transform.convertToRaw(array);
                
                return ArrayValue.doubleStore(rawArray);
            }
        };
    }
}
