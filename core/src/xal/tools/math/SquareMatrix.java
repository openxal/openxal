/**
 * BaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2013
 */
package xal.tools.math;

/**
 * <p>
 * Class <code>SquareMatrix</code> is the abstract base class for square matrix
 * objects supported in the XAL tools packages.
 * </p>
 * <p>
 * Currently the internal matrix operations are supported by the <tt>Jama</tt>
 * matrix package.  However, the <tt>Jama</tt> matrix package has been deemed a 
 * "proof of principle" for the Java language and scientific computing and 
 * is, thus, no longer supported.  The objective of this base class is to hide
 * the internal implementation of matrix operations from the child classes and
 * all developers using the matrix packages.  If it is determined that the <tt>Jama</tt>
 * matrix package is to be removed from XAL, the modification will be substantially
 * simplified in the current architecture.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2013
 */
public abstract class SquareMatrix<M extends SquareMatrix<M>> extends BaseMatrix<M> {


    
    /*
     *  Local Attributes
     */

    /** size of the the square matrix */
    private final int   intSize;
    


    /*
     *  Assignment
     */

    /**
     * Set the element specified by the given position indices to the
     * given new value.
     * 
     * @param   iRow    matrix row location
     * @param   iCol    matrix column index
     * 
     * @param   dblVal  matrix element at given row and column will be set to this value
     */
    public void setElem(IIndex iRow, IIndex iCol, double dblVal) {
        this.getMatrix().set(iRow.val(), iCol.val(), dblVal);
    }

    /**
     * Assign this matrix to be the identity matrix.  The
     * identity matrix is the square matrix with 1's on the
     * diagonal and 0's everywhere else.
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignIdentity() {
        this.assignZero();
        
        for (int i=0; i<this.getSize(); i++)
            this.setElem(i, i, 1.0);
    }
    


    /*
     *  Matrix Attributes
     */

    /**
     * Returns the size of this square matrix, that is, the equal
     * number of rows and columns.
     * 
     * @return  matrix is square of this size on a side
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    public int  getSize() {
        return this.intSize;
    }


    
    
    
    /*
     * Matrix Properties
     */
    
    /**
     *  Check if matrix is symmetric.  
     * 
     *  @return true if matrix is symmetric 
     */
    public boolean isSymmetric()   {
    
//        System.out.println("SquareMatrix#isSymmetric() : class " + this.getClass().getName());
//        System.out.println(this);
//        System.out.println();
        
        for (int i=0; i<this.getSize(); i++)
            for (int j=i; j<this.getSize(); j++) {
                if (!ElementaryFunction.approxEq( getElem(i,j), getElem(j,i) ) )
                    return false;
            }
        return true;
    }

    /**
     * Checks if the given matrix is algebraically equivalent to this
     * matrix.  That is, it is equal in size and element values.
     * 
     * @param matTest   matrix under equivalency test
     * 
     * @return          <code>true</code> if the argument is equivalent to this matrix,
     *                  <code>false</code> if otherwise
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    public boolean isEquivalentTo(BaseMatrix<M> matTest) {
        if ( !this.getClass().equals(matTest.getClass()) )
            return false;

        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++)
                if (this.getElem(i, j) != matTest.getElem(i, j))
                    return false;

        return true;
    }


    /*
     *  Matrix Operations
     */

    /**
     *  Non-destructive transpose of this matrix.
     * 
     *  @return     transposed copy of this matrix or <code>null</code> if error
     */
    public M transpose()  {
        Jama.Matrix impTrans = this.getMatrix().transpose();
        M           matTrans = this.newInstance();
        matTrans.assignMatrix(impTrans);
        
        return matTrans;
    }

    /**
     *  Matrix determinant function.
     *
     *  @return     the determinant of this square matrix
     */
    public double det()     { 
        return this.getMatrix().det(); 
    };

    /**
     *  Non-destructive inverse of this matrix.
     *
     *  @return     the algebraic inverse of this matrix or <code>null</code> if error
     */
    public M inverse()    {
        Jama.Matrix impInv = this.getMatrix().inverse();
        M           matInv = this.newInstance();
        matInv.assignMatrix(impInv);
        
        return matInv;
    }
    
    /**
     * <p>
     * Solves the linear matrix-vector system without destroying the given
     * data vector.  Say the linear system can be represented algebraically
     * as
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>Ax</b> = <b>y</b> ,
     * <br>
     * <br>
     * where <b>A</b> is this matrix, <b>x</b> is the solution matrix to be
     * determined, and <b>y</b> is the data vector provided as the argument.
     * The returned value is equivalent to 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>x</b> = <b>A</b><sup>-1</sup><b>y</b> ,
     * <br>
     * <br>
     * that is, the value of vector <b>x</b>.  
     * <p>
     * </p>
     * The vector <b>y</b> is left
     * unchanged.  However, this is somewhat expensive in that the solution
     * vector must be created through reflection and exceptions may occur.
     * For a safer implementation, but where the solution is returned within the
     * existing data vector <b>y</b> see <code>{@link #solveInPlace(BaseVector)}</code>.
     * </p>
     * <p>
     * Note that the inverse matrix
     * <b>A</b><sup>-1</sup> is never computed, the system is solved in 
     * less than <i>N</i><sup>2</sup> time.  However, if this system is to be
     * solved repeated for the same matrix <b>A</b> it may be preferable to 
     * invert this matrix and solve the multiple system with matrix multiplication.
     * </p>
     * 
     * @param vecObs        the data vector
     * 
     * @return              vector which, when multiplied by this matrix, will equal the data vector
     * 
     * @throws IllegalArgumentException     the argument has the wrong size
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> V solve(final V vecObs) throws IllegalArgumentException {
        
        // Check sizes
        if ( vecObs.getSize() != this.getSize() ) 
            throw new IllegalArgumentException(vecObs.getClass().getName() + " vector must have compatible size");
        
        // Get the implementation matrix.
        Jama.Matrix impL = this.getMatrix();
        
        // Create a Jama matrix for the observation vector 
        Jama.Matrix impObs = new Jama.Matrix(this.getSize(), 1 ,0.0);
        for (int i=0; i<this.getSize(); i++) 
            impObs.set(i,0, vecObs.getElem(i));
        
        // Solve the matrix-vector system in the Jama package
        Jama.Matrix impState = impL.solve(impObs);
        
        V   vecSoln = vecObs.newInstance();
        
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = impState.get(i,  0);
            
            vecSoln.setElem(i,  dblVal);
        }
        
        return vecSoln;
 
    }

    /**
     * <p>
     * Solves the linear matrix-vector system and returns the solution in
     * the given data vector.  Say the linear system can be represented 
     * algebraically as
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>Ax</b> = <b>y</b> ,
     * <br>
     * <br>
     * where <b>A</b> is this matrix, <b>x</b> is the solution matrix to be
     * determined, and <b>y</b> is the data vector provided as the argument.
     * The returned value is equivalent to 
     * <br>
     * <br>
     * &nbsp; &nbsp; <b>x</b> = <b>A</b><sup>-1</sup><b>y</b> ,
     * <br>
     * <br>
     * that is, the value of vector <b>y</b>.  
     * </p>
     * <p> 
     * The value of <b>x</b> is returned within the argument vector.  Thus,
     * the argument cannot be immutable.
     * <p>
     * Note that the inverse matrix
     * <b>A</b><sup>-1</sup> is never computed, the system is solved in 
     * less than <i>N</i><sup>2</sup> time.  However, if this system is to be
     * solved repeated for the same matrix <b>A</b> it may be preferable to 
     * invert this matrix and solve the multiple system with matrix multiplication.
     * </p>
     * 
     * @param vecObs        the data vector on call, the solution vector upon return
     * 
     * @throws IllegalArgumentException     the argument has the wrong size
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> void solveInPlace(V vecObs) throws IllegalArgumentException {
        
        // Check sizes
        if ( vecObs.getSize() != this.getSize() ) 
            throw new IllegalArgumentException(vecObs.getClass().getName() + " vector must have compatible size");
        
        // Get the implementation matrix.
        Jama.Matrix impL = this.getMatrix();
        
        // Create a Jama matrix for the observation vector 
        Jama.Matrix impObs = new Jama.Matrix(this.getSize(), 1 ,0.0);
        for (int i=0; i<this.getSize(); i++) 
            impObs.set(i,0, vecObs.getElem(i));
        
        // Solve the matrix-vector system in the Jama package
        Jama.Matrix impState = impL.solve(impObs);
        
        for (int i=0; i<this.getSize(); i++) {
            double dblVal = impState.get(i,  0);
            
            vecObs.setElem(i,  dblVal);
        }
    }

    //    /**
    //     *  Perform an eigenvalue decomposition of this matrix.
    //     *  If the matrix is symmetric it can be decomposed as
    //     * 
    //     *      A = R*D*R'
    //     * 
    //     *  where A is this matrix, R is an (special) orthogonal matrix
    //     *  in SO(3), and D is the diagonal matrix of eigenvales of A.
    //     * 
    //     * @return  eigen-system decomposition object for R2x2 matrix
    //     */
    //    public R2x2EigenDecomposition   eigenDecomposition()    {
    //        return new R2x2EigenDecomposition( this.getMatrix().eig() );
    //    }
    //



    /*
     *  Algebraic Operations
     */

    /**
     *  Non-destructive scalar multiplication.  This matrix is unaffected.
     *
     *  @param  s   multiplier
     *
     *  @return     new matrix equal to the element-wise product of <i>s</i> and this matrix,
     *                      or <code>null</code> if an error occurred
     */
    public M    times(double s) {
        Jama.Matrix impPrd = this.getMatrix().times(s);
        M           matAns = this.newInstance(impPrd);
        
        return matAns;
    }
    
    /**
     *  In-place scalar multiplication.  Each element of this matrix is replaced
     *  by its product with the argument.
     *
     *  @param  s   multiplier
     */
    public void timesEquals(double s) {
        this.getMatrix().timesEquals(s);
    }
    
    /**
     * <p>
     * Non-destructive matrix-vector multiplication.  The returned value is the
     * usual product of the given vector pre-multiplied by this matrix.  Specifically,
     * denote by <b>A</b> this matrix and by <b>x</b> the argument vector, then
     * the components {<i>y<sub>i</sub></i>} of the returned vector <b>y</b> are given by
     * <br>
     * <br>
     * &nbsp; &nbsp; <i>y</i><sub><i>i</i></sub> = &Sigma;<sub><i>j</i></sub> <i>A<sub>ij</sub>x<sub>j</sbu></i>
     * <br>
     * <br>
     * </p>
     * <p>
     * The returned vector must be created using Java reflection, so this operation
     * is somewhat more risky and expensive than and in place multiplication.
     * </p>
     *  
     * @param vecFac    the vector factor
     * 
     * @return          the matrix-vector product of this matrix with the argument
     * 
     * @throws IllegalArgumentException the argument vector must be the same size
     * 
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public <V extends BaseVector<V>> V times(V vecFac) throws IllegalArgumentException {
        // Check sizes
        if ( vecFac.getSize() != this.getSize() ) 
            throw new IllegalArgumentException(vecFac.getClass().getName() + " vector must have compatible size");
    
//        V   vecSoln = vecFac.newInstance();
        double[]    arrVec = new double[vecFac.getSize()];
        
        for (int i=0; i<this.getSize(); i++) {
            double dblSum = 0.0;

            for (int j=0; j<this.getSize(); j++) {
                double dblFac = this.getElem(i, j)*vecFac.getElem(j);
             
                dblSum += dblFac;
            }
            
            arrVec[i] = dblSum;
//            vecSoln.setElem(i,  dblSum);
        }
        
        V vecSoln   = vecFac.newInstance(arrVec);
        
        return vecSoln;
    }
    
    /**
     *  Non-destructive matrix multiplication.  A new matrix is returned with the
     *  product while both multiplier and multiplicand are unchanged.  
     *
     *  @param  matRight    multiplicand - right operand of matrix multiplication operator
     *
     *  @return             new matrix which is the matrix product of this matrix and the argument,
     *                      or <code>null</code> if an error occurred
     */
    public M    times(M matRight) {
        BaseMatrix<M>   matBase = (BaseMatrix<M>)matRight;
        Jama.Matrix     impMult = matBase.getMatrix();
        Jama.Matrix     impProd = this.getMatrix().times(impMult);
        M               matAns  = this.newInstance(impProd);

        return matAns;
    }
    
    /**
     *  In-place matrix multiplication.  The final value of this matrix is assigned
     *  to be the matrix product of the pre-method-call value time the given matrix.
     *
     *  @param  matMult    multiplicand - right operand of matrix multiplication operator
     */
    public void timesEquals(BaseMatrix<M>   matMult) {
        BaseMatrix<M> matBase = matMult;
        
        this.getMatrix().arrayTimesEquals( matBase.getMatrix() );
    }
    
    
    /**
     *  <p>
     *  Function for transpose conjugation of this matrix by the argument matrix.  
     *  This method is non-destructive, returning a new matrix.
     *  </p>
     *  <p>
     *  Denote by <b>&sigma;</b><sub>0</sub> this matrix object, and denote 
     *  the argument matrix as <b>&Phi;</b>.  Then the returned matrix,
     *  <b>&sigma;</b><sub>1</sub> is given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&sigma;</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>T</i></sup>
     *  <br>
     *  <br> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi^T, or <code>null</code> if an error occurred
     */
    public M    conjugateTrans(M matPhi) {
        Jama.Matrix impPhi  = ((BaseMatrix<M>)matPhi).getMatrix();
        Jama.Matrix impPhiT = impPhi.transpose();
        Jama.Matrix impAns  = impPhi.times( this.getMatrix().times( impPhiT) );
        
        M   matAns = this.newInstance(impAns);
        
        return matAns;
    };
    
    /**
     *  <p>
     *  Function for inverse conjugation of this matrix by the argument matrix.  
     *  This method is non-destructive, return a new matrix.
     *  </p>
     *  <p>
     *  Denote by <b>&sigma;</b><sub>0</sub> this matrix object, and denote 
     *  the argument matrix as <b>&Phi;</b>.  Then the returned matrix,
     *  <b>&sigma;</b><sub>1</sub> is given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&sigma;</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>-1</i></sup>
     *  <br>
     *  <br> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi<sup>-1</sup>
     */
    public M conjugateInv(M matPhi) {  
        Jama.Matrix impPhi = ((BaseMatrix<M>)matPhi).getMatrix();
        Jama.Matrix impInv = impPhi.inverse();
        Jama.Matrix impAns = impPhi.times( this.getMatrix().times( impInv) );
        
        M   matAns = this.newInstance(impAns);
        
        return matAns;
    };
    
    

    /*
     * Child Class Support
     */

    /**
     * Constructor for SquareMatrix.
     * Creates a new, uninitialized instance of a square matrix with the given
     * matrix dimensions. The matrix contains all zeros.
     *
     * @param intSize   size of this square matrix
     * 
     * @throws UnsupportedOperationException  child class has not defined a public, zero-argument constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 14, 2013
     */
    protected SquareMatrix(final int intSize) throws UnsupportedOperationException {
        super(intSize, intSize);
        
        this.intSize = intSize;
    }

    /**
     * Copy constructor for <code>SquareMatrix</code>.  Creates a deep
     * copy of the given object.  The dimensions are set and the 
     * internal array is cloned. 
     *
     * @param matParent     the matrix to be cloned
     *
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     *  
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    protected SquareMatrix(M matParent) throws UnsupportedOperationException {
        super(matParent);
        
        this.intSize = matParent.getSize();
    }
    
    /**
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  intSize     the matrix size of this object
     *  @param  strTokens   token vector of getSize()^2 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    protected SquareMatrix(int intSize, String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        super(intSize, intSize, strTokens);
        
        this.intSize = intSize;
    }
    
    /**
     * <p>
     * Initializing constructor for bases class <code>SquareMatrix</code>.  
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array. Warning! The argument becomes the internal matrix representation
     * and, thus, is not immutable. 
     * </p>
     * <p>
     * The dimensions of the given Java double array must be 
     * consistent with the size of the matrix.  Thus, if the arguments are
     * inconsistent, an exception is thrown.
     * </p>
     * 
     * @param arrMatrix   Java primitive array containing new matrix values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     * @exception  IllegalArgumentException        the argument is degenerate, not fully allocated
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    protected SquareMatrix(double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);

        if (arrVals.length != arrVals[0].length)
            throw new ArrayIndexOutOfBoundsException("The given array is not square " + arrVals);
        
        this.intSize = arrVals.length;
    }



    
//    /**
//     *  <p>
//     *  Constructor for initializing this matrix from a suitable Jama.Matrix. This
//     *  constructor should be called from a corresponding child class constructor.  
//     *  </p>
//     *  </p>
//     *  <p>
//     *  <h4>NOTE</h4>
//     *  The argument should be a new object not owned by another object, because
//     *  the internal matrix representation is assigned to the target argument.
//     *  </p>
//     *
//     *  @param  clsType    the class type of this object
//     *  @param  cntSize    size of the Jama.Matrix
//     *  @param  matInit     an appropriately sized Jama.Matrix object
//     */
//    private BaseMatrix(Class<M> clsType, int cntSize, Jama.Matrix matInit)  {
//        this.clsType  = clsType;
//        this.szMatrix = cntSize;
//        this.matBase  = matInit;
//    }




}
