/**
 * ASignalAttrs.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 13, 2014
 */

package xal.smf.impl.profile;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation describing the acquisition channels for the signal 
 * attributes of a wire scanner profile.  The attributes are those
 * of the <code>WireScanner.SignalAttrs</code> data structure. 
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @author  Christopher K. Allen
 * @since   Oct 3, 2011
 * @version March 19, 2013
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ASignalAttrs {

    
    /*
     * ASignalAttrs Properties
     */
    
    /**  Channel handle for the maximum value of the signal over baseline */
    public String   hndAmpRb();

    /**  Channel handle for the value of the signal baseline, i.e., sensor output at zero input */
    public String   hndOffsetRb();

    /**  Channel handle for the area under the signal curve minus baseline */
    public String   hndAreaRb();

    /**  Channel handle for the axis location of the center of mass */
    public String   hndMeanRb();

    /**  Channel handle for the the statistical standard deviation */
    public String   hndStdevRb();
    
    
    /*
     * Inner Annotations
     */
    
    /**
     * Annotation for the PV channels providing the signal attributes
     * for the wire scanner profiles.  All three
     * planes are represented.  This is done by repeating the same
     * annotation interface three times, one for each plane.  This is
     * the only solution I can using Java annotation.
     *
     * @author Christopher K. Allen
     * @since   Oct 3, 2011
     */
    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE) 
    public @interface ASet {
        
        /** Signal attributes for the horizontal axis */
        public ASignalAttrs   attrHor();
        
        /** Signal attributes for the vertical axis */
        public ASignalAttrs   attrVer();
        
        /** Signal attributes for the diagonal axis */
        public ASignalAttrs   attrDia();
        
    }
}
