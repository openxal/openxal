/**
 * Rmxn.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 21, 2015
 */
package xal.tools.math.rn;

import xal.tools.math.BaseMatrix;
import xal.tools.math.BaseVector;

/**
 * Real matrix with arbitrary row and column dimensions.  This class supports the usual
 * matrix operations for non-square matrices but does not optimize for square matrices.
 * Because general matrix dimensions must be considered there is more overhead when
 * using this class over the specialized matrix classes. 
 *
 *
 * @author Christopher K. Allen
 * @since  Jul 21, 2015
 */
public class Rmxn extends BaseMatrix<Rmxn> {

    
    /*
     * Global Methods
     */
    
    /**
     * Creates and returns a new instance of the identity matrix with the
     * given dimensions.
     * 
     * @param cntSize   the size of the identity matrix (i.e., row and column count)
     * @return
     *
     * @since  Jul 23, 2015   by Christopher K. Allen
     */
    public static Rmxn  newIdentity(int cntSize) {
        double  arrInternal[][] = new double[cntSize][cntSize];
        
        for (int i=0; i<cntSize; i++)
            arrInternal[i][i] = 1.0;
        
        Rmxn    matId = new Rmxn(arrInternal);
        
        return matId;
    }
    
    
    /*
     * Initialization
     */
    
    /**
     * Zero-matrix constructor for class <code>Rmxn</code>.  A matrix
     * of the given dimension is created, all the elements are zero.
     * 
     * @param   cntRows     number of matrix rows
     * @param   cntCols     number of matrix columns 
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(int cntRows, int cntCols) {
        super(cntRows, cntCols);
    }

    /**
     * <p>
     * Initializing constructor for class <code>Rmxn</code>.  
     * Initializes the matrix to the values given in the Java primitive type 
     * double array by setting the internal matrix representation to the given
     * Java array.  The matrix is shaped according to the (row-packed) argument. 
     * </p>
     * <p>
     * The dimensions of the given Java double array determine the size of the matrix.
     * An <i>m</i>x<i>n</i> Java double array creates an <i>m</i>x<i>n</i> 
     * <code>Rmxn</code> array.  If the argument is not fully allocated or 
     * inconsistent, an exception is thrown.
     * </p>
     * <p>
     * As an example consider the following Java array
     * <pre>
     * <code>
     * double[][] arrInternal = new double[][] { 
     *                               {1.1, 1.2, 1.3, 1.4, 1.5},
     *                               {2.1, 2.2, 2.3, 2.0, 2.5},
     *                               {3.1, 3.2, 3.3, 3.4, 3.0}
     *                                };
     * </code>
     * </pre>
     * This array would produce a 3&times;5 matrix.  Note that the given argument becomes
     * the internal representation of the matrix object.  Thus, the Java array 
     * <code>arrInternal</code> will be changed by the the encapsulating matrix object
     * so should no longer be referenced after presenting it to this constructor.
     * </p>
     * 
     * @param arrMatrix   Java primitive array to be new internal matrix value representation
     * 
     * @exception  IllegalArgumentException  the argument is degenerate and cannot represent a matrix
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);
    }

    /**
     *  <p>
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.  
     *  </p>
     *  <p>
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *  </p>
     *
     *  @param  cntRows     the matrix row size of this object
     *  @param  cntCols     the matrix column size of this object
     *  @param  strTokens   token vector of getSize()^2 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(int cntRows, int cntCols, String strTokens)
            throws IllegalArgumentException, NumberFormatException 
    {
        super(cntRows, cntCols, strTokens);
    }

    /**
     * Copy constructor for <code>Rmxn</code>.  A new instance is created which
     * is a deep copy of the given argument.
     *
     * @param matTemplate   matrix to be cloned
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(Rmxn matTemplate) {
        super(matTemplate);
    }


    /*
     * Algebraic Operations
     */
    
    /**
     *
     * @see xal.tools.math.BaseMatrix#plus(xal.tools.math.BaseMatrix)
     *
     * @throws IllegalArgumentException     Inconsistent matrix dimensions
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    public Rmxn plus(Rmxn matAddend) {
        this.checkEqualDimensions(matAddend);
        return super.plus(matAddend);
    }

    /**
     *
     * @see xal.tools.math.BaseMatrix#plusEquals(xal.tools.math.BaseMatrix)
     * 
     * @throws IllegalArgumentException     Inconsistent matrix dimensions
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    public void plusEquals(Rmxn matAddend) {
        this.checkEqualDimensions(matAddend);
        super.plusEquals(matAddend);
    }

    /**
     *
     * @see xal.tools.math.BaseMatrix#minus(xal.tools.math.BaseMatrix)
     *
     * @throws IllegalArgumentException     Inconsistent matrix dimensions
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    public Rmxn minus(Rmxn matSub) {
        this.checkEqualDimensions(matSub);
        return super.minus(matSub);
    }

    /**
     *
     * @see xal.tools.math.BaseMatrix#minusEquals(xal.tools.math.BaseMatrix)
     *
     * @throws IllegalArgumentException     Inconsistent matrix dimensions
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    public void minusEquals(Rmxn matSub) {
        this.checkEqualDimensions(matSub);
        super.minusEquals(matSub);
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
    public Rmxn times(Rmxn matRight) {
        this.checkInternalDimensions(matRight);
        
        Jama.Matrix     impRight = matRight.getMatrix();
        Jama.Matrix     impProd  = this.getMatrix().times( impRight);
        Rmxn            matProd  = this.newInstance(impProd);

        return matProd;
    }
    
    /**
     *  In-place matrix multiplication.  The final value of this matrix is assigned
     *  to be the matrix product of the pre-method-call value time the given matrix.
     *
     *  @param  matMult    multiplicand - right operand of matrix multiplication operator
     */
    public void timesEquals(Rmxn   matMult) {
        this.getMatrix().arrayTimesEquals( matMult.getMatrix() );
    }
    
    /**
     * <p>
     * Non-destructive matrix-vector multiplication.  The returned value is the
     * usual product of the given vector pre-multiplied by this matrix.  Specifically,
     * denote by <b>A</b> this matrix and by <b>x</b> the argument vector, then
     * the components {<i>y<sub>i</sub></i>} of the returned vector <b>y</b> are given by
     * <br/>
     * <br/>
     * &nbsp; &nbsp; <i>y</i><sub><i>i</i></sub> = &Sigma;<sub><i>j</i></sub> <i>A<sub>ij</sub>x<sub>j</sbu></i>
     * <br/>
     * <br/>
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
     * @throws IllegalArgumentException the argument vector must have compatible dimensions
     * 
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    public Rn times(Rn vecFac) throws IllegalArgumentException {
        // Check sizes
        if ( vecFac.getSize() != this.getColCnt() ) 
            throw new IllegalArgumentException(vecFac.getClass().getName() + " vector must have compatible size");
    
        // Create solution vector of appropriate size
        Rn   vecSoln = new Rn(this.getRowCnt());
        
        // Perform matrix-vector multiplication
        for (int i=0; i<this.getRowCnt(); i++) {
            double dblSum = 0.0;

            for (int j=0; j<this.getColCnt(); j++) {
                double dblFac = this.getElem(i, j)*vecFac.getElem(j);
             
                dblSum += dblFac;
            }
            
            vecSoln.setElem(i,  dblSum);
        }
        
        return vecSoln;
    }
    
    
    /*
     * Abstract Methods
     */
    
    /**
     *
     * @see xal.tools.math.BaseMatrix#clone()
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @Override
    public Rmxn clone() {
        return new Rmxn(this);
    }

    /**
     * Returns a new, zero element, instance of <code>Rmxn</code> which has the
     * same dimensions as this object.  That is, the returned object is in 
     * the same equivalence class of matrices as this one.
     *
     * @see xal.tools.math.BaseMatrix#newInstance()
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @Override
    protected Rmxn newInstance() {
        int     cntRows = this.getRowCnt();
        int     cntCols = this.getColCnt();
        
        Rmxn    matNew = new Rmxn(cntRows, cntCols);
        
        return matNew;
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Checks that the given matrix's dimensions are equal to the
     * dimensions of this matrix.
     *  
     * @param matTest   matrix under test
     * 
     * @throws IllegalArgumentException     the given matrix's dimensions are not equal
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    private void checkEqualDimensions(Rmxn matTest) throws IllegalArgumentException {
        int cntRowsTest = matTest.getRowCnt();
        int cntColsTest = matTest.getColCnt();
        
        if (this.getRowCnt() != cntRowsTest || this.getColCnt() != cntColsTest)
            throw new IllegalArgumentException("Unequal matrix dimensions");
    }
    
    /**
     * Checks that the given matrix dimensions are suitable for pre-multiplication
     * by this matrix.
     * 
     * @param matTest   matrix under test
     * 
     * @throws IllegalArgumentException the given matrix row count is different than this matrix's column count
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    private void checkInternalDimensions(Rmxn matTest) throws IllegalArgumentException {
        int cntColThis = this.getColCnt();
        int cntRowTest = matTest.getRowCnt();
        
        if (cntColThis != cntRowTest)
            throw new IllegalArgumentException("Inconsistent internal dimensions");
    }

}
