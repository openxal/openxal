/*
 * DataWindow.java
 *
 * Created on Fri Feb 27 11:54:57 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import xal.extension.widgets.swing.KeyValueFilteredTableModel;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;


/**
 * DataWindow is a window for displaying the data from a database table query
 * @author  tap
 */
public class DataWindow extends JDialog implements ScrollPaneConstants {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/**
	 * DataWindow Constructor
	 */
	public DataWindow( final JFrame owner, final String title, final List<Map<String,Object>> records, final List<TableAttribute> attributes ) {
		super(owner, title, false);
		setLocationRelativeTo(owner);
		makeContents( records, attributes );
	}
	
	
	/**
	 * Make the contents of the data window
	 */
	protected void makeContents( final List<Map<String,Object>> records, final List<TableAttribute> attributes ) {
		setSize( 700, 500 );
		final Box mainView = new Box( BoxLayout.Y_AXIS );
		getContentPane().add( mainView );
		
		final Box searchBox = new Box( BoxLayout.X_AXIS );
		mainView.add( searchBox );
		final JTextField filterField = new JTextField();
		searchBox.add( filterField );
		filterField.setMaximumSize( new java.awt.Dimension( 32000, 50 ) );
		filterField.putClientProperty( "JTextField.variant", "search" );
		filterField.putClientProperty( "JTextField.Search.Prompt", "Filter Records" );
		
		final int attributeCount = attributes.size();
		final String[] keyPaths = new String[attributeCount];
		for ( int attributeIndex = 0 ; attributeIndex < attributeCount ; attributeIndex++ ) {
			keyPaths[attributeIndex] = attributes.get( attributeIndex ).name;
		}
		final KeyValueFilteredTableModel<Map<String,Object>> tableModel = new KeyValueFilteredTableModel<Map<String,Object>>( records, keyPaths );
		tableModel.setInputFilterComponent( filterField );
		final JTable dataTable = new JTable( tableModel );
		dataTable.setAutoCreateRowSorter( true );
		dataTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		JScrollPane scrollPane = new JScrollPane( dataTable );
		JViewport headerViewport = new JViewport();
		headerViewport.setView( dataTable.getTableHeader() );
		scrollPane.setColumnHeader(headerViewport);
		mainView.add(scrollPane);
		// right justify numeric values
        final TableColumnModel columnModel = dataTable.getColumnModel();
		for ( int index = 0 ; index < attributeCount ; index++ ) {
			final TableAttribute attribute = attributes.get(index);
			switch( attribute.dataType ) {
				case java.sql.Types.NUMERIC: 
				case java.sql.Types.INTEGER: 
				case java.sql.Types.BIGINT: 
				case java.sql.Types.DECIMAL: 
				case java.sql.Types.FLOAT: 
				case java.sql.Types.REAL: 
				case java.sql.Types.DOUBLE: 
					columnModel.getColumn( index ).setCellRenderer( getNumericCellRenderer() );
					break;
				default:
					break;
			}
		}
	}
    
    
    /**
     * Right justify text associated with numeric values.
     * @return A renderer for numeric values.
     */
    private TableCellRenderer getNumericCellRenderer() {
        return new DefaultTableCellRenderer() {
            /** serialization identifier */
            private static final long serialVersionUID = 1L;
            
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(RIGHT);
				label.setForeground(java.awt.Color.blue);
                return label;
            }
        };
    }
}

