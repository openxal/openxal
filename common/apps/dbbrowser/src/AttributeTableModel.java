/*
 * AttributeTableModel.java
 *
 * Created on Fri Feb 20 10:48:20 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import javax.swing.table.*;
import java.util.*;


/**
 * AttributeTableModel is the Table model for displaying database table attributes
 *
 * @author  tap
 */
public class AttributeTableModel extends AbstractTableModel implements BrowserModelListener {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** enum for the name column */
	final static protected int NAME_COLUMN = 0;
	
	/** enum for the primary key column */
	final static protected int PRIMARY_KEY_COLUMN = 1;
	
	/** enum for the nullable column (displays if the table attribute is nullable) */
	final static protected int NULLABLE_COLUMN = 2;
	
	/** enum for the type column */
	final static protected int TYPE_COLUMN = 3;
	
	/** enum for the width column */
	final static protected int WIDTH_COLUMN = 4;
	
	
	/** browser model*/
	protected BrowserModel _browserModel;
	
	/** List of the attributes to display */
	protected List<TableAttribute> _attributes;
	
	
	/**
	 * AttributeTableModel constructor
	 */
	public AttributeTableModel(BrowserModel browserModel) {
		_browserModel = browserModel;
		_browserModel.addBrowserModelListener(this);
		_attributes = _browserModel.getTableAttributes();
	}
	
	
	/**
	 * The model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged(BrowserModel model) {
		_attributes = _browserModel.getTableAttributes();
		fireTableDataChanged();
	}
	
	
	/**
	 * Database schema changed notification
	 * @param model The browser model whose database schema changed
	 * @param newSchema The new database schema
	 */
	public void schemaChanged(BrowserModel model, String newSchema) {
		_attributes = _browserModel.getTableAttributes();
		fireTableDataChanged();
	}
	
	
	/**
	 * Database table changed notification
	 * @param model The browser model whose database table changed
	 * @param newTable The new database table
	 */
	public void tableChanged(BrowserModel model, String newTable) {
		_attributes = _browserModel.getTableAttributes();
		fireTableDataChanged();
	}
    
    
    /**
     * Get the number of table rows.
     * @return The number of table rows.
     */
    public int getRowCount() {
        return _browserModel.getTableAttributes().size();
    }
    
    
    /**
     * Get the number of table columns.
     * @return The number of table columns.
     */
    public int getColumnCount() {
        return 5;
    }
    
    
    /**
     * Get the value to display for the table cell at the specified row and column.
     * @param row The table cell row.
     * @param column The table cell column.
     * @return The value to display for the specified table cell.
     */
    public Object getValueAt(int row, int column) {
        switch(column) {
            case NAME_COLUMN:
                return _attributes.get(row).name;
            case TYPE_COLUMN:
                return _attributes.get(row).type;
            case WIDTH_COLUMN:
                return new Integer( _attributes.get(row).width );
            case NULLABLE_COLUMN:
                return _attributes.get(row).nullable;
            case PRIMARY_KEY_COLUMN:
                return new Boolean( _attributes.get(row).isPrimaryKey );
            default:
                return "";
        }
        
    }
    
    
    /** 
     * Get the title of the specified column.
     * @param column The index of the column.
     * @return The title for the specified column.
     */
    public String getColumnName( final int column ) {
        switch(column) {
            case NAME_COLUMN:
                return "Name";
            case TYPE_COLUMN:
                return "Type";
            case WIDTH_COLUMN:
                return "Width";
            case NULLABLE_COLUMN:
                return "Nullable";
           case PRIMARY_KEY_COLUMN:
                return "Primary Key";
            default:
                return "";
        }
    }
}

