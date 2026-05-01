package xal.extension.fit.lsm;

import xal.tools.ArrayMath;

/**
 *  The Levenberg-Marquardt fitting solver.
 *
 *@author    shishlo
 */
public class SolverLM implements FitSolver {

	private double[] a = new double[0];
	private Solution solution = new Solution();

	//Parameters of the method
	private double factor = 10.;
	private double lambda_ini = 0.001;
	private double lambda_max = 10000.0;
	private double eps_toll = 1.0E-5;

	int iter_limit = 5;
	int total_iter_limit = 30;


	/**
	 *  Constructor for the SolverLM object
	 */
	public SolverLM() { }


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
			boolean[] a_use) {

		int nD = ds.size();
		int count = 0;
		for (int i = 0; i < a_ini.length; i++) {
			if (a_use[i] == true) {
				count++;
			}
		}

		if (nD < count) {
			return false;
		}

		if (a_ini.length != a.length) {
			a = new double[a_ini.length];
		}

		for (int i = 0; i < a_ini.length; i++) {
			a[i] = a_ini[i];
			a_err_ini[i] = 0.;
		}

		//calc. y_abs_avg
		double y_avg = 0.;
		for (int j = 0; j < nD; j++) {
			y_avg = Math.abs(ds.getY(j));
		}
		y_avg /= nD;

		solution.init(ds, mf, a, a_use);

		double chi2ini = 0.;
		double chi2new = 0.;

		double chi2_min = 0.;

		double dev_ini = 0.;
		double dev_new = 0.;

		boolean i_stop = false;
		double lambda = lambda_ini;
		double d = 0.;

		int iter = 0;
		int iter_total = 0;

		boolean result = true;

		while (!i_stop) {
			iter_total++;

			if (lambda > lambda_max) {
				return result;
			}

			if (iter_total > total_iter_limit) {
				return result;
			}

			if (!solution.solve(lambda)) {
				return false;
			}

			chi2ini = solution.getChi2ini();
			chi2new = solution.getChi2new();

			if (iter_total == 1) {
				chi2_min = Math.min(chi2ini, chi2new);
				if (chi2ini >= chi2new) {
					solution.setParam(a_ini);
				}
				solution.setParamErr(a_err_ini);
			} else {
				if (chi2new <= chi2_min) {
					chi2_min = chi2new;
					solution.setParam(a_ini);
					solution.setParamErr(a_err_ini);
				}
			}

			dev_ini = solution.getDevAvgIni();
			dev_new = solution.getDevAvgNew();

			//System.out.println("debug lambda=" + lambda + "  dev_ini=" + dev_ini + " dev_new=" + dev_new);
			//System.out.println("debug lambda=" + lambda + "  chi2ini=" + chi2ini + " chi2new=" + chi2new);

			if (y_avg > 0.) {
				d = Math.abs(dev_ini - dev_new) / y_avg;
				if (d <= eps_toll) {
					break;
				}
			}

			if (chi2ini <= chi2new) {
				lambda *= factor;
				iter = 0;
			} else {
				result = true;
				lambda /= factor;
				solution.setParam(a);
				solution.init(ds, mf, a, a_use);
				iter++;
			}
			if (iter >= iter_limit) {
				i_stop = true;
			}
		}

		return result;
	}


	/**
	 *  Sets the lambdaFactor attribute of the SolverLM object
	 *
	 *@param  factor  The new lambdaFactor value
	 */
	public void setLambdaFactor(double factor) {
		this.factor = factor;
	}


	/**
	 *  Gets the lambdaFactor attribute of the SolverLM object
	 *
	 *@return    The lambdaFactor value
	 */
	public double getLambdaFactor() {
		return factor;
	}


	/**
	 *  Sets the lambdaIni attribute of the SolverLM object
	 *
	 *@param  lambda_ini  The new lambdaIni value
	 */
	public void setLambdaIni(double lambda_ini) {
		this.lambda_ini = lambda_ini;
	}


	/**
	 *  Gets the lambdaIni attribute of the SolverLM object
	 *
	 *@return    The lambdaIni value
	 */
	public double getLambdaIni() {
		return lambda_ini;
	}


	/**
	 *  Sets the lambdaMax attribute of the SolverLM object
	 *
	 *@param  lambda_max  The new lambdaMax value
	 */
	public void setLambdaMax(double lambda_max) {
		this.lambda_max = lambda_max;
	}


	/**
	 *  Gets the lambdaMax attribute of the SolverLM object
	 *
	 *@return    The lambdaMax value
	 */
	public double getLambdaMax() {
		return lambda_max;
	}



	/**
	 *  Sets the toll attribute of the SolverLM object
	 *
	 *@param  eps_toll  The new toll value
	 */
	public void setToll(double eps_toll) {
		this.eps_toll = eps_toll;
	}


	/**
	 *  Gets the toll attribute of the SolverLM object
	 *
	 *@return    The toll value
	 */
	public double getToll() {
		return eps_toll;
	}



	/**
	 *  Sets the iterLimit attribute of the SolverLM object
	 *
	 *@param  total_iter_limit  The new iterLimit value
	 */
	public void setIterLimit(int total_iter_limit) {
		this.total_iter_limit = total_iter_limit;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public int getIterLimit() {
		return total_iter_limit;
	}


	/**
	 *  MAIN for debugging
	 *
	 *@param  args  The array of strings as parameters
	 */
	public static void main(String args[]) {

		ModelFunction1D mf =
			new ModelFunction1D() {

				public double getValue(double x, double[] a) {
					double res = 0.;
					double x_pow = 1.;
					for (int i = 0; i < a.length; i++) {
						res += x_pow * a[i];
						x_pow *= x;
					}
					return res;
				}


				public double getDerivative(double x, double[] a, int a_index) {
					double res = 1.;
					for (int i = 0; i < a_index; i++) {
						res *= x;
					}
					return res;
				}

			};

		int nPoints = 11;

		double[] y_arr = new double[nPoints];
		double[] y_err_arr = new double[nPoints];
		double[][] x_arr = new double[nPoints][1];
		double z = 0.;
		for (int i = 0; i < nPoints; i++) {
			z = i + 1;
			x_arr[i][0] = z;
			y_arr[i] = 1.0 + z + z * z + z * z * z;
			y_err_arr[i] = 1.0;
			//if(i%2 == 0) y_arr[i] += 1.0;
		}

		double[] a_fit_ = new double[4];
		a_fit_[0] = 0.3;
		a_fit_[1] = 1.0;
		a_fit_[2] = 0.3;
		a_fit_[3] = 0.3;

		double[] a_fit__err = new double[4];
		a_fit__err[0] = 0.;
		a_fit__err[1] = 0.;
		a_fit__err[2] = 0.;
		a_fit__err[3] = 0.;

		boolean[] mask = new boolean[4];
		mask[0] = true;
		mask[1] = true;
		mask[2] = true;
		mask[3] = true;

		DataStore ds = new DataStore(y_arr, y_err_arr, x_arr);

		SolverLM solver = new SolverLM();

		System.out.println("======BEFORE=========");

		for (int i = 0; i < a_fit_.length; i++) {
			System.out.println("i=" + i + " a=" + a_fit_[i] + " +- " + a_fit__err[i]);
		}
		System.out.println("======START Solver=======");

		boolean res = solver.solve(ds, mf, a_fit_, a_fit__err, mask);

		System.out.println("sucess =" + res);

		for (int i = 0; i < a_fit_.length; i++) {
			System.out.println("i=" + i + " a=" + a_fit_[i] + " +- " + a_fit__err[i]);
		}
		System.out.println("======STOP=======");

		//a_fit_[0] = 1.;
		//a_fit_[1] = 1.;
		//a_fit_[2] = 1.;
		//a_fit_[3] = 1.;
		System.out.println("  x        y          y_appr   ");
		for (int i = 0; i < x_arr.length; i++) {
			System.out.println(" " + x_arr[i][0] + "  "
					 + y_arr[i] + "  "
					 + mf.getValue(x_arr[i][0], a_fit_));
		}
		System.out.println("============");

	}


	/**
	 *  Auxiliary inner class.
	 *
	 *@author    shishlo
	 */
	class Solution {

		private double[] a_ini = new double[0];
		private double[] a_new = new double[0];

		private double[] a = new double[0];
		private int[] a_ind = new int[0];
		private double[] a_err = new double[0];

		private double[][] ATWA_ini = new double[0][0];
		private double[][] ATWA = new double[0][0];

		private double[] ATWY = new double[0];

		private double[] W = new double[0];

		private DataStore ds = null;

		//array for (y_exp - y_theory) array
		private double[] dlt_arr = new double[0];

		private double chi2_ini = 0.;
		private double chi2_new = 0.;

		private double dev_avg_ini = 0.;
		private double dev_avg_new = 0.;

		private ModelFunction mf = null;

		private boolean err_exist = false;


		/**
		 *  Constructor for the Solution object
		 */
		Solution() { }


		/**
		 *  Initialize solution
		 *
		 *@param  ds_in     The data store
		 *@param  mf_in     The model function
		 *@param  a_ini_in  The initial parameters
		 *@param  a_use     The boolean mask on parameters to use in fitting
		 */
		void init(DataStore ds_in,
				ModelFunction mf_in,
				double[] a_ini_in,
				boolean[] a_use) {
			ds = ds_in;
			mf = mf_in;

			if (a_ini.length != a_ini_in.length) {
				a_ini = new double[a_ini_in.length];
				a_new = new double[a_ini_in.length];
			}

			for (int i = 0; i < a_ini.length; i++) {
				a_ini[i] = a_ini_in[i];
				a_new[i] = a_ini_in[i];
			}

			int na = 0;
			for (int i = 0; i < a_ini.length; i++) {
				if (a_use[i] == true) {
					na++;
				}
			}

			if (na != a.length) {
				a = new double[na];
				a_ind = new int[na];
				a_err = new double[na];
				ATWA = new double[na][na];
				ATWA_ini = new double[na][na];
				ATWY = new double[na];
			}

			int count = 0;
			for (int i = 0; i < a_ini.length; i++) {
				if (a_use[i] == true) {
					a[count] = a_ini[i];
					a_ind[count] = i;
					a_err[count] = 0.;
					count++;
				}
			}

			int nD = ds.size();
			if (nD != W.length) {
				W = new double[nD];
				dlt_arr = new double[nD];
			}

			for (int i = 0; i < nD; i++) {
				W[i] = 1.0;
			}

			err_exist = true;

			for (int i = 0; i < nD; i++) {
				if (ds.getErrY(i) <= 0.) {
					err_exist = false;
					break;
				}
			}

			if (err_exist == true) {
				for (int i = 0; i < nD; i++) {
					W[i] = 1. / (ds.getErrY(i) * ds.getErrY(i));
				}
			}

			//calculation ATWY
			chi2_ini = 0.;
			for (int j = 0; j < nD; j++) {
				dlt_arr[j] = ds.getY(j) - mf.getValue(ds.getArrX(j), a_ini);
				chi2_ini += dlt_arr[j] * dlt_arr[j] / W[j];
			}

			dev_avg_ini = 0.;
			for (int j = 0; j < nD; j++) {
				dev_avg_ini += Math.abs(dlt_arr[j]);
			}
			dev_avg_ini /= nD;

			for (int i = 0; i < na; i++) {
				ATWY[i] = 0.;
				for (int j = 0; j < nD; j++) {
					ATWY[i] += mf.getDerivative(ds.getArrX(j), a_ini, a_ind[i]) *
							W[j] * dlt_arr[j];
				}
			}

			//calculation ATWA
			for (int i = 0; i < na; i++) {
				for (int k = 0; k < na; k++) {
					ATWA_ini[i][k] = 0.;
					for (int j = 0; j < nD; j++) {
						ATWA_ini[i][k] += mf.getDerivative(ds.getArrX(j), a_ini, a_ind[i]) *
								mf.getDerivative(ds.getArrX(j), a_ini, a_ind[k]) *
								W[j];
					}
				}
			}

		}


		/**
		 *  Description of the Method
		 *
		 *@param  lambda  Description of the Parameter
		 *@return         Description of the Return Value
		 */
		boolean solve(double lambda) {
			int na = a.length;
			int nD = ds.size();

			for (int i = 0; i < na; i++) {
				for (int k = 0; k < na; k++) {
					ATWA[i][k] = ATWA_ini[i][k];
					if (i == k) {
						ATWA[i][k] += lambda;
					}
				}
			}

			boolean res = ArrayMath.invertMatrix(ATWA);
			if (res != true) {
				return false;
			}

			for (int i = 0; i < na; i++) {
				for (int k = 0; k < na; k++) {
					a[i] += ATWA[i][k] * ATWY[k];
				}
			}

			for (int i = 0; i < na; i++) {
				a_new[a_ind[i]] = a[i];
			}

			chi2_new = 0.;
			for (int j = 0; j < nD; j++) {
				dlt_arr[j] = ds.getY(j) - mf.getValue(ds.getArrX(j), a_new);
				chi2_new += dlt_arr[j] * dlt_arr[j] / W[j];
			}

			dev_avg_new = 0.;
			for (int j = 0; j < nD; j++) {
				dev_avg_new += Math.abs(dlt_arr[j]);
			}
			dev_avg_new /= nD;

			return true;
		}


		/**
		 *  Returns average deviation from initial data
		 *
		 *@return    The deviation value
		 */
		double getDevAvgIni() {
			return dev_avg_ini;
		}


		/**
		 *  Returns average deviation from initial data
		 *
		 *@return    The deviation value
		 */
		double getDevAvgNew() {
			return dev_avg_new;
		}


		/**
		 *  Gets the chi2ini attribute of the Solution object
		 *
		 *@return    The chi2ini value
		 */
		double getChi2ini() {
			return chi2_ini;
		}


		/**
		 *  Gets the chi2new attribute of the Solution object
		 *
		 *@return    The chi2new value
		 */
		double getChi2new() {
			return chi2_new;
		}


		/**
		 *  Sets the new values of parameters to the external array.
		 *
		 *@param  a_ini_in  The external array.
		 */
		void setParam(double[] a_ini_in) {
			int na = a.length;

			for (int i = 0; i < a_ini_in.length; i++) {
				a_ini_in[i] = a_new[i];
			}
		}


		/**
		 *  Sets the new errors of parameters to the external array.
		 *
		 *@param  a_err_in  The new array of errors values
		 */
		void setParamErr(double[] a_err_in) {
			int na = a.length;
			int nD = ds.size();

			for (int i = 0; i < a_err_in.length; i++) {
				a_err_in[i] = 0.;
			}

			if (nD <= (na - 1)) {
				return;
			}

			for (int i = 0; i < na; i++) {
				for (int k = 0; k < na; k++) {
					ATWA[i][k] = ATWA_ini[i][k];
				}
			}

			boolean res = ArrayMath.invertMatrix(ATWA);

			if (!res) {
				return;
			}

			if (err_exist) {
				for (int i = 0; i < na; i++) {
					a_err_in[a_ind[i]] = Math.sqrt(Math.abs(ATWA[i][i]));
				}
			} else {
				double coeff = Math.sqrt(getChi2new() / (nD - na));
				for (int i = 0; i < na; i++) {
					a_err_in[a_ind[i]] = coeff * Math.sqrt(Math.abs(ATWA[i][i]));
				}
			}

		}

	}

}

