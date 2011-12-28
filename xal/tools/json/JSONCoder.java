//
//  JSONCoder.java
//  xal
//
//  Created by Tom Pelaia on 6/16/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;

import java.util.*;
import java.util.regex.*;


/** encode and decode objects with JSON */
public class JSONCoder {
    /** custom key identifying a custom type translated in terms of JSON representations */
    static final String EXTENDED_TYPE_KEY = "__XALTYPE__";
    
    /** custom key identifying a custom value to translate in terms of JSON representations */
    static final String EXTENDED_VALUE_KEY = "value";
    
    /** default coder */
    static JSONCoder DEFAULT_CODER;
    
    /** adaptors between all custom types and representation JSON types */
    final Map<String,JSONAdaptor> TYPE_EXTENSION_ADAPTORS;
    
    
    // static initializer
    static {
        DEFAULT_CODER = new JSONCoder( true );
    }
    
    
    /** get a new JSON Coder only if you need to customize it, otherwise use the static methods to encode/decode */
    static public JSONCoder getInstance() {
        return new JSONCoder( false );
    }
    
    
    /** Constructor to be called for the default coder */
    private JSONCoder( final boolean isDefault ) {
        TYPE_EXTENSION_ADAPTORS = new HashMap<String,JSONAdaptor>();
        
        if ( isDefault ) {
            registerStandardExtensions();
        }
        else {
            TYPE_EXTENSION_ADAPTORS.putAll( DEFAULT_CODER.TYPE_EXTENSION_ADAPTORS );
        }
    }
    
    
    /** Get a list of types (including JSON standard types plus standard extensions) which are supported for coding and decoding */
    static public List<String> getStandardTypes() {
        return DEFAULT_CODER.getSupportedTypes();
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        final List<String> types = new ArrayList<String>();
        
        types.add( Double.class.toString() );
        types.add( Boolean.class.toString() );
        types.add( String.class.toString() );
        types.add( Map.class.toString() );
        types.add( Object[].class.toString() );
        
        types.addAll( getExtendedTypes() );
        
        Collections.sort( types );
        
        return types;
    }
    
    
    /** Get a list of types which extend beyond the JSON standard types */
    public List<String> getExtendedTypes() {
        final List<String> types = new ArrayList<String>();
        
        for ( final String type : TYPE_EXTENSION_ADAPTORS.keySet() ) {
            types.add( type );
        }
        
        Collections.sort( types );
        
        return types;
    }
    
    
    /** register the standard type extensions (only needs to be done for the default coder) */
    private void registerStandardExtensions() {
        registerType( Short.class, new JSONAdaptor<Short,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Short custom ) {
                return custom.doubleValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Short toCustom( final Double representation ) {
                return representation.shortValue();
            }
        });
        
        registerType( Integer.class, new JSONAdaptor<Integer,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Integer custom ) {
                return custom.doubleValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Integer toCustom( final Double representation ) {
                return representation.intValue();
            }
        });
        
        registerType( Long.class, new JSONAdaptor<Long,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Long custom ) {
                return custom.doubleValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Long toCustom( final Double representation ) {
                return representation.longValue();
            }
        });
        
        registerType( Float.class, new JSONAdaptor<Float,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Float custom ) {
                return custom.doubleValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Float toCustom( final Double representation ) {
                return representation.floatValue();
            }
        });
        
        registerType( Date.class, new JSONAdaptor<Date,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Date timestamp ) {
                return (double)timestamp.getTime();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Date toCustom( final Double msecFromEpoch ) {
                return new Date( msecFromEpoch.longValue() );
            }
        });
        
        registerType( ArrayList.class, new JSONAdaptor<ArrayList,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final ArrayList list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public ArrayList toCustom( final Object[] array ) {
                final ArrayList list = new ArrayList( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        registerType( Vector.class, new JSONAdaptor<Vector,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final Vector list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Vector toCustom( final Object[] array ) {
                final Vector list = new Vector( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        registerType( Hashtable.class, new JSONAdaptor<Hashtable,Map>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )    // map and table don't have compile time types
            public Map toRepresentation( final Hashtable table ) {
                return new HashMap( table );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Hashtable toCustom( final Map map ) {
                return new Hashtable( map );
            }
        });
    }
    
    
    /** 
     * Register the custom type and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final JSONAdaptor<CustomType,RepresentationType> adaptor ) {
        TYPE_EXTENSION_ADAPTORS.put( type.toString(), adaptor );
    }
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
	public static Object decode( final String archive ) {
        return DEFAULT_CODER.unarchive( archive );
	}
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
    public Object unarchive( final String archive ) {
        final AbstractDecoder decoder = AbstractDecoder.getInstance( archive, Collections.unmodifiableMap( TYPE_EXTENSION_ADAPTORS ) );
		return decoder != null ? decoder.decode() : null;
    }
	
	
	/** encode an object */
    static public String encode( final Object value ) {
        return DEFAULT_CODER.archive( value );
	}
    
    
	/** encode a string */
    private String archiveString( final String value ) {
		return value != null ? "\"" + value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + "\"" : "null";
	}
	
	
	/** encode a boolean */
    private String archiveBoolean( final boolean value ) {
		return value ? "true" : "false";
	}
	
	
	/** encode a number */
    private String archiveDouble( final Double value ) {
		return value != null ? value.toString() : "null";
	}
	
	
	/** encode an array */
    private String archiveArray( final Object[] values ) {
		if ( values != null ) {
			final int count = values.length;
			final StringBuffer buffer = new StringBuffer();
			buffer.append( "[" );
			for ( int index = 0 ; index < count ; index++ ) {
				switch ( index ) {
					case 0:
						break;
					default:
						buffer.append( ", " );
						break;
				}
				final Object value = values[index];
				buffer.append( encode( value ) );
			}
			buffer.append( "]" );
			return buffer.toString();
		}
		else {
			return "null";
		}
	}
	
	
	/** encode a hash table */
    private <ValueType> String archiveMap( final HashMap<String,ValueType> map ) {
		if ( map != null ) {
			final StringBuffer buffer = new StringBuffer();
			buffer.append( "{" );
			final Set<Map.Entry<String,ValueType>> entries = map.entrySet();
			int index = 0;
			for ( final Map.Entry<String,ValueType> entry : entries ) {
				switch ( index ) {
					case 0:
						break;
					default:
						buffer.append( ", " );
						break;
				}
				final String key = entry.getKey();
				final Object value = entry.getValue();
				buffer.append( encode( key ) );
				buffer.append( ": " );
				buffer.append( encode( value ) );
				++index;
			}
			buffer.append( "}" );
			return buffer.toString();
		}
		else {
			return "null";
		}
	}
	
	
	/** encode an object */
    @SuppressWarnings( "unchecked" )    // no way to guarantee at compile time that maps are keyed by string
    public String archive( final Object value ) {
		if ( value == null ) {
			return "null";
		}
		else if ( value.getClass().equals( String.class ) ) {
			return archiveString( (String)value );
		}
		else if ( value.getClass().equals( Boolean.class ) ) {
			return archiveBoolean( (Boolean)value );
		}
		else if ( value.getClass().equals( Double.class ) ) {
			return archiveDouble( (Double)value );
		}
		else if ( value.getClass().equals( HashMap.class ) ) {  // no way to check at compile time that the key type is string
			return archiveMap( (HashMap)value );
		}
		else if ( value.getClass().isArray() ) {
			return archiveArray( (Object[])value );
		}
		else {
            final String valueType = value.getClass().toString();
            final JSONAdaptor adaptor = TYPE_EXTENSION_ADAPTORS.get( valueType );
            if ( adaptor != null ) {
                final HashMap<String,Object> valueRep = new HashMap<String,Object>();
                final Object representationValue = adaptor.toRepresentation( value );
                valueRep.put( EXTENDED_TYPE_KEY, valueType );
                valueRep.put( EXTENDED_VALUE_KEY, representationValue );
                return archiveMap( valueRep );
            }
            else {
                throw new RuntimeException( "No coder for encoding objects of type: " + valueType );
            }
		}
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
	protected static AbstractDecoder getInstance( final String archive, final Map<String,JSONAdaptor> typeExtensionAdaptors ) {
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
					return new ArrayDecoder( source, typeExtensionAdaptors );
				case '{':
					return new DictionaryDecoder( source, typeExtensionAdaptors );
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
class ArrayDecoder extends AbstractDecoder<Object[]> {
    /** custom type adaptors */
    final private Map<String,JSONAdaptor> TYPE_EXTENSION_ADAPTORS;
    
    
	/** Constructor */
	protected ArrayDecoder( final String archive, final Map<String,JSONAdaptor> typeExtensionAdaptors ) {
		super( archive );
        TYPE_EXTENSION_ADAPTORS = typeExtensionAdaptors;
	}
	
	
	/** decode the source to extract the next object */	
	protected Object[] decode() {
		final String arrayString = ARCHIVE.substring( 1 ).trim();	// strip the leading bracket
		final List<Object> items = new ArrayList<Object>();
		appendItems( items, arrayString );
		return items.toArray();
	}
	
	
	/** append to the items the parsed items from the array string */
	private void appendItems( final List<Object> items, final String arrayString ) {
		if ( arrayString != null && arrayString.length() > 0 ) {
			try {
				if ( arrayString.charAt( 0 ) == ']' ) {
					_remainder = arrayString.substring( 1 ).trim();
					return;
				}
				else {
					final AbstractDecoder itemDecoder = AbstractDecoder.getInstance( arrayString, TYPE_EXTENSION_ADAPTORS );
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
class DictionaryDecoder extends AbstractDecoder<Object> {
    /** custom type adaptors */
    final private Map<String,JSONAdaptor> TYPE_EXTENSION_ADAPTORS;
    
    
	/** Constructor */
	protected DictionaryDecoder( final String archive, final Map<String,JSONAdaptor> typeExtensionAdaptors ) {
		super( archive );
        TYPE_EXTENSION_ADAPTORS = typeExtensionAdaptors;
	}
	
	
	/** decode the source to extract the next object */	
    @SuppressWarnings( "unchecked" )    // no way to validate representation value and type at compile time
	protected Object decode() {
		final String dictionaryString = ARCHIVE.substring( 1 ).trim();	// strip the leading brace
		final Map<String,Object> dictionary = new HashMap<String,Object>();
		appendItems( dictionary, dictionaryString );
        
        if ( dictionary.containsKey( JSONCoder.EXTENDED_TYPE_KEY ) && dictionary.containsKey( JSONCoder.EXTENDED_VALUE_KEY ) ) {
            final String extendedType = (String)dictionary.get( JSONCoder.EXTENDED_TYPE_KEY );
            final Object representationValue = dictionary.get( JSONCoder.EXTENDED_VALUE_KEY );
            final JSONAdaptor adaptor = TYPE_EXTENSION_ADAPTORS.get( extendedType );
            if ( adaptor == null )  throw new RuntimeException( "Missing JSON adaptor for type: " + extendedType );
            return adaptor.toCustom( representationValue );
        }
        else {
            return dictionary;
        }
	}
	
	
	/** append to the items the parsed items from the array string */
	private void appendItems( final Map<String,Object> dictionary, final String dictionaryString ) {
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
					final AbstractDecoder valueDecoder = AbstractDecoder.getInstance( valueBuffer, TYPE_EXTENSION_ADAPTORS );
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



