/**
 * r6.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 15, 2013
 */
package xal.tools.math.r4;

import xal.tools.math.BaseVector;
import xal.tools.math.IIndex;

/**
 * Implements the set of real 4-vectors in <b>R</b><sup>4</sup> 
 *
 * @author Christopher K. Allen
 * @since  Oct 15, 2013
 */
public class R4 extends BaseVector<R4> {

    
    /*
     * Internal Types
     */
    
    /**
     * Class <code>R4x4.IND</code> is an enumeration of the matrix indices
     * for the <code>R4x4</code> class.
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    public enum IND implements IIndex {
        
        /** the 1<i>st</i> axis index of <b>R</b><sup>6</sup> */
        X1(0),

        /** the 2<i>nd</i> axis index of <b>R</b><sup>6</sup> */
        X2(1),
        
        /** the <i>rd</i> axis index of <b>R</b><sup>6</sup> */
        X3(2),
        
        /** the <i>rd</i> axis index of <b>R</b><sup>6</sup> */
        X4(3);

        
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
    
    
    /*
     *  Global Constants
     */
     
     /** serialization version identifier */
    private static final long serialVersionUID = 1L;

     
     
     /** number of dimensions (DIM=3) */
     public static final int    INT_SIZE = 4;
     
    
     /*
      *  Global Methods
      */
     
     /**
      *  Create a new instance of a zero vector.
      *
      *  @return         zero vector
      */
     public static R4   newZero()   {
         R4 vecZero = new R4();
         
         vecZero.assignZero();
         
         return vecZero; 
     }
     
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for <code>R6</code>.
     *
     * @param intSize
     * @throws UnsupportedOperationException
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public R4() throws UnsupportedOperationException {
        super(INT_SIZE);
    }

    /**
     * <p>
     * Initializing constructor for bases class <code>Vector</code>.  
     * Sets the entire matrix to the values given in the Java primitive type 
     * double array. The argument itself remains unchanged. 
     * </p>
     * <p>
     * The dimensions of the given Java double array must be 
     * consistent with the size of the matrix.  Thus, if the arguments are
     * inconsistent, an exception is thrown.
     * </p>
     * 
     * @param arrMatrix   Java primitive array containing new vector values
     * 
     * @exception  IllegalArgumentException  the argument must have the same dimensions as this matrix
     * 
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public R4(double[] arrVals) throws IllegalArgumentException {
        super(arrVals);

        if (arrVals.length != INT_SIZE)
            throw new IllegalArgumentException("Argument has wrong dimensions " + arrVals);
    }

    /**
     * Copy constructor for <code>R4</code>.  Creates a cloned copy of the 
     * given parent object.
     *
     * @param matParent     template object for which the deep copy is performed
     *
     * @author Christopher K. Allen
     * @since  Jul 3, 2014
     */
    public R4(R4 matParent) {
        super(matParent);
    }
    
    /*
     * Object Method Overrides
     */
    
    /**
     * Creates and returns a deep copy of <b>this</b> vector.
     * 
     * @see xal.tools.math.BaseVector#clone()
     * 
     * @author Jonathan M. Freed
     * @since Jul 3, 2014
     */
    @Override
    public R4 clone(){
    	return new R4(this);
    }


	/**
     * Handles object creation required by the base class.
     *
	 * @see xal.tools.math.BaseVector#newInstance()
	 *
	 * @author Ivo List
	 * @author Christopher K. Allen
	 * @since  Jun 17, 2014
	 */
	@Override
	protected R4 newInstance() {
		return new R4();
	}


    /**
     *
     * @see xal.tools.math.BaseVector#newInstance(double[])
     *
     * @since  Jul 24, 2015   by Christopher K. Allen
     */
    @Override
    protected R4 newInstance(double[] arrVecInt) {
        return new R4(arrVecInt);
    }
}
