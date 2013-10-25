package xal.extension.fit.lsm;

/**
 *  The interface for fitting solvers.
 *
 *@author    shishlo
 */
public interface FitSolver {

	/**
	 *  Solve the fitting problem.
	 *
	 *@param  ds         The data for fitting.
	 *@param  a_ini      The initial values of the parameters.
	 *@param  a_err_ini  The parameter values' errors.
	 *@param  a_use      The mask array specifying if the parameter will be used in
	 *      fitting.
	 *@param  mf         The model function
	 *@return            The boolean value specifying success of fitting.
	 */
	public boolean solve(DataStore ds, ModelFunction mf,
			double[] a_ini, double[] a_err_ini,
			boolean[] a_use);

}

