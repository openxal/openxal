package xal.app.emittanceanalysis.phasespaceanalysis;

import java.util.*;
import java.awt.*;

import xal.extension.widgets.plot.*;

/**
 * This class keeps raw phase space data and can create
 * the ColorSurfaceData instance to show the phase space
 * distribution as a color contour plot. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class PhaseSpaceData{

    private int nChunk = 10000;
    private int nPoints = 0;

    private double [] pointsX = null;
    private double [] pointsXP = null;

    private double X_sum  = 0.;
    private double XP_sum = 0.;

    private double x_min, x_max;
    private double xp_min,xp_max;

    private ColorSurfaceData data = null;

    private PhasePlaneEllipse innerPhasePlaneEllipse = new PhasePlaneEllipse();

    //--------------------------------------------------------
    // grid parameters - we use the grid for integration over
    // the phase space density
    //-------------------------------------------------------- 
    private int gridSizeX  = 100;
    private int gridSizeXP = 100;
    private double stepX  = 0.;
    private double stepXP = 0.;

    /**  The data constructor.*/   
    public PhaseSpaceData(int dataSizeX, int dataSizeXP){
	data = Data3DFactory.getDefaultData3D(dataSizeX,dataSizeXP);
	init();
    }

    /**  The data constructor with data name.*/   
    public PhaseSpaceData(int dataSizeX, int dataSizeXP, String dataName){
	data = Data3DFactory.getData3D(dataSizeX,dataSizeXP,dataName);
	init();
    }

    private void init(){
	pointsX = new double[nChunk];
	pointsXP = new double[nChunk];
        x_min =  Double.MAX_VALUE;
        x_max = -Double.MAX_VALUE;
        xp_min =  Double.MAX_VALUE;
        xp_max = -Double.MAX_VALUE;
    }

    /**  Sets the grid size for analysis in X direction.*/ 
    public void setGridSizeX(int gridSizeX){
	this.gridSizeX = gridSizeX;
	makeColorSurfaceData();
    }

    /**  Sets the grid size for analysis in XP direction.*/ 
    public void setGridSizeXP(int gridSizeXP){
	this.gridSizeXP = gridSizeXP;
	makeColorSurfaceData();
    }

    /**  Returns the grid size for analysis in X direction.*/ 
    public int getGridSizeX(){
	return gridSizeX;
    }

    /**  Returns the grid size for analysis in XP direction.*/ 
    public int getGridSizeXP(){
	return gridSizeXP;
    }

    /**  Returns the step size for analysis in X direction.*/ 
    public double getStepX(){
	return stepX;
    }

    /**  Returns the step size for analysis in XP direction.*/ 
    public double getStepXP(){
	return stepXP;
    }

    /**  Returns the grid point's X for index i.*/ 
    public double getGridPointX(int i){
	return (i+0.5)*stepX;
    }

    /**  Returns the grid point's XP for index i.*/ 
    public double getGridPointXP(int i){
	return (i+0.5)*stepXP;
    }

    /**  Returns phase space density value for point (x,xp).*/ 
    public double getValue(double x,double xp){
	return data.getValue(x,xp);
    }

    /**  Returns phase space density value for point with indexes i and j.*/ 
    public double getValue(int i, int j){
	return data.getValue((i+0.5)*stepX,(j+0.5)*stepXP);
    }

    /**  Deletes all points.*/ 
    public void clear(){
	nPoints = 0;
        x_min =  Double.MAX_VALUE;
        x_max = -Double.MAX_VALUE;
        xp_min =  Double.MAX_VALUE;
        xp_max = -Double.MAX_VALUE;
	data.setZero();
        stepX = 0.;
        stepXP = 0.;
	X_sum  = 0.;
	XP_sum = 0.;
    }

    /**  Returns a number of the raw data points.*/ 
    public int getSize(){
	return nPoints;
    }

    /**  Adds a raw data point.*/ 
    public void addPoint(double x, double xp){
	resize(nPoints+1);
	pointsX[nPoints] = x;
	pointsXP[nPoints] = xp;
	X_sum  += x;
	XP_sum += xp;
	nPoints++; 
	if(x_min  > x)   x_min  = x;
	if(xp_min > xp)  xp_min = xp;
	if(x_max  < x)   x_max  = x;
	if(xp_max < xp)  xp_max = xp;
    }

    /**  Returns the averaged X for the initial raw data.*/ 
    public double getCenterX(){
	if(nPoints > 0){
	    return X_sum/nPoints;
	}
	return 0.;
    }

    /**  Returns the averaged XP for the initial raw data.*/ 
    public double getCenterXP(){
	if(nPoints > 0){
	    return XP_sum/nPoints;
	}
	return 0.;
    }

    /**  Returns the x-value for index i.*/ 
    public double getX(int i){
	return pointsX[i];
    }

    /**  Returns the xp-value for index i.*/ 
    public double getXP(int i){
	return pointsXP[i];
    }

    /**  Returns the minimal X value. */
    public double getMinX(){
	return x_min;
    }

    /**  Returns the maximal X value. */
    public double getMaxX(){
	return x_max;
    }

    /**  Returns the minimal XP value. */
    public double getMinXP(){
	return xp_min;
    }

    /**  Returns the maximal XP value. */
    public double getMaxXP(){
	return xp_max;
    }

    /**  Creates contour plot data. After that you can get ColorSurfaceData instance.*/
    public void makeColorSurfaceData(){
	data.setZero();
	if(nPoints > 0){
	    x_min =  Double.MAX_VALUE;
	    x_max = -Double.MAX_VALUE;
	    xp_min =  Double.MAX_VALUE;
	    xp_max = -Double.MAX_VALUE;
	    for(int i = 0; i < nPoints; i++){
		if(x_min  > pointsX[i])   x_min  = pointsX[i];
		if(xp_min > pointsXP[i])  xp_min = pointsXP[i];
		if(x_max  < pointsX[i])   x_max  = pointsX[i];
		if(xp_max < pointsXP[i])  xp_max = pointsXP[i];
	    } 
	    if(data.getSizeX() > 2 && data.getSizeY() > 2){
		double gapX = (x_max - x_min)/(data.getSizeX()-2);
		double gapXP = (xp_max - xp_min)/(data.getSizeY()-2);
		x_min = x_min -3*gapX;
		x_max = x_max +3*gapX;
		xp_min = xp_min -3*gapXP;
		xp_max = xp_max +3*gapXP;
	    }
	    data.setMinMaxX(x_min, x_max);
	    data.setMinMaxY(xp_min,xp_max);
	    for(int i = 0; i < nPoints; i++){
		data.addValue(pointsX[i],pointsXP[i],1.0);
	    }
            stepX  = (data.getMaxX() - data.getMinX())/(gridSizeX);
            stepXP = (data.getMaxY() - data.getMinY())/(gridSizeXP);
	    double S = 0.;
	    double x,xp;
            for(int i = 0; i < gridSizeX; i++){
		for(int j = 0; j < gridSizeXP; j++){
		    x  = stepX*i +0.5*stepX  + x_min;
		    xp = stepXP*j+0.5*stepXP + xp_min;
		    S += data.getValue(x,xp);
		}
	    }
	    S *= stepX*stepXP;
	    if(S != 0.0){
		data.multiplyBy(1.0/S);
	    }
	    calcInnerPhasePlaneEllipse();
	}
    }

    /**  Returns the ColorSurfaceData instance. */
    public ColorSurfaceData getColorSurfaceData(){
	return data;
    }

    /**  Recalculate raw data relative the center point with average X and XP. */
    public void centerData(){
        x_min =  Double.MAX_VALUE;
        x_max = -Double.MAX_VALUE;
        xp_min =  Double.MAX_VALUE;
        xp_max = -Double.MAX_VALUE;
	if(nPoints > 0){
	    double localX_sum  = 0.;
	    double localXP_sum = 0.;
	    for(int i = 0; i < nPoints; i++){
		localX_sum  += pointsX[i];
                localXP_sum += pointsXP[i];
	    }
	    double centerX  = localX_sum/nPoints;
	    double centerXP = localXP_sum/nPoints;
	    for(int i = 0; i < nPoints; i++){
		pointsX[i] = pointsX[i] - centerX;
		pointsXP[i] = pointsXP[i] - centerXP;
		if(x_min  > pointsX[i])   x_min  = pointsX[i];
		if(xp_min > pointsXP[i])  xp_min = pointsXP[i];
		if(x_max  < pointsX[i])   x_max  = pointsX[i];
		if(xp_max < pointsXP[i])  xp_max = pointsXP[i];
	    }            
	}          
    }

    /**  Returns the inner PhasePlaneEllipse instance. 
     *   It will be the PhasePlaneEllipse instance 
     *   calculated on the base of raw data set if user 
     *   did not use <code> setPhasePlaneEllipse() </code>. 
     */
    public PhasePlaneEllipse getPhasePlaneEllipse(){
	return innerPhasePlaneEllipse;
    }

    /**  Returns the correlation coefficient 
     *   calculated on the ellipse. 
     */
    public double getSShapeCorrelation(PhasePlaneEllipse phPE){
        double coeff = 0.0;
        double rho_avg = 0;
        double rho2_avg = 0;
        double rho_m_avg = 0;
        double rho2_m_avg = 0;
        double corr_avg = 0;
	int n = phPE.getNumberCurvePoints();
	double phi_step = 2*Math.PI/n;
	double phi, phi_m, x, xp, x_m, xp_m, rho, rho_m;
	for(int i = 0; i < n; i++){
	    phi = i*phi_step;
            phi_m = phi + Math.PI;
            x    = phPE.getX(phi);
            xp   = phPE.getXP(phi);
            x_m  = phPE.getX(phi_m);
            xp_m = phPE.getXP(phi_m);
	    rho = data.getValue(x,xp);
	    rho_m = data.getValue(x_m,xp_m);
	    rho_avg += rho;
	    rho_m_avg += rho_m;
	    rho2_avg += rho*rho;
	    rho2_m_avg += rho_m*rho_m;
	    corr_avg += rho*rho_m;
	}

	rho_avg    /= n;
	rho_m_avg  /= n;
	rho2_avg   /= n;
	rho2_m_avg /= n;
	corr_avg   /= n;

	double par = rho2_avg - rho_avg*rho_avg;
	if(par != 0.){
	    if(rho_avg != 0.){
		if(Math.abs(Math.sqrt(Math.abs(par))/rho_avg) < 1.0e-5){
		    return 0.;
		}
	    }
	    coeff = (corr_avg - rho_avg*rho_m_avg)/par;
	}
	return coeff;
    }

    /**  Calculates integral over the phase plane bounded by ellipse.
     *   This method uses interpolated and normalized <code> ColorSurfaceData </code>.
     */
    public double getIntegral(PhasePlaneEllipse phPE){
            double stepX  = (data.getMaxX() - data.getMinX())/(gridSizeX);
            double stepXP = (data.getMaxY() - data.getMinY())/(gridSizeXP);
	    double S = 0.;
	    double x,xp;
            for(int i = 0; i < gridSizeX; i++){
		for(int j = 0; j < gridSizeXP; j++){
		    x  = stepX*i +0.5*stepX  + data.getMinX();
		    xp = stepXP*j+0.5*stepXP + data.getMinY();
		    if(phPE.isInside(x,xp)){
			S += data.getValue(x,xp);
		    }
		}
	    }
	    S *= stepX*stepXP;
	    return S;
    }

    /**  Calculates integral over the phase plane bounded by ellipse.
     *   This method uses the raw data.
     */
    public double getRawIntegral(PhasePlaneEllipse phPE){
	double S = 0.;
	for(int i = 0; i < nPoints; i++){
	    if(phPE.isInside(pointsX[i],pointsXP[i])){
		S += 1.0;
	    }
	}
	if(nPoints > 0){
	    S /= nPoints;
	}
	return S;
    }

    /**  Calculates parameters of the  inner PhasePlaneEllipse
     *   calculated on the base of raw data set. 
     */
    private void calcInnerPhasePlaneEllipse(){
	if(nPoints > 1 ){
	    double x_avg = 0;
	    double xp_avg = 0.;
	    double x2_avg = 0.;
	    double xp2_avg = 0.;
	    double x_xp_avg = 0.;
	    for(int i = 0; i < nPoints; i++){
		x_avg  += pointsX[i];
		xp_avg += pointsXP[i];
		x2_avg  += pointsX[i]*pointsX[i];
		xp2_avg += pointsXP[i]*pointsXP[i];
		x_xp_avg += pointsX[i]*pointsXP[i];
	    }
	    x_avg    /= nPoints;
	    xp_avg   /= nPoints; 
	    x2_avg   /= nPoints;
	    xp2_avg  /= nPoints;
	    x_xp_avg /= nPoints;
	    double D_x  = x2_avg  - x_avg*x_avg;
	    double D_xp = xp2_avg - xp_avg*xp_avg;
	    double emitt = Math.sqrt(D_x*D_xp - x_xp_avg*x_xp_avg);
	    if( emitt > 0. ){
		double alpha = -  x_xp_avg/emitt;
		double beta = D_x/emitt;
		innerPhasePlaneEllipse.setEmittance(emitt);
		innerPhasePlaneEllipse.setAlpha(alpha);
		innerPhasePlaneEllipse.setBeta(beta);
		innerPhasePlaneEllipse.calcCurvePoints();
	    }
	}
    }


    private void resize(int nSize){
	if( nSize > pointsX.length){
	    double [] tmp_x  = new double[pointsX.length + nChunk];
	    double [] tmp_xp = new double[pointsX.length + nChunk];
            for(int i = 0;  i < pointsX.length; i++ ){
		tmp_x[i]  = pointsX[i];
		tmp_xp[i] = pointsXP[i];
	    }
            pointsX  = tmp_x;
            pointsXP = tmp_xp;
	}
    }
}
