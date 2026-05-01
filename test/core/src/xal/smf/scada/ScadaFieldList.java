/**
 * ScadaFieldList.java
 *
 * @author Christopher K. Allen
 * @since  Mar 9, 2011
 *
 */

package xal.smf.scada;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a list of all <code>{@link ScadaFieldDescriptor}</code> objects
 * parsed from a SCADA data structure. The field descriptors are sorted by
 * the ordering encountered in the declaration of fields in the SCADA data
 * structure.
 * 
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Mar 9, 2011
 */
public class ScadaFieldList extends LinkedList<ScadaFieldDescriptor> implements List<ScadaFieldDescriptor> {

    /** Default serialization version */
    private static final long serialVersionUID = 1L;
    

    /*
     * Initialization
     */
    
    /**
     * Create a new list containing the field descriptors
     * parsed from the given SCADA class.
     * 
     * @param clsScada  class annotated with <code>{@link AScada.Field}</code> information
     *
     * @author  Christopher K. Allen
     * @since   Mar 9, 2011
     */
    public ScadaFieldList(Class<?> clsScada ) {
        super();
        
        Field[]       arrFlds = clsScada.getFields();
        for (Field fld : arrFlds) {
            
            //Process only data structure fields which are marked as process variables
            if (! fld.isAnnotationPresent(AScada.Field.class) )
                continue;

            String        strName = fld.getName();
            AScada.Field  annFld  = fld.getAnnotation(AScada.Field.class);

            ScadaFieldDescriptor fd = ScadaFieldDescriptor.makeFieldDescriptor(strName, annFld);
            
            
            this.add(fd);
        }
    }
    
}
