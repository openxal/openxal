/**
 * TestTrajectory.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 25, 2014
 */
package xal.model.probe.traj;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.IComponent;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.elem.Element;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;
import xal.tools.beam.PhaseVector;

/**
 * <p>
 * Class of test cases for class <code>{@link Trajectory}</code>.
 * <p/>
 * <p>
 * Use Java virtual machine command line switch
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <tt>java -agentlib:hprof=cpu=times</tt>
 * <br/>
 * <br/>
 * to create <code>java.hprof.TMP</code> files for profiling.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Aug 25, 2014
 */
public class TestTrajectory {

    
    /*
     * Global Constants
     */
    
    /** Flag used for indicating whether to type out to stout or file */
    private static final boolean        BOL_TYPE_STOUT = false;
    
    /** Output file name */
    static final private String         STR_FILENAME_OUTPUT = TestTrajectory.class.getName() + ".txt";

    
    /** Accelerator sequence used for testing */
    public static final String     STR_ACCL_SEQ_ID = "HEBT2";
//    public static final String     STR_ACCL_SEQ_ID = "SCLMed";
    
    
    /** Bending Dipole ID */
    public static final String      STR_DH1_ID = "HEBT_Mag:DH11";
    
    /** Bending Dipole ID */
    public static final String      STR_DH2_ID = "HEBT_Mag:DH12";
    
    
    /*
     * Global Resources
     */
    
    /** Accelerator hardware under test */
    private static Accelerator    ACCEL_TEST;
    
    /** Accelerator sequence under test */
    private static AcceleratorSeq SEQ_TEST;

    
    /** The results output file stream */
    static private PrintStream        PSTR_OUTPUT;

    
    /*
     * Global Attributes
     */

    /** The online model scenario for the given accelerator sequence */
    private static Scenario         MODEL_TEST;
    
    /** Envelope probe used for simulations */
    private static EnvelopeProbe    PROBE_ENV;
    
    /** Particle probe used for simulations */
    private static ParticleProbe    PROBE_PARTC;
    
    /** Transfer map probe used for simulations */
    private static TransferMapProbe PROBE_XFER;
    
    
    
    
    /*
     * Global Methods
     */
    
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Aug 25, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        if (BOL_TYPE_STOUT) {
            PSTR_OUTPUT = System.out;
            
        } else {
            
            File fileOutput = ResourceManager.getOutputFile(TestTrajectory.class, STR_FILENAME_OUTPUT);
            
            PSTR_OUTPUT = new PrintStream(fileOutput);
        }
        
        try {
//            ACCEL_TEST = XMLDataManager.loadDefaultAccelerator();
            ACCEL_TEST = ResourceManager.getTestAccelerator();
            SEQ_TEST   = ACCEL_TEST.getSequence(STR_ACCL_SEQ_ID);
            MODEL_TEST = Scenario.newScenarioFor(SEQ_TEST);
            
            IAlgorithm      algor = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
            PROBE_ENV = ProbeFactory.getEnvelopeProbe(SEQ_TEST, algor);
            PROBE_ENV.initialize();

            algor = AlgorithmFactory.createParticleTracker(SEQ_TEST);
            PROBE_PARTC = ProbeFactory.createParticleProbe(SEQ_TEST, algor);
            PROBE_PARTC.initialize();

            algor = AlgorithmFactory.createTransferMapTracker(SEQ_TEST);
            PROBE_XFER = ProbeFactory.getTransferMapProbe(SEQ_TEST, algor);
            PROBE_XFER.initialize();
            
        } catch (ModelException | InstantiationException e) {

            fail("Unable to create Scenario");
            e.printStackTrace();
        }
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Aug 25, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }
    
    
    /*
     * Test Cases
     */
    
    /**
     * Prints out all the element in the online model.
     *
     * @throws ModelException 

     * @author Christopher K. Allen
     * @since  Aug 26, 2014
     */
    @Test
    public final void TestModel() throws ModelException {
        Lattice              latTest = MODEL_TEST.getLattice();
        Iterator<IComponent> itrCmps = latTest.globalIterator();
        
        int index = 0;
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("ELEMENTS contained in MODEL");
        while (itrCmps.hasNext()) {
            IComponent cmp = itrCmps.next();
            if (cmp instanceof Element)
                PSTR_OUTPUT.println("  " + index + " " + (Element)cmp);
            else
                PSTR_OUTPUT.println("  " + index + " " + cmp.getId());
            index++;
        }
    }
 
    /**
     * Iterates through all the states in the trajectory using a
     * for each construct.
     * 
     * @author Christopher K. Allen
     * @since  Sep 5, 2014
     */
    @Test
    public final void testStateIterator() {
        Trajectory<ParticleProbeState>  trjPartc = this.runModel(PROBE_PARTC);
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES retrieved iteratation using the Iterable<> interface");
        int index = 0;
        for (ParticleProbeState state : trjPartc) {
            PSTR_OUTPUT.println("  " + index 
                    + " " + state.getElementId()
                    + " from " + state.getHardwareNodeId()
                    + " at position " + state.getPosition()
                    );
            index++;
        }
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#stateForElement(java.lang.String)}.
     */
    @Test
    public final void testStateForElement() {
        Trajectory<TransferMapState>    trjXfer = this.runModel(PROBE_XFER);
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("SINGLE STATE for " + STR_DH1_ID);
        TransferMapState state1 = trjXfer.stateForElement(STR_DH1_ID);
        PSTR_OUTPUT.println("  " + state1.getElementId() + " at position " + state1.getPosition());
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("SINGLE STATE for " + STR_DH2_ID);
        TransferMapState state2 = trjXfer.stateForElement(STR_DH2_ID);
        PSTR_OUTPUT.println("  " + state2.getElementId() + " at position " + state2.getPosition());
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement() {
        Trajectory<TransferMapState>    trjXfer = this.runModel(PROBE_XFER);
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : trjXfer.statesForElement(STR_DH1_ID)) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES for " + STR_DH2_ID);
        for (TransferMapState state : trjXfer.statesForElement(STR_DH2_ID)) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
        
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#stateAtPosition(double)}.
     */
    @Test
    public final void testStateAtPosition() {
    }

    /**
     * Prints out all the states in the trajectory to standard out as retrieved
     * by the internal numeric indexer.
     *
     * @author Christopher K. Allen
     * @since  Aug 26, 2014
     */
    @Test
    public final void testGetStateViaIndexer() {
        Trajectory<TransferMapState>    trjXfer = this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.getStatesViaIndexer();
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES retrieved by the INDEXER");
        int index = 0;
        for (TransferMapState state : lstStates) {
            PSTR_OUTPUT.println("  " + index 
                             + " " + state.getElementId()
                             + " from " + state.getHardwareNodeId()
                             + " at position " + state.getPosition()
                             );
            index++;
        }
    }
    
    /**
     * Prints out all the states in the trajectory to standard out as retrieved
     * by the node ID to state map.
     *
     * @author Christopher K. Allen
     * @since  Aug 26, 2014
     */
    @Test
    public final void testGetStateViaMap() {
        Trajectory<TransferMapState>    trjXfer = this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.getStatesViaStateMap();
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES retrieved by the STATE MAP");
        int index = 0;
        for (TransferMapState state : lstStates) {
            PSTR_OUTPUT.println("  " + index + " " + state.getElementId() + " at position " + state.getPosition());
            index++;
        }
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement_OLD() {
        Trajectory<TransferMapState>    trjXfer = this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.statesForElement(STR_DH1_ID);
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : lstStates) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
    }
    
    /**
     * Test the sub-trajectories methods of the <code>Trajectory</code> class.
     *
     * @author Christopher K. Allen
     * @since  Nov 17, 2014
     */
    @Test
    public final void testSubTrajectory() {
        Trajectory<TransferMapState>    trjXfer  = this.runModel(PROBE_XFER);
        
        Trajectory<TransferMapState>    trjSubEx = trjXfer.subTrajectory(STR_DH1_ID, STR_DH2_ID);
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("SUBTRAJECTORY (EXCLUSIVE): STATES between " + STR_DH1_ID + " and " + STR_DH2_ID);
        for (TransferMapState state : trjSubEx) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition());
        }

        Trajectory<TransferMapState>    trjSubIn = trjXfer.subTrajectoryInclusive(STR_DH1_ID, STR_DH2_ID);
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("SUBTRAJECTORY (INCLUSIVE): STATES between " + STR_DH1_ID + " and " + STR_DH2_ID);
        for (TransferMapState state : trjSubIn) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testParticleProbe() {
        PROBE_PARTC.setPhaseCoordinates(new PhaseVector(0.001, 0, 0, 0, 0, 0) );
        PROBE_PARTC.initialize();
        
        Trajectory<ParticleProbeState>    trjPartc = this.runModel(PROBE_PARTC);
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("PARTICLE PROBE STATES");
        for (ParticleProbeState state : trjPartc.getStatesViaIndexer()) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition() + ": z = " + state.getPhaseCoordinates());
        }
        
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testEnvelopeProbe() {
//        PROBE_ENV.setPhaseCoordinates(new PhaseVector(0.001, 0, 0, 0, 0, 0) );
//        PROBE_ENV.initialize();
        
        Trajectory<EnvelopeProbeState>    trjEnv = this.runModel(PROBE_ENV);
        
        PSTR_OUTPUT.println();
        PSTR_OUTPUT.println("ENVELOPE PROBE STATES");
        for (EnvelopeProbeState state : trjEnv.getStatesViaIndexer()) {
            PSTR_OUTPUT.println("  " + state.getElementId() + " at position " + state.getPosition() + ": sigma = " + state.getCovarianceMatrix());
        }
        
    }
    
    
    /*
     * Support Methods
     */

    /**
     * Runs the global online model for the testing class on the
     * given probe object.  The results are returned in an untyped
     * <code>Trajectory<?></code> object. 
     *
     * @param prbTest   The probe to be simulated
     * 
     * @return          simulation data for the given probe
     *
     * @author Christopher K. Allen
     * @since  Aug 25, 2014
     */
    private <S extends ProbeState<S>> Trajectory<S>   runModel(Probe<S> prbTest) {
        
        try {
            prbTest.reset();
            MODEL_TEST.setProbe( prbTest );
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
            Trajectory<S>   trjTest = MODEL_TEST.getTrajectory();
            
            return trjTest;
            
        } catch (SynchronizationException e) {
            e.printStackTrace();
            fail("Unable to synchronize model values");
            
        } catch (ModelException e) {
            e.printStackTrace();
            fail("Error running the online model");
        }
        
        return null;
    }
}
