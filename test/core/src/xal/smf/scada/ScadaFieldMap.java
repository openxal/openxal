/**
 * ScadaFieldMap.java
 *
 * @author Christopher K. Allen
 * @since  Mar 9, 2011
 *
 */

package xal.smf.scada;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * This class parses the meta-data of the <code>{@link AScada.Field}</code> annotation
 * used to identify fields in data structures as Supervisory Control And Data
 * Acquisition (SCADA) fields.  The meta-data is taken from each field annotation and
 * is used to populate a <code>ScadaFieldDescriptor</code> object.  The sum of all
 * annotation data for each field is returned as a map of the data structure field
 * name to the actual field descriptor.
 * </p>
 * <h3>NOTE:</h3>
 * <p>
 * This whole mechanism of <code>ScadaFieldDescriptor</code> usage is (hopefully) going
 * to be eradicated.  It is too clumsy and the Java Annotation mechanism seems
 * more appropriate.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @author Christopher K. Allen
 * @since  Mar 9, 2011
 *
 */
public class ScadaFieldMap extends HashMap<String, ScadaFieldDescriptor> {

    
    /*
     * Global Attribute
     */

    
    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new SCADA field map object for the given SCADA data structure.
     * The map is keyed by SCADA field name.
     * 
     * @param clsScada  class representing a SCADA data structure
     *
     * @author  Christopher K. Allen
     * @since   Mar 9, 2011
     */
    public ScadaFieldMap(Class<?> clsScada) {
        super();
        
        Field[]       arrFlds = clsScada.getFields();
        for (Field fld : arrFlds) {
            
            //Process only data structure fields which are marked as process variables
            if (! fld.isAnnotationPresent(AScada.Field.class) )
                continue;

            String       strName = fld.getName();
            AScada.Field  annFld  = fld.getAnnotation(AScada.Field.class);

            ScadaFieldDescriptor fd = ScadaFieldDescriptor.makeFieldDescriptor(strName, annFld);
            
            
            this.put(strName, fd);
        }
    }
    
    /**
     * <p>
     * Create a new SCADA field map object which contains the fields
     * in the given list.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * The class <code>ScadaFieldList</code> is derived from the type
     * <code>List<ScadaFieldDescriptor></code>.  Thus, objects of that type
     * maybe be used as arguments.
     * </p>  
     * 
     * @param lstFlds   list containing field descriptors to be mapped (keyed by field name)
     *
     * @author  Christopher K. Allen
     * @since   Oct 14, 2011
     * 
     * @see ScadaFieldList
     */
    public ScadaFieldMap(List<ScadaFieldDescriptor> lstFlds) {
        super();
        
        for (ScadaFieldDescriptor fd : lstFlds) {
            String  strFldNm = fd.getFieldName();
            
            this.put(strFldNm, fd);
        }
    }
    
}
