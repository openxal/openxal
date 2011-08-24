/*
 * Util.java
 *
 * Created on April 4, 2003, 11:04 AM
 */

package xal.application;

import java.util.*;

/**
 * Utility class providing convenience methods for use in the application framework.
 *
 * @author  tap
 */
public class Util {
    
    /** Creates a new instance of Util */
    protected Util() {
    }
    
    
    /**
     * Load the resource bundle specified by the path.  The path should be 
     * given in Java classpath notation and should exclude the "properties" 
     * suffix of the properties file.  When the resource bundle is loaded, 
     * the properties are stored in a Map for convenience data access.
     * @param path The path to the properties file
     * @return The map equivalent of the resource bundle
     * @throws java.util.MissingResourceException If the resource bundle cannot be found.
     */
    static public Map<String,String> loadResourceBundle( final String path ) throws MissingResourceException {
        final Map<String,String> infoMap = new HashMap<String,String>();
        final ResourceBundle bundle = ResourceBundle.getBundle( path );
        
        final Enumeration<String> keyEnum = bundle.getKeys();
        while( keyEnum.hasMoreElements() ) {
            final String key = keyEnum.nextElement();
            infoMap.put( key, bundle.getString( key ) );
        }
        
        return infoMap;
    }
	
	
	/**
	 * Merge the resource bundle at the path into the specified map.
	 * @param map the map into which the resources should be merged
	 * @param path the path to the resource bundle
	 */
    static public void mergeResourceBundle( final Map<String,String> map, final String path ) throws MissingResourceException {
        final ResourceBundle bundle = ResourceBundle.getBundle( path );
		
        final Enumeration<String> keyEnum = bundle.getKeys();
        while( keyEnum.hasMoreElements() ) {
            final String key = keyEnum.nextElement();
			final String assignment = bundle.getString( key );
			
			// if the key begins with "+" then prepend the new assignment onto the existing assignment
			if ( key.startsWith( "+" ) ) {
				final String baseKey = key.substring( 1 );
				final String initialAssignment = map.get( baseKey );
				if ( initialAssignment != null ) {
					map.put( baseKey, assignment + " - " + initialAssignment );
				}
				else {
					map.put( baseKey, assignment );
				}
			}
			// if the key ends with "+" then append the new assignment onto the existing assignment
			else if ( key.endsWith( "+" ) ) {
				final String baseKey = key.substring( 0, key.length() - 1 );
				final String initialAssignment = map.get( baseKey );
				if ( initialAssignment != null ) {
					map.put( baseKey, initialAssignment + " - " + assignment );
				}
				else {
					map.put( baseKey, assignment );
				}				
			}
			// if no modifier is found, then simply overwrite the assignment if existing or add a new one
			else {
				map.put( key, assignment );
			}
        }
    }
	
	
	/**
	 * Convenience method for loading the resource bundle from a file whose path is specified relative
	 * to the application's resources folder.
	 * @param propertyFile The property file path relative to the application's resources folder
	 * @return The map equivalent of the resource bundle
     * @throws java.util.MissingResourceException If the resource bundle cannot be found.
	 * @see #loadResourceBundle
	 */
	static public Map<String,String> getPropertiesFromResource(String propertyFile) throws MissingResourceException {
		return loadResourceBundle( Application.getAdaptor().getResourcesPath() + "." + propertyFile );
	}
    
    
    /**
     * Parse a string into tokens where whitespace is the delimiter.
     * @param string The string to parse.
     * @return The array of tokens.
     */
    static protected String[] getTokens(String string) {
        return getTokens(string, " \t");
    }
    
    
    /**
     * Parse a string into tokens with the specified delimiter.
     * @param string The string to parse.
     * @param delim The delimiter
     * @return The array of tokens.
     */
    static protected String[] getTokens(String string, String delim) {
        StringTokenizer tokenizer = new StringTokenizer(string, delim);
        int numTokens = tokenizer.countTokens();
        String[] tokens = new String[ numTokens ];
        
        for ( int index = 0 ; index < numTokens ; index++ ) {
            tokens[index] = tokenizer.nextToken();
        }
        
        return tokens;
    }    
 }
