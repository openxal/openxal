/*
 *  DoubleSymmetricGaussian.java
 *
 *  Created on January 3, 2008, 10:59 AM
 */
package xal.extension.fit.lsm;

/**
 *  This class is for data fitting with two Gaussian functions with the same center. 
 *  The function form used in this class is 
 *  y = pedestal+amp*exp(-(x-center0)^2/(sigma^2/2.)) + amp*exp(-(x-center1)^2/(sigma^2/2.)).
 *  Users should keep in mind that guess does not work very well 
 *  when two peaks are not separated clearly. You should use nonlinear methods first,
 *  and then try the linear approach to get errors for parameters. 
 *
 *@author    shishlo
 */
public class DoubleSymmetricGaussian {

	private double sigma = 0.5;
	private double amp = 1.;
	private double center0 = 0.;
	private double center1 = 0.;
	private double pedestal = 0.;

	private double sigma_err = 0.;
	private double amp_err = 0.;
	private double center0_err = 0.;
	private double center1_err = 0.;
	private double pedestal_err = 0.;

	private boolean sigma_incl = true;
	private boolean amp_incl = true;
	private boolean center0_incl = true;
	private boolean center1_incl = true;
	private boolean pedestal_incl = true;

	private ModelFunction1D mf = null;

	private SolverLM solver = new SolverLM();

	private DataStore ds = new DataStore();

	private double[] a = new double[5];
	private double[] a_err = new double[5];

	private double[] x_tmp = new double[1];

	/**
	 *  The "sigma0" parameter
	 */
	public static String SIGMA = "sigma";
	/**
	 *  The "amplitude0" parameter
	 */
	public static String AMP = "amplitude";
	/**
	 *  The "center" parameter
	 */
	public static String CENTER = "center";	
	/**
	 *  The "center0" parameter
	 */
	public static String CENTER0 = "center0";
	/**
	 *  The "center1" parameter
	 */
	public static String CENTER1 = "center1";
	/**
	 *  The "pedestal" parameter
	 */
	public static String PEDESTAL = "pedestal";


	/**
	 *  Creates a new instance of Gaussian
	 */
	public DoubleSymmetricGaussian() {
		init();
	}


	/**
	 *  Description of the Method
	 */
	private void init() {

		mf =
			new ModelFunction1D() {

				public double getValue(double x, double[] a) {
					if (a.length != 5) {
						return 0.;
					}

					double res = a[3] + a[1] * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0]));
					res = res + a[1] * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0]));
					return res;
				}


				public double getDerivative(double x, double[] a, int a_index) {
					double res = 0.;
					if (a.length != 5) {
						return 0.;
					}
					switch (a_index) {
						case 0:
							res = a[1] * (x - a[2]) * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0] * a[0]);
							res = res + a[1] * (x - a[4]) * (x - a[4]) * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0])) / (a[0] * a[0] * a[0]);
							break;
						case 1:
							res = Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) + Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0]));
							break;
						case 2:
							res = a[1] * (x - a[2]) * Math.exp(-(x - a[2]) * (x - a[2]) / (2.0 * a[0] * a[0])) / (a[0] * a[0]);
							break;
						case 3:
							res = 1.0;
							break;
						case 4:
							res = a[1] * (x - a[4]) * Math.exp(-(x - a[4]) * (x - a[4]) / (2.0 * a[0] * a[0])) / (a[0] * a[0]);;
							break;
					}

					return res;
				}

			};
	}


	/**
	 *  Sets parameters array from all parameters
	 */
	private void updateParams() {
		a[0] = sigma;
		a[1] = amp;
		a[2] = center0;
		a[3] = pedestal;
		a[4] = center1;	
	}


	/**
	 *  Returns the parameter value
	 *
	 *@param  key  The parameter name
	 *@return      The parameter value
	 */
	public double getParameter(String key) {
		if (key.equals(SIGMA)) {
			return sigma;
		} else if (key.equals(AMP)) {
			return amp;
		} else if (key.equals(CENTER0)) {
			return center0;
		} else if (key.equals(CENTER1)) {
			return center1;
		} else if (key.equals(CENTER)) {
			return (center0+center1)/2.0;
		} else if (key.equals(PEDESTAL)) {
			return pedestal;
		}
		return 0.;
	}


	/**
	 *  Returns the parameter value error
	 *
	 *@param  key  The parameter name
	 *@return      The parameter value error
	 */
	public double getParameterError(String key) {
		if (key.equals(SIGMA)) {
			return sigma_err;
		} else if (key.equals(AMP)) {
			return amp_err;
		} else if (key.equals(CENTER)) {
			return (center0_err+center1_err)/2.0;
		} else if (key.equals(CENTER0)) {
			return center0_err;
		} else if (key.equals(CENTER1)) {
			return center1_err;
		} else if (key.equals(PEDESTAL)) {
			return pedestal_err;
		}
		return 0.;
	}


	/**
	 *  Includes or excludes the parameter into fitting
	 *
	 *@param  key   The parameter name
	 *@param  incl  The boolean vaiable about including variable into the fitting
	 */
	public void fitParameter(String key, boolean incl) {
		if (key.equals(SIGMA)) {
			sigma_incl = incl;
		} else if (key.equals(AMP)) {
			amp_incl = incl;
		} else if (key.equals(CENTER)) {
			center0_incl = incl;
			center1_incl = incl;
		} else if (key.equals(CENTER0)) {
			center0_incl = incl;
		} else if (key.equals(CENTER1)) {
			center1_incl = incl;
		} else if (key.equals(PEDESTAL)) {
			pedestal_incl = incl;
		}
	}


	/**
	 *  Returns the boolean vaiable about including variable into the fitting
	 *
	 *@param  key  The parameter name
	 */
	public boolean fitParameter(String key) {
		if (key.equals(SIGMA)) {
			return sigma_incl;
		} else if (key.equals(AMP)) {
			return amp_incl;
		} else if (key.equals(CENTER0)) {
			return center0_incl;
		} else if (key.equals(CENTER1)) {
			return center1_incl;
		} else if (key.equals(PEDESTAL)) {
			return pedestal_incl;
		}
		return false;
	}



	/**
	 *  Sets the parameter value
	 *
	 *@param  key  The parameter name
	 *@param  val  The new parameter value
	 */
	public void setParameter(String key, double val) {
		if (key.equals(SIGMA)) {
			sigma = val;
		} else if (key.equals(AMP)) {
			amp = val;
		} else if (key.equals(CENTER0)) {
			center0 = val;
		} else if (key.equals(CENTER1)) {
			center1 = val;
		} else if (key.equals(PEDESTAL)) {
			pedestal = val;
		}
		updateParams();
	}


	/**
	 *  Sets the data attribute of the Gaussian object
	 *
	 *@param  y_arr      Y data array
	 *@param  y_err_arr  Y values error array
	 *@param  x_arr      The new data value
	 */
	public void setData(double[] x_arr,
			double[] y_arr,
			double[] y_err_arr) {

		ds.clear();

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
	 *  Sets the data attribute of the Gaussian object
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
	 *  perform the data fit
	 *
	 *@param  iteration  The number of iterations
	 *@return            Success or not
	 */
	public boolean fit(int iteration) {
		for (int i = 0; i < iteration; i++) {
			if (!fit()) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  perform one step of the data fit
	 *
	 *@return    Success or not
	 */
	public boolean fit() {

		boolean[] mask = new boolean[6];
		mask[0] = sigma_incl;
		mask[1] = amp_incl;
		mask[2] = center0_incl;
		mask[3] = pedestal_incl;
		mask[4] = center1_incl;

		updateParams();

		a_err[0] = 0.;
		a_err[1] = 0.;
		a_err[2] = 0.;
		a_err[3] = 0.;
		a_err[4] = 0.;

		solver = new SolverLM();
		boolean res = solver.solve(ds, mf, a, a_err, mask);

		if (res) {
			sigma = a[0];
			amp = a[1];
			center0 = a[2];
			center1 = a[4];
			pedestal = a[3];

			sigma_err = a_err[0];
			amp_err = a_err[1];
			center0_err = a_err[2];
			center1_err = a_err[4];
			pedestal_err = a_err[3];
		}

		return res;
	}


	/**
	 *  Perform the several iterations of the data fit with guessing the initial
	 *  values of parameters
	 *
	 *@param  iteration  The number of iterations
	 *@return            Success or not
	 */
	public boolean guessAndFit(int iteration) {

		if (!guessAndFit()) {
			return false;
		}

		for (int i = 1; i < iteration; i++) {
			if (!fit()) {
				return false;
			}
		}
		return true;
	}


	/**
	 *  Finds the parameters of Gaussian with initial values defined from raw data
	 *
	 *@return    The true is the initial parameters have been defined successfully
	 */
	public boolean guessAndFit() {
		int n = ds.size();
		double y_min = Double.MAX_VALUE;
		double y_max = -Double.MAX_VALUE;
		double y = 0.;
		for (int i = 0; i < n; i++) {
			y = ds.getY(i);
			if (y > y_max) {
				y_max = y;
			}
			if (y < y_min) {
				y_min = y;
			}
		}
		if (y_min > y_max) {
			return false;
		}
		double y_level = 0.607 * (y_max - y_min) + y_min;
		int n_cross = 0;
		double x_min = Double.MAX_VALUE;
		double x_max = -Double.MAX_VALUE;
		int i_x_min = -1;
		int i_x_max = -1;		
		for (int i = 1; i < n; i++) {
			if ((y_level - ds.getY(i - 1)) * (y_level - ds.getY(i)) <= 0.) {
				n_cross++;
				if (x_min > ds.getArrX(i)[0]) {
					x_min = ds.getArrX(i)[0];
					i_x_min = i;
				}
				if (x_max < ds.getArrX(i)[0]) {
					x_max = ds.getArrX(i)[0];
					i_x_max = i;
				}
			}
		}
		if (x_max <= x_min || i_x_min < 0 || i_x_max < 0) {
			return false;
		}
		
		if( (i_x_max - i_x_min) < 3){
			sigma = Math.abs(x_min - x_max) / 2.0;
			center0 = (x_min + x_max) / 2.0 - sigma*0.1;
			center1 = (x_min + x_max) / 2.0 + sigma*0.1;
			pedestal = Math.min(Math.abs(y_min), Math.abs(y_max));
			amp = (y_max - y_min);
		} else {
			//System.out.println("Debug  i_x_min = " + i_x_min + " i_x_max=" + i_x_max);
			int i_cent = (i_x_min + i_x_max)/2;
			int i_min = -1;
			for (int i = i_x_min; i < i_cent; i++) {
				if(ds.getY(i+1) > ds.getY(i)){
					i_min = i+1;
				}
			}
			if(i_min <0){
				return false;
			}
			center0 = ds.getArrX(i_min)[0];
			double sig0 = ds.getArrX(i_min)[0] - ds.getArrX(i_x_min)[0];
			
			int i_max = -1;
			for (int i = i_x_max; i > i_cent; i--) {
				if(ds.getY(i-1) > ds.getY(i)){
					i_max = i-1;
				}
			}
			if(i_max < 0){
				return false;
			}
			center1 = ds.getArrX(i_max)[0];
			double sig1 = ds.getArrX(i_max)[0] - ds.getArrX(i_x_max)[0];
			
			//System.out.println("Debug  i_min = " + i_min + " i_max=" + i_max);
			
			sigma = (Math.abs(sig0)+Math.abs(sig1))/2.0;
			
			pedestal = Math.min(Math.abs(y_min), Math.abs(y_max));
			amp = (y_max - y_min);			
		}
		
		boolean sigma_incl_ini = sigma_incl;
		boolean amp_incl_ini = amp_incl;
		boolean center0_incl_ini = center0_incl;
		boolean center1_incl_ini = center1_incl;
		boolean pedestal_incl_ini = pedestal_incl;
		
		sigma_incl = false;
		amp_incl = true;
		center0_incl = false;
		center1_incl = false;
		pedestal_incl = false;
		
		boolean res = fit(4);
		if(res == false) {
			return res;
		}
		
		/**
		System.out.println("Debug  s = " + getParameter(DoubleSymmetricGaussian.SIGMA) + " +- " + getParameterError(DoubleSymmetricGaussian.SIGMA));
		System.out.println("Debug  a = " + getParameter(DoubleSymmetricGaussian.AMP) + " +- " + getParameterError(DoubleSymmetricGaussian.AMP));
		System.out.println("Debug  c0  = " + getParameter(DoubleSymmetricGaussian.CENTER0) + " +- " + getParameterError(DoubleSymmetricGaussian.CENTER0));
		System.out.println("Debug  c1  = " + getParameter(DoubleSymmetricGaussian.CENTER1) + " +- " + getParameterError(DoubleSymmetricGaussian.CENTER1));
		System.out.println("Debug  p  = " + getParameter(DoubleSymmetricGaussian.PEDESTAL) + " +- " + getParameterError(DoubleSymmetricGaussian.PEDESTAL));
		*/
		
		sigma_incl =  sigma_incl_ini;
		amp_incl = amp_incl_ini;
		center0_incl =  center0_incl_ini;
		center1_incl =  center1_incl_ini;
		pedestal_incl =  pedestal_incl_ini;		
		
		res = fit(1);
		if(res == false) {
			return res;
		}		
		
		return res;
	}


	/**
	 *  Returns the value of Gaussian function
	 *
	 *@param  x  The x-value
	 *@return    The Gauss function value
	 */
	public double getValue(double x) {
		return mf.getValue(x, a);
	}


	/**
	 *  MAIN for debugging
	 *
	 *@param  args  The array of strings as parameters
	 */
	public static void main(String args[]) {

		double p = 0.2;
		double a = 1.5;
		double c0 = 0.1;
		double c1 = 0.9;
		double s = 0.3;


		int n = 100;
		double x_min = c0 - 3 * s;
		double x_max = c1 + 3 * s;
		double step = (x_max - x_min) / (n - 1);

		double[] x_a = new double[n];
		double[] y_a = new double[n];

		double x = 0.;
		double err_level = 0.0;

		for (int i = 0; i < n; i++) {
			x = x_min + step * i;
			x_a[i] = x;
			y_a[i] = p + a * Math.exp(-(x-c0) * (x-c0) / (2.0*s*s)) + a * Math.exp(-(x-c1) * (x-c1) / (2.0*s*s));
			y_a[i] = y_a[i] * (1.0 + err_level * 2.0 * (Math.random() - 0.5));
		}

		DoubleSymmetricGaussian gs = new DoubleSymmetricGaussian();

		gs.setData(x_a, y_a);

		gs.setParameter(DoubleSymmetricGaussian.SIGMA, s * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.AMP, a * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.CENTER0, c0 * 0.9);
		gs.setParameter(DoubleSymmetricGaussian.CENTER1, c1 * 1.1);
		gs.setParameter(DoubleSymmetricGaussian.PEDESTAL, p * 1.0);

		gs.fitParameter(DoubleSymmetricGaussian.SIGMA, true);
		gs.fitParameter(DoubleSymmetricGaussian.AMP, true);
		gs.fitParameter(DoubleSymmetricGaussian.CENTER0, true);
		gs.fitParameter(DoubleSymmetricGaussian.CENTER1, true);
		gs.fitParameter(DoubleSymmetricGaussian.PEDESTAL, true);

		System.out.println("================START================");
		System.out.println("data error level [%]= " + err_level * 100);
		System.out.println("Main ini: s  = " + s);
		System.out.println("Main ini: a  = " + a);
		System.out.println("Main ini: c0 = " + c0);
		System.out.println("Main ini: c1 = " + c1);
		System.out.println("Main ini: p  = " + p);

		int n_iter = 8;

		boolean res = false;

		//guess does not work very well 
		//when two peaks are not separated clearly
		res = gs.guessAndFit();

		for (int j = 0; j < n_iter; j++) {
			System.out.println("Main: iteration =" + j + "  res = " + res);
			System.out.println("Main: s = " + gs.getParameter(DoubleSymmetricGaussian.SIGMA) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.SIGMA));
			System.out.println("Main: a = " + gs.getParameter(DoubleSymmetricGaussian.AMP) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.AMP));
			System.out.println("Main: c0  = " + gs.getParameter(DoubleSymmetricGaussian.CENTER0) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.CENTER0));
			System.out.println("Main: c1  = " + gs.getParameter(DoubleSymmetricGaussian.CENTER1) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.CENTER1));
			System.out.println("Main: p  = " + gs.getParameter(DoubleSymmetricGaussian.PEDESTAL) + " +- " + gs.getParameterError(DoubleSymmetricGaussian.PEDESTAL));
			res = gs.fit();
		}

		for (int i = 0; i < n; i++) {
			x = x_min + step * i;
			System.out.println("i=" + i + " x=" + x + " y_ini=" + y_a[i] + " model=" + gs.getValue(x));
		}

		n_iter = 100;
		java.util.Date start = new java.util.Date();
		for (int j = 0; j < n_iter; j++) {
			res = gs.fit();
		}
		java.util.Date stop = new java.util.Date();
		double time = (stop.getTime() - start.getTime()) / 1000.;
		time /= n_iter;
		System.out.println("time for one step [sec] =" + time);

	}

}

