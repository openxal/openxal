/*
 * @(#)MainApplication.java          06/07/2009
 *
 * Copyright (c) 2001-2009 Oak Ridge National Laboratory
 * Oak Ridge, Tennessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.app.pta;

import xal.extension.application.Application;
import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.Commander;
import xal.extension.application.XalDocument;
import xal.extension.application.XalWindow;
import xal.app.pta.cmdmgt.FileMenuCommands;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PropertyDisplayDialog;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.cptblty.WireAnalysisFormatter;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.logging.JavaLogger;
import xal.app.pta.tools.logging.NullLogger;
import xal.app.pta.tools.swing.SplashWindow;
import xal.service.pvlogger.RemoteLoggingCenter;
import xal.extension.application.smf.AcceleratorApplication;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


/*
 * A small change to test GIT branching
 * 
 * - The next change
 * - Change #3
 * - Change #4
 * - Change #5
 * 
 */



/**
 * <h1>Application PTA</h1>
 * <p>
 * Main class for the <tt>PTA</t> application.  It is the
 * entry point <code>{@link #main(String[])}</code> for the
 * <tt>PTA</tt> application.
 * </p>
 * <p>
 * This application is a 
 * high-level tool for the general purpose configuration, control, 
 * and operation of profile data diagnostic devices in charged-particle
 * beam systems.  The objective here is to consolidate the function
 * of several different profile device applications into on centralized
 * application.   
 * </p>
 * <p>
 * The application architecture is based upon the 
 * <tt>Model/View/Controller</tt> design pattern.  This class takes
 * the role of "<tt>Global Controller</tt>" component in the architecture.  
 * That is,
 * it provides services and resources for all instances of the 
 * application.  Consequently, most of the attributes and methods are
 * static and the class behaves more as a utility class.  The 
 * <tt>XAL GUI Framework</tt> supports multiple documents in the
 * <tt>Model/View/Controller</tt> architecture.  The controller
 * object for each document (the "<tt>Local Controller</tt>" if
 * you will) is embodied in the <code>MainWindow</code> class
 * which coordinates all the views onto a document.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * 
 * @since  Jun 11, 2009
 * @version 2.0 {@code date}
 * 
 * @see xal.extension.application.ApplicationAdaptor
 */
public class MainApplication extends ApplicationAdaptor {

    /**
     * <h3>Application Entry Point</h3>
     * <p>
     * Java entry point for event driven applications.
     * The application is launched via a call to the 
     * static {@link AcceleratorApplication#launch(ApplicationAdaptor)}
     * </p>
     * 
     * 
     * @param args      command-line arguments (unused)
     * 
     * @since  Jun 8, 2009
     * @author Christopher K. Allen
     */
    static public void main(String[] args) {
        System.out.println("Starting application...");
        
        try {
            APP_MAIN = new MainApplication();
            
        } catch (NullPointerException e) {
            
            MainApplication.applicationLaunchFailure("MainApplication#main() launch failure", e);
        }
    }

    /**
     * <p>
     * This is the application class for the XAL GUI framework.  It is meant to be
     * a singleton object held by the <code>MainApplication</code> class as a 
     * customized proxy for the <code>{@link Application}</code> object.
     * </p>
     * <p>
     * Note that it is somewhat impractical to enforce the singleton nature of this class
     * (by hiding the constructor) since the base class constructor requires arguments.
     * </p>
     *
     * @author Christopher K. Allen
     * @since   Apr 18, 2012
     */
    public class ApplicationProxy extends AcceleratorApplication {

        /**
         * Creates a new <code>ApplicationProxy</code> object for the given
         * application adaptor.
         * 
         * @param adaptor   the defining functionality for the application 
         *
         * @author  Christopher K. Allen
         * @since   Apr 18, 2012
         */
        public ApplicationProxy(ApplicationAdaptor adaptor) {
            this(adaptor, new URL[] {});
        }
        
        /**
         * Creates a new <code>ApplicationProxy</code> object for the given
         * application adaptor and attempt to 
         * initialize it (whatever that means, in the context of the application)
         * with the data stored in the given URLs.
         * 
         * @param adaptor   the functionality associated with the application 
         * @param urls      location of initializing data
         *
         * @author  Christopher K. Allen
         * @since   Apr 18, 2012
         */
        public ApplicationProxy(ApplicationAdaptor adaptor, URL[] urls) {
            super(adaptor, urls);
        }
        
        
        /*
         * Application Overrides
         */
        
        /**
         * Override the <code>Application</code> method to cancel the default application startup
         * dialog.
         * 
         * @since Apr 18, 2012
         * @see xal.extension.application.Application#showsWelcomeDialogAtLaunch()
         */
        @Override
        protected boolean showsWelcomeDialogAtLaunch() {
            return false;
        }

        /**
         * Simply calls the super class's corresponding method.
         * 
         * @since Apr 27, 2012
         */
        @Override
        public void closeAllDocuments() {
            super.closeAllDocuments();
        }

    }
    
    
    /*
     * Global Constants
     */
    
    /** The global name of the application */
    private static final String             STR_APP_NAME = AppProperties.APP.NAME.getValue().asString();
    
    /** Default data file types of this application */
    public static final String              STR_DAT_FILE_EXT = AppProperties.FILE.DAT_EXT.getValue().asString();
    
    /** Default device configuration file type for this application */
    public static final String              STR_CFG_FILE_EXT = AppProperties.FILE.CFG_EXT.getValue().asString();
    
    /** Default file drop location */
    public static final String              STR_PATH_DROP = AppProperties.FILE.DROP_PATH.getValue().asString();

    /** Array of file suffixes which application document recognizes */
    public static final String []           ARR_DOCTYPES_APP = AppProperties.FILE.APP_TYPES.getValue().asStringArray();  
    
    
    
    /*
     * Global Operations
     */
    
    /**
     * <h3>Critical Error Exception and Application Exit</h3>
     * <p>
     * Call when an application-level exception is encountered.  An
     * error message dialogue box is brought up, displaying the given
     * message along with the message in the given exception object.
     * The error message and a stack trace are sent to the standard
     * error stream and a complete virtual machine exit is invoked.
     * </p>
     *
     * @param strErrMsg         Error message to display
     * @param e                 The exception that caused the critical error
     * 
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     System#exit(int)
     */
    public static void applicationLaunchFailure(String strErrMsg, Exception e) {
        String      strErrTitle = "Critical Error - Launch Failure";
        String      strMessage  = strErrMsg + "\n" + "IOException message: " + e.getMessage();

        JOptionPane.showMessageDialog(null, strMessage, strErrTitle, JOptionPane.ERROR_MESSAGE);
        
        System.err.println(strErrTitle);
        System.err.println(strMessage);
        e.printStackTrace();
    }

    
    
    /*
     * Global Attributes
     */
    
    /** The main application instance */
    private static MainApplication              APP_MAIN;
    
//    /** The machine configuration manager */
//    static MainConfiguration            CFG_MAIN;
    
    /** Remote PV Logger */
    private static RemoteLoggingCenter          PVLGR_REMOTE;
    
    

    /** Provides the time stamps for the acquired data */
    private static final Calendar               CAL_TM_STP;
    
    /** Provides the format for the time stamps */
    private static final DateFormat             FMT_TM_STP;
    
    

    /** The application logger object */
    private static final JavaLogger             LGR_APP_EVT;

    /** Enable flag for PV Logging */
    private static final boolean                PVLGR_ENABLED;

    
    
    
    /**
     * <p>
     * Creates the application's universal objects.
     * These are the singular objects that must exist
     * uniquely throughout all applications.  
     * </p>
     * <p>
     * Some objects require parameters.  
     * Also, the order of instantiation is important, since there
     * are dependencies.  Thus, the global objects are 
     * initialized in the static area.
     * </p>
     * 
     * @since  Nov 19, 2009
     * @author Christopher K. Allen
     */
    static {
//        STR_APP_NAME = AppProperties.APP.NAME.getValue().asString();
        
        // Create the global objects
//        CFG_MAIN      = new MainConfiguration();
//        MainScanController.DAQ_CTRLR     = new MainScanController();
        CAL_TM_STP    = Calendar.getInstance();
//        FMT_TM_STP    = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
//      FMT_TM_STP    = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
        FMT_TM_STP    = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");

        
        /** Setup the PV logger */
        PVLGR_ENABLED = AppProperties.PVLOG.ENABLE.getValue().asBoolean();
        PVLGR_REMOTE = null;
        
        // Configure Event Logging
        //      Get the logging configuration
//        String                strFileName = AppProperties.EVTLOG.FILE.getValue().asString();
//        boolean               bolAppend   = AppProperties.EVTLOG.CONTINUE.getValue().asBoolean();
        boolean               bolVerbose  = AppProperties.EVTLOG.VERBOSE.getValue().asBoolean();
        boolean               bolEnable   = AppProperties.EVTLOG.ENABLE.getValue().asBoolean();

        
        //      Create the logging output file and write out log buffer
//        File fileLog = createDropFile(strFileName);
//        
//        LGR_APP_EVT = new FileLogger2(fileLog.getAbsolutePath(), bolAppend, bolVerbose, bolEnable);
//        LGR_APP_EVT.beginLogging();
//        LGR_APP_EVT.logInfo(MainApplication.class, "Created new application logging object: " + LGR_APP_EVT.getClass());
//        LGR_APP_EVT.logInfo(MainApplication.class, "Global application objects initialized. ");
        
        LGR_APP_EVT = new JavaLogger(Logger.GLOBAL_LOGGER_NAME, bolVerbose, bolEnable);
        LGR_APP_EVT.beginLogging();
        LGR_APP_EVT.logInfo(MainApplication.class, "Created new application logging object: " + LGR_APP_EVT.getClass());
        LGR_APP_EVT.logInfo(MainApplication.class, "Global application objects initialized. ");
        
    }
    

    
    /*
     * Application Objects 
     */
    
    /**
     * This is a convenience method to get the singleton
     * <code>{@link AcceleratorApplication}</code>
     * object associated with the XAL Application Framework.
     *
     * @return  the unique <code>AcceleratorApplication</code> object
     * 
     * @since  Apr 29, 2010
     * @author Christopher K. Allen
     */
    public static AcceleratorApplication getAcceleratorApplication() {
        return AcceleratorApplication.getAcceleratorApp();    
    }

    /**
     * Returns the singleton instance of the main application object.  The object
     * is actually a derived type of <code>{@link ApplicationAdaptor}</code>.
     *
     * @return  The application object controlling the application function.
     *
     * @author Christopher K. Allen
     * @since  Apr 20, 2012
     */
    public static MainApplication getApplicationInstance() {
        return APP_MAIN;
    }
    
    /**
     * Returns the XAL GUI framework application object associated with
     * the <code>MainApplication</code> object, which is actually an
     * application adaptor.
     * 
     * @return the XAL application object
     */
    public static ApplicationProxy getApplicationProxy() {
        return APP_MAIN.appProxy;
    }

    /**
     * Convenience method for return the application document of the currently
     * active application.  Since there is only one application allowed, this 
     * document will be unique at any one time.  However, the application can
     * close documents and open new ones so there can be multiple documents over
     * the lifetime of the application (none alive simultaneously).
     *
     * @return  The currently active data document
     *
     * @author Christopher K. Allen
     * @since  Jul 12, 2012
     */
    public static MainDocument      getApplicationDocument() {
        return getApplicationInstance().getMainDocument();
    }

    /**
     * Return the event logger for the application.
     * All events recorded to the logger are saved
     * to a persistent backing store.
     *
     * @return  application event logger, returns
     *          a <code>NullLogger</code> object if the application 
     *          logger hasn't been created yet.
     * 
     * @since  Nov 25, 2009
     * @author Christopher K. Allen
     * 
     * @see     NullLogger
     */
    public static IEventLogger   getEventLogger() {
        if (MainApplication.LGR_APP_EVT == null)
            return new NullLogger();
            
        return MainApplication.LGR_APP_EVT;
    }
    

    /*
     * Application Properties
     */
    
    /**
     * Returns the given name of this application.
     * 
     * @return  application name
     *
     * @author Christopher K. Allen
     * @since  May 4, 2012
     */
    public static String getApplicationName() {
        return MainApplication.STR_APP_NAME;
    }

    /**
     * Returns the default data file extension for this application.
     *
     * @return  the three-character file extension defining the 
     *          data file type for this application
     * 
     * @since  Apr 29, 2010
     * @author Christopher K. Allen
     */
    public static String getDataFileExtension() {
        return MainApplication.STR_DAT_FILE_EXT;
    }
    
    /**
     * Returns the default device configuration file extension for this application.
     *
     * @return  the three-character file extension defining the 
     *          device configuration file type for this application
     * 
     * @since  Apr 29, 2010
     * @author Christopher K. Allen
     */
    public static String getConfigurationFileExtension() {
        return MainApplication.STR_CFG_FILE_EXT;
    }
    
    /**
     * Returns the formatting object used for time stamps within the application.
     * (Centralizes the formatting.)
     *
     * @return  application time stamp format
     *
     * @author Christopher K. Allen
     * @since  Apr 25, 2012
     */
    public static DateFormat getTimeStampFormat() {
        return FMT_TM_STP;
    }

    
    /*
     * Application Services
     */
    
    /**
     * Use a <code>Calendar</code> object to get the current date
     * and time as  <code>Date</code> object and return it.
     * (Intended for use as a time stamp for measurement data.)
     *
     * @return  The current date and time as a <code>Date</code> object
     * 
     * @since  Mar 19, 2010
     * @author Christopher K. Allen
     */
    public static Date timeStamp() {
        CAL_TM_STP.setTime(new Date());
        
        return CAL_TM_STP.getTime();
    }

    /**
     * Takes a PV Logger snapshot of the current state
     * of the machine and return the ID of the 
     * snapshot.
     *  
     * @param strCmt    comment string saved with the saved snapshot 
     *
     * @return  PV logger snapshot ID
     * 
     * @since  Mar 18, 2010
     * @author Christopher K. Allen
     */
    public static long pvLoggerSnapshot(String strCmt) {
        if (!PVLGR_ENABLED) {
            getEventLogger().logInfo(MainApplication.class, "PV Logging disabled, no snapshot taken.");
            return -1;
        }
        
        if (PVLGR_REMOTE == null)
            PVLGR_REMOTE = new RemoteLoggingCenter();
        
//        String  strGrpId = AppProperties.PVLOG.MSMT_ID.getValue().asString();

//        if ( !PVLGR_REMOTE.hasLogger(strGrpId) ) {
//            getEventLogger().logWarning(MainApplication.class,
//                            "PV Logger Error: No default PV Logger service. " +
//                            "No PV Logger snapshot taken."
//                            );
//            
//            return -1;
//        }
        
        long pvLogId = PVLGR_REMOTE.takeAndPublishSnapshot( "default", strCmt);

        if (pvLogId < 0){
            getEventLogger().logWarning(MainApplication.class,"PV Logger Error: " + "" +
            		"General exception while taking a PV Logger snapshot.");
        }
        
        return pvLogId;
    }

    /**
     * <p>
     * Creates a file object with the given name in the default
     * drop file location.    Currently, this location is the directory
     * under the the current directory with name 
     * <code>{@link MainApplication#STR_PATH_DROP}</code>
     * </p>
     * <p>
     * If the file already exists nothing is
     * done and the <code>File</code> object descriptor of that
     * file is returned.  If the file does not exist, an empty
     * file is created and the handle of that file is returned.
     * </p>
     *
     * @param strFileName       (local) name of the drop file
     * 
     * @return                  handle to the drop file or 
     *                          <code>null</code if an error occurred 
     * 
     * @since  Jul 2, 2010
     * @author Christopher K. Allen
     * 
     * @see     MainApplication#STR_PATH_DROP
     */
    public static File  createDropFile(String strFileName) {
    
        // Check that the drop location exists, create it if not
        String   strDirHome  = System.getProperty("user.home");
        File     fileDir     = new File(strDirHome, MainApplication.STR_PATH_DROP);
        
        if (!fileDir.exists()) {
            fileDir.mkdirs();
            MainApplication.getEventLogger().logInfo(
                            MainApplication.class, 
                            "A new drop location has been created : " + 
                            fileDir.getAbsolutePath()
                            );
        }
        
        // Create the drop file
        File            fileDrop    = new File(fileDir, strFileName);
        
        try {
            if (!fileDrop.exists()) {
                MainApplication.getEventLogger().logInfo(
                                MainApplication.class, 
                                "Creating new drop file: " + 
                                fileDrop.getAbsolutePath()
                                );
            }
            fileDrop.createNewFile();
            
            return fileDrop;
            
        } catch (IOException e) {
            String      strMsg = "Unable to create drop file: " + fileDrop.getAbsolutePath(); 
            String      strOrg = "Cause: " + e.getMessage();
            
            System.err.println(strMsg);
            System.err.println(strOrg);
            MainApplication.getEventLogger().logError(MainApplication.class, strMsg + " " + strOrg);
            
            return null;
        }
    }

    /**
     * Used to convert an data formatting label within a PTA data file to version 3
     * from version 2 format.  This applies to the data of classes with the
     * <code>xal.apps.pta</code> package.
     * 
     * @param strLblVer2    a PTA data file XML label in the version 1 format
     * 
     * @return              the corresponding PTA data label in version 2 format
     *
     * @author Christopher K. Allen
     * @since  Oct 8, 2014
     */
    public static String  convertPtaDataLabelToVer3(String strLblVer2) {
        String  strLblVer3 = strLblVer2.replace("xal.app", "gov.sns.apps");
        
        return strLblVer3;
    }
    
    /**
     * Used to convert an data formatting label within a PTA data file to version 3
     * from version 2 format.  This applies to the data of classes with the
     * <code>xal.smf.imple</code> package.
     * 
     * @param strLblVer2    a PTA data file XML label in the version 2 format
     * 
     * @return              the corresponding PTA data label in version 3 format
     *
     * @author Christopher K. Allen
     * @since  Oct 8, 2014
     */
    public static String  convertSmfDataLabelToVer3(String strLblVer2) {
        String  strLblVer3 = "gov.sns." + strLblVer2;
        
        return strLblVer3;
    }
    


    /*
     * Local Attributes
     */
    
    /** The XAL GUI Framework application proxy */
    private ApplicationProxy    appProxy;
    
    /** the single active document for this application */
    private MainDocument        docMain;
    
    /** the single machine configuration manager for this application */
//    private MainConfiguration   cfgMain;

    
    /** The set of file commands */
    private FileMenuCommands    cmdsFile;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>Main</code> object without
     * any attached data file.
     *
     *
     * @since     Jun 8, 2009
     * @author    Christopher K. Allen
     */
    private MainApplication() {
        this.displaySplashScreen();

        APP_MAIN = this;
//        this.winMain  = null;
        this.docMain  = null;
//        this.cfgMain  = null;
        this.appProxy = new ApplicationProxy(this); 
    }

    /**
     * Returns the active document for 
     * this application instance.  
     *
     * @return  the active document 
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     * 
     * @see     #newDocument(URL)
     * @see     #newEmptyDocument()
     */
    public MainDocument   getMainDocument() {
        return this.docMain;
    }
    
    
    /*
     * GUI Components
     */
    
    /**
     * Returns the main GUI frame for the application. 
     *
     * @return GUI frame
     *
     * @since  Jul 31, 2009
     * @author Christopher K. Allen
     */
    public MainWindow getMainWindow() {
       return (MainWindow)this.docMain.getMainWindow();
    }

    
    
    /*
     * File Menu Commands
     */
    

//    /**
//     * Prompts the user for a file location which is intended 
//     * for a save operation.  If the user cancels the request
//     * or points to a bad file location a <code>null</code> value
//     * is returned.
//     *
//     * @return  The URL of a file location provided by the user,
//     *          or null if canceled.
//     * 
//     * @since  May 7, 2010
//     * @author Christopher K. Allen
//     */
//    public URL  userRequestedFileUrl() {
//        
//        // Get the file chooser dialogue
//        AcceleratorApplication appGbl  = MainApplication.getAcceleratorApplication();
//
//        File                   fileDir = appGbl.getDefaultDocumentFolder();
//        File                   fileDef = this.timeStampFileName(fileDir);
//        
//        // Initialize the file chooser dialogue
//        JFileChooser           chrSave = appGbl.getSaveFileChooser();
////      File                   fileDir = chrSave.getCurrentDirectory();
//        chrSave.setSelectedFile(fileDef);
//        
//        // Retrieve the file name
//        MainWindow  winMain    = this.getMainWindow();
//        int         intResult  = chrSave.showSaveDialog(winMain);
//        
//        if (intResult == JFileChooser.APPROVE_OPTION) {
//            File    file = chrSave.getSelectedFile();
//            URI     uri  = file.toURI();
//
//            // If all goes well return the URL
//            try {
//                URL url = uri.toURL();
//                
//                return url;
//
//            } catch (MalformedURLException e) {
//                this.getMainWindow().displayError("Save Error", "Bad file name " + uri.toString());
//                getEventLogger().logException(this.getClass(), 
//                                e, 
//                                "Unable to save data, bad file name " + 
//                                uri.toString()
//                                );
//            }
//        }
//        
//        // Otherwise we indicate that no proper URL was offered
//        return null;
//    }
    
    /**
     * Closes the current document and creates a new,
     * empty document.
     *
     * 
     * @since  May 11, 2010
     * @author Christopher K. Allen
     */
    public void menuFileNew() {
        if ( this.docMain.hasChanges() && this.docMain.warnUserOfUnsavedChangesWhenClosing() ) {
            if ( !this.getMainWindow().userPermitsCloseWithUnsavedChanges() )  
                return;
        }

//        MainDocument    docNew  = new xal.app.pta.MainDocument(this);
        MainDocument    docNew  = new xal.app.pta.MainDocument();
        MainApplication.getApplicationProxy().produceDocument(docNew, true);

        this.docMain.closeDocument();
        this.docMain = docNew;

        getEventLogger().logInfo(this.getClass(), "A new empty document has been created.");
    }
    
    /**
     * Opens the previously saved document on disk, indicated
     * by the given URL, then
     * loads it into the main document.  An open file dialogue is 
     * presented to the user where he or she may selected the
     * disk file.  
     *
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public void menuFileOpen() {

//        URL                    url;
//        AcceleratorApplication appGbl  = AcceleratorApplication.getAcceleratorApp();
        AcceleratorApplication appGbl  = MainApplication.getApplicationProxy();
        MainWindow             winMain = this.getMainWindow();
        JFileChooser           chrOpen = appGbl.getOpenFileChooser();

        int intResult = chrOpen.showOpenDialog(winMain);
        if (intResult == JFileChooser.APPROVE_OPTION) {
            File    file = chrOpen.getSelectedFile();
            URI     uri  = file.toURI();
            
            try {
                URL url = uri.toURL();

                // Load the document data from the given URL
                // The document does not exist
                if (this.docMain == null) {
                    this.docMain = (MainDocument) this.newDocument(url);

                    // The document exists and has no unsaved changes
                } else if (!docMain.hasChanges()) {
                    this.docMain.openDocument(url);

                    // The document exists and has unsaved changes
                } else {

                    //      Confirm overwrite
                    int intOpt = winMain.displayConfirmDialog(
                            "Confirm", "Over write " + 
                            this.docMain.getSource().toString() + 
                            "?"
                    );

                    if (intOpt == XalWindow.YES_OPTION) {
                        this.docMain.openDocument(url);
                    }
                }

                getEventLogger().logInfo(this.getClass(), 
                        "Loaded application data from " + 
                        url.toString()
                );
                
            } catch (MalformedURLException e) {
                winMain.displayError("File Error", "Unable to read file " + uri.toString());
                winMain.getLogger().logException(this.getClass(), e, "Unable to read input file " + uri.toString());

                return;
            }

        } else      // The user didn't provide a file name 
            return; //  Nothing to do


    }
    
    /**
     * Saves the current data in a new disk file.  As
     * save file dialog is spawned and the user provides
     * the new URL.
     *
     * 
     * @since  May 11, 2010
     * @author Christopher K. Allen
     */
    public void menuFileSaveAs() {
        if (this.docMain == null) {
            this.getMainDocument().displayWarning("Empty Document!", "Save operation not available.");
            return;
        }
        
//        if (!this.docMain.hasChanges()) {
//            this.getMainDocument().displayWarning("No Changes", "Nothing to save.");
//            return;
//        }
        
        // Get the file chooser dialogue
        AcceleratorApplication appGbl  = MainApplication.getAcceleratorApplication();
        JFileChooser           chrSave = appGbl.getSaveFileChooser();

        // Initialize the file chooser dialogue
        File                   fileDir = appGbl.getDefaultDocumentFolder();
//        File                   fileDir = chrSave.getCurrentDirectory();
        File                   fileDef = this.timeStampFileName(fileDir);
        chrSave.setSelectedFile(fileDef);

        // Retrieve the file name
        MainWindow  winMain    = this.getMainWindow();
        int         intResult  = chrSave.showSaveDialog(winMain);
        //File        fileNewDir = chrSave.getCurrentDirectory();

        if (intResult == JFileChooser.APPROVE_OPTION) {
            File    file = chrSave.getSelectedFile();
            URI     uri  = file.toURI();

            try {
                this.docMain.saveDocumentAs( uri.toURL() );
                
            } catch (MalformedURLException e) {
                winMain.displayError("Save Error", "Bad file name " + uri.toString());
                winMain.getLogger().logException(
                                this.getClass(), 
                                e, "Unable to save data, bad file name " + 
                                uri.toString()
                                );

                return;
            }
        }
    }
    
    /**
     * <p>
     * Save the document to its default location.  
     * </p>
     * <p>
     * If there is no
     * allocated document we return.  If there are no changes to the
     * document we return.  If the document has no default file
     * location we call <code>{@link #menuFileSaveAs()}</code> then
     * return.  If we get this far, we simply save the document
     * to its default location.
     * </p>
     * 
     * @since  May 11, 2010
     * @author Christopher K. Allen
     */
    public void menuFileSave() {

        if (this.docMain.getSource() == null) {
            this.menuFileSaveAs();
            return;
        }
        
        if (this.docMain.hasChanges()) {
            int iResp = this.docMain.displayConfirmDialog(
                    "WARNING!", 
                    "Overwrite existing data in " + docMain.getSource().toString() + "?"
                    );
            if (iResp == XalDocument.NO_OPTION) {
                this.menuFileSaveAs();
                return;
            }
        }
        
        // Overwrite the current data set
        this.docMain.saveDocument();
    }
    
    /**
     * Closes the given application document (managed by the
     * application).  Document asks for user confirmation and 
     * updates/saves data as necessary.
     *
     * @since  Jul 31, 2009
     * @author Christopher K. Allen
     */
    public void menuFileClose() {

        if ( this.docMain.hasChanges() && this.docMain.warnUserOfUnsavedChangesWhenClosing() ) {
            if ( !this.getMainWindow().userPermitsCloseWithUnsavedChanges() )  
                return;
        }

        this.docMain.closeDocument();
//
//        int cntCmps = this.winMain.getComponentCount();
//        for (int i=0; i<cntCmps; i++) {
//            Component cmp = this.winMain.getComponent(i);
//
//            cmp.setBackground( Color.LIGHT_GRAY );
//        }
        
        getEventLogger().logInfo(this.getClass(), "Closing current document");

        this.docMain = null;
    }
    
    /**
     * Exports the document data to disk in 
     * the format of the <tt>WireScan</tt> application.
     * The exported file can then be used by the 
     * <tt>WireAnalysis</tt> application for post
     * processing.
     * 
     * @since  Mar 22, 2010
     * @author Christopher K. Allen
     */
    public void menuFileExport() {
        // Get the measurement data
        MeasurementData     datMsmt = this.docMain.getMeasurementData();
        
        // Check if there is any data to export
        if (datMsmt.getDataSet().size() == 0)
            this.docMain.displayError("ERROR", "No measurement data to export.");
        
        //Initialize the file chooser
        AcceleratorApplication  appGbl  = AcceleratorApplication.getAcceleratorApp();
        JFileChooser            chrSave = appGbl.getSaveFileChooser();
        File                    fileDir = appGbl.getDefaultDocumentFolder();
        File                    fileDef = this.wireAnalysisFileName(fileDir);
        chrSave.setDialogTitle("Save data to disk in Wire Analysis readable format.");
        chrSave.setSelectedFile(fileDef);
        
        // Save file and respond to result
        int                     intResult = chrSave.showSaveDialog(this.getMainWindow());
        if (intResult == JFileChooser.APPROVE_OPTION) {
            File            file = chrSave.getSelectedFile();

            try {
                FileWriter              osDisk   = new FileWriter(file);
                WireAnalysisFormatter   fmtrWAnl = new WireAnalysisFormatter();
                String                  strFmttd = fmtrWAnl.exportWireAnalAppFmt(datMsmt);
                osDisk.write(strFmttd);
                osDisk.close();
                
                getEventLogger().logInfo(this.getClass(), "Exporting data");

            } catch (IOException e) {
                e.printStackTrace();
                this.getMainDocument().displayError("Export Failed!", 
                             "Export to file " + 
                              file.toString() + 
                             " failed due to an internal write exception!", 
                             e);
                getEventLogger().logException(getClass(), e, "Document save failure due to write exception");
            }
        }
    }
    
    
    /**
     * A violent exit.  For the moment, simply calls
     * <code>{@link System#exit(int)}</code>.
     * 
     * @since  May 11, 2010
     * @author Christopher K. Allen
     */
    public void menuFileExit() {
        
        // Log the user request
        getEventLogger().logInfo(this.getClass(), "User requested application termination");
        
        // Check for unsaved machine configuration and confirm with user
        if (MainConfiguration.getInstance().isDirty()) {
            int intResponse = JOptionPane.showConfirmDialog(this.getMainWindow(), 
                    "Unsaved machine configuration. Continue exiting?", 
                    "WARNING", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE
                    );
            
            if (intResponse == JOptionPane.CANCEL_OPTION)
                return;
        }
        
        // This method checks for unsaved document data and confirms with the user
        //  Exit is performed if all checks out
        MainApplication.getAcceleratorApplication().quit();
    }
    
    
//    /*
//     * Device Menu Commands
//     */
//    
//    /**
//     *  Restores the wire scanner devices to a configuration set
//     *  saved earlier by the user.
//     *
//     * @author Christopher K. Allen
//     * @since  Apr 23, 2012
//     */
//    public void menuDeviceConfigRestore() {
//        
//    }
//    
//    /**
//     *  Saves the current wire scanner device configuration to the
//     *  current device configuration file.
//     *
//     * @author Christopher K. Allen
//     * @since  Apr 23, 2012
//     */
//    public void menuDeviceConfigStore() {
//        
//    }
//    
//    /**
//     *  Saves the current wire scanner device configuration to a
//     *  new device configuration file.
//     *
//     * @author Christopher K. Allen
//     * @since  Apr 23, 2012
//     */
//    public void menuDeviceConfigStoreAs() {
//        
//        // Get the file chooser dialogue
//        AcceleratorApplication appGbl  = MainApplication.getAcceleratorApplication();
//        JFileChooser           chrSave = appGbl.getSaveFileChooser();
//
//        // Initialize the file chooser dialogue
//        File                   fileDir = appGbl.getDefaultDocumentFolder();
////        File                   fileDir = chrSave.getCurrentDirectory();
//        File                   fileDef = this.timeStampFileName(fileDir);
//        chrSave.setSelectedFile(fileDef);
//
//        // Retrieve the file name
//        MainWindow  winMain    = this.getMainWindow();
//        int         intResult  = chrSave.showSaveDialog(winMain);
//        //File        fileNewDir = chrSave.getCurrentDirectory();
//
//        if (intResult == JFileChooser.APPROVE_OPTION) {
//            File    file = chrSave.getSelectedFile();
//            URI     uri  = file.toURI();
//
//            try {
//                this.docMain.saveDocumentAs( uri.toURL() );
//                
//            } catch (MalformedURLException e) {
//                winMain.displayError("Save Error", "Bad file name " + uri.toString());
//                winMain.getLogger().logException(
//                                this.getClass(), 
//                                e, "Unable to save data, bad file name " + 
//                                uri.toString()
//                                );
//
//                return;
//            }
//        }
//    }
    
    /*
     * Application Functions
     */
    
    /**
     * Creates and displays the applications start-up 
     * splash window.  Parameters for the splash window
     * are taken from the application properties.
     *
     * @author Christopher K. Allen
     * @since  Jan 14, 2011
     */
    public void displaySplashScreen() {
        
        // Display the banner
        SplashWindow    scrBanner = this.createSplashScreen();
        int             cntTime   = AppProperties.SPLASH.TIME.getValue().asInteger();
        
        scrBanner.splash(cntTime);
    }
    
    /**
     * Creates an application preferences dialog and displays it.
     *
     * @author Christopher K. Allen
     * @since  Jan 20, 2011
     */
    public void displayPreferencesDialog() {
        
        Class<AppProperties>   clsMgr   = AppProperties.class;
        PropertyDisplayDialog  dlgProps = new PropertyDisplayDialog(this.getMainWindow(), clsMgr);
        
        dlgProps.setVisible(true);
    }

//    /**
//     * Used by the application's main document
//     * object to set the application's main
//     * window reference.  The main window
//     * is created in the main document object
//     * so this type of approach is necessary. 
//     *
//     * @param winMain   the application's main GUI interface
//     * 
//     * @since  Aug 17, 2009
//     * @author Christopher K. Allen
//     * 
//     * @see     MainDocument#makeMainWindow()
//     */
//    void setMainWindow(MainWindow winMain) {
//        this.winMain = winMain;
//    }

    
    
    /*
     * AbstractApplicationAdaptor Overrides
     */

    /**
     * Returns the application name.
     * 
     * @return  application name
     *
     * @since 	Jun 8, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#applicationName()
     */
    @Override
    public String applicationName() {
        return STR_APP_NAME;
    }
    
    /**
     * We just log the event.
     * 
     * @since 	Jul 29, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#applicationFinishedLaunching()
     */
    @Override
    public void applicationFinishedLaunching() {
        LGR_APP_EVT.logInfo(this.getClass(), "Application finished launching.");
    }




//    /**
//     *
//     * @since 	Jul 29, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.extension.application.AbstractApplicationAdaptor#applicationWillOpenInitialDocuments()
//     */
//    @Override
//    public void applicationWillOpenInitialDocuments() {
//        super.applicationWillOpenInitialDocuments();
//    }

    /**
     * Save the current bug report then close down the
     * application logging service.
     *
     * @since 	Jul 29, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#applicationWillQuit()
     */
    @Override
    public void applicationWillQuit() {
        LGR_APP_EVT.logInfo(this.getClass(), "Application shutting down: " + STR_APP_NAME);
        LGR_APP_EVT.endLogging();
    }

    /**
     * Registers the application specific commands with the
     * application command manager.  Note that the command 
     * manager works the the <tt>XAL</tt> GUI framework's
     * <code>Commander</code> object.
     * 
     * @since 	Jul 29, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#customizeCommands(xal.extension.application.Commander)
     * @see xal.extension.application.Commander
     * @see xal.app.pta.cmdmgt.CommandSet
     */
    @Override
    public void customizeCommands(Commander commander) {
        try {
            this.cmdsFile = new FileMenuCommands( this );
            this.cmdsFile.register(commander);

//            this.cmdsDevCfg = new DeviceMenuCommands( this );
//            this.cmdsDevCfg.register(commander);
            
        } catch (InstantiationException e) {
            String      strMsg = "MainWindow#customizeCommands(): " +
                                "Main window command set creation failure"; 
 
            this.getMainDocument().displayError("Fatal Error", strMsg);
            getEventLogger().logException(this.getClass(), e, 
                            "FATAL ERROR: Main window command set creation failure");
        }

    }

    /**
     * <p>
     * Return the list of file suffixes which the
     * PTA document objects recognize.  (I'm
     * not sure why this method is not 
     * a member of the document class.)
     * </p>
     *
     * @return  an array of recognized file suffix strings
     *  
     * @since   Jun 8, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#writableDocumentTypes()
     */
    @Override
    public String[] writableDocumentTypes() {
        return MainApplication.ARR_DOCTYPES_APP;
    }
    
    /**
     *
     * @since   Jun 8, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.AbstractApplicationAdaptor#readableDocumentTypes()
     */
    @Override
    public String[] readableDocumentTypes() {
        return MainApplication.ARR_DOCTYPES_APP;
    }



    /*
     * ApplicationAdaptor Overrides
     */

    /**
     * <p>
     * Creates a new document object for this application.  The
     * document is initialized from persistent data using the 
     * storage location of the given URL.  
     * </p>
     * 
     * @return  The new initialized document object for the application,
     *          <code>null</code> if the current document failed to close. 
     *
     * @since 	Jun 8, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.ApplicationAdaptor#newDocument(java.net.URL)
     */
    @Override
    public XalDocument newDocument(java.net.URL url) {
        if (this.docMain != null)
            if (!docMain.closeDocument())
                return null;
        
        this.docMain = new xal.app.pta.MainDocument();
//        this.docMain = new xal.app.pta.MainDocument(this);
        this.docMain.openDocument(url);

        getEventLogger().logInfo(this.getClass(), "A new document has been created at " + url.toString());
        
        return this.docMain;
    }
    
    /**
     * <p>
     * Instantiate and return an empty document object for the
     * application.
     * </p>
     * <p>
     * Note that  
     * the application document is the data container for the 
     * <tt>Document/View/Controller</tt> software architecture 
     * pattern.  Thus, each new document object is almost a 
     * separate instance of the application.
     * </p>
     * 
     * @return an empty document object for the PTA application
     * 
     * @since 	Jun 8, 2009
     * @author  Christopher K. Allen
     *
     * @see xal.extension.application.ApplicationAdaptor#newEmptyDocument()
     */
    @Override
    public XalDocument newEmptyDocument() {
        if (this.docMain != null)
            if (!docMain.closeDocument())
                return null;
        
        this.docMain = new xal.app.pta.MainDocument();
//        this.docMain = new xal.app.pta.MainDocument(this);

        getEventLogger().logInfo(this.getClass(), "A new empty document has been created.");
        
        return this.docMain;
    }

    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Generates a file name for the current document based upon
     * the time stamp which the measurement was taken and the
     * given directory.
     *
     * @param fileDir   the directory in which the file is to be located
     * 
     * @return  file object representing the file name, with full path included
     * 
     * @since  Apr 29, 2010
     * @author Christopher K. Allen
     */
    private File        timeStampFileName(File fileDir) {
        
        // Generate the time stamp
        DateFormat fmtTmStp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss"); 
        Date       datTmStp = this.docMain.getMeasurementData().getTimeStamp();
        
        // Generate the file name
        String     strTmStp  = fmtTmStp.format(datTmStp);
        String     strFileNm = strTmStp + "." + MainApplication.getDataFileExtension();
        
        // Add the full path and return
        File                   fileFullPath = new File(fileDir, strFileNm);
        
        return fileFullPath;
    }
    
    /**
     * Generates a file name for an export of the current data to the
     * <code>WireAnalysis</code> application format.
     *
     * @param fileDir   the default directory
     * 
     * @return          file with full path
     * 
     * @since  Aug 18, 2010
     * @author Christopher K. Allen
     * @param datMsmt 
     */
    private File        wireAnalysisFileName(File fileDir) {
        // Generate the time stamp
        DateFormat fmtTmStp = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss"); 
        Date       datTmStp = this.docMain.getMeasurementData().getTimeStamp();
        
        String     strFileNm = "WireAnalysisFmt-" + fmtTmStp.format(datTmStp) + ".pta.txt";
        
        File       fileFullPath = new File(fileDir, strFileNm);
        
        return fileFullPath;
    }
    
    /**
     * Creates the applications start-up 
     * splash window.  Parameters for the splash window
     * are taken from the application properties.
     *
     * @return  splash screen for application
     *
     * @author Christopher K. Allen
     * @since  Jan 26, 2011
     */
    private SplashWindow createSplashScreen() {
        
        // Get the screen size
        int         szScrWd = AppProperties.SPLASH.WIDTH.getValue().asInteger();
        int         szScrHt = AppProperties.SPLASH.HEIGHT.getValue().asInteger();
        Dimension   dimScr  = new Dimension(szScrWd, szScrHt);
    
        
        // Get the splash screen image
        String  strUrlIcon = AppProperties.SPLASH.ICON.getValue().asString();
        ImageIcon   icnScr = PtaResourceManager.getImageIcon(strUrlIcon);
    
        
        // Splash screen title
        String  strTitle   = AppProperties.SPLASH.TITLE.getValue().asString();
        
        
        // Create splash screen
        SplashWindow    scrBanner = new SplashWindow(dimScr, icnScr, strTitle);
        
        
        // Add optional parameters to splash screen object
        String  strText  = AppProperties.SPLASH.TEXT.getValue().asString();
        String  strAuths = AppProperties.SPLASH.AUTHORS.getValue().asString();
        String  strCpyRt = AppProperties.SPLASH.COPYRT.getValue().asString();
        Font    fntLarge = AppProperties.SPLASH.LGFONT.getValue().asFont();
        Font    fntSmall = AppProperties.SPLASH.SMFONT.getValue().asFont();
        
        scrBanner.setText(strText, fntLarge);
        scrBanner.setAuthors(strAuths, fntSmall);
        scrBanner.setCopyright(strCpyRt, fntSmall);
        
        
        return scrBanner;
    }
    
}



