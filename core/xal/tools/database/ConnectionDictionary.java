/*
 * ConnectionDictionary.java
 *
 * Created on Thu Dec 18 11:01:11 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.database;

import java.util.logging.*;
import java.util.Properties;
import java.util.prefs.*;
import java.io.*;
import java.net.*;


/**
 * ConnectionDictionary contains properties that can be used to establish a database connection.
 * It should contain at least the database URL, user name and password.  A default connection dictionary
 * can be fetched from a file specified in the user's preferences.
 *
 * @author  tap
 */
public class ConnectionDictionary extends Properties {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	// public dictionary keys
	static final public String USER_KEY = "user";
	static final public String PASSWORD_KEY = "password";
	static final public String URL_KEY = "url";
	static final public String DATABASE_ADAPTOR_KEY = "database_adaptor";
	
	
	/**
	 * Constructor
	 */
	public ConnectionDictionary() {
		super();
	}
	
	
	/**
	 * Get the connection dictionary from the file specified in the user's preferences.
	 * @return the user's default connection dictionary
	 */
	static public ConnectionDictionary defaultDictionary() {
		return getInstance();
	}
	
	
	/**
	 * Get the connection dictionary from the URL specified in the user's preferences.
	 * @return the user's default connection dictionary
	 */
	static public ConnectionDictionary getInstance() {
		final DBConfiguration configuration = DBConfiguration.getInstance();
		return configuration != null ? configuration.defaultConnectionDictionary() : null;
	}
	
	
	/**
	 * Get the connection dictionary from the URL specified in the user's preferences and for the specified account.
	 * @param accountName name of the account for which to initializae the connection dictionary (or null to use the default account if any)
	 * @return the user's default connection dictionary
	 */
	static public ConnectionDictionary getInstance( final String accountName ) {
		final DBConfiguration configuration = DBConfiguration.getInstance();
		return configuration != null ? configuration.newConnectionDictionary( accountName ) : null;
	}
	
	
	/**
	 * Get the available connection dictionary which is the most preferred
	 * @param accountNames ordered (most preferred is first) accounts to search among
	 */
	static public ConnectionDictionary getPreferredInstance( final String ... accountNames ) {
		final DBConfiguration configuration = DBConfiguration.getInstance();
		return configuration != null ? configuration.availableConnectionDictionary( accountNames ) : null;
	}
	
	
	/**
	 * Get the connection dictionary from the URL specified in the user's preferences and for the specified account and server.
	 * @param accountName name of the account for which to initializae the connection dictionary (or null to use the default account if any)
	 * @param serverName name of the database server for which to initialize the connection dictionary (or null to use the default server if any)
	 * @return the user's default connection dictionary
	 */
	static public ConnectionDictionary getInstance( final String accountName, final String serverName ) {
		final DBConfiguration configuration = DBConfiguration.getInstance();
		return configuration != null ? configuration.newConnectionDictionary( accountName, serverName ) : null;
	}
	
	
	/**
	 * Determine if the dictionary is sufficiently complete regardless whether the data is right or wrong.
	 * @return true if the dictionary contains the user name, password and database URL (database adaptor is optional)
	 */
	public boolean hasRequiredInfo() {
		return getUser() != null && getPassword() != null && getURLSpec() != null;
	}
	
	
	/**
	 * Get the user name for connecting
	 * @return the user name
	 */
	public String getUser() {
		return getProperty(USER_KEY);
	}
	
	
	/**
	 * Set the user ID
	 * @param userID The user ID
	 */
	public void setUser(String userID) {
		setProperty(USER_KEY, userID);
	}
	
	
	/**
	 * Get the user password for connecting
	 * @return the user password
	 */
	public String getPassword() {
		return getProperty(PASSWORD_KEY);
	}
	
	
	/**
	 * Set the password
	 * @param password the password
	 */
	public void setPassword( final String password ) {
		setProperty(PASSWORD_KEY, password);
	}
	
	
	/**
	 * Get the connection URL
	 * @return the connection URL
	 */
	public String getURLSpec() {
		return getProperty(URL_KEY);
	}
	
	
	/**
	 * Set the URL spec
	 * @param urlSpec The URL spec
	 */
	public void setURLSpec( final String urlSpec ) {
		setProperty(URL_KEY, urlSpec);
	}
	
	
	/**
	 * Get the database adaptor to use
	 * @return the database adaptor
	 */
	public DatabaseAdaptor getDatabaseAdaptor() {
		final String className = getProperty( DATABASE_ADAPTOR_KEY );
		if ( className == null || className.equals( "" ) )  return null;
		try {
			final Class<?> databaseAdaptorClass = Class.forName( className );
			return (DatabaseAdaptor)databaseAdaptorClass.newInstance();
		}
		catch(Exception exception) {
			final String message = "Failed to instantiate database adaptor for class:  " + className;
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
			throw new RuntimeException( message, exception );
		}
	}
	
	
	/**
	 * Set the database adaptor class
	 * @param databaseAdaptorClass the database adaptor class to use
	 */
	public void setDatabaseAdaptorClass( final Class<?> databaseAdaptorClass ) {
		setDatabaseAdaptorClass( databaseAdaptorClass.getName() );
	}
	
	
	/**
	 * Set the database adaptor class
	 * @param className the database adaptor class name to use
	 */
	public void setDatabaseAdaptorClass( final String className ) {
		setProperty(DATABASE_ADAPTOR_KEY, className);
	}
}

