//
//  KeyedRecord.java
//  xal
//
//  Created by Thomas Pelaia on 5/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Represents objects whose parameters can be retrieved using keys. */
public interface KeyedRecord {
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key );
}
