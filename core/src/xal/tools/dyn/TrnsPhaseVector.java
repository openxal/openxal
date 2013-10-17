/**
 * TrnsPhaseVector.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 17, 2013
 */
package xal.tools.dyn;

import java.util.EnumSet;

import xal.tools.math.BaseVector;
import xal.tools.math.IIndex;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 17, 2013
 */
public class TrnsPhaseVector extends BaseVector<TrnsPhaseVector> {

    
    /*
     * Internal Classes
     */
    
    /**
     * Enumeration for the element transverse position and velocity indices 
     * for homogeneous phase space objects.  This set include the phase space 
     * coordinates and the projective coordinate.
     *
     * @author Christopher K. Allen
     * @since  Oct 8, 2013
     */
    public enum IND implements IIndex {
        
        /*
         * Enumeration Constants
         */
        
        /** Index of the x coordinate */
        X(0),
        
        /** Index of the X' coordinate */
        Xp(1),
        
        /** Index of the Y coordinate */
        Y(2),
        
        /** Index of the Y' coordinate */
        Yp(3),
        
        /** Index of the homogeneous coordinate */
        HOM(4);
        
        
        /*
         * Global Constants
         */

        /** the set of IND constants that only include phase space variables (not the homogeneous coordinate) */
        private final static EnumSet<IND> SET_PHASE = EnumSet.of(X, Xp, Y, Yp);
        
        
        /*
         * Global Operations
         */
        
        /**
         * Returns the set of index constants that correspond to phase
         * coordinates only.  The homogeneous coordinate index is not
         * included (i.e., the <code>IND.HOM</code> constant).
         * 
         * @return  the set of phase indices <code>IND</code> less the <code>HOM</code> constant
         *
         * @see xal.tools.math.BaseMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since  Oct 15, 2013
         */
        public static EnumSet<IND>  valuesPhase() {
            return SET_PHASE;
        }
        
        
        /*
         * IIndex Interface
         */
        
        /**
         * Returns the numerical index of this enumeration constant,
         * corresponding to the index into the phase matrix.
         * 
         * @return  numerical value of this index
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        public int val() {
            return this.val;
        }
        
        /*
         * Initialization
         */
        
        /** The numerical value of this enumeration index */
        final private int     val; 
        
        /**
         * Creates a new <code>IND</code> enumeration constant
         * initialized to the given index value.
         * 
         * @param index     numerical index value for this constant
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        private IND(int index) {
            this.val = index;
        }
    }
    
    
    /*
     * Global Constants
     */
    
    /** number of matrix dimensions  */
    public static final int    INT_SIZE = 5;
   
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero phase matrix.
     *
     *  @return         zero vector
     */
    public static TrnsPhaseMatrix  newZero()   {
        TrnsPhaseMatrix matZero = new TrnsPhaseMatrix();
        
        // Zero then set the homogeneous element to unity
        matZero.assignZero();
        matZero.setElem(IND.HOM.val, IND.HOM.val, 1.0);
        
        return matZero; 
    }
    

    /*
     * Initialization
     */
    
    /**
     * Zero argument constructor for <code>TrnsPhaseVector</code>.
     * The new vector contains all zeros as elements.
     *
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 17, 2013
     */
    public TrnsPhaseVector() throws UnsupportedOperationException {
        super(INT_SIZE);
        super.setElem(IND.HOM, 1.0);
    }

    /**
     * Copy constructor for <code>TrnsPhaseVector</code>.  The 
     * constructed object is a deep copy of the given vector.
     *
     * @param vecParent     vector to be cloned
     * 
     * @throws UnsupportedOperationException  base class has not defined a public, zero-argument constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 17, 2013
     */
    public TrnsPhaseVector(TrnsPhaseVector vecParent) throws UnsupportedOperationException {
        super(vecParent);
        super.setElem(IND.HOM, 1.0);
    }

    /**
     *  <p>
     *  Parsing Constructor - creates an instance of the class and initialize it
     *  according to a token string of element values.
     *  </p>  
     *  <p>
     *  The token string argument is assumed to be one-dimensional and delimited
     *  by any of the characters <tt>" ,()[]{}"</tt>  Repeated, contiguous delimiters 
     *  are parsed together.  This conditions allows a variety of parseable string
     *  representations. For example,
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; { 1, 2, 3, 4 }
     *  <br/>
     *  <br/>
     *  and
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; [1 2 3 4]
     *  <br/>
     *  <br/>
     *  would parse to the same homogeneous vector (1, 2, 3, 4 | 1).
     *  </p>
     *
     *  @param  strTokens   token vector of {@link #getSize()} numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     *
     * @author Christopher K. Allen
     * @since  Oct 17, 2013
     */
    public TrnsPhaseVector(String strTokens)
        throws IllegalArgumentException, NumberFormatException 
    {
        super(INT_SIZE, strTokens);
    }

    /**
     * <p>
     * Initializing constructor for class <code>TrnsPhaseVector</code>.  
     * Sets the entire vector to the values given in the Java primitive type 
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
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 17, 2013
     */
    public TrnsPhaseVector(double[] arrVals) throws ArrayIndexOutOfBoundsException {
        super(INT_SIZE, arrVals);
    }

}
