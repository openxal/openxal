/**
 * TestJamaMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Jul 21, 2015
 */
package xal.tools.math;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Jul 21, 2015
 */
public class TestJamaMatrix {

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * Testing the internal structure of the Jama matrices.
     *
     * @since  Jul 21, 2015   by Christopher K. Allen
     */
    @Test
    public final void testJamaMatrix() {
        double[][]      arrMat = new double[4][2];
        
        arrMat[0] = new double[] {1,2};
        arrMat[1] = new double[] {3,4};
        arrMat[2] = new double[] {5,6};
        arrMat[3] = new double[] {7,8};
        
        Jama.Matrix matTest = new Jama.Matrix(arrMat);
//        System.out.println("matTest = "); 
//        matTest.print(matTest.getRowDimension(), matTest.getColumnDimension());
        
        double[][] arrTest = matTest.getArrayCopy();
        double[]   vecCol  = matTest.getColumnPackedCopy();
        double[]   vecRow  = matTest.getRowPackedCopy();
        int        szRow   = matTest.getRowDimension();
        int        szCol   = matTest.getColumnDimension();
        
        if (szRow != arrMat.length)
            fail("number of rows is not 4");
        if (szCol != arrMat[0].length)
            fail("number of columns is not 2");
        
        Jama.Matrix matCtrl = new Jama.Matrix(arrTest);
//        System.out.println("matCtrl = "); 
//        matCtrl.print(matCtrl.getRowDimension(), matCtrl.getColumnDimension());
        
        Jama.Matrix matRes = matCtrl.minus(matTest);
        double      dblErr = matRes.normInf();
        
        if (dblErr != 0.0)
            fail("The matrix internals are not consistent");
    }
    
}
