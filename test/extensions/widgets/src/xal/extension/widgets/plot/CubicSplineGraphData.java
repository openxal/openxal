package xal.extension.widgets.plot;

import java.util.*;

/*
 * This class is a container class for data used in the FunctionGraphsJPanel class.
 * This class interpolates y-values by using the spline interpolation.
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public class CubicSplineGraphData extends BasicGraphData{

    private Vector<Cubic> cY;

    /**  data set constructor */ 
    public CubicSplineGraphData(){
        nInterpPoints = 200;
	init(50,nInterpPoints);
    }

    /**  data set constructor with defined initial capacity
     * for number of (x,y) points and interpolated points 
     */
    public CubicSplineGraphData(int nPoint, int nInterpPoints){
	init(nPoint,nInterpPoints);
    }

    /** returns the y-value for a certain x-value by using the spline interpolation schema*/
    public double getValueY(double x){
	synchronized(lockUpObj){
	    if( xyPointV.size() == 0 ) return (-Double.MAX_VALUE);
	    if( xyPointV.size() == 1 ) return xyPointV.get(0).getY();
	    int i = 0;
	    for( i = 0; i < xyPointV.size(); i++){
		if( x < xyPointV.get(i).getX() ) break;
	    }
	    if( i == 0) i = 1;
	    if( i == xyPointV.size()) i = xyPointV.size() - 1;
	    XYpoint p1 =  xyPointV.get(i-1); 
	    XYpoint p2 =  xyPointV.get(i);
	    double u = (x-p1.getX())/(p2.getX() - p1.getX()); 
	    return cY.get(i-1).eval(u);
	}
    }

    /** returns the y'-value for a certain x-value by using the spline interpolation schema*/
    public double getValueDerivativeY(double x){
	synchronized(lockUpObj){
	    if( xyPointV.size() == 0 ) return (-Double.MAX_VALUE);
	    if( xyPointV.size() == 1 ) return 0.0;
	    int i = 0;
	    for( i = 0; i < xyPointV.size(); i++){
		if( x < xyPointV.get(i).getX()) break;
	    }
	    if( i == 0) i = 1;
	    if( i == xyPointV.size()) i = xyPointV.size() - 1;
	    XYpoint p1 =  xyPointV.get(i-1); 
	    XYpoint p2 =  xyPointV.get(i); 
	    double u = (x-p1.getX())/(p2.getX() - p1.getX());
	    return cY.get(i-1).evalDerivative(u)/(p2.getX() - p1.getX()); 
	}       
    }


    /** calculates the spline coefficients */
    protected void calculateRepresentation(){
        int i;

	int n = this.getNumbOfPoints()-1;
        int n1 = n+1;
        if( n < 1 ) return;  

        double[]gamma = new double[n1];
        double[]delta = new double[n1];
        double[]    D = new double[n1];
        double[]    z = new double[n1];

        if(cY == null){ 
	    cY= new Vector<Cubic>(this.getCapacity());
	}

        if(cY.size() < n1 ){
	    int nAddPoints = (n1 - cY.size());
	    for( i=0; i < nAddPoints; i++){
		cY.add(new Cubic());
	    }
	}

        for( i=0; i < n1; i++){
	    z[i]= this.getY(i);
	}
  
        gamma[0] = 0.5;
        for ( i = 1; i < n; i++) {
	    gamma[i] = 1.0/(4.0-gamma[i-1]);
        }
        gamma[n] = 1.0/(2.0-gamma[n-1]);
    
        delta[0] = 3*(z[1]-z[0])*gamma[0];
        for ( i = 1; i < n; i++) {
	    delta[i] = (3*(z[i+1]-z[i-1])-delta[i-1])*gamma[i];
        }
        delta[n] = (3*(z[n]-z[n-1])-delta[n-1])*gamma[n];
    
        D[n] = delta[n];
        for ( i = n-1; i >= 0; i--) {
	    D[i] = delta[i] - gamma[i]*D[i+1];
        } 

        for ( i = 0; i < n; i++) {
	    cY.get(i).setCoeff(z[i], D[i], 3*(z[i+1] - z[i]) - 2*D[i] - D[i+1],
					 2*(z[i] - z[i+1]) + D[i] + D[i+1]);
        }       
    }

    /*
     *The inner class Cubic to keep the cubic coefficients.
     *
     * @version 1.0
     * @author  A. Shishlo
     */
    private class Cubic {

	double a,b,c,d;         /* a + b*u + c*u^2 +d*u^3 */
	double e,f,g;

	public Cubic(){}

	public void setCoeff(double a, double b, double c, double d){
	    this.a = a;  this.b = b;  this.c = c; this.d = d;
	    this.g = 3.0* d; this.f = 2.0* c; this.e = b;
	}
 
	/** evaluate cubic */
	public double eval(double u) {
	    return (((d*u) + c)*u + b)*u + a;
	}

	/** evaluate cubic */
	public double evalDerivative(double u) {
	    return ((g*u) + f)*u + e;
	}

    } 

    /*
     *The "main" test method for debugging
     *
     * @version 1.0
     * @author  A. Shishlo
     */
    public static void main(String args[]) { 
	CubicSplineGraphData spl = new CubicSplineGraphData();
	int nPoint = 20;
	double[] xV = new double[nPoint];
	double[] yV = new double[nPoint]; 
	System.out.println("Added ====As an example sin(x) has been used=======");
	System.out.println("Added ====x====  ====y=====");
	for( int i = 0; i <  nPoint; i++){
	    xV[i] = 0.3*i;
	    yV[i] = Math.sin(xV[i]);
	    spl.addPoint(xV[i],yV[i]);
	    System.out.println(xV[i]+ " " + yV[i]);
	}

	double x;
	double y;
	double yp;
	int NgraphPoint = 50;
	System.out.println("==CubicSplineGraphData results========");
	System.out.println("====x====  ====y=====   ====derivative y====");
	double step = (spl.getMaxX() - spl.getMinX())/NgraphPoint;

	for( int i = 0; i <  NgraphPoint; i++){
	    x = spl.getMinX() + step*i + 0.5*step;
	    y = spl.getValueY(x);
	    yp = spl.getValueDerivativeY(x);
	    System.out.println(x+"  "+y+"  "+yp);
	}
 
	System.out.println("Stop.");  
    }

}
