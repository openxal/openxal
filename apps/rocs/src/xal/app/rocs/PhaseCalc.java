/**********************************************************
//
//class PhaseCalc_Achr: 
// will perform a four point interpolation
// based on the user given x and y coord.
//
/**********************************************************/

package xal.app.rocs;
import java.math.*;
import java.util.*;
import java.io.*;

public class PhaseCalc extends PhaseSettings{

    //Variable declaration
    //public int xi, yj;
    public double distx, disty;         //grid spacing variables
    public double deltax, deltay;       //fractional distance
    public double k[] = new double[6];  //final answers for magnet families   
    public double ptx, pty;             //initial point x and y
    public int count = 0;

    public double[] pointInterpolation(int xi, int yj, double inx, 
				       double iny, int choice)
	throws Exception{
	
	readData(choice);  //read in data

	//Initialize the k[] values
	for(count = 0; count < 6; count++)
	    k[count] = 0;

	//initialize the x and y starting position
	ptx = phase_x[xi][yj];
	pty = phase_y[xi][yj];

	if(ptx == phase_x[imax][yj]){   // Initialize delta variables
	    deltax = 0.0;               // to zero if the user input
	}                               // number is the max to be used
	else{
	    distx = phase_x[xi+1][yj] - ptx;
	    deltax =  (inx - phase_x[xi][yj]) / distx;
	}
	if(pty == phase_y[xi][jmax]){
	    deltay= 0.0;
	}
	else{
	    disty = phase_y[xi][yj+1] - pty;
	    deltay =  (iny - phase_y[xi][yj]) / disty;
	}


	//Four point interpolation calculations
	//kd magnet family
	k[0] = ( (1-deltax)*(1-deltay)*(kd[xi][yj])+
		 (deltax)*(1-deltay)*(kd[xi+1][yj])+
		 (1-deltax)*(deltay)*(kd[xi][yj+1])+
		 (deltax*deltay)*(kd[xi+1][yj+1]));
	
	//kfs magnet family
	k[1] = ( (1-deltax)*(1-deltay)*(kfs[xi][yj])+
		 (deltax)*(1-deltay)*(kfs[xi+1][yj])+
		 (1-deltax)*(deltay)*(kfs[xi][yj+1])+
		 (deltax*deltay)*(kfs[xi+1][yj+1]));

	//kfl magnet family
	k[2] = ( (1-deltax)*(1-deltay)*(kfl[xi][yj])+
		 (deltax)*(1-deltay)*(kfl[xi+1][yj])+
		 (1-deltax)*(deltay)*(kfl[xi][yj+1])+
		 (deltax*deltay)*(kfl[xi+1][yj+1]));

	//kdee magnet family
	k[3] = ( (1-deltax)*(1-deltay)*(kdee[xi][yj])+
		 (deltax)* (1-deltay)*(kdee[xi+1][yj])+
		 (1-deltax)*(deltay)*(kdee[xi][yj+1])+
		 (deltax*deltay)*(kdee[xi+1][yj+1]));

	//kdc magnet family
	k[4] = ( (1-deltax)*(1-deltay)*(kdc[xi][yj])+
		 (deltax)*(1-deltay)*(kdc[xi+1][yj])+
		 (1-deltax)*(deltay)*(kdc[xi][yj+1])+
		 (deltax*deltay)*(kdc[xi+1][yj+1]));

	//kfc magnet family
	k[5] = ( (1-deltax)*(1-deltay)*(kfc[xi][yj])+
		 (deltax)*(1-deltay)*(kfc[xi+1][yj])+
		 (1-deltax)*(deltay)*(kfc[xi][yj+1])+
		 (deltax*deltay)*(kfc[xi+1][yj+1]));
	
	return k;
	
    }

    //get methods to access the individual k array values
    public double getK0(){
	return k[0];
    }

    public double getK1(){
	return k[1];
    }

    public double getK2(){
	return k[2];
    }

    public double getK3(){
	return k[3];
    }

    public double getK4(){
	return k[4];
    }

    public double getK5(){
	return k[5];
    }
}









