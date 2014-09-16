/**
 * TestParticleProbeTrajectory.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 12, 2014
 */
package xal.model.probe.traj;

import static org.junit.Assert.fail;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.extension.widgets.olmplot.GraphFrame;
import xal.extension.widgets.olmplot.PLANE;
import xal.extension.widgets.olmplot.ParticleCurve;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.model.alg.ParticleTracker;
import xal.model.probe.ParticleProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.ResourceManager;
import xal.tools.beam.PhaseVector;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Sep 12, 2014
 */
public class TestParticleProbeTrajectory {

    /*
     * Global Constants
     */
    
    
    /** Flag used for indicating whether to type out to stout or file */
    private static final boolean        BOL_TYPE_STOUT = true;
    
    /** Flag used for running tests involving live accelerator */
    private static final boolean        BOL_MAKE_PLOTS = false;
    
    /** Flag used for comparing the design and production trajectories (otherwise just compute design) */
    private static final boolean        BOL_COMPARE = false;
    

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_DSGN = "/site/optics/design/main.xal";

    /** Location of the design accelerator configuration */
    static final private String         STR_CFGFILE_PROD = "/site/optics/production/main.xal";
    
    
    /** Location of the output file */
    static final private String         STR_FILENAME_OUTPUT = "ParticleTrajOutput.txt";

   
    
    /** The sequence we are testing in both accelerator configurations */
    static final private String         STR_ID_TESTSEQ = "SCLMed";

    
    /*
     * Global Attributes
     */
    
    /** The design Accelerator under test */
    static private Accelerator          ACCEL_DSGN;
    
    /** The production Accelerator under test */
    static private Accelerator          ACCEL_PROD;
    
    
    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_PROD;

    /** The design Accelerator Sequence under test */
    static private AcceleratorSeq     SEQ_DSGN;
    
    
    /** The online model of the design accelerator sequence */
    static private Scenario           MOD_DSGN;

    /** The online model of the production accelerator sequence */
    static private Scenario           MOD_PROD;
    
    
    /** The results output file stream */
    static private PrintStream        PRN_OUTPUT;


    /*
     * Global Methods
     */
    
    /**
     * Loads an SMF accelerator object given the path relative to the
     * Open XAL project home (i.e., OPENXAL_HOME).
     * 
     * @param strPathRel    relative path to the accelerator configuration file
     * 
     * @return              SMF accelerator object loaded from the given path
     *
     * @author Christopher K. Allen
     * @since  Sep 8, 2014
     */
    private static Accelerator loadAccelerator(String ...arrPathRel) {
        if (arrPathRel.length == 0)
            return XMLDataManager.loadDefaultAccelerator();
        String  strPathRel = arrPathRel[0];
        String  strPathXal = ResourceManager.getProjectHomePath();
        String  strFileAccel = strPathXal + strPathRel;
        
        Accelerator accel = XMLDataManager.acceleratorWithPath(strFileAccel);
        return accel;
    }
    
    /**
     * Creates a new output file in the testing output directory with the 
     * given file name.
     * 
     * @param strFileName   name of the output file
     * 
     * @return              new output file object
     *
     * @author Christopher K. Allen
     * @since  Sep 11, 2014
     */
    private static File createOutputFile(String strFileName) {
        String  strPack     = TestParticleProbeTrajectory.class.getPackage().getName();
        String  strPathRel  = strPack.replace('.', '/');
        String  strPathFile = strPathRel + '/' + strFileName; 
        File    fileOutput  = xal.test.ResourceManager.getOutputFile(strPathFile);
        
        return fileOutput;
    }
    
    /**
     * Creates new particle probes, loads them into the class's online models and
     * runs the models.
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    private static void runModels() {
        
        try {
            ParticleTracker algDsgn = AlgorithmFactory.createParticleTracker(SEQ_DSGN);
            ParticleProbe   prbDsgn = ProbeFactory.createParticleProbe(SEQ_DSGN, algDsgn);
            PhaseVector     vecDsgn = new PhaseVector(0.001, 0.0,  0.0, 0.010,  0.0, 0.0);
            
            prbDsgn.setPhaseCoordinates(vecDsgn);
            MOD_DSGN.setProbe(prbDsgn);
            MOD_DSGN.run();
            
            ParticleTracker algProd = AlgorithmFactory.createParticleTracker(SEQ_PROD);
            ParticleProbe   prbProd = ProbeFactory.createParticleProbe(SEQ_PROD, algProd);
            PhaseVector     vecProd = new PhaseVector(0.001, 0.0,  0.0, 0.010,  0.0, 0.0);
            
            prbProd.setPhaseCoordinates(vecProd);
            MOD_PROD.setProbe(prbProd);
            MOD_PROD.run();
            
        } catch (Exception e) {
            e.printStackTrace();
            
            fail("Unable to run model");
        }
    }
    
//    /**
//     * Creates a thread for the given frame to display itself then
//     * spawns the thread
//     * 
//     * @param frm   graphics frame to be given its own thread and launched
//     *
//     * @author Christopher K. Allen
//     * @since  Sep 12, 2014
//     */
//    @SuppressWarnings("unused")
//    private static void spawnGraphFrame(final GraphFrame frm) {
//        
//        /**
//         * Create an interface that displays the given frame. 
//         *
//         * @see java.lang.Runnable#run()
//         *
//         * @author Christopher K. Allen
//         * @since  Sep 12, 2014
//         */
//        Runnable lmbFrame = new Runnable() {
//
//            @Override
//            public void run() {
//                frm.display();
//            }
//        };
//        
//        Thread  thrFrame = new Thread(lmbFrame);
//        thrFrame.start();
//    }
    
    
    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        if (BOL_COMPARE) {
            ACCEL_DSGN = loadAccelerator(STR_CFGFILE_DSGN);
            ACCEL_PROD = loadAccelerator(STR_CFGFILE_PROD);
        } else {
            ACCEL_DSGN = loadAccelerator();
            ACCEL_PROD = loadAccelerator();
        }

        SEQ_DSGN = ACCEL_DSGN.getSequence(STR_ID_TESTSEQ);
        SEQ_PROD = ACCEL_PROD.getSequence(STR_ID_TESTSEQ);
        
        MOD_DSGN = Scenario.newScenarioFor(SEQ_DSGN);
        MOD_DSGN.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        MOD_DSGN.resync();
        
        MOD_PROD = Scenario.newScenarioFor(SEQ_PROD);
        MOD_PROD.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
        MOD_PROD.resync();
        
        if (BOL_TYPE_STOUT) {
            PRN_OUTPUT = System.out;
        } else {
            File       fileOut = createOutputFile(STR_FILENAME_OUTPUT);
            
            PRN_OUTPUT = new PrintStream(fileOut);
        }
    }

    /**
     * @throws java.lang.Exception
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (!BOL_TYPE_STOUT) {
            PRN_OUTPUT.close();
        }
    }

    /**
     * Make plots of the design and production particle trajectories for
     * all the phase planes.
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    @Test
    public final void testPlotDesignAndProduction() {
        if (BOL_MAKE_PLOTS == false)
            return;
        
        runModels();
        
        Trajectory<ParticleProbeState>  trjDsgn = MOD_DSGN.getTrajectory();
        Trajectory<ParticleProbeState>  trjProd = MOD_PROD.getTrajectory();

        for (PLANE plane : PLANE.values()) {
            final ParticleCurve crvDsgn = new ParticleCurve(plane, trjDsgn);
            final ParticleCurve crvProd = new ParticleCurve(plane, trjProd);

            FunctionGraphsJPanel pltPlane = new FunctionGraphsJPanel();
            pltPlane.addGraphData(crvDsgn);
            pltPlane.addGraphData(crvProd);
            pltPlane.setLegendVisible(true);
            pltPlane.setPreferredSize(new Dimension(850,650));
            
            
            final GraphFrame  frmPlotTraj = new GraphFrame("Particle Trajectory for Plane " + plane.name(), pltPlane);
            frmPlotTraj.display();
        }
       
        while (true);
    }

    /**
     * Save the results of the of the design and production particle 
     * trajectory simulations to disk;
     *
     * @author Christopher K. Allen
     * @since  Sep 12, 2014
     */
    @Test
    public final void testSaveDesignAndProduction() {
        try {
            runModels();
            
            Trajectory<ParticleProbeState>  trjDsgn = MOD_DSGN.getTrajectory();
            this.writeTrajectory("DESIGN TRAJECTORY", trjDsgn);
            
            Trajectory<ParticleProbeState>  trjProd = MOD_PROD.getTrajectory();
            this.writeTrajectory("PRODUCTION TRACTORY", trjProd);

        } catch (Exception e) {
            
            e.printStackTrace();
            fail("Unable to write out trajectory");
        }
    }
    
    /*
     * Support Methods
     */
    
    /**
     * Writes out the contains of the given trajectory to the output
     * file.
     * 
     * @param trj   trajectory object to be saved
     * 
     * @throws IOException      It didn't work 
     *
     * @author Christopher K. Allen
     * @since  Sep 15, 2014
     */
    private void writeTrajectory(String strTitle, Trajectory<ParticleProbeState> trj) throws IOException {

        PrintStream     prs = PRN_OUTPUT;
        
        prs.println(strTitle);
        Iterator<ParticleProbeState>    itr = trj.iterator();
        while (itr.hasNext()) {
            ParticleProbeState  state     = itr.next();
            String              strElemId = state.getElementId();
            double              dblPos    = state.getPosition();
            PhaseVector         vecState  = state.getPhaseCoordinates();

            String strLine = strElemId + " " + dblPos + " " + vecState.toString();
            prs.println(strLine);
        }
        prs.println();
    }

}
