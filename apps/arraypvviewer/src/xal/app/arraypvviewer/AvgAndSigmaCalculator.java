/*
 *  AvgAndSigmaCalculator.java
 *
 *  Created on July 12, 2004
 */
package xal.app.arraypvviewer;

import xal.extension.widgets.plot.CurveData;

/**
 *  This class calculates average value and dispersion of the array
 *
 *@author     shishlo
 *@since    June 1, 2004
 */
public class AvgAndSigmaCalculator {

	private static double[] dArr = new double[2];

	private static ValuesGraphPanel vgp = null;


	/**
	 *  Constructor for the AvgAndSigmaCalculator object
	 */
	private AvgAndSigmaCalculator() { }


	/**
	 *  Sets the valuesGraphPanel attribute of the AvgAndSigmaCalculator class
	 *
	 *@param  vgpIn  The new valuesGraphPanel value
	 */
	public static void setValuesGraphPanel(ValuesGraphPanel vgpIn) {
		vgp = vgpIn;
	}


	/**
	 *  Calculates average value and dispersion of the array
	 *
	 *@param  cd         CurveData instance
	 *@return            array with average and sigma values
	 */
	public static double[] calculateAvgAndSigma(CurveData cd) {

		boolean isLimited = false;
		double x_min = -Double.MAX_VALUE;
		double x_max = Double.MAX_VALUE;

		if (vgp != null) {
			isLimited = vgp.useLimits();
			x_min = vgp.getMinLim();
			x_max = vgp.getMaxLim();
		}
		
		dArr[0] = 0.;
		dArr[1] = 0.;

		double sigma = 0.;

		double z_sum = 0.;
		double z2_sum = 0.;
		double z = 0.;
		double x = 0.;

		double n = cd.getSize();

		if (n == 0) {
			return dArr;
		}

		int count = 0;

		if (isLimited) {
			for (int i = 0; i < n; i++) {
				x = cd.getX(i);
				if (x >= x_min && x <= x_max) {
					z = cd.getY(i);
					z_sum += z;
					z2_sum += z * z;
					count++;
				}
			}
		} else {
			for (int i = 0; i < n; i++) {
				x = cd.getY(i);
				z = cd.getY(i);
				z_sum += z;
				z2_sum += z * z;
				count++;
			}
		}

		dArr[0] = z_sum;

		if (count <= 1) {
			return dArr;
		}

		z_sum /= count;

		sigma = (z2_sum - count * z_sum * z_sum) / ((count-1));
		sigma = Math.sqrt(Math.abs(sigma));

		dArr[0] = z_sum;
		dArr[1] = sigma;

		return dArr;
	}

}

