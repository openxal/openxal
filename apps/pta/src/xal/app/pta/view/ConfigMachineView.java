/*
 * ConfigMachineView.java
 *
 * @author Christopher K. Allen
 * @since  Apr 30, 2012
 *
 */

package xal.app.pta.view;

import xal.app.pta.IConfigView;
import xal.app.pta.MainApplication;
import xal.app.pta.MainConfiguration;
import xal.app.pta.daq.MachineConfig;
import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.view.cmn.DeviceSelectorPanel;
import xal.tools.apputils.files.DefaultFolderAccessory;
import xal.tools.apputils.files.FileFilterFactory;
import xal.tools.apputils.files.RecentFileTracker;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.impl.WireScanner;
import xal.smf.impl.WireHarp;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * This class provides the user a GUI for managing the machine configuration.
 * That this, he or she can save and restore all the wire scanner device
 * configurations for a convenient, consistent measurement environment.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author  Christopher K. Allen
 * @since   Apr 30, 2012
 */
public class ConfigMachineView extends JPanel implements IConfigView {

    
    /*
     * Global Constants
     */
    
    /** Serialization Version */
    private static final long serialVersionUID = 1L;

    
    /*
     * Event Responses
     */
    
    /**
     * GUI panel that displays a list of file URLs (as strings) for the user
     * to choose from.  Event responses can be registered to respond to the
     * user selection event as <code>{@link ListSelectionListeners}</code>.
     *
     * @author Christopher K. Allen
     * @since   May 7, 2012
     */
    private class FileSelectorPanel extends JScrollPane {
        
        /*
         * Global Constants 
         */
        
        /** Serialization version */
        private static final long serialVersionUID = 1L;
        
        /*
         * Local Attributes
         */
        
        /** The list of recent files */
        private final JList<String>                 pnlFileList;
        
        /** Collection of objects listening for double click events */
        private final List<ListSelectionListener>   lstDcCallBacks;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>FileSelectorPanel</code> object initialized with the 
         * given set of file URLs to be displayed.
         * 
         * @param arrFileUrls   collection of URLs for selection by user
         *
         * @author  Christopher K. Allen
         * @since   May 7, 2012
         */
        public FileSelectorPanel(String[] arrFileUrls) {
            this.lstDcCallBacks = new LinkedList<ListSelectionListener>();
            this.pnlFileList    = new JList<String>(arrFileUrls);
            this.pnlFileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            this.setViewportView(this.pnlFileList);
            this.buildMouseListener();
        }
        
        
        
        /*
         * Operations
         */
        
        /**
         * Returns the URL of the currently selected file name.  The file name is 
         * entered into the list as a string, so it is converted into a <code>URL</code>
         * object then returned.  If the string used to initialize the list is not
         * a valid URL an exception is thrown.  If there is no selection then a 
         * <code>null</code> value is returned.
         *
         * @return  The URL of of the current selection or <code>null</code> if there is no selection
         * 
         * @throws MalformedURLException    the current selection is not a valid URL string
         *
         * @author Christopher K. Allen
         * @since  May 18, 2012
         */
        public URL  getSelectedFile() throws MalformedURLException {
            
            String  strFileName = this.pnlFileList.getSelectedValue();
            
            if (strFileName == null)
                return null;
            
            URL     urlSelected = new URL(strFileName);
//            File    fileSelect  = new File(strFileName);
//            URI     uriSelected = fileSelect.toURI();
//            URL     urlSelected = uriSelected.toURL();
            
            return urlSelected;
        }
        
        /**
         * Resets the list selections displayed the the user to the given collection.
         * 
         * @param arrFileUrls   new set of select-able file URLs
         *
         * @author Christopher K. Allen
         * @since  May 7, 2012
         */
        public void resetFileSelection(String[] arrFileUrls) {
            this.pnlFileList.setListData(arrFileUrls);
        }
        
        
        /**
         * Register to receive double-click events from the <code>JList</code>
         * panel containing the file names.
         * 
         * @param lsnDcEvt  receiver of the mouse double-click events
         *
         * @author Christopher K. Allen
         * @since  Jun 20, 2012
         */
        public void addDoubleClickListener(ListSelectionListener lsnDcEvt) {
            this.lstDcCallBacks.add(lsnDcEvt);
        }
        
        
        /*
         * Support Methods
         */
        
        /**
         * Installs a <code>MouseListener</code> into the <code>JList</code> component
         * so we can catch double-click events.  These events are passed to any listener
         * object that registered for the events.
         *
         * @author Christopher K. Allen
         * @since  Jun 20, 2012
         */
        private void buildMouseListener() {
            MouseListener   lsnMouse = new MouseListener() {

                @Override
                @SuppressWarnings("synthetic-access")
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2 && !e.isConsumed()) {
                        e.consume();

                        JList<String> pnlSrc = FileSelectorPanel.this.pnlFileList;
                        int iMin = pnlSrc.getMinSelectionIndex();
                        int iMax = pnlSrc.getMaxSelectionIndex();
                        ListSelectionEvent  evtLstSel = new ListSelectionEvent(pnlSrc, iMin, iMax, false);
                        for (ListSelectionListener lsnListSel : FileSelectorPanel.this.lstDcCallBacks) {
                            lsnListSel.valueChanged(evtLstSel);
                        }
                    }
                        
                }
                @Override public void mouseEntered(MouseEvent e) {}
                @Override public void mouseExited(MouseEvent e) {}
                @Override public void mousePressed(MouseEvent e) {}
                @Override public void mouseReleased(MouseEvent e) {}
            };
            
           this.pnlFileList.addMouseListener(lsnMouse);
           }
    }

    
    
    /*
     * Local Attributes
     */
    
    /** We are configuring the devices of this accelerator */
    private Accelerator                 smfAccel;
    
    /** The current machine configuration and management thereof */
    private final MainConfiguration  mgrDevCfg;

    
    //
    // GUI Components
    // 
    
    /** The current file name containing the configuration information */
    private JTextField                  txtCfgFileNm;
    

    /** The load past device configuration command button */
    private JButton                     butLoadAny;
    
    /** The current (default) configuration file directory location */
    private JTextField                  txtCfgFileLoc;
    
    
    /** The load recent past device configuration command button */
    private JButton                     butLoadRct;
    
    /** list of recent device configuration files */
    private FileSelectorPanel           pnlFileRec;    
    
    /** File chooser for loading a stored device configuration set */
    private JFileChooser                fcOpen;
    
    /** Button for clearing the recent configuration file history */
    private JButton                     butClearRct;
    

    /** The save current configuration to file command button */
    private JButton                     butSave;
    
    /** The save current configuration to a new file command button */
    private JButton                     butSaveAs;
    
    /** The status of the save operation */
    private JTextField                  txtCfgStatus;
    
    /** The device selection panel for saving configurations*/
    private DeviceSelectorPanel         pnlDevSel;
    
    /** The device selection panel for displaying which devices had configurations restored */
    private DeviceSelectorPanel        pnlDevRestore;
    
    /** File chooser for saving the current device configuration */
    private JFileChooser                fcSave;   

    
    
    /** Cache of the most recent files used for saving configurations */
    private final RecentFileTracker        nmsConfigFiles;
    
    /** Current location of the device configuration files */
    private final DefaultFolderAccessory   locConfigFiles;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ConfigMachineView</code> object for configuration management of 
     * all the wire scanner devices in the given accelerator.
     * 
     * @param cfgMain  we manager the profile devices of this machine configuration
     *
     * @author  Christopher K. Allen
     * @since   Apr 30, 2012
     */
    public ConfigMachineView(MainConfiguration cfgMain) {
        this.mgrDevCfg = cfgMain;
        this.smfAccel  = cfgMain.getAccelerator();

        // The configuration persistence
        String   strAppName = MainApplication.getApplicationName();
        int      cntCfgFile = AppProperties.FILE.CFG_CNT.getValue().asInteger();
        this.nmsConfigFiles = new RecentFileTracker( cntCfgFile, MainConfiguration.class, strAppName);
        this.locConfigFiles = new DefaultFolderAccessory( MachineConfig.class, null, strAppName );
        
        // Build GUI components then do a screen layout
        this.buildGuiComponents();
        this.buildGuiResponses();
        this.layoutGuiComponents();
    }
    


    /*
     * IConfigurationView Interface
     */
    
    /**
     * Get the reference to the new application accelerator object and reset the device
     * selection panel with the devices therein.
     * 
     * @since May 1, 2012
     * @see xal.app.pta.IConfigView#updateAccelerator(MainConfiguration)
     */
    @Override
    public void updateAccelerator(MainConfiguration cfgMain) {
        this.smfAccel = cfgMain.getAccelerator();
        this.pnlDevSel.resetAccelerator( cfgMain.getAccelerator() ); 
//        this.mgrDevCfg.resetAccelerator( cfgMain.getAccelerator() );
    }

    /**
     * Nothing to do.
     * 
     * @since Jul 12, 2012
     * @see xal.app.pta.IConfigView#updateConfiguration(xal.app.pta.MainConfiguration)
     */
    @Override
    public void updateConfiguration(MainConfiguration cfgMain) {
    }
    


   /*
    * Event Response
    */

    /**
     * Restores the machine configuration from the snapshot stored in 
     * the URL currently selected in the Recent Files List.
     *
     * @author Christopher K. Allen
     * @since  May 18, 2012
     */
    private void evtRestoreRecentConfiguration() {
        try {
            URL urlSelect = this.pnlFileRec.getSelectedFile();
            
            if (urlSelect == null) {
                JOptionPane.showMessageDialog(this, "No file selected");
                return;
            }
            
            this.restoreConfigurationFrom(urlSelect);
            
        } catch (MalformedURLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage() , "ERROR", JOptionPane.ERROR_MESSAGE);
            this.txtCfgStatus.setText("Bad URL");
            MainApplication.getEventLogger().logError(this.getClass(), "Restore configuration failure  - bad URL"); 
            
        }
        
    }
    
    /**
     * Restore the machine configuration to that of a saved configuration.
     * The user specifies the location of the file containing the configuration
     * information.
     *
     * @author Christopher K. Allen
     * @since  May 18, 2012
     */
    private void evtRestoreAnyConfiguration() {
        
        // Retrieve the file name
        int         intResult  = this.fcOpen.showOpenDialog(this);

        if (intResult == JFileChooser.APPROVE_OPTION) {
            try {
                File    fileLoc = this.fcOpen.getSelectedFile();
                URI     uriLoc  = fileLoc.toURI();
                URL     urlLoc = uriLoc.toURL();

                this.restoreConfigurationFrom(urlLoc);

                
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage() , "ERROR", JOptionPane.ERROR_MESSAGE);
                this.txtCfgStatus.setText("Bad URL");
                MainApplication.getEventLogger().logError(this.getClass(), "Restore configuration failure  - bad URL"); 
                
            }
        }
    }

    /**
     * Responds to the save as configuration command button.  The
     * currently selected devices have their configuration saved to 
     * a location that the user specifies.
     * 
     * @see ConfigMachineView#saveConfigurationTo(URL)
     *
     * @author Christopher K. Allen
     * @since  May 18, 2012
     */
    private void evtSaveAsConfiguration() {
        
        // Initialize the file chooser dialogue
        File                   fileDir = this.locConfigFiles.getDefaultFolder(); //appGbl.getDefaultDocumentFolder();
        File                   fileDef = this.timeStampFileName(fileDir);
        this.fcSave.setSelectedFile(fileDef);

        // Retrieve the file name
        int         intResult  = this.fcSave.showSaveDialog(this);

        if (intResult == JFileChooser.APPROVE_OPTION) {
            try {
                File    fileLoc = this.fcSave.getSelectedFile();
                URI     uriLoc  = fileLoc.toURI();
                URL     urlLoc = uriLoc.toURL();

                // Save the currently selected devices to the user-specified location
                this.saveConfigurationTo(urlLoc);
                
            } catch (MalformedURLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage() , "ERROR", JOptionPane.ERROR_MESSAGE);
                this.txtCfgStatus.setText("Bad URL");
                MainApplication.getEventLogger().logInfo(this.getClass(), "Saving machine configuration failure  - bad URL"); 
                
            }
        }
    }
    
    /**
     * Responds to the save configuration command button.  The currently selected
     * devices have their configuration saved to the default URL location.
     * 
     * @see #saveConfigurationTo(URL)
     *
     * @author Christopher K. Allen
     * @since  May 17, 2012
     */
    private void evtSaveConfiguration() {
        URL urlSource = this.mgrDevCfg.getSource();
        
        if (urlSource == null) { 
            this.evtSaveAsConfiguration();
         
            return;
        }

        int intResponse = JOptionPane.showConfirmDialog(this, 
                "This action will overwite the configuration file " + urlSource.getPath() + ".", 
                "WARNING", 
                JOptionPane.OK_CANCEL_OPTION);
        if (intResponse == JOptionPane.CANCEL_OPTION)
            return;
        
        this.saveConfigurationTo(urlSource);
    }
    
    
    
    /*
     * GUI Support Methods
     */
    
    /**
     * Initializes all the components of the
     * GUI display.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unchecked")
    private void buildGuiComponents()   {
        
        String[] arrFileExt = new String [] { MainApplication.getConfigurationFileExtension() }; 

        // The save configuration file chooser
        this.fcSave = new JFileChooser();
        this.locConfigFiles.applyTo( fcSave );
        FileFilterFactory.applyFileFilters(fcSave, arrFileExt );

        // The device selection panel for saving configurations
        this.pnlDevSel = new DeviceSelectorPanel(this.smfAccel, WireScanner.class, WireHarp.class);
//        this.pnlDevSel.setDeviceTableVisible(false);
        this.pnlDevSel.setDeviceTableVisible(true);
//        this.pnlDevSel.setBorder(new TitledBorder("Select Profile Devices") );
        
        // The device selection panel for displaying restored devices
        this.pnlDevRestore = new DeviceSelectorPanel(this.smfAccel, WireScanner.class, WireHarp.class);
        this.pnlDevRestore.setDeviceTableVisible(true);

        // Save current configuration command button
        ImageIcon icnSave = AppProperties.ICON.CFG_SAVE.getValue().asIcon();
        this.butSave      = new JButton("  Save  ", icnSave );
        
        // Save current configuration as command button
        ImageIcon   icnSaveAs = AppProperties.ICON.CFG_SAVEAS.getValue().asIcon();
        this.butSaveAs        = new JButton(" Save As ", icnSaveAs);
        
        // The save configuration operation status text box
        this.txtCfgStatus = new JTextField();
        this.txtCfgStatus.setHorizontalAlignment(SwingConstants.LEFT);
        this.txtCfgStatus.setEditable(false);
        

        // The previous device configuration file list
        String[]    arrUrlFiles = this.nmsConfigFiles.getRecentURLSpecs();
        this.pnlFileRec = new FileSelectorPanel( arrUrlFiles );
        
        // The load configuration file chooser
        this.fcOpen = new JFileChooser();
        this.locConfigFiles.applyTo( fcOpen );
        FileFilterFactory.applyFileFilters( fcOpen, arrFileExt );
        fcOpen.setMultiSelectionEnabled( false );
        
        // Names of the current device configuration file and default file location 
        File   fileDefFldr = this.locConfigFiles.getDefaultFolder();
        String strDefFldr  = (fileDefFldr == null)? " None " : fileDefFldr.getPath();

        this.txtCfgFileLoc = new JTextField( strDefFldr );
        this.txtCfgFileLoc.setHorizontalAlignment(SwingConstants.LEFT);
        this.txtCfgFileLoc.setEditable(false);
        this.txtCfgFileLoc.setEnabled(true);
        
        this.txtCfgFileNm  = new JTextField(" Default ");
        this.txtCfgFileNm.setHorizontalAlignment(SwingConstants.LEFT);
        this.txtCfgFileNm.setEditable(false);
        
        // The restore new configuration command button
        ImageIcon icnLoad = AppProperties.ICON.CFG_RESTORE.getValue().asIcon();
        this.butLoadAny      = new JButton(" Restore ", icnLoad);
        
        // The restore recent configuration command button
        ImageIcon icnLoadRct = AppProperties.ICON.CFG_RESTORE_RCT.getValue().asIcon();
        this.butLoadRct      = new JButton(" Restore Recent ", icnLoadRct);
        
        // The clear all recently used configuration files button
        ImageIcon   icnClrRct = AppProperties.ICON.CFG_CLEAR_RCT.getValue().asIcon();
        this.butClearRct      = new JButton(" Clear History ", icnClrRct);
    }
    
    /**
     * Creates the responses to the user and attaches them to the appropriate
     * GUI component.
     *
     * @author Christopher K. Allen
     * @since  May 15, 2012
     */
    private void buildGuiResponses() {
        
        // Create the load previous configuration response
        ActionListener  actLoad = new ActionListener() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent arg0) {
                ConfigMachineView.this.evtRestoreAnyConfiguration();
            }
        };
        this.butLoadAny.addActionListener(actLoad);
        
        // Create the load recent configuration response
        ActionListener  actLoadRct = new ActionListener() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent arg0) {
                ConfigMachineView.this.evtRestoreRecentConfiguration();
            }
        };
        this.butLoadRct.addActionListener(actLoadRct);

        // Create the double-click response for the recent files display list
        ListSelectionListener   lsnLoadRct = new ListSelectionListener() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void valueChanged(ListSelectionEvent e) {
                ConfigMachineView.this.evtRestoreRecentConfiguration();
            }
        };
        this.pnlFileRec.addDoubleClickListener(lsnLoadRct);
        
        // Create the clear recent configuration history response
        ActionListener actClrRct = new ActionListener() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent arg0) {
                ConfigMachineView   cmvOwner = ConfigMachineView.this;
                
                cmvOwner.nmsConfigFiles.clearCache();
                cmvOwner.pnlFileRec.resetFileSelection( cmvOwner.nmsConfigFiles.getRecentURLSpecs() );
            }
        };
        this.butClearRct.addActionListener(actClrRct);
        
        // Create the save configuration response
        ActionListener  actSave = new ActionListener() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent e) {
                ConfigMachineView.this.evtSaveConfiguration();
            }
        };
        this.butSave.addActionListener(actSave);
        
        // Create the save configuration as response
        ActionListener  actSaveAs = new ActionListener() {

            @Override
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent arg0) {
                ConfigMachineView.this.evtSaveAsConfiguration();
            }
        };
        this.butSaveAs.addActionListener(actSaveAs);
    }

    /**
     * Lays out and build the GUI using
     * the initialized components.
     *
     * 
     * @since  Aug 19, 2009
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        
        //
        // Layout the right-side panel
        //
        JPanel  pnlRight = new JPanel();
        pnlRight.setLayout( new GridBagLayout() );
        pnlRight.setBorder( new TitledBorder("Save Current Machine Configuration"));

        GridBagConstraints      gbcRight = new GridBagConstraints();
        gbcRight.insets = new Insets(5,5,5,5);
        
        // The device selection panel
        gbcRight.gridx = 0;
        gbcRight.gridy = 0;
        gbcRight.weightx = 0.3;
        gbcRight.weighty = 0.01;
        gbcRight.fill = GridBagConstraints.BOTH;
        gbcRight.anchor = GridBagConstraints.CENTER;
        gbcRight.gridheight = 1;
        gbcRight.gridwidth = 2;
        pnlRight.add(this.pnlDevSel, gbcRight);
        
        // Create some space
        gbcRight.gridx = 0;
        gbcRight.gridy = 1;
        gbcRight.weightx = 0.1;
        gbcRight.weighty = 0.;
        gbcRight.fill = GridBagConstraints.NONE;
        gbcRight.anchor = GridBagConstraints.LINE_START;
        gbcRight.gridheight = 1;
        gbcRight.gridwidth = 2;
        pnlRight.add( Box.createVerticalStrut(25), gbcRight);
        
        
        // Create a panel for the "save" buttons
        JPanel pnlSave = new JPanel();
        pnlSave.setLayout( new GridBagLayout() );
        pnlSave.setBorder( new BevelBorder(BevelBorder.LOWERED) );
        
        GridBagConstraints  gbcSave = new GridBagConstraints();
        gbcSave.insets = new Insets(5,5,5,5);
        
        // The save configuration command button
        gbcSave.gridx = 0;
        gbcSave.gridy = 0;
        gbcSave.weightx = 0.1;
        gbcSave.weighty = 0.;
        gbcSave.fill = GridBagConstraints.NONE;
        gbcSave.anchor = GridBagConstraints.LINE_START;
        gbcSave.gridheight = 1;
        gbcSave.gridwidth = 1;
        pnlSave.add(this.butSave, gbcSave);
        
        // The save as configuration command button
        gbcSave.gridx = 1;
        gbcSave.gridy = 0;
        gbcSave.weightx = 0.1;
        gbcSave.weighty = 0.;
        gbcSave.fill = GridBagConstraints.NONE;
        gbcSave.anchor = GridBagConstraints.LINE_START;
        gbcSave.gridheight = 1;
        gbcSave.gridwidth = 1;
        pnlSave.add(this.butSaveAs, gbcSave);

        // Add the save buttons panel
        gbcRight.gridx = 0;
        gbcRight.gridy = 2;
        gbcRight.weightx = 0.1;
        gbcRight.weighty = 0.;
        gbcRight.fill = GridBagConstraints.HORIZONTAL;
        gbcRight.anchor = GridBagConstraints.CENTER;
        gbcRight.gridheight = 1;
        gbcRight.gridwidth = 1;
        pnlRight.add( pnlSave, gbcRight);
        
        
        //
        // Layout the left side panel
        //
        
        // The current configuration/status file display
        //
        // The configuration
        Box     boxCurr = Box.createVerticalBox();
        boxCurr.setBorder(new EtchedBorder());
        
        JLabel  lblTitle   = new JLabel("Restore Saved Machine Configuration");
        Font    fntTitle   = lblTitle.getFont();
//        int     szFont     = fntTitle.getSize();
        lblTitle.setFont( fntTitle.deriveFont(Font.BOLD) );
        
        Box     boxCfgFile = Box.createHorizontalBox();
        JLabel  lblCfgFile = new JLabel("Current Configuration ");
        boxCfgFile.add(lblCfgFile);
        boxCfgFile.add(this.txtCfgFileNm);
        
        // The default configuration file location
        Box boxCfgFileLoc = Box.createHorizontalBox();
        boxCfgFileLoc.add( new JLabel("Configuration Directory ") );
        boxCfgFileLoc.add(this.txtCfgFileLoc);
        
        // The operation status box
        JLabel  lblCfgStatus = new JLabel("Status ");
        Box     boxStatus = Box.createHorizontalBox();
        boxStatus.add( lblCfgStatus );
        boxStatus.add( this.txtCfgStatus );

//        boxCurr.add();
//        boxCurr.add( Box.createVerticalStrut(10) );
        boxCurr.add( boxCfgFile );
        boxCurr.add( Box.createVerticalStrut(10) );
        boxCurr.add( boxCfgFileLoc);
        boxCurr.add( Box.createVerticalStrut(10) );
        boxCurr.add( boxStatus );
        
        
        // Layout the restore recent configuration panel
        //  
        // The restore configuration chooser panel
        JPanel  pnlLoadRec = new JPanel();
        pnlLoadRec.setLayout( new GridBagLayout() );
        pnlLoadRec.setBorder( new TitledBorder("Restore Recently Saved Machine Configuration") );

        GridBagConstraints      gbcLoadRec = new GridBagConstraints();
        gbcLoadRec.insets = new Insets(5,5,5,5);;
        
        // The previous configuration list display
        gbcLoadRec.gridx = 0;
        gbcLoadRec.gridy = 0;
        gbcLoadRec.weightx = 0.75;
        gbcLoadRec.weighty = 0.75;
        gbcLoadRec.fill = GridBagConstraints.BOTH;
        gbcLoadRec.anchor = GridBagConstraints.CENTER;
        gbcLoadRec.gridheight = 1;
        gbcLoadRec.gridwidth = 2;
        pnlLoadRec.add( this.pnlFileRec, gbcLoadRec);
        
        // The restore previous configuration command button
        gbcLoadRec.gridx = 0;
        gbcLoadRec.gridy = 1;
        gbcLoadRec.weightx = 0.0;
        gbcLoadRec.weighty = 0.0;
        gbcLoadRec.fill = GridBagConstraints.NONE;
        gbcLoadRec.anchor = GridBagConstraints.LINE_START;
        gbcLoadRec.gridheight = 1;
        gbcLoadRec.gridwidth = 1;
        pnlLoadRec.add(this.butLoadRct, gbcLoadRec);
        
//        // Add a horizontal spacer
//        gbcLoadRec.gridx = 1;
//        gbcLoadRec.gridy = 1;
//        gbcLoadRec.weightx = 0.0;
//        gbcLoadRec.weighty = 0.0;
//        gbcLoadRec.fill = GridBagConstraints.NONE;
//        gbcLoadRec.anchor = GridBagConstraints.CENTER;
//        gbcLoadRec.gridheight = 1;
//        gbcLoadRec.gridwidth = 1;
//        pnlLoadRec.add(Box.createHorizontalStrut(25), gbcLoadRec);
        
        // The clear configuration history button
        gbcLoadRec.gridx = 1;
        gbcLoadRec.gridy = 1;
        gbcLoadRec.weightx = 0.0;
        gbcLoadRec.weighty = 0.0;
        gbcLoadRec.fill = GridBagConstraints.NONE;
        gbcLoadRec.anchor = GridBagConstraints.LINE_END;
        gbcLoadRec.gridheight = 1;
        gbcLoadRec.gridwidth = 1;
        pnlLoadRec.add( this.butClearRct, gbcLoadRec);
        
        
        // Layout restore any configuration panel
        //
        // Restore any configuration user controls
        JPanel  pnlLoadAny = new JPanel();
        pnlLoadAny.setLayout( new GridBagLayout() );
        pnlLoadAny.setBorder( new TitledBorder("Restore Any Saved Machine Configuration") );

        GridBagConstraints      gbcLoadAny = new GridBagConstraints();
        gbcLoadAny.insets = new Insets(5,5,5,5);
        
        // The restore any configuration button
        gbcLoadAny.gridx = 0;
        gbcLoadAny.gridy = 0;
        gbcLoadAny.weightx = 0.5;
        gbcLoadAny.weighty = 0.5;
        gbcLoadAny.fill = GridBagConstraints.NONE;
        gbcLoadAny.anchor = GridBagConstraints.LINE_START;
        gbcLoadAny.gridheight = 1;
        gbcLoadAny.gridwidth = 1;
        pnlLoadAny.add( this.butLoadAny, gbcLoadAny );

        
        // Create the left-side panel itself
        //
        JPanel  pnlLeft = new JPanel();
        pnlLeft.setLayout( new GridBagLayout() );
        
        GridBagConstraints      gbcLeft = new GridBagConstraints();
        gbcLeft.insets = new Insets(5,5,5,5);
        
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 0;
        gbcLeft.weightx = 0.0;
        gbcLeft.weighty = 0.0;
        gbcLeft.fill = GridBagConstraints.NONE;
        gbcLeft.anchor = GridBagConstraints.PAGE_START;
        gbcLeft.gridheight = 1;
        gbcLeft.gridwidth = 1;
        pnlLeft.add(lblTitle, gbcLeft);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 1;
        gbcLeft.weightx = 0.5;
        gbcLeft.weighty = 0.0;
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.anchor = GridBagConstraints.LINE_START;
        gbcLeft.gridheight = 1;
        gbcLeft.gridwidth = 1;
        pnlLeft.add(boxCurr, gbcLeft);

        gbcLeft.gridx = 0;
        gbcLeft.gridy = 2;
        gbcLeft.weightx = 0.5;
        gbcLeft.weighty = 0.75;
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.anchor = GridBagConstraints.CENTER;
        gbcLeft.gridheight = 1;
        gbcLeft.gridwidth = 1;
        pnlLeft.add( pnlLoadRec, gbcLeft );
        
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 3;
        gbcLeft.weightx = 0.5;
        gbcLeft.weighty = 0.0;
        gbcLeft.fill = GridBagConstraints.HORIZONTAL;
        gbcLeft.anchor = GridBagConstraints.CENTER;
        gbcLeft.gridheight = 1;
        gbcLeft.gridwidth = 1;
        pnlLeft.add( pnlLoadAny, gbcLeft );
        
        gbcLeft.gridx = 0;
        gbcLeft.gridy = 4;
        gbcLeft.weightx = 0.5;
        gbcLeft.weighty = 0.5;
        gbcLeft.fill = GridBagConstraints.BOTH;
        gbcLeft.anchor = GridBagConstraints.CENTER;
        gbcLeft.gridheight = 1;
        gbcLeft.gridwidth = 1;
        pnlLeft.add( this.pnlDevRestore, gbcLeft );

        //
        // Put it all together
        //  Place the restore configuration, current configuration, and save configuration 
        //  panels in the main panel
        this.setLayout( new GridLayout(1,2) );
        this.add( pnlLeft );
        this.add( pnlRight );
    }
    
    /**
     * Restores the machine configuration to the snapshot specified in 
     * the given location.
     *
     * @param urlLoc    location of the file containing configuration information
     *
     * @author Christopher K. Allen
     * @since  May 18, 2012
     */
    private void    restoreConfigurationFrom(URL urlLoc) {
        
        // Ask for a confirmation of the restore operation
        int iResult = JOptionPane.showConfirmDialog(this, "Restore to " + urlLoc.getFile() + "?", " WARNING - Current configuration will be overwritten", JOptionPane.OK_CANCEL_OPTION);
        if (iResult == JOptionPane.CANCEL_OPTION) {
            this.txtCfgStatus.setText("Restore operation canceled by user");
            
            return;
        }
         
        try {
            // Restore the machine state saved in the given location
            Collection<String>    setSuccessIds = this.mgrDevCfg.applyConfiguration(urlLoc);
            
            this.pnlDevRestore.setDevicesSelected(setSuccessIds);

            // Update the status text boxes with the result of the restore operation
            File        fileLoc = new File( urlLoc.getPath() );
            String      strName = fileLoc.getName();
            String      strPath = fileLoc.getPath().replace(strName, "");
            
            this.txtCfgStatus.setText("Restored configuration to " + strName);
            this.txtCfgFileNm.setText( strName );
            this.txtCfgFileLoc.setText( strPath );
            
            MainApplication.getEventLogger().logInfo(this.getClass(), "Restored machine configuration from " + strName ); 

            
        } catch (Exception e) {
            String  strMsg = "Restore configuration failure  - " + e.getMessage();
            
            JOptionPane.showMessageDialog(this, strMsg , "ERROR", JOptionPane.ERROR_MESSAGE);
            this.txtCfgStatus.setText(strMsg);
            MainApplication.getEventLogger().logInfo(this.getClass(), strMsg); 
            
        }
    }

    /**
     * Saves the current machine snapshot configuration to the given location. 
     *
     * @param urlLoc    location of file to store machine configuration information
     *
     * @author Christopher K. Allen
     * @since  May 17, 2012
     */
    private void    saveConfigurationTo(URL urlLoc) {
        
        // Get the selected devices, make sure that something is selected
        List<WireScanner>   lstDevs = this.retrieveSelectedDevices();
        
        if (lstDevs.size() == 0) {
            JOptionPane.showMessageDialog(this, 
                    "No devices selected. Nothing to do.", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // Snapshot the machine configuration then save it to file
            this.mgrDevCfg.snapshotConfiguration(lstDevs);
            this.mgrDevCfg.saveSnapshotAt(urlLoc);
            
            // Update the GUI state and display status
            this.txtCfgStatus.setText("Saved current configuration to file " + urlLoc);
            this.txtCfgFileNm.setText(urlLoc.toString());
            this.nmsConfigFiles.cacheURL(urlLoc);
            this.pnlFileRec.resetFileSelection( this.nmsConfigFiles.getRecentURLSpecs() );
            
            MainApplication.getEventLogger().logInfo(this.getClass(), "Saving machine configuration to file " + urlLoc.toString());
            
            // Inform the user of the configuration save
            StringBuffer    bufDevIds = new StringBuffer(lstDevs.size()*10);
            for (WireScanner ws : lstDevs) 
                bufDevIds.append(ws.getId() + "\n");
            JOptionPane.showMessageDialog(this, bufDevIds.toString(), "Saved Device Configurations", JOptionPane.INFORMATION_MESSAGE);
                    
        } catch (Exception e) {
            String  strMsg = e.getMessage();
            
            JOptionPane.showMessageDialog(this, strMsg, "ERROR", JOptionPane.ERROR_MESSAGE);
            this.txtCfgStatus.setText("Save configuration failure");
            MainApplication.getEventLogger().logInfo(this.getClass(), "Machine configuration save attempt failed: " + urlLoc.toString()); 
        }
    }
    
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
        Date       datTmStp = MainApplication.timeStamp();
        String     strFlExt = MainApplication.getConfigurationFileExtension();
        
        // Generate the file name
        String     strTmStp  = fmtTmStp.format(datTmStp);
        String     strFileNm = "PtaDeviceConfig-" + strTmStp + "." + strFlExt;
        
        // Add the full path and return
        File                   fileFullPath = new File(fileDir, strFileNm);
        
        return fileFullPath;
    }
    
    /**
     * Retrieves the collection of user selected devices in the device selection panel,
     * then saves all the device IDs into a list and returns it.
     * 
     * @return  list of IDs for the user selected devices.
     *
     * @author Christopher K. Allen
     * @since  May 16, 2012
     */
    private List<WireScanner>    retrieveSelectedDevices() {

        // Retrieve the selected devices and pack device IDs into a list
        List<AcceleratorNode>  lstNodes = this.pnlDevSel.getSelectedDevices();
        List<WireScanner>      lstDevs  = new LinkedList<WireScanner>();
        
        for (AcceleratorNode smfNode : lstNodes)
            if (smfNode instanceof WireScanner) {
                WireScanner     ws = (WireScanner)smfNode;
                
                lstDevs.add(ws);
            }
        
        return lstDevs;
    }

}
