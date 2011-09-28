//
//  JSONDecoder.java
//  xal
//
//  Created by Tom Pelaia on 8/11/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;

import java.util.regex.*;
import java.util.*;


/** decode a JSON string representation of an object tree */
public class JSONDecoder {
	/** Constructor */
	private JSONDecoder() {}
	
	
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
	public static Object decode( final String archive ) {
		final AbstractDecoder decoder = AbstractDecoder.getInstance( archive );
		return decoder != null ? decoder.decode() : null;
	}
}



/** Base class of decoders */
abstract class AbstractDecoder<T> {
	/** archive to parse */
	final protected String ARCHIVE;
	
	/** unparsed remainder of the source string after parsing */
	protected String _remainder;
	
	
	/** Constructor */
	protected AbstractDecoder( final String archive ) {
		ARCHIVE = archive;
	}
	
	
	/** decode the source to extract the next object */	
	abstract protected T decode();
	
	
	/** get the unparsed remainder of the source string */
	protected String getRemainder() {
		return _remainder;
	}
	
	
	/** check for a match of the archive against the specified pattern, update the remainder and return the matching string */
	protected String processMatch( final Pattern pattern ) {
		final Matcher matcher = pattern.matcher( ARCHIVE );
		matcher.find();
		final int nextIndex = matcher.end();
		_remainder = ARCHIVE.length() > nextIndex ? ARCHIVE.substring( nextIndex ) : null;
		return matcher.group();
	}
	
	
	/** Get a decoder for the archive */
	protected static AbstractDecoder getInstance( final String archive ) {
		final String source = archive.trim();
		if ( source.length() > 0 ) {
			final char firstChar = source.charAt( 0 );
			switch ( firstChar ) {
				case '+': case '-': case '.':
				case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
					return new NumberDecoder( source );
				case 't': case 'f':
					return new BooleanDecoder( source );
				case 'n':
					return new NullDecoder( source );
				case '\"':
					return new StringDecoder( source );
				case '[':
					return new ArrayDecoder( source );
				case '{':
					return new DictionaryDecoder( source );
				default:
					return null;
			}
		}
		else {
			return null;
		}
	}	
}



/** decode a number from a source string */
class NumberDecoder extends AbstractDecoder<Number> {
	/** pattern for matching doubles */
	static final Pattern DOUBLE_PATTERN;
	
	
	// static initializer
	static {
		DOUBLE_PATTERN = Pattern.compile( "[+-]?((\\d+\\.?\\d*)|(\\.?\\d+))([eE][+-]?\\d+)?" );
	}
	
	
	/** Constructor */
	protected NumberDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Number decode() {
		final String match = processMatch( DOUBLE_PATTERN );
		return match != null ? Double.valueOf( match ) : null;
	}
}



/** decode a boolean from a source string */
class BooleanDecoder extends AbstractDecoder<Boolean> {
	/** pattern for matching booleans */
	static final Pattern BOOLEAN_PATTERN;
	
	
	// static initializer
	static {
		BOOLEAN_PATTERN = Pattern.compile( "(true)|(false)" );
	}
	
	
	/** Constructor */
	protected BooleanDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Boolean decode() {
		final String match = processMatch( BOOLEAN_PATTERN );
		return match != null ? Boolean.valueOf( match ) : null;
	}
}



/** decode a null identifier from a source string */
class NullDecoder extends AbstractDecoder<Object> {
	/** pattern for matching the null identifier */
	static final Pattern NULL_PATTERN;
	
	
	// static initializer
	static {
		NULL_PATTERN = Pattern.compile( "null" );
	}
	
	
	/** Constructor */
	protected NullDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Object decode() {
		final String match = processMatch( NULL_PATTERN );
		return null;
	}
}



/** decode a string from a source string */
class StringDecoder extends AbstractDecoder<String> {
	/** pattern for matching a string */
	static final Pattern STRING_PATTERN;
	
	
	// static initializer
	static {
		// a string begins and ends with a quotation mark and no unescaped quotation marks in between them
		STRING_PATTERN = Pattern.compile( "\\\"(((\\\\)+\")|[^\"])*\\\"" );
	}
	
	
	/** Constructor */
	protected StringDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected String decode() {
		final String match = processMatch( STRING_PATTERN );
		if ( match != null ) {
			final int length = match.length();
			return length > 0 ? unescape( match.substring( 1, length-1 ) ) : "";
		}
		else {
			return null;
		}
	}
	
	
	/** unescape (replace occurences of a backslash and a character by the character itself) the input and return the resulting string */
	static private String unescape( final String input ) {
		final StringBuffer buffer = new StringBuffer();
		unescapeToBuffer( buffer, input );
		return buffer.toString();
	}
	
	
	/** unescape the input and append the text to the buffer */
	static private void unescapeToBuffer( final StringBuffer buffer, final String input ) {
		if ( input != null && input.length() > 0 ) {
			final int location = input.indexOf( '\\' );
			if ( location < 0 ) {
				buffer.append( input );
			}
			else {
				buffer.append( input.substring( 0, location ) );
				final int inputLength = input.length();
				if ( inputLength > location ) {
					buffer.append( input.charAt( location + 1 ) );
					final String remainder = inputLength > location + 1 ? input.substring( location + 2 ) : null;
					unescapeToBuffer( buffer, remainder );
				}
			}
		}
	}
}



/** decode an array from a source string */
class ArrayDecoder extends AbstractDecoder<List> {
	/** Constructor */
	protected ArrayDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected List decode() {
		final String arrayString = ARCHIVE.substring( 1 ).trim();	// strip the leading bracket
		final List items = new ArrayList();
		appendItems( items, arrayString );
		return items;
	}
	
	
	/** append to the items the parsed items from the array string */
    @SuppressWarnings( "unchecked" )    // list of items can be any combination of types
	private void appendItems( final List items, final String arrayString ) {
		if ( arrayString != null && arrayString.length() > 0 ) {
			try {
				if ( arrayString.charAt( 0 ) == ']' ) {
					_remainder = arrayString.substring( 1 ).trim();
					return;
				}
				else {
					final AbstractDecoder itemDecoder = AbstractDecoder.getInstance( arrayString );
					items.add( itemDecoder.decode() );
					final String itemRemainder = itemDecoder.getRemainder().trim();
					final char closure = itemRemainder.charAt( 0 );
					final String archiveRemainder = itemRemainder.substring(1).trim();
					switch ( closure ) {
						case ',':
							appendItems( items, archiveRemainder );
							return;
						case ']':
							_remainder = archiveRemainder;
							return;
						default:
							throw new RuntimeException( "Invalid array closure mark: " + closure );
					}
				}
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
		else {
			_remainder = null;
		}
	}
}



/** decode a dictionary from a source string */
class DictionaryDecoder extends AbstractDecoder<Map> {
	/** Constructor */
	protected DictionaryDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Map decode() {
		final String dictionaryString = ARCHIVE.substring( 1 ).trim();	// strip the leading brace
		final Map dictionary = new HashMap();
		appendItems( dictionary, dictionaryString );
		return dictionary;
	}
	
	
	/** append to the items the parsed items from the array string */
    @SuppressWarnings( "unchecked" )    // dictionary can be any combination of types
	private void appendItems( final Map dictionary, final String dictionaryString ) {
		if ( dictionaryString != null && dictionaryString.length() > 0 ) {
			try {
				if ( dictionaryString.charAt( 0 ) == '}' ) {
					_remainder = dictionaryString.substring( 1 ).trim();
					return;
				}
				else {
					final StringDecoder keyDecoder = new StringDecoder( dictionaryString );
					final String key = keyDecoder.decode();
					final String keyRemainder = keyDecoder.getRemainder();
					final String valueBuffer = keyRemainder.trim().substring( 1 );	// trim spaces and strip the leading colon
					final AbstractDecoder valueDecoder = AbstractDecoder.getInstance( valueBuffer );
					final Object value = valueDecoder.decode();
					dictionary.put( key, value );
					final String itemRemainder = valueDecoder.getRemainder().trim();
					final char closure = itemRemainder.charAt( 0 );
					final String archiveRemainder = itemRemainder.substring(1).trim();
					switch ( closure ) {
						case ',':
							appendItems( dictionary, archiveRemainder );
							return;
						case '}':
							_remainder = archiveRemainder;
							return;
						default:
							throw new RuntimeException( "Invalid dictionary closure mark: " + closure );
					}
				}
			}
			catch ( Exception exception ) {
				exception.printStackTrace();
			}
		}
		else {
			_remainder = null;
		}
	}
}



