/*
 * @(#)GenDocument.java          2.0 06/10/2009
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tennessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.pta;

import xal.extension.application.Commander;
import xal.app.pta.daq.ScannerData;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.tools.logging.IEventLogger;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.Accelerator;
import xal.extension.application.smf.AcceleratorDocument;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * <h1>Document class for the PTA Application</h1>
 * <p>
 * This class is the <tt>Model</tt> component in the
 * <tt>Model/View/Controller</tt> architecture for
 * the PTA application.
 * </p> 
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @author  Christopher K. Allen
 *
 * @version   2.0
 * @since   Jun 10, 2009
 */

public class MainDocument extends AcceleratorDocument implements DataListener{    




    /*
     * Global Constants
     */


    /**  Data format version */
    private static final long           LNG_VAL_FMTVER = 2L;

    /** The data label used to identify profile persistent data in external storage */
    public static final String          STR_TAG_DATA = "pta"; //$NON-NLS-1$



    /*
     * Local Attributes
     */

//    /** 
//     * The application main controller
//     *  
//     * <br/> We need this because the <code>MainWindow</code> object is 
//     * instantiated here.
//     */
//    private final MainApplication       appMain;

    
    //
    //  All interested parties
    //
    
    /** List of views attached to this document */
    private final List<IDocView>         lstViews;

    

    //
    // Application Measurement Data
    //

    /** All measurement data sets, one for each device */
    private MeasurementData              setData;          

    

    /*
     * Initialization
     */


    /**
     * Create a new <code>MainDocument</code> object and initializes
     * the machine configuration manager.
     *
     * @since     Jun 11, 2009
     * @author    Christopher K. Allen
     */
    public MainDocument() {
        this.lstViews = new LinkedList<IDocView>();

        super.loadDefaultAccelerator();
    }

    
    /*
     * Attributes
     */

    /**
     * Returns the main window for this document object.  The main window
     * object is referenced as a base type of <code>{@link MainWindow}</code> 
     * in a parent of this class.  So we simply case that object to a 
     * <code>MainDocument</code> type and return it.  The main window is 
     * assured to be of type <code>MainWindow</code> since we create it here.
     *
     * @return  the main window of this document as a <code>MainWindow</code> object
     *
     * @author Christopher K. Allen
     * @since  May 14, 2012
     */
    public MainWindow   getDocMainWindow() {
        return (MainWindow)this.getMainWindow();
    }
    /**
     * <p>
     * Returns the current measurement data.
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * I need to make the measurement data
     * immutable.
     * </p>
     *
     * @return  the current profile measurement set
     * 
     * @since  Mar 26, 2010
     * @author Christopher K. Allen
     */
    public MeasurementData getMeasurementData() {
        return this.setData;
    }
    
    /**
     * Returns the user's notes on the current measurement data.
     *
     * @return  user notes concerning the data, 
     *          <code>null</code> if measurement set is empty
     *
     * @author Christopher K. Allen
     * @since  Apr 12, 2012
     */
    public String   getNotes() {
        if (this.setData == null)
            return null;
        
        return this.setData.getNotes();
    }
    


    /*
     * Document Operations
     */

    /**
     * <p>
     * Register as a view onto this document.  The given
     * view object will then receive call-backs to
     * the <code>gov.sns.apps.sensei.IView{@link #acceleratorChanged()}.n 
     * document changes
     * </p>
     *
     * @param view      view object requesting update notifications
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     */
    public void registerView(IDocView view)        {
        this.lstViews.add(view);
    }


    /*
     * Data Operations
     */


    /**
     * Set the currently active measurement data for the
     * application.  The argument is a list of 
     * <code>{@link ScannerData}</code> objects, each one is
     * a data set produced by the DAQ device in the profile 
     * measurement.  The list then comprises all the data in a
     * profile scan involving multiple scanners.
     * 
     * @param datMsmt   list of acquired data sets, one for each wire device
     * 
     * @since  Mar 1, 2010
     * @author Christopher K. Allen
     */
    public void setMeasurementData(MeasurementData datMsmt) {

        // Set the new data
        this.setData = datMsmt;

        this.setHasChanges(true);

        // Notify all the document listeners
        for (IDocView view : this.lstViews) 
            view.updateMeasurementData(this);
    }


    /**
     *  Sets the user notes associated with the current data set.
     *  
     * @param strNotes  user notes on the current measurements
     *
     * @author Christopher K. Allen
     * @since  Apr 12, 2012
     */
    public void setNotes(String strNotes) {
        if (this.setData == null)
            return;
        
        this.setData.setNotes(strNotes);
        this.setHasChanges(true);

        // Notify all the document listeners
        for (IDocView view : this.lstViews) 
            view.updateMeasurementData(this);
    }
    
    /**
     * Responds to a change in the accelerator object
     * connected to this document.  We reset the accelerator
     * object associated with the application's configuration manager
     * (see <code>{@link MainConfiguration#getInstance()})</code>).
     * Note that the configuration manager will then notify all the
     * registered views (i.e., <code>IConfigView</code> objects) that 
     * the accelerator has changed.
     * 
     * @since   Nov 12, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.xal.smf.application.AcceleratorDocument#acceleratorChanged()
     */
    @Override
    public void acceleratorChanged() {
//        for (IDocView view :this.lstViews) {
//            view.updateAccelerator(this);
//        }
        
        Accelerator         smfAccel = this.getAccelerator();
        MainConfiguration   cfgMain  = MainConfiguration.getInstance();
        
        cfgMain.resetAccelerator(smfAccel);
    }


    /*
     * XalDocument Overrides
     */

    /**
     * Attempt to load the user's default accelerator.  If no default accelerator is specified,
     * then prompt the user to specify the default optics path.  The document's accelerator
     * file is set to the user's default accelerator.
     * 
     * @return true if the default accelerator was successfully loaded
     */
    @Override
    public boolean loadDefaultAccelerator() {
        return super.loadDefaultAccelerator();
    }

    /**
     * <p>
     * Called to indicate that the data in this document
     * has been changed.
     * </p>
     * <p>
     * Currently this method current simply calls the over-ridden
     * super-class method. 
     * </p>
     * 
     * @since 	Jun 15, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.XalDocument#setHasChanges(boolean)
     */
    @Override
    public void setHasChanges(boolean changeStatus) {
        super.setHasChanges(changeStatus);
    }


	/**
	 * Overriden to disable warning users of unsaved changes.
	 * @return false to disable the warning of unsaved changes
	 */
	@Override
	public boolean warnUserOfUnsavedChangesWhenClosing() {
		return false;
	}


    /**
     * Ask the user if he/she really wants to close.  If so,
     * free the resources and close the document.
     *
     * @since       Jul 29, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.XalDocument#closeDocument()
     */
//    @Override
//    public boolean closeDocument() {
//
//        documentListenerProxy.documentWillClose(this);
//        super.willClose();
//        documentListenerProxy.documentHasClosed(this);
//        super.freeResources();
//
//        return true;
//    }




    /*
     * XalAbstractDocument Overrides
     */

    /**
     * <p>Instantiates the application's GUI frame
     * if necessary.
     * </p>
     * <p>
     *  There is only one main window for the
     *  application.  The application's main window object is 
     *  created here only  
     *  if necessary.  The main window is
     *  created once, only if the current value is
     *  <code>null</code>.  Otherwise, this document
     *  adopts the previously instantiated
     *  main window (referenced in the main application object) 
     *  as its main window.
     *  </p> 
     *  <p>
     *  Admittedly not the most desirable way to do
     *  things, but this is really the most practical
     *  considering the architecture of the XAL GUI
     *  framework. 
     *  </p>
     *
     * @since 	Jun 11, 2009
     * @author  Christopher K. Allen
     *
     */
    @Override
    public void makeMainWindow() {
            MainWindow  winMain = new MainWindow(this);
            
            this.mainWindow = winMain;
//            this.getApplication().setMainWindow( winMain );

        //        MainWindow      win = this.getApplication().getMainWindow();
//        MainWindow      win = null;
//        
//        if (win == null) {
//            win = new MainWindow(this);
//            this.mainWindow = win;
//            this.getApplication().setMainWindow(win);
//            
////            super.removeXalDocumentListener( win );
//        } else {
//            this.mainWindow = win;
//
//        }

    }

    /**
     * Customize any special button commands.
     *
     * @since 	Jun 12, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.XalAbstractDocument#customizeCommands(gov.sns.application.Commander)
     */
    @Override
    public void customizeCommands(Commander commander) {

    }


//    /**
//     *
//     * @since   Jul 29, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see gov.sns.application.XalAbstractDocument#willClose()
//     */
//    @Override
//    protected void willClose() {
//        // TODO Auto-generated method stub
//        super.willClose();
//    }




    /*
     * Data Persistence
     */

    /**
     * Opens the previously saved document on disk, indicated
     * by the given URL, then
     * loads it into the main document.  
     * <p>
     * <em>This is no longer true</em>
     * <br>
     * If the argument URL
     * is <code>null</code>, then an open file dialogue is 
     * presented to the user where he or she may selected the
     * disk file.
     * </p>  
     *
     * @param url       data document file location
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public void openDocument(java.net.URL url) {
        try {
            DataAdaptor daDoc  = XmlDataAdaptor.adaptorForUrl(url, false);
            DataAdaptor daData = daDoc.childAdaptor(MainDocument.STR_TAG_DATA); 

            this.update( daData );
            this.setHasChanges(false);
            this.setSource(url);
            // This is not necessary since the MainDocument#update() method
            //  fires the updateMeasurementData event when setting the new measurement data.
//            for (IDocView view : this.lstViews) 
//                view.updateMeasurementData(this);

            getLogger().logInfo(this.getClass(), "Loaded application data from file " + url); //$NON-NLS-1$

        } catch(Exception e) {
            super.displayError("Open Failed!", url + " data format corrupted", e); //$NON-NLS-1$ //$NON-NLS-2$
            getLogger().logException(getClass(), e, "Open Failed! Format corrupted for " + url); //$NON-NLS-1$
        }
    }

    /**
     * <p>
     * Saves the current document data to the current
     * data file (i.e., it is over-written).  If the 
     * file exists, the user is prompted for this over-write.
     * </p>
     * <p>
     * <em>This is no longer true</em>
     * If there is no data file as of yet, then we defer to 
     * the <code>{@link MainDocument#saveDocumentAs(URL)}</code>
     * method by passing a <code>null</code> argument.
     * </p>
     *
     * @since   Nov 7, 2009
     * @author  Christopher K. Allen
     *
     */
    @Override
    public void saveDocument() {
        this.saveDocumentAs(this.getSource());
    }

    /**
     * Save the document to the specified URL. If the URL is
     * <code>null</code> then nothing is done.  If the 
     * 
     * <p>
     * <em>This has been changed</em>
     * <br/>If the URL is
     * <code>null</code>, then a file chooser dialogue is presented
     * to the user in order to select a file location and name.
     * </p>
     * 
     * @param url   The URL to which the document should be saved.
     */
    @Override
    public void saveDocumentAs(java.net.URL url) {
        //        if (url == null) {
        //            // Get the file chooser dialogue
        //            AcceleratorApplication appGbl  = MainApplication.getAcceleratorApplication();
        //            JFileChooser           chrSave = appGbl.getSaveFileChooser();
        //            
        //            // Initialize the file chooser dialogue
        //            File                   fileDir = appGbl.getDefaultDocumentFolder();
        ////            File                   fileDir = chrSave.getCurrentDirectory();
        //            File                   fileDef = this.generateFileName(fileDir);
        //            chrSave.setSelectedFile(fileDef);
        //            
        //            // Retrieve the file name
        //            MainWindow  winMain    = this.getMainView();
        //            int         intResult  = chrSave.showSaveDialog(winMain);
        //            //File        fileNewDir = chrSave.getCurrentDirectory();
        //            
        //            if (intResult == JFileChooser.APPROVE_OPTION) {
        //                File    file = chrSave.getSelectedFile();
        //                URI     uri  = file.toURI();
        //                try {
        //                    url = uri.toURL();
        //
        //                } catch (MalformedURLException e) {
        //                    winMain.displayError("Save Error", "Bad file name " + uri.toString());
        //                    winMain.getLogger().logException(this.getClass(), e, "Unable to save data, bad file name " + uri.toString());
        //                    
        //                    return;
        //                }
        //
        //            }
        //        }

        if (url == null) 
            return;

        if (url.equals(this.getSource()) && !hasChanges())
            return;

        try {
            XmlDataAdaptor daptDoc = XmlDataAdaptor.newEmptyDocumentAdaptor();
            daptDoc.writeNode(this);
            daptDoc.writeToUrl(url);

            this.setHasChanges(false);
            this.setSource(url);

            this.getLogger().logInfo(this.getClass(), "Saving application data to file " + url.toString()); //$NON-NLS-1$

        } catch(XmlDataAdaptor.WriteException e) {
            displayError("Save Failed!",  //$NON-NLS-1$
                    "Save to file " +  //$NON-NLS-1$
                    url.toString() + 
                    " failed due to an internal write exception!",  //$NON-NLS-1$
                    e);
            this.getLogger().logException(getClass(), e, "Document save failure, URL = " + url.getFile()); //$NON-NLS-1$
        }
    }



    //    /**
    //     * Save the document to the specified URL.
    //     * 
    //     * @param   url The URL to which the document should be saved.
    //     */
    //    @Override
    //    public void saveDocumentAs(java.net.URL url) {
    //        // Check if there is anything to do
    //        if (this.hasChanges() == false)
    //            return;
    //        
    //        try {
    //            URI                 uriDoc = url.toURI();
    //            File                pthDoc = new File(uriDoc);
    //            FileOutputStream    fosDoc = new FileOutputStream(pthDoc);
    //            XMLEncoder          xecDoc = new XMLEncoder(fosDoc);
    //            
    //            for (Map.Entry<AcceleratorNode, ScannerData> entry : this.mapMsmtData.entrySet()) {
    //                ScannerData datMsmt = entry.getValue();
    //                
    //                xecDoc.writeObject(datMsmt);
    //            }
    //            
    //            this.setHasChanges(false);
    //            
    //        }  catch(NullPointerException e) {
    //            String      strMsg = "Unable to save data to file " + url.toString() + 
    //                                 ". Bad URL.";
    //            this.getLogger().logException(getClass(), e, strMsg);
    //            this.displayError("Save Failed!", strMsg, e);
    //            
    //        } catch (IllegalArgumentException e) {
    //            String      strMsg = "Unable to save data to file " + url.toString() + 
    //            ". Bad URL.";
    //            this.getLogger().logException(getClass(), e, strMsg);
    //            this.displayError("Save Failed!", strMsg, e);
    //
    //        } catch (URISyntaxException e) {
    //            String      strMsg = "Unable to save data to file " + url.toString() + 
    //            ". Bad URL.";
    //            this.getLogger().logException(getClass(), e, strMsg);
    //            this.displayError("Save Failed!", strMsg, e);
    //            
    //        } catch (FileNotFoundException e) {
    //            String      strMsg = "Unable to save data to file " + url.toString() + 
    //            ". Unable to create the file.";
    //            this.getLogger().logException(getClass(), e, strMsg);
    //            this.displayError("Save Failed!", strMsg, e);
    //            
    //        } catch (SecurityException e) {
    //            String      strMsg = "Unable to save data to file " + url.toString() + 
    //            ". Access rights violation.";
    //            this.getLogger().logException(getClass(), e, strMsg);
    //            this.displayError("Save Failed!", strMsg, e);
    //            
    //        }
    //    }




    /*
     * DataListener Interface
     */


    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return AppProperties.APP.TAG_DATA.getValue().asString();
    }

    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * The new measurement data is read in from the given data source. 
     * The {@link #setMeasurementData(MeasurementData)} method is called
     * to install the new data, thus, firing an {@link IDocView#updateMeasurementData(MainDocument)}
     * event.
     * 
     * @param daptSrc The data adaptor corresponding to this object's data 
     *                  node.
     * 
     * @throws IllegalArgumentException  the data format behind the data adaptor is unrecognized 
     */
    public void update(DataAdaptor daptSrc) throws IllegalArgumentException {

        // Check the version information
        long     lngVer = 0;
        
        if (daptSrc.hasAttribute("ver")) //$NON-NLS-1$
            lngVer = daptSrc.longValue("ver"); //$NON-NLS-1$
        
        if (lngVer > LNG_VAL_FMTVER)
            throw new IllegalArgumentException("Unknown format version " + lngVer); //$NON-NLS-1$

//        // Get the notes - These are part of the data now
//        this.docNotes.update(daptSrc);

        // Get the data
        MeasurementData datMsmt = MeasurementData.load(daptSrc);
        this.setMeasurementData(datMsmt);
    }

    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * 
     * @param daptSnk The data adaptor corresponding to this object's data 
     * node.
     */
    public void write(DataAdaptor daptSnk) {
        daptSnk.setValue("ver", LNG_VAL_FMTVER); //$NON-NLS-1$

//        this.docNotes.write(daptSnk);
        this.setData.write(daptSnk);
    }

    /*
     * Support Methods
     */

    /**
     * Returns the (singleton) event logger for the
     * application.
     *
     * @return  application event logger
     * 
     * @since  Mar 2, 2010
     * @author Christopher K. Allen
     */
    private IEventLogger        getLogger() {
        return MainApplication.getEventLogger();
    }


}
