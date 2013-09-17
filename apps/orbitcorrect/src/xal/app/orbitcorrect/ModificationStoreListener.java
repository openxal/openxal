//
//  ModificationStoreListener.java
//  xal
//
//  Created by Thomas Pelaia on 11/17/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.orbitcorrect;

import java.util.Map;


/** Listener of modifications */
public interface ModificationStoreListener {
	/**
	 * Event indicating that a modification has occured.
	 * @param store the modification store
	 * @param source the source of the modification
	 * @param modification optional modification information
	 */
	public void modificationMade( final ModificationStore store, final Object source, final Map<?, ?> modification );
}
