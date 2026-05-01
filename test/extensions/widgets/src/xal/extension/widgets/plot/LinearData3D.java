package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;

/**
 * This class is a data class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values
 * will be presented as colored rectangles in the plot. It uses 4-points
 * linear interpolation to calculate z-value between grid points. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class LinearData3D extends ColorSurfaceData{

    /**  The data set constructor with size of the grid.*/   
    public LinearData3D(int nX, int nY){
	super(nX,nY);
    }

    /**  Returns the interpolated value of the 2D array for x and y. */
    public double getValue(double x, double y){

	int i,j;
	double fracX,fracY;
	double Wxm,Wxp, Wym, Wyp;
	double Vm, Vp;

	if(x < x_min || y < y_min || x > x_max || y > y_max){
	    return z_min;
	}

        i = (int) ((x-x_min)/x_step + 0.5);
        j = (int) ((y-y_min)/y_step + 0.5);
	        
        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	if(fracX < 0.){
            i = i - 1;
	}

	if(fracY < 0.){
            j = j - 1;
	}

        if( i < 0) i = 0;
        if( i > (nX-2)) i = nX-2;
        if( j < 0) j = 0;
        if( j > (nY-2)) j = nY-2;

        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	Wxm = 1.0 - fracX;
	Wxp = fracX;


	Wym = 1.0 - fracY;
	Wyp = fracY;


	Vm = Wxm*gridData[i][j]+Wxp*gridData[i+1][j];
	Vp = Wxm*gridData[i][j+1]+Wxp*gridData[i+1][j+1];

        return Wym*Vm + Wyp*Vp;
    }

    /**  Bins value into the 2D array for x and y with weight = value. */
    public void addValue(double x, double y, double value){
	int i,j;
	double fracX,fracY;
	double Wxm,Wxp, Wym, Wyp;
	double Vm, Vp, tmp;

        i = (int) ((x-x_min)/x_step + 0.5);
        j = (int) ((y-y_min)/y_step + 0.5);
	        
        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	if(fracX < 0.){
            i = i - 1;
	}

	if(fracY < 0.){
            j = j - 1;
	}

        if( i < 0) i = 0;
        if( i > (nX-2)) i = nX-2;
        if( j < 0) j = 0;
        if( j > (nY-2)) j = nY-2;

        fracX = (x - x_min - i*x_step)/x_step;
        fracY = (y - y_min - j*y_step)/y_step;

	Wxm = 1.0 - fracX;
	Wxp = fracX;

	Wym = 1.0 - fracY;
	Wyp = fracY;

	tmp = Wym * value;
	gridData[i  ][j]  += Wxm * tmp;
	gridData[i+1][j]  += Wxp * tmp;
	tmp = Wyp * value;
	gridData[i  ][j+1]  += Wxm * tmp;
	gridData[i+1][j+1]  += Wxp * tmp;

	for(int ii = 0; ii < 2; ii++){
	    for(int jj = 0; jj < 2; jj++){
		if(z_min > gridData[i+ii][j+jj]) z_min = gridData[i+ii][j+jj];
		if(z_max < gridData[i+ii][j+jj]) z_max = gridData[i+ii][j+jj];        
	    }
	}


    }

}
