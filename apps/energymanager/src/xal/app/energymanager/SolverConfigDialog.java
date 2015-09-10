//
//  SolverConfigDialog.java
//  xal
//
//  Created by Thomas Pelaia on 6/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.application.*;
import xal.extension.solver.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.util.*;
import java.text.*;


/** Dialog for configuring a solve session. */
public class SolverConfigDialog extends JDialog implements SolverSessionListener {
    
    private static final long serialVersionUID = 1L;
    
	final static protected NumberFormat FORMATTER;
	
	final protected Box _objectivesContainer;
	final protected List<OpticsObjectiveEditor> _objectiveEditors;
	
	protected OpticsOptimizer _optimizer;
	protected SolverSession _solverSession;
	
	protected JTextField _minSolveTimeField;
	protected JTextField _maxSolveTimeField;
	protected JTextField _targetSatisfactionField;
	
	protected JButton _solveButton;
	
	
	// static initializer
	static {
		FORMATTER = new DecimalFormat( "#.###" );
	}
	
	
	/**
	 * Constructor
	 */
	public SolverConfigDialog( final JFrame owner, final OpticsOptimizer optimizer ) {
		super( owner, "Solver Configuration", true );
		
		_objectivesContainer = new Box( BoxLayout.Y_AXIS );
		_objectivesContainer.setBorder( BorderFactory.createTitledBorder( "Objectives" ) );
		_objectiveEditors = new ArrayList<OpticsObjectiveEditor>();
		
		makeContentView();
		setResizable( false );
		
		setOptimizer( optimizer );
		
		handleWindowEvents();
	}
	
	
	/**
	 * Handle window events.
	 */
	protected void handleWindowEvents() {
		addWindowListener( new WindowAdapter() {
			public void windowClosing( final WindowEvent event ) {
				System.out.println( "config window closing..." );
			}
		});
	}
	
	
	/** Make the content view */
	protected void makeContentView() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( makeStopperView() );
		mainView.add( _objectivesContainer );
		mainView.add( makeButtonView() );
		mainView.add( Box.createVerticalStrut( 15 ) );
	}
	
	
	/** Make the stopper view */
	private Component makeStopperView() {
		final int SPACE = 15;
		final int FIELD_WIDTH = 7;
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.setBorder( BorderFactory.createTitledBorder( "Solve Stopper" ) );
		
		_minSolveTimeField = new JTextField( FIELD_WIDTH );
		_minSolveTimeField.setHorizontalAlignment( JTextField.RIGHT );
		_minSolveTimeField.setMaximumSize( _minSolveTimeField.getPreferredSize() );
		view.add( Box.createHorizontalStrut( SPACE ) );
		view.add( new JLabel( "Min Solve Time(sec): " ) );
		view.add( _minSolveTimeField );
		
		_maxSolveTimeField = new JTextField( FIELD_WIDTH );
		_maxSolveTimeField.setHorizontalAlignment( JTextField.RIGHT );
		_maxSolveTimeField.setMaximumSize( _maxSolveTimeField.getPreferredSize() );
		view.add( Box.createHorizontalStrut( SPACE ) );
		view.add( new JLabel( "Max Solve Time(sec): " ) );
		view.add( _maxSolveTimeField );
		
		_targetSatisfactionField = new JTextField( FIELD_WIDTH );
		_targetSatisfactionField.setHorizontalAlignment( JTextField.RIGHT );
		_targetSatisfactionField.setMaximumSize( _targetSatisfactionField.getPreferredSize() );
		view.add( Box.createHorizontalStrut( SPACE ) );
		view.add( new JLabel( "Target Satisfaction: " ) );
		view.add( _targetSatisfactionField );
		
		return view;
	}
	
	
	/** Make optics objective views */
	protected void populateObjectiveViews() {
		_objectiveEditors.clear();
		_objectivesContainer.removeAll();
		
		if ( _solverSession == null )  return;
		
		for ( final OpticsObjective objective : _solverSession.getObjectives() ) {
			final OpticsObjectiveEditor editor = OpticsObjectiveEditor.getInstance( objective );
			_objectiveEditors.add( editor );
			_objectivesContainer.add( editor );
		}
	}
	
	
	/** Make the button view */
	private Component makeButtonView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		view.setBorder( BorderFactory.createEtchedBorder() );
		
		view.add( Box.createHorizontalGlue() );
		
		final JButton designButton = new JButton( "Use Design" );
		view.add( designButton );
		designButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Simulation designSimulation = _solverSession.getDesignSimulation();
				for ( final OpticsObjectiveEditor editor : _objectiveEditors ) {
					editor.initializeWithDesign( designSimulation );
				}
			}
		});
		
		view.add( Box.createHorizontalStrut( designButton.getPreferredSize().width ) );
		
		final JButton cancelButton = new JButton( "Cancel" );
		view.add( cancelButton );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				SolverConfigDialog.this.setVisible( false );
			}
		});
		
		
		final JButton okayButton = new JButton( "Okay" );
		view.add( okayButton );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				applySettings();
				SolverConfigDialog.this.setVisible( false );
			}
		});
		
		
		_solveButton = new JButton( "Run" );
		view.add( _solveButton );
		_solveButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					applySettings();
					SolverConfigDialog.this.setVisible( false );
					_optimizer.spawnRun();
				}
				catch( Exception exception ) {
					Application.displayError( exception );
					exception.printStackTrace();
				}
			}
		});
		
		return view;
	}
	
	
	/**
	 * Set optics optimizer.
	 * @param optimizer the new optimizer
	 */
	public void setOptimizer( final OpticsOptimizer optimizer ) {
		_optimizer = optimizer;
		
		setSolverSession( ( optimizer != null ) ? optimizer.getActiveSolverSession() : null );
		
		pack();
	}
	
	
	/**
	 * Set a new solver session.
	 * @param solverSession the new solver session
	 */
	public void setSolverSession( final SolverSession solverSession ) {
		if ( _solverSession != null ) {
			_solverSession.removeSolverSessionListener( this );
		}
		
		_solverSession = solverSession;
		
		if ( solverSession != null ) {
			solverSession.addSolverSessionListener( this );
		}
		
		populateObjectiveViews();
		
		refreshSettings();
	}
	
	
	/**
	 * Override the set visible method to refresh settings.
	 * @param makeVisible true to make the dialog visible and false to hide it.
	 */
	public void setVisible( final boolean makeVisible ) {
		if ( makeVisible ) {
			refreshSettings();
		}
		
		super.setVisible( makeVisible );
	}
	
	
	/** apply the settings to the solver session */
	protected void applySettings() {
		final double minDuration = Double.parseDouble( _minSolveTimeField.getText() );
		final double maxDuration = Double.parseDouble( _maxSolveTimeField.getText() );
		final double targetSatisfaction = Double.parseDouble( _targetSatisfactionField.getText() );
		_solverSession.setStopperSettings( minDuration, maxDuration, targetSatisfaction );
		
		final Iterator<OpticsObjectiveEditor> objectiveEditorIter = _objectiveEditors.iterator();
		while( objectiveEditorIter.hasNext() ) {
			final OpticsObjectiveEditor editor = objectiveEditorIter.next();
			editor.applySettings();
		}
	}
	
	
	/** refresh this view with the latest settings of the solver session and the objectives. */
	protected void refreshSettings() {
		refreshSolverSettings();
		
		final Iterator<OpticsObjectiveEditor> objectiveEditorIter = _objectiveEditors.iterator();
		while( objectiveEditorIter.hasNext() ) {
			final OpticsObjectiveEditor editor = objectiveEditorIter.next();
			editor.refreshSettings();
		}
	}
	
	
	/** refresh this view with the latest settings of the solver session */
	protected void refreshSolverSettings() {
		if ( _solverSession != null ) {
			_minSolveTimeField.setText( FORMATTER.format( _solverSession.getMinSolveTime() ) );
			_maxSolveTimeField.setText( FORMATTER.format( _solverSession.getMaxSolveTime() ) );
			_targetSatisfactionField.setText( FORMATTER.format( _solverSession.getTargetSatisfaction() ) );
			_solveButton.setEnabled( !_optimizer.isRunning() );
		}		
		else {
			_minSolveTimeField.setText( "" );
			_maxSolveTimeField.setText( "" );
			_targetSatisfactionField.setText( "" );						
			_solveButton.setEnabled( false );
		}
	}
	
	
	/**
	 * Handler which indicates that the solver's stopper has changed for the specified session.
	 * @param session the session whose stopper has changed.
	 */
	public void stopperChanged( final SolverSession session ) {
		refreshSolverSettings();
	}
	
	
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param session the session whose objective enable state has changed
	 * @param objective the objective whose enable state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( final SolverSession session, final OpticsObjective objective, final boolean isEnabled ) {}
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * @param session the session whose objective has changed
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( final SolverSession session, final OpticsObjective objective ) {}
}



