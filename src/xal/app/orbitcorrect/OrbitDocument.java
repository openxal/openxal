/*
 * OrbitDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.orbitcorrect;

import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.Timer;
import javax.swing.JToggleButton.ToggleButtonModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import java.text.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.extension.bricks.WindowReference;
import xal.tools.data.*;
import xal.extension.widgets.plot.*;
import xal.extension.widgets.swing.KeyValueRecordSelector;
import xal.tools.text.FormattedNumber;
import xal.tools.xml.XmlDataAdaptor;
import xal.smf.*;
import xal.smf.data.XMLDataManager;


/**
 * Document for the orbit correction application
 * @author  t6p
 */
public class OrbitDocument extends AcceleratorDocument implements DataListener, ModificationStoreListener, SwingConstants {
	/** reference to the main window */
	protected WindowReference _windowReference;
	
	/** store of this document's modifications */
	protected ModificationStore _modificationStore;
	
	/** Orbit model */
	protected OrbitModel _model;
	
	/** model for the chart of the orbit traces */
	protected ChartModel _chartModel;
	
	/** Dialog window for displaying and setting trace properties */
	protected TraceChartDialog _traceDialog;
	
	/** Dialog window for displaying and managing orbit sources */
	protected OrbitSourcesDialog _orbitSourcesDialog;
	
	
    /** Constructor */
    public OrbitDocument() {
        this( null );		
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public OrbitDocument( final URL url ) {
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
		_windowReference = windowReference;
		
		_modificationStore = new ModificationStore();
		_modificationStore.addModificationStoreListener( this );
		
        setSource( url );
		
		_model = new OrbitModel( _modificationStore );
						
        if ( url != null ) {
			final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			update( documentAdaptor.childAdaptor( dataLabel() ) );
		}
		
		new FlattenController( windowReference, _model );

		setHasChanges( false );
    }
	
	
	/** Dispose of this document's resources. */
	public void freeCustomResources() {
		if ( _model != null ) {
			_model.dispose();
			_model = null;
		}
	}
	
	
	/**
	 * Get the orbit model
	 * @return the orbit model
	 */
	public OrbitModel getModel() {
		return _model;
	}
	
	
	/**
	 * Get this document's modification store.
	 * @return this document's modification store
	 */
	public ModificationStore getModificationStore() {
		return _modificationStore;
	}
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
		final WindowReference windowReference = _windowReference;
		
        mainWindow = (XalWindow)windowReference.getWindow();
		
		final FunctionGraphsJPanel plot = (FunctionGraphsJPanel)windowReference.getView( "OrbitPlot" );
		_chartModel = new ChartModel( plot, _model );
		// add the synoptic view to the chart
		final Box synopticBox = (Box)windowReference.getView( "SynopticContainer" );
		synopticBox.add( _chartModel.getNodeView() );
		
		setupFilters( windowReference );
		setupButtons( windowReference );
		setupBPMDisplay( windowReference );
		setupConfigurationView( windowReference );
    }

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        try {
            final XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
            documentAdaptor.writeNode( this );
            documentAdaptor.writeToUrl( url );
            setHasChanges( false );
        }
        catch( XmlDataAdaptor.WriteException exception ) {
			if ( exception.getCause() instanceof java.io.FileNotFoundException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file access exception!", exception );
			}
			else if ( exception.getCause() instanceof java.io.IOException ) {
				System.err.println( exception );
				displayError( "Save Failed!", "Save failed due to a file IO exception!", exception );
			}
			else {
				exception.printStackTrace();
				displayError( "Save Failed!", "Save failed due to an internal write exception!", exception );
			}
        }
        catch( Exception exception ) {
			exception.printStackTrace();
            displayError( "Save Failed!", "Save failed due to an internal exception!", exception );
        }
    }
    
    
    /**
     * Hook for handling the accelerator change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void acceleratorChanged() {
		_model.setAccelerator( this.accelerator );
		_modificationStore.postModification( this );
    }
    
    
    /**
     * Hook for handling the selected sequence change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void selectedSequenceChanged() {
		_model.setSequence( this.selectedSequence );
		_modificationStore.postModification( this );
    }


    /**
     * Hook for handling the accelerator file path change event.  Subclasses should override
     * this method to provide custom handling.  The default handler does nothing.
     */
    public void acceleratorFilePathChanged() {
    }
	
	
	/**
	 * Event indicating that a modification has occured.
	 * @param store the modification store
	 * @param source the source of the modification
	 * @param modification optional modification information
	 */
	public void modificationMade( final ModificationStore store, final Object source, final Map<?, ?> modification ) {
		setHasChanges( true );
	}

	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "OrbitCorrection";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "acceleratorPath" ) ) {
			final String acceleratorPath = adaptor.stringValue( "acceleratorPath" );
			applySelectedAcceleratorWithDefaultPath( acceleratorPath );
		}
		
		if ( adaptor.hasAttribute( "sequence" ) ) {
			final String sequenceID = adaptor.stringValue( "sequence" );
			setSelectedSequence( getAccelerator().getSequence( sequenceID ) );
		}
		else {
			final DataAdaptor comboAdaptor = adaptor.childAdaptor( "comboseq" );
			if ( comboAdaptor != null ) {
				setSelectedSequence( AcceleratorSeqCombo.getInstance( getAccelerator(), comboAdaptor ) );				
			}
		}
		
		final DataAdaptor modelAdaptor = adaptor.childAdaptor( _model.dataLabel() );
        _model.update( modelAdaptor );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
		adaptor.setValue( "acceleratorPath", getAcceleratorFilePath() );
		
		final AcceleratorSeq sequence = getSelectedSequence();
		if ( sequence instanceof AcceleratorSeqCombo ) {
			final DataAdaptor comboAdaptor = adaptor.createChild( "comboseq" );
			((AcceleratorSeqCombo)sequence).write( comboAdaptor );
		}
		else {
			adaptor.setValue( "sequence", getSelectedSequence().getId() );
		}
		
        adaptor.writeNode( _model );		
	}
	
	
	/** Make a view that allows the user to filter the traces on the display. */
	protected void setupFilters( final WindowReference windowReference ) {
		final JCheckBox xDisplayCheckbox = (JCheckBox)windowReference.getView( "HorizontalDisplayCheckbox" );
		final JCheckBox yDisplayCheckbox = (JCheckBox)windowReference.getView( "VerticalDisplayCheckbox" );
		final JCheckBox amplitudeDisplayCheckbox = (JCheckBox)windowReference.getView( "AmplitudeDisplayCheckbox" );
						
		associateTraceSource( xDisplayCheckbox, OrbitTraceAdaptor.X_AVG_TYPE );
		associateTraceSource( yDisplayCheckbox, OrbitTraceAdaptor.Y_AVG_TYPE );
		associateTraceSource( amplitudeDisplayCheckbox, OrbitTraceAdaptor.AMP_AVG_TYPE );
		
		final JCheckBox legendCheckbox = (JCheckBox)windowReference.getView( "LegendCheckbox" );
		legendCheckbox.setSelected( _chartModel.isLegendVisible() );
		legendCheckbox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_chartModel.setLegendVisible( legendCheckbox.isSelected() );
			}
		});
		
		final JCheckBox gridCheckbox = (JCheckBox)windowReference.getView( "GridCheckbox" );
		gridCheckbox.setSelected( _chartModel.isGridVisible() );
		gridCheckbox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_chartModel.setGridVisible( gridCheckbox.isSelected() );
			}
		});
	}
	
	
	/**
	 * Make a checkbox for enabling and disabling traces of a specified type.
	 * @param control the checkbox to configure for the trace source
	 * @param type the trace source type used in the qualifier
	 */
	protected void associateTraceSource( final JCheckBox control, final String type ) {
		control.setSelected( _chartModel.isSourceTypeVisible( type ) );
		control.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_chartModel.displayTraceSourceWithType( type, control.isSelected() );
			}
		});
	}


	/** Create a button view for displaying buttons that control the chart. */
	protected void setupButtons( final WindowReference windowReference ) {
		final JButton orbitsButton = (JButton)windowReference.getView( "OrbitsButton" );
		orbitsButton.addActionListener(
			new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					showOrbitSourcesDialog();
				}
			} );
	}
	
	
	/** setup the BPM display */
	protected void setupBPMDisplay( final WindowReference windowReference ) {
		final JTable bpmTable = (JTable)windowReference.getView( "BPMTable" );
		bpmTable.setModel( new BPMTableModel( _model ) );
	}
	
	
	/** setup the configuration view */
	protected void setupConfigurationView( final WindowReference windowReference ) {
		final JCheckBox beamEventCheckbox = (JCheckBox)windowReference.getView( "Beam Event Checkbox" );
		beamEventCheckbox.addActionListener( newBeamEventCheckboxHandler( beamEventCheckbox ) );
	}
	
	
	/** create a new beam event checkbox handler */
	private ActionListener newBeamEventCheckboxHandler( final JCheckBox beamEventCheckbox ) {
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_model.setUseBeamEventTrigger( beamEventCheckbox.isSelected() );
			}
		};
	}
	
	
	/** Show the trace dialog window for displaying and editing trace properties. */
	protected void showTraceDialog() {
		if ( _traceDialog == null ) {
			_traceDialog = new TraceChartDialog( _chartModel, mainWindow );
		}
		_traceDialog.showNear( mainWindow );
	}


	/** Show the orbit sources dialog window for managing the model's orbit sources. */
	protected void showOrbitSourcesDialog() {
		if ( _orbitSourcesDialog == null ) {
			_orbitSourcesDialog = new OrbitSourcesDialog( _model, mainWindow );
		}
		_orbitSourcesDialog.showNear( mainWindow );
	}


	/**
	 * Register actions specific to this document instance.
	 * @param commander  The commander with which to register the custom commands.
	 */
	public void customizeCommands( final Commander commander ) {
		final Action addLiveOrbitAction = new AbstractAction( "add-live-orbit" ) {
            
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( ActionEvent event ) {
				System.out.println( "Add Live Orbit..." );
				openOrbitSourceEditor( OrbitSourceEditor.LIVE_TYPE );
			}
		};
		commander.registerAction( addLiveOrbitAction );
		
		final Action addDifferenceOrbitAction = new AbstractAction( "add-diff-orbit" ) {
            
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( ActionEvent event ) {
				System.out.println( "Add Difference Orbit..." );
				openOrbitSourceEditor( OrbitSourceEditor.DIFFERENCE_TYPE );
			}
		};
		commander.registerAction( addDifferenceOrbitAction );
		
		final Action addSnapshotOrbitAction = new AbstractAction( "add-snapshot-orbit" ) {
            
            private static final long serialVersionUID = 1L;

			public void actionPerformed( ActionEvent event ) {
				System.out.println( "Add Snapshot Orbit..." );
				openOrbitSourceEditor( OrbitSourceEditor.SNAPSHOT_TYPE );
			}
		};
		commander.registerAction( addSnapshotOrbitAction );
		
		final Action addLoggedOrbitAction = new AbstractAction( "add-logged-orbit" ) {
            
            private static final long serialVersionUID = 1L;

			public void actionPerformed( ActionEvent event ) {
				System.out.println( "Add Logged Orbit..." );
				openOrbitSourceEditor( OrbitSourceEditor.LOGGED_TYPE );
			}
		};
		commander.registerAction( addLoggedOrbitAction );
        
        
        // import an orbit from another open document
        final Action importOrbitAction = new AbstractAction( "import-document-orbit" ) {
            
            private static final long serialVersionUID = 1L;

            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Importing an orbit from another open document" );
                importOrbitFromOpenDocument();
            }
        };
        commander.registerAction( importOrbitAction );
		
		final Action defineOrbitAction = new AbstractAction( "define-orbit" ) {
            
            private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				System.out.println( "Define a new orbit..." );
				openOrbitSourceEditor( OrbitSourceEditor.USER_DEFINED_TYPE );
			}
		};
		commander.registerAction( defineOrbitAction );
	}


	/**
	 * Open the orbit source editor for the specified type of orbit.
	 * @param type  the orbit source type
	 */
	protected void openOrbitSourceEditor( final int type ) {
		final OrbitSourceEditor editor = new OrbitSourceEditor( type, _model, mainWindow );
		editor.showNear( mainWindow );
	}
    
    
    /** import selected orbits from other open documents */
    private void importOrbitFromOpenDocument() {
        final List<?> documents = Application.getApp().getDocuments();
        final List<SnapshotOrbitSource> snapshots = new ArrayList<SnapshotOrbitSource>();
        for ( final Object document : documents ) {
            if ( document != this && document instanceof OrbitDocument ) {
                final OrbitDocument orbitDocument = (OrbitDocument)document;
                final OrbitModel model = orbitDocument.getModel();
                final List<OrbitSource> orbitSources = model.getOrbitSources();
                for ( final OrbitSource orbitSource : orbitSources ) {
                    if ( orbitSource instanceof SnapshotOrbitSource ) {
                        snapshots.add( (SnapshotOrbitSource)orbitSource );
                    }
                }
            }
        }
        
        final KeyValueRecordSelector<SnapshotOrbitSource> snapshotSelector = KeyValueRecordSelector.getInstance( snapshots, getMainWindow(), "Import Snapshots", "label", "orbit.timeStamp", "sequence.id" );
        snapshotSelector.getRecordTableModel().setColumnName( "sequence.id", "Sequence" );
        final List<SnapshotOrbitSource> selectedSnapshots = snapshotSelector.showDialog();
        
        if ( selectedSnapshots != null ) {
            for ( final SnapshotOrbitSource snapshot : selectedSnapshots ) {
                final InMemoryDataAdaptor adaptor = new InMemoryDataAdaptor( "Snapshot" );
                snapshot.write( adaptor );
                _model.addOrbitSourceFromArchive( adaptor );
            }
        }
    }
}



/** Table model of BPMs. */
class BPMTableModel extends AbstractTableModel implements OrbitModelListener, BpmEventListener {
    
    private static final long serialVersionUID = 1L;

	static final protected int ENABLE_COLUMN = 0;
	static final protected int LABEL_COLUMN = 1;
	static final protected int POSITION_COLUMN = 2;
	static final protected int XAVG_COLUMN = 3;
	static final protected int YAVG_COLUMN = 4;
	static final protected int AMPAVG_COLUMN = 5;
	
	/** orbit model for which to display the BPMs */
	final protected OrbitModel ORBIT_MODEL;
	
	/** BPM agents */
	final protected List<BpmAgent> BPM_AGENTS;
	
	
	/** Constructor */
	public BPMTableModel( final OrbitModel model ) {
		BPM_AGENTS = new ArrayList<BpmAgent>( model.getAvailableBPMAgents().size() );
		ORBIT_MODEL = model;
		model.addOrbitModelListener( this );
		reloadBPMAgents();
	}
	
	
	/** Get the row count. */
	public int getRowCount() {
		return ORBIT_MODEL.getAvailableBPMAgents().size();
	}
	
	
	/** Get the column count. */
	public int getColumnCount() {
		return 6;
	}
	
	
	/** get the BPM agent for the specified row */
	protected BpmAgent getBpmAgent( final int row ) {
		synchronized ( BPM_AGENTS ) {
			return BPM_AGENTS.get( row );
		}
	}
	
	
	/** get the row for the specified BPM agent */
	protected int getRowForBpmAgent( final BpmAgent bpmAgent ) {
		synchronized ( BPM_AGENTS ) {
			return BPM_AGENTS.indexOf( bpmAgent );
		}
	}
	
	
	/** Get the column's value class. */
	public Class<?> getColumnClass( final int column ) {
		switch( column ) {
			case ENABLE_COLUMN:
				return Boolean.class;
			case LABEL_COLUMN:
				return String.class;
			case POSITION_COLUMN:
				return FormattedNumber.class;
			case XAVG_COLUMN:
				return FormattedNumber.class;
			case YAVG_COLUMN:
				return FormattedNumber.class;
			case AMPAVG_COLUMN:
				return FormattedNumber.class;
			default:
				return Object.class;
		}
	}
	
	
	/** Determine whether the cell is editable. */
	public boolean isCellEditable( final int row, final int column ) {
		switch( column ) {
			case ENABLE_COLUMN:
				return true;
			default:
				return false;
		}
	}
	
	
	/** Get the value to display in the cell. */
	public Object getValueAt( final int row, final int column ) {
		final BpmAgent bpmAgent = getBpmAgent( row );
		if ( bpmAgent == null )  return null;
		final BpmRecord bpmRecord = bpmAgent.getLatestRecord();
		Object value = null;
		
		switch( column ) {
			case ENABLE_COLUMN:
				value = Boolean.valueOf( bpmAgent.isEnabled() );
				break;
			case LABEL_COLUMN:
				value = bpmAgent.getID();
				break;
			case POSITION_COLUMN:
				final AcceleratorSeq sequence = ORBIT_MODEL.getSequence();
				return sequence != null ? new FormattedNumber( "0.0", bpmAgent.getPositionIn( sequence ) ) : new FormattedNumber( "0.0", Double.NaN );
			case XAVG_COLUMN:
				return bpmRecord != null ? new FormattedNumber( "0.0", bpmRecord.getXAvg() ) : new FormattedNumber( "0.0", Double.NaN );
			case YAVG_COLUMN:
				return bpmRecord != null ? new FormattedNumber( "0.0", bpmRecord.getYAvg() ) : new FormattedNumber( "0.0", Double.NaN );
			case AMPAVG_COLUMN:
				return bpmRecord != null ? new FormattedNumber( "0.0", bpmRecord.getAmpAvg() ) : new FormattedNumber( "0.0", Double.NaN );
			default:
				break;
		}
		
		return value;
	}
	
	
	/** Set the value associated with the specified cell. */
	public void setValueAt( final Object value, final int row, final int column ) {
		final BpmAgent bpmAgent = getBpmAgent( row );
		if ( bpmAgent == null )  return;
		
		switch( column ) {
			case ENABLE_COLUMN:
				final boolean enabled = ((Boolean)value).booleanValue();
				bpmAgent.setEnabled( enabled );
				ORBIT_MODEL.refreshEnabledBPMs();
				break;
			default:
				break;
		}
	}
	
	
	/** Get the column name. */
	public String getColumnName( final int column ) {
		switch( column ) {
			case ENABLE_COLUMN:
				return "Use";
			case LABEL_COLUMN:
				return "BPM";
			case POSITION_COLUMN:
				return "Position (m)";
			case XAVG_COLUMN:
				return "X Average (mm)";
			case YAVG_COLUMN:
				return "Y Average (mm)";
			case AMPAVG_COLUMN:
				return "Amplitude Average";
			default:
				return "";
		}
	}
	
	
	/** load the BPM agents */
	protected void reloadBPMAgents() {
		synchronized( BPM_AGENTS ) {
			for ( final BpmAgent bpmAgent : BPM_AGENTS ) {
				bpmAgent.removeBpmEventListener( this );
			}
			BPM_AGENTS.clear();
			final List<BpmAgent> bpmAgents = ORBIT_MODEL.getAvailableBPMAgents();
			for ( final BpmAgent bpmAgent : bpmAgents ) {
				bpmAgent.addBpmEventListener( this );
			}
			BPM_AGENTS.addAll( bpmAgents );
		}
	}
	
	
	/**
	 * The BPM's monitored state has changed.
	 * @param agent   The BPM agent with the channel whose value has changed
	 * @param record  The record of the new BPM state
	 */
	public void stateChanged( final BpmAgent agent, final BpmRecord record ) {
		final int row = getRowForBpmAgent( agent );
		if ( row >= 0 ) {
			fireTableRowsUpdated( row, row );
		}
	}
	
	
	/**
	 * The channel's connection has changed. Either it has established a new connection or the existing connection has dropped.
	 * @param agent      The BPM agent with the channel whose connection has changed
	 * @param handle     The handle of the BPM channel whose connection has changed.
	 * @param connected  The channel's new connection state
	 */
	public void connectionChanged( final BpmAgent agent, final String handle, final boolean connected ) {}
	
	
	/**
	 * Notification that the sequence has changed.
	 * @param  model        the model sending the notification
	 * @param  newSequence  the new accelerator sequence
	 */
	public void sequenceChanged( final OrbitModel model, final AcceleratorSeq newSequence ) {
		reloadBPMAgents();
		fireTableDataChanged();
	}
	
	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( OrbitModel model, List<BpmAgent> bpmAgents ) {}
	
	
	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param  model           the model sending the notification
	 * @param  newOrbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( OrbitModel model, OrbitSource newOrbitSource ) {}
	
	
	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( OrbitModel model, OrbitSource orbitSource ) {}
}
