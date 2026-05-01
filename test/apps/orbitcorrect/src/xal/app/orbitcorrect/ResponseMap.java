/*
 * ResponseMap.java
 *
 * Created on Fri Oct 15 13:38:57 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.tools.data.*;
import xal.smf.*;
import xal.smf.impl.*;

import java.util.*;


/**
 * ResponseMap
 * @author  tap
 * @since Oct 15, 2004
 */
final public class ResponseMap {
	/** data adaptor tag for the corrector response map */
	static protected final String CORRECTOR_RESPONSE_TAG = "CorrectorResponseMap";
	
	/** data adaptor tag for the BPM response */
	static protected final String BPM_RESPONSE_TAG = "BpmResponse";
	
	/** data adaptor tag */
	static public final String DATA_LABEL = "ResponseMap";
	
	/** data adaptor axis key */
	static public final String AXIS_KEY = "axis";
	
	/** identifies the X axis */
	static public final int X_AXIS = 0;
	
	/** identifies the Y axis */
	static public final int Y_AXIS = 1;
	
	/** BPM response map keyed by corrector ID */
	protected Map<String, Map<String, Double>> _responseMap;
	
	
	/**
	 * Constructor
	 */
	public ResponseMap() {
		_responseMap = new HashMap<String, Map<String, Double>>();
	}
	
	
	/**
	 * Update the response map with the data from the adaptor.
	 */
	public void update( final int axis, final DataAdaptor adaptor ) {
		generateDataListener( axis ).update( adaptor );
	}
	
	
	/**
	 * Generate a data adaptor for the response map.
	 */
	public DataListener generateDataListener( final int axis ) {
		return new DataListener() {
			/** 
			* Provides the name used to identify the class in an external data source.
			*
			* @return a tag that identifies the receiver's type
			*/
			public String dataLabel() {
				return DATA_LABEL;
			}
			
			
			/**
			 * Update the data based on the information provided by the data provider.
			 *
			 * @param adaptor The adaptor from which to update the data
			 */
			public void update( final DataAdaptor adaptor ) {
                for (final DataAdaptor correctorResponseAdaptor : adaptor.childAdaptors(CORRECTOR_RESPONSE_TAG)){
                    final String correctorID = correctorResponseAdaptor.stringValue( "corrector" );
                    
                     for (final DataAdaptor bpmResponseAdaptor : correctorResponseAdaptor.childAdaptors(BPM_RESPONSE_TAG)){
						final String bpmID = bpmResponseAdaptor.stringValue( "bpm" );
						final double response = bpmResponseAdaptor.doubleValue( "response" );
						setResponse( correctorID, bpmID, response );
					}
				}
			}
			
			
			/**
			 * Write data to the data adaptor for storage.
			 *
			 * @param adaptor The adaptor to which the receiver's data is written
			 */
			public void write( final DataAdaptor adaptor ) {
				adaptor.setValue( "version", "1.0.0" );
				adaptor.setValue( "axis", axis );
				final Iterator<String> correctorIter = _responseMap.keySet().iterator();
				while ( correctorIter.hasNext() ) {
					final String correctorID = correctorIter.next();
					final DataAdaptor correctorResponseAdaptor = adaptor.createChild( CORRECTOR_RESPONSE_TAG );
					correctorResponseAdaptor.setValue( "corrector", correctorID );
					
					final Map<String,Double> responseMap = getResponseMap( correctorID );
					final Iterator<String> bpmIter = responseMap.keySet().iterator();
					while ( bpmIter.hasNext() ) {
						final String bpmID = bpmIter.next();
						final double response = getResponse( correctorID, bpmID );
						final DataAdaptor bpmResponseAdaptor = correctorResponseAdaptor.createChild( BPM_RESPONSE_TAG );
						bpmResponseAdaptor.setValue( "bpm", bpmID );
						bpmResponseAdaptor.setValue( "response", response );
					}
				}
			}
		};
	}
	
	
	/** clear the map */
	public void clear() {
		_responseMap.clear();
	}
	
	
	/** set the corrector-detector response */
	public void setResponse( final String correctorSupplyID, final String bpmID, double response ) {
		getResponseMap( correctorSupplyID ).put( bpmID, response );
	}
	
	
	/** set the corrector supply-detector response */
	public void setResponse( final CorrectorSupply correctorSupply, final BpmAgent bpmAgent, double response ) {
		setResponse( correctorSupply.getID(), bpmAgent.getID(), response );
	}
	
	
	/** remove the corrector supply response */
	public void removeResponses( final String correctorSupplyID ) {
		getResponseMap( correctorSupplyID ).clear();
	}
	
	
	/** remove the corrector supply response */
	public void removeResponses( final CorrectorSupply correctorSupply ) {
		removeResponses( correctorSupply.getID() );
	}
	
	
	/** get the corrector-detector response */
	public double getResponse( final String correctorSupplyID, final String bpmID ) {
		try {
			return getResponseMap( correctorSupplyID ).get( bpmID ).doubleValue();
		}
		catch( Exception exception ) {
			return Double.NaN;
		}
	}
	
	
	/** get the corrector supply-detector response */
	public double getResponse( final CorrectorSupply correctorSupply, final BpmAgent bpmAgent ) {
		return getResponse( correctorSupply.getID(), bpmAgent.getID() );
	}
	
	
	/** get the corrector map keyed by BPM ID */
	protected Map<String, Double> getResponseMap( final String correctorSupplyID ) {
		Map<String, Double> map = _responseMap.get( correctorSupplyID );
		
		if ( map == null ) {
			map = new HashMap<String, Double>();
			_responseMap.put( correctorSupplyID, map );
		}
		
		return map;
	}
	
	
	/** get the corrector map keyed by BPM ID */
	protected Map<String, Double> getResponseMap( final CorrectorSupply supply ) {
		return getResponseMap( supply.getID() );
	}
	
	
	/** Determine if we have an entry for the corrector supply/bpm pair */
	public boolean hasResponse( final String correctorSupplyID, final String bpmID ) {
		return getResponseMap( correctorSupplyID ).containsKey( bpmID );
	}
	
	
	/** Determine if we have an entry for the corrector supply/bpm pair */
	public boolean hasResponse( final CorrectorSupply supply, final BpmAgent bpmAgent ) {
		return hasResponse( supply.getID(), bpmAgent.getID() );
	}
	
	
	/** Determine if we have an entry for the corrector supply and all of the bpms */
	public boolean hasResponse( final CorrectorSupply supply, final List<BpmAgent> bpmAgents ) {
		for ( BpmAgent bpmAgent : bpmAgents ) {
			if ( !hasResponse( supply, bpmAgent ) )  return false;
		}
		
		return true;
	}
	
	
	/** Determine if we have an entry for each corrector supply/detector pair */
	public boolean hasResponse( final List<CorrectorSupply> supplies, final List<BpmAgent> bpmAgents ) {
		// first determine if each corrector supply has a corresponding resonse map
		for ( CorrectorSupply supply : supplies ) {
			if ( !_responseMap.containsKey( supply.getID() ) )  return false;
		}
		
		// check that every BPM is accounted for each corrector supply
		for ( CorrectorSupply supply : supplies ) {
			if ( !hasResponse( supply, bpmAgents ) )  return false;
		}
		
		return true;
	}
	
	
	/** Determine if the corrector supply yields a non-zero response for at least one detector */
	public boolean hasNonZeroEffect( final CorrectorSupply supply, final List<BpmAgent> bpmAgents ) {
		// first determine if the corrector has a corresponding resonse map
		if ( !_responseMap.containsKey( supply.getID() ) )  return false;
		
		// check that at least one BPM has a response to the corrector
		for ( BpmAgent bpmAgent : bpmAgents ) {
			if ( hasResponse( supply, bpmAgent ) ) {
				final double response = getResponse( supply, bpmAgent );
				if ( response != 0.0 )  return true;	// we only need to find one non-zero response
			}
		}
		
		return false;	// no detector responded to the corrector
	}
}

