//
//  ParameterStore.java
//  xal
//
//  Created by Thomas Pelaia on 6/8/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;

import java.util.*;


/** Storage for the parameters. */
public class ParameterStore implements LiveParameterListener, DataListener {
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final ParameterStoreListener _eventProxy;
	
	/** live parameters */
	final protected List<LiveParameter> _liveParameters;
	
	/** core parameters */
	final protected Map<String, CoreParameter> _coreParameterTable;
	final protected List<CoreParameter> _coreParameters;
	
	/** indicates whether the parameters need sorting */
	private boolean _needsSorting;
	
	
	/** Constructor */
	public ParameterStore() {
		_messageCenter = new MessageCenter( "Live Parameter" );
		_eventProxy = _messageCenter.registerSource( this, ParameterStoreListener.class );
		
		_liveParameters = new ArrayList<LiveParameter>();
		_coreParameters = new ArrayList<CoreParameter>();
		_coreParameterTable = new HashMap<String, CoreParameter>();
	}
	
	
	/** Dispose of this object's resources */
	public void dispose() {
		clear();
	}
	
	
	/** Clear the parameters */
	public void clear() {
		for ( LiveParameter parameter : _liveParameters ) {
			parameter.removeLiveParameterListener( this );
		}
		
		_liveParameters.clear();
		_coreParameters.clear();
		_coreParameterTable.clear();
	}
	
	
	/**
	 * Generate a live parameter and add it to the store.
	 * @param nodeAgent the node agent for which the parameter belongs
	 * @param typeAdaptor the type adaptor used to define the parameter's behavior
	 */
	public LiveParameter addLiveParameter( final NodeAgent nodeAgent, final ParameterTypeAdaptor typeAdaptor ) {
		final String coreName = typeAdaptor.getControlChannel( nodeAgent ).channelName();
		
		CoreParameter coreParameter;
		if ( !_coreParameterTable.containsKey( coreName ) ) {
			coreParameter = new CoreParameter( coreName, typeAdaptor );
			_coreParameterTable.put( coreName, coreParameter );
			_coreParameters.add( coreParameter );
			_eventProxy.coreParameterAdded( this, coreParameter );
		}
		else {
			coreParameter = _coreParameterTable.get( coreName );
		}
		
		final LiveParameter liveParameter = new LiveParameter( nodeAgent, coreParameter );		
		_liveParameters.add( liveParameter );
		_needsSorting = true;
		
		liveParameter.addLiveParameterListener( this );
		_eventProxy.liveParameterAdded( this, liveParameter );
		
		return liveParameter;
	}
	
	
	/**
	 * Add the specified listener to receive parameter store event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addParameterStoreListener( final ParameterStoreListener listener ) {
		_messageCenter.registerTarget( listener, this, ParameterStoreListener.class );
	}
	
	
	/**
	 * Remove the specified listener from receiving parameter store event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeParameterStoreListener( final ParameterStoreListener listener ) {
		_messageCenter.removeTarget( listener, this, ParameterStoreListener.class );
	}
	
	
	/**
	 * Get a core parameter by name.
	 * @param name the name of the core parameter to get
	 */
	public CoreParameter getCoreParameter( final String name ) {
		return _coreParameterTable.get( name );
	}
	
	
	/**
	 * Get the live parameters.
	 * @return the live parameters.
	 */
	public List<LiveParameter> getLiveParameters() {
		sortLiveParametersIfNeeded();
		return _liveParameters;
	}
	
	
	/** Sort the live parameters if needed */
	private void sortLiveParametersIfNeeded() {
		if ( _needsSorting ) {
			LiveParameter.sortByPosition( _liveParameters );
			_needsSorting = false;
		}
	}
	
	
	/**
	 * Get the core parameters.
	 * @return the core parameters
	 */
	public List<CoreParameter> getCoreParameters() {
		return _coreParameters;
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "ParameterStore";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
        for (final DataAdaptor coreAdaptor : adaptor.childAdaptors(CoreParameter.DATA_LABEL )) {
            final CoreParameter parameter = getCoreParameter( coreAdaptor.stringValue( "name" ) );
			parameter.update( coreAdaptor );
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		final Iterator<CoreParameter> paramIter = _coreParameters.iterator();
		while ( paramIter.hasNext() ) {
			final CoreParameter coreParameter = paramIter.next();
			final DataAdaptor coreAdaptor = adaptor.createChild( coreParameter.dataLabel() );
			coreParameter.write( coreAdaptor );
		}
	}
	
	
	/**
	 * Handle the event indicating that parameter's control channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void controlConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {}
	
	
	/** 
	 * Handle the event indicating that parameter's readback channel connection has changed.
	 * @param parameter the live parameter whose connection has changed.
	 * @param isConnected true if the channel is now connected and false if it is disconnected
	 */
	public void readbackConnectionChanged( final LiveParameter parameter, final boolean isConnected ) {}
	
	
	/** 
	 * Handle the event indicating that parameter's control value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new control value of the parameter
	 */
	public void controlValueChanged( final LiveParameter parameter, final double value ) {}
	
	
	/**
	 * Handle the event indicating that parameter's readback value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new readback value of the parameter
	 */
	public void readbackValueChanged( final LiveParameter parameter, final double value ) {}
	
	
	/**
	 * Handle the event in which the parameter's variable status has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param isVariable indicates whether the parameter is now variable or not
	 */
	public void variableStatusChanged( final LiveParameter parameter, final boolean isVariable ) {
		_eventProxy.liveParameterModified( this, parameter );
	}
	
	
	/**
	 * Handle the event in which the parameter's custom value has changed.
	 * @param parameter the core parameter whose value has changed.
	 * @param value the new custom value
	 */
	public void customValueChanged( final LiveParameter parameter, final double value ) {
		_eventProxy.liveParameterModified( this, parameter );
	}
	
	
	/**
	 * Handle the event in which the parameter's custom limits have changed.
	 * @param parameter the core parameter whose limits have changed.
	 * @param limits the new custom limits
	 */
	public void customLimitsChanged( final LiveParameter parameter, final double[] limits ) {
		_eventProxy.liveParameterModified( this, parameter );
	}
	
	
	/**
	 * Handle the event in which the parameter's initial variable value has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param value the new initial variable value
	 */
	public void initialValueChanged( final LiveParameter parameter, final double value ) {
		if ( parameter.getActiveSource() == LiveParameter.CUSTOM_SOURCE ) {
			_eventProxy.liveParameterModified( this, parameter );			
		}
	}
	
	
	/**
	 * Handle the event in which the parameter's lower variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new lower variable limit
	 */
	public void lowerLimitChanged( final LiveParameter parameter, final double limit ) {
		if ( parameter.getActiveSource() == LiveParameter.CUSTOM_SOURCE ) {
			_eventProxy.liveParameterModified( this, parameter );			
		}		
	}
	
	
	/**
	 * Handle the event in which the parameter's upper variable limit has changed.
	 * @param parameter the live parameter whose value has changed.
	 * @param limit the new upper variable limit
	 */
	public void upperLimitChanged( final LiveParameter parameter, final double limit ) {
		if ( parameter.getActiveSource() == LiveParameter.CUSTOM_SOURCE ) {
			_eventProxy.liveParameterModified( this, parameter );			
		}		
	}
	
	
	/**
	 * Handle the event in which the parameter's variable source has changed.
	 * @param parameter the live parameter whose variable source has changed.
	 * @param source the indicator of the parameter's variable source
	 */
	public void variableSourceChanged( final LiveParameter parameter, final int source ) {
		_eventProxy.liveParameterModified( this, parameter );
	}
}
