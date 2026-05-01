//
//  SimpleEnergyScaleDialog.java
//  xal
//
//  Created by Thomas Pelaia on 11/22/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.text.*;


/** Dialog for allow the user to specify energy scaling */
public class SimpleEnergyScaleDialog extends JDialog {
	
    private final static long serialVersionUID = 1L;

    protected volatile boolean _okayStatus;
	final protected JFormattedTextField _initialKineticEnergyField;
	final protected JFormattedTextField _targetKineticEnergyField;
	protected String _source;
	
	
	/** Constructor */
	public SimpleEnergyScaleDialog( final Frame owner, final double initialKineticEnergy ) {
		super( owner, "Evaluation Position Range", true );
		
		setResizable( false );
		
		final Format format = new DecimalFormat( "0.0" );
		final int numColumns = 10;
		
		_initialKineticEnergyField = new JFormattedTextField( format );
		_initialKineticEnergyField.setColumns( numColumns );
		_initialKineticEnergyField.setHorizontalAlignment( JTextField.RIGHT );
		_initialKineticEnergyField.setMaximumSize( _initialKineticEnergyField.getPreferredSize() );
		
		_targetKineticEnergyField = new JFormattedTextField( format );
		_targetKineticEnergyField.setColumns( numColumns );
		_targetKineticEnergyField.setHorizontalAlignment( JTextField.RIGHT );
		_targetKineticEnergyField.setMaximumSize( _targetKineticEnergyField.getPreferredSize() );
		
		makeContentView();
		pack();
		
		_initialKineticEnergyField.setValue( initialKineticEnergy );
		_targetKineticEnergyField.setValue( initialKineticEnergy );
	}
	
	
	/**
	 * Get the source key (control, design or custom) selected by the user.
	 * @return the source selected
	 */
	public String getSource() {
		return _source;
	}
	
	
	/**
	 * Get the initial kinetic energy (eV)
	 * @return the initial kinetic energy
	 */
	public double getInitialKineticEnergy() {
		return 1.0e6 * ((Number)_initialKineticEnergyField.getValue()).doubleValue();
	}
	
	
	/**
	 * Get the target kinetic energy (eV)
	 * @return the target kinetic energy
	 */
	public double getTargetKineticEnergy() {
		return 1.0e6 * ((Number)_targetKineticEnergyField.getValue()).doubleValue();
	}
	
	
	/** make content view */
	protected void makeContentView() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( makeFieldRow( "Initial Kinetic Energy(MeV): ", _initialKineticEnergyField ) );
		mainView.add( makeFieldRow( "Target Kinetic Energy(MeV): ", _targetKineticEnergyField ) );
		mainView.add( makeSourceSelectorRow() );
		mainView.add( makeButtonRow() );
	}
	
	
	/**
	 * Make a row with the specified label and field.
	 */
	protected Component makeFieldRow( final String label, final JTextField field ) {
		final Box row = new Box( BoxLayout.X_AXIS );
		row.add( Box.createHorizontalGlue() );
		row.add( Box.createHorizontalStrut( 10 ) );
		row.add( new JLabel( label ) );
		row.add( Box.createHorizontalStrut( 10 ) );
		row.add( field );
		
		return row;
	}
	
	
	/**
	 * Make a row of radio buttons to select the source.
	 */
	protected Component makeSourceSelectorRow() {
		final ButtonGroup sourceButtonGroup = new ButtonGroup();
		final Box row = new Box( BoxLayout.X_AXIS );
		row.add( Box.createHorizontalGlue() );
		row.add( new JLabel( "Select the source to scale:  " ) );
		row.add( createSourceButton( sourceButtonGroup, "Design", LiveParameter.DESIGN_VALUE_KEY, false ) );
		row.add( createSourceButton( sourceButtonGroup, "Control", LiveParameter.CONTROL_VALUE_KEY, false ) );
		row.add( createSourceButton( sourceButtonGroup, "Initial", LiveParameter.INITIAL_VALUE_KEY, true ) );
		row.add( Box.createHorizontalGlue() );
		return row;
	}
	
	
	/**
	 * Create a new radio button for selecting a source of the given label.
	 */
	protected JRadioButton createSourceButton( final ButtonGroup buttonGroup, final String label, final String source, final boolean select  ) {
		final JRadioButton button = new JRadioButton( label );
		buttonGroup.add( button );
		button.setSelected( select );
		if ( select ) {
			_source = source;
		}
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_source = source;
			}
		});
		
		return button;
	}
	
	
	/**
	 * Make a row of buttons.
	 */
	protected Component makeButtonRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		row.setBorder( BorderFactory.createEtchedBorder() );
		
		row.add( Box.createHorizontalGlue() );
		
		final JButton cancelButton = new JButton( "Cancel" );
		row.add( cancelButton );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_okayStatus = false;
				setVisible( false );
			}
		});
		
		final JButton okayButton = new JButton( "Okay" );
		row.add( okayButton );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_okayStatus = true;
				setVisible( false );
			}
		});
		
		return row;
	}
	
	
	/**
	 * Present this dialog to the user.
	 */
	public boolean present() {
		_okayStatus = false;
		setLocationRelativeTo( getOwner() );
		setVisible( true );
		
		return _okayStatus;
	}	
}
