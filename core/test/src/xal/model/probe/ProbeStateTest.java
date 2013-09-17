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
import xal.model.probe.traj.DiagnosticProbeTrajectory;
import xal.model.probe.traj.EnsembleProbeState;
import xal.model.probe.traj.EnsembleTrajectory;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.EnvelopeTrajectory;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ParticleTrajectory;
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
		ProbeState state = probe.createProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof DiagnosticProbeState);
		assertTrue(probe.getPosition() == 
			((DiagnosticProbeState)state).getPosition());
		assertTrue(probe.getElementsVisited() == 
			((DiagnosticProbeState)state).getElementsVisited());
		
		// save the state to a trajectory	
		Trajectory trajectory = probe.createTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue(trajectory instanceof DiagnosticProbeTrajectory);
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
		probe.setPhaseCoordinates(PhaseVector.zero());
		ProbeState state = probe.createProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof ParticleProbeState);
		assertTrue(probe.getPosition() == 
			((ParticleProbeState)state).getPosition());
		assertTrue(probe.getPhaseCoordinates().equals(((ParticleProbeState)state).getPhaseCoordinates()));
		
		// save the state to a trajectory	
		Trajectory trajectory = probe.createTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue(trajectory instanceof ParticleTrajectory);
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
		probe.setCorrelation((CovarianceMatrix)PhaseMatrix.zero());     // causes class cast exception
		ProbeState state = probe.createProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof EnvelopeProbeState);
		assertTrue(probe.getPosition() == 
			((EnvelopeProbeState)state).getPosition());
		assertTrue(probe.bunchCharge() == 
			((EnvelopeProbeState)state).bunchCharge());
        assertTrue(probe.getBunchFrequency() == 
            ((EnvelopeProbeState)state).getBunchFrequency());
		assertTrue(probe.getBeamCurrent() == 
			((EnvelopeProbeState)state).getBeamCurrent());
		assertTrue(probe.getCovariance().equals(((EnvelopeProbeState)state).getCorrelationMatrix()));
		
		// save the state to a trajectory	
		Trajectory trajectory = probe.createTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue(trajectory instanceof EnvelopeTrajectory);
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
		ProbeState state = probe.createProbeState();
		
		//compare the snapshot to the probe
		assertTrue(state instanceof EnsembleProbeState);
		assertTrue(probe.getPosition() == 
			((EnsembleProbeState)state).getPosition());
		assertTrue(probe.bunchCharge() == 
			((EnsembleProbeState)state).bunchCharge());
        assertTrue(probe.getBunchFrequency() == 
            ((EnsembleProbeState)state).getBunchFrequency());
		assertTrue(probe.getBeamCurrent() == 
			((EnsembleProbeState)state).getBeamCurrent());
//		assertTrue(probe.getEnsemble().equals(((EnsembleProbeState)state).getEnsemble()));
		assertTrue(probe.getFieldCalculation() == 
			((EnsembleProbeState)state).getFieldCalculation());
		
		// save the state to a trajectory	
		Trajectory trajectory = probe.createTrajectory();
		trajectory.saveState(state);
		
		assertTrue(trajectory.stateAtPosition(INITIAL_POSITION) == state);
		assertTrue(trajectory instanceof EnsembleTrajectory);
		
	}

}
