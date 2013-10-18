/*
 * HistogramDocument.java
 *
 * Created on Feb 11, 2009, 1:32 PM
 */

package xal.app.pvhistogram;

import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.text.DecimalFormat;

import xal.extension.application.*;
import xal.extension.smf.application.*;
import xal.smf.*;
import xal.tools.apputils.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.data.XMLDataManager;
import xal.tools.data.*;
import xal.extension.bricks.WindowReference;
import xal.smf.widgets.NodeChannelSelector;
import xal.tools.plot.*;
import xal.tools.statistics.UnivariateStatistics;
import xal.ca.Channel;


/**
 * HistogramDocument represents the document for the PV Histogram application.
 * @author  t6p
 */
public class HistogramDocument extends AcceleratorDocument implements DataListener, ChannelHistogramListener {
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "HistogramDocument";
	
	/** format for displaying statistics */
	final static DecimalFormat STATS_FORMAT = new DecimalFormat( "0.00E0" );
	
	/** histogram model */
	final ChannelHistogram MODEL; 
	
	/** main window reference */
	final WindowReference WINDOW_REFERENCE;
	
	/** histogram curve data */
	final CurveData HISTOGRAM_DATA;

	
	
    /** Empty Constructor */
    public HistogramDocument() {
        this( null );
    }
    
    
    /** 
     * Primary constructor 
     * @param url The URL of the file to load into the new document.
     */
    public HistogramDocument( final java.net.URL url ) {
        setSource( url );
		
		WINDOW_REFERENCE = getDefaultWindowReference( "MainWindow", this );
		
		HISTOGRAM_DATA = new CurveData();
		HISTOGRAM_DATA.setColor( java.awt.Color.RED );
		HISTOGRAM_DATA.setLineWidth( 5 );

		MODEL = new ChannelHistogram();
		MODEL.addChannelHistogramListener( this );
		
		if ( url != null ) {
            System.out.println( "Opening document: " + url.toString() );
            final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
            update( documentAdaptor.childAdaptor( dataLabel() ) );
        }		
		
		configureWindow( WINDOW_REFERENCE );		
		
		setHasChanges( false );
    }
	    
    
    /** Make and configure the main window. */
    public void makeMainWindow() {
        mainWindow = (XalWindow)WINDOW_REFERENCE.getWindow();
		setHasChanges( false );
   }
   
   
   /** configure the main window */
   private void configureWindow( final WindowReference windowReference ) {
	   final JSpinner bufferSizeSpinner = (JSpinner)windowReference.getView( "BufferSizeSpinner" );
	   bufferSizeSpinner.setModel( new SpinnerNumberModel( MODEL.getBufferSize(), 2, 10000, 1 ) );
	   bufferSizeSpinner.addChangeListener( newBufferSizeHandler() );
	   
	   final JButton clearButton = (JButton)windowReference.getView( "ClearButton" );
	   clearButton.addActionListener( newClearBufferHandler() );
	   
	   final JToggleButton pauseButton = (JToggleButton)windowReference.getView( "PauseButton" );
	   pauseButton.addActionListener( newPauseHandler() );
	   
	   final JToggleButton startButton = (JToggleButton)windowReference.getView( "StartButton" );
	   startButton.addActionListener( newStartHandler() );
	   
	   final ButtonGroup runButtonGroup = new ButtonGroup();
	   runButtonGroup.add( pauseButton );
	   runButtonGroup.add( startButton );

	   final JSpinner binCountSpinner = (JSpinner)windowReference.getView( "BinCountSpinner" );
	   binCountSpinner.setModel( new SpinnerNumberModel( MODEL.getBinCount(), 2, 1000, 1 ) );
	   binCountSpinner.addChangeListener( newBinCountHandler() );
	   
	   final JTextField lowerLimitField = (JTextField)windowReference.getView( "LowerLimitField" );
	   final JTextField upperLimitField = (JTextField)windowReference.getView( "UpperLimitField" );
	   final ActionListener manualRangeHandler = newManualRangeHandler( lowerLimitField, upperLimitField );
	   lowerLimitField.addActionListener( manualRangeHandler );
	   upperLimitField.addActionListener( manualRangeHandler );
	   final double[] manualRange = MODEL.getManualValueRange();
	   lowerLimitField.setText( String.valueOf( manualRange[0] ) );
	   upperLimitField.setText( String.valueOf( manualRange[1] ) );
	   
	   final JCheckBox manualRangeSelection = (JCheckBox)windowReference.getView( "ManualRangeSelection" );
	   manualRangeSelection.addActionListener( newRangeCheckBoxHandler( lowerLimitField, upperLimitField) );
	   manualRangeSelection.setSelected( !MODEL.getAutoLimits() );
	   	   
	   final JTextField channelField = (JTextField)windowReference.getView( "ChannelField" );
	   channelField.addActionListener( newChannelFieldHandler( channelField ) );
	   
	   final FunctionGraphsJPanel histogramPlot = (FunctionGraphsJPanel)windowReference.getView( "HistogramPlot" );
	   histogramPlot.setAxisNames( "Channel Value", "Counts" );
	   SimpleChartPopupMenu.addPopupMenuTo( histogramPlot );
	   histogramPlot.addCurveData( HISTOGRAM_DATA ); 

	   final JButton channelSelectionButton = (JButton)windowReference.getView( "ChannelSelectionButton" );
	   channelSelectionButton.addActionListener( newChannelAddHandler( channelField ) );	   
   }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        writeDataTo( this, url );
    }
    
    
    /** Handle the accelerator changed event by displaying the elements of the accelerator in the main window. */
    public void acceleratorChanged() {
		MODEL.setAccelerator( getAccelerator() );
		setHasChanges( true );
	}
    
    
    /** Handle the selected sequence changed event by displaying the elements of the selected sequence in the main window. */
    public void selectedSequenceChanged() {
		setHasChanges( true );
	}
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			final Accelerator accelerator = applySelectedAcceleratorWithDefaultPath( acceleratorPath );
			
			if ( accelerator != null && adaptor.hasAttribute( "sequence" ) ) {
				final String sequenceID = adaptor.stringValue( "sequence" );
				setSelectedSequence( getAccelerator().findSequence( sequenceID ) );
			}
		}
		
		final DataAdaptor modelAdaptor = adaptor.childAdaptor( ChannelHistogram.DATA_LABEL );
		if ( modelAdaptor != null )  MODEL.update( modelAdaptor );
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		adaptor.writeNode( MODEL );
		
		if ( getAccelerator() != null ) {
			adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
			
			final AcceleratorSeq sequence = getSelectedSequence();
			if ( sequence != null ) {
				adaptor.setValue( "sequence", sequence.getId() );
			}
		}
    }
	
	
	/** set the run indicator to display whether the monitor is running */
	private void setRunIndicator( final boolean isRunning ) {
		final JToggleButton startButton = (JToggleButton)WINDOW_REFERENCE.getView( "StartButton" );
		startButton.setSelected( isRunning );
	}
	
	
	/** handler for actions in which a user specifies the histogram value range */
	private ActionListener newManualRangeHandler( final JTextField lowerLimitField, final JTextField upperLimitField ) {
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final double lowerLimit = Double.parseDouble( lowerLimitField.getText() );
				final double upperLimit = Double.parseDouble( upperLimitField.getText() );
				final double[] range = new double[] { lowerLimit, upperLimit };
				MODEL.setManualValueRange( range );
				setHasChanges( true );
			}
		};		
	}
	
	
	/** handler for actions in which a user checks/unchecks the manual range selection checkbox */
	private ActionListener newRangeCheckBoxHandler( final JTextField lowerLimitField, final JTextField upperLimitField ) {
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final boolean isManual = ((JCheckBox)event.getSource()).isSelected();
				lowerLimitField.setEditable( isManual );
				upperLimitField.setEditable( isManual );
				MODEL.setAutoLimits( !isManual );
				setHasChanges( true );
			}
		};		
	}
	
	
	/** handle the text field action to set a channel by PV name */
	private ActionListener newChannelFieldHandler( final JTextField channelField ) { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.setChannelSource( channelField.getText().trim() );
			}
		};		
	}
	
	
	/** handle the button action to select channels to add to the correlator */
	private ActionListener newChannelAddHandler( final JTextField channelField ) { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final AcceleratorSeq selectedSequence = getSelectedSequence();
				final AcceleratorSeq sequence = selectedSequence != null ? selectedSequence : getAccelerator();
				if ( sequence != null ) {
					final List<AcceleratorNode> nodes = sequence.getAllInclusiveNodes( true );
					final NodeChannelSelector channelRefSelector = NodeChannelSelector.getInstanceFromNodes( nodes, getAcceleratorWindow(), "Select Channel" );
					final List<NodeChannelRef> channelRefs = channelRefSelector.showDialog( ListSelectionModel.SINGLE_SELECTION );
					if ( channelRefs != null && channelRefs.size() > 0 ) {
						final NodeChannelRef channelRef = channelRefs.get( 0 );	// get the selected channel reference
						MODEL.setChannelSource( channelRef );
					}
				}
				else {
					// should display a warning dialog
					getAcceleratorWindow().displayError( "PV Selection Error", "You must first select an accelerator sequence from which the PVs will be supplied." );
				}
			}
		};		
	}
	
	
	/** handler for the bin count change events */
	private ChangeListener newBinCountHandler() {
		return new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final JSpinner spinner = (JSpinner)event.getSource();
				final Object value = spinner.getValue();
				if ( value != null && value instanceof Number ) {
					final int binCount = ((Number)value).intValue();
					if ( binCount > 1 ) {
						MODEL.setBinCount( binCount );
						setHasChanges( true );
					}
				}
			}
		};
	}
	
	
	/** handler for the buffer size change events */
	private ChangeListener newBufferSizeHandler() {
		return new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final JSpinner spinner = (JSpinner)event.getSource();
				final Object value = spinner.getValue();
				if ( value != null && value instanceof Number ) {
					final int bufferSize = ((Number)value).intValue();
					if ( bufferSize > 1 ) {
						MODEL.setBufferSize( bufferSize );
						setHasChanges( true );
					}
				}
			}
		};
	}
	
	
	/** handle the button action to clear the buffer */
	private ActionListener newClearBufferHandler() { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.clear();
			}
		};		
	}
	
	
	/** handle the button action to start the monitor */
	private ActionListener newStartHandler() { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.startMonitor();
				setRunIndicator( true );
			}
		};		
	}
	
	
	/** handle the button action to stop the monitor */
	private ActionListener newPauseHandler() { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.stopMonitor();
				setRunIndicator( false );
			}
		};		
	}
	
	
	/** event indicating that the model's channel source has changed */
	public void channelSourceChanged( final ChannelHistogram model, final ChannelSource channelSource ) {
		final JTextField channelField = (JTextField)WINDOW_REFERENCE.getView( "ChannelField" );
		
		if ( channelSource != null ) {
			final Channel channel = channelSource.getChannel();
			if ( channel != null ) {
				channelField.setText( channel.getId() );
				setRunIndicator( true );
			}
			else {
				channelField.setText( "" );
				setRunIndicator( false );
			}
		}
		else {
			channelField.setText( "" );
			setRunIndicator( false );
		}
		
		setHasChanges( true );
	}
	
	
	
	/** event indicating that the model's histogram has changed */
	public void histogramUpdated( final ChannelHistogram model, final double[] range, final int[] counts, final List<Double> values, final UnivariateStatistics statistics ) {
		final WindowReference windowReference = WINDOW_REFERENCE;
		
		final JTextField meanField = (JTextField)windowReference.getView( "MeanField" );
		final JTextField sigmaField = (JTextField)windowReference.getView( "SigmaField" );
		final int population = statistics.population();
		meanField.setText( population > 0 ? STATS_FORMAT.format( statistics.mean() ) : "" );
		sigmaField.setText( population > 1 ? STATS_FORMAT.format( statistics.sampleStandardDeviation() ) : "" );
		
		HISTOGRAM_DATA.clear();
		
		if ( counts.length > 0 ) {
			final double lowerLimit = range[0];
			final double span = range[1] - lowerLimit;
			if ( span > 0.0 ) {
				final double scale = span / counts.length;		
				//System.out.println( "lower: " + lowerLimit + ", span: " + span + ", counts: " + counts.length + ", scale: " + scale );
				double x = lowerLimit;
				double y = 0.0;
				for ( int bindex = 0 ; bindex < counts.length ; bindex++ ) {
					final double x0 = x;
					final double y0 = y;
					x += scale;
					y = (double)counts[bindex];
					HISTOGRAM_DATA.addPoint( x0, y0 );
					HISTOGRAM_DATA.addPoint( x0, y );
					HISTOGRAM_DATA.addPoint( x, y );
				}
				HISTOGRAM_DATA.addPoint( x, 0.0 );
			}
		}
		final FunctionGraphsJPanel histogramPlot = (FunctionGraphsJPanel)windowReference.getView( "HistogramPlot" );
		histogramPlot.refreshGraphJPanel();
	}	
}
