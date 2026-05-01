/*
 * GenWindow.java
 *
 * Created on 06/16/2003 by cp3
 *
 */
 
package xal.app.pta;

import xal.extension.application.Commander;
import xal.extension.application.XalDocument;
import xal.extension.application.XalWindow;
import xal.app.pta.cmdmgt.AcceleratorCommands;
import xal.app.pta.cmdmgt.CommandSet;
import xal.app.pta.cmdmgt.ToolBarCommands;
import xal.app.pta.cmdmgt.WindowMenuCommands;
import xal.app.pta.daq.MeasurementData;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.rscmgt.PtaResourceManager;
import xal.app.pta.tools.logging.IEventLogger;
import xal.app.pta.tools.swing.PersistentTextEditorPane;
import xal.app.pta.tools.swing.TextEditorPane;
import xal.app.pta.view.ConfigHarpView;
import xal.app.pta.view.ConfigScanView;
import xal.app.pta.view.CourantSnyderView;
import xal.app.pta.view.HarpAcquisitionView;
import xal.app.pta.view.ConfigMachineView;
import xal.app.pta.view.ConfigRemotePrcgView;
import xal.app.pta.view.ConfigTriggeringView;
import xal.app.pta.view.ScanAcquisitionView;
import xal.app.pta.view.DataAnalysisView;
import xal.app.pta.view.DataInspectionView;
import xal.smf.Accelerator;

import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;



/**
 * <h1>MainWindow</h1>
 * <p>
 * This class is the main interface for the application document.
 * It is the "<tt>Local Controller</tt>" in the <tt>Model/View/Controller</tt>
 * application architecture.  As such, it marshals communications 
 * between the document and its views. It also manages the creation
 * and operation of the main GUI for the application 
 * local
 * </p>
 * <p>
 * Upon instantiation the <code>MainWindow</code> class saves a 
 * back-pointer to the document instance 
 * (type <code>{@link MainDocument}</code>), registers the
 * application commands with the application <code>{@link Commander}</code>
 * object, then constructs the main user interface components.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 16, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Jun 11, 2009
 * @author Christopher K. Allen
 */
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  May 2, 2014
 */
public class MainWindow extends XalWindow implements IDocView, ComponentListener {

    
    
    /*
     * Global Constants
     */
    



    /**  Serialization Version */
    private static final long serialVersionUID = 1L;

    

    /**
     * Maintains synchronism with the main application document.
     * Specifically, the user edits to the screen are saved as
     * comments to the document. 
     *
     * @since  May 6, 2010
     * @author Christopher K. Allen
     */
    public static class NotebookAction implements DocumentListener {

        /** The document */
        private final MainDocument            docMain;



        /**
         * Create a new <code>NotebookAction</code> object.
         *
         * @param docMain       the main document to receive (notes) updates
         *
         * @since     May 6, 2010
         * @author    Christopher K. Allen
         */
        public NotebookAction(MainDocument docMain) {
            this.docMain = docMain;
        }

        /**
         * Do nothing - We're not monitoring style changes now.
         *
         * @since       May 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
        }

        /**
         * Tell the (data) document that there are changes.
         *
         * @since       May 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            this.docMain.setHasChanges(true);
        }

        /**
         * Tell the (data) document that there are changes.
         *
         * @since       May 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            this.docMain.setHasChanges(true);
        }

    }


    
    
    /*
     * Instance Attributes
     */
    
    
    /** The application logger object */
    private final IEventLogger          logMain;
    

    
    /** Collection of all the command sets supported by this window 
     * and registered with the <code>Commander</code> object.
     */
    private Collection<CommandSet>      setCmds;
    
    

    //
    // GUI Interface
    //
    
    // 
    // Main User Frame
    
    /** This is the main window SWING object */
    private JPanel                      appMainPanel;
    
    /** The tabbed pane of the main window */
    private JTabbedPane                 paneMainTab;
 
    
    // 
    // Data acquisition frames
    
    /** The wire scanner data acquisition view       */
    private ScanAcquisitionView         viewDaqScan;
    
    /** The harp acquisition view */
    private HarpAcquisitionView         viewDaqHarp;
    
    
    //
    // Data analysis frames
    
    /** The measurement data inspection view */
    private DataInspectionView          viewMsmtInsp;
    
    /** The measurement data analysis view */
    private DataAnalysisView            viewMsmtAnal;
    
    /** The Courant-Snyder reconstruction from Measurements view */
    private CourantSnyderView    viewMsmtTwiss;
    
    
    //
    // Device configuration frames
    
    /** The scanner configuration panel */
    private ConfigScanView              viewCfgScan;
    
    /** The harp configuration/status panel */
    private ConfigHarpView              viewCfgHarp;
    
    /** The machine configuration panel */
    private ConfigMachineView           viewCfgMach;
    
    /** The timing configuration panel */
    private ConfigTriggeringView        viewCfgTime;
    
    /** The data processing window configuration view */
    private ConfigRemotePrcgView        viewCfgPrcg;
    
    
    //
    // Auxiliary tools
    
    /** the notes window */
    private TextEditorPane              paneNotes;
    
    /** the bug report view */
    private PersistentTextEditorPane    paneBugRpt;

    
    
    /*
     * GUI Components
     */
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>MainWindow</code> for the given
     * data document.
     *
     * @param docMain   main data document to which this window views.
     *
     * @since     Jun 11, 2009
     * @author    Christopher K. Allen
     */
    public MainWindow(MainDocument docMain) {
        super(docMain);

//        this.appMain = docMain.getApplication();
        this.logMain = MainApplication.getEventLogger();

        this.getDocument().registerView(this);
        this.addComponentListener(this);

        this.initFrame();
        this.initGuiComponents();
        this.buildMainGui();
    }
    


    /*
     * Application Attributes
     */
    
    /**
     * Returns the <tt>Document</tt> object
     * for this <tt>View</tt>.  The document
     * is returned using the appropriate type
     * <code>MainDocument</code> for this application.
     *
     * @return  document object to which this view is attached
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     */
    public MainDocument getDocument()        {
        
        // From constructor, document must be of type MainWindow
        //      Thus, it's okay to cast.
        return (MainDocument)super.document;
    }

//    /**
//     * Returns the main application instance associated
//     * with the main window. 
//     *
//     * @return  main application instance
//     * 
//     * @since  Nov 24, 2009
//     * @author Christopher K. Allen
//     */
//    public MainApplication getApplication() {
//        return this.appMain;
//    }
//    
    /**
     * Return the main logger object for the 
     * application.
     *
     * @return  application logger object
     * 
     * @since  Nov 25, 2009
     * @author Christopher K. Allen
     */
    public IEventLogger getLogger() {
        return this.logMain;
    }

    
    
    
    /*
     * GUI Attributes
     */

//    /**
//     * Return the notes view window of the main window. 
//     *
//     * @return notes window
//     *
//     * @since  Aug 18, 2009
//     * @author Christopher K. Allen
//     */
//    public TextEditorPane getNotesView() {
//        return this.paneNotes;
//    }
//
//    /**
//     * Return the bug report view window of the main window. 
//     *
//     * @return notes window
//     *
//     * @since  Aug 18, 2009
//     * @author Christopher K. Allen
//     */
//    public PersistentTextEditorPane getBugReportView() {
//        return this.paneBugRpt;
//    }

    
    
    /*
     * GUI Operations
     */
    
    /**
     * Toggles the note pad view on and
     * off.
     *
     * 
     * @since  Aug 18, 2009
     * @author Christopher K. Allen
     */
    public void toggleNotepad() {
        
        boolean bolVisible = this.paneNotes.isVisible();
        
        this.paneNotes.setVisible(!bolVisible);
        this.pack();
        this.repaint();
    }

    /**
     * Toggles the bug report view on and
     * off.
     *
     * 
     * @since  Aug 18, 2009
     * @author Christopher K. Allen
     */
    public void toggleBugReport() {
        if (this.paneBugRpt == null)
            return;
        
        boolean bolVisible = this.paneBugRpt.isVisible();
        
        this.paneBugRpt.setVisible(!bolVisible);
        this.pack();
        this.repaint();
    }
    
    /**
     * Toggles the wire scanner configuration screen
     * on and off.
     * 
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public void toggleScanConfigPanel() {
        this.toggleTabbedView(this.viewCfgScan, "Scan Configuration", "Configure scanner operation parameters");
    }
    
    /**
     * Toggles the wire harp configuration screen on and off.
     *
     * @author Christopher K. Allen
     * @since  May 2, 2014
     */
    public void toggleHarpConfigPanel() {
        this.toggleTabbedView(this.viewCfgHarp, "Harp Configuration", "Configure harp operation parameters");
    }
    
//    /**
//     * Toggles the harp data acquisition panel on and off.
//     *
//     * @author Christopher K. Allen
//     * @since  Mar 24, 2014
//     */
//    public void toggleHarpDaqPanel() {
//        this.toggleTabbedView(this.viewHarpDaq, "Harp Data Acquisition", "Perform harp profile measurements");
//    }
    
    /**
     * Toggles the machine configuration screen on and off.
     * 
     */
    public void toggleMachConfigPanel() {
        this.toggleTabbedView(this.viewCfgMach,"Machine Configuration", "Save/Restore configuration of machine configuration");
    }

    /**
     * Toggles the DAQ triggering configuration screen
     * on and off.
     *
     * 
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public void toggleTimeConfigPanel() {
        this.toggleTabbedView(this.viewCfgTime, "DAQ Triggering", "Configure acquisition timing");
    }

    /**
     * Toggles the data processing configuration screen
     * on and off.
     *
     * 
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    public void togglePrcgConfigPanel() {
        this.toggleTabbedView(this.viewCfgPrcg, "Data Processing", "Configure data processing window");
    }

    
    /*
     * IDocView Interface
     */
    
    /**
     * Update the notes pane display with the notes included in the
     * given data document.  
     *
     * @since   Mar 1, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.IDocView#updateMeasurementData(xal.app.pta.MainDocument)
     */
    @Override
    public void updateMeasurementData(MainDocument docMain) {
        MeasurementData datMsmt = docMain.getMeasurementData();

        this.paneNotes.setText(datMsmt.getNotes());
    }

    
    
     /*
      * ComponentListener Interface
      */

    /**
     * Not implemented.
     * 
     * @since   Jun 10, 2009
     * @author  Christopher K. Allen
     *
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Not implemented.
     *
     * @since   Jun 10, 2009
     * @author  Christopher K. Allen
     *
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Save the new size of the main window in the application
     * properties.
     *
     * @since   Jun 10, 2009
     * @author  Christopher K. Allen
     *
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
        Dimension       dimWinMain = this.appMainPanel.getSize();

        String          strValHt   = Integer.toString( dimWinMain.height );
        String          strValWd   = Integer.toString( dimWinMain.width );

        AppProperties.APP.SCR_HEIGHT.getValue().set( strValHt );
        AppProperties.APP.SCR_WIDTH.getValue().set( strValWd );

        //        this.setPreferredSize( dimWinMain );
        //        
        //        System.out.println("Screen size is " + dimWinMain);
    }

    
    /**
     *
     * @since   Jun 10, 2009
     * @author  Christopher K. Allen
     *
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent e) {
    }

    
    /*
     * XalDocumentListener Interface
     */
    
    /**
     * Overrides the base class implementation which calls <code>closeWindow()</code>.
     * This method does nothing.
     * 
     * @since Apr 27, 2012
     * @see gov.sns.application.XalWindow#documentWillClose(gov.sns.application.XalDocument)
     */
    @Override
    public void documentWillClose(XalDocument document) {
    }

    /**
     * Overrides the base class implementation. Both implementation do nothing.
     * 
     * @since Apr 27, 2012
     * @see gov.sns.application.XalWindow#documentHasClosed(gov.sns.application.XalDocument)
     */
    @Override
    public void documentHasClosed(XalDocument document) {
        this.closeWindow();
    }

    
    
    /*
     * XalWindow Overrides
     */

    /**
     * Clean up any unfinished application processes
     * then release resources.
     *
     * @since   Nov 24, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.XalWindow#closeWindow()
     */
    @Override
    public void closeWindow() {
        this.getDocument().setNotes( this.paneNotes.getText() );
        this.paneBugRpt.closeBackingStore();
        super.closeWindow();
    }

    /**
     * Customize any special button commands.
     *
     * @since   Jun 12, 2009
     * @author  Christopher K. Allen
     *
     * @see gov.sns.application.XalAbstractDocument#customizeCommands(gov.sns.application.Commander)
     */
    @Override
    public void customizeCommands(Commander commander) {

        this.setCmds = new LinkedList<CommandSet>();

        try {
            this.setCmds.add( new AcceleratorCommands(this) );
            this.setCmds.add( new WindowMenuCommands(this) );
            this.setCmds.add( new ToolBarCommands( this ) );
            
            for (CommandSet cmds : this.setCmds) {
                cmds.register(commander);
            }

        } catch (InstantiationException e) {
            String      strMsg = "MainWindow#customizeCommands(): " +
                                "Main window command set creation failure"; 
 
            JOptionPane.showMessageDialog(null, strMsg, "Fatal Error", JOptionPane.ERROR_MESSAGE);
            this.getLogger().logException(this.getClass(), e,
                            "FATAL ERROR: Main window command set creation failure");
            e.printStackTrace();
        }

    }


    
    /*
     * Support Methods
     */
    
    /**
     * Initializes the main window frame for the
     * PTA application.  Parameters from the 
     * <tt>Configuration.ini</tt> file are used
     * to display the window. 
     *
     * 
     * @since  Aug 3, 2009
     * @author Christopher K. Allen
     */
    private void initFrame()   {
        
        
        int szWidth  = AppProperties.APP.SCR_WIDTH.getValue().asInteger();
        int szHeight = AppProperties.APP.SCR_HEIGHT.getValue().asInteger();
        Dimension       dimFrame = new Dimension(szWidth, szHeight);
        
        // Build the main panel of the for the window
        this.appMainPanel = new JPanel();
        this.appMainPanel.setPreferredSize(dimFrame);
        this.appMainPanel.setLayout(new BoxLayout(appMainPanel, BoxLayout.Y_AXIS));
        
        String  strNameIcon = AppProperties.APP.ICON.getValue().asString();
        ImageIcon icnApp = PtaResourceManager.getImageIcon(strNameIcon);
        
        this.setIconImage(icnApp.getImage());
        this.appMainPanel.setVisible(true);

        this.getContentPane().add(this.appMainPanel);
    }
    
    
    /**
     * Build all the common GUI components used
     * by the application.
     *
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    private void initGuiComponents() {

        // The main GUI face
        this.paneMainTab = new JTabbedPane(SwingConstants.TOP);

        
        //
        // Auxiliary Tools
        
        // Build the measurement notes editor
        this.initNotesEditor();
      
        // Build the bug report editor
        this.initBugReport();

        
        //
        // Data Acquisition Views 
        
        // Wire scanner data acquisition view
        this.viewDaqScan = new ScanAcquisitionView(this.getDocument());
        this.getDocument().registerView(this.viewDaqScan);
//        MainApplication.getApplicationDocument().registerView(viewDaqScan);
        MainConfiguration.getInstance().registerView(this.viewDaqScan);
        MainScanController.getInstance().registerControllerListener(this.viewDaqScan);
        
        // Harp data acquisition view
        this.viewDaqHarp = new HarpAcquisitionView(this.getDocument());
        this.getDocument().registerView(this.viewDaqHarp);
        MainConfiguration.getInstance().registerView(viewDaqHarp);;

        
        // 
        // Data Processing Views
        
        // Measurement data inspection view
        this.viewMsmtInsp = new DataInspectionView(this.getDocument());
        this.getDocument().registerView(this.viewMsmtInsp);
        MainConfiguration.getInstance().registerView(this.viewMsmtInsp);
        
        // Measurement data analysis view
        this.viewMsmtAnal = new DataAnalysisView(this.getDocument());
        this.getDocument().registerView(this.viewMsmtAnal);
        MainConfiguration.getInstance().registerView(this.viewMsmtAnal);
        
        // Courant-Snyder reconstruction view
        this.viewMsmtTwiss = new CourantSnyderView(this.getDocument());
        this.getDocument().registerView(this.viewMsmtTwiss);
        MainConfiguration.getInstance().registerView(this.viewMsmtTwiss);
        
        
        //
        // Device Configuration Views
        
        // Build the wire scanner configuration window
        this.viewCfgScan = new ConfigScanView( MainConfiguration.getInstance() );
//        this.getDocument().registerView(this.viewDevCfg);
        MainConfiguration.getInstance().registerView(this.viewCfgScan);
        
        // Build the harp configuration window
        this.viewCfgHarp = new ConfigHarpView( MainConfiguration.getInstance() );
        MainConfiguration.getInstance().registerView(this.viewCfgHarp);
        
        // Build the machine configuration panel
        this.viewCfgMach = new ConfigMachineView( MainConfiguration.getInstance() );
//        this.getDocument().registerView(this.viewMachCfg);
        MainConfiguration.getInstance().registerView(this.viewCfgMach);

        
        // Create the accelerator hardware views
        //
         Accelerator smfAccel = this.getDocument().getAccelerator();
        
        // Build the timing configuration window
        this.viewCfgTime = new ConfigTriggeringView(smfAccel);
//        this.getDocument().registerView(this.viewTrgCfg);
        MainConfiguration.getInstance().registerView(this.viewCfgTime);

        // Build the remote processing parameter view (for setting up the device controller)
        this.viewCfgPrcg = new ConfigRemotePrcgView(smfAccel);
//        this.getDocument().registerView(this.viewPrcCfg);
        MainConfiguration.getInstance().registerView(this.viewCfgPrcg);

    }
    
    /**
     * Create and initialize the measurement notes
     * editor.
     *
     * 
     * @since  Jul 2, 2010
     * @author Christopher K. Allen
     */
    private void initNotesEditor() {

        // Get the editor dimensions
        int     szWd = AppProperties.BGRPRT.SCR_WD.getValue().asInteger();
        int     szHt = AppProperties.BGRPRT.SCR_HT.getValue().asInteger();
        Dimension       dimRprt = new Dimension(szWd, szHt);

        
        // The document notes editor
        this.paneNotes = new TextEditorPane();
        this.paneNotes.setBorder( new TitledBorder("Acquisition and Analysis Notes") );
        this.paneNotes.setVisible(false);
        this.paneNotes.setPreferredSize(dimRprt);
        
        NotebookAction     actEdit = new NotebookAction(this.getDocument());
        this.paneNotes.getDocument().addDocumentListener(actEdit);
        
        this.paneNotes.setText( this.getDocument().getNotes() );
//        this.getDocument().setNotebook( this.paneNotes.getDocument() );
    }

    /**
     * Creates and initializes the bug report/feature request
     * editor.
     *
     * 
     * @since  Jul 2, 2010
     * @author Christopher K. Allen
     */
    private void initBugReport() {
        
        // Create the bug report editor
        String  strUrlBugRpt = AppProperties.BGRPRT.FILE.getValue().asString();
        String  strCharset   = AppProperties.BGRPRT.CHARSET.getValue().asString();
        int     szHt         = AppProperties.BGRPRT.SCR_HT.getValue().asInteger();
        int     szWd         = AppProperties.BGRPRT.SCR_WD.getValue().asInteger();

        Dimension       dimRprt = new Dimension(szWd, szHt);
        File            fileBgRpt = MainApplication.createDropFile(strUrlBugRpt);
        
        try {
            this.paneBugRpt = null;
//            this.paneBugRpt = new PersistentTextEditorPane(strUrlBugRpt, strCharset );
            this.paneBugRpt = new PersistentTextEditorPane(fileBgRpt, strCharset );
            this.paneBugRpt.setLogger( this.getLogger() );

//            this.paneBugRpt.setText( this.appMain.getBugReport() );
            this.paneBugRpt.setBorder( new TitledBorder("Bug Report/Feature Request") );
            this.paneBugRpt.setVisible(false);
            this.paneBugRpt.setPreferredSize(dimRprt);
            
        } catch (NullPointerException e) {
            String      strMsg = "Invalid URL on Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();
            
        } catch (IllegalCharsetNameException e) {
            String      strMsg = "The character set " + strCharset + " is illegal for Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();

        } catch (UnsupportedCharsetException e) {
            String      strMsg = "No support for character set " + strCharset + " is available for Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();
            
        } catch (IllegalArgumentException e) {
            String      strMsg = "Unable to open Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();
            
        } catch (FileNotFoundException e) {
            String      strMsg = "Unable to open Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();
            
        } catch (SecurityException e) {
            String      strMsg = "Invalid user privileges for Bug Report File: " + strUrlBugRpt;
            this.getLogger().logError(this.getClass(), strMsg);
            this.displayError("Bug Reporting Unavailable", strMsg);
            e.printStackTrace();
            
        }
    }

    /**
     * Create the main window sub-views.
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    private void buildMainGui() {

        // Create tabs 
        this.paneMainTab.addTab("Wire Scanner Acquisition", this.viewDaqScan);
        this.paneMainTab.addTab("Harp Acquisition", this.viewDaqHarp);
        
        this.paneMainTab.addTab("Data Inspection", this.viewMsmtInsp);
        this.paneMainTab.addTab("Data Analysis", this.viewMsmtAnal);
        this.paneMainTab.addTab("Courant-Snyder", this.viewMsmtTwiss);

//        SignalProcessingView viewSigPrc = new SignalProcessingView(this);
//        this.getDocument().registerView(viewSigPrc);
//        this.paneMainTab.addTab("Signal Processing", viewSigPrc);
        
        this.appMainPanel.add(this.paneMainTab);
        this.appMainPanel.add(this.paneNotes);
        this.appMainPanel.add(this.paneBugRpt);

        this.pack();
    }

    /**
     * Toggles the given <code>JPanel</code> derived GUI component
     * in and out of the main tab pane (as a tabbed pane).
     * 
     * @param pnlTab    the panel to toggle visibility 
     * @param strTitle  panel's title as seen on its tab
     * @param strTip    tool tip text seen on mouse-over
     *
     * @since  Feb 1, 2010
     * @author Christopher K. Allen
     */
    private void toggleTabbedView(JPanel pnlTab, String strTitle, String strTip) {
        int     indTab = this.paneMainTab.indexOfComponent(pnlTab);
        
        // The panel is not visible - add it
        if (indTab == -1) {   
            int cntTabs   = this.paneMainTab.getTabCount();
            
            this.paneMainTab.insertTab(strTitle, null, pnlTab, strTip, cntTabs);
            this.paneMainTab.setSelectedComponent(pnlTab);
            this.paneMainTab.setEnabledAt(cntTabs, true);
    
        // The panel is already up - remove it
        } else {
            int indSel = this.paneMainTab.getSelectedIndex();
            
            this.paneMainTab.removeTabAt(indTab);
            this.paneMainTab.setSelectedIndex( indSel==0 ? 0 : (indSel-1) );
        }
    }




}
