package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

import xal.app.emittanceanalysis.rawdata.*;
import xal.app.emittanceanalysis.*;
import xal.extension.widgets.plot.*;

/**
 *  This is an analysis controller that keeps the references to all analysis and
 *  responsible for their management
 *
 *@author     shishlo
 *@version    1.0
 */
public class AnalysisController {

    //message text field. It is actually message text field
    //from EmittanceDocument
    private JTextField messageTextLocal = new JTextField();

    //-------------------------------------------------
    //analysis panel. All GUI elements should be there
    //-------------------------------------------------
    private JPanel analysisPanel = new JPanel();
    private HashMap<String,Object> crossParamMap = new HashMap<>();

    //panels for custom GUI elements for the common part of analyses
    private JPanel leftTopPanel = new JPanel();
    private JPanel leftTopCommonPanel = new JPanel();
    private JPanel leftTopCustomPanel = new JPanel();
    private JPanel centerTopPanel = new JPanel();

    //panels for custom GUI elements and plotting panels
    private JPanel rightTopPanel = new JPanel();
    private JPanel rightTopCommonPanel = new JPanel();
    private JPanel rightTopCustomPanel = new JPanel();
    private JPanel bottomPanel = new JPanel();

    private ColorSurfaceData emittance3D = Data3DFactory.getData3D( 1, 1, "linear" );

    //---------------------------------------------
    //raw data object. All comminication with raw
    //data will be carried out through this reference
    //---------------------------------------------
    private RawDataPanel rawDataPanel = null;

    //this index specified what analysis will be shown
    private int currentAnalysisIndex = 0;

    //common part of the analysis
    private AnalysisCommonPart analysisCommonPart = null;

    //array of analyses
    private AnalysisBasic[] analyses = new AnalysisBasic[7];
    private String[] typeNames_arr = {"Phase density viewer",
        "Threshold finding",
        "Profile fitting",        
        "Emittance fitting",
        "Gauss fitting",
        "Threshold Independent",
        "Rotation Angle"};


    /**  Constructor for the AnalysisController object */
    public AnalysisController() {
        crossParamMap.put( "AnalysisController", this );
        analysisCommonPart = new AnalysisCommonPart( typeNames_arr, -1, crossParamMap );

        //----------------------------------------------------------------
        //create all analyses
        //----------------------------------------------------------------
        analyses[0] = new AnalysisEmpty( 0, crossParamMap );
        analyses[1] = new AnalysisFindThreshold( 2, crossParamMap );
        analyses[2] = new AnalysisProfileFit( 1, crossParamMap );        
        analyses[3] = new AnalysisFindEmitByFit( 3, crossParamMap );
        analyses[4] = new AnalysisGaussFit( 4, crossParamMap );
        analyses[5] = new AnalysisThresholdIndependentFit( 5, crossParamMap );
	analyses[6] = new AnalysisRotationAngle( 6, crossParamMap );

        //creation of all HashMap objects
        analysisCommonPart.createHashMapObjects();
        for ( int i = 0; i < analyses.length; i++ ) {
            analyses[i].createHashMapObjects();
        }

        //connection of HashMap objects
        analysisCommonPart.connectToHashMapObjects();
        for ( int i = 0; i < analyses.length; i++ ) {
            analyses[i].connectToHashMapObjects();
        }

        //-----------------------------------------------------------
        //creation of the analysis type switch listener
        //-----------------------------------------------------------
        ActionListener mainTypeChangeListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    messageTextLocal.setText( null );
                    int newIndex = e.getID();
                    int oldIndex = currentAnalysisIndex;
                    if ( newIndex == oldIndex ) {
                        return;
                    }
                    goingShowOff();
                    currentAnalysisIndex = newIndex;
                    goingShowUp();
                }
            };

        analysisCommonPart.addTypeChangeListener( mainTypeChangeListener );

        //-----------------------------------------------------------
        //define panles layout
        //-----------------------------------------------------------
        analysisPanel.setLayout( new BorderLayout() );
        leftTopPanel.setLayout( new BorderLayout() );
        leftTopCommonPanel.setLayout( new BorderLayout() );
        leftTopCustomPanel.setLayout( new BorderLayout() );
        centerTopPanel.setLayout( new BorderLayout() );
        rightTopPanel.setLayout( new BorderLayout() );
        rightTopCommonPanel.setLayout( new BorderLayout() );
        rightTopCustomPanel.setLayout( new BorderLayout() );
        bottomPanel.setLayout( new BorderLayout() );

        leftTopCommonPanel.add( analysisCommonPart.getLeftTopPanel(), BorderLayout.CENTER );
        centerTopPanel.add( analysisCommonPart.getCenterTopPanel(), BorderLayout.CENTER );
        rightTopCommonPanel.add( analysisCommonPart.getRightTopPanel(), BorderLayout.CENTER );

        leftTopPanel.add( leftTopCommonPanel, BorderLayout.NORTH );
        leftTopPanel.add( leftTopCustomPanel, BorderLayout.CENTER );

        rightTopPanel.add( rightTopCommonPanel, BorderLayout.NORTH );
        rightTopPanel.add( rightTopCustomPanel, BorderLayout.CENTER );

        JPanel tmp_1 = new JPanel( new BorderLayout() );
        tmp_1.add( centerTopPanel, BorderLayout.CENTER );
        tmp_1.add( rightTopPanel, BorderLayout.EAST );
        tmp_1.add( leftTopPanel, BorderLayout.WEST );

        JPanel tmp_0 = new JPanel( new BorderLayout() );
        tmp_0.add( tmp_1, BorderLayout.CENTER );

        analysisPanel.add( tmp_0, BorderLayout.NORTH );
        analysisPanel.add( bottomPanel, BorderLayout.CENTER );

    }


    /**  Performs actions before show the panel */
    public void goingShowUp() {
        leftTopCustomPanel.removeAll();
        rightTopCustomPanel.removeAll();
        bottomPanel.removeAll();

        crossParamMap.put( "GAMMA_BETA", new Double( rawDataPanel.getGammaBeta() ) );

        analysisCommonPart.goingShowUp();
        
        analyses[currentAnalysisIndex].goingShowUp();

        leftTopCustomPanel.add( analyses[currentAnalysisIndex].getLeftTopPanel(), BorderLayout.CENTER );
        rightTopCustomPanel.add( analyses[currentAnalysisIndex].getRightTopPanel(), BorderLayout.CENTER );
        bottomPanel.add( analyses[currentAnalysisIndex].getBottomPanel(), BorderLayout.CENTER );

        analysisCommonPart.setDescriptionText( analyses[currentAnalysisIndex].getDescriptionText() );

        analysisPanel.validate();
        analysisPanel.repaint();
    }


    /**  Performs actions before close the panel */
    public void goingShowOff() {
        analyses[currentAnalysisIndex].goingShowOff();
    }


    /**  Sets all analyzes in the initial state with removing all temporary data */
    public void initialize() {
        analysisCommonPart.initialize();
        for ( int i = 0; i < analyses.length; i++ ) {
            analyses[i].initialize();
        }
        currentAnalysisIndex = 0;
    }


    //------------------------------------------------------
    //SET external references, message text field, fonts etc.
    //------------------------------------------------------

    /**
     *  Sets the rawDataPane attribute of the AnalysisController object
     *
     *@param  rawDataPanel_In  The new rawDataPanel value
     */
    public void setRawDataPanel( RawDataPanel rawDataPanel_In ) {
        rawDataPanel = rawDataPanel_In;
        crossParamMap.put( "RawDataPanel", rawDataPanel );
        crossParamMap.put( "RawEmittanceData", rawDataPanel.getEmittanceData() );

        //set the listener for generation of the new raw emittance data
        rawDataPanel.setInitializationAnalysisListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    initialize();
                    crossParamMap.put( "GAMMA_BETA", new Double( rawDataPanel.getGammaBeta() ) );
                }
            } );
    }


    /**
     *  Returns the analysis panel
     *
     *@return    The analysisPanel value
     */
    public JPanel getAnalysisPanel() {
        return analysisPanel;
    }


    /**
     *  Returns the hash map table with global parameters of the analysis
     *
     *@return    The HashMap with global parameters
     */
    public HashMap<String,Object> getParamsHashMap() {
        return crossParamMap;
    }


    /**
     *  Sets the message text field.
     *
     *@param  messageTextLocal  The new message text filed
     */
    public void setMessageTextField( JTextField messageTextLocal ) {
        this.messageTextLocal = messageTextLocal;
        analysisCommonPart.setMessageTextField( messageTextLocal );
        for ( int i = 0; i < analyses.length; i++ ) {
            analyses[i].setMessageTextField( messageTextLocal );
        }
    }


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new fontForAll value
     */
    public void setFontForAll( Font fnt ) {

        //Font fnt = new Font( fnt_in.getName(), fnt_in.getStyle(), fnt_in.getSize() + 2 );

        analysisCommonPart.setFontForAll( fnt );
        for ( int i = 0; i < analyses.length; i++ ) {
            analyses[i].setFontForAll( fnt );
        }
    }

}
