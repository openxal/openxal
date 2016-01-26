/*TTFTools.java - Provides functions for calculating integral transit time factors, derivatives, R^2 fits, LS2 Norm errors, etc.
 * @author James Ghawaly Jr.
 * Created on Mon June 20 10:06:27 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */
package xal.app.ttffactory;

import java.util.List;

/**
 * The Class TTFTools.
 */
public class TTFTools {
	// establish some global variables
	final double c = 299792458.0;
	
	/**
	 * K calc.
	 *
	 * @param beta Beta
	 * @param f the frequency
	 * @return the double k
	 */
	public double kCalc(double beta, double f) {
		return (2.0*Math.PI*f)/(c*beta);
	}
	
	public double[] kCalcArray(double[] betaArray, double f) {
		double[] kArray = new double[betaArray.length];
		
		for(int i=0;i<betaArray.length;i++) {
			kArray[i] = (2.0*Math.PI*f)/(c*betaArray[i]);
		}
		
		return kArray;
	}
	
	/**
	 * Gets the TTF for beta range.
	 *
	 * @param ZData the z coordinate data
	 * @param EData the electric field data
	 * @param frequency the frequency
	 * @param betaRange equally spaced range from beta min to beta max
	 * @return the TTF for the specified range of betas
	 */
	public double[] getTTFForBetaRange(List<Double> ZData, List<Double> EData,Double frequency, double[] betaRange) {

		double[] ttfList = new double[betaRange.length];
		int i = 0;
		for (double currBeta:betaRange) {
			ttfList[i] = TTF(ZData, EData, currBeta, frequency);
			i++;
		}
		
		return ttfList;
	}
	
	/**
	 * Gets the STF for beta range.
	 *
	 * @param ZData the z coordinate data
	 * @param EData the electric field data
	 * @param frequency the frequency
	 * @param betaRange equally spaced range from beta min to beta max
	 * @return the STF for the specified range of betas
	 */
	public double[] getSTFForBetaRange(List<Double> ZData, List<Double> EData,Double frequency, double[] betaRange) {

		double[] stfList = new double[betaRange.length];
		int i = 0;
		for (double currBeta:betaRange) {
			stfList[i] = STF(ZData, EData, currBeta, frequency);
			i++;
		}
		
		return stfList;
	}
	
	/**
	 * Gets the TTFP for beta range.
	 *
	 * @param ZData the z coordinate data
	 * @param EData the electric field data
	 * @param frequency the frequency
	 * @param betaRange equally spaced range from beta min to beta max
	 * @return the TTFP for the specified range of betas
	 */
	public double[] getTTFPForBetaRange(List<Double> ZData, List<Double> EData,Double frequency, double[] betaRange) {

		double[] ttfpList = new double[betaRange.length];
		int i = 0;
		for (double currBeta:betaRange) {
			ttfpList[i] = TTFP(ZData, EData, currBeta, frequency);
			i++;
		}
		
		return ttfpList;
	}
	
	/**
	 * Gets the STFP for beta range.
	 *
	 * @param ZData the z coordinate data
	 * @param EData the electric field data
	 * @param frequency the frequency
	 * @param betaRange equally spaced range from beta min to beta max
	 * @return the STFP for the specified range of betas
	 */
	public double[] getSTFPForBetaRange(List<Double> ZData, List<Double> EData,Double frequency, double[] betaRange) {

		double[] stfpList = new double[betaRange.length];
		int i = 0;
		for (double currBeta:betaRange) {
			stfpList[i] = STFP(ZData, EData, currBeta, frequency);
			i++;
		}
		
		return stfpList;
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
	
	public double TTF(List<Double> ZData, List<Double> EData, double beta, Double frequency) {
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
		
		for(int i=0;i<N;i++) {
			ttf += EData.get(i)*Math.cos(k*ZData.get(i)*0.01)*deltaZ*10000.0;
		}
		
		return ttf*(1/v0);
	}
	
	public double STF(List<Double> ZData, List<Double> EData, double beta, Double frequency) {
		
		double v0 = 0.0;
		double stf = 0.0;
		int N = EData.size();
		Double L = ZData.get(ZData.size() - 1);
		Double deltaZ = L/((double) N);
		
		// This for loop integrates the electric field to get the voltage across the field.
		for(int i=0;i<N;i++) {
			v0 += EData.get(i)*deltaZ*10000.0; //10,000 is for converting to SI units. Each E point is in MV/m and each Z point is in cm.
		}
		
		double k = kCalc(beta,frequency); // calculate k from beta


		for(int i=0;i<N;i++) {
			stf += EData.get(i)*Math.sin(k*ZData.get(i)*0.01)*deltaZ*10000.0;
		}
		
		return stf*(1/v0);
	}

	public double TTFP(List<Double> ZData, List<Double> EData, double beta, Double frequency) {
		
		double v0 = 0.0;
		double ttfp = 0.0;
		int N = EData.size();
		Double L = ZData.get(ZData.size() - 1);
		Double deltaZ = L/((double) N);
		
		// This for loop integrates the electric field to get the voltage across the field.
		for(int i=0;i<N;i++) {
			v0 += EData.get(i)*deltaZ*10000.0; //10,000 is for converting to SI units. Each E point is in MV/m and each Z point is in cm.
		}
		
		double k = kCalc(beta,frequency); // calculate k from beta
		
		for(int i=0;i<N;i++) {
			ttfp += EData.get(i)*Math.sin(k*ZData.get(i)*0.01)*deltaZ*10000.0*ZData.get(i)*0.01;
		}
		
		return ttfp*(1/v0);
	}
	
	public double STFP(List<Double> ZData, List<Double> EData, double beta, Double frequency) {
		
		double v0 = 0.0;
		double stfp = 0.0;
		int N = EData.size();
		Double L = ZData.get(ZData.size() - 1);
		Double deltaZ = L/((double) N);
		
		// This for loop integrates the electric field to get the voltage across the field.
		for(int i=0;i<N;i++) {
			v0 += EData.get(i)*deltaZ*10000.0; //10,000 is for converting to SI units. Each E point is in MV/m and each Z point is in cm.
		}
		
		double k = kCalc(beta,frequency); // calculate k from beta
		
		for(int i=0;i<N;i++) {
			stfp += EData.get(i)*Math.cos(k*ZData.get(i)*0.01)*deltaZ*10000.0*ZData.get(i)*0.01;
		}
		
		return stfp*(1/v0);
	}
}