/**
 * AppProperties.java
 *
 *  Created	: Jun 17, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.rscmgt;


import xal.app.pta.MainApplication;
import xal.app.pta.tools.property.PropertiesManager;
import xal.app.pta.tools.property.Property;

import java.io.IOException;
import java.util.Properties;

/**
 * <h2>AppProperties</h2>
 * <p>
 * Manages the configuration properties for the application.  The default 
 * properties, i.e. the properties used upon application installation or 
 * a <code>Preferences</code> node corruption,
 * are keep in the properties file indicated by the constant
 * <code>{@link #STR_FILE_DEFAULT}</code>.  The application configuration
 * attributes are available programmatically using the enumerations
 * internal to this class.  They return a <code>PropertyValue</code>
 * object which provides methods for type conversion.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 *
 * @since  Jun 17, 2009
 * @author Christopher K. Allen
 * 
 * @see AppProperties#STR_FILE_DEFAULT
 * @see xal.app.pta.tools.property.Property
 * @see xal.app.pta.tools.property.Property.IProperty
 */
public final class AppProperties extends PropertiesManager  {

    
    /*
     * Class Constants
     */
    
    
    /**  
     * The name of the properties file containing the default values of for
     * the application configuration properties.
     */
    public static final String  STR_FILE_DEFAULT = "DefaultApp.properties"; //$NON-NLS-1$
    
    
    
    /*
     * Class Operations
     */
    
    
    /**
     * Provides access to the singleton application properties
     * manager.  
     *
     * @return  The single instance of the application properties manager 
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public static synchronized AppProperties getInstance() {
        return MGR_APP_PROPS;
    }
    
    
    
    /*
     * Application Properties
     */
    
    /**
     * <p>
     * Enumeration of the global configuration properties for the 
     * application.
     * </p>
     * <p>
     * The enumeration class initializes by loading the application
     * configuration file and creating the attribute dictionary.
     * Application property values are accessible from this
     * enumeration via the <tt>get<b>Type</b></tt> methods where
     * <tt><b>Type</b></tt> refers to the Java type requested
     * (i.e., <code>String</code>, <code>int</code>, etc).
     * Each value access method defers to the application properties
     * dictionary converting the string value to the
     * requested type. 
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Jun 11, 2009
     *
     */
    public enum APP implements Property.IProperty {
    
        /** The application name  */
        NAME("AppName"), //$NON-NLS-1$
        
        /** The size of each measurement set    */
        FACILITY("AppInstallation"), //$NON-NLS-1$
        
        /** Data tag (and file extension) for application persistent data  */
        TAG_DATA("AppDataLabel"), //$NON-NLS-1$
        
        /** Data tag (and file extension) for wire scanner device persistent configuration */
        TAG_DEV_CFG("AppConfigLabel"),
        
        /** Any icon associated with the application */
        ICON("AppIcon"), //$NON-NLS-1$
        
        /**  The default font size used by application */
        FONTSZ("AppFontSize"), //$NON-NLS-1$
        
        /** The default font used by the application */
        FONTTYPE("AppFontType"), //$NON-NLS-1$
        
        /** The value of position axis left end point  */
        SCR_WIDTH("AppScrWidth"), //$NON-NLS-1$
        
        /** Length between position locations */
        SCR_HEIGHT("AppScrHeight"); //$NON-NLS-1$
        

        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  This
         * object can then be used to convert the 
         * raw (string) value of the property value
         * into the proper type.
         * 
         * @return      value of the property corresponding to
         *              this enumeration constant
         *  
         * @since 	Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.prpVal;
        }

        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  prpVal;

        
        /**
         * <p> 
         * Constructs the application properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * @see        APP#STR_FILE_DEFAULT
         */
        @SuppressWarnings("synthetic-access")
        private APP(String strPropName) {
            this.prpVal = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of the bug report configuration
     * parameters for the application.
     *
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public enum BGRPRT implements Property.IProperty {
    
        /** Application bug report file location */
        FILE("BgRprtFile"), //$NON-NLS-1$
        
        /** Text editor character set */
        CHARSET("BgRprtEditorCharset"), //$NON-NLS-1$
        
        /** General text editor screen width */
        SCR_WD("BgRprtEditorScrWd"), //$NON-NLS-1$
        
        /** General text editor screen height */
        SCR_HT("BgRprtEditorScrHt"); //$NON-NLS-1$
    
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  This
         * object can then be used to convert the 
         * raw (string) value of the property value
         * into the proper type.
         * 
         * @return      value of the property corresponding to
         *              this enumeration constant
         *  
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.prpVal;
        }
    
        
        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property          prpVal;
    
        
        /**
         * <p> 
         * Constructs the bug report properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * 
         * @see        APP#STR_FILE_DEFAULT
         */
        @SuppressWarnings("synthetic-access")
        private BGRPRT(String strPropName) {
            this.prpVal = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of properties applicable to the scanning and data acquisition control
     * GUIs.  
     *
     * @since  Sep 15, 2009
     * @author Christopher K. Allen
     */
    public enum DAQGUI implements xal.app.pta.tools.property.Property.IProperty {
        
        /** Whether or not to display the EASY scan configuration clobber warning */
        WARN_EZSCAN("DaqEzScanWarn"),
        
        /** Total Width of the DAQ controller panel */
        TOTAL_WD("DaqPrgTblWd"), //$NON-NLS-1$
        
        /** Total Height of the DAQ controller panel */
        PRG_TBL_HT("DaqPrgTblHt"), //$NON-NLS-1$
        
        /** Device Id column width */
        DEVIDCOL_WD("DaqPrgTblDevIdColWd"), //$NON-NLS-1$
        
        /** The progress bar column width */
        PROGCOL_WD("DaqPrgTblProgBarColWd"), //$NON-NLS-1$
        
        /** The motion state display column width */
        MOTIONCOL_WD("DaqPrgTblMvtStateColWd"); //$NON-NLS-1$
        
        
        
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>Property</code> object
         *              
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        
    
        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property  valProperty;
        
        
        /**
         * Create the enumeration constant for the given data acquisition
         * GUI persistent property.
         * 
         * @param strPropName   name of the GUI property.
         *
         * @author  Christopher K. Allen
         * @since   Oct 28, 2011
         */
        @SuppressWarnings("synthetic-access")
        private DAQGUI(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }

    /**
     * Enumeration of the application's device control
     * configuration parameters.
     *
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public enum DEVICE implements xal.app.pta.tools.property.Property.IProperty {
    
        /** Time out before a scan is interrupted and terminated (in seconds) */
        TMO_SCAN("DevScanTmOut"), //$NON-NLS-1$
        
        /** Time out before a Channel access connection test is aborted (in seconds) */
        TMO_CONNTEST("DevConnTestTmOut"), //$NON-NLS-1$
        
        /**  Wait between sending a device command then clearing the command buffer (in milliseconds) */
        LATENCY_CMD("DevLatencyCmd"), //$NON-NLS-1$
        
        /** Wait after sending a CAPUT value, usually to allow LabView time to process even (in milliseconds) */
        LATENCY_PUT("DevLatencyPut"),
        
        /** Ignore scan errors and continue to save data */
        IGNR_ERR("DevIgnoreError"), //$NON-NLS-1$
        
        /** Check the device connections before requesting or sending data and commands */
        EPICS_CA_CHK("DevDoCaConnTest"), //$NON-NLS-1$
        
        /**  Use EPICS PV limits for device configuration (rather than default values) */
        EPICS_LMTS("DevUseEpicsLimits"); //$NON-NLS-1$
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value corresponding this enumeration constant
         *  
         * @since       Jan 26, 2011
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  valProperty;
        
        /**
         * Constructs the device configuration enumerations 
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         */
        @SuppressWarnings("synthetic-access")
        private DEVICE(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of application event logging properties.
     *
     * @since  Nov 20, 2009
     * @author Christopher K. Allen
     */
    public enum EVTLOG implements Property.IProperty {
        /** Logging on or off */
        ENABLE("EvtLogEnable"), //$NON-NLS-1$
        
        /** Application logging file location */
        FILE("EvtLogFile"), //$NON-NLS-1$
        
        /** Continuous logging flag (i.e., past one application instance) */
        CONTINUE("EvtLogContinuous"), //$NON-NLS-1$
        
        /** Log verbose debugging information */
        VERBOSE("EvtLogVerbose"); //$NON-NLS-1$
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  This
         * object can then be used to convert the 
         * raw (string) value of the property value
         * into the proper type.
         * 
         * @return      value of the property corresponding to
         *              this enumeration constant
         *  
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        
        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property   valProperty;
    
        
        /**
         * <p> 
         * Constructs the application logging properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * 
         * @see        APP#STR_FILE_DEFAULT
         */
        @SuppressWarnings("synthetic-access")
        private EVTLOG(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of options for file parameters.
     *
     * @author Christopher K. Allen
     * @since   May 1, 2012
     */
    public enum FILE implements xal.app.pta.tools.property.Property.IProperty {
        
        /** The number of configuration files to cache */
        CFG_CNT("FileDevFileCnt"),
        
        /** The extension of files containing machine configuration data */
        CFG_EXT("FileDevCfgExt"),
        
        /** The extension of files containing application data */
        DAT_EXT("FileAppDataExt"),
        
        /** The drop file directory (relative) location (relative to the install location) */
        DROP_PATH("FileDropLoc"),
        
        /** File types (extensions) that the application recognizes */
        APP_TYPES("FileAppFileTypes");
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value corresponding this enumeration constant
         *  
         * @since   May 1, 2012
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  valProperty;
        
        /**
         * Constructs the device configuration enumerations 
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  May 1, 2012
         * @author Christopher K. Allen
         */
        @SuppressWarnings("synthetic-access")
        private FILE(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of default parameters for the harp profile device.
     * This device currently has parameters needed by PTA that are available
     * through a Process Variable.
     *
     * @since  April 11, 2014
     * @author Christopher K. Allen
     */
    public enum HARP implements xal.app.pta.tools.property.Property.IProperty {
    
        /** Default number of wires for a harp device (currently no PV for this value) */
        CNT_WIRES("HarpCntWires"), //$NON-NLS-1$
        
        /** Default value for the number of samples to take in a data set */
        CNT_SMPLS("HarpCntSamples"),
        
        /** Weighting factor for sample averaging */
        WT_SMPLS("HarpWeightSamples"),
        
        /**  Use EPICS PV limits for device configuration (rather than default values) */
        VER_SFTWR("HarpVerSoftware"); //$NON-NLS-1$
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value corresponding this enumeration constant
         *  
         * @since       Jan 26, 2011
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  valProperty;
        
        /**
         * Constructs the device configuration enumerations 
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         */
        @SuppressWarnings("synthetic-access")
        private HARP(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of default parameters for the harp profile device.
     * This device currently has parameters needed by PTA that are available
     * through a Process Variable.
     *
     * @since  April 11, 2014
     * @author Christopher K. Allen
     */
    public enum MSMT implements xal.app.pta.tools.property.Property.IProperty {
    
        /** The scaling value used for the beam standard deviation measurements */
        SCALE_STD_WIRE("MsmtScaleStdWire"); //$NON-NLS-1$
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value corresponding this enumeration constant
         *  
         * @since       Jan 26, 2011
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  valProperty;
        
        /**
         * Constructs the device configuration enumerations 
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         */
        @SuppressWarnings("synthetic-access")
        private MSMT(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of the general-use icons employed by the application.
     *
     * @author Christopher K. Allen
     * @since   Jan 21, 2011
     */
    public enum ICON implements Property.IProperty {
        
        /** Scan mode used for scanning and data acquisition (e.g., expert, easy scan, etc.) */
        SCAN_MODE("IconScanMode"),
        
        /** Scan start icon */
        SCAN_START("IconScanStart"), //$NON-NLS-1$
        
        /** Easy scan icon */
        SCAN_EASY("IconScanEasy"),
        
        /** Scan stop icon */
        SCAN_STOP("IconScanStop"), //$NON-NLS-1$
        
        /** Scan abort icon */
        SCAN_ABORT("IconScanAbort"), //$NON-NLS-1$
        
        /** Scan park actuator icon */
        SCAN_PARK("IconScanPark"), //$NON-NLS-1$
        
        /** Scan mode unspecified */
        SCAN_MODE_UNSPEC("IconScanModeUnspecified"),
        
        /** Use expert DAQ parameters */
        SCAN_MODE_XPRT("IconScanModeExpert"),
        
        /** Use the default DAQ parameters for an "easy" scan */
        SCAN_MODE_EZ("IconScanModeEasy"),
        
        /** Machine configuration save */
        CFG_SAVE("IconCfgSave"),
        
        /** Machine configuration save as */
        CFG_SAVEAS("IconCfgSaveAs"),
        
        /** Machine configuration restore */
        CFG_RESTORE("IconCfgRestore"),
        
        /** Machine restore recent configuration */
        CFG_RESTORE_RCT("IconCfgRestoreRecent"),
        
        /** Clear the cache of recently used configuration files */
        CFG_CLEAR_RCT("IconCfgClearRecent"),
        
        
        /** (Re)acquisition of data from diagnostic device */
        DAQ_ACQUIRE("IconDaqAcquire"), //$NON-NLS-1$
    
        
        /** Computational progress */
        CMP_TORUS("IconCmpTorus"),
        
        
        /** Computational analysis - Estimate Courant-Snyder parameters */
        TWS_ANALYZE("IconTwsAnalyze"),
        
        /** Computational analysis - Analyze Courant-Snyder parameters */
        TWS_COMPUTE("IconTwsCompute"),
        
        /** "Include space charge" button icon (for computing CS paramters) */
        TWS_SCHEFF("IconTwsScheff"),
        
    
        /** Data Analysis - select for the head of the beam */
        PRCG_BMHEAD("IconPrcgSelBeamHead"),
        
        /** Data Analysis - select for the body of the beam */
        PRCG_BMBODY("IconPrcgSelBeamBody"),
        
        /** Data Analysis - select for the tail of the beam */
        PRCG_BMTAIL("IconPrcgSelBeamTail"),
        
        /** Warning icon for adjustment of timing parameters */
        PRCG_WARNING("IconTmgWarning"),
    
        /** Sampling - warning for parameter */
        SMPL_WARNING("IconSmplWarning"),
        
        /** General "Expert" icon */
        EXPERT("IconExpertButton"),
        
        /** Icon for clear plot button */
        CLEAR("IconClearButton"), //$NON-NLS-1$
    
        /** Icon for apply buttons */
        APPLY("IconApplyButton"), //$NON-NLS-1$
        
        /** Icon for refresh buttons */
        REFRESH("IconRefreshButton"); //$NON-NLS-1$
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value of this enumeration constant
         *  
         * @since   Jan 21, 2011
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.prpVal;
        }
    
        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  prpVal;
    
        
        /** 
         * Constructs the icon properties enumerations.
         * 
         * @param strPropName   name of the icon 
         */
        @SuppressWarnings("synthetic-access")
        private ICON(String strPropName) {
            this.prpVal = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of the various numeric tuning parameters used by the application.
     *
     * @author Christopher K. Allen
     * @since   Dec 15, 2011
     */
    public enum NUMERIC implements xal.app.pta.tools.property.Property.IProperty {
    
        /** The default maximum number of iterations for the Courant-Snyder fixed point method */
        CSFP_MAXITER("NumCsFixedPtMaxIter"),
        
        /** The default maximum error tolerance for the Courant-Snyder fixed point method */
        CSFP_MAXERROR("NumCsFixedPtMaxError"),
        
        /** The default numerical tuning parameter used in the fixed point Courant-Snyder reconstruction method */
        CSFP_ALPHA("NumCsFixedPtAlpha"),
        
        /** The number of solution iterations before some intermediate action */
        CSFP_ITER_MOD("NumCsFixedPtIterModulo"),
        
        
        /** The numeric format to display position values (typically in mm)  */
        FMT_POSVALS("NumFormtPosValues"),
        
        /** The default beam fraction to use for data analysis, 
         * in particular selecting beam head or tail */
        PRCG_BMFRACT("NumAnalyzeBmFract"),
        
        /** The allowable error tolerance for beam quantities computed from measurement data */
        TOL_BM_QUAN("NumErrTolBeamQuantites");
    
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>PropertyValue</code> object
         *              
         * @author  Christopher K. Allen
         * @since Dec 15, 2011
         * 
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property       valProperty;
        
        /**
         * Create the plot property enumeration constant.
         * 
         * @param strPropName   name of the plot property
         *
         * @author  Christopher K. Allen
         * @since   Oct 28, 2011
         */
        @SuppressWarnings("synthetic-access")
        private NUMERIC(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }

    /**
     * Enumeration of the profile data plot configuration
     * parameters.
     *
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     */
    public enum PLT implements xal.app.pta.tools.property.Property.IProperty {
        
        /**  The plot legend key for the horizontal projection data */
        LGD_KEY_HOR("PltLegendKeyHor"), //$NON-NLS-1$
        
        /**  The plot legend key for the vertical projection data */
        LGD_KEY_VER("PltLegendKeyVer"), //$NON-NLS-1$
        
        /**  The plot legend key for the diagonal projection data */
        LGD_KEY_DIA("PltLegendKeyDia"), //$NON-NLS-1$
        
        /**  The color of the curve for horizontal projection data */
        CLR_CRV_HOR("PltCurveColorHor"), //$NON-NLS-1$
        
        /**  The color of the curve for vertical projection data */
        CLR_CRV_VER("PltCurveColorVer"), //$NON-NLS-1$
        
        /**  The color of the curve for diagonal projection data */
        CLR_CRV_DIA("PltCurveColorDia"), //$NON-NLS-1$
        
        /** Profile plot RED color value */
        CLR_BGND("PltBgndColor"), //$NON-NLS-1$
        
        /** Color of the lines used to draw the trace processing window */
        CLR_TRC_WND("PltTraceWindowColor"), //$NON-NLS-1$
        
        /** Color of the lines used to draw the trace */
        CLR_TRC_CRV("PltTraceCurveColor"); //$NON-NLS-1$
        
        
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>PropertyValue</code> object
         *              
         * @since 	Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property       valProperty;
        
        
        /**
         * Create the plot property enumeration constant.
         * 
         * @param strPropName   name of the plot property
         *
         * @author  Christopher K. Allen
         * @since   Oct 28, 2011
         */
        @SuppressWarnings("synthetic-access")
        private PLT(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }

    /**
     * Enumeration of the application properties/preferences
     * dialog and properties.
     *
     * @author Christopher K. Allen
     * @since   Jan 13, 2011
     */
    public enum PREFS implements Property.IProperty {
        
        /** Message displayed on the preferences dialog */
        DLG_MSG("PrefsDialogMsg"), //$NON-NLS-1$
        
        /** The preferences dialog screen height */
        DLG_HT("PrefsDialogHt"), //$NON-NLS-1$
        
        /** The preferences dialog screen width */
        DLG_WD("PrefsDialogWd"); //$NON-NLS-1$
        

        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  
         * 
         * @return      property value of this enumeration constant
         *  
         * @since   Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.prpVal;
        }

        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  prpVal;

        
        /** Construct enumeration constants. 
         * @param strPropName name of the application property*/
        @SuppressWarnings("synthetic-access")
        private PREFS(String strPropName) {
            this.prpVal = MGR_APP_PROPS.getProperty(strPropName);
        }
    }
    
    
    /**
     * Enumeration of the application's process variable (PV)
     * logging configuration parameters.
     *
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public enum PVLOG implements Property.IProperty {
    
        /** Enable PV logger snapshots after each measurement */
        ENABLE("PvLogEnable"), //$NON-NLS-1$
        
        /**  Group ID of the PV log for measurements */
        MSMT_ID("PvLogMsmtId"); //$NON-NLS-1$
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  This
         * object can then be used to convert the 
         * raw (string) value of the property value
         * into the proper type.
         * 
         * @return      value of the property corresponding to
         *              this enumeration constant
         *  
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
    
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property   valProperty;
        
        /**
         * <p> 
         * Constructs the application logging properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * 
         * @see        APP#STR_FILE_DEFAULT
         */
        @SuppressWarnings("synthetic-access")
        private PVLOG(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Enumeration of the various beam parameters used for simulation purposes.
     *
     * @author Christopher K. Allen
     * @since   Dec 15, 2011
     */
    public enum SIM implements xal.app.pta.tools.property.Property.IProperty {
    
        /** The arrival frequency of beam bunches, typically a sub-harmonic of machine RF */
        BNCHFREQ("SimBunchFrequency"),
        
        /** The allowable error tolerance for beam quantities computed from measurement data */
        BMCURR("SimBeamCurrent");
    
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>PropertyValue</code> object
         *              
         * @author  Christopher K. Allen
         * @since Dec 15, 2011
         * 
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property       valProperty;
        
        /**
         * Create the plot property enumeration constant.
         * 
         * @param strPropName   name of the plot property
         *
         * @author  Christopher K. Allen
         * @since   Oct 28, 2011
         */
        @SuppressWarnings("synthetic-access")
        private SIM(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }

    /**
     * Enumeration of the application splash screen properties.
     *
     * @author Christopher K. Allen
     * @since   Jan 13, 2011
     */
    public enum SPLASH implements Property.IProperty {
        
        /** The title of the splash screen */
        TITLE("SplashTitle"), //$NON-NLS-1$
        
        /** The text displayed on the splash screen */
        TEXT("SplashText"), //$NON-NLS-1$
        
        /** Authors information */
        AUTHORS("SplashAuthors"), //$NON-NLS-1$
        
        /** Copyright notice */
        COPYRT("SplashCopyright"), //$NON-NLS-1$
        
        /** Time duration for splash screen display */
        TIME("SplashTime"), //$NON-NLS-1$
        
        /** The icon displayed on the splash screen */
        ICON("SplashIcon"), //$NON-NLS-1$
        
        /** Horizontal size of the splash screen  */
        WIDTH("SplashScrWidth"), //$NON-NLS-1$
        
        /** Vertical size of the splash screen  */
        HEIGHT("SplashScrHeight"), //$NON-NLS-1$
        
        /** Small font used on splash screen text */
        SMFONT("SplashFontSm"), //$NON-NLS-1$
        
        /** The default font used for the screen text */
        LGFONT("SplashFontLg"), //$NON-NLS-1$
        
        /** Background color */
        BKGNDCLR("SplashColorBkgnd"), //$NON-NLS-1$
        
        /** Foreground color */
        FRGNDCLR("SplashColorFrgnd"); //$NON-NLS-1$
        
        

        /*
         * Operations
         */
        
        /**
         * Returns the value of the property as a 
         * <code>PropertyValue</code> object.  This
         * object can then be used to convert the 
         * raw (string) value of the property value
         * into the proper type.
         * 
         * @return      value of the property corresponding to
         *              this enumeration constant
         *  
         * @since   Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.prpVal;
        }

        
        /*
         * Private Attributes
         */
        
        /** The application configuration property */
        private final Property  prpVal;

        
        /**
         * <p> 
         * Constructs the application properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * 
         * @see        APP#STR_FILE_DEFAULT
         */
        @SuppressWarnings("synthetic-access")
        private SPLASH(String strPropName) {
            this.prpVal = MGR_APP_PROPS.getProperty(strPropName);
        }
    }

    /**
     * Global (default) text field properties
     *
     * @since  Jan 15, 2010
     * @author Christopher K. Allen
     */
    public enum TEXTFLD implements Property.IProperty {
    
        /** The number of columns in the display */
        COLS("TextBoxColumnCnt"), //$NON-NLS-1$
        
        /** The size of the horizontal struts between text box and label */
        PADX("TextBoxPaddingHor"), //$NON-NLS-1$
        
        /** The size of the vertical struts between text boxes */
        PADY("TextBoxPaddingVer"); //$NON-NLS-1$
        
        
        
        
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>PropertyValue</code> object
         *              
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @SuppressWarnings("synthetic-access")
        public Property getValue() {
            Property    prpVal = MGR_APP_PROPS.getProperty(this.strPropName);
            
            return prpVal;
        }
        
        
        /*
         * Private Stuff
         */
        
        /** Property name for this enumeration constant */
        private final String    strPropName;
        
        /** 
         * Create the enumeration constant 
         * @param strPropName name of the text field property 
         */
        private TEXTFLD(String strPropName) {
            this.strPropName = strPropName;
        }
        
    }

 
    
    /**
     * Enumeration of properties applicable to device
     * tables.
     *
     * @since  Sep 15, 2009
     * @author Christopher K. Allen
     */
    public enum DEVSEL implements xal.app.pta.tools.property.Property.IProperty {
        /** Width of the profile data plots 
         * @deprecated Not used
         */
        @Deprecated
        TREE_WD("DevSelTreeTotalWd"), //$NON-NLS-1$
        
        /** Height of the profile data plots 
         * @deprecated Not used
         */
        @Deprecated
        TREE_HT("DevSelTableTotalHt"), //$NON-NLS-1$
        
        /** Width of the profile data plots 
         * @deprecated Not used
         */
        @Deprecated
        TABLE_WD("DevSelTableTotalWd"), //$NON-NLS-1$
        
        /** Height of the profile data plots 
         * @deprecated Not used
         */
        @Deprecated
        TABLE_HT("DevSelTableTotalHt"), //$NON-NLS-1$
        
        /** Device Id column width */
        DEVIDCOL_WD("DevSelTableDevIdColWd"); //$NON-NLS-1$
        
        
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>Property</code> object
         *              
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        

        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property  valProperty;
        
        
        /**
         * Create the new enumeration constant for a persistent property of
         * the device selection panel
         * .
         * @param strPropName   property name
         *
         * @author  Christopher K. Allen
         * @since   Oct 28, 2011
         */
        @SuppressWarnings("synthetic-access")
        private DEVSEL(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }

    
    /**
     * Enumeration of properties applicable to the
     * scan configuration panel.
     *
     * @since  Sep 15, 2009
     * @author Christopher K. Allen
     * 
     * @deprecated not used
     */
    @Deprecated
    public enum SCAN_CFG implements xal.app.pta.tools.property.Property.IProperty {
        /** Apply changes button icon 
         * @deprecated not used 
         */
        @Deprecated
        APPLY_ICON("ScanConfigApplyIcon"), //$NON-NLS-1$
        
        /** 
         * Width of the profile data plots 
         * @deprecated  not used
         */
        @Deprecated
        TOTAL_WD("ScanConfigPanelWd"), //$NON-NLS-1$
        
        /** 
         * Height of the profile data plots
         * @deprecated not used 
         */
        @Deprecated
        TOTAL_HT("ScanConfigPanelHt"); //$NON-NLS-1$
        
        
        /**
         * Return the property value associated with this
         * enumeration constant.
         * 
         * @return      value of the property as a 
         *              <code>PropertyValue</code> object
         *              
         * @since       Jul 15, 2009
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.property.Property.IProperty#getValue()
         */
        @Override
        public Property getValue() {
            return this.valProperty;
        }
        
        
        /*
         * Private Stuff
         */
        
        /** The property value object */
        private final Property          valProperty;
        
        /** 
         * Create the enumeration constant 
         * @param strPropName name of the scan configuration panel property 
         */
        @SuppressWarnings("synthetic-access")
        private SCAN_CFG(String strPropName) {
            this.valProperty = MGR_APP_PROPS.getProperty(strPropName);
        }
        
    }
    
    
    
    
    /*
     * Global Attributes
     */
    
    /** The singleton property manager for the persistent application properties */
    private static AppProperties         MGR_APP_PROPS;
    

    
    /*
     * Support Methods
     */

    /**
     *  Static block setting the single property manager instance 
     *  and default configuration properties.
     *
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     */
    static {
        try {
            Properties  propsDefault = PtaResourceManager.getProperties( STR_FILE_DEFAULT );
            
            MGR_APP_PROPS = new AppProperties(propsDefault);
            
        } catch (IOException e) {
            
            String      strErrMsg   = "Application configuration mechanism corrupted. \n" + //$NON-NLS-1$
                                      "See AppProperties class. \n"; //$NON-NLS-1$
            
            MainApplication.applicationLaunchFailure(strErrMsg, e);
        }
    }
    
    /**
     * Singleton class - prevent outside instantiation.
     *
     * @param   setDefProps     the set of default property values
     *
     * @since     Jul 15, 2009
     * @author    Christopher K. Allen
     */
    private AppProperties(Properties setDefProps)      {
        super(AppProperties.class, setDefProps);
    };
}
