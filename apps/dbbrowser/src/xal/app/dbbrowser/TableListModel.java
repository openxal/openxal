/*
 * TableListModel.java
 *
 * Created on Fri Feb 20 13:49:36 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import javax.swing.*;


/**
 * Model for generating the list of tables for the selected schema.
 * @author t6p
 */
class TableListModel extends AbstractListModel<String> {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** Browser model providing the list of database tables */
	protected BrowserModel _model;
	
	
	/**
	 * TableListModel constructor
	 */
	public TableListModel(BrowserModel aModel) {
		_model = aModel;
		
		_model.addBrowserModelListener( new BrowserModelListener() {
			/**
			 * Database schema changed notification
			 * @param model The browser model whose database schema changed
			 * @param newSchema The new database schema
			 */
			public void schemaChanged(BrowserModel model, String newSchema) {
				fireContentsChanged(this, 0, getSize());
			}
			
			/**
			 * Database table changed notification
			 * @param model The browser model whose database table changed
			 * @param newTable The new database table
			 */
			public void tableChanged(BrowserModel model, String newTable) {}
			
			
			/**
			 * The model's connection has changed
			 * @param model The model whose connection changed
			 */
			public void connectionChanged(BrowserModel model) {
				fireContentsChanged(this, 0, getSize());
			}
		});
	}
	
	
	/**
	 * Get the number of tables for the selected schema
	 * @return The number of tables to display
	 */
	public int getSize() {
		return _model.getTables().size();
	}
	
	
	/**
	 * Get the table name for the selected table
	 * @param index The index of the table to display
	 * @return the name of the table for the specified index
	 */
	public String getElementAt(int index) {
		return _model.getTables().get(index);
	}
}

