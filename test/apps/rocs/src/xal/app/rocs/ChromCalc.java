//This class will perform a four point interpolation
//based on the user given x and y cords that are
//read into the program in mnClass.java

package xal.app.rocs;

import java.math.*;
import java.util.*;
import java.io.*;

public class ChromCalc extends ChromSettings{

    //Variable declaration
    //public int xi, yj;
    public double distx, disty;           //grid spacing variables
    public double deltax, deltay;         //fractional distance
    public double sext[] = new double[4];  //final answers for magnet families   
    public double ptx, pty;               //initial point x and y
    public int i;                        //loop counter

    //Member function to calculate the four point
    // interpolation for each sextupole family
    public double[] pointInterpolation(int xi, int yj, double inx, double iny)
	throws Exception{
	
	readData();

	//Initialize the sext[] values
	for(i = 0; i < 4; i++)
	    sext[i] = 0;

	//initialize the x and y starting position
	ptx = chrom_x[xi][yj];
	pty = chrom_y[xi][yj];

	if(ptx == chrom_x[imax][yj]){   //Initialize delta variables
	    deltax = 0.0;              //to zero if the user input
	}                              //number is the max to be used
	else{
	    distx = chrom_x[xi+1][yj] - ptx;  
	    deltax =  (inx - chrom_x[xi][yj]) / distx;
	}
	if(pty == chrom_y[xi][jmax]){
	    deltay= 0.0;
	}
	else{
	    disty = chrom_y[xi][yj+1] - pty;
	    deltay =  (iny - chrom_y[xi][yj]) / disty;
	}
	
	System.out.println("Deltas are " + deltax + " " + deltay);

	//sext1 magnet family
	sext[0] = ( (1-deltax)*(1-deltay)*(sext1[xi][yj])+
		    (deltax)*(1-deltay)*(sext1[xi+1][yj])+
		    (1-deltax)*(deltay)*(sext1[xi][yj+1])+
		    (deltax*deltay)*(sext1[xi+1][yj+1]));
	
	//sext2 magnet family
	sext[1] = ( (1-deltax)*(1-deltay)*(sext2[xi][yj])+
		    (deltax)*(1-deltay)*(sext2[xi+1][yj])+
		    (1-deltax)*(deltay)*(sext2[xi][yj+1])+
		    (deltax*deltay)*(sext2[xi+1][yj+1]));
	
	//sext3 magnet family
	sext[2] = ( (1-deltax)*(1-deltay)*(sext3[xi][yj])+
		    (deltax)*(1-deltay)*(sext3[xi+1][yj])+
		    (1-deltax)*(deltay)*(sext3[xi][yj+1])+
		    (deltax*deltay)*(sext3[xi+1][yj+1]));
	
	//sext4 magnet family
	sext[3] = ( (1-deltax)*(1-deltay)*(sext4[xi][yj])+
		    (deltax)*(1-deltay)*(sext4[xi+1][yj])+
		    (1-deltax)*(deltay)*(sext4[xi][yj+1])+
		    (deltax*deltay)*(sext4[xi+1][yj+1]));
	
	return sext;
    }

    public double getSext1(){
	return sext[0];
    }

    public double getSext2(){
	return sext[1];
    }

    public double getSext3(){
	return sext[2];
    }

    public double getSext4(){
	return sext[3];
    }

}
