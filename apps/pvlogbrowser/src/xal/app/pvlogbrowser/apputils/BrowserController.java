/*
 * BrowserController.java
 *
 * Created on Thu Mar 25 09:00:11 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.apputils;



import xal.service.pvlogger.ChannelGroup;
import xal.service.pvlogger.ChannelSnapshot;
import xal.service.pvlogger.ChannelWrapper;
import xal.service.pvlogger.MachineSnapshot;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/**
 * BrowserController manages the selection state of the browser window.
 *
 * @author  tap
 */
public class BrowserController implements BrowserModelListener {
	/** browser model */
	protected BrowserModel _model;
	
	/** selected Machine snapshot **/
	protected MachineSnapshot _selectedSnapshot;
	
	/** selected PV signals */
	protected Set _selectedSignals;
	
	/** The message center for dispatching messages */
	protected MessageCenter _messageCenter;
	
	/** Proxy for forwarding messages to registered listeners */
	protected BrowserControllerListener _proxy;
	
	
	/**
	 * Constructor
	 */
	public BrowserController( final BrowserModel model ) {
		_model = model;
		model.addBrowserModelListener(this);
		_selectedSignals = new HashSet();
		
		_messageCenter = new MessageCenter("Browser Controller");
		_proxy = (BrowserControllerListener)_messageCenter.registerSource( this, BrowserControllerListener.class );
	}
	
	
	/**
	 * Add a listener of controller events from this controller
	 * @param listener the listener to add
	 */
	public void addBrowserControllerListener( final BrowserControllerListener listener ) {
		_messageCenter.registerTarget( listener, this, BrowserControllerListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving controller events from this controller
	 * @param listener the listener to remove
	 */
	public void removeBrowserControllerListener( final BrowserControllerListener listener ) {
		_messageCenter.removeTarget( listener, this, BrowserControllerListener.class );
	}
	
	
	/**
	 * Convert the array of channel wrappers to an array of signals.
	 * @param wrappers the array of channel wrappers
	 * @return the corresponding array of signals
	 */
	static protected String[] convertToPVs( final ChannelWrapper[] wrappers ) {
		String[] signals = new String[wrappers.length];
		for ( int index = 0 ; index < wrappers.length ; index++ ) {
			signals[index] = wrappers[index].getPV();
		}
		return signals;
	}
	
	
	/**
	 * Set the selected signals to the array specified
	 * @param signals the array of signals to set as selected
	 */
	private void setSelectedSignals( final String[] signals ) {
		_selectedSignals = new HashSet( signals.length );
		
		for ( int index = 0 ; index < signals.length ; index++ ) {
			_selectedSignals.add( signals[index] );
		}
	}
	
	
	/**
	 * Get the collection of selected signals
	 * @return the collection of selected signals
	 */
	public Collection getSelectedSignals() {
		return new HashSet(_selectedSignals);
	}
	
	
	/**
	 * Select or deselect the collection of signals without affecting the selection status
	 * of other signals.
	 * @param signals the signals to select/deselect
	 * @param select true to select signals and false to deselect signals
	 */
	public void selectSignals( final Collection signals, final boolean select ) {
		Iterator signalIter = signals.iterator();
		Set selectedSignals = new HashSet(_selectedSignals);
		
		if (select) {
			selectedSignals.addAll(signals);
		}
		else {
			selectedSignals.removeAll(signals);
		}
		
		_selectedSignals = selectedSignals;
		_proxy.selectedSignalsChanged( this, selectedSignals );
	}
	
	
	/**
	 * Select or deselect a signal without affecting other signal selection.
	 * @param signal the signals to select/deselect
	 * @param select true to select the signal or false to deselect the signal
	 */
	public void selectSignal( final String signal, final boolean select ) {
		selectSignals( Collections.singleton(signal), select );
	}
	
	
	/**
	 * Determine if a signal is selected
	 * @param signal the signal to test for selection
	 * @return true if the signal is selected and false if not
	 */
	public boolean isSignalSelected( final String signal ) {
		return _selectedSignals.contains(signal);
	}
	
	
	/**
	 * Filter each channel snapshot based on whether its signal is selected
	 * @param snapshots The snapshots to filter
	 * @return the array of filtered snapshots corresponding to selected signals
	 */
	public ChannelSnapshot[] filterSnapshots(final ChannelSnapshot[] snapshots ) {
		List filteredSnapshots = new ArrayList( snapshots.length );
		
		for ( int index = 0 ; index < snapshots.length ; index++ ) {
			final ChannelSnapshot snapshot = snapshots[index];
			if ( isSignalSelected( snapshot.getPV() ) )  filteredSnapshots.add(snapshot);
		}
		ChannelSnapshot[] result = new ChannelSnapshot[filteredSnapshots.size()];
		filteredSnapshots.toArray(result);
		
		return result;
	}
	
	
	/**
	 * Get the main model
	 * @return the main model
	 */
	public BrowserModel getModel() {
		return _model;
	}
	
	
	/**
	 * Set the snapshot which is selected by the user
	 * @param snapshot the machine snapshot to select
	 */
	public void setSelectedSnapshot( final MachineSnapshot snapshot ) {
		if ( snapshot != null ) {
			try {
				_model.populateSnapshot( snapshot );
			}
			catch( Exception exception ) {
				throw new RuntimeException( exception );
			}
		}
		_selectedSnapshot = snapshot;
		_proxy.snapshotSelected( this, snapshot );
	}
	
	
	/**
	 * The model's connection has changed
	 * @param model The model whose connection changed
	 */
	public void connectionChanged( final BrowserModel model ) {}
	
	
	/**
	 * Update the channel wrappers for the newly selected channel group and 
	 * forward this event to the browser controller listeners.
	 * @param model the source of the event
	 * @param newGroup the newly selected channel group
	 */
	public void selectedChannelGroupChanged( final BrowserModel model, final ChannelGroup newGroup ) {
		if ( newGroup != null ) {
			ChannelWrapper[] wrappers = newGroup.getChannelWrappers();
			setSelectedSignals( convertToPVs(wrappers) );
		}
		else {
			setSelectedSignals( new String[0] );
		}
		_proxy.selectedChannelGroupChanged( this, newGroup );
	}
	
	
	/**
	 * Handle the "machine snapshot fetched" event.  Does nothing.
	 * @param model the model providing the event
	 * @param snapshots the new snapshots that have been fetched
	 */
	public void machineSnapshotsFetched( final BrowserModel model, final MachineSnapshot[] snapshots) {}
}

