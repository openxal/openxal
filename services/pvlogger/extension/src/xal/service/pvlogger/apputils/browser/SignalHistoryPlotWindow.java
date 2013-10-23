/*
 * SignalHistoryPlotWindow.java
 *
 * Created on Fri May 21 09:05:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.pvlogger.apputils.browser;

import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;
import xal.service.pvlogger.*;

import javax.swing.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.util.*;


/**
 * SignalHistoryPlotWindow displays a plot history of the selected signals.
 *
 * @author  tap
 */
public class SignalHistoryPlotWindow extends JFrame implements SwingConstants {
	/** specify the serializable version as requied */
	static private final long serialVersionUID = 1;

	protected FunctionGraphsJPanel _chart;
	protected BrowserController _controller;
	protected BrowserModel _model;
	
	
	/**
	 * Primary constructor.
	 * @param controller the browser controller
	 */
	public SignalHistoryPlotWindow(final BrowserController controller) {
		super("Signal History Plot");
		_controller = controller;
		_model = controller.getModel();
		
		makeContent();
	}
	
	
	/**
	 * Show this window near the specified neighbor
	 * @param neighbor the view near which we wish to display this window
	 */
	public void showNear(Component neighbor) {
		updateChart();
		setLocationRelativeTo(neighbor);
		setVisible(true);
	}
	
	
	/**
	 * Build the component contents of the window.
	 */
	protected void makeContent() {
        setSize(900, 500);
		Box mainView = new Box(BoxLayout.X_AXIS);
		getContentPane().add(mainView);
		
		mainView.add( buildChartView() );
	}
	
	
	/**
	 * Build the chart view.
	 * @return the chart view
	 */
	protected Container buildChartView() {
		_chart = new FunctionGraphsJPanel();
		_chart.setSmartGL(false);
		_chart.addMouseListener(new SimpleChartPopupMenu(_chart));
		
		//_chart.setBackground(Color.black);
		
		_chart.setNumberFormatX( new DateGraphFormat("MMM dd, yyyy HH:mm") );

		// add legend support
		_chart.setLegendPosition( FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY );
		_chart.setLegendKeyString( "Legend" );
		_chart.setLegendBackground( Color.lightGray );
		_chart.setLegendColor( Color.black );
		_chart.setLegendVisible( true );
		
		return _chart;
	}
	
	
	/**
	 * Update the chart with the latest data.
	 */
	public void updateChart() {
		final List<String> signals = new ArrayList<String>( _controller.getSelectedSignals() );
		final int numSignals = signals.size();
		final MachineSnapshot[] machineSnapshots = _model.getSnapshots();
				
		final Map<String,List<ChannelSnapshot>> signalMap = new HashMap<String,List<ChannelSnapshot>>( numSignals );
		for ( int signalIndex = 0 ; signalIndex < numSignals ; signalIndex++ ) {
			final String signal = signals.get( signalIndex );
			signalMap.put( signal, new ArrayList<ChannelSnapshot>() );
		}
		
		try {
			_model.populateSnapshots();
		}
		catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
		
		for ( int machineSnapshotIndex = 0 ; machineSnapshotIndex < machineSnapshots.length ; machineSnapshotIndex++ ) {
			final MachineSnapshot machineSnapshot = machineSnapshots[machineSnapshotIndex];
			final ChannelSnapshot[] channelSnapshots = machineSnapshot.getChannelSnapshots();
			for ( int index = 0 ; index < channelSnapshots.length ; index++ ) {
				final ChannelSnapshot channelSnapshot = channelSnapshots[index];
				final String signal = channelSnapshot.getPV();
				final List<ChannelSnapshot> dataList = signalMap.get( signal );
				if ( dataList != null ) {
					dataList.add( channelSnapshot );
				}
			}
		}
		
		final Vector<BasicGraphData> seriesData = new Vector<BasicGraphData>();
		for ( int signalIndex = 0 ; signalIndex < numSignals ; signalIndex++ ) {
			final String signal = signals.get(signalIndex);
			final List<ChannelSnapshot> snapshots = signalMap.get(signal);
			final int numPoints = snapshots.size();
			double[] values = new double[numPoints];
			double[] timestamps = new double[numPoints];
			for ( int pointIndex = 0 ; pointIndex < numPoints ; pointIndex++ ) {
				final ChannelSnapshot snapshot = snapshots.get(pointIndex);
				values[pointIndex] = snapshot.getValue()[0];
				timestamps[pointIndex] = snapshot.getTimestamp().getSeconds();
			}
			if ( numPoints > 0 ) {
				Color color = IncrementalColors.getColor(signalIndex);
				BasicGraphData graphData = new BasicGraphData();
				graphData.addPoint(timestamps, values);
				graphData.setGraphColor( color );
				graphData.setGraphProperty( _chart.getLegendKeyString(), signal );
				graphData.setGraphName(signal);
				seriesData.add( graphData );
			}
		}
		_chart.removeAllGraphData();
		_chart.addGraphData( seriesData );
		
		// set the plot X range to only the selected snapshot range (exclude PV values with timestamps outside this range).
		double stepSize = (machineSnapshots[machineSnapshots.length-1].getTimestamp().getTime()/1000. -
				machineSnapshots[0].getTimestamp().getTime()/1000.)/3.;
		_chart.setLimitsAndTicksX(machineSnapshots[0].getTimestamp().getTime()/1000., 
				machineSnapshots[machineSnapshots.length-1].getTimestamp().getTime()/1000.,
				stepSize);
	}
	
	public FunctionGraphsJPanel getChart() {
		return _chart;
	}
}

