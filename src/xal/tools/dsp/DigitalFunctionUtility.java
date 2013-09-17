/**
 * DigitalFunctionUtility.java
 * 
 * Created      : August, 2007
 * Author       : Christopher K. Allen
 */
package xal.tools.dsp;

import JSci.maths.vectors.DoubleVector;


/**
 * <p>
 * Utility class for performing common operations on and for digital functions.
 * </p>
 * <p>
 * Within this package digital functions are taken to be objects of type 
 * <code>double[]</code>).  Because sub-typing from <code>double[]</code>
 * is illegal in Java, it is necessary to create a utility class to handle
 * the common operations on digital functions.  
 * </p>
 * 
 * @author Christopher K. Allen
 *
 */
public class DigitalFunctionUtility {

    
    
    /*
     * General
     */
    
    
    /**
     * Computes and returns the <i>l</i><sub>2</sub> distance between the 
     * given vector functions normalized by the norm ||<var>arrTarg</var>||<sub>2</sub>.
     * 
     * @param arrFunc  vector function
     * @param arrTarg  target vector function
     * 
     * @return          ||arrFunc1 - arrFunc2||<sub>2</sub>/||arrTarg||<sub>2</sub>
     */
    public static double  compError(final double[] arrFunc, final double[] arrTarg) {
        DoubleVector    vecFunc = new DoubleVector(arrFunc);
        DoubleVector    vecTarg = new DoubleVector(arrTarg);
        
        double      dblDis = vecFunc.subtract(vecTarg).norm();

        return dblDis/vecTarg.norm();
    }
    
    /**
     * Build a table of function values for comparison.  Each table row consists
     * of the function index, then corresponding function values, all separated by
     * tab characters ("\t").       
     * 
     * @param argFuncs  discrete functions to be tabulated
     * 
     * @return          formatted string of tabulated values
     * 
     * @throws ArrayIndexOutOfBoundsException   arguments are not all the same size
     */
    public static String  buildValueTable(final double[]... argFuncs) throws ArrayIndexOutOfBoundsException {
        
        // Check the size of each function
        int         szData = argFuncs[0].length;
        for (double[] arrFunc : argFuncs)
            if (arrFunc.length != szData)   
                throw new ArrayIndexOutOfBoundsException(
                                "TestFourierSineTransform#lstFuncValues() - "
                              + "arguments do not have the same size"
                              );
        
        // Build the table of function values
        String      strValTbl = "";
        int         index;

        for (index=0; index<szData; index++)    {
            strValTbl += Integer.toString(index) + "\t";
            for (double[] arrFunc : argFuncs)  
                strValTbl += Double.toString(arrFunc[index]) + "\t";
            strValTbl += "\n";
        }

        return strValTbl;
    }
    
    
    
    /*
     * Function Generation
     */
    
    /**
     * Generate a discrete sine function of the given size,
     * given discrete frequency, and given phase offset.  
     * Note that the largest frequency 
     * that returns a meaning function is <var>szArray</var>/2.
     * Any frequencies larger than that are unrecognizable due
     * to insufficient resolution.
     * 
     * @param dblPhase  phase offset (in radians)
     * @param szArray   array size of the returned function
     * @param intFreq   frequency component of the returned function
     * 
     * @return      sin(2<i>&pi;f</i> + <i>&phi;</i>)
     */
    public static double[]  generateSine(int szArray, int intFreq, double dblPhase)  {
        double[]    arrFunc = new double[szArray];
        for (int index=0; index<szArray; index++)  
            arrFunc[index] = Math.sin(intFreq*index*2.0*Math.PI/szArray + dblPhase);
        
        return arrFunc;
    }

    /**
     * Generate a discrete cosine function of the given size,
     * given discrete frequency, and given phase offset.  
     * Note that the largest frequency 
     * that returns a meaning function is <var>szArray</var>/2.
     * Any frequencies larger than that are unrecognizable due
     * to insufficient resolution.
     * 
     * @param dblPhase  phase offset (in radians)
     * @param szArray   array size of the returned function
     * @param intFreq   frequency component of the returned function
     * 
     * @return      cos(2<i>&pi;f</i> + <i>&phi;</i>)
     */
    public static double[]  generateCosine(int szArray, int intFreq, double dblPhase)  {
        double[]    arrFunc = new double[szArray];
        for (int index=0; index<szArray; index++)  
            arrFunc[index] = Math.cos(intFreq*index*2.0*Math.PI/szArray + dblPhase);
        
        return arrFunc;
    }
    

    /*
     * Algebraic
     */
    
    /**
     * Scale the function by the given value.
     * 
     * @param   dblVal  scale factor
     */
    public static void  scaleFunction(double dblVal, double[] arrFunc) {
        int N = arrFunc.length;
        for (int index=0; index<N; index++) {
            arrFunc[index] *= dblVal;
        }
    }
    
    /**
     * Compute and return the function sum of the given functions.
     * 
     * @param arrFunc1  addend
     * @param arrFunc2  adder
     * 
     * @return  sum of arguments
     * 
     * @throws IllegalArgumentException arguments are of different sizes
     */
    public static double[]  add(final double[]arrFunc1, final double[] arrFunc2)
        throws IllegalArgumentException
    {
        if (arrFunc1.length != arrFunc2.length)
            throw new IllegalArgumentException(
                            "DigitalFunctionUtility#add(): "
                          + "arguments are different sizes"
                            );
        int         N = arrFunc1.length;
        
        double[]    arrSum = new double[N];
        for (int index=0; index<N; index++) {
            arrSum[index] = arrFunc1[index] + arrFunc2[index];
        }
        
        return arrSum;
    }
    
    /**
     * Compute and return the function difference of the given functions.
     * 
     * @param arrFunc1  subtractend
     * @param arrFunc2  subtractor
     * 
     * @return  difference of arguments
     * 
     * @throws IllegalArgumentException argument are of different sizes
     */
    public static double[]  subtract(final double[]arrFunc1, final double[] arrFunc2)
        throws IllegalArgumentException
    {
        if (arrFunc1.length != arrFunc2.length)
            throw new IllegalArgumentException(
                            "DigitalFunctionUtility#subtract(): "
                          + "arguments are different sizes"
                            );
        int         N = arrFunc1.length;
        
        double[]    arrDif = new double[N];
        for (int index=0; index<N; index++) {
            arrDif[index] = arrFunc1[index] - arrFunc2[index];
        }
        
        return arrDif;
    }
    
    /**
     * In place subtraction of the given value from the function.
     * 
     * @param arrFunc   subtractend
     * @param dblVal    subtractor
     * 
     * @throws IllegalArgumentException argument are of different sizes
     */
    public static void  subtractFrom(double[] arrFunc, double dblVal) {
        for (int index=0; index<arrFunc.length; index++) {
            arrFunc[index] -= dblVal;
        }
    }
    
    
    /*
     * Analysis
     */
    
    
    /**
     * Return the maximum value of the function.
     * 
     * @param   arrFunc     digital function
     * 
     * @return  function maximum
     */
    public static double    maximumValue(final double[] arrFunc)    {
        double  dblMax = arrFunc[0];
        for (int index=0; index<arrFunc.length; index++)
            if (arrFunc[index] > dblMax)    {
                dblMax = arrFunc[index];
            }
        
        return dblMax;
    }

    /**
     * Return the minimum value of the function.
     * 
     * @param   arrFunc     digital function
     * 
     * @return  function minimum
     */
    public static double    minimumValue(final double[] arrFunc)    {
        double  dblMin = arrFunc[0];
        for (int index=0; index<arrFunc.length; index++)
            if (arrFunc[index] < dblMin)    {
                dblMin = arrFunc[index];
            }
        
        return dblMin;
    }
    
    /**
     * Return the index at with the function maximum occurs.
     * 
     * @param   arrFunc     digital function
     * 
     * @return      index of maximum value
     */
    public static int  argMaximum(final double[] arrFunc) {
        int     iMax   = 0;
        double  dblMax = arrFunc[iMax];
        for (int index=0; index<arrFunc.length; index++)
            if (arrFunc[index] > dblMax)    {
                iMax   = index;
                dblMax = arrFunc[index];
            }
        
        return iMax;
    }
    
    /**
     * Return the index at with the function minimum occurs.
     * 
     * @param   arrFunc     digital function
     * 
     * @return      index of minimum value
     */
    public static int  argMinimum(final double[] arrFunc) {
        int     iMin   = 0;
        double  dblMin = arrFunc[iMin];
        for (int index=0; index<arrFunc.length; index++)
            if (arrFunc[index] < dblMin)    {
                iMin   = index;
                dblMin = arrFunc[index];
            }
        
        return iMin;
    }
    
    /**
     * Return the index where the function first exceeds the
     * given value.
     * 
     * @param   dblVal      inspection value
     * @param   arrFunc     digital function
     * 
     * @return      index where function first exceeds value or <code>arrFunc.length</code> if none
     */
    public static int   argGreaterThan(double dblVal, final double[] arrFunc) {
        for (int index=0; index<arrFunc.length; index++)
            if (arrFunc[index] > dblVal)
                return index;
        
        // function never exceeds value
        return arrFunc.length;
    }
    
    
    /**
     * Compute and return the (functional) absolute value of the given 
     * function.
     * 
     * @param   arrFunc     function to process
     * 
     * @return  functional absolute value of the argument
     */
    public static double[]  abs(final double[] arrFunc)  {
        int     szFunc = arrFunc.length;
        
        double[]    arrAbs = new double[szFunc];
        for (int index=0; index<szFunc; index++)    
            arrAbs[index] = Math.abs(arrFunc[index]);
        
        return arrAbs;
    }
    
    /**
     * Compute and return the (functional) square of the given function.
     * 
     * @param   arrFunc     function to square
     * 
     * @return  functional square of the argument
     */
    public static double[]  square(final double[] arrFunc)  {
        int     szFunc = arrFunc.length;
        
        double[]    arrSqr = new double[szFunc];
        for (int index=0; index<szFunc; index++)    {
            double  dblVal = arrFunc[index];
            
            arrSqr[index] = dblVal*dblVal;
        }
        
        return arrSqr;
    }
    
    
    /**
     * Compute and return the total variation of the given
     * function.  The total variation <i>TV</i>(<i>f</i>) of a
     * function <i>f</i>(&middot;) is defined as
     * <br>
     * <br>&nbsp;&nbsp;  <i>TV</i>[<i>f</i>](<i>t</i>) 
     *                  &equiv; 
     *                  &int;<sup><i>t</i></sup>|<i>df</i>(<i>&tau;</i>)/<i>dt</i>|<i>d&tau;</i>
     * 
     * @param arrFunc   target function
     * 
     * @return  total variation of given function
     */
    public static double[]  totalVariation(double[] arrFunc) {
        int         N = arrFunc.length;
        
        if (N == 0) return null;
        
        double      dblSum = 0.0;
        double[]    arrTv = new double[N];
        arrTv[0] = 0.0;
        for (int index=1; index<N; index++) {
            dblSum += Math.abs(arrFunc[index] - arrFunc[index-1]);
            
            arrTv[index] = dblSum;
        }
        
        return arrTv;
    }
    
    /**
     * Compute and return the integral (i.e., the summation of all
     * the elements) of the given function.
     * 
     * @param   arrFunc     function to integrate
     * 
     * @return  the sum of all array elements
     */
    public static double    integral(final double[] arrFunc)  {
        double  dblSum = 0.0;
        for (double dblElem : arrFunc)
            dblSum += dblElem;
        
        return dblSum;
    }
    
    
}
