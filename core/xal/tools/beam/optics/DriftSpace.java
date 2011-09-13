/*
 * Created on Mar 11, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.tools.beam.optics;

/**
 * This is a utility class for computing properties of drift spaces.
 * 
 * @author Christopher K. Allen
 */
public class DriftSpace {


    /**
     * Compute the characteristic transfer matrix for a drift space 
     * of length <code>l</code>.
     * 
     * @param l     length of the drift space (in <b>meters</b>)
     * 
     * @return      2x2 transfer matrix for a phase plane drift
     */
    public static double[][] transferDriftPlane(double l) {
        double[][] arr0 = new double[][]
            {   { 1.0, l }, 
                { 0.0, 1.0 }
            };
     
        return arr0;   
    }
}
