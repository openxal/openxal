package xal.extension.fit;


import java.io.*;
import java.util.*;
import java.text.*;
import Jama.Matrix;

/**
 *
 * @author  jdg
 *
 * This is taken from M. Abdallah & J. Wang, U. Md., 1999
 *
 *		LEAST SQUARES ANALYSIS
 *
 * This program will input a series of m 2-D points and create a best fit
 * polynomial equation.
 * The governing equation is of the form:
 *                        y = co + c1*x + ... + ck*x^k
 *                        
 * The system of equations becomes:
 *			Y= A*C
 * The least square normal equation looks like: A'AC = AY;
 * where, C = (c0, c1, ..., ck)'
 *
 * The least squares result is computed through the  normal equation:
 *  			A'*A*C = A*Y;
 * where the equation is represented by AC=Y.
 *  and :
 *
 * (dX, dY)-    coordinate arrays for data points
 * m-      	int for number of data points 
 * k-		int for order of poly equation
 * mC-		Matrix for the constant coefficients of equation
 * dR-		correlation coefficient  
 * getValue(dx)-   returns value of y coordinate from equation
 * Correlation()-  returns the correlation coefficient as a String
 * Equation()- 	returns the characteristic equation as a String
*/ 

import java.io.*;
import java.util.*;
import java.text.*;
import Jama.Matrix;

public class PolyLeastsquares{

   private int m;		//number of data points
   private int k;		//order of best fit equation

   private double[] dX, dY;      //points to fit

   private Matrix mC;		//equation coefficent vector
   private double dR; 		//correlation coefficient factor

    /**
     * constructor
     */

   public PolyLeastsquares(double[] dX, double[] dY, int k) throws Exception {

      this.dY= dY;
      this.dX= dX;

      if(dX.length != dY.length)
	  throw new PolyLeastsquaresException("X & Y not equal length");

      if(dX.length <= k)
	  throw new PolyLeastsquaresException("You requested too high a polynimial order, for the number of points");
      this.m= dX.length;
      this.k= k;
      Init();
   }

    /**
     * Initialize various stuff
     */ 

   private void Init(){

      Matrix mA, mAl, mY;

      double[][] dCols= new double[m][k+1];

      for (int i=0; i<m; i++){
         for (int j=0; j<k+1; j++){
            dCols[i][j]= Math.pow(dX[i], j);
         }
      }
      mA= new Matrix(dCols,m,k+1);      mY= new Matrix(dY,m);
 
      mAl= (mA.transpose()).times(mA);	//mAl= mA'mA
      mC= mAl.inverse().times(mA.transpose()).times(mY);
            
      return;
   }

    /**
     * Calculate the predicted y value for an inputed x
     */

   public double getValue(double dX){
      double dY= 0;

      for (int j=0; j<k+1; j++){
         dY += mC.get(j,0)*Math.pow(dX, j); 
      }

      return dY;
   }
    /**
     * Find the correlation coefficient for the fit
     */

   public String Correlation(){
            
      //dR is the correlation coefficient
      double dMean, dVarUnexplained, dVarTotal;
      double[] dYfit =  new double[m];

   //to calculate dYfit:
      for (int i=0; i<m; i++){

         dYfit[i]= mC.get(0,0);
         for (int j=1; j<k+1; j++){
            dYfit[i] += mC.get(j,0)*Math.pow(dX[i],j); 
         }
         //System.out.println(""+ dYfit[i]);
      }

      //  calculate square sums:

      dMean= dY[0]/m;
      for (int i=1; i<m; i++){
         dMean += dY[i]/m;
      }

      dVarUnexplained= Math.pow( dY[0]-dYfit[0], 2);
      dVarTotal= Math.pow( dY[0]-dMean, 2);
      for (int i=1; i<m; i++){
         dVarUnexplained += Math.pow( dY[i]-dYfit[i], 2);
         dVarTotal += Math.pow( dY[i]-dMean, 2);
      }
      
    //calculate correlation coefficient:
      dR= Math.sqrt( 1-dVarUnexplained/dVarTotal );

      //System.out.println(""+dMean+dVarUnexplained+dVarTotal+dR);

      //to round it to 4 decimal places
      dR= Math.round( dR*10000.0 ) /10000.0;

      String sR= ""+dR;
      if (dR > 1) sR= " -- ";

      return sR;
   }

    /**
     * Return the characteristic equation as a String
     */

   public String Equation(){
      String eq= new String();

      //round the constants to two decimal places
      double dz;
      for (int i=0; i<k+1; i++){
         dz= Math.round( mC.get(i,0) *100.0 ) /100.0;
         mC.set(i,0, dz);
      }


      eq= "Y = " +mC.get(0,0)+ " + " +mC.get(1,0) + " X";
      for (int j=2; j<k+1; j++){
         eq += " + " +mC.get(j,0)+ " X^" +j;       
      }

     return eq;
   }
}

