//////////////////////////////////////////////////////////////
//
// FILE NAME
//    Spline.java $Revision: 8271 $
//
// AUTHOR
//    Scott Haney, LLNL, (510) 423-6308
//     jdg converted from C++ to java 3/9/2004
//
//
// CREATED
//    March 7, 1992
//
// DESCRIPTION
//    Static and non-inline functions for the classes used to perform
//    1-D spline fits.
//
///////////////////////////////////////////////////////////////////////////

package xal.extension.fit.spline;

import java.io.*;
import java.util.*;

///////////////////////////////////////////////////////////////////////////
//
// NAME
//    Spline::Spline
//
// DESCRIPTION
//    Constructors for the Spline class.  Various versions are supplied:
//
//      1) construction using another Spline.
//      2) construction using (x, y) values and derivatives at the end-
//         points.
//      3) construction uisng (x, y) values only (i.e., makes natural spline).
//
//    Routines based on those from "Numerical Recipes in " by Press, et al.
//
// PARAMETERS
//    [Depends on call].
//
// RETURNS
//    Nothing.
//
///////////////////////////////////////////////////////////////////////////

public class CubicSpline {


    private int nPoints = 0;
    /** work space */
    private double xMin, xMax;
    /** spline coefficient containers */
    private double [] aVal, bVal, cVal, dVal;

    /* the x xalues of the data points */
    private double [] xVals;

    /** constructor of a new spline from another one */
    public CubicSpline(CubicSpline sp) {
	xVals = sp.xVals;
	aVal = sp.aVal;
	bVal = sp.bVal;
	cVal = sp.cVal;
	dVal = sp.dVal;
	xMin = sp.xMin;
	xMax = sp.xMax;
	nPoints = sp.nPoints;
    }

    /** constructor which also takes initial and final slope constraints
     * @param xx - the data X values
     * @param y - the data Y values
     * @param fp0 - the slope at the starting point
     * @param fpn - the slope at the end point
     */

    public CubicSpline(double [] xx, double [] y, 
		       double fp0, double fpn) {
 
	nPoints = xx.length - 1;
	xVals = xx;
	aVal = y;
	int i;
	double [] alpha= new double  [nPoints + 1];
        double [] l = new double[nPoints + 1];
        double [] m = new double[nPoints + 1];
        double [] z = new double[nPoints + 1];
        double [] h = new double[nPoints + 1];
	double one3 = 1.0 / 3.0;
  
	bVal = new double [nPoints + 1]; 
	cVal = new double [nPoints + 1]; 
	dVal = new double [nPoints + 1];

	xMin = xVals[0];
	xMax = xVals[nPoints];
  
	for (i = 0; i < nPoints; i++)
	    h[i] = xVals[i + 1] - xVals[i];
  
	alpha[0] = 3.0 * ((aVal[1] - aVal[0]) / h[0] - fp0);
	for (i = 1; i < nPoints; i++)
	    alpha[i] = 3.0 * (aVal[i+1] * h[i - 1] - aVal[i] * (xVals[i + 1] - xVals[i - 1]) +  aVal[i - 1] * h[i]) / (h[i - 1] * h[i]);
	alpha[nPoints] = 3.0 * (fpn - (aVal[nPoints] - aVal[nPoints-1]) / h[nPoints-1]);
  
	l[0] = 2.0 * h[0];
	m[0] = 0.5;
	z[0] = alpha[0] / l[0];
	for (i = 1; i < nPoints; i++)
	    {
		l[i] = 2.0 * (xVals[i + 1] - xVals[i - 1]) - h[i - 1] * m[i - 1];
		m[i] = h[i] / l[i];
		z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
	    }
	l[nPoints] = h[nPoints-1] * (2.0 - m[nPoints-1]);
	z[nPoints] = (alpha[nPoints] - h[nPoints-1] * z[nPoints-1]) / l[nPoints];
	cVal[nPoints] = z[nPoints];
  
	for (i = nPoints-1; i >= 0; i--)
	    {
		cVal[i] = z[i] - m[i] * cVal[i + 1];
		bVal[i] = (aVal[i + 1] - aVal[i]) / h[i] - one3 * h[i] * 
		    (cVal[i + 1] + 2.0 * cVal[i]);
		dVal[i] = one3 * (cVal[i + 1] - cVal[i]) / h[i];
	    }
    }

    /** constructor which takes only the data points
     * fp0 - the slope at the starting point assumed = 0.
     * fpn - the slope at the end point assumed = 0.    
     * @param xx - the data X values
     * @param y - the data Y values
     */

    public CubicSpline(double [] xx, double [] y) {
	    this(xx,y, 0., 0.);
    }

    /** method to return an interpolated result at a given x value
     * @param xx - the x value to evaluate the interpolation at 
     * If x is outside the parameter rage - set return value = end point
     */
    public double evaluateAt(double xx)
    {
	int k, klo, khi;
	double dx;
  
	if (xx >= xMax)
		return aVal[nPoints];
	    //k = nPoints;
	else if (xx <= xMin)
		return aVal[0];
	    //k = 1;
	else
	    {
		klo = 1;
		khi = nPoints + 1;
		while (khi - klo > 1)
		    {
			k = (khi + klo) >> 1;
			if (xVals[k-1] > xx)
			    khi = k;
			else
			    klo = k;
		    }
		k = klo;
	    }
  
	dx = xx - xVals[k-1];
  
	return ((((dVal[k-1] * dx + cVal[k-1]) * dx + bVal[k-1]) * dx) + aVal[k-1]);
    }


/*  need to convert this from C++ still
///////////////////////////////////////////////////////////////////////////
//
// NAME
//    CubicSpline::coefs
//
// DESCRIPTION
//    Returns the spline coeficients at a given x value
//
// PARAMETERS
//    xx:              X-value to return coefs at.
//    x1,a,b,c,d:      Coefs.  f(x) = d dx^3 + c dx^2 + b dx + a; dx = xx - x1
//
// RETURNS
//    Fix at xx.
//
///////////////////////////////////////////////////////////////////////////

void CubicSpline::coefs(Real xx, Real &x1, Real &a, Real &b, Real &c, Real &d) const
{
register Integer k, klo, khi;
  
if (xx >= xMax)
k = nPoints;
else if (xx <= xMin)
k = 1;
else
{
    klo = 1;
    khi = nPoints + 1;
    while (khi - klo > 1)
	{
	    k = (khi + klo) >> 1;
	    if (xVals(k) > xx)
		khi = k;
	    else
		klo = k;
	}
    k = klo;
}

x1 = xVals(k);
a = aVal(k); b = bVal(k); c = cVal(k); d = dVal(k);
}
*/
    /** 
     *    Differentiates a spline in place.  After calling this routine,
     *    a calls to evaluateAt() return the derivative.
     */

    public void differentiate() {
	double [] iVals = new double[nPoints + 1];
	int  i;
	double fp0, fpn, dx;
	CubicSpline temp;
	fp0 = 0.;
	fpn = 0.;
  
	for (i = 0; i < nPoints; i++)
	    {
		if (i == 0)
		    fp0 = 2.0 * cVal[0];
		if (i == nPoints-1)
		    {
			dx = xVals[nPoints] - xVals[nPoints-1];
			iVals[nPoints] = (3.0 * dVal[nPoints-1] * dx + 2.0 * cVal[nPoints-1]) * dx + bVal[nPoints-1];
			fpn = 6.0 * dVal[nPoints-1] * dx + 2.0 * cVal[nPoints-1];
		    }
		iVals[i] = bVal[i];
	    }

	temp = new CubicSpline(xVals, iVals, fp0, fpn);

	for (i = 0; i < nPoints + 1; i++)
	    {
		aVal[i] = temp.aVal[i];
		bVal[i] = temp.bVal[i];
		cVal[i] = temp.cVal[i];
		dVal[i] = temp.dVal[i];
	    }
    }

    /**    Integrates a spline in place.  After calling this routine,
    *    a calls to evaluateAt() return the integral.  The
    *   constant of integration is chosen such that i(0) = 0.0;
    */

    public void integrate() {
        double [] iVals = new double[nPoints + 1];
        int i, i1;
	double dx;
	CubicSpline temp;
  
	iVals[0] = 0.0;
	for (i = 1; i < nPoints + 1; i++)
	    {
		i1 = i - 1;
		dx = xVals[i] - xVals[i1];
		iVals[i] = iVals[i1] +
		    (((3.0 * dVal[i1] * dx + 4.0 * cVal[i1]) * dx + 
		      6.0 * bVal[i1]) * dx + 12.0 * aVal[i1]) * dx / 12.0;
	    }
  
	temp = new CubicSpline(xVals, iVals, this.xMin, this.xMax);

	for (i = 0; i < nPoints + 1; i++)
	    {
		aVal[i] = temp.aVal[i];
		bVal[i] = temp.bVal[i];
		cVal[i] = temp.cVal[i];
		dVal[i] = temp.dVal[i];
	    } 
    }
}
