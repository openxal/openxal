/**
 * PHASEPLANE.java
 *
 * @author Christopher K. Allen
 * @since  Jul 20, 2012
 *
 */

/**
 * PHASEPLANE.java
 *
 * @author  Christopher K. Allen
 * @since	Jul 20, 2012
 */
package xal.extension.twissobserver;

import java.lang.reflect.Field;
import java.util.ArrayList;

import Jama.Matrix;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;

/**
 * Enumeration of the mechanical motion phase planes for use in 
 * identifying the families of Courant-Snyder parameters.  The enumeration
 * also contains useful methods for converting between covariance space
 * representations.
 *
 * @author Christopher K. Allen
 * @author Eric Dai
 * @since   Jul 20, 2012
 * @version March 27, 2013
 * 
 * @see PhaseMatrix
 * @see CorrelationMatrix
 */
public enum PHASEPLANE {

    /** Horizontal phase plane */
    HOR(0, "dblSigHor"), 
    
    /** Vertical phase plane */
    VER(2, "dblSigVer"),
    
    /** Longitudinal phase plane */
    LNG(4, "dblSigLng");
    
    


    /**
     * Global Operations
     */
    
    /**
     * Packs the second moments in the three vectors into a properly formatted
     * covariance matrix.
     *
     * @param vecMmtsHor    vector of horizontal second moments
     * @param vecMmtsVer    vector of vertical second moments
     * @param vecMmtsLng    vector of longitudinal second moments
     * 
     * @return              the covariance matrix with the given moments as elements
     *
     * @author Christopher K. Allen
     * @since  Aug 31, 2012
     */
    static public CovarianceMatrix   constructCovariance(Matrix vecMmtsHor, Matrix vecMmtsVer, Matrix vecMmtsLng) {
        CovarianceMatrix       matSig = CovarianceMatrix.newZero();
        
        for (int i=0; i<2; i++)
            for (int j=0; j<2; j++) {
                matSig.setElem(i, j,     vecMmtsHor.get(i+j, 0) );
                matSig.setElem(i+2, j+2, vecMmtsVer.get(i+j, 0) );
                matSig.setElem(i+4, j+4, vecMmtsLng.get(i+j, 0) );
            }
        
        matSig.setElem(6, 6, 1.0);
        
        return matSig;
    }
    
    
    
    /**
     * Local Operations
     */
    
    /**
     * Returns the index offset into the covariance matrix for this phase plane.
     * This is the diagonal index of the top left element of the 2&times;2
     * diagonal block that this enumeration constant represents.
     *    
     * @return  index offset identifying the block diagonal within the covariance matrix
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    public int  getCovIndexOffset() {
        return this.iMatOffset;
    }
    
    /**
     * Returns the cardinality of the standard basis set.
     * 
     * @return  number of standard basis set matrices
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2013
     */
    public int getStandardBasisSize() {
        return this.arrStdBasis.size();
    }
    
    /**
     * <p>
     * Returns the standard basis matrix for the given index.  There are 
     * <b>four</b> of these matrices for each phase plane.  They provide an independent
     * vector representation
     * of the 4&times;4 diagonal block occupied by this phase space within <b>phase
     * matrices</b>. Restricting to one block diagonal we have
     * </p>
     * <br/>
     * <table>
     *     <tr><td><b>e</b><sub>0</sub></td><td>&equiv;</td><td>| 1 0 |</td></tr>
     *     <tr><td/><td/><td>| 0 0 |</td></tr>
     * </table>
     * <br/>   
     * <table>
     *     <tr><td><b>e</b><sub>1</sub></td><td>&equiv;</td><td>| 0 1 |</td></tr>
     *     <tr><td/><td/><td>| 0 0 |</td></tr>
     * </table>
     * </br>       
     * <table>
     *     <tr><td><b>e</b><sub>2</sub></td><td>&equiv;</td><td>| 0 0 |</td></tr>
     *     <tr><td/><td/><td>| 1 0 |</td></tr>
     * </table>
     * <br/>
     * <table>
     *     <tr><td><b>e</b><sub>3</sub></td><td>&equiv;</td><td>| 0 0 |</td></tr>
     *     <tr><td/><td/><td>| 0 1 |</td></tr>
     * </table>
     * <br/>
     * The argument <var>indBasis</var> is the index <i>i</i> of <b>e</b><sub><i>i</i></sub>.
     *   
     * @param indBasis  The index of the basis vector
     * 
     * @return          The basis vector in covariance space as described above
     * 
     * @throws ArrayIndexOutOfBoundsException   The given index is greater than 3
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    public PhaseMatrix  getStandardBasis(int indBasis) throws ArrayIndexOutOfBoundsException {
        return this.arrStdBasis.get(indBasis);
    }

    
    /**
     * Returns the cardinality of the covariance basis set.
     * 
     * @return  number of covariance basis set matrices
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2013
     */
    public int getCovariantBasisSize() {
        return this.arrCovBasis.size();
    }
    
    /**
     * <p>
     * Returns the covariance basis matrix for the given index.  There are 
     * <b>three</b> of these matrices for each phase plane.  They provide a 
     * vector representation of the 4&times;4 block diagonal of the covariance matrix 
     * occupied by this phase plane.  Because of the symmetry of the covariance matrix,
     * there are only three independent basis vectors.
     * Restricting attention to one block diagonal we have
     * </p>
     * <br/>
     * <table>
     *     <tr><td><b>c</b><sub>0</sub></td><td>&equiv;</td><td>| 1 0 |</td></tr>
     *     <tr><td/><td/><td>| 0 0 |</td></tr>
     * </table>
     * <br/>   
     * <table>
     *     <tr><td><b>c</b><sub>1</sub></td><td>&equiv;</td><td>| 0 1 |</td></tr>
     *     <tr><td/><td/><td>| 1 0 |</td></tr>
     * </table>
     * </br>       
     * <table>
     *     <tr><td><b>c</b><sub>2</sub></td><td>&equiv;</td><td>| 0 0 |</td></tr>
     *     <tr><td/><td/><td>| 0 1 |</td></tr>
     * </table>
     * <br/>
     * The argument <var>indBasis</var> is the index <i>i</i> of <b>c</b><sub><i>i</i></sub>.
     *   
     * @param indBasis  The index of the basis vector
     * 
     * @return          The basis vector in covariance space as described above
     * 
     * @throws ArrayIndexOutOfBoundsException   The given index is greater than 3
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    public CovarianceMatrix  getCovarianceBasis(int indBasis) throws ArrayIndexOutOfBoundsException {
        return this.arrCovBasis.get(indBasis);
    }
    
    /**
     * Returns the beam size field corresponding to this phase plane from the given 
     * measurement data structure. 
     * 
     * @param msmt      data structure of beam size measurements for each phase plane
     * 
     * @return          the beam size measurement for this phase plane
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2013
     */
    public double   extractBeamSize(Measurement msmt)  {
        try {
            Double      dblBmSz = (Double) this.fldMsmtBmSz.get(msmt);
            
            return dblBmSz;
            
        } catch (IllegalArgumentException e) {
            System.err.println("Serious error - Exiting. " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            
            return 0.0;
            
        } catch (IllegalAccessException e) {
            System.err.println("Serious error - Exiting. " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
            
            return 0.0;
        }
    }
    
    /**
     * Extracts the (3) moments for the given phase plane from the covariance matrix
     * and return them as a 3 &times; 1 vector.  For example, for the horizontal plane
     * we return 
     * (&lt;<i>x</i><sup>2</sup>&gt;, &lt;<i>xx'</i>&gt;, &lt;<i>x'</i><sup>2</sup>&gt;)<sup><i>T</i></sup>.
     * 
     * @param matCov    the covariance matrix containing all the first and second order moments
     * 
     * @return          vector of moments for this phase plane
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    public Matrix   extractCovarianceVector(CovarianceMatrix matCov) {
        Matrix  vecCov = new Matrix(3,1);
        
        vecCov.set(0, 0, matCov.getElem(iMatOffset,     iMatOffset) );
        vecCov.set(1, 0, matCov.getElem(iMatOffset,     iMatOffset + 1) );
        vecCov.set(2, 0, matCov.getElem(iMatOffset + 1, iMatOffset + 1) );
        
        return vecCov;
    }
    
    
    /*
     * Enumeration Attributes
     */
    
    /** Field of the <code>Measurement</code> data structure which contains the RMS beam size for this plane */
    private Field           fldMsmtBmSz;
    
    /** Base index of the constant into the covariance matrix (<code>CorrelationMatrix</code>). */
    private final int       iMatOffset;
    
    /** Standard basis matrices for covariance space covered by this phase plane */
    private final ArrayList<PhaseMatrix>        arrStdBasis;
    
    /** Covariance basis matrices for covariance space covered by this phase plane */
    private final ArrayList<CovarianceMatrix>  arrCovBasis;
    
    
    /*
     * Initialization
     */
    
    /** 
     * Initializing constructor.  Accept the index offset into the covariance
     * matrix then initialize the basis matrices in covariance space.
     * 
     *  @param  indCovMat       index offset into the covariance matrix for this plane
     *  @param  strFldNm        name of the <code>Measurement</code> field containing beam size for this plane
     */
    private PHASEPLANE(final int indCovMat, String strFldNm) {
        this.iMatOffset  = indCovMat;
        this.arrStdBasis = new ArrayList<PhaseMatrix>();
        this.arrCovBasis = new ArrayList<CovarianceMatrix>();
        
        this.initBmSzFld(strFldNm);
        this.initStdBasis();
        this.initCovBasis();
    }

    /**
     * Initializes the <code>Field</code> object representing the data structure field
     * containing the beam size for this phase plane.
     * 
     * @param strFldNm      name of the <code>Measurement</code> field
     *
     * @author Christopher K. Allen
     * @since  Mar 28, 2013
     */
    private void initBmSzFld(String strFldNm) {
        
        try {
            this.fldMsmtBmSz = Measurement.class.getDeclaredField(strFldNm);
            
        } catch (NoSuchFieldException e) {
            System.err.println("Measurement field '" + strFldNm + "' not found.  Exiting.");
            e.printStackTrace();
            System.exit(1);

        } catch (SecurityException e) {
            System.err.println("Measurement field '" + strFldNm + "' not accessible.  Exiting.");
            e.printStackTrace();
            System.exit(1);
        
        }
    }
    
    /**
     * Initializes the standard basis matrices for this phase plane.  There are 
     * four of these matrices for each phase plane.  They allow a vector representation
     * of the block diagonal covariance matrix for a distribution. 
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    private void initStdBasis() {

        for(int i=0; i<2; i++)
            for (int j=0; j<2; j++) {
                PhaseMatrix matBasis = PhaseMatrix.zero();

                matBasis.setElem(i + this.iMatOffset, j + this.iMatOffset, 1.0);
                this.arrStdBasis.add(matBasis);
            }
    }
    
    /**
     * Initializes the covariance basis matrices for this phase plane.  There are 
     * three of these matrices for each phase plane.  They provide a vector representation
     * of the symmetric covariance space for a distribution. 
     *
     * @author Christopher K. Allen
     * @since  Mar 27, 2013
     */
    private void initCovBasis() {
        CovarianceMatrix matBasis1 = CovarianceMatrix.newZero();
        matBasis1.setElem(this.iMatOffset, this.iMatOffset, 1.0);
        this.arrCovBasis.add(matBasis1);
        
        CovarianceMatrix matBasis2 = CovarianceMatrix.newZero();
        matBasis2.setElem(this.iMatOffset,     this.iMatOffset + 1,  1.0);
        matBasis2.setElem(this.iMatOffset + 1, this.iMatOffset,      1.0);
        this.arrCovBasis.add(matBasis2);
        
        CovarianceMatrix matBasis3 = CovarianceMatrix.newZero();
        matBasis3.setElem(this.iMatOffset + 1, this.iMatOffset + 1, 1.0);
        this.arrCovBasis.add(matBasis3);
    }

}
