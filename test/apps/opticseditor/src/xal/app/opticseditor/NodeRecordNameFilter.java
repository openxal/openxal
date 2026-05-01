//
//  NodeRecordNameFilter.java
//  xal
//
//  Created by Tom Pelaia on 10/31/07.
//  Copyright 2007 Oak Ridge National Lab. All rights reserved.
//

package xal.app.opticseditor;

import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;


/** filter node records by name of node and sequence */
class NodeRecordNameFilter implements NodeRecordFilter {
	/** list of patterns representing words of the text pattern */
	private List<Pattern> _wordPatterns;
	
	/** text filter to apply to the node record ID and sequence ID */
	private String _filterText;
	
	
	/** Primary Constructor */
	public NodeRecordNameFilter( final String filterText ) {
		_wordPatterns = null;
		setFilterText( filterText );
	}
	
	
	/** Constructor */
	public NodeRecordNameFilter() {
		this( null );
	}
	
	
	/** set a new text filter to apply to the node record ID and sequence ID */
	public void setFilterText( final String filterText ) {		
		_filterText = filterText;
		if ( filterText != null && filterText.length() > 0 ) {
			// replace special characters with the appropriate regular expression
			final String textPattern = filterText.replaceAll( "\\\\", "\\\\\\\\" ).replaceAll( "\\(", "\\\\(" ).replaceAll( "\\)", "\\\\)" ).replaceAll( "\\[", "\\\\[" );

			final String[] words = textPattern.split( "\\s+" );
			final List<Pattern> wordPatterns = new ArrayList<Pattern>( words.length );
			for ( final String word : words ) {
				wordPatterns.add( Pattern.compile( ".*" + word + ".*", Pattern.CASE_INSENSITIVE ) );
			}
			_wordPatterns = wordPatterns;
		}
		else {
			_wordPatterns = null;
		}
	}
	
	
	/** accept or reject the node record based on whether its name matches the pattern */
	public boolean accept( final NodeRecord record ) {
		final List<Pattern> wordPatterns = _wordPatterns;
		if ( wordPatterns != null && wordPatterns.size() > 0 ) {
			// check that every word in the pattern text matches either the node ID or the sequence ID
			final String nodeID = record.getNodeID();
			final String sequenceID = record.getSequenceID();
			for ( final Pattern pattern : wordPatterns ) {
				if ( !( pattern.matcher( nodeID ).matches() || pattern.matcher( sequenceID ).matches() ) ) {
					return false;
				}
			}
		}
		return true;
	}
}
