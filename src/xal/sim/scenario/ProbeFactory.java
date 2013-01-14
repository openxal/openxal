/*
 *  ProbeFactory.java
 *
 *  Created on Fri Sep 03 14:53:13 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 *
 */
package xal.sim.scenario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import xal.model.IAlgorithm;
import xal.model.probe.BunchProbe;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.Twiss;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.data.SortOrdering;
import xal.tools.math.r3.R3;



/**
 * ProbeFactory is a factory for generating probes.
 *
 * @author   tap
 * @since    Sep 03, 2004
 */
public class ProbeFactory {
	/** table name for species */
	protected final static String SPECIES_TABLE = "species";
	
	/** table name for the beam parameters */
	protected final static String BEAM_TABLE = "beam";
	
	/** table name for the twiss parameters */
	protected final static String TWISS_TABLE = "twiss";
	
	/** table name for the location records */
	protected final static String LOCATION_TABLE = "location";

	/** parameter name for kinetic energy */
	protected final static String KINETIC_ENERGY_PARAM = "W";

	/** parameter name for species */
	protected final static String SPECIES_PARAM = "species";

	/** parameter name for species name parameter */
	protected final static String SPECIES_NAME_PARAM = "name";

	/** parameter name for charge */
	protected final static String CHARGE_PARAM = "charge";

	/** parameter name for mass */
	protected final static String MASS_PARAM = "mass";


//	/**
//	 * Generate a ParticlePerturb probe initialized with the default entrance parameters for the
//	 * specified sequence.  The location used defaults to the sequence's entrance ID.
//	 *
//	 * @param sequence   the sequence for which to initialize the probe
//	 * @param algorithm  the online model algorithm to use
//	 * @return           the initialized particle perturb probe
//	 */
//    @SuppressWarnings("deprecation")
//	public static ParticlePerturb getParticlePerturb( final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
//		return getParticlePerturb( sequence.getEntranceID(), sequence, algorithm );
//	}
//	
//	
//	/**
//	 * Generate a ParticlePerturb probe initialized with the entrance parameters for the specified location.
//	 *
//	 * @param locationID the location ID of the entrance parameters to use
//	 * @param sequence   the sequence for which to initialize the probe
//	 * @param algorithm  the online model algorithm to use
//	 * @return           the initialized particle perturb probe
//	 */
//	@SuppressWarnings("deprecation")
//	public static ParticlePerturb getParticlePerturb( final String locationID, final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
//		final ParticlePerturb probe = new ParticlePerturb();
//		
//		if ( !probe.setAlgorithm( algorithm ) ) {
//			return null;
//		}
//		
//		boolean success = initializeLocation( probe, locationID, sequence, algorithm );
//		
//		return success ? probe : null;
//	}
	

	/**
	 * Generate a Particle probe initialized with the default entrance parameters for the
	 * specified sequence.  The location used defaults to the sequence's entrance ID.
	 *
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized particle probe
	 */
	public static ParticleProbe createParticleProbe( final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		return getParticleProbe( sequence.getEntranceID(), sequence, algorithm );
	}
	
	
	/**
	 * Generate a Particle probe initialized with the entrance parameters for the specified location.
	 *
	 * @param locationID the location ID of the entrance parameters to use
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized particle probe
	 */
	public static ParticleProbe getParticleProbe( final String locationID, final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		final ParticleProbe probe = new ParticleProbe();
		
		if ( !probe.setAlgorithm( algorithm ) ) {
			return null;
		}
		
		boolean success = initializeLocation( probe, locationID, sequence);
		
		return success ? probe : null;
	}
	
	
	/**
	 * Generate a TransferMap probe initialized with the default entrance parameters for the
	 * specified sequence.  The location used defaults to the sequence's entrance ID.
	 *
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized transfer map probe
	 */
	public static TransferMapProbe getTransferMapProbe( final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		return getTransferMapProbe( sequence.getEntranceID(), sequence, algorithm );
	}
	
	
	/**
	 * Generate a TransferMap probe initialized with the entrance parameters for the specified location.
	 *
	 * @param locationID the location ID of the entrance parameters to use
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized transfer map probe
	 */
	public static TransferMapProbe getTransferMapProbe( final String locationID, final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		final TransferMapProbe probe = new TransferMapProbe();
		
		if ( !probe.setAlgorithm( algorithm ) ) {
			return null;
		}
		
		boolean success = initializeLocation( probe, locationID, sequence);
		
		return success ? probe : null;
	}
	
	
	/**
	 * Generate an Envelope probe initialized with the default entrance parameters for the
	 * specified sequence.  The location used defaults to the sequence's entrance ID.
	 *
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized transfer map probe
	 */
	public static EnvelopeProbe getEnvelopeProbe( final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		return getEnvelopeProbe( sequence.getEntranceID(), sequence, algorithm );
	}
	
	
	/**
	 * Generate an Envelope probe initialized with the entrance parameters for the specified location.
	 *
	 * @param locationID the location ID of the entrance parameters to use
	 * @param sequence   the sequence for which to initialize the probe
	 * @param algorithm  the online model algorithm to use
	 * @return           the initialized transfer map probe
	 */
	public static EnvelopeProbe getEnvelopeProbe( final String locationID, final AcceleratorSeq sequence, final IAlgorithm algorithm ) {
		final EnvelopeProbe probe = new EnvelopeProbe();
		
		if ( !probe.setAlgorithm( algorithm ) ) {
			return null;
		}
		
		boolean success = initializeLocation( probe, sequence.getEntranceID(), sequence);
		success &= initializeBeam( probe, sequence );
		success &= initializeTwiss( probe, locationID, sequence );
		
		return success ? probe : null;
	}
	
	
	/**
	 * Get the list of available location IDs ordered alpha-numerically.
	 * 
	 * @param accelerator  accelerator object to parse
	 *  
	 * @return a list of available location IDs.
	 */
	static public List<String> getLocationIDs( final Accelerator accelerator ) {
		final List<GenericRecord> locationRecords = getLocationRecords( accelerator );
		final List<String> locationIDs = new ArrayList<String>( locationRecords.size() );
		
		final Iterator<GenericRecord> locationIter = locationRecords.iterator();
		while ( locationIter.hasNext() ) {
			final GenericRecord record = locationIter.next();
			locationIDs.add( record.stringValueForKey( "name" ) );
		}
		
		return locationIDs;
	}
	
	
	/**
	 * Get the list of available location records ordered by name.
     * 
     * @param accelerator  accelerator object to parse
     *  
	 * @return a list of available location records.
	 */
	static public List<GenericRecord> getLocationRecords( final Accelerator accelerator ) {
		final EditContext editContext = accelerator.editContext();
		final DataTable locationTable = editContext.getTable( LOCATION_TABLE );
		
		return locationTable.getRecords( new SortOrdering("name") );
	}
	

	/**
	 * Initialize the location parameters of the probe with the specified sequence and algorithm.
	 *
	 * @param probe      the probe to initialize
	 * @param locationID location within the acceleration where the probe is initialized
	 * @param sequence   the sequence for which to initialize the probe
	 * 
	 * @return           true for successful initialization and false if it fails
	 */
	private static boolean initializeLocation( final Probe probe, final String locationID, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
		System.out.println("editContext = "+editContext);
		final DataTable speciesTable = editContext.getTable( SPECIES_TABLE );
		final DataTable locationTable = editContext.getTable( LOCATION_TABLE );
		
		final GenericRecord locationRecord = locationTable.record( "name", locationID );
		if ( locationRecord == null ) {
			return false;
		}

		double kineticEnergy = locationRecord.doubleValueForKey( KINETIC_ENERGY_PARAM );

		final String species = locationRecord.stringValueForKey( SPECIES_PARAM );
		final double position = locationRecord.doubleValueForKey( "s" );
		final double time = locationRecord.doubleValueForKey( "t" );
		final String currentElement = locationRecord.stringValueForKey( "elem" );
		
		final GenericRecord speciesRecord = speciesTable.record( SPECIES_NAME_PARAM, species );
		final double mass = speciesRecord.doubleValueForKey( MASS_PARAM );
		final double charge = speciesRecord.doubleValueForKey( CHARGE_PARAM );

		probe.setSpeciesRestEnergy( mass );
		probe.setSpeciesCharge( charge );
		probe.setKineticEnergy( kineticEnergy );
		probe.setCurrentElement( currentElement );
		probe.setTime( time );
		probe.setPosition( position );

		return true;
	}
	
	
	/**
	 * Initialize the beam parameters of the probe with the specified sequence and algorithm.
	 *
	 * @param probe      the probe to initialize
	 * @param sequence   the sequence for which to initialize the probe
	 * @return           true for successful initialization and false if it fails
	 */
	private static boolean initializeBeam( final BunchProbe probe, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
		final DataTable beamTable = editContext.getTable( BEAM_TABLE );
		
		final GenericRecord beamRecord = beamTable.record( "name", "default" );
        final double bunchFreq = beamRecord.doubleValueForKey( "bunchFreq" );
		final double beamCurrent = beamRecord.doubleValueForKey( "current" );
        final String phase = beamRecord.stringValueForKey( "phase" );
				
        probe.setBunchFrequency( bunchFreq );
		probe.setBeamCurrent( beamCurrent );
        probe.setBetatronPhase( new R3( phase ) );
		
		return true;
	}
	
	
	/**
	 * Initialize the twiss parameters of the probe with the specified sequence and algorithm.
	 *
	 * @param probe      the probe to initialize
     * @param locationID location within the acceleration where the probe is initialized
	 * @param sequence   the sequence for which to initialize the probe
	 * @return           true for successful initialization and false if it fails
	 */
	private static boolean initializeTwiss( final EnvelopeProbe probe, final String locationID, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
		final DataTable twissTable = editContext.getTable( "twiss" );
		
		final Map<String, String> bindings = new HashMap<String, String>();
		bindings.put( "name", locationID );
		
		bindings.put( "coordinate", "x" );
		final GenericRecord twissX = twissTable.record( bindings );
		bindings.put( "coordinate", "y" );
		final GenericRecord twissY = twissTable.record( bindings );
		bindings.put( "coordinate", "z" );
		final GenericRecord twissZ = twissTable.record( bindings );
		
		final Twiss[] twissVector = new Twiss[] { getTwiss( twissX ), getTwiss( twissY ), getTwiss( twissZ ) };
		
		probe.initFromTwiss( twissVector );
		
		return true;
	}
	
	
	/**
	 * Generate a Twiss instance from a record containing alpha, beta and emittance.
	 * 
	 * @param record   data object containing Courant-Snyder parameters 
	 *
	 * @return an instance of Twiss for the alpha, beta and emittance from the twiss record.
	 */
	private static Twiss getTwiss( final GenericRecord record ) {
		final double alpha = record.doubleValueForKey( "alpha" );
		final double beta = record.doubleValueForKey( "beta" );
		final double emittance = record.doubleValueForKey( "emittance" );
		
		return new Twiss( alpha, beta, emittance );
	}
}

