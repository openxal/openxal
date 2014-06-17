//
//  DBConfiguration.java
//  xal
//
//  Created by Tom Pelaia on 7/23/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.database;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.data.DataAdaptor;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.*;


/** load a database configuration */
public class DBConfiguration {
	/** key for getting the URL from the preferences */
	final static protected String PREFERENCES_URL_KEY = "configURL";

	/** name of the default database adaptor */
	final private String DEFAULT_DATABASE_ADAPTOR_NAME;
	
	/** name of the default server */
	final private String DEFAULT_SERVER_NAME;
	
	/** name of the default account */
	final private String DEFAULT_ACCOUNT_NAME;

	/** table mapping database adaptor names to database adaptor class names */
	final private Map<String,String> DATABASE_ADAPTOR_MAP;
	
	/** table of servers keyed by name */
	final private Map<String,DBServerConfig> SERVERS;
	
	/** table of accounts keyed by name */
	final private Map<String,DBAccountConfig> ACCOUNTS;
	
	
	/** Primary Constructor */
	private DBConfiguration( final String defaultDBAdaptorName, final Map<String,String> dbAdaptorMap, final String defaultServerName, final Map<String,DBServerConfig> servers, final String defaultAccountName, final Map<String,DBAccountConfig> accounts ) {
		DEFAULT_DATABASE_ADAPTOR_NAME = defaultDBAdaptorName;
		DEFAULT_SERVER_NAME = defaultServerName;
		DEFAULT_ACCOUNT_NAME = defaultAccountName;

		DATABASE_ADAPTOR_MAP = dbAdaptorMap;
		SERVERS = servers;
		ACCOUNTS = accounts;
	}
	
	
	/** get the default server name */
	public String getDefaultServerName() {
		return DEFAULT_SERVER_NAME;
	}
	
	
	/** get the default account name */
	public String getDefaultAccountName() {
		return DEFAULT_ACCOUNT_NAME;
	}
	
	
	/** determine whether this configuration has the named account */
	public boolean hasAccount( final String accountName ) {
		return ACCOUNTS.containsKey( accountName );
	}
	
	
	/** determine whether this configuration has the named server */
	public boolean hasServer( final String serverName ) {
		return SERVERS.containsKey( serverName );
	}
	
	
	/** get an alpha-numerically ordered list of account names */
	public List<String> getAccountNames() {
		final Set<String> nameSet = ACCOUNTS.keySet();
		final List<String> names = new ArrayList<String>( nameSet );
		Collections.sort( names );
		return names;
	}
	
	
	/** get an alpha-numerically ordered list of server names */
	public List<String> getServerNames() {
		final Set<String> nameSet = SERVERS.keySet();
		final List<String> names = new ArrayList<String>( nameSet );
		Collections.sort( names );
		return names;
	}
	
	
	/** 
	 * Generate a new connection dictionary for the specified account name and server configuration name
	 * @param accountName name of the account for which to initializae the connection dictionary (or null to use the default account if any)
	 * @param serverName name of the database server for which to initialize the connection dictionary (or null to use the default server if any)
	 * @return a new connection dictionary from the database configuration
	 */
	public ConnectionDictionary newConnectionDictionary( final String accountName, final String serverName ) {
		final DBAccountConfig account = accountName != null && accountName.length() > 0 ? ACCOUNTS.get( accountName ) : ACCOUNTS.get( DEFAULT_ACCOUNT_NAME );

		// get the default server name for the account if any
		final String accountServerName = account != null ? account.getDefaultServerName() : null;

		// resolve the server name to use by first selecting the serverName parameter if not null, then falling back to the account's default server name if not null and finally falling back to the DEFAULT_SERVER_NAME
		final String resolvedServerName = serverName != null && serverName.length() > 0 ? serverName : accountServerName != null && accountServerName.length() > 0 ? accountServerName : DEFAULT_SERVER_NAME;

		// get the resolved server configuration
		final DBServerConfig serverConfig = SERVERS.get( resolvedServerName );
		
		final ConnectionDictionary connectionDictionary = new ConnectionDictionary();
		if ( serverConfig != null ) {
			final String urlSpec = serverConfig.getURLSpec();
			if ( urlSpec != null )  connectionDictionary.setURLSpec( urlSpec );
			final String adaptorClassSpec = serverConfig.getAdaptorClassSpec();
			if ( adaptorClassSpec != null )  connectionDictionary.setDatabaseAdaptorClass( adaptorClassSpec );
		}
		
		if ( account != null ) {
			final String user = account.getUserName();
			if ( user != null )  connectionDictionary.setUser( user );
			final String password = account.getPassword();
			if ( password != null )  connectionDictionary.setPassword( account.getPassword() );
		}
		
		return connectionDictionary;
	}
	
	
	/** generate a new connection dictionary for the specified account name and the default database server */
	public ConnectionDictionary newConnectionDictionary( final String accountName ) {
		return newConnectionDictionary( accountName, null );
	}
	
	
	/** 
	 * Get the available connection dictionary which is the most preferred
	 * @param useDefaultIfNeeded use the default account if none of the listed accounts is available
	 * @param accounts ordered (most preferred is first) accounts to search among
	 */
	public ConnectionDictionary availableConnectionDictionary( final boolean useDefaultIfNeeded, final String ... accounts ) {
		for ( final String account : accounts ) {
			if ( hasAccount( account ) ) return newConnectionDictionary( account );
		}
		return useDefaultIfNeeded ? defaultConnectionDictionary() : null;
	}
	
	
	/** 
	 * Get the available connection dictionary which is the most preferred and use the default one if there are no matches
	 * @param accounts ordered (most preferred is first) accounts to search among
	 */
	public ConnectionDictionary availableConnectionDictionary( final String ... accounts ) {
		return availableConnectionDictionary( true, accounts );
	}
	
	
	/** generate a new connection dictionary from the default database server configuration and default account */
	public ConnectionDictionary defaultConnectionDictionary() {
		return newConnectionDictionary( DEFAULT_ACCOUNT_NAME, DEFAULT_SERVER_NAME );
	}
	
	
	/** load the configuration from the default configuration URL */
	static public DBConfiguration getInstance() {
		try {
			return hasDefaultConfiguration() ? getInstance( getDefaultURL() ) : null;
		}
		catch( MalformedURLException exception ) {
			System.err.println( exception.getMessage() );
			throw new RuntimeException( "Malformed URL specification", exception );
		}
	}
	
	
	/** load a configuration from the specified URL */
	static public DBConfiguration getInstance( final URL configURL ) {
		final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( configURL, false );
		return getInstance( documentAdaptor );
	}
	
	
	/** load a configuration from the specified configuration document adaptor */
	static public DBConfiguration getInstance( final DataAdaptor documentAdaptor ) {
		final DataAdaptor configAdaptor = documentAdaptor.childAdaptor( "dbconfig" );
		
		final DataAdaptor dbAdaptorGroup = configAdaptor.childAdaptor( "adaptors" );
		final String defaultDBAdaptorName = dbAdaptorGroup.hasAttribute( "default" ) ? dbAdaptorGroup.stringValue( "default" ) : null;
		final List<DataAdaptor> dbAdaptors = dbAdaptorGroup.childAdaptors( "adaptor" );
		final Map<String,String> dbAdaptorTable = new HashMap<String,String>();
		for ( final DataAdaptor dbAdaptor : dbAdaptors ) {
			final String name = dbAdaptor.stringValue( "name" );
			final String className = dbAdaptor.stringValue( "class" );
			dbAdaptorTable.put( name, className );
		}
		
		final DataAdaptor serverGroup = configAdaptor.childAdaptor( "servers" );
		final String defaultServerName = serverGroup.hasAttribute( "default" ) ? serverGroup.stringValue( "default" ) : null;
		final List<DataAdaptor> serverAdaptors = serverGroup.childAdaptors( "server" );
		final Map<String,DBServerConfig> serverTable = new HashMap<String,DBServerConfig>();
		for ( final DataAdaptor serverAdaptor : serverAdaptors ) {
			final String name = serverAdaptor.stringValue( "name" );
			final String url = serverAdaptor.stringValue( "url" );
			final String dbAdaptorName = serverAdaptor.hasAttribute( "adaptor" ) ? serverAdaptor.stringValue( "adaptor" ) : defaultDBAdaptorName;
			final String dbAdaptorClassName = dbAdaptorTable.get( dbAdaptorName );
			final DBServerConfig serverConfig = new DBServerConfig( name, url, dbAdaptorClassName );
			serverTable.put( name, serverConfig );
		}
		
		final DataAdaptor accountGroup = configAdaptor.childAdaptor( "accounts" );
		final String defaultAccountName = accountGroup.hasAttribute( "default" ) ? accountGroup.stringValue( "default" ) : null;
		final List<DataAdaptor> accountAdaptors = accountGroup.childAdaptors( "account" );
		final Map<String,DBAccountConfig> accountTable = new HashMap<String,DBAccountConfig>();
		for ( final DataAdaptor accountAdaptor : accountAdaptors ) {
			final String name = accountAdaptor.stringValue( "name" );
			final String user = accountAdaptor.stringValue( "user" );
			final String password = accountAdaptor.hasAttribute( "password" ) ? accountAdaptor.stringValue( "password" ) : null;
			final String serverName = accountAdaptor.hasAttribute( "server" ) ? accountAdaptor.stringValue( "server" ) : null;
			accountTable.put( name, new DBAccountConfig( name, user, password, serverName ) );
		}
		
		return new DBConfiguration( defaultDBAdaptorName, dbAdaptorTable, defaultServerName, serverTable, defaultAccountName, accountTable );
	}
	
	
	/** determine whether a defualt configuraiton has been specified */
	static public boolean hasDefaultConfiguration() {
		try {
			final String urlSpec = getDefaultURLSpec();
			return urlSpec != null && !urlSpec.isEmpty() && new File( new URL( urlSpec ).toURI() ).exists();
		}
		catch ( Exception exception ) {
			return false;
		}
	}
	
	
	/**
	 * Get the user preferences for this class
	 * @return the user preferences for this class
	 */
	static protected Preferences getDefaults() {
		return Preferences.userNodeForPackage( DBConfiguration.class );
	}
	
	
	/**
	 * Get the URL Spec of the default connection dictionary's properties file
	 * @return the URL Spec of the configuration
	 */
	static public String getDefaultURLSpec() {
		return getDefaults().get( PREFERENCES_URL_KEY, "" );
	}
	
	
	/**
	 * Set the URL spec of the default configuration.
	 * @param urlSpec URL spec of the configuration
	 * @throws java.util.prefs.BackingStoreException if the url spec failed to be saved as a default
	 */
	static public void setDefaultURLSpec( final String urlSpec ) throws BackingStoreException {
		Preferences preferences = getDefaults();
		preferences.put( PREFERENCES_URL_KEY, urlSpec );
		preferences.flush();
	}
	
	
	/**
	 * Get the URL of the default configuration
	 * @return the URL of the default configuration
	 * @throws java.net.MalformedURLException if the default URL spec cannot form a valid URL
	 */
	static public URL getDefaultURL() throws MalformedURLException {
		if ( hasDefaultConfiguration() ) {
			return new URL( getDefaultURLSpec() );
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Set the URL of the default configuration.
	 * @param url URL of the configuration.
	 * @throws java.util.prefs.BackingStoreException if the url failed to be saved as a default
	 */
	static public void setDefaultURL( final URL url ) throws BackingStoreException {
		setDefaultURLSpec( url.toString() );
	}



	/**
	 * Get the default database adaptor
	 * @return the database adaptor
	 */
	public DatabaseAdaptor getDefaultDatabaseAdaptor() {
		if ( DATABASE_ADAPTOR_MAP == null )  return null;
		
		final String className = DATABASE_ADAPTOR_MAP.get( DEFAULT_DATABASE_ADAPTOR_NAME );
		if ( className == null || className.equals( "" ) )  return null;
		try {
			final Class<?> databaseAdaptorClass = Class.forName( className );
			return (DatabaseAdaptor)databaseAdaptorClass.newInstance();
		}
		catch(Exception exception) {
			final String message = "Failed to instantiate database adaptor for class:  " + className;
			throw new RuntimeException( message, exception );
		}
	}
}



/** holds a database server configuration */
class DBServerConfig {
	/** local name for the server (not an official name) */
	final private String NAME;
	
	/** URL specification */
	final private String URL_SPEC;
	
	/** string representation for the adaptor class */
	final private String ADAPTOR_CLASS_SPEC;
	
	/** Constructor */
	public DBServerConfig( final String name, final String url, final String adaptorClass ) {
		NAME = name;
		URL_SPEC = url;
		ADAPTOR_CLASS_SPEC = adaptorClass;
	}
	
	
	/** get the local server name */
	public String getName() {
		return NAME;
	}
	
	
	/** get the URL spec */
	public String getURLSpec() {
		return URL_SPEC;
	}
	
	
	/** get the string representation for the database adaptor class */
	public String getAdaptorClassSpec() {
		return ADAPTOR_CLASS_SPEC;
	}
}



/** holds a database account configuration */
class DBAccountConfig {
	/** local account name (not an official name) */
	final private String NAME;
	
	/** user name */
	final private String USER_NAME;
	
	/** user's password */
	final private String PASSWORD;

	/** name of the default server for the specified account if any */
	final private String DEFAULT_SERVER_NAME;


	/** Constructor */
	public DBAccountConfig( final String name, final String user, final String password, final String defaultServerName ) {
		NAME = name;
		USER_NAME = user;
		PASSWORD = password;
		DEFAULT_SERVER_NAME = defaultServerName;
	}
	
	
	/** get the account name */
	public String getName() {
		return NAME;
	}
	
	
	/** get the user name */
	public String getUserName() {
		return USER_NAME;
	}
	
	
	/** get the password */
	public String getPassword() {
		return PASSWORD;
	}


	/** get the account's default server name */
	public String getDefaultServerName() {
		return DEFAULT_SERVER_NAME;
	}
}
