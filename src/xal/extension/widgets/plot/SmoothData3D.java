package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;

/**
 * This class is a data class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values
 * will be presented as colored rectangles in the plot. It uses 9-points smooth
 * interpolation to calculate z-value between grid points. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class SmoothData3D extends ColorSurfaceData{

    /**  The data set constructor with size of the grid.*/   
    public SmoothData3D(int nX, int nY){
	super(nX,nY);
    }

    /**  Returns the interpolated value of the 2D array for x and y. */
    public double getValue(double x, double y){

	int i,j;
	double fracX,fracY;
	double Wxm, Wx0, Wxp, Wym, Wy0, Wyp;
	double Vm,  V0,  Vp;


	if(x < x_min || y < y_min || x > x_max || y > y_max){
	    return z_min;
	}

        i = (int) ((x-x_min)/x_step + 0.5);
        j = (int) ((y-y_min)/y_step + 0.5);

        if( i < 1) i = 1;
        if( i > (nX-2)) i = nX-2;
        if( j < 1) j = 1;
        if( j > (nY-2)) j = nY-2;	        

        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	Wxm = 0.5*(0.5 - fracX)*(0.5 - fracX);
	Wxp = 0.5*(0.5 + fracX)*(0.5 + fracX);
	Wx0 = 0.75 - fracX*fracX;

	Wym = 0.5*(0.5 - fracY)*(0.5 - fracY);
	Wyp = 0.5*(0.5 + fracY)*(0.5 + fracY);
	Wy0 = 0.75 - fracY*fracY;

	Vm = Wxm*gridData[i-1][j-1]+Wx0*gridData[i][j-1]+Wxp*gridData[i+1][j-1];
	V0 = Wxm*gridData[i-1][j]  +Wx0*gridData[i][j]  +Wxp*gridData[i+1][j];
	Vp = Wxm*gridData[i-1][j+1]+Wx0*gridData[i][j+1]+Wxp*gridData[i+1][j+1];

        return Wym*Vm + Wy0*V0 + Wyp*Vp;
    }

    /**  Bins value into the 2D array for x and y with weight = value. */
    public void addValue(double x, double y, double value){
	int i,j;
	double fracX,fracY;
	double Wxm,Wx0,Wxp,Wym,Wy0,Wyp,tmp;

        i = (int) ((x-x_min)/x_step + 0.5);
        j = (int) ((y-y_min)/y_step + 0.5);

        if( i < 1) i = 1;
        if( i > (nX-2)) i = nX-2;
        if( j < 1) j = 1;
        if( j > (nY-2)) j = nY-2;

        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	Wxm = 0.5 * (0.5 - fracX) * (0.5 - fracX);
	Wx0 = 0.75 - fracX * fracX;
	Wxp = 0.5 * (0.5 + fracX) * (0.5 + fracX);
	Wym = 0.5 * (0.5 - fracY) * (0.5 - fracY);
	Wy0 = 0.75 - fracY * fracY;
	Wyp = 0.5 * (0.5 + fracY) * (0.5 + fracY);

	tmp = Wym * value;
	gridData[i-1][j-1]  += Wxm * tmp;
	gridData[i  ][j-1]  += Wx0 * tmp;
	gridData[i+1][j-1]  += Wxp * tmp;
	tmp = Wy0 * value;
	gridData[i-1][j]    += Wxm * tmp;
	gridData[i  ][j]    += Wx0 * tmp;
	gridData[i+1][j]    += Wxp * tmp;
	tmp = Wyp * value;
	gridData[i-1][j+1]  += Wxm * tmp;
	gridData[i  ][j+1]  += Wx0 * tmp;
	gridData[i+1][j+1]  += Wxp * tmp;

	for(int ii = -1; ii < 2; ii++){
	    for(int jj = -1; jj < 2; jj++){
		if(z_min > gridData[i+ii][j+jj]) z_min = gridData[i+ii][j+jj];
		if(z_max < gridData[i+ii][j+jj]) z_max = gridData[i+ii][j+jj];        
	    }
	}

    }

   /**  The test method of this class. */
    public static void main(String args[]) {

	int nx = 200;
        int ny = 300;

	ColorSurfaceData data = new SmoothData3D(nx,ny);

        double minX  = -2;
        double minY  = -3;
        double maxX  =  2;
        double maxY  =  3;        
	double stepX = (maxX - minX)/(nx-1);
        double stepY = (maxY - minY)/(ny-1);  

	data.setMinMaxX(minX,maxX);
	data.setMinMaxY(minY,maxY);

	double x,y,v;

	for(int i = 0; i < nx; i++){
	for(int j = 0; j < ny; j++){
	    x = minX + i*stepX;
	    y = minY + j*stepY;
            v = Math.exp(-(x*x+y*y));
            data.setValue(i,j,v);
            //data.addValue(x,y,v);
	}}

	double max_dev = 0;
	int i_max = 0, j_max = 0;

	for(int i = 6; i < nx-6; i++){
	for(int j = 6; j < ny-6; j++){
	    x = i*stepX + minX + 0.5*stepX;
	    y = j*stepY + minY + 0.5*stepY;
            v = Math.abs(Math.exp(-(x*x+y*y)) - data.getValue(x,y))/Math.exp(-(x*x+y*y));
            if(max_dev < v) {
              max_dev = v;
	      i_max = i;
	      j_max = j;
	    }
	}}      

	System.out.println("max dev [%] = "+max_dev*100);

	double v_calc = 0;
	x = i_max*stepX + minX + 0.5*stepX;
	y = j_max*stepY + minY + 0.5*stepY; 
	v = Math.exp(-(x*x+y*y));
	v_calc =  data.getValue(x,y);
  	System.out.println("stepX = "+stepX);
  	System.out.println("stepY = "+stepY);
  	System.out.println("i_max = "+i_max);
  	System.out.println("j_max = "+j_max);
  	System.out.println("x = "+x);
  	System.out.println("y = "+y);
  	System.out.println("v = "+v);
 	System.out.println("v_calc = "+v_calc);   

    }
}
