/*
 * BrowserModelListener.java
 *
 * Created on Fri Feb 20 10:14:28 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.dbbrowser;


/**
 * BrowserModelListener is a notification interface for browser model events.
 *
 * @author  tap
 */
public interface BrowserModelListener {
	/**
	 * The model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged(BrowserModel model);
	
	
	/**
	 * Database schema changed notification
	 * @param model The browser model whose database schema changed
	 * @param newSchema The new database schema
	 */
	public void schemaChanged(BrowserModel model, String newSchema);
	
	
	/**
	 * Database table changed notification
	 * @param model The browser model whose database table changed
	 * @param newTable The new database table
	 */
	public void tableChanged(BrowserModel model, String newTable);
}

