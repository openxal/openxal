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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.*;
import java.util.*;
import javax.xml.bind.DatatypeConverter;


/** Utility for processing messages passed through sockets on top of the WebSocket protocol */
class WebSocketIO {
	/** key with which to encode the web socket header key for completing the handshake */
	static final private String HANDSHAKE_ENCODE_KEY = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";



	/** Send the handshake (from the client) generating a random security value and process the response. Returns true upon success. */
	static boolean performHandshake( final Socket socket ) throws java.net.SocketException, java.io.IOException, SocketPrematurelyClosedException {
		sendHandshakeRequest( socket );
		return processResponseHandshake( socket );
	}


	/** Send the handshake (from the client) generating a random security value. Use this method when you don't need to valide the header response. */
	static void sendHandshakeRequest( final Socket socket ) throws java.net.SocketException, java.io.IOException {
		sendHandshakeRequest( socket, new Random().nextLong() );
	}


	/** Initiate the handshake (from the client) passing a random value for the security key. Use this method when you want to validate the header response. */
	static void sendHandshakeRequest( final Socket socket, final long randomSecurityValue ) throws java.net.SocketException, java.io.IOException {
		final String randomKey = String.valueOf( randomSecurityValue );
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
	static boolean processRequestHandshake( final Socket socket ) throws java.net.SocketException, java.io.IOException {
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


	/** process the handshake response for the socket without any validation */
	static boolean processResponseHandshake( final Socket socket ) throws java.net.SocketException, java.io.IOException, WebSocketIO.SocketPrematurelyClosedException {
		final int BUFFER_SIZE = socket.getReceiveBufferSize();
		final char[] streamBuffer = new char[BUFFER_SIZE];
		final InputStream readStream = socket.getInputStream();
		final BufferedReader reader = new BufferedReader( new InputStreamReader( readStream ) );
		final StringBuilder inputBuffer = new StringBuilder();

		// empty out the buffer and store the header info
		do {
			final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );

			if ( readCount == -1 ) {     // the session has been closed
				throw new SocketPrematurelyClosedException( "The remote socket has closed while reading the remote response..." );
			}
			else if  ( readCount > 0 ) {
				inputBuffer.append( streamBuffer, 0, readCount );
			}
		} while ( reader.ready() || readStream.available() > 0 );

		// TODO: might want to validate the response handshake
		return true;
	}


	/** send the message */
	static void sendMessage( final Socket socket, final String message ) throws java.net.SocketException, java.io.IOException {
		//System.out.println( "Sending message of length: " + message.length() );

		final OutputStream output = socket.getOutputStream();

		final byte opcode = 1;		// response is text
		final int byte1 = opcode | 0b10000000;
		output.write( byte1 );

		final int messageLength = message.length();

		if ( messageLength < 126 ) {
			output.write( messageLength );
		}
		else if ( messageLength < 65536 ) {
			output.write( 126 );

			// write the length as two bytes
			try {
				short shortLen = (short)messageLength;
				final byte[] lenBytes = new byte[2];
				final ByteBuffer lenByteBuffer = ByteBuffer.wrap( lenBytes );
				lenByteBuffer.putShort( 0, shortLen );
				output.write( lenBytes, 0, 2 );
			}
			catch( RuntimeException exception ) {
				System.err.println( "Exception writing short message length: " + exception );
				exception.printStackTrace();
				throw exception;
			}
		}
		else {
			output.write( 127 );

			// write the length as 8 bytes
			try {
				long longLen = (long)messageLength;
				final byte[] lenBytes = new byte[8];
				final ByteBuffer lenByteBuffer = ByteBuffer.wrap( lenBytes );
				lenByteBuffer.putLong( 0, longLen );
				output.write( lenBytes, 0, 8 );
			}
			catch( RuntimeException exception ) {
				System.err.println( "Exception writing long message length: " + exception );
				exception.printStackTrace();
				throw exception;
			}
		}

		// write the raw message
		final byte[] messageBytes = message.getBytes( Charset.forName( "UTF-8" ) );
		output.write( messageBytes, 0, messageBytes.length );
		output.flush();
	}


	/** Read the message from the socket and return it */
	static String readMessage( final Socket socket ) throws java.net.SocketException, java.io.IOException, WebSocketIO.SocketPrematurelyClosedException {
		//System.out.println( "Reading message..." );

		final int BUFFER_SIZE = socket.getReceiveBufferSize();
		final InputStream readStream = socket.getInputStream();
		final StreamByteReader byteReader = new StreamByteReader( readStream, BUFFER_SIZE );

		try {
			final byte head1 = byteReader.nextByte();
			final byte head2 = byteReader.nextByte();

			final boolean fin = ( head1 & 0b10000000 ) == 0b10000000;
			final byte opcode = (byte)( head1 & 0b00001111 );
			final boolean masked = ( head2 & 0b10000000 ) == 0b10000000;
			final byte lengthCode = (byte)( head2 & 0b01111111 );

			//System.out.println( "fin: " + fin + ", opcode: " + opcode + ", masked: " + masked + ", length code: " + lengthCode );

			int dataLength = 0;
			switch ( lengthCode ) {
				case 126:
					// payload length defined by next 2 bytes
					try {
						final byte[] lenBytes = byteReader.nextBytes( 2 );
						final ByteBuffer lenByteBuffer = ByteBuffer.wrap( lenBytes );
						final short shortLen = lenByteBuffer.getShort();

						// since Java doesn't have unsigned short, we must take care to interpret negative numbers properly
						dataLength = shortLen >= 0 ? shortLen : 65536 + shortLen;
					}
					catch( RuntimeException exception ) {
						System.err.println( "Exception getting short message length: " + exception );
						exception.printStackTrace();
						throw exception;
					}
					break;

				case 127:
					// payload length defined by next 8 bytes
					// TODO: Need to handle true 8 byte lengths. Java only accepts 4 byte lengths (i.e. int) for arrays, so the following code really only supports processing 4 byte lengths even though it reads the 8 byte length.
					try {
						final byte[] lenBytes = byteReader.nextBytes( 8 );
						final ByteBuffer lenByteBuffer = ByteBuffer.wrap( lenBytes );
						dataLength = (int)lenByteBuffer.getLong();	// cast the long to int since arrays only allow 32 bit lengths
					}
					catch( RuntimeException exception ) {
						System.err.println( "Exception getting long message length: " + exception );
						exception.printStackTrace();
						throw exception;
					}
					break;

				default:
					// payload length is simply the lengthCode itself
					dataLength = lengthCode;
					break;
			}

			// TODO: need to check the fin bit to see whether more data is coming

			// TODO: need to check the opcode to see what kind of data has arrived (e.g. continuation, text, data, ping or pong)


			MaskPayloadReader maskPayloadReader = null;
			if ( masked ) {
				final byte[] mask = byteReader.nextBytes( 4 );
				maskPayloadReader = new MaskPayloadReader( mask );
			}

			try {
				final byte[] rawDataBytes = byteReader.nextBytes( dataLength );
				byte[] dataBytes = null;

				if ( masked ) {
					dataBytes = new byte[dataLength];
					for ( int index = 0 ; index < dataLength ; index++ ) {
						dataBytes[index] = maskPayloadReader.readCharCode( rawDataBytes, index );
					}
				}
				else {
					dataBytes = rawDataBytes;
				}

				final String result = new String( dataBytes, 0, dataLength, "UTF-8" );
				return result;
			}
			catch( Exception exception ) {
				System.err.println( "Exception reading characters: " + exception );
				exception.printStackTrace();
				return "";
			}
		}
		catch( StreamByteReader.StreamPrematurelyClosedException exception ) {
			throw new SocketPrematurelyClosedException( "The remote socket has closed while reading the message..." );
		}
	}


	/** Encode the the specified input string as Base64 */
	static private String toBase64( final String input ) {
		final byte[] rawInputBytes = input.getBytes( Charset.forName( "UTF-8" ) );
		return DatatypeConverter.printBase64Binary( rawInputBytes );
	}



	/** Exception indicating that the socket closed prematurely */
	static public class SocketPrematurelyClosedException extends Exception {
		/** required serial version ID */
		static final long serialVersionUID = 0L;


		/** Constructor */
		public SocketPrematurelyClosedException( final String message ) {
			super( message );
		}
	}
}



/** Reads the payload when there is a mask */
class MaskPayloadReader {
	/** mask to use */
	final private byte[] MASK;


	/** Constructor */
	public MaskPayloadReader( final byte[] mask ) {
		MASK = mask;
	}


	/** read the specified character and mask it */
	public byte readCharCode( final byte[] inputBuffer, final int index ) {
		return (byte)( MASK[index%4] ^ inputBuffer[index] );
	}
}



/** read bytes from a stream as requested */
class StreamByteReader {
	/** stream of data from which to read */
	final private InputStream SOURCE_STREAM;

	/** buffer size for reading from the stream */
	final private int BUFFER_SIZE;

	/** current position */
	private int _position;

	/** stack of bytes */
	private byte[] _byteStack;


	/** Constructor */
	public StreamByteReader( final InputStream inputStream, final int bufferSize ) {
		SOURCE_STREAM = inputStream;
		BUFFER_SIZE = bufferSize;

		_position = 0;
		_byteStack = new byte[0];
	}


	/** read the next byte waiting for data from the stream if necessary */
	public byte nextByte() throws java.io.IOException, StreamPrematurelyClosedException {
		final int position = _position;
		if ( position >= _byteStack.length ) {
			popNextBytes();
		}

		final byte nextByte = _byteStack[_position];
		_position += 1;

		return nextByte;
	}


	private void popNextBytes() throws java.io.IOException, StreamPrematurelyClosedException {
		final byte[] streamBuffer = new byte[BUFFER_SIZE];
		final InputStream readStream = SOURCE_STREAM;
		final BufferedInputStream reader = new BufferedInputStream( readStream );
		final ByteArrayOutputStream rawByteBuffer = new ByteArrayOutputStream();

		do {
			final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );

			if ( readCount == -1 ) {     // the session has been closed
				throw new StreamPrematurelyClosedException( "The stream has closed while reading the remote response..." );
			}
			else if  ( readCount > 0 ) {
				rawByteBuffer.write( streamBuffer, 0, readCount );
			}

			if ( readCount < BUFFER_SIZE ) {
				break;
			}
		} while ( true );

		_byteStack = rawByteBuffer.toByteArray();
		_position = 0;
	}


	/** read and return the next specified count of bytes */
	public byte[] nextBytes( final int count ) throws java.io.IOException, StreamPrematurelyClosedException {
		final byte[] result = new byte[count];
		nextBytes( result );
		return result;
	}


	/** read the next bytes into the specified destination */
	public void nextBytes( final byte[] destination ) throws java.io.IOException, StreamPrematurelyClosedException {
		nextBytes( destination, 0, destination.length );
	}


	/** read the next bytes into the specified destination */
	public void nextBytes( final byte[] destination, final int offset, final int count ) throws java.io.IOException, StreamPrematurelyClosedException {
		int position = offset;
		for ( int index = 0 ; index < count ; index++ ) {
			destination[position++] = nextByte();
		}
	}


	/** Exception indicating that the socket closed prematurely */
	static public class StreamPrematurelyClosedException extends Exception {
		/** required serial version ID */
		static final long serialVersionUID = 0L;

		
		/** Constructor */
		public StreamPrematurelyClosedException( final String message ) {
			super( message );
		}
	}
}



