package xal.model.probe;


import java.io.PrintStream;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.CovarianceMatrix;

/**
 * The Class TestModelTTFs tests whether or not the individual RF Gap TTFs are loaded into the simulation.
 * @since  July 6, 2015   by James M. Ghawaly Jr.
 */
public class TestModelTTFs {
	/*
     * Global Variables
     */
	/** The seq id. */
	private static String seqID = "MEBT";  // To choose a different sequence or combosequence, change the seqID string
	
	/** The ostr typeout. */
	private static PrintStream        OSTR_TYPEOUT;
	
	/** The accl test. */
	private static Accelerator		  ACCL_TEST;
	
	/** The seq test. */
	private static AcceleratorSeq	  SEQ_TEST;
	
	/** The algorithm. */
	private static EnvTrackerAdapt    ALGORITHM;
	
	/**
	 * Sets up the global variable before testing.
	 *
	 * @throws Exception the exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		OSTR_TYPEOUT = System.out;
		OSTR_TYPEOUT.println("Launching Model RF Gap TTF tester...");
		ACCL_TEST = XMLDataManager.loadDefaultAccelerator();
		SEQ_TEST = ACCL_TEST.findSequence(seqID);
		ALGORITHM = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
	}
	
	/**
	 * Tests whether or not the individual RF Gap TTFs are loaded into the simulation.
	 *
	 * @throws InstantiationException the instantiation exception
	 * @throws ModelException the model exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	
	@Test
	public final void test() throws InstantiationException, ModelException {

		ALGORITHM.setMaxIterations(30000);
		
		Scenario model = Scenario.newScenarioFor(SEQ_TEST);

		
		EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(SEQ_TEST,ALGORITHM);
		
		model.setProbe(probe);
		model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		model.resync();
		model.run();

		
		Probe<?> newProbe = model.getProbe();
		
		Trajectory<?> trajectory = newProbe.getTrajectory();

		Iterator<?> dataFinal = trajectory.iterator();
		OSTR_TYPEOUT.println("Sigma X-------------------------------------------------");
		while ( dataFinal.hasNext()) {
			Object state = dataFinal.next();
			double statePosition = ((EnvelopeProbeState) state).getPosition();
			CovarianceMatrix covMat = ((EnvelopeProbeState) state).getCovarianceMatrix();
			OSTR_TYPEOUT.println(statePosition);
			OSTR_TYPEOUT.println(covMat.getSigmaX());
		}
		Iterator<?> dataFinal2 = trajectory.iterator();
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("Sigma Y-------------------------------------------------");
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("");
		while ( dataFinal2.hasNext()) {
			Object state = dataFinal2.next();
			double statePosition = ((EnvelopeProbeState) state).getPosition();
			CovarianceMatrix covMat = ((EnvelopeProbeState) state).getCovarianceMatrix();
			OSTR_TYPEOUT.println(statePosition);
			OSTR_TYPEOUT.println(covMat.getSigmaY());
		}
		Iterator<?> dataFinal3 = trajectory.iterator();
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("Sigma Z-------------------------------------------------");
		OSTR_TYPEOUT.println("");
		OSTR_TYPEOUT.println("");
		while ( dataFinal3.hasNext()) {
			Object state = dataFinal3.next();
			double statePosition = ((EnvelopeProbeState) state).getPosition();
			CovarianceMatrix covMat = ((EnvelopeProbeState) state).getCovarianceMatrix();
			OSTR_TYPEOUT.println(statePosition);
			OSTR_TYPEOUT.println(covMat.getSigmaZ());
		}
	}
	

	/**
	 * Tear down after class.
	 *
	 * @throws Exception the exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	@AfterClass
    public static void tearDownAfterClass() throws Exception {
		OSTR_TYPEOUT.println("Test Complete");
    }

}
