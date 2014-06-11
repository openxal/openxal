//
//  EnergyManagerWindow.java
//  xal
//
//  Created by Thomas Pelaia on 2/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.model.probe.*;
import xal.model.alg.*;
import xal.tools.data.*;
import xal.extension.widgets.apputils.SimpleProbeEditor;
//import xal.tools.apputils.NumericCellRenderer;
import xal.tools.apputils.files.RecentFileTracker;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.*;


/** Main window. */
public class EnergyManagerWindow extends AcceleratorWindow implements EnergyManagerListener, SwingConstants {
	
    private static final long serialVersionUID = 1L;
    
    /** date format for message view */
	final static private SimpleDateFormat MESSAGE_DATE_FORMAT;
	
	/** table of parameters */
	final private JTable _parameterTable;
		
	/** table model for displaying the live parameters */
	final private LiveParameterTableModel _parameterTableModel;
	
	/** view for displaying messages */
	final private JLabel _messageView;
	
	/** live parameter view */
	private LiveParameterInspector _parameterInspector;
	
	/** sort ordering for ordering the live parameters */
	private SortOrdering _parameterOrdering;
	
	/** dialog for configuring a solve */
	protected SolverConfigDialog _solverConfigDialog;
	
	/** dialog for specifying the evaluaton node range in terms of position */
	protected EvaluationRangeDialog _evaluationRangeDialog;
	
	/** a dialog for specifying simple energy scaling information */
	protected SimpleEnergyScaleDialog _energyScaleDialog;
	
	/** optimizer window */
	protected OptimizerWindow _optimizerWindow;
	
	/** optimal results file chooser */
	protected JFileChooser _exportsFileChooser;
	
	/** file tracker to keep track of file exports */
	protected RecentFileTracker _exportsTracker;
	
	/** file imports file chooser */
	protected JFileChooser _importsFileChooser;
	
	/** file tracker to keep track of file imports */
	protected RecentFileTracker _importsTracker;
	
	
	// static initializer
	static {
		MESSAGE_DATE_FORMAT = new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" );
	}
	
	
    /** Constructor */
    public EnergyManagerWindow( final XalDocument aDocument ) {
        super( aDocument );
		
		_parameterOrdering = new SortOrdering( NodeAgent.POSITION_KEY );
				
		_parameterTableModel = new LiveParameterTableModel();
		_parameterTable = new JTable( _parameterTableModel );
		
		_messageView = new JLabel( "messages..." );
		
		makeContents();
		
		setupParameterTable();
    }

    
    /**
	 * Overrides the super class method to disable the toolbar.
	 * @return false
     */
    public boolean usesToolbar() {
        return false;
    }
	
	
	/**
	 * Get the energy manager document.
	 * @return this window's document as an energy manager document
	 */
	protected EnergyManagerDocument getEnergyManagerDocument() {
		return (EnergyManagerDocument)document;
	}
	
	
	/**
	 * Get the energy manager model.
	 * @return the energy manager model
	 */
	protected EnergyManager getModel() {
		return getEnergyManagerDocument().getModel();
	}
	
	
	/** Make the window contents */
	private void makeContents() {
        setSize( 1200, 900 );
		
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( makeQualifierView() );
		mainView.add( new JScrollPane( _parameterTable ) );
		mainView.add( makeMessageView() );
	}
	
	
    /**
	 * Register actions for the custom menu items.
     * @param commander The commander with which to register the custom commands.
     */
    protected void customizeCommands( final Commander commander ) {
        final Action specifyEvaluationRangeAction = new AbstractAction( "specify-evaluation-range" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				if ( _evaluationRangeDialog == null ) {
					_evaluationRangeDialog = new EvaluationRangeDialog( EnergyManagerWindow.this );
				}
				
				final List<LiveParameter> selectedParameters = getSelectedParameters();
				double[] selectionRange = new double[] { 0.0, 0.0 };
				if ( selectedParameters.size() > 0 ) {
					selectionRange[0] = selectedParameters.get( 0 ).getPosition();
					selectionRange[1] = selectedParameters.get( selectedParameters.size() - 1 ).getPosition();
				}
				
				final double[] range = _evaluationRangeDialog.present( getEnergyManagerDocument().getModel().getEvaluationRange(), selectionRange );
				if ( range != null ) {
					try {
						getEnergyManagerDocument().getModel().setEvaluationRange( range[0], range[1] );						
					}
					catch( Exception exception ) {
						displayError( exception );
					}
				}
            }
        };
        commander.registerAction( specifyEvaluationRangeAction );
		
        final Action editEntranceProbeAction = new AbstractAction( "edit-entrance-probe" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				try {
					final SimpleProbeEditor probeEditor;
					final Probe probe = Probe.newProbeInitializedFrom( getModel().getEntranceProbe() );
//					final JDialog probeEditorDialog = probeEditor.createSimpleProbeEditor( probe );
                    probeEditor = new SimpleProbeEditor( EnergyManagerWindow.this , probe );
					final Tracker algorithm = (Tracker)probe.getAlgorithm();
					getModel().setEntranceProbe( probeEditor.getProbe() );
				}
				catch( Exception exception ) {
					displayError( exception );
				}
            }
        };
        commander.registerAction( editEntranceProbeAction );
		
        final Action importCavitySettingsAction = new AbstractAction( "import-cavity-settings" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				importRFCavitySettings();
            }
        };
        commander.registerAction( importCavitySettingsAction );
		
        final Action importQuadFieldsAction = new AbstractAction( "import-quad-fields" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				importQuadrupoleFields();
            }
        };
        commander.registerAction( importQuadFieldsAction );
		
        final Action applyBestLongitudinalGuessAction = new AbstractAction( "apply-best-longitudinal-guess" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				postMessage( "Begin calculating best longitudinal guess..." );
				getEnergyManagerDocument().getModel().guessRFPhaseToPreserveLongitudinalFocusing();
				postMessage( "Applied best longitudinal guess..." );
            }
        };
        commander.registerAction( applyBestLongitudinalGuessAction );
		
        final Action applyBestTransverseGuessAction = new AbstractAction( "apply-best-transverse-guess" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				applyBestTransverseGuess();
            }
        };
        commander.registerAction( applyBestTransverseGuessAction );
		
        final Action scaleMagnetsForEnergyChangeAction = new AbstractAction( "scale-magnets-for-energy-change" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            
            public void actionPerformed( final ActionEvent event ) {
                // kinetic energies are presented in MeV
				if ( _energyScaleDialog == null ) {
                    // use the default input kinetic energy in MeV as the default energy
                    final double defaultEnergy = 1.e-6 * OnlineModelSimulator.getDefaultEntranceKineticEnergy( getEnergyManagerDocument().getSelectedSequence() );
					_energyScaleDialog = new SimpleEnergyScaleDialog( EnergyManagerWindow.this, defaultEnergy );
				}
				if ( _energyScaleDialog.present() ) {
					try {
						final String source = _energyScaleDialog.getSource();
						final double initialKineticEnergy = _energyScaleDialog.getInitialKineticEnergy();
						final double targetKineticEnergy = _energyScaleDialog.getTargetKineticEnergy();
						getEnergyManagerDocument().getModel().scaleMagneticFieldsForEnergyChange( source, initialKineticEnergy, targetKineticEnergy );						
						postMessage( "Scaled magnets for the specified energy change..." );
					}
					catch( Exception exception ) {
						displayError( exception );
					}
				}
            }
        };
        commander.registerAction( scaleMagnetsForEnergyChangeAction );
		
        final Action importOptimalValuesAction = new AbstractAction( "import-optimal-values" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				getEnergyManagerDocument().getModel().importOptimalValues();
				postMessage( "Optimal values imported..." );
            }
        };
        commander.registerAction( importOptimalValuesAction );
		
        final Action uploadMagnetFieldsAction = new AbstractAction( "upload-magnet-fields" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				uploadMagnetFields();
            }
        };
        commander.registerAction( uploadMagnetFieldsAction );
		
		final Action exportParametersAction = new AbstractAction( "export-parameters" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				exportParameters();
			}
		};
		commander.registerAction( exportParametersAction );
		
		final Action exportOpticsAction = new AbstractAction( "export-optics" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				exportOpticsChanges();
			}
		};
		commander.registerAction( exportOpticsAction );
		
		final Action exportModelParamsAction = new AbstractAction( "export-model-params" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				exportModelParams();
			}
		};
		commander.registerAction( exportModelParamsAction );
		
        final Action makeSelectedVariableAction = new AbstractAction( "make-selected-variable" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				setAsVariable( getSelectedParameters(), true );
            }
        };
        commander.registerAction( makeSelectedVariableAction );
		
		final Action makeSelectedFixedAction = new AbstractAction( "make-selected-fixed" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				setAsVariable( getSelectedParameters(), false );
            }
        };
        commander.registerAction( makeSelectedFixedAction );
		
		final Action makeDisabledFixedAction = new AbstractAction( "make-disabled-fixed" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				getEnergyManagerDocument().getModel().freezeParametersOfDisabledCavities();
			}
		};
		commander.registerAction( makeDisabledFixedAction );
		
		final Action useSelectedDesignAction = new AbstractAction( "use-selected-design" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				setParameterSource( getSelectedParameters(), LiveParameter.DESIGN_SOURCE );
            }
        };
        commander.registerAction( useSelectedDesignAction );		
		
		final Action useSelectedControlAction = new AbstractAction( "use-selected-control" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				setParameterSource( getSelectedParameters(), LiveParameter.CONTROL_SOURCE );
            }
        };
        commander.registerAction( useSelectedControlAction );		
		
		final Action useSelectedCustomAction = new AbstractAction( "use-selected-custom" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				setParameterSource( getSelectedParameters(), LiveParameter.CUSTOM_SOURCE );
            }
        };
        commander.registerAction( useSelectedCustomAction );		
		
		final Action copySelectedDesignAction = new AbstractAction( "copy-selected-design" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				copyDesignSettings( getSelectedParameters() );
            }
        };
        commander.registerAction( copySelectedDesignAction );		
		
		final Action copySelectedControlAction = new AbstractAction( "copy-selected-control" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				copyControlSettings( getSelectedParameters() );
            }
        };
        commander.registerAction( copySelectedControlAction );		
		
		final Action copySelectedControlLimitsAction = new AbstractAction( "copy-selected-control-limits" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				copyControlLimits( getSelectedParameters() );
            }
        };
        commander.registerAction( copySelectedControlLimitsAction );		
		
		final Action specifySelectedRelativeCustomLimitsAction = new AbstractAction( "set-relative-custom-limits" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				specifyRelativeCustomLimits( getSelectedParameters() );
            }
        };
        commander.registerAction( specifySelectedRelativeCustomLimitsAction );		
		
		final Action solveAction = new AbstractAction( "solve" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				showSolverConfigDialog();
            }
        };
        commander.registerAction( solveAction );		
		
		final Action evaluateAction = new AbstractAction( "evaluate" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed( final ActionEvent event ) {
				getModel().getOptimizer().evaluateInitialPoint();
            }
        };
        commander.registerAction( evaluateAction );		
		
		final Action solvingProgressAction = new AbstractAction( "optimization-progress" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            
            public void actionPerformed( final ActionEvent event ) {
				showOptimizerWindow();
			}
        };
        commander.registerAction( solvingProgressAction );
		
		final Action exportOptimalResultsAction = new AbstractAction( "export-optimal-results" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				exportOptimalResults();
			}
		};
		commander.registerAction( exportOptimalResultsAction );
		
		final Action exportTwissAction = new AbstractAction( "export-twiss" ) {
            
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
			public void actionPerformed( final ActionEvent event ) {
				exportTwiss();
			}
		};
		commander.registerAction( exportTwissAction );
	}
	
	
	/** upload quadrupole fields to the control system */
	protected void applyBestTransverseGuess() {
		final String ALL_MAGNETS = "Apply to all Magnetic Fields";
		final String SELECTED_MAGNETS = "Apply to selected Magnetic Fields";
		final String[] OPTIONS = { ALL_MAGNETS, SELECTED_MAGNETS };
		final Object selection = JOptionPane.showInputDialog( this, "Apply best transverse guess to magnets?", "Best Transverse Guess", JOptionPane.QUESTION_MESSAGE, null, OPTIONS, ALL_MAGNETS );
		
		if ( selection == null )  return;	// nothing left to do
		
		// make sure we only scale magnet fields
		final Qualifier qualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, ElectromagnetFieldAdaptor.NAME );
		List<LiveParameter> magnetParameters;
		if ( selection == ALL_MAGNETS ) {
			magnetParameters = getEnergyManagerDocument().getModel().getLiveParameters( qualifier );
		}
		else if ( selection == SELECTED_MAGNETS ) {
			magnetParameters = EnergyManager.getFilteredLiveParameters( getSelectedParameters(), qualifier );
		}
		else {
			return;
		}
		
		postMessage( "Begin calculating best transverse guess..." );		
		getEnergyManagerDocument().getModel().scaleMagnetFieldsToEnergy( magnetParameters );
		postMessage( "Applied best transverse guess..." );
	}
	
	
	/** upload quadrupole fields to the control system */
	protected void uploadMagnetFields() {
		final String UPLOAD_ALL = "Upload All Magnetic Fields";
		final String UPLOAD_SELECTED = "Upload Selected Magnetic Fields";
		final String[] OPTIONS = { UPLOAD_ALL, UPLOAD_SELECTED };
		final Object selection = JOptionPane.showInputDialog( this, "Upload quadrupole fields to the accelerator? \n Please take a SCORE save set before continuing!", "Magnetic Field Upload", JOptionPane.QUESTION_MESSAGE, null, OPTIONS, UPLOAD_SELECTED );
		
		if ( selection == null )  return;	// nothing left to do
		
		// make sure we only upload magnet fields
		final Qualifier qualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, ElectromagnetFieldAdaptor.NAME );
		List<LiveParameter> parameters;
		if ( selection == UPLOAD_ALL ) {
			parameters = getEnergyManagerDocument().getModel().getLiveParameters( qualifier );
		}
		else if ( selection == UPLOAD_SELECTED ) {
			parameters = EnergyManager.getFilteredLiveParameters( getSelectedParameters(), qualifier );
		}
		else {
			return;
		}
		
		final int[] result = EnergyManager.uploadInitialValues( parameters );
		if ( result[0] != result[1] ) {
			final int numFailed = result[1] - result[0];
			displayWarning( "Some update requests failed", numFailed + " of " + result[1] + " requests failed! \nSee log for details..."  );
		}
		else {
			JOptionPane.showMessageDialog( this, result[0] + " magnet field upload requests performed! \n Please take a SCORE save set!" );
		}
	}
	
	
	/** 
	 * Copy the design initial value and limits of selected variables to their custom limits. 
	 * @param parameters the parameters on which to perform the action
	 */
	protected void copyDesignSettings( final List<LiveParameter> parameters ) {
		final int count = parameters.size();
		
		for ( int index = 0 ; index < count ; index++ ) {
			final LiveParameter parameter = parameters.get( index );
			parameter.copyDesignToCustom();
		}
	}
	
	
	/** 
	 * Copy the control initial value and limits of selected variables to their custom limits. 
	 * @param parameters the parameters on which to perform the action
	 */
	protected void copyControlSettings( final List<LiveParameter> parameters ) {
		final int count = parameters.size();
		
		for ( int index = 0 ; index < count ; index++ ) {
			final LiveParameter parameter = parameters.get( index );
			parameter.copyControlToCustom();
		}
	}
	
	
	/** 
	 * Copy the control limits of selected variables to their custom limits. 
	 * @param parameters the parameters on which to perform the action
	 */
	protected void copyControlLimits( final List<LiveParameter> parameters ) {
		final int count = parameters.size();
		
		for ( int index = 0 ; index < count ; index++ ) {
			final LiveParameter parameter = parameters.get( index );
			parameter.copyControlLimitsToCustom();
		}
	}
	
	
	/** 
	 * Specify custom limits as a percent about the current custom value. 
	 * @param parameters the parameters on which to perform the action
	 */
	protected void specifyRelativeCustomLimits( final List<LiveParameter> parameters ) {
		final int count = parameters.size();
		
		if ( count > 0 ) {
			final String response = JOptionPane.showInputDialog( this, "Enter the relative custom limits (%) about the initial value.", "10" );
			
			if ( response != null ) {
				try {
					final double relativeLimit = Double.parseDouble( response ) / 100.0;
					for ( int index = 0 ; index < count ; index++ ) {
						final LiveParameter parameter = parameters.get( index );
						parameter.setRelativeCustomLimits( relativeLimit );
					}
				}
				catch ( NumberFormatException exception ) {
					exception.printStackTrace();
				}
			}			
		}		
	}
	
	
	/**
	 * Set the selected parameters as variable or fixed depending on the value passed.
	 * @param parameters the parameters on which to perform the action
	 * @param shouldVary true for variable and false for fixed
	 */
	protected void setAsVariable( final List<LiveParameter> parameters, final boolean shouldVary ) {
		final int count = parameters.size();
		
		for ( int index = 0 ; index < count ; index++ ) {
			final LiveParameter parameter = parameters.get( index );
			parameter.setIsVariable( shouldVary );
		}
	}
	
	
	/**
	 * Set the parameters to have the specified source.
	 * @param parameters the parameters on which to perform the action
	 * @param source DESIGN_SOURCE, CONTROL_SOURCE or CUSTOM_SOURCE
	 */
	protected void setParameterSource( final List<LiveParameter> parameters, final int source ) {
		final int count = parameters.size();
		
		for ( int index = 0 ; index < count ; index++ ) {
			final LiveParameter parameter = parameters.get( index );
			parameter.setActiveSource( source );
		}
	}
	
	
	/** 
	 * Choose the file to which to export the results. No file name is assigned by default.
	 * @return the file to which to export results
	 */
	protected File chooseExportFile() {
        return chooseExportFile( null );
    }
	
	
	/** 
	 * Choose the file to which to export the results.
     * @param defaultFileName the default name of the file to assign the export
	 * @return the file to which to export results
	 */
	protected File chooseExportFile( final String defaultFileName ) {
		if ( _exportsTracker == null ) {
			_exportsTracker = new RecentFileTracker( 1, this.getClass(), "EXPORT_URL" );
		}
		
		if ( _exportsFileChooser == null ) {
			_exportsFileChooser = new JFileChooser();
			_exportsTracker.applyRecentFolder( _exportsFileChooser );
		}
        
        // if a default file name is specified then set it in the file chooser relative to the current selection directory if any
        if ( defaultFileName != null ) {
            try {
                final File currentSelection = _exportsFileChooser.getSelectedFile();
                final File defaultDirectory = currentSelection != null ? currentSelection.getParentFile() : null;
                final File defaultFile = new File( defaultDirectory, defaultFileName );
                _exportsFileChooser.setSelectedFile( defaultFile );
            }
            catch ( Exception exception ) {
                exception.printStackTrace();
            }
        }
		
		final int status = _exportsFileChooser.showSaveDialog( this );
		
		switch ( status ) {
			case JFileChooser.APPROVE_OPTION:
				break;
			default:
				return null;
		}
		
		final File file = _exportsFileChooser.getSelectedFile();
		
		if ( file.exists() ) {
			final int continueStatus = JOptionPane.showConfirmDialog( this, "The file: \"" + file.getPath() + "\" already exits!\n Overwrite this file?" );
			switch ( continueStatus ) {
				case JOptionPane.YES_OPTION:
					break;							// return the file to which to export
				case JOptionPane.NO_OPTION:
					return chooseExportFile();
				default:
					return null;							// cancel the operation entirely
			}
 		}
		
		_exportsTracker.cacheURL( file );		// remember where we put this file for next time
		
		return file;
	}
    
    
    /** Get the sequence name */
    private String getSequenceName() {
        return getEnergyManagerDocument().getSelectedSequence().getId();
    }
	
	
	/** Export the current parameters. */
	protected void exportParameters() {
		final File file = chooseExportFile( getSequenceName() + ".txt" );
		
		if ( file == null )  return;
		
		try {
			final Writer writer = new FileWriter( file );
			getEnergyManagerDocument().getModel().exportInitialParameters( writer );
			writer.flush();
		}
		catch ( java.io.IOException exception ) {
			displayError( "Write exception", "Error writing out parameters.", exception );
		}		
	}
	
	
	/** Generate a new optics extra input file with the current parameter changes from the design. */
	protected void exportOpticsChanges() {
		final File file = chooseExportFile( getSequenceName() + ".xdxf" );
		
		if ( file == null )  return;
		
		try {
            final FileWriter writer = new FileWriter( file );
			OpticsExporter.exportChanges( getModel(), writer );
            writer.close();
		}
		catch ( java.io.IOException exception ) {
			displayError( "Write exception", "Error exporting the optics changes.", exception );
		}
		catch( Exception exception ) {
			displayError( "Export exception", "Error exporting the optics changes.", exception );
		}
	}
	
	
	/** Export model input parameters. */
	protected void exportModelParams() {
		final File file = chooseExportFile( "untitled.params" );
		
		if ( file == null )  return;
		
		try {
			getEnergyManagerDocument().getModel().exportModelInputParameters( file.toURI().toURL() );
		}
		catch ( java.net.MalformedURLException exception ) {
			displayError( "URL Exception", "Could not generate a URL for the specified file.", exception );
		}
		catch( Exception exception ) {
			displayError( "Export exception", "Error exporting the model input parameters.", exception );
		}
	}
	
	
	/** Export the optimal results to the user selected file. */
	protected void exportOptimalResults() {
		if ( getEnergyManagerDocument().getModel().canExportOptimalResults() ) {
			final File file = chooseExportFile();
			
			if ( file == null )  return;
			
			try {
				final Writer writer = new FileWriter( file );
				getEnergyManagerDocument().getModel().exportOptimalResults( writer );
				writer.flush();
			}
			catch ( java.io.IOException exception ) {
				displayError( "Write exception", "Error writing out optimization/evaluation results.", exception );
			}
			catch( Exception exception ) {
				displayError( "Export exception", "Error exporting optimization/evaluation results.", exception );
			}
		}
		else {
			displayWarning( "Cannot Export Results", "There are no results to export since no optimization/evaluation has been run." );
		}
	}
	
	
	/** Export the twiss parameters to the user selected file. */
	protected void exportTwiss() {
		if ( getEnergyManagerDocument().getModel().canExportTwiss() ) {
			final File file = chooseExportFile();
			
			if ( file == null )  return;
			
			try {
				final Writer writer = new FileWriter( file );
				getEnergyManagerDocument().getModel().exportTwiss( writer );
				writer.flush();
			}
			catch ( java.io.IOException exception ) {
				displayError( "Write exception", "Error writing out optimization/evaluation twiss parameters.", exception );
			}
			catch( Exception exception ) {
				displayError( "Export exception", "Error exporting optimization/evaluation twiss parameters.", exception );
			}
		}
		else {
			displayWarning( "Cannot Export Results", "There are no results to export since no optimization/evaluation has been run." );
		}
	}
	
	
	/** 
	 * Allow the user to choose the import file.
	 * @return the file to import
	 */
	protected File chooseImportFile() {
		if ( _importsTracker == null ) {
			_importsTracker = new RecentFileTracker( 1, this.getClass(), "IMPORT_URL" );
		}
		
		if ( _importsFileChooser == null ) {
			_importsFileChooser = new JFileChooser();
			_importsFileChooser.setMultiSelectionEnabled( false );
			_importsTracker.applyRecentFolder( _importsFileChooser );
		}
		
		final int status = _importsFileChooser.showOpenDialog( this );
		
		switch ( status ) {
			case JFileChooser.APPROVE_OPTION:
				break;
			default:
				return null;
		}
		
		final File file = _importsFileChooser.getSelectedFile();
		_importsTracker.cacheURL( file );	// remember where to find this file for next time
		
		return file;		
	}
	
	
	/** Import the RF Amplitude and phase settings from a file selected by the user */
	protected void importRFCavitySettings() {
		final File file = chooseImportFile();
		
		if ( file == null )  return;
		
		final Map<String, Map<String, Object>> amplitudeTable = new HashMap<>();
		final Map<String, Map<String, Object>> phaseTable = new HashMap<>();
		
		// file format should be a line for each cavity:  cavityID	amplitude phase
		try {
			final BufferedReader reader = new BufferedReader( new FileReader( file ) );
			while ( reader.ready() ) {
				try {
					final String line = reader.readLine();					
					final StringTokenizer tokenizer = new StringTokenizer( line );
					if ( tokenizer.hasMoreTokens() ) {
						try {
							// parse the cavity ID
							final String cavityID = tokenizer.nextToken();
							
							// parse the cavity field
							if ( tokenizer.hasMoreTokens() ) {
								final double amplitude = Double.parseDouble( tokenizer.nextToken() );	// this is both the maximum value and the limit
								final double[] limits = new double[] { 0.0, amplitude };
								final Map <String, Object> fieldSetting = new HashMap<>();
								fieldSetting.put( "value", amplitude );
								fieldSetting.put( "limits", limits );
								amplitudeTable.put( cavityID, fieldSetting );
								
								// parse the cavity average phase
								if ( tokenizer.hasMoreTokens() ) {
									final double rawCavityPhase = Double.parseDouble( tokenizer.nextToken() );
									final double cavityPhase = constrainPhaseDegrees( rawCavityPhase );
									/*final RFCavityAgent cavityAgent = (RFCavityAgent)getEnergyManagerDocument().getModel().getNodeAgentWithID( cavityID );
									final double cavityPhase = cavityAgent.toCavityPhaseFromAverage( averagePhase, amplitude ); */
			
									final Map<String, Object> phaseSetting = new HashMap<>();
									phaseSetting.put( "value", cavityPhase );
									//phaseSetting.put( "value", averagePhase );
									phaseTable.put( cavityID, phaseSetting );
								}
							}
						}
						catch ( NumberFormatException exception ) { displayError( exception ); }
					}
				}
				catch ( java.io.IOException exception ) { displayError( exception ); }
			}
			
			final Qualifier amplitudeQualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, RFCavityAgent.AMPLITUDE_ADAPTOR.getName() );
			final List<LiveParameter> amplitudeParameters = getEnergyManagerDocument().getModel().getLiveParameters( amplitudeQualifier );
			getEnergyManagerDocument().getModel().loadCustomSettings( amplitudeTable, amplitudeParameters );
			final Qualifier phaseQualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, RFCavityAgent.PHASE_ADAPTOR.getName() );
			final List<LiveParameter> phaseParameters = getEnergyManagerDocument().getModel().getLiveParameters( phaseQualifier );
			getEnergyManagerDocument().getModel().loadCustomSettings( phaseTable, phaseParameters );
			postMessage( "RF Amplitude and Phase settings imported from:  " + file.getCanonicalPath() );
		}
		catch ( java.io.FileNotFoundException exception ) { displayError( exception ); }
		catch ( java.io.IOException exception ) { displayError( exception ); }
	}
	
	
	/** import quadrupole fields from a file */
	protected void importQuadrupoleFields() {
		final File file = chooseImportFile();
		
		if ( file == null )  return;
		
		final Map<String,Map<String,Object>> fieldTable = new HashMap<String,Map<String,Object>>();
		
		// file format should be a line for each quadrupole:  quadrupoleID	field
		try {
			final BufferedReader reader = new BufferedReader( new FileReader( file ) );
			while ( reader.ready() ) {
				try {
					final String line = reader.readLine().trim();
					if ( line == null || line == "" || line.startsWith( "#" ) )  continue;
					final String[] tokens = line.split( "\\s+" );
					
					if ( tokens.length >= 2 ) {
						try {
							// parse the quadrupole ID
							final String quadID = tokens[0];
							
							final Map<String, Object> settings = new HashMap<>();
							// parse the quadrupole field
							final double field = Double.parseDouble( tokens[1] );
							settings.put( "value", field );
							fieldTable.put( quadID, settings );							
						}
						catch ( NumberFormatException exception ) { displayError( exception ); }
					}
				}
				catch ( java.io.IOException exception ) { displayError( exception ); }
			}
			
			final Qualifier fieldQualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, QuadAgent.FIELD_ADAPTOR.getName() );
			final List<LiveParameter> fieldParameters = getEnergyManagerDocument().getModel().getLiveParameters( fieldQualifier );
			getEnergyManagerDocument().getModel().loadCustomSettings( fieldTable, fieldParameters );
			postMessage( "Quadrupole Field settings imported from:  " + file.getCanonicalPath() );
		}
		catch ( java.io.FileNotFoundException exception ) { displayError( exception ); }
		catch ( java.io.IOException exception ) { displayError( exception ); }		
	}
	
	
	/** 
	 * Convert phase to fall between -180 and +180 degrees 
	 * @param rawPhase the phase in degrees
	 * @return constrained phase in degrees
	 */
	static private double constrainPhaseDegrees( final double rawPhase ) {
		return 180.0 / Math.PI * constrainPhaseRadians( rawPhase * Math.PI / 180.0 );
	}
	
	
	/** 
	 * Convert phase to fall between -pi and +pi degrees 
	 * @param rawPhase the phase in radians
	 * @return constrained phase in radians
	 */
	static private double constrainPhaseRadians( final double rawPhase ) {
		final double xPhase = Math.acos( Math.cos( rawPhase ) );	// 0.0 to pi
		final double yPhase = Math.asin( Math.sin( rawPhase ) );	// -pi/2 to +pi/2
		
		return yPhase > 0.0 ? xPhase : -xPhase;
	}
	
	
	
	/**
	 * Make the view for filtering parameters.
	 */
	private Component makeQualifierView() {
		final int SPACE = 10;
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.setBorder( BorderFactory.createTitledBorder( "Filter" ) );
		
		final LiveParameterQualifier qualifier = _parameterTableModel.getQualifier();
		
		final JCheckBox passFixedButton = new JCheckBox( "Fixed" );
		view.add( passFixedButton );
		passFixedButton.setSelected( qualifier.getPassFixed() );
		passFixedButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassFixed( passFixedButton.isSelected() );
			}
		});
		
		final JCheckBox passVariableButton = new JCheckBox( "Variable" );
		view.add( passVariableButton );
		passVariableButton.setSelected( qualifier.getPassVariables() );
		passVariableButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassVariables( passVariableButton.isSelected() );
			}
		});
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		final JCheckBox passBendsButton = new JCheckBox( "Bend Field" );
		view.add( passBendsButton );
		passBendsButton.setSelected( qualifier.getPassBendField() );
		passBendsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassBendField( passBendsButton.isSelected() );
			}
		});
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		final JCheckBox passDipoleCorrectorsButton = new JCheckBox( "Dipole Corrector Field" );
		view.add( passDipoleCorrectorsButton );
		passDipoleCorrectorsButton.setSelected( qualifier.getPassDipoleCorrectorField() );
		passDipoleCorrectorsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassDipoleCorrectorField( passDipoleCorrectorsButton.isSelected() );
			}
		});
		
		view.add( Box.createHorizontalStrut( SPACE ) );
		
		final JCheckBox passQuadsButton = new JCheckBox( "Quadrupole Field" );
		view.add( passQuadsButton );
		passQuadsButton.setSelected( qualifier.getPassQuadField() );
		passQuadsButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassQuadField( passQuadsButton.isSelected() );
			}
		});
		
		final JCheckBox passRFAmplitudeButton = new JCheckBox( "RF Amplitude" );
		view.add( passRFAmplitudeButton );
		passRFAmplitudeButton.setSelected( qualifier.getPassRFAmplitude() );
		passRFAmplitudeButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassRFAmplitude( passRFAmplitudeButton.isSelected() );
			}
		});
		
		final JCheckBox passRFPhaseButton = new JCheckBox( "RF Phase" );
		view.add( passRFPhaseButton );
		passRFPhaseButton.setSelected( qualifier.getPassRFPhase() );
		passRFPhaseButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				qualifier.setPassRFPhase( passRFPhaseButton.isSelected() );
			}
		});
		
		view.setMaximumSize( new Dimension( 10000, view.getPreferredSize().height ) );
		
		return view;
	}
	
	
	/** 
	 * Make and return a view for displaying messages.
	 * @return the message view
	 */
	private Component makeMessageView() {
		final Box box = new Box( BoxLayout.X_AXIS );
		
		box.add( _messageView );
		box.add( Box.createHorizontalGlue() );
		
		return box;
	}
	
	
	/** setup the parameter table  */
	private void setupParameterTable() {
		_parameterTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		
		// allow the user to double click a row to view the associated parameter inspector
		_parameterTable.addMouseListener( new MouseAdapter() {
			public void mouseClicked( final MouseEvent event ) {
				if ( event.getClickCount() == 2 ) {
					showParameterInspector( _parameterTable.rowAtPoint( event.getPoint() ), true );
				}
			}
		});
		
		_parameterTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged( final ListSelectionEvent event ) {
				if ( isParameterInspectorVisible() ) {
					showParameterInspector( _parameterTable.getSelectedRow(), false );					
				}
			}
		});		
	}
	
	
	/** make parameter table cell renderers */
	private void makeParameterTableCellRenderers() {
		final int variableWidth = new JLabel( "Variable" ).getPreferredSize().width;
		_parameterTable.getColumnModel().getColumn( LiveParameterTableModel.VARIABLE_COLUMN ).setMaxWidth( variableWidth );		
	}
	
	
	/**
	 * Determine whether the parameter inspector is visible.
	 * @return true if the inspector is visible and false if not
	 */
	protected boolean isParameterInspectorVisible() {
		return ( _parameterInspector != null && _parameterInspector.isVisible() );
	}
	
	
	/**
	 * Show the parameter inspector for the parameter at the specified parameter table row.
	 * @param row the parameter table row identifying the parameter to display
	 * @param bringToFront if true, then bring the inspector to the front
	 */
	protected void showParameterInspector( final int row, final boolean bringToFront ) {
		if ( row >= 0 ) {
			showParameterInspector( _parameterTableModel.getParameter( row ), bringToFront );			
		}
	}
	
	
	/**
	 * Show the parameter inspector for the specified parameter.
	 * @param parameter the parameter for which to display the inspector
	 * @param bringToFront if true, then bring the inspector to the front
	 */
	protected void showParameterInspector( final LiveParameter parameter, final boolean bringToFront ) {
		if ( _parameterInspector == null ) {
			_parameterInspector = new LiveParameterInspector( EnergyManagerWindow.this );
		}
		
		_parameterInspector.setParameter( parameter );
		
		if ( !_parameterInspector.isVisible() || bringToFront ) {
			_parameterInspector.setVisible( true );				
		}		
	}
	
	
	/** 
	 * Get the selected parameters 
	 * @return the selected parameters
	 */
	protected List<LiveParameter> getSelectedParameters() {
		final List<LiveParameter> parameters = new ArrayList<LiveParameter>();
		final List<LiveParameter> filteredParameters = _parameterTableModel.getFilteredParameters();
		final int[] selectedRows = _parameterTable.getSelectedRows();
		
		for ( int index = 0 ; index < selectedRows.length ; index++ ) {
			parameters.add( filteredParameters.get( selectedRows[index] ) );
		}
		
		return parameters;
	}
	
	
	/**
	 * Post a normal informational message to the message view.
	 * @param message the message to display
	 */
	protected void postMessage( final String message ) {
		postMessage( message, false );
	}
	
	
	/**
	 * Post a message to the message view.
	 * @param message the message to display
	 * @param isWarning indicates whether this message is a warning message or just informational
	 */
	protected void postMessage( final String message, final boolean isWarning ) {
		final String timestamp = MESSAGE_DATE_FORMAT.format( new Date() );
		final String messageColor = isWarning ? "#ff0000" : "#000000";
		final String text = "<html><body><font color=\"#808080\"> " + timestamp + "</font><font color=\"" + messageColor + "\"> " + message + " </font></body></html>";
		_messageView.setText( text );
		_messageView.setForeground( isWarning ? Color.RED : Color.BLACK );
		validate();							
	}
	
	
	/** Show the solver config dialog */
	public void showSolverConfigDialog() {
		if ( _solverConfigDialog == null ) {
			_solverConfigDialog = new SolverConfigDialog( EnergyManagerWindow.this, getEnergyManagerDocument().getModel().getOptimizer() );
			_solverConfigDialog.setLocationRelativeTo( EnergyManagerWindow.this );
		}
		else {
			_solverConfigDialog.setOptimizer( getEnergyManagerDocument().getModel().getOptimizer() );
		}
		
		_solverConfigDialog.setVisible( true );		
	}
	
	
	/** Show the optimizer window */
	public void showOptimizerWindow() {
		if ( _optimizerWindow == null ) {
			_optimizerWindow = new OptimizerWindow( getEnergyManagerDocument().getModel().getOptimizer() );
			_optimizerWindow.setLocationRelativeTo( this );
		}
		
		_optimizerWindow.setVisible( true );
	}
	
	
	/**
	 * Handle the event indicating that the list of evaluation nodes has changed.
	 * @param model the model posting the event
	 * @param range the new position range of evaluation nodes (first position, last position)
	 * @param nodes the new evaluation nodes
	 */
	public void evaluationNodesChanged( final EnergyManager model, final double[] range, final List<AcceleratorNode> nodes ) {}
	
	
	/**
	 * Handle the event indicating that the model's entrance probe has changed.
	 * @param model the model posting the event
	 * @param entranceProbe the new entrance probe
	 */
	public void entranceProbeChanged( final EnergyManager model, final xal.model.probe.Probe entranceProbe ) {}
		
	
	/** 
	* Handle the event indicating that the model's sequence has changed. 
	* @param model the model posting the event
	* @param sequence the model's new sequence
	*/
	public void sequenceChanged( final EnergyManager model, final AcceleratorSeq sequence, final List<NodeAgent> nodeAgents, final List<LiveParameter> parameters ) {
		((JComponent)getContentPane().getComponent(0)).setBorder( BorderFactory.createTitledBorder( ( sequence != null ) ? sequence.getId() : "" ) );
		
		List<LiveParameter> sortedParameters;
		if ( parameters != null ) {
			sortedParameters = new ArrayList<LiveParameter>( parameters );
			Collections.sort( sortedParameters, _parameterOrdering );
		}
		else {
			sortedParameters = Collections.<LiveParameter>emptyList();
		}
		
		_parameterTableModel.setParameters( sortedParameters );
		makeParameterTableCellRenderers();
		if ( _parameterInspector != null && _parameterInspector.isVisible() ) {
			_parameterInspector.setVisible( false );
			_parameterInspector.setParameter( null );
		}
		
		_optimizerWindow = null;
		
		postMessage( "sequence changed to " + ( ( sequence != null ) ? sequence.getId() : "none" ) );
		
		validate();
	}
	
	
	/**
	 * Event indicating that a live parameter has been modified.
	 * @param model the source of the event.
	 * @param parameter the parameter which has changed.
	 */
	public void liveParameterModified( final EnergyManager model, final LiveParameter parameter ) {}
	
	
	/**
	 * Event indicating that the optimizer settings have changed.
	 * @param model the source of the event.
	 * @param optimizer the optimizer whose settings have changed.
	 */
	public void optimizerSettingsChanged( final EnergyManager model, final OpticsOptimizer optimizer ) {}
	
	
	/**
	 * Event indicating that the optimizer has found a new optimal solution.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has found a new optimial solution.
	 */
	public void newOptimalSolutionFound( EnergyManager model, OpticsOptimizer optimizer ) {}
	
	
	/**
	 * Event indicating that the optimizer has started.
	 * @param model the source of the event.
	 * @param optimizer the optimizer which has started.
	 */
	public void optimizerStarted( final EnergyManager model, final OpticsOptimizer optimizer ) {
		showOptimizerWindow();
	}
	
	
	/**
	 * Select the next parameter in the table.
	 */
	public void selectNextParameter() {
		final int selectedRow = _parameterTable.getSelectedRow();
		final int nextRow = ( selectedRow < _parameterTable.getRowCount() - 1 ) ? selectedRow + 1 : selectedRow;
		_parameterTable.getSelectionModel().setSelectionInterval( nextRow, nextRow );
	}
	
	
	/**
	 * Select the next parameter in the table.
	 */
	public void selectPreviousParameter() {
		final int selectedRow = _parameterTable.getSelectedRow();
		final int nextRow = ( selectedRow > 0 ) ? selectedRow - 1 : selectedRow;
		_parameterTable.getSelectionModel().setSelectionInterval( nextRow, nextRow );
	}
}



