/**
 * ContinuationSolution.java
 *
 * @author Christopher K. Allen
 * @since  Apr 15, 2013
 */
package xal.extension.twissobserver;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.model.ModelException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Jama.Matrix;

/**
 * <p>
 * Computes the covariance matrix at a given location which is most likely to produce the provided
 * data.  That is, the covariance matrix is constructed at the given device location
 * from the data provided.  The method used is a continuation method where a curve of 
 * covariance matrices is constructed from a known solution value, the zero-current case, to the
 * solution value for the given beam charge.
 * </p>
 * <p>  
 * The iterates are computed using a continuation method as
 * described in the paper "Implementation of a Beam Envelope State Observer."  Specifically,
 * let <i>n</i> be the number of independent phase planes we are considering (here it is
 * 3).  Then assume a <em>smooth</em> solution curve 
 * <b>s</b>(&middot;) : <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> 
 * mapping bunch charge <i>q</i> to the solution <b>&sigma;</b> &in; <b>R</b><sup>3<i>n</i></sup> of 
 * independent beam moments at that charge.  
 * Moreover, we have <b>&sigma;</b>* = <b>s</b>(<i>q</i>*) where <i>q</i>* is the bunch charge
 * at the given profile data <b>X</b>.  
 * </p>
 * <p>
 * In the analysis we have constructed a known function 
 * <b>G</b> : <b>R</b><sup>3<i>n</i></sup> &times; <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> such that
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>G</b>[<b>s</b>(<i>q</i>),<i>q</i>] = <b>0</b>,
 * <br>
 * <br>
 * that is, <b>G</b> = 0 whenever <b>s</b> is the least-squares solution to the problem of reconstructing
 * the Courant-Snyder parameters for the given bunch charge <i>q</i> (and given data <b>X</b>).
 * The solution curve <b>s</b>(&middot;) is constructed using continuity starting from a known value 
 * <b>s</b>(0) = <b>&sigma;</b><sub>0</sub>, the zero-current solution.  Given that the value of <b>s</b>
 * is known at <i>q</i>, the value of <b>s</b> at a small distance &Delta;<i>q</i> from <i>q</i> is 
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>s</b>(<i>q</i>+&Delta;<i>q</i>) = <b>s</b>(<i>q</i>) 
 *                                                  + [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>
 *                                                  + <i>O</i>(&Delta;<i>q</i>&sup2;) .
 * <br>
 * <br>
 * Essentially this method recursively computes d<b>s</b>(<i>q</i>)/d<i>q</i> and updates <b>s</b>(<i>q</i>)
 * according to the above.
 * </p>
 * <p>
 * The value d<b>s</b>(<i>q</i>)/d<i>q</i> is computed by consideration of the known function 
 * <b>G</b>.  We take the full derivative of the equation <b>G</b> = <b>0</b> w.r.t. to <i>q</i>
 * which yields
 * <br>
 * <br>
 * &nbsp; &nbsp; d<b>s</b>(<i>q</i>)/d<i>q</i> = [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<b>s</b>]<sup>-1</sup>
 *                                               [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<i>q</i>] .
 * <br>
 * <br>
 * Once this value is computed the next value on the curve <b>s</b>(&middot) 
 * is the vector <b>s</b>(<i>q</i>+&Delta;<i>q</i>) = <b>s</b>(<i>q</i>) + [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>.  
 * The partial derivatives are computed numerically
 * about the given values of <b>s</b> = <var>matSig0</var> and <i>q</i> = <var>dblBnchChg</var> using
 * step lengths provided by the methods <code>{@link #setChargeDerivativeStepPercent(double)}</code>
 * and <code>{@link #setMomentDerivativeStepPercent(double)}</code>. 
 * </p>
 *
 * <h3>NOTES:</h3>
 * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
 *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
 * <br>
 * &middot; A <code>{@link TransferMatrixGenerator}</code> object must be supplied
 * for the construction of one of these objects.  This is done because of
 * the variety of options that exist when creating the transfer matrix
 * generator.  It is safer to require pre-construction of the matrix
 * generator rather than offer all the options for such generation here.
 * <br>
 * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
 *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
 * <br>
 * &middot; The derivatives are computed by take the percentage of the current
 * value of the independent variable.  This will not work well when that value
 * is near zero and the independent variable has a large domain.  Taking the 
 * percentage of the domain size would be a better policy if that is possible.
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  Apr 15, 2013
 *
 */
public class CsContinuationEstimator extends CourantSnyderEstimator {

    
    
    /*
     * Global Constants
     */

    //
    // Default Numerical Parameters
    //
    
    /** Whether or not to use an embedded fixed point search */
    public static final boolean BOL_FIX_PT_SRCH = true;
    
    /** Default number of space charge steps used in continuation */
    public static final int     CNT_CHRG_STEPS = 20;
    
    /** Default fractional perturbation of moment vector used to compute the partial of the recursion function */
    public static final double  DBL_DEL_MMT_FRAC = 0.01;
    
    /** Default fractional perturbation of the beam current used to compute the partial of the recursion function */
    public static final double  DBL_DEL_CURR_FRAC = 0.05;
    
    

    /*
     * Local Attributes
     */
    
    
    /** Whether to use secondary search (fixed point method) embedded in this method */
    private boolean             bol2ndSrch;

    /** The fixed point search algorithm embedded in this method */
    private CsFixedPtEstimator     slnEmbed;
    
    
    /** Number of beam current steps - method 2 */
    private int         cntCurSteps;
    
    
    /** Derivative fractional step (0,1) for computing partials w.r.t. moments - method 2 */
    private double      dblDelMmtPct;
    
    /** Derivative fractional step (0,1) for computing partials w.r.t. beam current - method 2*/
    private double      dblDelCurPct;
    
    

    /*
     * Initialization
     */
    
    
    /**
     * Creates a new instance of <code>ContinuationSolution</code>.
     *
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2013
     */
    public CsContinuationEstimator(TransferMatrixGenerator genTransMat) {
        this(BOL_FIX_PT_SRCH, CNT_CHRG_STEPS, DBL_DEL_MMT_FRAC, DBL_DEL_CURR_FRAC, genTransMat);
    }
    
    /**
     * Creates a new instance of <code>ContinuationSolution</code>.
     *
     * @param bol2ndSrch    use the fixed point secondary search between beam charge steps 
     * @param cntCurSteps   number of steps used to move (continuously) from zero charge to full charge
     * @param dblDelMmtFrac the fractional perturbation in moment vector used to compute the partial of the recursion function
     * @param dblDelCurFrac the fractional perturbation of the beam current used to compute the recursion function partial
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2013
     */
    public CsContinuationEstimator(boolean bol2ndSrch, int cntChgSteps, double dblDelMmtFrac, double dblDelCurFrac, TransferMatrixGenerator genTransMat) {
        super(false, genTransMat);
        
        this.bol2ndSrch = bol2ndSrch;
        this.slnEmbed   = new CsFixedPtEstimator(genTransMat);
        
        this.cntCurSteps  = cntChgSteps;
        this.dblDelCurPct = dblDelCurFrac;
        this.dblDelMmtPct = dblDelMmtFrac;
    }

    
    /**
     * Sets whether or not to use a second, internal search method within the continuation
     * solution method (i.e., method 2).  This internal search is simply and application of
     * method 1 to move the current beam charge value back onto the (continuous) solution curve.
     * 
     * @param bolScndSrch   use the secondary search if <code>true</code>, no internal search if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Apr 5, 2013
     */
    public void setUseSecondarySearch(boolean bolScndSrch) {
        this.bol2ndSrch = bolScndSrch;
    }
    
    /**
     * Directly sets the maximum number of iterations allowed for the secondary search algorithm.
     * 
     * @param cntMaxIters    maximum number of allowed secondary search iterations
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public void setSecondarySearchIterations(int cntMaxIters) {
        this.slnEmbed.setMaxIterations(cntMaxIters);
    }
    
    /**
     * <p>
     * Sets the number of steps <i>N<sub>q</sub></i> used to move from the zero current 
     * solution <b>&sigma;</b><sub>0</sub> to the finite current solution <b>&sigma;</b>* 
     * using the continuation method.  Letting <i>q</i>* denote the beam charge at solution
     * <b>&sigma;*</b>,
     * then the continuation method computes the solutions <b>&sigma;</b><sub><i>n</i></sub>
     * to the sub-problems with beam charge <i>n&Delta;q</i> 
     * for each <i>n</i> = 0, 1, ..., <i>N<sub>q</sub></i> 
     * where &Delta;<i>q</i> &equiv; <i>q*</i>/<i>N<sub>q</sub></i> .
     * </p>
     * 
     * @param cntCurSteps   number of steps used to approach the true beam charge solution from the 
     *                      zero current solution using the continuation method
     *
     * @author Christopher K. Allen
     * @since  Apr 2, 2013
     */
    public void setBeamChargeSteps(int cntChgSteps) {
        this.cntCurSteps = cntChgSteps;
    }
    
    /**
     * <p>
     * Sets the perturbation factor used to (numerically) compute the partial derivatives with
     * respect to the beam charge.  The value of beam current is increased this fractional amount
     * of the current value when computing 
     * numerical derivatives.  Specifically, if perturbation factor is denoted &epsilon;, then  
     * the beam charge <i>q</i> is perturbed by an amount &epsilon;<i>q</i>, that is, the perturbed
     * charged <i>q</i>' is given by
     * <br>
     * <br>
     * &nbsp; &nbsp;  <i>q</i>' = <i>q</i> + &epsilon;<i>q</i>
     * <br>
     * <br>
     * </p>
     *
     * @param dblDelCurPct  a value in (0,1) indicating the fraction of the current charge used as perturbation
     *
     * @author Christopher K. Allen
     * @since  Nov 28, 2012
     */
    public void setChargeDerivPerturb(double dblDelChgPct) {
        this.dblDelCurPct = dblDelChgPct;
    }
    
    /**
     * <p>
     * Sets the perturbation used to compute (numerically) the partial derivatives with
     * respect to the initial beam moments.  The value of each moment is increased 
     * this fractional amount of its current value when computing 
     * numerical derivatives.  Specifically, if this value is denoted &epsilon;
     * and &sigma;<sub><i>i</i></sub> is the <i>i<sup>th</sup></i> element of moment
     * vector <b>&sigma;</b>, then the perturbed moment vector <b>&sigma;</b>' is given
     * by 
     * <br>
     * <br>
     * &nbsp; &nbsp;  <b>&sigma;</b>' = <b>&sigma;</b> + &epsilon;&sigma;<sub><i>i</i></sub><b>e</b><sub><i>i</i></sub> ,
     * <br>
     * <br>
     * where <b>e</b><sub><i>i</i></sub> is the standard basis vector for moment &sigma;<i>i</i>. 
     * </p>
     *
     * @param dblDelMmtPct  a value in (0,1) is the fraction of the current moment value used as the derivative step
     *
     * @author Christopher K. Allen
     * @since  Nov 28, 2012
     */
    public void setMomentDerivPerturb(double dblDelMmtPct) {
        this.dblDelMmtPct = dblDelMmtPct;
    }
    
    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Computes the covariance matrix at the given location which is most likely to produce the given
     * data.  That is, the covariance matrix is constructed at the given device location
     * from the data provided.  The method used is a continuation method where a curve of 
     * covariance matrices is constructed from a known solution value, the zero-current case, to the
     * solution value for the given beam charge.
     * </p>
     * <p>  
     * The iterates are computed using a continuation method as
     * described in the paper "Implementation of a Beam Envelope State Observer."  Specifically,
     * let <i>n</i> be the number of independent phase planes we are considering (here it is
     * 3).  Then assume a <em>smooth</em> solution curve 
     * <b>s</b>(&middot;) : <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> 
     * mapping bunch charge <i>q</i> to the solution <b>&sigma;</b> &in; <b>R</b><sup>3<i>n</i></sup> of 
     * independent beam moments at that charge.  
     * Moreover, we have <b>&sigma;</b>* = <b>s</b>(<i>q</i>*) where <i>q</i>* is the bunch charge
     * at the given profile data <b>X</b>.  
     * </p>
     * <p>
     * In the analysis we have constructed a known function 
     * <b>G</b> : <b>R</b><sup>3<i>n</i></sup> &times; <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> such that
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>G</b>[<b>s</b>(<i>q</i>),<i>q</i>] = <b>0</b>,
     * <br>
     * <br>
     * that is, <b>G</b> = 0 whenever <b>s</b> is the least-squares solution to the problem of reconstructing
     * the Courant-Snyder parameters for the given bunch charge <i>q</i> (and given data <b>X</b>).
     * The solution curve <b>s</b>(&middot;) is constructed using continuity starting from a known value 
     * <b>s</b>(0) = <b>&sigma;</b><sub>0</sub>, the zero-current solution.  Given that the value of <b>s</b>
     * is known at <i>q</i>, the value of <b>s</b> at a small distance &Delta;<i>q</i> from <i>q</i> is 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>s</b>(<i>q</i>+&Delta;<i>q</i>) = <b>s</b>(<i>q</i>) 
     *                                                  + [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>
     *                                                  + <i>O</i>(&Delta;<i>q</i>&sup2;) .
     * <br>
     * <br>
     * Essentially this method recursively computes d<b>s</b>(<i>q</i>)/d<i>q</i> and updates <b>s</b>(<i>q</i>)
     * according to the above.
     * </p>
     * <p>
     * The value d<b>s</b>(<i>q</i>)/d<i>q</i> is computed by consideration of the known function 
     * <b>G</b>.  We take the full derivative of the equation <b>G</b> = <b>0</b> w.r.t. to <i>q</i>
     * which yields
     * <br>
     * <br>
     * &nbsp; &nbsp; d<b>s</b>(<i>q</i>)/d<i>q</i> = [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<b>s</b>]<sup>-1</sup>
     *                                               [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<i>q</i>] .
     * <br>
     * <br>
     * Once this value is computed the next value on the curve <b>s</b>(&middot) 
     * is the vector <b>s</b>(<i>q</i>+&Delta;<i>q</i>) = <b>s</b>(<i>q</i>) + [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>.  
     * The partial derivatives are computed numerically
     * about the given values of <b>s</b> = <var>matSig0</var> and <i>q</i> = <var>dblBnchChg</var> using
     * step lengths provided by the methods <code>{@link #setChargeDerivativeStepPercent(double)}</code>
     * and <code>{@link #setMomentDerivativeStepPercent(double)}</code>. 
     * </p>
     * 
     * @param strRecDevId   ID of the device where the reconstruction is to be performed
     * @param dblBnchFreq   bunch arrival frequency for the given data (in Hz)
     * @param dblBmCurr     beam current (in Amperes)
     * @param arrData       the profile measurement data used for the reconstruction
     *  
     * @return  block diagonal covariance matrix (uncoupled in the phase planes) containing the second-order
     *          moments of the beam at the reconstruction location
     *          
     * @throws ModelException       error occurred during the transfer matrix computations
     * @throws ConvergenceException this is for the internal call to {@link #computeReconstruction(String, double, CovarianceMatrix, ArrayList)}
     *
     * @author Christopher K. Allen
     * @since  Apr 2, 2013
     */
    public CovarianceMatrix computeReconstruction(String strRecDevId, double dblBnchFreq, double dblBmCurr, ArrayList<Measurement> arrData)
        throws ModelException
    {
        // "Convergence" does not make sense here, unless we run the secondary search
        super.dblConvErr = Double.NaN;
        
        // Compute the initial values
        double             dblDelI = dblBmCurr/this.cntCurSteps;
        CovarianceMatrix   matSig0 = this.computeZeroCurrReconFunction(strRecDevId, arrData);

        this.matCurrSigma = matSig0;
        this.matCurrF     = matSig0;
        
        // Initialize the iterative beam current stepping
        double              dblCurrI = 0.0;

        // Compute the solution curve step by step by incrementing the beam charge
        for (int n=1; n<=this.cntCurSteps; n++) {

            dblCurrI = n*dblDelI;
            
            // Compute the new covariance matrix from the current one
            CovarianceMatrix   matSig1 = this.iterateNext(matSig0, strRecDevId, dblBnchFreq, dblCurrI, dblDelI, arrData);

            // Move the current solution value back onto the solution curve
            if (this.bol2ndSrch) {
                try {
                    matSig1 = this.slnEmbed.computeReconstruction(strRecDevId, dblBnchFreq, dblCurrI, matSig1, arrData);

                    super.dblConvErr = this.slnEmbed.getReconConvergenceError();
                    
                } catch (ConvergenceException e) {
                    matSig1 = this.getReconstruction();

                }

                if (super.isDebuggingOn()) { 
                    System.out.println("  --Finished second stage search for continuation method---------------");
                    System.out.println("    iterations=" + this.slnEmbed.getSolnIterations() + 
                                       ", residual error=" + this.slnEmbed.getReconResidualError() + 
                                       ", convergence error=" + this.slnEmbed.getReconConvergenceError() +
                                       "\n"
                                       );
                    
                }
            }
            
            // Compute the residual error and save it
            //  If it is less than the maximum return the solution
            super.dblResErr  = super.computeResidualError(matSig1, strRecDevId, arrData);

            // Record the current solution
            super.matCurrSigma = matSig1;
            
            //  Print out debug info
            if (super.isDebuggingOn()) {
                System.out.println("----Continuation Method: Charge step# " + n +
                                   " charge=" + dblCurrI + 
                                   ", residual error=" + super.getReconResidualError() + 
                                   ", converge error=" + super.getReconConvergenceError()
                                   );
                System.out.print( matSig1.toStringMatrix(fmtMatrix, 12) );
                System.out.println("-------------------------------------------------\n");
            }

            // Reset the initial covariance matrix and do another iteration
            matSig0 = matSig1;
        }
        
        // We stepped through all the charge values.
        //  Report the error if in debug mode
        //  Then return the computed answer
        if (bolDebug)
            System.out.println("Used " + this.cntCurSteps + " charge steps with final residual error " + super.getReconResidualError() + ", and convergence error " + super.getReconConvergenceError());
        
        return matSig0;
    }
    
    
    /*
     * Internal Support
     */
    
    /**
     * <p>
     * Computes the next iterate 
     * <b>&sigma;</b><sub><i>i</i>+1</sub> = <b>s</b>(<i>q<sub>i</sub></i> + &Delta;<i>q<sub>i</sub></i>)
     * from the given moment matrix <b>&sigma;</b><sub>0</sub> = <b>s</b>(<i>q<sub>i</sub></i>),
     * given beam charge <i>q<sub>i</sub></i>, and given change in beam charge 
     * &Delta;<i>q<sub>i</sub></i>.
     * </p>
     * <p>  
     * The iterate is computed using a continuation method as
     * described in the paper "Implementation of a Beam Envelope State Observer."  Specifically,
     * let <i>n</i> be the number of independent phase planes we are considering (here it is
     * 3).  Then assume a <em>smooth</em> solution curve 
     * <b>s</b>(&middot;) : <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> 
     * mapping bunch charge <i>q</i> to the solution <b>&sigma;</b> &in; <b>R</b><sup>3<i>n</i></sup> of 
     * independent beam moments at that charge.  
     * Moreover, we have <b>&sigma;</b>* = <b>s</b>(<i>q</i>*) where <i>q</i>* is the bunch charge
     * for the given profile data <b>X</b>.  
     * </p>
     * <p>
     * In the analysis we have constructed a function 
     * <b>G</b> : <b>R</b><sup>3<i>n</i></sup> &times; <b>R</b> &rarr; <b>R</b><sup>3<i>n</i></sup> such that
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>G</b>[<b>s</b>(<i>q</i>),<i>q</i>] = <b>0</b>,
     * <br>
     * <br>
     * that is, <b>G</b> = 0 whenever <b>s</b> is the least-squares solution to the problem of reconstructing
     * the Courant-Snyder parameters for the given bunch charge <i>q</i> and given data <b>X</b>.
     * The solution curve <b>s</b>(&middot;) is constructed using continuity starting from a known value 
     * <b>s</b>(0) = <b>&sigma;</b><sub>0</sub>, the zero-current case.  Given that the value of <b>s</b>
     * is known at <i>q</i>, the value of <b>s</b> at a small distance &Delta;<i>q</i> from <i>q</i> is 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>s</b>(<i>q</i>+&Delta;<i>q</i>) = <b>s</b>(<i>q</i>) 
     *                                                  + [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>
     *                                                  + <i>O</i>(&Delta;<i>q</i>&sup2;) .
     * <br>
     * <br>
     * Essentially this method computes d<b>s</b>(<i>q</i>)/d<i>q</i>.
     * </p>
     * <p>
     * The value d<b>s</b>(<i>q</i>)/d<i>q</i> is computed by consideration of the known function 
     * <b>G</b>.  We take the full derivative of the equation <b>G</b> = 0 w.r.t. to <i>q</i>
     * which yields
     * <br>
     * <br>
     * &nbsp; &nbsp; d<b>s</b>(<i>q</i>)/d<i>q</i> = [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<b>s</b>]<sup>-1</sup>
     *                                               [&part;<b>G</b>(<b>s</b>,<i>q</i>)/&part;<i>q</i>] .
     * <br>
     * <br>
     * Once this value is computed the return value is the vector <b>s</b>(<i>q</i>) + 
     * [d<b>s</b>(<i>q</i>)/d<i>q</i>]&Delta;<i>q</i>.  The partial derivatives are computed numerically
     * about the given values of <b>s</b> = <var>matSig0</var> and <i>q</i> = <var>dblBnchChg</var> using
     * step lengths provided by the methods <code>{@link #setChargeDerivativeStepPercent(double)}</code>
     * and <code>{@link #setMomentDerivativeStepPercent(double)}</code>. 
     * </p>
     * 
     * @param matSig0       current solution iterate
     * @param strRecDevId   ID of device where reconstruction is located
     * @param dblBnchFreq   bunch arrival frequency (in Hz)
     * @param dblBnchChg    beam charge to use in current iterate
     * @param dblDelChg     increase in beam charge for returned iterate
     * @param arrData       the reconstruction problem data
     * 
     * @return              the next solution iterate &approx; <b>s</b>(<i>q</i>+&Delta;<i>q</i>)
     * 
     * @throws ModelException   Error in computing the partial derivatives using the online model
     *
     * @author Christopher K. Allen
     * @since  Apr 2, 2013
     */
    private CovarianceMatrix    iterateNext(
            CovarianceMatrix matSig0, 
            String strRecDevId, 
            double dblBnchFreq,
            double dblBnchChg, 
            double dblDelChg,
            ArrayList<Measurement> arrData
            ) 
        throws ModelException 
    {
        
        // Compute the partial derivatives of the solution curve w.r.t. the beam charge
        //  at the current beam charge and store them in a map.
        Map<PHASEPLANE, Matrix>   mapVecDSigdq = new HashMap<PHASEPLANE, Matrix>();
        
        for (PHASEPLANE plane : PHASEPLANE.values()) {
            int     cntDim    = plane.getCovariantBasisSize();
            Matrix  matId     = Matrix.identity(cntDim, cntDim);

            // Compute the moment function resolvent
            Matrix  matDFdSig = this.computePartialWrtMoments(plane, matSig0, strRecDevId, dblBnchFreq, dblBnchChg, arrData);
            Matrix  matDGdSig    = matDFdSig.minus(matId);
            Matrix  matDGdSigInv = matDGdSig.inverse();
            
            // Compute the partial of the solution curve w.r.t. charge and store
            Matrix  vecDFdq   = this.computePartialWrtCharge(plane, matSig0, strRecDevId, dblBnchFreq, dblBnchChg, arrData);
            Matrix  vecDSigDq = matDGdSigInv.times(vecDFdq);
            
            mapVecDSigdq.put(plane, vecDSigDq);
        }
        
        Matrix  vecDelSigHor = mapVecDSigdq.get(PHASEPLANE.HOR).times(dblDelChg);
        Matrix  vecDelSigVer = mapVecDSigdq.get(PHASEPLANE.VER).times(dblDelChg);
        Matrix  vecDelSigLng = mapVecDSigdq.get(PHASEPLANE.LNG).times(dblDelChg);
        
        CovarianceMatrix   matDelSig = PHASEPLANE.constructCovariance(vecDelSigHor, vecDelSigVer, vecDelSigLng);
        CovarianceMatrix   matSig1   = new CovarianceMatrix( matSig0.plus(matDelSig) );
        
        return matSig1;
    }
    
    /**
     * <p>
     * Computes the partial derivative of the recursion function <b>F</b>(<b>&sigma;</b>,<i>q</i>) for the
     * given phase plane and at the
     * given value <b>&sigma;</b><sub>0</sub> of the covariance matrix and the given beam charge
     * <i>q</i><sub>0</sub>.
     * </p>
     * <p>
     * The partial &part;<b>F</b>(<b>&sigma;</b>,<i>q</i>)/&part;<i>q</i> is computed numerically by perturbing the beam charge <i>q</i> by a small
     * percentage &epsilon;
     * then recomputing <b>F</b>(<b>&sigma;</b>,<i>q</i>) and taking differences. Specifically,
     * <br>
     * <br>
     * &nbsp; &nbsp; &part;<b>F</b>(<b>&sigma;</b>,<i>q</i>)/&part;<i>q</i> 
     *                   &approx; [ <b>F</b>(<b>&sigma;</b>,<i>q</i>+&epsilon;<i>q</i>)
     *                   -        <b>F</b>(<b>&sigma;</b>,<i>q</i>) ]
     *                   / &epsilon;q
     * <br>
     * <br>
     * where &epsilon; is the parameter provided by method {@link #setChargeDerivativeStepPercent(double)}.
     * </p>
     * 
     * @param   plane       phase plane we are using
     * @param   matSig0     covariance matrix we are computing partials about
     * @param   strDevId    the device at the beamline location
     * @param   dblBnchFreq arrival frequency of the beam bunches
     * @param   dblBmCurr   current beam current
     * @param   arrMsmts    the measurement data 
     *           
     * @throws ModelException   Failed to generate transfer matrices due to a simulation error 
     *
     */
    private Matrix  computePartialWrtCharge(PHASEPLANE plane, CovarianceMatrix matSig0, String strRecDevId, double dblBnchFreq, double dblChg, ArrayList<Measurement> arrMsmts) 
        throws ModelException 
    {
        
        // Compute the current moment vector
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matSig0);
        Matrix  vecMmtsInit = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);
        
        // Perturb the beam charge and recompute the moment vector
        double  dblChgPert  = (1.0 + this.dblDelCurPct)*dblChg;
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChgPert, matSig0);
        Matrix  vecMmtsPert = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);
        
        // Approximate the moment vector derivative by finite difference
        Matrix vecDelF = vecMmtsPert.minus(vecMmtsInit);
        Matrix vecDFdq = vecDelF.times(1.0/(this.dblDelCurPct*dblChg));
        
        return vecDFdq;
    }
    
    /**
     * <p>
     * Computes the partial derivative of the recursion function <b>F</b>(<b>&sigma;</b>,<i>q</i>) for the
     * given phase plane and at the
     * given value <b>&sigma;</b><sub>0</sub> of the covariance matrix and the given beam charge
     * <i>q</i><sub>0</sub>.
     * The partials are computed numerically by perturbing each moment (element of <b>&sigma;</b>)
     * and recomputing <b>F</b>(<b>&sigma;</b>,<i>q</i>) and taking differences.
     * </p>
     * <p>
     * We compute column vector &part;<b>F</b>/&part;&sigma;<sub><i>i</i></sub> for each 
     * independent variable &sigma;<sub><i>i</i></sub> &equiv; [<b>&sigma;</b>]<sub><i>i</i></sub>. 
     * of vector <b>&sigma;</b>.  The partials are computed numerically by perturbing 
     * &sigma;<sub><i>i</i></sub> by &epsilon;<b>e</b><sub><i>i</i></sub> at <b>&sigma;</b><sub>0</sub>
     * where <b>e</b><sub><i>i</i></sub> is the <i>i</i><sup>th</sup> covariance basis matrix 
     * and &epsilon; is a percentage of the current value of &sigma;<sub><i>i</i></sub>.  The
     * returned value &part;<b>F</b>/&part;<b>&sigma;</b> is the augmentation
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>F</b>/&part;<b>&sigma;</b> = ( 
     *                                                 &part;<b>F</b>/&part;&sigma;<sub>1</sub> |
     *                                                 &part;<b>F</b>/&part;&sigma;<sub>2</sub> |
     *                                                 &part;<b>F</b>/&part;&sigma;<sub>3</sub> 
     *                                                )
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; This method assumes the phase planes to be independent.  Specifically it does not
     * currently consider the partials of the moments in one phase plane with respect to the
     * variation of moments in a different phase plane.
     * </p>
     * 
     * @param plane         phase plane to compute partials
     * @param matSig0       initial beam state (i.e., second-order moments) at reconstruction location
     * @param strRecDevId   ID of device where Courant-Snyder parameters are reconstructed
     * @param dblBnchFreq   beam bunch arrival frequency (in Hz)
     * @param dblChg        beam bunch current<i>I</i><sub>0</sub> (in Amperes)
     * @param arrMsmts      the measured profile data
     * 
     * @return  the partial derivative <b>F</b>/&part;<b>&sigma;</b> of the recursion operator <b>F</b>  
     * 
     * @throws ModelException   Failed to generate transfer matrices due to a simulation error 
     *
     * @author Christopher K. Allen
     * @since  Apr 1, 2013
     */
    private Matrix  computePartialWrtMoments(PHASEPLANE plane, CovarianceMatrix matSig0, String strRecDevId, double dblBnchFreq, double dblChg, ArrayList<Measurement> arrMsmts)
        throws ModelException
    {
        
        // Extract the initial moments from the covariance matrix
        //  These values are the coordinates in the domain about which we are taking 
        //  the numerical partial derivative
        Matrix  vecSig0 = plane.extractCovarianceVector(matSig0);
        
        // Compute the current value of F(sig0,q) from the initial moments (and charge)
        //  These values are the point in the range of F that which sig0 maps to 
        this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matSig0);
        Matrix  vecMmtsInit = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);
        
        // Perturb each moment and recompute the result
        //  We compute column vector DF/dSig_i for each independent variable sig_i 
        //  of vector sig.  The partials are computed numerically by perturbing sig 
        //  by del*e_i at sig0 where e_i is the ith covariance basis matrix.
        ArrayList<Matrix>       arrVecDelF = new ArrayList<Matrix>();
        
        for (int i=0; i<plane.getCovariantBasisSize(); i++) {
            double  dblMmt0    = vecSig0.get(i, 0);
            double  dblMmtPert = this.dblDelMmtPct*dblMmt0;
            
            CovarianceMatrix matBasis  = plane.getCovarianceBasis(i);
            PhaseMatrix       matPert   = matBasis.times(dblMmtPert);
            CovarianceMatrix matDelSig = new CovarianceMatrix( matSig0.plus(matPert) );
            
            this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblChg, matDelSig);
            Matrix  vecMmtsPert = this.computeReconSubFunction(plane, strRecDevId, arrMsmts);
            Matrix  vecDelF     = vecMmtsPert.minus(vecMmtsInit);
            Matrix  vecDFdSig   = vecDelF.times(1.0/dblMmtPert);
            
            arrVecDelF.add(vecDFdSig);
        }
        
        // Build the partial derivative matrix
        int     iRowMaxF   = vecMmtsInit.getRowDimension() - 1;
        int     iRowMaxSig = vecSig0.getRowDimension() - 1;
        Matrix  matDFdSig = new Matrix(iRowMaxF + 1, iRowMaxSig + 1);
        
        for (int j=0; j<=iRowMaxSig; j++) {
            Matrix vecDelF = arrVecDelF.get(j);
            
            matDFdSig.setMatrix(0, iRowMaxF, j, j, vecDelF);
        }
        
        return matDFdSig;
    }
    
}
