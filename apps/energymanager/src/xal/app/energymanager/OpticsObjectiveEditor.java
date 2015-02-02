//
//  OpticsObjectiveEditor.java
//  xal
//
//  Created by Thomas Pelaia on 6/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Component;
import java.text.*;


/** View for editing optics objective settings. */
public class OpticsObjectiveEditor extends Box implements OpticsObjectiveListener {
	
    private final static long serialVersionUID = 1L;

    final static protected NumberFormat FORMATTER;
	final static protected NumberFormat PERCENT_FORMATTER;
	
	final protected OpticsObjective _objective;
	
	final protected JCheckBox _enableCheckbox;
	
	
	// static initializer
	static {
		FORMATTER = new DecimalFormat( "#.####" );
		PERCENT_FORMATTER = new DecimalFormat( "#.#### %" );
	}
	
	
	/** Constructor */
	public OpticsObjectiveEditor( final OpticsObjective objective ) {
		super( BoxLayout.X_AXIS );
		
		_objective = objective;
		
		_enableCheckbox = new JCheckBox( objective.getName(), objective.isEnabled() );
		
		makeContent();
		
		objective.addOpticsObjectiveListener( this );		
	}
	
	
	/** Create an editor for the specified objective */
	static public OpticsObjectiveEditor getInstance( final OpticsObjective objective ) {
		if ( objective instanceof EnergyObjective ) {
			return new EnergyObjectiveEditor( objective );
		}
		else if ( objective instanceof BetaMaxObjective ) {
			return new BetaMaxObjectiveEditor( objective );
		}
		else if ( objective instanceof BetaMinObjective ) {
			return new BetaMinObjectiveEditor( objective );
		}
		else if ( objective instanceof BetaMeanErrorObjective ) {
			return new BetaMeanErrorObjectiveEditor( objective );
		}
		else if ( objective instanceof BetaWorstErrorObjective ) {
			return new BetaWorstErrorObjectiveEditor( objective );
		}
		else if ( objective instanceof EtaMaxObjective ) {
			return new EtaMaxObjectiveEditor( objective );
		}
		else if ( objective instanceof EtaMinObjective ) {
			return new EtaMinObjectiveEditor( objective );
		}
		else {
			return new OpticsObjectiveEditor( objective );			
		}
	}
	
	
	/** make the content */
	protected void makeContent() {
		add( _enableCheckbox );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		_objective.setEnable( _enableCheckbox.isSelected() );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		_enableCheckbox.setSelected( _objective.isEnabled() );
	}
	
	
	/**
	 * Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
	}
	
	
	/**
	 * Handler that indicates that the enable state of the specified objective has changed.
	 * @param objective the objective whose state has changed.
	 * @param isEnabled the new enable state of the objective.
	 */
	public void objectiveEnableChanged( OpticsObjective objective, boolean isEnabled ) {
		_enableCheckbox.setSelected( objective.isEnabled() );		
	}
	
	
	/**
	 * Handler indicating that the specified objective's settings have changed.
	 * Leave the body of this method empty otherwise it will compete with the "apply" and "refresh" methods for objectives with
	 * more than one setting that changes.
	 * @param objective the objective whose settings have changed.
	 */
	public void objectiveSettingsChanged( OpticsObjective objective ) {}
}



/** Value and tolerance objective editor */
abstract class TargetToleranceObjectiveEditor extends OpticsObjectiveEditor {
	/** provide the required serial version ID */
	private static final long serialVersionUID = 1L;

	protected JFormattedTextField _targetField;
	protected JFormattedTextField _toleranceField;
	
	
	/** Constructor */
	public TargetToleranceObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** make the content */
	protected void makeContent() {
		final int SPACE = 15;
		final int FIELD_WIDTH = 10;
		
		super.makeContent();
		
		_targetField = new JFormattedTextField( FORMATTER );
		_targetField.setColumns( FIELD_WIDTH );
		_targetField.setHorizontalAlignment( JTextField.RIGHT );
		_targetField.setMaximumSize( _targetField.getPreferredSize() );
		add( Box.createHorizontalStrut( SPACE ) );
		add( new JLabel( getTargetLabel() ) );
		add( _targetField );
		
		_toleranceField = new JFormattedTextField( FORMATTER );
		_toleranceField.setColumns( FIELD_WIDTH );
		_toleranceField.setHorizontalAlignment( JTextField.RIGHT );
		_toleranceField.setMaximumSize( _toleranceField.getPreferredSize() );
		add( Box.createHorizontalStrut( SPACE ) );
		add( new JLabel( getToleranceLabel() ) );
		add( _toleranceField );
	}
	
	
	/**
	 * Get the target field label.
	 * @return the label for the target field
	 */
	abstract protected String getTargetLabel();
	
	
	/**
	 * Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	abstract protected String getToleranceLabel();
}




/** Energy objective editor */
class EnergyObjectiveEditor extends TargetToleranceObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	protected final double ENERGY_SCALE = 1.0;
	
	
	/** Constructor */
	public EnergyObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		// grab the field values before applying them
		final double target = ((Number)_targetField.getValue()).doubleValue();
		final double tolerance =  ((Number)_toleranceField.getValue()).doubleValue();
		
		((EnergyObjective)_objective).setTargetEnergy( ENERGY_SCALE * target );
		((EnergyObjective)_objective).setTolerance( ENERGY_SCALE * tolerance );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		_targetField.setValue( new Double( ((EnergyObjective)_objective).getTargetEnergy() / ENERGY_SCALE ) );
		_toleranceField.setValue( new Double( ((EnergyObjective)_objective).getTolerance() / ENERGY_SCALE ) );
	}
	
	
	/**
	 * Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
		final double targetEnergy = simulation.getOutputKineticEnergy() / ENERGY_SCALE;
		_targetField.setValue( new Double( targetEnergy ) );
		_toleranceField.setValue( new Double( targetEnergy / 20 ) );		// 5% tolerance
	}
	
	
	/**
	 * Get the target field label.
	 * @return the label for the target field
	 */
	protected String getTargetLabel() {
		return "Target Energy (MeV): ";
	}
	
	
	/**
	 * Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	protected String getToleranceLabel() {
		return "Energy Tolerance (MeV): ";
	}
}




/** Beta maximum objective editor */
class BetaMaxObjectiveEditor extends TargetToleranceObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	/** Constructor */
	public BetaMaxObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		// grab the field values before applying them
		final double target = ((Number)_targetField.getValue()).doubleValue();
		final double tolerance =  ((Number)_toleranceField.getValue()).doubleValue();
		
		((BetaMaxObjective)_objective).setMaxBeta( target );
		((BetaMaxObjective)_objective).setTolerance( tolerance );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		
		_targetField.setValue( new Double( ((BetaMaxObjective)_objective).getMaxBeta() ) );
		_toleranceField.setValue( new Double( ((BetaMaxObjective)_objective).getTolerance() ) );
	}
	
	
	/**
	 * Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
		final double maxBeta = ((BetaMaxObjective)_objective).getDesignTarget( simulation );
		_targetField.setValue( new Double( maxBeta ) );
		_toleranceField.setValue( new Double( maxBeta / 10 ) );
	}
	
	
	/**
	 * Get the target field label.
	 * @return the label for the target field
	 */
	protected String getTargetLabel() {
		return "Max Beta (m): ";
	}
	
	
	/**
	 * Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	protected String getToleranceLabel() {
		return "Tolerance (m): ";
	}
}




/** Beta minimum objective editor */
class BetaMinObjectiveEditor extends TargetToleranceObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	/** Constructor */
	public BetaMinObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		// grab the field values before applying them
		final double target = ((Number)_targetField.getValue()).doubleValue();
		final double tolerance =  ((Number)_toleranceField.getValue()).doubleValue();
		
		((BetaMinObjective)_objective).setMinBeta( target );
		((BetaMinObjective)_objective).setTolerance( tolerance );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		_targetField.setValue( new Double( ((BetaMinObjective)_objective).getMinBeta() ) );
		_toleranceField.setValue( new Double( ((BetaMinObjective)_objective).getTolerance() ) );
	}
	
	
	/**
	 * Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
		final double minBeta = ((BetaMinObjective)_objective).getDesignTarget( simulation );
		_targetField.setValue( new Double( minBeta ) );
		_toleranceField.setValue( new Double( minBeta / 10 ) );
	}
	
	
	/**
	 * Get the target field label.
	 * @return the label for the target field
	 */
	protected String getTargetLabel() {
		return "Min Beta (m): ";
	}
	
	
	/**
	 * Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	protected String getToleranceLabel() {
		return "Tolerance (m): ";
	}
}



/** Beta mean error objective editor */
class BetaMeanErrorObjectiveEditor extends OpticsObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	protected JFormattedTextField _toleranceField;
	
	
	/** Constructor */
	public BetaMeanErrorObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** make the content */
	protected void makeContent() {
		final int SPACE = 15;
		final int FIELD_WIDTH = 10;
		
		super.makeContent();
		
		_toleranceField = new JFormattedTextField( PERCENT_FORMATTER );
		_toleranceField.setColumns( FIELD_WIDTH );
		_toleranceField.setMaximumSize( _toleranceField.getPreferredSize() );
		_toleranceField.setHorizontalAlignment( JTextField.RIGHT );
		add( Box.createHorizontalStrut( SPACE ) );
		add( new JLabel( "Tolerance (%): " ) );
		add( _toleranceField );
		add( Box.createHorizontalGlue() );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		((BetaMeanErrorObjective)_objective).setTolerance( ((Number)_toleranceField.getValue()).doubleValue() );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		_toleranceField.setValue( new Double( ((BetaMeanErrorObjective)_objective).getTolerance() ) );
	}
}



/** Beta mean error objective editor */
class BetaWorstErrorObjectiveEditor extends OpticsObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	protected JFormattedTextField _toleranceField;
	
	
	/** Constructor */
	public BetaWorstErrorObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** make the content */
	protected void makeContent() {
		final int SPACE = 15;
		final int FIELD_WIDTH = 10;
		
		super.makeContent();
		
		_toleranceField = new JFormattedTextField( PERCENT_FORMATTER );
		_toleranceField.setColumns( FIELD_WIDTH );
		_toleranceField.setMaximumSize( _toleranceField.getPreferredSize() );
		_toleranceField.setHorizontalAlignment( JTextField.RIGHT );
		add( Box.createHorizontalStrut( SPACE ) );
		add( new JLabel( "Tolerance (%): " ) );
		add( _toleranceField );
		add( Box.createHorizontalGlue() );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		((BetaWorstErrorObjective)_objective).setTolerance( ((Number)_toleranceField.getValue()).doubleValue() );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		_toleranceField.setValue( new Double( ((BetaWorstErrorObjective)_objective).getTolerance() ) );
	}
}



/** chromatic dispersion maximum objective editor */
class EtaMaxObjectiveEditor extends TargetToleranceObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	/** Constructor */
	public EtaMaxObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		// grab the field values before applying them
		final double target = ((Number)_targetField.getValue()).doubleValue();
		final double tolerance =  ((Number)_toleranceField.getValue()).doubleValue();
		
		((EtaMaxObjective)_objective).setMaxEta( target );
		((EtaMaxObjective)_objective).setTolerance( tolerance );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		
		_targetField.setValue( new Double( ((EtaMaxObjective)_objective).getMaxEta() ) );
		_toleranceField.setValue( new Double( ((EtaMaxObjective)_objective).getTolerance() ) );
	}
	
	
	/**
	* Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
		final double maxEta = ((EtaMaxObjective)_objective).getDesignTarget( simulation );
		_targetField.setValue( maxEta );
		final double tolerance = maxEta != 0.0 ? 0.1 * maxEta : 0.1;
		_toleranceField.setValue( tolerance );
	}
	
	
	/**
	* Get the target field label.
	 * @return the label for the target field
	 */
	protected String getTargetLabel() {
		return "Max Eta (m): ";
	}
	
	
	/**
	* Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	protected String getToleranceLabel() {
		return "Tolerance (m): ";
	}
}




/** chromatic dispersion minimum objective editor */
class EtaMinObjectiveEditor extends TargetToleranceObjectiveEditor {
    
    private final static long serialVersionUID = 1L;

	/** Constructor */
	public EtaMinObjectiveEditor( final OpticsObjective objective ) {
		super( objective );
	}
	
	
	/** apply the settings to the solver session */
	public void applySettings() {
		super.applySettings();
		
		// grab the field values before applying them
		final double target = ((Number)_targetField.getValue()).doubleValue();
		final double tolerance =  ((Number)_toleranceField.getValue()).doubleValue();
		
		((EtaMinObjective)_objective).setMinEta( target );
		((EtaMinObjective)_objective).setTolerance( tolerance );
	}
	
	
	/** apply the settings to the solver session */
	public void refreshSettings() {
		super.refreshSettings();
		_targetField.setValue( new Double( ((EtaMinObjective)_objective).getMinEta() ) );
		_toleranceField.setValue( new Double( ((EtaMinObjective)_objective).getTolerance() ) );
	}
	
	
	/**
	* Initialize with design simulation.
	 * @param simulation the design simulation
	 */
	public void initializeWithDesign( final Simulation simulation ) {
		final double minEta = ((EtaMinObjective)_objective).getDesignTarget( simulation );
		_targetField.setValue( minEta );
		final double tolerance = minEta != 0.0 ? 0.1 * minEta : 0.1;
		_toleranceField.setValue( tolerance );
	}
	
	
	/**
	* Get the target field label.
	 * @return the label for the target field
	 */
	protected String getTargetLabel() {
		return "Min Eta (m): ";
	}
	
	
	/**
	* Get the tolerance field label.
	 * @return the label for the tolerance field
	 */
	protected String getToleranceLabel() {
		return "Tolerance (m): ";
	}
}













