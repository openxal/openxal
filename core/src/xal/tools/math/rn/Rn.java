/**
 * Rn.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 22, 2015
 */
package xal.tools.math.rn;

import xal.tools.data.DataAdaptor;
import xal.tools.math.BaseVector;

/**
 * Represents a vector of real numbers with arbitrary length.
 *
 * @author Christopher K. Allen
 * @since  Jul 22, 2015
 */
public class Rn extends BaseVector<Rn> {

    
    /*
     * Global Constants
     */
    
    /** Serialization version number */
    private static final long serialVersionUID = 1L;

    
    
    /*
     * Initialization
     */
    
    /**
     * Zero matrix constructor for <code>Rn</code>.  A new vector is created
     * with the given dimension and all zero elements.
     *
     * @param intSize   vector size
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    public Rn(int intSize) {
        super(intSize);
    }

    /**
     * Initializing constructor for <code>Rn</code>.  The
     * element values are loaded from the given data source.
     *
     * @param intSize   size of the vector
     *      
     * @param daSource  data source contains initial values for elements
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    public Rn(int intSize, DataAdaptor daSource) {
        super(intSize, daSource);
    }

    /**
     * <p>
     * Initializing constructor for bases class <code>Rn</code>.  
     * Sets the entire vector to the values given in the Java primitive type 
     * double array. The argument itself remains unchanged. 
     * </p>
     * <p>
     * The dimensions of the new vector will be the length of the given Java double array. 
     * </p>
     * <p>
     * <h4>NOTE</h4>
     * &middot; The given array is set to the internal vector representation.
     * Thus the given array should not be referenced afterwards!
     * <br/>
     * <br/>
     * &middot; This action is done for performance.  If the given array is
     * intended for further manipulation then a clone should be offered to this
     * constructor.
     * </p>
     * 
     * @param arrMatrix   Java primitive array containing new vector values
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    public Rn(double[] arrVals) {
        super(arrVals);
    }

    /**
     *  <p>
     *  Parsing Constructor - creates an instance of the child class and initialize it
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
     *  would parse to the same real vector (1.0, 2.0, 3.0, 4.0).
     *  </p>
     *
     *  @param  intSize     the matrix size of this object
     *  @param  strTokens   token vector of getSize() numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    public Rn(int intSize, String strTokens) throws IllegalArgumentException, NumberFormatException {
        super(intSize, strTokens);
    }

    /**
     * Copy constructor.  Creates a deep copy of the given argument.
     * 
     * @param   vecTemplate vector to be cloned
     *
     *  @since  Jul 22, 2015   by Christopher K. Allen
     */
    public Rn(Rn vecTemplate) throws UnsupportedOperationException {
        super(vecTemplate);
    }

    
    /*
     * Abstract Implementations
     */
    
    /**
     *
     * @see xal.tools.math.BaseVector#clone()
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    public Rn clone() {
        return new Rn(this);
    }

    /**
     * Returns a new, zero element, instance of <code>Rn</code> which has the
     * same size as this object.  That is, the returned object is in 
     * the same equivalence class of vectors as this one.
     *
     * @see xal.tools.math.BaseVector#newInstance()
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @Override
    protected Rn newInstance() {
        int intSize = this.getSize();
        
        return new Rn(intSize);
    }

    /**
     *
     * @see xal.tools.math.BaseVector#newInstance(double[])
     *
     * @since  Jul 24, 2015   by Christopher K. Allen
     */
    @Override
    protected Rn newInstance(double[] arrVecInt) {
        return new Rn(arrVecInt);
    }

}
