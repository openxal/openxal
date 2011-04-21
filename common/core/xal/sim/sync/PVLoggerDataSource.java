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
import xal.smf.impl.CurrentMonitor;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.impl.MagnetTrimSupply;
import xal.smf.impl.TrimmedQuadrupole;
import xal.smf.impl.qualify.AndTypeQualifier;
import xal.smf.impl.qualify.NotTypeQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.ArrayValue;
import xal.tools.database.ConnectionDictionary;
import xal.tools.database.ConnectionPreferenceController;
import xal.tools.pvlogger.ChannelSnapshot;
import xal.tools.pvlogger.MachineSnapshot;
import xal.tools.pvlogger.PVLogger;
import xal.ca.Channel;

/**
 * This class provides an interface for online model with PV logger data source.
 *
 * @version 0.1 03 Jan 2005
 * @author Paul Chu
 */
public class PVLoggerDataSource {
	protected Map<String,ChannelSnapshot> SNAPSHOT_MAP;

  protected ChannelSnapshot[] CHANNEL_SNAPSHOTS;

	protected HashMap qPVMap;

	protected HashMap qPSPVMap;

	protected AcceleratorSeq _sequence;

	protected PVLogger pvLogger;

	/**
	 * @param id
	 *            the PV logger ID
	 */
	public PVLoggerDataSource(long id) {
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
	 * @param id
	 *            the PV logger ID
	 */
	public void updatePVLoggerId(long id) {
		try {
			final MachineSnapshot machineSnapshot = pvLogger.fetchMachineSnapshot( id );
			CHANNEL_SNAPSHOTS = machineSnapshot.getChannelSnapshots();
			SNAPSHOT_MAP = populateChannelSnapshotTable();
			qPVMap = getQuadMap();
			qPSPVMap = getQuadPSMap();
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

	
	
	

	public HashMap<String, Double> getQuadMap() {
		HashMap<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:Q") > -1
					|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_Q") > -1) && (CHANNEL_SNAPSHOTS[i]
							.getPV().indexOf(":B") > -1))
					|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:ShntC_Q") > -1) && (CHANNEL_SNAPSHOTS[i]
							.getPV().indexOf(":B_Set") > -1))
					|| CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:DC") > -1
					|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_DC") > -1) && (CHANNEL_SNAPSHOTS[i]
							.getPV().indexOf(":B_Set") > -1))
					//|| CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:DH") > -1
					//|| ((CHANNEL_SNAPSHOTS[i].getPV().indexOf("_Mag:PS_DH") > -1) && (CHANNEL_SNAPSHOTS[i]
					//		.getPV().indexOf(":B_Set") > -1))					
					) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
			}
		}

		return pvMap;
	}

	public HashMap<String, Double> getQuadPSMap() {
		HashMap<String, Double> pvMap = new HashMap<String, Double>();

		for (int i = 0; i < CHANNEL_SNAPSHOTS.length; i++) {
			if (CHANNEL_SNAPSHOTS[i].getPV().indexOf("Mag:PS_Q") > -1) {
				double[] val = CHANNEL_SNAPSHOTS[i].getValue();
				pvMap.put(CHANNEL_SNAPSHOTS[i].getPV(), new Double(val[0]));
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

	/**
	 * set the model lattice with PV logger data source
	 *
	 * @param seq
	 *            accelerator sequence
	 * @param scenario
	 *            Model Scenario object
	 * @return a new scenario with lattice from PV logger data
	 */
	public Scenario setModelSource(AcceleratorSeq seq, Scenario scenario) {
		_sequence = seq;
		AndTypeQualifier atq = new AndTypeQualifier();
		// all quads
		atq.and("Q");
		// all dipole correctors
		OrTypeQualifier otq1 = new OrTypeQualifier();
		otq1.or("DCH");
		otq1.or("DCV");
		//otq1.or("DH");
		
		OrTypeQualifier otq = new OrTypeQualifier();
		otq.or("PMQH");
		otq.or("PMQV");
		// exclude PMQ here
		NotTypeQualifier ntq = new NotTypeQualifier(otq);
		atq.and(ntq);
		otq1.or(atq);

		List<AcceleratorNode> allMags = seq.getNodesWithQualifier(otq1);
		List<AcceleratorNode> mags = AcceleratorSeq.filterNodesByStatus(
				allMags, true);

		for (int i = 0; i < mags.size(); i++) {
			Electromagnet quad = (Electromagnet) mags.get(i);
			String pvName = "";
			double val = 0.;

			// get polarity so we can determine the sign
			double pol = quad.getPolarity();
			// use field readback
			if (quad.useFieldReadback()) {
				// System.out.println("Quad " + quad.getId() + " use
				// fieldReadback");
				Channel chan = quad.getChannel(Electromagnet.FIELD_RB_HANDLE);
				pvName = chan.channelName();
				if (qPVMap.containsKey(pvName)) {
					val = ((Double) qPVMap.get(pvName)).doubleValue();
					// take into account of proper transform
					val = val*pol;
//					val = chan.getValueTransform().convertFromRaw(
//							ArrayValue.doubleStore(val)).doubleValue();
				}
				// If there is no magnet field readback, use corresponding power
				// supply field readback, instead.
				else {
					Channel chan2 = quad.getMainSupply()
							.getChannel("psFieldRB");
					String pvName2 = chan2.channelName();
					if (qPSPVMap.containsKey(pvName2)) {
						val = ((Double) qPSPVMap.get(pvName2)).doubleValue();
						// take into account of proper transform
						val = val*pol;
//						val = chan2.getValueTransform().convertFromRaw(
//								ArrayValue.doubleStore(val)).doubleValue();
					}
					// if no power supply readback, use power supply fieldSet
					else {
						chan2 = quad.getMainSupply().getChannel("fieldSet");
						pvName2 = chan2.channelName();
						if (qPSPVMap.containsKey(pvName2)) {
							val = ((Double) qPSPVMap.get(pvName2))
									.doubleValue();
							// take into account of proper transform
							val = val*pol;
//							val = chan2.getValueTransform().convertFromRaw(
//									ArrayValue.doubleStore(val)).doubleValue();
						} else
							System.out.println(pvName2 + " has no value");
					}
				}
			}
			// use field set, we need to handle magnets with trim power supplies
			// here. However, if no readback,
			// we have to use field readback
			else {
				// for main power supply
				// System.out.println("Quad " + quad.getId() + " has fieldSet");
				Channel chan = quad.getMainSupply().getChannel(
						MagnetMainSupply.FIELD_SET_HANDLE);
				pvName = chan.channelName();
				if (qPVMap.containsKey(pvName)) {
					val = ((Double) qPVMap.get(pvName)).doubleValue();
					// take into account of proper transform
					val = val*pol;
//					val = chan.getValueTransform().convertFromRaw(
//							ArrayValue.doubleStore(val)).doubleValue();
					// for trim power supply (check if it has trim first)
					if (quad instanceof TrimmedQuadrupole) {
						Channel chan1 = ((TrimmedQuadrupole) quad)
								.getTrimSupply().getChannel(
										MagnetTrimSupply.FIELD_SET_HANDLE);
						String pvName1 = chan1.channelName();
						if (qPVMap.containsKey(pvName1)) {
							double trimVal = Math.abs(((Double) qPVMap
									.get(pvName1)).doubleValue());
							// take into account of proper transform
//							trimVal = trimVal*pol;
							trimVal = chan1.getValueTransform().convertFromRaw(
									ArrayValue.doubleStore(trimVal))
									.doubleValue();

							// handle shunt PS differently
							if (pvName1.indexOf("ShntC") > -1) {
								// for backward compatibility (old data), we
								// take absolute value for the shunt field
								val = val - trimVal;
							} else {
								val = val + trimVal;
							}
						}
					}
				}
				// use readback, if no field settings
				else {
					// System.out.println("Quad " + quad.getId() + "does not
					// have fieldSet, use fieldReadback, instead");
					pvName = quad.getChannel(Electromagnet.FIELD_RB_HANDLE)
							.channelName();
					if (qPVMap.containsKey(pvName)) {
						val = ((Double) qPVMap.get(pvName)).doubleValue();
					}
				}
			}

			scenario.setModelInput(quad,
					ElectromagnetPropertyAccessor.PROPERTY_FIELD, val);
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
