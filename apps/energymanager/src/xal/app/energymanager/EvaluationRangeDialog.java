//
//  EvaluationRangeDialog.java
//  xal
//
//  Created by Thomas Pelaia on 7/6/05.
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


/** Dialog for allow the user to specify the evaluation node range */
public class EvaluationRangeDialog extends JDialog {

    private final static long serialVersionUID = 1L;

    final protected JFormattedTextField _firstPositionField;
	final protected JFormattedTextField _lastPositionField;
	final protected JCheckBox _useSelectionRangeCheckBox;
	
	protected double[] _selectionRange;
	protected double[] _result;
	
	
	/** Constructor */
	public EvaluationRangeDialog( final Frame owner ) {
		super( owner, "Evaluation Position Range", true );
		
		setResizable( false );
		
		_useSelectionRangeCheckBox = new JCheckBox( "Use Selection Range" );
		_useSelectionRangeCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				updateRangeInputFields();
			}
		});
		
		final Format format = new DecimalFormat( "0.0" );
		final int numColumns = 10;
		
		_firstPositionField = new JFormattedTextField( format );
		_firstPositionField.setColumns( numColumns );
		_firstPositionField.setHorizontalAlignment( JTextField.RIGHT );
		_firstPositionField.setMaximumSize( _firstPositionField.getPreferredSize() );
		
		_lastPositionField = new JFormattedTextField( format );
		_lastPositionField.setColumns( numColumns );
		_lastPositionField.setHorizontalAlignment( JTextField.RIGHT );
		_lastPositionField.setMaximumSize( _lastPositionField.getPreferredSize() );
		
		makeContentView();
		pack();
	}
	
	
	/** make content view */
	protected void makeContentView() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( makeCheckboxRow() );
		mainView.add( makeFieldRow( "First Position: ", _firstPositionField ) );
		mainView.add( makeFieldRow( "Last Position: ", _lastPositionField ) );
		mainView.add( makeButtonRow() );
	}
	
	
	/** make the checkbox row */
	protected Component makeCheckboxRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		
		row.add( _useSelectionRangeCheckBox );
		row.add( Box.createHorizontalGlue() );
		
		return row;		
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
	 * Make a row of buttons.
	 * @return view with row of buttons
	 */
	protected Component makeButtonRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		row.setBorder( BorderFactory.createEtchedBorder() );
		
		row.add( Box.createHorizontalGlue() );
		
		final JButton cancelButton = new JButton( "Cancel" );
		row.add( cancelButton );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_result = null;
				setVisible( false );
			}
		});
		
		final JButton okayButton = new JButton( "Okay" );
		getRootPane().setDefaultButton( okayButton );
		row.add( okayButton );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final double firstPosition = ((Number)_firstPositionField.getValue()).doubleValue();
				final double lastPosition = ((Number)_lastPositionField.getValue()).doubleValue();
				_result = new double[] { firstPosition, lastPosition };
				setVisible( false );
			}
		});
		
		return row;
	}
	
	
	/** update the range input fields based on whether or not to use the selection range */
	protected void updateRangeInputFields() {
		final boolean useSelectionRange = _useSelectionRangeCheckBox.isSelected();
		
		_firstPositionField.setEditable( !useSelectionRange );
		_firstPositionField.setEnabled( !useSelectionRange );
		_lastPositionField.setEditable( !useSelectionRange );
		_lastPositionField.setEnabled( !useSelectionRange );
		
		if ( useSelectionRange ) {
			_firstPositionField.setValue( _selectionRange[0] );
			_lastPositionField.setValue( _selectionRange[1] );
		}
	}
	
	
	/**
	 * Present this dialog to the user.
	 */
	public double[] present( final double[] currentRange, final double[] selectionRange ) {
		_result = null;
		_selectionRange = selectionRange;
		
		_firstPositionField.setValue( currentRange[0] );
		_lastPositionField.setValue( currentRange[1] );
		
		_useSelectionRangeCheckBox.setSelected( false );
		updateRangeInputFields();
		
		setLocationRelativeTo( getOwner() );
		setVisible( true );
		
		return _result;
	}
}





