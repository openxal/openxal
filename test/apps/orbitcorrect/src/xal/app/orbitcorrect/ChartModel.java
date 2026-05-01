/*
 *  ChartModel.java
 *
 *  Created on Wed Jan 07 13:46:12 EST 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import javax.swing.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.text.*;
import java.util.*;
import java.util.Timer;

import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.tools.apputils.*;
import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.qualify.*;
import xal.extension.widgets.smf.XALSynopticPanel;


/**
 * ChartModel manages the display of the orbit traces.
 * @author     tap
 * @since    Jan 7, 2004
 */
public class ChartModel implements OrbitModelListener, OrbitSourceListener {
	/** earliest time */
	final static private Date EARLIEST_DATE;
	
	/** chart date format */
	final static protected DateFormat CHART_DATE_FORMAT;
	
	/** orbit model */
	protected OrbitModel _model;
	
	/** trace models */
	protected List<TraceSource> _traceSources;
	
	/** map of colors keyed by trace source */
	protected Map<TraceSource, Color> _traceColors;
	
	/** chart of traces */
	protected FunctionGraphsJPanel _chart;
	
	/** view of nodes */
	protected XALSynopticPanel _nodeView;
	
	/** timer for updating the chart */
	protected Timer _chartTimer;
	
	/** message center for forwarding messages */
	protected MessageCenter _messageCenter;
	
	/** proxy for posting broadcasting chart model events */
	protected ChartModelListener _proxy;
	
	/** trace source types to display */
	protected Collection<String> _traceSourceTypesToDisplay;
	
	/** trace source orbit labels */
	//unused
    //protected Collection _disabledTraceSourceOrbitLabels;
	
	
	/* static initializer */
	static {
		CHART_DATE_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
		EARLIEST_DATE = new Date( 0 );
	}
	
	
	/**
	 * Constructor
	 * @param  model  the orbit model
	 */
	public ChartModel( final FunctionGraphsJPanel chart, final OrbitModel model ) {		
		_messageCenter = new MessageCenter( "Chart Model" );
		_proxy = _messageCenter.registerSource( this, ChartModelListener.class );
		
		_chart = chart;
		setupChart( chart );
		
		SimpleChartPopupMenu.addPopupMenuTo( _chart );
		_traceSources = new ArrayList<TraceSource>();
		_traceColors = new HashMap<TraceSource, Color>();
		
		_traceSourceTypesToDisplay = new HashSet<String>();
		_traceSourceTypesToDisplay.add( OrbitTraceAdaptor.X_AVG_TYPE );
		_traceSourceTypesToDisplay.add( OrbitTraceAdaptor.Y_AVG_TYPE );
		_traceSourceTypesToDisplay.add( OrbitTraceAdaptor.AMP_AVG_TYPE );
		
		setModel( model );
		
		createIconicNodeView();
		
		updateChartWithPeriod( 1.0 );
		synchronizeNodeView();
	}
	
	
	/**
	 * Set the chart model to have a new orbit model.
	 * @param  model  the orbit model
	 */
	public void setModel( final OrbitModel model ) {
		if ( _model != null ) {
			_model.removeOrbitModelListener( this );
			final Iterator<OrbitSource> orbitSourceIter = model.getOrbitSources().iterator();
			while ( orbitSourceIter.hasNext() ) {
				final OrbitSource orbitSource = orbitSourceIter.next();
				orbitSource.removeOrbitSourceListener( this );
			}
		}
		
		_model = model;
		if ( model != null ) {
			_model.addOrbitModelListener( this );
			final Iterator<OrbitSource> orbitSourceIter = model.getOrbitSources().iterator();
			while ( orbitSourceIter.hasNext() ) {
				final OrbitSource orbitSource = orbitSourceIter.next();
				orbitSource.addOrbitSourceListener( this );
			}
		}
		
		updateTraceSources();
		updateTitle( model );
	}
	
	
	/**
	 * Update the chart's title to reflect the model's selected sequence.
	 * @param  model  the orbit model whose sequence is used to build the title
	 */
	protected void updateTitle( final OrbitModel model ) {
		String title = "Orbit Traces";
		String xLabel = "Position from ";
		if ( model != null ) {
			AcceleratorSeq sequence = model.getSequence();
			title += ( sequence != null ) ? " for " + sequence.getId() : "";
			xLabel += ( sequence != null ) ? sequence.getId() : "sequence"; 
		}
		_chart.setName( title );
		_chart.setAxisNameX( xLabel + " start (m)" );
	}
	
	
	/** Generate the trace sources from the orbit model's orbit sources.	 */
	protected void updateTraceSources() {
		synchronized ( _traceSources ) {
			_traceSources.clear();
			
			if ( _model != null ) {
				int colorIndex = 0;		// index of the trace color
				final Iterator<OrbitSource> sourceIter = _model.getOrbitSources().iterator();
				while ( sourceIter.hasNext() ) {
					final OrbitSource orbitSource = sourceIter.next();
					if ( orbitSource.isEnabled() ) {
						final OrbitTraceAdaptor adaptor = new OrbitTraceAdaptor( orbitSource, true );
						final TraceSource[] traceSources = adaptor.getTraceSources();
						
						final Color traceColor = IncrementalColors.getColor( colorIndex );
						colorIndex += 2;
						for ( int traceIndex = 0 ; traceIndex < traceSources.length ; traceIndex++ ) {
							addTraceSource( traceSources[traceIndex], traceColor );						
						}
					}
				}
			}
			
			_proxy.traceSourcesChanged( this, new ArrayList<TraceSource>( _traceSources ) );
		}
	}
	
	
	/**
	 * Add the specified listener as a receiver of chart model events from this model.
	 * @param  listener  the listener to add for receiving chart model events.
	 */
	public void addChartModelListener( ChartModelListener listener ) {
		_messageCenter.registerTarget( listener, this, ChartModelListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving chart model events from this model.
	 * @param  listener  the listener to remove from receiving chart model events.
	 */
	public void removeChartModelListener( ChartModelListener listener ) {
		_messageCenter.removeTarget( listener, this, ChartModelListener.class );
	}
	
	
	/**
	 * Get this model's chart.
	 * @return    this model's chart
	 */
	public FunctionGraphsJPanel getChart() {
		return _chart;
	}
	
	
	/**
	 * Get the node view
	 * @return the node view
	 */
	public JComponent getNodeView() {
		return _nodeView;
	}
	
	
	/**
	 * Add a trace source to display on the chart.
	 * @param  traceSource  a trace source to display on the chart.
	 * @param  traceColor   the color to apply to the trace when displayed.
	 */
	public void addTraceSource( final TraceSource traceSource, final Color traceColor ) {
		synchronized ( _traceSources ) {
			_traceSources.add( traceSource );
			setTraceColor( traceSource, traceColor );
			_proxy.traceSourcesChanged( this, new ArrayList<TraceSource>( _traceSources ) );
			updateTraceSourceDisplay();
		}
	}
	
	
	/**
	 * Remove the trace source from being displayed on the chart.
	 * @param  traceSource  the trace source to remove from being displayed on the chart.
	 */
	public void removeTraceSource( final TraceSource traceSource ) {
		synchronized ( _traceSources ) {
			_traceSources.remove( traceSource );
			_traceColors.remove( traceSource );
			_proxy.traceSourcesChanged( this, new ArrayList<TraceSource>( _traceSources ) );
		}
	}
	
	
	/**
	 * Set the trace color for the specified trace source.
	 * @param  traceSource  the trace source
	 * @param  traceColor   the color to associate with the trace source
	 */
	public void setTraceColor( final TraceSource traceSource, final Color traceColor ) {
		_traceColors.put( traceSource, traceColor );
	}
	
	
	/**
	 * Get the list of trace sources to display on the chart.
	 * @return    the list of trace sources to display on the chart.
	 */
	public List<TraceSource> getTraceSources() {
		synchronized ( _traceSources ) {
			return new ArrayList<TraceSource>( _traceSources );
		}
	}
	
	
	/**
	 * Get the trace sources filtered using the specified qualifier.
	 * @param qualifier the qualifier used to filter the trace sources
	 * @return the list of filtered trace sources
	 */
	public List<TraceSource> getTraceSources( final Qualifier qualifier ) {
		final ArrayList<TraceSource> filteredSources = new ArrayList<TraceSource>();
		
		final Iterator<TraceSource> sourceIter = getTraceSources().iterator();
		while ( sourceIter.hasNext() ) {
			final TraceSource traceSource = sourceIter.next();
			if ( qualifier.matches( traceSource ) ) {
				filteredSources.add( traceSource );
			}
		}
		
		return filteredSources;
	}
	
	
	/**
	 * Get the trace source for the specified index.
	 * @param  index  the index of the trace source to get.
	 * @return the trace source at the specified index.
	 */
	public TraceSource getTraceSource( int index ) {
		synchronized ( _traceSources ) {
			return _traceSources.get( index );
		}
	}
	
	
	/**
	 * Get the color of the trace source.
	 * @param  traceSource  the trace source for which to get the color.
	 * @return              The traceColor value
	 */
	public Color getTraceColor( final TraceSource traceSource ) {
		synchronized ( _traceSources ) {
			return _traceColors.get( traceSource );
		}
	}
	
	
	/**
	 * Get the number of traces to display on the chart.
	 * @return    the number of traces to display on the chart.
	 */
	public int getNumTraces() {
		synchronized ( _traceSources ) {
			return _traceSources.size();
		}
	}
	
	
	/**
	 * Determine if the specified source type is visible.
	 * @param type the source type to test for visiblity
	 * @return true if the source type is visible and false if not
	 */
	public boolean isSourceTypeVisible( final String type ) {
		return _traceSourceTypesToDisplay.contains( type );
	}
	
	
	/**
	 * Enable or disable the display of the specified trace source type.
	 * @param type the trace source type to display
	 * @param visible indicates whether or not to display this type
	 */
	public void displayTraceSourceWithType( final String type, final boolean visible ) {
		if ( visible ) {
			_traceSourceTypesToDisplay.add( type );
		}
		else {
			_traceSourceTypesToDisplay.remove( type );
		}
		
		updateTraceSourceDisplay();
	}
	
	
	/**
	 * Display only the qualified trace sources.
	 * @param qualifier the qualifier to filter the trace sources to display
	 */
	protected void displayOnlyQualifiedTraceSources( final Qualifier qualifier ) {
		final Iterator<TraceSource> sourceIter = getTraceSources().iterator();
		while ( sourceIter.hasNext() ) {
			final TraceSource traceSource = sourceIter.next();
			traceSource.setEnabled( qualifier.matches( traceSource ) );
		}
		
		updateTraces();
	}
	
	
	/** Update the trace source display based on the filters that have been applied */
	protected void updateTraceSourceDisplay() {
		final OrQualifier typeQualifier = new OrQualifier();
		
		final Iterator<String> typeIter = _traceSourceTypesToDisplay.iterator();
		while ( typeIter.hasNext() ) {
			final String type = typeIter.next();
			typeQualifier.append( new KeyValueQualifier( TraceSource.TYPE_KEY, type ) );
		}
		
		displayOnlyQualifiedTraceSources( typeQualifier );
	}
	
	
	/** Update the traces to display. */
	public void updateTraces() {
		if ( _chart.getAllGraphData().size() > 0 ) {
			_chart.removeAllGraphData();
		}
		final Vector<BasicGraphData> series = generateGraphData();
		if ( series.size() > 0 ) {
			_chart.addGraphData( series );
		}
	}
	
	
	/**
	 * Get the array of series as a Vector of GraphData instances.
	 * @return    The series data to display in the FunctionGraphsJPanel chart.
	 */
	protected Vector<BasicGraphData> generateGraphData() {
		final List<Trace> traces = new ArrayList<Trace>();
		final List<Color> colors = new ArrayList<Color>();
		
		Date chartTimeStamp = EARLIEST_DATE;
		
		synchronized ( _traceSources ) {
			final Iterator<TraceSource> traceIter = _traceSources.iterator();
			while ( traceIter.hasNext() ) {
				final TraceSource traceSource = traceIter.next();
				if ( traceSource.isEnabled() ) {
					final Trace trace = traceSource.getTrace();
					if ( trace.getValues().length > 0 ) {
						final Date traceTimeStamp = trace.getTimeStamp();
						if ( traceTimeStamp.after( chartTimeStamp ) )  chartTimeStamp = traceTimeStamp;
						traces.add( trace );
						colors.add( getTraceColor( traceSource ) );
					}
				}
			}
		}
		
		if ( traces.size() > 0 ) {
			_chart.setName( CHART_DATE_FORMAT.format( chartTimeStamp ) );
		}
		
		final int numTraces = traces.size();
		final Vector<BasicGraphData> series = new Vector<BasicGraphData>( numTraces );
		
		for ( int traceIndex = 0; traceIndex < numTraces; traceIndex++ ) {
			final Trace trace = traces.get( traceIndex );
			final Color color = colors.get( traceIndex );
			final double[] positions = trace.getPositions();
			final double[] values = trace.getValues();
			
			final BasicGraphData graphData = new BasicGraphData();
			for ( int index = 0 ; index < positions.length ; index++ ) {
				final double value = values[index];
				if ( !Double.isNaN( value ) ) {
					final double position = positions[index];
					graphData.addPoint( position, value );
				}
			}
			graphData.setGraphColor( color );
			graphData.setGraphProperty( _chart.getLegendKeyString(), trace.getLabel() );
			graphData.setLineStroke( 2.0f, trace.getDashPattern() );
			graphData.setGraphPointShape( trace.getPointMark() );
			
			series.add( graphData );
		}
		
		return series;
	}
	
	
	/**
	 * Update the chart automatically with the specified update period.
	 * @param  period  the period in seconds for updating the chart with new data.
	 */
	protected void updateChartWithPeriod( final double period ) {
		final long msecPeriod = Math.round( 1000 * period );
		_chartTimer = new Timer();
		_chartTimer.schedule( newChartUpdateTask( msecPeriod ), msecPeriod, msecPeriod );
	}
	
	
	/** make a timer task to update the chart */
	private TimerTask newChartUpdateTask( final long period ) {
		return new TimerTask() {
			public void run() {
				try {
					SwingUtilities.invokeAndWait( newChartUpdater() );
					Thread.sleep( period );	// make sure we rest for at least the specified period
				}
				catch ( Exception exception ) {
					System.err.println( "Exception updating the chart..." );
					exception.printStackTrace();
				}
			}
		};
	}
	
	
	/** make a runnable to update the chart */
	private Runnable newChartUpdater() {
		return new Runnable() {
			public void run() {
				updateChart();
			}
		};
	}
	
	
	/** Update the chart with new data. */
	protected void updateChart() {
		updateTraces();
	}
	
	
	/**
	 * Configure the chart for displaying traces.
	 * @param chart the chart to configure for displaying traces.
	 */
	protected void setupChart( final FunctionGraphsJPanel chart ) {
		// labels
		chart.setName( "" );
		chart.setAxisNameX("Position from sequence start (m)");
		chart.setAxisNameY("Beam displacement (mm)");
		
		chart.setNumberFormatX( new DecimalFormat( "0.00E0" ) );
		chart.setNumberFormatY( new DecimalFormat( "0.00E0" ) );
		
		// add the horizontal and vertical guides
		chart.addVerticalLine( chart.getCurrentMinX(), Color.black );
		chart.addVerticalLine( chart.getCurrentMaxX(), Color.black );
		chart.addHorizontalLine( chart.getCurrentMinY(), Color.black );
		chart.addHorizontalLine( chart.getCurrentMaxY(), Color.black );
		
		// Hide buttons on the plot since they are handled by a dialog box
		chart.setChooseModeButtonVisible( false );
		chart.setHorLinesButtonVisible( false );
		chart.setVerLinesButtonVisible( false );
		
		// Show the horizontal and vertical guides
		chart.setDraggingHorLinesGraphMode( true );
		chart.setDraggingVerLinesGraphMode( true );
		
		// add legend support
		chart.setLegendPosition( FunctionGraphsJPanel.LEGEND_POSITION_ARBITRARY );
		chart.setLegendKeyString( "Legend" );
		chart.setLegendBackground( Color.lightGray );
		chart.setLegendColor( Color.black );
		chart.setLegendVisible( true );
	}
	
	
	/** Create iconic node view */
	protected void createIconicNodeView() {
		_nodeView = new XALSynopticPanel();
		
		_chart.addHorLimitsListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				synchronizeNodeView();
			}
		});
		
		final AcceleratorSeq sequence = _model.getSequence();
		if ( sequence != null ) {
			_nodeView.setAcceleratorSequence( sequence );
		}
	}
	
	
	/** Synchronize the node view with the chart's horizontal axis. */
	protected void synchronizeNodeView() {
        final double start = _chart.getCurrentMinX();
        final double end = _chart.getCurrentMaxX();
        
        final int left = _chart.getScreenX( start );
        final int right = _nodeView.getWidth() - _chart.getScreenX( end );
        
        if ( _nodeView.getMargin().left != left || _nodeView.getMargin().right != right || start != _nodeView.getStartPosition() || end != _nodeView.getEndPosition() ) {
            _nodeView.setMargin( new Insets(5, left, 5, right) );
			if ( start < _nodeView.getEndPosition() ) {
				_nodeView.setStartPosition( start );
				_nodeView.setEndPosition( end );
			}
			else {
				_nodeView.setEndPosition( end );
				_nodeView.setStartPosition( start );
			}
            _nodeView.repaint();
        }
	}
    
    
    /** 
     * Returns the visibility of the grid.
     * @return True if the grid is visible and False if the grid is not visible.
     */
    public boolean isGridVisible() {
        return _chart.getGridLinesVisibleX() || _chart.getGridLinesVisibleY();
    }
    
    
    /** 
     * Sets the visibility of the grid.
     * @param isVisible True if the grid should be made visible and False if it should be hidden.
     */
    public void setGridVisible(boolean isVisible) {
		if ( isVisible == isGridVisible() )  return;	// nothing to do
		
        _chart.setGridLinesVisibleX(isVisible);
        _chart.setGridLinesVisibleY(isVisible);
    }
    
    
    /** Toggle the visibility of the chart grid. */
    public void toggleGridVisible() {
        setGridVisible( !isGridVisible() );
    }
    
    
    /** 
     * Returns the visibility of the legend.
     * @return True if the legend is visible and False if the legend is not visible.
     */
    public boolean isLegendVisible() {
		return _chart.isLegendVisible();
    }
    
    
    /** 
     * Sets the visibility of the legend.
     * @param isVisible True if the legend should be made visible and False if it should be hidden.
     */
    public void setLegendVisible( final boolean isVisible ) {
		_chart.setLegendVisible(isVisible);
    }
    
    
    /** Toggle the visibility of the chart legend. */
    public void toggleLegendVisible() {
        _chart.setLegendVisible( !_chart.isLegendVisible() );
    }
	
	
	/**
	 * Notification that the sequence has changed.
	 * @param  model        the model sending the notification
	 * @param  newSequence  the new accelerator sequence
	 */
	public void sequenceChanged( final OrbitModel model, final AcceleratorSeq newSequence ) {
		updateTitle( model );
		_nodeView.setAcceleratorSequence( newSequence );
		synchronizeNodeView();
	}
	
	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( OrbitModel model, List<BpmAgent> bpmAgents ) {}
	
	
	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( final OrbitModel model, final OrbitSource orbitSource ) {
		orbitSource.addOrbitSourceListener( this );
		updateTraceSources();
	}
	
	
	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( final OrbitModel model, final OrbitSource orbitSource ) {
		orbitSource.removeOrbitSourceListener( this );
		updateTraceSources();
	}
	
	
	/**
	 * Handle the event indicating that the specified orbit source has generated a new orbit.
	 * @param source    the orbit source generating the new orbit
	 * @param newOrbit  the new orbit
	 */
	public void orbitChanged( final OrbitSource source, final Orbit newOrbit ) {}
	
	
	/**
	 * Handle the event indicating that the orbit source's sequence has changed.
	 * @param source       the orbit source generating the new orbit
	 * @param newSequence  the new sequence
	 * @param newBPMs      the new BPMs
	 */
	public void sequenceChanged( final OrbitSource source, final AcceleratorSeq newSequence, final List<BpmAgent> newBPMs ) {}
	
	
	/**
	 * Handle the event indicating that the orbit source enable state has changed.
	 * @param source the orbit source generating the event
	 * @param isEnabled the new enable state of the orbit source
	 */
	public void enableChanged( final OrbitSource source, final boolean isEnabled ) {
		updateTraceSources();
	}
}

