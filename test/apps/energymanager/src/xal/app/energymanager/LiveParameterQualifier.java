//
//  LiveParameterQualifier.java
//  xal
//
//  Created by Thomas Pelaia on 6/13/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.messaging.MessageCenter;


/** qualifier of live parameters */
public class LiveParameterQualifier implements Qualifier {
	final static protected Qualifier PASS_ALL_QUALIFIER;
	final static protected Qualifier PASS_NOTHING_QUALIFIER;
	
	/** Message center for dispatching events to registered listeners. */
	private final MessageCenter _messageCenter;
	
	/** Proxy which forwards events to registered listeners. */
	private final LiveParameterQualifierListener _eventProxy;
	
	protected Qualifier _qualifier;
	
	protected boolean _passVariables;
	protected boolean _passFixed;
	protected boolean _passQuadField;
	protected boolean _passBendField;
	protected boolean _passDipoleCorrectorField;
	protected boolean _passRFAmplitude;
	protected boolean _passRFPhase;
	
	
	static {
		PASS_ALL_QUALIFIER = new PassAllQualifier();
		PASS_NOTHING_QUALIFIER = new PassNothingQualifier();
	}
	
	
	/** Constructor */
	public LiveParameterQualifier() {
		_messageCenter = new MessageCenter( "Live Parameter" );
		_eventProxy = _messageCenter.registerSource( this, LiveParameterQualifierListener.class );
		
		_qualifier = PASS_ALL_QUALIFIER;
		
		_passVariables = true;
		_passFixed = true;
		
		_passBendField = true;
        _passDipoleCorrectorField = true;
		_passQuadField = true;
		_passRFAmplitude = true;
		_passRFPhase = true;
		
		generateQualifier();
	}
	
	
	/**
	 * Add the specified listener to receive live parameter event notifications from this object.
	 * @param listener the listener to receive the event notifications.
	 */
	public void addLiveParameterQualifierListener( final LiveParameterQualifierListener listener ) {
		_messageCenter.registerTarget( listener, this, LiveParameterQualifierListener.class );
		listener.qualifierChanged( this );
	}
	
	
	/**
		* Remove the specified listener from receiving live parameter event notifications from this object.
	 * @param listener the listener to remove from receiving event notifications.
	 */
	public void removeLiveParameterQualifierListener( final LiveParameterQualifierListener listener ) {
		_messageCenter.removeTarget( listener, this, LiveParameterQualifierListener.class );
	}
	
	
	/**
	 * Set whether or not to pass fixed parameters.
	 */
	public void setPassFixed( final boolean passIt ) {
		_passFixed = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass fixed parameters.
	 */
	public boolean getPassFixed() {
		return _passFixed;
	}
	
	
	/**
	 * Set whether or not to pass variable parameters.
	 */
	public void setPassVariables( final boolean passIt ) {
		_passVariables = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass variable parameters.
	 */
	public boolean getPassVariables() {
		return _passVariables;
	}
	
	
	/**
	 * Set whether or not to pass quadrupole field.
	 */
	public void setPassQuadField( final boolean passIt ) {
		_passQuadField = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass quadrupole field.
	 */
	public boolean getPassQuadField() {
		return _passQuadField;
	}
	
	
	/**
	 * Set whether or not to pass bend field.
	 */
	public void setPassBendField( final boolean passIt ) {
		_passBendField = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass bend field.
	 */
	public boolean getPassBendField() {
		return _passBendField;
	}
	
	
	/**
	 * Set whether or not to pass dipole corrector field.
	 */
	public void setPassDipoleCorrectorField( final boolean passIt ) {
		_passDipoleCorrectorField = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass dipole corrector field.
	 */
	public boolean getPassDipoleCorrectorField() {
		return _passDipoleCorrectorField;
	}
	
	
	/**
	 * Set whether or not to pass RF amplitude.
	 */
	public void setPassRFAmplitude( final boolean passIt ) {
		_passRFAmplitude = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass RF amplitude.
	 */
	public boolean getPassRFAmplitude() {
		return _passRFAmplitude;
	}
	
	
	/**
	 * Set whether or not to pass RF phase.
	 */
	public void setPassRFPhase( final boolean passIt ) {
		_passRFPhase = passIt;
		generateQualifier();
	}
	
	
	/**
	 * Determine whether or not to pass RF phase.
	 */
	public boolean getPassRFPhase() {
		return _passRFPhase;
	}
	
	
	/** Generate the qualifier based on the settings. */
	protected void generateQualifier() {
		try {
			final CompoundQualifier qualifier = new AndQualifier();
			
			if ( _passFixed && _passVariables ) {
				// do nothing
			}
			else if ( !_passFixed && !_passVariables ) {
				_qualifier = PASS_NOTHING_QUALIFIER;
				return;
			}
			else if ( _passFixed )  qualifier.append( new KeyValueQualifier( LiveParameter.VARIABLE_KEY, LiveParameter.IS_NOT_VARIABLE ) );
			else if ( _passVariables )  qualifier.append( new KeyValueQualifier( LiveParameter.VARIABLE_KEY, LiveParameter.IS_VARIABLE ) );
			
			final OrQualifier nameQualifier = new OrQualifier();
			if ( _passBendField )  nameQualifier.append( new KeyValueQualifier( LiveParameter.TYPE_KEY, BendAgent.FIELD_ADAPTOR.getID() ) );
			if ( _passDipoleCorrectorField )  nameQualifier.append( new KeyValueQualifier( LiveParameter.TYPE_KEY, DipoleCorrectorAgent.FIELD_ADAPTOR.getID() ) );
			if ( _passQuadField )  nameQualifier.append( new KeyValueQualifier( LiveParameter.TYPE_KEY, QuadAgent.FIELD_ADAPTOR.getID() ) );
			if ( _passRFAmplitude )  nameQualifier.append( new KeyValueQualifier( LiveParameter.NAME_KEY, RFCavityAgent.AMPLITUDE_ADAPTOR.getName() ) );
			if ( _passRFPhase )  nameQualifier.append( new KeyValueQualifier( LiveParameter.NAME_KEY, RFCavityAgent.PHASE_ADAPTOR.getName() ) );
			qualifier.append( nameQualifier );
			
			_qualifier = qualifier;			
		}
		finally {
			_eventProxy.qualifierChanged( this );			
		}
	}
	
	
	/** 
	* Determine if the specified object is a match to this qualifier's criteria. 
	* @param object the object to test for matching
	* @return true if the object matches the criteria and false if not.
	*/
	public boolean matches( final Object object ) {
		return _qualifier.matches( object );
	}



	/** Qualifier that passes all objects. */
	static class PassNothingQualifier implements Qualifier {
		public boolean matches( final Object object ) { return false; }
	}

	
	
	/** Qualifier that passes all objects. */
	static class PassAllQualifier implements Qualifier {
		public boolean matches( final Object object ) { return true; }
	}
}
