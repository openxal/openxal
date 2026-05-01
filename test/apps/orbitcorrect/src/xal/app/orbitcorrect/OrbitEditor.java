//
//  OrbitEditor.java
//  xal
//
//  Created by Thomas Pelaia on 12/22/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import xal.tools.text.FormattedNumber;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;


/** 
 * View which allows the user to edit a mutable orbit
 * 
 *  TODO (CKA) - I don't understand enough class behavior to fix the Javadoc
 */
public class OrbitEditor extends JDialog {
	
       /**  serialization version */
    private static final long serialVersionUID = 1L;



    /** 
        * possible status upon closing the dialog 
        */
	public enum CloseStatus { 
	
	    /**  Save changes */
	    OKAY, 
	
	    /**  Cancel changes */
	    CANCELED 
	}
	
	/** the orbit being edited */
	final protected MutableOrbit _orbit;
	
	/** table of orbit settings */
	final protected JTable _orbitTable;
	
	/** table model for the orbit */
	final protected OrbitTableModel _orbitTableModel;
	
	/** get the close status */
	protected CloseStatus _closeStatus;
	
	
	/** Constructor 
	 * @param owner parent frame
	 * @param orbit I don't know*/
	public OrbitEditor( final Frame owner, final MutableOrbit orbit ) {
		super( owner, "Orbit Editor", true );
		_orbit = orbit;
		_closeStatus = CloseStatus.CANCELED;
		
		_orbitTableModel = new OrbitTableModel();
		_orbitTable = new JTable( _orbitTableModel );
		
		makeContent();
	}
	
	
	/** Constructor */
	public OrbitEditor( final Dialog owner, final MutableOrbit orbit ) {
		super( owner, "Orbit Editor", true );
		_orbit = orbit;
		_closeStatus = CloseStatus.CANCELED;
		
		_orbitTableModel = new OrbitTableModel();
		_orbitTable = new JTable( _orbitTableModel );
		
		makeContent();
	}
	
	
	/** make content */
	protected void makeContent() {
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		mainView.add( new JScrollPane( _orbitTable ) );
		mainView.add( createConfirmationBar() );
		
		pack();
	}
	
	
	/**
	 * Get the close status
	 * @return get the status upon closing the dialog
	 */
	public CloseStatus getStatus() {
		return _closeStatus;
	}
	
	
	/**
	 * Show this dialog window near the specified component
	 * @param owner the component near which to display this dialog
	 * @param orbit the orbit to edit
	 * @return OKAY or CANCELED depending on the user selection
	 */
	static public CloseStatus showEditor( final Frame owner, final MutableOrbit orbit ) {
		final OrbitEditor editor = new OrbitEditor( owner, orbit );
				
		return showEditor( editor );
	}
	
	
	/**
	 * Show this dialog window near the specified component
	 * @param owner the component near which to display this dialog
	 * @param orbit the orbit to edit
	 * @return OKAY or CANCELED depending on the user selection
	 */
	static public CloseStatus showEditor( final Dialog owner, final MutableOrbit orbit ) {
		final OrbitEditor editor = new OrbitEditor( owner, orbit );
		
		return showEditor( editor );
	}
	
	
	/**
	 * Setup and show this dialog window near its owner
	 * @param editor the editor to show
	 * @return OKAY or CANCELED depending on the user selection
	 */
	static private CloseStatus showEditor( final OrbitEditor editor ) {
		editor.setLocationRelativeTo( editor.getOwner() );
		editor.setVisible( true );
		
		return editor.getStatus();
	}

	
	/**
	 * Create the confirmation bar.
	 * @return   a bar of confirmation buttons
	 */
	protected Component createConfirmationBar() {
		Box confirmBar = new Box( BoxLayout.X_AXIS );
		confirmBar.setBorder( BorderFactory.createEtchedBorder() );
		
		confirmBar.add( Box.createHorizontalGlue() );
		
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				_closeStatus = CloseStatus.CANCELED;
				setVisible( false );
				dispose();
			}
		});
		confirmBar.add( cancelButton );
		
		JButton okayButton = new JButton( "Okay" );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent event ) {
				_closeStatus = CloseStatus.OKAY;
				applyChanges();
				setVisible( false );
				dispose();
			}
		});
		confirmBar.add( okayButton );
		getRootPane().setDefaultButton( okayButton );
		
		return confirmBar;
	}
	
	
	/** Apply the user's changes to the mutable orbit */
	protected void applyChanges() {
		final List<BpmRecord> records = _orbitTableModel.getRecords();
		for ( BpmRecord record : records ) {
			_orbit.addRecord( record );
		}
	}
	
	
	
	/** table model of knob elements */
	protected class OrbitTableModel extends AbstractTableModel {
        
        private static final long serialVersionUID = 1L;
        
		final protected int BPM_COLUMN = 0;
		final protected int POSITION_COLUMN = 1;
		final protected int XAVG_COLUMN = 2;
		final protected int YAVG_COLUMN = 3;
		
		final protected List<BpmRecord> _records;
		
		
		/** Constructor */
		public OrbitTableModel() {
			_records = _orbit.getRecords();
		}
		
		
		/**
		 * Get the records.
		 * @return the records
		 */
		public List<BpmRecord> getRecords() {
			synchronized( _records ) {
				return _records;
			}
		}
		
		
		/** get the number of rows */
		public int getRowCount() {
			synchronized( _records ) {
				return _records.size();
			}
		}
		
		
		/** get the number of columns */
		public int getColumnCount() {
			return 4;
		}
		
		
		/** get the column name */
		@Override
        public String getColumnName( final int column ) {
			switch ( column ) {
				case BPM_COLUMN:
					return "BPM";
				case POSITION_COLUMN:
					return "Position";
				case XAVG_COLUMN:
					return "X Average";
				case YAVG_COLUMN:
					return "Y Average";
				default:
					return "?";
			}
		}
		
		
		/** get the class for values associated with the specified column */
		@Override
        public Class<?> getColumnClass( final int column ) {
			switch ( column ) {
				case BPM_COLUMN:
					return String.class;
				case POSITION_COLUMN:
					return FormattedNumber.class;
				case XAVG_COLUMN:
					return FormattedNumber.class;
				case YAVG_COLUMN:
					return FormattedNumber.class;
				default:
					return String.class;
			}
		}
		
		
		/** determine if the table cell is editable */
		@Override
        public boolean isCellEditable( final int row, final int column ) {
			switch ( column ) {
				case XAVG_COLUMN:
					return true;
				case YAVG_COLUMN:
					return true;
				default:
					return false;
			}
		}
		
		
		/** get the value at the specified row and column */
		public Object getValueAt( final int row, final int column ) {
			BpmRecord record;
			
			synchronized( _records ) {
				if ( row >= _records.size() )  return null;
				record = _records.get( row );
				if ( record == null )  return null;
			}
				
			switch ( column ) {
				case BPM_COLUMN:
					return record.getBpmID();
				case POSITION_COLUMN:
					return new FormattedNumber( "0.0000", record.getPositionIn( _orbit.getSequence() ) );
				case XAVG_COLUMN:
					return new FormattedNumber( "0.0", record.getXAvg() );
				case YAVG_COLUMN:
					return new FormattedNumber( "0.0", record.getYAvg() );
				default:
					return "?";
			}
		}
		
		
		/** Set the value for the specified table cell */
		@Override
        public void setValueAt( final Object value, final int row, final int column ) {
			synchronized( _records ) {
				if ( row >= _records.size() )  return;
				final BpmRecord record = _records.get( row );
				if ( record == null )  return;
				
				final double doubleValue = ((Number)value).doubleValue();
				BpmRecord newRecord;
				
				switch ( column ) {
					case XAVG_COLUMN:
						newRecord = new BpmRecord( record, record.getTimestamp(), doubleValue, record.getYAvg() );
						break;
					case YAVG_COLUMN:
						newRecord = new BpmRecord( record, record.getTimestamp(), record.getXAvg(), doubleValue );
						break;
					default:
						return;
				}
				
				_records.remove( row );
				_records.add( row, newRecord );
			}			
		}
	}
}
