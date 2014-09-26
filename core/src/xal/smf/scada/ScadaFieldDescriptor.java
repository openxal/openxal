/**
 * 
 */
package xal.smf.scada;

import java.lang.reflect.Field;
import java.util.List;


/**
 * <p>
 * Maintains the (field name, field PV) pairs for the
 * data sets formed of device PVs.  For example,
 * such data sets are supported by {@link ScadaRecord}.
 * </p>
 * <p>
 * <code>ScadaFieldDescriptor</code> objects are immutable.
 * </p>  
 *
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 18, 2009
 * @author Christopher K. Allen
 */
public class ScadaFieldDescriptor extends XalPvDescriptor {
    
    
    
    /*
     * Global Methods
     */
    
    /**
     * <p>
     * This method returns the field descriptor objects for each field
     * in the SCADA data structure, only here it is returned as an array.
     * This is a convenience method which simply calls 
     * <code>{@link ScadaFieldDescriptor#makeFieldDescriptorList(Class)}</code> then converts
     * the result into an array.
     * </p>
     *
     * @param clsScada  class type of the SCADA data structure
     * 
     * @return      an array of descriptor objects, one for each data structure field
     *
     * @author Christopher K. Allen
     * @since  Mar 1, 2011
     */
    public static ScadaFieldDescriptor[]  makeFieldDescriptorArray(Class<?> clsScada) {
        List<ScadaFieldDescriptor>   lstFlds = makeFieldDescriptorList(clsScada);

        ScadaFieldDescriptor[]   arrFds = new ScadaFieldDescriptor[lstFlds.size()];

        return lstFlds.toArray(arrFds);
    }


    /**
     * <p>
     * This method returns meta-data of the <code>{@link AScada.Field}</code> annotation
     * used to identify fields in data structures as Supervisory Control And Data
     * Acquisition (SCADA) fields.  The meta-data is taken from each field annotation and
     * is used to populate a <code>ScadaFieldDescriptor</code> object.  The sum of all
     * annotation data for each field is returned as a list of field descriptors.
     * </p>
     * <p>
     * This whole mechanism of <code>ScadaFieldDescriptor</code> usage is (hopefully) going
     * to be eradicated.  It is too clumsy and the Java Annotation mechanism seems
     * more appropriate.
     * </p>
     *
     * @param clsScada  class type which is meant to be a SCADA data structure 
     *                  It must be annotated with <code>AScada</code>
     * 
     * @return  list of field descriptors describing all the field in the data structure 
     *          used for SCADA.
     *
     * @author Christopher K. Allen
     * @since  Feb 16, 2011
     */
    public static List<ScadaFieldDescriptor> makeFieldDescriptorList(Class<?> clsScada) {

        return new ScadaFieldList(clsScada);
        
    }

    /**
     * <p>
     * This method returns data of the <code>{@link AScada.Field}</code> annotation
     * meta data.  The meta data is used to identify fields in data structures as 
     * Supervisory Control And Data Acquisition (SCADA) fields.  
     * The meta-data is taken the annotation around the field and
     * entered into a new a <code>ScadaFieldDescriptor</code> object.  
     * </p>
     *
     * @param strFldName    field name within the SCADA data structure
     * @param clsScada      class type which is meant to be a SCADA data structure 
     *                      It must be annotated with <code>AScada</code>
     * 
     * @return  field descriptor describing the field meta-data in the data structure 
     *          or <code>null</code> if the field is not a SCADA type
     *          
     *          
        //     * @throws SecurityException    the given field is not public
        //     * @throws NoSuchFieldException there is no field of the given name
     *
     * @author Christopher K. Allen
     * @since  Mar 1, 2011
     */
    public static ScadaFieldDescriptor   makeFieldDescriptor(
            String strFldName, 
            Class<?> clsScada) 
    {

        // Get the field of the given data structure
        //  then check if it has the necessary annotation
        Field   fldTgt;
        try {
            fldTgt = clsScada.getField(strFldName);

        } catch (SecurityException e) {
            return null;

        } catch (NoSuchFieldException e) {
            return null;

        }

        if ( !fldTgt.isAnnotationPresent(AScada.Field.class) )
            return null;

        // Get the field's annotation, recover the field meta-data
        //  and place it into a field descriptor
        AScada.Field annTgt = fldTgt.getAnnotation(AScada.Field.class);

        ScadaFieldDescriptor fd = makeFieldDescriptor(strFldName, annTgt);

        return fd;
    }

    /**
     * Creates a <code>ScadaStruct$ScadaFieldDescriptor</code> object
     * according to the specifications in the given arguments.
     *
     * @param   strName     SCADA field name
     * @param   annFld      SCADA field annotation
     * 
     * @return  SCADA field descriptor corresponding to the given arguments
     *
     * @author Christopher K. Allen
     * @since  Mar 3, 2011
     */
    public static ScadaFieldDescriptor   makeFieldDescriptor(String strName, AScada.Field annFld) {
        Class<?>    clsType   = annFld.type();
        boolean     bolCtrl   = annFld.ctrl();
        String      strHndRb  = annFld.hndRb();
        String      strHndSet = annFld.hndSet();

        ScadaFieldDescriptor     fd;
        if (bolCtrl)
            fd = new ScadaFieldDescriptor(strName, clsType, strHndRb, strHndSet);
        else
            fd = new ScadaFieldDescriptor(strName, clsType, strHndRb);

        return fd;
    }

    /**
     * Creates and returns an array of <code>ScadaFieldDescriptor</code> objects
     * each of which is described in the given annotation class 
     * <code>{@link AScada}</code>.
     *
     * @param clsRec    class type annotated with the <code>AScada.Record</code> annotation
     * 
     * @return          array of field descriptors described in the above annotation
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2011
     */
    public static ScadaFieldDescriptor[]    makeFieldDescriptors(Class<? extends AScada.Record> clsRec) {

        AScada.Record annFldArr = clsRec.getAnnotation(AScada.Record.class);
        
        int         cntFlds = annFldArr.cntFlds();
        String[]    arrNms  = annFldArr.arrNames();
        String[]    arrRbs  = annFldArr.arrHndRb();
        String[]    arrCtls = annFldArr.arrHndCtl();
        Class<?>[]  arrTyps = annFldArr.arrTypes();
        boolean[]   arrCtl  = annFldArr.arrCtl();
        
        ScadaFieldDescriptor[]      arrFldDscrs = new ScadaFieldDescriptor[cntFlds];
        
        for (int i = 0; i < cntFlds; i++) {

            if(arrCtl[i] != true)
                arrFldDscrs[i] = new ScadaFieldDescriptor(arrNms[i], arrTyps[i], arrRbs[i]);
            else
                arrFldDscrs[i]  = new ScadaFieldDescriptor(arrNms[i], arrTyps[i], arrRbs[i], arrCtls[i]);
        }
        
        return arrFldDscrs;
    }
    
    
    
    /*
     * Local Attributes
     */

    /** Name of the data field in the SCADA data structure */
    public final String           strFldNm;


    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>ScadaFieldDescriptor</code> object built from an
     * existing <code>{@link PvDescriptor}</code>.
     *
     * @param strFldNm      the name of the data field
     * @param pvdFld        channel access PV descriptor of data field 
     *
     * @since     Dec 18, 2009
     * @author    Christopher K. Allen
     */
    public ScadaFieldDescriptor(String strFldNm, XalPvDescriptor pvdFld) {
        super(pvdFld);
        this.strFldNm = strFldNm;
    }

    /**
     * Create a new <code>ScadaFieldDescriptor</code> object.
     *
     * @param strFldNm      the name of the data field
     * @param clsType       the class type of the data field
     * @param strHandleRb   the handle of the read back channel 
     *
     * @since     Jan 13, 2010
     * @author    Christopher K. Allen
     */
    public ScadaFieldDescriptor(String strFldNm, Class<?> clsType, String strHandleRb) {
        super(clsType, strHandleRb);
        this.strFldNm = strFldNm;
    }

    /**
     * Create a new <code>ScadaFieldDescriptor</code> object. This descriptor
     * represents a process variable that can be controlled and has a "set value"
     * channel.
     *
     * @param strFldNm      the name of the data field
     * @param clsType       the class type of the data field
     * @param strHndRb      the handle of the read back channel 
     * @param strHndSet     the handle of the set PV value channel
     *
     * @since     Jan 13, 2010
     * @author    Christopher K. Allen
     */
    public ScadaFieldDescriptor(String strFldNm, Class<?> clsType, String strHndRb, String strHndSet) {
        super(clsType, strHndRb, strHndSet);
        this.strFldNm = strFldNm;
    }

    
    /*
     * Interface IFieldDescriptor 
     */
    
    /**
     * Return the data field's name.
     * 
     * @return  name of the data field
     *
     * @since   Dec 18, 2009
     * @author  Christopher K. Allen
     */
    public String getFieldName() {
        return this.strFldNm;
    }

    
    /*
     * Object Overrides
     */
    
    /**
     * Write out the contents of this field
     * descriptor as a string.
     * 
     * @return  description of the field descriptor
     * 
     * @since Mar 3, 2011
     * @see java.lang.Object#toString()
     */
    @Override
    public String   toString() {
        String      strBuf = "(Field=" + this.strFldNm; //$NON-NLS-1$
        strBuf += ", type=" + this.getPvType(); //$NON-NLS-1$
        strBuf += ", ctrl=" + this.isControllable(); //$NON-NLS-1$
        strBuf += ", RbChn=" + this.getRbHandle(); //$NON-NLS-1$
        
        if (this.isControllable())
            strBuf += ", SetChn=" + this.getSetHandle(); //$NON-NLS-1$
        
        strBuf += ")"; //$NON-NLS-1$
        
        return strBuf;
    }


    /**
     * Returns a deep copy of this object.
     * 
     * @since Apr 19, 2012
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        ScadaFieldDescriptor sfdCopy = new ScadaFieldDescriptor(this.strFldNm, this);
        
        return sfdCopy;
    }
    
    
}