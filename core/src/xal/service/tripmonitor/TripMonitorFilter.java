//
//  TripMonitorFilter.java
//  xal
//
//  Created by Thomas Pelaia on 7/31/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.tripmonitor;

import java.util.*;
import java.util.regex.*;

import xal.tools.data.*;
import xal.smf.*;
import xal.smf.data.*;


/** filter to get trip monitor PVs from the optics */
public class TripMonitorFilter {
	/** data label */
	final public static String DATA_LABEL = "TripMonitorFilter";
	
	/** trip channel filters */
	final List<TripChannelFilter> TRIP_CHANNEL_FILTERS;
	
	/** name */
	final protected String NAME;
	
	/** ID of the accelerator sequence from which to fetch the nodes */
	final protected String SEQUENCE_ID;
	
	/** node type */
	final protected String NODE_TYPE;
	
	/** pattern for filtering the node key from the a node ID */
	final protected Pattern NODE_KEY_PATTERN;
	
	/** indicates whether the monitor is enabled */
	final protected boolean IS_ENABLED;
	
	/** persistent store for logging trips */
	final protected PersistentStore PERSISTENT_STORE;
	
	/** group of PV formats which are used to generate PVs from node names */
	final protected PVNodeFormatGroup PV_NODE_FORMAT_GROUP;
	
	
	/** Constructor */
	public TripMonitorFilter( final DataAdaptor adaptor, final TripFilterFactory tripFilterFactory ) {
		NAME = adaptor.stringValue( "name" );
		SEQUENCE_ID = adaptor.stringValue( "sequence" );
		IS_ENABLED = adaptor.booleanValue( "enable" );
		NODE_TYPE = adaptor.stringValue( "nodeType" );
		
		PV_NODE_FORMAT_GROUP = new PVNodeFormatGroup( adaptor.childAdaptor( PVNodeFormatGroup.DATA_LABEL ) );
													  
		NODE_KEY_PATTERN = generateNodeKeyPattern( adaptor.stringValue( "nodeIDPattern" ) );
		
		final String pvFormat = adaptor.stringValue( "pvFormat" );		
		
		TRIP_CHANNEL_FILTERS = new ArrayList<TripChannelFilter>();
		final List<DataAdaptor> monitorAdaptors = adaptor.childAdaptors( TripChannelFilter.DATA_LABEL );
		for ( final DataAdaptor monitorAdaptor : monitorAdaptors ) {
			final String tripFilterName = monitorAdaptor.stringValue( "tripFilter" );
			final TripFilter tripFilter = tripFilterFactory.getTripFilter( tripFilterName );
			if ( tripFilter != null ) {
				TRIP_CHANNEL_FILTERS.add( new TripChannelFilter( monitorAdaptor, tripFilter ) );
			}
			else {
				final String monitorName = monitorAdaptor.stringValue( "PVFormat" );
				System.out.println( "Error!  Trip filter requested for \"" + monitorName + "\" with name: \"" + tripFilterName + "\" does not exist." );
			}
		}
		
		final DataAdaptor persistentStoreAdaptor = adaptor.childAdaptor( PersistentStore.DATA_LABEL );
		PERSISTENT_STORE = new PersistentStore( persistentStoreAdaptor );
	}
	
	
	/** get the name of the trip monitor filter */
	public String getName() {
		return NAME;
	}
	
	
	/** indicates whether the corresponding trip monitor should be enabled by default */
	public boolean isEnabled() {
		return IS_ENABLED;
	}
	
	
	/** get the persistent store */
	public PersistentStore getPersistentStore() {
		return PERSISTENT_STORE;
	}
	
	
	/** generate the node key pattern */
	protected Pattern generateNodeKeyPattern( final String nodeIDPatternString ) {
		return Pattern.compile( nodeIDPatternString );
	}
	
	
	/** get the sequence from the default accelerator */
	public AcceleratorSeq getSequence() {
		final Accelerator accelerator = XMLDataManager.loadDefaultAccelerator();
		return accelerator != null ? getSequence( accelerator ) : null;
	}
	
	
	/** get the accelerator sequence */
	public AcceleratorSeq getSequence( final Accelerator accelerator ) {
		return accelerator.findSequence( SEQUENCE_ID );
	}
	
	
	/** get the list of nodes from the default accelerator */
	public List<AcceleratorNode> getNodes() {
		final AcceleratorSeq sequence = getSequence();
		return sequence != null ? getNodes( sequence ) : new ArrayList<AcceleratorNode>();
	}
	
	
	/** get the list of nodes */
	public List<AcceleratorNode> getNodes( final AcceleratorSeq sequence ) {
		return sequence.getNodesOfType( NODE_TYPE, true );
	}
	
	
	/** extract the node key from the specified pv */
	public String getNodeKey( final String pv ) {
		return PV_NODE_FORMAT_GROUP.getNodeKey( pv );
	}
	
	
	/** get the channel monitors for the specified node */
	public List<ChannelMonitor> getChannelMonitors( final AcceleratorNode node ) {
		final List<ChannelMonitor> channelMonitors = new ArrayList<ChannelMonitor>();
		
		final Matcher match = NODE_KEY_PATTERN.matcher( node.getId() );
		
		if ( match.matches() && match.groupCount() > 0 ) {
			final String nodeKey = match.group(1);
			for ( final TripChannelFilter channelFilter : TRIP_CHANNEL_FILTERS ) {
				final String pv = channelFilter.getPV( nodeKey );
				final TripFilter tripFilter = channelFilter.getTripFilter();
				channelMonitors.add( new ChannelMonitor( pv, tripFilter ) );
			}
		}
		
		return channelMonitors;		
	}
	
	
	/** get the trip PVs to monitor for the specified node */
	public List<String> getTripPVs( final AcceleratorNode node ) {
		final List<String> pvs = new ArrayList<String>();
		
		final Matcher match = NODE_KEY_PATTERN.matcher( node.getId() );
		
		if ( match.matches() && match.groupCount() > 0 ) {
			final String nodeKey = match.group(1);
			for ( final TripChannelFilter channelFilter : TRIP_CHANNEL_FILTERS ) {
				pvs.add( channelFilter.getPV( nodeKey ) );
			}
		}
		
		return pvs;		
	}
	
	
	/** get the trip PV for the specified channel filter and node key */
	public String getTripPV( final TripChannelFilter channelFilter, final String nodeKey ) {
		return channelFilter.getPV( nodeKey );
	}
	
	
	/** get the trip channel filters */
	public List<TripChannelFilter> getTripChannelFilters() {
		return TRIP_CHANNEL_FILTERS;
	}
	
	
	/** string representation of this instance */
	public String toString() {
		final StringBuffer buffer = new StringBuffer();
		
		buffer.append( "name:  " + NAME + ", sequence:  " + SEQUENCE_ID + ", node type:  " + NODE_TYPE + ", node key pattern:  " + NODE_KEY_PATTERN );
		buffer.append( ", channel filters:  " + TRIP_CHANNEL_FILTERS );
		
		return buffer.toString();
	}
}
