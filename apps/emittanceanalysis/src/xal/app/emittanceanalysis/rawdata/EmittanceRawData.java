package xal.app.emittanceanalysis.rawdata;

import xal.extension.widgets.plot.*;

/**
 *  This class keeps and operates with raw emittance data. Mostly it is
 *  temporary place for emittance raw data. The
 *  makeColorSurfaceData(ColorSurfaceData csd) method is used to create color
 *  surface data for emittance.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public final class EmittanceRawData {

	//data arrays emmData[ind_position][ind_angle] - signals
	private double[][] emmData = null;

	//angle values for scpecific position angl_arr[ind_position][ind_angle]
	private double[][] angl_arr = null;

	//positions array
	private double[] pos_arr = null;

	//min and max angle for

	//number of positions
	private int nPos = 100;

	//number of angles
	private int nAngl = 30;

	//min and max positions
	private double pos_min = 0.;
	private double pos_max = 0.;

	//angle min and max
	private double angl_min = 0.;
	private double angl_max = 0.;

	//chunk size
	private int chunkSize = 10;

	//info has been initialized or not
	private boolean hasBeenInit = false;

	//take into account data for centering only with values
	//more than that value
	private static double SIGNIFICANCE_PERCENTAGE = 0.5;


	/**
	 *  EmittanceRawData constructor.
	 */
	public EmittanceRawData() {
		emmData = new double[nPos + chunkSize][nAngl + chunkSize];
		angl_arr = new double[nPos + chunkSize][nAngl + chunkSize];
		pos_arr = new double[nPos + chunkSize];
	}


	/**
	 *  Resizes data arrays.
	 *
	 *@param  nPos   The number of slit positions
	 *@param  nAngl  The number of harp wires (different angles)
	 */
	public void resize(int nPos, int nAngl) {
		this.nPos = nPos;
		this.nAngl = nAngl;
			
		if (pos_arr.length > nPos && angl_arr[0].length > nAngl) {
			for (int ip = 0; ip < nPos; ip++) {
				for (int ia = 0; ia < nAngl; ia++) {
					emmData[ip][ia] = 0.;
					angl_arr[ip][ia] = (double) (ia - nAngl / 2);
				}
				pos_arr[ip] = (double) (ip - nPos / 2);
			}
			return;
		}

		emmData = new double[nPos + chunkSize][nAngl + chunkSize];
		angl_arr = new double[nPos + chunkSize][nAngl + chunkSize];
		pos_arr = new double[nPos + chunkSize];
		for (int ip = 0; ip < nPos; ip++) {
			for (int ia = 0; ia < nAngl; ia++) {
				emmData[ip][ia] = 0.;
				angl_arr[ip][ia] = (double) (ia - nAngl / 2);
			}
			pos_arr[ip] = (double) (ip - nPos / 2);
		}

	}


	/**
	 *  Sets the phase space density value for certain position index and angle.
	 *
	 *@param  iPos   The index of harp position
	 *@param  iAngl  The index of angle
	 *@param  pos    The position value
	 *@param  angl   The angle value
	 *@param  val    The signal value
	 */
	public void setRawData(int iPos, int iAngl, double pos, double angl, double val) {
		pos_arr[iPos] = pos;
		angl_arr[iPos][iAngl] = angl;
		emmData[iPos][iAngl] = val;
	}


	/**
	 *  Returns the phase space density value for certain position index and angle.
	 *
	 *@param  x   The transverse coordinate
	 *@param  xp  The transverse angle
	 *@return     The phase space density
	 */
	public double getValue(double x, double xp) {

		if (x < pos_arr[0]) {
			return 0.;
		}
		if (x > pos_arr[nPos - 1]) {
			return 0.;
		}

		int iPos0 = getInd0(pos_arr, nPos, x);

		if (xp < angl_arr[iPos0][0]) {
			return 0.;
		}
		if (xp > angl_arr[iPos0][nAngl - 1]) {
			return 0.;
		}

		int iPos1 = iPos0 + 1;

		double val0 = getInterpVal(angl_arr[iPos0], emmData[iPos0], nAngl, xp);
		double val1 = getInterpVal(angl_arr[iPos1], emmData[iPos1], nAngl, xp);
		return val0 + (val1 - val0) * (x - pos_arr[iPos0]) / (pos_arr[iPos1] - pos_arr[iPos0]);
	}


	/**
	 *  Returns the interpolated value of y(x)
	 *
	 *@param  arr_x  The x-array
	 *@param  arr_y  The y-array
	 *@param  nP     The number of points in arrays
	 *@param  x      The x-value
	 *@return        The y-interpolated value
	 */
	private double getInterpVal(double[] arr_x, double[] arr_y, int nP, double x) {
		int ind0 = getInd0(arr_x, nP, x);
		int ind1 = ind0 + 1;
		if(Math.abs(arr_x[ind1] - arr_x[ind0]) < 1.0e-6){
			return arr_y[ind0];
		}		
		return arr_y[ind0] + (arr_y[ind1] - arr_y[ind0]) * (x - arr_x[ind0]) / (arr_x[ind1] - arr_x[ind0]);
	}


	/**
	 *  Returns the index in array for which arr[index] <= x < arr[index+1]
	 *
	 *@param  arr  The array
	 *@param  nP   The number of elements in the array
	 *@param  x    The x-value
	 *@return      The index for which arr[index] <= x < arr[index+1]
	 */
	private int getInd0(double[] arr, int nP, double x) {
		int ind0 = 0;
		int ind1 = nP - 1;
		if (x <= arr[ind0]) {
			return ind0;
		}
		if (x >= arr[ind1]) {
			return (ind1 - 1);
		}
		int ind = 0;
		while ((ind1 - ind0) != 1) {
			ind = (ind1 + ind0) / 2;
			if (arr[ind] > x && arr[ind0] <= x) {
				ind1 = ind;
			} else {
				ind0 = ind;
			}
		}
		return ind0;
	}


	/**
	 *  Returns min position value;
	 *
	 *@return    The minPos value
	 */
	public double getMinPos() {
		return pos_min;
	}


	/**
	 *  Returns max position value.
	 *
	 *@return    The maxPos value
	 */
	public double getMaxPos() {
		return pos_max;
	}


	/**
	 *  Returns min angle value.
	 *
	 *@return    The minAngl value
	 */
	public double getMinAngl() {
		return angl_min;
	}


	/**
	 *  Returns max angle value.
	 *
	 *@return    The maxAngl value
	 */
	public double getMaxAngl() {
		return angl_max;
	}


	/**
	 *  Shift positions and angles to make a centered phase density.
	 */
	private void makeCentered() {
		double pos_avg = 0.;
		double sum = 0.;
		double angle_avg = 0.;
		double val = 0.;

		double val_max = 0.;

		for (int ip = 0; ip < nPos; ip++) {
			for (int ia = 0; ia < nAngl; ia++) {
				if (emmData[ip][ia] > val_max) {
					val_max = emmData[ip][ia];
				}
			}
		}

		for (int ip = 0; ip < nPos; ip++) {
			for (int ia = 0; ia < nAngl; ia++) {
				val = emmData[ip][ia];
				if (val > SIGNIFICANCE_PERCENTAGE * val_max) {
					sum += val;
					pos_avg += val * pos_arr[ip];
					angle_avg += val * angl_arr[ip][ia];
				}
			}
		}
		if (sum > 0.) {
			pos_avg /= sum;
			angle_avg /= sum;
		} else {
			pos_avg = 0.;
			angle_avg = 0.;
		}
		shiftPos(-pos_avg);
		shiftAngles(-angle_avg);
	}


	/**
	 *  Shift positions by delta.
	 *
	 *@param  delta  The position shift value
	 */
	private void shiftPos(double delta) {
		for (int ip = 0; ip < nPos; ip++) {
			pos_arr[ip] += delta;
		}
		pos_min += delta;
		pos_max += delta;
	}


	/**
	 *  Shift angles by delta.
	 *
	 *@param  delta  The angle shift value
	 */
	private void shiftAngles(double delta) {
		for (int ip = 0; ip < nPos; ip++) {
			for (int ia = 0; ia < nAngl; ia++) {
				angl_arr[ip][ia] += delta;
			}
		}
		angl_min += delta;
		angl_max += delta;
	}


	/**
	 *  Sets the initialization flag.
	 *
	 *@param  hasBeenInit  The initialization flag
	 */
	public void setInitialized(boolean hasBeenInit) {
		this.hasBeenInit = hasBeenInit;
		if (hasBeenInit) {
			pos_min = pos_arr[0];
			pos_max = pos_arr[nPos - 1];

			angl_min = angl_arr[0][0];
			angl_max = angl_arr[0][0];

			for (int ip = 0; ip < nPos; ip++) {
				for (int ia = 0; ia < nAngl; ia++) {
					if (angl_min >= angl_arr[ip][ia]) {
						angl_min = angl_arr[ip][ia];
					}
					if (angl_max <= angl_arr[ip][ia]) {
						angl_max = angl_arr[ip][ia];
					}
				}
			}
		} else {
			pos_min = 0.;
			pos_max = 0.;
			angl_min = 0.;
			angl_max = 0.;
		}
	}


	/**
	 *  Returns the initialization flag.
	 *
	 *@return    The initialization flag
	 */
	public boolean isInitialized() {
		return hasBeenInit;
	}


	/**
	 *  Fills out the color surface data to show the phase space density.
	 *
	 *@param  csd  The ColorSurfaceData instance to be filled out
	 */
	public void makeColorSurfaceData(ColorSurfaceData csd) {
		if (!hasBeenInit) {
			return;
		}
		makeCentered();
		int nX = csd.getSizeX();
		int nY = csd.getSizeY();
		csd.setMinMaxX(pos_min, pos_max);
		csd.setMinMaxY(angl_min, angl_max);

		double xgr = 0.;
		double ygr = 0.;
		double val = 0.;

		for (int ix = 0; ix < nX; ix++) {
			xgr = csd.getX(ix);
			for (int iy = 0; iy < nY; iy++) {
				ygr = csd.getY(iy);
				val = getValue(xgr, ygr);
				csd.setValue(ix, iy, val);
			}
		}
	}
}

