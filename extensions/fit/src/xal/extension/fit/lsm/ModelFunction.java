package xal.extension.fit.lsm;

/**
 *  This interface define a model function and its partial derivatives that will
 *  be used in the linear least square method. Users have to implement this
 *  interface to use LSM solver from this package.
 *
 *@author    shishlo
 */
public interface ModelFunction {
	/**
	 *  Returns the value of the model function for the particular set of
	 *  independent variables and parameters
	 *
	 *@param  x  The array with the set of independent variables
	 *@param  a  The array with set of parameters that should be found
	 *@return    The value of the function
	 */
	public double getValue(double[] x, double[] a);


	/**
	 *  Returns the partial derivative of the model function for the particular set
	 *  of independent variables and parameters and the index of parameters for
	 *  which the derivative is calculated
	 *
	 *@param  x        The array with the set of independent variables
	 *@param  a        The array with set of parameters that should be found
	 *@param  a_index  The index of parameters for which the derivative is
	 *                 calculated
	 *@return          The value of the function
	 */
	public double getDerivative(double[] x, double[] a, int a_index);
}

