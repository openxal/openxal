/*
 * App.java
 *
 * Created on Fri Mar 05 14:20:42 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;

import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.messaging.MessageCenter;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;
import java.util.jar.*;


/**
 * App is a delegate for an XAL application to run.  The XAL application should be available as
 * an executable script.  The App instance includes the path to the executable and notes to display
 * describing the application.  The instance is capable of launching the executable.
 *
 * @author  tap
 */
public class App implements Comparable<App> {
	/** pattern for identifying script notes */
	static private final Pattern SCRIPT_NOTES_PATTERN;
	
	/** Path to the executable */
	private final File FILE;
	
	/** rule */
	private final Rule RULE;
	
	/** Text describing this application */
	private String _notes;
	
	/** label for this application */
	private String _label;

	/** time of last run */
	private Date _lastLaunchTime;

	
	static {
		SCRIPT_NOTES_PATTERN = Pattern.compile( "^#\\s+(\\w+\\s+[\\w\\s\\p{Punct}]+)$" );
	}
	
	
	/**
	 * Constructor
	 * @param file The file of the executable.
	 * @param rule the rule used to generate the app
	 */
	public App( final File file, final Rule rule ) {
		FILE = file;
		RULE = rule;

		_lastLaunchTime = null;
		_label = file.getName().split( "[.]" )[0];
		
		parseInfo( file );
	}
	
	
	/** get the rule */
	public Rule getRule() {
		return RULE;
	}
	
	
	/** get the command to run the application in a local environment */
	public List<String> getCommands() {
		return RULE.getCommands( this );
	}
	
	
	/** get the file to which the app refers */
	public File getFile() {
		return FILE;
	}
	
	
	/**
	 * Get the path to the application
	 * @return the path to the application
	 */
	public String getPath() {
		return FILE.getPath();
	}
	
	
	/**
	 * Get the label to display identifying this application
	 * @return the short name of the file path of the executable
	 */
	public String getLabel() {
		return _label;
	}


	/** get the time of the last run for this app */
	public Date getLastLaunchTime() {
		return _lastLaunchTime;
	}


	/** set the last run time */
	public void setLastLaunchTime( final Date runTime ) {
		_lastLaunchTime = runTime;
	}

	
	/**
	 * Test if the executable exists
	 * @return true if the executable exists and false if not
	 */
	public boolean exists() {
		return FILE.exists();
	}
	
	
	/**
	 * Get the notes associated with the application
	 * @return The notes associated with the application
	 */
	public String getNotes() {
		return _notes;
	}
	
	
	/**
	 * Get the string representation of this instance
	 * @return the label
	 * @see #getLabel
	 */
	public String toString() {
		return getLabel();
	}
	
	
	/** parse notes from the file */
	private void parseInfo( final File file ) {
		if ( file.canRead() ) {
			final String filename = file.getName();
			final int typeIndex = filename.lastIndexOf( "." );
			if ( typeIndex >= 0 && typeIndex < filename.length() ) {
				final String type = file.getName().substring( typeIndex + 1 );
				if ( type != null && type.length() > 0 ) {
					if ( type.equals( "rb" ) || type.equals( "py" ) ) {
						parseScriptHeader( file );
					}
					else if ( type.equals( "jar" ) ) {
						parseJarAppInfo( file );
					}
					else {
						return;
					}
				}
				else {
					return;
				}
			}
			else {
				return;
			}
		}
		else {
			return;
		}
	}
	
	
	/** parse a standard script header for notes */
	private void parseScriptHeader( final File file ) {
		try {
			final BufferedReader reader = new BufferedReader( new FileReader( file ) );
			try {
				int count = 0;
				while ( count++ < 10 ) {	// only attempt to read the first 10 lines
					final String line = reader.readLine();
					final Matcher matcher = SCRIPT_NOTES_PATTERN.matcher( line );
					if ( matcher.matches() && matcher.groupCount() > 0 ) {
						_notes = matcher.group(1);
						return;
					}
				}
				return;
			}
			finally {
				reader.close();
			}
		}
		catch ( Exception exception ) {
			return;
		}
	}
	
	
	/** open the application's jar file, find the about properties and return the description */
	private void parseJarAppInfo( final File file ) {
		try {
			final JarFile jarFile = new JarFile( file );
			try {
				final Enumeration<JarEntry> entryEnum = jarFile.entries();
				while ( entryEnum.hasMoreElements() ) {
					final JarEntry entry = entryEnum.nextElement();
					final String entryName = entry.getName();
					if ( entryName.endsWith( "About.properties" ) ) {
						final Properties properties = new Properties();
						final InputStream propertyStream = jarFile.getInputStream( entry );
						properties.load( propertyStream );
						try {
							_notes = properties.getProperty( "description" );
							_label = properties.getProperty( "name" );
						}
						finally {
							propertyStream.close();
						}
					}
				}
				return;
			}
			catch ( Exception exception ) {
				return;
			}
			finally {
				jarFile.close();
			}
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			return;
		}
	}
	
	
	/**
	 * Compare two instances.  The two instances are compared by label and the 
	 * comparison is based on alphebetical sorting.
	 * @param app the other App against which to compare
	 * @return a positive number if this comes after the argument, negative if this comes before and 0 if they are equal
	 * @see #getLabel
	 */
	public int compareTo( final App app ) {
		return getLabel().compareToIgnoreCase( app.getLabel() );
	}
}
