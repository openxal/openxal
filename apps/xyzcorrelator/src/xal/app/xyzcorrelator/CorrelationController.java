//
//  CorrelationController.java
//  xal
//
//  Created by Tom Pelaia on 12/12/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.xyzcorrelator;

import java.io.File;
import java.io.FileWriter;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import xal.extension.bricks.WindowReference;
import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.ca.*;
import xal.smf.*;
import xal.extension.widgets.smf.*;
import xal.tools.correlator.Correlation;


/** controller for binding the correlation model to the user interface */
public class CorrelationController implements DataListener {
 	/** the data adaptor label used for reading and writing this controller */
	static public final String DATA_LABEL = "CorrelationController";
	
	/** file dialog for choosing the export file */
	private static JFileChooser EXPORT_FILE_DIALOG;
	
	/** Model for managing the correlations */
	final private CorrelationModel MODEL;
	
	/** document */
	final private XyzDocument DOCUMENT;
	
	/** bricks window reference */
	final private WindowReference WINDOW_REFERENCE;
	
	/** plot for displaying correlated data */
	final private FunctionGraphsJPanel CORRELATION_PLOT;
	
	/** spinner for setting the buffer limit */
	final private JSpinner BUFFER_SPINNER;
	
	/** spinner for setting the fit order */
	final private JSpinner FIT_ORDER_SPINNER;
	
	/** handles the plotting of correlations */
	private CorrelationPlotter _correlationPlotter;
	
	/** table model for the channel table */
	private ChannelTableModel CHANNEL_TABLE_MODEL;
	
	
	/** Primary Constructor */
	public CorrelationController( final XyzDocument document, final WindowReference windowReference ) {
		DOCUMENT = document;
		WINDOW_REFERENCE = windowReference;		
		
		CORRELATION_PLOT = (FunctionGraphsJPanel)windowReference.getView( "CorrelationPlot" );
		CORRELATION_PLOT.setName( "Channel Correlations" );
		SimpleChartPopupMenu.addPopupMenuTo( CORRELATION_PLOT );
		
		_correlationPlotter = CorrelationPlotter.getEmptyCorrelationPlotterInstance();
		
		BUFFER_SPINNER = (JSpinner)windowReference.getView( "BufferSpinner" );
		FIT_ORDER_SPINNER = (JSpinner)windowReference.getView( "FitOrderSpinner" );
		
		MODEL = document.getModel();
		setBufferLimit( CorrelationModel.DEFAULT_CORRELATION_BUFFER_LIMIT );	// initialize the buffer limit
		
		setupViews( windowReference );
		
		// start handling events
		MODEL.addCorrelationModelListener( new ModelHandler() );	
	}
	
    
    /** provides the name used to identify the class in an external data source. */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /** Instructs the receiver to update its data based on the given adaptor. */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "bufferLimit" ) ) {
			final int bufferLimit = adaptor.intValue( "bufferLimit" );
			_correlationPlotter.setBufferLimit( bufferLimit );
			setBufferLimit( bufferLimit );
			BUFFER_SPINNER.setValue( bufferLimit );
		}
		
		if ( adaptor.hasAttribute( "fitOrder" ) ) {
			final int fitOrder = adaptor.intValue( "fitOrder" );
			_correlationPlotter.setFitOrder( fitOrder );
			FIT_ORDER_SPINNER.setValue( fitOrder );
		}
		
		if ( adaptor.hasAttribute( "correlationResolution" ) ) {
			final double resolution = adaptor.doubleValue( "correlationResolution" );
			MODEL.setCorrelationResolution( resolution );
			final JTextField correlationResolutionField = (JTextField)WINDOW_REFERENCE.getView( "CorrelationResolutionField" );
			correlationResolutionField.setText( String.valueOf( resolution ) );
		}
    }
    
    
    /** Instructs the receiver to write its data to the adaptor for external storage. */
    public void write( final DataAdaptor adaptor ) {
        adaptor.setValue( "bufferLimit", _correlationPlotter.getBufferLimit() );
        adaptor.setValue( "fitOrder", _correlationPlotter.getFitOrder() );
		adaptor.setValue( "correlationResolution", MODEL.getCorrelationResolution() );
    }
	
	
	/** setup the views */
	private void setupViews( final WindowReference windowReference ) {
		final JTable channelTable = (JTable)windowReference.getView( "ChannelTable" );
		channelTable.setDragEnabled( true );
		channelTable.setTransferHandler( new ChannelTransferHandler() );
		CHANNEL_TABLE_MODEL = new ChannelTableModel( MODEL );
		channelTable.setModel( CHANNEL_TABLE_MODEL );
		channelTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
		channelTable.getColumnModel().getColumn( ChannelTableModel.ENABLE_COLUMN ).setMaxWidth( new JLabel(" Monitor ").getPreferredSize().width );
		channelTable.getColumnModel().getColumn( ChannelTableModel.PLOTTING_COLUMN ).setMaxWidth( new JLabel(" Plot ").getPreferredSize().width );
		
		
		final JToggleButton playButton = (JToggleButton)windowReference.getView( "PlayButton" );
		playButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.startCorrelator();
				updateRunStatus();
			}
		});
		
		final JToggleButton stopButton = (JToggleButton)windowReference.getView( "StopButton" );
		stopButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				MODEL.stopCorrelator();	
				updateRunStatus();
			}
		});
		
		final ButtonGroup runButtonGroup = new ButtonGroup();
		runButtonGroup.add( playButton );
		runButtonGroup.add( stopButton );
		
		final JButton clearButton = (JButton)windowReference.getView( "ClearButton" );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				clearPlot();
				MODEL.clearCorrelationBuffer();
			}
		});
		
		final JTextField correlationResolutionField = (JTextField)windowReference.getView( "CorrelationResolutionField" );
		correlationResolutionField.setText( String.valueOf( MODEL.getCorrelationResolution() ) );
		correlationResolutionField.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String resolutionText = correlationResolutionField.getText();
				try {
					 final double resolution = Double.parseDouble( resolutionText );
					 MODEL.setCorrelationResolution( resolution );
					 DOCUMENT.setHasChanges( true );
			}
				catch ( Exception exception ) {
					 correlationResolutionField.setText( String.valueOf( MODEL.getCorrelationResolution() ) );
				}
			}
		});

		final SpinnerNumberModel bufferModel = (SpinnerNumberModel)BUFFER_SPINNER.getModel();
		bufferModel.setMinimum( 2 );
		bufferModel.setValue( _correlationPlotter.getBufferLimit() );
		BUFFER_SPINNER.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final int bufferSize = bufferModel.getNumber().intValue();
				final int bufferLimit = bufferSize >= 2 ? bufferSize : 2;	// restrict buffer limit to be at least 2
				setBufferLimit( bufferLimit );
				DOCUMENT.setHasChanges( true );
			}
		});
		
		final JButton exportButton = (JButton)windowReference.getView( "ExportButton" );
		exportButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				exportBuffer();
			}		
		});
		
		final JButton insertChannelButton = (JButton)windowReference.getView( "InsertChannelButton" );
		insertChannelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int selection = channelTable.getSelectedRow();
				if ( selection < 0 ) {
					MODEL.addChannelPlaceholder();	// add a placeholder to the end
				}
				else {
					MODEL.insertChannelPlaceholder( selection );	// insert the placeholder before the selected row
				}
				CHANNEL_TABLE_MODEL.fireTableDataChanged();
			}
		});

		final JButton addChannelRefButton = (JButton)windowReference.getView( "AddChannelRefButton" );
		addChannelRefButton.addActionListener( getChannelAddHandler( channelTable ) );

		final JButton deleteChannelButton = (JButton)windowReference.getView( "DeleteChannelButton" );
		deleteChannelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int selection = channelTable.getSelectedRow();
				if ( selection >= 0 ) {
					MODEL.deleteChannelPlaceholder( selection );
					CHANNEL_TABLE_MODEL.fireTableDataChanged();
				}
			}
		});
		
		final JButton clearFitButton = (JButton)windowReference.getView( "ClearFitButton" );
		clearFitButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_correlationPlotter.clearFit();
			}						  
		});
		
		final JButton fitButton = (JButton)windowReference.getView( "FitButton" );
		fitButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_correlationPlotter.performFit();
			}						
		});
		
		final JButton copyFitButton = (JButton)windowReference.getView( "CopyFitButton" );
		copyFitButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String equation = _correlationPlotter.getFitEquation();
				System.out.println( "Copying Equation: " + equation );
				final StringSelection equationSelection = new StringSelection( equation );
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents( equationSelection, equationSelection );
			}
		 });
		
		final SpinnerNumberModel fitOrderModel = (SpinnerNumberModel)FIT_ORDER_SPINNER.getModel();
		fitOrderModel.setMinimum( 1 );
		fitOrderModel.setMaximum( 8 );
		fitOrderModel.setValue( _correlationPlotter.getFitOrder() );
		FIT_ORDER_SPINNER.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				final int fitOrder = fitOrderModel.getNumber().intValue();
			    _correlationPlotter.setFitOrder( fitOrder >= 1 ? fitOrder : 1 );
				DOCUMENT.setHasChanges( true );
			}
		});
	}
	
	
	/** update the view to indicate run status */
	private void updateRunStatus() {
		final JToggleButton playButton = (JToggleButton)WINDOW_REFERENCE.getView( "PlayButton" );
		playButton.setSelected( MODEL.isRunning() );
	}
	
	
	/** handle the button action to select channels to add to the correlator */
	private ActionListener getChannelAddHandler( final JTable channelTable ) { 
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final AcceleratorSeq selectedSequence = DOCUMENT.getSelectedSequence();
				final AcceleratorSeq sequence = selectedSequence != null ? selectedSequence : DOCUMENT.getAccelerator();
				if ( sequence != null ) {
					final List<AcceleratorNode> nodes = sequence.getAllInclusiveNodes( true );
					final NodeChannelSelector channelRefSelector = NodeChannelSelector.getInstanceFromNodes( nodes, DOCUMENT.getAcceleratorWindow(), "Select Channels" );
					final List<NodeChannelRef> channelRefs = channelRefSelector.showDialog();
					if ( channelRefs != null && channelRefs.size() > 0 ) {
						final int selectedRow = channelTable.getSelectedRow();	// specifies where to insert the channels
						if ( selectedRow < 0 ) {
							MODEL.addChannelRefs( channelRefs );
						}
						else {
							MODEL.addChannelRefs( selectedRow, channelRefs );	// insert the channel references before the selected row
						}
						CHANNEL_TABLE_MODEL.fireTableDataChanged();
					}
				}
				else {
					// should display a warning dialog
					DOCUMENT.getAcceleratorWindow().displayError( "PV Selection Error", "You must first select an accelerator sequence from which the PVs will be supplied." );
				}
			}
		};		
	}	
	
	
	/** clear the plot */
	private void clearPlot() {
		_correlationPlotter.clearPlot();
	}
	
	
	/** set the buffer limit */
	private void setBufferLimit( final int bufferLimit ) {
		MODEL.setCorrelationBufferLimit( bufferLimit );
		_correlationPlotter.setBufferLimit( bufferLimit );
	}

	
	/** export the correlation buffer */
	private void exportBuffer() {
		if ( EXPORT_FILE_DIALOG == null ) {
			EXPORT_FILE_DIALOG = new JFileChooser();
		}
		final Date timeStamp = new Date();
		try {	// attempt to set a default file name based upon the current time
			final String defaultName = "Correlations_" + new SimpleDateFormat( "yyyyMMdd'T'HHmmss" ).format( timeStamp ) + ".dat";
			EXPORT_FILE_DIALOG.setSelectedFile( new File( EXPORT_FILE_DIALOG.getCurrentDirectory(), defaultName ) );
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
		final JFrame window = (JFrame)WINDOW_REFERENCE.getWindow();
		final int status = EXPORT_FILE_DIALOG.showSaveDialog( window );
		switch ( status ) {
			case JFileChooser.APPROVE_OPTION:
				final File file = EXPORT_FILE_DIALOG.getSelectedFile();
				if ( file.exists() ) {
					final int proceedStatus = JOptionPane.showOptionDialog( window, "Warning, " + file + " Exists! \nDo you want to overwrite this file?", "Existing File", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null );
					switch ( proceedStatus ) {
						case JOptionPane.CLOSED_OPTION: case JOptionPane.CANCEL_OPTION:
							return;
						default:
							break;
					}
				}
				System.out.println( "Exporting data to: " + file );
				try {
					final FileWriter writer = new FileWriter( file );
					writer.write( "XYZ Correlations - " + new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" ).format( timeStamp ) + "\n" );
					final List<Channel> channels = MODEL.getMonitoredChannels();
					writer.write( "Monitored channels: " );
					boolean isFirstChannel = true;
					for ( final Channel channel : channels ) {
						if ( !isFirstChannel )  writer.write( ", " );
						writer.write( channel.getId() );
						isFirstChannel = false;
					}
					writer.write( "\n" );
					
					final List<Correlation<ChannelTimeRecord>> buffer = MODEL.getCorrelationBufferCopy();
					
					final SimpleDateFormat timestampFormatter = new SimpleDateFormat( "MM/dd/yyyy HH:mm:ss.SSS" );
					for ( final Correlation<ChannelTimeRecord> correlation : buffer ) {
						writer.write( timestampFormatter.format( correlation.meanDate() ) );
						for ( final Channel channel : channels ) {
							writer.write ( "\t" );
							final String channelID = channel.getId();
							final ChannelTimeRecord record = correlation.getRecord( channelID );
							if ( record != null ) {
								final double value = record.doubleValue();
								writer.write( String.valueOf( value ) );
							}
							else {
								writer.write( "N/A" );
							}
						}
						writer.write( "\n" );
					}
					
					writer.flush();
					writer.close();
				}
				catch ( Exception exception ) {
					JOptionPane.showMessageDialog( window, exception.getMessage(), "Exception exporting data", JOptionPane.ERROR_MESSAGE );
					exception.printStackTrace();
				}
				break;
			default:
				break;
		}
	}
	
	
	
	/** Handle model events */
	private class ModelHandler implements CorrelationModelListener {		
		/** the plotting channels have changed */
		public void plottingChannelsChanged( final List<Channel> channels ) {
			final int numChannels = channels.size();
			if ( numChannels > 1 ) {
				final String xLabel = channels.get( 0 ).getId();
				final String yLabel = channels.get( 1 ).getId();
				CORRELATION_PLOT.setAxisNames( xLabel, yLabel );
				
				final Box spectrumBox = (Box)WINDOW_REFERENCE.getView( "Spectrum Box" );
				final JLabel colorPVLabel = (JLabel)WINDOW_REFERENCE.getView( "ColorPVLabel" );
				if ( numChannels > 2 ) {
					colorPVLabel.setText( channels.get( 2 ).getId() );
					spectrumBox.setVisible( true );
				}
				else {
					colorPVLabel.setText( "" );
					spectrumBox.setVisible( false );
				}
			}
			clearPlot();
			
			// if the current plotter can't handle the new channels then we need to create a new one
			if ( !_correlationPlotter.supportsChannelCount( numChannels ) ) {
				final int bufferLimit = _correlationPlotter.getBufferLimit();
				final int fitOrder = _correlationPlotter.getFitOrder();
				_correlationPlotter = CorrelationPlotter.getInstance( numChannels, CORRELATION_PLOT, WINDOW_REFERENCE, bufferLimit, fitOrder );
			}
			
			// plot the plot records in the buffer
			final List<String> plotChannelIDs = CorrelationModel.getChannelIDs( channels );
			final List<Correlation<ChannelTimeRecord>> correlations = MODEL.getCorrelationBufferCopy();
			for ( final Correlation<ChannelTimeRecord> correlation : correlations ) {
				final List<ChannelTimeRecord> plotRecords = CorrelationModel.getCorrelationRecordsForChannelIDs( correlation, plotChannelIDs );
				_correlationPlotter.plot( plotRecords, correlation.meanDate() );
			}
			
			CHANNEL_TABLE_MODEL.fireTableDataChanged();			
		}
		
		
		/** correlation captured */
		public void correlationCaptured( final Correlation<ChannelTimeRecord> correlation, final List<ChannelTimeRecord> plotRecords ) {
			_correlationPlotter.plot( plotRecords, correlation.meanDate() );			
		}
		
		
		/** the monitored channels have changed */
		public void monitoredChannelsChanged( final List<Channel> channels ) {
			CHANNEL_TABLE_MODEL.fireTableDataChanged();
			updateRunStatus();
		}
	}
	
	
	
	/** channel transfer handler */
	class ChannelTransferHandler extends TransferHandler {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
		/** Constructor */
		public ChannelTransferHandler() {
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			return TransferHandler.NONE;
		}
		
		
		/** determine if the table can import at least one of the tranferable flavors */
		public boolean canImport( final JComponent component, final DataFlavor[] flavors ) {
			for ( DataFlavor flavor : flavors ) {
				if ( flavor == DataFlavor.stringFlavor )  return true;
			}
			return false;
		}
		
		
		/** import the transferable */
		public boolean importData( final JComponent component, final Transferable transferable ) {
			final JTable table = (JTable)component;
			try {
				final int selectedRow = table.getSelectedRow();
				final String transfer = (String)transferable.getTransferData( DataFlavor.stringFlavor );
				final String pv = transfer.replaceAll( "[^\\p{Graph}]", "" );	// strip all but visible print characters
				if ( selectedRow >= 0 ) {
					MODEL.setChannelPV( selectedRow, pv );
				}
				return true;
			}
			catch( UnsupportedFlavorException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( java.io.IOException exception ) {
				exception.printStackTrace();
				return false;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				return false;
			}
		}
		
		
		/** complete the transfer */
		public void exportDone( final JComponent component, final Transferable transferable, final int action ) {
		}
	}	
}
