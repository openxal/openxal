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
import xal.model.probe.TwissProbe;
import xal.model.probe.traj.BunchProbeState;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.data.SortOrdering;
import xal.tools.math.r3.R3;



/**
 * ProbeFactory is a factory for generating probes.
 * 
 * TODO 
 *    <br> &middot; Let's do the proper type of the probe species on the methods
 *    <br> &middot; Perhaps we could rename the <code>get</code>-prefixed methods to
 *                   a more conventional naming (e.g., <code>create</code>-)
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
	
	/** table name for phase coordinates */
	protected final static String PHASECOORD_TABLE = "PhaseCoordinates";
	
    /** table name for phase coordinates */
    protected final static String CENTRCOORD_TABLE = "CentroidCoordinates";
    
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
	
	/** XML attribute name for the phase coordinates themselves */
	protected final static String PHASECOORD_VALUE_PARAM = "coordinates";
	
    


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
		
		// Set the initial phase coordinates if they are defined in the model.params file
		initializeCoordinates(probe, locationID, sequence);
		
		// Initialize the location parameters.  If this fails we have to quit
		boolean success = initializeLocation( probe, locationID, sequence);
		
		// initialize the probe so the initial state is set
	    probe.initialize();
		
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
		
		// initialize the probe so the initial state is set
	    probe.initialize();
		
		return success ? probe : null;
	}
	
	/**
     * Create and initialize a new <code>TwissProbe</code> object with the default parameters
     * in the <tt>model.params</tt> file.  The parameters are taken for the entrance location 
     * of the provided accelerator hardware sequence.  The
     * given algorithm object is also verified and attached to the probe.
     *   
     * @param seqParent    accelerator sequence containing the location
     * @param algDynamics  algorithm used for the simulation 
     * 
     * @return             new, initialized <code>TwissProbe</code> object 
	 *
	 * @author Christopher K. Allen
	 * @since  Sep 6, 2014
	 */
	public static TwissProbe getTwissProbe( final AcceleratorSeq seqParent, final IAlgorithm algDynamics ) {
	    return ProbeFactory.getTwissProbe( seqParent.getEntranceID(), seqParent, algDynamics);
	}
	
	/**
	 * Create and initialize a new <code>TwissProbe</code> object with the default parameters
	 * in the <tt>model.params</tt> file.  The parameters are taken for the location 
	 * of the provided location ID along the given accelerator hardware sequence.  The
	 * given algorithm object is also verified and attached to the probe.  
	 * 
	 * @param strLocId     location of the accelerator where the model parameters are taken
	 * @param seqParent    accelerator sequence containing the location
	 * @param algDynamics  algorithm used for the simulation 
	 * 
	 * @return             new, initialized <code>TwissProbe</code> object 
	 *
	 * @author Christopher K. Allen
	 * @since  Nov 5, 2013
	 */
	public static TwissProbe getTwissProbe( final String strLocId, final AcceleratorSeq seqParent, final IAlgorithm algDynamics) {
	    TwissProbe     prbTwiss = new TwissProbe();
	    
	    if ( !prbTwiss.setAlgorithm(algDynamics) )
	        return null;
	    
	    boolean bolResult = ProbeFactory.initializeLocation(prbTwiss, strLocId, seqParent);
	    bolResult &= ProbeFactory.initializeBeam(prbTwiss, seqParent);
	    bolResult &= ProbeFactory.initializeTwiss(prbTwiss, strLocId, seqParent);
	 
	    // initialize the probe so the initial state is set
	    prbTwiss.initialize();
	    
	    return bolResult ? prbTwiss : null;
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
		
		// initialize the probe so the initial state is set
		probe.initialize();
		
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
	private static boolean initializeLocation( final Probe<?> probe, final String locationID, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
//		System.out.println("editContext = "+editContext);
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
		final String name = speciesRecord.stringValueForKey(SPECIES_NAME_PARAM);

		probe.setSpeciesName(name);
		probe.setSpeciesRestEnergy( mass );
		probe.setSpeciesCharge( charge );
		probe.setKineticEnergy( kineticEnergy );
		probe.setCurrentElement( currentElement );
		probe.setTime( time );
		probe.setPosition( position );

		return true;
	}
	
	/**
	 * Retrieves the phase coordinates from the model.params file for the given
	 * location ID and uses them to set the initial phase coordinates of the
	 * given particle probe.  This is basically an optional operation; if the
	 * phase coordinates table is missing, or the entry for the location ID is
	 * missing then the operation simply quits and the probe is left unaltered.
	 * Thus, it is always safe to call this methods.
	 * 
	 * @param probe        probe whose phase coordinates are to be initialized 
	 * @param strLocId     record name (i.e. "accelerator position") containing phase coordinates
	 * @param smfSeq       the accelerator sequence being modeled
	 *
	 * @author Christopher K. Allen
	 * @since  Sep 6, 2014
	 */
	private static void initializeCoordinates(final ParticleProbe probe, final String strLocId, final AcceleratorSeq smfSeq ) {
	    // Get the edit context and look for the phase coordinates table
        final EditContext editContext = smfSeq.getAccelerator().editContext();
        final DataTable tblPhsCoords  = editContext.getTable( PHASECOORD_TABLE );
        if (tblPhsCoords == null)
            return;
        
        // If the table is there look for a record with the given location ID
        final GenericRecord recCoords = tblPhsCoords.record( "name", strLocId );
        if ( recCoords == null ) {
            return;
        }
        
        //

        // Get the phase coordinate string from the record and create a new
        //  phase vector object to be the initial phase coordinates of the probe
        String  strPhsCoord = recCoords.stringValueForKey( PHASECOORD_VALUE_PARAM );
	    PhaseVector    vecCoords = PhaseVector.parse(strPhsCoord);
	    
	    probe.setPhaseCoordinates(vecCoords);
	}
	
	
	/**
	 * Initialize the beam parameters of the probe with the specified sequence and algorithm.
	 *
	 * @param probe      the probe to initialize
	 * @param sequence   the sequence for which to initialize the probe
	 * @return           true for successful initialization and false if it fails
	 */
	private static boolean initializeBeam( final BunchProbe<? extends BunchProbeState<?>> probe, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
		final DataTable beamTable = editContext.getTable( BEAM_TABLE );
		
		final GenericRecord beamRecord = beamTable.record( "name", "default" );
        final double bunchFreq = beamRecord.doubleValueForKey( "bunchFreq" );
		final double beamCurrent = beamRecord.doubleValueForKey( "current" );
				
        probe.setBunchFrequency( bunchFreq );
		probe.setBeamCurrent( beamCurrent );
//        probe.setBetatronPhase( new R3( phase ) );
		
		return true;
	}
	
	/**
     * Initialize the twiss parameters of the probe with Twiss parameters taken from the
     * <tt>model.params</tt> file.
     * 
	 * @param prbTwiss     probe to initialize
	 * @param strLocId     location of the accelerator where Twiss parameters are taken
	 * @param seqLoc       hardware accelerator sequence
	 * 
	 * @return             <code>true</code> always
	 *
	 * @author Christopher K. Allen
	 * @since  Nov 5, 2013
	 */
	private static boolean initializeTwiss(final TwissProbe prbTwiss, final String strLocId, final AcceleratorSeq seqLoc) {
        final EditContext   edcData = seqLoc.getAccelerator().editContext();

        // Extract the betratron phase and set it
        final DataTable     tblBeam = edcData.getTable(BEAM_TABLE);
	    final GenericRecord recBeam = tblBeam.record("name", "default");   
        final String        strPhs  = recBeam.stringValueForKey( "phase" );
        final R3            vecPhs  = new R3(strPhs);
        prbTwiss.setBetatronPhase(vecPhs);
        
        // Extract the Twiss parameters and set them
        Twiss[] arrTwiss = getTwissArray( strLocId, edcData );
        prbTwiss.setTwiss( new Twiss3D(arrTwiss) );
        
        return true;
	}
	
	/**
	 * Initialize the covariance matrix of the given probe using the Twiss parameters in the
	 * <tt>model.params</tt> file.  
	 *
	 * @param probe      the probe to initialize
     * @param locationID location within the acceleration where the probe is initialized
	 * @param sequence   the sequence for which to initialize the probe
	 * 
	 * @return           true for successful initialization and false if it fails
	 */
	private static boolean initializeTwiss( final EnvelopeProbe probe, final String locationID, final AcceleratorSeq sequence) {
		final EditContext editContext = sequence.getAccelerator().editContext();
//		final DataTable twissTable = editContext.getTable( "twiss" );
//		
//		final Map<String, String> bindings = new HashMap<String, String>();
//		bindings.put( "name", locationID );
//		
//		bindings.put( "coordinate", "x" );
//		final GenericRecord twissX = twissTable.record( bindings );
//		bindings.put( "coordinate", "y" );
//		final GenericRecord twissY = twissTable.record( bindings );
//		bindings.put( "coordinate", "z" );
//		final GenericRecord twissZ = twissTable.record( bindings );
//		
//		final Twiss[] twissVector = new Twiss[] { getTwiss( twissX ), getTwiss( twissY ), getTwiss( twissZ ) };

		final Twiss[] twissVector = getTwissArray( locationID, editContext );
		final PhaseVector vecCent = getCentroidLocation( locationID, editContext );
		
		CovarianceMatrix  matCov;
		if (vecCent != null) 
		    matCov = CovarianceMatrix.buildCovariance(twissVector[0], twissVector[1], twissVector[2], vecCent);
		else
            matCov = CovarianceMatrix.buildCovariance(twissVector[0], twissVector[1], twissVector[2]);
		    
		probe.setCovariance(matCov);
		
		return true;
	}
	
	/**
	 * Parses the Twiss parameters for the given accelerator location ID from the
	 * data within the given <code>EditContext</code> object.
	 * 
	 * @param strLocId     location where the Twiss parameters are taken
	 * @param edcData      the data store containing all the Twiss data
	 * 
	 * @return             Twiss parameters in all three planes for the given location 
	 *
	 * @author Christopher K. Allen
	 * @since  Nov 5, 2013
	 */
	private static Twiss[] getTwissArray( final String strLocId, final EditContext edcData) {
        final DataTable twissTable = edcData.getTable( "twiss" );
        
        final Map<String, String> bindings = new HashMap<String, String>();
        bindings.put( "name", strLocId );
        
        bindings.put( "coordinate", "x" );
        final GenericRecord twissX = twissTable.record( bindings );
        bindings.put( "coordinate", "y" );
        final GenericRecord twissY = twissTable.record( bindings );
        bindings.put( "coordinate", "z" );
        final GenericRecord twissZ = twissTable.record( bindings );
        
        final Twiss[] arrTwiss = new Twiss[] { getTwiss( twissX ), getTwiss( twissY ), getTwiss( twissZ ) };
        
        return arrTwiss;
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
	
    /**
     * Retrieves the phase vector representing the (envelope) centroid location
     * from the given edit context.  If there is no such entry then the
     * <code>null</code> value is returned.
     *  
     * @param strLocId      record name in the centroid location table
     * @param editContext   edit context containing centroid location table
     * 
     * @return              phase vector representing the envelope centroid location
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2014
     */
    private static PhaseVector getCentroidLocation( final String strLocId, final EditContext editContext) {

        // Look for the centroid coordinates table
        final DataTable tblCentCoords = editContext.getTable( CENTRCOORD_TABLE );
        if (tblCentCoords == null)
            return null;
        
        // If the table is there look for a record with the given location ID
        final GenericRecord recCoords = tblCentCoords.record( "name", strLocId );
        if ( recCoords == null ) {
            return null;
        }

        // Get the phase coordinate string from the record and create a new
        //  phase vector object to be the initial centroid coordinates of the probe
        String       strPhsCoord = recCoords.stringValueForKey( PHASECOORD_VALUE_PARAM );
        PhaseVector    vecCoords = PhaseVector.parse(strPhsCoord);
        
        return vecCoords;
    }
    
	
}

