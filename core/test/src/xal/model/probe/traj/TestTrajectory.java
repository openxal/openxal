/**
 * TestTrajectory.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 25, 2014
 */
package xal.model.probe.traj;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.IComponent;
import xal.model.Lattice;
import xal.model.ModelException;
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

/**
 * Class of test cases for class <code>{@link Trajectory}</code>.
 *
 * @author Christopher K. Allen
 * @since  Aug 25, 2014
 */
public class TestTrajectory {

    
    /*
     * Global Constants
     */
    
    /** Accelerator sequence used for testing */
    public static final String     STR_ACCL_SEQ_ID = "HEBT1";
    
    
    /** Bending Dipole ID */
    public static final String      STR_DH1_ID = "HEBT_Mag:DH11";
    
    /** Bending Dipole ID */
    public static final String      STR_DH2_ID = "HEBT_Mag:DH12";
    
    
    /*
     * Global Resources
     */
    
    /** Accelerator hardware under test */
    private static final Accelerator    ACCEL_TEST = ResourceManager.getTestAccelerator();
    
    /** Accelerator sequence under test */
    private static final AcceleratorSeq SEQ_TEST = ACCEL_TEST.getSequence(STR_ACCL_SEQ_ID);

    
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
        try {
            MODEL_TEST = Scenario.newScenarioFor(SEQ_TEST);
            
            IAlgorithm      algor = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
            PROBE_ENV = ProbeFactory.getEnvelopeProbe(SEQ_TEST, algor);
            PROBE_ENV.initialize();

            algor = AlgorithmFactory.createParticleTracker(SEQ_TEST);
            PROBE_PARTC = ProbeFactory.createParticleProbe(SEQ_TEST, algor);
            PROBE_PARTC.initialize();

            algor = AlgorithmFactory.createTransferMapTracker(SEQ_TEST);
            PROBE_XFER = ProbeFactory.getTransferMapProbe(SEQ_TEST, algor);
            PROBE_ENV.initialize();
            
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
        System.out.println();
        System.out.println("ELEMENTS contained in MODEL");
        while (itrCmps.hasNext()) {
            IComponent cmp = itrCmps.next();
            System.out.println("  " + index + " " + cmp.getId());
            index++;
        }
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
        @SuppressWarnings("unchecked")
        Trajectory<TransferMapState>    trjXfer = (Trajectory<TransferMapState>)this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.getStatesViaIndexer();
        System.out.println();
        System.out.println("STATES retrieved by the INDEXER");
        int index = 0;
        for (TransferMapState state : lstStates) {
            System.out.println("  " + index + " " + state.getElementId() + " at position " + state.getPosition());
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
        @SuppressWarnings("unchecked")
        Trajectory<TransferMapState>    trjXfer = (Trajectory<TransferMapState>)this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.getStatesViaStateMap();
        System.out.println();
        System.out.println("STATES retrieved by the STATE MAP");
        int index = 0;
        for (TransferMapState state : lstStates) {
            System.out.println("  " + index + " " + state.getElementId() + " at position " + state.getPosition());
            index++;
        }
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#stateAtPosition(double)}.
     */
    @Test
    public final void testStateAtPosition() {
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#stateForElement(java.lang.String)}.
     */
    @Test
    public final void testStateForElement() {
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement_OLD(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement_OLD() {
        @SuppressWarnings("unchecked")
        Trajectory<TransferMapState>    trjXfer = (Trajectory<TransferMapState>)this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.statesForElement(STR_DH1_ID);
        System.out.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : lstStates) {
            System.out.println("  " + state.getElementId() + " at position " + state.getPosition());
        }
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public final void testStatesForElement() {
        @SuppressWarnings("unchecked")
        Trajectory<TransferMapState>    trjXfer = (Trajectory<TransferMapState>)this.runModel(PROBE_XFER);
        
        List<TransferMapState>     lstStates = trjXfer.statesForElement(STR_DH1_ID);
        System.out.println("STATES for " + STR_DH1_ID);
        for (TransferMapState state : lstStates) {
            System.out.println("  " + state.getElementId() + " at position " + state.getPosition());
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
    private Trajectory<?>   runModel(Probe<?> prbTest) {
        
        try {
            prbTest.reset();
            MODEL_TEST.setProbe( prbTest );
            MODEL_TEST.resync();
            MODEL_TEST.run();
            
            Trajectory<?>   trjTest = MODEL_TEST.getTrajectory();
            
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
