//
//  CorrectionDialog.java
//  xal
//
//  Created by Thomas Pelaia on 1/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.smf.impl.*;
import xal.smf.*;
//import xal.extension.widgets.swing.patternfilter.*;
import xal.tools.text.FormattedNumber;

import java.text.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.event.*;
import java.awt.Component;
import java.awt.Container;
import java.util.*;
import java.util.logging.*;


/** Dialog for viewing and applying proposed corrections */
public class CorrectionDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
	/** the orbit model */
	final protected OrbitModel _orbitModel;
	
	/** the corrector table model */
	protected CorrectorTableModel _tableModel;
	
	
	/** Constructor */
	public CorrectionDialog( final java.awt.Frame owner, final OrbitModel model ) {
		super( owner, "Proposed Corrector Fields", true );
		
		_orbitModel = model;
		
		makeContents();
	}
	
	
	/** Show this dialog relative to the owner. */
	public void displayNearOwner() {
		_tableModel.fireTableDataChanged();
		setLocationRelativeTo( getOwner() );
		setVisible( true );
	}
	
	
	/** Make the dialog's contents */
	protected void makeContents() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		_tableModel = new CorrectorTableModel();
		mainView.add( new JScrollPane( new JTable( _tableModel ) ) );
		
		mainView.add( makeButtonBar() );
		
		pack();
	}
	
	
	/**
	 * Make the button bar
	 * @return the button bar
	 */
	protected Component makeButtonBar() {
		final Box bar = new Box( BoxLayout.X_AXIS );
		
		final JButton revertButton = new JButton( "Revert" );
		revertButton.setToolTipText( "Revert to the fields to the initial values." );
		revertButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					_orbitModel.getFlattener().revertCorrections();
					CorrectionDialog.this.setVisible( false );
				}
				catch( Exception exception ) {
                    exception.printStackTrace();
					JOptionPane.showMessageDialog( CorrectionDialog.this, exception.getMessage(), "Error reverting correctors", JOptionPane.ERROR_MESSAGE );
				}
			}
		});
		
		final JButton cancelButton = new JButton( "Cancel" );
		cancelButton.setToolTipText( "Cancel the correction and dismiss the dialog." );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				CorrectionDialog.this.setVisible( false );
			}
		});
		
		final JSpinner fractionSpinner = new JSpinner( new SpinnerNumberModel( 1.0, 0.0, 1.0, 0.05 ) );		// default to 100% with a range of 0 to 100% and increment of 5%
		fractionSpinner.setEditor( new JSpinner.NumberEditor( fractionSpinner, "0%" ) );	// display the fraction as a percent
		fractionSpinner.setToolTipText( "Fraction of the correction to load." );
		
		final JButton applyButton = new JButton( "Apply" );
		applyButton.setToolTipText( "Apply the specified fraction of correction." );
		applyButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				loadCorrection( fractionSpinner );
			}
		});
		
		final JButton okayButton = new JButton( "Okay" );
		okayButton.setToolTipText( "Apply the specified fraction of correction and dismiss the dialog." );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				loadCorrection( fractionSpinner );
				CorrectionDialog.this.setVisible( false );
			}
		});
		
		bar.add( Box.createHorizontalGlue() );
		bar.add( revertButton );
		bar.add( cancelButton );
		bar.add( Box.createHorizontalStrut( 10 ) );
		bar.add( fractionSpinner );
		bar.add( applyButton );
		bar.add( okayButton );
		bar.add( Box.createHorizontalStrut( 10 ) );
		
		getRootPane().setDefaultButton( okayButton );
		
		return bar;
	}
	
	
	/** load the correction based on the fraction in the spinner */
	private void loadCorrection( final JSpinner fractionSpinner ) {
		try {
			final Number fractionToLoad = (Number)fractionSpinner.getValue();
			_orbitModel.getFlattener().applyCorrections( fractionToLoad.doubleValue() );
		}
		catch( Exception exception ) {
            exception.printStackTrace();
			JOptionPane.showMessageDialog( CorrectionDialog.this, exception.getMessage(), "Error applying corrections", JOptionPane.ERROR_MESSAGE );
		}		
	}
	
	
	
	/** table of initial and proposed corrector strengths */
	protected class CorrectorTableModel extends AbstractTableModel {
                
        final private static long serialVersionUID = 1L;
        
		final protected int LABEL_COLUMN = 0;
		final protected int INITIAL_FIELD_COLUMN = 1;
		final protected int PROPOSED_FIELD_COLUMN = 2;
		
		
		/** Get the number of rows */
		public int getRowCount() {
			return _orbitModel.getFlattener().getCorrectorSuppliesToVary().size();
		}
		
		
		/** Get the number of columns to display */
		public int getColumnCount() {
			return 3;
		}
		
		
		/** Get the column class */
		public Class<?> getColumnClass( final int column ) {
			switch( column ) {
				case LABEL_COLUMN:
					return String.class;
				case INITIAL_FIELD_COLUMN:
					return Number.class;
				case PROPOSED_FIELD_COLUMN:
					return Number.class;
				default:
					return String.class;
			}
		}
		
		
		/** Get the title for the specified column */
		public String getColumnName( final int column ) {
			switch( column ) {
				case LABEL_COLUMN:
					return "Dipole";
				case INITIAL_FIELD_COLUMN:
					return "Initial Field (T)";
				case PROPOSED_FIELD_COLUMN:
					return "Proposed Field (T)";
				default:
					return "?";
			}
		}
		
		
		/** Get the value to display in the table cell */
		public Object getValueAt( final int row, final int column ) {
			final List<CorrectorSupply> supplies = _orbitModel.getFlattener().getCorrectorSuppliesToVary();
			if ( row >= supplies.size() )  return null;
			final CorrectorSupply correctorSupply = supplies.get( row );
			if ( correctorSupply == null )  return null;
			
			switch( column ) {
				case LABEL_COLUMN:
					return correctorSupply.getID();
				case INITIAL_FIELD_COLUMN:
					return new FormattedNumber( "0.00000", _orbitModel.getFlattener().getInitialCorrectorDistribution().getField( correctorSupply ) );
				case PROPOSED_FIELD_COLUMN:
					return new FormattedNumber( "0.00000", _orbitModel.getFlattener().getProposedCorrectorDistribution().getField( correctorSupply ) );
				default:
					return null;
			}
			
		}
	}
}



