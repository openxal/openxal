/*
 * DataTransform.java
 *
 * Created on October 16, 2002, 11:32 AM
 */

package xal.tools.transforms;

/**
 * Provides a mapping from a primitive transform to a ValueTransform.
 * While ValueTransform provides the recognized means for transforming between 
 * raw and physical values it requires a transformation of ArrayValue which is 
 * a general wrapper for value types including primitives and arrays of primitives.
 * Often this is invconvenient to code.  It is often much more convenient to code 
 * with a particular primitive value type (e.g. double)  and hence implement 
 * a more specific transform (e.g. DoubleTransform).  DataTransform facilitates 
 * the conversion from the specific transform to the more general ValueTransform.
 * DataTransform is referenced only in its subclasses.
 *
 * @author  tap
 * @see DataTransformFactory
 */
public interface DataTransform {
    /** No Operation Transform suitable as a default transform */
    final static public DataTransform noOperationTransform = new DataTransform() {
        public ValueTransform valueTransform() {
            return ValueTransform.noOperationTransform;
        }
    };
    
    
    /** 
     * Construct and return a general ValueTransform from the DataTransform. 
     * @return  An equivalent value transform for converting ArrayValue stores.
     */
    public ValueTransform valueTransform();
}
