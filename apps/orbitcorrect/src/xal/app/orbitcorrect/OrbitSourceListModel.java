/*
 * OrbitSourceListModel.java
 *
 * Created on Mon Oct 04 14:54:50 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.extension.application.*;
import xal.smf.AcceleratorSeq;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.util.*;


/**
 * OrbitSourceListModel
 *
 * @author  tap
 * @since Oct 04, 2004
 */
class OrbitSourceListModel extends AbstractListModel<Object> implements ComboBoxModel<Object>, OrbitModelListener {
	
    private static final long serialVersionUID = 1L;
    
    /** orbit model */
	protected OrbitModel _orbitModel;

	/** the orbit sources in the model */
	protected List<Object> _orbitSources;

	/** lock to synchronize orbit source access */
	protected Object _lock;
	
	/** selected orbit source */
	protected Object _selectedOrbitSource;
	
	/** indicates whether this model allows no selection */
	final protected boolean _allowsNoSelection;


	/**
	 * Primary Constructor
	 * @param orbitModel  the orbit model
	 */
	public OrbitSourceListModel( final OrbitModel orbitModel, final boolean allowsNoSelection ) {
		_lock = new Object();
		_allowsNoSelection = allowsNoSelection;

		setOrbitModel( orbitModel );
	}
	
	
	/**
	 * Constructor
	 * @param orbitModel  the orbit model
	 */
	public OrbitSourceListModel( final OrbitModel orbitModel ) {
		this( orbitModel, false );
	}
	

	/**
	 * Set the orbit model.
	 * @param orbitModel  The new orbitModel value
	 */
	public void setOrbitModel( final OrbitModel orbitModel ) {
		if ( _orbitModel != null ) {
			_orbitModel.removeOrbitModelListener( this );
		}

		_orbitModel = orbitModel;

		if ( _orbitModel != null ) {
			_orbitModel.addOrbitModelListener( this );
		}

		updateOrbitSources();
	}


	/** Update the orbit sources from the orbit model. */
	protected void updateOrbitSources() {
		synchronized ( _lock ) {
			_orbitSources = new ArrayList<Object>();
			if ( _allowsNoSelection ) {
				_orbitSources.add( "Zero" );
			}
			if ( _orbitModel != null ) {
				_orbitSources.addAll( _orbitModel.getOrbitSources() );
			}
			if ( getSelectedItem() == null && _orbitSources.size() > 0 ) {
				setSelectedItem( _orbitSources.get( 0 ) );
			}
		}
		fireContentsChanged( this, 0, getSize() );
	}


	/**
	 * Get the list's element at the specified index.
	 * @param index  index of the parameter to get
	 * @return       The elementAt value
	 */
	public Object getElementAt( final int index ) {
		synchronized ( _lock ) {
			return _orbitSources.get( index );
		}
	}


	/**
	 * Get the number of elements in the list.
	 * @return   The size value
	 */
	public int getSize() {
		synchronized ( _lock ) {
			return _orbitSources.size();
		}
	}
	
	
	/**
	 * Get the selected orbit source.
	 * @return the selected orbit source
	 */
	public OrbitSource getSelectedOrbitSource() {
		return ( _selectedOrbitSource != null && _selectedOrbitSource instanceof OrbitSource ) ? (OrbitSource)_selectedOrbitSource : null;
	}
	
	
	/**
	 * Get the selected orbit source.
	 */
	public Object getSelectedItem() {
		return _selectedOrbitSource;
	}
	
	
	/**
	 * Set the selected orbit source.
	 */
	public void setSelectedItem( final Object selectedItem ) {
		_selectedOrbitSource = selectedItem;
	}


	/**
	 * Notification that the sequence has changed.
	 * @param model        the model sending the notification
	 * @param newSequence  the new accelerator sequence
	 */
	public void sequenceChanged( final OrbitModel model, final AcceleratorSeq newSequence ) { }
	
	/**
	 * Notification that the enabled BPMs have changed.
	 * @param  model      model sending this notification
	 * @param  bpmAgents  new enabled bpms
	 */
	public void enabledBPMsChanged( final OrbitModel model, final List<BpmAgent> bpmAgents ) {}
	

	/**
	 * Notification that the orbit model has added a new orbit source.
	 * @param model           the model sending the notification
	 * @param newOrbitSource  the newly added orbit source
	 */
	public void orbitSourceAdded( final OrbitModel model, final OrbitSource newOrbitSource ) {
		updateOrbitSources();
	}


	/**
	 * Notification that the orbit model has removed an orbit source.
	 * @param model        the model sending the notification
	 * @param orbitSource  the orbit source that was removed
	 */
	public void orbitSourceRemoved( final OrbitModel model, final OrbitSource orbitSource ) {
		updateOrbitSources();
	}
}


