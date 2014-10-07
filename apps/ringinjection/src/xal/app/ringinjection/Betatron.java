/*
 *  Betatron.java
 *
 *  Created on February 24
 */
package xal.app.ringinjection;
import xal.extension.fit.lsm.*;

import java.util.*;
import java.net.*;
import java.io.*;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.event.*;

import xal.smf.proxy.*;
import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.tools.*;
import xal.ca.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.ca.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.beam.calc.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;
import java.util.HashMap;
import xal.tools.xml.XmlDataAdaptor;

import xal.model.probe.traj.*;
import xal.model.probe.*;
import xal.model.xml.*;
import xal.sim.scenario.*;
import java.util.*;
import java.lang.*;
import xal.tools.math.r3.R3;
import xal.model.alg.*;




/**
 * This class is for data fitting with betatron motion function.
 * The class uses the online model to get pha
 *@author    cousineau
 */
public class Betatron {

	private double phase = 0;
	private double amp = 0.05;

	private double phase_err = 0.;
	private double amp_err = 0.;

	private boolean phase_incl = true;
	private boolean amp_incl = true;

	public boolean horizontal_data = true;

	private ModelFunction1D mf = null;

	private SolverLSM solver = new SolverLSM();

	private DataStore ds = new DataStore();

	private double[] a = new double[2];
	private double[] a_err = new double[2];

	private double[] x_tmp = new double[1];

	private AcceleratorSeqCombo seq;
	private Accelerator accl = new Accelerator();
	private TransferMapProbe probe;
	private Scenario scenario;
	private Trajectory<TransferMapState> traj;
	private SimpleSimResultsAdaptor _simulationResultsAdaptor;

	/**
	 *  The "phase" parameter
	 */
	public static String PHASE = "phase";
	/**
	 *  The "amplitude" parameter
	 */
	public static String AMP = "amp";


	/**
	 *  Creates a new instance of Gaussian
	 */

	GenDocument doc;

	public Betatron(GenDocument aDocument) {

		doc=aDocument;
		init();
	}


	/**
	 *  Description of the Method
	 */
	private void init() {

		mf = new ModelFunction1D() {

			public double getValue(double s, double[] a) {
				double res=0.0;
				double[] twiss = new double[6];
				twiss = (double[])getTwiss(s);

				if (a.length != 2) {
					return 0.0;
				}

				if(horizontal_data){
					res = a[0]*Math.sqrt(twiss[0])*Math.cos(twiss[2] + a[1]);

				}
				else{
					res = a[0]*Math.sqrt(twiss[1])*Math.cos(twiss[3] + a[1]);
				}
				return res;
			}


			public double getDerivative(double s, double[] a, int a_index) {
				double res = 0.0;
				double[] twiss = new double[6];
				twiss = (double[])getTwiss(s);

				if (a.length != 2) {
					return 0.;
				}
				switch (a_index) {
					case 0:
						if(horizontal_data){
							res=Math.sqrt(twiss[0])*Math.cos(twiss[2] + a[1]);
						}
						else{
							res=Math.sqrt(twiss[1])*Math.cos(twiss[3] + a[1]);
						}
						break;
					case 1:
						if(horizontal_data){
							res =-a[0]*Math.sqrt(twiss[0])*Math.sin(twiss[2]+a[1]);
						}
						else{
							res =-a[0]*Math.sqrt(twiss[1])*Math.sin(twiss[3]+a[1]);
						}
						break;
				}
				return res;
			}
		};
	}

	public void setupModel(){

		accl = doc.getAccelerator();
		seq = accl.getComboSequence("Ring");

		try{
			TransferMapTracker tracker = new TransferMapTracker();
			//tracker.initializeFromEditContext(seq);
			probe = ProbeFactory.getTransferMapProbe(seq, tracker);
			scenario = Scenario.newScenarioFor(seq);
			scenario.setProbe(probe);
			//scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
			//testFixedOrbit(0.005, 0.005);
			scenario.setStartElementId("Ring_Inj:Foil");
			scenario.resync();
			scenario.run();

			traj = probe.getTrajectory();
			_simulationResultsAdaptor = new SimpleSimResultsAdaptor( traj );
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
	}


	private double[] getTwiss(double pos){

		double[] twissparams = new double[6];

		try{
			double position = scenario.getPositionRelativeToStart(pos);
			TransferMapState state = traj.statesInPositionRange(position - 0.00001, position + 0.00001).get(0);
			Twiss[] twiss = _simulationResultsAdaptor.computeTwissParameters( state );
			twissparams[0]=twiss[0].getBeta();
			twissparams[1]=twiss[1].getBeta();
			//System.out.println("twiss is = " + twiss[0] + " " + state.getElementId());
			//System.out.println("twiss is = " + twiss[0].getBeta() +  " " + twiss[1].getBeta());

			R3 phase = _simulationResultsAdaptor.computeBetatronPhase( state );
			twissparams[2] = phase.getx();
			twissparams[3] = phase.gety();

			PhaseVector orbit = _simulationResultsAdaptor.computeFixedOrbit( state );

			twissparams[4] = orbit.getx();
			twissparams[5] = orbit.gety();

		}
		catch(Exception exception){
			exception.printStackTrace();
		}

		return twissparams;
	}

	private void testFixedOrbit(double hstrength, double vstrength){
		scenario.setModelInput(seq.getNodeWithId("Ring_Mag:DCH_B06"), ElectromagnetPropertyAccessor.PROPERTY_FIELD, hstrength);
		scenario.setModelInput(seq.getNodeWithId("Ring_Mag:DCV_B07"), ElectromagnetPropertyAccessor.PROPERTY_FIELD, vstrength);
	}

	/**
	 *  Sets parameters array from all parameters
	 */
	private void updateParams() {
		a[0] = amp;
		a[1] = phase;
	}


	/**
	 *  Returns the parameter value
	 *
	 *@param  key  The parameter name
	 *@return      The parameter value
	 */
	public double getParameter(String key) {
		if (key.equals(PHASE)) {
			return phase;
		} else if (key.equals(AMP)) {
			return amp;
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
		if (key.equals(PHASE)) {
			return phase_err;
		} else if (key.equals(AMP)) {
			return amp_err;
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
		if (key.equals(PHASE)) {
			phase_incl = incl;
		} else if (key.equals(AMP)) {
			amp_incl = incl;
		}
	}


	/**
	 *  Returns the boolean vaiable about including variable into the fitting
	 *
	 *@param  key  The parameter name
	 */
	public boolean fitParameter(String key) {
		if (key.equals(PHASE)) {
			return phase_incl;
		} else if (key.equals(AMP)) {
			return amp_incl;
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
		if (key.equals(PHASE)) {
			phase = val;
		} else if (key.equals(AMP)) {
			amp = val;
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

		boolean[] mask = new boolean[2];
		mask[0] = amp_incl;
		mask[1] = phase_incl;


		updateParams();

		a_err[0] = 0.;
		a_err[1] = 0.;

		boolean res = solver.solve(ds, mf, a, a_err, mask);

		if (res) {
			amp = a[0];
			phase = a[1];

			amp_err = a_err[0];
			phase_err = a_err[1];
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

		phase = 0.0;
		amp = ((y_max - y_min))/2.0;  //This gives only an order of magnitude approximation.

		boolean res = fit();
		return res;
	}


	/**
	 *  Returns the value of Gaussian function
	 *
	 *@param  x  The x-value
	 *@return    The Gauss function value
	 */
	public double getValue(double x) {
		return ((ModelFunction1D) mf).getValue(x, a);
	}


	/**
	 *  MAIN for debugging
	 *
	 *@param  args  The array of strings as parameters
	 */

	/*public static void main(String args[]) {

		double p = 0.0;
		double a = 1.5;


		double[] x_a = new double[n];
		double[] y_a = new double[n];

		double x = 0.;
		double err_level = 0.05;


		Betatron bt = new Betatron();

		bt.setData(x_a, y_a);

		bt.setParameter(Gaussian.PHASE, s * 1.5);
		bt.setParameter(Gaussian.AMP, a * 0.9);

		bt.fitParameter(Gaussian.PHASE, true);
		bt.fitParameter(Gaussian.AMP, true);

		System.out.println("================START================");
		System.out.println("data error level [%]= " + err_level * 100);
		System.out.println("Main ini: a = " + a);
		System.out.println("Main ini: p = " + p);

		int n_iter = 8;

		boolean res = false;

		res = gs.guessAndFit();

		for (int j = 0; j < n_iter; j++) {
	 System.out.println("Main: iteration =" + j + "  res = " + res);
	 System.out.println("Main: p = " + bt.getParameter(Gaussian.PHASE) + " +- " + gs.getParameterError(Gaussian.SIGMA));
	 System.out.println("Main: a = " + gs.getParameter(Gaussian.AMP) + " +- " + gs.getParameterError(Gaussian.AMP));
	 System.out.println("Main: c = " +
	 res = bt.fit();
		}


		for (int i = 0; i < n; i++) {
	 x = x_min + step * i;
	 System.out.println("i=" + i + " x=" + x + " y_ini=" + y_a[i] + " model=" + bt.getValue(x));
		}


		n_iter = 100;
		java.util.Date start = new java.util.Date();
		for (int j = 0; j < n_iter; j++) {
	 res = bt.fit();
		}
		java.util.Date stop = new java.util.Date();
		double time = (stop.getTime() - start.getTime()) / 1000.;
		time /= n_iter;
		System.out.println("time for one step [sec] =" + time);

	 }
	 */

}

