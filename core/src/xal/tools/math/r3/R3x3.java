/*
 * R3x3.java
 *
 * Created on September 16, 2003, 2:32 PM
 * Modified:
 * 
 */

package xal.tools.math.r3;


import java.util.EnumSet;

import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r6.R6x6;



/**
 *  <p>
 *  Represents an element of <b>R</b><sup>3&times;3</sup>, the set of real, 
 *  3&times;3 matrices.
 *  The class also contains the usual set of matrix operations and linear
 *  transforms on <b>R</b><sup>3</sup> induced by the matrix.  
 *  </p>
 *
 * @author  Christopher Allen
 *
 *  @see    xal.tools.r3.R3
 */

public class R3x3 extends SquareMatrix<R3x3> implements java.io.Serializable {
    
    
    /*
     * Internal Types
     */
    
    /**
     * Class <code>R3x3.IND</code> is an enumeration of the matrix indices
     * for the <code>R3x3</code> class.
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public enum IND implements IIndex {
        
        /** the <i>x</i> axis index of <b>R</b><sup>3</sup> */
        X(0),

        /** the <i>y</i> axis index of <b>R</b><sup>3</sup> */
        Y(1),
        
        /** the <i>z</i> axis index of <b>R</b><sup>3</sup> */
        Z(2);

        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the <code>R3x3</code> matrix index that this
         * enumeration constant represents.
         * 
         * @return      matrix index
         *
         * @see xal.tools.math.SquareMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since  Oct 4, 2013
         */
        @Override
        public int val() {
            return this.index;
        }

        /*
         * Internal Attributes
         */
        
        /** the matrix index value */
        private final int       index;
        
        /*
         * Initialization
         */

        /**
         * Constructor for IND, initializes the enumeration constant
         * index to the given value.
         *
         * @param index     matrix index for this constant
         *
         * @author Christopher K. Allen
         * @since  Oct 4, 2013
         */
        private IND(int index) {
            this.index = index;
        }

    }
    
    /** 
     * Enumeration for the element positions of an 
     * <code>R3x3</code> matrix element.  Also provides some 
     * convenience functions for accessing these <code>R3x3</code> 
     * elements.
     * 
     * @author  Christopher K. Allen
     */
    public enum POS  {
        
        /*
         * Enumeration Constants
         */
        XY (0,1), 
        XZ (0,2), 
        YZ (1,2),
        XX (0,0),
        YY (1,1),
        ZZ (2,2),
        YX (1,0),
        ZX (2,0),
        ZY (2,1);
                    
        
        /*
         * Local Attributes
         */
        
        /** row index */
        private final int i;
        
        /** column index */
        private final int j;
        
        
        /*
         * Initialization
         */
        
        
        /** 
         * Default enumeration constructor 
         */
        POS(int i, int j)  {
            this.i = i;
            this.j = j;
        }
        
        /** return the row index of the matrix position */
        public int row()    { return i; };

        /** return the column index of the matrix position */
        public int col()    { return j; };

        
        /** 
         * Return the <code>Position</code> object representing the 
         * transpose element of this position.
         * 
         * NOTE:
         * The current implementation is slow.
         * 
         * @return  the transpose position of the current position
         */ 
        public POS transpose() { 
            int i = this.col();
            int j = this.row();
            
            for (POS pos : POS.values()) {
                if (pos.row() == i && pos.col() == j)
                    return pos;
            }

            return null;
        };
        
        
        
        /*
         * Enumerating Positions
         */
        
        /** 
         *  Returns the set of all element positions above the matrix 
         *  diagonal.
         *  
         *  @return     set of upper triangle matrix positions
         */
        public static EnumSet<POS> getUpperTriangle() { return EnumSet.of(XY, XZ, YZ); };
        
        /**
         * Return the set of all matrix element positions along the 
         * diagonal.
         * 
         * @return      set of diagonal element positions
         */
        public static EnumSet<POS> getDiagonal()      { return EnumSet.of(XX, YY, ZZ); };
        
        /**
         * Return the set of all element positions below the matrix
         * diagonal.
         * 
         * @return      set of lower triangle matrix positions
         */
        public static EnumSet<POS> getLowerTriangle() { return EnumSet.of(YX, ZX, ZY); };
        
        /**
         * Return the set of all off-diagonal matrix positions.
         * 
         * @return      set of off diagonal positions, both upper and lower.
         */
        public static EnumSet<POS> getOffDiagonal() { return EnumSet.complementOf(POS.getDiagonal()); };
        
        
        
        /*
         * Matrix Element Accessing
         */
        
        /** 
         * Return the matrix element value for this position
         * 
         * @param   matTarget   target matrix
         * @return              element value for this position
         */
        public double   getValue(R3x3 matTarget)    { return matTarget.getElem(row(),col()); };

        /**
         * Get the diagonal element in the same row as this element position.
         * 
         * @param matTarget     target matrix
         * @return              row diagonal element value
         */
        public double   getRowDiag(R3x3 matTarget)  { return matTarget.getElem(row(),row()); };
        
        /**
         * Get the diagonal element in the same column as this element position.
         * 
         * @param matTarget     target matrix
         * @return              column diagonal element value
         */
        public double   getColDiag(R3x3 matTarget)  { return matTarget.getElem(col(),col()); };
        
        
        /*
         * Matrix Element Assignment 
         */
        
        /** 
         * Set matrix element value for this position
         * 
         * @param   matTarget   target matrix
         * @param   s           new value for matrix element
         */
        public void setValue(R3x3 matTarget, double s)    { matTarget.setElem(row(),col(), s); };

        /**
         * Set the diagonal element in the same row as this element position.
         * 
         * @param matTarget     target matrix
         * @param s             new value for matrix element
         */
        public void setRowDiag(R3x3 matTarget, double s)  { matTarget.setElem(row(),row(), s); };

        /**
         * Set the diagonal element in the same column as this element position.
         * 
         * @param matTarget     target matrix
         * @param s             new value for matrix element
         */
        public void setColDiag(R3x3 matTarget, double s)  { matTarget.setElem(col(),col(), s); };
    };

    

    
    /*
     *  Global Constants
     */
     
     /** serialization version identifier */
    private static final long serialVersionUID = 1L;

//    /** index of x position */
//     public static final int    IND_X = 0;
//     
//     /** index of y position */
//     public static final int    IND_Y = 1;
//     
//     /** index of z position */
//     public static final int    IND_Z = 2;
     
     
     /** number of dimensions (DIM=3) */
     public static final int    INT_SIZE = 3;
     
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero matrix.
     *
     *  @return         matrix whose elements are all zero
     */
    public static R3x3  newZero()   {
        R3x3    matZero = new R3x3();
        matZero.assignZero();
        
        return matZero;
    }
    
    /**
     *  Create a new instance of the identity matrix
     *
     *  @return         identity matrix object
     */
    public static R3x3  newIdentity()   {
        R3x3    matIden = new R3x3();
        matIden.assignIdentity();
        
        return matIden;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the x axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  x-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationX(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRx = R3x3.newIdentity();

        matRx.setElem(POS.YY,  cos);
        matRx.setElem(POS.YZ, -sin);
        matRx.setElem(POS.ZY,  sin);
        matRx.setElem(POS.ZZ,  cos);
        
        return matRx;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the y axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  y-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationY(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRy = R3x3.newIdentity();

        matRy.setElem(POS.XX,  cos);
        matRy.setElem(POS.XZ,  sin);
        matRy.setElem(POS.ZX, -sin);
        matRy.setElem(POS.ZZ,  cos);
        
        return matRy;
    }
    
    /**
     * Create and return the generator element of SO(3) which is
     * a counter-clockwise rotation about the z axis.
     * 
     * @param   rotation angle in radians
     * 
     * @return  z-plane counter-clockwise rotation matrix 
     */
    public static R3x3  newRotationZ(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R3x3    matRz = R3x3.newIdentity();

        matRz.setElem(POS.XX,  cos);
        matRz.setElem(POS.XY, -sin);
        matRz.setElem(POS.YX,  sin);
        matRz.setElem(POS.YY,  cos);
        
        return matRz;
    }
    
    /**
     * Create a deep copy of the given <code>R3x3</code> matrix object.  The returned 
     * object is completely decoupled from the original.
     * 
     * @param   matTarget   matrix to be copied
     * @return              a deep copy of the argument object
     */
    public static R3x3  copy(R3x3 matTarget)    {
    	return matTarget.copy();
    }
    
    /**  
     *  Create a R3x3 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 3x3=9 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static R3x3 parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new R3x3(strTokens);
    }
    
    
    
    /*
     * Object Overrides
     */
    
    /**
     * Creates and returns a deep copy of this matrix.
     *
     * @see xal.tools.math.BaseMatrix#clone()
     *
     * @author Christopher K. Allen
     * @since  Jul 3, 2014
     */
    @Override
    public R3x3 clone() {
        return new R3x3(this);
    }

    
    /*
     * Initialization
     */
    
    /** 
     *  Creates a new instance of R3x3 initialized to zero.
     */
    public R3x3() {
        super(INT_SIZE);
    }
    
    /**
     *  Copy Constructor - create a deep copy of the target matrix.
     *
     *  @param  matInit     initial value
     */
    public R3x3(R3x3 matInit) {
        super(matInit);
    }
    
    /**
     * Initializing constructor for class <code>R3x3</code>.  The values of the
     * new matrix are set to the given Java primitive type array (the array
     * itself is unchanged).  The dimensions of the argument must be
     * 3&times;3.
     *
     * @param   arrValues   initial values for the new matrix object
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the dimensions 3&times;3
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public R3x3(double[][] arrValues) throws ArrayIndexOutOfBoundsException {
        super(arrValues);
    }
    
    /**
     *  Parsing Constructor - create a R3x3 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 3x3=9 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public R3x3(String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(INT_SIZE, strTokens);
    }
    


    /*
     *  Assignment
     */
    
    /**
     *  Element assignment - assigns matrix element to the specified value
     *
     *  @param  i       row index
     *  @param  j       column index
     *  @parm   s       new matrix element value
     *
     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2}
     */
    public void setElem(IND i, IND j, double s) throws ArrayIndexOutOfBoundsException {
        super.setElem(i, j, s);
    }
    
    /**
     * Set the element specified by the position in the argument to the
     * new value in the second argument.
     * 
     * @param   pos     matrix position
     * @param   val     new element value
     */
    public void setElem(POS pos, double val)   {
        super.setElem(pos.row(),pos.col(), val);
    }
    
//    /**
//     *  Element assignment - assigns matrix element to the specified value
//     *
//     *  @param  i       row index
//     *  @param  j       column index
//     *  @parm   s       new matrix element value
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2}
//     */
//    public void setElem(int i, int j, double s) throws ArrayIndexOutOfBoundsException {
//        super.setElem(i,j, s);
//    }
//    
//    /**
//     *  Set a submatrix within the phase matrix.
//     *
//     *  @param  i0      row index of upper left block
//     *  @param  i1      row index of lower right block
//     *  @param  j0      column index of upper left block
//     *  @param  j1      column index of lower right block
//     *  @param  arrSub  two-dimensional sub element array
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  submatrix does not fit into 3x3 matrix
//     */
//    public void setSubMatrix(int i0, int i1, int j0,  int j1, double[][] arrSub)
//        throws ArrayIndexOutOfBoundsException
//    {
//        Jama.Matrix matSub = new Matrix(arrSub);
//        
//        this.getMatrix().setMatrix(i0,i1,j0,j1, matSub);
//    }
    
    
    
    /*
     *  Matrix Properties
     */

    /**
     *  Return matrix element value.  Get matrix element value at specified 
     *  position.
     *
     *  @param  i       row index
     *  @param  j       column index
     */
    public double getElem(POS pos)   {
        return super.getElem(pos.row(), pos.col());
    }
    
//    /**
//     *  Return matrix element value.  Get matrix element value at specified 
//     *  <code>Diagonal</code> position.
//     *
//     *  @param  i       row index
//     *  @param  j       column index
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2}
//     */
//    public double getElem(int i, int j) 
//    {
//        return super.getElem(i,j);
//    }
    

//    /**
//     *  Check if matrix is symmetric.  
//     * 
//     *  @return true if matrix is symmetric 
//     */
//    public boolean isSymmetric()   {
//        if ( (getElem(0,1) != getElem(1,0)) 
//          || (getElem(0,2) != getElem(2,0))
//          || (getElem(1,2) != getElem(2,1))
//          )
//            return false;
//        else
//            return true;
////        int     i,j;        //loop control variables
////        
////        for (i=0; i<DIM; i++)
////            for (j=i; j<DIM; j++) {
////                if (getElem(i,j) != getElem(j,i) )
////                    return false;
////            }
////        return true;
//    }

    

    /*
     *  Object method overrides
     */
     
//     /**
//      * Return true if this object is equal to o, false otherwise.
//      */
//     @Override
//    public boolean equals(Object o) {
//     	if (o == null) return false;
//     	if ( !(o instanceof R3x3)) return false;
//     	R3x3 pm = (R3x3)o;
//        for (int i=0; i<INT_SIZE; i++) {
//            for (int j= 0; j<INT_SIZE; j++) {
//                if (!(this.getElem(i,j) == (pm.getElem(i,j)))) 
//                    return false;
//            }
//        }
//        return true;
//     } 
//     
//     /**
//      *  Convert the contents of the matrix to a string representation.
//      *  The format is similar to that of Mathematica, e.g.
//      *
//      *      { {a b }{c d } }
//      *
//      *  @return     string representation of the matrix
//      */
//     @Override
//    public String   toString()  {
//        final int size = (INT_SIZE*INT_SIZE * 16) + (INT_SIZE*2) + 4; // double is 15 significant digits plus the spaces and brackets
//        StringBuffer strBuf = new StringBuffer(size);
//         
//         synchronized(strBuf) { // get lock once instead of once per append
//            strBuf.append("{ ");
//            for (int i=0; i<INT_SIZE; i++) {
//                strBuf.append("{ ");
//                for (int j=0; j<INT_SIZE; j++) {
//                    strBuf.append(this.getElem(i,j));
//                    strBuf.append(" ");
//                }
//                strBuf.append("}");
//            }
//            strBuf.append(" }");
//         }
//         
//         return strBuf.toString();
//     }
//     
//    /**
//     * "Borrowed" implementation from AffineTransform, since it is based on
//     * double attribute values.  Must implement hashCode to be consistent with
//     * equals as specified by contract of hashCode in <code>Object</code>.
//     * 
//     * @return a hashCode for this object
//     */
//    @Override
//    public int hashCode() {
//        long bits = 0;
//        for (int i=0; i<INT_SIZE; i++) {
//            for (int j= 0; j<INT_SIZE; j++) {
//                bits = bits * 31 + Double.doubleToLongBits(getElem(i,j));;
//            }
//        }
//
//        return (((int) bits) ^ ((int) (bits >> 32)));
//    }           
//    
//
//    
    /*
     *  Matrix Operations
     */
    
    
//    /**
//     * Create a deep copy of the this matrix object.  The returned 
//     * object is completely decoupled from the original.
//     * 
//     * @return  a deep copy object of this matrix
//     */
//    public R3x3  copy()    {
//        return new R3x3(this);
//    }
//    
//    /**
//     *  Matrix determinant function.
//     *
//     *  @return     det(this)
//     */
//    public double det()     { return this.getMatrix().det(); };
//    
//    /**
//     *  Nondestructive transpose of this matrix.
//     * 
//     *  @return     transposed matrix
//     */
//    public R3x3 transpose()  {
//        return new R3x3( this.getMatrix().transpose() );
//    }
//    
//    /**
//     *  Nondestructive inverse of this matrix.
//     *
//     *  @return     the algebraic inverse of this matrix
//     */
//    public R3x3 inverse()    {
//        return new R3x3( this.getMatrix().inverse() );
//    }
//    
//    /**
//     *  Perform an eigenvalue decomposition of this matrix.
//     *  If the matrix is symmetric it can be decomposed as
//     * 
//     *      A = R*D*R'
//     * 
//     *  where A is this matrix, R is an (special) orthogonal matrix
//     *  in SO(3), and D is the diagonal matrix of eigenvales of A.
//     * 
//     * @return  eigen-system decomposition object for R3x3 matrix
//     */
//    public R3x3EigenDecomposition   eigenDecomposition()    {
//        return new R3x3EigenDecomposition( this.getMatrix().eig() );
//    }
//
    
    
    /*
     *  Algebraic Operations
     */
    
//    /**
//     *  Nondestructive matrix addition.
//     *
//     *  @param  mat     matrix to be added to this
//     *
//     *  @return         this + mat (elementwise)
//     */
//    public R3x3  plus(R3x3 mat)   {
//        return new R3x3( this.getMatrix().plus( mat.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix addition.
//     *
//     *  @param  mat     matrix to be added to this (result replaces this)
//     */
//    public void plusEquals(R3x3  mat)    {
//        this.getMatrix().plusEquals( mat.getMatrix() );
//    }
//    
//    /**
//     *  Nondestructive matrix subtraction.
//     *
//     *  @param  mat     matrix to be subtracted from this
//     *
//     *  @return         this - mat (elementwise)
//     */
//    public R3x3  minus(R3x3 mat)   {
//        return new R3x3( this.getMatrix().minus( mat.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix subtraction.
//     *
//     *  @param  mat     matrix to be subtracted from this (result replaces this)
//     */
//    public void minusEquals(R3x3  mat)    {
//        this.getMatrix().minusEquals( mat.getMatrix() );
//    }
//    
//    /**
//     *  Nondestructive scalar multiplication.
//     *
//     *  @param  s   scalar value to multiply this matrix
//     *
//     *  @return     new matrix equal to s*this
//     */
//    public R3x3 times(double s) {
//        return new R3x3( this.getMatrix().times(s) );
//    }
//    
//    /**
//     *  In-place scalar multiplication.
//     *
//     *  @param  s   scalar value to multiply this matrix (result replaces this)
//     */
//    public void timesEquals(double s) {
//        this.getMatrix().timesEquals(s);
//    }
//    
    /**
     *  Nondestructive Matrix-Vector multiplication.
     *
     *  @return     this*vec
     */
    public R3  times(R3 vec)  {
//        Jama.Matrix     matRes;     // resultant vector
        
        double x = getElem(0,0)*vec.getx() + getElem(0,1)*vec.gety() + getElem(0,2)*vec.getz();
        double y = getElem(1,0)*vec.getx() + getElem(1,1)*vec.gety() + getElem(1,2)*vec.getz();
        double z = getElem(2,0)*vec.getx() + getElem(2,1)*vec.gety() + getElem(2,2)*vec.getz();
     
        return new R3(x, y, z);
    }

	/**
     * Handles object creation required by the base class. 
     *
	 * @see xal.tools.math.BaseMatrix#newInstance()
	 * 
	 * @author Ivo List
	 * @author Christopher K. Allen
	 * @since  Jun 17, 2014
	 */
	@Override
	protected R3x3 newInstance() {
		return new R3x3();
	}

//    /**
//     *  Matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     *
//     *  @return             this*matRight
//     */
//    public R3x3  times(R3x3 matRight) {
//        return new R3x3( this.getMatrix().times( matRight.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     */
//    public void timesEquals(R3x3 matRight) {
//        this.getMatrix().arrayTimesEquals( matRight.getMatrix() );
//    }
//    
//
//    
//    /**
//     *  Function for transpose conjugation of this matrix by the argument matrix.  
//     *  This method is nondestructive, return a new matrix.
//     *
//     *  @param  matPhi      conjugating matrix (typically a tranfer matrix)
//     *
//     *  @return             matPhi*this*matPhi^T
//     */
//    public R3x3 conjugateTrans(R3x3 matPhi) {
//        R3x3 matResult;      // resultant matrix
//        
//        matResult = this.times(matPhi.transpose());
//        matResult = matPhi.times(matResult);
//        
//        return matResult;
//    };
//    
//    /**
//     *  Function for inverse conjugation of this matrix by the argument matrix.  
//     *  This method is nondestructive, return a new matrix.
//     *
//     *  @param  matPhi      conjugating matrix (typically a tranfer matrix)
//     *
//     *  @return             matPhi*this*matPhi^-1
//     */
//    public R3x3 conjugateInv(R3x3 matPhi) {
//        R3x3 matResult;      // resultant matrix
//        
//        matResult = this.times(matPhi.inverse());
//        matResult = matPhi.times(matResult);
//        
//        return matResult;
//    };
//    
//    
//    
    /*
     *  Topological Operations
     */
    
//    /**
//     * Return the maximum element value of this matrix
//     * 
//     * @return  maximum absolute value
//     */
//    public double   max()   {
//        int         i,j;
//        double      val = 0.0;
//        double      max = Math.abs(getElem(0,0));
//        
//        for (i=0; i<3; i++)
//            for (j=0; j<3; j++) {
//                val = Math.abs( getElem(i,j) );
//                if (val > max)
//                    max = val;
//            }
//      
//        return max;
//    }
//    
//    /**
//     *  Return the l1 norm of this matrix, which is the maximum
//     *  column sum.
//     *
//     *  @return     ||M||_1 = Sum |m_ij|
//     */
//    public double   norm1()     { return this.getMatrix().norm1(); };
//    
//    /**
//     *  Return the l2 norm of this matrix, which is the maximum
//     *  singular value.
//     *
//     *  @return     ||M||_2 = [ Sum (m_ij)^2 ]^1/2
//     */
//    public double   norm2()     { return this.getMatrix().norm2(); };
//    
//    /**
//     *  Return the l-infinity norm of this matrix, which is the 
//     *  maximum row sum.
//     *
//     *  @return     ||M||_inf = sup_ij |m_ij|
//     */
//    public double   normInf()   { return this.getMatrix().normInf(); };
//    
//    /**
//     * Return the Frobenius norm, which is the square-root of the sum
//     * of the squares of all the elements.
//     * 
//     *  @return     ||M||_F = [ Sum (m_ij)^2 ]^1/2
//     */
//    public double   normF()     { return this.getMatrix().normF(); };
//    
//    
//    
//    
    /*
     *  Internal Support
     */
    
    
//    /**
//     *  Construct a R3x3 from a suitable Jama.Matrix.  Note that the
//     *  argument should be a new object not owned by another object, because
//     *  the internal matrix representation is assigned to the target argument.
//     *
//     *  @param  matInit     a 3x3 Jama.Matrix object
//     */
//    R3x3(Jama.Matrix matInit)  {
//        m_mat3x3 = matInit;
//    }
//    
//    /**
//     *  Return the internal matrix representation.
//     *
//     *  @return     the Jama matrix object
//     */
//    Jama.Matrix getMatrix()   { 
//        return m_mat3x3; 
//    };
    
    
    
//    /*
//     *  Testing and Debugging
//     */
//    
//    /**
//     *  Print out the contents of the R3x3 in text format.
//     *
//     *  @param  os      output stream to receive text dump
//     */
//    public void print(PrintWriter os)   {
////        m_mat3x3.print(os, DIM, DIM);
//		  m_mat3x3.print(os, new DecimalFormat("0.#####E0"), INT_SIZE);
//    }
//    
//    
//    
//    
//    /**
//     *  Testing Driver
//     */
//    public static void main(String arrArgs[])   {
//        PrintWriter     os = new PrintWriter(System.out);
//        
//        R3x3     mat1 = R3x3.newIdentity();
//        mat1.print(os);
//        
//        R3x3     mat2 = new R3x3();
//        mat2.print(os);
//        
//        os.flush();
//    }
//    
}
