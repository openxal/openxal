/*
 * PolynomialFit.java Fits a 5th order polynomial to given x and y x
 * @author James Ghawaly Jr.
 * @author Doug Brown
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;


import java.util.Arrays;

import xal.tools.math.rn.Rmxn;

public class PolynomialFit{
    final double[] x;
    final double[] y;
    
    public PolynomialFit(double[] xDat, double[] yDat){
    	x = xDat;
    	y = yDat;

    }
    
    public double[] getPolyConstants() {
//    	Tools tools = new Tools();
    	
    	double[][] x2D = new double[x.length][5];
    	
    	/*
    	 * Create a 2D Nx5 matrix from the Beta values
    	 * where N is the length of x: e.g. the number of betas
    	 * 
    	 * [1 B1 B1^2 B1^3 B1^4]
    	 * [1 B2 B2^2 B2^3 B2^4]
    	 * [...                ]
    	 * [1 BN BN^2 BN^3 BN^4]
    	 */
    	int i =0;
    	for(double dbl:x){
    		x2D[i][0] = 1.0;
    		x2D[i][1] = dbl;
    		x2D[i][2] = Math.pow(dbl, 2.0);
    		x2D[i][3] = Math.pow(dbl, 3.0);
    		x2D[i][4] = Math.pow(dbl, 4.0);
    		i++;
    	}
    	
    	Rmxn xMat = new Rmxn(x2D.clone());               //K
    	
    	Rmxn yMat = new Rmxn(y.length,1);                //T
    	
    	i = 0;
    	for(double dbl:y) {
    		yMat.setElem(i, 0, dbl);
    		i++;
    	}
    	
    	Rmxn xT = xMat.transpose();                  //KT

    	Rmxn xTx = xT.times(xMat);                   //KT K

    	Rmxn xTy = xT.times(yMat);                   //KT T

    	Rmxn xTxI = xTx.inverse();                   //(KT K)^-1

    	Rmxn c = xTxI.times(xTy);                    //polynomial coefficients

    	double[] polyConstants = new double[5];
    	
    	for(int rowIndex = 0;rowIndex<c.getRowCnt();rowIndex++) {
    		polyConstants[rowIndex] = c.getElem(rowIndex, 0);
    	}
    	
		return polyConstants;
    	
    }
    
    /**
     * Norm.
     *
     * @param c the double array of polynomial constants
     * @return the norm of the matrix: double
     */
    public double norm(double[] c) {
    	double total = 0.0;
    	for(double dbl: c) {
    		total+=Math.pow(dbl,2.0);
    	}
    	return Math.sqrt(total);
    }
    
    /**
     * A string representing the polynomial
     *
     * @param c the double array of polynomial constants
     * @return the string representing the polynomial
     */
    public String toStringPolynomialRep(double[] c) {
    	return sign(c[0]) + "(" + Math.abs(c[0]) + ")" + sign(c[1]) + "(" + Math.abs(c[1]) + ")x" + sign(c[2])
    			+ "(" + Math.abs(c[2]) + ")x^2" + sign(c[3]) + "(" + Math.abs(c[3]) + ")x^3" + sign(c[4]) + "(" + Math.abs(c[4]) + ")x^4";
    }
    
    /**
     * A string representing the constants of the polynomial
     *
     * @param c the double array of polynomial constants
     * @return the string representing the polynomial
     */
    public String toStringConsts(double[] c) {
    	return Arrays.toString(c).replace("[", "").replace("]", "").trim().replaceAll(" ", "");
    }
    
    
    /**
     * Checks if the given double is positive.
     *
     * @param dbl the double 
     * @return the boolean: true if positive, false if negative
     */
    private Boolean isPos(double dbl) {
		Boolean pos = null;
		if(dbl>Double.MIN_VALUE) {
			pos = true;
		} else {
			pos = false;
		}
		return pos;
	};
	
	/**
	 * Sign. Calls isPos and returns the string representation of the sign of the double: "-" or "+"
	 *
	 * @param dbl the double to receve the sign of
	 * @return the string representing the sign of the double: "-" or "+"
	 */
	private String sign(double dbl) {
		String signString = "";
		if(isPos(dbl)) {
			signString = "+";
		} else {
			signString = "-";
		}
		return signString;
	}
	
    
}

