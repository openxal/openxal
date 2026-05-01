package xal.app.emittanceanalysis.analysis;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.*;
import java.util.List;
import java.text.*;
import javax.swing.border.*;
import java.awt.event.*;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.*;
import xal.extension.solver.*;
import xal.extension.solver.hint.*;


/**
 *  This analysis displays a one-dimensional transverse profile of the beam and
 *  calculates Gaussian fitting
 *
 *@author     shishlo
 *@version    1.0
 */
class AnalysisProfileFit extends AnalysisBasic {

    //emittance data as ColorSurfaceData instance (for analysis only)
    private ColorSurfaceData emittance3Da = null;

    //threshold text field from common part of the left top corner panel
    private DoubleInputTextField threshold_Text = null;

    private boolean is_ready_rms = false;

    //bottom panel. It includes the graph panel (bottom left)
    //and the controll panel (bottom right)
    JPanel bottomPanel = null;
    JPanel graphPanel = new JPanel( new BorderLayout() );
    JPanel controllPanel = new JPanel( new BorderLayout() );

    private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

    private BasicGraphData gdFitted = new BasicGraphData();

    private BasicGraphData gdProfile = new BasicGraphData();

    //----------------------------------
    //GUI elements of the controll panel
    //----------------------------------

    //threshold scan parameters
    private JPanel fitResultsPanel =
        new JPanel( new BorderLayout() );

    private JLabel fitResults_Label =
        new JLabel( "=== GAUSSIAN FITTING RESULTS ===", JLabel.CENTER );

    private JLabel centerValue_Label = new JLabel( "Center [mm] ", JLabel.CENTER );
    private JLabel widthValue_Label = new JLabel( "Width [mm] ", JLabel.CENTER );

    private DoubleInputTextField centerValue_Text = new DoubleInputTextField( 14 );
    private DoubleInputTextField widthValue_Text = new DoubleInputTextField( 14 );

	private NumberFormat dbl_Format = new ScientificNumberFormat(5);

    private JButton plotAndFitButton =
        new JButton( "PLOT & FIT PROFILE" );

     //initialization listener - it is called by
    //others analyses when necessary
    private ActionListener init_listener_profile = null;       
              
    //boolean variable indicating that data are not empty
    private boolean isDataExist = false;

    //scorer for Gaussian profile approximation
    private ProfileGaussScorer profileGaussScorer = new ProfileGaussScorer();

    //solver for fitting
    private Solver solver;
	private Problem problem;


    /**
     *  Constructor for the AnalysisProfileFit object
     *
     *@param  crossParamMap         The HashMap with Parameters of the analyses
     *@param  analysisTypeIndex_In  The type index of the analysis
     */
    AnalysisProfileFit( int analysisTypeIndex_In, HashMap<String,Object> crossParamMap ) {
        super( analysisTypeIndex_In, crossParamMap );

        analysisDescriptionString =
            " PROFILE FITTING" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "This analysis plots the one-dimensional profile of the beam " +
            "and calculates Gaussian Fitting for it. The formula for " +
            "Gaussian distribution is" +
            System.getProperties().getProperty( "line.separator" ).toString() +
            "y=A*exp(-(x-xc)^2/(2*w^2)).";

        //graph panel properties
        GP.setLegendVisible( true );
        GP.setLegendButtonVisible( true );
        GP.setOffScreenImageDrawing( true );
        GP.setLegendKeyString( "Legend" );
        GP.setGraphBackGroundColor( Color.white );
        GP.removeAllGraphData();

        //buttons look and feel
        plotAndFitButton.setForeground( Color.blue.darker() );
        //plotAndFitButton.setBackground( Color.cyan );

        centerValue_Text.setNumberFormat( dbl_Format );
        centerValue_Text.setHorizontalAlignment( JTextField.CENTER );
        centerValue_Text.setEditable( false );
        centerValue_Text.setText( null );
        centerValue_Text.setBackground( Color.white );

        widthValue_Text.setNumberFormat( dbl_Format );
        widthValue_Text.setHorizontalAlignment( JTextField.CENTER );
        widthValue_Text.setEditable( false );
        widthValue_Text.setText( null );
        widthValue_Text.setBackground( Color.white );

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

        JPanel plotAndFitButtonPanel =
            new JPanel( new FlowLayout( FlowLayout.CENTER, 3, 3 ) );
        plotAndFitButtonPanel.add( plotAndFitButton );

        JPanel resultsPanel =
            new JPanel( new BorderLayout() );

        JPanel resAndButtonPanel = new JPanel( new BorderLayout() );
        resAndButtonPanel.add( resultsPanel, BorderLayout.CENTER );
        resAndButtonPanel.add( plotAndFitButtonPanel, BorderLayout.SOUTH );

        controllPanel.add( fitResultsPanel, BorderLayout.CENTER );
        controllPanel.add( resAndButtonPanel, BorderLayout.SOUTH );

        //threshold scan control parameter panel
        JPanel plotAndFitResPanel_0 = new JPanel( new GridLayout( 2, 3, 1, 1 ) );
        plotAndFitResPanel_0.setBorder( etchedBorder );
        plotAndFitResPanel_0.add( centerValue_Label );
        plotAndFitResPanel_0.add( widthValue_Label );
        plotAndFitResPanel_0.add( centerValue_Text );
        plotAndFitResPanel_0.add( widthValue_Text );

        JPanel plotAndFitResPanel_1 = new JPanel();
        plotAndFitResPanel_1.setLayout(
            new FlowLayout( FlowLayout.CENTER, 1, 1 ) );

        JPanel plotAndFitResPanel_2 = new JPanel( new BorderLayout() );
        plotAndFitResPanel_2.setBorder( etchedBorder );
        plotAndFitResPanel_2.add( fitResults_Label, BorderLayout.NORTH );
        plotAndFitResPanel_2.add( plotAndFitResPanel_0, BorderLayout.CENTER );
        plotAndFitResPanel_2.add( plotAndFitResPanel_1, BorderLayout.SOUTH );

        fitResultsPanel.add( plotAndFitResPanel_2, BorderLayout.NORTH );

        gdFitted.setGraphColor( Color.red );
        gdFitted.setDrawPointsOn( false );
        gdFitted.removeAllPoints();
        gdFitted.setGraphProperty( GP.getLegendKeyString(),
            "fitting " );
        gdFitted.setLineThick( 2 );
        gdFitted.setImmediateContainerUpdate( false );

        gdProfile.setGraphColor( Color.black );
        gdProfile.setDrawPointsOn( true );
        gdProfile.removeAllPoints();
        gdProfile.setGraphProperty( GP.getLegendKeyString(),
            "profile " );
        gdProfile.setLineThick( 2 );
        gdProfile.setImmediateContainerUpdate( false );

        //default index of graph is 0
        GP.addGraphData( gdProfile );
        GP.addGraphData( gdFitted );

        GP.setAxisNameX( "x, mm" );
        GP.setAxisNameY( "profile" );
        GP.setName( "The profile of the beam" );

        //graph calculations and fitting button action
        plotAndFitButton.addActionListener(
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {

                    getTextMessage().setText( null );

                    centerValue_Text.setText( null );
                    centerValue_Text.setBackground( Color.white );

                    widthValue_Text.setText( null );
                    widthValue_Text.setBackground( Color.white );

                    //clear all old information
                    gdProfile.removeAllPoints();
                    gdFitted.removeAllPoints();

                    GP.clearZoomStack();

                    //plotting the graphs
                    calculateAndFitGraphs();

                    //refresh graph panel
                    GP.refreshGraphJPanel();

                    centerValue_Text.setValue( profileGaussScorer.getCenterValue() );
                    widthValue_Text.setValue( profileGaussScorer.getWidthValue() );

                }
            } );
        
        
         //initialization listener - it is called by
        //others analyses when necessary
        init_listener_profile =
            new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    initialize();
                }
            };       
        

        //solver problem definition
		problem = makeProblem( profileGaussScorer );
    }


	/**
	 * Generate a new problem.
	 * @param scorer the Profile Gauss Scorer from which to configure the problem and for which to set the variables
	 * @return the new problem
	 */
	private Problem makeProblem( final ProfileGaussScorer scorer ) {
		final Problem problem = ProblemFactory.getInverseSquareMinimizerProblem( new ArrayList<>(), scorer, 0.1 );

		final InitialDelta initialDeltaHint = new InitialDelta();
		problem.addHint( initialDeltaHint );

		final Variable cntrVariable = new Variable( "cntr", scorer.cntr, -Double.MAX_VALUE, Double.MAX_VALUE );
		problem.addVariable( cntrVariable );
		initialDeltaHint.addInitialDelta( cntrVariable, scorer.cntrStep );

		final Variable widthVariable = new Variable( "width", scorer.width, 0.0, Double.MAX_VALUE );
		problem.addVariable( widthVariable );
		initialDeltaHint.addInitialDelta( widthVariable, scorer.width );

		final Variable ampVariable = new Variable( "amp", scorer.amp, 0.0, Double.MAX_VALUE );
		problem.addVariable( ampVariable );
		initialDeltaHint.addInitialDelta( ampVariable, scorer.amp );

		// set the variables on the scorer
		scorer.setVariables( cntrVariable, widthVariable, ampVariable );

		return problem;
	}


    /**  Performs actions before show the panel */
    void goingShowUp() {

        emittance3Da = (ColorSurfaceData) getParamsHashMap().get( "RawEmittanceData" );
        is_ready_rms = ( (Boolean) getParamsHashMap().get( "IS_READY_RMS" ) ).booleanValue();

        isDataExist = true;

        double z_min = emittance3Da.getMinZ();
        double z_max = emittance3Da.getMaxZ();

        if ( z_max <= 0. || z_min == z_max ) {
            isDataExist = false;
        }

        if ( !isDataExist ) {
            getTextMessage().setText( null );
            getTextMessage().setText( "The data for analysis" + " do not exist." );
            Toolkit.getDefaultToolkit().beep();

            plotAndFitButton.setEnabled( false );

            centerValue_Text.setText( null );
            centerValue_Text.setBackground( Color.white );

            widthValue_Text.setText( null );
            widthValue_Text.setBackground( Color.white );

            //clear all old information
            gdProfile.removeAllPoints();
            gdFitted.removeAllPoints();

            GP.clearZoomStack();

        }
        else {

            if ( is_ready_rms ) {
                plotAndFitButton.setEnabled( true );
            }
            else {
                getTextMessage().setText( null );
                getTextMessage().setText( "You have to set threshold in the " +
                    "Threshold Finding analysis first." );
                Toolkit.getDefaultToolkit().beep();

                centerValue_Text.setText( null );
                centerValue_Text.setBackground( Color.white );

                widthValue_Text.setText( null );
                widthValue_Text.setBackground( Color.white );

                //clear all old information
                gdProfile.removeAllPoints();
                gdFitted.removeAllPoints();

                GP.clearZoomStack();

                plotAndFitButton.setEnabled( false );
            }
        }

    }


    /**  Performs actions before close the panel */
    void goingShowOff() {
    }


    /**  Sets all analyzes in the initial state with removing all temporary data */
    void initialize() {

        isDataExist = false;

        //clear all old information
        gdProfile.removeAllPoints();
        gdFitted.removeAllPoints();

        centerValue_Text.setText( null );
        centerValue_Text.setBackground( Color.white );

        widthValue_Text.setText( null );
        widthValue_Text.setBackground( Color.white );

        GP.clearZoomStack();

    }


    /**  Creates objects for the global HashMap using put method only */
    void createHashMapObjects() {
		getParamsHashMap().put( "INIT_LISTENER_PROFILE", init_listener_profile );
    }


    /**
     *  Connects to the objects in the global HashMap using only get method of
     *  the HashMap
     */
    void connectToHashMapObjects() {
        threshold_Text = (DoubleInputTextField) getParamsHashMap().get( "THRESHOLD_TEXT" );
    }


    /**
     *  Sets all fonts.
     *
     *@param  fnt  The new font
     */
    void setFontForAll( Font fnt ) {

        fitResults_Label.setFont( fnt );
        centerValue_Label.setFont( fnt );
        widthValue_Label.setFont( fnt );

        plotAndFitButton.setFont( fnt );

        centerValue_Text.setFont( fnt );
        widthValue_Text.setFont( fnt );

    }


    /**  Calculates all graphs data and fits the Gaussian parameters */
    private void calculateAndFitGraphs() {

        profileGaussScorer.init(
            emittance3Da,
            threshold_Text,
            gdProfile,
            gdFitted );

        //fitting
		solver = new Solver( SolveStopperFactory.minMaxTimeSatisfactionStopper( 0.5, 2.0, 0.99 ) );
        solver.solve( problem );
        
        //printing the fitting results
        System.out.println( "===RESULTS of PROFILE FITTING=====" );
        final ScoreBoard scoreBoard = solver.getScoreBoard();
        System.out.println( scoreBoard.toString() );

		final Trial bestSolution = scoreBoard.getBestSolution();
		profileGaussScorer.applyTrialPoint( bestSolution.getTrialPoint() );

        profileGaussScorer.makeFittedGraph();

    }
}

/**
 *  This is an implementation of the Scorer interface for our fitting
 *
 *@author     shishlo
 *@version    1.0
 */
class ProfileGaussScorer implements Scorer {


    //emittance data as ColorSurfaceData instance (for analysis only)
    private ColorSurfaceData emittance3Da = null;

    //threshold text field from common part of the left top corner panel
    private DoubleInputTextField threshold_Text = null;

    //graph data
    private BasicGraphData gdFitted = null;
    private BasicGraphData gdProfile = null;

	// starting values, step sizes and value references
	double cntr = 0.0;
	double cntrStep = 1.0;
	private Variable cntrVariable;
	double width = 10.0;
	double widthStep = 1.0;
	private Variable widthVariable;
	double amp = 1.0;
	double ampStep = 0.01;
	private Variable ampVariable;

    //temporary array for measured profile
    private double[] x_arr = new double[0];
    private double[] y_arr = new double[0];

    //temporary array for fitted profile
    private double[] y_fit_arr = new double[0];



    /**  Constructor for the ProfileGaussScorer object */
    ProfileGaussScorer() {
    }


	/** Set the value references for the variables */
	public void setVariables( final Variable cntrVariable, final Variable widthVariable, final Variable ampVariable ) {
		this.cntrVariable = cntrVariable;
		this.widthVariable = widthVariable;
		this.ampVariable = ampVariable;
	}



	/** apply the trial point values */
	void applyTrialPoint( final TrialPoint trialPoint ) {
		this.cntr = trialPoint.getValue( cntrVariable );
		this.width = trialPoint.getValue( widthVariable );
		this.amp = trialPoint.getValue( ampVariable );
	}


	void setValues( final double cntr, final double width, final double amp ) {
		this.cntr = cntr;
		this.width = width;
		this.amp = amp;
	}


    /**
     *  Returns the center of the Gaussian distribution
     *
     *@return    The center position
     */
    public double getCenterValue() {
        return cntr;
    }


    /**
     *  Returns the width of the Gaussian distribution
     *
     *@return    The width value
     */
    double getWidthValue() {
        return width;
    }


    /**  Fills out fitted graph data */
    void makeFittedGraph() {
        makeFittedArray();
        gdFitted.addPoint( x_arr, y_fit_arr );
    }


    /**  Makes inner array with fitted data */
    public void makeFittedArray() {
        double x = 0.;
        for ( int i = 0, n = x_arr.length; i < n; i++ ) {
            x = x_arr[i] - this.cntr;
            x = x * x;
            x /= 2. * this.width * this.width;
            y_fit_arr[i] = this.amp * Math.exp( -x );
        }
    }


    /**
     *  This method initializes all data for fitting. It should be called before
     *  optimization starts
     *
     *@param  emittance3Da    The experimental phase space density
     *@param  threshold_Text  The threshold text field
     *@param  gdProfile       The profile data
     *@param  gdFitted        The fitted profile graph data
     */
    void init(
        ColorSurfaceData emittance3Da,
        DoubleInputTextField threshold_Text,
        BasicGraphData gdProfile,
        BasicGraphData gdFitted ) {

        this.emittance3Da = emittance3Da;
        this.threshold_Text = threshold_Text;
        this.gdFitted = gdFitted;
        this.gdProfile = gdProfile;

        int nX = emittance3Da.getSizeX();
        int nXP = emittance3Da.getSizeY();

        if ( x_arr.length != nX ) {
            x_arr = new double[nX];
            y_arr = new double[nX];
        }

        double threshold = threshold_Text.getValue();
        double z_gr = threshold * 0.01 * emittance3Da.getMaxZ();

        double y_max = -Double.MAX_VALUE;
        double val = 0.;

        for ( int i = 0; i < nX; i++ ) {
            x_arr[i] = emittance3Da.getX( i );
            y_arr[i] = 0.;
            for ( int j = 0; j < nXP; j++ ) {
                val = emittance3Da.getValue( i, j );
                if ( val > z_gr ) {
                    y_arr[i] += val;
                }
            }
            if ( y_arr[i] > y_max ) {
                y_max = y_arr[i];
            }
        }

        if ( y_max <= 0. ) {
            y_max = 0.;
        }

        for ( int i = 0; i < nX; i++ ) {
            y_arr[i] /= y_max * 0.999;
        }

        gdProfile.addPoint( x_arr, y_arr );

        //set the initial parameters for fitting
        double x_cnt = 0.;
        double y_sum = 0.;
        for ( int i = 0; i < nX; i++ ) {
            y_sum += y_arr[i];
            x_cnt += y_arr[i] * x_arr[i];
        }

        if ( y_sum > 0. ) {
            x_cnt /= y_sum;
        }
        else {
            x_cnt = 0.;
        }

        double x_rms = 0.;
        for ( int i = 0; i < nX; i++ ) {
            x_rms += y_arr[i] * ( x_arr[i] - x_cnt ) * ( x_arr[i] - x_cnt );
        }

        if ( y_sum > 0. ) {
            x_rms /= y_sum;
        }
        else {
            x_rms = 0.;
        }

        if ( x_rms <= 0. ) {
            x_rms = 1.0;
        }
        else {
            x_rms = Math.sqrt( x_rms );
        }

		this.cntr = x_cnt;
		this.width = x_rms;
		this.amp = 1.0;

        cntrStep = Math.abs( x_cnt ) * 0.05;
        widthStep = Math.abs( x_rms ) * 0.05;
        ampStep = 0.01;

        if ( y_fit_arr.length != x_arr.length ) {
            y_fit_arr = new double[x_arr.length];
        }

        makeFittedGraph();
    }


    /**
     *  The score method implementation of the Scorer interface
     *
     *@return    The score
     */
    public double score( final Trial trial, final List<Variable> variables ) {
		applyTrialPoint( trial.getTrialPoint() );

        double sum2 = 0.;
        double diff = 0.;
        makeFittedArray();
        for ( int i = 0, n = x_arr.length; i < n; i++ ) {
            diff = y_arr[i] - y_fit_arr[i];
            sum2 += diff * diff;
        }

		if ( Double.isNaN( sum2 ) ) {
			sum2 = Double.POSITIVE_INFINITY;
		}

		//System.out.println( "sum2: " + sum2 );

        return sum2;
    }
}

