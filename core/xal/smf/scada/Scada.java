/**
 * Scada.java
 *
 * @author Christopher K. Allen
 * @since  Feb 8, 2011
 *
 */

/**
 * Scada.java
 *
 * @author  Christopher K. Allen
 * @since	Feb 8, 2011
 */
package xal.smf.scada;

import xal.ca.Channel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation identifying classes representing data 
 * structures used for data acquisition and control.
 * The fields of such classes correspond to XAL
 * <code>{@link Channel}</code>s.
 *
 * @author Christopher K. Allen
 * @since   Feb 8, 2011
 * 
 * @see ScadaPacket
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scada {


    /**
     * Annotation required by all classes which are data
     * acquisition and control data structures.
     *
     * @author Christopher K. Allen
     * @since   Feb 8, 2011
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Field {


        /**
         * The primitive data type of the field
         * 
         * @return  type as a Java class 
         *
         * @author Christopher K. Allen
         * @since  Feb 8, 2011
         */
        Class<?>    type();

        /**
         *  Specifies whether or not the field is
         *  controllable as an EPICS PV.  That is,
         *  is it a "read/write" channel.
         *  
         * @return  <code>true</code if the corresponding PV can be set
         *          <code>false</code> if the PV is read only
         *
         * @author Christopher K. Allen
         * @since  Feb 8, 2011
         */
        boolean     ctrl();

        /**
         * The XAL channel handle of the read back channel
         * for the field.
         *
         * @return  name of the read back channel 
         *
         * @author Christopher K. Allen
         * @since  Feb 8, 2011
         */
        String      hndRb();


        /**
         * Channel handle of the field's set-value channel,
         * if it exists.
         *
         * @return  name of set channel handle.
         *
         * @author Christopher K. Allen
         * @since  Feb 8, 2011
         */
        String      hndSet() default "";

    }
}
