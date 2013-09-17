package xal.app.ringbpmviewer;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.text.*;

import xal.tools.plot.*;
import xal.tools.swing.*;
import xal.tools.scan.UpdatingEventController;

/**
 *  The wrapper for the FunctionGraphsJPanel to show the wave form of the array
 *  PV. After tests it should be relocated to the SNS plotting package.
 *
 *@author     shishlo
 */
public class PVWaveFormPanel {

	//-------------------------------------------------------------
	//panels
	//-------------------------------------------------------------

	private JPanel waveFormPanel = new JPanel(new BorderLayout());
	private JPanel cntrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));

	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	private double[] arr_x = new double[0];
	private double[] arr_y = new double[0];

	private double[] arr_a_x = new double[0];
	private double[] arr_a_y = new double[0];

	//-------------------------------------------------------------
	//knobs on the panel
	//-------------------------------------------------------------

	//the controll knobs
	private JRadioButton useLimits_Button = new JRadioButton("Use WF index limits.", false);

	private JLabel fromTurn_Label =
			new JLabel(" From index #:", JLabel.LEFT);
	private JSpinner fromTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(0, 0, 2000, 1));

	private JLabel toTurn_Label =
			new JLabel(" to #:", JLabel.LEFT);
	private JSpinner toTurn_Spinner =
			new JSpinner(new SpinnerNumberModel(50, 0, 2000, 1));

	private TitledBorder border = null;

	//formatters
	private SmartFormaterX smartFormatterX = new SmartFormaterX();
	private SmartFormaterY smartFormatterY = new SmartFormaterY();

	//data
	BasicGraphData gdWF = new BasicGraphData();
	//BasicGraphData gdAnl = new CubicSplineGraphData();
	BasicGraphData gdAnl = new BasicGraphData();

	//the fit line width
	private int fit_width = 2;


	/**
	 *  Constructor for the PVWaveFormPanel object
	 */
	public PVWaveFormPanel() {

		GP.setOffScreenImageDrawing(true);
		GP.setSmartGL(false);
		GP.refreshGraphJPanel();

		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "Title");
		border.setTitleColor(Color.blue);
		waveFormPanel.setBorder(border);

		JPanel tmp_grph = new JPanel(new BorderLayout());
		tmp_grph.add(GP, BorderLayout.CENTER);
		tmp_grph.setBorder(etchedBorder);

		waveFormPanel.add(tmp_grph, BorderLayout.CENTER);
		waveFormPanel.add(cntrlPanel, BorderLayout.SOUTH);

		//make panels
		cntrlPanel.add(useLimits_Button);
		cntrlPanel.add(fromTurn_Label);
		cntrlPanel.add(fromTurn_Spinner);
		cntrlPanel.add(toTurn_Label);
		cntrlPanel.add(toTurn_Spinner);

		//set graph data propertiews
		gdWF.setGraphProperty(GP.getLegendKeyString(), "Wave Form");
		gdWF.setDrawLinesOn(false);
		gdWF.setDrawPointsOn(true);
		gdWF.setImmediateContainerUpdate(false);
		gdWF.setGraphColor(Color.blue);

		gdAnl.setGraphProperty(GP.getLegendKeyString(), "Fitting");
		gdAnl.setDrawLinesOn(true);
		gdAnl.setDrawPointsOn(false);
		gdAnl.setImmediateContainerUpdate(false);
		gdAnl.setGraphColor(Color.red);
		gdAnl.setLineThick(fit_width);

		GP.addGraphData(gdWF);
		GP.addGraphData(gdAnl);
		GP.setLegendButtonVisible(true);

		useLimits_Button.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					updateGraphs();
				}
			});

		fromTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateGraphs();
				}
			});

		toTurn_Spinner.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					updateGraphs();
				}
			});

	}


	/**
	 *  Returns the JPanel instance with a wave form graph
	 *
	 *@return    The panel instance
	 */
	public JPanel getPanel() {
		return waveFormPanel;
	}


	/**
	 *  Returns only the graph panel
	 *
	 *@return    The graph panel instance which is inside of the PVWaveFormPanel
	 *      panel
	 */
	public FunctionGraphsJPanel getGraphPanel() {
		return GP;
	}


	/**
	 *  Sets the new name of the X-axis
	 *
	 *@param  name  The new name
	 */
	public void setAxisNameX(String name) {
		GP.setAxisNameX(name);
	}


	/**
	 *  Sets the new name of the Y-axis
	 *
	 *@param  name  The new name
	 */
	public void setAxisNameY(String name) {
		GP.setAxisNameY(name);
	}


	/**
	 *  Clear all data
	 */
	public void clear() {
		gdAnl.removeAllPoints();
		gdWF.removeAllPoints();
		arr_x = new double[0];
		arr_y = new double[0];
		arr_a_x = new double[0];
		arr_a_y = new double[0];
		updateGraphs();
	}

	/**
	 *  Clear analysis data
	 */
	public void clearAnalysis() {
		gdAnl.removeAllPoints();
		arr_a_x = new double[0];
		arr_a_y = new double[0];
	}


	/**
	 *  Returns the useLimits attribute of the PVWaveFormPanel object
	 *
	 *@return    The useLimits value
	 */
	public boolean getUseLimits() {
		return useLimits_Button.isSelected();
	}

	/**
	 *  Returns the low limit attribute of the PVWaveFormPanel object
	 *
	 *@return    The low limit value
	 */
	public int getLowLimit() {
		return ((Integer) fromTurn_Spinner.getValue()).intValue();
	}

	/**
	 *  Returns the upper limit attribute of the PVWaveFormPanel object
	 *
	 *@return    The upper limit value
	 */
	public int getUppLimit() {
		return ((Integer) toTurn_Spinner.getValue()).intValue();
	}

	/**
	 *  Returns the array of indexes for the waveform
	 *
	 *@return    The array of indexes for the waveform
	 */
	public double[] getArrX() {
		return arr_x;
	}

	/**
	 *  Returns the array of values for the waveform
	 *
	 *@return    The array of values for the wavefo
	 */
	public double[] getArrY() {
		return arr_y;
	}


	/**
	 *  Sets the array of values for the waveform
	 *
	 *@param  arr_yIn  The array of values for the waveform
	 */
	public void setData(double[] arr_yIn) {

		if(arr_x.length != arr_yIn.length) {
			arr_x = new double[arr_yIn.length];
			arr_y = new double[arr_yIn.length];
			for(int i = 0; i < arr_yIn.length; i++) {
				arr_x[i] = (double) i;
			}
		}

		for(int i = 0; i < arr_yIn.length; i++) {
			arr_y[i] = arr_yIn[i];
		}

		gdWF.updateValues(arr_x, arr_y);
		updateGraphs();
	}

	/**
	 *  Sets the array of indexes and values for the fit to the waveform data
	 *
	 *@param  arr_yIn  The analysis arrays
	 *@param  arr_xIn  The new analysisData value
	 */
	public void setAnalysisData(double[] arr_xIn, double[] arr_yIn) {

		if(arr_xIn.length != arr_yIn.length) {
			clearAnalysis();
			return;
		}

		if(arr_a_x.length != arr_yIn.length) {
			arr_a_x = new double[arr_yIn.length];
			arr_a_y = new double[arr_yIn.length];
		}

		for(int i = 0; i < arr_yIn.length; i++) {
			arr_a_x[i] = arr_xIn[i];
			arr_a_y[i] = arr_yIn[i];
		}

		gdAnl.updateValues(arr_a_x, arr_a_y);
		updateGraphs();
	}

	/**
	 *  Updates graphics on the waveform panel
	 */
	public void updateGraphs() {
		double x_min = Double.MAX_VALUE;
		double x_max = -Double.MAX_VALUE;

		double y_min = Double.MAX_VALUE;
		double y_max = -Double.MAX_VALUE;

		if(useLimits_Button.isSelected()) {
			x_min = ((Integer) fromTurn_Spinner.getValue()).intValue();
			x_max = ((Integer) toTurn_Spinner.getValue()).intValue();
			if(x_min > x_max) {
				fromTurn_Spinner.setValue(new Integer((int) Math.floor(x_max)));
				toTurn_Spinner.setValue(new Integer((int) Math.floor(x_min)));
				x_min = ((Integer) fromTurn_Spinner.getValue()).intValue();
				x_max = ((Integer) toTurn_Spinner.getValue()).intValue();
			}

			int x_min_ind = (int) Math.floor(x_min);
			int x_max_ind = (int) Math.floor(x_max);

			int x_max_ind_tmp = Math.min(x_max_ind, gdWF.getNumbOfPoints() - 1);
			for(int i = x_min_ind; i <= x_max_ind_tmp; i++) {
				if(gdWF.getY(i) < y_min) {
					y_min = gdWF.getY(i);
				}
				if(gdWF.getY(i) > y_max) {
					y_max = gdWF.getY(i);
				}
			}

			x_max_ind_tmp = Math.min(x_max_ind, gdAnl.getNumbOfPoints() - 1);
			for(int i = x_min_ind; i <= x_max_ind_tmp; i++) {
				if(gdAnl.getY(i) < y_min) {
					y_min = gdAnl.getY(i);
				}
				if(gdAnl.getY(i) > y_max) {
					y_max = gdAnl.getY(i);
				}
			}

		} else {

			if(gdWF.getNumbOfPoints() > 0) {
				if(gdWF.getMinX() < x_min) {
					x_min = gdWF.getMinX();
				}
				if(gdWF.getMaxX() > x_max) {
					x_max = gdWF.getMaxX();
				}
				if(gdWF.getMinY() < y_min) {
					y_min = gdWF.getMinY();
				}
				if(gdWF.getMaxY() > y_max) {
					y_max = gdWF.getMaxY();
				}
			}

			if(gdAnl.getNumbOfPoints() > 0) {
				if(gdAnl.getMinX() < x_min) {
					x_min = gdAnl.getMinX();
				}
				if(gdAnl.getMaxX() > x_max) {
					x_max = gdAnl.getMaxX();
				}
				if(gdAnl.getMinY() < y_min) {
					y_min = gdAnl.getMinY();
				}
				if(gdAnl.getMaxY() > y_max) {
					y_max = gdAnl.getMaxY();
				}
			}
		}

		if(x_min >= x_max || y_min >= y_max) {
			x_max = +1.0;
			x_min = -1.0;
			y_max = +1.0;
			y_min = -1.0;
		}

		//System.out.println("debug x_min=" + x_min + " x_max" + x_max + " y_min=" + y_min + " y_max=" + y_max);

		smartFormatterX.makeAnalysis((int) Math.floor(x_min), (int) Math.floor(x_max));

		smartFormatterY.makeAnalysis(y_min, y_max);

		//System.out.println("debug smart X min=" + smartFormatterX.getMin() +
		//		" step=" + smartFormatterX.getStep() +
		//		" n=" + smartFormatterX.getStepN() +
		//		" ticks=" + smartFormatterX.getMinorTicks());

		GP.clearZoomStack();
		GP.setExternalGL(null);

		GP.setNumberFormatX(smartFormatterX.getFormat());
		GP.setLimitsAndTicksX(smartFormatterX.getMin(),
				smartFormatterX.getMax(),
				smartFormatterX.getStep(),
				smartFormatterX.getMinorTicks());

		GP.setNumberFormatY(smartFormatterY.getFormat());
		GP.setLimitsAndTicksY(smartFormatterY.getMin(),
				smartFormatterY.getMax(),
				smartFormatterY.getStep());

		GP.refreshGraphJPanel();
	}


	/**
	 *  Sets the font of all GUI elements
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		border.setTitleFont(fnt);

		useLimits_Button.setFont(fnt);
		fromTurn_Label.setFont(fnt);
		toTurn_Label.setFont(fnt);

		fromTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) fromTurn_Spinner.getEditor()).getTextField().setFont(fnt);

		toTurn_Spinner.setFont(fnt);
		((JSpinner.DefaultEditor) toTurn_Spinner.getEditor()).getTextField().setFont(fnt);

	}


	/**
	 *  Sets the title of the PVWaveFormPanel object
	 *
	 *@param  title  The new title
	 */
	public void setTitle(String title) {
		border.setTitle(title);
	}


	/**
	 *  Sets the title color of the PVWaveFormPanel object
	 *
	 *@param  color  The new titleColor value
	 */
	public void setTitleColor(Color color) {
		border.setTitleColor(color);
	}

	/**
	 *  Test method
	 *
	 *@param  args  No arguments needed
	 */
	public static void main(String args[]) {
		JFrame mainFrame = new JFrame("Test of the PVWaveFormPanel class");
		mainFrame.addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					System.exit(0);
				}
			});

		mainFrame.getContentPane().setLayout(new BorderLayout());

		PVWaveFormPanel wfp = new PVWaveFormPanel();

		mainFrame.getContentPane().add(wfp.getPanel(), BorderLayout.CENTER);

		wfp.setAxisNameY("Value, [mm]");
		wfp.setAxisNameX("turn");

		mainFrame.pack();
		mainFrame.setSize(new Dimension(300, 430));
		mainFrame.setVisible(true);

		double[] y_arr = new double[256];

		int n_start = 1;
		int n_step = 1;

		double y_amp = 0.1;

		while(true) {

			for(int i = 0; i < y_arr.length; i++) {
				y_arr[i] = y_amp * Math.sin(i * 0.1);
			}

			wfp.setData(y_arr);

			try {
				Thread.sleep(1000000);
			} catch(InterruptedException exc) {}

			wfp.clear();

			try {
				Thread.sleep(4000);
			} catch(InterruptedException exc) {}

			n_step = n_step + 5;
			y_amp = y_amp + 1.0;

		}

	}


	//===========================================================
	// Auxiliary classes
	//===========================================================

	/**
	 *  The smart formatter calculates min, max, step values and chooses the right
	 *  format for two initial parameters v_min, v_max.
	 *
	 *@author     shishlo
	 */
	class SmartFormaterY {

		private double value_min = 0.;
		private double value_max = 0.;
		private double scale = 0.0;

		private DecimalFormat fmt_result = new DecimalFormat("#.###E0");

		private DecimalFormat[] simpleFormats = new DecimalFormat[5];

		private DecimalFormat[] scientificFormats = new DecimalFormat[5];

		private DecimalFormat universalFormat = new DecimalFormat("#.###E0");


		/**
		 *  Constructor for the SmartFormaterY object
		 */
		public SmartFormaterY() {
			simpleFormats[0] = new DecimalFormat("###0");
			simpleFormats[1] = new DecimalFormat("###0.#");
			simpleFormats[2] = new DecimalFormat("###0.##");
			simpleFormats[3] = new DecimalFormat("###0.###");
			simpleFormats[4] = new DecimalFormat("###0.####");

			scientificFormats[0] = new DecimalFormat("#.E0");
			scientificFormats[1] = new DecimalFormat("#.#E0");
			scientificFormats[2] = new DecimalFormat("#.##E0");
			scientificFormats[3] = new DecimalFormat("#.###E0");
			scientificFormats[4] = new DecimalFormat("#.####E0");
		}


		/**
		 *  Analyzes the limits and calculates suggestion for new limits and formats
		 *
		 *@param  v_min  The min value
		 *@param  v_max  The max value
		 */
		void makeAnalysis(double v_min, double v_max) {
			fmt_result = universalFormat;
			double range = v_max - v_min;
			scale = 0.;
			if(range > 0.) {
				scale = Math.pow(10., Math.floor(1.000001 * Math.log(range) / Math.log(10.0)));
			}
			if(scale == 0.) {
				scale = 1.;
				value_min = -1.0;
				value_max = +1.0;
				return;
			}
			value_min = scale * Math.floor(v_min / scale);
			value_max = scale * Math.ceil(v_max / scale);
			if(value_min * value_max == 0. && (scale == Math.abs(value_max) || scale == Math.abs(value_min))) {
				scale = scale / 5.0;
				value_min = scale * Math.floor(v_min / scale);
				value_max = scale * Math.ceil(v_max / scale);
			}

			double[] arr = new double[3];
			arr[0] = scale;
			arr[1] = value_min;
			arr[2] = value_max;

			double zz_max = Math.max(Math.abs(arr[1]), Math.abs(arr[2]));
			int nV = (int) (Math.floor(1.0001 * Math.log(zz_max) / Math.log(10.0)));
			if(nV >= 0) {
				nV += 1;
			} else {
				nV -= 1;
			}

			zz_max = zz_max / Math.abs(arr[0]);
			int nD = (int) (Math.floor(1.0001 * Math.log(zz_max) / Math.log(10.0)));
			if(nD >= 0) {
				nD += 1;
			}

			//This is for zoom, so we want to increase number of significant digits
			nD = nD + 1;

			if(nV >= 4) {
				int n = Math.min(4, Math.abs(nD));
				fmt_result = scientificFormats[n];
				return;
			}

			if(nV > 0 && nV < 4) {
				if(nV >= nD) {
					fmt_result = simpleFormats[0];
					return;
				} else {
					int n = Math.min(4, Math.abs(nV - nD));
					fmt_result = simpleFormats[n];
					return;
				}
			}

			if(nV < 0 && nV > -4) {
				int n = Math.abs(nV) + Math.abs(nD) - 2;
				if(n <= 4) {
					fmt_result = simpleFormats[n];
					return;
				}
			}
		}


		/**
		 *  Returns the step value
		 *
		 *@return    The step value
		 */
		double getStep() {
			return scale;
		}


		/**
		 *  Returns the min value
		 *
		 *@return    The min value
		 */
		double getMin() {
			return value_min;
		}


		/**
		 *  Returns the max value
		 *
		 *@return    The max value
		 */
		double getMax() {
			return value_max;
		}


		/**
		 *  Returns the smart format
		 *
		 *@return    The decimal format
		 */
		DecimalFormat getFormat() {
			return fmt_result;
		}
	}

	/**
	 *  The smart formatter calculates min, max, step values and chooses the right
	 *  format for two initial parameters v_min, v_max in the case of intigers
	 *  limits
	 *
	 *@author     shishlo
	 */
	class SmartFormaterX {

		private double value_min = -1.;
		private double value_max = +1.;
		private double step = 1.0;
		private int nSteps = 5;
		private int nMinorTicks = 0;

		private DecimalFormat fmt_result = new DecimalFormat("###0");

		private int[] possibleSteps = {1, 2, 3, 4, 5, 8, 10, 12, 15, 20, 25, 30, 40, 50, 60, 80, 100, 150, 200, 250, 400, 500};
		private int[] possibleTicks = {0, 1, 2, 3, 4, 3, 1, 3, 2, 3, 4, 2, 3, 4, 3, 4, 4, 2, 3, 4, 3, 4};

		/**
		 *  Constructor for the SmartFormaterX object
		 */
		public SmartFormaterX() { }


		/**
		 *  Analyzes the limits and calculates suggestion for new limits and formats
		 *
		 *@param  iv_min  The Parameter
		 *@param  iv_max  The Parameter
		 */
		void makeAnalysis(int iv_min, int iv_max) {

			if(iv_min % 5 != 0) {
				iv_min = iv_min - iv_min % 5;
			}

			int irange = Math.abs(iv_max - iv_min);

			if(irange < 6) {
				step = 1.0;
				value_min = (double) iv_min;
				value_max = (double) iv_max;
				nSteps = irange;
				nMinorTicks = 0;
				return;
			}

			if(irange / possibleSteps[possibleSteps.length - 1] < 5) {
				nSteps = irange / possibleSteps[0];
				int ind = 0;

				while(nSteps >= 5) {
					ind++;
					nSteps = irange / possibleSteps[ind];
				}

				step = possibleSteps[ind];
				if(nSteps*step != irange) {
					nSteps++;
				}
				value_min = (double) iv_min;
				value_max = value_min + (step * nSteps);
				nMinorTicks = possibleTicks[ind];

			} else {
				nSteps = 5;
				step = irange / nSteps;
				if(irange % nSteps != 0) {
					nSteps++;
				}
				value_min = (double) iv_min;
				value_max = value_min + (step * nSteps);
				nMinorTicks = 0;
			}

		}


		/**
		 *  Returns the step value
		 *
		 *@return    The step value
		 */
		double getStep() {
			return step;
		}

		/**
		 *  Returns the number of steps
		 *
		 *@return    The number of steps
		 */
		double getStepN() {
			return nSteps;
		}

		/**
		 *  Returns the min value
		 *
		 *@return    The min value
		 */
		double getMin() {
			return value_min;
		}


		/**
		 *  Returns the max value
		 *
		 *@return    The max value
		 */
		double getMax() {
			return value_max;
		}

		/**
		 *  Returns the minorTicks attribute of the SmartFormaterX object
		 *
		 *@return    The minorTicks value
		 */
		int getMinorTicks() {
			return nMinorTicks;
		}


		/**
		 *  Returns the smart format
		 *
		 *@return    The decimal format
		 */
		DecimalFormat getFormat() {
			return fmt_result;
		}
	}

}

