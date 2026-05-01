//
//  DataTypeAdaptor.java
//  xal
//
//  Created by Tom Pelaia on 7/30/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.app.score;

import xal.ca.*;

import javax.swing.SwingConstants;
import java.util.*;
import java.text.*;


/** Subclasses provide data type specific operations */
abstract class DataTypeAdaptor {
	/** default type code */
	final static public String DEFAULT_TYPE = DoubleTypeAdaptor.TYPE;
	
	/** table of data adaptors keyed by type */
	final static private Map<String,DataTypeAdaptor> ADAPTORS;
	
	// static initializer
	static {
		ADAPTORS = new HashMap<String,DataTypeAdaptor>();
		ADAPTORS.put( DoubleTypeAdaptor.TYPE, new DoubleTypeAdaptor() );
		ADAPTORS.put( StringTypeAdaptor.TYPE, new StringTypeAdaptor() );
	}
	
	
	/** get the data type adaptor for the specified type */
	static public DataTypeAdaptor adaptorForType( final String type ) {
		return ADAPTORS.get( type.toLowerCase() );
	}
	
	
	/** get the type of the adaptor */
	abstract public String getType();
	
	/** parse the specified string reference to get an instance of the data type */
	abstract public Object parse( final String string );
	
	/** get the value as a string */
	abstract public String asString( final Object value );
	
	
	/** 
	 * get the appropriate horizontal alignment to use during display of a value 
	 * @return LEFT as the default alignment
	 */
	public int getHorizontalAlignment() {
		return SwingConstants.LEFT;
	}

	
	/** get a value associated with an unspecified quantity */
	abstract public Object empty();
	
	
	/** get the relative error (fraction) between the two values */
	abstract public double getRelativeError( final Object value, final Object reference );
		
	/** determine whether the value is equivalent to the target within the specified tolerance */
	abstract public boolean isWithinTolerance( final Object value, final Object target, final double tolerance );
	
	/** determine wither the value is valid for channel access */
	public boolean isValidCAValue( final Object value ) {
		return value != null;
	}
	
	/** get the value from the specified channel wrapper */
	abstract public Object getValue( final ChannelWrapper channelWrapper );
	
	/** put the value to the channel */
	abstract public void putValCallback( final Channel channel, final Object value, final PutListener listener ) throws Exception;
}



/** Data type adaptor for Double */
class DoubleTypeAdaptor extends DataTypeAdaptor {
	/** type code for this data type adaptor */
	final static public String TYPE = "double";
	
	/** format for displaying in scientific notation */
	final static private DecimalFormat SCIENTIFIC_FORMAT = new DecimalFormat( "0.000E0" );;
	
	/** format for displaying in float notation */
	final static private DecimalFormat FLOAT_FORMAT = new DecimalFormat( "0.00000" );
	
	
	/** get the type of the adaptor */
	public String getType() {
		return TYPE;
	}
	
	
	/** parse the specified string reference to get an instance of the data type */
	public Double parse( final String string ) {
		try {
			return ( string != null && string.length() > 0 ) ? Double.parseDouble( string ) : Double.NaN;
		}
		catch ( Exception exception) {
			return Double.NaN;
		}
	}
	
	
	/** get the value as a string */
	public String asString( final Object objectValue ) {
		if ( objectValue == null ) {
			return "";
		}
		else if( ((Double)objectValue).isNaN() ) {
			return "NaN";
		}
		else {
			final double value = ((Double)objectValue).doubleValue();
			final NumberFormat fieldFormat = ( Math.abs( value ) > 10000. || Math.abs( value ) < 0.01 ) ? SCIENTIFIC_FORMAT : FLOAT_FORMAT; 
			return fieldFormat.format( value );			
		}		
	}
	
	
	/** 
	 * get the appropriate horizontal alignment to use during display of a value 
	 * @return RIGHT since numbers should be right aligned
	 */
	public int getHorizontalAlignment() {
		return SwingConstants.RIGHT;
	}
	
	
	/** get a value associated with an unspecified quantity */
	public Double empty() {
		return Double.NaN;
	}
	
	
	/** get the relative error (fraction) between the two values */
	private double getRelativeError( final Double value, final Double reference ) {
		if ( value == null && reference == null ) {
			return 0.0;
		}
		else if ( value == null || reference == null ) {
			return Double.POSITIVE_INFINITY;
		}
		else if ( value.isNaN() && reference.isNaN() ) {
			return 0.0;
		}
		else if ( value.isNaN() || reference.isNaN() ) {
			return Double.POSITIVE_INFINITY;
		}
		else {
			final double target = reference.doubleValue();
			final double presentValue = value.doubleValue();
			final double denominator = 2.0 * Math.abs( target * presentValue );
			return target == presentValue ? 0.0 : denominator != 0 ? Math.abs( presentValue - target ) * ( Math.abs( presentValue ) + Math.abs( target ) ) / denominator : Double.POSITIVE_INFINITY;
		}
	}
		
	
	/** get the relative error (fraction) between the two values */
	public double getRelativeError( final Object value, final Object reference ) {
		return getRelativeError( (Double)value, (Double)reference );
	}
	
	
	/** determine whether the value is equivalent to the target within the specified tolerance */
	public boolean isWithinTolerance( final Object value, final Object target, final double tolerance ) {
		return value != null && target != null ? Math.abs( (Double)value - (Double)target ) <= tolerance * Math.abs( (Double)target ) : value == target;
	}
	
	
	/** get the value from the specified channel wrapper */
	public Double getValue( final ChannelWrapper channelWrapper ) {
		return channelWrapper != null ? channelWrapper.doubleValue() : null;
	}
	
	
	/** put the value to the channel */
	public void putValCallback( final Channel channel, final Object value, final PutListener listener ) throws Exception {
		channel.putValCallback( (Double)value, listener );
	}

	
	/** determine wither the value is valid for channel access */
	public boolean isValidCAValue( final Object value ) {
		return value != null && !((Double)value).isNaN();
	}
}



/** Data type adaptor for String */
class StringTypeAdaptor extends DataTypeAdaptor {
	/** type code for this data type adaptor */
	final static public String TYPE = "string";
	
	
	/** get the type of the adaptor */
	public String getType() {
		return TYPE;
	}	
	
	
	/** parse the specified string reference to get an instance of the data type */
	public String parse( final String string ) {
		return string;
	}
	
	
	/** get the value as a string */
	public String asString( final Object value ) {
		return value != null ? value.toString() : "";
	}
	
	
	/** get a value associated with an unspecified quantity */
	public String empty() {
		return "";
	}
	
	
	/** get the relative error (fraction) between the two values */
	public double getRelativeError( final Object value, final Object reference ) {
		if ( value == null && reference == null ) {
			return 0.0;
		}
		else if ( value == null || reference == null ) {
			return Double.POSITIVE_INFINITY;
		}
		else {
			return value.equals( reference ) ? 0.0 : Double.POSITIVE_INFINITY;
		}
	}
	
	
	/** determine whether the value is equivalent to the target within the specified tolerance */
	public boolean isWithinTolerance( final Object value, final Object target, final double tolerance ) {
		return value != null ? ((String)value).equals( (String)target ) : value == target;
	}
	
	
	/** get the value from the specified channel wrapper */
	public String getValue( final ChannelWrapper channelWrapper ) {
		return channelWrapper != null ? channelWrapper.stringValue() : null;
	}	
	
	
	/** put the value to the channel */
	public void putValCallback( final Channel channel, final Object value, final PutListener listener ) throws Exception {
		channel.putValCallback( (String)value, listener );
	}
}
