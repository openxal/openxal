/**
 * TestR2x2.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 27, 2013
 */
package xal.tools.math.r2;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * JUnit test cases for class <code>xal.math.r2.R2x2</code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 27, 2013
 */
@RunWith(JUnit4.class)
public class TestR2x2 {

    
    
    
    /*
     * Global Attributes
     */
    
    
    /** Static identity matrix */
    static private R2x2     MAT_I;
    
    /** static symplectic matrix */
    static private R2x2     MAT_J;
    
    /** Static testing matrix */
    static private R2x2     MAT_2;
    
    /** static testing matrix - a rotation */
    static private R2x2     MAT_R;
    

    @BeforeClass
    public static void buildTestingResources() {
        
        MAT_I = R2x2.newIdentity();
        MAT_J  = R2x2.newSymplectic();
        MAT_2 = MAT_I.plus( MAT_I );
        MAT_R = R2x2.newRotation(Math.PI/2.0);
    }
    
    
	/**
	 * Test method for {@link xal.tools.math.r2.R2x2#newZero()}.
	 */
	@Test
	public void testZero() {
		R2x2	matTest = R2x2.newZero();
		
//		fail("Not able to create a zero matrix");
	}

	/**
	 * Test method for {@link xal.tools.math.r2.R2x2#getSize()}.
	 */
	@Test
	public void testGetSize() {
		R2x2 matTest = new R2x2();
		
		int	szMatrix = matTest.getSize();
		
		Assert.assertTrue( szMatrix == 2 );
		
//		System.out.println("\nTest matrix dynamic size = " + szMatrix);
	}

	@Test
	public void testMatrixAddition() {
	    R2x2   mat1 = R2x2.newIdentity();
	    R2x2   mat2 = R2x2.newIdentity();
	    
	    R2x2   matSum = mat1.plus( mat2 );
	    
	    Assert.assertTrue( matSum.isEquivalentTo(MAT_2) );
	    
//	    System.out.println("\nThe matrix addition test");
//	    System.out.println( matSum.toString() );
	}

    @Test
    public void testMatrixInPlaceAddition() {
        R2x2   mat1 = R2x2.newIdentity();
        R2x2   mat2 = R2x2.newIdentity();
        
        mat1.plusEquals( mat2 );
        
        Assert.assertTrue( mat1.isEquivalentTo(MAT_2) );
        
//        System.out.println("\nThe matrix in place addition test");
//        System.out.println( mat1.toString() );
    }

    @Test
    public void testMatrixMultiplication() {
        R2x2   mat1 = R2x2.newIdentity();
        R2x2   mat2 = R2x2.newIdentity();
        
        R2x2   matProd = mat1.times( mat2 );
        
        Assert.assertTrue( matProd.isEquivalentTo(MAT_I) );
        
//        System.out.println("\nThe matrix multiplication test");
//        System.out.println( matProd.toString() );
    }

    @Test
    public void testMatrixInPlaceMultiplication() {
        R2x2   mat1 = R2x2.newIdentity();
        R2x2   mat2 = R2x2.newIdentity();
        
        mat1.timesEquals( mat2 );
        
        Assert.assertTrue( mat1.isEquivalentTo(MAT_I) );
        
//        System.out.println("\nThe matrix in place multiplication test");
//        System.out.println( mat1.toString() );
    }
    
    @Test
    public void testMatrixDeterminant() {
        double  dblDetSp2 = MAT_J.det();
        double  dblDetId  = MAT_I.det();
        double  dblDetTst2 = MAT_2.det();
        
//        System.out.println("\nDeterminant Function");
//        System.out.println("|I|  = " + dblDetId);
//        System.out.println("|2I| = " + dblDetTst2);
//        System.out.println("|J|  = " + dblDetSp2);
    }
    
    @Test
    public void testMatrixOperations() {
        R2x2    matTrn = MAT_J.transpose();
        R2x2    matInv = MAT_J.inverse();
        R2x2    matCjt = MAT_J.conjugateTrans(MAT_R);
        
//        System.out.println("\nMatrix Operations");
//        System.out.println("Sp(2) matrix J = " + MAT_J);
//        System.out.println("transpose of J = " + matTrn);
//        System.out.println("inverse of J   = " + matInv);
//        System.out.println("CT of J w/ Rot = " + matCjt);
    }
    
    @Test
    public void testMatrixNorm() {
        double  dblL1   = MAT_J.norm1();
        double  dblL2   = MAT_J.norm2();
        double  dblLinf = MAT_J.normInf();
        double  dblFrob = MAT_J.normF();
        
//        System.out.println("\nNorms of the Symplectic Matrix");
//        System.out.println("||J||_1   = " + dblL1);
//        System.out.println("||J||_2   = " + dblL2);
//        System.out.println("||J||_inf = " + dblLinf);
//        System.out.println("||J||_F   = " + dblFrob);
    }
    
    @Test
    public void testRandom() {
    }

}
