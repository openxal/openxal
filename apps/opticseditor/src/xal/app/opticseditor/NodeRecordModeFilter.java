//
//  NodeRecordModeFilter.java
//  xal
//
//  Created by Tom Pelaia on 10/25/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;


/** filter a node record */
abstract public class NodeRecordModeFilter implements NodeRecordFilter {
	/** accept only records that fail the test */
	final static public int FAIL_MODE = -1;
	
	/** accept any record */
	final static public int ANY_MODE = 0;
	
	/** accept only records that pass the test */
	final static public int PASS_MODE = 1;
	
	/** filter mode */
	private int _mode;
	
	
	/** Primary Constructor */
	protected NodeRecordModeFilter( final int mode ) {
		_mode = mode;
	}
	
	
	/** Constructor defaulting to ANY mode */
	protected NodeRecordModeFilter() {
		this( ANY_MODE );
	}
	
	
	/** get a new instance of the status filter */
	static public NodeRecordModeFilter getStatusFilterInstance() {
		return new NodeRecordStatusFilter();
	}
	
	
	/** get a new instance of the exclusion filter */
	static public NodeRecordModeFilter getExclusionFilterInstance() {
		return new NodeRecordExcludeFilter();
	}
	
	
	/** get a new instance of the modification filter */
	static public NodeRecordModeFilter getModificationFilterInstance() {
		return new NodeRecordModifiedFilter();
	}
	
	
	/** get the mode */
	public int getMode() {
		return _mode;
	}
	
	
	/** set the mode */
	public void setMode( final int mode ) {
		_mode = mode;
	}
	
	
	/** determine wheter to accept the record */
	public boolean accept( final NodeRecord record ) {
		switch ( _mode ) {
			case FAIL_MODE:
				return !test( record );
			case ANY_MODE:
				return true;
			case PASS_MODE:
				return test( record );
			default:
				return true;
		}
	}
	
	
	/** test the record for the default condition */
	abstract public boolean test( final NodeRecord record );
}



/** status filter */
class NodeRecordStatusFilter extends NodeRecordModeFilter {
	/** test the record for good status */
	public boolean test( final NodeRecord record ) {
		return record.getStatus();
	}
}



/** exclusion filter */
class NodeRecordExcludeFilter extends NodeRecordModeFilter {
	/** test the record for good status */
	public boolean test( final NodeRecord record ) {
		return record.isExcluded();
	}
}



/** modification filter */
class NodeRecordModifiedFilter extends NodeRecordModeFilter {
	/** test the record for good status */
	public boolean test( final NodeRecord record ) {
		return record.isModified();
	}
}


