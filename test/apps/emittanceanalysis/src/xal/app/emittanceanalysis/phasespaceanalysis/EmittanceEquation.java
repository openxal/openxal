package xal.app.emittanceanalysis.phasespaceanalysis;

/**
 *  This class keeps the ellipse parameters and has the method to define is a
 *  space phase point located inside the ellipse
 *
 *@author     shishlo
 *@version    1.0
 */
public class EmittanceEquation {

    private double emt = 1.0;
    private double alpha = 1.0;
    private double beta = 1.0;
    private double gamma = 2.0;

    private double maxX = 1;
    private double maxXP = Math.sqrt( 2.0 );


    /**  Constructor for the EmittanceEquation object */
    public EmittanceEquation() { }


    /**
     *  Sets the parameters of the EmittanceEquation object. The area bounded by
     *  the emittance value emt is PI*emt
     *
     *@param  emt    The new emittance
     *@param  alpha  The new alpha
     *@param  beta   The new beta
     */
    public void setPrams( double emt, double alpha, double beta ) {
        calculateLimits( emt, alpha, beta );
    }



    /**
     *  Sets the emittnace attribute of the EmittanceEquation object
     *
     *@param  emt  The new emittnace value
     */
    public void setEmittnace( double emt ) {
        calculateLimits( emt, alpha, beta );
    }


    /**
     *  Sets the alpha attribute of the EmittanceEquation object
     *
     *@param  alpha  The new alpha value
     */
    public void setAlpha( double alpha ) {
        calculateLimits( emt, alpha, beta );
    }


    /**
     *  Sets the beta attribute of the EmittanceEquation object
     *
     *@param  beta  The new beta value
     */
    public void setBeta( double beta ) {
        calculateLimits( emt, alpha, beta );
    }


    private void calculateLimits( double emt, double alpha, double beta ) {
        if ( emt <= 0. || beta <= 0. ) {
            return;
        }
        this.emt = emt;
        this.alpha = alpha;
        this.beta = beta;
        gamma = ( 1.0 + alpha * alpha ) / beta;
        maxX = Math.sqrt( emt * beta );
        maxXP = Math.sqrt( emt * gamma );
    }


    /**
     *  Returns "true" if the space phase point is inside the ellipse or "false"
     *  if it is not
     *
     *@param  x   coordinates value of the space phase point
     *@param  xp  momentum value of the space phase point
     *@return     true or false
     */
    public boolean isInside( double x, double xp ) {
        if ( gamma * x * x + 2.0 * alpha * x * xp + beta * xp * xp <= emt ) {
            return true;
        }
        return false;
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
     *  Returns the emttance
     *
     *@return    The emittance value
     */
    public double getEmt() {
        return emt;
    }


    /**
     *  Returns the alpha parameter of the ellipse
     *
     *@return    The alpha value
     */
    public double getAlpha() {
        return alpha;
    }


    /**
     *  Returns the beta parameter of the ellipse
     *
     *@return    The beta value
     */
    public double getBeta() {
        return beta;
    }


    /**
     *  Returns the gamma parameter of the ellipse
     *
     *@return    The gamma value
     */
    public double getGamma() {
        return gamma;
    }

}
