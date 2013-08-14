/*
 * @(#)PVLoggerDataSource.java          0.0 01/03/2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.sim.sync;

import java.util.*;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.smf.proxy.*;
import xal.tools.ArrayValue;
import xal.service.pvlogger.*;
import xal.tools.database.*;
import xal.sim.scenario.*;
import xal.sim.sync.SynchronizationException;
import xal.ca.Channel;
import xal.tools.transforms.ValueTransform;


/**
 * This class provides an interface for online model with PV logger data source.
 *
 * @version 0.1 03 Jan 2005
 * @author Paul Chu
 */
public class PVLoggerDataSource {
	/** PV Logger */
	final private PVLogger PV_LOGGER;
    
	private Map<String,ChannelSnapshot> SNAPSHOT_MAP;

    private ChannelSnapshot[] CHANNEL_SNAPSHOTS;
	
	/** magnet values keyed by PV */
	private Map<String,Double> _magnetFields;
	
	/** magnet power supply values keyed by PV */
	private Map<String,Double> _magnetPowerSupplyValues;
	
	/** accelerator sequence */
	private AcceleratorSeq _sequence;
	
	/** indicates whether bend fields from the PV Logger are used in the scenario */
	private boolean _usesLoggedBendFields;
    
    
    /** Primary Constructor
     * @param id the PV Logger ID
     * @param theLogger existing, connected PV Logger to use
     */
    public PVLoggerDataSource( final long id, final PVLogger theLogger ) {
		_usesLoggedBendFields = false;
		
        if ( theLogger != null ) {
            PV_LOGGER = theLogger;
        }
        else {
            // initialize PVLogger
            ConnectionDictionary dict = PVLogger.newBrowsingConnectionDictionary();
            
            if (dict != null) {
                PV_LOGGER = new PVLogger( dict );
            } 
            else {
                ConnectionPreferenceController.displayPathPreferenceSelector();
                dict = PVLogger.newBrowsingConnectionDictionary();
                PV_LOGGER = new PVLogger( dict );
            }
        }
        
        updatePVLoggerId( id );
    }
    
    
	/**
     * Constructor
	 * @param id the PV logger ID
	 */
	public PVLoggerDataSource( final long id ) {
        this( id, null );
	}
	
	
	/** Determine whether logged bend fields are applied in the scenario */
	public boolean getUsesLoggedBendFields() {
		return _usesLoggedBendFields;
	}
	
	
	/** Sets whether to use the logged bend fields in the scenario */
	public void setUsesLoggedBendFields( final boolean useLoggedBends ) {
		_usesLoggedBendFields = useLoggedBends;
	}
	
	
	/** close the PV Logger connection */
	public void closeConnection() {
		try {
			PV_LOGGER.closeConnection();
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
		}
	}


	/**
	 * Update this data source with the data from the specified PV Logger snapshot
	 * @param id the PV logger ID
	 */
	public void updatePVLoggerId( final long id ) {
		try {
			final MachineSnapshot machineSnapshot = PV_LOGGER.fetchMachineSnapshot( id );
			CHANNEL_SNAPSHOTS = machineSnapshot.getChannelSnapshots();
			SNAPSHOT_MAP = populateChannelSnapshotTable();
			_magnetFields = getMagnetMap();
			_magnetPowerSupplyValues = getMagnetPSMap();
		}
		catch( Exception exception ) {
			throw new RuntimeException( exception );
		}
	}

	/** populate the channel snapshot table */
	protected Map<String,ChannelSnapshot> populateChannelSnapshotTable() {
		final Map<String,ChannelSnapshot> snapshotMap = new HashMap<String,ChannelSnapshot>( CHANNEL_SNAPSHOTS.length );
		for ( final ChannelSnapshot channelSnapshot : CHANNEL_SNAPSHOTS ) {
			snapshotMap.put( channelSnapshot.getPV(), channelSnapshot );
		}
		return snapshotMap;
	}


	/** get a channel snapshot for the specified PV */
	public ChannelSnapshot getChannelSnapshot( final String pv ) {
		return SNAPSHOT_MAP.get( pv );
	}


	/** get the value for the channel snapshot corresponding to the specified PV */
	public double[] getChannelSnapshotValue( final String pv ) {
		final ChannelSnapshot snapshot = getChannelSnapshot( pv );
		return snapshot != null ? snapshot.getValue() : null;
	}
	
	
	/** 
	 * Get the value map for magnets 
	 * @deprecated use getMagnetMap() instead as this method is misleading since it applies to more than just quads
	 */
	@Deprecated
	public Map<String, Double> getQuadMap() {
		return getMagnetMap();
	}
	
	
	/** Get the value map for magnets */
	public Map<String, Double> getMagnetMap() {
		final Map<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			final String snapshotPV = CHANNEL_SNAPSHOTS[i].getPV();
			if ( CHANNEL_SNAPSHOTS[i].getPV().contains( "Mag:" ) ) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put( snapshotPV, val[0] );
			}
		}

		return pvMap;
	}
	
	
	/** 
	 * Get the value map for magnets 
	 * @deprecated use getMagnetPSMap() instead as this method is misleading since it applies to more than just quads
	 */
	@Deprecated
	public Map<String, Double> getQuadPSMap() {
		return getMagnetMap();
	}
	
	
	/** Get the value map for magnet power supplies */
	public Map<String, Double> getMagnetPSMap() {
		final Map<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:PS_Q") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return pvMap;
	}

	
	/** Get the value map for horizontal BPM signals */
	public Map<String, Double> getBPMXMap() {
		final Map<String, Double> bpmXMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":xAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmXMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmXMap;
	}

	
	/** Get the value map for vertical BPM signals */
	public Map<String, Double> getBPMYMap() {
		Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":yAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}

	
	/** Get the value map for BPM amplitude */
	public Map<String, Double> getBPMAmpMap() {
		final Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":amplitudeAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}
	
	
	/** Get the value map for BPM phase */
	public Map<String, Double> getBPMPhaseMap() {
		final Map<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":phaseAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}
	
	
	/** Get the logged magnets that are in the specified sequence */
	private List<Electromagnet> getLoggedMagnets( final AcceleratorSeq sequence ) {
		// inlclude quadrupoles, dipole correctors and optionally bends
		final OrTypeQualifier magnetQualifier = OrTypeQualifier.qualifierForKinds( Quadrupole.s_strType, HDipoleCorr.s_strType, VDipoleCorr.s_strType );
		if ( _usesLoggedBendFields )  magnetQualifier.or( Bend.s_strType );	// optionally include bends
		
		// filter magnets for those that are strictly electromagnets with good status
		final TypeQualifier electromagnetQualifier = AndTypeQualifier.qualifierWithQualifiers( magnetQualifier, new KindQualifier( Electromagnet.s_strType ) ).andStatus( true );
		final List<AcceleratorNode> magnetNodes = sequence.getNodesWithQualifier( electromagnetQualifier );
		final List<Electromagnet> magnets = new ArrayList<Electromagnet>( magnetNodes.size() );
		for ( final AcceleratorNode magnetNode : magnetNodes ) {
			magnets.add( (Electromagnet)magnetNode );
		}
		
		return magnets;
	}
	
	
	/** Remove this data source from the specified scenario */
	public void removeModelSourceFromScenario( final AcceleratorSeq sequence, final Scenario scenario ) {
		final List<Electromagnet> magnets = getLoggedMagnets( sequence );
		for ( final Electromagnet magnet : magnets ) {
			scenario.removeModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD );
		}
		
		try {
			scenario.resync();
		} 
		catch ( SynchronizationException exception ) {
			exception.printStackTrace();
		}
	}
	
	
	/** 
	 * PV Logger logs raw values, but optics includes channel transforms that need to be applied. 
	 * @param rawValue raw channel value
	 * @return physical value
	 */
	static private double toPhysicalValue( final Channel channel, final double rawValue ) {
		final ValueTransform transform = channel.getValueTransform();
		return transform != null ? transform.convertFromRaw( ArrayValue.doubleStore( rawValue ) ).doubleValue() : rawValue;
	}
	
	
	/** 
	 * PV Logger logs raw values, but optics includes channel transforms that need to be applied and conversion to field (e.g. polarity scaling). 
	 * @param rawValue raw channel value
	 * @return field
	 */
	static private double toFieldFromRaw( final Electromagnet magnet, final Channel channel, final double rawValue ) {
		final double transformedValue = toPhysicalValue( channel, rawValue );
		return magnet.toFieldFromCA( transformedValue );
	}


	/** Get the magnet's field from the PV Logger Snapshot */
	public double getLoggedField( final Electromagnet magnet ) {
		double totalField = 0.0;

		// use field readback
		if ( magnet.useFieldReadback() ) {
			// System.out.println("Quad " + magnet.getId() + " use fieldReadback");
			final Channel channel = magnet.getChannel( Electromagnet.FIELD_RB_HANDLE );
			final String pvName = channel.channelName();
			if ( _magnetFields.containsKey( pvName ) ) {
				final double rawValue = _magnetFields.get( pvName );
				// take into account of proper transform
				totalField = toFieldFromRaw( magnet, channel, rawValue );
			}
			else {		// If there is no magnet field readback, use corresponding power supply field readback, instead.
				final Channel mainSupplyReadbackChannel = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_RB_HANDLE );
				final String mainSupplyReadbackPV = mainSupplyReadbackChannel.channelName();
				if ( _magnetPowerSupplyValues.containsKey( mainSupplyReadbackPV ) ) {
					final double rawValue = _magnetPowerSupplyValues.get( mainSupplyReadbackPV );
					// take into account of proper transform
					totalField = toFieldFromRaw( magnet, mainSupplyReadbackChannel, rawValue );
				}
				else {		// if no power supply readback, use power supply fieldSet
					final Channel mainSupplySetpointChannel = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
					final String mainSupplySetpointPV = mainSupplySetpointChannel.channelName();
					if ( _magnetPowerSupplyValues.containsKey( mainSupplySetpointPV ) ) {
						final double rawValue = _magnetPowerSupplyValues.get( mainSupplySetpointPV );
						// take into account of proper transform
						totalField = toFieldFromRaw( magnet, mainSupplySetpointChannel, rawValue );
					}
					else {
						System.out.println( "No logged field for " + magnet.getId() + " after trying: " + pvName + ", " + mainSupplyReadbackPV + ", " + mainSupplySetpointPV  );
					}
				}
			}
		}
		else {		// use field set, we need to handle magnets with trim power supplies here. However, if no readback, we have to use field readback
			// for main power supply
			final Channel chan = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
			final String fieldSetPV = chan.channelName();
			if ( _magnetFields.containsKey( fieldSetPV ) ) {
				final double rawValue = _magnetFields.get( fieldSetPV );
				// take into account of proper transform
				totalField = toFieldFromRaw( magnet, chan, rawValue );
				// for trim power supply (check if it has trim first)
				if ( magnet instanceof TrimmedQuadrupole ) {
					final Channel trimFieldChannel = ((TrimmedQuadrupole) magnet).getTrimSupply().getChannel( MagnetTrimSupply.FIELD_SET_HANDLE );
					final String trimFieldPV = trimFieldChannel.channelName();
					if ( _magnetFields.containsKey( trimFieldPV ) ) {
						final double trimVal = _magnetFields.get( trimFieldPV );
						// take into account of proper transform
						final double trimField = toFieldFromRaw( magnet, trimFieldChannel, trimVal );

						// todo: this logic needs to move to the TrimmedQuadrupole class
						// handle shunt PS differently
						if ( trimFieldPV.indexOf( "ShntC" ) > -1 ) {
							final double shuntField = Math.abs( trimField );	// shunt is unipolar
							// shunt always opposes the main field
							totalField = totalField * trimField > 0 ? totalField - shuntField : totalField + shuntField;
						}
						else {
							totalField += trimField;
						}
					}
				}
			}
			// use readback, if no field settings
			else {
				final Channel readbackChannel = magnet.getChannel( Electromagnet.FIELD_RB_HANDLE );
				final String fieldReadbackPV = readbackChannel.channelName();
				if ( _magnetFields.containsKey( fieldReadbackPV ) ) {
					final double rawValue = _magnetFields.get( fieldReadbackPV );
					totalField = toFieldFromRaw( magnet, readbackChannel, rawValue );
				}
			}
		}

		return totalField;
	}


	/**
	 * set the model lattice with PV logger data source
	 * @param sequence accelerator sequence
	 * @param scenario Model Scenario object
	 * @return a new scenario with lattice from PV logger data
	 */
	public Scenario setModelSource( final AcceleratorSeq sequence, final Scenario scenario ) {
		_sequence = sequence;
		
		final List<Electromagnet> magnets = getLoggedMagnets( sequence );
		for ( int i = 0; i < magnets.size(); i++ ) {
			final Electromagnet magnet = magnets.get(i);
			final double field = getLoggedField( magnet );
			scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field );
		}

		try {
			scenario.resync();
		} catch (SynchronizationException e) {
			System.out.println(e);
		}

		return scenario;
	}

	public void setAccelSequence(AcceleratorSeq seq) {
		_sequence = seq;
	}

	/**
	 * get the beam current in mA, we use the first available BCM in the
	 * sequence. If the first in the sequence is not available, use MEBT BCM02.
	 * If it's also not available, then default to 20mA
	 *
	 * @return beam current
	 */
	public double getBeamCurrent() {
		double current = 20.;
		List<AcceleratorNode> bcms = _sequence.getAllNodesOfType("BCM");
		List<AcceleratorNode> allBCMs = AcceleratorSeq.filterNodesByStatus(bcms, true);

		if (_sequence.getAllNodesOfType("BCM").size() > 0) {
			String firstBCM = ((CurrentMonitor) allBCMs.get(0)).getId();
			for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
				if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(firstBCM) > -1
						&& CHANNEL_SNAPSHOTS[i].getPV().indexOf(":currentMax") > -1) {
					current = CHANNEL_SNAPSHOTS[i].getValue()[0];
					return current;
				} else if (CHANNEL_SNAPSHOTS[i].getPV().equals("MEBT_Diag:BCM02:currentMax")) {
					current = CHANNEL_SNAPSHOTS[i].getValue()[0];
					return current;
				}
			}
		}

		return current;
	}

	/**
	 * get the beam current in mA, use the BCM specified here. If it's not
	 * available, use 20mA as default
	 *
	 * @param bcm
	 *            the BCM you want the beam current reading from
	 * @return beam current
	 */
	public double getBeamCurrent(String bcm) {
		double current = 20;
		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(bcm) > -1
					&& CHANNEL_SNAPSHOTS[i].getPV().indexOf(":currentMax") > -1) {
				current = CHANNEL_SNAPSHOTS[i].getValue()[0];
				return current;
			}
		}
		return current;
	}

	/**
	 * get all the channel snapshots.
	 *
	 * @return channel snapshots in array
	 */
	public ChannelSnapshot[] getChannelSnapshots() {
		return CHANNEL_SNAPSHOTS;
	}
}
