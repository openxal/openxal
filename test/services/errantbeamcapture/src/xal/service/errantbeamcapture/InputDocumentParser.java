//
// InputDocumentParser.java
// xal
//
// Created by Tom Pelaia on 3/26/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.service.errantbeamcapture;

import java.io.*;
import java.util.*;


/** Parse the input document */
public class InputDocumentParser {
	static public void parse( final BufferedReader reader, final Map<String,List<String>> keyedArchive ) throws IOException {
		final TokenStream tokenStream = new TokenStream( reader );
		for ( final String token : tokenStream ) {
			final String key = token;
			if ( !keyedArchive.containsKey( key ) ) {
				keyedArchive.put( key, new ArrayList<String>() );
			}
			final List<String> archiveList = keyedArchive.get( key );
			InputListParser.parse( tokenStream, archiveList );
		}
	}
}



/** parse the token stream for a list */
class InputListParser {
	/** parse the token stream to the end of the passed list until the array end marker is reached */
	static public void parse( final TokenStream tokenStream, final List<String> archiveList ) throws IOException {
		final String arrayOpenToken = tokenStream.nextToken();
		if ( arrayOpenToken != null ) {
			if ( arrayOpenToken.equals( "[" ) ) {	// list must begin with the left bracket
				for ( final String token : tokenStream ) {
					if ( token != null && token.equals( "]" ) ) {	// right bracket indicates end of list
						break;
					}
					else {
						archiveList.add( token );
					}
				}
			}
			else if ( arrayOpenToken.equals( "[]" )  ) {
				return;	// there is nothing to add
			}
			else {
				throw new RuntimeException( "Unexpected token >> " + arrayOpenToken + " << where array left bracket [ was expected." );
			}
		}
	}
}



/** Wrap a reader and provide a stream for getting space delimited tokens */
class TokenStream implements Iterable<String> {
	/** reader from which the tokens are streamed */
	private final BufferedReader SOURCE_READER;
	
	
	/** buffer of tokens that have already been read from the source but not yet popped */
	private final ArrayDeque<String> TOKEN_BUFFER;
	
	
	/** Constructor */
	public TokenStream( final BufferedReader sourceReader ) {
		SOURCE_READER = sourceReader;
		TOKEN_BUFFER = new ArrayDeque<String>();
	}
	
	
	/** Get the next token */
	public String nextToken() throws IOException {
		return hasNextToken() ? TOKEN_BUFFER.removeFirst() : null;
	}
	
	
	/** Get an iterator that will iterate over the tokens */
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			/** determine if there are any tokens left */
			public boolean hasNext() {
				try { 
					return hasNextToken();
				}
				catch ( IOException exception ) {
					throw new RuntimeException( exception );
				}
			}
			
			/** get the next token */
			public String next() {
				try {
					return nextToken();
				}
				catch( IOException exception ) {
					throw new RuntimeException( exception );
				}
			}
			
			/** unsupported operation */
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	
	/** Determine whether there are any tokens left */
	public boolean hasNextToken() throws IOException {
		if ( TOKEN_BUFFER.isEmpty() ) {
			fillBuffer();
		}
		
		return !TOKEN_BUFFER.isEmpty();
	}
	
	
	/** Fill the buffer from the source */
	private void fillBuffer() throws IOException {
		if ( SOURCE_READER.ready() ) {
			final String line = parseNextSourceLine( SOURCE_READER );
			if ( line == null || line.length() == 0 )  fillBuffer();
			if ( line.contains( " " ) ) {
				final String[] tokens = line.split( "\\s+" );
				for ( final String token : tokens ) {
					TOKEN_BUFFER.addLast( token );
				}
			}
			else {
				TOKEN_BUFFER.addLast( line );
			}
		}
	}
	
	
	/** parse the line */
	static private String parseNextSourceLine( final BufferedReader reader ) throws IOException {
		final String line = reader.readLine().trim();			// ignore leading and trailing spaces
		if ( line.length() > 0 && !line.startsWith( "#" ) ) {	// skip blank lines and comments
			// relevant part of line is up to but excluding the first hash which marks the beginning of a comment
			final int commentIndex = line.indexOf( '#' );
			return commentIndex > -1 ? line.substring( 0, commentIndex - 1 ).trim() : line;
		}
		else if ( reader.ready() ){
			return parseNextSourceLine( reader );
		}
		else {
			return null;
		}
	}
}
