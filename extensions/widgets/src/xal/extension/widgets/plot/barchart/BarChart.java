package xal.extension.widgets.plot.barchart;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.text.*;

import xal.tools.text.FortranNumberFormat;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.*;

/**
 *  The wrapper for the FunctionGraphsJPanel to show the bar chart
 *
 *@author     shishlo
 *created    October 10, 2005
 */
public class BarChart {

	private JPanel barChartPanel = new JPanel(new BorderLayout());

	private FunctionGraphsJPanel GP = new FunctionGraphsJPanel();

	private BarColumnColor bcColor = new BarColumnColor();

	private Vector<BarColumn> barColumns = new java.util.Vector<BarColumn>();

	private TitledBorder border = null;

	private MarkerFormat MarkerFormat = null;

	private SmartFormater formatter = new SmartFormater();

	private FortranNumberFormat fortranFrmt = new FortranNumberFormat("G8.3");

	//the bar line width
	private volatile int width = 3;

	//internal curve data
	private Vector<CurveData> cvV = new Vector<CurveData>();

	private String emptyStr = new String(" ");


	/**
	 *  Constructor for the BarChart object
	 */
	public BarChart() {

		//set the initial size of the store for curve data
		for (int i = 0; i < 5; i++) {
			cvV.add(new CurveData());
		}

		barChartPanel.add(GP, BorderLayout.CENTER);

		Border etchedBorder = BorderFactory.createEtchedBorder();
		border = BorderFactory.createTitledBorder(etchedBorder, "Title");
		border.setTitleColor(Color.blue);
		barChartPanel.setBorder(border);

		MarkerFormat = new MarkerFormat(barColumns);

		GP.setSmartGL(false);
		GP.setNumberFormatX(MarkerFormat);

		GP.addHorLimitsListener(
								new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int nClmns = barColumns.size();
				int nMaxLines = 0;
				java.util.Iterator<BarColumn> itr = barColumns.iterator();
				while (itr.hasNext()) {
					BarColumn bc = itr.next();
					if (nMaxLines < bc.size()) {
						nMaxLines = bc.size();
					}
				}

				if (nClmns > 0 && nMaxLines > 0) {
					//int iMin = GP.getScreenX(GP.getCurrentMinX());
					//int iMax = GP.getScreenX(GP.getCurrentMaxX());
					int iMin = GP.getScreenX(GP.getInnerMinX());
					int iMax = GP.getScreenX(GP.getInnerMaxX());

					width = (int) ((iMax - iMin) / (1.9 * nMaxLines * nClmns));
					if (width < 1) {
						width = 1;
					}

					for (int i = 1, n = Math.min(nClmns * nMaxLines + 1, cvV.size()); i < n; i++) {
						CurveData cd = cvV.get(i);
						cd.setLineWidth(width);
					}
				}

				Runnable runRefresh =
				new Runnable() {
					public void run() {
						GP.refreshGraphJPanel();
					}
				};
				Thread mThread = new Thread(runRefresh);
				mThread.start();
			}
		});

		//operations with clicked point object
		JLabel infoLabel = new JLabel("  Clicked Point Info: ", JLabel.CENTER);

		FunctionGraphsJPanel.ClickedPoint cpObj = GP.getClickedPointObject();
		cpObj.xValueLabel = new JLabel("X-Marker=", JLabel.RIGHT);
		cpObj.xValueFormat = MarkerFormat;
		cpObj.xValueText = new JTextField(10);
		cpObj.xValueText.setHorizontalAlignment(JTextField.CENTER);
		cpObj.xValueText.setForeground(Color.blue);

		cpObj.yValueLabel = new JLabel("    Value=", JLabel.RIGHT);
		cpObj.yValueFormat = fortranFrmt;
		cpObj.yValueText = new JTextField(12);
		cpObj.yValueText.setHorizontalAlignment(JTextField.CENTER);
		cpObj.yValueText.setForeground(Color.blue);

		JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
		infoPanel.add(infoLabel);
		infoPanel.add(cpObj.xValueLabel);
		infoPanel.add(cpObj.xValueText);
		infoPanel.add(cpObj.yValueLabel);
		infoPanel.add(cpObj.yValueText);

		barChartPanel.add(infoPanel, BorderLayout.SOUTH);
	}


	/**
	 *  Returns the JPanel instance with a Bar Chart
	 *
	 *@return    The panel instance
	 */
	public JPanel getPanel() {
		return barChartPanel;
	}


	/**
	 *  Returns only the graph panel
	 *
	 *@return    The graph panel instance which is inside of the BarChart panel
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
	 *  Returns the BarColumnColor instance of the BarChart object. User can use
	 *  it if the color schema should be changed.
	 *
	 *@return    The BarColumnColor instance
	 */
	public BarColumnColor getBarColumnColor() {
		return bcColor;
	}


	/**
	 *  Clear all data
	 */
	public void clear() {
		final Vector<BarColumn> v = new Vector<BarColumn>();
		setBarColumns(v);
	}


	/**
	 *  Sets the list of bar columns to the BarChart object
	 *
	 *@param  clmns  The new list of bar columns
	 */
	public void setBarColumns( final Vector<BarColumn> clmns ) {

		GP.removeAllCurveData();
		barColumns.clear();
		int nCv = 0;
		for ( final BarColumn bc : clmns ) {
			barColumns.add(bc);
			nCv += bc.size();
		}

		//resize the store for Curve Data
		if (nCv > (cvV.size()-2)) {
			for (int i = cvV.size() - 1, n = (nCv + 5); i < n; i++) {
				cvV.add(new CurveData());
			}
		}

		//calculate the width of the lines in the bar
		width = 3;

		GP.clearZoomStack();
		GP.setExternalGL(null);

		if (barColumns.size() < 10) {
			GP.setLimitsAndTicksX(0., barColumns.size() + 1.0, 1.0);
		}

		updateChart();
	}


	/**
	 *  Updates graphics part of the bar chart
	 */
	public void updateChart() {
		double val_min = Double.MAX_VALUE;
		double val_max = -Double.MAX_VALUE;
		Vector<CurveData> cdV = new Vector<CurveData>();

		int maxMarkLength = 1;
		int nClmns = barColumns.size();
		int nMaxLines = 0;
		for ( final BarColumn bc : barColumns ) {
			if (bc.show()) {
				for (int j = 0; j < bc.size(); j++) {
					if (val_min > bc.value(j)) {
						val_min = bc.value(j);
					}
					if (val_max < bc.value(j)) {
						val_max = bc.value(j);
					}
				}
			}
			if (nMaxLines < bc.size()) {
				nMaxLines = bc.size();
			}
			if (maxMarkLength < bc.marker().length()) {
				maxMarkLength = bc.marker().length();
			}
		}

		String tmp_str = "";
		for (int i = 0; i < maxMarkLength; i++) {
			tmp_str = tmp_str + " ";
		}
		emptyStr = tmp_str;

		//System.out.println("debug =========== val_min=" + val_min + " val_max=" + val_max);

		if (val_min * val_max > 0.) {
			if (val_min > 0.) {
				val_min = 0.;
			} else {
				val_max = 0.;
			}
		}

		int iMin = GP.getScreenX(GP.getCurrentMinX());
		int iMax = GP.getScreenX(GP.getCurrentMaxX());
		//System.out.println("debug iMin=" + iMin + "  iMax=" + iMax);
		width = (int) ((iMax - iMin) / (1.9 * nMaxLines * nClmns));
		//System.out.println("debug width=" + width);
		if (width < 1) {
			width = 1;
		}

		//make line from
		CurveData cd = cvV.get(0);
		if (nClmns > 0) {
			cd.clear();
			cd.addPoint(0., 0.);
			cd.addPoint(1.0 * (nClmns + 1), 0.);
			cd.setColor(Color.black);
			cd.setLineWidth(1);
			cd.findMinMax();
			cdV.add(cd);
		}

		int cvCount = 1;
		for (int i = 1; i <= nClmns; i++) {
			BarColumn bc = barColumns.get(i - 1);
			if (bc.show()) {
				double d_min = i - 0.35;
				double d_max = i + 0.35;
				int nL = bc.size();
				double st = (d_max - d_min) / nL;
				for (int j = 0; j < nL; j++) {
					if (bc.show(j)) {
						if(cvCount < cvV.size()){
							cd = cvV.get(cvCount);
						}
						else{
							cd = new CurveData();
							cvV.add(cd);
						}
						cd.clear();
						cd.addPoint(d_min + (j + 0.5) * st, 0.);
						cd.addPoint(d_min + (j + 0.5) * st, bc.value(j));
						cd.setLineWidth(width);
						if (bc.getColor(j) == null) {
							cd.setColor(bcColor.getColor(j));
						} else {
							cd.setColor(bc.getColor(j));
						}
						cd.findMinMax();
						cdV.add(cd);
						cvCount++;
					}
				}
			}
		}

		//System.out.println("debug ===========start plotting=============== nClmns= " + nClmns);
		if (val_min < val_max) {
			formatter.makeAnalysis(val_min, val_max);
			GP.setNumberFormatY(formatter.getFormat());
			GP.setLimitsAndTicksY(formatter.getMin(), formatter.getMax(), formatter.getStep());
		}

		if (barColumns.size() >= 10) {
			GP.getCurrentGL().setXminOn(false);
			GP.getCurrentGL().setXmaxOn(false);
		}

		if (cdV.size() > 0) {
			GP.setCurveData(cdV);
		} else {
			GP.removeAllCurveData();
		}
	}


	/**
	 *  Sets the font of the Bar Chart
	 *
	 *@param  fnt  The new font
	 */
	public void setFont(Font fnt) {
		border.setTitleFont(fnt);
	}


	/**
	 *  Sets the title of the BarChart object
	 *
	 *@param  title  The new title
	 */
	public void setTitle(String title) {
		border.setTitle(title);
	}


	/**
	 *  Sets the title color of the BarChart object
	 *
	 *@param  color  The new title color
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
		JFrame mainFrame = new JFrame("Test of the BarChart class");
		mainFrame.addWindowListener(
									new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				System.exit(0);
			}
		});

		mainFrame.getContentPane().setLayout(new BorderLayout());

		BarChart brch = new BarChart();

		mainFrame.getContentPane().add(brch.getPanel(), BorderLayout.CENTER);

		int nCol = 15;
		final int nLines = 5;
		Vector<BarColumn> barsV = new Vector<BarColumn>();
		for (int i = 0; i < nCol; i++) {
			BarColumn bc =
			new BarColumn() {
				public int size() {
					return nLines;
				}


				public boolean show(int index) {
					return true;
				}


				public boolean show() {
					return true;
				}


				public double value(int index) {
					return 1.23333 * (index + 1);
				}


				public String marker() {
					return "A00";
				}


				public Color getColor(int index) {
					return null;
				}

			};
			barsV.add(bc);
		}

		brch.setBarColumns(barsV);

		brch.setAxisNameY("Value, [mm]");
		brch.setAxisNameX("BPM");

		mainFrame.pack();
		mainFrame.setSize(new Dimension(300, 430));
		mainFrame.setVisible(true);

		while (true) {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException exc) {}

			brch.clear();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException exc) {}

			brch.setBarColumns(barsV);
			Object obj = barsV.lastElement();
			if (obj != null) {
				barsV.remove(obj);
			}
		}

	}


	//===========================================================
	// Auxiliary classes
	//===========================================================

	/**
	 *  The format for marked values
	 *
	 *@author     shishlo
	 *created    October 10, 2005
	 */
    class MarkerFormat extends NumberFormat {
        /** serialization ID */
        private static final long serialVersionUID = 1L;


        private Vector<BarColumn> barColumns = null;


        /**
         *  Constructor for the MarkerFormat object
         *
         *@param  barColumnsIn  Description of the Parameter
         */
        public MarkerFormat( final Vector<BarColumn> barColumnsIn ) {
            barColumns = barColumnsIn;
        }


		/** Don't call. Just returns null. Satisfied abstract method requirement. */
		public Number parse( final String input, final ParsePosition position ) {
			return null;
		}


		/** Generate the formatted string buffer for the specified double value */
		private StringBuffer formattedStringBuffer( final double value ) {
			StringBuffer strb = new StringBuffer(" ");
			if (barColumns != null && barColumns.size() > 0) {
				int ind = (int) Math.round(value - 1.0);
				//System.out.println("debug ind=" + ind);
				if (ind >= 0 && ind < barColumns.size()) {
					strb.append( barColumns.get( ind ).marker() );
				} else {
					strb.append(emptyStr);
				}
			} else {
				strb.append(emptyStr);
			}

			//System.out.println("debug strb=" + strb);

			strb.append(" ");

			return strb;
		}


		/** Generate the formatted string buffer for the specified long value */
		private StringBuffer formattedStringBuffer( final long value ) {
			return formattedStringBuffer( (double)value );
		}


        /**
         *  The overridden format method of the NumberFormat class.
         *
         *@param  val         The value to format
         *@param  toAppendTo  The string buffer to add to
         *@param  pos         The position where to add
         *@return             The formated string
         */
        public StringBuffer format(double val, StringBuffer toAppendTo, FieldPosition pos) {
			return formattedStringBuffer( val );
        }


        /**
         *  The overridden method of the NumberFormat that delegates formatting to
         *  the specific inner formatter
         *
         *@param  val         The integer value to be formatted
         *@param  toAppendTo  Where the text is to be appended
         *@param  pos         On input: an alignment field, if desired. On output:
         *      the offsets of the alignment field
         *@return             The text that will be displayed
         */
        public StringBuffer format(long val, StringBuffer toAppendTo, FieldPosition pos) {
			return formattedStringBuffer( val );
        }
    }


	/**
	 *  The smart formatter calculates min, max, step values and chooses the right
	 *  format for two initial parameters v_min, v_max.
	 *
	 *@author     shishlo
	 *created    October 10, 2005
	 */
	class SmartFormater {

		private double value_min = 0.;
		private double value_max = 0.;
		private double scale = 0.0;

		private NumberFormat fmt_result = new DecimalFormat("#.###E0");

		private NumberFormat[] simpleFormats = new NumberFormat[5];

		private NumberFormat[] scientificFormats = new NumberFormat[5];

		private NumberFormat universalFormat = new DecimalFormat("#.###E0");


		/**
		 *  Constructor for the SmartFormater object
		 */
		public SmartFormater() {
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
			if (range > 0.) {
				scale = Math.pow(10., Math.floor(1.000001 * Math.log(range) / Math.log(10.0)));
			}
			if (scale == 0.) {
				scale = 1.;
				value_min = -1.0;
				value_max = +1.0;
				return;
			}
			value_min = scale * Math.floor(v_min / scale);
			value_max = scale * Math.ceil(v_max / scale);
			if (value_min * value_max == 0. && (scale == Math.abs(value_max) || scale == Math.abs(value_min))) {
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
			if (nV >= 0) {
				nV += 1;
			} else {
				nV -= 1;
			}

			zz_max = zz_max / Math.abs(arr[0]);
			int nD = (int) (Math.floor(1.0001 * Math.log(zz_max) / Math.log(10.0)));
			if (nD >= 0) {
				nD += 1;
			}

			//This is for zoom, so we want to increase number of significant digits
			nD = nD + 1;

			if (nV >= 4) {
				int n = Math.min(4, Math.abs(nD));
				fmt_result = scientificFormats[n];
				return;
			}

			if (nV > 0 && nV < 4) {
				if (nV >= nD) {
					fmt_result = simpleFormats[0];
					return;
				} else {
					int n = Math.min(4, Math.abs(nV - nD));
					fmt_result = simpleFormats[n];
					return;
				}
			}

			if (nV < 0 && nV > -4) {
				int n = Math.abs(nV) + Math.abs(nD) - 2;
				if (n <= 4) {
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
		NumberFormat getFormat() {
			return fmt_result;
		}
	}
}

