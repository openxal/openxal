/*
 *  ScalarPV.java
 *
 *  Created on May 24, 2005
 */
package xal.app.scalarpvviewer;

import xal.extension.widgets.plot.CurveData;
import xal.extension.widgets.plot.DateGraphFormat;
import xal.extension.widgets.plot.GraphDataOperations;
import xal.extension.scan.MonitoredPV;
import xal.extension.scan.UpdatingEventController;
import xal.tools.statistics.RunningWeightedStatistics;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *  Keeps the reference to the monitoredPV and graph data instances.
 *
 *@author    shishlo
 */
public class ScalarPV {
	/** default count of pulses used in averaging (effective since we are using exponential weighted averaging) */
	final static public int DEFAULT_AVERAGING_PULSE_COUNT = 10;

	//graph data to display current value and reference values
	//these graph data include only two points each
	//(x,0)-(x,val)
	//(x,0)-(x,ref)
	private final CurveData gd_val = new CurveData();
	private final CurveData gd_ref = new CurveData();
	private final CurveData gd_dif = new CurveData();

	private boolean showValue = false;
	private boolean showRef = false;
	private boolean showDif = true;

	//graph data to display chart
	private final CurveData gd_val_chart = new CurveData();
	private final CurveData gd_ref_chart = new CurveData();
	private final CurveData gd_dif_chart = new CurveData();

	private boolean showValueChart = false;
	private boolean showRefChart = false;
	private boolean showDifChart = true;

	private boolean wrap_phase = false;
	
	private UpdatingEventController uc = null;

	private MonitoredPV mpv = null;
	private ActionListener updateListener = null;

	/** maintain a running weighted value to suppress noise */
	private RunningWeightedStatistics _valueStatistics;

	private static int nextIndex = 0;


	/**
	 *  Constructor for the ScalarPV object
	 *
	 *@param  ucIn  Update controller
	 */
	public ScalarPV(UpdatingEventController ucIn) {
		setAveragingPulseCount( DEFAULT_AVERAGING_PULSE_COUNT );

		gd_val.addPoint((nextIndex) - 0.125, 0.);
		gd_ref.addPoint((nextIndex) + 0.125, 0.);

		gd_val.addPoint((nextIndex) - 0.125, 0.);
		gd_ref.addPoint((nextIndex) + 0.125, 0.);

		gd_dif.addPoint((nextIndex), 0.);
		gd_dif.addPoint((nextIndex), 0.);

		uc = ucIn;

		updateListener =
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					update();
				}
			};

		mpv = MonitoredPV.getMonitoredPV("ScalarPV_" + nextIndex);
		mpv.addValueListener(updateListener);
		nextIndex++;

		//set lines width
		gd_val.setLineWidth(4);
		gd_ref.setLineWidth(4);
		gd_dif.setLineWidth(4);

		gd_val_chart.setLineWidth(3);
		gd_ref_chart.setLineWidth(3);
		gd_dif_chart.setLineWidth(3);

		setChartColor(Color.black);

		setValColor(Color.blue);
		setRefColor(Color.red);

		setDifColor(Color.magenta);

	}


	/** set the averaging pulse count */
	public void setAveragingPulseCount( final int pulseCount ) {
		final RunningWeightedStatistics valueStatistics = new RunningWeightedStatistics( 1.0 / pulseCount );

		// if the current statistics already has data, prime the new one with data to simulate the specified history for the mean
		if ( _valueStatistics != null && _valueStatistics.population() > 0 ) {
			final int population = Math.min( pulseCount, _valueStatistics.population() );	// take the minimum since we only need pulseCount items to reach the asymptotic weighting (don't want to loop thousands of times)
			final double mean = _valueStatistics.mean();
			for ( int index = 0 ; index < population ; index++ ) {
				valueStatistics.addSample( mean );
			}
		}

		_valueStatistics = valueStatistics;
	}


	/**
	 *  Returns the wrapped value if the wrapping switch is on
	 *
	 *@return    The wrapped value if the wrapping switch is on
	 */
	private double phase_wrap(double val){
		if(wrap_phase == true){
			return GraphDataOperations.unwrap(val,0.);
		}
		return val;
	}
	
	/**
	 *  Sets the wrapping switch
	 *
	 */
	public void setWrappingSwitch(boolean wrap_phase){
		this.wrap_phase = wrap_phase;
		update();
	}
	
	/**
	 *  Returns the boolean value for showing Value on the graph
	 *
	 *@return    The boolean value for showing Value on the graph
	 */
	public boolean showValue() {
		return showValue;
	}


	/**
	 *  Returns the boolean value for showing Reference Value on the graph
	 *
	 *@return    The boolean value for showing Reference Value on the graph
	 */
	public boolean showRef() {
		return showRef;
	}


	/**
	 *  Returns the boolean value for showing difference signal on the graph
	 *
	 *@return    The boolean value for showing difference signal on the graph
	 */
	public boolean showDif() {
		return showDif;
	}


	/**
	 *  Sets the boolean value for showing Value on the graph
	 *
	 *@param  showValue  The boolean value for showing Value on the graph
	 */
	public void showValue(boolean showValue) {
		this.showValue = showValue;
	}


	/**
	 *  Sets the boolean value for showing Reference Value on the graph
	 *
	 *@param  showRef    Description of the Parameter
	 */
	public void showRef(boolean showRef) {
		this.showRef = showRef;
	}


	/**
	 *  Sets the boolean value for showing difference signal on the graph
	 *
	 *@param  showDif  Description of the Parameter
	 */
	public void showDif(boolean showDif) {
		this.showDif = showDif;
	}


	/**
	 *  Returns the boolean Chart for showing Values Chart on the graph
	 *
	 *@return    The boolean Chart for showing Values Chart on the graph
	 */
	public boolean showValueChart() {
		return showValueChart;
	}


	/**
	 *  Returns the boolean Chart for showing Reference Values Chart on the graph
	 *
	 *@return    The boolean Chart for showing Reference Values Chart on the graph
	 */
	public boolean showRefChart() {
		return showRefChart;
	}


	/**
	 *  Returns the boolean Chart for showing diffference Chart on the graph
	 *
	 *@return    The boolean Chart for showing diffference Chart on the graph
	 */
	public boolean showDifChart() {
		return showDifChart;
	}


	/**
	 *  Sets the boolean Chart for showing Chart on the graph
	 *
	 *@param  showValueChart  The boolean Chart for showing Chart on the graph
	 */
	public void showValueChart(boolean showValueChart) {
		this.showValueChart = showValueChart;
	}


	/**
	 *  Sets the boolean Chart for showing Reference Values Chart on the graph
	 *
	 *@param  showRefChart    Description of the Parameter
	 */
	public void showRefChart(boolean showRefChart) {
		this.showRefChart = showRefChart;
	}


	/**
	 *  Sets the boolean Chart for showing diffference Chart on the graph
	 *
	 *@param  showDifChart  Description of the Parameter
	 */
	public void showDifChart(boolean showDifChart) {
		this.showDifChart = showDifChart;
	}


	/**
	 *  Sets the color to the value graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setValColor(Color color) {
		gd_val.setColor(color);
	}


	/**
	 *  Sets the color to the reference graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setRefColor(Color color) {
		gd_ref.setColor(color);
	}


	/**
	 *  Sets the color to the difference graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setDifColor(Color color) {
		gd_dif.setColor(color);
	}


	/**
	 *  Sets the color to the chart graph data.
	 *
	 *@param  color  The new color.
	 */
	public void setChartColor(Color color) {
		gd_ref_chart.setColor(color);
		gd_val_chart.setColor(color);
		gd_dif_chart.setColor(color);
	}


	/**
	 *  Gets the chartColor attribute of the ScalarPV object
	 *
	 *@return    The chartColor value
	 */
	public Color getChartColor() {
		return gd_ref_chart.getColor();
	}


	/**
	 *  Returns the monitoredPV object
	 *
	 *@return    The monitoredPV object
	 */
	public MonitoredPV getMonitoredPV() {
		return mpv;
	}


	/**
	 *  Returns the graphData attribute of the ScalarPV object
	 *
	 *@return    The graphData value
	 */
	public CurveData getValueGraphData() {
		return gd_val;
	}


	/**
	 *  Returns the graphData attribute of the ScalarPV object
	 *
	 *@return    The graphData value
	 */
	public CurveData getRefGraphData() {
		return gd_ref;
	}


	/**
	 *  Returns the graph data for difference signal
	 *
	 *@return    The graph data
	 */
	public CurveData getDifGraphData() {
		return gd_dif;
	}


	/**
	 *  Returns the chart graph data of the ScalarPV object
	 *
	 *@return    The graph data value
	 */
	public CurveData getValueChartGraphData() {
		return gd_val_chart;
	}


	/**
	 *  Returns the chart graph data of the ScalarPV object
	 *
	 *@return    The graph data value
	 */
	public CurveData getRefChartGraphData() {
		return gd_ref_chart;
	}


	/**
	 *  Returns the difference signal chart graph data of the ScalarPV object
	 *
	 *@return    The difference signal graph data value
	 */
	public CurveData getDifChartGraphData() {
		return gd_dif_chart;
	}


	/**
	 *  Clears the chart
	 */
	public void clearChart() {
		gd_val_chart.clear();
		gd_ref_chart.clear();
		gd_dif_chart.clear();
	}


	/**
	 *  Adds one point to the chart of the ScalarPV object
	 *
	 *@param  time  The time
	 *@param  val   The value
	 */
	public void addChartPoint(double time, double val) {
		gd_val_chart.addPoint(time, phase_wrap(val));
		gd_ref_chart.addPoint(time, phase_wrap(gd_ref.getY(1)));
		gd_dif_chart.addPoint(time, phase_wrap(val - gd_ref.getY(1)));
	}


	/**
	 *  Returns the reference value of the ScalarPV object
	 *
	 *@return    The refValue value
	 */
	public double getRefValue() {
		return phase_wrap(gd_ref.getY(1));
	}

	/**
	 *  Returns the value of the ScalarPV object
	 *
	 *@return    The refValue value
	 */
	public double getValue() {
		return phase_wrap(gd_val.getY(1));
	}

	/**
	 *  Sets the value
	 *
	 *@param  val  The new reference value
	 */
	public void setValue(double val) {
		gd_val.setPoint(1, gd_val.getX(0), phase_wrap(val));
		gd_dif.setPoint(1, gd_dif.getX(0), phase_wrap(gd_val.getY(1) - gd_ref.getY(1)));
		update();
	}


	/**
	 *  Sets the reference value
	 *
	 *@param  ref_val  The new reference value
	 */
	public void setRefValue(double ref_val) {
		gd_ref.setPoint(1, gd_ref.getX(0), phase_wrap(ref_val));
		int nP = gd_ref_chart.getSize();
		for (int i = 0; i < nP; i++) {
			gd_ref_chart.setPoint(i, gd_ref_chart.getX(i), phase_wrap(ref_val));
			gd_dif_chart.setPoint(i, gd_ref_chart.getX(i), phase_wrap(gd_val_chart.getY(i) - ref_val));
		}
		update();
	}


	/**
	 *  Memorizes the current value as the reference one.
	 */
	public void memorizeRef() {
		setRefValue(phase_wrap(gd_val.getY(1)));
	}


	/**
	 *  Sets the index of the ScalarPV object
	 *
	 *@param  ind  The new index value
	 */
	public void setIndex(int ind) {
		double x = ((ind + 1));
		gd_ref.setPoint(0, x + 0.125, 0.);
		gd_val.setPoint(0, x - 0.125, 0.);
		gd_dif.setPoint(0, x, 0.);
		gd_ref.setPoint(1, x + 0.125, phase_wrap(gd_ref.getY(1)));
		gd_val.setPoint(1, x - 0.125, phase_wrap(gd_val.getY(1)));
		gd_dif.setPoint(1, x, phase_wrap(gd_val.getY(1) - gd_ref.getY(1)));
	}


	/**
	 *  Gets the index of the ScalarPV object
	 *
	 *@return    The index value
	 */
	public int getIndex() {
		return (((int) gd_ref.getX(0)) - 1);
	}


	/**
	 *  Sets the current value from PV
	 */
	public void measure() {
		if ( mpv.isGood() ) {
			final double val = phase_wrap(mpv.getValue());
			_valueStatistics.addSample( val );

			gd_val.setPoint( 1, gd_val.getX(0), _valueStatistics.mean() );
			gd_dif.setPoint( 1, gd_dif.getX(0), phase_wrap( gd_val.getY(1) - gd_ref.getY(1) ) );
		}
	}


	/**
	 *  Adds one point to the chart graph
	 */
	public void memorize() {
		double time = DateGraphFormat.getSeconds(new java.util.Date());
		final double val = _valueStatistics.population() > 0 ? phase_wrap( _valueStatistics.mean() ) : 0.0;
		addChartPoint( time, val );
	}


	/**
	* Finds min and max for all graph data.
	*/
	public void findMinMax(){
    if( showValue ) gd_val.findMinMax();
		if( showRef ) gd_ref.findMinMax();
		if( showDif ) gd_dif.findMinMax();
		if( showValueChart ) gd_val_chart.findMinMax();
		if( showRefChart ) gd_ref_chart.findMinMax();
		if( showDifChart ) gd_dif_chart.findMinMax();
	}

	/**
	 *  Calls for controller's update method.
	 */
	private void update() {
		uc.update();
	}


	/**
	 *  Removes the monitored PV.
	 */
	@Override
    protected void finalize() throws Throwable {
		try {
			MonitoredPV.removeMonitoredPV(mpv);
		}
		finally {
			super.finalize();
		}
	}

}

