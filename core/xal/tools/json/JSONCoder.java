//
//  JSONCoder.java
//  xal
//
//  Created by Tom Pelaia on 6/16/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.json;

import java.util.List;
import java.util.Map;
import java.util.Set;


/** encode and decode objects with JSON */
public class JSONCoder {
	/** encode a string */
	static public String encode( final String value ) {
		return value != null ? "\"" + value.replace( "\\", "\\\\" ).replace( "\"", "\\\"" ) + "\"" : "null";
	}
	
	
	/** encode a boolean */
	static public String encode( final boolean value ) {
		return value ? "true" : "false";
	}
	
	
	/** encode a number */
	static public String encode( final Number value ) {
		return value != null ? value.toString() : "null";
	}
	
	
	/** encode a list */
	static public String encode( final List values ) {
		if ( values != null ) {
			final int count = values.size();
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
				final Object value = values.get( index );
				buffer.append( encode( value ) );
			}
			buffer.append( "]" );
			return buffer.toString();
		}
		else {
			return "null";
		}
	}
	
	
	/** encode an array */
	static public String encode( final Object[] values ) {
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
    @SuppressWarnings( "unchecked" )    // map can be of any combination of types
	static public String encode( final Map map ) {
		if ( map != null ) {
			final StringBuffer buffer = new StringBuffer();
			buffer.append( "{" );
			final Set<Map.Entry> entries = map.entrySet();
			int index = 0;
			for ( final Map.Entry entry : entries ) {
				switch ( index ) {
					case 0:
						break;
					default:
						buffer.append( ", " );
						break;
				}
				final String key = (String)entry.getKey();
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
	static public String encode( final Object value ) {
		if ( value == null ) {
			return "null";
		}
		else if ( value instanceof String ) {
			return encode( (String)value );
		}
		else if ( value instanceof Boolean ) {
			return encode( ((Boolean)value).booleanValue() );
		}
		else if ( value instanceof Number ) {
			return encode( (Number)value );
		}
		else if ( value instanceof List ) {
			return encode( (List)value );
		}
		else if ( value instanceof Map ) {
			return encode( (Map)value );
		}
		else if ( value.getClass().isArray() ) {
			return encode( (Object[])value );
		}
		else {
			throw new RuntimeException( "No coder for encoding objects of type: " + value.getClass().toString() );
		}
	}
}
