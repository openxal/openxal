/**
 * TestR2x2.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 27, 2013
 */
package xal.tools.math.r2;

import static org.junit.Assert.*;

import org.junit.Ignore;
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

	/**
	 * Test method for {@link xal.tools.math.r2.R2x2#zero()}.
	 */
	@Test
	public void testZero() {
		R2x2	matTest = R2x2.zero();
		
//		fail("Not able to create a zero matrix");
	}

	/**
	 * Test method for {@link xal.tools.math.r2.R2x2#getSize()}.
	 */
	@Test
	public void testGetSize() {
		R2x2 matTest = new R2x2();
		
		int	szMatrix = matTest.getSize();
		
		System.out.println("Test matrix dynamic size = " + szMatrix);
	}

	/**
	 * Test method for {@link xal.tools.math.r2.R2x2#getMatrixSize()}.
	 */
	@Test
	@Ignore
	public void testGetMatrixSize() {
		R2x2 matTest = new R2x2();
		
//		int	szMatrix = matTest.getMatrixSize();
//		
//		System.out.println("Test matrix static size = " + szMatrix);
	}

}
