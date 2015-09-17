package xal.app.emittanceanalysis.analysis;

/**
 *  The interface defines transformation function with methods for forward
 *  function and an inverse function calculations
 *
 *@author     shishlo
 *@version    1.0
 */
interface TransformFunction {

    /**
     *  The function
     *
     *@param  inX  The input independent variable
     *@return      The function value
     */
    public Double forwardF( double inX );


    /**
     *  The inverse function
     *
     *@param  inY  The function value
     *@return      The independent variable that corresponds to the function
     *      value
     */
    public Double backwardF( double inY );

}
