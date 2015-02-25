//
//  CAServer.java
//  xal
//
//  Created by Tom Pelaia on 4/28/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.virtualaccelerator;

import xal.ca.*;
import xal.smf.*;
import xal.smf.impl.qualify.*;
import xal.smf.impl.*;

import java.io.*;
import java.util.*;


/** Server channel access */
public class VAServer {	
	/** size for array PVs */
	static final int DEFAULT_ARRAY_SIZE = 1024;
    
	/** The sequence for which to server channels */
	final private AcceleratorSeq SEQUENCE;

	/** Server to process channel requests */
	final private ChannelServer CHANNEL_SERVER;

	
	/**
	 * Constructor
	 * @param sequence The sequence for which to serve channels
	 */
	public VAServer( final AcceleratorSeq sequence ) throws Exception {
		SEQUENCE = sequence;

		/** Create a new instance of the channel server */
		CHANNEL_SERVER = ChannelServer.getInstance();

		/** register channels for the sequence's nodes */
		registerNodeChannels();
	}
	
	
	/** dispose of the context */
	public void destroy() throws Exception {
		if ( CHANNEL_SERVER != null ) {
			CHANNEL_SERVER.destroy();
		}
	}
	
	
	/**
	 * Write the PCAS input file for the list of PVs in the usual nodes
	 * @param writer The writer to which to output the PCAS file
	 */
	private void registerNodeChannels() throws Exception {
		registerNodeChannels( Quadrupole.s_strType, null, AndTypeQualifier.qualifierWithQualifiers( new KindQualifier( Quadrupole.s_strType ), new NotTypeQualifier( TrimmedQuadrupole.s_strType ) ) );		// untrimmed quads
		registerNodeChannels( TrimmedQuadrupole.s_strType );
		registerNodeChannels( Bend.s_strType );
		registerNodeChannels( Sextupole.s_strType );
		registerNodeChannels( HDipoleCorr.s_strType );
		registerNodeChannels( VDipoleCorr.s_strType );
		registerNodeChannels( ExtractionKicker.s_strType );
		registerNodeChannels( RfCavity.s_strType );
		registerNodeChannels( CurrentMonitor.s_strType );
		registerNodeChannels( BPM.s_strType );
		registerNodeChannels( BLM.s_strType );
		registerNodeChannels( Solenoid.s_strType );
        
        // need to distinguish profile monitors from wire scanners which share the same type but have different soft types
		registerNodeChannels( ProfileMonitor.s_strType, ProfileMonitor.SOFTWARE_TYPE, AndTypeQualifier.qualifierWithQualifiers( new KindQualifier( ProfileMonitor.s_strType ), QualifierFactory.getSoftTypeQualifier( ProfileMonitor.SOFTWARE_TYPE ) ) );
        // TODO: register wirescanner nodes
//		registerNodeChannels( WireScanner.s_strType, WireScanner.SOFTWARE_TYPE, AndTypeQualifier.qualifierWithQualifiers( new KindQualifier( WireScanner.s_strType ), QualifierFactory.getSoftTypeQualifier( WireScanner.SOFTWARE_TYPE ) ) );
        
		registerTimingSignals();
	}
	
	
	/**
	 * Write to the PCAS input file the list of PVs for the specified node type.
	 * @param type node type for which to register channels
	 */
	private void registerNodeChannels( final String type ) throws IOException {
		registerNodeChannels( type, null, new KindQualifier( type ) );
	}
	
	
	/**
	 * Write to the PCAS input file the list of PVs for the specified node type.
	 * @param type node type for which to register channels
	 * @param nodeFilter a qualifier to filter which nodes to process
	 */
	private void registerNodeChannels( final String type, final String softType, final TypeQualifier nodeFilter ) throws IOException {
		final NodeSignalProcessor processor = NodeSignalProcessor.getInstance( type, softType );
		final List<SignalEntry> signals = new ArrayList<SignalEntry>();
		final TypeQualifier qualifier = QualifierFactory.qualifierForQualifiers( true, nodeFilter );
		final List<AcceleratorNode> nodes = SEQUENCE.getAllInclusiveNodesWithQualifier( qualifier );
		
        System.out.println( "type: " + type );
		for ( AcceleratorNode node : nodes ) {
            System.out.println( "node: " + node.getId() + ", soft type: " + node.getSoftType() + ", status: " + node.getStatus() );
			final Collection<String> handles = processor.getHandlesToProcess( node );
            System.out.println( "handles: " + handles + "\n\n" );
            for ( final String handle : handles ) {
				System.out.println( "Getting channel for handle: " + handle );
				final Channel channel = node.getChannel( handle );
				if ( channel != null ) {
					final String signal = channel.channelName();
					System.out.println( "Channel for handle: " + signal );
					final SignalEntry entry = new SignalEntry( signal, handle );
					if ( !signals.contains( entry ) ) {
						signals.add( entry );
					}
				}
			}
		}
		
		for ( SignalEntry entry : signals ) {
			processor.makePV( CHANNEL_SERVER, entry );
		}
	}
	
	
	/**
	 * Write to the PCAS input file the list of timing signals.
	 */
	protected void registerTimingSignals() throws IOException {
		final TimingCenterProcessor processor = new TimingCenterProcessor();
		final List<SignalEntry> signals = new ArrayList<SignalEntry>();
		final TimingCenter timingCenter = SEQUENCE.getAccelerator().getTimingCenter();
		
		if ( timingCenter == null )  return;
		final Collection<String> handles = processor.getHandlesToProcess( timingCenter );
		
        for ( final String handle : handles ) {
			final Channel channel = timingCenter.getChannel( handle );
			if ( channel != null ) {
				final String signal = channel.channelName();
				final SignalEntry entry = new SignalEntry( signal, handle );
				if ( !signals.contains( entry ) ) {
					signals.add( entry );
				}
			}
		}
		
		for ( SignalEntry entry : signals ) {
			processor.makePV( CHANNEL_SERVER, entry );
		}
	}
}



/** Default processor for a signal */
class SignalProcessor {
	/** size for array PVs */
	static final private int DEFAULT_ARRAY_SIZE = VAServer.DEFAULT_ARRAY_SIZE;

	/** process the signal entry */
	public ChannelServerPV makePV( final ChannelServer server, final SignalEntry entry ) {
		final String signalName = entry.getSignal();
		final int size = entry.getSignal().matches( ".*(TBT|A)" ) ? DEFAULT_ARRAY_SIZE : 1;
		final ChannelServerPV serverPV = server.registerPV( signalName, new double[size] );
		serverPV.setUnits( "units" );

		appendLimits( entry, serverPV );
        
        if ( size == 1 ) {
            final String[] warningPVs = ChannelFactory.defaultFactory().getChannel( signalName ).getWarningLimitPVs();
            server.registerPV( warningPVs[0], 0 );
            server.registerPV( warningPVs[1], 0 );
            
            final String[] alarmPVs = ChannelFactory.defaultFactory().getChannel( signalName ).getAlarmLimitPVs();
            server.registerPV( alarmPVs[0], 0 );
            server.registerPV( alarmPVs[1], 0 );

            final String[] operationLimitPVs = ChannelFactory.defaultFactory().getChannel( signalName ).getOperationLimitPVs();
            server.registerPV( operationLimitPVs[0], serverPV.getLowerDispLimit().doubleValue() );
            server.registerPV( operationLimitPVs[1], serverPV.getUpperDispLimit().doubleValue() );

            final String[] driveLimitPVs = ChannelFactory.defaultFactory().getChannel( signalName ).getDriveLimitPVs();
            server.registerPV( driveLimitPVs[0], serverPV.getLowerCtrlLimit().doubleValue() );
            server.registerPV( driveLimitPVs[1], serverPV.getUpperCtrlLimit().doubleValue() );
        }

		return serverPV;
	}
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {}
	
	
	protected void setLimits( final ChannelServerPV mpv, final double lowerLimit, final double upperLimit ) {
		mpv.setLowerDispLimit( lowerLimit );
		mpv.setUpperDispLimit( upperLimit );
		
		mpv.setLowerAlarmLimit( lowerLimit );
		mpv.setUpperAlarmLimit( upperLimit );
		
		mpv.setLowerCtrlLimit( lowerLimit );
		mpv.setUpperCtrlLimit( upperLimit );
		
		mpv.setLowerWarningLimit( lowerLimit );
		mpv.setUpperWarningLimit( upperLimit );
	}
}



/**
 * Signal processor class for nodes.
 */
class NodeSignalProcessor extends SignalProcessor {
	/**
	 * Get the appropriate processor instance for the specified node type
	 * @param type The type of node for which to process the signals
	 * @return An instance of SignalProcessor or a subclass appropriate for the node type
	 */
	static public NodeSignalProcessor getInstance( final String type, final String softType ) {
		if ( type == Quadrupole.s_strType || type == Bend.s_strType || type == Solenoid.s_strType )  return new UnipolarEMProcessor();
		else if ( type == TrimmedQuadrupole.s_strType )  return new TrimmedQuadrupoleProcessor();
		else if ( type == BPM.s_strType )  return new BPMProcessor();
		else if ( type == VDipoleCorr.s_strType || type == HDipoleCorr.s_strType )  return new DipoleCorrectorProcessor();
		else if ( type == Sextupole.s_strType )  return new SextupoleProcessor();
		else if ( type == ProfileMonitor.s_strType && softType == ProfileMonitor.SOFTWARE_TYPE )  return new ProfileMonitorProcessor();
        // TODO: add support for WireScanner processors
//		else if ( type == WireScanner.s_strType && softType == WireScanner.SOFTWARE_TYPE )  return new WireScannerProcessor();
//		else if ( type == WireScanner2.s_strType && softType == WireScanner2.SOFTWARE_TYPE) return new WireScanner2Processor();
		else return new NodeSignalProcessor();
	}
	
	
	/**
	 * Get the handles we wish to process for a node.  By default we process all of a node's handles.  Subclasses may wish to override this method to return only a subset of handles.
	 * @param node The node whose handles we wish to get
	 * @return the collection of the node's handles we wish to process
	 */
	public Collection<String> getHandlesToProcess( final AcceleratorNode node ) {
		return node.getHandles();
	}
}



/** Implement the processor for the ProfileMonitor.  This class returns only the X and Y sigma M handles. */
class ProfileMonitorProcessor extends NodeSignalProcessor {
	final static private Collection<String> HANDLES;
	
	// static initializer
	static {
		HANDLES = new ArrayList<String>();
		HANDLES.add( ProfileMonitor.H_SIGMA_M_HANDLE );
		HANDLES.add( ProfileMonitor.V_SIGMA_M_HANDLE );
		HANDLES.add( ProfileMonitor.H_SIGMA_F_HANDLE );
		HANDLES.add( ProfileMonitor.V_SIGMA_F_HANDLE );
	}
	
	
	/**
	 * Get the handles we wish to process for a node.  This processor overrides this method to return only the handles of interest for the node.
	 * @return the collection of the node's handles we wish to process
	 */
	public Collection<String> getHandlesToProcess( final AcceleratorNode node ) {
		return HANDLES;
	}	
}



/** Implement the processor for the WireScanner. */
class WireScannerProcessor extends NodeSignalProcessor {
	final static private Collection<String> HANDLES;
	
	// static initializer
	static {
		HANDLES = new ArrayList<String>();
        // TODO: add wirescanner handles
//		HANDLES.add( WireScanner.HORIZONTAL_SIGMA_GAUSS_HANDLE );
//		HANDLES.add( WireScanner.VERTICAL_SIGMA_GAUSS_HANDLE );
	}
	
	
	/**
	 * Get the handles we wish to process for a node.  This processor overrides this method to return only the handles of interest for the node.
	 * @return the collection of the node's handles we wish to process
	 */
	public Collection<String> getHandlesToProcess( final AcceleratorNode node ) {
		return HANDLES;
	}	
}


/** Implement the processor for the WireScanner2. */
class WireScanner2Processor extends NodeSignalProcessor {
    final static private Collection<String> HANDLES;
    
    // static initializer
    static {
        HANDLES = new ArrayList<String>();
        // TODO: add wirescanner 2 handles
//        HANDLES.add( WireScanner2.HORIZONTAL_SIGMA_GAUSS_HANDLE );
//        HANDLES.add( WireScanner2.VERTICAL_SIGMA_GAUSS_HANDLE );
    }
    
    
    /**
     * Get the handles we wish to process for a node.  This processor overrides this method to return only the handles of interest for the node.
     * @return the collection of the node's handles we wish to process
     */
    public Collection<String> getHandlesToProcess( final AcceleratorNode node ) {
        return HANDLES;
    }   
}


/** Signal processor appropriate for processing sextupole electro magnets */
class SextupoleProcessor extends NodeSignalProcessor {
	static final private Set<String> LIMIT_HANDLES;
	
	
	//Static initializer for setting constant values
	static {		
		LIMIT_HANDLES = new HashSet<String>();
		LIMIT_HANDLES.add( Electromagnet.FIELD_RB_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_SET_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_RB_HANDLE );
	}
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		if ( LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, -10.0, 10.0 );
		}
	}
}



/** Signal processor appropriate for processing unipolar electro magnets */
class UnipolarEMProcessor extends NodeSignalProcessor {
	static final private Set<String> LIMIT_HANDLES;
	
	
	//Static initializer for setting constant values
	static {		
		LIMIT_HANDLES = new HashSet<String>();
		LIMIT_HANDLES.add( Electromagnet.FIELD_RB_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_SET_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_RB_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_BOOK_HANDLE );
	}
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		if ( LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, 0.0, 50.0 );
		}
	}
}



/** Signal processor appropriate for processing trimmed quadrupoles */
class TrimmedQuadrupoleProcessor extends NodeSignalProcessor {
	static final private Set<String> MAIN_LIMIT_HANDLES;
	static final private Set<String> TRIM_LIMIT_HANDLES;
	
	
	// Static initializer for setting constant values
	static {		
		MAIN_LIMIT_HANDLES = new HashSet<String>();
		MAIN_LIMIT_HANDLES.add( Electromagnet.FIELD_RB_HANDLE );
		MAIN_LIMIT_HANDLES.add( MagnetMainSupply.FIELD_SET_HANDLE );
		MAIN_LIMIT_HANDLES.add( MagnetMainSupply.FIELD_RB_HANDLE );
		MAIN_LIMIT_HANDLES.add( MagnetMainSupply.FIELD_BOOK_HANDLE );
		
		TRIM_LIMIT_HANDLES = new HashSet<String>();
		TRIM_LIMIT_HANDLES.add( MagnetTrimSupply.FIELD_RB_HANDLE );
		TRIM_LIMIT_HANDLES.add( MagnetTrimSupply.FIELD_SET_HANDLE );
	}
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		if ( MAIN_LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, 0.0, 50.0 );
		}
		else if ( TRIM_LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, -1.0, 1.0 );
		}
	}
}



/** Signal processor appropriate for processing BPMs */
class BPMProcessor extends NodeSignalProcessor {
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		final String handle = entry.getHandle();
		if ( BPM.AMP_AVG_HANDLE.equals( handle ) ) {
			setLimits( mpv, 0.0, 50.0 );
		}
		else {
			setLimits( mpv, -1000.0, 1000.0 );
		}
	}	
}



/** Signal processor appropriate for processing bends */
class BendProcessor extends NodeSignalProcessor {
	static final private Set<String> LIMIT_HANDLES;
	
	
	/**
	 * Static initializer for setting constant values
	 */
	static {		
		LIMIT_HANDLES = new HashSet<String>();
		LIMIT_HANDLES.add( Electromagnet.FIELD_RB_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_SET_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_RB_HANDLE );
	}
	
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		if ( LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, -1.5, 1.5 );
		}
	}
}



/** Signal processor appropriate for processing dipole correctors */
class DipoleCorrectorProcessor extends NodeSignalProcessor {
	static final private Set<String> LIMIT_HANDLES;
	
	
	/**
	 * Static initializer for setting constant values
	 */
	static {
		LIMIT_HANDLES = new HashSet<String>();
		LIMIT_HANDLES.add( Electromagnet.FIELD_RB_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_SET_HANDLE );
		LIMIT_HANDLES.add( MagnetMainSupply.FIELD_RB_HANDLE );
	}
	
	
	protected void appendLimits( final SignalEntry entry, final ChannelServerPV mpv ) {
		if ( LIMIT_HANDLES.contains( entry.getHandle() ) ) {
			setLimits( mpv, -0.01, 0.01 );
		}
	}
}



/** Implement the processor for the TimingCenter. */
class TimingCenterProcessor extends SignalProcessor {
	/**
	 * Get the handles from the TimingCenter.
	 * @param timingCenter The timing center whose handles we wish to get
	 * @return the collection of the node's handles we wish to process
	 */
	public Collection<String> getHandlesToProcess( final TimingCenter timingCenter ) {
		return timingCenter.getHandles();
	}
}



/** Signal/handle pair */
final class SignalEntry {
	final protected String _signal;
	final protected String _handle;
	
	
	/**
	 * Constructor
	 */
	public SignalEntry( final String signal, final String handle ) {
		_signal = signal;
		_handle = handle;
	}
	
	
	/**
	 * Get the signal
	 * @return the signal
	 */
	final public String getSignal() {
		return _signal;
	}
	
	
	/**
	 * Get the handle
	 * @return the handle
	 */
	final public String getHandle() {
		return _handle;
	}
	
	
	/**
	 * Two signal entries are equal if they have the same signal
	 * @param anObject The signal entry against which to compare
	 * @return true if the two signal entries have the same signal
	 */
	final public boolean equals( final Object anObject ) {
		return anObject instanceof SignalEntry && _signal.equals( ((SignalEntry)anObject).getSignal());
	}


	/** Override hashCode() as required for consistency with equals() */
	final public int hashCode() {
		return _signal.hashCode();
	}
}
