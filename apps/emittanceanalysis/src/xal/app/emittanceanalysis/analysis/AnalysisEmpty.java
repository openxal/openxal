package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 *  This is an empty analysis. It does nothing
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisEmpty extends AnalysisBasic {

    //emittance data as ColorSurfaceData instance (for analysis only)
    private ColorSurfaceData emittance3Da = null;

    //threshold text field from common part of the left top corner panel
    private DoubleInputTextField threshold_Text = null;

    //bottom panel. It includes the graph panel (bottom left)
    //and the controll panel (bottom right)
    private JPanel bottomPanel = null;
    private JPanel graphPanel = new JPanel( new BorderLayout() );
    private JPanel controllPanel = new JPanel( new BorderLayout() );
    private JPanel leftControllPanel = new JPanel( new BorderLayout() );
    private JPanel rightControllPanel = new JPanel( new BorderLayout() );

    //usual graph with a cross-section graph
    private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

    //color surface graph with space phase data
    private FunctionGraphsJPanel GP_ep = null;

    //JRadioButtons for cross line types
    private JRadioButton cent_button = new JRadioButton();
    private JRadioButton vert_button = new JRadioButton();
    private JRadioButton horz_button = new JRadioButton();

    private ButtonGroup buttonGroup = new ButtonGroup();

    private JLabel cent_label = new JLabel( "C", JLabel.CENTER );
    private JLabel vert_label = new JLabel( "V", JLabel.CENTER );
    private JLabel horz_label = new JLabel( "H", JLabel.CENTER );

    //sliders for cross line types
    private JScrollBar cent_slider = new JScrollBar( JScrollBar.VERTICAL, 0, 4, 0, 364 );
    private JScrollBar vert_slider = new JScrollBar( JScrollBar.VERTICAL, 0, 4, 0, 200 );
    private JScrollBar horz_slider = new JScrollBar( JScrollBar.VERTICAL, 0, 4, 0, 200 );

    //curve data for threshold and bounding rectangular
    private CurveData thresh_CurveData = new CurveData();
    private CurveData boundR_CurveData = new CurveData();
    private CurveData waveForm_CurveData = new CurveData();
    private CurveData section_CurveData = new CurveData();

    //boolean variable indicating that data are not empty
    private boolean isDataExist = false;


    /**
     *  Constructor for the AnalysisEmpty object
     *
     *@param  crossParamMap         The HashMap with Parameters of the analyses
     *@param  analysisTypeIndex_In  The type index of the analysis
     */
    AnalysisEmpty( int analysisTypeIndex_In, HashMap<String,Object> crossParamMap ) {
        super( analysisTypeIndex_In, crossParamMap );
        analysisDescriptionString = " PHASE DENSITY VIEWER" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "RED - phase space density" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "BLUE - threshold" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "BLACK - min value";

        //graph panel properties
        GP.setLegendVisible( false );
        GP.setLegendButtonVisible( false );
        GP.setOffScreenImageDrawing( true );
        GP.setGraphBackGroundColor( Color.white );
        GP.setAxisNameX( "position in section" );
        GP.setAxisNameY( "value" );
        GP.setName( "Phase Space Section" );
        GP.removeAllGraphData();

        //define buttons and labels
        cent_button.setSelected( true );
        vert_button.setSelected( false );
        horz_button.setSelected( false );

        buttonGroup.add( cent_button );
        buttonGroup.add( vert_button );
        buttonGroup.add( horz_button );

        cent_button.setToolTipText( "centered line section" );
        vert_button.setToolTipText( "horizontal line section" );
        horz_button.setToolTipText( "vertical line section" );

        cent_label.setToolTipText( "centered line section" );
        vert_label.setToolTipText( "horizontal line section" );
        horz_label.setToolTipText( "vertical line section" );

        //Panels
        Border etchedBorder = BorderFactory.createEtchedBorder();

        bottomPanel = getBottomPanel();
        bottomPanel.setBorder( etchedBorder );
        bottomPanel.setLayout( new BorderLayout() );
        bottomPanel.add( graphPanel, BorderLayout.CENTER );
        bottomPanel.add( controllPanel, BorderLayout.EAST );

        controllPanel.setBorder( etchedBorder );
        controllPanel.add( leftControllPanel, BorderLayout.WEST );
        controllPanel.add( rightControllPanel, BorderLayout.CENTER );

        //left custom panel (bottom)
        JPanel crossTypeButtonPanel = new JPanel( new GridLayout( 2, 3, 1, 1 ) );
        crossTypeButtonPanel.add( cent_button );
        crossTypeButtonPanel.add( vert_button );
        crossTypeButtonPanel.add( horz_button );

        crossTypeButtonPanel.add( cent_label );
        crossTypeButtonPanel.add( vert_label );
        crossTypeButtonPanel.add( horz_label );

        JPanel crossTypeSliderPanel = new JPanel( new GridLayout( 1, 3, 1, 1 ) );
        crossTypeSliderPanel.add( cent_slider );
        crossTypeSliderPanel.add( vert_slider );
        crossTypeSliderPanel.add( horz_slider );

        JPanel crossTypePanel = new JPanel( new BorderLayout() );
        crossTypePanel.add( crossTypeButtonPanel, BorderLayout.NORTH );
        crossTypePanel.add( crossTypeSliderPanel, BorderLayout.CENTER );
        crossTypePanel.setBorder( etchedBorder );

        leftControllPanel.add( crossTypePanel, BorderLayout.CENTER );

        graphPanel.setBorder( etchedBorder );
        graphPanel.add( GP, BorderLayout.CENTER );

        //define curve properties
        thresh_CurveData.setColor( Color.blue );
        boundR_CurveData.setColor( Color.black );
        waveForm_CurveData.setColor( Color.red );
        section_CurveData.setColor( Color.red );

        thresh_CurveData.setLineWidth( 2 );
        section_CurveData.setLineWidth( 2 );
        waveForm_CurveData.setLineWidth( 2 );
        boundR_CurveData.setLineWidth( 1 );

        //define sliders (scroll bars' listener)
        cent_slider.setEnabled( true );
        vert_slider.setEnabled( false );
        horz_slider.setEnabled( false );

        cent_slider.setUnitIncrement( 1 );
        vert_slider.setUnitIncrement( 1 );
        horz_slider.setUnitIncrement( 1 );

        cent_slider.setBlockIncrement( 20 );
        vert_slider.setBlockIncrement( 20 );
        horz_slider.setBlockIncrement( 20 );

        ActionListener radioButtonListener =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    if ( cent_button.isSelected() ) {
                        cent_slider.setEnabled( true );
                    }
                    else {
                        cent_slider.setEnabled( false );
                    }

                    if ( vert_button.isSelected() ) {
                        vert_slider.setEnabled( true );
                    }
                    else {
                        vert_slider.setEnabled( false );
                    }

                    if ( horz_button.isSelected() ) {
                        horz_slider.setEnabled( true );
                    }
                    else {
                        horz_slider.setEnabled( false );
                    }

                    plotSectionGraph( getScrollBarTypeIndex() );
                }
            };

        cent_button.addActionListener( radioButtonListener );
        vert_button.addActionListener( radioButtonListener );
        horz_button.addActionListener( radioButtonListener );

        ChangeListener scrollBar_Listener =
            new ChangeListener() {
                public void stateChanged( ChangeEvent e ) {
                    plotSectionGraph( getScrollBarTypeIndex() );
                }
            };

        cent_slider.getModel().addChangeListener( scrollBar_Listener );
        vert_slider.getModel().addChangeListener( scrollBar_Listener );
        horz_slider.getModel().addChangeListener( scrollBar_Listener );

    }


    private void plotSectionGraph( int typeIndex ) {

        waveForm_CurveData.clear();

        if ( !isDataExist ) {
            return;
        }

        double z_max = emittance3Da.getMaxZ();

        double x_min = emittance3Da.getMinX();
        double x_max = emittance3Da.getMaxX();
        double xp_min = emittance3Da.getMinY();
        double xp_max = emittance3Da.getMaxY();

        double x_start = 0.;
        double xp_start = 0.;

        double x_stop = 0.;
        double xp_stop = 0.;

        double frac = 0.;

        int nPoints = 0;

        if ( typeIndex == 0 ) {
            nPoints = cent_slider.getMaximum();
            frac = (double) cent_slider.getValue();

            double[] arr = CrossingProducer.getRotatedLimits( frac, 0., 0., emittance3Da );
            x_start = arr[0];
            xp_start = arr[1];
            x_stop = arr[2];
            xp_stop = arr[3];

        }
        if ( typeIndex == 1 ) {
            nPoints = vert_slider.getMaximum();
            frac = ( (double) vert_slider.getValue() ) /
                ( vert_slider.getMaximum() - vert_slider.getVisibleAmount() );
            xp_start = xp_min;
            xp_stop = xp_max;
            x_start = x_max - ( x_max - x_min ) * frac;
            x_stop = x_start;
        }
        if ( typeIndex == 2 ) {
            nPoints = horz_slider.getMaximum();
            frac = ( (double) horz_slider.getValue() ) /
                ( horz_slider.getMaximum() - horz_slider.getVisibleAmount() );
            x_start = x_min;
            x_stop = x_max;
            xp_start = xp_min + ( xp_max - xp_min ) * frac;
            xp_stop = xp_start;
        }

        double x = 0.;
        double xp = 0.;
        double pos = 0.;
        double val = 0.;

        for ( int i = 0; i < nPoints; i++ ) {
            pos = ( (double) i ) / nPoints;
            x = x_start + ( x_stop - x_start ) * pos;
            xp = xp_start + ( xp_stop - xp_start ) * pos;
            pos = 2.0 * ( pos - 0.5 );
            val = 100.0 * emittance3Da.getValue( x, xp ) / z_max;
            waveForm_CurveData.addPoint( pos, val );
        }

        section_CurveData.clear();
        section_CurveData.addPoint( x_start, xp_start );
        section_CurveData.addPoint( x_stop, xp_stop );

        GP.refreshGraphJPanel();
        GP_ep.refreshGraphJPanel();
    }


    private int getScrollBarTypeIndex() {
        if ( cent_button.isSelected() ) {
            return 0;
        }
        if ( vert_button.isSelected() ) {
            return 1;
        }
        return 2;
    }


    /**  Performs actions before show the panel */
    void goingShowUp() {

        emittance3Da = (ColorSurfaceData) getParamsHashMap().get( "RawEmittanceData" );

        getTextMessage().setText( null );

        isDataExist = true;

        //make rectangular and threshold on the usual graph
        double z_min = emittance3Da.getMinZ();
        double z_max = emittance3Da.getMaxZ();

        if ( z_max <= 0. || z_min == z_max ) {
            isDataExist = false;
            getTextMessage().setText( "The data for analysis do not" +
                " exist" );
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        thresh_CurveData.clear();
        thresh_CurveData.addPoint( -1., threshold_Text.getValue() );
        thresh_CurveData.addPoint( 1., threshold_Text.getValue() );

        boundR_CurveData.clear();
        boundR_CurveData.addPoint( -1.0, 100. );
        boundR_CurveData.addPoint( 1.0, 100. );
        boundR_CurveData.addPoint( 1.0, 100.0 * z_min / z_max );
        boundR_CurveData.addPoint( -1.0, 100.0 * z_min / z_max );
        boundR_CurveData.addPoint( -1.0, 100. );

        GP.removeAllCurveData();
        GP.addCurveData( waveForm_CurveData );
        GP.addCurveData( thresh_CurveData );
        GP.addCurveData( boundR_CurveData );

        GP_ep.addCurveData( section_CurveData );

        plotSectionGraph( getScrollBarTypeIndex() );

    }


    /**  Performs actions before close the panel */
    void goingShowOff() {
        GP_ep.removeAllCurveData();
    }


    /**  Sets all analyzes in the initial state with removing all temporary data */
    void initialize() { }


    /**  Creates objects for the global HashMap using put method only */
    void createHashMapObjects() { }


    /**
     *  Connects to the objects in the global HashMap using only get method of
     *  the HashMap
     */
    void connectToHashMapObjects() {
        threshold_Text = (DoubleInputTextField) getParamsHashMap().get( "THRESHOLD_TEXT" );
        GP_ep = (FunctionGraphsJPanel) getParamsHashMap().get( "EMITTANCE_3D_PLOT" );
    }


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new font
     */
    void setFontForAll( Font fnt ) {

        cent_button.setFont( fnt );
        vert_button.setFont( fnt );
        horz_button.setFont( fnt );

        cent_label.setFont( fnt );
        vert_label.setFont( fnt );
        horz_label.setFont( fnt );

        cent_slider.setFont( fnt );
        vert_slider.setFont( fnt );
        horz_slider.setFont( fnt );

    }

}

