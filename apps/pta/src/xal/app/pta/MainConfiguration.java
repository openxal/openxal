/**
 * DeviceConfigManager.java
 *
 * @author Christopher K. Allen
 * @since  Apr 30, 2012
 *
 */
package xal.app.pta;

import xal.app.pta.daq.MachineConfig;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.service.pvlogger.PvLoggerException;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;
import xal.tools.xml.XmlDataAdaptor.WriteException;
import xal.smf.Accelerator;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice;

import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * Singleton class that manages the wire scanner device configurations.  We manage the 
 * persistence aspect of a configuration, offering load and store operations, as well as 
 * a default directory and a current data file name.  Specifically, we use the
 * configuration snapshots provided by the <code>{@link MachineConfig}</code> class 
 * to save and restore the machine configurations.
 * </p>
 *
 * <p>
 * <h4>------- IGNORE THE FOLLOWING ----------</h4>
 * This is a singleton class since we only need one object to manage the hardware.  In fact,
 * since modifying hardware configurations is somewhat risky to begin with, we should be doing it
 * from a central location.
 * <br/>
 * ------------------------------------------
 * </p>
 * <p>
 * If the accelerator used by the application (the one in the document) is changed then you must
 * update the accelerator used by the hardware configuration manager (i.e., this class).
 * </p>
 * <br/>
 * <br/>
 * TODO: Add WireHarp to configuration. We can generalize to ProfileDevice objects.1
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @see IConfigView#updateAccelerator(MainConfiguration)
 * @see IConfigView#updateConfiguration(MainConfiguration)
 *
 * @author Christopher K. Allen
 * @since   Apr 30, 2012
 */
public class MainConfiguration {
    
    
    /*
     * Global Attributes
     */
    
    /** The machine configuration manager */
    static private MainConfiguration            CFG_MAIN = null;

    
    //    /** The singleton machine configuration manager object */
    //    private static MachineConfigManager        MGR_MACHINE = null;
    
    
    /*
     * Global Methods
     */
    
//    /**
//     * Returns the application's machine configuration manager.
//     * On first creation of the application, the main configuration has
//     * no accelerator.  The <code>MainDocument</code> object must assign
//     * the current accelerator since it is the only object that is allowed
//     * to maintain the accelerator within the application framework.  
////     * If the
////     * configuration manager had not been instantiated yet, it is done so (instantiation
////     * of the configuration manager require a valid <code>{@link MainDocument}</code>
////     * object to provide the underlying accelerator object).
//     *
//     * @return  configuration document for the application
//     *
//     * @author Christopher K. Allen
//     * @since  Jul 10, 2012
//     */
//    public static MainConfiguration    getMainConfiguration() {
//        return MainApplication.CFG_MAIN;
//    }

    /**
     * Get the singleton instance of the hardware configuration manager.  If the 
     * manager is already instantiated, that instance is returned.  For the first call
     * to this method the configuration manager is created, using the current accelerator
     * object in the application document, then returned.  
     * 
     * @return   the singleton instance of the configuration manager
     *
     * @see MainApplication#getMainDocument()
     * @see MainDocument#getAccelerator()
     * 
     * @author Christopher K. Allen
     * @since  May 10, 2012
     */
    public static MainConfiguration getInstance() {
        
        // We already have an instance of the configuration manager
        if (CFG_MAIN != null) 
            return CFG_MAIN;
        
        // The configuration manager has not been requested yet
        //  and we need to create the instance and return it
        MainApplication appMain  = MainApplication.getApplicationInstance();
        if (appMain == null) {
            CFG_MAIN = new MainConfiguration();
            
            return CFG_MAIN;
        }
        
        MainDocument    docMain  = appMain.getMainDocument();
        if (docMain == null) {
            CFG_MAIN = new MainConfiguration();
            
            return CFG_MAIN;
        }
        
        Accelerator     smfAccel = docMain.getAccelerator();
        if (smfAccel == null) {
            CFG_MAIN = new MainConfiguration();
            
            return CFG_MAIN;
        }
        
        MainConfiguration.CFG_MAIN = new MainConfiguration(smfAccel);
        
        return CFG_MAIN;
    }
    
//    /**
//     * <p>
//     * Must be called whenever the user changes the underlying accelerator object.
//     * This class remembers the hardware object so that it is not necessary to 
//     * include it as a parameter for <code>{@link #saveConfiguration()}</code> and
//     * <code>{@link #saveConfigurationAs(URL)}</code>.
//     * </p>
//     * <p>
//     * Any previous machine configuration snapshots are erased, the source
//     * location is cleared, and the dirty bit is cleared.
//     * </p>
//     *
//     * @param smfAccel  new accelerator object containing managed hardware.
//     * 
//     * @see IDocView#updateAccelerator(MainDocument)
//     *
//     * @author Christopher K. Allen
//     * @since  May 10, 2012
//     */
//    public static void resetAccelerator(Accelerator smfAccel) {
//
//        // We already have an instance of the configuration manager
//        if (MGR_MACHINE != null) 
//            MGR_MACHINE.setAccelerator(smfAccel);
//        
//        MachineConfigManager.MGR_MACHINE = new MachineConfigManager(smfAccel);
//    }
    
    
    
    /*
     * Local Attributes
     */
    
    /** The accelerator containing the hardware which we are configuring */
    private Accelerator                 smfAccel;
    
    
    /** Current name of file containing device configuration data */
    private URL                         urlSource;
    
    /** Configuration is current (not dirty) flag */
    private boolean                     bolDirty;
    
    /** List of dirty devices */
    private final List<ProfileDevice>   lstDirtyDevs;
    
    
    /** The current machine configuration */
    private MachineConfig               cfgMachine;
    
    
    /** The set of registered views receiving updated configuration notifications */
    private final List<IConfigView>     lstViews;
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>MachineConfigManager</code> object with no accelerator.
     * The accelerator object must be specified later before use.
     * 
     * @see #resetAccelerator(Accelerator)
     *
     * @author  Christopher K. Allen
     * @since   May 14, 2012
     */
    private MainConfiguration() {
        this(null);
    }
    
    /**
     * <p>
     * Creates a new <code>MachineConfigManager</code> object attached to
     * the given accelerator object. 
     * </p>
     * <p>
     * Because this is a singleton class we prevent any outside instantiation.
     * </p>
     * 
     * @param smfAccel      accelerator object where all our managed devices live
     *
     * @author  Christopher K. Allen
     * @since   Apr 30, 2012
     */
    private MainConfiguration(Accelerator smfAccel) {
        this.smfAccel   = smfAccel;
        
        this.urlSource  = null;
        this.cfgMachine = null;
        this.bolDirty   = false;
        
        this.lstDirtyDevs = new LinkedList<ProfileDevice>();
        this.lstViews     = new LinkedList<IConfigView>();
    }
    
    /**
     * Resets the underlying accelerator object which contains all the managed
     * devices. Any previous machine configuration snapshots are erased, the source
     * location is cleared, and the dirty bit is cleared.
     *
     * @param smfAccel  new accelerator object contained the devices we are to manage
     *
     * @author Christopher K. Allen
     * @since  May 10, 2012
     */
    void resetAccelerator(Accelerator smfAccel) {
        this.smfAccel = smfAccel;
        this.cfgMachine = null;
        this.urlSource  = null;

        this.clearDirtyFlags();
        this.notifyAcceleratorUpdated();
    }
    

    /*
     * Attributes
     */
    
    /**
     * Returns the machine hardware we are configuring.
     *
     * @return  SMF accelerator object representing machine hardware
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    public Accelerator  getAccelerator() {
        return this.smfAccel;
    }
    
    /**
     * Returns the location of the file contain the machine configuration data contained in this
     * data structure.
     *
     * @return  file location of persistent backing store, or <code>null</code> if the structure is empty
     *
     * @author Christopher K. Allen
     * @since  May 2, 2012
     */
    public URL  getSource() {
        return this.urlSource;
    }
    
    /**
     * Returns the "is data structure current" flag.  That is, it returns a <code>true</code>
     * when the data structure (thinks it) contains the image of the current machine
     * configuration.
     *
     * @return  <code>true</code> when the data structure is up-to-date, <code>false</code> when it is dirty
     *
     * @author Christopher K. Allen
     * @since  May 2, 2012
     */
    public boolean  isDirty() {
        return this.bolDirty;
        
    }
    
    /**
     * Returns number of wire scanner devices composing the configuration.  This is a proxy
     * to the underlying <code>{@link MachineConfig}</code> object.
     * 
     * @return  returns the number of devices configured in this <code>MachineConfig</code> instance
     *
     * @author Christopher K. Allen
     * @since  May 10, 2012
     * 
     * @see MachineConfig#getDeviceCount()
     */
    public int  getDeviceCount() {
        return this.cfgMachine.getDeviceCount();
    }
    
    /**
     * Returns the string identifiers of the devices composing this machine configuration.  If the
     * machine configuration has not yet been defined an empty set is returned (i.e., a set of 
     * size zero).  This is a proxy
     * to the underlying <code>{@link MachineConfig}</code> object.
     *
     * @return  the identifiers of all devices included in this configuration set.
     *
     * @author Christopher K. Allen
     * @since  Jun 19, 2012
     * 
     * @see MachineConfig#getDeviceIds()
     */
    public Set<String> getConfiguredDeviceIds() {
        return this.cfgMachine.getDeviceIds();
    }
    
    /**
     * Returns the current collection of devices that have been flagged as
     * modified.  Currently nothing is done with this collection internally.
     *
     * @return  collection of devices which have previously be flagged by the user as modified
     *
     * @author Christopher K. Allen
     * @since  Aug 24, 2012
     * 
     * @see #setDirty(Object, Collection)
     * @see #setDirty(Object, WireScanner)
     */
    public Collection<ProfileDevice>  getDirtyDevices() {
        return this.lstDirtyDevs;
    }
    

    /*
     * Operations
     */
    
    /**
     * Sets the dirty flag; that is, indicate that the configuration of the machine devices
     * are different than that recorded here.  Also, record the device that was modified.  Currently
     * nothing is done with this information, but maybe later we'll have a method to save only the 
     * modified devices if that would make sense somehow.
     *
     * @param objSource     the calling object - the source of the dirty configuration 
     * @param smfDev        the device whose configuration was modified
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public void setDirty(Object objSource, ProfileDevice smfDev) {
        this.bolDirty = true;
        this.lstDirtyDevs.add(smfDev);
        
        this.notifyConfigurationUpdated(objSource);
    }
    
    /**
     * Sets the dirty flag; that is, indicate that the configuration of the machine devices
     * are different than that recorded here.  Also, record the device that was modified.  Currently
     * nothing is done with this information, but maybe later we'll have a method to save only the 
     * modified devices if that would make sense somehow.
     *
     * @param objSource     the calling object - the source of the dirty configuration 
     * @param setDevs       the devices whose configuration was modified
     *
     * @author Christopher K. Allen
     * @since  Aug 24, 2012
     */
    public void setDirty(Object objSource, Collection<ProfileDevice> setDevs) {
        this.bolDirty = true;
        this.lstDirtyDevs.addAll(setDevs);
        
        this.notifyConfigurationUpdated(objSource);
    }
    
    /**
     * Registers the given object as a listener of <code>IConfigView</code>
     * events.
     * 
     * @param lsnConfigEvts     object to receive configuration events
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     * 
     * @see IConfigView
     */
    public void registerView(IConfigView lsnConfigEvts) {
        this.lstViews.add(lsnConfigEvts);
    }
    
//    /**
//     * Sets the location of the persistent backing store.
//     *
//     * @param urlSource     URL of the machine configuration file
//     *
//     * @author Christopher K. Allen
//     * @since  May 2, 2012
//     */
//    public void    setSource(URL urlSource) {
//        this.urlSource = urlSource;
//    }
    
    /**
     * Applies the configuration information contained in the file at the given URL to the 
     * devices in the current accelerator.  The configuration information is first
     * loaded into the internal configuration data structure.  Then the data structure
     * pushes this information to all devices specified in the file.
     *
     * @param urlFile   location of the file with device configuration information
     * 
     * @return          a collection of device IDs that were specified in the file and 
     *                  whose configuration were successfully be updated
     *                  
     * @throws NullPointerException         the attached accelerator object was never specified  
     * @throws ResourceNotFoundException    the location was bad
     * @throws ParseException               the file was not parse-able as XML
     * @throws IllegalArgumentException     bad file format
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public Collection<String>   applyConfiguration(URL urlFile)
        throws NullPointerException, ResourceNotFoundException, ParseException, IllegalArgumentException
    {
        XmlDataAdaptor  daptSrc = XmlDataAdaptor.adaptorForUrl(urlFile, false);
     
        this.urlSource  = urlFile;
        MachineConfig cfgMachine = new MachineConfig(daptSrc);
        
        return this.applyConfiguration(cfgMachine);
    }
    
    /**
     * Applies the configuration information contained in the given data structure to the 
     * devices in the current accelerator.  Then the data structure
     * pushes this information to all devices specified in the file then is stored as the 
     * default configuration.
     *
     * @param cfgMachine    data structure containing the device configuration parameters to be
     *                      set to the machine hardware
     * 
     * @return          a collection of device IDs that were specified in the configuration and 
     *                  whose configuration were successfully be updated
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    public Collection<String>   applyConfiguration(MachineConfig cfgMachine) {
        this.cfgMachine = cfgMachine;

        Collection<String>  lstSuccessIds = this.cfgMachine.applyConfiguration(this.smfAccel);
        
        if ( lstSuccessIds.size() > 0 ) {
            this.clearDirtyFlags();
            this.notifyConfigurationUpdated(this);
        } 
        
        return lstSuccessIds;
    }
    
    
    /**
     * Applies the current machine configuration parameters to the machine
     * devices.  We assume that this information has already been specified with a
     * call to <code>{@link MainConfiguration#applyConfiguration(URL)}</code>,
     * <code>{@link MainConfiguration#applyConfiguration(MachineConfig)}</code>, or
     * <code>{@link MainConfiguration#snapshotConfiguration(List)}</code>.
     *
     * @return          a collection of device IDs that were specified in the file and 
     *                  whose configuration were successfully be updated
     *
     * @throws  IllegalStateException   this method was called before specifying a default configuration
     * 
     * @author Christopher K. Allen
     * @since  May 2, 2012
     */
    public Collection<String>   applyDefaultConfiguration() throws IllegalStateException {
        if (this.urlSource == null)
            throw new IllegalStateException(
                    "Operation cannot be performed, configuration information was not previously loaded"
                    );

        return this.applyConfiguration( this.getSource() );
    }
    
    
    
    
    //    /**
    //     * Sets the location of the persistent backing store.
    //     *
    //     * @param urlSource     URL of the machine configuration file
    //     *
    //     * @author Christopher K. Allen
    //     * @since  May 2, 2012
    //     */
    //    public void    setSource(URL urlSource) {
    //        this.urlSource = urlSource;
    //    }
        
        /**
         * Load the configuration parameter of the given wire scanner devices into the
         * managed data structure of device configuration information.
         *
         * @param lstDevs   list of wire scanners from which configuration information is retrieved
         * 
         * @return          the configuration parameters of the given device set
         *                  
         * @throws PvLoggerException        could not take PV Logger machine snapshot 
         * @throws ConnectionException      could not connect to a profile device
         * @throws GetException             could not read configuration from a profile device
         *
         * @author Christopher K. Allen
         * @since  May 4, 2012
         */
        public MachineConfig snapshotConfiguration(List<WireScanner> lstDevs) 
            throws ConnectionException, GetException, PvLoggerException 
        {
            this.cfgMachine = MachineConfig.acquire(lstDevs);
            this.bolDirty = false;
            
            return this.cfgMachine;
        }

    /**
     * Saves the current machine snapshot to the current source URL. This is the same
     * source URL used as the argument to a previous call to {@link #saveSnapshotAt(URL)}.
     *
     * @throws IllegalArgumentException the source object was never specified
     * @throws IllegalStateException    no snapshot of the current machine configuration is available and/or
     *                                  the source URL was never defined
     * @throws WriteException           unable to write to the given file location
     *
     * @see #snapshotConfiguration(List)
     * @see #getSource()
     * 
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public void     saveSnapshot() 
        throws IllegalArgumentException, IllegalStateException, WriteException 
    {
        if (this.urlSource == null)
            throw new IllegalStateException("Source location was never defined");
        
        this.saveSnapshotAt( this.urlSource );
    }
    
    
    /**
     * Saves the current device configuration information to a file at the given location.
     * If the file already exists, it is overwritten.  The given file location becomes the
     * new "source" object and proceeding <code>{@link #saveSnapshot()}</code> operations
     * are stored there.
     *
     * @param urlLoc    location of the file to receive configuration information
     * 
     * @throws IllegalArgumentException the argument is <code>null</code>
     * @throws IllegalStateException    no snapshot of the current machine configuration is available
     * @throws WriteException           unable to write to the given file location
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public void saveSnapshotAt(URL  urlLoc) 
        throws IllegalArgumentException, IllegalStateException, WriteException 
    {

        // Check for a bad file name
        if (urlLoc == null) 
            throw new IllegalArgumentException(
                    "Operation cannot be performed, no configuration file was specified"
                    );

        // Check to make sure we have a configuration snapshot to save
        if (this.cfgMachine == null)
            throw new IllegalStateException(
                    "Operation cannot be performed, configuration information was not previously loaded"
            );

        // Create a data adaptor, write out the configuration contents, then save the adaptor to disk
        XmlDataAdaptor daptDoc = XmlDataAdaptor.newEmptyDocumentAdaptor();
        daptDoc.writeNode(this.cfgMachine);
        daptDoc.writeToUrl(urlLoc);

        this.clearDirtyFlags();
        this.urlSource = urlLoc;
    }

    
    /*
     *  Support Methods 
     */
    
    /**
     * Notify all the registered configuration event listeners that the
     * accelerator has been changed.
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    private void notifyAcceleratorUpdated() {
        for (IConfigView lsn : this.lstViews)
            lsn.updateAccelerator(this);
    }
    
    /**
     * Notify all the registered configuration event listeners that the
     * accelerator configuration has been changed.
     * 
     * @param   objSource       source of the configuration change
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    private void notifyConfigurationUpdated(Object objSource) {
        for (IConfigView lsn : this.lstViews) {

            // Do not create a potential strange loop by notifying the 
            //      source of the configuration change 
            if (lsn.equals(objSource))
                continue;
            
            lsn.updateConfiguration(this);
        }
    }
    
    /**
     * Called to clear out all the dirty configuration flags and
     * information, whatever that is at present.
     *
     * @author Christopher K. Allen
     * @since  Aug 24, 2012
     */
    private void clearDirtyFlags() {
        this.bolDirty = false;
        this.lstDirtyDevs.clear();
    }
    
    
} 
