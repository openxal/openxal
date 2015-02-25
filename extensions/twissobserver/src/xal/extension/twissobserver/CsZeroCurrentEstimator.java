/**
 * ZeroCurrentSolution.java
 *
 * @author Christopher K. Allen
 * @since  Apr 18, 2013
 */
package xal.extension.twissobserver;

import xal.tools.beam.CovarianceMatrix;
import xal.model.ModelException;

import java.util.ArrayList;

import Jama.Matrix;

/**
 * Reconstructs the zero-current second-order beam moments from multiple profile data measurements
 * along a beamline.  This class is a simple extension of the <code>{@link TwissObjserverBase}</code>
 * class providing only public access to the tools for computing the recursion function in the 
 * case of zero beam current.
 *
 * @author Christopher K. Allen
 * @since  Apr 18, 2013
 *
 */
public class CsZeroCurrentEstimator extends CourantSnyderEstimator {

    /**
     * Creates a new instance of <code>ZeroCurrentSolution</code>.
     *
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public CsZeroCurrentEstimator(TransferMatrixGenerator genTransMatrix) {
        super(genTransMatrix);
    }

    /**
     * Creates a new instance of <code>ZeroCurrentSolution</code>.
     *
     * @param bolDebug      output debug information to the console if <code>true</code>
     * @param genTransMat   a pre-configured transfer matrix engine used internally.   
     *
     * @author Christopher K. Allen
     * @since  Apr 18, 2013
     */
    public CsZeroCurrentEstimator(boolean bolDebug, TransferMatrixGenerator genTransMat) {
        super(bolDebug, genTransMat);
    }
    
    
    /*
     * Operations
     */

    /**
     * <p>
     * Computes the covariance matrix <b>&sigma;</b> of Courant-Snyder parameters 
     * from the given data at the given device location.  This is a least-squares reconstruction 
     * of the CS parameters using the given data.  
     * This is the set of second-order moments of the beam distribution.  Since the
     * measurement data cannot show coupling between phase planes, the covariance matrix is
     * block diagonal where the 2&times;2 blocks correspond to each independent phase plane
     * (see <code>{@link CorrelationMatrix}</code>).  If the measurement data is zero
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
     * <p>
     * <h4>NOTE</h4>
     * &middot;  If the reconstruction device has non-zero length the location of the moments corresponds 
     * to the exit of the device.
     * </p>
     * @param strRecDevId   ID of the device where the reconstruction is performed
     * @param arrData       measurement data consisting of RMS beam sizes
     * 
     * @return              block diagonal covariance matrix containing second-order moments
     *                      at the reconstruction location
     * 
     * @throws ModelException   error occurred during the transfer matrix computations
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2012
     */
    public CovarianceMatrix computeReconstruction(String strRecDevId, ArrayList<Measurement> arrData)
        throws ModelException 
    {
        this.genTransMat.generateWithoutSpaceCharge();

        Matrix  vecMmtsHor = this.computeReconSubFunction(PHASEPLANE.HOR, strRecDevId, arrData);
        Matrix  vecMmtsVer = this.computeReconSubFunction(PHASEPLANE.VER, strRecDevId, arrData);
        Matrix  vecMmtsLng = this.computeReconSubFunction(PHASEPLANE.LNG, strRecDevId, arrData);

        CovarianceMatrix   matSig = PHASEPLANE.constructCovariance(vecMmtsHor, vecMmtsVer, vecMmtsLng);

        super.matCurrF     = matSig;
        super.matCurrSigma = matSig;
        super.dblResErr    = super.computeResidualError(matSig, strRecDevId, arrData);

        return matSig;
    }
    
    
    /*
     * Base Class Requirements
     */

    /**
     * Fulfills the requirement of the base class abstract function.  The beam charge 
     * parameter is ignored.  This method is simply a proxy to 
     * {@link #computeReconstruction(String, ArrayList)}.
     * 
     * @param strRecDevId   ID of the device where the reconstruction is performed
     * @param dblBnchFreq   bunch arrival frequency - <em>Ignored</em>
     * @param dblBmCurr     beam current - <em>Ignored</em>
     * @param arrData       measurement data consisting of RMS beam sizes
     * 
     * @return              block diagonal covariance matrix containing second-order moments
     *                      at the reconstruction location
     * 
     * @throws ModelException   error occurred during the transfer matrix computations
     *
     * @author Christopher K. Allen
     * @since  May 1, 2013
     */
    public CovarianceMatrix    computeReconstruction(String strRecDevId, double dblBnchFreq, double dblBmCurr, ArrayList<Measurement> arrMsmts) 
        throws ModelException 
    {
        CovarianceMatrix  matSigRec = this.computeReconstruction(strRecDevId, arrMsmts);
        
        return matSigRec;
    }
    
}
