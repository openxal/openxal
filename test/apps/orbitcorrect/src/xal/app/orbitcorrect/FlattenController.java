//
// FlattenController.java: Source file for 'FlattenController'
// Project xal
//
// Created by t6p on 8/18/10
//


package xal.app.orbitcorrect;

import xal.extension.application.Application;
import xal.extension.bricks.WindowReference;
import xal.ca.Channel;
import xal.smf.impl.*;
import xal.smf.*;
import xal.tools.data.*;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.extension.widgets.swing.KeyValueFilteredTableModel;
import xal.tools.text.FormattedNumber;
import xal.model.probe.Probe;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.*;
import java.util.logging.*;
import javax.swing.Timer;


/** FlattenController */
public class FlattenController implements FlattenListener, OrbitModelListener {
	/** index for the empirical simulator */
	final public static int EMPIRICAL_SIMULATOR_INDEX = 0;
	
	/** index for the online model simulator */
	final public static int ONLINE_MODEL_SIMULATOR_INDEX = 1;
	
	/** maximum value for the progress bar */
	final static int MAX_PROGRESS = 100;
	
	/** window reference containing this controller's views */
	final private WindowReference WINDOW_REFERENCE;
	
	/** orbit model */
	final private  OrbitModel ORBIT_MODEL;
	
	/** flattener */
	final private Flattener FLATTENER;
	
	/** button for initiating the flatten process */
	protected JButton _flattenButton;
	
	/** button for applying the predicted correction */
	protected JButton _applyButton;
	
	/** button for stopping the flatten process */
	protected JButton _stopButton;
	
	/** the selected orbit source */
	protected OrbitSource _selectedOrbitSource;
	
	/** displays the flatten progress */
	protected JProgressBar _progressBar;
	
	/** timer for keeping track of the flatten progress */
	protected Timer _progressTimer;
	
	/** table model for BPM Agents */
	private final KeyValueFilteredTableModel<BpmAgent> BPM_TABLE_MODEL;
	
	/** table model for Corrector Supplies */
	private final KeyValueFilteredTableModel<CorrectorSupply> CORRECTOR_TABLE_MODEL;
	
	/** Orbit source menu */
	protected JComboBox<Object> _orbitSourceMenu;
	
	/** Menu of orbit source to use as a reference orbit which is the flatten target */
	protected JComboBox<Object> _referenceOrbitSourceMenu;
	
	/** list model which feeds the reference orbit source menu */
	protected OrbitSourceListModel _referenceOrbitSourceListModel;
	
	/** pulldown menu of simulators */
	protected JComboBox<String> _simulatorMenu;
	
	/** Dialog for viewing and applying proposed corrections */
	protected CorrectionDialog _correctionDialog;
	
	/** The view which allows the user to change the duration of the solving run */
	protected JSpinner _flattenDurationSpinner;
	
	/** Predicted orbit source */
	protected SnapshotOrbitSource _predictedOrbitSource;
	
	
	/**
	 * Constructor
	 * @param orbitModel the main model
	 */
	public FlattenController( final WindowReference windowReference, final OrbitModel orbitModel ) {		
		WINDOW_REFERENCE = windowReference;
		ORBIT_MODEL = orbitModel;
		FLATTENER = orbitModel.getFlattener();
		
		BPM_TABLE_MODEL = new KeyValueFilteredTableModel<BpmAgent>();
		CORRECTOR_TABLE_MODEL = new KeyValueFilteredTableModel<CorrectorSupply>();
		
		makeContent( windowReference );
		
		_orbitSourceMenu.setModel( new OrbitSourceListModel( orbitModel ) );
		_referenceOrbitSourceListModel =  new OrbitSourceListModel( orbitModel, true );
		_referenceOrbitSourceMenu.setModel( _referenceOrbitSourceListModel );
		
		FLATTENER.addFlattenListener( this );				
		ORBIT_MODEL.addOrbitModelListener( this );
		
		refreshBeamPositionMonitors();
		refreshCorrectors();
		
		updateSelectedOrbitSource();
	}
    
    
    /** post a modification notice */
    private void postModification() {
        ORBIT_MODEL.postModification();
    }
	
	
	/**
	 * Set the flattener's simulator to the one specified.
	 * @param simulator the new simulator to use
	 */
	protected void setSimulator( final MachineSimulator simulator ) {		
		FLATTENER.setSimulator( simulator );
		
		final JButton configureSimulatorButton = (JButton)WINDOW_REFERENCE.getView( "ConfigureSimulatorButton" );
		if ( simulator instanceof EmpiricalSimulator ) {
			configureSimulatorButton.setEnabled( true );
		}
		else if ( simulator instanceof MappedSimulator ) {
			configureSimulatorButton.setEnabled( false );
		}
	}
	
	
	/** Make the window content. */
	protected void makeContent( final WindowReference windowReference ) {
		makeMachineSettingsView( windowReference );
		makeSimulatorView( windowReference );
		makeBottomButtonRow( windowReference );
	}
	
	
	/**
	 * Make the simulator view
     * @param windowReference the window reference for which to configure the window
	 */
    //getView cannot be cast
    @SuppressWarnings( "unchecked" )
	protected void makeSimulatorView( final WindowReference windowReference ) {
		_simulatorMenu = (JComboBox)windowReference.getView( "Machine Simulator Menu" );
		_simulatorMenu.addItem( "Empirical Simulator" );
		_simulatorMenu.addItem( "Online Model Simulator" );
		
		_simulatorMenu.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int selectedIndex = _simulatorMenu.getSelectedIndex();
				loadSimulator( selectedIndex );
			}
		});

		final JButton probeEditButton = (JButton)windowReference.getView( "ProbeEditButton" );
		probeEditButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				editBaseProbe();
			}
		});
		
		final JButton resetSimulatorButton = (JButton)windowReference.getView( "ResetSimulatorButton" );
		resetSimulatorButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final MachineSimulator simulator = FLATTENER.getSimulator();
				if ( simulator != null ) {
					simulator.clear();
				}
			}
		});
		
		final JButton configureSimulatorButton = (JButton)windowReference.getView( "ConfigureSimulatorButton" );
		configureSimulatorButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int selectedIndex = _simulatorMenu.getSelectedIndex();
				switch ( selectedIndex ) {
					case EMPIRICAL_SIMULATOR_INDEX:
						configureEmpiricalSimulator( windowReference, configureSimulatorButton );
						break;
					default:
						break;
				}
			}
		});
	}


	/** Edit the base probe */
	private void editBaseProbe() {
		try {
			final Probe<?> probe = ORBIT_MODEL.getBaseProbe();
			if ( probe != null ) {
				new SimpleProbeEditor( (JFrame)WINDOW_REFERENCE.getWindow(), probe );
				ORBIT_MODEL.clearFlattenSimulator();		// mark it to regenerate maps as necessary
			} else {
				final String title = "Error Editing Probe";
				final String message = "There is no probe to edit. Please verify that you have selected an accelerator sequence.";
				JOptionPane.showMessageDialog( (JFrame)WINDOW_REFERENCE.getWindow(), message, title, JOptionPane.WARNING_MESSAGE );
			}
		}
		catch( Exception exception ) {
			final String title = "Error Editing Probe";
			final String message = "Exception while attempting to edit the probe.";
			JOptionPane.showMessageDialog( (JFrame)WINDOW_REFERENCE.getWindow(), message, title, JOptionPane.WARNING_MESSAGE );
		}
	}


	/** configure the empirical simulator */
	private void configureEmpiricalSimulator( final WindowReference windowReference, final JButton configureSimulatorButton ) {
		final EmpiricalSimulator simulator = (EmpiricalSimulator)FLATTENER.getSimulator();
		
		final WindowReference configDialogRef = Application.getAdaptor().getDefaultWindowReference( "EmpiricalSimulatorEditorDialog", windowReference.getWindow() );
		
		final JDialog editorDialog = (JDialog)configDialogRef.getWindow();
		
		final JTextField beamSettleTimeField = (JTextField)configDialogRef.getView( "Beam Settle Time Field" );
		final JTextField fieldExcursionField = (JTextField)configDialogRef.getView( "Field Excursion Field" );
		final JTextField samplesPerCorrectorField = (JTextField)configDialogRef.getView( "Samples Per Corrector Field" );
		
		// initialize the fields
		beamSettleTimeField.setText( String.valueOf( simulator.getBeamSettleTime() ) );
		fieldExcursionField.setText( String.valueOf( 100 * simulator.getCorrectorSampleExcursion() ) );
		samplesPerCorrectorField.setText( String.valueOf( simulator.getCorrectorCalibrationTrials() ) );

		final JButton cancelButton = (JButton)configDialogRef.getView( "CancelButton" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				editorDialog.setVisible( false );
			}
		});
		
		final JButton applyButton = (JButton)configDialogRef.getView( "ApplyButton" );
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				simulator.setBeamSettleTime( Long.parseLong( beamSettleTimeField.getText() ) );
				simulator.setCorrectorCalibrationTrials( Integer.parseInt( samplesPerCorrectorField.getText() ) );
				simulator.setCorrectorSampleExcursion( Double.parseDouble( fieldExcursionField.getText() ) / 100.0 );
				editorDialog.setVisible( false );
			}
		});
				
		editorDialog.setLocationRelativeTo( configureSimulatorButton );
		editorDialog.setModal( true );
		editorDialog.setVisible( true );		
	}
	
	
	/**
	 * Load the simulator corresponding to the specified code.
	 * @param simulatorCode  the code identifying the simulator to load
	 */
	protected void loadSimulator( final int simulatorCode ) {
		MachineSimulator simulator;
		switch( simulatorCode ) {
			case EMPIRICAL_SIMULATOR_INDEX:
				simulator = FLATTENER.loadSimulator( EmpiricalSimulator.getType() );
				break;
			case ONLINE_MODEL_SIMULATOR_INDEX:
				simulator = FLATTENER.loadSimulator( OnlineModelSimulator.getType() );
				break;
			default:
				return;
		}
		
		setSimulator( simulator );
	}
	
	
	/** 
	 * Make the machine view
	 */
    //getView cannot be cast
    @SuppressWarnings( "unchecked" )
	protected void makeMachineSettingsView( final WindowReference windowReference ) {
		_orbitSourceMenu = (JComboBox)windowReference.getView( "Orbit Source Menu" );
		_referenceOrbitSourceMenu = (JComboBox)windowReference.getView( "Orbit Reference Menu" );
		
		_orbitSourceMenu.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				updateSelectedOrbitSource();
			}
		});
		
		makeObjectivesView( windowReference );
		makeCorrectorView( windowReference );
		makeBPMView( windowReference );
		makeProgressView( windowReference );
	}
	
	
	/** update selected orbit source */
	protected void updateSelectedOrbitSource() {
		_selectedOrbitSource = (OrbitSource)_orbitSourceMenu.getSelectedItem();
		_flattenButton.setEnabled( _selectedOrbitSource != null );		
	}
	
	
	/**
	 * Make the progress view
	 */
	protected void makeProgressView( final WindowReference windowReference ) {
		_progressBar = (JProgressBar)windowReference.getView( "Progress Bar" );
        
		_flattenDurationSpinner = (JSpinner)windowReference.getView( "FlattenDurationSpinner" );
		_flattenDurationSpinner.setModel( new SpinnerNumberModel( 0.0, 0.0, 3600.0, 1.0 ) );
		_flattenDurationSpinner.addChangeListener( new ChangeListener() {
			public void stateChanged( final ChangeEvent event ) {
				double duration = ((Number)_flattenDurationSpinner.getValue()).doubleValue();
				FLATTENER.setSolvingTime( duration );
			}
		});
	}
	
	
	/** Make the table of correctors. */
	protected void makeCorrectorView( final WindowReference windowReference ) {
		final JTextField filterField = (JTextField)windowReference.getView( "Corrector Filter Field" );
		filterField.putClientProperty( "JTextField.variant", "search" );
		filterField.putClientProperty( "JTextField.Search.Prompt", "Corrector Filter" );
		
		CORRECTOR_TABLE_MODEL.setInputFilterComponent( filterField );
		
		CORRECTOR_TABLE_MODEL.setKeyPaths( "enabled", "ID", "formattedLatestField", "lowerFieldLimit", "upperFieldLimit" );
		
		CORRECTOR_TABLE_MODEL.setColumnEditable( "enabled", true );
		CORRECTOR_TABLE_MODEL.setColumnEditable( "lowerFieldLimit", true );
		CORRECTOR_TABLE_MODEL.setColumnEditable( "upperFieldLimit", true );
		
		CORRECTOR_TABLE_MODEL.setColumnName( "enabled", "Use" );
		CORRECTOR_TABLE_MODEL.setColumnName( "ID", "Corrector" );
		CORRECTOR_TABLE_MODEL.setColumnName( "formattedLatestField", "Field" );
		CORRECTOR_TABLE_MODEL.setColumnName( "lowerFieldLimit", "Lower Limit" );
		CORRECTOR_TABLE_MODEL.setColumnName( "upperFieldLimit", "Upper Limit" );
		
		CORRECTOR_TABLE_MODEL.setColumnClass( "enabled", Boolean.class );
		CORRECTOR_TABLE_MODEL.setColumnClass( "ID", String.class );
		CORRECTOR_TABLE_MODEL.setColumnClass( "formattedLatestField", Number.class );
		CORRECTOR_TABLE_MODEL.setColumnClass( "lowerFieldLimit", Double.class );
		CORRECTOR_TABLE_MODEL.setColumnClass( "upperFieldLimit", Double.class );
		
		CORRECTOR_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<CorrectorSupply>,CorrectorSupply>() {
			public void recordModified( final KeyValueTableModel<CorrectorSupply> tableModel, final CorrectorSupply supply, final String keyPath, final Object value ) {
                System.out.println( "Corrector modified..." );
                
				final MachineSimulator simulator = FLATTENER.getSimulator();
				if ( simulator != null ) {
					simulator.setCorrectorSupplyEnable( supply, supply.isEnabled() );
				}
                
                postModification();
			}
		});		
		
		final JTable correctorTable = (JTable)windowReference.getView( "Corrector Table" );
		correctorTable.setModel( CORRECTOR_TABLE_MODEL );
		
		// squeeze the checkbox column
		final TableColumn useColumn = correctorTable.getColumnModel().getColumn( 0 );
		useColumn.setMaxWidth( new JLabel(" Use ").getPreferredSize().width );
		
		final JButton clearButton = (JButton)windowReference.getView( "Corrector Filter Clear Button" );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				filterField.setText( "" );
			}
		});
		
		final JButton enableButton = (JButton)windowReference.getView( "Corrector Enable Button" );
		enableButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = correctorTable.getSelectedRows();
				setEnableForCorrectorsAtRows( selectedRows, true );
			}
		});
		
		final JButton disableButton = (JButton)windowReference.getView( "Corrector Disable Button" );
		disableButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = correctorTable.getSelectedRows();
				setEnableForCorrectorsAtRows( selectedRows, false );
			}
		});
        
        final JButton correctorLimitsButton = (JButton)windowReference.getView( "Corrector Limits Button" );
        correctorLimitsButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = correctorTable.getSelectedRows();
                requestCustomLimitsForCorrectorsAtRows( selectedRows );
            }
        });
        
        final JButton correctorSettingButton = (JButton)windowReference.getView( "Corrector Setting Button" );
        correctorSettingButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = correctorTable.getSelectedRows();
                requestCustomFieldForCorrectorsAtRows( selectedRows );
            }
        });
	}
	
	
	/** Make the table of BPMs. */
	protected void makeBPMView( final WindowReference windowReference ) {
		final JTextField filterField = (JTextField)windowReference.getView( "BPM Filter Field" );
		filterField.putClientProperty( "JTextField.variant", "search" );
		filterField.putClientProperty( "JTextField.Search.Prompt", "BPM Filter" );
		
		BPM_TABLE_MODEL.setInputFilterComponent( filterField );
		
		BPM_TABLE_MODEL.setKeyPaths( "flattenEnabled", "ID", "latestRecord.formattedXAvg", "latestRecord.formattedYAvg" );
		
		BPM_TABLE_MODEL.setColumnEditable( "flattenEnabled", true );
		
		BPM_TABLE_MODEL.setColumnName( "flattenEnabled", "Use" );
		BPM_TABLE_MODEL.setColumnName( "ID", "BPM" );
		BPM_TABLE_MODEL.setColumnName( "latestRecord.formattedXAvg", "X Avg" );
		BPM_TABLE_MODEL.setColumnName( "latestRecord.formattedYAvg", "Y Avg" );
		
		BPM_TABLE_MODEL.setColumnClass( "flattenEnabled", Boolean.class );
		BPM_TABLE_MODEL.setColumnClass( "ID", String.class );
		BPM_TABLE_MODEL.setColumnClass( "latestRecord.formattedXAvg", Number.class );
		BPM_TABLE_MODEL.setColumnClass( "latestRecord.formattedYAvg", Number.class );
		
		BPM_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<BpmAgent>,BpmAgent>() {
			public void recordModified( final KeyValueTableModel<BpmAgent> tableModel, final BpmAgent bpmAgent, final String keyPath, final Object value ) {
				final MachineSimulator simulator = FLATTENER.getSimulator();
				if ( simulator != null )  simulator.bpmFlattenEnableChanged( bpmAgent );
                
                postModification();
			}
		});
				
		// create the BPM table and associated model
		final JTable table = (JTable)windowReference.getView( "BPM Flatten Table" );
		table.setModel( BPM_TABLE_MODEL );
		
		// squeeze the checkbox column
		final TableColumn useColumn = table.getColumnModel().getColumn( 0 );
		useColumn.setMaxWidth( new JLabel(" Use ").getPreferredSize().width );		
		
		final JButton clearButton = (JButton)windowReference.getView( "BPM Filter Clear Button" );
		clearButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				filterField.setText( "" );
			}
		});
		
		final JButton enableButton = (JButton)windowReference.getView( "BPM Enable Button" );
		enableButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = table.getSelectedRows();
				setEnableForBPMsAtRows( selectedRows, true );
			}
		});
		
		final JButton disableButton = (JButton)windowReference.getView( "BPM Disable Button" );
		disableButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] selectedRows = table.getSelectedRows();
				setEnableForBPMsAtRows( selectedRows, false );
			}
		});
	}
	
	
	/** set the enable for the BPM at the displayed table row */
	private void setBPMEnableAtRow( final int row, final boolean enable ) {
		final MachineSimulator simulator = FLATTENER.getSimulator();
		final BpmAgent bpmAgent = BPM_TABLE_MODEL.getRecordAtRow( row );
		bpmAgent.setFlattenEnabled( enable );
		simulator.bpmFlattenEnableChanged( bpmAgent );
        
        postModification();
	}
	
	
	/** Set the BPM enable for the specified rows */
	private void setEnableForBPMsAtRows( final int[] rows, final boolean enable ) {		
		if ( rows != null && rows.length > 0 ) {
			for ( final int row : rows )  setBPMEnableAtRow( row, enable );
			if ( rows.length > 0 )  BPM_TABLE_MODEL.fireTableRowsUpdated( rows[0], rows[rows.length - 1] );
		}
		else {
			final int rowCount = BPM_TABLE_MODEL.getRowCount();
			for ( int row = 0 ; row < rowCount ; row++ )  setBPMEnableAtRow( row, enable );
			if ( rowCount > 0 )  BPM_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
		}
        
        postModification();
	}
	
	
	/** set the enable for the Corrector at the displayed table row */
	private void setCorrectorEnableAtRow( final int row, final boolean enable ) {
		final MachineSimulator simulator = FLATTENER.getSimulator();
		final CorrectorSupply supply = CORRECTOR_TABLE_MODEL.getRecordAtRow( row );
		if ( simulator != null ) {
			simulator.setCorrectorSupplyEnable( supply, enable );
		}
        
        postModification();
	}
	
	
	/** Set the Corrector enable for the specified rows */
	private void setEnableForCorrectorsAtRows( final int[] rows, final boolean enable ) {		
		if ( rows != null && rows.length > 0 ) {
			for ( final int row : rows )  setCorrectorEnableAtRow( row, enable );
			if ( rows.length > 0 )  CORRECTOR_TABLE_MODEL.fireTableRowsUpdated( rows[0], rows[rows.length - 1] );
		}
		else {
			final int rowCount = CORRECTOR_TABLE_MODEL.getRowCount();
			for ( int row = 0 ; row < rowCount ; row++ )  setCorrectorEnableAtRow( row, enable );
			if ( rowCount > 0 )  CORRECTOR_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
		}
        
        postModification();
	}
	
	
	/** Request from the user the custom limits to apply to the correctors at the specified rows */
	private void requestCustomLimitsForCorrectorsAtRows( final int[] rows ) {
        final CorrectorLimitsSetting limitsSetter = BulkCorrectorLimitsController.showDialog( (JFrame)WINDOW_REFERENCE.getWindow() );
        
        if ( limitsSetter != null ) {
            if ( rows != null && rows.length > 0 ) {
                for ( final int row : rows ) {
                    final CorrectorSupply supply = CORRECTOR_TABLE_MODEL.getRecordAtRow( row );
                    limitsSetter.setCorrectorLimits( supply );
                }
                if ( rows.length > 0 )  CORRECTOR_TABLE_MODEL.fireTableRowsUpdated( rows[0], rows[rows.length - 1] );
            }
            else {
                final int rowCount = CORRECTOR_TABLE_MODEL.getRowCount();
                for ( int row = 0 ; row < rowCount ; row++ )  {
                    final CorrectorSupply supply = CORRECTOR_TABLE_MODEL.getRecordAtRow( row );
                    limitsSetter.setCorrectorLimits( supply );
                }
                if ( rowCount > 0 )  CORRECTOR_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
            }
            
            postModification();
        }
	}
	
	
	/** Request from the user for a common custom field to apply to the correctors at the specified rows */
	private void requestCustomFieldForCorrectorsAtRows( final int[] rows ) {
        final String fieldString = JOptionPane.showInputDialog( WINDOW_REFERENCE.getWindow(), "Common field (T) to apply to correctors.", 0.0 );
        if ( fieldString != null ) {
            try {
                boolean bendsInSelection = false;   // indicates whether bends were in the user selection as they are not supported for bulk field setting
                
                final double field = Double.parseDouble( fieldString );
                final int rowCount = CORRECTOR_TABLE_MODEL.getRowCount();
                
                if ( rows != null && rows.length > 0 ) {
                    for ( final int row : rows ) {
                        final CorrectorSupply supply = CORRECTOR_TABLE_MODEL.getRecordAtRow( row );
                        if ( supply.isCorrectorSupply() ) {     // do not allow bends to be set in bulk as this would likely trip the machine
                            supply.requestFieldSetting( field );
                        }
                        else {
                            bendsInSelection = true;
                        }
                    }
                }
                else {  // no rows selected so update all correctors
                    for ( int row = 0 ; row < rowCount ; row++ )  {
                        final CorrectorSupply supply = CORRECTOR_TABLE_MODEL.getRecordAtRow( row );
                        if ( supply.isCorrectorSupply() ) {     // only true correctors (not bends) should have their field modified
                            supply.requestFieldSetting( field );
                        }
                    }
                }
                
                Channel.flushIO();
                Thread.sleep( 2000 );   // wait two seconds for the fields to update
                if ( rowCount > 0 )  CORRECTOR_TABLE_MODEL.fireTableRowsUpdated( 0, rowCount - 1 );
                
                if ( bendsInSelection ) {
                    JOptionPane.showMessageDialog( WINDOW_REFERENCE.getWindow(), "Bends were in your selection, but only true dipole correctors support batch field setting. No bend fields were changed.", "Field Setting Warning", JOptionPane.WARNING_MESSAGE );
                }
            }
            catch ( NumberFormatException exception ) {
                JOptionPane.showMessageDialog( WINDOW_REFERENCE.getWindow(), "Error parsing field setpoint as a real number: " + fieldString, "Field Setpoint Error", JOptionPane.ERROR_MESSAGE );
            }
            catch ( Exception exception ) {
                JOptionPane.showMessageDialog( WINDOW_REFERENCE.getWindow(), "Exception apply field setpoint to correctors: " + exception.getMessage(), "Field Setpoint Error", JOptionPane.ERROR_MESSAGE );
            }
        }
	}
	
	
	/** Make the table of objectives. */
	protected void makeObjectivesView( final WindowReference windowReference ) {
		final KeyValueTableModel<OrbitObjective> tableModel = new KeyValueTableModel<OrbitObjective>();
		tableModel.setKeyPaths( "enabled", "name" );
		
		tableModel.setColumnEditable( "enabled", true );
		
		tableModel.setColumnName( "enabled", "Use" );
		
		tableModel.setColumnClass( "enabled", Boolean.class );
		tableModel.setColumnClass( "name", String.class );
		
		final List<OrbitObjective> objectives = ( FLATTENER != null ) ? FLATTENER.getObjectives() : new ArrayList<OrbitObjective>();
		tableModel.setRecords( objectives );

		final JTable objectivesTable = (JTable)windowReference.getView( "ObjectivesTable" );
		objectivesTable.setModel( tableModel );
						
		// squeeze the checkbox column
		final TableColumn useColumn = objectivesTable.getColumnModel().getColumn( 0 );
		useColumn.setMaxWidth( new JLabel(" Use ").getPreferredSize().width );
	}
	
	
	/** Make the button row. */
	protected void makeBottomButtonRow( final WindowReference windowReference ) {
		final Box buttonRow = new Box( BoxLayout.X_AXIS );
		
		buttonRow.add( Box.createHorizontalGlue() );
		
		_flattenButton = (JButton)windowReference.getView( "FlattenRunButton" );
		_stopButton = (JButton)windowReference.getView( "FlattenStopButton" );
		_applyButton = (JButton)windowReference.getView( "FlattenApplyButton" );
		
		_stopButton.addActionListener( new ActionListener()  {
			public void actionPerformed( final ActionEvent event ) {
				FLATTENER.stopFlattening();
				_stopButton.setEnabled( false );
			}
		});
		
		_flattenButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _selectedOrbitSource != null ) {
					System.out.println( "flattening the orbit..." );
					flatten( _selectedOrbitSource.getOrbit() );
				}
				else {
					final String message = "No orbit selected for flattening.";
					final String title = "No Orbit Selected";
					JOptionPane.showMessageDialog( (JFrame)WINDOW_REFERENCE.getWindow(), message, title, JOptionPane.WARNING_MESSAGE ); 
				}
			}
		});
		
		_applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _correctionDialog == null ) {
					_correctionDialog = new CorrectionDialog( (JFrame)WINDOW_REFERENCE.getWindow(), ORBIT_MODEL );
				}
				_correctionDialog.displayNearOwner();
			}
		});
	}

	
	/**
	 * Flatten the selected orbit.
	 * @param orbit the orbit to flatten
	 */
	protected void flatten( final Orbit orbit ) {
		_flattenButton.setEnabled( false );
		_stopButton.setEnabled( true );
		_applyButton.setEnabled( false );
		
		FLATTENER.clearSolver();
		
		final OrbitSource referenceSource = _referenceOrbitSourceListModel.getSelectedOrbitSource();
		final Orbit orbitError = referenceSource != null ? Orbit.calcDifference( orbit, referenceSource.getOrbit() ) : orbit;
		
		if ( _predictedOrbitSource == null ) {
			_predictedOrbitSource = new SnapshotOrbitSource( "Predicted Orbit Error", orbitError );
		}
		else {
			_predictedOrbitSource.setSnapshot( orbitError );
		}
		ORBIT_MODEL.addOrbitSource( _predictedOrbitSource );
		
		updateProgress();
		
		startProgressTimer();
		spawnFlatten( orbitError );
	}
	
	
	/**
	 * Spawn the flatten thread.
	 * @param orbit the orbit to flatten
	 */
	private void spawnFlatten( final Orbit orbit ) {
		new Thread( new Runnable() {
			public void run() {
				try {
					if ( !FLATTENER.flatten( orbit ) ) {
						_applyButton.setEnabled( false );
					}
					else {
						_applyButton.setEnabled( true );
					}
				}
				catch( Exception exception ) {
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log( Level.SEVERE, "Exception flattening the orbit.", exception );
					exception.printStackTrace();
					final String message = exception.getMessage();
					final String title = "Exception Flattening the Orbit";
					JOptionPane.showMessageDialog( (JFrame)WINDOW_REFERENCE.getWindow(), message, title, JOptionPane.ERROR_MESSAGE );
				}
				finally {
					_stopButton.setEnabled( false );
					_flattenButton.setEnabled( true );
					_progressTimer.stop();
				}
			}
		}).start();
	}
	
	
	/** Start the progress timer. */
	protected void startProgressTimer() {
		if ( _progressTimer == null ) {
			_progressTimer = new Timer( 1000, new ActionListener() {
				public void actionPerformed( final ActionEvent event ) {
					updateProgress();
				}
			});
			
			_progressTimer.setRepeats( true );
			_progressTimer.start();
		}
		else {
			_progressTimer.restart();
		}
	}
	
	
	/**
	 * Update the feedback to reflect the flattening progress.
	 */
	protected void updateProgress() {
		_progressBar.setValue( (int) ( MAX_PROGRESS * FLATTENER.getFractionComplete() ) );
	}
	
	
	/** reload the Beam Position Monitors */
	private void refreshBeamPositionMonitors() {
		final List<BpmAgent> bpmAgents = ( FLATTENER != null ) ? FLATTENER.getBPMAgents() : new ArrayList<BpmAgent>();
		BPM_TABLE_MODEL.setRecords( bpmAgents );
	}
	
	
	/** reload the Beam Position Monitors */
	private void refreshCorrectors() {
		final List<CorrectorSupply> supplies = ( FLATTENER != null ) ? FLATTENER.getCorrectorSupplies() : new ArrayList<CorrectorSupply>();
		CORRECTOR_TABLE_MODEL.setRecords( supplies );
	}
	
	
	/** sequence changed */
	public void simulatorChanged( final Flattener source, final MachineSimulator simulator ) {
		refreshBeamPositionMonitors();
		refreshCorrectors();
		
		if ( simulator instanceof OnlineModelSimulator ) {
			_simulatorMenu.setSelectedIndex( ONLINE_MODEL_SIMULATOR_INDEX );
		}
		else if ( simulator instanceof EmpiricalSimulator ) {
			_simulatorMenu.setSelectedIndex( EMPIRICAL_SIMULATOR_INDEX );
		}
		else {
			_simulatorMenu.setSelectedIndex( -1 );
		}		
	}
	
	
	/**
	 * solving time changed
	 * @param source       the flattener whose solving time changed
	 * @param solvingTime  new solving time in seconds
	 */
	public void solvingTimeChanged( final Flattener source, final double solvingTime ) {
		_flattenDurationSpinner.setValue( new Double( solvingTime ) );
	}
	
	
	/** sequence changed */
	public void sequenceChanged( final Flattener source, final AcceleratorSeq sequence ) {
		refreshBeamPositionMonitors();
		refreshCorrectors();
	}
	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( final OrbitModel model, final List<BpmAgent> bpmAgents ) {}
	
	
	/** BPMs changed */
	public void bpmsChanged( final Flattener source, final List<BpmAgent> bpms ) {
		refreshBeamPositionMonitors();
	}
	
	
	/** Corrector supplies changed */
	public void correctorSuppliesChanged( final Flattener source, final List<CorrectorSupply> supplies ) {
		refreshCorrectors();
	}
	
	
	/** New optimal orbit found */
	public void newOptimalOrbit( final Flattener source, final Orbit orbit ) {
		_predictedOrbitSource.setSnapshot( orbit );
	}
	
	
	/** indicates that the flattener progress has been updated */
	public void progressUpdated( Flattener source, double fractionComplete, String message ) {
		_progressBar.setString( message );
		_progressBar.setValue( (int) ( MAX_PROGRESS * fractionComplete ) );
	}
	
	
	/**
	 * Notification that the sequence has changed.
	 * @param  model        the model sending the notification
	 * @param  sequence  the new accelerator sequence
	 */
	public void sequenceChanged( OrbitModel model, AcceleratorSeq sequence ) {
		FLATTENER.setSequence( sequence, model.getBPMAgents(), model.getCorrectorSupplies() );
	}
	
	
	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param  model           the model sending the notification
	 * @param  newOrbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( OrbitModel model, OrbitSource newOrbitSource ) {
	}
	
	
	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( OrbitModel model, OrbitSource orbitSource ) {
	}
}



/** controller for the dialog which allows the user to supply common custom corrector limits */
class BulkCorrectorLimitsController {
    /** status for user cancel */
    static final private int CANCEL_STATUS = 0;
    
    /** status for user selection of percent limits */
    static final private int PERCENT_LIMITS_STATUS = 1;
    
    /** status for user selection of absolute limits */
    static final private int ABSOLUTE_LIMITS_STATUS = 2;
    
	/** window reference containing this controller's views */
	final private WindowReference WINDOW_REFERENCE;
    
    /** status when closed */
    private int _status;
    
    
    /** Constructor */
    private BulkCorrectorLimitsController( final JFrame owner ) {
        _status = CANCEL_STATUS;
        
        WINDOW_REFERENCE = Application.getAdaptor().getDefaultWindowReference( "BulkCorrectorLimitsDialog", owner );
        final JDialog dialog = (JDialog)WINDOW_REFERENCE.getWindow();
        
        final JButton percentLimitsApplyButton = (JButton)WINDOW_REFERENCE.getView( "PercentLimitsApplyButton" );
        percentLimitsApplyButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                _status = PERCENT_LIMITS_STATUS;
                dialog.setVisible( false );
                
            }
        });
        
        final JButton absoluteLimitsApplyButton = (JButton)WINDOW_REFERENCE.getView( "AbsoluteLimitsApplyButton" );
        absoluteLimitsApplyButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                _status = ABSOLUTE_LIMITS_STATUS;
                dialog.setVisible( false );
            }
        });
        
        dialog.setLocationRelativeTo( owner );
        dialog.setVisible( true );
    }
    
    
    /** show the dialog and return the selected limits setter if any */
    static public CorrectorLimitsSetting showDialog( final JFrame owner ) {
        final BulkCorrectorLimitsController controller = new BulkCorrectorLimitsController( owner );
        return controller.getSelectedLimitsSetter();
    }
    
    
    /** get the selected limits setter if any */
    private CorrectorLimitsSetting getSelectedLimitsSetter() {
        switch( _status ) {
            case PERCENT_LIMITS_STATUS:
                final JSlider percentLimitsSlider = (JSlider)WINDOW_REFERENCE.getView( "PercentLimitsSlider" );
                final int percent = percentLimitsSlider.getValue();
                return new CorrectorPercentOperationalLimitsSetter( percent );
            case ABSOLUTE_LIMITS_STATUS:
                try {
                    final JTextField lowerLimitField = (JTextField)WINDOW_REFERENCE.getView( "Lower Limit Field" );
                    final JTextField upperLimitField = (JTextField)WINDOW_REFERENCE.getView( "Upper Limit Field" );
                    final String lowerLimitText = lowerLimitField.getText();
                    final String upperLimitText = upperLimitField.getText();
                    final double lowerLimit = Double.parseDouble( lowerLimitText );
                    final double upperLimit = Double.parseDouble( upperLimitText );
                    return new CorrectorAbsoluteLimitsSetter( lowerLimit, upperLimit );
                }
                catch( Exception exception ) {
                    exception.printStackTrace();
                    final JDialog dialog = (JDialog)WINDOW_REFERENCE.getWindow();
                    final java.awt.Window owner = dialog.getOwner();
                    
                    if ( owner != null ) {
                        JOptionPane.showMessageDialog( owner, "Exception attempting to set limits: " + exception.getMessage(), "Limits Setting Error", JOptionPane.ERROR_MESSAGE );
                    }
                    else {
                        Application.displayError( "Limits Setting Error", "Exception attempting to set limits", exception );
                    }
                    return null;
                }
            default:
                return null;
        }
    }
}



/** Interface for setting the corrector limits */
interface CorrectorLimitsSetting {
    /** set the custom corrector limits for the specified supply */
    public void setCorrectorLimits( final CorrectorSupply supply );
}



/** Sets corrector limits based on a percent of the operational limits */
class CorrectorPercentOperationalLimitsSetter implements CorrectorLimitsSetting {
    /** fraction of the operations limits to set as custom limits */
    final private double FRACTION;
    
    
    /** Constructor */
    public CorrectorPercentOperationalLimitsSetter( final int percent ) {
        FRACTION = ((double)percent) / 100.0;
    }
    
    
    /** set the custom corrector limits for the specified supply */
    public void setCorrectorLimits( final CorrectorSupply supply ) {
        supply.setOperationalFieldLimitsFraction( FRACTION );
    }
}



/** Sets corrector limits using absolute custom limits */
class CorrectorAbsoluteLimitsSetter implements CorrectorLimitsSetting {
    /** lower limit */
    final private double LOWER_LIMIT;
    
    /** upper limit */
    final private double UPPER_LIMIT;
    
    
    /** Constructor */
    public CorrectorAbsoluteLimitsSetter( final double lowerLimit, final double upperLimit ) {
        LOWER_LIMIT = lowerLimit;
        UPPER_LIMIT = upperLimit;
    }
    
    
    /** set the custom corrector limits for the specified supply */
    public void setCorrectorLimits( final CorrectorSupply supply ) {
        supply.setLowerFieldLimit( LOWER_LIMIT );
        supply.setUpperFieldLimit( UPPER_LIMIT );
    }
}

