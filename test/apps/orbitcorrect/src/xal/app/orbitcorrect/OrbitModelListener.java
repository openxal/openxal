/*
 *  OrbitModelListener.java
 *
 *  Created on Tue Jun 22 16:23:08 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.orbitcorrect;

import xal.smf.*;

import java.util.*;


/**
 * OrbitModelListener
 *
 * @author     tap
 * @since      Jun 22, 2004
 */
public interface OrbitModelListener {
	/**
	 * Notification that the sequence has changed.
	 * @param  model        the model sending the notification
	 * @param  newSequence  the new accelerator sequence
	 */
	public void sequenceChanged( OrbitModel model, AcceleratorSeq newSequence );

	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( OrbitModel model, List<BpmAgent> bpmAgents );
	

	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param  model           the model sending the notification
	 * @param  newOrbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( OrbitModel model, OrbitSource newOrbitSource );


	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param  model        the model sending the notification
	 * @param  orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( OrbitModel model, OrbitSource orbitSource );
}

