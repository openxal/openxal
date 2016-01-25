package xal.model.probe;


import java.io.File;
import java.io.PrintStream;
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
import xal.test.ResourceManager;
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
	private static String seqID = "DTL";  // To choose a different sequence or combosequence, change the seqID string
	
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
	    File   fileOutput = ResourceManager.getOutputFile(TestModelTTFs.class, TestModelTTFs.class.getName() + ".txt");
	    
		OSTR_TYPEOUT = new PrintStream(fileOutput);
		OSTR_TYPEOUT.println("Launching Model RF Gap TTF tester...");
		ACCL_TEST = ResourceManager.getTestAccelerator();
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

	    // Increase the number of adaptive steps for stiff systems
		ALGORITHM.setMaxIterations(30000);
		
		// Create the simulation probe
        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(SEQ_TEST,ALGORITHM);
        probe.reset();
		
        // Create the simulation scenario, assign the probe, and run
		Scenario model = Scenario.newScenarioFor(SEQ_TEST);
		
		model.setProbe(probe);
		model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		model.resync();
		model.run();

		
		// Extract the simulation data and type out
		Trajectory<EnvelopeProbeState> trjData = probe.getTrajectory();

		for ( EnvelopeProbeState state : trjData ) {
            CovarianceMatrix matCov  = state.getCovarianceMatrix();

            double          dblPos  = state.getPosition();
            double          dblSigX = matCov.getSigmaX();
            double          dblSigY = matCov.getSigmaY();
            double          dblSigZ = matCov.getSigmaZ();
			
            String      strLine = "" + dblPos + '\t' + dblSigX + '\t' + dblSigY + '\t' + dblSigZ;
            
            OSTR_TYPEOUT.println(strLine);
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
