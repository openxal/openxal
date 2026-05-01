/*
 * Created on Sep 29, 2003
 */
package xal.tools.text;

import xal.tools.text.DoubleToString;

import junit.framework.TestCase;

/**
 * @author Craig McChesney
 */
public class StringBufferTest extends TestCase {
	
	/**
	 * Entry point for the JUnit test suite.
	 * 
	 * @param args command line arguments (not used)
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public static void main(String[] args) {
		junit.textui.TestRunner.run(StringBufferTest.class);
	}

//	public void testAppendDouble() {
//		StringBuffer buf = new StringBuffer();
//		double d = 42.0;
//		buf.append(d);
//	}

	/**
	 *  Test the <code>{@link DoubleToString#append(StringBuffer, double)}</code>
	 *  method.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 19, 2011
	 */
	public void testDoubleToStringClassAppendMethod() {
		double d = 42.0;
		StringBuffer buf = new StringBuffer();
		DoubleToString.append(buf, d);		
	}

//	public void testDoubleToString() {
//		double d = 42.0;
//		String s = Double.toString(d);
//	}

}
