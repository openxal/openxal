/*
 * Util.java
 *
 * Created on April 4, 2003, 11:04 AM
 */

package xal.extension.application;

import java.io.InputStream;
import java.net.URL;
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
     * Load the resource bundle specified by the URL.  When the resource bundle is loaded, the properties are stored in a Map for convenience data access.
     * @param resourceURL The URL to the properties file
     * @return The map equivalent of the resource bundle or null if the resource was not found
     */
    static public Map<String,String> loadResourceBundle( final URL resourceURL ) throws RuntimeException {
		final Map<String,String> infoMap = new HashMap<>();

		if ( resourceURL == null )  return infoMap;

		try {
			final InputStream inputStream = resourceURL.openStream();
			final Properties properties = new Properties();
			properties.load( inputStream );

			for ( final Map.Entry<Object,Object> entry : properties.entrySet() ) {
				final Object key = entry.getKey();
				final String keyString = key != null ? key.toString() : null;
				final Object value = entry.getValue();
				final String valueString = entry != null ? value.toString() : null;

				infoMap.put( keyString, valueString );
			}

			return infoMap;
		}
		catch( java.io.FileNotFoundException exception ) {
			// this may be fine as the resource may be optional
			return null;	// return null to indicate that the resource was missing
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Exception loading bundle from resource: " + resourceURL, exception );
		}
    }
	
	
	/**
	 * Merge the resource bundle from the specified source into the specified map. If the file does not exist at the source, then nothing is merged as this is intened for optional modifications.
	 * @param map the map into which the resources should be merged
	 * @param source URL to the resource to merge
	 */
    static public void mergeResourceBundle( final Map<String,String> map, final URL source ) {
		final Map<String,String> sourceBundle = loadResourceBundle( source );

		// if the bundle at the source was not found then we have nothing to merge so we are done
		if ( sourceBundle == null )  return;

		// merge the properties from the source onto the map (overriding it if conflicting)
		for ( final String key : sourceBundle.keySet() ) {
			final String assignment = sourceBundle.get( key );
			
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
	 * Convenience method for loading the resource bundle from a file whose path is specified relative to the application's resources folder.
	 * @param propertyFile The property file relative to the application's resources folder
	 * @return The map equivalent of the resource bundle
     * @throws java.util.MissingResourceException If the resource bundle cannot be found.
	 * @see #loadResourceBundle
	 */
	static public Map<String,String> getPropertiesForResource( final String propertyFile ) {
		return loadResourceBundle( Application.getAdaptor().getResourceURL( propertyFile ) );
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
