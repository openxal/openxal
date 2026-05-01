package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.*;

/**
 *  This analysis is threshold independent. It calculates
 *  avg(emittance)/fraction as a function of the threshold (in this case it is
 *  an independent value) and plots it in (1-(1-t)(1-ln(1-t))/t coordinates.
 *  Analysis of this curve that should be straight line in the case of caussian
 *  distribution gives us emittance estiamtion
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisThresholdIndependentFit extends AnalysisBasic {

    //emittance data as ColorSurfaceData instance (for analysis only)
    private ColorSurfaceData emittance3Da = null;

    //bottom panel. It includes the graph panel (bottom left)
    //and the controll panel (bottom right)
    JPanel bottomPanel = null;
    JPanel graphPanel = new JPanel( new BorderLayout() );
    JPanel controllPanel = new JPanel( new BorderLayout() );

    private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

    private BasicGraphData gdFitted = new BasicGraphData();

    private BasicGraphData gdRatio = new BasicGraphData();

    //----------------------------------
    //GUI elements of the controll panel
    //----------------------------------

    //threshold scan parameters
    private JPanel thresholdScanPanel =
        new JPanel( new BorderLayout() );

    private JLabel emtCalculation_Label =
        new JLabel( "=== THRESHOLD SCAN [%] ===", JLabel.CENTER );

    private JLabel emtStart_Label = new JLabel( "Start", JLabel.CENTER );
    private JLabel emtStep_Label = new JLabel( "Step", JLabel.CENTER );
    private JLabel emtStop_Label = new JLabel( "Stop", JLabel.CENTER );

    private JSpinner emtStart_Spinner =
        new JSpinner( new SpinnerNumberModel( 50, -100, 100, 5 ) );
    private JSpinner emtStep_Spinner =
        new JSpinner( new SpinnerNumberModel( 5, 1, 25, 1 ) );
    private JSpinner emtStop_Spinner =
        new JSpinner( new SpinnerNumberModel( 95, 5, 100, 5 ) );

    //calculation buttons
    private JButton plotGraphs_Button = new JButton( "PLOT GRAPHS" );
    private JButton calcEmitButton =
        new JButton( "FIT & SHOW FITTED EMITTANCE" );

    //text field to show fitted independent emittance
    private DoubleInputTextField emtLocal_Text = new DoubleInputTextField( 14 );
    private JLabel fittedEmt_Label = new JLabel( "Fitted emittance :", JLabel.RIGHT );

    private NumberFormat dbl_Format = new ScientificNumberFormat(5);

    //boolean variable indicating that data are not empty
    private boolean isDataExist = false;


    /**
     *  Constructor for the AnalysisThresholdIndependentFit object
     *
     *@param  crossParamMap         The HashMap with Parameters of the analyses
     *@param  analysisTypeIndex_In  The type index of the analysis
     */
    AnalysisThresholdIndependentFit( int analysisTypeIndex_In, HashMap<String,Object> crossParamMap ) {
        super( analysisTypeIndex_In, crossParamMap );

        analysisDescriptionString =
            " THRESH. INDEPENDENT" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "This analysis plots the emittance and fraction " +
            "ratio as a function of the special value " +
            "(1-t*(1-ln(t)))/(1-t)  where t is threshold." +
            "By fitting this function with a line we can" +
            " find the emittance as a slope coefficient.";

        //graph panel properties
        GP.setLegendVisible( true );
        GP.setLegendButtonVisible( true );
        GP.setOffScreenImageDrawing( true );
        GP.setLegendKeyString( "Legend" );
        GP.setGraphBackGroundColor( Color.white );
        GP.removeAllGraphData();

        //buttons look and feel
        plotGraphs_Button.setForeground( Color.blue.darker() );
        //plotGraphs_Button.setBackground( Color.cyan );

        calcEmitButton.setForeground( Color.blue.darker() );
        //calcEmitButton.setBackground( Color.cyan );

        emtLocal_Text.setNumberFormat( dbl_Format );
        emtLocal_Text.setHorizontalAlignment( JTextField.CENTER );
        emtLocal_Text.setEditable( false );
        emtLocal_Text.setText( null );
        emtLocal_Text.setBackground( Color.white );

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

        graphPanel.add( GP, BorderLayout.CENTER );

        JPanel calcEmiButtonPanel =
            new JPanel( new FlowLayout( FlowLayout.CENTER, 3, 3 ) );
        calcEmiButtonPanel.add( calcEmitButton );

        JPanel resultsPanel =
            new JPanel( new BorderLayout() );
        resultsPanel.add( fittedEmt_Label, BorderLayout.CENTER );
        resultsPanel.add( emtLocal_Text, BorderLayout.EAST );

        JPanel resAndButtonPanel = new JPanel( new BorderLayout() );
        resAndButtonPanel.add( resultsPanel, BorderLayout.CENTER );
        resAndButtonPanel.add( calcEmiButtonPanel, BorderLayout.SOUTH );

        controllPanel.add( thresholdScanPanel, BorderLayout.CENTER );
        controllPanel.add( resAndButtonPanel, BorderLayout.SOUTH );

        //threshold scan control parameter panel
        JPanel emtScanSubPanel_0 = new JPanel( new GridLayout( 2, 3, 1, 1 ) );
        emtScanSubPanel_0.setBorder( etchedBorder );
        emtScanSubPanel_0.add( emtStart_Label );
        emtScanSubPanel_0.add( emtStep_Label );
        emtScanSubPanel_0.add( emtStop_Label );
        emtScanSubPanel_0.add( emtStart_Spinner );
        emtScanSubPanel_0.add( emtStep_Spinner );
        emtScanSubPanel_0.add( emtStop_Spinner );

        JPanel emtScanSubPanel_1 = new JPanel();
        emtScanSubPanel_1.setLayout(
            new FlowLayout( FlowLayout.CENTER, 1, 1 ) );
        emtScanSubPanel_1.add( plotGraphs_Button );

        JPanel emtScanSubPanel_2 = new JPanel( new BorderLayout() );
        emtScanSubPanel_2.setBorder( etchedBorder );
        emtScanSubPanel_2.add( emtCalculation_Label, BorderLayout.NORTH );
        emtScanSubPanel_2.add( emtScanSubPanel_0, BorderLayout.CENTER );
        emtScanSubPanel_2.add( emtScanSubPanel_1, BorderLayout.SOUTH );

        thresholdScanPanel.add( emtScanSubPanel_2, BorderLayout.NORTH );

        gdFitted.setGraphColor( Color.red );
        gdFitted.setDrawPointsOn( false );
        gdFitted.removeAllPoints();
        gdFitted.setGraphProperty( GP.getLegendKeyString(),
            "fitting" );
        gdFitted.setLineThick( 2 );

        gdRatio.setGraphColor( Color.black );
        gdRatio.setDrawPointsOn( true );
        gdRatio.removeAllPoints();
        gdRatio.setGraphProperty( GP.getLegendKeyString(),
            "data" );
        gdRatio.setLineThick( 2 );

        //default index of graph is 0
        GP.addGraphData( gdRatio );
        GP.addGraphData( gdFitted );

        GP.setAxisNameX( "(1-t*(1-ln(t)))/(1-t), t is threshold" );
        GP.setAxisNameY( "<emitt>/<fraction>" );
        GP.setName( "The threshold independent analysis" );

        //graph calculations button action
        plotGraphs_Button.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    getTextMessage().setText( null );

                    emtLocal_Text.setText( null );
                    emtLocal_Text.setBackground( Color.white );

                    //clear all old information
                    gdRatio.removeAllPoints();
                    gdFitted.removeAllPoints();

                    GP.clearZoomStack();

                    //plotting the graphs
                    calculateGraphs();

                    //refresh graph panel
                    GP.refreshGraphJPanel();
                }
            } );

        calcEmitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    getTextMessage().setText( null );

                    emtLocal_Text.setText( null );
                    emtLocal_Text.setBackground( Color.white );

                    Double betaGamma_D = (Double) getParamsHashMap().get( "GAMMA_BETA" );
                    double betaGamma = betaGamma_D.doubleValue();

                    //place for emittance calculations

                    double emt = 0.;

                    gdFitted.removeAllPoints();

                    //make fitting ????
                    double x_min = GP.getCurrentMinX();
                    double x_max = GP.getCurrentMaxX();
                    double y_min = GP.getCurrentMinY();
                    double y_max = GP.getCurrentMaxY();

                    double yx_avg = 0.;
                    double x2_avg = 0.;

                    double x = 0.;
                    double y = 0.;

                    int nPoints = gdRatio.getNumbOfPoints();

                    for ( int i = 0; i < nPoints; i++ ) {
                        x = gdRatio.getX( i );
                        y = gdRatio.getY( i );
                        if ( x >= x_min &&
                            x <= x_max &&
                            y >= y_min &&
                            y <= y_max ) {
                            yx_avg += x * y;
                            x2_avg += x * x;
                        }
                    }

                    if ( yx_avg <= 0. || x2_avg <= 0. ) {
                        getTextMessage().setText(
                            "Cannot get fit for emittance." +
                            " Make right zoom region." );
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }

                    //important note - <emt> = 2*emt where emt calculated over
                    //region where density > threshold,so we do not need factor 2
                    emt = yx_avg / x2_avg;

                    gdFitted.addPoint( 0., 0. );
                    gdFitted.addPoint( gdRatio.getX( nPoints - 1 ), gdRatio.getX( nPoints - 1 ) * emt );

                    emt *= betaGamma;

                    emtLocal_Text.setValue( emt );

                    GP.clearZoomStack();
                    GP.refreshGraphJPanel();

                }
            } );

    }


    /**  Performs actions before show the panel */
    void goingShowUp() {
        emittance3Da = (ColorSurfaceData) getParamsHashMap().get( "RawEmittanceData" );

        isDataExist = true;

        double z_min = emittance3Da.getMinZ();
        double z_max = emittance3Da.getMaxZ();

        if ( z_max <= 0. || z_min == z_max ) {
            isDataExist = false;
        }

        if ( !isDataExist ) {
            getTextMessage().setText( null );
            getTextMessage().setText( "The data for analysis" +
                " do not exist." );
            Toolkit.getDefaultToolkit().beep();

            plotGraphs_Button.setEnabled( false );
            calcEmitButton.setEnabled( false );

            emtLocal_Text.setText( null );
            emtLocal_Text.setBackground( Color.white );

            //clear all old information
            gdRatio.removeAllPoints();
            gdFitted.removeAllPoints();

            GP.clearZoomStack();

        }
        else {
            plotGraphs_Button.setEnabled( true );
            calcEmitButton.setEnabled( true );
        }
    }


    /**  Performs actions before close the panel */
    void goingShowOff() {
    }


    /**  Sets all analyzes in the initial state with removing all temporary data */
    void initialize() {

        isDataExist = false;

        //clear all old information
        gdRatio.removeAllPoints();
        gdFitted.removeAllPoints();

        emtLocal_Text.setText( null );
        emtLocal_Text.setBackground( Color.white );

        GP.clearZoomStack();

    }


    /**  Creates objects for the global HashMap using put method only */
    void createHashMapObjects() {
    }


    /**
     *  Connects to the objects in the global HashMap using only get method of
     *  the HashMap
     */
    void connectToHashMapObjects() {

    }


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new font
     */
    void setFontForAll( Font fnt ) {

        emtCalculation_Label.setFont( fnt );
        emtStart_Label.setFont( fnt );
        emtStep_Label.setFont( fnt );
        emtStop_Label.setFont( fnt );

        plotGraphs_Button.setFont( fnt );
        calcEmitButton.setFont( fnt );

        emtLocal_Text.setFont( fnt );
        fittedEmt_Label.setFont( fnt );

        emtStart_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) emtStart_Spinner.getEditor() ).getTextField().setFont( fnt );
        emtStep_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) emtStep_Spinner.getEditor() ).getTextField().setFont( fnt );
        emtStop_Spinner.setFont( fnt );
        ( (JSpinner.DefaultEditor) emtStop_Spinner.getEditor() ).getTextField().setFont( fnt );

    }


    /**  Calculates all graphs data - fraction, emittance, alpha, beta, gamma */
    private void calculateGraphs() {

        int start = ( (Integer) emtStart_Spinner.getValue() ).intValue();
        int step = ( (Integer) emtStep_Spinner.getValue() ).intValue();
        int stop = ( (Integer) emtStop_Spinner.getValue() ).intValue();

        Double betaGamma_D = (Double) getParamsHashMap().get( "GAMMA_BETA" );
        double betaGamma = betaGamma_D.doubleValue();

        //array elem. 0 - fraction [%]
        //array elem. 1 - emittance
        //array elem. 2 - alpha
        //array elem. 3 - beta
        //array elem. 4 - gamma
        double[] resArr = null;

        double thresh = 0.;
        double x = 0.;

        for ( int i = start; i < stop; i += step ) {
            thresh = ( (double) i ) / 100.;

            resArr = EmtCalculations.getFracEmtAlphaBetaGamma( 100. * thresh, emittance3Da );
            if ( resArr == null ) {
                continue;
            }

            if ( thresh > 0. && thresh < 1.0 && resArr[0] > 0. ) {
                x = ( 1.0 - thresh * ( 1.0 - Math.log( thresh ) ) ) / ( 1.0 - thresh );
                gdRatio.addPoint( x, resArr[1] );
            }
        }
    }
}

