/**
 * RealFunctionSamples.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2015
 */
package xal.tools.math.fnc;

/**
 * Real-valued function constructed from a set of function samples.
 * 
 * <h3>Unimplemented!</h3>
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2015
 */
public class RealFunctionSamples {


    /*
     * Internal Classes
     */

    /**
     * Class representing the sample of a real-valued function on the real line.  
     * It contains the sample location (on the abscissa) and the function value there.
     * The intent is that one can create a polynomial fit for a real function from 
     * a suitable number of samples from that function.
     *
     * @author Christopher K. Allen
     * @since  Sep 24, 2015
     */
    public class FunctionSample {

        /*
         * Local Attributes
         */

        /** the sample location (abscissa location) */
        private final double    dblLoc;

        /** the sample value */
        private final double    dblVal;


        /*
         * Initialization
         */

        /**
         * Create a new function sample.
         * 
         * @param dblLoc    sample location
         * @param dblVal    sample value
         *
         * @since  Sep 24, 2015   by Christopher K. Allen
         */
        public FunctionSample(double dblLoc, double dblVal) {
            this.dblLoc = dblLoc;
            this.dblVal = dblVal;
        }


        /*
         * Attribute Query
         */

        /**
         * Returns the sampling location.
         * 
         * @return  the sample location
         *
         * @since   Sep 24, 2015   by Christopher K. Allen
         */
        public double getLocation() {
            return dblLoc;
        }

        /**
         * Return the sample value.
         * 
         * @return  the sample value
         *
         * @since  Sep 24, 2015   by Christopher K. Allen
         */
        public double getValue() {
            return dblVal;
        }
    }


    /**
     *
     * Constructor for RealFunctionSamples.
     *
     *
     * @since  Sep 25, 2015   by Christopher K. Allen
     */
    public RealFunctionSamples() {
        // TODO Auto-generated constructor stub
    }

}
