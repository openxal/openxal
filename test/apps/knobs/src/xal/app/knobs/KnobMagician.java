//
//  KnobMagician.java
//  xal
//
//  Created by Tom Pelaia on 8/10/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.text.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.text.FormattedNumber;


/** Controller that magically creates knob coefficients for two specified machine states */
public class KnobMagician {
	/** reference to the dialog window */
	final protected WindowReference WINDOW_REFERENCE;
	
	/** table of knob element states */
	final protected KnobElementStateTableModel ELEMENT_STATE_TABLE_MODEL;
	
	/** currently active knob */
	protected Knob _knob;
	
	/** list of initial and final states for the the knob elements */
	protected final List<KnobElementStateRecord> ELEMENT_STATE_RECORDS;
	
	
	/** Constructor */
	public KnobMagician( final JFrame owner ) {
		WINDOW_REFERENCE = Application.getAdaptor().getDefaultWindowReference( "KnobMagicDialog", owner );
		ELEMENT_STATE_RECORDS = new ArrayList<KnobElementStateRecord>();
		ELEMENT_STATE_TABLE_MODEL = new KnobElementStateTableModel( ELEMENT_STATE_RECORDS );
		
		setupView( WINDOW_REFERENCE );
	}
	
	
	/** initialize the view */
	protected void setupView( final WindowReference windowReference ) {
		final JTable elementStateTable = (JTable)windowReference.getView( "KnobsTable" );
		elementStateTable.setModel( ELEMENT_STATE_TABLE_MODEL );
		
		final JFormattedTextField knobUnitsField = (JFormattedTextField)windowReference.getView( "KnobUnitsField" );
		knobUnitsField.setValue( 1.0 );
		
		final JFormattedTextField knobValueField = (JFormattedTextField)windowReference.getView( "KnobValueField" );
		knobValueField.setValue( 1.0 );
		
		final JButton initialStateButton = (JButton)windowReference.getView( "InitialStateButton" );
		initialStateButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final KnobElementStateRecord record : ELEMENT_STATE_RECORDS ) {
					record.recordInitialState();
				}
				ELEMENT_STATE_TABLE_MODEL.fireTableDataChanged();
			}
		} );
		
		final JButton finalStateButton = (JButton)windowReference.getView( "FinalStateButton" );
		finalStateButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				for ( final KnobElementStateRecord record : ELEMENT_STATE_RECORDS ) {
					record.recordFinalState();
				}
				ELEMENT_STATE_TABLE_MODEL.fireTableDataChanged();
			}
		} );
		
		final JButton cancelButton = (JButton)windowReference.getView( "CancelButton" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				getDialog().setVisible( false );
			}
		} );
		
		final JButton applyButton = (JButton)windowReference.getView( "ApplyButton" );
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final double knobUnits = ((Number)knobUnitsField.getValue()).doubleValue();
				final double knobValue = ((Number)knobValueField.getValue()).doubleValue();
				for ( final KnobElementStateRecord record : ELEMENT_STATE_RECORDS ) {
					record.applyCoefficient( knobUnits );
				}
				for ( final KnobElementStateRecord record : ELEMENT_STATE_RECORDS ) {
					final KnobElement element = record.getElement();
					_knob.coefficientChanged( element, element.getCoefficient() );
				}
				_knob.setLimitsNeedUpdating();
				_knob.setCurrentSetting( knobValue );
				getDialog().setVisible( false );
			}
		} );
	}
	
	
	/** get the dialog */
	protected JDialog getDialog() {
		return (JDialog)WINDOW_REFERENCE.getWindow();
	}
	
	
	/** display the dialog */
	public void display( final Knob knob ) {
		ELEMENT_STATE_RECORDS.clear();
		
		_knob = knob;
		final List<KnobElement> elements = knob.getElements();
		for ( final KnobElement element : elements ) {
			ELEMENT_STATE_RECORDS.add( new KnobElementStateRecord( element ) );
		}
		ELEMENT_STATE_TABLE_MODEL.fireTableDataChanged();
		
		final JDialog dialog = getDialog();
		dialog.setLocationRelativeTo( dialog.getOwner() );
		dialog.setVisible( true );
	}
}



/** hold the states of a knob element */
class KnobElementStateRecord {
	/** knob element */
	final protected KnobElement KNOB_ELEMENT;
	
	/** initial state value */
	protected double _initialValue;
	
	/** final state value */
	protected double _finalValue;
	
	
	/** Constructor */
	public KnobElementStateRecord( final KnobElement element ) {
		KNOB_ELEMENT = element;
	}
	
	
	/** get a name */
	public String getName() {
		return KNOB_ELEMENT.getChannelString();
	}
	
	
	/** get the initial state */
	public double getInitialValue() {
		return _initialValue;
	}
	
	
	/** get the final state */
	public double getFinalValue() {
		return _finalValue;
	}
	
	
	/** apply the coefficient for the specified knob unit change */
	public void applyCoefficient( final double knobUnits ) {
		final double coefficient = ( _finalValue - _initialValue ) / knobUnits;
		KNOB_ELEMENT.setCoefficient( coefficient, false );
	}
	
	
	/** get the knob element */
	public KnobElement getElement() {
		return KNOB_ELEMENT;
	}
	
	
	/** record the initial state */
	public double recordInitialState() {
		_initialValue = KNOB_ELEMENT.getLatestValue();
		return _initialValue;
	}
	
	
	/** record the final state */
	public double recordFinalState() {
		_finalValue = KNOB_ELEMENT.getLatestValue();
		return _finalValue;
	}
}



/** table model to populate the knob element table */
class KnobElementStateTableModel extends AbstractTableModel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** format pattern for displaying the set point values */
	protected final NumberFormat VALUE_FORMAT = new DecimalFormat( "0.000E0" );
	
	/** column for the name of the element */
	protected final int ELEMENT_NAME_COLUMN = 0;
	
	/** column for the initial state of the element */
	protected final int INITIAL_STATE_COLUMN = 1;
	
	/** column for the final state of the element */
	protected final int FINAL_STATE_COLUMN = 2;
	
	/** list of initial and final states for the the knob elements */
	protected final List<KnobElementStateRecord> ELEMENT_STATE_RECORDS;
	
	
	/** Constructor */
	public KnobElementStateTableModel( final List<KnobElementStateRecord> elementStateRecords ) {
		ELEMENT_STATE_RECORDS = elementStateRecords;
	}
	
	
	/** get the number of columns to display */
	public int getColumnCount() {
		return 3;
	}
	
	
	/** get the title of the specified column */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case ELEMENT_NAME_COLUMN:
				return "Element";
			case INITIAL_STATE_COLUMN:
				return "Initial State";
			case FINAL_STATE_COLUMN:
				return "Final State";
			default:
				return "?";
		}
	}
	
	
	/** get the class of the specified column */
	public Class<?> getColumnClass( final int column ) {
		switch ( column ) {
			case ELEMENT_NAME_COLUMN:
				return String.class;
			case INITIAL_STATE_COLUMN:
				return Number.class;
			case FINAL_STATE_COLUMN:
				return Number.class;
			default:
				return super.getColumnClass( column );
		}
	}
	
	
	/** get the number of rows to display */
	public int getRowCount() {
		return ELEMENT_STATE_RECORDS.size();
	}
	
	
	/** get the value at the specified row and column */
	public Object getValueAt( final int row, final int column ) {
		final KnobElementStateRecord record = ELEMENT_STATE_RECORDS.get( row );
		
		switch ( column ) {
			case ELEMENT_NAME_COLUMN:
				return record.getName();
			case INITIAL_STATE_COLUMN:
				return new FormattedNumber( VALUE_FORMAT, record.getInitialValue() );
			case FINAL_STATE_COLUMN:
				return new FormattedNumber( VALUE_FORMAT, record.getFinalValue() );
			default:
				return "?";
		}
	}
}
