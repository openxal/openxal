/*
 * AppMain.java
 *
 * Created on July, 2008 
 *
 * Copyright (c) 2008 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ptsd;


import xal.application.Application;
import xal.application.ApplicationAdaptor;
import xal.application.XalDocument;
import xal.smf.application.AcceleratorApplication;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;


/**
 * AppMain is the ApplicationAdaptor for the PTSD application.
 *
 * @author  Christopher K. Allen
 */
public class PtsdMain extends ApplicationAdaptor {

    
    
    
    
    /*
     * --------- Global application management --------------------------------- 
     */
    

    
    /*
     * Global Constants
     */
    
    /**  String */
    public static final String STR_LOGGER_ID = "xal.app.ptsd";

    
    
    /*
     * Class Attributes
     */
    
     /** Logging object for the PTSD application */
     private static final Logger         PTSD_LOGGER;

     /** Handler object for PTSD logger */
     private static final Handler        PTSD_HANDLER;
    
     
     
     /*
      * Global Methods
      */
     
     /**
      * Initialize the class attributes 
      */
     static      {
         PTSD_HANDLER = new ConsoleHandler();
         
         PTSD_LOGGER = Logger.getLogger(STR_LOGGER_ID);
         PTSD_LOGGER.addHandler(PTSD_HANDLER);
         
     }

     
    /**
     * <p>
     * <b>getAppLogger</b> - Returns the global logger object for
     * the <code>PTSD</code> application.
     * </p>
     * <p>
     * Classes and components within the <code>PTSD</code> application
     * should use this singleton logger object when reporting events.
     * Doing so consolidates the appication logging information 
     * for profiling and error analysis.
     * </p>
     *
     * @return  <code>PTSD</code> global logging object
     * 
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    public static final Logger       getAppLogger()     {
         return PtsdMain.PTSD_LOGGER;
     }


    
    /**
     * Return the logging handler used by the <code>PTSD</code>
     * application logger. 
     *
     * @return the global logging handler object
     *
     * @since  Jul 28, 2008
     * @author Christopher K. Allen
     */
    public static Handler getAppLogHandler() {
        return PTSD_HANDLER;
    }



    
    
    /*
     * --------- Application management --------------------------------- 
     */
    

    /**
     * Specifies the name this application.
     * 
     * @return This application's name.
     * 
     * @since   Jul 22, 2008
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.AbstractApplicationAdaptor#applicationName()
     */
    @Override
    public String applicationName() {
        return "PTSD";
    }



    /*
     * --------- Document management -------------------------------------------
     */

    /**
     * Returns the suffices of files that this application recognizes.
     * 
     * @return String array of recognized file extensions.
     */
    @Override
    public String[] readableDocumentTypes() {
        return new String[] {"txt"};
    }


    /**
     * Returns the suffices of files this application can write to disk.
     * 
     * @return Suffices of writable files
     */
    @Override
    public String[] writableDocumentTypes() {
        return new String[] {"txt"};
    }


    /**
     * Returns an new, uninitialized, instance of a 
     * <code>PULPTAFE</code> application document.
     * 
     * @return An uninitialized instance the application document.
     */
    @Override
    public XalDocument newEmptyDocument() {
        return new PtsdDocument();
    }


    /**
     * Returns a new instance of a <code>PULPTAFE</code>
     * document which has been initialized with the
     * data file in the given URL.
     * 
     * @param url       The URL of the initializing data file.
     * 
     * @return An instance of my custom document.
     */
    @Override
    public XalDocument newDocument(java.net.URL url) {
        return new PtsdDocument(url);
    }


    // --------- Application events --------------------------------------------

    /** 
     * Capture the application launched event and print it.  This is an optional
     * hook that can be used to do something useful at the end of the application launch.
     * 
     * @since   Jul 22, 2008
     * @author  Christopher K. Allen
     * 
     * @see gov.sns.application.AbstractApplicationAdaptor#applicationFinishedLaunching()
     */
    @Override
    public void applicationFinishedLaunching() {
        System.out.println("PTSD has finished launching!");
    }


    /**
     * Constructor - nothing really to do here.
     */
    public PtsdMain() {    
    };




    /** 
     * The main method of the application.  Launches the application
     * and catches for any exceptions. 
     * 
     * @param args      Not used.
     *  
     **/
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
            AcceleratorApplication.launch( new PtsdMain() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
            System.exit(-1);
        }
    }
}

