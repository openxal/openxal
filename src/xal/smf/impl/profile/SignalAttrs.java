/**
 * SignalAttrs.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 17, 2014
 */
package xal.smf.impl.profile;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;
import xal.smf.scada.AScada;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaFieldDescriptor;
import xal.smf.scada.ScadaRecord;
import xal.tools.data.DataAdaptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * Quantitative properties of a signal.
 *
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Feb 19, 2010
 * @author Christopher K. Allen
 */
@AScada
public class SignalAttrs extends ScadaRecord {


    /**
     * Enumeration of the signal properties data fields.
     *
     * @since  Feb 23, 2010
     * @author Christopher K. Allen
     */
    public enum ATTRS {

        /**  Signal amplitude */
        AMP("amp", "hndAmpRb"), //$NON-NLS-1$

        /**  Additive offset of the signal from zero baseline */
        OFFSET("offset", "hndOffsetRb"), //$NON-NLS-1$

        /**  Area under the signal curve; the integral; the total mass */
        AREA("area", "hndAreaRb"), //$NON-NLS-1$

        /**  The statistical average; the center of mass */
        MEAN("mean", "hndMeanRb"), //$NON-NLS-1$

        /**  The standard deviation */
        STDEV("stdev", "hndStdevRb"); //$NON-NLS-1$


        /*
         * Global Constants
         */

        //            /** The attribute tag identifying the wire from which the signal was taken */
        //            public static final String      STR_TAG_WIRE_ID = "angle"; //$NON-NLS-1$


        /*
         * Global Operations
         */

        /**
         * <p>
         * Creates a new SCADA (PV) field descriptor for the signal attribute represented by
         * this enumeration constant.  The data
         * to create the field descriptor is taken from the provided annotation.  
         * </p>
         * <h3>NOTE:</h3>
         * <p>
         * &middot; If the annotation field corresponding to this enumeration is left blank or
         * set to the empty string <tt>""</tt>, the method will throw a <code>NoSuchFieldException</code>.
         * Since it is not necessary for all handles to be specified this is not necessarily a failure
         * condition.
         * </p>
         * 
         * @param annSig    the annotation from which we are extracting the parameters
         * 
         * @return      A new SCADA field descriptor for the given signal attribute, or 
         *              <code>null</code> if field is missing
         * 
         * @author Christopher K. Allen
         * @since  Feb 6, 2013
         */
        public ScadaFieldDescriptor createDescriptor(ASignalAttrs annSig) 
        {
            try {
                String      strFldNm  = this.strPropNm;
                String      strFldHnd = (String) this.mthAnn.invoke(annSig);

                ScadaFieldDescriptor    sfdFld = new ScadaFieldDescriptor(strFldNm, double.class, strFldHnd);

                return sfdFld;

            } catch (IllegalArgumentException e) {
                System.err.println("Unspecified channel handle for signal attribute " + this.strPropNm);
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("Unspecified channel handle for signal attribute " + this.strPropNm);
                e.printStackTrace();
                
            } catch (InvocationTargetException e) {
                System.err.println("Unable to extract property value " + this.mthAnn.getName() + " from " + annSig.getClass());
                e.printStackTrace();
                
            }

            //                if ( strFldHnd.equals("") ) 
            //                    throw new NoSuchFieldException("Unspecified channel handle for signal attribute " + strFldNm);
            //                
            return null;
        }


        /**
         * Returns the name of the field in the data structure
         * which corresponds to this enumeration constant.
         *
         * @return  data structure field name
         * 
         * @since  Nov 13, 2009
         * @author Christopher K. Allen
         */
        public String       getFieldName() {
            return this.strPropNm;
        }

        /**
         * Using reflection, we return the value of the field that this
         * enumeration constant represents, within the given data structure.
         *
         * @param data      data structure having field corresponding to this constant
         * 
         * @return          value of the given data structure's field 
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public double       getFieldValue(SignalAttrs data) {

            Class<? extends ScadaRecord> clsData    = data.getClass();
            try {
                Field       fldDataFld = clsData.getField( getFieldName() );
                double      dblFldVal  = fldDataFld.getDouble(data);

                return dblFldVal;

            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (NoSuchFieldException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#getFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            }

            return 0.0;
        }

        /**
         * Using reflection, we set the field value of the given <code>SignalAttrs</code>
         * object that this enumeration constant represents.  The value of that field
         * is the <code>double</code> valued field provided in the arguments.
         *
         * @param attrs     data structure to receive new field value
         * @param dblVal    new value of that field
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public double       setFieldValue(SignalAttrs attrs, double dblVal) {

            Class<? extends ScadaRecord> clsAttrs    = attrs.getClass();
            try {
                Field       fldAttrFld = clsAttrs.getField( getFieldName() );
                fldAttrFld.setDouble(attrs,  dblVal);;
                

            } catch (SecurityException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#setFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (NoSuchFieldException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#setFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalArgumentException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#setFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            } catch (IllegalAccessException e) {
                System.err.println("SERIOUS ERROR: WireScanner$SignalAttrs#setFieldValue()"); //$NON-NLS-1$
                e.printStackTrace();

            }

            return 0.0;
        }


        /** name of the field in the data structure */
        private String          strPropNm;

        /** name of the annotation field containing the channel handle for this enumeration */
        private Method          mthAnn;


        /**
         * Creates a new instance of ATTRS and initializes the field names of
         * <code>{@link SignalAttrs}</code> and <code>{@link ASignalAttrs}</code> that 
         * the constant represents.
         *
         * @param strPropNm    name of the field in the <code>{@link SignalAttrs}</code> 
         *                          structure this constant represents
         * @param strAnnFldNm    field name of annotation <code>{@link ASignalAttrs}</code> which identifies
         *                          the data type of the channel value
         * @param strChanHnd     field name of annotation <code>{@link ASignalAttrs}</code>containing the 
         *                          metal-data to be assigned to the <code>Signal</code> field. 
         *
         * @author Christopher K. Allen
         * @since  Feb 4, 2013
         */
        private ATTRS(final String strPropNm, String strAnnFldNm)  {

            // Create an error message in case this operation fails
            String  strErrMsg = "Instantiation error while extracting meta data from annotation " + ASignalAttrs.class;

            // Initialize the attributes of the enumeration
            try {
                this.strPropNm = strPropNm;
                this.mthAnn    = ASignalAttrs.class.getMethod(strAnnFldNm);

            } catch (NoSuchMethodException e) {
                System.err.println(strErrMsg);
                e.printStackTrace();

            } catch (SecurityException e) {

                System.err.println(strErrMsg);
                e.printStackTrace();
            }
        }
    }


    /*
     * Global Operations
     */

    /**
     * Creates a new instance of the <code>Signal</code> class which is connected
     * to a given device whose signal fields are described in the given annotation.
     * When created here, the <code>Signal</code> object becomes a fully functional
     * <code>{@link ScadaRecord}</code> and can automatically acquire data from the
     * connected device.
     *  
     * @param annAttrs    annotation class containing the meta-data describing the signal connections
     * 
     * @return          fully operational <code>ScadaRecord</code> class capable of data acquisition
     * 
     * @throws IllegalAccessException   a needed field in the annotation is not publicly accessible   
     *
     * @author Christopher K. Allen
     * @since  Feb 7, 2013
     */
    static public SignalAttrs    createConnectedSignal(ASignalAttrs annAttrs)  
    {
        List<ScadaFieldDescriptor>  lstDscr = new LinkedList<ScadaFieldDescriptor>();

        for ( SignalAttrs.ATTRS enmFld : ATTRS.values() ) {

            ScadaFieldDescriptor    sfdSigFld = enmFld.createDescriptor(annAttrs);

            lstDscr.add(sfdSigFld);
        }

        SignalAttrs  sigInit = new SignalAttrs(lstDscr);

        return sigInit;
    }


    /*
     * Data Fields
     */

    /**  Maximum value of the signal over baseline */
    public double   amp;

    /**  Value of the signal baseline, i.e., sensor output at zero input */
    public double   offset;

    /**  Area under the signal curve minus baseline */
    public double   area;

    /**  Axis location of the center of mass */
    public double   mean;

    /**  The statistical standard deviation */
    public double   stdev;


    /*
     * Initialization
     */

    /**
     * Creates a new, uninitialized instance of <code>SignalAttrs</code> which
     * is not connected to any hardware.
     *
     *
     * @author Christopher K. Allen
     * @since  Mar 21, 2013
     */
    public SignalAttrs() {
    }

    /**
     * Create a new <code>SignalAttrs</code> object.
     *
     * @param lstPfdSet     list of PV descriptors for each data field
     *
     * @since     Feb 23, 2010
     * @version   March 21, 2013
     * @author    Christopher K. Allen
     */
    protected SignalAttrs(List<ScadaFieldDescriptor> lstPfdSet) {
        super(lstPfdSet);
    }

    /**
     * Create a new, initialized <code>SignalAttrs</code> object.  Data 
     * field values are taken immediately from the diagnostic devices.
     *
     * @param lstPfdSet     field descriptors for this data set
     * @param ws            hardware device to acquire data
     * 
     * @throws IllegalArgumentException     general field incompatibility exception
     * @throws ConnectionException          unable to connect to a parameter read back channel
     * @throws GetException                 general CA GET exception while fetch field value
     *
     *
     * @since     Feb 25, 2010
     * @author    Christopher K. Allen
     */
    protected SignalAttrs(List<ScadaFieldDescriptor> lstPfdSet, AcceleratorNode ws) 
            throws ConnectionException, GetException
    {
        super(lstPfdSet);
        super.loadHardwareValues(ws);
    }
    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Performs an averaging operation with the given <code>SignalAttrs</code> object using the
     * given weighting factor.  The quantities within this data structure are averaged in
     * place with that of the given signal attributes structure.  Letting &lambda; denote the 
     * provided averaging factor, which is in the interval [0,1], the new values of this
     * structure, say <i>v'</i> are given by the formula
     * <br>
     * <br>
     * &nbsp; &nbsp; <i>v'</i> = &lambda;<i>u</i> + (1 - &lambda;)<i>v</i>
     * <br>
     * <br>
     * where <i>v</i> is the previous value of <i>v'</i> and <i>u</i> is the new value
     * of <i>v</i> in <code>sigAcq</code>. 
     * </p>  
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The standard deviation is weighted vectorally.
     * </p>
     * 
     * @param sigAcq       signal <i>u</i> to average into this one <i>v</i>
     * @param dblWtFac     weighting factor &lambda; &in; [0,1] for argument <i>u</i> 
     * 
     * @throws IllegalArgumentException the provided signal is not the same size as this signal
     *
     * @author Christopher K. Allen
     * @since  May 1, 2014
     */
    public void average(SignalAttrs sigAcq, double dblWtFac) throws IllegalArgumentException {
        
        
        for ( ATTRS attr : ATTRS.values() ) {
            if (attr == ATTRS.STDEV)
                continue;
            
            double      dblAddend = attr.getFieldValue(sigAcq);
            double      dblAccum  = attr.getFieldValue(this);
            double      dblAvgVal = dblAddend*dblWtFac + dblAccum*(1.0 - dblWtFac);
            
            attr.setFieldValue(this, dblAvgVal);
        }
        
        double      dblAddend = sigAcq.stdev;
        double      dblAccum  = this.stdev;
        double      dblAvgVal = dblAddend*dblAddend*dblWtFac + dblAccum*dblAccum*(1.0 - dblWtFac);
        
        this.stdev = Math.sqrt(dblAvgVal);
    }


    /*
     * DataListener Interface
     */

    /**
     * Label used for parameter set identification. 
     *
     * @since       Mar 4, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
        return this.getClass().getCanonicalName();
    }

    /**
     * Loads the data structure from the given data source while respecting
     * the various data format versions.
     *
     * @see xal.smf.scada.ScadaRecord#update(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2014
     */
    @Override
    public void update(DataAdaptor daptSrc) throws MissingResourceException, BadStructException {
        // New format - Get the data adaptor node corresponding to this signal from the 
        //  provided parent node
        String            strLabel    = this.dataLabel();
        DataAdaptor       daptSgnl = daptSrc.childAdaptor(strLabel);
        
        if (daptSgnl == null) { // this is XAL version
            strLabel  = "gov.sns." + strLabel;
            daptSgnl  = daptSrc.childAdaptor(strLabel);
        }
        
        if (daptSgnl == null) // we were given the data node itself
            daptSgnl = daptSrc;

        super.update(daptSgnl);
    }

    /**
     *
     * @see xal.smf.scada.ScadaRecord#write(xal.tools.data.DataAdaptor)
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2014
     */
    @Override
    public void write(DataAdaptor daptSink) throws BadStructException {
        super.write(daptSink);
    }
    
  
}
