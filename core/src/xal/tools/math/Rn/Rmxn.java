/**
 * Rmxn.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 21, 2015
 */
package xal.tools.math.Rn;

import xal.tools.math.BaseMatrix;

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
     * Constructor for Rmxn.
     *
     * @param cntRows
     * @param cntCols
     * @param arrVals
     * @throws ArrayIndexOutOfBoundsException
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        super(arrVals);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor for Rmxn.
     *
     * @param cntRows
     * @param cntCols
     * @param strTokens
     * @throws IllegalArgumentException
     * @throws NumberFormatException
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(int cntRows, int cntCols, String strTokens)
            throws IllegalArgumentException, NumberFormatException {
        super(cntRows, cntCols, strTokens);
    }

    /**
     * Constructor for Rmxn.
     *
     * @param matParent
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    public Rmxn(Rmxn matParent) {
        super(matParent);
        // TODO Auto-generated constructor stub
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
        // TODO Auto-generated method stub
        return null;
    }

    /**
     *
     * @see xal.tools.math.BaseMatrix#newInstance()
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @Override
    protected Rmxn newInstance() {
        // TODO Auto-generated method stub
        return null;
    }

}
