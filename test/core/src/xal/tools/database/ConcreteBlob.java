//
//  ConcreteBlob.java
//  xal
//
//  Created by Thomas Pelaia on 1/3/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.database;

import java.sql.Blob;
import java.io.*;


/**
 * Concrete implementation of an SQL Blob.
 */
public class ConcreteBlob implements Blob {
	protected byte[] _data;
	
	
	/**
	 * Primary Constructor
	 * @param capacity The number of bytes allocated for data storage.
	 */
	public ConcreteBlob( final int capacity ) {
		_data = new byte[ capacity ];
	}
	
	
	/**
	 * Constructor
	 */
	public ConcreteBlob() {
		this( 0 );
	}
	
	
	/**
	 * Get an input stream that can read the BLOB data.
	 */
	public InputStream getBinaryStream() throws java.sql.SQLException {
		return new java.io.ByteArrayInputStream( _data );
	}
	
	public InputStream getBinaryStream(long position, long length) {
		final byte[] data = new byte[(int)length - (int) position];
		System.arraycopy( _data, (int)position, data, 0, (int) length);		
		return new java.io.ByteArrayInputStream( data );
	}
		
	public void free() {
		// clean up after the Blob object finished
	}
	
	/**
	 * Get the specified part of the blob data as an array of bytes.
	 */
	public byte[] getBytes( final long position, final int length ) {
		final byte[] data = new byte[length - (int)position];
		System.arraycopy( _data, (int)position, data, 0, length);
		
		return data;
	}
	
	
	/**
	 * Get the number of bytes in this BLOB.
	 */
	public long length() {
		return _data.length;
	}
	
	
	/**
	 * Get the position of the first occurence of pattern in this BLOB starting at the position specified by start.
	 */
	public long position( final Blob pattern, final long start ) throws java.sql.SQLException {
		return position( pattern.getBytes( 0, (int)pattern.length() ), start );
	}
	
	
	/**
	 * Get the position of the first occurence of pattern in this BLOB starting at the position specified by start.
	 */
	public long position( final byte[] pattern, long start ) {
		final long MAX_INDEX = _data.length - pattern.length + 1;
		
		for ( long index = 0 ; index < MAX_INDEX ; index++, start++ ) {
			if ( isMatch( pattern, 0, (int)start ) )  return start;
		}
			  
		return -1;
	}
	
	
	/**
	 * Determine if the pattern matches the specified range of bytes in this BLOB.
	 * 
	 * @param pattern the pattern of bytes to match against _data bytes
	 * @param offset the index of the byte in pattern against which to match the corresponding byte in _data
	 * @param start the index in of the byte in _data against which to match the corresponding byte in pattern
	 */
	private boolean isMatch( final byte[] pattern, final int offset, final int start ) {
		// if the offset equals pattern length then we have successfully matched every byte
		// if the byte in pattern matches the corresponding byte in _data then check the next byte and so forth
		return offset == pattern.length ? true : ( (pattern[offset] == _data[start]) ? isMatch( pattern, offset+1, start+1 ) : false );
	}
	
	
	/**
	 * Get an output stream for writing to this BLOB.  This implementation simply throws an unsupported operation exception.
	 */
	public OutputStream setBinaryStream( final long position ) {
		throw new UnsupportedOperationException();
	}
	
	
	/**
	 * Set the specified bytes.
	 */
	public int setBytes( long position, byte[] bytes, int offset, int length ) {
		System.arraycopy( bytes, offset, _data, (int)position, length);
		
		return length;
	}
	
	
	/**
	 * Set the specified bytes.
	 */
	public int setBytes( long position, byte[] bytes ) {		
		return setBytes( position, bytes, 0, bytes.length );
	}
	
	
	/**
	 * Truncate this BLOB to be the specified length.
	 */
	public void truncate( long length ) {
		final byte[] data = new byte[(int)length];
		System.arraycopy( _data, 0, data, 0, (int)length );
		_data = data;
	} 
	
}
