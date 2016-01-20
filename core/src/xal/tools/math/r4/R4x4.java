/**
 * R6x6.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 15, 2013
 */
package xal.tools.math.r4;

import xal.tools.beam.PhaseMatrix;
import xal.tools.dyn.TrnsPhaseMatrix;
import xal.tools.dyn.TrnsPhaseMatrix.IND;
import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r2.R2x2;
import xal.tools.math.r3.R3x3;
import xal.tools.math.r3.R3x3.POS;
import xal.tools.math.r6.R6x6;

/**
 *  <p>
 *  Represents an element of <b>R</b><sup>4&times;4</sup>, the set of real, 
 *  4&times;4 matrices.
 *  The class also contains the usual set of matrix operations and linear
 *  transforms on <b>R</b><sup>4</sup> that are represented by these
 *  matrices.  
 *  </p>
 *
 * @author Christopher K. Allen
 * @since  Oct 15, 2013
 */
public class R4x4 extends SquareMatrix<R4x4> {

    /*
     * Internal Types
     */
    
    /** 
     * Enumeration for the element position indices of a homogeneous
     * phase space objects.  Extends the <code>PhaseIndex</code> class
     * by adding the homogeneous element position <code>HOM</code>.
     * 
     * @author  Christopher K. Allen
     * @since   Oct, 2013
     */
    public enum IND implements IIndex {
        
        /*
         * Enumeration Constants
         */
        /** Index of the 1st coordinate */
        X1(0),
        
        /** Index of the 2nd coordinate */
        X2(1),
        
        /** Index of the 3rd coordinate */
        X3(2),
        
        /** Index of the 4th coordinate */
        X4(3);
        
        
        /*
         * IIndex Interface
         */
        
        /** 
         * Return the integer value of the index position 
         */
        public int val()    { return i; };

        
        /*
         * Local Attributes
         */
        
        /** index value */
        private final int i;
        
        
        /*
         * Initialization
         */
        
        /** 
         * Default enumeration constructor 
         */
        IND(int i)  {
            this.i = i;
        }
    }

    
    /*
     * Global Constants
     */
    
    
    /** number of dimensions */
    public static final int    INT_SIZE = 4;

    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero matrix.
     *
     *  @return         zero vector
     */
    public static R4x4 newZero()   {
        R4x4 matZero = new R4x4();
        matZero.assignZero();
        return matZero;
    }
    
    /**
     *  Create a new identity matrix
     *
     *  @return         4&times;4 real identity matrix
     */
    public static R4x4  newIdentity()   {
        R4x4 matIden = new R4x4();
        matIden.assignIdentity();
        return matIden;
    }
    
    /**
     * <p>
     * Compute the rotation matrix in phase space that is essentially the 
     * Cartesian product of the given rotation matrix in <i>SO</i>(3).  That is,
     * if the given argument is the rotation <b>O</b>, the returned matrix, 
     * denoted <b>M</b>, is the <b>M</b> = <b>O</b>&times;<b>O</b>&times;<b>I</b> embedding
     * into homogeneous phase space <b>R</b><sup>6&times;6</sup>&times;{1}. Thus, 
     * <b>M</b> &in; <i>SO</i>(6) &sub; <b>R</b><sup>6&times;6</sup>&times;{1}.   
     * </p>
     * <p>
     * Viewing phase-space as a 6D manifold built as the tangent bundle over
     * <b>R</b><sup>3</sup> configuration space, then the fibers of 3D configuration space at a 
     * point (<i>x,y,z</i>) are represented by the Cartesian planes (<i>x',y',z'</i>).  The returned
     * phase matrix rotates these fibers in the same manner as their base point (<i>x,y,z</i>).  
     * </p>
     * <p>
     * This is a convenience method to build the above rotation matrix in <i>SO</i>(7).
     * </p>
     * 
     * @param matSO2    a rotation matrix in two dimensions, i.e., a member of <i>SO</i>(2) &sub; <b>R</b><sup>2&times;2</sup> &cong; <i>S</i><sup>1</sup> 
     * 
     * @return  rotation matrix in <i>S0</i>(4) which is direct product of rotations in <i>S0</i>(2) 
     */
    public static R4x4  rotationProduct(R2x2 matSO2)  {

        // Populate the phase rotation matrix
        R4x4 matSO4 = R4x4.newIdentity();
        
        int         m, n;       // indices into the SO(7) matrix
        double      val;        // matSO3 matrix element
        
        for (IND i : IND.values())  {
            for (IND j : IND.values()) {
                m = 2*i.val();
                n = 2*j.val();

                val = matSO2.getElem(i,j);

                matSO4.setElem(m,  n,   val);   // configuration space
                matSO4.setElem(m+1,n+1, val);   // momentum space
            }
        }
        return matSO4;
    }
    
    /**
     * Extracts a copy of the transverse portion of the given <code>PhaseMatrix</code>
     * and returns it.  
     * 
     * @param matPhi    phase matrix to be copied
     * 
     * @return          the part of the phase matrix corresponding to the transverse 
     *                  phase coordinates <i>x, x', y, </i> and <i> y'</i>.
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public static R4x4  extractTransverse( final PhaseMatrix matPhi ) {
        R4x4    matTrans = R4x4.newZero();
        
        for (IND i : IND.values())
            for (IND j : IND.values()) {
                double  dblVal = matPhi.getElem(i, j);
                
                matTrans.setElem(i, j, dblVal);
            }
        
        return matTrans;
    }
            

    
    /**
     *  <p>
     *  Create a new <code>R6x6</code> instance and initialize it
     *  according to a token string of element values.
     *  </p>  
     *  <p>
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *  </p>
     *
     *  @param  strTokens   token vector of 4x4=16 numeric values
     *  
     *  @return             real matrix with elements initialized by the given numeric tokens 
     *
     *  @exception  IllegalArgumentException    wrong number of string tokens
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static R4x4 parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new R4x4(strTokens);
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
    public R4x4 clone() {
        return new R4x4(this);
    }

    
    /*
     * Initialization
     */
    
    /**
     * Zero argument constructor for R6x6.  Returns a matrix
     * of zeros.
     *
     * @throws UnsupportedOperationException    only thrown in the absence of this constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public R4x4() throws UnsupportedOperationException {
        super(INT_SIZE);
    }

    /**
     * Initializing constructor for <code>R6x6</code>.  The matrix elements are
     * set to those in the given Java native array, which must be 6&times;6
     * dimensional.
     *
     * @param arrVals   initial values for new matrix
     * 
     * @throws ArrayIndexOutOfBoundsException   the given native array is not 6&times;6 dimensional
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public R4x4(double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);
    }

    /**
     * <p>
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.
     *  </p>  
     *  <p>
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *  </p>
     *
     *  @param  strTokens   token vector of getSize()^2 numeric values
     * 
     *  @exception  IllegalArgumentException    wrong number of string tokens
     *  @exception  NumberFormatException       bad number format, unparseable
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public R4x4(String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(INT_SIZE, strTokens);
    }

    /**
     * Copy constructor for <code>R6x6</code>.  Creates a deep
     * copy of the given object.  The dimensions are set and the 
     * internal array is cloned. 
     *
     * @param matParent     the matrix to be cloned
     *
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     * 
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public R4x4(R4x4 matParent) throws UnsupportedOperationException {
        super(matParent);
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
	protected R4x4 newInstance() {
		return new R4x4();
	}
    
    
    
    

}
