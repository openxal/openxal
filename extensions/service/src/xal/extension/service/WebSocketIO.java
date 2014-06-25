//
// WebSocketIO.java
// Open XAL
//
// Created by Pelaia II, Tom on 6/20/2014
// Copyright 2014 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.service;

import java.net.Socket;
import java.io.*;
import java.nio.charset.Charset;
import java.security.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;


/** Utility for processing messages passed through sockets on top of the WebSocket protocol */
class WebSocketIO {
	/** key with which to encode the web socket header key for completing the handshake */
	static final private String HANDSHAKE_ENCODE_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";


	/** send the handshake (from the client) */
	static void sendHandshakeRequest( final Socket socket ) throws java.net.SocketException, java.io.IOException {
		final String randomKey = String.valueOf( new Random().nextLong() );
		final String encodedRandomKey = toBase64( randomKey );	// base64 encoded random key

		final Writer writer = new OutputStreamWriter( socket.getOutputStream() );
		writer.write( "GET /stuff HTTP/1.1\r\n" );
		writer.write( "Upgrade: websocket\r\n" );
		writer.write( "Host: " + socket.getInetAddress().getHostName() + ":" + socket.getPort() + "\r\n" );
		writer.write( "Origin: file://\r\n" );
		writer.write( "Sec-WebSocket-Key: " +  encodedRandomKey + "\r\n" );
		writer.write( "Sec-WebSocket-Version: 13\r\n" );
		writer.write( "Origin: file://\r\n" );
		writer.write( "\r\n" );
		writer.flush();
	}


	/** process the handshake (on the server) */
	static private boolean sendHandshakeResponse( final Socket socket, final String requestHeader ) throws java.net.SocketException, java.io.IOException {
		final Map<String,String> headerMap = new HashMap<>();
		final BufferedReader reader = new BufferedReader( new StringReader( requestHeader ) );
		while( true ) {
			final String line = reader.readLine();
			if ( line != null ) {
				final String[] pair = line.split( ":" );	// key/value pair
				if ( pair.length == 2 ) {
					headerMap.put( pair[0].trim(), pair[1].trim() );
				}
			}
			else {
				break;
			}
		}

		try {
			final String secWebSocketKey = headerMap.get( "Sec-WebSocket-Key" );
			final String input_plus = secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
			final MessageDigest messageDigest = MessageDigest.getInstance( "SHA-1" );
			messageDigest.update( input_plus.getBytes( Charset.forName( "UTF-8" ) ) );
			final String secWebSocketAccept = DatatypeConverter.printBase64Binary( messageDigest.digest() );

			final Writer writer = new OutputStreamWriter( socket.getOutputStream() );
			writer.write( "HTTP/1.1 101 Switching Protocols\r\n" );
			writer.write( "Upgrade: websocket\r\n" );
			writer.write( "Connection: Upgrade\r\n" );
			writer.write( "Sec-WebSocket-Accept: " + secWebSocketAccept + "\r\n" );
			writer.write( "Access-Control-Allow-Headers: content-type\r\n" );
			writer.write( "\r\n" );
			writer.flush();

			return true;
		}
		catch ( NoSuchAlgorithmException exception ) {
			throw new RuntimeException( "Exception encoding websocket server handshake.", exception );
		}
	}


	/** process the handshake with the socket */
	static boolean processHandshake( final Socket socket ) throws java.net.SocketException, java.io.IOException {
		final int BUFFER_SIZE = socket.getReceiveBufferSize();
		final char[] streamBuffer = new char[BUFFER_SIZE];
		final InputStream readStream = socket.getInputStream();
		final BufferedReader reader = new BufferedReader( new InputStreamReader( readStream ) );
		final StringBuilder inputBuffer = new StringBuilder();

		do {
			final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );

			if ( readCount == -1 ) {     // the session has been closed
				throw new RuntimeException( "The remote socket has closed while reading the remote response..." );
			}
			else if  ( readCount > 0 ) {
				inputBuffer.append( streamBuffer, 0, readCount );
			}
		} while ( reader.ready() || readStream.available() > 0 );

		return sendHandshakeResponse( socket, inputBuffer.toString() );
	}


	/** send the message */
	static void sendMessage( final Socket socket, final String message ) {

	}


	/** Read the message from the socket and return it */
	static String readMessage( final Socket socket ) throws java.net.SocketException, java.io.IOException {
		System.out.println( "Waiting for a websocket message..." );

		final int BUFFER_SIZE = socket.getReceiveBufferSize();
		final byte[] streamBuffer = new byte[BUFFER_SIZE];
		final InputStream readStream = socket.getInputStream();
		final BufferedInputStream reader = new BufferedInputStream( readStream );
		final ByteArrayOutputStream rawByteBuffer = new ByteArrayOutputStream();

		do {
			final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );

			if ( readCount == -1 ) {     // the session has been closed
				throw new RuntimeException( "The remote socket has closed while reading the remote response..." );
			}
			else if  ( readCount > 0 ) {
				rawByteBuffer.write( streamBuffer, 0, readCount );
			}
		} while ( readStream.available() > 0 );

		final byte[] rawBytes = rawByteBuffer.toByteArray();

		int offset = 0;

		final byte head1 = rawBytes[0];
		final byte head2 = rawBytes[1];
		offset += 2;

		final boolean fin = ( head1 & 0b10000000 ) == 0b10000000;
		final byte opcode = (byte)( head1 & 0b00001111 );
		final boolean masked = ( head2 & 0b10000000 ) == 0b10000000;
		final byte lengthCode = (byte)( head2 & 0b01111111 );

		System.out.println( "fin: " + fin + ", opcode: " + opcode + ", masked: " + masked + ", length: " + lengthCode );

		// TODO: might be wise to add some length validation
		switch ( lengthCode ) {
			case 126:
				offset += 2;	// payload length defined by next 2 bytes though we won't extract it
				break;

			case 127:
				offset += 8;	// payload length defined by next 8 bytes though we won't extract it
				break;

			default:
				// payload length is simply the lengthCode itself
				break;
		}

		// TODO: need to check the fin bit to see whether more data is coming

		// TODO: need to check the opcode to see what kind of data has arrived (e.g. continuation, text, data, ping or pong)

		final StringBuilder resultBuilder = new StringBuilder();

		PayloadReader payloadReader = PayloadReader.getInstance();
		if ( masked ) {
			final byte[] mask = new byte[4];
			System.arraycopy( rawBytes, offset, mask, 0, 4 );
			offset += 4;

			payloadReader = new MaskPayloadReader( mask, offset );
		}

		for ( int index = offset ; index < rawBytes.length ; index++ ) {
			int charCode = payloadReader.readCharCode( rawBytes, index );
			final char[] chars = Character.toChars( charCode );
			resultBuilder.append( chars );
		}

		System.out.println( "Result:\n" + resultBuilder.toString() );

		return resultBuilder.toString();
	}


	/** Encode the the specified input string as Base64 */
	static private String toBase64( final String input ) {
		final byte[] rawInputBytes = input.getBytes( Charset.forName( "UTF-8" ) );
		return DatatypeConverter.printBase64Binary( rawInputBytes );
	}
}



/** Reads the payload directly without masking */
class PayloadReader {
	/** direct reader singleton */
	final private static PayloadReader DEFAULT_READER = new PayloadReader();


	/** get the default instance */
	static public PayloadReader getInstance() {
		return DEFAULT_READER;
	}


	/** read the specified character directly */
	public int readCharCode( final byte[] inputBuffer, final int index ) {
		return inputBuffer[index];
	}
}



/** Reads the payload when there is a mask */
class MaskPayloadReader extends PayloadReader {
	/** mask to use */
	final private byte[] MASK;

	/** offset from the payload start */
	final private int PAYLOAD_OFFSET;


	/** Constructor */
	public MaskPayloadReader( final byte[] mask, final int payloadOffset ) {
		MASK = mask;
		PAYLOAD_OFFSET = payloadOffset;
	}


	/** read the specified character and mask it */
	public int readCharCode( final byte[] inputBuffer, final int index ) {
		final int position = index - PAYLOAD_OFFSET;			// relative to payload start
		return MASK[position%4] ^ inputBuffer[index];
	}
}



