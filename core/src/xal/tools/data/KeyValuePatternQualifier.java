//
//  KeyValuePatternQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 5/3/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.util.regex.*;


/** Qualifier to filter (based on the specified pattern) a record's value associated with a specified key. */
public class KeyValuePatternQualifier implements Qualifier {
	/** the key for which to fetch the record's value */
	private final String _key;
	
	/** the pattern with which to test for matches */
	private final Pattern _pattern;
	
	
	/**
	 * Primary constructor
	 * @param key the key identifying the value to test from the record
	 * @param regularExpression the regular expression used for matching
	 * @param flags pattern flags
	 */
	public KeyValuePatternQualifier( final String key, final String regularExpression, int flags ) {
		_key = key;
		_pattern = Pattern.compile( regularExpression, flags );
	}
	
	
	/**
	 * Constructor with the default pattern flags.
	 * @param key the key identifying the value to test from the record
	 * @param regularExpression the regular expression used for matching
	 */
	public KeyValuePatternQualifier( final String key, final String regularExpression ) {
		this( key, regularExpression, 0 );
	}
	
	
	/** 
	* Determine if the specified record's value associated with this qualifier's key is a match to this qualifier's pattern.
	* The value corresponding to this qualifier's key must be a <code>java.lang.CharSequence</code>.
	* @param object The object to test for matching.  The object must be a keyed record.
	* @return true if the object matches the criteria and false if not.
	*/
	public boolean matches( final Object object ) {
		final CharSequence value = (CharSequence)((KeyedRecord)object).valueForKey( _key );
		return _pattern.matcher( value ).matches();
	}	
	
	
	/**
	 * Get a string representation of this qualifier.
	 * @return a string representation of this qualifier.
	 */
	public String toString() {		
		return _key + " like \"" + _pattern.pattern() + "\"";
	}
}


