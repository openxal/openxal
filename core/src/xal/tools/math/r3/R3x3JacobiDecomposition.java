/*
 * Created on October 16, 2006
 * 
 * Author       : Christopher K. Allen
 * Institution  : JAEA
 *
 */
package xal.tools.math.r3;

import xal.tools.math.r3.R3x3.POS;




/**
 * <p>
 *  Encapsulates the results of an eigenvalue decomposition operation on
 *  a symmetric <b>R</b><sup>3&times;3</sup> matrix <b>A</b> object 
 *  using <em>Jacobi iterations</em>.
 *  </p>
 * <p>
 *  If the matrix is symmetric it can be factored as
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>A</b> = <b>RDR</b><sup>T</sup>
 * <br>
 * <br>
 *  where <b>A</b> is the target matrix, <b>R</b> is an orthogonal 
 *  matrix in <i>SO</i>(3), and <b>D</b> is the diagonal matrix of real 
 *  eigenvales of <b>A</b>.
 *  </p>
 *  The JAMA matrix package is <em>not</em> explicitly used in this class.
 *  <p>
 *  </p>
 *  <p>
 *  Most of the work of the Jacobi iterations is done in the helper
 *  class <code>{@link JacobiIterate}</code>.
 *  </p>
 * 
 * @author Christopher K. Allen
 * 
 * #see xal.tools.math.r3.R3x3
 * #see xal.tools.math.r3.JacobiIterate
 */

public class R3x3JacobiDecomposition {


    /*
     * Global Constants
     */

    /** the value of one degree in radians */
    public final static double     ONE_DEGREE = Math.PI/180.0;
    
    /** Stopping error criterion */
    public final static double     ROTATION_TOLERANCE = 0.00001 * Math.PI/180.0;
    
    /** small numerical tolerance */
    public final static double     ERROR_TOLERANCE = 1.0e5*Double.MIN_VALUE;
    
    
    /*
     * Global Variables
     */
    
    /** Class debugging flag */
    public static boolean      bolDebug = false;
    
    
    

    
    /*
     * Global Methods
     */
    
    /**
     * Turn debugging flag on or off.  Turning on debugging flag
     * sends debugging information to the console.
     * 
     * @param   bolDebug    value of debugging flag.
     */
    public static void setDebug(boolean bolDebug)  {
        R3x3JacobiDecomposition.bolDebug = bolDebug;
    }
    
    
    /*
     *  Local Attributes 
     */
    
    
    /** the number of iterations necessary to decompose the matrix */
    private int         cntIter;
    
    /** the eigenvalue decomposition rotation matrix */ 
    private R3x3        matRot;
    
    /** the eigenvalue decomposition diagonal matrix */
    private R3x3        matDiag;

    
    

    

    /*
     * Initialization
     */
     
     
    /**
     * Constructor for <code>R3x3JacobiDecomposition</code> objects.  The
     * decomposition is done in the construction of this object.
     * 
     * @param matTarget     target matrix to factorize
     * 
     * @throws  IllegalArgumentException    matrix is not symmetric
     */
    public R3x3JacobiDecomposition(final R3x3 matTarget) throws IllegalArgumentException {
        if (!matTarget.isSymmetric())
            throw new IllegalArgumentException("R3x3JacobiDecomposition: Target matrix is not symmetric");
        
        this.decompose(matTarget);
    }
    
    
    
    /*
     *  Data Query
     */

    /**
     * Get the number of iterations necessary to diagonalize the matrix
     * to give precision.  
     * 
     * @return  number of Jacobi iterations performed.
     * 
     * @see R3x3JacobiDecomposition#ROTATION_TOLERANCE
     */
    public int      getIterationCount()  {
        return this.cntIter;
    }
    
    /**
     * Get the array of all eigenvalues.
     * 
     *  @return     size 3 array of all the eigenvalues
     */
    public double[] getEigenvalues()  {
        double[]    arrEigvals = new double[3];
        
        for (POS pos : POS.getDiagonal()) {
            arrEigvals[pos.row()] = this.matDiag.getElem(pos);
        }
        
        return arrEigvals;
    }
    
//    /**
//     * Get the eigenvalues as a vector in R3.
//     * 
//     * @return  vector of eigenvalues (lambda1, lambda2, lambda3) 
//     */
//    public R3   getEigenvalues()    {
//        R3      vecVals = new R3(this.getRealEigenvalues());
//
//        return vecVals;
//    }
//    
    /**
     *  Get the matrix R of eigenvectors for the decomposition.  Note
     *  that this matrix is the diagonalizing matrix for the target matrix A. 
     *  Since the target matrix A is symmetric then the returned matrix R will 
     *  be in the special orthogonal group SO(3).
     *  
     *  Note that, in general, this matrix could be badly conditioned.
     *
     *  @return     diagonalizing matrix of A 
     */    
    public R3x3 getRotationMatrix()  {
        return this.matRot;
    }
    
    
    /**
     * Return the matrix D of eigenvalues in the decomposition.  Note that since 
     * target matrix A is symmetric then this matrix will be diagonal with real
     * elements.  
     *
     * @return      diagonal matrix D of eigenvalues of A
     */
    public R3x3 getDiagonalMatrix()   {
        return this.matDiag;    
    }
    




    
    /*
     * Internal Support
     */
    
    /** 
     * <p>
     * Decomposes the given matrix <b>&sigma;</b> into the product
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>&sigma;</b> = <b>RDR</b><sup><i>T</i></sup> ,
     * <br>
     * <br>
     * where <b>R</b> &in; <i>O</i>(3) is the conjugating rotation matrix and
     * <b>D</b> &in; <b>R</b><sub>+</sub><sup>3&times;3</sup> is the diagonal
     * matrix of eigenvalues of <b>&sigma;</b>.
     * </p>
     * <p>
     * The actual computations are done by the objects of the class 
     * <code>{@link JacobiIterate}</code>.
     * </p>
     * 
     * @param matTarget  the covariance matrix of the ellipsoid
     * 
     * @throws IllegalArgumentException     the covariance matrix is not symmetric 
     *                                      and/or positive definite
     * 
     * @see #setRotation
     * @see #setSemiAxes
     */
    private void decompose(final R3x3  matTarget) throws IllegalArgumentException {    
        JacobiIterate   iter  = new JacobiIterate(matTarget);
        
        // Initialize the loop
        double  angle = iter.getAngle();    // rotation angle
        R3x3    R     = iter.getRotation(); // rotation matrix
        R3x3    Rt    = R.transpose();      // transpose of rotation matrix          
        R3x3    D     = matTarget;          // diagonalization matrix

        this.cntIter = 0;
        this.matRot = R3x3.newIdentity();

        while (Math.abs(angle) > ROTATION_TOLERANCE )   {       // while the rotation angle is large
            R     = iter.getRotation();
            Rt    = R.transpose();

            D  = Rt.times(D.times(R));
            
            this.matRot = this.matRot.times(R);
            this.cntIter++;
            
            if (bolDebug) 
                System.out.printf("iter : %d, angle = %g degrees\n", this.cntIter, 180.0*(angle/Math.PI));

            iter  = new JacobiIterate(D);
            angle = iter.getAngle();
        }
        
        // Save the answer
        this.matDiag = D;
    }
    
    

}



/**
 * Class for computing a single Jacobi iteration.  
 * 
 * The class does most of the work in computing the
 * parameters and quantities for a single Jacobi iteration
 * in a matrix diagonalization.
 * 
 * @author Christopher K. Allen
 *
 */

class JacobiIterate {

    
    
    /*
     * Global Constants
     */
    
    /** the value pi/4 */
    public static final double      PI_BY_4 = Math.PI/4.0;
    
    /** the value pi/2 */
    public static final double      PI_BY_2 = Math.PI/2.0;
    
    
    
    /*
     * Local Attributes
     */
    
    /** target matrix upon which to determine next iterate */
    private R3x3            matTarget;
    
    /** pivot position */
    private POS             posPivot;
    
    /** rotation angle */
    private double          dblAng;
    
    /** rotation matrix */
    private R3x3            matRot;
    
    
    /*
     * Initialization
     */
    
    /**
     * Construct a new <code>JacobiIterate</code> object.  The target matrix
     * is analyzed and the new iteration parameters are computed.
     * 
     * @param   matTarget   matrix for which new iterate parameters are to be computed
     */
    public JacobiIterate(final R3x3   matTarget)  {
        this.matTarget = matTarget;
        this.posPivot = compPivot();
        this.dblAng = compAngle2();
        this.matRot = compRotation();
    }
    
    
    /*
     * Data Query
     */
    
    
    /**
     * Return the pivot position of the iteration.
     * 
     * @return  off-diagonal position that we are pivoting upon
     */
    public POS getPosition()   {
        return this.posPivot;
    }
    
    /**
     * Return the angle of rotation for the iteration.
     * 
     * @return  rotation angle (in radians)
     */
    public double   getAngle()  {
        return this.dblAng;
    }
    
    /**
     * Return the rotation matrix for the iteration.
     * 
     * @return  rotation element in SO(3)
     */
    public R3x3     getRotation()   {
        return this.matRot;
    }
    
    
    
    /*
     * Internal Support
     */
    
    /**
     * Compute the pivot position for the iteration.  This is the 
     * off-diagonal position with the largest coupling coefficient.
     * 
     * @return  the pivot position as a <code>OffDiagonal</code> enumeration
     */
    private POS compPivot()    {

        double      cplVal;         // current coupling value
        double      cplMax;         // maximum coupling value
        POS    posMax;         // position of maximum value
        
        posMax = POS.XY;
        cplVal = compCoupling(posMax);
        cplMax = cplVal;
        for (POS pos : POS.getUpperTriangle()) {
            cplVal = compCoupling(pos);
            
            if (cplVal > cplMax)    {
                cplMax = cplVal;
                posMax = pos;
            }
        }
        
        return posMax;
    }
    
    /**
     * Called by <code>compPivot()</code> to determine the coupling
     * coefficient for an off-diagonal position.  This value provides
     * a measure of size of the rotation necessary to zero out the off-
     * diagonal.
     * 
     * Essentially, the coupling coefficient is computed to be the
     * square of the matrix element divided by the row and column 
     * diagonal intercepts.
     * 
     * <b>
     * This method has been simplified!  Currently it returns the 
     * magnitude of the target matrix element indicated by the 
     * argument.
     * 
     * This behavior is more general than that for a 3x3 matrix
     * assuming that it is a spatial covariance matrix.  It is
     * also numerically faster and probably more appropriate.
     * </b>
     * 
     * @param   pos     off-diagonal position of the target matrix
     * @return          value of the coupling coefficient for given <code>pos</code>
     */
    private double compCoupling(POS pos)   {
//        double  dblOffDiag = pos.getValue(matTarget);
//        double  dblValue_2 = dblOffDiag*dblOffDiag;
//        
//        if (dblValue_2 < R3x3JacobiDecomposition.ERROR_TOLERANCE)
//            return 0.0;
//
//        double  dblDiagRow = pos.getRowDiag(matTarget);
//        double  dblDiagCol = pos.getColDiag(matTarget);
//        return Math.abs( dblValue_2/(dblDiagRow*dblDiagCol) );
        
        double  dblVal = pos.getValue(matTarget);
        double  dblMag = Math.abs(dblVal);
        
        return dblMag;
    }

    /**
     * Compute the angle of rotation necessary to zero out the pivot element
     * in the Jacobi iteration.  Note that there are two solutions to this angle.
     * We return the one whose magnitude is less than <i>pi</i>/4.  This value 
     * provides the most stable reduction.
     * 
     * This method solves the simplified double angle formula for the rotation
     * angle then converts back to the smaller rotation angle.
     * 
     * @return  angle of rotation in the interval [-pi/4,pi/4]
     */
    @SuppressWarnings("unused")
    private double compAngle1() {
        double  dblVal = posPivot.getValue(matTarget);
        double  dblRow = posPivot.getRowDiag(matTarget);
        double  dblCol = posPivot.getColDiag(matTarget);
        
        double dblAng = 0.5*Math.atan2(2.0*dblVal,(dblRow - dblCol));
        
        if (dblAng >  PI_BY_4) dblAng = dblAng - PI_BY_2;
        if (dblAng < -PI_BY_4) dblAng = dblAng + PI_BY_2;
        
        return dblAng;
    }
    
    /**
     * Compute the angle of rotation necessary to zero out the pivot element
     * in the Jacobi iteration.  Note that there are two solutions to this angle.
     * We return the one whose magnitude is less than <i>pi</i>/4.  This value 
     * provides the most stable reduction.
     * 
     * This method uses the quadratic formula to determine the smaller of the
     * two solutions for tan(ang), this solution is in [-pi/4,+pi/4].
     * 
     * @return  angle of rotation in the interval [-pi/4,pi/4]
     */
    private double compAngle2() {
        double  aij = posPivot.getValue(matTarget);     // pivot element
        double  aii = posPivot.getRowDiag(matTarget);   // row diagonal
        double  ajj = posPivot.getColDiag(matTarget);   // column diagonal
        double  b   = (ajj - aii)/(2.0*aij);            // quadratic equation linear coeff
        double  des = Math.sqrt(b*b+1);                 // quadratic eq descriminant
        
        // Get the smaller rotation solution
        //      Note that des>0 and des>|b|
        double  tan;            // tangent of the rotation angle
        double  ang;            // rotation angle
        
        if (b > 0) 
            tan = b - des;    // 'minus' quadratic solution
        else
            tan = b + des;    // 'plus' quadratic solution
        
        ang = Math.atan(tan);
        
        return ang;
    }
    
    /**
     * Compute the rotation matrix required to zero out the 
     * pivot element of the target matrix.
     * 
     * @return  rotation matrix in SO(2) contained in SO(3)
     */
    private R3x3 compRotation() {
        R3x3    matRot = R3x3.newIdentity();
        double  dblSin = Math.sin(this.dblAng);
        double  dblCos = Math.cos(this.dblAng);
        
        int     iRow = posPivot.row();
        int     iCol = posPivot.col();
        
        matRot.setElem(iRow,iCol, -dblSin);
        matRot.setElem(iRow,iRow,  dblCos);
        matRot.setElem(iCol,iRow,  dblSin);
        matRot.setElem(iCol,iCol,  dblCos);
        
        return matRot;
    }

    
    /*
     * Class Debugging
     */
    
    /**
     * See if the original angle equation is satisfied.
     * 
     * @param   ang     rotation angle
     */
    @SuppressWarnings("unused")
    private boolean checkAngleFormula(double ang) {
        double  aij = posPivot.getValue(matTarget);     // pivot element
        double  aii = posPivot.getRowDiag(matTarget);   // row diagonal
        double  ajj = posPivot.getColDiag(matTarget);   // column diagonal
        
        double  sin = Math.sin(ang);
        double  cos = Math.cos(ang);

        double  val = aij*(cos*cos - sin*sin) + sin*cos*(ajj - aii);
        
        boolean res = Math.abs(val) < R3x3JacobiDecomposition.ERROR_TOLERANCE;
        
        return res;
    }
}
