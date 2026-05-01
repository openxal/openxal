/**
 * TwissObserverBase.java
 *
 * @author Christopher K. Allen
 * @since  Apr 12, 2013
 */
package xal.extension.twissobserver;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.Twiss;
import xal.model.ModelException;

import java.text.DecimalFormat;
import java.util.ArrayList;

import Jama.Matrix;

/**
 * <p>
 * Base class for the problem of reconstructing Courant-Snyder parameters from beam profile
 * data at multiple locations along a beamline.  This class provide common tools and attributes
 * that child classes can share, as well as the ability to compute the zero-current solution.
 * </p>
 * <p>
 * Subclasses compute the covariance matrix <b>&sigma;</b> from the given data at the given device
 * location.  This is the set of second-order moments of the beam distribution.  Since the
 * measurement data cannot show coupling between phase planes, the covariance matrix is
 * block diagonal where the 2&times;2 blocks correspond to each independent phase plane
 * (see <code>{@link CovarianceMatrix}</code>).  If the measurement data is zero
 * for any phase plane, then the diagonal block corresponding to that phase plane is also zero.
 * </p>
 * <p>
 * The second-order moments are computed from and <i>observation matrix</i> which, in the zero
 * current case, is independent of the initial state of the beam.  The observation matrix
 * relates the initial state (described by the second moments) to the observed quantities, in this
 * case the measurement data of RMS beam sizes.  For each phase plane: In the case were there are 
 * more data points than there are variables (i.e., greater than 3) 
 * then the returned solution is the least-squares solution; specifically, the set 
 * of moments that hits the most data.  In the case
 * where there are less data points than variables (i.e., less than 3) then the returned solution is
 * the minimum norm (or "least energy") solution; specifically, the solution that has the smallest
 * Lebesgue 2-norm.  If we have 3 data points for the phase plane then the unique solution is returned.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Apr 12, 2013
 *
 */
public abstract class CourantSnyderEstimator {

    
    /*
     * Subclass Requirements
     */
    
    /**
     * <p>
     * Computes the covariance matrix at the given location which is most likely to produce the given
     * data.  That is, the covariance matrix is constructed at the given device location
     * from the data provided and for the given beam current.  
     * </p>
     * 
     * @param strRecDevId   ID of the device where the reconstruction is to be performed
     * @param dblBnchFreq   bunch arrival frequency (in Hz)
     * @param dblBmCurr     beam current (in Amperes)
     * @param arrData       the profile measurement data used for the reconstruction
     *  
     * @return  block diagonal covariance matrix containing the second-order
     *          moments of the beam at the reconstruction location
     *          
     * @throws Exception   general error occurred during the reconstruction computations
     * 
     * @author Christopher K. Allen
     * @since  Apr 2, 2013
     */
    public abstract CovarianceMatrix computeReconstruction(String strRecDevId, double dblBnchFreq, double dblBmCurr, ArrayList<Measurement> arrData) throws Exception;
    
    
    
    /*
     * Global Constants
     */

    

    /*
     * Local Attributes
     */
    
    
    //
    // Debugging Tools
    //
    
    /** Toggle debugging output on/off - default is off */
    protected boolean       bolDebug;
    
    /** The numeric format to use when printing out matrices to standard output */
    protected DecimalFormat fmtMatrix;

    
    
    //
    // Algorithm Tools
    //
    
    /** Transfer matrix generation engine */
    protected final TransferMatrixGenerator genTransMat;


    //
    // Solution Characteristics
    //
    
    /** The last recorded solution error */
    protected double            dblResErr;
    
    /** The last Convergence solution error */
    protected double            dblConvErr;
    
    /** The last recorded moment solution */
    protected CovarianceMatrix matCurrSigma;
    
    /** The last recorded recursion function value */
    protected CovarianceMatrix matCurrF;

    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>TwissObserverBase</code> object which uses the given transfer matrix
     * generator.  
     * 
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     *
     * @author  Christopher K. Allen
     * @since   Sep 4, 2012
     */
    public CourantSnyderEstimator(TransferMatrixGenerator genTransMatrix) {
        this(false, genTransMatrix);
    }
    
    /**
     * <p>
     * Creates a new <code>TwissObserverBase</code> object using the given maximum iteration count,
     * maximum convergence error, and the given transfer matrix generator.  This constructor must
     * be used if Twiss parameters are to be computed in the presence of space charge 
     * (see <code>{@link #computeCovarianceFiniteCurrent(String, double, ArrayList)}</code>).
     * </p>
     *
     * @param cntMaxIter    maximum number of allowed search iterations 
     * @param dblMaxError   maximum <i>L</i><sub>2</sub> convergence error in the solution
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     * 
     * @author  Christopher K. Allen
     * @author  Eric Dai
     * @since   Jul 20, 2012
     */
    public CourantSnyderEstimator(boolean bolDebug, TransferMatrixGenerator genTransMat) {
        this.genTransMat = genTransMat;
        this.bolDebug    = bolDebug;
        
        this.dblResErr   = Double.MAX_VALUE;
        this.dblConvErr  = Double.MAX_VALUE;

        this.fmtMatrix = new DecimalFormat("0.##E0#");
        this.fmtMatrix.getDecimalFormatSymbols().setNaN("NaN");
    }
    
    /**
     * Turn debugging output on or off.  The default case is off.
     *
     * @param bolDebug  <code>true</code> to enable debugging output or <code>false</code> to turn it off
     *
     * @author Christopher K. Allen
     * @since  Nov 16, 2012
     */
    public void setDebug(boolean bolDebug) {
        this.bolDebug = bolDebug;
    }

    
    /*
     * Queries
     */
    
    /**
     * Returns the value of the debugging flag.  
     * 
     * @return  <code>true</code> if the class is in the debugging state,
     *          <code>false</code> no debugging information is being generated
     *
     * @author Christopher K. Allen
     * @since  Apr 16, 2013
     */
    public boolean  isDebuggingOn() {
        return this.bolDebug;
    }
    
    /**
     * <p>
     * Returns the current solution covariance matrix.  This matrix is useful if an iterative
     * solution algorithm was used (e.g., for the finite space charge) and it did not converge.
     * The returned value is then the last value of the solution before the algorithm was 
     * terminated.
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * &middot; It is the responsibility of the subclass to keep this value up to date.
     * </p>
     *
     * @return  covariance matrix being the last computed solution value in an iterative algorithm
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2012
     */
    public CovarianceMatrix getReconstruction() {
        return this.matCurrSigma;
    }

    /**
     * <p>
     * Returns the residual error of the last solution, i.e., the solution returned by
     * <code>{@link #getReconstruction()}</code>.  The numerical value is the Frobenius norm
     * of the solution residual matrix.
     * <h3>NOTE:</h3>
     * <p>
     * &middot; It is the responsibility of the subclass to keep this value up to date.
     * </p>
     *
     * @return      ||<b>&Omega;&sigma;</b><sup>*</sup> - <b>X</b>||<sub><i>F</i></sub> 
     *              &nbsp where <b>&sigma;</b><sup>*</sup> is the solution 
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2012
     */
    public double getReconResidualError() {
        return  this.dblResErr;
    }

    /**
     * <p>
     * Returns the convergence error for the last solution iteration, i.e., the distance between 
     * the solution returned by <code>{@link #getReconstruction()}</code> and the solution previous.  
     * The numerical value is the Frobenius norm
     * of the distance between iterates.
     * <h3>NOTE:</h3>
     * <p>
     * &middot; It is the responsibility of the subclass to keep this value up to date.
     * </p>
     *
     * @return      || <b>&sigma;</b><sub><i>i</i>+1</sub> - <b>&sigma;</b><sub><i>i</i></sub> ||<sub><i>F</i></sub> 
     *              &nbsp where <b>&sigma;</b><sup>*</sup> is the solution 
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2012
     */
    public double getReconConvergenceError() {
        return  this.dblConvErr;
    }
    

    /*
     * Operations
     */
    
    /**
     * Returns the recursion function for a zero-current beam at the given device location and
     * for the given profile measurement data.  This method actually returns the results of 
     * <code>{@link #computeReconFunction(CovarianceMatrix, double, String, ArrayList)}</code>
     * for zero current and undefined initial beam moments.
     * 
     * @param strRecDevId   device ID where the Courant-Snyder parameters are being reconstructed
     * @param arrData       profile measurement data along beamline
     *  
     * @return              covariance matrix containing the value of the recursion function generated from
     *                      <var>matSig0</var> and <var>dblChrg</var>
     *                       
     * @throws ModelException   a general error occurred while computing the transfer matricies
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public CovarianceMatrix computeZeroCurrReconFunction(String strRecDevId, ArrayList<Measurement> arrData)
        throws ModelException
    {
        return this.computeReconFunction(null, 0.0, 0.0, strRecDevId, arrData);
    }
    
    /**
     * Returns the recursion function for a finite current beam with the given beam charge for the
     * given beam state at the sequence start.  This method actually returns the results 
     * of <code>{@link #computeReconSubFunction(PHASEPLANE, String, ArrayList)}</code>
     * simultaneously for all three phase planes as a covariance matrix.  In this method, the
     * transfer matrices are explicitly computed for the given initial covariance matrix and the
     * given beam current.  
     * The returned matrix is block diagonal
     * (and symmetric) containing the moments in the usual locations.
     * 
     * @param matSig0       beam covariance matrix at the accelerator sequence start      
     * @param dblChrg       beam charge
     * @param strRecDevId   device ID where the Courant-Snyder parameters are being reconstructed
     * @param arrData       profile measurement data along beamline
     *  
     * @return              covariance matrix containing the value of the recursion function generated from
     *                      <var>matSig0</var> and <var>dblChrg</var>
     *                       
     * @throws ModelException   a general error occurred while computing the transfer matricies
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public CovarianceMatrix computeReconFunction(CovarianceMatrix matSig0, 
                                                      double dblBnchFreq, 
                                                      double dblBeamCurr,
                                                      String strRecDevId,
                                                      ArrayList<Measurement> arrData
                                                      ) 
        throws ModelException
    {
        if (dblBeamCurr == 0.0)
            this.genTransMat.generateWithoutSpaceCharge();
        else
            this.genTransMat.generateWithSpaceCharge(dblBnchFreq, dblBeamCurr, matSig0);
        
        Matrix  vecMmtsHor = this.computeReconSubFunction(PHASEPLANE.HOR, strRecDevId, arrData);
        Matrix  vecMmtsVer = this.computeReconSubFunction(PHASEPLANE.VER, strRecDevId, arrData);
        Matrix  vecMmtsLng = this.computeReconSubFunction(PHASEPLANE.LNG, strRecDevId, arrData);
        
        CovarianceMatrix   matSig = PHASEPLANE.constructCovariance(vecMmtsHor, vecMmtsVer, vecMmtsLng);
        
        this.matCurrSigma = matSig;
        this.dblResErr  = this.computeResidualError(matSig, strRecDevId, arrData);
        
        return matSig;
    }

    /**
     * <p>
     * Compute the observation matrix which yields the vector of beam moments at the given
     * beamline element from the set of beam size measurements provided in the argument.  The
     * matrix is returned as a <code>eMatrix</code> object so that matrices for all three phase planes
     * may be returned.  Currently coupling between phase planes is not consided.  
     * Thus, the returned matrix is block-diagonal having three 3<i>n</i>&times;<i>9</i> 
     * matrices on the diagonal (where <i>n</i> is the number of data locations), 
     * one for each phase plane, horizontal, vertical, diagonal, respectively.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The transfer matrix generator object is accessed to retrieve whatever
     * transfer matrices it contains when this method is called.  <b>BE CAREFUL</b> to
     * generate the appropriate transfer matrices before this method is called. 
     * </p>
     *  
     * @param strReconDevId String id of reconstruction element
     * @param arrData       list of beam size measurement data
     * 
     * @return  a <code>PhaseMatrix</code> containing the observation matrices for all 3 phase planes
     * 
     * @throws IllegalArgumentException unknown phase plane or unknown device 
     * @throws IllegalStateException    transfer matrices have not been generated
     *
     * @author Christopher K. Allen
     * @since  Apr 16, 2013
     */
    public Matrix  computeObservationMatrix(String strReconDevId, ArrayList <Measurement> arrData) 
        throws IllegalArgumentException, IllegalStateException
    {
        // These are the dimensions of the diagonal blocks
        //  NOT the final matrix
        int         cntRows = arrData.size();
        int         cntCols = 3;
        
        // Create the returned matrix
        Matrix      matObs  = new Matrix(3*cntRows, 3*cntCols);
        
        // Compute the diagonal block for each plane and write it into the returned matrix
        int     cntIter = 0;
        for ( PHASEPLANE plane : PHASEPLANE.values() ) {
            
            // Compute the block diagonal observation matrix, i.e., for this phase plane
            Matrix      matBlkDiag = this.computeObservationMatrix(plane, strReconDevId, arrData);
            
            // Get the index of the top left corner of the block diagonal with the phase matrix
            //  object.  With that we can set the entire sub array within the phase matrix.
            int         indTop = cntIter * cntRows;
            int         indBot = indTop + cntRows - 1;
            
            int         indLft = cntIter * cntCols;
            int         indRgt = indLft + cntCols - 1;
            matObs.setMatrix(indTop, indBot, indLft, indRgt, matBlkDiag);
            
            cntIter++;
        }
        
        return matObs;
    }
    
    /**
     * <p>
     * Computes the <b>&sigma;</b> vector from the observer matrix and the given data for the
     * given phase plane.  This
     * is the vector of second-order moments of the beam distribution.  The returned value is
     * a 3&times;1 matrix of real numbers having the form
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&sigma;</b> = ( &lt;<i>x</i><sup>2</sup>&gt;, &lt;<i>xx'</i>&gt;, &lt;<i>x'</i><sup>2</sup>&gt; )<sup>T</sup>
     * <br>
     * <br>
     * where the moments are taken from the horizontal plane.  The vertical plane computation returns an
     * analogous set.  The three phase planes are treated independently and solutions for each plane 
     * returned separately.
     * </p>
     * <p>
     * In the case were there are more data points than there are variables (i.e., greater than 3) 
     * then the returned solution is the least-squares solution; specifically, the set 
     * of moments that hits the most data.  In the case
     * where there are less data points than variables (i.e., less than 3) then the returned solution is
     * the minimum norm (or "least energy") solution; specifically, the solution that has the smallest
     * Lebesgue 2-norm.  If we have three data points then the unique solution is returned.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot;  <em>IMPORTANT</em>: The moments are computed using the transfer matrices currently stored in the
     * internal transfer matrix generator object.  Thus, whatever generation was used prior to
     * this call is used to compute the recursion sub-function.
     * <br>
     * &middot;  If the reconstruction device has non-zero length the location of the moments corresponds 
     * to the exit of the device.
     * </p>
     * 
     * @param arrData       Array List of Location Data
     * @param plane         Phase plane for which to compute the observer matrix 
     * @param strTargElemId reconstruction location 
     * 
     * @return Resulting <b>&sigma;</b> vector of second moments at the given device location
     */
    protected Matrix computeReconSubFunction(PHASEPLANE plane, String strTargElemId, ArrayList<Measurement> arrData) {
        Matrix vecData = this.constructDataVector(plane, arrData);
        Matrix matObs  = this.computeObservationMatrix(plane, strTargElemId, arrData);

        Matrix  vecSigma;
        int     N = arrData.size();
        if (N == 3) {
            vecSigma = matObs.inverse().times(vecData);

        } else if (N < 3) {
            Matrix	matRngOper   = matObs.times( matObs.transpose() );
            Matrix  matPseudoInv = matRngOper.inverse();

            vecSigma = matPseudoInv.times( matObs.times(vecData) );
        } else if (N > 3) {
            Matrix  matObsT      = matObs.transpose();
            Matrix	matDomOper   = matObsT.times( matObs );
            Matrix  matPseudoInv = matDomOper.inverse();

            vecSigma = matPseudoInv.times( matObsT.times(vecData) );

        } else {
            vecSigma = null;

        }

        return vecSigma;
    }

    /**
     * <p>
     * Computes the observation matrix from the given data and accelerator node ID
     * of the reconstruction location.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot; The transfer matrix generator object is accessed to retrieve whatever
     * transfer matrices it contains when this method is called.  <b>BE CAREFUL</b> to
     * generate the appropriate transfer matrices before this method is called. 
     * </p> 
     * 
     * @param plane         Phase plane to calculate to consider
     * @param strReconDevId String id of reconstruction element
     * @param arrData       list of beam size data
     * 
     * @return  Observation matrix for the given data and target location
     * 
     * @see TransferMatrixGenerator#generateWithoutSpaceCharge()
     * @see TransferMatrixGenerator#generateWithSpaceCharge(String, double, CovarianceMatrix)
     * 
     * @throws IllegalArgumentException unknown phase plane or unknown device 
     * @throws IllegalStateException    transfer matrices have not been generated
     */
    protected Matrix computeObservationMatrix(PHASEPLANE plane, String strReconDevId,
            ArrayList <Measurement> arrData) throws IllegalArgumentException, IllegalStateException {
                ArrayList<PhaseMatrix>  arrTransMatrices = new ArrayList<PhaseMatrix>();    
            
                // Get all the transfer matrices between the reconstruction location and the data locations
                int n = 0;
                for (Measurement datum : arrData) {
                    String strElemId = datum.strDevId;
            
                    PhaseMatrix matPhi = genTransMat.retrieveTransferMatrix(strReconDevId, strElemId);
                    arrTransMatrices.add(n, matPhi);
                    n ++;
                }
            
                // Create the new observation matrix and populate it
                Matrix matObs = new Matrix(arrData.size(), 3);
            
                for (int i = 0; i < arrData.size(); i++) {
                    PhaseMatrix matPhi = arrTransMatrices.get(i);
                    
                    for (int j=0; j<plane.getCovariantBasisSize(); j++) {
                        PhaseMatrix matE  = plane.getCovarianceBasis(j);
                        PhaseMatrix matTE = matE.conjugateTrans(matPhi);
                        
                        int         indOffSet = plane.getCovIndexOffset();
                        double      dblTE11   = matTE.getElem(indOffSet, indOffSet);
                        
                        matObs.set(i, j, dblTE11);
                    }
            
                }
                
                return matObs;
            }

    /**
     * Computes and returns the Lebesgue 2-norm of the error residual for 
     * the given solution and problem data.
     *
     * @param matSigSoln    solution matrix of second-order moments
     * @param strRecDevId   device ID where the reconstruction is performed (location of solution)
     * @param arrData       array of problem data
     * 
     * @return  ||<b>X</b> - <b>&Omega;</b><b>&sigma;</b>||<sub>2</sub>
     *
     * @author Christopher K. Allen
     * @since  Nov 15, 2012
     */
    protected double computeResidualError(CovarianceMatrix matSigSoln, String strRecDevId, ArrayList<Measurement> arrData) {
        int     N = arrData.size();
    
        double  dblErrTotal = 0.0;
        for (PHASEPLANE plane : PHASEPLANE.values()) {
            Matrix  vecSig  = plane.extractCovarianceVector(matSigSoln);
    
            double  dblError = 0.0;
            if (N < 3) {
                dblError = vecSig.normF(); 
    
            } else if (N >= 3) {
                Matrix  vecData = this.constructDataVector(plane, arrData);
                Matrix  matObs  = this.computeObservationMatrix(plane, strRecDevId, arrData);
    
                dblError = (vecData.minus( matObs.times(vecSig) )).normF();
    
            } else {
                dblError = 0.0;
                
            }
            dblErrTotal += dblError*dblError;
            
        }
        
        return Math.sqrt(dblErrTotal);
    }

    /**
     * Computes the norm of the distance between the given covariance solution
     * matrix and the last solution matrix.  This method should be called under the
     * assumption that an iterative solution technique is being employed and we are
     * monitoring a Cauchy sequence to check for convergence.  
     * 
     * @param   matSig1     current solution iterate <b>&sigma;</b><sub><i>i</i>+1</sub>
     * @param   matSig0     previous solution iterate <b>&sigma;</b><sub><i>i</i></sub>
     * 
     * @return      the (Frobenius) distance between the current iterate and the previous one
     *              || <b>&sigma;</b><sub><i>i</i>+1</sub> - <b>&sigma;</b><sub><i>i</i></sub> ||<sub><i>F</i></sub> 
     *
     * @author Christopher K. Allen
     * @since  Apr 5, 2013
     */
    protected double computeConvergenceError(CovarianceMatrix matSig1, CovarianceMatrix matSig0) {
        PhaseMatrix     matDispl = matSig1.minus( matSig0 );
        double          dblErr2  = matDispl.normF();
    
        return  Math.sqrt(dblErr2);
    }

    /**
     * Takes the given measurement data and packages it into an appropriate vector
     * for the given phase plane.  Also, converts the measurement RMS values (sigmas)
     * into the second-order moment values.
     * 
     * @param plane     phase plane we are reconstructing
     * @param arrMsmt   array of location and beam size data 
     *
     * @return          the vector (&lt;<i>x</i><sub>1</sub><sup>2</sup>&gt;, &lt;<i>x</i><sub>2</sub><sup>2</sup>&gt;, ...; &lt;<i>x<sub>N</sub></i><sup>2</sup>&gt;) 
     * 
     * @author Christopher K. Allen
     * @author Eric Dai
     * @since  Jul 20, 2012
     * 
     */
    private Matrix constructDataVector(PHASEPLANE plane, ArrayList<Measurement> arrMsmt) {
        Matrix matData = new Matrix(arrMsmt.size(), 1);
        
        for (int n = 0; n < arrMsmt.size(); n++) {
            Measurement msmt    = arrMsmt.get(n);
            
            double      dblSigma = plane.extractBeamSize(msmt);
            
            matData.set(n, 0, dblSigma*dblSigma);
        }
        
        return matData;
    }

    /**
     * Computes and returns the Courant-Snyder parameters (i.e, the <code>Twiss</code> object)
     * having the equivalent information as the given vector of second-order (RMS) moments.
     *
     * @param vecMmts   a 3&times;1 matrix vector 
     *                  (&lt;<i>x</i><sup>2</sup>&gt;,&lt;<i>xx'</i>&gt;,&lt;,&lt;<i>x'</i><sup>2</sup>&gt;)<sup>T</sup>
     * @return         Courant-Snyder parameters (&alpha;,&beta;,&epsilon;) corresponding to the given moments.
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2012
     */
    @SuppressWarnings("unused")
    private Twiss computeEquivalentTwiss(Matrix vecMmts) {
        
        double  dblMmtPos = vecMmts.get(0, 0);
        double  dblMmtCov = vecMmts.get(1, 0);
        double  dblMmtAng = vecMmts.get(2, 0);
        Twiss   twsEquiv  = Twiss.createFromMoments(dblMmtPos, dblMmtCov, dblMmtAng);
        
        return twsEquiv;
    }

}
