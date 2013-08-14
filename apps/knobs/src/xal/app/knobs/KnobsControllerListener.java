//
//  KnobsControllerListener.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;
import java.util.*;

/** interface of knobs controller events */
public interface KnobsControllerListener {
	/**
	 * Handle the event indicating that the controller has a new selected group.
	 * @param source The controller
	 * @param group The new selected group
	 */
	public void selectedGroupChanged( KnobsController source, KnobGroup group );
	
	
	/**
	 * Handle the event indicating that the controller has a new list of selected knobs.
	 * @param source The controller
	 * @param knobs the new list of selected knobs
	 */
	public void selectedKnobsChanged( KnobsController source, List<Knob> knobs );
	
}
