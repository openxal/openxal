//
//  KnobEditor.java
//  xal
//
//  Created by Thomas Pelaia on 12/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import xal.tools.text.FormattedNumber;
import xal.tools.IconLib;
import xal.extension.widgets.smf.*;
import xal.smf.*;

import java.util.*;
import java.text.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.Window;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.table.*;


/** view for editing knob detail */
public class KnobEditor extends Box implements KnobListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	/** the knob to edit */
	final protected Knob _knob;
	
	/** displays and hides the knob editor */
	final protected KnobEditDisplayer EDIT_DISPLAYER;
	
	/** table model of knob elements */
	final protected KnobElementTableModel _elementTableModel;
	
	/** table of knob elements */
	final protected JTable _elementTable;
	
	/** titled border */
	final protected TitledBorder TITLED_BORDER;
	
	
	/** Constructor */
	public KnobEditor( final Knob knob, KnobEditDisplayer editDisplayer ) {
		super( BoxLayout.X_AXIS );
		
		_knob = knob;
		EDIT_DISPLAYER = editDisplayer;
		TITLED_BORDER = new TitledBorder( knob.getName() + " Editor" );
		
		setBorder( TITLED_BORDER );
		
		_elementTableModel = new KnobElementTableModel();
		_elementTable = new JTable( _elementTableModel );
		
		buildView();
		
		knob.addKnobListener( this );
	}
	
	
	/** get the knob being edited */
	public Knob getKnob() {
		return _knob;
	}
	
	
	/** build view */
	protected void buildView() {
		add( buildKnobElementTableView() );
	}
	
	
	/** build knob element table view */
	protected Component buildKnobElementTableView() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		view.add( new JScrollPane( _elementTable ) );
		view.add ( buildBottomEditingRow() );
		return view;
	}
	
	
	/** build the add/remove button row */
	protected Component buildBottomEditingRow() {
		final Box row = new Box( BoxLayout.X_AXIS );
		final JButton removeButton = new JButton ( IconLib.getIcon( IconLib.IconGroup.TABLE, "RowDelete24.gif" ) );
		removeButton.setToolTipText( "Remove selected knob elements." );
		final JButton addButton = new JButton( IconLib.getIcon( IconLib.IconGroup.TABLE, "RowInsertAfter24.gif" ) );
		addButton.setToolTipText( "Add a new knob element." );
		
		addButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final KnobElement element = new KnobElement();
				_knob.addElement( element );
				_elementTableModel.fireTableDataChanged();
			}
		});
		
		removeButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final List<KnobElement> elements = new ArrayList<KnobElement>( _knob.getElements() );
				final int[] selectedRows = _elementTable.getSelectedRows();
				for ( int row : selectedRows ) {
					final KnobElement element = elements.get( row );
					_knob.removeElement( element );
				}
				_elementTableModel.fireTableDataChanged();
			}
		});
		
		final JButton plusButton = new JButton( "+" );
		plusButton.setToolTipText( "Add channels from a list in the sequence." );
		plusButton.addActionListener( nodeChannelSelectionHandler() );
				
		row.add( removeButton );
		row.add( addButton );
		row.add( plusButton );
		row.add( Box.createHorizontalGlue() );
		
		final JButton proportionalButton = new JButton( "Proportional" );
		row.add( proportionalButton );
		proportionalButton.setToolTipText( "Make coefficients equal to their values and set the knob value to 1.0" );
		proportionalButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_knob.makeProportionalCoefficients();
			}
		} );
		
		final JButton magicButton = new JButton( "Magic" );
		row.add( magicButton );
		magicButton.setToolTipText( "Generate knob element coefficients based on two machine states." );
		magicButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				new KnobMagician( (JFrame)SwingUtilities.getRoot( KnobEditor.this ) ).display( _knob );
			}
		} );
		
		final JButton fillButton = new JButton( "Fill Down" );
		row.add( fillButton );
		fillButton.setToolTipText( "Apply the coefficient of the first selected element to all subsequent selected elements." );
		fillButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final int[] rows = _elementTable.getSelectedRows();
				if ( rows.length > 1 ) {	// only meaninful if more than one row is selected
					final KnobElement prototypeElement = _elementTableModel.getKnobElement( rows[0] );
					final double coefficient = prototypeElement.getCoefficient();
					final boolean usingCustomLimits = prototypeElement.isUsingCustomLimits();
					final boolean wrapsValueAroundLimits = prototypeElement.wrapsValueAroundLimits();
					final double lowerLimit = prototypeElement.getLowerLimit();
					final double upperLimit = prototypeElement.getUpperLimit();
					for ( int rindex = 1 ; rindex < rows.length ; rindex++ ) {
						final int row = rows[rindex];
						final KnobElement element = _elementTableModel.getKnobElement( row );
						element.setCoefficient( coefficient );
						element.setUsingCustomLimits( usingCustomLimits );
						// only fill down custom limits if prototype uses custom limits
						if ( usingCustomLimits ) {
							element.setCustomLowerLimit( lowerLimit );
							element.setCustomUpperLimit( upperLimit );
						}
						element.setWrapsValueAroundLimits( wrapsValueAroundLimits );
					}
				}
			}
		} );
		
		row.add( Box.createHorizontalGlue() );
		
		final JButton doneButton = new JButton( "Done" );
		doneButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				EDIT_DISPLAYER.hide( KnobEditor.this );
			}
		});
		row.add( doneButton );
		
		return row;
	}
	
	
	/** handler of node channel selection request */
	private ActionListener nodeChannelSelectionHandler() {
		return new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final Accelerator accelerator = _knob.getAccelerator();
				final List<AcceleratorNode> nodes = accelerator.getAllNodes( true );
				final NodeChannelSelector channelRefSelector = NodeChannelSelector.getInstanceFromNodes( nodes, (JFrame)SwingUtilities.windowForComponent(KnobEditor.this), "Select Channels" );
				final List<NodeChannelRef> channelRefs = channelRefSelector.showDialog();
				if ( channelRefs != null ) {
					for ( NodeChannelRef channelRef : channelRefs ) {
						final KnobElement element = new KnobElement();
						element.setNodeChannelRef( channelRef );
						_knob.addElement( element );
					}
					_elementTableModel.fireTableDataChanged();
				}
			}
		};
	}
	
	
	/** revresh this view */
	public void refresh() {
		_elementTableModel.fireTableDataChanged();
	}
		
	
	/** event indicating that the specified knob's name has changed */
	public void nameChanged( final Knob knob, final String newName ) {
		TITLED_BORDER.setTitle( knob.getName() + " Editor" );
		SwingUtilities.getRoot( this ).validate();
		SwingUtilities.getRoot( this ).repaint();
	}
	
	
	/** ready state changed */
	public void readyStateChanged( final Knob knob, final boolean isReady ) {
	}
	
	
	/** event indicating that the knob's limits have changed */
	public void limitsChanged( final Knob knob, final double lowerLimit, final double upperLimit ) {
	}
	
	
	/** event indicating that the knob's current value setting has changed */
	public void currentSettingChanged( final Knob knob, final double value ) {
	}
	
	
	/** event indicating that the knob's most previously pending set operation has completed */
	public void valueSettingPublished( final Knob knob ) {}
	
	
	/** event indicating that an element has been added */
	public void elementAdded( final Knob knob, final KnobElement element ) {
		_elementTableModel.fireTableDataChanged();
	}
	
	
	/** event indicating that an element has been removed */
	public void elementRemoved( final Knob knob, final KnobElement element ) {
		_elementTableModel.fireTableDataChanged();
	}
	
	
	/** event indicating that the specified knob element has been modified */
	public void elementModified( final Knob knob, final KnobElement element ) {
		_elementTableModel.fireTableDataChanged();
	}
	
	
	
	/** table model of knob elements */
	protected class KnobElementTableModel extends AbstractTableModel {
        /** serialization identifier */
        private static final long serialVersionUID = 1L;
		final protected int PV_COLUMN = 0;
		final protected int COEFFICIENT_COLUMN = PV_COLUMN + 1;
		final protected int CUSTOM_LIMITS_COLUMN = COEFFICIENT_COLUMN + 1;
		final protected int WRAPS_AROUND_LIMITS_COLUMN = CUSTOM_LIMITS_COLUMN + 1;
		final protected int LOWER_LIMIT_COLUMN = WRAPS_AROUND_LIMITS_COLUMN + 1;
		final protected int UPPER_LIMIT_COLUMN = LOWER_LIMIT_COLUMN + 1;
		final private int COLUMN_COUNT = UPPER_LIMIT_COLUMN + 1;
		
		
		/** Constructor */
		public KnobElementTableModel() {}
		
		
		/** get the number of rows */
		public int getRowCount() {
			return _knob.getElements().size();
		}
		
		
		/** get the number of columns */
		public int getColumnCount() {
			return COLUMN_COUNT;
		}
		
		
		/** get the knob element at the specified row */
		public KnobElement getKnobElement( final int row ) {
			return _knob.getElements().get( row );
		}
		
		
		/** get the column name */
		public String getColumnName( final int column ) {
			switch ( column ) {
				case PV_COLUMN:
					return "PV";
				case COEFFICIENT_COLUMN:
					return "Coefficient";
				case CUSTOM_LIMITS_COLUMN:
					return "Custom Limits";
				case WRAPS_AROUND_LIMITS_COLUMN:
					return "Wraps Around Limits";
				case LOWER_LIMIT_COLUMN:
					return "Lower Limit";
				case UPPER_LIMIT_COLUMN:
					return "Upper Limit";
				default:
					return "?";
			}
		}
		
		
		/** get the class for values associated with the specified column */
		public Class<?> getColumnClass( final int column ) {
			switch ( column ) {
				case PV_COLUMN:
					return String.class;
				case COEFFICIENT_COLUMN:
					return FormattedNumber.class;
				case CUSTOM_LIMITS_COLUMN:
					return Boolean.class;
				case WRAPS_AROUND_LIMITS_COLUMN:
					return Boolean.class;
				case LOWER_LIMIT_COLUMN:
					return FormattedNumber.class;
				case UPPER_LIMIT_COLUMN:
					return FormattedNumber.class;
				default:
					return String.class;
			}
		}
		
		
		/** determine if the table cell is editable */
		public boolean isCellEditable( final int row, final int column ) {
			final KnobElement element = getKnobElement( row );
			
			switch ( column ) {
				case PV_COLUMN:
					return true;
				case COEFFICIENT_COLUMN:
					return true;
				case CUSTOM_LIMITS_COLUMN:
					return true;
				case WRAPS_AROUND_LIMITS_COLUMN:
					return true;
				case LOWER_LIMIT_COLUMN:
					return element != null && element.isUsingCustomLimits();
				case UPPER_LIMIT_COLUMN:
					return element != null && element.isUsingCustomLimits();
				default:
					return false;
			}
		}
		
		
		/** get the value at the specified row and column */
		public Object getValueAt( final int row, final int column ) {
			final KnobElement element = getKnobElement( row );
			if ( element == null )  return null;
			
			switch ( column ) {
				case PV_COLUMN:
					return element.getChannelString();
				case COEFFICIENT_COLUMN:
					return new FormattedNumber( element.getCoefficient() );
				case CUSTOM_LIMITS_COLUMN:
					return element.isUsingCustomLimits();
				case WRAPS_AROUND_LIMITS_COLUMN:
					return element.wrapsValueAroundLimits();
				case LOWER_LIMIT_COLUMN:
					return new FormattedNumber( element.getLowerLimit() );
				case UPPER_LIMIT_COLUMN:
					return new FormattedNumber( element.getUpperLimit() );
				default:
					return "?";
			}
		}
		
		
		/** Set the value for the specified table cell */
		public void setValueAt( final Object value, final int row, final int column ) {
			final KnobElement element = getKnobElement( row );
			if ( element == null )  return;
			
			switch ( column ) {
				case PV_COLUMN:
					setElementChannel( element, value.toString().trim() );
					break;
				case COEFFICIENT_COLUMN:
					final double coefficient = ((Number)value).doubleValue();
					element.setCoefficient( coefficient );
					break;
				case CUSTOM_LIMITS_COLUMN:
					element.setUsingCustomLimits( (Boolean)value );
					break;
				case WRAPS_AROUND_LIMITS_COLUMN:
					element.setWrapsValueAroundLimits( (Boolean)value );
					break;
				case LOWER_LIMIT_COLUMN:
					element.setCustomLowerLimit( ((Number)value).doubleValue() );
					break;
				case UPPER_LIMIT_COLUMN:
					element.setCustomUpperLimit( ((Number)value).doubleValue() );
					break;
				default:
					break;
			}
		}
		
		
		/** set the specified element's channel */
		protected void setElementChannel( final KnobElement element, final String channelString ) {
			final Accelerator accelerator = _knob.getAccelerator();
			final NodeChannelRef channelRef = accelerator != null ? NodeChannelRef.getInstance( accelerator, channelString ) : null;
			if ( channelRef != null ) {
				element.setNodeChannelRef( channelRef );
			}
			else {
				element.setPV( channelString );
			}
		}
	}
}


