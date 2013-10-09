/*
 * RecentFileTracker.java
 *
 * Created on Thu May 20 10:01:29 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.apputils.files;

import xal.tools.StringJoiner;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.prefs.Preferences;
import java.net.*;
import java.io.File;
import javax.swing.JFileChooser;



/**
 * RecentFileTracker caches recently accessed files into the the user's preferences and has accessors
 * for getting the recent files and the most recent folder.
 *
 * @author  tap
 */
public class RecentFileTracker {
	/** default buffer size for a tracker */
	final static protected int DEFAULT_BUFFER_SIZE = 10;
	
	/** pattern for storing the URL spec in a string */
	final static private Pattern URL_SPEC_STORE_PATTERN;
	
	/** buffer size for this tracker */
	final protected int RECENT_URLS_BUFFER_SIZE;
	
	/** preferences storage */
	final protected Preferences PREFS;
	
	/** ID for the preferences */
	final protected String PREFERENCE_ID;
	
	
	// static initializer
	static {
		URL_SPEC_STORE_PATTERN = Pattern.compile( "\"[^\"]*\"" );	// specs are enclosed within quotes
	}
	
	
	/**
	 * Primary constructor
	 * @param bufferSize the buffer size of the recent URL specs to cache
	 * @param prefs the preferences used to save the cache of recent URL specs
	 * @param preferenceID the ID of the preference to save
	 */
	public RecentFileTracker(final int bufferSize, final Preferences prefs, final String preferenceID) {
		RECENT_URLS_BUFFER_SIZE = bufferSize;
		PREFERENCE_ID = preferenceID;
        PREFS = prefs;
	}
	
	
	/**
	 * Constructor which generates the preferences from the specified preference node
	 * @param bufferSize the buffer size of the recent URL specs to cache
	 * @param preferenceNode the node used for saving the preference
	 * @param preferenceID the ID of the preference to save
	 */
	public RecentFileTracker(final int bufferSize, final Class<?> preferenceNode, final String preferenceID) {
		this(bufferSize, Preferences.userNodeForPackage(preferenceNode), preferenceID);
	}
	
	
	/**
	 * Constructor with a default buffer size of 10
	 * @param preferenceNode the node used for saving the preference
	 * @param preferenceID the ID of the preference to save
	 */
	public RecentFileTracker(final Class<?> preferenceNode, final String preferenceID) {
		this(DEFAULT_BUFFER_SIZE, preferenceNode, preferenceID);
	}
	
	
	/**
	 * Clear the cache of the recent URL specs
	 */
	public void clearCache() {
        PREFS.put(PREFERENCE_ID, "");
	}
	
	
	/**
	 * Cache the URL of the specified file.
	 * @param file the file whose URL is to be cached.
	 */
	public void cacheURL(File file) {
		try {
			cacheURL( file.toURI().toURL() );
		}
		catch(MalformedURLException exception) {
			final String message = "Exception translating the file: " + file + " to a URL.";
			throw new RuntimeException(message, exception);
		}
	}
	
	
	/**
	 * Cache the URL.
	 * @param url the URL to cache.
	 */
	public void cacheURL(URL url) {
		cacheURL( url.toString() );
	}
	
	
	/**
	 * Cache the URL
	 * @param urlSpec the URL Spec to cache.
	 */
	public void cacheURL( final String urlSpec ) {
        final String[] recentURLSpecArray = getRecentURLSpecs();		// get the current list of specs
        final List<String> recentSpecs = new ArrayList<String>( RECENT_URLS_BUFFER_SIZE );		// hold the new list of specs
        recentSpecs.add( urlSpec );		// add the new spec as the first item
		
		// add the original specs expect for any spec matching the new one to avoid repetitions and don't exceed the buffer size
        for ( int index = 0 ; index < recentURLSpecArray.length && recentSpecs.size() < RECENT_URLS_BUFFER_SIZE ; index++ ) {
            final String recentURLSpec = recentURLSpecArray[index];
            if ( !recentSpecs.contains( recentURLSpec ) ) {			// make sure we don't repeat the new spec
                recentSpecs.add( recentURLSpec );		// add the spec
            }
        }
		
		// create a new array with the recent specs encoded
		final List<String> recentEncodedSpecs = new ArrayList<String>( recentSpecs.size() );
		for ( final String spec : recentSpecs ) {
			recentEncodedSpecs.add( encodeItem( spec ) );
		}
        
		// merge the specs into a single comma delimited string
        final StringJoiner joiner = new StringJoiner(",");
        joiner.append( recentEncodedSpecs.toArray() );
		
		// record the preference
        PREFS.put( PREFERENCE_ID, joiner.toString() );
	}
	
	
	/** encode the item for caching */
	static private String encodeItem( final String item ) {
		return "\"" + item + "\"";		// place quotes around the item
	}
	
	
	/** decode the encoded item */
	static private String decodeItem( final String encodedItem ) {
		if ( encodedItem == null || encodedItem.length() == 0 )  return null;
		final int encodedLength = encodedItem.length();
		if ( encodedLength > 2 && encodedItem.startsWith( "\"" ) && encodedItem.endsWith( "\"" ) ) {
			return encodedItem.substring( 1, encodedLength - 1 );	// strip the starting and ending quotes
		}
		else {
			return null;
		}
	}
    
    
    /**
     * Get the array of URLs corresponding to recently registered URLs. Fetch the recent items from the list saved in the user's preferences for the preference node.
     * @return The array of recent URLs.
     */
    public String[] getRecentURLSpecs() {
        final String pathsStr = PREFS.get( PREFERENCE_ID, "" );
		// check whether the paths are encoded using the new format ( quotes around each URL Spec )
		if ( pathsStr != null && pathsStr.length() > 2 && pathsStr.startsWith( "\"" ) && pathsStr.endsWith( "\"" ) ) {
			final Matcher matcher = URL_SPEC_STORE_PATTERN.matcher( pathsStr );
			final List<String> urlSpecs = new ArrayList<String>();
			while ( matcher.find() ) {
				final String encodedItem = matcher.group();
				urlSpecs.add( decodeItem( encodedItem ) );
			}
			return urlSpecs.toArray( new String[urlSpecs.size()] );
		}
		else {
			return getTokens( pathsStr, "," );		// old format uses comma delimited items
		}
    }
	
	
	/**
	 * Get the folder corresponding to the most recently cached URL.
	 * @return the most recent folder accessed
	 */
	public File getRecentFolder() {
		final File recentFile = getMostRecentFile();
		return recentFile != null ? recentFile.getParentFile() : null;
	}
	
	
	/**
	 * Get the folder path corresponding to the most recently cached URL.
	 * @return path to the most recent folder accessed or null if none has been accessed
	 */
	public String getRecentFolderPath() {
		final File recentFolder = getRecentFolder();
		return recentFolder != null ? recentFolder.getPath() : null;
	}
	
	
	/**
	 * Get the most recent file
	 * @return the most recently accessed file.
	 */
	public File getMostRecentFile() {
		final String[] recentURLSpecs = getRecentURLSpecs();
		final String recentSpec = recentURLSpecs.length > 0 ? recentURLSpecs[0] : null;
		try {
			if ( recentSpec != null ) {
				final URL recentURL = new URL( recentSpec );
				return new File( recentURL.toURI() );
			}
			else {
				return null;
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}
		
	
	/**
	 * Set the file chooser's current directory to the recent folder.
	 * @param fileChooser the file chooser for which to set the current directory
	 * @return the file chooser (same as the argument)
	 */
	public JFileChooser applyRecentFolder(final JFileChooser fileChooser) {
		final File recentFolder = getRecentFolder();
		if ( recentFolder != null ) {
			fileChooser.setCurrentDirectory(recentFolder);
		}
		
		return fileChooser;
	}
	
	
	/**
	 * Set the file chooser's selected file to the most recent file.
	 * @param fileChooser the file chooser for which to set the current directory
	 * @return the file chooser (same as the argument)
	 */
	public JFileChooser applyMostRecentFile( final JFileChooser fileChooser ) {
		final File recentFile = getMostRecentFile();
		if ( recentFile != null && recentFile.exists() ) {
			fileChooser.setSelectedFile( recentFile );
		}
		
		return fileChooser;
	}
    
    
    /**
     * Parse a string into tokens where whitespace is the delimiter.
     * @param string The string to parse.
     * @return The array of tokens.
     */
    static protected String[] getTokens( final String string ) {
        return getTokens( string, " \t" );
    }
    
    
    /**
     * Parse a string into tokens with the specified delimiter.
     * @param string The string to parse.
     * @param delim The delimiter
     * @return The array of tokens.
     */
    static protected String[] getTokens( final String string, final String delim ) {
        final StringTokenizer tokenizer = new StringTokenizer(string, delim);
        final int numTokens = tokenizer.countTokens();
        final String[] tokens = new String[ numTokens ];
		
        for ( int index = 0 ; index < numTokens ; index++ ) {
            tokens[index] = tokenizer.nextToken();
        }
        
        return tokens;
    }    
}

