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

import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.ArrayValue;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.ConnectionPreferenceController;
import xal.tools.pvlogger.ChannelSnapshot;
import xal.tools.pvlogger.MachineSnapshot;
import xal.tools.pvlogger.PVLogger;
import xal.ca.Channel;
import xal.tools.transforms.ValueTransform;


/**
 * This class provides an interface for online model with PV logger data source.
 *
 * @version 0.1 03 Jan 2005
 * @author Paul Chu
 */
public class PVLoggerDataSource {
	private Map<String,ChannelSnapshot> SNAPSHOT_MAP;

	private ChannelSnapshot[] CHANNEL_SNAPSHOTS;
	
	/** magnet values keyed by PV */
	private Map<String,Double> _magnetFields;
	
	/** magnet power supply values keyed by PV */
	private Map<String,Double> _magnetPowerSupplyValues;
	
	/** accelerator sequence */
	private AcceleratorSeq _sequence;
	
	/** PV Logger */
	private PVLogger pvLogger;
	
	/** indicates whether bend fields from the PV Logger are used in the scenario */
	private boolean _usesLoggedBendFields;

	
	/**
	 * @param id
	 *            the PV logger ID
	 */
	public PVLoggerDataSource(long id) {
		_usesLoggedBendFields = false;
		
		// initialize PVLogger
		ConnectionDictionary dict = PVLogger.newBrowsingConnectionDictionary();

		if (dict != null) {
			pvLogger = new PVLogger( dict );
		} 
		else {
			ConnectionPreferenceController.displayPathPreferenceSelector();
			dict = PVLogger.newBrowsingConnectionDictionary();
			pvLogger = new PVLogger( dict );
		}
		
		updatePVLoggerId( id );
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
			pvLogger.closeConnection();
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
			final MachineSnapshot machineSnapshot = pvLogger.fetchMachineSnapshot( id );
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

	
	
	

	private HashMap<String, Double> getMagnetMap() {
		HashMap<String, Double> pvMap = new HashMap<String, Double>();
		
		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:Q") > -1
				|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_Q") > -1) && (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":B") > -1))
				|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:ShntC_Q") > -1) && (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":B_Set") > -1))
				|| CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:DC") > -1
				|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_DC") > -1) && (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":B_Set") > -1))
				|| CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:DH") > -1
				|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_DH") > -1) && (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":B_Set") > -1))					
				) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put( CHANNEL_SNAPSHOTS[i].getPV(), new Double( val[0] ) );
			}
		}
		
		return pvMap;
	}
	
	
	private HashMap<String, Double> getMagnetPSMap() {
		HashMap<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if ( CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:PS_Q") > -1 || CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:PS_DH") > -1 ) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put( CHANNEL_SNAPSHOTS[i].getPV(), new Double( val[0] ) );
			}
		}

		return pvMap;
	}

	public HashMap<String, Double> getBPMXMap() {
		HashMap<String, Double> bpmXMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":xAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmXMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmXMap;
	}

	public HashMap<String, Double> getBPMYMap() {
		HashMap<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":yAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}

	public HashMap<String, Double> getBPMAmpMap() {
		HashMap<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":amplitudeAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}

	public HashMap<String, Double> getBPMPhaseMap() {
		HashMap<String, Double> bpmYMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf(":phaseAvg") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				bpmYMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return bpmYMap;
	}
	
	
	/** Get the logged magnets that are in the specified sequence */
	private List<AcceleratorNode> getLoggedMagnets( final AcceleratorSeq sequence ) {
		// inlclude quadrupoles, dipole correctors and optionally bends
		final OrTypeQualifier magnetQualifier = OrTypeQualifier.qualifierForKinds( Quadrupole.s_strType, HDipoleCorr.s_strType, VDipoleCorr.s_strType );
		if ( _usesLoggedBendFields )  magnetQualifier.or( Bend.s_strType );	// optionally include bends
		
		// filter magnets for those that are strictly electromagnets with good status
		final TypeQualifier electromagnetQualifier = AndTypeQualifier.qualifierWithQualifiers( magnetQualifier, new KindQualifier( Electromagnet.s_strType ) ).andStatus( true );
		
		return sequence.getNodesWithQualifier( electromagnetQualifier );
	}
	
	
	/** Remove this data source from the specified scenario */
	public void removeModelSourceFromScenario( final AcceleratorSeq sequence, final Scenario scenario ) {
		final List<AcceleratorNode> magnets = getLoggedMagnets( sequence );
		for ( final AcceleratorNode magnet : magnets ) {
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
	 * Set the model lattice with PV logger data source
	 * @param seq accelerator sequence
	 * @param scenario Model Scenario object
	 * @return a new scenario with lattice from PV logger data
	 */
	public Scenario setModelSource( final AcceleratorSeq sequence, final Scenario scenario ) {
		_sequence = sequence;
		
		final List<AcceleratorNode> magnets = getLoggedMagnets( sequence );
		for (int i = 0; i < magnets.size(); i++) {
			final Electromagnet magnet = (Electromagnet) magnets.get(i);
			String pvName = "";
			double val = 0.;
			
			// get polarity so we can determine the sign
			double pol = magnet.getPolarity();
			// use field readback
			if (magnet.useFieldReadback()) {
				// System.out.println("Quad " + magnet.getId() + " use
				// fieldReadback");
				Channel chan = magnet.getChannel(Electromagnet.FIELD_RB_HANDLE);
				pvName = chan.channelName();
				if (_magnetFields.containsKey(pvName)) {
					val = _magnetFields.get( pvName ).doubleValue();
					// take into account of proper transform
					val = pol * toPhysicalValue( chan, pol );
				}
				else {		// If there is no magnet field readback, use corresponding power supply field readback, instead.
					Channel chan2 = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_RB_HANDLE );
					String pvName2 = chan2.channelName();
					if (_magnetPowerSupplyValues.containsKey(pvName2)) {
						val = _magnetPowerSupplyValues.get( pvName2 ).doubleValue();
						// take into account of proper transform
						val = pol * toPhysicalValue( chan2, val );
					}
					else {		// if no power supply readback, use power supply fieldSet
						chan2 = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
						pvName2 = chan2.channelName();
						if (_magnetPowerSupplyValues.containsKey(pvName2)) {
							val = _magnetPowerSupplyValues.get( pvName2 ).doubleValue();
							// take into account of proper transform
							val = pol * toPhysicalValue( chan2, val );
						} else
							System.out.println(pvName2 + " has no value");
					}
				}
			}
			else {		// use field set, we need to handle magnets with trim power supplies here. However, if no readback, we have to use field readback
				// for main power supply
				Channel chan = magnet.getMainSupply().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
				pvName = chan.channelName();
				if (_magnetFields.containsKey(pvName)) {
					val = _magnetFields.get( pvName ).doubleValue();
					// take into account of proper transform
					val = pol * toPhysicalValue( chan, val );
					// for trim power supply (check if it has trim first)
					if (magnet instanceof TrimmedQuadrupole) {
						Channel chan1 = ((TrimmedQuadrupole) magnet).getTrimSupply().getChannel( MagnetTrimSupply.FIELD_SET_HANDLE );
						String pvName1 = chan1.channelName();
						if (_magnetFields.containsKey(pvName1)) {
							double trimVal = Math.abs( _magnetFields.get( pvName1 ).doubleValue() );
							// take into account of proper transform
							trimVal = toPhysicalValue( chan1, trimVal );
							
							// handle shunt PS differently
							if (pvName1.indexOf("ShntC") > -1) {
								// for backward compatibility (old data), we take absolute value for the shunt field
								val = val - trimVal;
							} else {
								val = val + trimVal;
							}
						}
					}
				}
				// use readback, if no field settings
				else {
					final Channel readbackChannel = magnet.getChannel( Electromagnet.FIELD_RB_HANDLE );
					pvName = readbackChannel.channelName();
					if ( _magnetFields.containsKey( pvName ) ) {
						val = _magnetFields.get( pvName ).doubleValue();
						val = toPhysicalValue( readbackChannel, val );
					}
				}
			}
			
			scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, val );
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
