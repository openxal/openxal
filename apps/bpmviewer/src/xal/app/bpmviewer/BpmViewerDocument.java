/*
 *  BpmViewerDocument.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.bpmviewer;

import java.net.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import javax.swing.tree.DefaultTreeModel;

import xal.tools.data.DataAdaptor;
import xal.ca.*;
import xal.extension.widgets.plot.*;
import xal.extension.application.*;
import xal.extension.application.util.PredefinedConfController;
import xal.tools.xml.*;
import xal.tools.apputils.*;
import xal.tools.apputils.pvselection.*;
import xal.extension.widgets.swing.*;
import xal.extension.application.smf.*;


/**
 *  BpmViewerDocument is a custom XalDocument for bpmViewer application. The
 *  document manages the data that is displayed in the window.
 *
 *@author     shishlo
 *@version    1.0
 */

public class BpmViewerDocument extends AcceleratorDocument {

    static {
        ChannelFactory.defaultFactory().init();
    }

    //message text field. It is actually message text field from
    private JTextField messageTextLocal = new JTextField();

    //accelerator data file
    private File acceleratorDataFile = null;

    //Updating controller
    UpdatingController updatingController = new UpdatingController();

    //------------------------------------------------------
    //actions
    //------------------------------------------------------
    private Action setViewlAction = null;
    private Action setPVsAction = null;
    private Action setPredefConfigAction = null;

    //---------------------------------------------
    //view panel
    //---------------------------------------------
    private JPanel viewPanel = new JPanel();

    private JLabel viewPanelTitle_Label =
        new JLabel( "============UPDATING MANAGEMENT============", JLabel.CENTER );

    private JRadioButton autoUpdateView_Button = new JRadioButton( "Auto Update", true );

    private JSpinner freq_ViewPanel_Spinner = new JSpinner( new SpinnerNumberModel( 10, 1, 20, 1 ) );
    private JLabel viewPanelFreq_Label =
        new JLabel( "Update Freq.[Hz]", JLabel.LEFT );

    //PVsTreePanel placed on the view panel
    private PVsTreePanel pvsTreePanelView = null;

    //BpmViewerPV vectors
    private Vector<BpmViewerPV> phasePVs = new Vector<BpmViewerPV>();
    private Vector<BpmViewerPV>  xPosPVs = new Vector<BpmViewerPV> ();
    private Vector<BpmViewerPV>  yPosPVs = new Vector<BpmViewerPV> ();

    private FunctionGraphsJPanel phaseGraphs = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel xPosGraphs = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel yPosGraphs = new FunctionGraphsJPanel();
    private FunctionGraphsJPanel sigmaGraphs = new FunctionGraphsJPanel();

    //---------------------------------------------
    //set PVs panel
    //---------------------------------------------
    private JPanel setPVsPanel = new JPanel();

    private ValuesGraphPanel phaseGraphPanel = null;
    private ValuesGraphPanel xPosGraphPanel = null;
    private ValuesGraphPanel yPosGraphPanel = null;
    private SigmaGraphPanel sigmaGraphPanel = null;

    //root node of the PVTree for PVsSelector
    private String root_Name = "ROOT";
    private String rootPhasePV_Name = "BPM Phases PVs";
    private String rootXposPV_Name = "BPM X-pos. PVs";
    private String rootYposPV_Name = "BPM Y-pos. PVs";

    private PVTreeNode root_Node = null;
    private PVTreeNode rootPhasePV_Node = null;
    private PVTreeNode rootXposPV_Node = null;
    private PVTreeNode rootYposPV_Node = null;

    //PVsSelector by itself
    private PVsSelector pvsSelector = null;

    //PVTree listeners
    private ActionListener switchPVTreeListener = null;
    private ActionListener createDeletePVTreeListener = null;
    private ActionListener renamePVTreeListener = null;

    //-------------------------------------------------------------
    //PREFERENCES_PANEL and GUI elements, actions etc.
    //-------------------------------------------------------------
    private JPanel preferencesPanel = new JPanel();
    private JButton setFont_PrefPanel_Button = new JButton( "Set Font Size" );
    private JSpinner fontSize_PrefPanel_Spinner = new JSpinner( new SpinnerNumberModel( 7, 7, 26, 1 ) );
    private Font globalFont = new Font( "Monospaced", Font.PLAIN, 10 );

    //------------------------------------------------
    //PREDEFINED CONFIGURATION PANEL
    //------------------------------------------------
    private PredefinedConfController predefinedConfController = null;
    private JPanel configPanel = null;

    //------------------------------------------------
    //PANEL STATE
    //------------------------------------------------
    private int ACTIVE_PANEL = 0;
    private int VIEW_PANEL = 0;
    private int SET_PVS_PANEL = 1;
    private int PREFERENCES_PANEL = 2;
    private int PREDEF_CONF_PANEL = 3;

    //-------------------------------------
    //time and date related member
    //-------------------------------------
    private static DateAndTimeText dateAndTime = new DateAndTimeText();

    //------------------------------------------
    //SAVE RESTORE PART
    //------------------------------------------
    //root node name
    private String dataRootName = "BPM_VIEWER";


    /**  Create a new empty BpmViewerDocument */
    public BpmViewerDocument() {

        ACTIVE_PANEL = VIEW_PANEL;

        double freq = 10.0;
        updatingController.setUpdateFrequency( freq );
        freq_ViewPanel_Spinner.setValue( new Integer( (int) freq ) );

        updatingController.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    if ( ACTIVE_PANEL == VIEW_PANEL ) {
                        if ( autoUpdateView_Button.isSelected() ) {
                            for ( int i = 0; i < phasePVs.size(); i++ ) {
                                (  phasePVs.get( i ) ).setWrapDataProperty(true);
                                (  phasePVs.get( i ) ).update();
                            }
                            for ( int i = 0; i < xPosPVs.size(); i++ ) {
                                (  xPosPVs.get( i ) ).update();
                            }
                            for ( int i = 0; i < yPosPVs.size(); i++ ) {
                                ( yPosPVs.get( i ) ).update();
                            }
                            updateGraphPanels();
                        }
                    }
                }
            } );

        //make all panels
        makePreferencesPanel();
        makePredefinedConfigurationsPanel();
        makePVsSelectionPanel();
        makeViewPanel();
    }


    /**
     *  Create a new document loaded from the URL file
     *
     *@param  url  The URL of the file to load into the new document.
     */
    public BpmViewerDocument( URL url ) {
        this();
        if ( url == null ) {
            return;
        }
        setSource( url );
        readBpmViewerDocument( url );

        //super class method - will show "Save" menu active
        if ( url.getProtocol().equals( "jar" ) ) {
            return;
        }
        setHasChanges( true );
    }


    /**
     *  Make a main window by instantiating the BpmViewerWindow window.
     */
    public void makeMainWindow() {
        mainWindow = new BpmViewerWindow( this );
        //---------------------------------------------------------------
        //this is the place for initializing initial state of main window
        //---------------------------------------------------------------

        //define initial state of the window
        getBpmViewerWindow().setJComponent( viewPanel );

        //set connections between message texts
        messageTextLocal = getBpmViewerWindow().getMessageTextField();

        //set all text messages for sub frames
        //???
        pvsSelector.getMessageJTextField().setDocument( messageTextLocal.getDocument() );

        fontSize_PrefPanel_Spinner.setValue( new Integer( globalFont.getSize() ) );
        setFontForAll( globalFont );

        //set connections for  message text in selection of config. panel
        predefinedConfController.setMessageTextField( getBpmViewerWindow().getMessageTextField() );

        //set timer
        JToolBar toolbar = getBpmViewerWindow().getToolBar();
        JTextField timeTxt_temp = dateAndTime.getNewTimeTextField();
        timeTxt_temp.setHorizontalAlignment( JTextField.CENTER );
        toolbar.add( timeTxt_temp );

        mainWindow.setSize( new Dimension( 700, 600 ) );
    }


    /**
     *  Dispose of BpmViewerDocument resources. This method overrides an empty
     *  superclass method.
     */
    public void freeCustomResources() {
        cleanUp();
    }


    /**
     *  Reads the content of the document from the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public void readBpmViewerDocument( URL url ) {

        //read the document content from the persistent storage

        XmlDataAdaptor readAdp = null;
        readAdp = XmlDataAdaptor.adaptorForUrl( url, false );

        if ( readAdp != null ) {
            DataAdaptor bpmViewerData_Adaptor = readAdp.childAdaptor( dataRootName );
            if ( bpmViewerData_Adaptor != null ) {
                cleanUp();
                setTitle( bpmViewerData_Adaptor.stringValue( "title" ) );

                //set font
                DataAdaptor params_font = bpmViewerData_Adaptor.childAdaptor( "font" );
                int font_size = params_font.intValue( "size" );
                int style = params_font.intValue( "style" );
                String font_Family = params_font.stringValue( "name" );
                globalFont = new Font( font_Family, style, font_size );
                fontSize_PrefPanel_Spinner.setValue( new Integer( font_size ) );
                setFontForAll( globalFont );

                //get the information about updating
                DataAdaptor params_DA = bpmViewerData_Adaptor.childAdaptor( "PARAMS" );
                boolean autoUpdateOn = params_DA.booleanValue( "AutoUpdate" );
                int frequency = params_DA.intValue( "Frequency" );
                freq_ViewPanel_Spinner.setValue( new Integer( frequency ) );

                //temporary to calm down all updating during creations of PV nodes
                autoUpdateView_Button.setSelected( false );

                //set graph panels
                XmlDataAdaptor phasePanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.childAdaptor( "PHASE_PANEL" );
                XmlDataAdaptor xPosPanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.childAdaptor( "X_POSITION_PANEL" );
                XmlDataAdaptor yPosPanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.childAdaptor( "Y_POSITION_PANEL" );

                phaseGraphPanel.setConfig( phasePanelDA );
                xPosGraphPanel.setConfig( xPosPanelDA );
                yPosGraphPanel.setConfig( yPosPanelDA );

                //----------------------------
                //create the tree
                //----------------------------
                DataAdaptor phasePVsDA = bpmViewerData_Adaptor.childAdaptor( "PHASE_PVs" );
                DataAdaptor xPosPVsDA = bpmViewerData_Adaptor.childAdaptor( "X_POS_PVs" );
                DataAdaptor yPosPVsDA = bpmViewerData_Adaptor.childAdaptor( "Y_POS_PVs" );

                //create BpmViewerPV vectors
                
                for (final DataAdaptor g_DA : phasePVsDA.childAdaptors()) {
                    BpmViewerPV bpmPV = new BpmViewerPV( phaseGraphs );
                    bpmPV.setConfig( g_DA );
                    phasePVs.add( bpmPV );
                    updatingController.addArrayDataPV( bpmPV.getArrayDataPV() );
                }

                
                for (final DataAdaptor g_DA : xPosPVsDA.childAdaptors()) {
                    BpmViewerPV bpmPV = new BpmViewerPV( xPosGraphs );
                    bpmPV.setConfig( g_DA );
                    xPosPVs.add( bpmPV );
                    updatingController.addArrayDataPV( bpmPV.getArrayDataPV() );
                }
                for (final DataAdaptor g_DA : yPosPVsDA.childAdaptors()) {
                    BpmViewerPV bpmPV = new BpmViewerPV( yPosGraphs );
                    bpmPV.setConfig( g_DA );
                    yPosPVs.add( bpmPV );
                    updatingController.addArrayDataPV( bpmPV.getArrayDataPV() );
                }

                //copy structure from BpmViewerPV vectors to the tree
                for ( int i = 0, n = phasePVs.size(); i < n; i++ ) {
                    BpmViewerPV bpmPV =  phasePVs.get( i );
                    PVTreeNode pvNodeNew = new PVTreeNode( bpmPV.getChannelName() );
                    pvNodeNew.setChannel( bpmPV.getChannel() );
                    pvNodeNew.setAsPVName( true );
                    pvNodeNew.setCheckBoxVisible( true );
                    rootPhasePV_Node.add( pvNodeNew );
                    pvNodeNew.setSwitchedOn( bpmPV.getArrayDataPV().getSwitchOn() );
                    pvNodeNew.setSwitchedOnOffListener( switchPVTreeListener );
                    pvNodeNew.setCreateRemoveListener( createDeletePVTreeListener );
                    pvNodeNew.setRenameListener( renamePVTreeListener );
                }

                for ( int i = 0, n = xPosPVs.size(); i < n; i++ ) {
                    BpmViewerPV bpmPV =  xPosPVs.get( i );
                    PVTreeNode pvNodeNew = new PVTreeNode( bpmPV.getChannelName() );
                    pvNodeNew.setChannel( bpmPV.getChannel() );
                    pvNodeNew.setAsPVName( true );
                    pvNodeNew.setCheckBoxVisible( true );
                    rootXposPV_Node.add( pvNodeNew );
                    pvNodeNew.setSwitchedOn( bpmPV.getArrayDataPV().getSwitchOn() );
                    pvNodeNew.setSwitchedOnOffListener( switchPVTreeListener );
                    pvNodeNew.setCreateRemoveListener( createDeletePVTreeListener );
                    pvNodeNew.setRenameListener( renamePVTreeListener );
                }

                for ( int i = 0, n = yPosPVs.size(); i < n; i++ ) {
                    BpmViewerPV bpmPV =  yPosPVs.get( i );
                    PVTreeNode pvNodeNew = new PVTreeNode( bpmPV.getChannelName() );
                    pvNodeNew.setChannel( bpmPV.getChannel() );
                    pvNodeNew.setAsPVName( true );
                    pvNodeNew.setCheckBoxVisible( true );
                    rootYposPV_Node.add( pvNodeNew );
                    pvNodeNew.setSwitchedOn( bpmPV.getArrayDataPV().getSwitchOn() );
                    pvNodeNew.setSwitchedOnOffListener( switchPVTreeListener );
                    pvNodeNew.setCreateRemoveListener( createDeletePVTreeListener );
                    pvNodeNew.setRenameListener( renamePVTreeListener );
                }

                ( (DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel() ).reload();
                ( (DefaultTreeModel) pvsTreePanelView.getJTree().getModel() ).reload();

                setColors( rootPhasePV_Node, -1 );
                setColors( rootXposPV_Node, -1 );
                setColors( rootYposPV_Node, -1 );

                updateGraphPanels();

                //permanent definition of auto update
                autoUpdateView_Button.setSelected( autoUpdateOn );

            }
        }
    }


    /**
     *  Save the BpmViewerDocument document to the specified URL.
     *
     *@param  url  Description of the Parameter
     */
    public void saveDocumentAs( final URL url ) {
        //this is the place to write document to the persistent storage

        XmlDataAdaptor da = XmlDataAdaptor.newEmptyDocumentAdaptor();
        XmlDataAdaptor bpmViewerData_Adaptor = (XmlDataAdaptor) da.createChild( dataRootName );
        bpmViewerData_Adaptor.setValue( "title", url.getFile() );

        //dump parameters
        DataAdaptor params_font = bpmViewerData_Adaptor.createChild( "font" );
        params_font.setValue( "name", globalFont.getFamily() );
        params_font.setValue( "style", globalFont.getStyle() );
        params_font.setValue( "size", globalFont.getSize() );

        DataAdaptor params_DA = bpmViewerData_Adaptor.createChild( "PARAMS" );
        params_DA.setValue( "AutoUpdate", autoUpdateView_Button.isSelected() );
        params_DA.setValue( "Frequency", ( (Integer) freq_ViewPanel_Spinner.getValue() ).intValue() );

        //dump graph panels states
        XmlDataAdaptor phasePanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.createChild( "PHASE_PANEL" );
        XmlDataAdaptor xPosPanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.createChild( "X_POSITION_PANEL" );
        XmlDataAdaptor yPosPanelDA = (XmlDataAdaptor) bpmViewerData_Adaptor.createChild( "Y_POSITION_PANEL" );
        phaseGraphPanel.dumpConfig( phasePanelDA );
        xPosGraphPanel.dumpConfig( xPosPanelDA );
        yPosGraphPanel.dumpConfig( yPosPanelDA );

        //dump graph data and PVs
        DataAdaptor phasePVsDA = bpmViewerData_Adaptor.createChild( "PHASE_PVs" );
        DataAdaptor xPosPVsDA = bpmViewerData_Adaptor.createChild( "X_POS_PVs" );
        DataAdaptor yPosPVsDA = bpmViewerData_Adaptor.createChild( "Y_POS_PVs" );

        for ( int i = 0, n = phasePVs.size(); i < n; i++ ) {
            BpmViewerPV bpmViewer =  phasePVs.get( i );
            bpmViewer.dumpConfig( phasePVsDA );
        }

        for ( int i = 0, n = xPosPVs.size(); i < n; i++ ) {
            BpmViewerPV bpmViewer =  xPosPVs.get( i );
            bpmViewer.dumpConfig( xPosPVsDA );
        }

        for ( int i = 0, n = yPosPVs.size(); i < n; i++ ) {
            BpmViewerPV bpmViewer =  yPosPVs.get( i );
            bpmViewer.dumpConfig( yPosPVsDA );
        }

		//dump data into the file
		try {
			da.writeToUrl( url );

			//super class method - will show "Save" menu active
			setHasChanges( true );
		}
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file access exception!", exception);
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println(exception);
				displayError("Save Failed!", "Save failed due to a file IO exception!", exception);
			}
			else {
				exception.printStackTrace();
				displayError("Save Failed!", "Save failed due to an internal write exception!", exception);
			}
        }
        catch( Exception exception ) {
			exception.printStackTrace();
            displayError("Save Failed!", "Save failed due to an internal exception!", exception);
        }

    }


    /**  Edit preferences for the document. */
    void editPreferences() {
        //place for edit preferences
        setActivePanel( PREFERENCES_PANEL );
    }


    /**
     *  Convenience method for getting the BpmViewerWindow window. It is the
     *  cast to the proper subclass of XalWindow. This allows me to avoid
     *  casting the window every time I reference it.
     *
     *@return    The main window cast to its dynamic runtime class
     */
    private BpmViewerWindow getBpmViewerWindow() {
        return (BpmViewerWindow) mainWindow;
    }


    /**
     *  Register actions for the menu items and toolbar.
     *
     *@param  commander  Description of the Parameter
     */

    public void customizeCommands( Commander commander ) {

        // define the "show-view-panel" set raw emittance panel action action
        setViewlAction = new AbstractAction( "show-view-panel" ) {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent event ) {
                    setActivePanel( VIEW_PANEL );
                }
            };
        commander.registerAction( setViewlAction );

        // define the "show-set-pvs-panel" set PVs panel appearance action
        setPVsAction = new AbstractAction( "show-set-pvs-panel" ) {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent event ) {
                    setActivePanel( SET_PVS_PANEL );
                }
            };
        commander.registerAction( setPVsAction );


        setPredefConfigAction = new AbstractAction( "set-predef-config" ) {
                /** serialization ID */
                private static final long serialVersionUID = 1L;
                public void actionPerformed( ActionEvent event ) {
                    setActivePanel( PREDEF_CONF_PANEL );
                }
            };
        commander.registerAction( setPredefConfigAction );

    }


    /**  Description of the Method */
    private void makePreferencesPanel() {

        fontSize_PrefPanel_Spinner.setAlignmentX( JSpinner.CENTER_ALIGNMENT );

        preferencesPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 1, 1 ) );
        preferencesPanel.add( fontSize_PrefPanel_Spinner );
        preferencesPanel.add( setFont_PrefPanel_Button );
        setFont_PrefPanel_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    int fnt_size = ( (Integer) fontSize_PrefPanel_Spinner.getValue() ).intValue();
                    globalFont = new Font( globalFont.getFamily(), globalFont.getStyle(), fnt_size );
                    setFontForAll( globalFont );
                }
            } );
    }


    /**  Description of the Method */
    private void makePredefinedConfigurationsPanel() {
        predefinedConfController = new PredefinedConfController( "config", "predefinedConfiguration.bpm" );
        configPanel = predefinedConfController.getJPanel();
        ActionListener selectConfListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    URL url = (URL) e.getSource();

                    if ( url == null ) {
                        Toolkit.getDefaultToolkit().beep();
                        messageTextLocal.setText( null );
                        messageTextLocal.setText( "Cannot find an input configuration file!" );
                    }

                    cleanUp();
                    readBpmViewerDocument( url );

                    //super class method - will show "Save" menu unactive
                    setHasChanges( false );
                    setFontForAll( globalFont );
                    setActivePanel( VIEW_PANEL );
                }
            };
        predefinedConfController.setSelectorListener( selectConfListener );
    }


    /**  Creates the PVs selection panel. */
    private void makePVsSelectionPanel() {
        root_Node = new PVTreeNode( root_Name );
        rootPhasePV_Node = new PVTreeNode( rootPhasePV_Name );
        rootXposPV_Node = new PVTreeNode( rootXposPV_Name );
        rootYposPV_Node = new PVTreeNode( rootYposPV_Name );

        rootPhasePV_Node.setPVNamesAllowed( true );
        rootXposPV_Node.setPVNamesAllowed( true );
        rootYposPV_Node.setPVNamesAllowed( true );

        root_Node.add( rootXposPV_Node );
        root_Node.add( rootYposPV_Node );
        root_Node.add( rootPhasePV_Node );

        //make PVs selectror and place it on the selectionPVsPanel
        pvsSelector = new PVsSelector( root_Node );
        if ( accelerator != null ) {
            pvsSelector.setAccelerator( accelerator );
        }
        pvsSelector.removeMessageTextField();

        setPVsPanel.setLayout( new BorderLayout() );
        setPVsPanel.add( pvsSelector, BorderLayout.CENTER );

        //-------------------------------------------
        //make tree listeners
        //-------------------------------------------

        //listener for switch ON and OFF
        switchPVTreeListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    String command = e.getActionCommand();
                    PVTreeNode pvn = (PVTreeNode) e.getSource();
                    boolean switchOnLocal = command.equals( PVTreeNode.SWITCHED_ON_COMMAND );
                    PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
                    int index = -1;

                    BpmViewerPV bpmPV = null;
                    if ( pvn_parent == rootPhasePV_Node ) {
                        //System.out.println("debug switch on phase PV switchOn=" + switchOnLocal);
                        index = pvn_parent.getIndex( pvn );
                        bpmPV =  phasePVs.get( index );
                    }
                    if ( pvn_parent == rootXposPV_Node ) {
                        //System.out.println("debug switch on the x-position PV switchOn=" + switchOnLocal);
                        index = pvn_parent.getIndex( pvn );
                        bpmPV =  xPosPVs.get( index );
                    }
                    if ( pvn_parent == rootYposPV_Node ) {
                        //System.out.println("debug switch on the x-position PV switchOn=" + switchOnLocal);
                        index = pvn_parent.getIndex( pvn );
                        bpmPV =  yPosPVs.get( index );
                    }

                    if ( index >= 0 && bpmPV != null ) {
                        bpmPV.getArrayDataPV().setSwitchOn( switchOnLocal );
                        updateGraphPanels();
                    }
                }
            };

        //listener deleting or create PV
        createDeletePVTreeListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    PVTreeNode pvn = (PVTreeNode) e.getSource();
                    PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
                    String command = e.getActionCommand();
                    boolean bool_removePV = command.equals( PVTreeNode.REMOVE_PV_COMMAND );
                    int index = -1;
                    BpmViewerPV pv_tmp = null;
                    if ( bool_removePV ) {
                        if ( pvn_parent == rootPhasePV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug delete BpmViewerPV  from PhasePVs index=" + index);
                            pv_tmp =  phasePVs.get( index );
                            phasePVs.remove( pv_tmp );
                        }

                        if ( pvn_parent == rootXposPV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug delete BpmViewerPV  from xPosPVs index=" + index);
                            pv_tmp =  xPosPVs.get( index );
                            xPosPVs.remove( pv_tmp );
                        }

                        if ( pvn_parent == rootYposPV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug delete BpmViewerPV  from yPosPVs index=" + index);
                            pv_tmp =  yPosPVs.get( index );
                            yPosPVs.remove( pv_tmp );
                        }

                        if ( index >= 0 ) {
                            updatingController.removeArrayDataPV( pv_tmp.getArrayDataPV() );
                            setColors( pvn_parent, index );
                            updateGraphPanels();
                        }
                    }
                    else {

                        if ( pvn_parent == rootPhasePV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug add BpmViewerPV  from PhasePVs index="+index );
                            pv_tmp = new BpmViewerPV( phaseGraphs );
                            phasePVs.add( index, pv_tmp );
                        }
                        if ( pvn_parent == rootXposPV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug add BpmViewerPV  from xPosPVs index="+index );
                            pv_tmp = new BpmViewerPV( xPosGraphs );
                            xPosPVs.add( index, pv_tmp );
                        }
                        if ( pvn_parent == rootYposPV_Node ) {
                            index = pvn_parent.getIndex( pvn );
                            //System.out.println("debug add BpmViewerPV  from yPosPVs index="+index );
                            pv_tmp = new BpmViewerPV( yPosGraphs );
                            yPosPVs.add( index, pv_tmp );
                        }

                        if ( index >= 0 ) {
                            pv_tmp.setChannel( pvn.getChannel() );
                            updatingController.addArrayDataPV( pv_tmp.getArrayDataPV() );
                            setColors( pvn_parent, -1 );
                            updateGraphPanels();
                        }
                    }
                }
            };

        //listener rename PV
        renamePVTreeListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    PVTreeNode pvn = (PVTreeNode) e.getSource();
                    PVTreeNode pvn_parent = (PVTreeNode) pvn.getParent();
                    int index = -1;
                    BpmViewerPV pv_tmp = null;

                    if ( pvn_parent == rootPhasePV_Node ) {
                        index = pvn_parent.getIndex( pvn );
                        //System.out.println("debug rename BpmViewerPV  from PhasePVs index="+index );
                        pv_tmp =  phasePVs.get( index );
                    }

                    if ( pvn_parent == rootXposPV_Node ) {
                        index = pvn_parent.getIndex( pvn );
                        //System.out.println("debug rename BpmViewerPV  from xPosPVs index="+index );
                        pv_tmp =  xPosPVs.get( index );
                    }

                    if ( pvn_parent == rootYposPV_Node ) {
                        index = pvn_parent.getIndex( pvn );
                        //System.out.println("debug rename BpmViewerPV  from yPosPVs index="+index );
                        pv_tmp =  yPosPVs.get( index );
                    }

                    if ( index >= 0 ) {
                        pv_tmp.setChannel( pvn.getChannel() );
                        setColors( pvn_parent, -1 );
                        updateGraphPanels();
                    }
                }
            };

        //register the listeners
        rootPhasePV_Node.setSwitchedOnOffListener( switchPVTreeListener );
        rootXposPV_Node.setSwitchedOnOffListener( switchPVTreeListener );
        rootYposPV_Node.setSwitchedOnOffListener( switchPVTreeListener );

        rootPhasePV_Node.setCreateRemoveListener( createDeletePVTreeListener );
        rootXposPV_Node.setCreateRemoveListener( createDeletePVTreeListener );
        rootYposPV_Node.setCreateRemoveListener( createDeletePVTreeListener );

        rootPhasePV_Node.setRenameListener( renamePVTreeListener );
        rootXposPV_Node.setRenameListener( renamePVTreeListener );
        rootYposPV_Node.setRenameListener( renamePVTreeListener );

    }


    /**  Creates the PV viewer panel with all graphs sub-panels, */
    private void makeViewPanel() {

        phaseGraphPanel = new ValuesGraphPanel(
            "bpm phase waveform",
            phasePVs,
            phaseGraphs,
            this );

        xPosGraphPanel = new ValuesGraphPanel(
            "bpm x-pos. waveform",
            xPosPVs,
            xPosGraphs,
            this );

        yPosGraphPanel = new ValuesGraphPanel(
            "bpm y-pos. waveform",
            yPosPVs,
            yPosGraphs,
            this );

        sigmaGraphPanel = new SigmaGraphPanel(
            "sqrt(<(z -<z>)^2>)",
            phaseGraphPanel,
            xPosGraphPanel,
            yPosGraphPanel,
            sigmaGraphs );

        //make left tree and "automatic updating" radio button
        pvsTreePanelView = pvsSelector.getNewPVsTreePanel();
        pvsTreePanelView.getJTree().setBackground( Color.white );

        pvsTreePanelView.setPreferredSize( new Dimension( 0, 0 ) );
        pvsSelector.setPreferredSize( new Dimension( 0, 0 ) );

        //set frequency spinner
        freq_ViewPanel_Spinner.setAlignmentX( JSpinner.CENTER_ALIGNMENT );
        freq_ViewPanel_Spinner.addChangeListener(
            new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    int freq = ( (Integer) freq_ViewPanel_Spinner.getValue() ).intValue();
                    updatingController.setUpdateFrequency( (double) freq );
                }
            } );

        //make the view panel
        viewPanel.setLayout( new BorderLayout() );

        JPanel tmp_panel_0 = new JPanel();
        tmp_panel_0.setLayout( new GridLayout( 2, 2, 1, 1 ) );
        tmp_panel_0.add( xPosGraphPanel.getJPanel() );
        tmp_panel_0.add( yPosGraphPanel.getJPanel() );
        tmp_panel_0.add( phaseGraphPanel.getJPanel() );
        tmp_panel_0.add( sigmaGraphPanel.getJPanel() );

        JPanel tmp_panel_1 = new JPanel();
        tmp_panel_1.setLayout( new FlowLayout( FlowLayout.LEFT, 1, 1 ) );
        tmp_panel_1.add( autoUpdateView_Button );

        JPanel tmp_panel_2 = new JPanel();
        tmp_panel_2.setLayout( new FlowLayout( FlowLayout.LEFT, 1, 1 ) );
        tmp_panel_2.add( freq_ViewPanel_Spinner );
        tmp_panel_2.add( viewPanelFreq_Label );

        JPanel tmp_panel_3 = new JPanel();
        tmp_panel_3.setLayout( new GridLayout( 1, 2, 1, 1 ) );
        tmp_panel_3.add( tmp_panel_1 );
        tmp_panel_3.add( tmp_panel_2 );

        JPanel tmp_panel_4 = new JPanel();
        tmp_panel_4.setLayout( new VerticalLayout() );
        tmp_panel_4.add( viewPanelTitle_Label );
        tmp_panel_4.add( tmp_panel_3 );

        JPanel tmp_panel_5 = new JPanel();
        tmp_panel_5.setLayout( new BorderLayout() );
        tmp_panel_5.add( tmp_panel_4, BorderLayout.NORTH );
        tmp_panel_5.add( pvsTreePanelView, BorderLayout.CENTER );

        viewPanel.add( tmp_panel_0, BorderLayout.CENTER );
        viewPanel.add( tmp_panel_5, BorderLayout.WEST );

    }


    /**  Clean up the document content */
    private void cleanUp() {

        cleanMessageTextField();

        for ( int i = 0, n = phasePVs.size(); i < n; i++ ) {
            BpmViewerPV pv_tmp =  phasePVs.get( i );
            updatingController.removeArrayDataPV( pv_tmp.getArrayDataPV() );
        }

        for ( int i = 0, n = xPosPVs.size(); i < n; i++ ) {
            BpmViewerPV pv_tmp = xPosPVs.get( i );
            updatingController.removeArrayDataPV( pv_tmp.getArrayDataPV() );
        }

        for ( int i = 0, n = yPosPVs.size(); i < n; i++ ) {
            BpmViewerPV pv_tmp =  yPosPVs.get( i );
            updatingController.removeArrayDataPV( pv_tmp.getArrayDataPV() );
        }

        phasePVs.clear();
        xPosPVs.clear();
        yPosPVs.clear();

        rootPhasePV_Node.removeAllChildren();
        rootXposPV_Node.removeAllChildren();
        rootYposPV_Node.removeAllChildren();

        setColors( rootPhasePV_Node, -1 );
        setColors( rootXposPV_Node, -1 );
        setColors( rootYposPV_Node, -1 );

        ( (DefaultTreeModel) pvsSelector.getPVsTreePanel().getJTree().getModel() ).reload();
        ( (DefaultTreeModel) pvsTreePanelView.getJTree().getModel() ).reload();

    }


    /**  Description of the Method */
    private void cleanMessageTextField() {
        messageTextLocal.setText( null );
        messageTextLocal.setForeground( Color.red );
    }


    /**
     *  Sets the fontForAll attribute of the BpmViewerDocument object
     *
     *@param  fnt  The new fontForAll value
     */
    private void setFontForAll( Font fnt ) {

        pvsSelector.setAllFonts( fnt );

        phaseGraphPanel.setAllFonts( fnt );
        xPosGraphPanel.setAllFonts( fnt );
        yPosGraphPanel.setAllFonts( fnt );
        sigmaGraphPanel.setAllFonts( fnt );

        pvsTreePanelView.setAllFonts( fnt );
        viewPanelTitle_Label.setFont( fnt );
        autoUpdateView_Button.setFont( fnt );

        viewPanelFreq_Label.setFont( fnt );
        freq_ViewPanel_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) freq_ViewPanel_Spinner.getEditor() ).getTextField().setFont( fnt );

        messageTextLocal.setFont( fnt );
        fontSize_PrefPanel_Spinner.setValue( new Integer( fnt.getSize() ) );
        predefinedConfController.setFontsForAll( fnt );
        globalFont = fnt;
    }



    /**
     *  Sets the colors for PVs ( graphs ) and tree nodes. If deleteIndex < 0
     *  then nothing to delete.
     *
     *@param  deleteIndex  The new colors value
     *@param  pvNode       The new colors value
     */
    private void setColors( PVTreeNode pvNode, int deleteIndex ) {

        if ( pvNode == rootPhasePV_Node ) {
            for ( int i = 0, n = phasePVs.size(); i < n; i++ ) {
                BpmViewerPV bpmPV =  phasePVs.get( i );
                bpmPV.setColor( IncrementalColors.getColor( i ) );
            }
        }
        if ( pvNode == rootXposPV_Node ) {
            for ( int i = 0, n = xPosPVs.size(); i < n; i++ ) {
                BpmViewerPV bpmPV = xPosPVs.get( i );
                bpmPV.setColor( IncrementalColors.getColor( i ) );
            }
        }
        if ( pvNode == rootYposPV_Node ) {
            for ( int i = 0, n = yPosPVs.size(); i < n; i++ ) {
                BpmViewerPV bpmPV = yPosPVs.get( i );
                bpmPV.setColor( IncrementalColors.getColor( i ) );
            }
        }

        final Enumeration<PVTreeNode> enumNodes =  pvNode.children();
		int keptNodeCounter = 0;  // counter of nodes that are not deleted
        for ( int nodeIndex = 0 ; enumNodes.hasMoreElements() ; nodeIndex++ ) {
			final PVTreeNode pvn = enumNodes.nextElement();
			if ( nodeIndex != deleteIndex ) {
				pvn.setColor( IncrementalColors.getColor( keptNodeCounter ) );
				keptNodeCounter++;
			}
		}
    }


    /**  Updates all data on graphs panels */
    public void updateGraphPanels() {
        phaseGraphPanel.update();
        xPosGraphPanel.update();
        yPosGraphPanel.update();
        sigmaGraphPanel.update();
    }

    
    //attempt to make an accelerator based application
    public void acceleratorChanged() {
		if (accelerator != null) {
            if ( pvsSelector != null ) {
                pvsSelector.setAccelerator( accelerator );
            }
            
			setHasChanges(true);
		}
        
	}
    
      /**
     *  Sets the activePanel attribute of the BpmViewerDocument object
     *
     *@param  newActPanelInd  The new activePanel value
     */
    private void setActivePanel( int newActPanelInd ) {
        int oldActPanelInd = ACTIVE_PANEL;

        if ( oldActPanelInd == newActPanelInd ) {
            return;
        }

        //shut up active panel
        if ( oldActPanelInd == VIEW_PANEL ) {
            //action before view panel will disappear
        }
        else if ( oldActPanelInd == SET_PVS_PANEL ) {
            //action before set PVs panel will disappear
            
        }
        else if ( oldActPanelInd == PREFERENCES_PANEL ) {
            //action before preferences panel will disappear
        }
        else if ( oldActPanelInd == PREDEF_CONF_PANEL ) {
            //action before predifined configurations panel will disappear
        }

        //make something before the new panel will show up
        if ( newActPanelInd == VIEW_PANEL ) {
            getBpmViewerWindow().setJComponent( viewPanel );
        }
        else if ( newActPanelInd == SET_PVS_PANEL ) {
            getBpmViewerWindow().setJComponent( setPVsPanel );
        }
        else if ( newActPanelInd == PREFERENCES_PANEL ) {
            getBpmViewerWindow().setJComponent( preferencesPanel );
        }
        else if ( newActPanelInd == PREDEF_CONF_PANEL ) {
            getBpmViewerWindow().setJComponent( configPanel );
        }

        ACTIVE_PANEL = newActPanelInd;

        cleanMessageTextField();
    }
}

//----------------------------------------------
//Class deals with date and time
//----------------------------------------------
/**
 *  Description of the Class
 *
 *@author     shishlo
 *@version
 */
class DateAndTimeText {


    private SimpleDateFormat dFormat = null;
    private JFormattedTextField dateTimeField = null;


    /**  Constructor for the DateAndTimeText object */
    public DateAndTimeText() {
        dFormat = new SimpleDateFormat( "'Time': MM.dd.yy HH:mm " );
        dateTimeField = new JFormattedTextField( dFormat );
        dateTimeField.setEditable( false );
        Runnable timer =
            new Runnable() {
                public void run() {
                    while ( true ) {
                        dateTimeField.setValue( new Date() );
                        try {
                            Thread.sleep( 30000 );
                        }
                        catch ( InterruptedException e ) {}
                    }
                }
            };

        Thread thr = new Thread( timer );
        thr.start();
    }


    /**
     *  Returns the time attribute of the DateAndTimeText object
     *
     *@return    The time value
     */
    protected String getTime() {
        return dateTimeField.getText();
    }


    /**
     *  Returns the timeTextField attribute of the DateAndTimeText object
     *
     *@return    The timeTextField value
     */
    protected JFormattedTextField getTimeTextField() {
        return dateTimeField;
    }


    /**
     *  Returns the newTimeTextField attribute of the DateAndTimeText object
     *
     *@return    The newTimeTextField value
     */
    protected JTextField getNewTimeTextField() {
        JTextField newText = new JTextField();
        newText.setDocument( dateTimeField.getDocument() );
        newText.setEditable( false );
        return newText;
    }
}





