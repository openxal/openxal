/*
 * ValueTransformFactory.java
 *
 * Created on September 23, 2002, 1:46 PM
 */

package xal.tools.transforms;

import xal.tools.ArrayValue;

/**
 * Factory for instantiating DataTransforms implementing common transformations
 * such as linear, scale and offset transformations.
 *
 * @author  tap
 */
public class DataTransformFactory {    
    /** Creates a new instance of ValueTransformFactory */
    protected DataTransformFactory() {
    }    
    
    
    /** Transform that does nothing.  Suitable as a default transform. */
    static public DataTransform noOperationTransform() {
        return DataTransform.noOperationTransform;
    }
    
        
    // ------ DoubleTransforms ----------------------------------------------------
    
    /** Convert double values with a simple scale conversion */
    static public DoubleTransform doubleScaleTransform(final double scale) {
        return new DoubleTransformAdaptor() {
            public double convertFromRaw(double rawValue) {
                return scale * rawValue;
            }
            
            public double convertToRaw(double physicalValue) {
                return physicalValue / scale;
            }
        };
    }
    
    
    /** Convert double values with a simple translation conversion */
    static public DoubleTransform doubleTranslationTransform(final double offset) {
        return new DoubleTransformAdaptor() {
            public double convertFromRaw(double rawValue) {
                return rawValue + offset;
            }
            
            public double convertToRaw(double physicalValue) {
                return physicalValue - offset;
            }
        };
    }
    
    
    /** Convert double values with a simple linear conversion */
    static public DoubleTransform doubleLinearTransform(final double scale, final double offset) {
        return new DoubleTransformAdaptor() {
            public double convertFromRaw(double rawValue) {
                return scale * rawValue + offset;
            }
            
            public double convertToRaw(double physicalValue) {
                return (physicalValue - offset) / scale;
            }
        };
    }
    
    // ------ End DoubleTransforms -----------------------------------------------------
    
    
    // ------ DoubleArrayTransforms ----------------------------------------------------
    
    /** Convert a double precision array with a simple scale conversion */
    static public DoubleArrayTransform doubleArrayScaleTransform(final double scale) {
        return new DoubleArrayTransformAdaptor() {
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = scale * rawArray[index];
                }
                
                return physicalArray;
            }
            
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = physicalArray[index] / scale;
                }
                
                return rawArray;
            }
        };
    }    

    
    /** Convert a double precision array with a simple translation conversion */
    static public DoubleArrayTransform doubleArrayTranslationTransform(final double offset) {
        return new DoubleArrayTransformAdaptor() {
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = offset + rawArray[index];
                }
                
                return physicalArray;
            }
            
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = physicalArray[index] - offset;
                }
                
                return rawArray;
            }
        };
    }    
    

    /** Convert a double precision array with a simple linear conversion */
    static public DoubleArrayTransform doubleArrayLinearTransform(final double scale, final double offset) {
        return new DoubleArrayTransformAdaptor() {
            public double[] convertFromRaw(double[] rawArray) {
                double[] physicalArray = new double[rawArray.length];
                
                for( int index = 0 ; index < rawArray.length ; index++ ) {
                    physicalArray[index] = offset + scale * rawArray[index];
                }
                
                return physicalArray;
            }
            
            public double[] convertToRaw(double[] physicalArray) {
                double[] rawArray = new double[physicalArray.length];
                
                for( int index = 0 ; index < physicalArray.length ; index++ ) {
                    rawArray[index] = (physicalArray[index] - offset) / scale;
                }
                
                return rawArray;
            }
        };
    }    
    

    // ------ End DoubleArrayTransforms ------------------------------------------------
}
