/**
 * BaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2013
 */
package xal.tools.math;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

import Jama.Matrix;

/**
 * <p>
 * Class <code>SquareMatrix</code> is the abstract base class for matrix
 * objects supported in the XAL tools packages.
 * </p>
 * <p>
 * Currently the internal matrix operations are supported by the <tt>Jama</tt>
 * matrix package.  However, the Jama matrix package has been deemed a 
 * "proof of principle" for the Java language and scientific computing and 
 * is, thus, no longer supported.  The objective of this base class is to hide
 * the internal implementation of matrix operations from the child classes and
 * all developers using the matrix packages.  If it is determined that the Jama
 * matrix package is to be removed from XAL, the modification will be substantially
 * simplified in the current architecture.
 * </p> 
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2013
 */
public abstract class SquareMatrix<M extends SquareMatrix<M>>  {


    /*
     * Internal Classes
     */

    /**
     * Interface <code>BaseMatrix.Ind</code> is exposed by objects
     * representing matrix indices.  In particular, the <code>enum</code>
     * types that are matrix indices expose this interface.
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    protected interface IIndex {

        /**
         * Returns the value of this matrix index object.
         * 
         * @return  the numerical index represented by this object 
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        public int val();
    }

    
    /*
     *  Local Attributes
     */

    /** class type of child class */
    private final Class<M>          clsType;
        
    /** zero-argument constructor for this type */
    private final Constructor<M>    ctrType;

    /** size of the the square matrix */
    private final int               intSize;

    /** internal matrix implementation */
    private final Jama.Matrix       matImpl;




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
    public void setElem(IIndex iRow, IIndex iCol, double dblVal)   {
        this.getMatrix().set(iRow.val(), iCol.val(), dblVal);
    }

    /**
     *  Element assignment - assigns matrix element to the specified value
     *
     *  @param  i       row index
     *  @param  j       column index
     *  @parm   s       new matrix element value
     *
     *  @exception  ArrayIndexOutOfBoundsException  an index was equal to or larger than the matrix size
     */
    public void setElem(int i, int j, double s) throws ArrayIndexOutOfBoundsException {

        this.getMatrix().set(i,j, s);
    }

    /**
     *  Set a block sub-matrix within the current matrix.  If the given two-dimensional
     *  array is larger than block described by the indices it is truncated. If the
     *  given indices describe a matrix larger than the given two-dimensional array
     *  then an exception is thrown. 
     *
     *  @param  i0      row index of upper left block
     *  @param  i1      row index of lower right block
     *  @param  j0      column index of upper left block
     *  @param  j1      column index of lower right block
     *  @param  arrSub  two-dimensional sub element array
     *
     *  @exception  ArrayIndexOutOfBoundsException  sub-matrix does not fit into base matrix
     */
    public void setSubMatrix(int i0, int i1, int j0,  int j1, double[][] arrSub) throws ArrayIndexOutOfBoundsException  {
        Jama.Matrix matSub = new Matrix(arrSub);

        this.getMatrix().setMatrix(i0,i1,j0,j1, matSub);
    }
    
    /**
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array.
     * 
     * @param arrMatrix Java primitive array containing new matrix values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public void setMatrix(double[][] arrMatrix) throws ArrayIndexOutOfBoundsException {
        
        // Check the dimensions of the argument double array
        if (this.getSize() != arrMatrix.length  ||  arrMatrix[0].length != this.getSize() )
            throw new ArrayIndexOutOfBoundsException(
                    "Dimensions of argument do not correspond to size of this matrix = " 
                   + this.getSize()
                   );
        
        // Set the elements of this array to that given by the corresponding 
        //  argument entries
        for (int i=0; i<this.getSize(); i++) 
            for (int j=0; j<this.getSize(); j++) {
                double dblVal = arrMatrix[i][j];
                
                this.setElem(i, j, dblVal);
            }
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

    /**
     * Returns a copy of the internal Java array containing
     * the matrix elements.  The array dimensions are given by
     * the size of this matrix, available from 
     * <code>{@link #getSize()}</code>.  
     * 
     * @return  copied array of matrix values
     *
     * @author Christopher K. Allen
     * @since  Sep 25, 2013
     */
    public double[][]   getArrayCopy() {
        return this.matImpl.getArrayCopy();
    }

    /**
     * Create a deep copy of the this matrix object.  The returned 
     * object is completely decoupled from the original.
     * 
     * @return  a deep copy object of this matrix
     * 
     * @throws InstantiationException error in new object construction
     */
    public M copy() throws InstantiationException   {

        M  matClone = this.newInstance();
        ((SquareMatrix<M>)matClone).setMatrix( this.matImpl );
            
        return matClone;
    }

    /**
     *  Return matrix element value.  Get matrix element value at specified 
     *  <code>Diagonal</code> position.
     *
     *  @param  i       row index
     *  @param  j       column index
     *
     *  @exception  ArrayIndexOutOfBoundsException  an index was equal to or larger than the matrix size
     */
    public double getElem(int i, int j)   throws ArrayIndexOutOfBoundsException {

        return this.getMatrix().get(i,j);
    }
    
    
    /*
     * Matrix Properties
     */
    
    /**
     *  Matrix determinant function.
     *
     *  @return     the determinant of this square matrix
     */
    public double det()     { 
        return this.getMatrix().det(); 
    };

    /**
     *  Check if matrix is symmetric.  
     * 
     *  @return true if matrix is symmetric 
     */
    public boolean isSymmetric()   {
    
        for (int i=0; i<this.getSize(); i++)
            for (int j=i; j<this.getSize(); j++) {
                if (getElem(i,j) != getElem(j,i) )
                    return false;
            }
        return true;
    }




    /*
     *  Object method overrides
     */

    /**
     * Checks absolute equivalency.  That is, checks whether or not the
     * argument is this object.
     * 
     * @param   objTest     object under equivalency test
     * 
     * @return              <code>true</code> if the argument is this object,
     *                      <code>false</code> otherwise
     */
    @Override
    public boolean equals(Object objTest) {
        boolean bResult = this.equals(objTest);
        
        return bResult;
    } 

    /**
     *  Convert the contents of the matrix to a string representation.
     *  The format is similar to that of Mathematica. Specifically,
     *  <br/>
     *  <br/>
     *      { {a b }{c d } }
     *  <br/>
     *
     *  @return     string representation of the matrix
     */
    @Override
    public String   toString()  {
        // double is 15 significant digits plus the spaces and brackets
        final int size = (this.getSize()*this.getSize() * 16) + (this.getSize()*2) + 4; 
        StringBuffer strBuf = new StringBuffer(size);

        synchronized(strBuf) { // get lock once instead of once per append
            strBuf.append("{ ");
            for (int i=0; i<this.getSize(); i++) {
                strBuf.append("{ ");
                for (int j=0; j<this.getSize(); j++) {
                    strBuf.append(this.getElem(i,j));
                    strBuf.append(" ");
                }
                strBuf.append("}");
            }
            strBuf.append(" }");
        }

        return strBuf.toString();
    }

    /**
     * "Borrowed" implementation from AffineTransform, since it is based on
     * double attribute values.  Must implement hashCode to be consistent with
     * equals as specified by contract of hashCode in <code>Object</code>.
     * 
     * @return a hashCode for this object
     */
    @Override
    public int hashCode() {
        long bits = 0;
        for (int i=0; i<this.getSize(); i++) {
            for (int j= 0; j<this.getSize(); j++) {
                bits = bits * 31 + Double.doubleToLongBits(getElem(i,j));;
            }
        }

        return (((int) bits) ^ ((int) (bits >> 32)));
    }           



    /*
     *  Matrix Operations
     */


    /**
     *  Assign this matrix to be the zero matrix, specifically
     *  the matrix containing all 0's. 
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignZero() {
        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++)
                this.setElem(i, j, 0.0);
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
    
    /**
     *  Non-destructive transpose of this matrix.
     * 
     *  @return     transposed copy of this matrix or <code>null</code> if error
     */
    public M transpose()  {
        try {
            
            Jama.Matrix impTrans = this.getMatrix().transpose();
            M           matTrans = this.newInstance();
            ((SquareMatrix<M>)matTrans).setMatrix(impTrans);
            
            return matTrans;
            
        } catch (InstantiationException e) {
            
            return null;
        }
    }

    /**
     *  Non-destructive inverse of this matrix.
     *
     *  @return     the algebraic inverse of this matrix or <code>null</code> if error
     */
    public M inverse()    {
        try {
            
            Jama.Matrix impInv = this.getMatrix().inverse();
            M           matInv = this.newInstance();
            ((SquareMatrix<M>)matInv).setMatrix(impInv);
            
            return matInv;
            
        } catch (InstantiationException e) {
            
            return null;
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
    public boolean isEquivalentTo(M matTest) {
        if ( !this.getClass().equals(matTest.getClass()) )
            return false;

        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++)
                if (this.getElem(i, j) != matTest.getElem(i, j))
                    return false;

        return true;
    }


    /**
     *  Non-destructive matrix addition. This matrix is unaffected.
     *
     *  @param  matAddend     matrix to be added to this
     *
     *  @return         the result of this matrix plus the given matrix (element-wise), 
     *                  or <code>null</code> if error
     */
    public M plus(M matAddend)   {
        try {
            Jama.Matrix    impAdd = ((SquareMatrix<M>)matAddend).getMatrix();
            Jama.Matrix    impSum = this.getMatrix().plus( impAdd );
            M              matAns = this.newInstance(impSum);

            return matAns;

        } catch (InstantiationException e) {

            return null;
        }
    }     
    
    /**
     *  In-place matrix addition. The given matrix is added to this matrix 
     *  algebraically (element by element).
     *
     *  @param  mat     matrix to be added to this (no new objects are created)
     */
    public void plusEquals(M  mat)    {
        SquareMatrix<M>     matBase = (SquareMatrix<M>)mat;
        
        this.getMatrix().plusEquals( matBase.getMatrix() );
    }

    /**
     *  Non-destructive matrix subtraction.  This matrix is unaffected.
     *
     *  @param  matSub     the subtrahend 
     *
     *  @return         the value of this matrix minus the value of the given matrix,
     *                      or <code>null</code> if an error occurred
     */
    public M  minus(M matSub)   {
        try {
            Jama.Matrix    impSub = ((SquareMatrix<M>)matSub).getMatrix();
            Jama.Matrix    impDif = this.getMatrix().minus( impSub );
            M              matAns = this.newInstance(impDif);

            return matAns;

        } catch (InstantiationException e) {

            return null;
        }
    }
    
    /**
     *  In-place matrix subtraction.  The given matrix is subtracted from the
     *  value of this matrix.  No additional objects are created.
     *
     *  @param  mat     subtrahend
     */
    public void minusEquals(M mat)    {
        SquareMatrix<M> matBase = (SquareMatrix<M>)mat;
        
        this.getMatrix().minusEquals( matBase.getMatrix() );
    }
    
    /**
     *  Non-destructive scalar multiplication.  This matrix is unaffected.
     *
     *  @param  s   multiplier
     *
     *  @return     new matrix equal to the element-wise product of <i>s</i> and this matrix,
     *                      or <code>null</code> if an error occurred
     */
    public M    times(double s) {
        try {
            Jama.Matrix impPrd = this.getMatrix().times(s);
            M           matAns = this.newInstance(impPrd);
            
            return matAns;
            
        } catch (InstantiationException e) {
            
            return null;
        }
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
     *  Non-destructive matrix multiplication.  A new matrix is returned with the
     *  product while both multiplier and multiplicand are unchanged.  
     *
     *  @param  matRight    multiplicand - right operand of matrix multiplication operator
     *
     *  @return             new matrix which is the matrix product of this matrix and the argument,
     *                      or <code>null</code> if an error occurred
     */
    public M    times(M matRight) {
        try {
            SquareMatrix<M> matBase = (SquareMatrix<M>)matRight;
            Jama.Matrix     impMult = matBase.getMatrix();
            Jama.Matrix     impProd = this.getMatrix().times( impMult);
            M               matAns  = this.newInstance(impProd);

            return matAns;

        } catch (InstantiationException e) {

            return null;
        }
    }
    
    /**
     *  In-place matrix multiplication.  The final value of this matrix is assigned
     *  to be the matrix product of the pre-method-call value time the given matrix.
     *
     *  @param  matMult    multiplicand - right operand of matrix multiplication operator
     */
    public void timesEquals(M   matMult) {
        SquareMatrix<M> matBase = (SquareMatrix<M>)matMult;
        
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
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; <b>&sigma;</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>T</i></sup>
     *  <br/>
     *  <br/> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi^T, or <code>null</code> if an error occurred
     */
    public M    conjugateTrans(M matPhi) {
        try {
            Jama.Matrix impPhi  = ((SquareMatrix<M>)matPhi).getMatrix();
            Jama.Matrix impPhiT = impPhi.transpose();
            Jama.Matrix impAns  = impPhi.times( this.getMatrix().times( impPhiT) );
            
            M   matAns = this.newInstance(impAns);
            
            return matAns;
            
        } catch (InstantiationException e) {
            
            return null;
        }
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
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; <b>&sigma</b><sub>1</sub> = <b>&Phi;</b><b>&sigma;</b><sub>0</sub><b>&Phi;</b><sup><i>-1</i></sup>
     *  <br/>
     *  <br/> 
     *  </p>
     *
     *  @param  matPhi      conjugating matrix <b>&Phi;</b> (typically a transfer matrix)
     *
     *  @return             matPhi*this*matPhi^-1
     */
    public M conjugateInv(M matPhi) {
        try {
            Jama.Matrix impPhi = ((SquareMatrix<M>)matPhi).getMatrix();
            Jama.Matrix impInv = impPhi.inverse();
            Jama.Matrix impAns = impPhi.times( this.getMatrix().times( impInv) );
            
            M   matAns = this.newInstance(impAns);
            
            return matAns;
            
        } catch (InstantiationException e) {
            
            return null;
        }
    };
    
    
    
    /*
     *  Topological Operations
     */
    
    /**
     * <p>
     * Return the maximum absolute value of all matrix elements.  This can
     * be considered a norm on matrices, but it is not sub-multiplicative.
     * That is,
     * <br/>
     * <br/>
     * ||<b>AB</b>||<sub>max</sub> is not necessarily bound by ||<b>A</b>||<sub>max</sub> ||<b>B</b>||<sub>max</sub> .    
     * <br/>
     * <br/>
     * </p>
     * 
     * @return  max<sub><i>i,j</i></sub> | <b>A</b><sub><i>i,j</i></sub> | 
     */
    public double   max()   {
        double      val = 0.0;
        double      max = Math.abs(getElem(0,0));
        
        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++) {
                val = Math.abs( getElem(i,j) );
                if (val > max)
                    max = val;
            }
      
        return max;
    }
    
    /**
     *  <p>
     *  The matrix norm || &middot; ||<sub>1</sub> <b>induced</b> from 
     *  the <i>l</i><sub>1</sub> vector norm on <b>R</b><sup><i>n</i></sup>.  That is,
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>1</sub> &equiv; max<sub><b>x</b>&in;<b>R</b><sup><i>n</i></sup></sub> ||<b>Ax</b>||<sub>1</sub>
     *  <br/>
     *  <br/>
     *  where, by context, the second occurrence of ||&middot;||<sub>1</sub> is the 
     *  Lesbeque 1-norm on <b>R</b><sup><i>n</i><sup>. 
     *  </p>
     *  <p>
     *  <h4>NOTES:</h4>
     *  &middot; For square matrices induced norms are sub-multiplicative, that is
     *  ||<b>AB</b>|| &le; ||<b>A</b>|| ||<b>B</b>||.
     *  <br/>
     *  <br/>
     *  &middot; The ||&middot;||<sub>1</sub> induced norm equates to the 
     *  the maximum absolute column sum.
     *  </p>
     *
     *  @return     ||<b>M</b>||<sub>1</sub> = max<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> |<i>M<sub>i,j</i></sub>|
     */
    public double   norm1()     { return this.getMatrix().norm1(); };
    
    /**
     *  <p>
     *  Returns the <i>l</i><sub>2</sub> induced norm of this matrix, 
     *  which is the maximum, which turns out to be the spectral radius
     *  of the matrix. Specifically,
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>2</sub> &equiv; [ max &lambda;(<b>A</b><sup><i>T</i></sup><b>A</b>) ]<sup>1/2</sup> ,
     *  <br/>
     *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = max &rho;(<b>A</b>) ,                                 
     *  <br/>
     *  <br/>
     *  where &lambda;(&middot;) is the eigenvalue operator and &rho;(&middot;) is the 
     *  singular value operator.
     *  </p>
     *
     *  @return     the maximum singular value of this matrix
     */
    public double   norm2()     { return this.getMatrix().norm2(); };
    
    /**
     *  <p>
     *  The matrix norm || &middot; ||<sub>&infin;</sub> <b>induced</b> from 
     *  the <i>l</i><sub>&infin;</sub> vector norm on <b>R</b><sup><i>n</i></sup>.  That is,
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>&infin;</sub> &equiv; max<sub><b>x</b>&in;<b>R</b><sup><i>n</i></sup></sub> 
     *                                                      ||<b>Ax</b>||<sub>&infin;</sub>
     *  <br/>
     *  <br/>
     *  where, by context, the second occurrence of ||&middot;||<sub>&infin;</sub> is the 
     *  Lesbeque &infin;-norm on <b>R</b><sup><i>n</i><sup>. 
     *  </p>
     *  <p>
     *  <h4>NOTES:</h4>
     *  &middot; For square matrices induced norms are sub-multiplicative, that is
     *  ||<b>AB</b>|| &le; ||<b>A</b>|| ||<b>B</b>||.
     *  <br/>
     *  <br/>
     *  &middot; The ||&middot;||<sub>&infin;</sub> induced norm equates to the 
     *  the maximum absolute column sum.
     *  </p>
     *
     *  @return     ||<b>M</b>||<sub>1</sub> = max<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> |<i>M<sub>i,j</i></sub>|
     */
    public double   normInf()   { return this.getMatrix().normInf(); };
    
    /**
     * <p>
     * Return the Frobenius norm ||<b>A</b>||<sub><i>F</i></sub> . 
     * The Frobenius norm has the property that it is 
     * both the element-wise Lebesgue 2-norm the Schatten 2-norm.  Thus we have
     * <br/>
     * <br/>
     * &nbsp; &nbsp; ||<b>A</b>||<sub><i>F</i></sub> = [ &Sigma;<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> <i>A</i><sub><i>i,j</i></sub><sup>2</sup> ]<sup>1/2</sup>
     *                  = [ Tr(<b>A</b><sup><i>T</i></sup><b>A</b>) ]<sup>1/2</sup> 
     *                  = [ &Sigma;<sub><i>i</i></sub> &sigma;<sub><i>i</i></sub><sup>2</sup> ]<sup>1/2</sup>
     * <br/>
     * <br/>
     * where Tr is the trace operator and &sigma;<sub><i>i</i></sub> are the singular values of
     * matrix <b>A</b>.  
     * </p>
     * <p>
     * <h4>NOTES</h4>
     * &middot; Since the Schatten norms are sub-multiplicative, the Frobenius norm
     * is sub-multiplicative.
     * <br/>
     * <br/>
     * &middot; The Frobenius norm is invariant under rotations by elements of 
     * <i>O</i>(2) &sub; <b>R</b><sup><i>n</i>&times;<i>n</i></sup> .
     * </p>
     * 
     * 
     *  @return     ||<b>A</b>||<sub><i>F</i></sub> = [ &Sigma;<sub><i>i,j</i></sub> <i>A<sub>ij</sub></i><sup>2</sup> ]<sup>1/2</sup>
     */
    public double   normF()     { 
        return this.getMatrix().normF(); 
    };
    
    
    


    /*
     *  Testing and Debugging
     */

    /**
     *  Print out the contents of the R2x2 in text format.
     *
     *  @param  os      output stream to receive text dump
     */
    public void print(PrintWriter os)   {
        this.matImpl.print(os, new DecimalFormat("0.#####E0"), this.getSize());
    }



    /*
     * Child Class Support
     */

    /**
     * <p>
     * Returns the matrix element at the position indicated by the
     * given row and column index sources.  
     * </p>
     * <p>
     * <h4>NOTES</h4>
     * &middot; It is expected that the
     * object exposing the <code>IIndex</code> interface is an enumeration
     * class restricting the number of possible index values.
     * <br/>
     * &middot; Consequently we do not declare a thrown exception assuming
     * that that enumeration class eliminates the possibility of an out of
     * bounds error.
     * </p>
     *  
     * @param indRow        source of the row index
     * @param indCol        source of the column index
     * 
     * @return          value of the matrix element at the given row and column
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2013
     */
    protected double getElem(IIndex indRow, IIndex indCol) {
        double  dblVal = this.matImpl.get(indRow.val(), indCol.val());

        return dblVal;
    }
    
    /** 
     * Creates a new, uninitialized instance of a square matrix with the given
     * size. The matrix contains all zeros.
     *  
     * @param  intSize     the matrix size of this object
     *  
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     */
    @SuppressWarnings("unchecked")
    protected SquareMatrix(int intSize) throws UnsupportedOperationException {
        
        try {
            this.clsType = (Class<M>) this.getClass();
            
            this.ctrType = this.clsType.getConstructor();
            this.intSize = intSize;
            this.matImpl = new Jama.Matrix(intSize, intSize, 0.0);
            
        } catch (NoSuchMethodException | SecurityException e) {
            
            throw new UnsupportedOperationException("Could not find public, zero-argument constructor for " 
                    + this.clsType.getName()
                    );
        }
    }

    /**
     * Copy constructor for <code>BaseMatrix</code>.  Creates a deep
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
        this(matParent.getSize());
        
        SquareMatrix<M> matBase = (SquareMatrix<M>)matParent;
        this.setMatrix(matBase.getMatrix()); 
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
        this(intSize);
        
        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strTokens, " ,()[]{}");
        
        if (tokArgs.countTokens() != this.getSize()*this.getSize())
            throw new IllegalArgumentException("SquareMatrix, wrong number of token in string initializer: " + strTokens);
        
        
        // Extract initial phase coordinate values
        
        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++) {
                String  strVal = tokArgs.nextToken();
                double  dblVal = Double.valueOf(strVal).doubleValue();
            
                this.setElem(i,j, dblVal);
            }
    }
    
    /**
     * <p>
     * Initializing constructor for bases class <code>SquareMatrix</code>.  
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array. The argument itself remains unchanged. 
     * </p>
     * <p>
     * The dimensions of the given Java double array must be 
     * consistent with the size of the matrix.  Thus, if the arguments are
     * inconsistent, an exception is thrown.
     * </p>
     * 
     * @param intSize     the matrix size of this object
     * @param arrMatrix Java primitive array containing new matrix values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    protected SquareMatrix(int intSize, double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        this(intSize);
        
        this.setMatrix(arrVals);;
    }


    /*
     * Internal Support
     */

    /**
     *  Return the internal matrix representation.
     *
     *  @return     the Jama matrix object
     */
    private Jama.Matrix getMatrix()   { 
        return matImpl; 
    };
    
    /**
     * Sets the internal matrix value to that given in the argument. This
     * is a deep copy operation.
     * 
     * @param matValue  internal implementation of matrix values
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    private void setMatrix(Jama.Matrix matValue) {
        for (int i=0; i<this.getSize(); i++)
            for (int j=0; j<this.getSize(); j++) {
                double dblVal = matValue.get(i, j);
                
                this.matImpl.set(i, j, dblVal);
            }
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
    
    /**
     * Creates a new, uninitialized instance of this matrix type.
     * 
     * @return  uninitialized matrix object of type <code>M</code>
     * 
     * @throws InstantiationException   error occurred in the reflection constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    private M newInstance() throws InstantiationException {
        try {
            M matNewInst = this.ctrType.newInstance();

            return matNewInst;

        } catch (InstantiationException   | 
                IllegalAccessException   | 
                IllegalArgumentException | 
                InvocationTargetException e) {

            throw new InstantiationException("Unable to copy matrix " + this.getClass().getName());
        }
    }

    /**
     * Creates a new instance of this matrix type initialized to the given
     * implementation matrix.
     * 
     * @param   impInit implementation matrix containing initialization values    
     * 
     * @return          initialized matrix object of type <code>M</code>
     * 
     * @throws InstantiationException   error occurred in the reflection constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    private M newInstance(Jama.Matrix impInit) throws InstantiationException {
        
        M   matNewInst = this.newInstance();
        
        ((SquareMatrix<M>)matNewInst).setMatrix(impInit);
        
        return matNewInst;
    }



}
