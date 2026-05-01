package xal.extension.fit.lsm;

import xal.tools.text.ScientificNumberFormat;

/**
 *  This class is for data fitting with polynomial equation.
 *
 *@author    shishlo
 */
public class Polynomial {
	
	static double[] fact = new double[100];
	
	static {
		fact[0] = 1.0;
		for(int i = 1; i < fact.length; i++){
			fact[i] = fact[i-1]*i;
		}
	}
	

	private double[] a = new double[0];
	private double[] a_err = new double[0];
	private boolean[] mask = new boolean[0];

	private ModelFunction1D mf = null;

	private SolverLSM solver = new SolverLSM();

	private DataStore ds = new DataStore();
	private DataStore ds_tmp = new DataStore();
	
	private double[] x_tmp = new double[1];

	private ScientificNumberFormat frmt = new ScientificNumberFormat(4);


	/**
	 *  Creates a new instance of Polynomial
	 */
	public Polynomial() {
		init();
		setOrder(1);
	}


	/**
	 *  Creates a new instance of Polynomial
	 *
	 *@param  n  The order of the Polynomial object
	 */
	public Polynomial(int n) {
		init();
		setOrder(n);
	}


	/**
	 *  Description of the Method
	 */
	private void init() {

		mf =
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
	}


	/**
	 *  Returns the parameter value
	 *
	 *@param  index  The coefficient for power "index" of the polynomial
	 *@return        The parameter value
	 */
	public double getParameter(int index) {
		return a[index];
	}


	/**
	 *  Returns the parameter value error
	 *
	 *@param  index  The coefficient index for power equals to "index" in the
	 *      polynomial
	 *@return        The parameter value error
	 */
	public double getParameterError(int index) {
		return a_err[index];
	}


	/**
	 *  Includes or excludes the parameter into fitting
	 *
	 *@param  index    The coefficient index for power equals to "index" in the
	 *      polynomial
	 *@param  fitting  The boolean vaiable about including the coefficient into the
	 *      fitting
	 */
	public void fitParameter(int index, boolean fitting) {
		mask[index] = fitting;
	}


	/**
	 *  Returns the boolean vaiable about including the coefficient into the
	 *  fitting
	 *
	 *@param  index  The coefficient index for power equals to "index" in the
	 *      polynomial
	 *@return        fitting The boolean vaiable about including the coefficient
	 *      into the fitting
	 */
	public boolean fitParameter(int index) {
		return mask[index];
	}



	/**
	 *  Sets the parameter value
	 *
	 *@param  val    The new parameter value
	 *@param  index  he coefficient index for power equals to "index" in the
	 *      polynomial
	 */
	public void setParameter(int index, double val) {
		a[index] = val;
	}


	/**
	 *  Sets the data attribute of the Polynomial object
	 *
	 *@param  y_arr      Y data array
	 *@param  y_err_arr  Y values error array
	 *@param  x_arr      The new data value
	 */
	public void setData(double[] x_arr,
			double[] y_arr,
			double[] y_err_arr) {

		ds.clear();
		setToZero();

		if (x_arr.length != y_arr.length) {
			return;
		}

		double[] x = new double[1];

		for (int i = 0; i < x_arr.length; i++) {
			x[0] = x_arr[i];
			if (y_err_arr != null) {
				ds.addRecord(y_arr[i], y_err_arr[i], x);
			} else {
				ds.addRecord(y_arr[i], x);
			}
		}
	}


	/**
	 *  Sets the data attribute of the Polynomial object
	 *
	 *@param  y_arr  Y data array
	 *@param  x_arr  The new data value
	 */
	public void setData(double[] x_arr,
			double[] y_arr) {
		setData(x_arr, y_arr, null);
	}


	/**
	 *  Removes all internal data
	 */
	public void clear() {
		ds.clear();
		setToZero();
	}


	/**
	 *  Sets all coefficients and errors to zero value
	 */
	private void setToZero() {
		for (int i = 0; i < a.length; i++) {
			if (mask[i]) {
				a[i] = 0.;
			}
			a_err[i] = 0.;
		}
	}



	/**
	 *  Sets the order of the Polynomial object
	 *
	 *@param  n  The new order value
	 */
	public void setOrder(int n) {
		a = new double[n + 1];
		a_err = new double[n + 1];
		mask = new boolean[n + 1];
		for (int i = 0; i < mask.length; i++) {
			mask[i] = true;
		}
		setToZero();
	}


	/**
	 *  Returns the order of the Polynomial object
	 *
	 *@return    The order
	 */
	public int getOrder() {
		return (a.length - 1);
	}


	/**
	 *  Adds a data point to the internal data
	 *
	 *@param  x  The x value
	 *@param  y  The y value
	 */
	public void addData(double x, double y) {
		x_tmp[0] = x;
		ds.addRecord(y, x_tmp);
	}


	/**
	 *  Adds a data point to the internal data
	 *
	 *@param  x      The x value
	 *@param  y      The y valu
	 *@param  y_err  The error of the y value
	 */
	public void addData(double x, double y, double y_err) {
		x_tmp[0] = x;
		ds.addRecord(y, y_err, x_tmp);
	}

	/**
	 *  It performs one step of the data fit
	 *
	 *@return    Success or not
	 */
	public boolean fit() {
		setToZero();
		boolean res = solver.solve(ds, mf, a, a_err, mask);
		return res;
	}	
	
	
	/**
	 *  It performs one step of the data fit by using centered data.
	 *  This method could be more accurate in some cases, but you cannot
	 *  use masks to eliminate fitting of some polynomial coefficients.
	 *
	 *@return    Success or not
	 */
	public boolean fitFromCenter() {
		setToZero();
		if(a.length >= fact.length) return false;
		//center data and store in the temporary data container
		double x_avg = 0.;
		double y_avg = 0.;
		int n_data = ds.size();
		if(n_data == 0) return false;
		
		for(int i = 0; i < n_data; i++){
			x_avg = x_avg + ds.getArrX(i)[0];
			y_avg = y_avg + ds.getY(i);
		}
		x_avg = x_avg / n_data;
		y_avg = y_avg / n_data;
		
		ds_tmp.clear();
		for(int i = 0; i < n_data; i++){
			ds_tmp.addRecord(ds.getY(i) - y_avg,ds.getErrY(i),ds.getArrX(i)[0] - x_avg);
		}	
		
		//prepare resulting arrays
		double[] a_tmp = new double[a.length];
		double[] a_err_tmp = new double[a.length];
		boolean[] mask_tmp = new boolean[a.length];
		for(int i = 0; i < a.length; i++){
			a_tmp[i] = 0.;
			a_err_tmp[i] = 0.;
			mask_tmp[i] = true;
		}
		
		boolean res = solver.solve(ds_tmp, mf, a_tmp, a_err_tmp, mask_tmp);
		
		//shift x and y back 
		if(res != false){
			for(int j = 0; j < a.length; j++){			
				double s = 0.;
				double s2 = 0.;
				double x = 1.0;
				double cij = 0.;
				for(int i = j; i < a.length; i++){
					cij = fact[i]/(fact[j]*fact[i-j]);
					s = s + a_tmp[i]*x*cij;
					s2 = s2 + (a_err_tmp[i]*x*cij)*(a_err_tmp[i]*x*cij);
					x = -x*x_avg;
				}
				a[j] = s;
				a_err[j] = Math.sqrt(s2);
			}
			a[0] = a[0] + y_avg;
		}
		return res;
	}


	/**
	 *  Returns the value of Polynomial function
	 *
	 *@param  x  The x-value
	 *@return    The polynimial function value
	 */
	public double getValue(double x) {
		return mf.getValue(x, a);
	}


	/**
	 *  Returns the value of Polynomial function
	 *
	 *@param  x  The x-value
	 *@param  a  The array of coefficients
	 *@return    The polynomial equation value
	 */
	public double getValue(double x, double[] a) {
		return mf.getValue(x, a);
	}


	/**
	 *  Returns the array with the coefficients of the Polynomial
	 *
	 *@return    The array with the coefficients of the Polynomial
	 */
	public double[] getCoefficients() {
		double[] a_new = new double[a.length];
		System.arraycopy(a, 0, a_new, 0, a.length);
		return a_new;
	}


	/**
	 *  Returns the array with the errors of the coefficients of the Polynomial
	 *
	 *@return    The array with the errors of the coefficients of the Polynomial
	 */
	public double[] getCoefficientsErr() {
		double[] a_err_new = new double[a_err.length];
		System.arraycopy(a_err, 0, a_err_new, 0, a_err.length);
		return a_err_new;
	}


	/**
	 *  Return the characteristic equation as a String
	 *
	 *@param  frmt_loc  The format for coefficients
	 *@return           The characteristic equation as a String
	 */
	private String equation(final ScientificNumberFormat frmt_loc ) {
		String eq = new String();
		eq = "Y = ";
		if (a.length <= 0) {
			return eq;
		}
		if (mask[0] == false && a[0] == 0.) {
		} else {
			eq += "(" + frmt_loc.format(a[0]) +
					" +- " + frmt_loc.format(a_err[0]) + ")";
		}
		for (int i = 1; i < a.length; i++) {
			if (mask[i] == false && a[i] == 0.) {
			} else {
				eq += " + x^" + i +
						"*(" + frmt_loc.format(a[i]) +
						" +- " + frmt_loc.format(a_err[i]) + ")";
			}
		}
		return eq;
	}


	/**
	 *  Return the characteristic equation as a String
	 *
	 *@return    The characteristic equation as a String
	 */
	public String equation() {
		return equation(frmt);
	}


	/**
	 *  Return the characteristic equation as a String
	 *
	 *@param  format_pattern  The fortran format pattern, e.g. G12.5
	 *@return                 The characteristic equation as a String
	 */
//	public String equation(String format_pattern) {
//		FortranNumberFormat frmt_loc = new FortranNumberFormat(format_pattern);
//		return equation(frmt_loc);
//	}


	/**
	 *  MAIN for debugging
	 *
	 *@param  args  The array of strings as parameters
	 */
	public static void main(String args[]) {

		int n = 4;
		double[] x = new double[n];
		double[] y = new double[n];
		double[] y_err = new double[n];

		double a0 = 0.5;
		double a3 = 1.5;
		double a5 = 0.25;

		for (int i = 0; i < n; i++) {
			x[i] = i;
			y[i] = a0 + a3 * Math.pow(x[i], 3.0) + a5 * Math.pow(x[i], 5.0);
			y_err[i] = 1;
		}

		int nPoly = 6;
		Polynomial gs = new Polynomial(nPoly);

		gs.fitParameter(1, false);
		gs.fitParameter(2, false);
		gs.fitParameter(4, false);
		gs.fitParameter(6, false);

		gs.setData(x, y, y_err);
		boolean res = gs.fit();

		System.out.println("result = " + res);
		System.out.println("Exact val a0 = " + a0);
		System.out.println("Exact val a3 = " + a3);
		System.out.println("Exact val a5 = " + a5);

		for (int i = 0; i <= nPoly; i++) {
			System.out.println("i = " + i +
					"  a=" + gs.getParameter(i) +
					" err=" + gs.getParameterError(i));
		}

		System.out.println("eq:" + gs.equation());

	}

}

