package xal.extension.fit.lsm;

import xal.tools.ArrayMath;

/**
 *  The least square method solver
 *
 *@author    shishlo
 */
public class SolverLSM implements FitSolver {

	private double[] a = new double[0];
	private int[] a_ind = new int[0];
	private double[] a_err = new double[0];

	private double[][] ATWA = new double[0][0];

	private double[] ATWY = new double[0];

	private double[] W = new double[0];


	/**
	 *  Constructor for the SolverLSM object
	 */
	public SolverLSM() { }


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
			ATWY = new double[na];
		}

		int count = 0;
		for (int i = 0; i < a_ini.length; i++) {
			a_err_ini[i] = 0.;
			if (a_use[i] == true) {
				a[count] = a_ini[i];
				a_ind[count] = i;
				a_err[count] = 0.;
				count++;
			}
		}

		int nD = ds.size();
		if (nD < na) {
			return false;
		}

		if (nD != W.length) {
			W = new double[nD];
		}

		for (int i = 0; i < nD; i++) {
			W[i] = 1.0;
		}

		boolean err_exist = true;

		for (int i = 0; i < nD; i++) {
			if (ds.getErrY(i) <= 0.) {
				err_exist = false;
				break;
			}
		}

		if (err_exist == true) {
			for (int i = 0; i < nD; i++) {
				W[i] = 1. / (ds.getErrY(i)*ds.getErrY(i));
			}
		}

		//calculation ATWY
		for (int i = 0; i < na; i++) {
			ATWY[i] = 0.;
			for (int j = 0; j < nD; j++) {
				ATWY[i] += mf.getDerivative(ds.getArrX(j), a_ini, a_ind[i]) *
						W[j] *
						(ds.getY(j) - mf.getValue(ds.getArrX(j), a_ini));
			}
		}

		//calculation ATWA
		for (int i = 0; i < na; i++) {
			for (int k = 0; k < na; k++) {
				ATWA[i][k] = 0.;
				for (int j = 0; j < nD; j++) {
					ATWA[i][k] += mf.getDerivative(ds.getArrX(j), a_ini, a_ind[i]) *
							mf.getDerivative(ds.getArrX(j), a_ini, a_ind[k]) *
							W[j];
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
			a_ini[a_ind[i]] = a[i];
			a_err_ini[a_ind[i]] = Math.sqrt(Math.abs(ATWA[i][i]));
		}

		if (err_exist != true) {
			double y2_avg = 0.;
			double y_t = 0.;
			double y_a = 0.;
			for (int j = 0; j < nD; j++) {
				y_a = mf.getValue(ds.getArrX(j), a_ini);
				y_t = ds.getY(j);
				y2_avg += (y_a - y_t) * (y_a - y_t);
			}
			double err = 0.;
			if(nD != na){
			   err = y2_avg / (nD - na);
			}
			err = Math.sqrt(Math.abs(err));
			for (int i = 0; i < na; i++) {
				a_err_ini[a_ind[i]] *= err;
			}
		}
		return true;
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

		double[] a = new double[4];
		a[0] = 0.3;
		a[1] = 1.0;
		a[2] = 0.3;
		a[3] = 0.3;

		double[] a_err = new double[4];
		a_err[0] = 0.;
		a_err[1] = 0.;
		a_err[2] = 0.;
		a_err[3] = 0.;

		boolean[] mask = new boolean[4];
		mask[0] = true;
		mask[1] = true;
		mask[2] = true;
		mask[3] = true;

		DataStore ds = new DataStore(y_arr, y_err_arr, x_arr);

		SolverLSM solver = new SolverLSM();

		System.out.println("======BEFORE=========");

		for (int i = 0; i < a.length; i++) {
			System.out.println("i=" + i + " a=" + a[i] + " +- " + a_err[i]);
		}
		System.out.println("======START Solver=======");

		boolean res = solver.solve(ds, mf, a, a_err, mask);

		System.out.println("sucess =" + res);

		for (int i = 0; i < a.length; i++) {
			System.out.println("i=" + i + " a=" + a[i] + " +- " + a_err[i]);
		}
		System.out.println("======STOP=======");

		a[0] = 1.;
		a[1] = 1.;
		a[2] = 1.;
		a[3] = 1.;
		System.out.println("  x        y          y_appr   ");
		for (int i = 0; i < x_arr.length; i++) {
			System.out.println(" " + x_arr[i][0] + "  "
					 + y_arr[i] + "  "
					 + mf.getValue(x_arr[i][0], a));
		}

	}
	
	
	
	
	
	
	
	

}

