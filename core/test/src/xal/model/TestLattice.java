/**
 * TestLattice.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 25, 2014
 */
package xal.model;

import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.extension.widgets.olmplot.GraphFrame;
import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.olmplot.ParticleCurve;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.model.elem.Element;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.traj.ParticleProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseVector;

/**
 * Class of test cases for class <code>{@link Trajectory}</code>.
 *
 * @author Christopher K. Allen
 * @since  Aug 25, 2014
 */
public class TestLattice {

    
    /*
     * Global Constants
     */
    
    /** Accelerator sequence used for testing */
//    public static final String     STR_ACCL_SEQ_ID = "HEBT2";
//    public static final String     STR_ACCL_SEQ_ID = "SCLMed";
    public static final String     STR_ACCL_SEQ_ID = "CCL1";
    
    /** Flag for making plots of the simulation */
    public static final boolean BOL_MAKE_PLOTS  = true;    
    
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
            ACCEL_TEST = XMLDataManager.loadDefaultAccelerator();
            SEQ_TEST   = ACCEL_TEST.getSequence(STR_ACCL_SEQ_ID);
            MODEL_TEST = Scenario.newScenarioFor(SEQ_TEST);
            MODEL_TEST.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            
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
    public final void testModel() throws ModelException {
        Lattice              latTest = MODEL_TEST.getLattice();
        Iterator<IComponent> itrCmps = latTest.globalIterator();
        
        int index = 0;
        System.out.println();
        System.out.println("ELEMENTS contained in MODEL");
        while (itrCmps.hasNext()) {
            IComponent cmp = itrCmps.next();
            if (cmp instanceof Element)
                System.out.println("  " + index + " " + (Element)cmp);
            else
                System.out.println("  " + index + " " + cmp.getId());
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
    public final void testSimulation() {
        Trajectory<ParticleProbeState>  trjPartc = this.runModel(PROBE_PARTC);
        
        System.out.println();
        System.out.println("PARTICLE PROBE STATES retrieved iteratation using the Iterable<> interface");
        int index = 0;
        for (ParticleProbeState state : trjPartc) {
            System.out.println("  " + index 
                    + " " + state.getElementId()
                    + " from " + state.getHardwareNodeId() );
            System.out.println("    position  " + state.getPosition());
            System.out.println("    energy    " + state.getKineticEnergy());
            System.out.println("    phase     " + (180.0/Math.PI)*state.getLongitudinalPhase());
            System.out.println("    phase|360 " + (180.0/Math.PI) *Math.IEEEremainder(state.getLongitudinalPhase(), 2.0*Math.PI) );
            index++;
        }
    }

    /**
     * Make plots of the design and production particle trajectories for
     * all the phase planes.
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    @SuppressWarnings("unused")
    @Test
    public final void testPlotDesignAndProduction() {
        if (BOL_MAKE_PLOTS == false)
            return;
        
        
        Trajectory<ParticleProbeState>  trjDsgn = runModel(PROBE_PARTC);
    
        for (PLANE plane : PLANE.values()) {
            final ParticleCurve crvSim = new ParticleCurve(plane, trjDsgn);
    
            FunctionGraphsJPanel pltPlane = new FunctionGraphsJPanel();
            pltPlane.addGraphData(crvSim);
            pltPlane.setLegendVisible(true);
            pltPlane.setPreferredSize(new Dimension(1000,750));
            
            
            final GraphFrame  frmPlotTraj = new GraphFrame("Particle Trajectory for Plane " + plane.name(), pltPlane);
            frmPlotTraj.display();
        }
    
        while (true);
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

        prbTest.reset();
        
        if (prbTest instanceof ParticleProbe) {
            
            PhaseVector     vecInit = new PhaseVector(0.001, 0.0,  0.0, 0.010,  0.0, 0.0);
            ((ParticleProbe)prbTest).setPhaseCoordinates(vecInit);
        }
        
        try {
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
