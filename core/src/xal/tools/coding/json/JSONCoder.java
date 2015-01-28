//
//  JSONCoder.java
//  xal
//
//  Created by Tom Pelaia on 6/16/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding.json;

import xal.tools.coding.*;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.*;
import java.io.*;


/** encode and decode objects with JSON */
public class JSONCoder implements Coder {
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
    
    
    /** Get a list of the standard types encoded directly into JSON */
    static public List<String> getStandardTypes() {
        return ConversionAdaptorStore.getStandardTypes();
    }
    
    
    /** Determine whether the specified type is a standard JSON type */
    static public boolean isStandardType( final String type ) {
        return ConversionAdaptorStore.isStandardType( type );
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
     * Register the custom type by class and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        CONVERSION_ADAPTOR_STORE.registerType( type, adaptor );
    }
    
    
    /** Get the conversion adaptor for the given value */
    protected ConversionAdaptor<?,?> getConversionAdaptor( final String valueType ) {
        return CONVERSION_ADAPTOR_STORE.getConversionAdaptor( valueType );
    }
    
    
	/** 
	 * Decode the JSON string using the default coder
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
	static public Object defaultDecode( final String archive ) {
        return DEFAULT_CODER.decode( archive );
	}
    
    
	/** 
	 * Decode the JSON string
	 * @param archive JSON string representation of an object
	 * @return an object with the data described in the archive
	 */
    public Object decode( final String archive ) {
        final JSONDecoder decoder = JSONDecoder.getInstance( archive, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
		return decoder != null ? decoder.decode() : null;
    }
	
	
	/** 
     * Encode the object as a JSON string using the default encoder
     * @param value the object to encode
     * @return a JSON string representing the value
     */
    static public String defaultEncode( final Object value ) {
        return DEFAULT_CODER.encode( value );
	}
	
	
	/** 
     * Encode the object as a JSON string 
     * @param value the object to encode
     * @return a JSON string representing the value
     */
    public String encode( final Object value ) {
        return JSONEncoder.encode( value, new ConversionAdaptorStore( CONVERSION_ADAPTOR_STORE ) );
	}
}



/** encode an object graph into JSON */
class JSONEncoder {
	/** store of conversion adaptors to use when instantiating new instances from the JSON archive */
	final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;

	/** stores references to objects already decoded and referenced in new objects */
	private ReferenceStore _referenceStore;


	/** Constructor */
	protected JSONEncoder( final ConversionAdaptorStore conversionAdaptorStore ) {
		CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;

		_referenceStore = null;
	}


	/** Get a decoder for the archive */
	public static JSONEncoder getInstance( final ConversionAdaptorStore conversionAdaptorStore ) {
		return new JSONEncoder( conversionAdaptorStore );
	}


	/** get the reference store */
	public ReferenceStore getReferenceStore() {
		return _referenceStore;
	}


	/** get the conversion adaptor store */
	public ConversionAdaptorStore getConversionAdaptorStore() {
		return CONVERSION_ADAPTOR_STORE;
	}


	/** encode the specified value */
	public String encode( final Object value ) {
		_referenceStore = new ReferenceStore();

		final AbstractEncoder<?> rootEncoder = getEncoder( value );
		rootEncoder.preprocess( this, value );

		final StringBuilder jsonBuilder = new StringBuilder();
		rootEncoder.encode( this, jsonBuilder, value );

		return jsonBuilder.toString();
	}


	/** encode the specified value into JSON */
	static public String encode( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
		return JSONEncoder.getInstance( conversionAdaptorStore ).encode( value );
	}


	/** get the encoder for the specified value */
	@SuppressWarnings( "unchecked" )    // no way to guarantee at compile time conversion types
	protected AbstractEncoder<?> getEncoder( final Object value ) {
		final Class<?> valueClass = value != null ? value.getClass() : null;

		if ( valueClass == null ) {
			return NullEncoder.getInstance();
		}
		else if ( valueClass.equals( Boolean.class ) ) {
			return BooleanEncoder.getInstance();
		}
		// handle each immediate concrete subclass of Number
		else if ( valueClass.equals( JSONNumber.class ) ) {
			return NumberEncoder.getInstance();
		}
		else if ( valueClass.equals( String.class ) ) {
			return StringEncoder.getInstance();
		}
		else {      // these are the ones that support references
			if ( valueClass.equals( HashMap.class ) ) {  // no way to check at compile time that the key type is string
				return DictionaryEncoder.getInstance();
			}
			else if ( valueClass.isArray() ) {
				return ArrayEncoder.getInstance( value );
			}
			else if ( CONVERSION_ADAPTOR_STORE.isExtendedClass( valueClass ) ) {  // if the type is not among the standard ones then look to extensions
				return ExtensionEncoder.getInstance();
			}
			else if ( value instanceof Serializable ) {
				return SerializationEncoder.getInstance();
			}
			else {
				throw new RuntimeException( "No JSON support for the object of type: " + valueClass );
			}
		}
	}
}



/** Base class of encoders */
abstract class AbstractEncoder<DataType> {
	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	abstract public void preprocess( final JSONEncoder encoder, final Object value );


	/** encode the specified object to the JSON builder */
	abstract public void encode( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value );


	/** encode the specified object to the JSON builder */
	abstract public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value );
}



/** Base class of encoders for hard objects (not references) */
abstract class HardEncoder<DataType> extends AbstractEncoder<DataType> {
	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {}


	/** encode the specified object to the JSON builder */
	public void encode( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		encodeRaw( encoder, jsonBuilder, value );
	}
}



/** Base class of encoders for objects that support references */
abstract class SoftValueEncoder<DataType> extends AbstractEncoder<DataType> {
	/** key to indicate a reference */
	static final public String REFERENCE_KEY = "__XALREF";

    /** key identifies an object that is referenced */
    static final public String OBJECT_ID_KEY = "__XALID";
    
    /** key for the referenced value */
    static final public String VALUE_KEY = "value";


	/** encode the specified object to the JSON builder */
	public void encode( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		if ( allowsReference( value ) ) {
			final ReferenceStore referenceStore = encoder.getReferenceStore();
			final IdentityReference<?> identityReference = referenceStore.getIdentityReference( value );
			if ( identityReference != null && identityReference.hasMultiple() ) {
				if ( identityReference.isEncoded() ) {
					// create dictionary with the reference
					encodeReference( encoder, jsonBuilder, value, identityReference.getID() );
				} else {
					// first mark the reference as encoded in case there is a nested reference to itself
					identityReference.setEncoded( true );		// further encoding of the value will be encoded as references

					// create dictionary with the value so we can generate an object that can be referenced
					encodeReferenceSource( encoder, jsonBuilder, value, identityReference.getID() );
				}
			} else {
				encodeRaw( encoder, jsonBuilder, value );
			}
		} else {
			encodeRaw( encoder, jsonBuilder, value );
		}
	}


	/** encode the string */
	@SuppressWarnings( "unchecked" )	// need to cast the value to Map<String,Object>
	private void encodeReferenceSource( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value, final long referenceID ) {
		jsonBuilder.append( "{" );

		StringEncoder.getInstance().encodeRaw( encoder, jsonBuilder, OBJECT_ID_KEY );
		jsonBuilder.append( " : " );
		NumberEncoder.getInstance().encode( encoder, jsonBuilder, referenceID );

		jsonBuilder.append( ", " );
		StringEncoder.getInstance().encodeRaw( encoder, jsonBuilder, VALUE_KEY );
		jsonBuilder.append( " : " );
		encoder.getEncoder( value ).encodeRaw( encoder, jsonBuilder, value );

		jsonBuilder.append( "}" );
	}


	/** encode the string */
	@SuppressWarnings( "unchecked" )	// need to cast the value to Map<String,Object>
	private void encodeReference( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value, final long referenceID ) {
		jsonBuilder.append( "{" );

		jsonBuilder.append( "\"" + REFERENCE_KEY + "\"" );
		jsonBuilder.append( " : " );
		NumberEncoder.getInstance().encode( encoder, jsonBuilder, referenceID );

		jsonBuilder.append( "}" );
	}


	/** determine whether the value allows referencing */
	public boolean allowsReference( final Object value ) {
		return true;
	}


	/** encode the raw value directly */
	abstract public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value );
}



/** encode a null to JSON */
class NullEncoder extends HardEncoder<Object> {
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


	/** encode the specified object to the JSON builder */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		jsonBuilder.append( "null" );
	}
}



/** encode a string to JSON */
class StringEncoder extends SoftValueEncoder<String> {
	/** encoder singleton */
	static final private StringEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new StringEncoder();
	}


	/** get the shared instance */
	static public StringEncoder getInstance() {
		return SHARED_ENCODER;
	}


    /** determine whether the string allows referencing */
	public boolean allowsReference( final String value ) {
        return value.length() > 20;     // don't bother using references unless the string is long enough to warrant the overhead
    }


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		if ( allowsReference( value ) ) {
			final ReferenceStore referenceStore = encoder.getReferenceStore();
			referenceStore.store( value );
		}
	}


	/** encode the string */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		jsonBuilder.append( toJSON( value.toString() ) );
	}


	/** encode a String value as a JSON String */
	public static String toJSON( final String value ) {
		return "\"" + value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + "\"";
	}
}



/** encode a boolean to JSON */
class BooleanEncoder extends HardEncoder<Boolean> {
	/** encoder singleton */
	static final private BooleanEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new BooleanEncoder();
	}


	/** get the shared instance */
	static public BooleanEncoder getInstance() {
		return SHARED_ENCODER;
	}


	/** encode the specified object to the JSON builder */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		jsonBuilder.append( ((Boolean)value).booleanValue() ? "true" : "false" );
	}
}



/** encode a number to JSON */
class NumberEncoder extends HardEncoder<Number> {
	/** encoder singleton */
	static final private NumberEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new NumberEncoder();
	}


	/** get the shared instance */
	static public NumberEncoder getInstance() {
		return SHARED_ENCODER;
	}


	/** encode the specified object to the JSON builder */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		jsonBuilder.append( value.toString() );
	}
}



/** encode a hash map to JSON */
class DictionaryEncoder extends SoftValueEncoder<Map<String,Object>> {
	/** encoder singleton */
	static final private DictionaryEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new DictionaryEncoder();
	}


	/** get the shared instance */
	static public DictionaryEncoder getInstance() {
		return SHARED_ENCODER;
	}


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	@SuppressWarnings( "unchecked" )	// need to cast the value to Map<String,Object>
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		final ReferenceStore referenceStore = encoder.getReferenceStore();
		referenceStore.store( value );

		final Map<String,Object> dictionary = (Map<String,Object>)value;
		for ( final Map.Entry<String,Object> entry : dictionary.entrySet() ) {
			final String entryKey = entry.getKey();
			final Object entryValue = entry.getValue();

			StringEncoder.getInstance().preprocess( encoder, entryKey );
			encoder.getEncoder( entryValue ).preprocess( encoder, entryValue );
		}
	}


	/** encode the string */
	@SuppressWarnings( "unchecked" )	// need to cast the value to Map<String,Object>
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		final Map<String,Object> dictionary = (Map<String,Object>)value;

		jsonBuilder.append( "{" );

		int index = 0;
		for ( final Map.Entry<String,Object> entry : dictionary.entrySet() ) {
			switch ( index ) {
				case 0:
					break;
				default:
					jsonBuilder.append( ", " );
					break;
			}

			final String entryKey = entry.getKey();
			StringEncoder.getInstance().encode( encoder, jsonBuilder, entryKey );

			jsonBuilder.append( ": " );

			final Object entryValue = entry.getValue();
			encoder.getEncoder( entryValue ).encode( encoder, jsonBuilder, entryValue );

			++index;
		}

		jsonBuilder.append( "}" );
	}
}


/** encoder for extensions which piggybacks on the dictionary encoder */
class ExtensionEncoder extends SoftValueEncoder<Object> {
	/** custom key identifying a custom type translated in terms of JSON representations */
	static final public String EXTENDED_TYPE_KEY = "__XALTYPE";

	/** custom key identifying a custom value to translate in terms of JSON representations */
	static final public String EXTENDED_VALUE_KEY = "value";

	/** encoder singleton */
	static final private ExtensionEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new ExtensionEncoder();
	}


	/** get the shared instance */
	static public ExtensionEncoder getInstance() {
		return SHARED_ENCODER;
	}

    
    /** get the value type for the specified value */
    static private String getValueType( final Object value ) {
        return value.getClass().getName();
    }


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		final ReferenceStore referenceStore = encoder.getReferenceStore();
		referenceStore.store( value );

		// NOTE: don't want to reference the dictionary itself, but just the dictionary's keys and values
		// create dictionary with the value so we can generate an object that can be referenced
//		final ConversionAdaptorStore conversionAdaptorStore = encoder.getConversionAdaptorStore();
//		final HashMap<String,Object> valueRep = getValueRep( value, conversionAdaptorStore );
//		DictionaryEncoder.getInstance().preprocess( encoder, valueRep );		// preprocess the value rep
	}


	/** encode the string */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		// create dictionary with the value so we can generate an object that can be referenced
		final ConversionAdaptorStore conversionAdaptorStore = encoder.getConversionAdaptorStore();
		final HashMap<String,Object> valueRep = getValueRep( value, conversionAdaptorStore );
		DictionaryEncoder.getInstance().encodeRaw( encoder, jsonBuilder, valueRep );		// encode this dictionary directly
	}


	/** get the value representation as a dictionary keyed for the extended type and value */
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	static private HashMap<String,Object> getValueRep( final Object value, final ConversionAdaptorStore conversionAdaptorStore ) {
		final String valueType = getValueType( value );
		final ConversionAdaptor adaptor = conversionAdaptorStore.getConversionAdaptor( valueType );
		if ( adaptor != null ) {
			final HashMap<String,Object> valueRep = new HashMap<String,Object>();
			final Object representationValue = adaptor.toRepresentation( value );
			valueRep.put( EXTENDED_TYPE_KEY, valueType );
			valueRep.put( EXTENDED_VALUE_KEY, representationValue );
			return valueRep;
		}
		else {
			throw new RuntimeException( "No coder for encoding objects of type: " + valueType );
		}
	}
}



/** encoder for serialized objects which piggybacks on the dictionary encoder */
class SerializationEncoder extends SoftValueEncoder<Serializable> {
    /** custom key identifying the serialization byte array */
    static final public String SERIALIZATION_VALUE_KEY = "__XALSERIALIZATION";

	/** encoder singleton */
	static final private SerializationEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new SerializationEncoder();
	}


	/** get the shared instance */
	static public SerializationEncoder getInstance() {
		return SHARED_ENCODER;
	}

    
    /** get the serialization byte array */
    @SuppressWarnings( "unchecked" )
    static private HashMap<String,Object> getValueRep( final Object value ) {
        try {
            final HashMap<String,Object> valueRep = new HashMap<String,Object>();
            final ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutStream = new ObjectOutputStream( byteOutStream );
            objectOutStream.writeObject( value );
            objectOutStream.flush();
                        
            valueRep.put( SERIALIZATION_VALUE_KEY, byteOutStream.toByteArray() );
            
            objectOutStream.close();
            byteOutStream.close();
                        
            return valueRep;
        }
        catch ( IOException exception ) {
            throw new RuntimeException( "Exception serializing object: " + value , exception );
        }
    }


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		final ReferenceStore referenceStore = encoder.getReferenceStore();
		referenceStore.store( value );

		// NOTE: don't want to reference the dictionary itself, but just the dictionary's keys and values
		// create dictionary with the value so we can generate an object that can be referenced
		//		final HashMap<String,Object> valueRep = getValueRep( value );
		//		DictionaryEncoder.getInstance().preprocess( encoder, valueRep );		// preprocess the value rep
	}


	/** encode the string */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		// create dictionary with the value so we can generate an object that can be referenced
		final HashMap<String,Object> valueRep = getValueRep( value );
		DictionaryEncoder.getInstance().encodeRaw( encoder, jsonBuilder, valueRep );		// encode this dictionary directly
	}
}



/** encoder for an array of items of a common extended type which piggybacks on the dictionary encoder */
class TypedArrayEncoder extends ArrayEncoder {
    /** primitive type wrappers keyed by type */
    final static private Map<Class<?>,Class<?>> PRIMITIVE_TYPE_WRAPPERS;

    /** key for identifying the array data of an extended type */
    static final public String ARRAY_ITEM_TYPE_KEY = "__XALITEMTYPE";
    
    /** key for identifying the array data of an extended type */
    static final public String ARRAY_KEY = "array";
    
    /** primitive classes keyed by type name */
    static final private Map<String,Class<?>> PRIMITIVE_CLASSES;

	/** encoder singleton */
	static final private TypedArrayEncoder SHARED_ENCODER;

    
    // static initializer
    static {
        PRIMITIVE_CLASSES = generatePrimitiveClassMap();
        PRIMITIVE_TYPE_WRAPPERS = populatePrimitiveTypeWrappers();

		SHARED_ENCODER = new TypedArrayEncoder();
    }

	
	/** get the shared instance */
	static public TypedArrayEncoder getInstance() {
		return SHARED_ENCODER;
	}


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		final ReferenceStore referenceStore = encoder.getReferenceStore();
		referenceStore.store( value );

		// NOTE: don't want to reference the dictionary itself, but just the dictionary's keys and values
		// create dictionary with the value so we can generate an object that can be referenced
	}


	/** encode the string */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object array ) {
		final String itemType = getComponentType( array );

		final ConversionAdaptorStore conversionAdaptorStore = encoder.getConversionAdaptorStore();
		final boolean isItemExtendedType = conversionAdaptorStore.isExtendedType( itemType );

		final int arrayLength = Array.getLength( array );
		final Object[] objectArray = new Object[ arrayLength ];    // encode as a generic object array
		for ( int index = 0 ; index < arrayLength ; index++ ) {
			objectArray[index] = Array.get( array, index );
		}

		jsonBuilder.append( "{" );

		StringEncoder.getInstance().encodeRaw( encoder, jsonBuilder, ARRAY_ITEM_TYPE_KEY );
		jsonBuilder.append( " : " );
		StringEncoder.getInstance().encodeRaw( encoder, jsonBuilder, itemType );

		jsonBuilder.append( ", " );
		StringEncoder.getInstance().encodeRaw( encoder, jsonBuilder, ARRAY_KEY );
		jsonBuilder.append( " : " );

		final boolean isExtendedType = conversionAdaptorStore.isExtendedType( itemType );
		if ( !isExtendedType ) {
			super.encodeRaw( encoder, jsonBuilder, objectArray );
		}
		else {
			@SuppressWarnings( "rawtypes" )
			final ConversionAdaptor adaptor = conversionAdaptorStore.getConversionAdaptor( itemType );
			if ( adaptor != null ) {
				encodeExtendedTypeArray( encoder, jsonBuilder, objectArray, adaptor );
			}
			else {
				throw new RuntimeException( "Unknown extended type: " + itemType );
			}
		}

		jsonBuilder.append( "}" );
	}


	/** manually encode the extended type array by excluding the extended type for each item since it is already included as a common type for the entire array */
	@SuppressWarnings( "rawtypes" )
	private void encodeExtendedTypeArray( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value, final ConversionAdaptor adaptor ) {
		final String itemType = getComponentType( value );

		final Object[] array = (Object[])value;

		jsonBuilder.append( "[" );
		for ( int index = 0 ; index < array.length ; index++ ) {
			switch ( index ) {
				case 0:
					break;
				default:
					jsonBuilder.append( ", " );
					break;
			}

			// encode the item
			final Object item = array[index];
			@SuppressWarnings( "unchecked" )
			final Object representationValue = adaptor.toRepresentation( item );
			encoder.getEncoder( representationValue ).encode( encoder, jsonBuilder, representationValue );
		}
		jsonBuilder.append( "]" );

	}

    
    /** Get the component type appropriate for an Object (e.g. wrapper for a primitive) */
    private static String getComponentObjectType( final Object array ) {
        final Class<?> componentClass = array.getClass().getComponentType();
        return getObjectTypeForClass( componentClass );
    }
    
    
    /** Get the type appropriate for an instance of the specified class (e.g. wrapper for a primitive) of the specified raw type */
    public static String getObjectTypeForClass( final Class<?> rawClass ) {
        final Class<?> wrapperClass = PRIMITIVE_TYPE_WRAPPERS.get( rawClass );
        final Class<?> objectClass = wrapperClass != null ? wrapperClass : rawClass;
        return objectClass.getName();
    }
    
    
    /** get the raw component type for the specified array */
    static private String getComponentType( final Object array ) {
        return array.getClass().getComponentType().getName();
    }
    
    
    /** get the primitive type for the specified type name */
    static public Class<?> getPrimitiveType( final String typeName ) {
        return PRIMITIVE_CLASSES.get( typeName );
    }
    
    
    /** populate the table of primitive type wrappers */
    private static Map<Class<?>,Class<?>> populatePrimitiveTypeWrappers() {
        final Map<Class<?>,Class<?>> table = new Hashtable<Class<?>,Class<?>>();
        
        table.put( Integer.TYPE, Integer.class );
        table.put( Long.TYPE, Long.class );
        table.put( Short.TYPE, Short.class );
        table.put( Byte.TYPE, Byte.class );
        table.put( Character.TYPE, Character.class );
        table.put( Float.TYPE, Float.class );
        table.put( Double.TYPE, Double.class );
        table.put( Boolean.TYPE, Boolean.class );
        
        return table;
    }

    
    /** generate the table of primitive classes keyed by name */
    static private Map<String,Class<?>> generatePrimitiveClassMap() {
        final Map<String,Class<?>> classTable = new Hashtable<String,Class<?>>();
        registerPrimitiveType( classTable, Float.TYPE );
        registerPrimitiveType( classTable, Double.TYPE );
        registerPrimitiveType( classTable, Byte.TYPE );
        registerPrimitiveType( classTable, Character.TYPE );
        registerPrimitiveType( classTable, Short.TYPE );
        registerPrimitiveType( classTable, Integer.TYPE );
        registerPrimitiveType( classTable, Long.TYPE );
        return classTable;
    }
    
    
    /** register the primitive type in the table */
    static private void registerPrimitiveType( final Map<String,Class<?>> table, final Class<?> type ) {
        table.put( type.getName(), type );
    }
}



/** encode an array to JSON */
class ArrayEncoder extends SoftValueEncoder<Object[]> {
	/** encoder singleton */
	static final private ArrayEncoder SHARED_ENCODER;


	// static initializer
	static {
		SHARED_ENCODER = new ArrayEncoder();
	}


	/** get the shared instance */
	static public ArrayEncoder getInstance( final Object value ) {
		return isTypedArray( value ) ? TypedArrayEncoder.getInstance() : SHARED_ENCODER;
	}


	/** preprocess the object graph prior to encoding so the references can be resolved and encoded in order (definition first then any references to it) */
	public void preprocess( final JSONEncoder encoder, final Object value ) {
		final ReferenceStore referenceStore = encoder.getReferenceStore();
		referenceStore.store( value );

		final Object[] array = (Object[])value;
		for ( final Object item : array ) {
			encoder.getEncoder( item ).preprocess( encoder, item );
		}
	}


	/** encode the string */
	public void encodeRaw( final JSONEncoder encoder, final StringBuilder jsonBuilder, final Object value ) {
		final Object[] array = (Object[])value;

		jsonBuilder.append( "[" );
		for ( int index = 0 ; index < array.length ; index++ ) {
			switch ( index ) {
				case 0:
					break;
				default:
					jsonBuilder.append( ", " );
					break;
			}

			// encode the item
			final Object item = array[index];
			encoder.getEncoder( item ).encode( encoder, jsonBuilder, item );
		}
		jsonBuilder.append( "]" );
	}


    /** Determine whether the array is of a common extended type */
    static private boolean isTypedArray( final Object array ) {
        final Class<?> itemClass = array.getClass().getComponentType();
		final boolean isTyped = itemClass != null && itemClass != Object.class;
		return isTyped;
    }
}



/** Decode JSON into an object graph */
class JSONDecoder {
	/** JSON archive to parse */
	final private String JSON_ARCHIVE;

	/** store of conversion adaptors to use when instantiating new instances from the JSON archive */
	final private ConversionAdaptorStore CONVERSION_ADAPTOR_STORE;

	/** stores references to objects already decoded and referenced in new objects */
	private KeyedReferenceStore _referenceStore;

	/** current position of the scanner in the archive */
	private int _scanPosition;


	/** Constructor */
	protected JSONDecoder( final String jsonArchive, final ConversionAdaptorStore conversionAdaptorStore ) {
		JSON_ARCHIVE = jsonArchive.trim();
		CONVERSION_ADAPTOR_STORE = conversionAdaptorStore;

		_scanPosition = 0;
		_referenceStore = null;
	}


	/** Get a decoder for the archive */
	public static JSONDecoder getInstance( final String jsonArchive, final ConversionAdaptorStore conversionAdaptorStore ) {
		return new JSONDecoder( jsonArchive, conversionAdaptorStore );
	}


	/** get the current scan position */
	public int getScanPosition() {
		return _scanPosition;
	}


	/** set the scan postion */
	public void setScanPosition( final int scanPosition ) {
		_scanPosition = scanPosition;
	}


	/** advance the scan position the specified number of steps */
	public void advanceScanPosition( final int scanLength ) {
		_scanPosition += scanLength;
	}


	/** get the JSON Archive */
	public String getArchive() {
		return JSON_ARCHIVE;
	}


	/** get the conversion adaptor store */
	public ConversionAdaptorStore getConversionAdaptorStore() {
		return CONVERSION_ADAPTOR_STORE;
	}


	/** get the keyed reference store */
	public KeyedReferenceStore getReferenceStore() {
		return _referenceStore;
	}


	/** decode the archive */
	public Object decode() {
		_scanPosition = 0;
		_referenceStore = new KeyedReferenceStore();

		return parseNext();
	}


	/** parse the next object starting at the current scan position and advance the scan position */
	public Object parseNext() {
		final AbstractDecoder<?> nextDecoder = nextDecoder();
		if ( nextDecoder != null ) {
			return nextDecoder.decode( this );
		}
		else {
			return null;
		}
	}


	/** get the next decoder */
	private AbstractDecoder<?> nextDecoder() {
		if ( _scanPosition < JSON_ARCHIVE.length() ) {
			final char nextChar = JSON_ARCHIVE.charAt( _scanPosition );

			switch ( nextChar ) {
			case '+': case '-': case '.':
			case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7': case '8': case '9':
				return NumberDecoder.getInstance();
			case 't': case 'f':
				return BooleanDecoder.getInstance();
			case 'n':
				return NullDecoder.getInstance();
			case '\"':
				return StringDecoder.getInstance();
			case '[':
				return ArrayDecoder.getInstance();
			case '{':
				return DictionaryDecoder.getInstance();
			default:
				if ( Character.isWhitespace( nextChar ) ) {		// ignore whitespace
					++_scanPosition;	// increment the scan position
					return nextDecoder();
				}
				else {
					return null;
				}
			}
		}
		else {
			return null;	// nothing left to parse
		}
	}
}



/** Base class of decoders */
abstract class AbstractDecoder<DataType> {
	/** decode the source to extract the next object */
	abstract protected DataType decode( final JSONDecoder source );
}



/** decode a number from a source string */
class NumberDecoder extends AbstractDecoder<JSONNumber> {
	/** default number decoder */
	static private final NumberDecoder DEFAULT_DECODER;

	/** pattern for matching a number */
	static final Pattern NUMBER_PATTERN;
	
	
	// static initializer
	static {
		NUMBER_PATTERN = Pattern.compile( "[+-]?((\\d+\\.?\\d*)|(\\.?\\d+))([eE][+-]?\\d+)?" );
		DEFAULT_DECODER = new NumberDecoder();
	}
	

	/** get an instance of the number decoder */
	static public NumberDecoder getInstance() {
		return DEFAULT_DECODER;
	}
	
	
	/** decode the source to extract the next object */	
	protected JSONNumber decode( final JSONDecoder source ) {
		final int startScanPosition = source.getScanPosition();
		final Matcher matcher = NUMBER_PATTERN.matcher( source.getArchive() );
		if ( matcher.find( startScanPosition ) ) {
			final String match = matcher.group();
			if ( match != null ) {
				source.advanceScanPosition( matcher.end() - startScanPosition );
				return JSONNumber.valueOf( match );
			}
			else {
				throw new RuntimeException( "JSON Number parse exception at position: " + startScanPosition );
			}
		}
		else {
			throw new RuntimeException( "JSON Number parse exception at position: " + startScanPosition );
		}
	}
}



/** decode a boolean from a source string */
class BooleanDecoder extends AbstractDecoder<Boolean> {
	/** default boolean decoder */
	static private final BooleanDecoder DEFAULT_DECODER;

	
	// static initializer
	static {
		DEFAULT_DECODER = new BooleanDecoder();
	}


	/** get an instance of the number decoder */
	static public BooleanDecoder getInstance() {
		return DEFAULT_DECODER;
	}

	
	/** decode the source to extract the next object */	
	protected Boolean decode( final JSONDecoder source ) {
		final int startScanPosition = source.getScanPosition();
		final String archive = source.getArchive();
		final char firstChar = archive.charAt( startScanPosition );

		final int scanLength = firstChar == 't' ? 4 : 5;
		if ( startScanPosition + scanLength <= archive.length() ) {
			final String input = archive.substring( startScanPosition, startScanPosition + scanLength );
			if ( input.equals( "true" ) ) {
				source.advanceScanPosition( scanLength );
				return true;
			}
			else if ( input.equals( "false" ) ) {
				source.advanceScanPosition( scanLength );
				return false;
			}
			else {
				throw new RuntimeException( "JSON boolean parse exception at position: " + startScanPosition );
			}
		}
		else {
			throw new RuntimeException( "JSON boolean decode exception at position: " + startScanPosition + ". The input terminated prematurely." );
		}
	}
}



/** decode a null identifier from a source string */
class NullDecoder extends AbstractDecoder<Object> {
	/** default null decoder */
	static private final NullDecoder DEFAULT_DECODER;


	// static initializer
	static {
		DEFAULT_DECODER = new NullDecoder();
	}


	/** get an instance of the number decoder */
	static public NullDecoder getInstance() {
		return DEFAULT_DECODER;
	}


	/** decode the source to extract the next object */
	protected Object decode( final JSONDecoder source ) {
		final int startScanPosition = source.getScanPosition();
		final String archive = source.getArchive();
		final char firstChar = archive.charAt( startScanPosition );

		final int scanLength = 4;
		if ( startScanPosition + scanLength <= archive.length() ) {
			final String input = archive.substring( startScanPosition, startScanPosition + scanLength );
			if ( input.equals( "null" ) ) {
				source.advanceScanPosition( scanLength );
				return null;
			}
			else {
				throw new RuntimeException( "JSON null parse exception at position: " + startScanPosition );
			}
		}
		else {
			throw new RuntimeException( "JSON null decode exception at position: " + startScanPosition + ". The input terminated prematurely." );
		}
	}
}



/** decode a string from a source string */
class StringDecoder extends AbstractDecoder<String> {
	/** default string decoder */
	static private final StringDecoder DEFAULT_DECODER;


	// static initializer
	static {
		DEFAULT_DECODER = new StringDecoder();
	}


	/** get an instance of the number decoder */
	static public StringDecoder getInstance() {
		return DEFAULT_DECODER;
	}


	/** decode the source starting at the specified position */
	private String decode( final JSONDecoder source, final int startPosition ) {
		final String archive = source.getArchive();
		final int archiveLength = archive.length();

		int position = startPosition;
		while( true ) {
			if ( position >= archiveLength ) {
				throw new RuntimeException( "JSON String decode exception at position: " + source.getScanPosition() + ". The input terminated prematurely." );
			}

			final char nextChar = archive.charAt( position );

			if ( nextChar == '\\' ) {	// escape character => replace with next character literally
				String prefix = "";
				if ( position > startPosition ) {
					prefix = archive.substring( startPosition, position );	// grab what we've already parsed preceding the escape character
				}
				position += 1;	// skip the escape character
				final char literalChar = archive.charAt( position );	// this is the character immediatel following the escape character to process literally
				return prefix + literalChar + decode( source, position + 1 );		// combine the prefix, literal character and continue processing the characters following it normally
			}
			else if ( nextChar == '"' ) {		// terminating quotation mark
				source.setScanPosition( position + 1 );
				break;
			}
			else {		// normal character
				position += 1;		// increment the scan position
			}
		}

		return archive.substring( startPosition, position );
	}


	/** decode the source to extract the next object */
	protected String decode( final JSONDecoder source ) {
		final int startScanPosition = source.getScanPosition();
		final String archive = source.getArchive();
		final int archiveLength = archive.length();

		// start decoding the string at the character immediately following the initial quotation mark
		return decode( source, startScanPosition + 1 );
	}
}



/** decode an array from a source string */
class ArrayDecoder extends AbstractDecoder<Object[]> {
	/** default array decoder */
	static private final ArrayDecoder DEFAULT_DECODER;


	// static initializer
	static {
		DEFAULT_DECODER = new ArrayDecoder();
	}


	/** get an instance of the number decoder */
	static public ArrayDecoder getInstance() {
		return DEFAULT_DECODER;
	}


	/** decode the source to extract the next object */
	protected Object[] decode( final JSONDecoder source ) {
		final List<Object> items = new ArrayList<Object>();
		appendItems( source, items );
		return items.toArray();
	}

	
	/** append to the items the parsed items from the array string */
	private void appendItems( final JSONDecoder source, final List<Object> items ) {
		final int startScanPosition = source.getScanPosition();
		final String archive = source.getArchive();
		final int archiveLength = archive.length();

		int position = startScanPosition + 1;	// start at first character after leading bracket
		boolean expectingNextItem = true;		// indicates that the next thing we expect is an item (or white space)
		while( true ) {
			if ( position >= archiveLength ) {
				throw new RuntimeException( "JSON Array decode exception at position: " + startScanPosition + ". The input terminated prematurely." );
			}

			final char nextChar = archive.charAt( position );

			if ( Character.isWhitespace( nextChar ) ) {		// ignore whitespace and keep going
				++position;
			}
			else if ( expectingNextItem ) {		// process the next array item
				if ( items.size() == 0 && nextChar == ']' ) {	// we've got an empty array
					source.setScanPosition( position + 1 );
					return;		// we're done with this array
				}
				else {
					expectingNextItem = false;		// need a comma before we can begin parsing the next item
					source.setScanPosition( position );
					final Object item = source.parseNext();
					items.add( item );
					position = source.getScanPosition();	// get the current scan position after having scanned the item
				}
			}
			else {		// not expecting a new item so we expect either a comma or closing bracket
				switch( nextChar ) {
					case ']':		// closing bracket of array
						source.setScanPosition( position + 1 );
						return;		// we're done with this array
					case ',':		// comma preceding next item
						expectingNextItem = true;		// comma indicates we are awaiting the next item
						++position;
						break;
					default:
						throw new RuntimeException( "JSON Array decode exception. Encountered invalid character, " + nextChar + " at position, " + position + "." );
				}
			}
		}
	}
}



/** decode a dictionary from a source string */
class DictionaryDecoder extends AbstractDecoder<Object> {
	/** default dictionary decoder */
	static private final DictionaryDecoder DEFAULT_DECODER;


	// static initializer
	static {
		DEFAULT_DECODER = new DictionaryDecoder();
	}


	/** get an instance of the number decoder */
	static public DictionaryDecoder getInstance() {
		return DEFAULT_DECODER;
	}


	/** decode the source to extract the next object */
	@SuppressWarnings( "unchecked" )    // no way to validate representation value and type at compile time
	protected Object decode( final JSONDecoder source ) {
		final Map<String,Object> dictionary = new HashMap<String,Object>();
		appendItems( source, dictionary );

		final ConversionAdaptorStore conversionAdaptorStore = source.getConversionAdaptorStore();
		final KeyedReferenceStore referenceStore = source.getReferenceStore();

		if ( dictionary.containsKey( ExtensionEncoder.EXTENDED_TYPE_KEY ) && dictionary.containsKey( ExtensionEncoder.EXTENDED_VALUE_KEY ) ) {
			// decode object of extended type
			final String extendedType = (String)dictionary.get( ExtensionEncoder.EXTENDED_TYPE_KEY );
			final Object representationValue = dictionary.get( ExtensionEncoder.EXTENDED_VALUE_KEY );
			return toNative( conversionAdaptorStore, representationValue, extendedType );
		}
		else if ( dictionary.containsKey( TypedArrayEncoder.ARRAY_ITEM_TYPE_KEY ) && dictionary.containsKey( TypedArrayEncoder.ARRAY_KEY ) ) {
			// decode array of with a specified component type from a generic object array
			final String componentType = (String)dictionary.get( TypedArrayEncoder.ARRAY_ITEM_TYPE_KEY );
			final Object[] objectArray = (Object[])dictionary.get( TypedArrayEncoder.ARRAY_KEY );

			try {
				final Class<?> primitiveClass = TypedArrayEncoder.getPrimitiveType( componentType );
				final Class<?> componentClass = primitiveClass != null ? primitiveClass : Class.forName( componentType );
				final String componentObjectType = TypedArrayEncoder.getObjectTypeForClass( componentClass );   // this allows us to handle primitive wrappers
				final Class<?> componentObjectClass = Class.forName( componentObjectType );
				final Object array = Array.newInstance( componentClass, objectArray.length );
				for ( int index = 0 ; index < objectArray.length ; index++ ) {
					final Object rawItem = objectArray[index];
					// if the raw item is an extended type it will automatically have been decoded unless it is of the common component type in which case we tranlate it
					final Object item = componentObjectClass.isInstance( rawItem ) ? rawItem : toNative( conversionAdaptorStore, rawItem, componentObjectType );
					Array.set( array, index, item );
				}
				return array;
			}
			catch( Exception exception ) {
				exception.printStackTrace();
				throw new RuntimeException( "Exception decoding a typed array of type: " + componentType, exception );
			}
		}
		else if ( dictionary.containsKey( SerializationEncoder.SERIALIZATION_VALUE_KEY ) ) {
			final byte[] serializationData = (byte[])dictionary.get( SerializationEncoder.SERIALIZATION_VALUE_KEY );

			try {
				final ByteArrayInputStream byteInputStream = new ByteArrayInputStream( serializationData );
				final ObjectInputStream objectInputStream = new ObjectInputStream( byteInputStream );
				final Object value = objectInputStream.readObject();
				objectInputStream.close();
				byteInputStream.close();
				return value;
			}
			catch ( Exception exception ) {
				throw new RuntimeException( "Exception decoding serialized object from dictionary: " + dictionary, exception );
			}
		}
		else if ( dictionary.containsKey( SoftValueEncoder.OBJECT_ID_KEY ) && dictionary.containsKey( SoftValueEncoder.VALUE_KEY ) ) {
			// decode a referenced object definition and store it
			final JSONNumber itemID = (JSONNumber)dictionary.get( SoftValueEncoder.OBJECT_ID_KEY );
			final Object item = dictionary.get( SoftValueEncoder.VALUE_KEY );
			referenceStore.store( itemID.longValue(), item );
			return item;
		}
		else if ( dictionary.containsKey( SoftValueEncoder.REFERENCE_KEY ) ) {
			// decode a reference to an object in the store
			final JSONNumber itemID = (JSONNumber)dictionary.get( SoftValueEncoder.REFERENCE_KEY );
			return referenceStore.get( itemID.longValue() );
		}
		else {
			return dictionary;
		}
	}


	/** append to the items the parsed items from the array string */
	private void appendItems( final JSONDecoder source, final Map<String,Object> dictionary ) {
		final int startScanPosition = source.getScanPosition();
		final String archive = source.getArchive();
		final int archiveLength = archive.length();

		int position = startScanPosition + 1;	// start at first character after leading bracket
		boolean expectingNextPair = true;		// indicates whether the scanner expects a key value pair next (or white space)
		while( true ) {
			if ( position >= archiveLength ) {
				throw new RuntimeException( "JSON Dictionary decode exception at position: " + startScanPosition + ". The input terminated prematurely." );
			}

			final char nextChar = archive.charAt( position );

			if ( Character.isWhitespace( nextChar ) ) {		// ignore whitespace and keep going
				++position;
			}
			else if ( expectingNextPair ) {		// process the next key/value pair
				if ( dictionary.size() == 0 && nextChar == '}' ) {	// we've got an empty dictionary
					source.setScanPosition( position + 1 );
					return;		// we're done with this dictionary
				}
				else {
					expectingNextPair = false;

					// parse the key
					source.setScanPosition( position );
					final Object keyObject = source.parseNext();
					if ( ! ( keyObject instanceof String ) ) {
						throw new RuntimeException( "JSON Dictionary decode exception at position: " + startScanPosition + ". The key at position, " + position + " is not a String as it should be." );
					}

					final String key = (String)keyObject;
					position = source.getScanPosition();	// get the current scan position after having scanned the key

					// search for the comma while skipping white space
					while ( true ) {
						if ( position >= archiveLength ) {
							throw new RuntimeException( "JSON Dictionary decode exception at position: " + startScanPosition + ". The input terminated prematurely." );
						}

						final char nextSeparatorChar = archive.charAt( position );

						if ( Character.isWhitespace( nextSeparatorChar ) ) {		// ignore whitespace and keep going
							++position;
						}
						else if ( nextSeparatorChar == ':' ) {	// now we got the colon
							++position;
							break;
						}
						else {
							throw new RuntimeException( "Dictionary decode parse exception at position: " + position + ". Invalid character: " + nextChar );
						}
					}

					// now parse the value
					source.setScanPosition( position );
					final Object value = source.parseNext();
					dictionary.put( key, value );
					position = source.getScanPosition();	// get the current scan position after having scanned the value
				}
			}
			else {		// not whitespace and not expecting a key/value pair
				switch( nextChar ) {
					case '}':		// closing brace of dictionary
						source.setScanPosition( position + 1 );
						return;		// we're done with this dictionary
					case ',':		// comma preceding next item
						expectingNextPair = true;		// comma indicates we are awaiting the next key/value pair
						++position;
						break;
					default:
						throw new RuntimeException( "JSON Dictionary decode exception. Encountered invalid character, " + nextChar + " at position, " + position + "." );
				}
			}
		}
	}


    /** Convert the representation value to native using the specified extension type */
    @SuppressWarnings( {"unchecked", "rawtypes"} )
    private Object toNative( final ConversionAdaptorStore conversionAdaptorStore, final Object representationValue, final String extendedType ) {
        final ConversionAdaptor adaptor = conversionAdaptorStore.getConversionAdaptor( extendedType );
        if ( adaptor == null )  throw new RuntimeException( "Missing JSON adaptor for type: " + extendedType );
        return adaptor.toNative( representationValue );
    }
}



/** Stores referenced items keyed by ID */
class KeyedReferenceStore {
    /** references keyed by ID */
    final private Map<Long,Object> REFERENCES;
    
    /** Constructor */
    public KeyedReferenceStore() {
        REFERENCES = new HashMap<Long,Object>();
    }
    
    
    /** store the value associated with the key */
    public void store( final long key, final Object value ) {
        REFERENCES.put( key, value );
    }
    
    
    /** get the item associated with the key */
    public Object get( final long key ) {
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


	/** get the item's identify reference */
	@SuppressWarnings( "unchecked" )    // no way to test type at compile time
	public <ItemType> IdentityReference<ItemType> getIdentityReference( final ItemType item ) {
		if ( EQUALITY_REFERENCES.containsKey( item ) ) {
			final EqualityReference<ItemType> equalityReference = (EqualityReference<ItemType>)EQUALITY_REFERENCES.get( item );
			return equalityReference.getIdentityReference( item );
		} else {
			return null;
		}
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
		final IdentityReference<ItemType> existingReference = getIdentityReference( item );

		if ( existingReference != null ) {
			existingReference.setHasMultiple( true );
			return existingReference;
		} else {
			// create a new reference
			final IdentityReference<ItemType> reference = new IdentityReference<ItemType>( item, uniqueID );
			IDENTITY_REFERENCES.add( reference );
			return reference;
		}
    }


	/** get the identity reference for the specified item */
	public IdentityReference<ItemType> getIdentityReference( final ItemType item ) {
		// search for references that are identical
		for ( final IdentityReference<ItemType> reference : IDENTITY_REFERENCES ) {
			if ( reference.getItem() == item ) {
				return reference;
			}
		}

		// no reference found
		return null;
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

	/** indicates that the reference has already been assigned */
	private boolean _isEncoded;
    
    
    /** Constructor */
    public IdentityReference( final ItemType item, final long uniqueID ) {
        ITEM = item;
        ID = uniqueID;
        _hasMultiple = false;
		_isEncoded = false;
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


	/** gets whether this reference has been encoded so future encoding can just be references */
	public boolean isEncoded() {
		return _isEncoded;
	}


	/** sets whether this reference has been encoded */
	public void setEncoded( final boolean isEncoded ) {
		_isEncoded = isEncoded;
	}
}



/** conversion adaptors container whose contents cannot be changed */
class ConversionAdaptorStore {    
    /** adaptors between all custom types and representation JSON types */
    final protected Map<String,ConversionAdaptor<?,?>> TYPE_EXTENSION_ADAPTORS;
    
    /** set of standard types */
    static final private Set<String> STANDARD_TYPES;
    
    
    // static initializer
    static {
        STANDARD_TYPES = new HashSet<String>();
        populateStandardTypes();
    }
    
    
    /** Constructor */
    protected ConversionAdaptorStore() {
        TYPE_EXTENSION_ADAPTORS = new HashMap<String,ConversionAdaptor<?,?>>();
    }
    
    
    /** Unmodifiable Copy Constructor */
    public ConversionAdaptorStore( final ConversionAdaptorStore sourceAdaptors ) {
        TYPE_EXTENSION_ADAPTORS = Collections.unmodifiableMap( sourceAdaptors.TYPE_EXTENSION_ADAPTORS );
    }
    
    
    /** populate the set of standard types */
    static private void populateStandardTypes() {
        final Set<String> types = new HashSet<String>();
        
        types.add( Boolean.class.getName() );
        types.add( Double.class.getName() );
        types.add( Long.class.getName() );
        types.add( HashMap.class.getName() );
        types.add( Object[].class.getName() );
        types.add( String.class.getName() );
        
        STANDARD_TYPES.addAll( types );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    static public List<String> getStandardTypes() {
        final List<String> types = new ArrayList<String>( STANDARD_TYPES );
        Collections.sort( types );
        return types;
    }
    
    
    /** determine whether the specified type is a standard JSON type */
    static public boolean isStandardType( final String type ) {
        return STANDARD_TYPES.contains( type );
    }
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes() {
        final List<String> types = new ArrayList<String>();
        
        types.addAll( getStandardTypes() );
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
    
    
    /** Determine if the specified object is of an extended type */
    public boolean isOfExtendedType( final Object value ) {
        return isExtendedClass( value.getClass() );
    }
    
    
    /** Determine if the specified class corresponds to an extended type */
    public boolean isExtendedClass( final Class<?> valueClass ) {
        return isExtendedType( valueClass.getName() );
    }
    
    
    /** Determine if the specified type is an extended type */
    public boolean isExtendedType( final String valueType ) {
        return TYPE_EXTENSION_ADAPTORS.containsKey( valueType );
    }
    
    
    /** Get the conversion adaptor for the given value */
    public ConversionAdaptor<?,?> getConversionAdaptor( final String valueType ) {
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
     * Register the custom type by class and its associated adaptor 
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
	 * @param alternateKeys zero or more alternate names used to reference the adaptor (e.g. "double" for "java.lang.Double")
     */
    public <CustomType,RepresentationType> void registerType( final Class<?> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor, final String ... alternateKeys ) {
        registerType( type.getName(), adaptor );

		if ( alternateKeys != null ) {
			for ( final String key : alternateKeys ) {
				registerType( key, adaptor );
			}
		}
    }


    /**
     * Register the custom type by name and its associated adaptor
     * @param type type to identify and process for encoding and decoding
     * @param adaptor translator between the custom type and representation JSON constructs
     */
    public <CustomType,RepresentationType> void registerType( final String type, final ConversionAdaptor<CustomType,RepresentationType> adaptor ) {
        TYPE_EXTENSION_ADAPTORS.put( type, adaptor );
    }
    
    
    /** register the standard type extensions (only needs to be done for the default coder) */
    private void registerStandardExtensions() {
        registerType( Character.class, new ConversionAdaptor<Character,String>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public String toRepresentation( final Character custom ) {
                return custom.toString();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Character toNative( final String representation ) {
                return representation.charAt( 0 );
            }
        });

		registerType( Byte.class, new ConversionAdaptor<Byte,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Byte custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Byte toNative( final JSONNumber representation ) {
				return representation.byteValue();
			}
		}, "byte" );

        registerType( Short.class, new ConversionAdaptor<Short,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Short custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Short toNative( final JSONNumber representation ) {
                return representation.shortValue();
            }
        }, "short" );

        registerType( Integer.class, new ConversionAdaptor<Integer,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Integer custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Integer toNative( final JSONNumber representation ) {
                return representation.intValue();
            }
        }, "int" );

		registerType( Long.class, new ConversionAdaptor<Long,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Long custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Long toNative( final JSONNumber representation ) {
				return representation.longValue();
			}
		}, "long" );

        registerType( Float.class, new ConversionAdaptor<Float,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Float custom ) {
                return new JSONNumber( custom );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Float toNative( final JSONNumber representation ) {
                return representation.floatValue();
            }
        }, "float" );

		registerType( Double.class, new ConversionAdaptor<Double,JSONNumber>() {
			/** convert the custom type to a representation in terms of representation JSON constructs */
			public JSONNumber toRepresentation( final Double custom ) {
				return new JSONNumber( custom );
			}


			/** convert the JSON representation construct into the custom type */
			public Double toNative( final JSONNumber representation ) {
				return representation.doubleValue();
			}
		}, "double" );

        registerType( Date.class, new ConversionAdaptor<Date,JSONNumber>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public JSONNumber toRepresentation( final Date timestamp ) {
                return new JSONNumber( timestamp.getTime() );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            public Date toNative( final JSONNumber msecFromEpoch ) {
                return new Date( msecFromEpoch.longValue() );
            }
        });
        
        this.<ArrayList<?>,Object[]>registerType( ArrayList.class, new ConversionAdaptor<ArrayList<?>,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final ArrayList<?> list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public ArrayList<?> toNative( final Object[] array ) {
                final ArrayList<Object> list = new ArrayList<>( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        this.<Vector<?>,Object[]>registerType( Vector.class, new ConversionAdaptor<Vector<?>,Object[]>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            public Object[] toRepresentation( final Vector<?> list ) {
                return list.toArray();
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    // list can represent any type
            public Vector<?> toNative( final Object[] array ) {
                final Vector<Object> list = new Vector<>( array.length );
                for ( final Object item : array ) {
                    list.add( item );
                }
                return list;
            }
        });
        
        this.<Hashtable<String,?>,HashMap<String,?>>registerType( Hashtable.class, new ConversionAdaptor<Hashtable<String,?>,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )
            public HashMap<String,?> toRepresentation( final Hashtable<String,?> table ) {
                return new HashMap<>( table );
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )
            public Hashtable<String,?> toNative( final HashMap<String,?> map ) {
                return new Hashtable<>( map );
            }
        });
        
        registerType( StackTraceElement.class, new ConversionAdaptor<StackTraceElement,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" ) 
            public HashMap<String,?> toRepresentation( final StackTraceElement traceElement ) {
                final HashMap<String,Object> traceElementMap = new HashMap<>( 3 );
                traceElementMap.put( "className", traceElement.getClassName() );
                traceElementMap.put( "methodName", traceElement.getMethodName() );
                traceElementMap.put( "fileName", traceElement.getFileName() );
                traceElementMap.put( "lineNumber", traceElement.getLineNumber() );
                return traceElementMap;
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )    
            public StackTraceElement toNative( final HashMap<String,?> traceElementMap ) {
                final String className = (String)traceElementMap.get( "className" );
                final String methodName = (String)traceElementMap.get( "methodName" );
                final String fileName = (String)traceElementMap.get( "fileName" );
                final int lineNumber = (Integer)traceElementMap.get( "lineNumber" );
                return new StackTraceElement( className, methodName, fileName, lineNumber );
            }
        });
        
        registerType( RuntimeException.class, new ConversionAdaptor<RuntimeException,HashMap<String,?>>() {
            /** convert the custom type to a representation in terms of representation JSON constructs */
            @SuppressWarnings( "unchecked" )
            public HashMap<String,?> toRepresentation( final RuntimeException exception ) {
                final String rawMessage = exception.getMessage();
                final HashMap<String,Object> exceptionMap = new HashMap<>( 3 );
                exceptionMap.put( "message", rawMessage != null ? rawMessage : exception.toString() );
                exceptionMap.put( "stackTrace", exception.getStackTrace() );
                return exceptionMap;
            }
            
            
            /** convert the JSON representation construct into the custom type */
            @SuppressWarnings( "unchecked" )
            public RuntimeException toNative( final HashMap<String,?> exceptionMap ) {
                final String message = (String)exceptionMap.get( "message" );
                final StackTraceElement[] stackTrace = (StackTraceElement[])exceptionMap.get( "stackTrace" );
                final RuntimeException exception = new RuntimeException( message );
                exception.setStackTrace( stackTrace );
                return exception;
            }
        });
    }
}



/** Concrete class to hold a generic JSON number. JSON and JavaScript don't distinguish between floats and ints and the various numeric sizes */
class JSONNumber extends Number {
	/** class variable required for serializable classes */
	private static final long serialVersionUID = 1L;


	/** actual number with data */
	final private Number WRAPPED_NUMBER;


	/** Constructor */
	public JSONNumber( final Number wrappedNumber ) {
		WRAPPED_NUMBER = wrappedNumber;
	}


	/** get this number as a byte value */
	public byte byteValue() {
		return WRAPPED_NUMBER.byteValue();
	}


	/** get this number as a double value */
	public double doubleValue() {
		return WRAPPED_NUMBER.doubleValue();
	}


	/** get this number as a float value */
	public float floatValue() {
		return WRAPPED_NUMBER.floatValue();
	}


	/** get this number as a int value */
	public int intValue() {
		return WRAPPED_NUMBER.intValue();
	}


	/** get this number as a long value */
	public long longValue() {
		return WRAPPED_NUMBER.longValue();
	}


	/** get this number as a short value */
	public short shortValue() {
		return WRAPPED_NUMBER.shortValue();
	}


	/** Generate the string representation of the wrapped number */
	public String toString() {
		return WRAPPED_NUMBER.toString();
	}


	/** convert the specified string to a number */
	public static JSONNumber valueOf( final String numstr ) {
		if ( numstr.contains( "." ) ) {
			return new JSONNumber( Double.valueOf( numstr ) );
		}
		else {
			return new JSONNumber( Long.valueOf( numstr ) );
		}
	}
}



