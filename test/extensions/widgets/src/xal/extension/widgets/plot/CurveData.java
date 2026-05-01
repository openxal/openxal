package xal.extension.widgets.plot;

import java.awt.*;

/**
 * This class is a curve data class for data used in the FunctionGraphsJPanel class.
 * This class contains a set of 2D points that will be connected on the graph's plane.
 * This class does not update the graph panel automatically. User must call the
 * method <code> refreshGraphJPanel() </code> of the  FunctionGraphsJPanel
 * in the program.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class CurveData{

    private int nPoints = 0;
    private int nChunk = 50;

    private double [] pointsX = null;
    private double [] pointsY = null;

    private int nX, nY;

    private double x_min,x_max;
    private double y_min,y_max;

    private Color color = Color.black;

    private int lineWidth = 1;

    private BasicStroke lineStroke = new BasicStroke(1.0f);

    /**  The data set constructor.*/
    public CurveData(){
	pointsX = new double[nChunk];
	pointsY = new double[nChunk];
        x_min =  Double.MAX_VALUE;
        x_max = -Double.MAX_VALUE;
        y_min =  Double.MAX_VALUE;
        y_max = -Double.MAX_VALUE;
    }

    /**  Sets the color of the curve.*/
    public void setColor(Color color){
	if(color != null){
	    this.color = color;
	}
    }

    /**  Returns the color of the curve.*/
    public Color getColor(){
	return color;
    }

    /**  Deletes all points.*/
    public void clear(){
	nPoints = 0;
        x_min =  Double.MAX_VALUE;
        x_max = -Double.MAX_VALUE;
        y_min =  Double.MAX_VALUE;
        y_max = -Double.MAX_VALUE;
    }

    /**  Returns number of points.*/
    public int getSize(){
	return nPoints;
    }

    /**  Sets the line width.*/
    public void setLineWidth(int lineWidth){
	this.lineWidth = lineWidth;
        lineStroke = new BasicStroke((float) lineWidth,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL);
    }

    /**  Returns the line width.*/
    public int getLineWidth(){
	return lineWidth;
    }

   /** returns the stroke for drawing.*/
    public BasicStroke getStroke(){
	return lineStroke;
    }

   /** sets the stroke for drawing.*/
    public void setStroke(BasicStroke lineStroke){
	   this.lineStroke = lineStroke;
		 this.lineWidth = (int) lineStroke.getLineWidth();
    }

    /**  Sets the points.*/
    public void setPoints(double [] x, double [] y){
	if(x.length == y.length){
	    x_min =  Double.MAX_VALUE;
	    x_max = -Double.MAX_VALUE;
	    y_min =  Double.MAX_VALUE;
	    y_max = -Double.MAX_VALUE;
	    resize(x.length);
            for(int i = 0;  i < x.length; i++ ){
		pointsX[i] = x[i];
		pointsY[i] = y[i];
		if(x_min > x[i]) x_min = x[i];
		if(y_min > y[i]) y_min = y[i];
		if(x_max < x[i]) x_max = x[i];
		if(y_max < y[i]) y_max = y[i];
	    }
	    nPoints = x.length;
	}
    }

    /**  Adds a point.*/
    public void addPoint(double x, double y){
	resize(nPoints+1);
	pointsX[nPoints] = x;
	pointsY[nPoints] = y;
	nPoints++;
	if(x_min > x) x_min = x;
	if(y_min > y) y_min = y;
	if(x_max < x) x_max = x;
	if(y_max < y) y_max = y;
    }

	/**  Finds min and max values.*/
	public void findMinMax(){
		x_min =  Double.MAX_VALUE;
	    x_max = -Double.MAX_VALUE;
	    y_min =  Double.MAX_VALUE;
	    y_max = -Double.MAX_VALUE;
		for(int i = 0;  i < nPoints; i++ ){
		if(x_min > pointsX[i]) x_min = pointsX[i];
		if(y_min > pointsY[i]) y_min = pointsY[i];
		if(x_max < pointsX[i]) x_max = pointsX[i];
		if(y_max < pointsY[i]) y_max = pointsY[i];
		}
	}


    /**  Sets a particular point with the index i.*/
    public void setPoint(int i, double x, double y){
	if(i < nPoints){
	    pointsX[i] = x;
	    pointsY[i] = y;
	    if(x_min > x) x_min = x;
	    if(y_min > y) y_min = y;
	    if(x_max < x) x_max = x;
	    if(y_max < y) y_max = y;
	}
    }

    /**  Returns the x-value for index i.*/
    public double getX(int i){
	return pointsX[i];
    }

    /**  Returns the y-value for index i.*/
    public double getY(int i){
	return pointsY[i];
    }

    /**  Returns the minimal X value. */
    public double getMinX(){
	return x_min;
    }

    /**  Returns the maximal X value. */
    public double getMaxX(){
	return x_max;
    }

    /**  Returns the minimal Y value. */
    public double getMinY(){
	return y_min;
    }

    /**  Returns the maximal Y value. */
    public double getMaxY(){
	return y_max;
    }

    private void resize(int nSize){
	if( nSize > pointsX.length){
	    double [] tmp_x = new double[nSize + nChunk];
	    double [] tmp_y = new double[nSize + nChunk];
            for(int i = 0;  i < pointsX.length; i++ ){
		tmp_x[i] = pointsX[i];
		tmp_y[i] = pointsY[i];
	    }
            pointsX = tmp_x;
            pointsY = tmp_y;
	}
    }
}
