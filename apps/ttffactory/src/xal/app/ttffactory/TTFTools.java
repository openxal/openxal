/*TTFTools.java - Provides functions for calculating integral transit time factors, derivatives, R^2 fits, LS2 Norm errors, etc.
 * @author James Ghawaly Jr.
 * Created on Mon June 20 10:06:27 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.app.ttffactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class TTFTools.
 */
public class TTFTools {
	// establish some global variables
	final double c = 299792458.0;
	
	public double ttfAtPoint(List<Double> ZData, List<Double> EData, double beta, Boolean cos, Double frequency) {
		
		double v0 = 0.0;
		double ttf = 0.0;
		int N = EData.size();
		Double L = ZData.get(ZData.size() - 1);
		Double deltaZ = L/((double) N);
		
		// This for loop integrates the electric field to get the voltage across the field.
		for(int i=0;i<N;i++) {
			v0 += EData.get(i)*deltaZ*10000.0; //10,000 is for converting to SI units. Each E point is in MV/m and each Z point is in cm.
		}
		
		double k = kCalc(beta,frequency); // calculate k from beta
		
		// check if the user wants ttf or stf (cosine and sine transit time factor respectively)
		if(cos) {
			for(int i=0;i<N;i++) {
				ttf += EData.get(i)*Math.cos(k*ZData.get(i)*0.01)*deltaZ*10000.0;
			}
		}
		else {
			for(int i=0;i<N;i++) {
				ttf += EData.get(i)*Math.sin(k*ZData.get(i)*0.01)*deltaZ*10000.0;
			}
		}
		
		return ttf*(1/v0);
	}
	
	/**
	 * K calc.
	 *
	 * @param beta Beta
	 * @param f the frequency
	 * @return the double k
	 */
	public double kCalc(Double beta, Double f) {
		return (2.0*Math.PI*f)/(c*beta);
	}
	
	/**
	 * Gets the TTF for beta range.
	 *
	 * @param ZData the z coordinate data
	 * @param EData the electric field data
	 * @param cos set cos to True to get TTF, and False to get STF
	 * @param frequency the frequency
	 * @param betaRange equally spaced range from beta min to beta max
	 * @return the TTF for the specified range of betas
	 */
	public double[] getTTFForBetaRange(List<Double> ZData, List<Double> EData, Boolean cos, Double frequency, double[] betaRange) {
		//List<Double> ttfList = new ArrayList<Double>();
		double[] ttfList = new double[betaRange.length];
		int i = 0;
		for (double currBeta:betaRange) {
			ttfList[i] = ttfAtPoint(ZData, EData, currBeta, cos,frequency);
			i++;
		}
		
		return ttfList;
	}
	
	

	/**
	 * Linspace.
	 *
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param n the number of items in the list
	 * @return the equally spaced double array from min to max
	 */
	public double[] linspace(double min, double max, int n) {
		double[] betaRange = new double[n];
		double step = (max - min)/n;
		double currValue = min;

		betaRange[0] = min;
		betaRange[n-1] = max;
		
		int i = 1;
		while (i < n) {
			betaRange[i] = currValue + step;
			currValue += step;
			i++;
		}
		
		return betaRange;
	}

}