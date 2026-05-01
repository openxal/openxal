/*
 * DatabaseException.java
 *
 * Created on Thu Feb 19 08:44:11 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.database;

import java.sql.SQLException;


/**
 * DatabaseException wraps SQLException exceptions thrown while interacting with the database
 * Eventually this class will provide a common interpretation of SQLException codes regardless
 * of the database source.
 *
 * @author  tap
 */
public class DatabaseException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    
	/**
	 * Constructor to wrap an SQLException
	 * @param message The exception message
	 * @param adaptor The database adaptor to use to interpret the SQLException
	 * @param cause The cause of the exception
	 */
	public DatabaseException(String message, DatabaseAdaptor adaptor, SQLException cause) {
		super(message, cause);
	}
	
	
	/**
	 * Get the database specific error code.
	 * @return the error code of the SQLException which is the cause of this database exception
	 */
	public int getRawErrorCode() {
		return ((SQLException)getCause()).getErrorCode();
	}
}

