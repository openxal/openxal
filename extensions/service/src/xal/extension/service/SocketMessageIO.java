//
// SocketMessageIO.java
// Open XAL
//
// Created by Pelaia II, Tom on 10/8/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.service;

import java.net.Socket;
import java.io.*;


/** Utility for processing messages passed through sockets */
class SocketMessageIO {
	/** terminator for remote messages */
	final static char REMOTE_MESSAGE_TERMINATOR = '\0';


	/** Read the message from the socket and return it */
	static String readMessage( final Socket socket ) throws java.net.SocketException, java.io.IOException, SocketPrematurelyClosedException {
        final int BUFFER_SIZE = socket.getReceiveBufferSize();
        final char[] streamBuffer = new char[BUFFER_SIZE];
		final InputStream readStream = socket.getInputStream();
        final BufferedReader reader = new BufferedReader( new InputStreamReader( readStream ) );
        final StringBuilder inputBuffer = new StringBuilder();
		boolean moreToRead = true;
        do {
            final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );

            if ( readCount == -1 ) {     // the session has been closed
				throw new SocketPrematurelyClosedException( "The remote socket has closed while reading the remote response..." );
            }
            else if  ( readCount > 0 ) {
                inputBuffer.append( streamBuffer, 0, readCount );
				moreToRead = streamBuffer[readCount - 1] != REMOTE_MESSAGE_TERMINATOR;
            }
        } while ( reader.ready() || readStream.available() > 0 || moreToRead );

        return inputBuffer.toString().trim();
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
