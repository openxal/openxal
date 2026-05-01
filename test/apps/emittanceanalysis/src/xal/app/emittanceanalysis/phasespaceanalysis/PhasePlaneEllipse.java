package xal.app.emittanceanalysis.phasespaceanalysis;

import java.util.*;
import java.awt.*;

import xal.extension.widgets.plot.*;

/**
 *  This class keeps the emittance and twiss parameters to define ellipse in the
 *  phase plane. It also can generate a curve (<code> CurveData  </code> class)
 *  to show graphics image of the ellipse. The ellipse equation is emitt =
 *  gamma*x^2+2*alpha*x*xp+beta*xp^2.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class PhasePlaneEllipse {

    private double emitt = 0.;
    private double alpha = 0.;
    private double beta = 1.;
    private double gamma = 1.;

    private double maxX = 0.;
    private double maxXP = 0.;

    private CurveData curveData = new CurveData();

    private int nGraphPoints = 150;

    //parametric parameters of the ellipse
    // x  = a*cos(phi)*cos(phi_0) + b*sin(phi)*sin(phi_0)
    // xp = b*sin(phi)*cos(phi_0) - a*cos(phi)*sin(phi_0)
    private double a_par, b_par, cos_phi_0_par, sin_phi_0_par;


    /**
     *  The phase plane ellipse constructor. By default emittance = 0, alfa = 0,
     *  beta = 1.
     */
    public PhasePlaneEllipse() {
        calcParametrization();
    }


    /**
     *  The phase plane ellipse constructor.
     *
     *@param  emitt  Description of the Parameter
     *@param  alpha  Description of the Parameter
     *@param  beta   Description of the Parameter
     */
    public PhasePlaneEllipse( double emitt, double alpha, double beta ) {
        this.emitt = emitt;
        this.alpha = alpha;
        this.beta = beta;
        gamma = ( 1.0 + alpha * alpha ) / beta;
        maxX = Math.sqrt( emitt * beta );
        maxXP = Math.sqrt( emitt * gamma );
        calcParametrization();
    }


    /**
     *  Sets the emittance parameter.
     *
     *@param  emitt  The new emittance value
     */
    public void setEmittance( double emitt ) {
        if ( emitt >= 0. ) {
            this.emitt = emitt;
            maxX = Math.sqrt( emitt * beta );
            maxXP = Math.sqrt( emitt * gamma );
            calcParametrization();
        }
    }


    /**
     *  Sets the alpha parameter.
     *
     *@param  alpha  The new alpha value
     */
    public void setAlpha( double alpha ) {
        this.alpha = alpha;
        gamma = ( 1.0 + alpha * alpha ) / beta;
        maxX = Math.sqrt( emitt * beta );
        maxXP = Math.sqrt( emitt * gamma );
        calcParametrization();
    }


    /**
     *  Sets the alpha parameter.
     *
     *@param  beta  The new beta value
     */
    public void setBeta( double beta ) {
        if ( beta >= 0. ) {
            this.beta = beta;
            gamma = ( 1.0 + alpha * alpha ) / beta;
            maxX = Math.sqrt( emitt * beta );
            maxXP = Math.sqrt( emitt * gamma );
            calcParametrization();
        }
    }



    /**
     *  Sets the emittnace, alpha and beta attributes of the PhasePlaneEllipse
     *  object
     *
     *@param  emitt  The new emittance value
     *@param  alpha  The new alpha value
     *@param  beta   The new beta value
     */
    public void setEmtAlphaBeta( double emitt, double alpha, double beta ) {
        if ( emitt >= 0. && beta >= 0. ) {
            this.emitt = emitt;
            this.alpha = alpha;
            this.beta = beta;
            gamma = ( 1.0 + alpha * alpha ) / beta;
            maxX = Math.sqrt( emitt * beta );
            maxXP = Math.sqrt( emitt * gamma );
            calcParametrization();
        }
    }


    /**
     *  Returns the emittance parameter.
     *
     *@return    The emittance value
     */
    public double getEmittance() {
        return emitt;
    }


    /**
     *  Returns the alpha parameter.
     *
     *@return    The alpha value
     */
    public double getAlpha() {
        return alpha;
    }


    /**
     *  Returns the alpha parameter.
     *
     *@return    The beta value
     */
    public double getBeta() {
        return beta;
    }


    /**
     *  Returns the maximal coordinate of the ellipse
     *
     *@return    The maximal coordinate of the ellipse
     */
    public double getMaxX() {
        return maxX;
    }


    /**
     *  Returns the maximal momentum value of the ellipse
     *
     *@return    The maximal momentum value of the ellipse
     */
    public double getMaxXP() {
        return maxXP;
    }


    /**
     *  Returns true is the (x,xp) is inside this ellipse.
     *
     *@param  x   Description of the Parameter
     *@param  xp  Description of the Parameter
     *@return     The inside value
     */
    public boolean isInside( double x, double xp ) {
        if ( gamma * x * x + 2.0 * alpha * x * xp + beta * xp * xp <= emitt ) {
            return true;
        }
        return false;
    }


    /**
     *  Returns value of the emittance if the (x,xp) point is on the boudary of
     *  this ellipse.
     *
     *@param  x   Description of the Parameter
     *@param  xp  Description of the Parameter
     *@return     The boundedEmittance value
     */
    public double getBoundedEmittance( double x, double xp ) {
        return ( gamma * x * x + 2.0 * alpha * x * xp + beta * xp * xp );
    }


    /**  Calculates (x,xp) points for graphic presentation. */
    public void calcCurvePoints() {
        curveData.clear();
        double phi;
        double x;
        double xp;
        double phi_step;
        phi_step = 2 * Math.PI / ( nGraphPoints - 1 );
        for ( int i = 0; i < nGraphPoints; i++ ) {
            phi = i * phi_step;
            x = getX( phi );
            xp = getXP( phi );
            curveData.addPoint( x, xp );
        }
    }


    /**
     *  Calculates x on the ellipse for angle phi in polar coordinate.
     *
     *@param  phi  Description of the Parameter
     *@return      The x value
     */
    public double getX( double phi ) {
        return ( a_par * Math.cos( phi ) * cos_phi_0_par +
            b_par * Math.sin( phi ) * sin_phi_0_par );
    }


    /**
     *  Calculates x on the ellipse for angle phi in polar coordinate.
     *
     *@param  phi  Description of the Parameter
     *@return      The xP value
     */
    public double getXP( double phi ) {
        return ( b_par * Math.sin( phi ) * cos_phi_0_par -
            a_par * Math.cos( phi ) * sin_phi_0_par );
    }


    /**
     *  Calculates (x,xp) points for graphic presentation. N - number of
     *  graphics points. N = 150 by default.
     *
     *@param  N  Description of the Parameter
     */
    public void calcCurvePoints( int N ) {
        nGraphPoints = N;
        calcCurvePoints();
    }


    /**
     *  Returns the number of graphics points. N = 150 by default. This number
     *  will be the same as number of points of the CurveData instance delivered
     *  by <code> getCurveData() </code> method of this class;
     *
     *@return    The numberCurvePoints value
     */
    public int getNumberCurvePoints() {
        return nGraphPoints;
    }


    /**
     *  Returns the reference to the CurveData. To get correct curve data points
     *  the method calcCurvePoints should be called first.
     *
     *@return    The curveData value
     */
    public CurveData getCurveData() {
        return curveData;
    }


    /**
     *  Returns the new instance of the PhasePlaneEllipse (this) class with the
     *  new emmitance and the same others parameters. The CurveData is ready to
     *  use.
     *
     *@param  emmitNew  Description of the Parameter
     *@return           The newPhasePlaneEllipse value
     */
    public PhasePlaneEllipse getNewPhasePlaneEllipse( double emmitNew ) {
        PhasePlaneEllipse phe = new PhasePlaneEllipse( emmitNew, getAlpha(), getBeta() );
        phe.calcCurvePoints( this.getNumberCurvePoints() );
        return phe;
    }


    /**
     *  Returns the new instance of the PhasePlaneEllipse (this) class with the
     *  same parameters (emittance, alpha, and beta). The CurveData is ready to
     *  use.
     *
     *@return    The newPhasePlaneEllipse value
     */
    public PhasePlaneEllipse getNewPhasePlaneEllipse() {
        PhasePlaneEllipse phe = new PhasePlaneEllipse( getEmittance(), getAlpha(), getBeta() );
        phe.calcCurvePoints( this.getNumberCurvePoints() );
        return phe;
    }


    /**
     *  Calculates parameters of the ellipse. x = a*cos(phi)*cos(phi_0) +
     *  b*sin(phi)*sin(phi_0) xp = b*sin(phi)*cos(phi_0) - a*cos(phi)*sin(phi_0)
     */
    private void calcParametrization() {
        double a = alpha;
        double b = beta;
        double g = gamma;
        double apb = g + b;
        double amb = g - b;
        double a2 = a * a;
        double l1 = apb / 2 + Math.sqrt( a2 + amb * amb / 4 );
        double l2 = apb / 2 - Math.sqrt( a2 + amb * amb / 4 );
        double phi0 = 0.;
        if ( a != 0. ) {
            phi0 = Math.acos( Math.abs( a ) / Math.sqrt( a2 + ( l1 - g ) * ( l1 - g ) ) );
            phi0 *= -a / Math.abs( a );
            a_par = Math.sqrt( emitt / l1 );
            b_par = Math.sqrt( emitt / l2 );
        }
        else {
            a_par = Math.sqrt( emitt / gamma );
            b_par = Math.sqrt( emitt / beta );
        }
        sin_phi_0_par = Math.sin( phi0 );
        cos_phi_0_par = Math.cos( phi0 );
    }

}
