//
//  KeyValueRecordListener.java
//  xal
//
//  Created by Tom Pelaia on 2/12/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashMap;
import java.util.Map;


/** Listener for key value record modifications */
public interface KeyValueRecordListener<SourceType,RecordType> {
	/**
	 * Event indicating that the specified record has been modified in the specified source.
	 * @param source the source posting the modification event
	 * @param record the record which was modified
	 * @param keyPath the key path to the modified value
	 * @param value the new value
	 */
	public void recordModified( final SourceType source, final RecordType record, final String keyPath, final Object value );
}
