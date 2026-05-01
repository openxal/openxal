/*
 * DataStore.java
 *
 * Created on July 29, 2002, 10:42 AM
 */

package xal.tools;

import java.lang.reflect.Array;

/**
 * ArrayStore is the base class of a class cluster which manages the storage 
 * of an array of primitive types and String.  It includes factory methods 
 * to instantiate storage of specific types.
 *
 * @author  tap
 */
public abstract class ArrayValue extends Number {
	/** required for Serializable */
	static final private long serialVersionUID = 1L;

	/** the array of data */
    protected Object array;
    
    
    /** Empty constructor */
    public ArrayValue() {
        array = new Double( Double.NaN );
    }
    
    
    /** Primary constructor to override */
    protected ArrayValue( final Object anArray ) {}
    
    
    /** get the length of the array */
    final public int getCount() {
        return Array.getLength( array );
    }
    
    
    /** get the native type of the array */
    final public Class<?> getType() {
        return array.getClass().getComponentType();
    }
	
	
	/** override toString() to return a string value */
	public String toString() {
		return stringValue();
	}
    
    
    /** get the first element value as a byte */
    final public byte byteValue() {
        return byteValueAt( 0 );
    }
    
    abstract public byte byteValueAt(int index);
    abstract public byte[] byteArray();
    
    /** get the first element value as a short */
    final public short shortValue() {
        return shortValueAt(0);
    }
    
    abstract public short shortValueAt(int index);
    abstract public short[] shortArray();
    
    /** get the first element value as an int */
    final public int intValue() {
        return intValueAt(0);
    }    
    
    abstract public int intValueAt(int index);
    abstract public int[] intArray();

    /** get the first element value as a long */
    final public long longValue() {
        return longValueAt(0);
    }    
    
    abstract public long longValueAt(int index);
    abstract public long[] longArray();

    /** get the first element value as a float */
    final public float floatValue() {
        return floatValueAt(0);
    }    
    
    abstract public float floatValueAt(int index);
    abstract public float[] floatArray();
    
    /** get the first element value as a double */
    final public double doubleValue() {
        return doubleValueAt(0);
    }
    
    abstract public double doubleValueAt(int index);
    abstract public double[] doubleArray();
    
    /** get the first element value as a String */
    final public String stringValue() {
        return stringValueAt(0);
    }
    
    abstract public String stringValueAt(int index);
    abstract public String[] stringArray();
    
    
    /** Factory method to instantiate a new ArrayValue store for the byte array type */
    static public ArrayValue byteStore( final byte[] newArray ) {
        return new ByteStore( newArray );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the byte scalar type */
    static public ArrayValue byteStore( final byte scalar ) {
        return byteStore( new byte[] {scalar} );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the short array type */
    static public ArrayValue shortStore( final short[] newArray ) {
        return new ShortStore( newArray );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the short scalar type */
    static public ArrayValue shortStore( final short scalar ) {
        return shortStore( new short[] {scalar} );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the int array type */
    static public ArrayValue intStore( final int[] newArray ) {
        return new IntStore( newArray );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the int scalar type */
    static public ArrayValue intStore( final int scalar ) {
        return intStore( new int[] {scalar} );
    }
	
    
    /** Factory method to instantiate a new ArrayValue store for the long array type */
    static public ArrayValue longStore( final long[] newArray ) {
        return new LongStore( newArray );
    }
	
    
    /** Factory method to instantiate a new ArrayValue store for the long scalar type */
    static public ArrayValue longStore( final long scalar ) {
        return longStore( new long[] {scalar} );
    }
	
    
    /** Factory method to instantiate a new ArrayValue store for the float array type */
    static public ArrayValue floatStore( final float[] newArray ) {
        return new FloatStore( newArray );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the float scalar type */
    static public ArrayValue floatStore( final float scalar ) {
        return floatStore( new float[] {scalar} );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the double array type */
    static public ArrayValue doubleStore( final double[] newArray ) {
        return new DoubleStore( newArray );
    }

    
    /** Factory method to instantiate a new ArrayValue store for the double scalar type */
    static public ArrayValue doubleStore( final double scalar ) {
        return doubleStore( new double[] {scalar} );
    }
    
    
    /**
     * Factory method to instantiate a new ArrayValue store from a Number.
     * @param number The number to represent.
     * @return The ArrayValue representation
     */
    static public ArrayValue numberStore( final Number number ) {
        if ( number instanceof Double ) {
            return doubleStore( number.doubleValue() );
        }
        if ( number instanceof Float ) {
            return floatStore( number.floatValue() );
        }
        if ( number instanceof Integer ) {
            return intStore( number.intValue() );
        }
        if ( number instanceof Short ) {
            return shortStore( number.shortValue() );
        }
        if ( number instanceof Byte ) {
            return byteStore( number.byteValue() );
        }
        
        return null;
    }

    
    /** Factory method to instantiate a new ArrayValue store for the String array type */
    static public ArrayValue stringStore( final String[] newArray ) {
        return new StringStore( newArray );
    }
    
    
    /** Factory method to instantiate a new ArrayValue store for the String scalar type */
    static public ArrayValue stringStore( final String scalar ) {
        return stringStore( new String[] {scalar} );
    }
    
    
    /**
     * Create a new ArrayValue from an Object array.  Inspect the array type 
     * to create the proper kind of storage.
     */
    static public ArrayValue arrayValueFromArray( final Object newArray ) throws IllegalArgumentException {
        final Class<?> componentType = newArray.getClass().getComponentType();
        
        if ( componentType == null ) {
            throw new IllegalArgumentException("Argument must be an array!");
        }
        
        if ( componentType == Byte.TYPE ) {
            return byteStore( (byte[])newArray );
        }
        else if ( componentType == Short.TYPE ) {
            return shortStore( (short[])newArray );
        }
        else if ( componentType == Integer.TYPE ) {
            return intStore( (int[])newArray );
        }
        else if ( componentType == Float.TYPE ) {
            return floatStore( (float[])newArray );
        }
        else if ( componentType == Double.TYPE ) {
            return doubleStore( (double[])newArray );
        }
        else if ( componentType == java.lang.String.class ) {
            return stringStore( (String[])newArray );
        }
        else {
            throw new IllegalArgumentException("Argument type does not match an accepted array type!");            
        }
    }
}



/** Storage for a numeric array type.  There will be a concrete subclass of NumericStore for 
 *  each numeric type.
 */
abstract class NumericStore extends ArrayValue {
	/** required for Serializable */
	static final private long serialVersionUID = 1L;


	/** Constructor */
    public NumericStore( final Object newArray ) {
        super( newArray );
    }


    /** return the value at the index as a byte */
    final public byte byteValueAt( final int index ) {
        return ((Number)Array.get( array, index )).byteValue();
    }


    /** return the array converted to a byte array */
    public byte[] byteArray() {
        int count = getCount();
        byte[] newArray = new byte[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = byteValueAt(index);
        }
        return newArray;
    }

    /** return the value at the index as a short */
    final public short shortValueAt(int index) {
        return ((Number)Array.get(array, index)).shortValue();
    }

    /** return the array converted to a short array */
    public short[] shortArray() {
        int count = getCount();
        short[] newArray = new short[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = shortValueAt( index );
        }
        return newArray;
    }

    /** return the value at the index as an int */
    final public int intValueAt( final int index ) {
        return ((Number)Array.get( array, index )).intValue();
    }

    /** return the array converted to an int array */
    public int[] intArray() {
        int count = getCount();
        int[] newArray = new int[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = intValueAt( index );
        }
        return newArray;
    }
    
    /** return the value at the index as a long */
    final public long longValueAt( final int index ) {
        return ((Number)Array.get( array, index )).longValue();
    }
    
    /** return the array converted to a long array */
    public long[] longArray() {
        int count = getCount();
        long[] newArray = new long[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = longValueAt( index );
        }
        return newArray;
    }

    /** return the value at the index as a float */
    final public float floatValueAt(int index) {
        return ((Number)Array.get(array, index)).floatValue();
    }

    /** return the array converted to a float array */
    public float[] floatArray() {
        int count = getCount();
        float[] newArray = new float[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = floatValueAt( index );
        }
        return newArray;
    }

    /** return the value at the index as a double */
    final public double doubleValueAt( final int index ) {
        return ((Number)Array.get( array, index )).doubleValue();
    }

    /** return the array converted to a double array */
    public double[] doubleArray() {
        int count = getCount();
        double[] newArray = new double[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = doubleValueAt( index );
        }
        return newArray;
    }


    /** return the array converted to a String array */
    final public String[] stringArray() {
        int count = getCount();
        String[] newArray = new String[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = stringValueAt( index );
        }

        return newArray;
    }
}



/** Native byte storage */
final class ByteStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** Constructor */
    public ByteStore( final byte[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new byte[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the byte array for best performance */
    public byte[] byteArray() {
        return (byte[])array;
    }


    /** Return the string equivalent of the byte value at the specified index */
    final public String stringValueAt( final int index ) {
		final byte value = byteValueAt( index );
        return String.valueOf( value );
    }
	
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (byte[])array );
	}
}



/** Native short storage */
final class ShortStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public ShortStore( final short[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new short[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the short array for best performance */
    public short[] shortArray() {
        return (short[])array;
    }


    /** Return the string equivalent of the short value at the specified index */
    final public String stringValueAt( final int index ) {
        final short value = shortValueAt( index );
        return String.valueOf( value );
    }
	
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (short[])array );
	}
}



/** Native int storage */
final class IntStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public IntStore( final int[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new int[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the int array for best performance */
    public int[] intArray() {
        return (int[])array;
    }


    /** Return the string equivalent of the int value at the specified index */
    final public String stringValueAt( final int index ) {
        final int value = intValueAt( index );
        return String.valueOf( value );
    }
	
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (int[])array );
	}
}



/** Native long storage */
final class LongStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public LongStore( final long[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new long[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the long array for best performance */
    public long[] longArray() {
        return (long[])array;
    }


    /** Return the string equivalent of the long value at the specified index */
    final public String stringValueAt( final int index ) {
        final long value = longValueAt( index );
        return String.valueOf( value );
    }
	
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (long[])array );
	}
}



/** Native float storage */
final class FloatStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public FloatStore( final float[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new float[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the float array for best performance */
    public float[] floatArray() {
        return (float[])array;
    }


    /** Return the string equivalent of the float value at the specified index */
    final public String stringValueAt(int index) {
        final float value = floatValueAt( index );
        return String.valueOf( value );
    }

	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (float[])array );
	}
}



/** Native double storage */
final class DoubleStore extends NumericStore {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public DoubleStore( final double[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new double[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }
    
    
    /** Override numeric store by directly returning the double array for best performance */
    public double[] doubleArray() {
        return (double[])array;
    }


    /** Return the string equivalent of the double value at the specified index */
    final public String stringValueAt( final int index ) {
        final double value = doubleValueAt( index );
        return String.valueOf( value );
    }
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (double[])array );
	}
}



/** Native String storage */
final class StringStore extends ArrayValue {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    public StringStore( final String[] newArray ) {
        super( newArray );
        final int count = newArray.length;
        array = new String[count];
        System.arraycopy( newArray, 0, array, 0, count );
    }


    /** convert the value at the index to a byte */
    final public byte byteValueAt( final int index ) {
        final String string = stringValueAt( index );
        return Byte.parseByte( string );
    }


    /** convert the array to a byte array */
    final public byte[] byteArray() {
        final int count = getCount();
        final byte[] newArray = new byte[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = byteValueAt( index );
        }
        return newArray;
    }

	
    /** convert the value at the index to a short */
    final public short shortValueAt( final int index ) {
        final String string = stringValueAt( index );
        return Short.parseShort( string );
    }

	
    /** convert the array to a short array */
    final public short[] shortArray() {
        final int count = getCount();
        final short[] newArray = new short[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = shortValueAt( index );
        }
        return newArray;
    }

	
    /** convert the value at the index to an int */
    final public int intValueAt( final int index ) {
        final String string = stringValueAt( index );
        return Integer.parseInt( string );
    }

	
    /** convert the array to an int array */
    final public int[] intArray() {
        final int count = getCount();
        final int[] newArray = new int[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = intValueAt(index);
        }
        return newArray;
    }

	
    /** convert the value at the index to a long */
    final public long longValueAt( final int index ) {
        final String string = stringValueAt( index);
        return Long.parseLong( string );
    }

	
    /** convert the array to a long array */
    final public long[] longArray() {
        final int count = getCount();
        final long[] newArray = new long[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = longValueAt( index );
        }
        return newArray;
    }

	
    /** convert the value at the index to a float */
    final public float floatValueAt( final int index ) {
        final String string = stringValueAt( index );
		try {
			return Float.parseFloat( string );
		}
		catch( NumberFormatException exception ) {
			return Float.NaN;
		}
    }

	
    /** convert the array to a float array */
    final public float[] floatArray() {
        final int count = getCount();
        final float[] newArray = new float[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = floatValueAt( index );
        }
        return newArray;
    }

	
    /** convert the value at the index to a double */
    final public double doubleValueAt( final int index ) {
        final String string = stringValueAt( index );
		try {
			return Double.parseDouble( string );
		}
		catch( NumberFormatException exception ) {
			return Double.NaN;
		}
    }

	
    /** convert the array to a double array */
    final public double[] doubleArray() {
        final int count = getCount();
        final double[] newArray = new double[count];
        for ( int index = 0 ; index < count ; index++ ) {
            newArray[index] = doubleValueAt( index );
        }
        return newArray;
    }


    /** return the String value at the specified index */
    final public String stringValueAt( final int index ) {
        return (String)Array.get( array, index );
    }
    
    
    /** return the String array */
    final public String[] stringArray() {
        return (String[])array;
    }    
	
	
	/**
	 * Get the string representation of this storage data.
	 * @return a string representation of the data
	 */
	final public String toString() {
		return getCount() == 1 ? stringValueAt( 0 ) : ArrayTool.asString( (Object[])array );
	}
}
