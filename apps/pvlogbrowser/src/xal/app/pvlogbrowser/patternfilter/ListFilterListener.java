/*
 * ListFilterListener.java
 *
 * Created on Wed May 12 11:57:16 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.patternfilter;

import java.util.List;


/**
 * ListFilterListener is the interface for objects which wish to receive notification that the
 * a filter has been applied to a list.
 *
 * @author  tap
 */
public interface ListFilterListener<E> {
	/**
	 * Event indicating that a list has been filtered and a new filtered list is available.
	 * @param source the filter used to filter the list and post the event
	 * @param filteredList the new filtered list
	 */
	public void filteredListChanged( final PatternListFilter<E> source, List<E> filteredList);
}

