/*
 * PatternChangeListener.java
 *
 * Created on Wed May 12 10:48:51 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.patternfilter;

import java.util.regex.*;


/**
 * PatternChangeListener is an interface implemented by listeners which want to be notified
 * of changes in the pattern used for filtering and specified in the PatternEventPoster.
 *
 * @author  tap
 */
public interface PatternChangeListener {
	/**
	 * Event indicating that the pattern used to filter has changed
	 * @param source The poster of this event.
	 * @param pattern The pattern used for filtering
	 * @param narrowing true if the new pattern strictly narrows filtering from the old pattern
	 */
	public void patternChanged(PatternEventPoster source, Pattern pattern, boolean narrowing);
}

