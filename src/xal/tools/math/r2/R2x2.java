/*
 * R2x2.java
 *
 * Created on September 16, 2003, 2:32 PM
 * Modified: September 30, 2013
 * 
 */

package xal.tools.math.r2;


import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r6.R6x6;





/**
 *  <p>
 *  Represents an element of R2x2, the set of real 3x3 matrices.
 *  The class a set of the usual matrix operations and linear
 *  transforms on R3 represented by the matrix.  
 *  </p>
 *
 * @author  Christopher Allen
 *
 *  @see    Jama.Matrix
 *  @see    xal.tools.r3.R3
 */

public class R2x2 extends SquareMatrix<R2x2> implements java.io.Serializable {
    
    /**
     * Enumeration of the allowed index positions for objects of type
     * <code>R2x2</code>.
     *
     *
     * @author Christopher K. Allen
     * @since  Oct 2, 2013
     */
    public enum IND implements IIndex {
    	
    	/** represents the phase coordinate index */
    	X(0),
    	
    	/** represents the phase angle index */
    	P(1);

		/**
		 * Returns the numerical value of this index enumeration constant.
		 * 
		 * @return	numerical index value
		 *
		 * @see xal.tools.math.SquareMatrix.IIndex#val()
		 *
		 * @author Christopher K. Allen
		 * @since  Sep 27, 2013
		 */
		@Override
		public int val() {
			return this.intVal;
		}

		
		/*
		 * Local Attributes
		 */
		
		/** The numerical index value */
		private final int		intVal;
		

		/*
         * Initialization
         */
        
        /** 
         * Default enumeration constructor 
         */
		private IND(int intVal) {
			this.intVal = intVal;
		}
    }
    
//    /**  
//     * Enumeration for the element positions of an 
//     * <code>R2x2</code> matrix element.  Also provides some 
//     * convenience functions for accessing these <code>R2x2</code> 
//     * elements.
//     * 
//     * @author  Christopher K. Allen
//     */
//    public enum Position  {
//        
//        /*
//         * Enumeration Constants
//         */
//        XX (0,0),
//        XY (0,1), 
//        YY (1,1),
//        YX (1,0);
//                    
//        
//        /*
//         * Local Attributes
//         */
//        
//        /** row index */
//        private final int i;
//        
//        /** column index */
//        private final int j;
//        
//        
//        /*
//         * Initialization
//         */
//        
//        
//        /** 
//         * Default enumeration constructor 
//         */
//        Position(int i, int j)  {
//            this.i = i;
//            this.j = j;
//        }
//        
//        /** return the row index of the matrix position */
//        public int row()    { return i; };
//
//        /** return the column index of the matrix position */
//        public int col()    { return j; };
//
//        
//        /** 
//         * Return the <code>Position</code> object representing the 
//         * transpose element of this position.
//         * 
//         * NOTE:
//         * The current implementation is slow.
//         * 
//         * @return  the transpose position of the current position
//         */ 
//        public Position transpose() { 
//            int i = this.col();
//            int j = this.row();
//            
//            for (Position pos : Position.values()) {
//                if (pos.row() == i && pos.col() == j)
//                    return pos;
//            }
//
//            return null;
//        };
//        
//        
//        
//        /*
//         * Enumerating Positions
//         */
//        
//        /** 
//         *  Returns the set of all element positions above the matrix 
//         *  diagonal.
//         *  
//         *  @return     set of upper triangle matrix positions
//         */
//        public static EnumSet<Position> getUpperTriangle() { return EnumSet.of(XY); };
//        
//        /**
//         * Return the set of all matrix element positions along the 
//         * diagonal.
//         * 
//         * @return      set of diagonal element positions
//         */
//        public static EnumSet<Position> getDiagonal()      { return EnumSet.of(XX, YY); };
//        
//        /**
//         * Return the set of all element positions below the matrix
//         * diagonal.
//         * 
//         * @return      set of lower triangle matrix positions
//         */
//        public static EnumSet<Position> getLowerTriangle() { return EnumSet.of(YX); };
//        
//        /**
//         * Return the set of all off-diagonal matrix positions.
//         * 
//         * @return      set of off diagonal positions, both upper and lower.
//         */
//        public static EnumSet<Position> getOffDiagonal() { return EnumSet.complementOf(Position.getDiagonal()); };
//        
//        
//        
//        /*
//         * Matrix Element Accessing
//         */
//        
//        /** 
//         * Return the matrix element value for this position
//         * 
//         * @param   matTarget   target matrix
//         * @return              element value for this position
//         */
//        public double   getValue(R2x2 matTarget)    { return matTarget.getElem(row(),col()); };
//
//        /**
//         * Get the diagonal element in the same row as this element position.
//         * 
//         * @param matTarget     target matrix
//         * @return              row diagonal element value
//         */
//        public double   getRowDiag(R2x2 matTarget)  { return matTarget.getElem(row(),row()); };
//        
//        /**
//         * Get the diagonal element in the same column as this element position.
//         * 
//         * @param matTarget     target matrix
//         * @return              column diagonal element value
//         */
//        public double   getColDiag(R2x2 matTarget)  { return matTarget.getElem(col(),col()); };
//        
//        
//        /*
//         * Matrix Element Assignment 
//         */
//        
//        /** 
//         * Set matrix element value for this position
//         * 
//         * @param   matTarget   target matrix
//         * @param   s           new value for matrix element
//         */
//        public void setValue(R2x2 matTarget, double s)    { matTarget.setElem(row(),col(), s); };
//
//        /**
//         * Set the diagonal element in the same row as this element position.
//         * 
//         * @param matTarget     target matrix
//         * @param s             new value for matrix element
//         */
//        public void setRowDiag(R2x2 matTarget, double s)  { matTarget.setElem(row(),row(), s); };
//
//        /**
//         * Set the diagonal element in the same column as this element position.
//         * 
//         * @param matTarget     target matrix
//         * @param s             new value for matrix element
//         */
//        public void setColDiag(R2x2 matTarget, double s)  { matTarget.setElem(col(),col(), s); };
//    };

    

    
    /*
     *  Global Constants
     */
     
     /** serialization version identifier */
    private static final long serialVersionUID = 1L;

     
     /** Matrix size */
     public static final int    INT_SIZE = 2;
     
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create and return a new instance of a zero matrix.
     *
     *  @return         a matrix with all zero elements
     */
    public static R2x2  newZero()   {
        R2x2    matZero = new R2x2();
        matZero.assignZero();
        
        return matZero;
    }
    
    /**
     *  Create and return a new identity matrix
     *
     *  @return         identity matrix object
     */
    public static R2x2  newIdentity()   {
        R2x2    matIden = new R2x2();
        matIden.assignIdentity();
        
        return  matIden; 
    }
    
    public static R2x2 newSymplectic() {
        R2x2    matJ = new R2x2();
        matJ.assignZero();
        matJ.setElem(IND.X, IND.P, +1.0);
        matJ.setElem(IND.P, IND.X, -1.0);
        
        return matJ;
    }
    
    /**
     * Create and return the generator element of SO(2) which is
     * a counter-clockwise rotation.
     * 
     * @param   rotation angle in radians
     * 
     * @return  x-plane counter-clockwise rotation matrix 
     */
    public static R2x2  newRotation(double dblAng)    {
        double  sin   = Math.sin(dblAng);
        double  cos   = Math.cos(dblAng);
        R2x2    matRx = R2x2.newIdentity();

        matRx.setElem(IND.X,  IND.X,  +cos);
        matRx.setElem(IND.X,  IND.P, -sin);
        matRx.setElem(IND.P, IND.X,  +sin);
        matRx.setElem(IND.P, IND.P, +cos);
        
        return matRx;
    }
    
    /**
     * Create a deep copy of the given <code>R2x2</code> matrix object.  The returned 
     * object shares no references with the argument.
     * 
     * @param   matTarget   matrix to be copied
     * 
     * @return              a deep copy of the argument object
     * 
     */
    public static R2x2  clone(R2x2 matTarget) {
    	return matTarget.copy();
    }
    
    /**
     *  Create a R2x2 instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 2x2=4 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static R2x2 parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new R2x2(strTokens);
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
    public R2x2 clone() {
        return new R2x2(this);
    }
    
    
    /*
     * Initialization
     */
    
    /** 
     *  Creates a new instance of R2x2 initialized to zero.
     */
    public R2x2() {
    	super(INT_SIZE);
    }
    
    /**
	 *  Copy Constructor - create a deep copy of the given matrix.
	 *
	 *  @param  matParent     initial value
	 */
	public R2x2(R2x2 matParent) {
		super(matParent);
	}
    
	/**
	 *  Parsing Constructor - create a R2x2 instance and initialize it
	 *  according to a token string of element values.  
	 *
	 *  The token string argument is assumed to be one-dimensional and packed by
	 *  column (ala FORTRAN).
	 *
	 *  @param  strTokens   token vector of 2x2=4 numeric values
	 *
	 *  @exception  IllegalArgumentException    wrong number of token strings
	 *  @exception  NumberFormatException       bad number format, unparseable
	 */
	public R2x2(String strTokens) throws IllegalArgumentException, NumberFormatException {
	    super(INT_SIZE, strTokens);
	}


    /*
     *  Assignment
     */
    
	/**
     * Set the element the given indices to the new value.
     * 
     * @param   iRow	matrix row location
     * @param	iCol	matrix column index
     * 
     * @param   val     matrix element at given row and column will be set to this value
     */
    public void setElem(IND iRow, IND iCol, double dblVal)   {
        super.setElem(iRow, iCol, dblVal);
    }
    
    
    
    /*
     *  Matrix Properties
     */

    /**
     *  Return matrix element value.  Get matrix element value at specified 
     *  position.
     *
     *  @param  iRow       row index
     *  @param  iCol       column index
     * 
     * @return			the matrix element at the position specified by the indices.
     */
    public double getElem(IND iRow, IND iCol) 
    {
        return super.getElem(iRow, iCol);
    }
    
    

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
//    public R2x2  plus(R2x2 mat)   {
//        return new R2x2( this.getMatrix().plus( mat.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix addition.
//     *
//     *  @param  mat     matrix to be added to this (result replaces this)
//     */
//    public void plusEquals(R2x2  mat)    {
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
//    public R2x2  minus(R2x2 mat)   {
//        return new R2x2( this.getMatrix().minus( mat.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix subtraction.
//     *
//     *  @param  mat     matrix to be subtracted from this (result replaces this)
//     */
//    public void minusEquals(R2x2  mat)    {
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
//    public R2x2 times(double s) {
//        return new R2x2( this.getMatrix().times(s) );
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
     *  Non-destructive Matrix-Vector multiplication. Specifically, the
     *  vector <b>y</b> given by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>y</b> = <b>Ax</b>
     *  <br>
     *  <br>
     *  where <b>A</b> is this matrix and <b>x</b> is the given vector.
     *  
     *  @param vec	the vector factor <b>x</bx>
     *
     *  @return     the matrix-vector product of this matrix with the given vector
     */
    public R2  times(R2 vec)  {
        
        double x = getElem(0,0)*vec.getx() + getElem(0,1)*vec.gety();
        double y = getElem(1,0)*vec.getx() + getElem(1,1)*vec.gety();
     
        return new R2(x, y);
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
	protected R2x2 newInstance() {
		return new R2x2();
	}

//    /**
//     *  Matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     *
//     *  @return             this*matRight
//     */
//    public R2x2  times(R2x2 matRight) {
//        return new R2x2( this.getMatrix().times( matRight.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     */
//    public void timesEquals(R2x2 matRight) {
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
//    public R2x2 conjugateTrans(R2x2 matPhi) {
//        R2x2 matResult;      // resultant matrix
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
//    public R2x2 conjugateInv(R2x2 matPhi) {
//        R2x2 matResult;      // resultant matrix
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
     *  Internal Support
     */
    
//    /**
//     *  <p>
//     *  Constructor for initializing this matrix from a suitable <code>Jama.Matrix</code>.
//     *  This constructor should only be called to create new <code>R2x2</code> objects
//     *  from within this class.  The given <code>Jama.Matrix</code> should not be 
//     *  referenced any where else, that is, the newly constructed <code>R2x2</code>
//     *  object should contain the sole reference to the given <code>Jama</code>
//     *  matrix.  
//     *  </p>
//     *  </p>
//     *  <p>
//     *  <h4>NOTE</h4>
//     *  The argument should be a new object not owned by another object, because
//     *  the internal matrix representation is assigned to the target argument.
//     *  </p>
//     *
//     *  @param  matInit     a 3x3 Jama.Matrix object
//     */
//    private R2x2(Jama.Matrix matInit)  {
//    	super(INT_SIZE, matInit);
//    }
//
    
    
}
