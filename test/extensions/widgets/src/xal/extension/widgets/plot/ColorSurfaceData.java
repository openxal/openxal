package xal.extension.widgets.plot;

import java.util.*;
import java.awt.*;

/**
 * This class is a base abstract class for data used in the FunctionGraphsJPanel class.
 * This class contains 2D grid with values at the grid points. These values
 * will be presented as colored rectangles in the plot. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

abstract public class ColorSurfaceData{


    /** The values on the 2D grid. */
    protected double [][] gridData = new double [0][0];

    protected int nX, nY;

    protected double x_step, y_step;

    protected double x_min,x_max;
    protected double y_min,y_max;
    protected double z_min,z_max;

    private int nScreenX;
    private int nScreenY;

    private ColorGenerator colorGen =  RainbowColorGenerator.getColorGenerator();

    /**  The data set constructor with size of the grid.*/   
    public ColorSurfaceData(int nX, int nY){
	this.nX = nX;
        this.nY = nY;
        gridData = new double[nX][nY];
        x_min = -0.5; x_max = 0.5; 
        y_min =  0.5; y_max = 0.5;
	z_min =   0.; 
        z_max =   0.;
	if(nX > 1){
	    x_step = (x_max - x_min)/(nX-1);
	}
	else{
	    x_step = (x_max - x_min);
	}
	if(nY > 1){
	    y_step = (y_max - y_min)/(nY-1); 
	}
	else{
	    y_step = (y_max - y_min); 
	}
	nScreenX = 30;
	nScreenY = 30;
        setZero();
    }

    /**  Sets the color generator.*/ 
    public void setColorGenerator(ColorGenerator colorGen){
	if(colorGen != null){
	    this.colorGen = colorGen;
	}
    }

    /**  Returns the color generator instance.*/ 
    public ColorGenerator getColorGenerator(){
	return colorGen;
    }

    /**  Sets data size. It will clean the data inside.*/ 
    public void setSize(int nX,int nY){ 
	if(nX > gridData.length || nY > gridData[0].length){
          gridData = new double[nX][nY];
	}
	this.nX = nX;
        this.nY = nY;
	if(nX > 1){
	    x_step = (x_max - x_min)/(nX-1);
	}
	else{
	    x_step = (x_max - x_min);
	}
	if(nY > 1){
	    y_step = (y_max - y_min)/(nY-1); 
	}
	else{
	    y_step = (y_max - y_min); 
	}
	z_min =   0.; 
        z_max =   0.;
        setZero();
    }


    /**  Sets the screen resolution.*/ 
    public void setScreenResolution(int nScreenX,int nScreenY){
	this.nScreenX = nScreenX;
	this.nScreenY = nScreenY;
    }

    /**  Returns the horizontal screen resolution.*/ 
    public int getScreenSizeX(){
	return nScreenX;
    }

    /**  Returns the vertical screen resolution.*/ 
    public int getScreenSizeY(){
	return nScreenY;
    }

    /**  Returns the X size of the 2D array.*/ 
    public int getSizeX(){
	return nX;
    }

    /**  Returns the Y size of the 2D array.*/ 
    public int getSizeY(){
	return nY;
    }

    /**  Returns the X value of the grid for the index i.*/
    public double getX(int i){
	return (x_min + i*x_step);
    }

    /**  Returns the Y value of the grid for the index j.*/
    public double getY(int j){
	return (y_min + j*y_step);
    }

    /**  Returns the minimal X value of the grid. */
    public double getMinX(){
	return x_min;
    }

    /**  Returns the maximal X value of the grid. */
    public double getMaxX(){
	return x_max;
    }

    /**  Returns the minimal Y value of the grid. */
    public double getMinY(){
	return y_min;
    }

    /**  Returns the maximal Y value of the grid. */
    public double getMaxY(){
	return y_max;
    }

    /**  Returns the minimal Z value. */
    public double getMinZ(){
	return z_min;
    }

    /**  Returns the maximal Z value. */
    public double getMaxZ(){
	return z_max;
    }

    /**  Sets the minimal maximal X value of the grid. */
    public void setMinMaxX(double x_min, double x_max){
	this.x_min = x_min;
	this.x_max = x_max;
	if(nX > 1){
	    x_step = (x_max - x_min)/(nX-1);
	}
	else{
	    x_step = (x_max - x_min);
	}
    }

    /**  Sets the minimal maximal Y value of the grid. */
    public void setMinMaxY(double y_min, double y_max){
	this.y_min = y_min;
	this.y_max = y_max;
	if(nY > 1){
	    y_step = (y_max - y_min)/(nY-1);
	}
	else{
	    y_step = (y_max - y_min);
	}
    }

    /**  Sets all values of the 2D array to 0. */
    public void setZero(){
	for(int i =0,j; i < nX; i++){
	    for(j = 0; j < nY; j++){
		gridData[i][j] = 0.;
	    }
	}
	z_min =   0.; 
        z_max =   0.;
    }

    /**  Sets value of the 2D array with indexes i and j. */
    public void setValue(int i, int j, double value){
	gridData[i][j] = value;
        if(z_min > gridData[i][j]) z_min = gridData[i][j];
        if(z_max < gridData[i][j]) z_max = gridData[i][j];
    }


    /**  Returns the value of the 2D array with indexes i and j. */
    public double getValue(int i, int j){
	return gridData[i][j];
    }

    /**  Returns the interpolated value of the 2D array for x and y. 
     *   The subclasses should implement this method to provide specific
     *   interpolation scheme.
     */
    abstract public double getValue(double x, double y);

    /**  Bins value into the 2D array for x and y with weight = value.
     *   The subclasses should implement this method to provide specific
     *   interpolation scheme.
     */
    abstract public void addValue(double x, double y, double value);

    /**  Bins value into the 2D array for x and y with weight = 1.
     */
    public void addValue(double x, double y){
        addValue(x, y, 1.0);
    }


   /**  Multiplies all values of the 2D array by constant factor = value. */
    public void multiplyBy(double value){
 	z_min =   Double.MAX_VALUE; 
        z_max = - Double.MAX_VALUE;
	for(int i =0,j; i < nX; i++){
	    for(j = 0; j < nY; j++){
		gridData[i][j] *= value;
		if(z_min > gridData[i][j]) z_min = gridData[i][j];
		if(z_max < gridData[i][j]) z_max = gridData[i][j];
	    }
	} 
    }

   /**  Calculates minimal and maximal Z values. 
    *   In the begining all data = 0., and 
    *   if you want to get real min and max for Z
    *   you should use this method first.
    */
    public void calcMaxMinZ(){
 	z_min =   Double.MAX_VALUE; 
        z_max = - Double.MAX_VALUE;
	for(int i =0,j; i < nX; i++){
	    for(j = 0; j < nY; j++){
		if(z_min > gridData[i][j]) z_min = gridData[i][j];
		if(z_max < gridData[i][j]) z_max = gridData[i][j];
	    }
	} 
    }

   /**  Returns the color for (x,y) point. */
    public Color getColor(double x, double y){
        double value = getValue(x,y);
	if(z_max != z_min){
	    value = (value - z_min)/(z_max - z_min);
	    if(value > 1.0) value = 1.0;
	    if(value < 0.0) value = 0.0;
	}
	else{
	    value = 0.;
	}
        return colorGen.getColor(value);
    }
}
