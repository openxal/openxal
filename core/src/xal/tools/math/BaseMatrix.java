/**
 * BaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 11, 2013
 */
package xal.tools.math;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import xal.tools.beam.PhaseMatrix;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * <p>
 * Class <code>BaseMatrix</code>.  This is a base class for objects representing
 * real-number matrix objects.  Thus it contains basic matrix operations where the interacting
 * objects are all of type <code>M</code>, or vectors of the singular type <code>V</code>.
 * (If matrix and vectors are not of compatible dimensions the operations fail.)
 * The template parameter <code>M</code> is the type of the child class.  This 
 * mechanism allows <code>BaseMatrix&lt;M extends BaseMatrix&lt;M&gt;&gt;</code>
 * to recognize the type of it derived classes in order to create and process
 * new objects as necessary. 
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
 * @since  Oct 11, 2013
 */
public abstract class BaseMatrix<M extends BaseMatrix<M>> implements IArchive {

    
    
    /*
     * Global Constants
     */
    
    
   /** The default character width of matrices when displayed using {@link #toStringMatrix()}  */
    private static final int INT_COL_WD_DFLT = 15;


    /** Attribute marker for data managed by IArchive interface */
    public static final String ATTR_DATA = "values";
    

    /*
     * Global Attributes
     */
    
    /** Text format for outputting debug info */
    final static private DecimalFormat SCI_FORMAT = new DecimalFormat("0.000000E00");
   
    
//    /*
//     * Internal Classes
//     */
//
//    /**
//     * Interface <code>BaseMatrix.Ind</code> is exposed by objects
//     * representing matrix indices.  In particular, the <code>enum</code>
//     * types that are matrix indices expose this interface.
//     *
//     * @author Christopher K. Allen
//     * @since  Sep 25, 2013
//     */
//    public interface IIndex extends IIndex {
//    }
//
    
    
    /*
     *  Local Attributes
     */
    
    /** number of matrix rows */
    private final int               cntRows;
    
    /** number of matrix columns */
    private final int               cntCols;
    
    
    /** internal matrix implementation */
    protected Jama.Matrix     matImpl;

    
    /*
     * Object Overrides
     */
    
    /**
     * Base classes must override the clone operation in order to 
     * make deep copies of the current object.  This operation cannot
     * be done without the exact type.
     *
     * @see java.lang.Object#clone()
     *
     * @author Christopher K. Allen
     * @since  Jul 3, 2014
     */
    @Override
    public abstract M   clone();
    
    
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
    public void setSubMatrix(int i0, int i1, int j0,
            int j1, double[][] arrSub) throws ArrayIndexOutOfBoundsException {
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
        if (this.getRowCnt() != arrMatrix.length  ||  arrMatrix[0].length != this.getColCnt() )
            throw new ArrayIndexOutOfBoundsException(
                    "Dimensions of argument do not correspond to size of this matrix = " 
                   + this.getRowCnt() + "x" + this.getColCnt()
                   );
        
        // Set the elements of this array to that given by the corresponding 
        //  argument entries
        for (int i=0; i<this.getRowCnt(); i++) 
            for (int j=0; j<this.getColCnt(); j++) {
                double dblVal = arrMatrix[i][j];
                
                this.setElem(i, j, dblVal);
            }
    }

    /**
     *  Parsing assignment - set the <code>PhaseMatrix</code> value
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (aka FORTRAN).
     *
     *  @param  strValues   token vector of SIZE<sup>2</sup> numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public void setMatrix(String strValues) throws NumberFormatException,
            IllegalArgumentException {
                
                // Error check the number of token strings
                StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()[]{}"); //$NON-NLS-1$
                
                if (tokArgs.countTokens() != this.getRowCnt()*this.getColCnt())
                    throw new IllegalArgumentException("PhaseMatrix#setMatrix - wrong number of token strings: " + strValues); //$NON-NLS-1$
                
                
                // Extract initial phase coordinate values
                for (int i=0; i<this.getRowCnt(); i++)
                    for (int j=0; j<this.getColCnt(); j++) {
                        String  strVal = tokArgs.nextToken();
                        double  dblVal = Double.valueOf(strVal).doubleValue();
                    
                        this.setElem(i,j, dblVal);
                    }
            }



    /*
     *  Matrix Attributes
     */

    /**
     * Returns the number of rows in this matrix.  Specifically, if 
     * this matrix, denoted <b>M</b>, is in <b>R</b><sup><i>m</i>&times;<i>n</i></sup>,
     * then the returned value is <i>m</i>.
     * 
     * @return  the first dimension in the shape of this matrix.
     *
     * @author Christopher K. Allen
     * @since  Oct 14, 2013
     */
    public int getRowCnt() {
        return this.cntRows;
    }
    
    /**
     * Returns the number of columns in this matrix.  Specifically, if 
     * this matrix, denoted <b>M</b>, is in <b>R</b><sup><i>m</i>&times;<i>n</i></sup>,
     * then the returned value is <i>n</i>.
     * 
     * @return  the second dimension in the shape of this matrix.
     *
     * @author Christopher K. Allen
     * @since  Oct 14, 2013
     */
    public int getColCnt() {
        return this.cntCols;
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
    public double getElem(int i, int j) throws ArrayIndexOutOfBoundsException  {
        return this.getMatrix().get(i,j);
    }

    /**
     * <p>
     * Returns the matrix element at the position indicated by the
     * given row and column index sources.  
     * </p>
     * <h3>NOTES</h3>
     * <p>
     * &middot; It is expected that the
     * object exposing the <code>IIndex</code> interface is an enumeration
     * class restricting the number of possible index values.
     * <br>
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
    public double getElem(IIndex indRow, IIndex indCol) {
        double  dblVal = this.matImpl.get(indRow.val(), indCol.val());
    
        return dblVal;
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
    public double[][] getArrayCopy() {
        return this.matImpl.getArrayCopy();
    }

    
    /*
     * Matrix Operations
     */
    
    /**
     * Create a deep copy of the this matrix object.  The returned 
     * object is completely decoupled from the original.
     * 
     * @return  a deep copy object of this matrix
     */
    public M copy() {
    
        M  matClone = this.newInstance();
        ((BaseMatrix<M>)matClone).assignMatrix( this.getMatrix() );
            
        return matClone;
    }

    /**
     *  Assign this matrix to be the zero matrix, specifically
     *  the matrix containing all 0's. 
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2013
     */
    public void assignZero() {
        for (int i=0; i<this.getRowCnt(); i++)
            for (int j=0; j<this.getColCnt(); j++)
                this.setElem(i, j, 0.0);
    }

    /**
     * Ratio of the largest singular value over the smallest singular value.
     * Note that this method does a singular value decomposition just to
     * get the number (done in the (wasteful) <code>Jama.Matrix</code>
     * internal implementation).  Thus, this computation is not cheap
     * if the matrix is large.
     * 
     * @return      the ratio of extreme singular values
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public double conditionNumber() {
//        double dblCondNum = this.matImpl.cond();
        
        // Do a singular value decomposition 
        SingularValueDecomposition  svd = this.matImpl.svd();
        double[] arrDblSv = svd.getSingularValues();
        
        // Make a list of singular values
        LinkedList<Double>   lstDblSv = new LinkedList<Double>();
        for (double dblSv : arrDblSv)
            lstDblSv.add(dblSv);

        // Create a comparator to sort the singular values from
        //  smallest to largest
        Comparator<Double> ifcSorter = new Comparator<Double>() {
            @Override
            public int compare(Double d1, Double d2) {
                
                if (d1 < d2)
                    return -1;
                
                if (d1 == d2)
                    return 0;
                //else (d1 > d2)
                return +1;
            }
        };
        
        // Sort the singular values the get the largest and smallest
        lstDblSv.sort(ifcSorter);
        double  dblMaxSv = lstDblSv.getLast();
        double  dblMinSv = lstDblSv.getFirst();
        
        // Compute the condition number, ratio of largest to smallest singular values
        double dblCndNum = dblMaxSv/dblMinSv;
        
        return dblCndNum;
    }
    
    
    /*
     *  Algebraic Operations
     */

    /**
     *  Non-destructive matrix addition. This matrix is unaffected.
     *
     *  @param  matAddend     matrix to be added to this
     *
     *  @return         the result of this matrix plus the given matrix (element-wise), 
     *                  or <code>null</code> if error
     */
    public M plus(M matAddend) {
        Jama.Matrix    impAdd = ((BaseMatrix<M>)matAddend).getMatrix();
        Jama.Matrix    impSum = this.getMatrix().plus( impAdd );
        M              matAns = this.newInstance(impSum);

        return matAns;
    }

    /**
     *  In-place matrix addition. The given matrix is added to this matrix 
     *  algebraically (element by element).
     *
     *  @param  mat     matrix to be added to this (no new objects are created)
     */
    public void plusEquals(M  mat) {
        BaseMatrix<M>     matBase = (BaseMatrix<M>)mat;
        
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
    public M minus(M matSub) {
        Jama.Matrix    impSub = ((BaseMatrix<M>)matSub).getMatrix();
        Jama.Matrix    impDif = this.getMatrix().minus( impSub );
        M              matAns = this.newInstance(impDif);

        return matAns;
    }

    /**
     *  In-place matrix subtraction.  The given matrix is subtracted from the
     *  value of this matrix.  No additional objects are created.
     *
     *  @param  mat     subtrahend
     */
    public void minusEquals(M mat) {
        BaseMatrix<M> matBase = (BaseMatrix<M>)mat;
        
        this.getMatrix().minusEquals( matBase.getMatrix() );
    }


    /*
     *  Topological Operations
     */

    /**
     * <p>
     * Return the maximum absolute value of all matrix elements.  This can
     * be considered a norm on matrices, but it is not sub-multiplicative.
     * That is,
     * <br>
     * <br>
     * ||<b>AB</b>||<sub>max</sub> is not necessarily bound by ||<b>A</b>||<sub>max</sub> ||<b>B</b>||<sub>max</sub> .    
     * <br>
     * <br>
     * </p>
     * 
     * @return  max<sub><i>i,j</i></sub> | <b>A</b><sub><i>i,j</i></sub> | 
     */
    public double max() {
        double      val = 0.0;
        double      max = Math.abs(getElem(0,0));
        
        for (int i=0; i<this.getRowCnt(); i++)
            for (int j=0; j<this.getColCnt(); j++) {
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
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>1</sub> &equiv; max<sub><b>x</b>&in;<b>R</b><sup><i>n</i></sup></sub> ||<b>Ax</b>||<sub>1</sub>
     *  <br>
     *  <br>
     *  where, by context, the second occurrence of ||&middot;||<sub>1</sub> is the 
     *  Lesbeque 1-norm on <b>R</b><sup><i>n</i><sup>. 
     *  </p>
     *  <h3>NOTES:</h3>
     *  <p>
     *  &middot; For square matrices induced norms are sub-multiplicative, that is
     *  ||<b>AB</b>|| &le; ||<b>A</b>|| ||<b>B</b>||.
     *  <br>
     *  <br>
     *  &middot; The ||&middot;||<sub>1</sub> induced norm equates to the 
     *  the maximum absolute column sum.
     *  </p>
     *
     *  @return     ||<b>M</b>||<sub>1</sub> = max<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> |<i>M<sub>i,j</i></sub>|
     */
    public double norm1() { return this.getMatrix().norm1(); }

    /**
     *  <p>
     *  Returns the <i>l</i><sub>2</sub> induced norm of this matrix, 
     *  which is the maximum, which turns out to be the spectral radius
     *  of the matrix. Specifically,
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>2</sub> &equiv; [ max &lambda;(<b>A</b><sup><i>T</i></sup><b>A</b>) ]<sup>1/2</sup> ,
     *  <br>
     *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = max &rho;(<b>A</b>) ,                                 
     *  <br>
     *  <br>
     *  where &lambda;(&middot;) is the eigenvalue operator and &rho;(&middot;) is the 
     *  singular value operator.
     *  </p>
     *
     *  @return     the maximum singular value of this matrix
     */
    public double norm2() { return this.getMatrix().norm2(); }

    /**
     *  <p>
     *  The matrix norm || &middot; ||<sub>&infin;</sub> <b>induced</b> from 
     *  the <i>l</i><sub>&infin;</sub> vector norm on <b>R</b><sup><i>n</i></sup>.  That is,
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; ||<b>A</b>||<sub>&infin;</sub> &equiv; max<sub><b>x</b>&in;<b>R</b><sup><i>n</i></sup></sub> 
     *                                                      ||<b>Ax</b>||<sub>&infin;</sub>
     *  <br>
     *  <br>
     *  where, by context, the second occurrence of ||&middot;||<sub>&infin;</sub> is the 
     *  Lesbeque &infin;-norm on <b>R</b><sup><i>n</i><sup>. 
     *  </p>
     *  <h3>NOTES:</h3>
     *  <p>
     *  &middot; For square matrices induced norms are sub-multiplicative, that is
     *  ||<b>AB</b>|| &le; ||<b>A</b>|| ||<b>B</b>||.
     *  <br>
     *  <br>
     *  &middot; The ||&middot;||<sub>&infin;</sub> induced norm equates to the 
     *  the maximum absolute column sum.
     *  </p>
     *
     *  @return     ||<b>M</b>||<sub>1</sub> = max<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> |<i>M<sub>i,j</i></sub>|
     */
    public double normInf() { return this.getMatrix().normInf(); }

    /**
     * <p>
     * Return the Frobenius norm ||<b>A</b>||<sub><i>F</i></sub> . 
     * The Frobenius norm has the property that it is 
     * both the element-wise Lebesgue 2-norm the Schatten 2-norm.  Thus we have
     * <br>
     * <br>
     * &nbsp; &nbsp; ||<b>A</b>||<sub><i>F</i></sub> = [ &Sigma;<sub><i>i</i></sub> &Sigma;<sub><i>j</i></sub> <i>A</i><sub><i>i,j</i></sub><sup>2</sup> ]<sup>1/2</sup>
     *                  = [ Tr(<b>A</b><sup><i>T</i></sup><b>A</b>) ]<sup>1/2</sup> 
     *                  = [ &Sigma;<sub><i>i</i></sub> &sigma;<sub><i>i</i></sub><sup>2</sup> ]<sup>1/2</sup>
     * <br>
     * <br>
     * where Tr is the trace operator and &sigma;<sub><i>i</i></sub> are the singular values of
     * matrix <b>A</b>.  
     * </p>
     * <h3>NOTES</h3>
     * <p>
     * &middot; Since the Schatten norms are sub-multiplicative, the Frobenius norm
     * is sub-multiplicative.
     * <br>
     * <br>
     * &middot; The Frobenius norm is invariant under rotations by elements of 
     * <i>O</i>(2) &sub; <b>R</b><sup><i>n</i>&times;<i>n</i></sup> .
     * </p>
     * 
     * 
     *  @return     ||<b>A</b>||<sub><i>F</i></sub> = [ &Sigma;<sub><i>i,j</i></sub> <i>A<sub>ij</sub></i><sup>2</sup> ]<sup>1/2</sup>
     */
    public double normF() { 
        return this.getMatrix().normF(); 
    }

    
    /*
     *  Testing and Debugging
     */

    /**
     *  Print out the contents of the R2x2 in text format.
     *
     *  @param  os      output stream to receive text dump
     */
    public void print(PrintWriter os) {
        this.matImpl.print(os, new DecimalFormat("0.#####E0"), this.getColCnt());
    }

    
    /*
     * IArchive Interface
     */
    
    /**
     * Save the value of this <code>PhaseMatrix</code> to a data sink 
     * represented by the <code>DataAdaptor</code> interface.
     * 
     * @param daptArchive   interface to data sink 
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daptArchive) {
        daptArchive.setValue(ATTR_DATA, this.toString());
    }

    /**
     * Restore the value of the this <code>PhaseMatrix</code> from the
     * contents of a data archive.
     * 
     * @param daptArchive   interface to data source
     * 
     * @throws DataFormatException      malformed data
     * @throws IllegalArgumentException wrong number of string tokens
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daptArchive) throws DataFormatException {
        if ( daptArchive.hasAttribute(PhaseMatrix.ATTR_DATA) )  {
            String  strValues = daptArchive.stringValue(PhaseMatrix.ATTR_DATA);
            this.setMatrix(strValues);         
        }
    }

    /*
     * Object Overrides
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
        //boolean bResult = this.equals(objTest);	// this code causes an infinite recursion
		final boolean bResult = super.equals( objTest );
        
        return bResult;
    }

    /**
     *  Convert the contents of the matrix to a string representation.
     *  The format is similar to that of Mathematica. Specifically,
     *  <br>
     *  <br>
     *      { {a b }{c d } }
     *  <br>
     *
     *  @return     string representation of the matrix
     */
    @Override
    public String toString() {
        // double is 15 significant digits plus the spaces and brackets
        final int size = (this.getRowCnt()*this.getColCnt() * 16) + (this.getRowCnt()*2) + 4; 
        StringBuffer strBuf = new StringBuffer(size);
    
        synchronized(strBuf) { // get lock once instead of once per append
            strBuf.append("{ ");
            for (int i=0; i<this.getRowCnt(); i++) {
                strBuf.append("{ ");
                for (int j=0; j<this.getColCnt(); j++) {
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
     * Returns a string representation of this matrix.  The string contains 
     * multiple lines, one for each row of the matrix.  Within each line the
     * matrix entries are formatted.  Thus, the string should resemble the 
     * usual matrix format when printed out.
     * 
     * @return  multiple line formatted string containing matrix elements in matrix format
     *
     * @author Christopher K. Allen
     * @since  Feb 8, 2013
     */
    public String   toStringMatrix() {
        
        return this.toStringMatrix(SCI_FORMAT);
    }

    /**
     * Returns a string representation of this matrix.  The string contains 
     * multiple lines, one for each row of the matrix.  Within each line the
     * matrix entries are formatted according to the given number format.  
     * The default column width is used.
     * The string should resemble the usual matrix format when printed out.
     * 
     * @param   fmt     <code>NumberFormat</code> object containing output format for matrix entries
     * 
     * @return  multiple line formatted string containing matrix elements in matrix format
     *
     * @author Christopher K. Allen
     * @since  Feb 8, 2013
     */
    public String   toStringMatrix(NumberFormat fmt) {
        return  this.toStringMatrix(fmt, INT_COL_WD_DFLT);
    }
    
    /**
     * Returns a string representation of this matrix.  The string contains 
     * multiple lines, one for each row of the matrix.  Within each line the
     * matrix entries are formatted according to the given number format.  
     * The string should resemble the usual matrix format when printed out.
     * 
     * @param   fmt         <code>NumberFormat</code> object containing output format for matrix entries
     * @param   intColWd    number of characters used for each column (padding is with spaces)
     * 
     * @return  multiple line formatted string containing matrix elements in matrix format
     *
     * @author Christopher K. Allen
     * @since  Feb 8, 2013
     */
    public String   toStringMatrix(NumberFormat fmt, int intColWd) {
        StringWriter sw = new StringWriter();
        PrintWriter  pw = new PrintWriter(sw);
        
        matImpl.print(pw, fmt, intColWd);
        
        return  sw.toString();
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
        for (int i=0; i<this.getRowCnt(); i++) {
            for (int j= 0; j<this.getColCnt(); j++) {
                bits = bits * 31 + Double.doubleToLongBits(getElem(i,j));;
            }
        }
    
        return (((int) bits) ^ ((int) (bits >> 32)));
    }

    
    /*
     * Internal Support
     */
    
    /**
     *  Return the internal matrix representation.
     *
     *  @return     the Jama matrix object
     */
    protected Jama.Matrix getMatrix() { 
        return matImpl; 
    }

    /**
     * Sets the internal matrix value to that given in the argument. This
     * is a deep copy operation.  Note that the complete matrix is copy,
     * thus the dimensions and other parameters are assigned as well.
     * 
     * @param matValue  internal implementation of matrix values
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    protected void assignMatrix(Jama.Matrix matValue) {
//        for (int i=0; i<this.getRowCnt(); i++)
//            for (int j=0; j<this.getColCnt(); j++) {
//                double dblVal = matValue.get(i, j);
//                
//                this.matImpl.set(i, j, dblVal);
//            }
        double[][]  arrInternal = matValue.getArrayCopy();
        
        this.matImpl = new Jama.Matrix(arrInternal);
    }

    /**
     * <p>
     * Creates a new, uninitialized instance of this matrix type.
     * </p>
     * <p>
     * NOTE:
     * &middot; This method was made abstract by Ivo List.  Rather than use 
     * reflection to instantiate new objects, this function is now delegated
     * to the concrete classes.  This architecture is more robust and allows
     * the compiler to do more error checking.
     * </p>
     * 
     * @return  uninitialized matrix object of type <code>M</code>
     * 
     * @author Ivo List
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    protected abstract M newInstance();
//    protected M newInstance() throws InstantiationException {
//        try {
//            M matNewInst = this.ctrType.newInstance();
//    
//            return matNewInst;
//    
//        } catch (InstantiationException   | 
//                IllegalAccessException   | 
//                IllegalArgumentException | 
//                InvocationTargetException e) {
//    
//            throw new InstantiationException("Unable to copy matrix " + this.getClass().getName());
//        }
//    }
    
    
    /**
     * Creates a new instance of this matrix type initialized to the given
     * implementation matrix.
     * 
     * @param   impInit implementation matrix containing initialization values    
     * 
     * @return          initialized matrix object of type <code>M</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 1, 2013
     */
    protected M newInstance(Jama.Matrix impInit)     {
        
        M   matNewInst = this.newInstance();
        
        ((BaseMatrix<M>)matNewInst).assignMatrix(impInit);
        
        return matNewInst;
    }
    
//    public <U extends Vector<U>, V extends Vector<V>>  U times (V vec) {
//        return null;
//    }

    
    /*
     * Child Class Support
     */

    /** 
     * Creates a new, uninitialized instance of a square matrix with the given
     * matrix dimensions. The matrix contains all zeros.
     *  
     * @param  cntRows    the matrix row count of this object
     * @param  cntCols    the matrix column count
     *  
     * @throws UnsupportedOperationException  child class has not defined a public, zero-argument constructor
     */
    protected BaseMatrix(int cntRows, int cntCols) /*throws UnsupportedOperationException*/ {
        this.cntRows = cntRows;
        this.cntCols = cntCols;
        this.matImpl = new Jama.Matrix(cntRows, cntCols, 0.0);
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
    protected BaseMatrix(M matParent) {
        this.cntRows = matParent.getRowCnt();
        this.cntCols = matParent.getColCnt();
        
        BaseMatrix<M> matBase = (BaseMatrix<M>)matParent;
        this.assignMatrix(matBase.getMatrix()); 
    }
    
    /**
     *  Parsing Constructor - creates an instance of the child class and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  cntRows     the matrix row size of this object
     *  @param  cntCols     the matrix column size of this object
     *  @param  strTokens   token vector of getSize()^2 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    protected BaseMatrix(int cntRows, int cntCols, String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        this(cntRows, cntCols);
        
        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strTokens, " ,()[]{}");
        
        if (tokArgs.countTokens() != this.getRowCnt()*this.getColCnt())
            throw new IllegalArgumentException("SquareMatrix, wrong number of token in string initializer: " + strTokens);
        
        
        // Extract initial phase coordinate values
        
        for (int i=0; i<this.getRowCnt(); i++)
            for (int j=0; j<this.getColCnt(); j++) {
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
     * @param cntRows     the matrix row size of this object
     * @param cntCols     the matrix column size of this object
     * @param arrMatrix   Java primitive array containing new matrix values
     * 
     * @exception  ArrayIndexOutOfBoundsException  the argument must have the same dimensions as this matrix
     *
     * @author Christopher K. Allen
     * @since  Oct 4, 2013
     */
    protected BaseMatrix(int cntRows, int cntCols, double[][] arrVals) throws ArrayIndexOutOfBoundsException {
        this.cntRows = cntRows;
        this.cntCols = cntCols;
        
        this.matImpl = new Jama.Matrix(arrVals);
    }


    
}
