/**
 * AScada.java
 *
 * @author Christopher K. Allen
 * @since  Feb 8, 2011
 *
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
 * <p>
 * Annotation identifying classes representing general data 
 * structures used for data
 * structure devoted toward data acquisition (DAQ) 
 * or supervisory control and data acquisition (SCADA).
 * The fields of such classes correspond to XAL
 * <code>{@link Channel}</code>s.
 * </p>
 * <p>
 * Adorn a class with this exterior annotation (i.e., <code>AScada</code>) to 
 * positively indicate it a class containing <code>AScada</code> fields
 * and/or records.  Many SCADA operations and classes require this.
 * </p>  
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Feb 8, 2011
 * 
 * @see ScadaRecord
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AScada {


    /**
     * Annotation specifying a class field is part of
     * a SCADA data structure.  The annotation provides the
     * XAL channel handles for setting and read back of the 
     * field value, as well as type information and whether or
     * not the field value is controllable.  
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
         * @return  <code>true</code> if the corresponding PV can be set
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
    
    
    /**
     * Alternate method of annotating the fields of a SCADA 
     * structure.  The data structure itself is annotated, rather
     * than each individual field.  The annotations are thus
     * arrays of field properties, each array containing an 
     * entry for each field of the structure.
     *
     * @author Christopher K. Allen
     * @since   Sep 30, 2011
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Record {
        
        /**
         * The number of SCADA fields within the data structure.
         * Thus, also the size of each property array.
         *
         * @return  number of SCADA fields
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        int         cntFlds();
        
        /**
         * Array of all the SCADA field names within the data
         * structure.  These are Java class attributes which are to
         * be specified as SCADA fields.
         *
         * @return  array containing the names of the SCADA fields
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        String[]    arrNames();
        
        /**
         * Array of the class types of all the SCADA fields within this
         * data structure.  
         *
         * @return  array of class types for the SCADA fields
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        Class<?>[]  arrTypes();
        
        /**
         * Array of all the XAL <code>{@link Channel}</code> handles for the
         * SCADA fields in this data structure.  These are the read back channels.
         *
         * @return  array of read back channel handles
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        String[]    arrHndRb();
        
        /**
         * Array of all the XAL <code>{@link Channel}</code> handles for the
         * SCADA fields in this data structure.  These are the set value channels.
         *
         * @return  array of set value channel handles 
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        String[]    arrHndCtl();
        
        /**
         * Array of controllable flags for each SCADA field in the data structure.
         * If the field is flagged as <code>true</code> then a non-null set-value 
         * channel handle must be present.
         *
         * @return  array of controllable PV flags for each SCADA field
         *
         * @author Christopher K. Allen
         * @since  Sep 30, 2011
         */
        boolean[]   arrCtl();
    }
    

//    /**
//     * Annotation for Java classes representing SCADA
//     * data records.  These classes are derived from the SCADA class
//     * <code>{@link ScadaRecord}</code> which manages their fields.
//     * The annotated class must also declare a field of type 
//     * <code>{@link ScadaFieldMap}</code> which contains an atlas of
//     * all (field name, field PV descriptor) pairs.
//     *
//     * @author Christopher K. Allen
//     * @since   Mar 9, 2011
//     */
//    @Documented
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.TYPE)
//    public @interface RecordX {
//        
//        /**
//         * Name of the <code>{@link ScadaFieldMap}</code> object
//         * for the data structure.
//         *
//         * @return  (attribute) name of the field map 
//         *
//         * @author Christopher K. Allen
//         * @since  Mar 9, 2011
//         */
//        String          value() default "";
//        
//        /**
//         * The name of any map of (field name, PV descriptor) pairs that the
//         * class is using.
//         *
//         * @return  name of a Java <code>Map</code> derived class
//         *
//         * @author Christopher K. Allen
//         * @since  Jan 28, 2013
//         */
//        String          fldMap() default "";
//    }
//    
//    
//    /**
//     * Identifies the annotated class as a one containing multiple SCADA 
//     * records, that is, a class with multiple fields annotated with
//     * <code>{@AScada.Record}</code>.  The data here provides the 
//     * <code>{@link ScadaRecord}</code> class hierarchy the information necessary
//     * to recursively parse the target class as a data structure and manage
//     * the underlying SCADA fields.
//     *
//     * @author Christopher K. Allen
//     * @since  Feb 1, 2013
//     *
//     */
//    @Documented
//    @Retention(RetentionPolicy.RUNTIME)
//    @Target(ElementType.TYPE)
//    public @interface DataStruct {
//    	
//    	/**
//    	 * The number of <code>{@link AScada.Record}</code> annotated fields in this
//    	 * class.
//    	 * 
//    	 * @return		number of SCADA record attributes of the class
//    	 *
//    	 * @author Christopher K. Allen
//    	 * @since  Feb 1, 2013
//    	 */
//    	int			recCnt();
//    	
//    	/**
//    	 * The names of the <code>{@AScada.Record}</code> annotated fields in this
//    	 * class.
//    	 * 
//    	 * @return		array containing the SCADA record names in this class
//    	 *
//    	 * @author Christopher K. Allen
//    	 * @since  Feb 1, 2013
//    	 */
//    	String[]	arrRecNames();
//    }
//    
}
