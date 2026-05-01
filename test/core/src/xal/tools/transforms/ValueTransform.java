/*
 * ValueTransform.java
 *
 * Created on September 23, 2002, 1:37 PM
 */

package xal.tools.transforms;

import xal.tools.ArrayValue;

/**
 * Provides a common interface for defining conversions between raw and physical values.
 * A raw value might be a value that you get directly from a channel access call.  The 
 * end user may want a processed value (e.g. convert current to field).  ValueTransform 
 * provides a common interface in XAL for such processing.
 *
 * @author  tap
 */
public interface ValueTransform {
    final static public ValueTransform noOperationTransform = new ValueTransform() {
        public ArrayValue convertFromRaw(ArrayValue rawValue) {
            return rawValue;
        }

        public ArrayValue convertToRaw(ArrayValue physicalValue) {
            return physicalValue;
        }
    };
    
    
    /** Convert an ArrayValue instance from a raw value to a processed value */
    public ArrayValue convertFromRaw(ArrayValue rawValue);
    
    /** Convert an ArrayValue instance from a processed value to a raw value */
    public ArrayValue convertToRaw(ArrayValue physicalValue);
}
