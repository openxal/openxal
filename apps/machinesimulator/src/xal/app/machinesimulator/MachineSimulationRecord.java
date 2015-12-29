//
// MachineSimulationRecord.java
//
//
// Created by Tom Pelaia on 9/19/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.app.machinesimulator;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.calc.SimResultsAdaptor;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;


/** MachineSimulation is the simulation result for a simulation on an accelerator sequence  */
public class MachineSimulationRecord implements DataListener {
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "MachineSimulationRecord"; 
	
	/** probe state wrapped by this record */
	private final ProbeState<?> PROBE_STATE;

	/** twiss parameters */
	private final Twiss[] TWISS_PARAMETERS;

	/** betatron phase */
	private final R3 BETATRON_PHASE;
	
	/**phase vector*/
	private final PhaseVector PHASE_VECTOR;


	/** Constructor */
    public MachineSimulationRecord( final SimResultsAdaptor resultsAdaptor, final ProbeState<?> probeState ) {
		PROBE_STATE = probeState;		
		TWISS_PARAMETERS = resultsAdaptor.computeTwissParameters( probeState );
		BETATRON_PHASE = resultsAdaptor.computeBetatronPhase( probeState );
		PHASE_VECTOR = resultsAdaptor.computeFixedOrbit( probeState );
    }
    
    /**Constructor with adaptor*/
    public MachineSimulationRecord ( final DataAdaptor adaptor ) {
    	PROBE_STATE = readFrom( adaptor ); 	
    	TWISS_PARAMETERS = new Twiss[3];
    	update(adaptor);
    	DataAdaptor betatronPhaseAdaptor = adaptor.childAdaptor("betatronPhase");
    	BETATRON_PHASE = new R3( betatronPhaseAdaptor.stringValue( "values" ) );
    	DataAdaptor phaseVecAdaptor = adaptor.childAdaptor("phaseVector");
    	PHASE_VECTOR = new PhaseVector( phaseVecAdaptor.stringValue( "values" ) );
    }
    
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * an instance of the appropriate ProbeState species.
       * 
     * @param container <code>DataAdaptor</code> to read a Trajectory from
     * @return a ProbeStete<?> for the contents of the DataAdaptor
     */
    private ProbeState<?> readFrom( final DataAdaptor adaptor ) {
    	DataAdaptor dapProb = adaptor.childAdaptor( ProbeState.STATE_LABEL );
   	
    	String type = dapProb.stringValue("type");
    	ProbeState<?> probeState = null;
    	try {
			Class<?> probeStateClass = Class.forName(type);
			try {
				probeState = (ProbeState<?>) probeStateClass.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	probeState.load( dapProb );
    	
    	return probeState;
    	
    }


	/** get the wrapped probe state */
	public ProbeState<?> getProbeState() {
		return PROBE_STATE;
	}


	/** Get the state's element ID */
	public String getElementID() {
		return PROBE_STATE.getElementId();
	}


	/** Get the state's beamline position */
	public double getPosition() {
		return PROBE_STATE.getPosition();
	}
	
	/**Get the position coordinates*/
	public R3 getPosCoordinates() {
		return PHASE_VECTOR.getPosition();
	}


	/** get the state's twiss parameters */
	public Twiss[] getTwissParameters() {
		return TWISS_PARAMETERS;
	}


	/** get the state's betatron phase */
	public R3 getBetatronPhase() {
		return BETATRON_PHASE;
	}
	
	/** provides the name used to identify the class in an external data source. */
	public String dataLabel() {
		return DATA_LABEL;
	}

	/** Instructs the receiver to update its data based on the given adaptor. */
	public void update(DataAdaptor adaptor) {
		DataAdaptor twissAdaptor = adaptor.childAdaptor( "twissParameter" );
		DataAdaptor twissXAdaptor = twissAdaptor.childAdaptor( "X" );
		TWISS_PARAMETERS[0] = new Twiss( twissXAdaptor.doubleValue("alpha"), twissXAdaptor.doubleValue("beta"), twissXAdaptor.doubleValue("emittance") );
		DataAdaptor twissYAdaptor = twissAdaptor.childAdaptor( "Y" );
		TWISS_PARAMETERS[1] = new Twiss( twissYAdaptor.doubleValue("alpha"), twissYAdaptor.doubleValue("beta"), twissYAdaptor.doubleValue("emittance") );
		DataAdaptor twissZAdaptor = twissAdaptor.childAdaptor( "Z" );
		TWISS_PARAMETERS[2] = new Twiss( twissZAdaptor.doubleValue("alpha"), twissZAdaptor.doubleValue("beta"), twissZAdaptor.doubleValue("emittance") );
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write(DataAdaptor adaptor) {
		PROBE_STATE.save(adaptor);
		DataAdaptor phaseVecAdaptor = adaptor.createChild("phaseVector");
		PHASE_VECTOR.save(phaseVecAdaptor);
		DataAdaptor betatronPhaseAdaptor = adaptor.createChild("betatronPhase");
		BETATRON_PHASE.save(betatronPhaseAdaptor);
		DataAdaptor twissAdaptor = adaptor.createChild("twissParameter");
		DataAdaptor twissXAdaptor = twissAdaptor.createChild( "X" );
		twissXAdaptor.setValue( "emittance", TWISS_PARAMETERS[0].getEmittance() );
		twissXAdaptor.setValue( "beta", TWISS_PARAMETERS[0].getBeta() );
		twissXAdaptor.setValue( "alpha", TWISS_PARAMETERS[0].getAlpha() );
		DataAdaptor twissYAdaptor = twissAdaptor.createChild( "Y" );
		twissYAdaptor.setValue( "emittance", TWISS_PARAMETERS[1].getEmittance() );
		twissYAdaptor.setValue( "beta", TWISS_PARAMETERS[1].getBeta() );
		twissYAdaptor.setValue( "alpha", TWISS_PARAMETERS[1].getAlpha() );
		DataAdaptor twissZAdaptor = twissAdaptor.createChild( "Z" );
		twissZAdaptor.setValue( "emittance", TWISS_PARAMETERS[2].getEmittance() );
		twissZAdaptor.setValue( "beta", TWISS_PARAMETERS[2].getBeta() );
		twissZAdaptor.setValue( "alpha", TWISS_PARAMETERS[2].getAlpha() );
	}
}
