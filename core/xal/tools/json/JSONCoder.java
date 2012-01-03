//
//  JSONCoder.java
//  xal
//
//  Created by Tom Pelaia on 6/16/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;

import xal.tools.ConversionAdaptor;

import java.util.*;
import java.util.regex.*;


/** encode and decode objects with JSON */
public class JSONCoder {
    /** default coder */
    static JSONCoder DEFAULT_CODER;
    
    /** adaptors between all custom types and representation JSON types */
    final MutableConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    
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
        CONVERSION_ADAPTOR_STORE = isDefault ? new MutableConversionAdaptorStore( true ) : new MutableConversionAdaptorStore( DEFAULT_CODER.CONVERSION_ADAPTOR_STORE );
    }
    
    
    /** Get a list of types (including JSON standard types plus default extensions) which are supported for coding and decoding */
    static public List<String> getDefaultTypes() {
        return DEFAULT_CODER.getSupportedTypes();
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        return CONVERSION_ADAPTOR_STORE.getSupportedTypes();
    }
    
    
    /** Get a list of types which extend beyond the JSON standard types */
    public List<String> getExtendedTypes() {
        return CONVERSION_ADAPTOR_STORE.getExtendedTypes();
    }
    
    
    /** 
     * Register the custom type and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        CONVERSION_ADAPTOR_STORE.registerType( type, adaptor );
    }
    
    
    /** Get the conversion adaptor for the given value */
    protected ConversionAdaptor getConversionAdaptor( final String valueType ) {
        return CONVERSION_ADAPTOR_STORE.getConversionAdaptor( valueType );
    }
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
	static public Object decode( final String archive ) {
        return DEFAULT_CODER.unarchive( archive );
	}
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
    public Object unarchive( final String archive ) {
        final AbstractDecoder decoder = AbstractDecoder.getInstance( archive, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
		return decoder != null ? decoder.decode() : null;
    }
	
	
	/** encode an object */
    static public String encode( final Object value ) {
        return DEFAULT_CODER.archive( value );
	}
	
	
	/** encode an object */
    public String archive( final Object value ) {
        return AbstractEncoder.encode( value, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
	}
}



/** Base class of encoders */
abstract class AbstractEncoder {
    /** encode the value using the coder */
    static public String encode( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        return record( value, conversionAdaptorStore ).encode();
    }
    
    /** encode the archived value to JSON */
    abstract public String encode();
    
    
    /** record the value for encoding later */
    static public AbstractEncoder record( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        return record( value, conversionAdaptorStore, new ReferenceStore() );
    }
    
    
    /** record a value for encoding later and store references in the supplied store */
    @SuppressWarnings( "unchecked" )    // no way to guarantee at compile time conversion types
    static protected AbstractEncoder record( final Object value, final ConversionAdaptorStore conversionAdaptorStore, final ReferenceStore referenceStore ) {
		if ( value == null ) {
			return NullEncoder.getInstance();
		}
        else if ( value.getClass().equals( Boolean.class ) ) {
            return new BooleanEncoder( (Boolean)value );
        }
        else if ( value.getClass().equals( Double.class ) ) {
            return new DoubleEncoder( (Double)value );
        }
        else if ( value.getClass().equals( Long.class ) ) {
            return new LongEncoder( (Long)value );
        }
        else if ( value.getClass().equals( String.class ) ) {
            final String stringValue = (String)value;
            final IdentityReference reference = StringEncoder.allowsReference( stringValue ) ? referenceStore.store( value ) : null;
            return reference != null && reference.hasMultiple() ? new ReferenceEncoder( reference.getID() ) : new StringEncoder( stringValue, reference );
        }
        else {      // these are the ones that support references
            final IdentityReference reference = referenceStore.store( value );
            if ( reference.hasMultiple() ) {
                return new ReferenceEncoder( reference.getID() );
            }
            else if ( value.getClass().equals( HashMap.class ) ) {  // no way to check at compile time that the key type is string
                return new DictionaryEncoder( (HashMap<String,Object>)value, conversionAdaptorStore, reference, referenceStore );
            }
            else if ( value.getClass().isArray() ) {
                return new ArrayEncoder( (Object[])value, conversionAdaptorStore, reference, referenceStore );
            }
            else {  // if the type is not among the standard ones then look to extensions
                return new ExtensionEncoder( value, conversionAdaptorStore, reference, referenceStore );
            }
        }
    }
}



/** Base class of encoders for hard objects (not references) */
abstract class HardEncoder<DataType> extends AbstractEncoder {
    /** value to encode */
    final protected DataType VALUE;
    
    
    /** Constructor */
    public HardEncoder( final DataType value ) {
        VALUE = value;
    }
}



/** Base class of encoders for objects that support references */
abstract class SoftValueEncoder extends AbstractEncoder {
    /** key identifies an object that is referenced */
    static final public String OBJECT_ID_KEY = "__XALID";
    
    /** key for the referenced value */
    static final public String VALUE_KEY = "value";

    /** reference to this value */
    final private IdentityReference REFERENCE;
    
    
    /** Constructor */
    public SoftValueEncoder( final IdentityReference reference ) {
        REFERENCE = reference;
    }
    
    
    /** encode an object ID and value if there are multiple references to the value, otherwise just encode the value */
    public String encode() {
        final String valueEncoding = encodeValue();
        if ( REFERENCE.hasMultiple() ) {
            return DictionaryEncoder.encodeKeyValueStringPairs( new KeyValueStringPair( OBJECT_ID_KEY, LongEncoder.encode( REFERENCE.getID() ) ), new KeyValueStringPair( VALUE_KEY, valueEncoding ) );
        }
        else {
            return valueEncoding;
        }
    }
    
    
    /** encode just the value */
    abstract String encodeValue();
}


/** encoder for references */
class ReferenceEncoder extends AbstractEncoder {
    /** key to indicate a reference */
    static final public String REFERENCE_KEY = "__XALREF";
    
    /** ID of referenced object */
    final private long REFERENCE_ID;
    
    
    /** Constructor */
    public ReferenceEncoder( final long referenceID ) {
        REFERENCE_ID = referenceID;
    }
    
    
    /** encode the reference to JSON */
    public String encode() {
        return DictionaryEncoder.encodeKeyValueStringPairs( new KeyValueStringPair( REFERENCE_KEY, LongEncoder.encode( REFERENCE_ID ) ) );
    }
}



/** encode a null to JSON */
class NullEncoder extends AbstractEncoder {
    /** encoder singleton */
    static final private NullEncoder SHARED_ENCODER;
    
    
    // static initializer
    static {
        SHARED_ENCODER = new NullEncoder();
    }
    
    
    /** get the shared instance */
    static public NullEncoder getInstance() {
        return SHARED_ENCODER;
    }
    
    
    /** Constructor */
    private NullEncoder() {}
    
    
    /** encode the archived value to JSON */
    public String encode() {
        return "null";
    }
}



/** encode a string to JSON */
class StringEncoder extends SoftValueEncoder {
    /** value to encode */
    final private String VALUE;
    
    
    /** Constructor */
    public StringEncoder( final String value, final IdentityReference reference ) {
        super( reference );
        VALUE = value;
    }
    
    
    /** determine whether the string allows referencing */
    static public boolean allowsReference( final String value ) {
        return value.length() > 20;     // don't bother using references unless the string is long enough to warrant the overhead
    }
    
    
    /** encode an object ID and value if there are multiple references to the value, otherwise just encode the value */
    public String encode() {
        return allowsReference( VALUE ) ? super.encode() : encodeValue();
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        return encode( VALUE );
    }
    
    
    /** encode a string */
    static public String encode( final String value ) {
        return "\"" + value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + "\"";
    }
}



/** encode a boolean to JSON */
class BooleanEncoder extends HardEncoder<Boolean> {
    /** Constructor */
    public BooleanEncoder( final Boolean value ) {
        super( value );
    }
    
    
    /** encode the archived value to JSON */
    public String encode() {
		return VALUE.booleanValue() ? "true" : "false";
    }
}



/** encode a double to JSON */
class DoubleEncoder extends HardEncoder<Double> {
    /** Constructor */
    public DoubleEncoder( final Double value ) {
        super( value );
    }
    
    
    /** encode the archived value to JSON */
    public String encode() {
		return encode( VALUE );
    }
    
    
    /** encode a double value */
    static public String encode( final Double value ) {
        return value.toString();
    }
}



/** encode a long integer to JSON */
class LongEncoder extends HardEncoder<Long> {
    /** Constructor */
    public LongEncoder( final Long value ) {
        super( value );
    }
    
    
    /** encode the archived value to JSON */
    public String encode() {
		return encode( VALUE );
    }
    
    
    /** encode a long value */
    static public String encode( final Long value ) {
        return value.toString();
    }
}



/** encode a hash map to JSON */
class DictionaryEncoder extends SoftValueEncoder {
    /** map of item encoders keyed by the corresponding orignal map keys */
    final private Map<String,AbstractEncoder> ENCODER_MAP;
    
    
    /** Constructor */
    public DictionaryEncoder( final HashMap<String,Object> map, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference reference, final ReferenceStore referenceStore ) {
        super( reference );
        
        ENCODER_MAP = new HashMap<String,AbstractEncoder>( map.size() );
        final Set<Map.Entry<String,Object>> entries = map.entrySet();
        for ( final Map.Entry<String,Object> entry : entries ) {
            final AbstractEncoder itemEncoder = AbstractEncoder.record( entry.getValue(), conversionAdaptorStore, referenceStore );
            ENCODER_MAP.put( entry.getKey(), itemEncoder );
        }
    }

    
    /** encode key value string pairs where the value is already encoded */
    static public String encodeKeyValueStringPairs( final KeyValueStringPair ... keyValuePairs ) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append( "{" );
        
        int index = 0;
        for ( final KeyValueStringPair keyValuePair : keyValuePairs ) {
            switch ( index ) {
                case 0:
                    break;
                default:
                    buffer.append( ", " );
                    break;
            }
            buffer.append( StringEncoder.encode( keyValuePair.KEY ) );
            buffer.append( ": " );
            buffer.append( keyValuePair.VALUE );
            ++index;
        }
        
        buffer.append( "}" );
        return buffer.toString();
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        final Set<Map.Entry<String,AbstractEncoder>> entries = ENCODER_MAP.entrySet();
        final KeyValueStringPair[] keyValuePairs = new KeyValueStringPair[ entries.size() ];
        int index = 0;
        for ( final Map.Entry<String,AbstractEncoder> entry : entries ) {
            final String key = entry.getKey();
            final AbstractEncoder itemEncoder = entry.getValue();
            final String value = itemEncoder.encode();
            keyValuePairs[index] = new KeyValueStringPair( key, value );
            ++index;
        }
        return encodeKeyValueStringPairs( keyValuePairs );
    }
}


/** encoder for extensions which piggybacks on the dictionary encoder */
class ExtensionEncoder extends DictionaryEncoder {
    /** Constructor */
    public ExtensionEncoder( final Object value, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference reference, final ReferenceStore referenceStore ) {
        super( getValueRep( value, conversionAdaptorStore ), conversionAdaptorStore, reference, referenceStore );
    }
    
    
    /** get the value representation as a dictionary keyed for the extended type and value */
    @SuppressWarnings( "unchecked" )
    static private HashMap<String,Object> getValueRep( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
        final String valueType = value.getClass().getName();
        final ConversionAdaptor adaptor = conversionAdaptorStore.getConversionAdaptor( valueType );
        if ( adaptor != null ) {
            final HashMap<String,Object> valueRep = new HashMap<String,Object>();
            final Object representationValue = adaptor.toRepresentation( value );
            valueRep.put( ConversionAdaptorStore.EXTENDED_TYPE_KEY, valueType );
            valueRep.put( ConversionAdaptorStore.EXTENDED_VALUE_KEY, representationValue );
            return valueRep;
        }
        else {
            throw new RuntimeException( "No coder for encoding objects of type: " + valueType );
        }
    }
}



/** encode an array to JSON */
class ArrayEncoder extends SoftValueEncoder {
    /** array of encoders each of which corresponds to an item in the original array */
    final private AbstractEncoder[] ITEM_ENCODERS;
    
    
    /** Constructor */
    public ArrayEncoder( final Object[] array, final ConversionAdaptorStore conversionAdaptorStore, final IdentityReference reference, final ReferenceStore referenceStore ) {
        super( reference );
        
        ITEM_ENCODERS = new AbstractEncoder[ array.length ];
        for ( int index = 0 ; index < array.length ; index++ ) {
            ITEM_ENCODERS[index] = AbstractEncoder.record( array[index], conversionAdaptorStore, referenceStore );
        }
    }
    
    
    /** encode the archived value to JSON */
    public String encodeValue() {
        final int count = ITEM_ENCODERS.length;
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
            final AbstractEncoder itemEncoder = ITEM_ENCODERS[index];
            buffer.append( itemEncoder.encode() );
        }
        buffer.append( "]" );
        return buffer.toString();
    }
}



/** Base class of decoders */
abstract class AbstractDecoder<DataType> {
	/** archive to parse */
	final protected String ARCHIVE;
	
	/** unparsed remainder of the source string after parsing */
	protected String _remainder;
	
	
	/** Constructor */
	protected AbstractDecoder( final String archive ) {
		ARCHIVE = archive;
	}
	
	
	/** decode the source to extract the next object */	
	abstract protected DataType decode();
	
	
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
	public static AbstractDecoder getInstance( final String archive, final ConversionAdaptorStore conversionAdaptorStore ) {
        return AbstractDecoder.getInstance( archive, conversionAdaptorStore, new KeyedReferenceStore() );
	}	
	
	
	/** Get a decoder for the archive */
	protected static AbstractDecoder getInstance( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
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
					return new ArrayDecoder( source, conversionAdaptorStore, referenceStore );
				case '{':
					return new DictionaryDecoder( source, conversionAdaptorStore, referenceStore );
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
	/** pattern for matching a number */
	static final Pattern NUMBER_PATTERN;
	
	
	// static initializer
	static {
		NUMBER_PATTERN = Pattern.compile( "[+-]?((\\d+\\.?\\d*)|(\\.?\\d+))([eE][+-]?\\d+)?" );
	}
	
	
	/** Constructor */
	protected NumberDecoder( final String archive ) {
		super( archive );
	}
	
	
	/** decode the source to extract the next object */	
	protected Number decode() {
		final String match = processMatch( NUMBER_PATTERN );
        // doubles always have a decimal point even if the fraction is zero, so the absence of a period indicates a long integer
        if ( match != null ) {
            if ( match.contains( "." ) ) {
                return Double.valueOf( match );
            }
            else {
                return Long.valueOf( match );
            }
        }
        else {
            return null;
        }
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
    final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    /** reference store */
    final private KeyedReferenceStore REFERENCE_STORE;
    
    
	/** Constructor */
	protected ArrayDecoder( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
		super( archive );
        CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;
        REFERENCE_STORE = referenceStore;
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
					final AbstractDecoder itemDecoder = AbstractDecoder.getInstance( arrayString, CONVERSION_ADAPTOR_STORE, REFERENCE_STORE );
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
    final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;
    
    /** reference store */
    final private KeyedReferenceStore REFERENCE_STORE;

    
	/** Constructor */
	protected DictionaryDecoder( final String archive, final ConversionAdaptorStore conversionAdaptorStore, final KeyedReferenceStore referenceStore ) {
		super( archive );
        CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;
        REFERENCE_STORE = referenceStore;
	}
	
	
	/** decode the source to extract the next object */	
    @SuppressWarnings( "unchecked" )    // no way to validate representation value and type at compile time
	protected Object decode() {
		final String dictionaryString = ARCHIVE.substring( 1 ).trim();	// strip the leading brace
		final Map<String,Object> dictionary = new HashMap<String,Object>();
		appendItems( dictionary, dictionaryString );
        
        if ( dictionary.containsKey( ConversionAdaptorStore.EXTENDED_TYPE_KEY ) && dictionary.containsKey( ConversionAdaptorStore.EXTENDED_VALUE_KEY ) ) {
            // decode object of extended type
            final String extendedType = (String)dictionary.get( ConversionAdaptorStore.EXTENDED_TYPE_KEY );
            final Object representationValue = dictionary.get( ConversionAdaptorStore.EXTENDED_VALUE_KEY );
            final ConversionAdaptor adaptor = CONVERSION_ADAPTOR_STORE.getConversionAdaptor( extendedType );
            if ( adaptor == null )  throw new RuntimeException( "Missing JSON adaptor for type: " + extendedType );
            return adaptor.toNative( representationValue );
        }
        else if ( dictionary.containsKey( SoftValueEncoder.OBJECT_ID_KEY ) && dictionary.containsKey( SoftValueEncoder.VALUE_KEY ) ) {
            // decode a referenced object definition and store it
            final Long itemID = (Long)dictionary.get( SoftValueEncoder.OBJECT_ID_KEY );
            final Object item = dictionary.get( SoftValueEncoder.VALUE_KEY );
            REFERENCE_STORE.store( itemID, item );
            return item;
        }
        else if ( dictionary.containsKey( ReferenceEncoder.REFERENCE_KEY ) ) {
            // decode a reference to an object in the store
            final Long itemID = (Long)dictionary.get( ReferenceEncoder.REFERENCE_KEY );
            return REFERENCE_STORE.get( itemID );
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
					final AbstractDecoder valueDecoder = AbstractDecoder.getInstance( valueBuffer, CONVERSION_ADAPTOR_STORE, REFERENCE_STORE );
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



/** Stores referenced items keyed by ID */
class KeyedReferenceStore {
    /** references keyed by ID */
    final private Map<Double,Object> REFERENCES;
    
    /** Constructor */
    public KeyedReferenceStore() {
        REFERENCES = new HashMap<Double,Object>();
    }
    
    
    /** store the value associated with the key */
    public void store( final double key, final Object value ) {
        REFERENCES.put( key, value );
    }
    
    
    /** get the item associated with the key */
    public Object get( final double key ) {
        return REFERENCES.get( key );
    }
}



/** pair of strings representing the key and value */
class KeyValueStringPair {
    /** key */
    final public String KEY;
    
    /** value */
    final public String VALUE;
    
    
    /** Constructor */
    public KeyValueStringPair( final String key, final String value ) {
        KEY = key;
        VALUE = value;
    }
}



/** storage of possible references */
class ReferenceStore {
    /** set of objects with a common equality */
    final private Map<Object,EqualityReference<Object>> EQUALITY_REFERENCES;
    
    /** counter of unique objects */
    private long _objectCounter;
    
    
    /** Constructor */
    public ReferenceStore() {
        EQUALITY_REFERENCES = new HashMap<Object,EqualityReference<Object>>();
        _objectCounter = 0;
    }
    
    
    /** store the item */
    @SuppressWarnings( "unchecked" )    // no way to test type at compile time
    public <ItemType> IdentityReference<ItemType> store( final ItemType item ) {
        if ( !EQUALITY_REFERENCES.containsKey( item ) ) {
            EQUALITY_REFERENCES.put( item, new EqualityReference<Object>() );
        }
        final EqualityReference<ItemType> equalityReference = (EqualityReference<ItemType>)EQUALITY_REFERENCES.get( item );
        return equalityReference.add( item, ++_objectCounter );
    }
}



/** reference to a collection of objects which are equal among themselves */
class EqualityReference<ItemType> {
    /** list of identity references */
    final private List<IdentityReference<ItemType>> IDENTITY_REFERENCES;
    
    
    /** Constructor */
    public EqualityReference() {
        IDENTITY_REFERENCES = new ArrayList<IdentityReference<ItemType>>();
    }
    
    
    /** add the object to the set of equals */
    public IdentityReference<ItemType> add( final ItemType item, final long uniqueID ) {
        for ( final IdentityReference<ItemType> reference : IDENTITY_REFERENCES ) {
            if ( reference.getItem() == item ) {
                reference.setHasMultiple( true );
                return reference;
            }
        }
        
        final IdentityReference<ItemType> reference = new IdentityReference<ItemType>( item, uniqueID );
        IDENTITY_REFERENCES.add( reference );
        return reference;
    }
}



/** reference to an object along with the count */
class IdentityReference<ItemType> {
    /** referenced item */
    final private ItemType ITEM;
    
    /** unique ID for this object */
    final private long ID;
    
    /** indicates multiple references to the item */
    private boolean _hasMultiple;
    
    
    /** Constructor */
    public IdentityReference( final ItemType item, final long uniqueID ) {
        ITEM = item;
        ID = uniqueID;
        _hasMultiple = false;
    }
    
    
    /** get the item */
    public ItemType getItem() {
        return ITEM;
    }
    
    
    /** get the unique ID for the item */
    public long getID() {
        return ID;
    }
    
    
    /** indicates whether more than one reference exists to the referenced item */
    public boolean hasMultiple() {
        return _hasMultiple;
    }
    
    
    /** mark whether multiple (more than one) references are made to the item */
    public void setHasMultiple( final boolean hasMultiple ) {
        _hasMultiple = hasMultiple;
    }
}



/** conversion adaptors container whose contents cannot be changed */
class ConversionAdaptorStore {
    /** custom key identifying a custom type translated in terms of JSON representations */
    static final public String EXTENDED_TYPE_KEY = "__XALTYPE";
    
    /** custom key identifying a custom value to translate in terms of JSON representations */
    static final public String EXTENDED_VALUE_KEY = "value";
    
    /** adaptors between all custom types and representation JSON types */
    final protected Map<String,ConversionAdaptor> TYPE_EXTENSION_ADAPTORS;
    
    
    /** Constructor */
    protected ConversionAdaptorStore() {
        TYPE_EXTENSION_ADAPTORS = new HashMap<String,ConversionAdaptor>();
    }
    
    
    /** Unmodifiable Copy Constructor */
    public ConversionAdaptorStore( final ConversionAdaptorStore sourceAdaptors ) {
        TYPE_EXTENSION_ADAPTORS = Collections.unmodifiableMap( sourceAdaptors.TYPE_EXTENSION_ADAPTORS );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        final List<String> types = new ArrayList<String>();
        
        types.add( Boolean.class.getName() );
        types.add( Double.class.getName() );
        types.add( Long.class.getName() );
        types.add( Map.class.getName() );
        types.add( Object[].class.getName() );
        types.add( String.class.getName() );
        
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
    
    
    /** Get the conversion adaptor for the given value */
    public ConversionAdaptor getConversionAdaptor( final String valueType ) {
        return TYPE_EXTENSION_ADAPTORS.get( valueType );
    }
}



/** conversion adaptors container whose contents can be changed */
class MutableConversionAdaptorStore extends ConversionAdaptorStore {    
    /** Constructor */
    public MutableConversionAdaptorStore( final boolean shouldRegisterStandardExtensions ) {
        super();
        if( shouldRegisterStandardExtensions )  registerStandardExtensions();
    }
    
    
    /** Copy Constructor */
    public MutableConversionAdaptorStore( final MutableConversionAdaptorStore sourceAdaptors ) {
        this( false );
        TYPE_EXTENSION_ADAPTORS.putAll( sourceAdaptors.TYPE_EXTENSION_ADAPTORS );
    }
    
    
    /** 
     * Register the custom type and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        TYPE_EXTENSION_ADAPTORS.put( type.getName(), adaptor );
    }
    
    
    /** register the standard type extensions (only needs to be done for the default coder) */
    private void registerStandardExtensions() {
        registerType( Short.class, new ConversionAdaptor<Short,Long>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Long toRepresentation( final Short custom ) {
                return custom.longValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Short toNative( final Long representation ) {
                return representation.shortValue();
            }
        });
        
        registerType( Integer.class, new ConversionAdaptor<Integer,Long>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Long toRepresentation( final Integer custom ) {
                return custom.longValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Integer toNative( final Long representation ) {
                return representation.intValue();
            }
        });
                
        registerType( Float.class, new ConversionAdaptor<Float,Double>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Double toRepresentation( final Float custom ) {
                return custom.doubleValue();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Float toNative( final Double representation ) {
                return representation.floatValue();
            }
        });
        
        registerType( Date.class, new ConversionAdaptor<Date,Long>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Long toRepresentation( final Date timestamp ) {
                return timestamp.getTime();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Date toNative( final Long msecFromEpoch ) {
                return new Date( msecFromEpoch );
            }
        });
        
        registerType( ArrayList.class, new ConversionAdaptor<ArrayList,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final ArrayList list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public ArrayList toNative( final Object[] array ) {
                final ArrayList list = new ArrayList( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        registerType( Vector.class, new ConversionAdaptor<Vector,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final Vector list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Vector toNative( final Object[] array ) {
                final Vector list = new Vector( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        registerType( Hashtable.class, new ConversionAdaptor<Hashtable,Map>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )    // map and table don't have compile time types
            public Map toRepresentation( final Hashtable table ) {
                return new HashMap( table );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Hashtable toNative( final Map map ) {
                return new Hashtable( map );
            }
        });
    }
}



