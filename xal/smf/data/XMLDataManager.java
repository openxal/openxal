/*
 * XMLDataManager.java
 *
 * Created on June 4, 2002, 12:49 PM
 */

package xal.smf.data;

import xal.sim.cfg.ModelConfiguration;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNodeFactory;
import xal.smf.TimingCenter;
import xal.tools.xml.*;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;
import xal.tools.data.*;
import xal.tools.UrlTool;

import org.w3c.dom.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.net.*;


/*****************************************************************************
 * The XMLDataManager is the central class providing XML specific access to 
 * the optics file (which represents static accelerator data) and the 
 * table files (which represent dynamic data).  A single main file lists 
 * the references to the optics file and the table files to load for a 
 * particular session.  XMLDataManager is the central class providing access
 * to parse and write all of this data.  It generates the appropriate 
 * data adaptors for populating the object graph.
 *
 * Three private inner member classes provide help to the XMLDataManager for 
 * parsing and writing the three data files (Main reference file, Static optics 
 * file and the dynamic table files).
 *
 * @author  tap
 */
public class XMLDataManager {
    private static final String MAIN_PATH_PREF_KEY = "mainPath";
	
	/** manage the bindings of device types to AcceleratorNode subclasses */
	final private DeviceManager DEVICE_MANAGER;
	
	/** Loads the model configuration information */
	final private ModelConfigLoader   ldrModelConfig;
	
    private MainManager mainManager;
    private AcceleratorManager acceleratorManager;
    private TableManager tableManager;
	private TimingDataManager _timingManager;
	
	
    /** Primary Constructor */
    public XMLDataManager( final String urlPath ) {
		DEVICE_MANAGER = new DeviceManager();
		ldrModelConfig = new ModelConfigLoader();
		_timingManager = new TimingDataManager();
        acceleratorManager = new AcceleratorManager();
        tableManager = new TableManager();
        mainManager = new MainManager( urlPath );
    }
	
	
	/** factory method given a URL to the main optics source */
	static public XMLDataManager getInstance( final URL url ) {
		return new XMLDataManager( url.toString() );
	}
    
    
	/**
	 * Create and return a new XMLDataManager with its source given by the specified file path.
	 * @param filePath The file path of the accelerator data source.
	 * @return The new XMLDataManager
	 */
    static public XMLDataManager managerWithFilePath( final String filePath ) throws UrlTool.FilePathException {
        String urlSpec = UrlTool.urlSpecForFilePath( filePath );
        return new XMLDataManager( urlSpec );
    }
	
	
	/**
	 * Get the default XMLDataManager based on the user's preferred path to the Main data source
	 * @return A new instance of the XMLDataManager with the user's preferred data source or null if no default has been specified
	 */
	static public XMLDataManager getDefaultInstance() {
        String path = defaultPath();
		return (path != null) ? new XMLDataManager(UrlTool.urlSpecForFilePath(path)) : null;
	}
    
    
    /**
	 * Read the timing center from the timing URL.
	 * @return a timing center read from the timing URL.
	 * @throws xal.tools.xml.XmlDataAdaptor.ParseException if the TimingCenter cannot be generated
     */    
    public TimingCenter getTimingCenter() throws XmlDataAdaptor.ParseException {
        return _timingManager.getTimingCenter();
    }
    
    
	/**
	 * Read the accelerator from the data source at the URL path.
	 * @param urlPath The URL spec of the data source.
	 * @return the new accelerator read from the data source.
	 */
    static public Accelerator acceleratorWithUrlSpec(String urlPath) {
        XMLDataManager dataManager = new XMLDataManager(urlPath);
        return dataManager.getAccelerator();
    }
    
    
	/**
	 * Read the accelerator from the data source at the file path.
	 * @param filePath The file path of the data source.
	 * @return the new accelerator read from the data source.
	 */
    static public Accelerator acceleratorWithPath(String filePath) throws UrlTool.FilePathException {
        XMLDataManager dataManager = managerWithFilePath(filePath);
        return dataManager.getAccelerator();
    }
    
    
	/**
	 * Read the accelerator from the data source at the file path and using DTD validation if 
	 * if the user specifies.
	 * @param filePath The file path of the data source.
	 * @param isValidating enable DTD validation if true and disable DTD validation if false
	 * @return the new accelerator read from the data source
	 */
    static public Accelerator acceleratorWithPath(String filePath, boolean isValidating) throws UrlTool.FilePathException {
        XMLDataManager dataManager = managerWithFilePath(filePath);
        return dataManager.getAccelerator(isValidating);
    }
    
    
	/**
	 * Load the accelerator corresponding to the default accelerator data source specified in the user's preferences.
	 * @return the accelerator built from the default data source or null if no default accelerator is specified
	 */
    static public Accelerator loadDefaultAccelerator() {
        String path = defaultPath();
        return (path != null) ? acceleratorWithUrlSpec(UrlTool.urlSpecForFilePath(path)) : null;
    }
    
    
	/**
	 * Get the path to the default main data source specified in the user's preferences.
	 * @return the file path to the default accelerator data source or null if a default hasn't been specified
	 */
    static public String defaultPath() {
        Preferences prefs = Preferences.userNodeForPackage( XMLDataManager.class );
        String path = prefs.get(MAIN_PATH_PREF_KEY, null);
        return path;
    }
    
    
	/**
	 * Set the path to the default main data source and store it in the user's preferences.
	 * @param path the new file path to the default accelerator data source
	 */
    static public void setDefaultPath(String path) {
        Preferences prefs = Preferences.userNodeForPackage( XMLDataManager.class );
        prefs.put(MAIN_PATH_PREF_KEY, path);
    }
    
    
	/**
	 * Get the URL to the accelerator data source which includes pointers to the optics 
	 * and other supporting data such as the edit context and optics corrections.
	 * @return The URL to the accelerator data source.
	 */
    public URL mainUrl() {
        return mainManager.mainUrl();
    }
     
    
	/**
	 * Get the URL spec to the accelerator data source which includes pointers to the optics
	 * and other supporting data such as the edit context and optics corrections.
	 * @return The URL spec to the accelerator data source.
	 */
    public String mainUrlSpec() {
        return mainManager.mainUrlSpec();
    }
    
    
	/**
	 * Set the URL spec to use as the accelerator data source which includes pointers to the 
	 * optics and other supporting data such as the edit context and optics corrections.
	 * @param urlSpec The new URL spec to the accelerator data source.
	 */
    public void setMainUrlSpec(String urlSpec) {
        mainManager.setMainUrlSpec(urlSpec);
    }
    
    
	/**
	 * Set the file path to use as the accelerator data source which includes pointers to the 
	 * optics and other supporting data such as the edit context and optics corrections.
	 * @param filePath The new file path to the accelerator data source.
	 */
    public void setMainPath(String filePath) throws UrlTool.FilePathException {
        String urlSpec = UrlTool.urlSpecForFilePath(filePath);
        
        setMainUrlSpec(urlSpec);
    }
    
    
	/**
	 * Get the URL spec to the accelerator optics.
	 * @return The URL spec to the accelerator optics.
	 */
    public String opticsUrlSpec() {
        return acceleratorManager.opticsUrlSpec();
    }
    
    
	/**
	 * Set the URL spec to the accelerator optics.
	 * @param urlSpec The new URL spec to the accelerator optics.
	 */
    public void setOpticsUrlSpec(String urlSpec) {
        acceleratorManager.setOpticsUrlSpec(urlSpec);
    }
    
    
	/**
	 * Get the URL spec of the DTD file used in the optics XML file.
	 * @return the URL spec of the DTD file used in the optics XML file.
	 */
    public String opticsDtdUrlSpec() {
        return acceleratorManager.dtdUrlSpec();
    }
    
    
	/**
	 * Set the URL spec of the DTD file to use in the optics XML file.
	 * @param urlSpec the URL spec of the DTD file to use in the optics XML file.
	 */
    public void setOpticsDtdUrlSpec(String urlSpec) {
        acceleratorManager.setDtdUrlSpec(urlSpec);
    }
	
	
	/** Get the URL spec for the hardware status */
	public String getHardwareStatusURLSpec() {
		return acceleratorManager.getHardwareStatusURLSpec();
	}
	
    
	/**
	 * Get the table groups (names) read from the edit context of the accelerator data source.
	 * An edit context lists table groups and each table group is the name of a group of 
	 * tables all of which reside in a single file.
	 * @return the collection of table group names read from the edit context
	 * @see xal.tools.data.EditContext
	 */
    public Collection getTableGroups() {
        return tableManager.getTableGroups();
    }
    
    
	/**
	 * Get the URL spec of the data source of the specified table group.
	 * An edit context lists table groups and each table group is the name of a group of 
	 * tables all of which reside in a single file.
	 * @param tableGroup The table group name
	 * @return The URL spec of the data source of the specified table group
	 */
    public String urlSpecForTableGroup(String tableGroup) {
        return tableManager.urlSpecForTableGroup(tableGroup);
    }
    
    
	/**
	 * Set the URL spec of the data source for the specified table group.
	 * An edit context lists table groups and each table group is the name of a group of 
	 * tables all of which reside in a single file.
	 * @param urlSpec The URL spec of the file where the table group resides.
	 * @param tableGroup The table group name
	 */
    public void setUrlSpecForTableGroup(String urlSpec, String tableGroup) {
        tableManager.setUrlSpecForTableGroup(urlSpec, tableGroup);
    }
    
        
    /** 
     * Parse the accelerator from the optics URL without DTD validation and
     * also populate the dynamic data.
	 * @return the accelerator parsed from the accelerator data source
     */    
    public Accelerator getAccelerator() throws XmlDataAdaptor.ParseException {
        return getAccelerator(false);
    }
    
    
    /** 
     * Parse the accelerator from the optics URL with the specified DTD
     * validation flag and also populate the dynamic data.
	 * @param isValidating use DTD validation if true and don't validate if it is valse
	 * @return the accelerator parsed from the accelerator data source
     */    
    public Accelerator getAccelerator(boolean isValidating) throws XmlDataAdaptor.ParseException {
        return acceleratorManager.getAccelerator(isValidating);        
    }
    
    
    /** 
     * update the accelerator with data from the optics URL with a
     * DTD validation flag
	 * @param accelerator The accelerato to update with data from the sources
	 * @param isValidating use DTD validation if true and don't validate if false
     */    
    public void updateOptics(Accelerator accelerator, boolean isValidating) throws XmlDataAdaptor.ParseException {
        acceleratorManager.updateAccelerator(accelerator, isValidating);
    }
    
    
    /**
     * Reads the tables of the table group into the editContext getting its 
     * data from the URL associated with the group.
	 * @param editContext The edit context into which to place the tables which are read
	 * @param group The table group to read from its associated URl
     */
    public void readTableGroup(EditContext editContext, String group) {
        tableManager.readTableGroup(editContext, group);
    }
    
    
    /**
     * Reads the tables of the table group into the editContext getting its 
     * data from the specified URL.
	 * @param editContext The edit context into which to place the tables which are read
	 * @param group The table group to read from the specified URL
	 * @param urlSpec The URL spec of the table group source
     */
    static public void readTableGroupFromUrl(EditContext editContext, String group, String urlSpec) {
        XmlTableIO.readTableGroupFromUrl(editContext, group, urlSpec);
    }
    
    
	/**
	 * Write the main file which lists the pointers to the optics, edit context and extra optics files.
	 * This does not write the files to which the main file points.
	 */
    public void writeMain() {
        mainManager.write();
    }
	
	
	/**
     * Write the entire accelerator including the optics to the optics file, edit context to the appropriate
	 * files for the table groups and the main file which references these sources.
	 * @param accelerator The accelerator which holds the optics and the edit context.
	 */
    public void writeAccelerator(Accelerator accelerator) {
        EditContext editContext = accelerator.editContext();
        
        writeEditContext(editContext);
        writeOptics(accelerator);
        writeMain();
    }
    
    
	/**
	 * Write the optics part of the accelerator to an optics file using the location set in this data manager.
	 * @param accelerator The accelerator to store in the optics file.
	 */
    public void writeOptics(Accelerator accelerator) {
        acceleratorManager.write(accelerator);
    }
    
    
	/**
	 *
	 */
    public void writeEditContext(EditContext editContext) {
        tableManager.writeEditContext(editContext);
    }
    
    
	/**
	 *
	 */
    static public void writeTableGroupToUrl(EditContext editContext, String tableGroup, String urlSpec) {
        XmlTableIO.writeTableGroupToUrl(editContext, tableGroup, urlSpec);
    }
    
    
	/**
	 *
	 */
    public void writeTableGroup(EditContext editContext, String groupName) {
        tableManager.writeTableGroup(editContext, groupName);
    }

    
    
    /*************************************************************************
     * Handle read/write for the Main XML reference sources.  This manager 
     * is used to handle the references to the optics file and the 
     * dynamic table files.  
     */
    private class MainManager {
        static final private String SOURCE_TAG = "sources";
        
        static final private String OPTICS_TAG = "optics_source";
        static final private String OPTICS_URL_KEY = "url";
        static final private String OPTICS_NAME_KEY = "name";
        static final private String OPTICS_NAME = "optics";
        static final private String OPTICS_EXTRA_TAG = "optics_extra";
        
		static final private String HARDWARE_STATUS_TAG = "hardware_status";
		
		static final private String TIMING_TAG = "timing_source";
		static final private String TIMING_URL_KEY = "url";
		static final private String TIMING_NAME_KEY = "name";
		
		static final private String DEVICEMAPPING_TAG = "deviceMapping_source";
		static final private String DEVICEMAPPING_URL_KEY = "url";
		
		static final private String MODELCONFIG_TAG = "modelConfig_source";
		static final private String MODELCONFIG_URL_KEY = "url";
		
        static final private String TABLE_GROUP_TAG = "tablegroup_source";
        static final private String TABLE_GROUP_KEY = "name";
        static final private String TABLE_GROUP_URL_KEY = "url";
        
        
        protected URL mainUrl;   /** URL of main XML source */
		
		
		/** Constructor */
        public MainManager( final String urlSpec ) {
            setMainUrlSpec( urlSpec );
            try {
                refresh();
            }
            catch( XmlDataAdaptor.ResourceNotFoundException exception ) {
                // if the file doesn't exist, don't load it
                System.err.println( exception );
                exception.printStackTrace();
            }
        }

        
		/** get the main URL */
        public URL mainUrl() {
            return mainUrl;
        }
        
        
		/** get the main URL spec */
        public String mainUrlSpec() {
            return mainUrl.toString();
        }

		
		/** set the main URL spec */
        public void setMainUrlSpec( final String urlSpec ) {
            try {
                mainUrl = new URL( urlSpec );
            }
            catch( MalformedURLException excpt ) {
                System.err.println( excpt );
                excpt.printStackTrace();
            }
        }
        
        
        /**  Sample XML input file to parse
         * <sources>
         *   <optics name="optics" url="file:./sns_mebt.xml"/>
         *   <tablegroup name="twiss_bpm" url="file:./twiss_bpm.xml"/>
         * </sources>
         */
        public void refresh() {
            final String mainUrlSpec = mainUrlSpec();
            final DataAdaptor mainAdaptor = XmlDataAdaptor.adaptorForUrl( mainUrlSpec, false );
            final DataAdaptor sourcesAdaptor = mainAdaptor.childAdaptor( SOURCE_TAG );

            // fetch the optics reference
            final DataAdaptor opticsAdaptor = sourcesAdaptor.childAdaptor( OPTICS_TAG );
            final String opticsUrl = opticsAdaptor.stringValue( OPTICS_URL_KEY );
            acceleratorManager.setOpticsUrlSpec( opticsUrl );
            
            // fetch the optics extra references
            final List<DataAdaptor> extraAdaptors = sourcesAdaptor.childAdaptors( OPTICS_EXTRA_TAG );
            for( final DataAdaptor extraAdaptor : extraAdaptors ) {
                final String extraUrl = extraAdaptor.stringValue( OPTICS_URL_KEY );
                acceleratorManager.addExtraUrlSpec( extraUrl );
            }
			
			// fetch the hardware status reference
			final DataAdaptor hardwareStatusRefAdaptor = sourcesAdaptor.childAdaptor( HARDWARE_STATUS_TAG );
			if ( hardwareStatusRefAdaptor != null ) {
                final String hardwareStatusURLSpec = hardwareStatusRefAdaptor.stringValue( OPTICS_URL_KEY );
                acceleratorManager.setHardwareStatusURLSpec( hardwareStatusURLSpec );
			}
            
            // fetch the timing reference
            final DataAdaptor timingReferenceAdaptor = sourcesAdaptor.childAdaptor( TIMING_TAG );
			if ( timingReferenceAdaptor != null ) {
				final String timingRelativeURL = timingReferenceAdaptor.stringValue( TIMING_URL_KEY );
				
				try {
					final URL timingURL = new URL( mainUrl, timingRelativeURL );
					_timingManager.setURLSpec( timingURL.toString() );
				}
				catch( MalformedURLException excpt ) {
					System.err.println( excpt );
					excpt.printStackTrace();
				}
			}
			
			// fetch the device mapping
            final DataAdaptor deviceMappingReferenceAdaptor = sourcesAdaptor.childAdaptor( DEVICEMAPPING_TAG );
			if ( deviceMappingReferenceAdaptor != null ) {
				final String deviceMappingURL = deviceMappingReferenceAdaptor.stringValue( DEVICEMAPPING_URL_KEY );
				
				try {
					final URL deviceURL = new URL( mainUrl, deviceMappingURL );
					DEVICE_MANAGER.setURL( deviceURL );
				}
				catch ( MalformedURLException excpt ){
					System.err.println( excpt );
					excpt.printStackTrace();
				}
			}
			
			// fetch the model configuration
			final DataAdaptor daModelConfig = sourcesAdaptor.childAdaptor( MODELCONFIG_TAG );
			if ( daModelConfig != null ) {
			    final String strUrlModelCfg = daModelConfig.stringValue( MODELCONFIG_URL_KEY );

			    try {
			        final URL urlModelConfig = new URL( mainUrl, strUrlModelCfg );
			        ldrModelConfig.setURL( urlModelConfig );
			    }
			    catch ( MalformedURLException excpt ){
			        System.err.println( excpt );
			        excpt.printStackTrace();
			    }
			}

            
            // fetch the table group references
            final List<DataAdaptor> tableAdaptors = sourcesAdaptor.childAdaptors( TABLE_GROUP_TAG );
            tableManager.clear();
            for( final DataAdaptor tableAdaptor : tableAdaptors ) { 
                final String tableGroup = tableAdaptor.stringValue( TABLE_GROUP_KEY );
                final String tableGroupUrl = tableAdaptor.stringValue( TABLE_GROUP_URL_KEY );

                tableManager.setUrlSpecForTableGroup( tableGroupUrl, tableGroup );
            }
        }
    
    	
		/** write the optics file */
        public void write() {
            XmlDataAdaptor docAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();

            DataAdaptor sourceAdaptor = docAdaptor.createChild( SOURCE_TAG );

            writeOpticsRef( sourceAdaptor );
            writeTableGroupRefs( sourceAdaptor );

            docAdaptor.writeToUrl( mainUrl );
        }
        
        
		/** write the optics reference */
        private void writeOpticsRef( final DataAdaptor parentAdaptor ) {
            DataAdaptor adaptor = parentAdaptor.createChild( OPTICS_TAG );

            adaptor.setValue( OPTICS_NAME_KEY, OPTICS_NAME );

            String opticsUrlSpec = opticsUrlSpec();
            adaptor.setValue( OPTICS_URL_KEY, opticsUrlSpec );
        }
        
        
		/** write the table group references */
        private void writeTableGroupRefs( final DataAdaptor parentAdaptor ) {
            Collection tableGroups = getTableGroups();
            
            Iterator tableGroupIter = tableGroups.iterator();
            while ( tableGroupIter.hasNext() ) {
                String tableGroup = (String)tableGroupIter.next();
                String tableGroupUrl = urlSpecForTableGroup( tableGroup );
                DataAdaptor adaptor = parentAdaptor.createChild( TABLE_GROUP_TAG );
                
                adaptor.setValue( TABLE_GROUP_KEY, tableGroup );
                adaptor.setValue( TABLE_GROUP_URL_KEY, tableGroupUrl );
            }
        }
    }
    
    
    
    /************************************************************************
     * Handle read/write for the Accelerator XML source (the optics file).
     */
    private class AcceleratorManager {
        private String dtdUrlSpec;
        private String opticsUrlSpec;
        private List<String> extraUrlSpecs;
		private String _hardwareStatusURLSpec;
        
        
		/** Constructor */
        public AcceleratorManager() {
            dtdUrlSpec = "xdxf.dtd";     // default DTD file
            extraUrlSpecs = new ArrayList<String>();
			_hardwareStatusURLSpec = null;
        }
        
        
		/** get the optics URL spec */
        public String opticsUrlSpec() {
            return opticsUrlSpec;
        }
        
        
        /** 
		 * Get absolute URL specifications given a URL spec relative to the main URL
		 * @return absolute URL specification
		 */
        public String absoluteUrlSpec( final String urlSpec ) {
            URL mainUrl = mainUrl();
            URL absoluteUrl = null;
            
            try {
                absoluteUrl = new URL( mainUrl, urlSpec );
            }
            catch( MalformedURLException excpt ) {
                System.err.println( excpt );
                excpt.printStackTrace();
            }
            
            return absoluteUrl.toString();
        }


		/** set the optics URL spec */
        public void setOpticsUrlSpec( final String urlSpec ) {
            opticsUrlSpec = urlSpec;
        }
        
        
		/** get the DTD URL spec */
        public String dtdUrlSpec() {
            return dtdUrlSpec;
        }
        
        
		/** set the URL of the DTD */
        public void setDtdUrlSpec( final String urlSpec ) {
            dtdUrlSpec = urlSpec;
        }

        
		/** add an extra optics URL spec */
        public void addExtraUrlSpec( final String urlSpec ) {
            extraUrlSpecs.add( urlSpec );
        }
		
		
		/** get the hardware status URL spec */
		public String getHardwareStatusURLSpec() {
			return _hardwareStatusURLSpec;
		}
		
		
		/** set the hardware status URL spec */
		public void setHardwareStatusURLSpec( final String urlSpec ) {
			_hardwareStatusURLSpec = urlSpec;
		}
        
        
        /** Parse the accelerator from the optics URL with the specified DTD validation flag */
        public Accelerator getAccelerator( final boolean isValidating ) throws XmlDataAdaptor.ParseException {
            String absoluteUrlSpec = absoluteUrlSpec( opticsUrlSpec );
            XmlDataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl( absoluteUrlSpec, isValidating );
			
            Document document = adaptor.document();
            DocumentType docType = document.getDoctype();
            String acceleratorTag = docType.getName();
			
            dtdUrlSpec = docType.getSystemId();
            
            DataAdaptor accelAdaptor = adaptor.childAdaptor( acceleratorTag );
            Accelerator accelerator = new Accelerator();
			
			accelerator.setNodeFactory( DEVICE_MANAGER.getNodeFactory() );
			
			accelerator.setModelConfiguration( ldrModelConfig.getModelConfiguration() );
			
			EditContext editContext = tableManager.readEditContext( isValidating );
			accelerator.setEditContext( editContext );
			
			accelerator.setTimingCenter( getTimingCenter() );
			
            accelerator.update( accelAdaptor );
            
            loadExtraOptics( accelerator, isValidating );
			
			loadHardwareStatus( accelerator, isValidating );
            
            return accelerator;
        }
        
        
        /** load extra optics files if any */
        protected void loadExtraOptics( final Accelerator accelerator, final boolean isValidating ) {
            Iterator extraOpticsIter = extraUrlSpecs.iterator();
            while ( extraOpticsIter.hasNext() ) {
                String urlSpec = (String)extraOpticsIter.next();
                updateAccelerator( urlSpec, accelerator, isValidating );
            }
        }
        
        
        /** load hardware status if any */
        protected void loadHardwareStatus( final Accelerator accelerator, final boolean isValidating ) {
			if ( _hardwareStatusURLSpec != null ) {
				updateAccelerator( _hardwareStatusURLSpec, accelerator, isValidating );
			}
        }
		

        /** update the accelerator with data from the optics URL with a DTD validation flag */
        public void updateAccelerator( final Accelerator accelerator, final boolean isValidating ) throws XmlDataAdaptor.ParseException {
            updateAccelerator(opticsUrlSpec, accelerator, isValidating);
            loadExtraOptics(accelerator, isValidating);
        }    


        /** update the accelerator with data from the optics URL with a DTD validation flag */
        public void updateAccelerator( final String urlSpec, final Accelerator accelerator, final boolean isValidating ) throws XmlDataAdaptor.ParseException {
            String absoluteUrlSpec = absoluteUrlSpec( urlSpec );
            XmlDataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl( absoluteUrlSpec, isValidating );
            
            String acceleratorTag = accelerator.dataLabel();
            
            DataAdaptor accelAdaptor = adaptor.childAdaptor( acceleratorTag );
            accelerator.update( accelAdaptor ); 
        }
        
        
		/** write the accelerator out to the optics file */
        public void write( final Accelerator accelerator ) throws XmlDataAdaptor.WriteException, XmlDataAdaptor.CreationException {
            String absoluteUrlSpec = absoluteUrlSpec( opticsUrlSpec );
            XmlDataAdaptor adaptor = XmlDataAdaptor.newDocumentAdaptor( accelerator, dtdUrlSpec );
            adaptor.writeToUrlSpec( absoluteUrlSpec );
        }
    }
	
    
	/** Bind device types to AcceleratorNode subclasses */
    private class DeviceManager {
    	private static final String DEVICE_TAG = "device";
		
		public static final String DEVICE_MAPPING = "deviceMapping";
    	
    	final private HashMap<String, String> _deviceMap;
		
		/** factory for generating accelerator nodes */
		private AcceleratorNodeFactory _nodeFactory;
    	
		
		/** Constructor */
    	public DeviceManager() {
    		_deviceMap = new HashMap<String, String>();
    	}
		
		
		/** get the accelerator node factory */
		public AcceleratorNodeFactory getNodeFactory() {
			return _nodeFactory;
		}
    	
		
//    	public Map<String, String> getDeviceMap() {
//    		return _deviceMap;
//    	}
		
    	public void setURL( final URL url ) {
    		final XmlDataAdaptor deviceMappingDocumentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			final DataAdaptor deviceMappingAdaptor = deviceMappingDocumentAdaptor.childAdaptor( DeviceManager.DEVICE_MAPPING );
			
			final List<DataAdaptor> deviceAdaptors = deviceMappingAdaptor.childAdaptors( DEVICE_TAG );
			
			final AcceleratorNodeFactory nodeFactory = new AcceleratorNodeFactory();
			for ( final DataAdaptor deviceAdaptor : deviceAdaptors ) {
				try {
					final String deviceType = deviceAdaptor.stringValue( "type" );
					final String softType = deviceAdaptor.hasAttribute( "softType" ) ? deviceAdaptor.stringValue( "softType" ) : null;
					final String deviceClassName = deviceAdaptor.stringValue( "class" );
					final Class<?> deviceClass = Class.forName( deviceClassName );
					nodeFactory.registerNodeClass( deviceType, softType, deviceClass );
					_deviceMap.put( deviceType, deviceClassName );
				}
				catch( ClassNotFoundException exception ) {
					exception.printStackTrace();
				}
			}
			_nodeFactory = nodeFactory;
    	}
    }
	
    /**
     * Loads the model configuration description from a given
     * URL specifying the configuration file (see <code>{@link #setURL(URL)}</code>.  
     * The configuration information is encapsulated as a 
     * <code>{@link ModelConfiguration}</code> object.  This object
     * is instantiated then attached to the SMF accelerator object.
     *
     * @author Christopher K. Allen
     * @since   May 24, 2011
     */
    private class ModelConfigLoader {
        
        /*
         * Local Attributes
         */
        
        /** The (global) model configuration manager */
        public ModelConfiguration       mgrMdlCfg;
        
        
        /*
         * Initialization
         */
        
        /**
         * Creates a new <code>ModelConfigLoader</code> model
         * configuration loading class.
         *
         * @author  Christopher K. Allen
         * @since   May 24, 2011
         */
        public ModelConfigLoader() {
        }
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the online model configuration manager, presumably after it 
         * has been loaded with the call to <code>{@link #setURL(URL)}</code>.
         *
         * @return  online model configuration manager
         *
         * @author Christopher K. Allen
         * @since  May 24, 2011
         */
        public ModelConfiguration   getModelConfiguration() {
            return this.mgrMdlCfg;
        }
        
        /**
         * Loads the the model configuration information from the file with
         * the given URL.  The model configuration information is encapsulated
         * in a class object <code>{@link ModelConfiguration}</code> which may
         * be recover with the method <code>{@link #getModelConfiguration()}</code>.
         *
         * @param url   model configuration file location
         *
         * @author Christopher K. Allen
         * @since  May 24, 2011
         */
        public void setURL( final URL url ) {
            
            try {
                this.mgrMdlCfg = new ModelConfiguration(url);
                
            } catch (ResourceNotFoundException e) {
                System.err.println("Unable to find model configuration file: " + url);
                e.printStackTrace();
                
            } catch (ParseException e) {
                System.err.println("Unable to find model configuration file: " + url);
                e.printStackTrace();

            } catch (ClassNotFoundException e) {
                System.err.println("Unknown class in model configuration file: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
    }
    
    /***************************************************************************
	 * Handle read/write for the Table XML sources (dynamic table files). Each
	 * file may consist of one or more tables associated with a single table
	 * group. Multiple files may be loaded and the set of tables is the union of
	 * tables found in all files (ie. groups). Each table must have a unique
	 * name. The grouping of tables within the same file is merely a convenience
	 * and has no other functionality. The group names are specified in the the
	 * main reference file. For each table, read/write the schema and the
	 * records.
	 */
    private class TableManager {
        protected Map<String, String> tableGroupUrlMap;  /** Map of url associated with group */
        
        public TableManager() {
            tableGroupUrlMap = new HashMap<String, String>();
        }
    
    
        /** get the url associated with the specified group */
        public String urlSpecForTableGroup(String tableGroup) {
            return tableGroupUrlMap.get(tableGroup);
        }


        /** From now on, associate the specified url with the specified group */
        public void setUrlSpecForTableGroup(String urlSpec, String tableGroupName) {
            tableGroupUrlMap.put(tableGroupName, urlSpec);
        }
        
        
        /** 
         * URL specifications are relative to the main URL so return the 
         * absolute URL specification given that the table group URL is relative
         * to the main URL.
         */
        public String absoluteUrlSpecForTableGroup(String tableGroup) throws MissingUrlForGroup {
            URL mainUrl = mainUrl();
            URL absoluteUrl = null;
            String groupUrlSpec = urlSpecForTableGroup(tableGroup);
            
            if ( groupUrlSpec == null ) {
                throw new MissingUrlForGroup(tableGroup);
            }
            
            try {
                absoluteUrl = new URL(mainUrl, groupUrlSpec);
            }
            catch(MalformedURLException excpt) {
                System.err.println(excpt);
                excpt.printStackTrace();
            }
            
            return absoluteUrl.toString();
        }
        
        
        /** Return the collection of all tables registered with a URL */
        public Collection getTableGroups() {
            return tableGroupUrlMap.keySet();
        }
        
        
        /** Clear all memory of tables and groups */
        protected void clear() {
            tableGroupUrlMap.clear();
        }
        
        
        /** For each table group, read the associated XML files to get the tables */
        public EditContext readEditContext(boolean isValidating) {
            EditContext editContext = new EditContext();
            Collection groups = getTableGroups();
            Iterator groupIter = groups.iterator();
            
            while ( groupIter.hasNext() ) {
                String group = (String)groupIter.next();
                readTableGroup(editContext, group, isValidating);
            }
            
            return editContext;
        }
        
        
        /** For each table group, read the associated XML files to get the tables */
        public EditContext readEditContext() {
            return readEditContext(false);
        }
        
        
		/**
		 *
		 */
        public void readTableGroup(EditContext editContext, String tableGroup) {
            readTableGroup(editContext, tableGroup, false);
        }
        
        
		/**
		 *
		 */
        public void readTableGroup(EditContext editContext, String tableGroup, boolean isValidating) {
            String urlSpec = "";
            try {
                urlSpec = absoluteUrlSpecForTableGroup(tableGroup);
                XmlTableIO.readTableGroupFromUrl(editContext, tableGroup, urlSpec, isValidating);
            }
            catch(XmlDataAdaptor.ResourceNotFoundException excpt) {
                System.err.println("The group: \"" + tableGroup + "\" could not be loaded due to a missing resource: " +  urlSpec);
            }
            catch(XmlDataAdaptor.ParseException excpt) {
                System.err.println("The group: \"" + tableGroup + "\" could not be loaded due to parse exception: " + excpt.getMessage());
            }            
            catch(Exception excpt) {
                System.err.println("The group: \"" + tableGroup + "\" could not be loaded due to exception: " + excpt.getMessage());
            }            
        }
        
        
        /** Write all table groups to XML */
        public void writeEditContext(EditContext editContext) {
            Collection tableGroups = editContext.getTableGroups();
            Iterator groupIter = tableGroups.iterator();
            
            while ( groupIter.hasNext() ) {
                String tableGroup = (String)groupIter.next();
                writeTableGroup(editContext, tableGroup);
            }
        }
        
        
        /** Write all tables associated with this group to XML */
        public void writeTableGroup(EditContext editContext, String group) {
            try {
                String urlSpec = absoluteUrlSpecForTableGroup(group);
                XmlTableIO.writeTableGroupToUrl(editContext, group, urlSpec);
            }
            catch ( MissingUrlForGroup excpt ) {
                System.err.println("Due to unspecified URL, will skip writing group: " + group);
            }
        }
    }
    

    
    /** 
     *  Exception thrown when a URL has not been specified for the given group and an attempt is made to read or write the group.
     */
    protected class MissingUrlForGroup extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        public MissingUrlForGroup(String note) {
            super(note);
        }
    }
}

