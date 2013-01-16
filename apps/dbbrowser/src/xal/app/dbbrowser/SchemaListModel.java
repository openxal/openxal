/*
 * SchemaListModel.java
 *
 * Created on Fri Feb 20 13:58:56 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;

import javax.swing.*;


/**
 * SchemaListModel
 *
 * @author  tap
 */
public class SchemaListModel extends AbstractListModel<String> {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** Browser model providing the list of database tables */
	protected BrowserModel _model;
	
	
	/**
	 * TableListModel constructor
	 */
	public SchemaListModel(BrowserModel aModel) {
		_model = aModel;
		
		_model.addBrowserModelListener( new BrowserModelListener() {
			/**
			 * Database schema changed notification
			 * @param model The browser model whose database schema changed
			 * @param newSchema The new database schema
			 */
			public void schemaChanged(BrowserModel model, String newSchema) {}
			
			
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
	 * Get the number of schemas for the selected schema
	 * @return The number of schemas to display
	 */
	public int getSize() {
		return _model.getSchemas().size();
	}
	
	
	/**
	 * Get the schema name for the specified schema index
	 * @param index The index of the schema to display
	 * @return the name of the schema for the specified index
	 */
	public String getElementAt(int index) {
		return _model.getSchemas().get(index);
	}
}

