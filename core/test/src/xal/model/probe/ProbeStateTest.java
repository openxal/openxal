package xal.model.probe;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.ens.Ensemble;
import xal.model.probe.DiagnosticProbe;
import xal.model.probe.EnsembleProbe;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.traj.DiagnosticProbeState;
import xal.model.probe.traj.EnsembleProbeState;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests each of the ProbeState and Trajectory subclasses.  Verifies not just
 * properties of concrete Probe implementations, but abstract and intermediate
 * super classes too.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class ProbeStateTest extends TestCase {
	
	private static double INITIAL_POSITION = 0.1;
//	private static double CHARGE = 42.0;
	private static double CURRENT = 0.42;
    private static double FREQUENCY = 420.0e6;

	/**
	 * Test driver.
	 *
	 * @param args command line arguments
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public static void main(String[] args) {
		TestRunner.run (suite());
	}
	
	/**
	 * Creates a JUnit <code>Test</code> object from
	 * this class type.
	 *
	 * @return <code>TestSuite</code> object encapsulating this class type
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public static Test suite() {
		return new TestSuite(ProbeStateTest.class);
	}
	
	/**
	 * Tests the diagnostic probe state object.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public void testDiagnosticProbeState() {
		
		// create a probe and set some state, capture state in snapshot
		DiagnosticProbe probe = new DiagnosticProbe();
		probe.setPosition(INITIAL_POSITION);
		probe.incrementElementsVisited();
		DiagnosticProbeState state = probe.cloneCurrentProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof DiagnosticProbeState);
		assertTrue(probe.getPosition() == 
			state.getPosition());
		assertTrue(probe.getElementsVisited() == 
			state.getElementsVisited());
		
		// save the state to a trajectory	
		Trajectory<DiagnosticProbeState> trajectory = probe.getTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue((trajectory.getStateClass()).equals(DiagnosticProbeState.class));
	}
	
    /**
     * Tests the particle probe state object.
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2011
     */
	public void testParticleProbeState() {
		
		// create a probe and set some state, capture state in snapshot
		ParticleProbe probe = new ParticleProbe();
		probe.setPosition(INITIAL_POSITION);
		probe.setPhaseCoordinates(PhaseVector.newZero());
		ParticleProbeState state = probe.cloneCurrentProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof ParticleProbeState);
		assertTrue(probe.getPosition() == 
			state.getPosition());
		assertTrue(probe.getPhaseCoordinates().isEquivalentTo(state.getPhaseCoordinates()));
		
		// save the state to a trajectory	
		Trajectory<ParticleProbeState> trajectory = probe.getTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue((trajectory.getStateClass()).equals(ParticleProbeState.class));
	}

    /**
     * Tests the envelope probe state object.
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2011
     */
    // disable this test case since there is a class cast exception (see below) -tap
	public void disabledTestEnvelopeProbeState() {
		
		// create a probe and set some state, capture state in snapshot
		EnvelopeProbe probe = new EnvelopeProbe();
		probe.setPosition(INITIAL_POSITION);
//		probe.setBeamCharge(CHARGE);
        probe.setBunchFrequency(FREQUENCY);
		probe.setBeamCurrent(CURRENT);
		probe.setCovariance((CovarianceMatrix)PhaseMatrix.zero());     // causes class cast exception
		EnvelopeProbeState state = probe.cloneCurrentProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof EnvelopeProbeState);
		assertTrue(probe.getPosition() == 
			state.getPosition());
		assertTrue(probe.bunchCharge() == 
			state.bunchCharge());
        assertTrue(probe.getBunchFrequency() == 
            state.getBunchFrequency());
		assertTrue(probe.getBeamCurrent() == 
			state.getBeamCurrent());
		assertTrue(probe.getCovariance().equals(state.getCovarianceMatrix()));
		
		// save the state to a trajectory	
		Trajectory<EnvelopeProbeState> trajectory = probe.getTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue((trajectory.getStateClass()).equals(EnvelopeProbeState.class));
	}

    /**
     * Tests the ensemble probe state object.
     * <br/>
     * <br/>
     * Note, however, that the <code>EnsembleProbe</code>
     *  class is not implemented.
     *
     * @author Christopher K. Allen
     * @since  Apr 15, 2011
     */
	public void testEnsembleProbeState() {
		
		// create a probe and set some state, capture state in snapshot
		EnsembleProbe probe = new EnsembleProbe();
		probe.setPosition(INITIAL_POSITION);
//		probe.setBeamCharge(CHARGE);
        probe.setBunchFrequency(FREQUENCY);
		probe.setBeamCurrent(CURRENT);
		probe.setEnsemble(new Ensemble());
		probe.setFieldCalculation(EnsembleProbe.FLDCALC_NONE);
		EnsembleProbeState state = probe.cloneCurrentProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof EnsembleProbeState);
		assertTrue(probe.getPosition() == 
			state.getPosition());
		assertTrue(probe.bunchCharge() == 
			state.bunchCharge());
        assertTrue(probe.getBunchFrequency() == 
            state.getBunchFrequency());
		assertTrue(probe.getBeamCurrent() == 
			state.getBeamCurrent());
//		assertTrue(probe.getEnsemble().equals(((EnsembleProbeState)state).getEnsemble()));
		assertTrue(probe.getFieldCalculation() == 
			state.getFieldCalculation());
		
		// save the state to a trajectory	
		Trajectory<EnsembleProbeState> trajectory = probe.getTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue((trajectory.getStateClass()).equals(EnsembleProbeState.class));
		
	}

}
