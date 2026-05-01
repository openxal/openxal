/*
 * XMLDataManager.java
 *
 * Created on June 4, 2002, 12:49 PM
 */

package xal.smf.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import xal.ca.ChannelFactory;
import xal.sim.scenario.DefaultElementMapping;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.FileBasedElementMapping;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorNodeFactory;
import xal.smf.TimingCenter;
import xal.tools.URLUtil;
import xal.tools.data.DataAdaptor;
import xal.tools.data.EditContext;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlTableIO;


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

	/** manage the timing center */
	final private TimingDataManager TIMING_MANAGER;

    private MainManager mainManager;
    private AcceleratorManager acceleratorManager;
    private TableManager tableManager;
	private ElementMapping elementMapping;
	
	
    /** Primary Constructor */
    public XMLDataManager( final String urlPath, final ChannelFactory channelFactory ) {
		DEVICE_MANAGER = new DeviceManager( channelFactory );
		TIMING_MANAGER = new TimingDataManager( channelFactory );
        acceleratorManager = new AcceleratorManager( channelFactory );
        tableManager = new TableManager();
        mainManager = new MainManager( urlPath );
        try {
            mainManager.refresh();
        }
        catch( XmlDataAdaptor.ResourceNotFoundException exception ) {
            // if the file doesn't exist, don't load it
            System.err.println( exception );
            exception.printStackTrace();
        }
    }


	/** Constructor */
	public XMLDataManager( final String urlPath ) {
		this( urlPath, ChannelFactory.defaultFactory() );
	}


	/** factory method given a URL to the main optics source */
	static public XMLDataManager getInstance( final URL url ) {
		return new XMLDataManager( url.toString() );
	}
    
    
	/**
	 * Create and return a new XMLDataManager with its source given by the specified file path.
	 * @param filePath The file path of the accelerator data source.
	 * @param channelFactory the channel factory for generating channels within the accelerator (nodes, timing, etc.)
	 * @return The new XMLDataManager
	 */
    static public XMLDataManager managerWithFilePath( final String filePath, final ChannelFactory channelFactory ) throws URLUtil.FilePathException {
        final String urlSpec = URLUtil.urlSpecForFilePath( filePath );
        return new XMLDataManager( urlSpec, channelFactory );
    }


	/**
	 * Create and return a new XMLDataManager with its source given by the specified file path.
	 * @param filePath The file path of the accelerator data source.
	 * @return The new XMLDataManager
	 */
	static public XMLDataManager managerWithFilePath( final String filePath ) throws URLUtil.FilePathException {
		return managerWithFilePath( filePath, ChannelFactory.defaultFactory() );
	}


	/**
	 * Get the default XMLDataManager based on the user's preferred path to the Main data source
	 * @return A new instance of the XMLDataManager with the user's preferred data source or null if no default has been specified
	 */
	static public XMLDataManager getDefaultInstance() {
        String path = defaultPath();
		return (path != null) ? new XMLDataManager(URLUtil.urlSpecForFilePath(path)) : null;
	}
    
    
    /**
	 * Read the timing center from the timing URL.
	 * @return a timing center read from the timing URL.
	 * @throws xal.tools.xml.XmlDataAdaptor.ParseException if the TimingCenter cannot be generated
     */    
    public TimingCenter getTimingCenter() throws XmlDataAdaptor.ParseException {
        return TIMING_MANAGER.getTimingCenter();
    }
    
    
	/**
	 * Read the accelerator from the data source at the URL path.
	 * @param urlPath The URL spec of the data source.
	 * @param channelFactory the channel factory from which items (e.g. nodes, timing center, etc.) generate their channels
	 * @return the new accelerator read from the data source.
	 */
    static public Accelerator acceleratorWithUrlSpec( final String urlPath, final ChannelFactory channelFactory ) {
        final XMLDataManager dataManager = new XMLDataManager( urlPath, channelFactory );
        return dataManager.getAccelerator();
    }


	/**
	 * Read the accelerator from the data source at the URL path.
	 * @param urlPath The URL spec of the data source.
	 * @return the new accelerator read from the data source.
	 */
	static public Accelerator acceleratorWithUrlSpec( final String urlPath ) {
		return acceleratorWithUrlSpec( urlPath, ChannelFactory.defaultFactory() );
	}


	/**
	 * Read the accelerator from the data source at the file path.
	 * @param filePath The file path of the data source.
	 * @param channelFactory the channel factory from which items (e.g. nodes, timing center, etc.) generate their channels
	 * @return the new accelerator read from the data source.
	 */
    static public Accelerator acceleratorWithPath( final String filePath, ChannelFactory channelFactory ) throws URLUtil.FilePathException {
        final XMLDataManager dataManager = managerWithFilePath( filePath, channelFactory );
        return dataManager.getAccelerator();
    }


	/**
	 * Read the accelerator from the data source at the file path.
	 * @param filePath The file path of the data source.
	 * @return the new accelerator read from the data source.
	 */
	static public Accelerator acceleratorWithPath( final String filePath ) throws URLUtil.FilePathException {
		return acceleratorWithPath( filePath, ChannelFactory.defaultFactory() );
	}


	/**
	 * Read the accelerator from the data source at the file path and using DTD validation if
	 * if the user specifies.
	 * @param filePath The file path of the data source.
	 * @param isValidating enable DTD validation if true and disable DTD validation if false
	 * @return the new accelerator read from the data source
	 */
    static public Accelerator acceleratorWithPath( final String filePath, final boolean isValidating ) throws URLUtil.FilePathException {
        XMLDataManager dataManager = managerWithFilePath(filePath);
        return dataManager.getAccelerator(isValidating);
    }
    
    
	/**
	 * Load the accelerator corresponding to the default accelerator data source specified in the user's preferences.
	 * @return the accelerator built from the default data source or null if no default accelerator is specified
	 */
    static public Accelerator loadDefaultAccelerator() {
		return loadDefaultAccelerator( ChannelFactory.defaultFactory() );
    }


	/**
	 * Load the accelerator corresponding to the default accelerator data source specified in the user's preferences.
	 * @param channelFactory the channel factory to use to generate the channels for the accelerator
	 * @return the accelerator built from the default data source or null if no default accelerator is specified
	 */
	static public Accelerator loadDefaultAccelerator( final ChannelFactory channelFactory ) {
		String path = defaultPath();
		return (path != null) ? acceleratorWithUrlSpec( URLUtil.urlSpecForFilePath(path), channelFactory ) : null;
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
    public void setMainPath(String filePath) throws URLUtil.FilePathException {
        String urlSpec = URLUtil.urlSpecForFilePath(filePath);
        
        setMainUrlSpec(urlSpec);
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
    public Collection<String> getTableGroups() {
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
		static final private String CURRENT_VERSION = "2.0";

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
		
		static final private String MODELCONFIG_TAG = "modelElementConfig_source";
		static final private String MODELCONFIG_URL_KEY = "url";
		
        static final private String TABLE_GROUP_TAG = "tablegroup_source";
        static final private String TABLE_GROUP_KEY = "name";
        static final private String TABLE_GROUP_URL_KEY = "url";
        
        
        protected URL mainUrl;   /** URL of main XML source */
		protected String mainSchema = "/xal/schemas/main.xsd";
		
		/** Constructor */
        public MainManager( final String urlSpec ) {
            setMainUrlSpec( urlSpec );
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
            final DataAdaptor mainAdaptor = XmlDataAdaptor.adaptorForUrl( mainUrlSpec, false,  mainSchema );
            final DataAdaptor sourcesAdaptor = mainAdaptor.childAdaptor( SOURCE_TAG );

			if ( sourcesAdaptor.hasAttribute( "version" ) ) {
				final String version = sourcesAdaptor.stringValue( "version" );
				if ( !version.trim().equals( CURRENT_VERSION	) ) {
					throw new OpticsVersionException( "The optics file, \"" + mainUrlSpec + "\" has an unsupported version: \"" + version + "\". The supported optics format version is \"" + CURRENT_VERSION + "\"." );
				}
			}
			else {
				throw new OpticsVersionException( "The optics file, \"" + mainUrlSpec + "\" specifies no version. The supported optics format version is \"" + CURRENT_VERSION + "\"." );
			}

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
				final String timingURL = absoluteUrlSpec( timingRelativeURL );
				TIMING_MANAGER.setURLSpec( timingURL, acceleratorManager.xdxfSchema );				
			}
			
			// fetch the device mapping
            final DataAdaptor deviceMappingReferenceAdaptor = sourcesAdaptor.childAdaptor( DEVICEMAPPING_TAG );
			if ( deviceMappingReferenceAdaptor != null ) {
				final String deviceMappingURL = deviceMappingReferenceAdaptor.stringValue( DEVICEMAPPING_URL_KEY );
				DEVICE_MANAGER.setURL(absoluteUrlSpec( deviceMappingURL ));				
			}
			
			// fetch the model configuration
			final DataAdaptor daModelConfig = sourcesAdaptor.childAdaptor( MODELCONFIG_TAG );
			if ( daModelConfig != null ) {
			    final String strUrlModelCfg = daModelConfig.stringValue( MODELCONFIG_URL_KEY );
				final String urlModelConfig = absoluteUrlSpec( strUrlModelCfg );
				elementMapping = FileBasedElementMapping.loadFrom(urlModelConfig, FileBasedElementMapping.elementMappingSchema );
			} else {
				elementMapping = DefaultElementMapping.getInstance();
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
            final Collection<String> tableGroups = getTableGroups();

			for ( final String tableGroup : tableGroups ) {
                final String tableGroupUrl = urlSpecForTableGroup( tableGroup );
                final DataAdaptor adaptor = parentAdaptor.createChild( TABLE_GROUP_TAG );
                
                adaptor.setValue( TABLE_GROUP_KEY, tableGroup );
                adaptor.setValue( TABLE_GROUP_URL_KEY, tableGroupUrl );
            }
        }
    }
    
    
    
    /************************************************************************
     * Handle read/write for the Accelerator XML source (the optics file).
     */
    private class AcceleratorManager {
		private final ChannelFactory CHANNEL_FACTORY;
        private String dtdUrlSpec;
        private String opticsUrlSpec;
        private List<String> extraUrlSpecs;
		private String _hardwareStatusURLSpec;
		private String xdxfSchema;
        public static final String acceleratorTag = "xdxf";
        
		/** Constructor */
        public AcceleratorManager( final ChannelFactory channelFactory ) {
			CHANNEL_FACTORY = channelFactory;
            dtdUrlSpec = "xdxf.dtd";     // default DTD file
            extraUrlSpecs = new ArrayList<String>();
			_hardwareStatusURLSpec = null;			
			xdxfSchema = "/xal/schemas/xdxf.xsd";
        }
        
        
		/** get the optics URL spec */
        public String opticsUrlSpec() {
            return opticsUrlSpec;
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
            XmlDataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl( absoluteUrlSpec, isValidating, xdxfSchema );
			
            Document document = adaptor.document();
            DocumentType docType = document.getDoctype();            
			if (docType != null) dtdUrlSpec = docType.getSystemId();                        
            
            DataAdaptor accelAdaptor = adaptor.childAdaptor( acceleratorTag );
            Accelerator accelerator = new Accelerator( CHANNEL_FACTORY );
			
			accelerator.setNodeFactory( DEVICE_MANAGER.getNodeFactory() );
			
			accelerator.setElementMapping(elementMapping);
			
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
			for ( final String urlSpec : extraUrlSpecs ) {
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
            XmlDataAdaptor adaptor = XmlDataAdaptor.adaptorForUrl( absoluteUrlSpec, isValidating,  xdxfSchema );
            
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
    	
		private final String deviceMappingSchema = "/xal/schemas/impl.xsd";
		
    	final private HashMap<String, String> _deviceMap;
		
		/** factory for generating accelerator nodes */
		final private AcceleratorNodeFactory NODE_FACTORY;
    	
		
		/** Constructor */
    	public DeviceManager( final ChannelFactory channelFactory ) {
			NODE_FACTORY = new AcceleratorNodeFactory( channelFactory );
    		_deviceMap = new HashMap<String, String>();
    	}
		
		
		/** get the accelerator node factory */
		public AcceleratorNodeFactory getNodeFactory() {
			return NODE_FACTORY;
		}
    	
		
//    	public Map<String, String> getDeviceMap() {
//    		return _deviceMap;
//    	}
		
    	public void setURL( final String url ) {
    		final XmlDataAdaptor deviceMappingDocumentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false, deviceMappingSchema );
			final DataAdaptor deviceMappingAdaptor = deviceMappingDocumentAdaptor.childAdaptor( DeviceManager.DEVICE_MAPPING );
			
			final List<DataAdaptor> deviceAdaptors = deviceMappingAdaptor.childAdaptors( DEVICE_TAG );
			
			for ( final DataAdaptor deviceAdaptor : deviceAdaptors ) {
				try {
					final String deviceType = deviceAdaptor.stringValue( "type" );
					final String softType = deviceAdaptor.hasAttribute( "softType" ) ? deviceAdaptor.stringValue( "softType" ) : null;
					final String deviceClassName = deviceAdaptor.stringValue( "class" );
					@SuppressWarnings("unchecked")	// cast to AcceleratorNode class
					final Class<AcceleratorNode> deviceClass = (Class<AcceleratorNode>)Class.forName( deviceClassName );
					NODE_FACTORY.registerNodeClass( deviceType, softType, deviceClass );
					_deviceMap.put( deviceType, deviceClassName );
				}
				catch( ClassNotFoundException exception ) {
					exception.printStackTrace();
				}
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
        public Collection<String> getTableGroups() {
            return tableGroupUrlMap.keySet();
        }
        
        
        /** Clear all memory of tables and groups */
        protected void clear() {
            tableGroupUrlMap.clear();
        }
        
        
        /** For each table group, read the associated XML files to get the tables */
        public EditContext readEditContext(boolean isValidating) {
            final EditContext editContext = new EditContext();
            final Collection<String> groups = getTableGroups();

			for ( final String group : groups ) {
                readTableGroup( editContext, group, isValidating );
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
            final Collection<String> tableGroups = editContext.getTableGroups();
			for ( final String tableGroup : tableGroups ) {
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

