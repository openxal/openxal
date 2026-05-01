//
//  NodeRecord.java
//  xal
//
//  Created by Tom Pelaia on 10/22/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;

import xal.tools.data.*;


/** record of the node's state */
public class NodeRecord implements Comparable<NodeRecord> {
	/** node ID */
	final private String NODE_ID;
	
	/** sequence */
	final private String SEQUENCE_ID;
	
	/** design status true=good, false=bad */
	final private boolean DESIGN_STATUS;
	
	/** design exclusion state */
	final private boolean DESIGN_EXCLUDE;
	
	/** current status true=good, false=bad  */
	private boolean _status;
	
	/** current exclusion state */
	private boolean _exclude;
	
	/** modification comment */
	private String _modificationComment;
	
	
	/** Primary Constructor */
	public NodeRecord( final String nodeID, final String sequenceID, final boolean status, final boolean exclude, final String modificationComment ) {
		NODE_ID = nodeID;
		SEQUENCE_ID = sequenceID;
		
		DESIGN_STATUS = status;
		DESIGN_EXCLUDE = exclude;
		
		_status = status;
		_exclude = exclude;
		_modificationComment = modificationComment;
	}
	
	
	/** Constructor */
	public NodeRecord( final NodeRecord record ) {
		this( record.NODE_ID, record.SEQUENCE_ID, record.DESIGN_STATUS, record.DESIGN_EXCLUDE, null );
		apply( record );
	}
	
	
	/** string representation of this record */
	public String toString() {
		return "node: " + NODE_ID + ", sequence: " + SEQUENCE_ID + ", status: " + _status + ", exclude: " + _exclude;
	}
	
	
	/** apply the status and exclusion from the specified node record */
	final public void apply( final NodeRecord record ) {
		_status = record._status;
		_exclude = record._exclude;
		_modificationComment = record._modificationComment;
	}
	
	
	/** get the node ID */
	public String getNodeID() {
		return NODE_ID;
	}
	
	
	/** get the sequence */
	public String getSequenceID() {
		return SEQUENCE_ID;
	}
	
	
	/** determine if the node has been modified from the original optics */
	public boolean isModified() {
		return _status != DESIGN_STATUS || _exclude != DESIGN_EXCLUDE;
	}
	
	
	/** get the status */
	public boolean getStatus() {
		return _status;
	}
	
	
	/** set the status */
	public void setStatus( final boolean status ) {
		_status = status;
	}
	
	
	/** determine if the node is excluded */
	public boolean isExcluded() {
		return _exclude;
	}
	
	
	/** set the exclusion */
	public void setExclude( final boolean exclude ) {
		_exclude = exclude;
	}
	
	
	/** get the modification comment */
	public String getModificationComment() {
		return _modificationComment;
	}
	
	
	/** set the modification comment */
	public void setModificationComment( final String comment ) {
		_modificationComment = comment;
	}
	
	
	/** compare this record with the specified one */
	public int compareTo( final NodeRecord record ) {
		return NODE_ID.compareTo( record.NODE_ID );
	}
	
	
	/** write this record to the specified data adaptor */
	void writeTo( final DataAdaptor adaptor ) {
		adaptor.setValue( "id", NODE_ID );
		adaptor.setValue( "status", _status );
		adaptor.setValue( "exclude", _exclude );
		if ( _modificationComment != null && _modificationComment.length() > 0 ) {
			adaptor.setValue( "mod-comment", _modificationComment );
		}
	}
}
