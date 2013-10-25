package xal.extension.fit.lsm;

/**
 *  This is an abstract adapter class for a model function with one independent
 *  variable. It delegates calls of getValue and getDerivative methods of
 *  interface to the abstract methods with the same names and different
 *  signatures. The signatures are different in a very simple way. The array of
 *  independent variables is replaced by a simple double value.
 *
 *@author    shishlo
 */
public abstract class ModelFunction1D implements ModelFunction {

	/**
	 *  Returns the value of the model function for the particular value of the
	 *  independent variable and parameters
	 *
	 *@param  x  The value of independent variable
	 *@param  a  The array with set of parameters that should be found
	 *@return    The value of the function
	 */
	public abstract double getValue(double x, double[] a);


	/**
	 *  Returns the partial derivative of the model function for the particular
	 *  value of independent variable and parameters and the index of parameters
	 *  for which the derivative is calculated
	 *
	 *@param  x        The value of independent variable
	 *@param  a        The array with set of parameters that should be found
	 *@param  a_index  The index of parameters for which the derivative is
	 *      calculated
	 *@return          The value of the function
	 */
	public abstract double getDerivative(double x, double[] a, int a_index);


	/**
	 *  Returns the value of the model function for the particular set of
	 *  independent variables and parameters
	 *
	 *@param  x  The array with the set of independent variables
	 *@param  a  The array with set of parameters that should be found
	 *@return    The value of the function
	 */
	public final double getValue(double[] x, double[] a) {
		return getValue(x[0], a);
	}


	/**
	 *  Returns the partial derivative of the model function for the particular set
	 *  of independent variables and parameters and the index of parameters for
	 *  which the derivative is calculated
	 *
	 *@param  x        The array with the set of independent variables
	 *@param  a        The array with set of parameters that should be found
	 *@param  a_index  The index of parameters for which the derivative is
	 *      calculated
	 *@return          The value of the function
	 */
	public final double getDerivative(double[] x, double[] a, int a_index) {
		return getDerivative(x[0], a, a_index);
	}
}

