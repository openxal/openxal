/*
 * Created on Oct 27, 2003
 */
package xal.sim.sync;


import java.util.Map;

import xal.model.IElement;

/**
 * Specifies abstract interface for element synchronizers, used by the
 * SynchronizationManager to synchronize lattice elements to a variety of data
 * sources.
 * 
 * @author Craig McChesney
 */
public interface Synchronizer {
	
	void resync(IElement anElem, Map valueMap) 
			throws SynchronizationException;
			
	void checkSynchronization(IElement anElem, Map valueMap)
			throws SynchronizationException;

}
