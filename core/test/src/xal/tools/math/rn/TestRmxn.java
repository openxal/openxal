/**
 * TestRmxn.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 22, 2015
 */
package xal.tools.math.rn;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.tools.math.rn.Rmxn;
import xal.tools.math.rn.Rn;

/**
 * Test cases for class <code>Rmxn</code>
 *
 *
 * @author Christopher K. Allen
 * @since  Jul 22, 2015
 */
public class TestRmxn {
    
    
    /*
     * Global Constants
     */
    
    
    /** A 1D Java array */
    private final static double[]   DBL_ARR_VEC = new double[] { 0, 1, 2, 3, 4 };
    
    
    /** A column-dominant 2D Java array */
    private final static double[][] DBL_ARR_ROWDOM = new double[][] {
                                                       { 1.1, 1.2, 1.3}, 
                                                       { 2.1, 2.2, 2.3},
                                                       { 3.0, 3.2, 3.3}, 
                                                       { 4.1, 4.0, 4.3},
                                                       { 5.1, 5.2, 5.0}
                                                       };
                                                       
    /** A row-dominant 2D Java array */
    private final static double[][] DBL_ARR_COLDOM = new double[][] 
                                                     { {1.1, 1.2, 1.3, 1.4, 1.5},
                                                       {2.1, 2.2, 2.3, 2.0, 2.5},
                                                       {3.1, 3.2, 3.3, 3.4, 3.0}
                                                     };
                                                      
    /** a square 2D Java array */
    private final static double[][] DBL_ARR_SQR = new double[][] {
                                                           { 1.0, 1.2, 1.3, 1.4, 1.5}, 
                                                           { 2.1, 2.0, 2.3, 2.4, 2.5},
                                                           { 3.1, 3.2, 3.0, 3.4, 3.5}, 
                                                           { 4.1, 4.2, 4.3, 4.0, 4.5},
                                                           { 5.1, 5.2, 5.3, 5.4, 5.0}
                                                           };
    
                                                           
    /** The number of rows in the row dominated matrix */
    private final static int    CNT_ROWS_ROWDOM = DBL_ARR_ROWDOM.length;
    
    /** The number of columns in the column dominated matrix */
    private final static int    CNT_COLS_ROWDOM = DBL_ARR_ROWDOM[0].length;
                                                           

    /** The number of rows in the row dominated matrix */
    private final static int    CNT_ROWS_COLDOM = DBL_ARR_COLDOM.length;
    
    /** The number of columns in the column dominated matrix */
    private final static int    CNT_COLS_COLDOM = DBL_ARR_COLDOM[0].length;
    
    
    /** The size of the square matrix */
    private final static int    SZ_SQR = DBL_ARR_SQR.length;
                                                           

    /** The zero matrix with dimensions of row dominated matrices */                                                       
    private final static Rmxn   MAT_ZERO_ROWDOM = new Rmxn(CNT_ROWS_ROWDOM, CNT_COLS_ROWDOM);
                                                           
    /** The zero matrix with dimensions of column dominated matrices */                                                       
    private final static Rmxn   MAT_ZERO_COLDOM = new Rmxn(CNT_ROWS_COLDOM, CNT_COLS_COLDOM);
                        
    
    /** The zero matrix with square dimensions  */                                                       
    private final static Rmxn   MAT_ZERO_SQR = new Rmxn(SZ_SQR, SZ_SQR);
                                                           
    
    /** A small number used in comparing matrix elements (e.g., #isEqual() ) */
    protected static final double DBL_EPS  = 1.0e-12;

    
    

    /*
     * Global Methods                                                      
     */

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 22, 2015   by Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }


    /*
     * Tests
     */
    
    /**
     * Test method for {@link xal.tools.math.rn.Rmxn#plus(xal.tools.math.rn.Rmxn)}.
     * 
     * This also indirectly tests the method <code>{@link Rmxn#times(double)}</code>.
     */
    @Test
    public final void testPlusRmxn() {
//        Rmxn    matZero   = new Rmxn(CNT_ROWS_ROWDOM, CNT_COLS_ROWDOM);
        Rmxn    matRowDom = new Rmxn(DBL_ARR_ROWDOM.clone());
        Rmxn    matDbl    = matRowDom.times(2.0);

        Rmxn    matSum   = matRowDom.plus(MAT_ZERO_ROWDOM);
        if ( !matSum.isApproxEqual(matRowDom) )
            fail("Adding zero matrix failed");

        Rmxn    matTwice = matRowDom.plus(matRowDom);
        if ( !matTwice.isApproxEqual(matDbl) )
            fail("Adding matrix to itself M*M is not equal to 2*M");

        matRowDom.plusEquals(matRowDom);
        if ( !matRowDom.isApproxEqual(matDbl) )
            fail("In place addtion of matrix to itself is not twice the matrix");
    }
    
    /**
     * Test method for {@link xal.tools.math.rn.Rmxn#minus(xal.tools.math.rn.Rmxn)}.
     */
    @Test
    public final void testMinusRmxn() {
        Rmxn    matRowDom = new Rmxn(DBL_ARR_ROWDOM.clone());

        Rmxn    matDiff   = matRowDom.minus(MAT_ZERO_ROWDOM.clone());
        if ( !matDiff.isApproxEqual(matRowDom) )
            fail("Subtracting by zero matrix failed");

        Rmxn    matZero = matRowDom.minus(matRowDom);
        if ( !matZero.isApproxEqual(MAT_ZERO_ROWDOM.clone()) )
            fail("Subtracting by itself is not zero");

        matRowDom.minusEquals(matRowDom);
        if ( !matRowDom.isApproxEqual(MAT_ZERO_ROWDOM.clone()) )
            fail("In place subtraction of matrix from itself is not zero");
    }

    /**
     * Test method for {@link xal.tools.math.rn.Rmxn#times(xal.tools.math.rn.Rmxn)}.
     */
    @Test
    public final void testTimesRmxn() {
//        Rmxn    matZeroSqr = new Rmxn(SZ_SQR, SZ_SQR);
        
        Rmxn    matSqr     = new Rmxn(DBL_ARR_SQR.clone());
        Rmxn    matRowDom  = new Rmxn(DBL_ARR_ROWDOM.clone());
        Rmxn    matColDom  = new Rmxn(DBL_ARR_COLDOM.clone());
        
        Rmxn    matProdSqr = matSqr.times(MAT_ZERO_SQR);
        if ( !matProdSqr.isApproxEqual(MAT_ZERO_SQR) )
            fail("Multiplying by zero matrix failed");
        
        Rmxn    matProdOut = matRowDom.times(matColDom);
        
//        System.out.println("Matrix outer product = " + matProdOut.toString());
        
        if ( matProdOut.getRowCnt() != CNT_ROWS_ROWDOM || matProdOut.getColCnt() != CNT_COLS_COLDOM)
            fail("Matrix outer product has wrong dimensions");
        
        double  dblCondOut = matProdOut.conditionNumber();
//        System.out.println("Output product matrix condition number = " + dblCondOut);
        
        
        Rmxn    matProdIn = matColDom.times(matRowDom);
        
//        System.out.println("Matrix inner product = " + matProdIn.toString());
        
        if ( matProdIn.getRowCnt() != CNT_ROWS_COLDOM || matProdIn.getColCnt() != CNT_COLS_ROWDOM )
            fail("Matrix inner product has wrong dimensions");
        
        double  dblCondIn = matProdIn.conditionNumber();
//        System.out.println("Inner product matrix condition number = " + dblCondIn);
    }

    /**
     * Test method for {@link xal.tools.math.rn.Rmxn#times(xal.tools.math.rn.Rn)}.
     */
    @Test
    public final void testTimesRn() {
        Rn      vecDrv    = new Rn(DBL_ARR_VEC.clone());
        Rmxn    matColDom = new Rmxn(DBL_ARR_COLDOM.clone());
        
        Rn  vecOut = matColDom.times(vecDrv);
        
//        System.out.println("Matrix-vector product = " + vecOut);
        
        if ( vecOut.getSize() != matColDom.getRowCnt() )
            fail("Matrix-vector product is the wrong shape");
    }

    /**
     * Test method for {@link xal.tools.math.rn.Rmxn#clone()}.
     */
    @Test
    public final void testClone() {
        
        Rmxn    matColDom = new Rmxn(DBL_ARR_COLDOM.clone());
        Rmxn    matClone  = matColDom.clone();
        
        if ( !matClone.isApproxEqual(matColDom) )
            fail("The matrix clone is not equal to its template");
    }

    /**
     * Test method for {@link Rmxn#inverse()}
     *
     * @since  Jul 23, 2015   by Christopher K. Allen
     */
    @Test
    public final void testInverse() {
        Rmxn    matId  = Rmxn.newIdentity(SZ_SQR);
        
        Rmxn    matSqr = new Rmxn(DBL_ARR_SQR.clone());
        Rmxn    matInv = matSqr.inverse();
        
//        System.out.println("Matrix Inverse = " + matInv);
        
        Rmxn    matLt  = matInv.times(matSqr);
        double  dblErrLt = matLt.minus(matId).norm2();
        
//        System.out.println("Matrix Ainv*A = " + matLt);
        if ( dblErrLt > DBL_EPS )
            fail("Method inverse() failed to recover identity upon left multiplication");
        
        Rmxn    matRt = matSqr.times(matInv);
        double  dblErrRt = matRt.minus(matId).norm2();
        if ( dblErrRt > DBL_EPS )
            fail("Mathod inverse() failed to recover identity upon right multiplication");
        
    }
    
    /**
     * Test method for {@link Rmxn#newIdentity(int)}. 
     *
     * @since  Jul 23, 2015   by Christopher K. Allen
     */
    @Test 
    public final void testIdentity() {
        int     cntSize = 100;
        Rmxn    matId   = Rmxn.newIdentity(cntSize);
        
//        System.out.println("MatrixIdentity = " + matId);
        
        for (int i=0; i<cntSize; i++)
            for (int j=0; j<cntSize; j++) {
                double  dblElem = matId.getElem(i, j);
                
                if (i==j && dblElem != 1.0)
                    fail("Bad identity matrix: M(" + i + "," + j + ") = " + dblElem);
                
                if (i!=j && dblElem != 0.0)
                    fail("Bad identity matrix: M(" + i + "," + j + ") = " + dblElem);
            }
    }
    
}
