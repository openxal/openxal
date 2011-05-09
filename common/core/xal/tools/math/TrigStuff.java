/*
 * Unwrap.java
 *
 *  Created on January 12,2005
 *  jdg: stolen from Andrei's GraphDataOperationms class.
 *   This is usful lots of places.
 */

package xal.tools.math;

/** general purpose trig stuff not found elsewhere */

public class TrigStuff {


  /** this method shifts the first argument by a multiple of 2*PI to produce the nearest point to another specified point
  * @param y = input number
  * @param yIn = number to get the input close to (within 2pi)
  */
    static public double unwrap(double y,double yIn){
        if( y == yIn) return y;
        int n = 0;
	double diff = yIn - y;
        double diff_min = Math.abs(diff);
        double sign = diff/diff_min;
        int n_curr = n+1;
        double diff_min_curr = Math.abs(y + sign*n_curr*360. - yIn);
	while(diff_min_curr < diff_min){
	    n = n_curr;
            diff_min = Math.abs(y + sign*n*360. - yIn);
            n_curr++;
            diff_min_curr = Math.abs(y + sign*n_curr*360. - yIn);
	}
        return  (y + sign*n*360.); 
    }
}
