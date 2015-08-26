/**
 * SignalAttrSet.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 17, 2014
 */
package xal.smf.impl.profile;

import java.util.LinkedList;
import java.util.List;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.AcceleratorNode;
import xal.smf.scada.ScadaFieldDescriptor;


/**
 * Data structure containing the signal properties of the
 * the profile data sets acquired from a wire profile device.
 *
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since  Feb 23, 2010
 * @author Christopher K. Allen
 */
public abstract class SignalAttrSet implements DataListener {


    /*
     * Global Methods
     */

    //        /**
    //         * Creates a new <code>SignalAttrs</code> object according to the information
    //         * annotated in the given class type.  The new object is created for the specified
    //         * profile plane (type <code>{@link WireScanner2.ANGLE}</code>) and the information
    //         * for that particular plane is taken from the set of annotations.  The annotations
    //         * contain the channel handles needed to create a <code>SignalAttrs</code> object. 
    //         * 
    //         * @param enmAng        profile plane
    //         * @param clsAttrSet    class type of the signal attribute set containing proper annotations
    //         * 
    //         * @return  new <code>SignalAttrs</code> object build from the information 
    //         *          annotated in the given class
    //         *
    //         * @throws ScadaAnnotationException the <code>ADaqWire.SgnlAttrs</code> annotations are incomplete
    //         *
    //         * @author Christopher K. Allen
    //         * @since  Oct 4, 2011
    //         */
    //        public static SignalAttrs createSignalAttrs(ANGLE enmAng, Class<? extends SignalAttrSet> clsAttrSet) 
    //            throws ScadaAnnotationException
    //        {
    //         
    //            if (  ! clsAttrSet.isAnnotationPresent(ADaqWire.SgnlAttrs.Hor.class) 
    //                    || ! clsAttrSet.isAnnotationPresent(ADaqWire.SgnlAttrs.Ver.class) 
    //                    || ! clsAttrSet.isAnnotationPresent(ADaqWire.SgnlAttrs.Dia.class)
    //                 )
    //                     throw new ScadaAnnotationException("The 'ADaqWire.SgnlAttrs' annotations are incomplete.");
    //
    //            switch (enmAng) {
    //
    //            case HOR: {
    //                ADaqWire.SgnlAttrs.Hor annHor = clsAttrSet.getAnnotation(ADaqWire.SgnlAttrs.Hor.class);
    //
    //                String  hndAmp  = annHor.hndAmpRb();
    //                String  hndBase = annHor.hndOffsetRb();
    //                String  hndArea = annHor.hndAreaRb();
    //                String  hndMean = annHor.hndMeanRb();
    //                String  hndStd  = annHor.hndStdevRb();
    //
    //                return createSignalAttrs(enmAng, hndAmp, hndBase, hndArea, hndMean, hndStd);
    //                }
    //
    //            case VER: {
    //                ADaqWire.SgnlAttrs.Ver annVer = clsAttrSet.getAnnotation(ADaqWire.SgnlAttrs.Ver.class);
    //
    //                String  hndAmp  = annVer.hndAmpRb();
    //                String  hndBase = annVer.hndOffsetRb();
    //                String  hndArea = annVer.hndAreaRb();
    //                String  hndMean = annVer.hndMeanRb();
    //                String  hndStd  = annVer.hndStdevRb();
    //
    //                return createSignalAttrs(enmAng, hndAmp, hndBase, hndArea, hndMean, hndStd);
    //                }
    //
    //            case DIA: {
    //                ADaqWire.SgnlAttrs.Dia annDia = clsAttrSet.getAnnotation(ADaqWire.SgnlAttrs.Dia.class);
    //
    //                String  hndAmp  = annDia.hndAmpRb();
    //                String  hndBase = annDia.hndOffsetRb();
    //                String  hndArea = annDia.hndAreaRb();
    //                String  hndMean = annDia.hndMeanRb();
    //                String  hndStd  = annDia.hndStdevRb();
    //
    //                return createSignalAttrs(enmAng, hndAmp, hndBase, hndArea, hndMean, hndStd);
    //                }
    //            
    //            default:
    //                return null;
    //            }
    //            
    //        }
    //        
    //        
    //        /**
    //         * Creates a new <code>SignalAttrs</code> object from the given channel handles and the
    //         * given profile plane.  It is assumed that all PV types are <code>double</code> and that
    //         * the PV is read only.
    //         *
    //         * @param enmAng    the profile plane of the signal attributes.
    //         * @param hndAmp    channel handle of the signal <em>amplitude</em> attribute
    //         * @param hndBase   channel handle of the signal <em>offset</em> or <em>baseline</em> attribute
    //         * @param hndArea   channel handle of the signal <em>area</em> or <em>integral</em> attribute
    //         * @param hndMean   channel handle of the signal <em>mean value</em> attribute
    //         * @param hndStd    channel handle of the signal <em>standard deviation</code> attribute
    //         * 
    //         * @return  new <code>SignalAttrs</code> object built from the above information
    //         *
    //         * @author Christopher K. Allen
    //         * @since  Oct 4, 2011
    //         */
    //        public static SignalAttrs   createSignalAttrs(ANGLE enmAng, String hndAmp, String hndBase, String hndArea, String hndMean, String hndStd) {
    //
    //            ScadaFieldDescriptor    sfdAmp  = new ScadaFieldDescriptor(SignalAttrs.ATTRS.AMP.getFieldName(), double.class, hndAmp);
    //            ScadaFieldDescriptor    sfdBase = new ScadaFieldDescriptor(SignalAttrs.ATTRS.OFFSET.getFieldName(), double.class, hndBase); 
    //            ScadaFieldDescriptor    sfdArea = new ScadaFieldDescriptor(SignalAttrs.ATTRS.AREA.getFieldName(), double.class, hndArea);
    //            ScadaFieldDescriptor    sfdMean = new ScadaFieldDescriptor(SignalAttrs.ATTRS.MEAN.getFieldName(), double.class, hndMean);
    //            ScadaFieldDescriptor    sfdStd  = new ScadaFieldDescriptor(SignalAttrs.ATTRS.STDEV.getFieldName(), double.class, hndStd);
    //            
    //            ScadaFieldDescriptor[]  arrSfd = { sfdAmp, sfdBase, sfdArea, sfdMean, sfdStd };
    //            
    //            SignalAttrs     saResult = new SignalAttrs(enmAng.getLabel(), arrSfd);
    //            
    //            return saResult;
    //        }

    /*
     * Instance Attributes
     */

    /** Horizontal wire signal properties */
    public SignalAttrs           hor;

    /** Horizontal wire signal properties */
    public SignalAttrs           ver;

    /** Horizontal wire signal properties */
    public SignalAttrs           dia;


    /*
     * Operations
     */

    //        /**
    //         * Returns the signal properties data structure
    //         * (i.e., <code>ProfileAttrs</code> object) corresponding
    //         * to the given projection angle.
    //         *
    //         * @param ang   projection angle
    //         * 
    //         * @return      profile signal properties for the given projection angle
    //         * 
    //         * @since  Apr 23, 2010
    //         * @author Christopher K. Allen
    //         */
    //        public SignalAttrs     getSignalAttrs(ANGLE ang) {
    //
    //            switch (ang) {
    //
    //            case HOR:
    //                return hor;
    //
    //            case VER:
    //                return ver;
    //
    //            case DIA:
    //                return dia;
    //            }
    //
    //            // This shouldn't happen
    //            return null;
    //        }
    //


    /*
     * Operations
     */
    
    /**
     * Returns the set of all SCADA field descriptors describing the
     * data acquisition channels.  Since this is an active data structure
     * these channels are used internally to populate the data fields,
     * which is profile data taken from the hardware.
     * <br>
     * <br>
     * This <b>will work</b>. If the child class has been annotated by 
     * <code>ASignal.ASet</code> the <code>Signal</code> attributes
     * will be connected in the zero-argument constructor.
     * 
     *
     * @return  set of all channel field descriptors used by this data structure
     *
     * @author Christopher K. Allen
     * @since  Mar 15, 2011
     */
    public List<ScadaFieldDescriptor> getFieldDescriptors() {
        List<ScadaFieldDescriptor>   lstFdHor = this.hor.getFieldDescriptors();
        List<ScadaFieldDescriptor>   lstFdVer = this.ver.getFieldDescriptors();
        List<ScadaFieldDescriptor>   lstFdDia = this.dia.getFieldDescriptors();
        
        List<ScadaFieldDescriptor>   lstFds = new LinkedList<ScadaFieldDescriptor>();

        lstFds.addAll(lstFdHor);
        lstFds.addAll(lstFdVer);
        lstFds.addAll(lstFdDia);
        
        return lstFds;
    }

    /*
     * Operations
     */
    
    /**
     * Sets the signal attribute for the given measurement angle to the given signal
     * attribute object.
     * 
     * @param angle         measurement angle of the modified signal attributes 
     * 
     * @param attrSignal    new signal attributes for the given measurement angle
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public void setSignalAttrs(ProfileDevice.ANGLE angle, SignalAttrs attrSignal) {
        
        switch (angle) {
        case HOR: this.hor = attrSignal; break;
        case VER: this.ver = attrSignal; break;
        case DIA: this.dia = attrSignal; break;
        default:  ;
        }
    }
    
    /**
     * Returns the signal attributes of this set corresponding to the give
     * profile angle.
     * 
     * @param angle     measurement angle of the desired signal properties
     * 
     * @return          signal properties corresponding to the given angle
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public SignalAttrs   getSignalAttrs(ProfileDevice.ANGLE angle) {
        
        switch (angle) {
        case HOR: return this.hor; 
        case VER: return this.ver; 
        case DIA: return this.dia; 
        default:  return null;
        }
    }

    /**
     * Averages all the signal attributes in the signal attributes set using the given weighting 
     * factor.
     * See <code>Signal{@link SignalAttrs#average(Signal, double)}</code> for additional information.
     * 
     * @param setAvg    signal set to average into this one
     * @param dblWtFac     averaging magnitude &lambda; &isin; [0,1]
     *
     * @throws IllegalArgumentException the provided signal is not the same size as this signal
     *
     * @author Christopher K. Allen
     * @since  May 1, 2014
     */
    public void average(SignalAttrSet setAvg, double dblWtFac) throws IllegalArgumentException {
        
        for ( ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values() ) {
            SignalAttrs     attrsAvg = setAvg.getSignalAttrs(angle);
            
            getSignalAttrs(angle).average(attrsAvg, dblWtFac);
        }
    }

    
    /*
     * DataListener Interface
     */

    /**
     * Label used for parameter set identification.
     * 
     *  @return     string label (identifier) for parameter set
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
     * Load the contents of this signal traits set
     * from the persistent store behind the 
     * <code>DataListener</code> interface.
     * 
     * @param daptSrc       data source
     *
     * @since       Mar 4, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     */
    @Override
    public void update(DataAdaptor daptSrc) {
        
        // Get the node containing this data from the given parent node
        String      strLabel = this.dataLabel();
        DataAdaptor daptSgnl = daptSrc.childAdaptor( strLabel );
        
        // Check the format is from the XAL version
        if (daptSgnl == null) {
            strLabel = "gov.sns." + strLabel;
            daptSgnl  = daptSrc.childAdaptor(strLabel);
        }

        // Were we given the current data node, not the parent node?
        if (daptSgnl == null) 
            daptSgnl = daptSrc;
        
        // Look for the middle version format - Open XAL before the format correction
        //  was made. This one is problematic, we must guess at the order.  
        String              strLblOld = SignalAttrs.class.getCanonicalName();

        List<DataAdaptor>   lstDaptOld = daptSgnl.childAdaptors(strLblOld);
        
        // If we are in the middle format, we load sequentially according to index and return.
        if (lstDaptOld.size() > 0)  {
            for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
                int         index = angle.getIndex();
                DataAdaptor dapt  = lstDaptOld.get(index);
                SignalAttrs attr  = this.getSignalAttrs(angle);

                attr.update(dapt);
            }

            return;
        }

        // Assume that we have the XAL format or the current format   
        // Read in each signal using the current data format
        for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
            DataAdaptor     dapt = daptSgnl.childAdaptor( angle.getLabel() );
            SignalAttrs     attr = this.getSignalAttrs(angle);

            attr.update(dapt);
        }

//        hor.update(daptSgnl);
//        ver.update(daptSgnl);
//        dia.update(daptSgnl);
    }

    /**
     * Write out the contents of this signal traits 
     * set to the given data store.
     * 
     * @param adaptor       data store exposing <code>DataListener</code> interface
     *
     * @since       Mar 4, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
     */
    @Override
    public void write(DataAdaptor adaptor) {
        DataAdaptor daptSgnls = adaptor.createChild( this.dataLabel() );

        for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
            DataAdaptor     dapt  = daptSgnls.createChild( angle.getLabel() );
            SignalAttrs     attr = this.getSignalAttrs(angle);

            attr.write(dapt);
        }
        
//        DataAdaptor daptSig = adaptor.createChild( this.dataLabel() );
//
//        hor.write(daptSig);
//        ver.write(daptSig);
//        dia.write(daptSig);
    }


    /* 
     * Initialization
     */

    /**
     * Create a new <code>ProfileAttrSet</code> object from the annotation
     * information in the class type.
     *
     * @since     Feb 25, 2010
     * @author    Christopher K. Allen
     */
    protected SignalAttrSet()  {
        super();

        // If there is not annotations for the channel bindings this is just an
        //    empty data structure
        if (! this.getClass().isAnnotationPresent(ASignalAttrs.ASet.class)) {
            this.hor = new SignalAttrs();
            this.ver = new SignalAttrs();
            this.dia = new SignalAttrs();

            return;
        }

        // Get the bindings annotation then create and connect the channels
        ASignalAttrs.ASet    annAttrSet = this.getClass().getAnnotation(ASignalAttrs.ASet.class);

        this.hor = SignalAttrs.createConnectedSignal(annAttrSet.attrHor());
        this.ver = SignalAttrs.createConnectedSignal(annAttrSet.attrVer());
        this.dia = SignalAttrs.createConnectedSignal(annAttrSet.attrDia());
    }

    /**
     * Create a new, initialized <code>ProfileAttrSet</code> object.
     *
     * @param arrPfdHor     set of process variable descriptors for the horizonal signal
     * @param arrPfdVer     set of process variable descriptors for the vertical signal
     * @param arrPfdDia     set of process variable descriptors for the diagonal signal
     * @param ws            hardware device containing initialization data.
     *
     * @throws ConnectionException          unable to connect to a parameter read back channel
     * @throws GetException                 general CA GET exception while fetch field value

     * @since     Feb 25, 2010
     * @author    Christopher K. Allen
     */
    protected SignalAttrSet(AcceleratorNode  ws) throws ConnectionException, GetException {
        this();

        this.hor.loadHardwareValues(ws);
        this.ver.loadHardwareValues(ws);
        this.dia.loadHardwareValues(ws);
    }

}
