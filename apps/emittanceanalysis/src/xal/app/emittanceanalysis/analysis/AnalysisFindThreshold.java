package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 *  This analysis plots the fraction of the beam, emittance, alpha, beta, and
 *  gamma as a function of threshold to find a reasonable value of the threshold
 *  for other analyses
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisFindThreshold extends AnalysisBasic {

    //emittance data as ColorSurfaceData instance (for analysis only)
    private ColorSurfaceData emittance3Da = null;

    //threshold text field from common part of the left top corner panel
    private DoubleInputTextField threshold_Text = null;

    //threshold text field listener (on the common left top corner panel)
    //It is local
    private ActionListener thresholdTextListener = null;

    //bottom panel. It includes the graph panel (bottom left)
    //and the controll panel (bottom right)
    JPanel bottomPanel = null;
    JPanel graphPanel = new JPanel( new BorderLayout() );
    JPanel controllPanel = new JPanel( new BorderLayout() );

    private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

    private ActionListener dragVerLine_Listener = null;

    private BasicGraphData gdFrac = new BasicGraphData();
    private BasicGraphData gdEmt = new BasicGraphData();
    private BasicGraphData gdAlpha = new BasicGraphData();
    private BasicGraphData gdBeta = new BasicGraphData();
    private BasicGraphData gdGamma = new BasicGraphData();
    private BasicGraphData[] gdArr = new BasicGraphData[5];

    private int dataIndex = 0;

    private Color[] colorArr = {
        Color.black,
        Color.red,
        Color.blue,
        Color.cyan,
        Color.magenta};

    private String[] graphNames = {
        "Fraction of the beam",
        "Emittance of the beam",
        "Alpha parameter [a.u.]",
        "Beta parameter [mm mrad]",
        "Gamma parameter [mrad/mm]"};

    private String[] xAxisNames = {
        "threshold [%]",
        "threshold [%]",
        "threshold [%]",
        "threshold [%]",
        "threshold [%]"};

    private String[] yAxisNames = {
        "fraction [%]",
        "emittance [mm mrad]",
        "alpha [ ]",
        "beta [mm mrad]",
        "gamma [mrad/mm]"};

    //----------------------------------
    //GUI elements of the controll panel
    //----------------------------------

    //radio-buttons panel
    private JPanel buttonPanel = new JPanel( new GridLayout( 5, 1 ) );

    private JRadioButton frac_Button = new JRadioButton( " fraction ", true );
    private JRadioButton emt_Button = new JRadioButton( " emittance ", false );
    private JRadioButton alpha_Button = new JRadioButton( " alpha ", false );
    private JRadioButton beta_Button = new JRadioButton( " beta ", false );
    private JRadioButton gamma_Button = new JRadioButton( " gamma ", false );
    private ButtonGroup buttonGroup = new ButtonGroup();
    private JRadioButton[] buttonArr = new JRadioButton[5];

    //threshold scan parameters
    private JPanel thresholdScanPanel =
        new JPanel( new BorderLayout() );

    private JLabel threshCalculation_Label =
        new JLabel( "=== THRESHOLD SCAN [%] ===", JLabel.CENTER );

    private JLabel threshStart_Label = new JLabel( "Start", JLabel.CENTER );
    private JLabel threshStep_Label = new JLabel( "Step", JLabel.CENTER );
    private JLabel threshStop_Label = new JLabel( "Stop", JLabel.CENTER );

    private JSpinner threshStart_Spinner =
        new JSpinner( new SpinnerNumberModel( -10.0, -100., 100., 1.0 ) );
    private JSpinner threshStep_Spinner =
        new JSpinner( new SpinnerNumberModel( 0.2, 0.1, 25.0, 0.1 ) );
    private JSpinner threshStop_Spinner =
        new JSpinner( new SpinnerNumberModel( 10.0, -100., 100., 1.0 ) );

    //calculation buttons
    private JButton plotGraphs_Button = new JButton( "PLOT GRAPHS" );
    private JButton calcEmitButton =
        new JButton( "SET THRESHOLD & RMS EMITTANCE" );

    //text fields borrowed from common panel
    private DoubleInputTextField alphaRMS_Text = null;
    private DoubleInputTextField betaRMS_Text = null;
    private DoubleInputTextField emtRMS_Text = null;
    private boolean is_ready_rms = false;

    //1-x*(1-ln(x)) function and inverse function
    private BasicGraphData emtFracFunc = new BasicGraphData();
    private BasicGraphData emtFracFuncInv = new BasicGraphData();


    /**
     *  Constructor for the AnalysisFindThreshold object
     *
     *@param  crossParamMap         The HashMap with Parameters of the analyses
     *@param  analysisTypeIndex_In  The type index of the analysis
     */
    AnalysisFindThreshold( int analysisTypeIndex_In, HashMap<String,Object> crossParamMap ) {
        super( analysisTypeIndex_In, crossParamMap );

        colorArr[3] = new Color( 128, 128, 255 );

        analysisDescriptionString =
            " THRESHOLD FINDING" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "This analysis plots the fraction " +
            "of the beam, emittance, alpha, beta, and gamma " +
            "as a function of threshold to find a " +
            "reasonable value of the threshold for " +
            "other analyses.";

        //graph panel properties
        GP.setLegendVisible( true );
        GP.setLegendButtonVisible( true );
        GP.setOffScreenImageDrawing( true );
        GP.setLegendKeyString( "Legend" );
        GP.setGraphBackGroundColor( Color.white );
        GP.removeAllGraphData();
        GP.setDraggingVerLinesGraphMode( true );
        GP.addVerticalLine( 100., Color.red );

        //buttons look and feel
        plotGraphs_Button.setForeground( Color.blue.darker() );
        //plotGraphs_Button.setBackground( Color.cyan );

        //set panels layout
        Border etchedBorder = BorderFactory.createEtchedBorder();

        bottomPanel = getBottomPanel();
        bottomPanel.setLayout( new BorderLayout() );
        bottomPanel.add( graphPanel, BorderLayout.CENTER );

        JPanel rightBottomPanel = new JPanel( new BorderLayout() );
        rightBottomPanel.add( controllPanel, BorderLayout.NORTH );

        bottomPanel.add( rightBottomPanel, BorderLayout.EAST );

        graphPanel.setBorder( etchedBorder );
        controllPanel.setBorder( etchedBorder );
        buttonPanel.setBorder( etchedBorder );

        graphPanel.add( GP, BorderLayout.CENTER );

        JPanel calcEmiButtonPanel =
            new JPanel( new FlowLayout( FlowLayout.CENTER, 3, 3 ) );
        calcEmiButtonPanel.add( calcEmitButton );

        calcEmitButton.setForeground( Color.blue.darker() );
        //calcEmitButton.setBackground( Color.cyan );

        controllPanel.add( buttonPanel, BorderLayout.WEST );
        controllPanel.add( thresholdScanPanel, BorderLayout.CENTER );
        controllPanel.add( calcEmiButtonPanel, BorderLayout.SOUTH );

        //set elements of the radio-buttons panel
        buttonPanel.add( frac_Button );
        buttonPanel.add( emt_Button );
        buttonPanel.add( alpha_Button );
        buttonPanel.add( beta_Button );
        buttonPanel.add( gamma_Button );

        buttonGroup.add( frac_Button );
        buttonGroup.add( emt_Button );
        buttonGroup.add( alpha_Button );
        buttonGroup.add( beta_Button );
        buttonGroup.add( gamma_Button );

        //threshold scan control parameter panel
        JPanel thresholdSubPanel_0 = new JPanel( new GridLayout( 2, 3, 1, 1 ) );
        thresholdSubPanel_0.setBorder( etchedBorder );
        thresholdSubPanel_0.add( threshStart_Label );
        thresholdSubPanel_0.add( threshStep_Label );
        thresholdSubPanel_0.add( threshStop_Label );
        thresholdSubPanel_0.add( threshStart_Spinner );
        thresholdSubPanel_0.add( threshStep_Spinner );
        thresholdSubPanel_0.add( threshStop_Spinner );

        JPanel thresholdSubPanel_1 = new JPanel();
        thresholdSubPanel_1.setLayout(
            new FlowLayout( FlowLayout.CENTER, 1, 1 ) );
        thresholdSubPanel_1.add( plotGraphs_Button );

        JPanel thresholdSubPanel_2 = new JPanel( new BorderLayout() );
        thresholdSubPanel_2.setBorder( etchedBorder );
        thresholdSubPanel_2.add( threshCalculation_Label, BorderLayout.NORTH );
        thresholdSubPanel_2.add( thresholdSubPanel_0, BorderLayout.CENTER );
        thresholdSubPanel_2.add( thresholdSubPanel_1, BorderLayout.SOUTH );

        thresholdScanPanel.add( thresholdSubPanel_2, BorderLayout.NORTH );

        gdArr[0] = gdFrac;
        gdArr[1] = gdEmt;
        gdArr[2] = gdAlpha;
        gdArr[3] = gdBeta;
        gdArr[4] = gdGamma;

        buttonArr[0] = frac_Button;
        buttonArr[1] = emt_Button;
        buttonArr[2] = alpha_Button;
        buttonArr[3] = beta_Button;
        buttonArr[4] = gamma_Button;

        ActionListener radioButtonListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    JRadioButton source = (JRadioButton) e.getSource();
                    int ind = -1;
                    for ( int i = 0; i < buttonArr.length; i++ ) {
                        if ( source == buttonArr[i] ) {
                            ind = i;
                        }
                    }
                    if ( ind < 0 ) {
                        return;
                    }
                    setDataIndex( ind );
                }
            };

        for ( int i = 0; i < colorArr.length; i++ ) {
            gdArr[i].setGraphColor( colorArr[i] );
            gdArr[i].setDrawPointsOn( false );
            gdArr[i].removeAllPoints();
            gdArr[i].setGraphProperty( GP.getLegendKeyString(),
                buttonArr[i].getText() );
            gdArr[i].setLineThick( 2 );
            gdArr[i].setImmediateContainerUpdate( false );
            buttonArr[i].setForeground( colorArr[i] );
            buttonArr[i].addActionListener( radioButtonListener );
        }

        //default index of graph is 0
        GP.addGraphData( gdArr[dataIndex] );
        GP.setAxisNameX( xAxisNames[dataIndex] );
        GP.setAxisNameY( yAxisNames[dataIndex] );
        GP.setName( graphNames[dataIndex] );

        //threshold text field listener (on the common left top corner panel)
        thresholdTextListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    DoubleInputTextField txt = (DoubleInputTextField) e.getSource();
                    GP.addDraggedVerLinesListener( null );
                    double thresh_new = txt.getValue();
                    GP.setVerticalLineValue( thresh_new, 0 );
                    GP.addDraggedVerLinesListener( dragVerLine_Listener );
                }
            };

        dragVerLine_Listener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    int ind = GP.getDraggedLineIndex();
                    if ( ind != 0 ) {
                        return;
                    }
                    double pos = GP.getVerticalValue( ind );
                    threshold_Text.setValueQuietly( pos );
                }
            };

        GP.addDraggedVerLinesListener( dragVerLine_Listener );
        GP.setDraggedVerLinesMotionListen( true );

        //graph calculations button action
        plotGraphs_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    getTextMessage().setText( null );

                    for ( int i = 0; i < buttonArr.length; i++ ) {
                        buttonArr[i].setEnabled( false );

                    }

                    for ( int i = 0; i < gdArr.length; i++ ) {
                        gdArr[i].removeAllPoints();
                    }

                    is_ready_rms = false;

                    getParamsHashMap().put( "IS_READY_RMS", new Boolean( is_ready_rms ) );

                    alphaRMS_Text.setText( null );
                    alphaRMS_Text.setBackground( Color.white );

                    betaRMS_Text.setText( null );
                    betaRMS_Text.setBackground( Color.white );

                    emtRMS_Text.setText( null );
                    emtRMS_Text.setBackground( Color.white );

                    calculateGraphs();

                    for ( int i = 0; i < buttonArr.length; i++ ) {
                        buttonArr[i].setEnabled( true );
                    }

                    GP.clearZoomStack();
                    GP.refreshGraphJPanel();
                }
            } );

        calcEmitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    Double betaGamma_D = (Double) getParamsHashMap().get( "GAMMA_BETA" );
                    double betaGamma = betaGamma_D.doubleValue();

                    getTextMessage().setText( null );

                    is_ready_rms = false;
                    getParamsHashMap().put( "IS_READY_RMS", new Boolean( is_ready_rms ) );

                    //array elem. 0 - fraction [%]
                    //array elem. 1 - emittance
                    //array elem. 2 - alpha
                    //array elem. 3 - beta
                    //array elem. 4 - gamma
                    double[] resArr = null;

                    double thresh = threshold_Text.getValue();
                    resArr = EmtCalculations.getFracEmtAlphaBetaGamma( thresh, emittance3Da );

                    if ( resArr == null ) {
                        getTextMessage().setText( null );
                        getTextMessage().setText( "Cannot process data. " +
                            "They can be empty or threshold parameter is wrong." );
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    resArr[1] *= betaGamma;

                    emtRMS_Text.setValue( resArr[1] );
                    alphaRMS_Text.setValue( resArr[2] );
                    betaRMS_Text.setValue( resArr[3] );

                    is_ready_rms = true;
                    getParamsHashMap().put( "IS_READY_RMS", new Boolean( is_ready_rms ) );

                }
            } );
    }


    /**
     *  Sets the new data index. See above for index meaning
     *
     *@param  dataIndex_new  The new new data index
     */
    private void setDataIndex( int dataIndex_new ) {
        int dataIndex_old = dataIndex;

        //set pressed button
        buttonArr[dataIndex_new].setSelected( true );

        //set graph panel decoration
        GP.removeAllGraphData();
        GP.addGraphData( gdArr[dataIndex_new] );
        GP.setAxisNameX( xAxisNames[dataIndex_new] );
        GP.setAxisNameY( yAxisNames[dataIndex_new] );
        GP.setName( graphNames[dataIndex_new] );

        //set vertical line at the right position
        //according to new transformation function
        GP.addDraggedVerLinesListener( null );
        double thresh_new = threshold_Text.getValue();
        GP.setVerticalLineValue( thresh_new, 0 );
        GP.addDraggedVerLinesListener( dragVerLine_Listener );

        GP.clearZoomStack();
        GP.refreshGraphJPanel();
    }



    /**  Performs actions before show the panel */
    void goingShowUp() {
        threshold_Text.setEditable( true );
        is_ready_rms = ( (Boolean) getParamsHashMap().get( "IS_READY_RMS" ) ).booleanValue();
        threshold_Text.addActionListener( thresholdTextListener );

        emittance3Da = (ColorSurfaceData) getParamsHashMap().get( "RawEmittanceData" );

        //set vertical line at the right position
        //according to new transformation function
        GP.addDraggedVerLinesListener( null );
        double thresh_new = threshold_Text.getValue();
        GP.setVerticalLineValue( thresh_new, 0 );
        GP.addDraggedVerLinesListener( dragVerLine_Listener );

        //check that data exist
        double z_min = emittance3Da.getMinZ();
        double z_max = emittance3Da.getMaxZ();

        if ( z_max <= 0. || z_min == z_max ) {
            getTextMessage().setText( "The data for analysis do not" +
                " exist" );
            Toolkit.getDefaultToolkit().beep();
            plotGraphs_Button.setEnabled( false );
            calcEmitButton.setEnabled( false );
        }
        else {
            plotGraphs_Button.setEnabled( true );
            calcEmitButton.setEnabled( true );
        }
    }


    /**  Performs actions before close the panel */
    void goingShowOff() {
        getParamsHashMap().put( "IS_READY_RMS", new Boolean( is_ready_rms ) );
        threshold_Text.setEditable( false );
        threshold_Text.setBackground( Color.white );
        threshold_Text.removeActionListener( thresholdTextListener );
    }


    /**  Sets all analyzes in the initial state with removing all temporary data */
    void initialize() {

        //set data index in the initial state
        setDataIndex( 0 );

        for ( int i = 0; i < gdArr.length; i++ ) {
            gdArr[i].removeAllPoints();
        }

    }


    /**  Creates objects for the global HashMap using put method only */
    void createHashMapObjects() { }


    /**
     *  Connects to the objects in the global HashMap using only get method of
     *  the HashMap
     */
    void connectToHashMapObjects() {
        threshold_Text = (DoubleInputTextField) getParamsHashMap().get( "THRESHOLD_TEXT" );

        alphaRMS_Text = (DoubleInputTextField) getParamsHashMap().get( "ALPHA_RMS" );
        betaRMS_Text = (DoubleInputTextField) getParamsHashMap().get( "BETA_RMS" );
        emtRMS_Text = (DoubleInputTextField) getParamsHashMap().get( "EMT_RMS" );

        is_ready_rms = ( (Boolean) getParamsHashMap().get( "IS_READY_RMS" ) ).booleanValue();

        //register the FIT analysis init listener
        ActionListener init_listener_fit = (ActionListener)
            getParamsHashMap().get( "INIT_LISTENER_FIT" );
        calcEmitButton.addActionListener( init_listener_fit );
        ActionListener init_listener_gau = (ActionListener)
            getParamsHashMap().get( "INIT_LISTENER_GAU" );
        calcEmitButton.addActionListener( init_listener_gau );
        ActionListener init_listener_profile = (ActionListener)
            getParamsHashMap().get( "INIT_LISTENER_PROFILE" );
        calcEmitButton.addActionListener( init_listener_profile );
	
	//angle of rotation for  transformed phase space
        ActionListener init_listener_aor = (ActionListener)
            getParamsHashMap().get( "INIT_LISTENER_AOR" );
	 calcEmitButton.addActionListener( init_listener_aor );   
	
        plotGraphs_Button.addActionListener( init_listener_fit );
        plotGraphs_Button.addActionListener( init_listener_gau );
	plotGraphs_Button.addActionListener( init_listener_aor );
    }


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new font
     */
    void setFontForAll( Font fnt ) {
        for ( int i = 0; i < buttonArr.length; i++ ) {
            buttonArr[i].setFont( fnt );
        }

        threshCalculation_Label.setFont( fnt );
        threshStart_Label.setFont( fnt );
        threshStep_Label.setFont( fnt );
        threshStop_Label.setFont( fnt );

        plotGraphs_Button.setFont( fnt );
        calcEmitButton.setFont( fnt );

        threshStart_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) threshStart_Spinner.getEditor() ).getTextField().setFont( fnt );
        threshStep_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) threshStep_Spinner.getEditor() ).getTextField().setFont( fnt );
        threshStop_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) threshStop_Spinner.getEditor() ).getTextField().setFont( fnt );

    }


    /**  Calculates all graphs data - fraction, emittance, alpha, beta, gamma */
    private void calculateGraphs() {

        double start = ( (Double) threshStart_Spinner.getValue() ).doubleValue();
        double step = ( (Double) threshStep_Spinner.getValue() ).doubleValue();
        double stop = ( (Double) threshStop_Spinner.getValue() ).doubleValue();

        Double betaGamma_D = (Double) getParamsHashMap().get( "GAMMA_BETA" );
        if ( betaGamma_D == null ) {
            getTextMessage().setText( null );
            getTextMessage().setText( "The emittance data are not ready." );
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        double betaGamma = betaGamma_D.doubleValue();

        //array elem. 0 - fraction [%]
        //array elem. 1 - emittance
        //array elem. 2 - alpha
        //array elem. 3 - beta
        //array elem. 4 - gamma
        double[] resArr = null;

        int nPoint = 0;

        for ( double thresh = start; thresh < stop; thresh += step ) {
            resArr = EmtCalculations.getFracEmtAlphaBetaGamma( thresh, emittance3Da );
            if ( resArr == null ) {
                getTextMessage().setText( null );
                getTextMessage().setText( "Cannot process data. " +
                    "They can be empty or threshold scan parameters are wrong." );
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            resArr[1] *= betaGamma;

            for ( int j = 0; j < gdArr.length; j++ ) {
                gdArr[j].addPoint( thresh, resArr[j] );
                nPoint++;
            }
        }
    }
}
