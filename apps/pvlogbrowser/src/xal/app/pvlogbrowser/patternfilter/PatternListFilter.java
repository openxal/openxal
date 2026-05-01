/*
 *  PatternListFilter.java
 *
 *  Created on Wed May 12 11:09:26 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.app.pvlogbrowser.patternfilter;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.regex.*;
import javax.swing.text.*;


/**
 * PatternListFilter uses a pattern poster to filter a list of named items. As the user enters
 * text into a text component, the list is filtered in real time. Items in the base list that
 * match the pattern are placed into the filtered list.
 *
 * @author   tap
 */
public class PatternListFilter<E> {
	/** message center */
	protected MessageCenter _messageCenter;
	
	/** proxy which forwards events to registered listeners */
	protected ListFilterListener _proxy;

	/** handles pattern changes */
	protected PatternChangeHandler _patternHandler;
	
	/** unfiltered list to which the filter is applied */
	protected List<E> _baseList;
	
	/** filtered list */
	protected List<E> _filteredList;
	
	/** poster of pattern change events */
	protected PatternEventPoster _patternPoster;


	/**
	 * Primary constructor
	 * @param list           the list to filter with the pattern. This list is not modified by the filter.
	 * @param patternPoster  the poster used to post pattern change events
	 */
	public PatternListFilter( final List<E> list, final PatternEventPoster patternPoster ) {
		_messageCenter = new MessageCenter( "Pattern List Filter" );
		_proxy = (ListFilterListener)_messageCenter.registerSource( this, ListFilterListener.class );

		_patternPoster = patternPoster;
		_patternHandler = new PatternChangeHandler();
		_patternPoster.addPatternChangeListener( _patternHandler );

		setBaseList( list );
	}


	/**
	 * Constructor which uses the default pattern generator to construct a "contains" pattern. An
	 * item in the list matches if it contains the text entered into the document.
	 * @param list           the list to filter with the pattern. This list is not modified by the filter.
	 * @param document       the document used to generate the patterns used to filter the list
	 * @param caseSensitive  true indicates the filter should be case sensitive and false indicates it should be case insensitive
	 */
	public PatternListFilter( final List<E> list, final Document document, final boolean caseSensitive ) {
		this( list, new PatternEventPoster( document, caseSensitive ) );
	}


	/**
	 * Constructor which uses the default pattern generator to construct a "contains" pattern. An
	 * item in the list matches if it contains the text entered into the document. The pattern
	 * flags are set to 0 and thus the pattern is case sensitive.
	 * @param list      the list to filter with the pattern. This list is not modified by the filter.
	 * @param document  the document used to generate the patterns used to filter the list
	 */
	public PatternListFilter( final List<E> list, final Document document ) {
		this( list, new PatternEventPoster( document ) );
	}


	/**
	 * Constructor which uses the default pattern generator to construct a "contains" pattern. An
	 * item in the list matches if it contains the text entered into the document. The pattern
	 * flags are set to 0.
	 * @param list              the list to filter with the pattern. This list is not modified by the filter.
	 * @param document          the document used to generate the patterns used to filter the list
	 * @param patternGenerator  the pattern generator to use
	 * @param caseSensitive     true indicates the filter should be case sensitive and false indicates it should be case insensitive
	 */
	public PatternListFilter( final List<E> list, final Document document, final PatternGenerator patternGenerator, final boolean caseSensitive ) {
		this( list, new PatternEventPoster( document, patternGenerator, caseSensitive ) );
	}


	/**
	 * Constructor which uses the default pattern generator to construct a "contains" pattern. An
	 * item in the list matches if it contains the text entered into the document. The pattern
	 * flags are set to 0.
	 * @param list              the list to filter with the pattern. This list is not modified by the filter.
	 * @param document          the document used to generate the patterns used to filter the list
	 * @param patternGenerator  the pattern generator to use
	 * @param patternFlags      the Pattern flags
	 */
	public PatternListFilter( final List<E> list, final Document document, final PatternGenerator patternGenerator, final int patternFlags ) {
		this( list, new PatternEventPoster( document, patternGenerator, patternFlags ) );
	}


	/**
	 * Add a listener of list filter events indicating that the list has been filtered and thus
	 * may have changed.
	 * @param listener  The listener to receive the list filter events.
	 */
	public void addListFilterListener( final ListFilterListener<E> listener ) {
		_messageCenter.registerTarget( listener, this, ListFilterListener.class );
		listener.filteredListChanged( this, _filteredList );
	}


	/**
	 * Remove the listener from receiving list filter events.
	 * @param listener  The listener to receive the list filter events.
	 */
	public void removeListFilterListener( final ListFilterListener<E> listener ) {
		_messageCenter.removeTarget( listener, this, ListFilterListener.class );
	}


	/**
	 * Get the base list whose items get filtered.
	 * @return   The base list whose items get filtered
	 */
	public List getBaseList() {
		return _baseList;
	}


	/**
	 * Set the base list to filter.
	 * @param list  The new base list to filter
	 */
	public void setBaseList( final List<E> list ) {
		_baseList = list;
		filterList();
	}


	/**
	 * Get the filtered list
	 * @return   the filtered list based on the present pattern.
	 */
	public List<E> getFilteredList() {
		return _filteredList;
	}


	/**
	 * Set the filtered list to the one specified and broadcast the filter list change event.
	 * @param list  the new filtered list.
	 */
	protected void setFilteredList( final List<E> list ) {
		_filteredList = list;
		_proxy.filteredListChanged( this, _filteredList );
	}


	/** Filter the base list using this instance's pattern poster's pattern.  */
	protected void filterList() {
		filterList( _patternPoster.getPattern(), false );
	}


	/**
	 * Filter the base list based on the pattern and whether the pattern change is narrowing. If
	 * the pattern change is narrowing, the previous filter list can be used instead of the larger base list.
	 * @param pattern    the pattern to use for filtering the list
	 * @param narrowing  true indicates that the latest pattern filter is narrower than the pervious
	 */
	protected void filterList( final Pattern pattern, final boolean narrowing ) {
		filterList( pattern, narrowing ? _filteredList : _baseList );
	}


	/**
	 * Filter the base list using the pattern. The base list is not changed, but rather items in
	 * the base list which match the pattern are placed into the filtered list.
	 * @param pattern   the pattern used to filter items in the base list
	 * @param baseList  the list of items we wish to filter
	 */
	protected void filterList( final Pattern pattern, final List<E> baseList ) {
		final List<E> filteredList = new ArrayList<E>( baseList.size() );

		for ( final E item : baseList ) {
			final String text = item.toString();
			if ( pattern.matcher( text ).matches() ) {
				filteredList.add( item );
			}
		}

		setFilteredList( filteredList );
	}
	
	
	
	/** PatternChangeHandler is an internal class whose instance is used to listen for pattern change events. */
	protected class PatternChangeHandler implements PatternChangeListener {
		/**
		 * Handle a pattern change event by filtering the base list.
		 * @param source     the source of the pattern change event
		 * @param pattern    the new pattern to use
		 * @param narrowing  indicates whether this latest pattern is strictly narrower than the previous one
		 */
		public void patternChanged( PatternEventPoster source, Pattern pattern, boolean narrowing ) {
			filterList( pattern, narrowing );
		}
	}
}

