//
//  TrackerAdaptive.java
//  xal
//
//  Created by Thomas Pelaia on 1/27/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.model.alg;

import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.model.IProbe;
import xal.smf.AcceleratorSeq;


/** Common abstract superclass for the adaptive tracker algorithms 
 * 
 * @deprecated  functionality pushed down into child class class <code>{@link EnvTrackerAdapt}</code>
 */
@Deprecated
abstract public class TrackerAdaptive extends Tracker {
    // Archiving constants 
	
    /** data node label for EnvTrackerAdapt settings */
    public final static String      NODETAG_ADAPT   = "adapt";
    
    /** attribute label for order of accuracy in algorithm */
    public final static String      ATTRTAG_ORDER   = "order";
    
    /** attribute label for maximum step size */
    public final static String      ATTRTAG_MAXSTEP = "maxstep";
    /** attribute label for maximum step size for drift space with pmq , sako 21 jul 06 */
    public final static String      ATTRTAG_MAXSTEP_DRIFTPMQ = "maxstepdriftpmq";
    
    /** attribute label for initial step size */
    public final static String      ATTRTAG_INITSTEP= "initstep";
    
    /** attribute label for error tolerance */
    public final static String      ATTRTAG_ERRTOL  = "errortol"; 
    
    /** attribute label for slack tolerance */
    public final static String      ATTRTAG_SLACK   = "slack";
    
    /** attribute label for residual norm */
    public final static String      ATTRTAG_NORM    = "norm";
    
    /** attribute label for maximum iteration count */
    public final static String      ATTRTAG_MAXITER = "maxiter";
	
    /** 
	 * Maximum distance we may travel before requiring another space charge kick.
	 *  If zero than stepping distance is not bound. 
	 */
    protected double m_dblMaxStep;       
    /** m_dbleMaxStep for drift field from PMQ */
    protected double m_dblMaxStepDriftPmq;       
    
    /** The current step size. */
    protected double m_dblStepSize;
	
    /**
	 * Residual error tolerance parameter.  Errors in the the residual between
     * a full step and half step can be no larger than this.
     */
    protected double m_dblErrTol;
    
    /**
	 * Step size adjustment slack tolerance.  If adjustments in the step size
     * are less than this percentage, we take special action.
     */
    protected double m_dblSlack;
    
    /**
	 * The type of Lebesque norm used in the residual calculations.  Since the 
     * residual is a matrix in R7x7 this is a matrix norm.
     */
    protected int m_enmNorm;
	
    private double initStepSize;
    
	
    /**
     * @param strType
     * @param intVersion
     * @param clsProbeType
     */
    protected TrackerAdaptive(String strType, int intVersion, Class<? extends IProbe> clsProbeType) {
        super(strType, intVersion, clsProbeType);
    }

	/**
	 * Load the sequence's model parameters for the adaptive tracker from the edit context.
	 * @param locationID The location ID of the entrance parameters to use
	 * @param sequence The sequence for which to get the adaptive tracker parameters.
	 */
	public void initializeFromEditContext( final String locationID, final AcceleratorSeq sequence ) {
		final EditContext editContext = sequence.getAccelerator().editContext();
		final DataTable adaptiveTrackerTable = editContext.getTable( "adaptivetracker" );
		GenericRecord record = adaptiveTrackerTable.record( "name",  locationID );
		if ( record == null ) {
			record = adaptiveTrackerTable.record( "name", "default" );  // just use the default record
		}
		
		final double errorTolerance = record.doubleValueForKey( ATTRTAG_ERRTOL );
		final double initStep = record.doubleValueForKey( ATTRTAG_INITSTEP );
		initStepSize = initStep;
		final double maxStep = record.doubleValueForKey( ATTRTAG_MAXSTEP );
		final double maxStepDriftPmq = record.doubleValueForKey( ATTRTAG_MAXSTEP_DRIFTPMQ );
		final int norm = record.intValueForKey( ATTRTAG_NORM );
		final int order = record.intValueForKey( ATTRTAG_ORDER );
		final double slack = record.doubleValueForKey( ATTRTAG_SLACK );
		final int maxIter = record.intValueForKey( ATTRTAG_MAXITER );
		
		if ( supportsConditionalTermination() ) {
			setAccuracyOrder( order );
			setMaxIterations( maxIter );
		}
		setErrorTolerance( errorTolerance );
		setStepSize( initStep );
		setMaxStepSize( maxStep );
		setMaxStepSizeDriftPmq( maxStepDriftPmq );
		setMatrixNorm( norm );
		setSlackTolerance( slack );
	}
	
    
	/**
	 * Determine whether this algorithm supports the optional accuracy odrer and maximum iteration methods.
	 * @return true if the optional methods are supported and false if not.
	 */
	abstract public boolean supportsConditionalTermination();
	
    
    /**
	 * Set the integration accuracy order for the underlying
     * stepping algorithm.  For example, when using a second-order
     * accurate algorithm the residual error is of the order
     * <i>h</i>^2 where <i>h</i> is the step size.
     * 
     * @param intOrder      integration order
     */
    abstract public void setAccuracyOrder( int intOrder );
    
	
    /**
	 * Set the maximum allowable step size.  If this value is
     * set then the step sizing algorithm will never generate
     * steps larger than the provided value regardless of the 
     * residual error.  If this value is
     * cleared to zero, then the step size is unbound.
     * 
     * @param dblMaxStep    maximum allowable step size in <b>meters</b>
     */
    public void setMaxStepSize( final double dblMaxStep )   {
        m_dblMaxStep = dblMaxStep;
    }
    
    /**
     * Sets the integration step size to be used at the beginning of
     * the algorithm or after <code>{@link #initialize()}</code> is 
     * called.
     *
     * @param stepSize  step size to be used after algorithm initialization
     *
     * @author Christopher K. Allen
     * @since  Aug 20, 2012
     */
    public void setInitStepSize(double stepSize){
        initStepSize = stepSize;
    }
    
    /**
	 * Set the maximum allowable step size.  If this value is
     * set then the step sizing algorithm will never generate
     * steps larger than the provided value regardless of the 
     * residual error.  If this value is
     * cleared to zero, then the step size is unbound.
     * 
     * @param dblMaxStepDriftPmq    maximum allowable step size in <b>meters</b>
     */
    public void setMaxStepSizeDriftPmq( final double dblMaxStepDriftPmq )   {
        m_dblMaxStepDriftPmq = dblMaxStepDriftPmq;
    }
    
	
    /**
	 * Sets the maximum allowable number of steps to progate a probe through
     * an element.  If the number of steps increases beyond this number a
     * race condition is assumed and an exception is thrown during the 
     * propagation.
     * 
     * If the value is zero then no maximum step count is enforced.
     * 
     * @param   intMaxIter maximum allowable single-element step count 
     */
    abstract public void setMaxIterations( final int intMaxIter );
	
	
    /**
	 * Set the acceptable error in the residual correlation matrix.
     * The residual correlation matrix is determined by going a full
     * step, then two half step and taking the difference in the 
     * resulting correlation matrices.  
     * 
     * The step size is continually adjusted to keep the resulting
     * residual error at or about this given tolerance value.  Thus,
     * yeild a more accurate solution however they also result in small 
     * step sizes.
     *  
     * @param dblErr    acceptable residual error
     */
    public void setErrorTolerance( final double dblErr )   {
        m_dblErrTol = dblErr;
    }
    
	
    /**
	* Set the slack size in the adaptive step size determination.  That is,
     * if the suggested new step size differs only by this percentage, then
     * no action is taken.  Explicitly, if the new step size lies in the 
     * interval [h-dblSlack, h+dblSlack], then nothing is done.
     * 
     * This feature prevents excessive micro-management 
     * of the step size.
     * 
     * @param dblSlack  size of the slack region in <b>meters</b>  
     */
    public void setSlackTolerance( final double dblSlack )   {
        m_dblSlack = dblSlack;
    }
	
	
    /**
	 * Set the initial step size for applying space charge
     * corrections.  Note that the step size will be modified
     * as the algorithm progresses according to the adaptation
     * rule.
     * 
     * @param dblStepSize   initial step size in <b>meters</b>
     */
    public void setStepSize( final double dblStepSize ) {
        m_dblStepSize = dblStepSize;
    }
    
	
    /**
	 * Set the type of Lebesque norm used in the matrix calculations.  
     * 
     * @param   enmNorm     enumeration constant of family <code>EnvTrackerAdapt.NORM_*</code>
     */
    public void setMatrixNorm( final int enmNorm )    {
        if (enmNorm <0 || enmNorm > 2) 
            return;
		
        m_enmNorm = enmNorm;
    }
	
    
    /*
     * Accessing
     */
    
    /**
	 * Get the current accuracy order of the space charge stepping
     * algorithm. 
     *  
     * @return      order of the integration algorithm accuracy
     * 
     * @see #setAccuracyOrder 
     */
    abstract public int  getAccuracyOrder();
    
	
    /**
	 * Return the current step size.
     *   
     * @return  the current step size in <b>meters</b>
     */
    public double getStepSize() {
        return m_dblStepSize;
    }
    
	
    /**
	 * Return the maximum allowable step size.  The step sizing aglorithm
     * is bound by this value, or unbounded if zero.
     * 
     * @return  maximum allowable step size in <b>meters</b>
     */
    public double getMaxStepSize()  {
        return this.m_dblMaxStep;
    }
    
	
    /**
	 * Return the maximum allowable step size.  The step sizing aglorithm
     * is bound by this value, or unbounded if zero.
     * 
     * @return  maximum allowable step size in <b>meters</b>
     */
    public double getMaxStepSizeDriftPmq()  {
        return this.m_dblMaxStepDriftPmq;
    }
    
	
    /**
	 * Return the maximum allowable number of steps to progate a probe through
     * an element.  If the number of steps increases beyond this number a
     * race condition is assumed and an exception is thrown during the 
     * propagation.
     * 
     * If the value is zero then no maximum step count is enforced.
     * 
     * @return  maximum alllowable step count while propagating thru a single element
     */
    abstract public int  getMaxIterations();
	
	
    /**
	 * Return the acceptable tolerance in the residual error between
     * a full step and two half steps.
     *  
     * @return  tolerable residual error
     */
    public double getErrorTolerance()   {
        return m_dblErrTol;    
    }
	
	
    /**
	 * Step size adjustment slack tolerance.  If adjustments in the step size
     * are less than this percentage, we do nothing.
     * 
     * @return      size of the slack region in <b>meters</b>
     */
    public double getSlackTolerance()   {
        return m_dblSlack;
    }
	
	
    /**
	 * Get the type of Lebesque norm used for matrices.  
     * 
     * @return  0 for l-inf norm, 1 for l-1 norm, 2 for l2 norm
     */
    public int  getMatrixNorm()       {
        return m_enmNorm;
    }
    
	
    /**
     * Returns the integration step size used at the initialization of the
     * algorithm.
     *
     * @return  step size used after call to <code>{@link #initialize()}</code>
     *
     * @author Christopher K. Allen
     * @author Sako
     * @since  Aug 20, 2012
     * @since    21 jul 07
     */
    public double getInitStepSize()  {
        return this.initStepSize;
    }
    
    
    /*
     * Tracker Overrides
     */
    
    /**
     * Resets the state of the algorithm object.
     * 
     * @since Aug 20, 2012
     * @see xal.model.alg.Tracker#initialize()
     */
    @Override
    public void initialize() {
    	super.initialize();
//    	if (initStepSize > 0.)
    		setStepSize(initStepSize);
    }
}
