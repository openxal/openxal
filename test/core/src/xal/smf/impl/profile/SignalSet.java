/**
 * SignalSet.java
 *
 * Author  : Christopher K. Allen
 * Since   : Mar 13, 2014
 */
package xal.smf.impl.profile;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.impl.WireScanner;
import xal.smf.scada.BadStructException;
import xal.smf.scada.ScadaAnnotationException;
import xal.smf.scada.ScadaFieldDescriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.MissingResourceException;

/**
 * <p>
 * Base class for data structures contains profile data as measured by the wire scanner
 * (ergo the identifier <code>SignalSet</code>.  Note that these guys are different
 * from the other data structures in that they all have common structure.  
 * Thus I have used a different (more simple, I hope) implementation
 * to populate them.  Each derived class supplies its out (field name, PV descriptor) 
 * pairs to the constructor just as in the base class.
 * </p>
 * <p>
 * This class is essentially an active data structure
 * which populates itself with the measurement data
 * from the diagnostic hardware.
 * </p>
 * <p>
 * A data set composed of beam profile signals measured from each plane of the wire harp.  
 * The data from each plane is available through the class attributes of type 
 * <code>{@link WireScanner.Signal}</code>.
 * </p>
 * <p>
 * The channel handles needed to connect the <code>Signal</code> attributes to the
 * wire harp device are specified in the derived classes by annotating it with the 
 * <code>{@link WireScanner.ASignal}</code> annotation.
 * </p>
 *
 * <p>
 * <b>Ported from XAL on Jul 15, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Nov 13, 2009
 * @version Mar 05, 2013
 * @author Christopher K. Allen
 */
public abstract class SignalSet implements DataListener {

    /*
     * Internal Classes
     */
    
    /*
     * Global Operations
     */
    
    /**
     * <p>
     * Returns the set of field descriptors for (field, channel)
     * pairs used by the given<code>SignalSet</code>-derived class.  The given
     * signal set must be annotated with the annotation <code>ASignal.ASet</code>
     * for the operation to succeed.
     * </p>
     * <p>
     * Only the descriptors for which the <code>SignalSet</code> annotation has values
     * will be returned.  This, of course, includes fields for which <code>SignalSet</code>
     * has default values.
     * </p> 
     *
     * @param clsData   the <code>SignalSet</code>-derived child class 
     *                  with ASignal.ASet annotations
     *  
     * @return  set of field descriptors used by the given class which are provided with
     *          non-empty values in the <code>SignalSet</code> annotation.
     *
     * @throws ScadaAnnotationException the <code>ASignal.ASet</code> annotation was missing
     * 
     * @author Christopher K. Allen
     * @since  Sep 27, 2011
     */
    public static List<ScadaFieldDescriptor>  getFieldDescriptorList(Class<? extends SignalSet> clsData)
        throws ScadaAnnotationException
    {
        List<ScadaFieldDescriptor>  lstFds = new LinkedList<ScadaFieldDescriptor>();
        
        
        if ( !clsData.isAnnotationPresent(ASignal.ASet.class) )
             throw new ScadaAnnotationException("There is no ASignal.ASet annotation on class " + clsData.getName());
        
        ASignal.ASet annSigSet = clsData.getAnnotation(ASignal.ASet.class);
        
        List<ASignal>   lstSigAnn = new LinkedList<ASignal>();
        lstSigAnn.add( annSigSet.sigHor() );
        lstSigAnn.add( annSigSet.sigVer() );
        lstSigAnn.add( annSigSet.sigDia() );
        
        for (ASignal sig : lstSigAnn) {
            for (Signal.FIELD fld : Signal.FIELD.values()) {
                
                try {
                    ScadaFieldDescriptor sfd = fld.createDescriptor(sig);
                    
                    if (sfd != null)
                        lstFds.add(sfd);

//                } catch (BadStructException e) {
//                    throw new ScadaAnnotationException("Missing field from annotation " + sig.getClass().getName(), e);
                    
                } catch (IllegalArgumentException e) {
                    throw new ScadaAnnotationException("Missing method " + fld + " in annotation " + sig.getClass().getName(), e);
                    
                }
                
            }
        }
        
        return lstFds;
    }



    /*
     * Local Attributes
     */

    /** The horizontal measurement signal */
    public Signal           hor;

    /** The vertical measurement signal */
    public Signal           ver;

    /** The diagonal measurement signal */
    public Signal           dia;


    
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
    
    /**
     * Returns the signal object of this set corresponding to the give
     * profile angle.
     * 
     * @param angle     measurement angle of the desired signal
     * 
     * @return          measurement signal corresponding to the given angle
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public Signal   getSignal(ProfileDevice.ANGLE angle) {
        
        switch (angle) {
        case HOR: return this.hor;
        case VER: return this.ver;
        case DIA: return this.dia;
        default:  return null;
        }
    }

//    /**
//     * Chooses the appropriate signal for this signal constant
//     * from the given signal set.
//     * 
//     * @param setSignals    set of signals to choose from
//     * 
//     * @return              the signal corresponding to this signal constant
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 14, 2014
//     */
//    public Signal   getSignal(Signal.WIRE enmWire) {
//        switch (enmWire) {
//        case HOR: return this.hor; 
//        case VER: return this.ver;
//        case DIA: return this.dia;
//        default: return null;
//        }
//    }
//    
    /**
     * Sets the signal for the given measurement angle to the given signal
     * object.
     * 
     * @param angle     measurement angle of the modified signal  
     * 
     * @param signal    new signal for the given measurement angle
     *
     * @author Christopher K. Allen
     * @since  Apr 23, 2014
     */
    public void setSignal(ProfileDevice.ANGLE angle, Signal signal) {
        
        switch (angle) {
        case HOR: this.hor = signal; break;
        case VER: this.ver = signal; break;
        case DIA: this.dia = signal; break;
        default:  ;
        }
    }
    
    /**
     * Populate the fields of this data set by acquiring the current
     * values of the signal process variables of the given device.  
     *
     * @param smfDev    hardware device from which the signal data is acquired
     * 
     * @throws BadStructException  data structure fields are ill-defined/incompatible 
     * @throws ConnectionException  unable to connect to a descriptor read back channel 
     * @throws GetException         unable to get PV value from channel access or
     * 
     * @since  Dec 18, 2009
     * @author Christopher K. Allen
     */
    public void loadHardwareValues(ProfileDevice smfDev) 
            throws BadStructException, ConnectionException, GetException 
    {
        this.hor.loadHardwareValues(smfDev);
        this.ver.loadHardwareValues(smfDev);
        this.dia.loadHardwareValues(smfDev);
    }
    
    /**
     * Averages all the signals in the signal set using the given averaging factor.
     * See <code>Signal{@link Signal#average(Signal, double)}</code> for additional information.
     * 
     * @param setCombine    signal set to average into this one
     * @param dblWtFac     averaging magnitude &lambda; &isin; [0,1]
     *
     * @throws IllegalArgumentException the provided signal is not the same size as this signal
     *
     * @author Christopher K. Allen
     * @since  May 1, 2014
     */
    public void average(SignalSet setCombine, double dblWtFac) throws IllegalArgumentException {
        for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
            Signal  sigAccum = this.getSignal(angle);
            Signal  sigAddend = setCombine.getSignal(angle);
            
            sigAccum.average(sigAddend, dblWtFac);
        }
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
     * Load the contents of this data set
     * from the persistent store behind the 
     * <code>DataListener</code> interface.
     * 
     * @param daptSrc       data source
     *
     * @throws  MissingResourceException        a data field was missing from the data source
     * @throws BadStructException  data structure fields are ill-defined/incompatible
     *  
     * @since       Mar 4, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
     */
    @Override
    public void update(DataAdaptor daptSrc) throws MissingResourceException, BadStructException {
        
        // Assume we are given the parent node then get this data node from the parent node
        String  strLabel = this.dataLabel();
        
        DataAdaptor daptSgnls = daptSrc.childAdaptor( strLabel );

        // Check if these data is in XAL format
        if (daptSgnls == null) {
            strLabel = "gov.sns." + strLabel;
            daptSgnls  = daptSrc.childAdaptor(strLabel);
        }

        // Turns out we were given the data adaptor for this node
        if (daptSgnls == null)
            daptSgnls = daptSrc;

        // Look for the middle version format - Open XAL before the format correction
        //  was made. This one is problematic, we must guess at the order.  
        String              strLblOld = Signal.class.getCanonicalName();

        List<DataAdaptor>   lstDaptOld = daptSgnls.childAdaptors(strLblOld);
        
        // If we are in the middle format, we load sequentially according to index and return.
        if (lstDaptOld.size() > 0)  {
            for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
                int         index = angle.getIndex();
                DataAdaptor dapt  = lstDaptOld.get(index);
                Signal      sgnl  = this.getSignal(angle);

                sgnl.update(dapt);
            }

            return;
        }

        // Assume that we have the XAL format or the current format   
        // Read in each signal using the current data format
        for (ProfileDevice.ANGLE angle : ProfileDevice.ANGLE.values()) {
            DataAdaptor     dapt = daptSgnls.childAdaptor( angle.getLabel() );
            Signal          sgnl = this.getSignal(angle);

            sgnl.update(dapt);
        }

//        hor.update(daptSgnls);
//        ver.update(daptSgnls);
//        dia.update(daptSgnls);
    }

    /**
     * Write out the contents of this measurement data 
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
            DataAdaptor     daptSgnl = daptSgnls.createChild( angle.getLabel() );
            Signal          sigAngle = this.getSignal(angle);

            sigAngle.write(daptSgnl);
        }
    }

    /*
     * Object Overrides
     */

    /**
     * Write out a text description of the data structure field
     * values.
     * 
     * @return  string representation of the data structure values
     *
     * @since   Feb 5, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer    bufStr = new StringBuffer();
        bufStr.append(this.getClass().getName() + " values\n"); //$NON-NLS-1$

        bufStr.append("hor signal = " + this.hor.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        bufStr.append("ver signal = " + this.ver.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        bufStr.append("dia signal = " + this.dia.toString() + "\n"); //$NON-NLS-1$ //$NON-NLS-2$

        return bufStr.toString();
    }



    /*
     * Initialization (Internal)
     */

    /**
     * Create a new, uninitialized <code>SignalSet</code> object initialize
     * with values fetched from the given device.
     *
     * @throws ScadaAnnotationException this class is not annotated with <code>ASignal.ASet</code>,
     *                                  or the annotation is corrupt   
     *  
     * @author  Christopher K. Allen
     * @since   Jan 31, 2013
     */
    protected SignalSet() throws ScadaAnnotationException {

        // Check for annotation by the SignalSet annotation.  If none then this is a
        //  "blank" data structure and there is nothing to do.  
        if (!this.getClass().isAnnotationPresent(ASignal.ASet.class)) {

//            this.hor = new Signal(ProfileDevice.ANGLE.HOR);
//            this.ver = new Signal(ProfileDevice.ANGLE.VER);
//            this.dia = new Signal(ProfileDevice.ANGLE.DIA);

            this.hor = new Signal();
            this.ver = new Signal();
            this.dia = new Signal();

            return;
        }

        // There are an active data connections
        //  Retrieve the annotations describing the signals composing this class 
        //  and create the connected Signal objects
        ASignal.ASet  annSigs = this.getClass().getAnnotation(ASignal.ASet.class);

        ASignal     annHor = annSigs.sigHor();
        ASignal     annVer = annSigs.sigVer();
        ASignal     annDia = annSigs.sigDia();

        this.hor = Signal.createConnectedSignal(annHor);
        this.ver = Signal.createConnectedSignal(annVer);
        this.dia = Signal.createConnectedSignal(annDia);
    }

    /**
     * Create a new <code>SignalSet</code> object initializing it
     * with values acquired from the given device.
     *
     * @param smfDev        data acquisition device
     * 
     * @throws ScadaAnnotationException     the <code>ASignal.ASet</code> annotations are incomplete
     * @throws ConnectionException          unable to connect to a parameter read back channel
     * @throws GetException                 general CA GET exception while fetch field value
     * @throws BadStructException          the DAQ data structure and DAQ channels are incompatible, bad definition
     *
     * @author  Christopher K. Allen
     * @since   Jan 31, 2013
     */
    protected SignalSet(ProfileDevice smfDev) 
        throws ScadaAnnotationException, ConnectionException, GetException, BadStructException 
    {
        this();
        this.loadHardwareValues(smfDev);
    }

    /**
     * Creates a new instance of <code>SignalSet</code> and initialize the signal data from the given
     * data source.
     *
     * @param daSrc     data source using the <code>DataAdaptor</code> interface
     * 
     * @throws IllegalAccessException       if an underlying <code>Signal</code> field is inaccessible.
     * @throws IllegalArgumentException     general field incompatibility exception
     *
     * @author Christopher K. Allen
     * @since  Feb 12, 2013
     */
    protected SignalSet(DataAdaptor daSrc) throws IllegalArgumentException, IllegalAccessException {
        this();

        this.update(daSrc);
    }
}
