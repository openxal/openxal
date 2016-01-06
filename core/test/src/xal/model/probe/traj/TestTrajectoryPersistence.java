/**
 * TestTrajectoryPersistence.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 29, 2015
 */
package xal.model.probe.traj;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IAlgorithm;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.ParticleTracker;
import xal.model.alg.TransferMapTracker;
import xal.model.alg.TwissTracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.ParticleProbe;
import xal.model.probe.Probe;
import xal.model.probe.TransferMapProbe;
import xal.model.probe.TwissProbe;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.test.ResourceManager;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 29, 2015
 */
public class TestTrajectoryPersistence {
    
    
    /*
     * Global Variables
     */
    
    /** Probe state used in persistence test */
    private static Trajectory<ParticleProbeState>       TRAJ_PART;
    
    /** Probe state used in persistence test */
    private static Trajectory<TransferMapState>         TRAJ_XFER;
    
    /** Probe state used in persistence test */
    private static Trajectory<EnvelopeProbeState>       TRAJ_ENV;
    
    /** Probe state used in persistence test */
    private static Trajectory<TwissProbeState>          TRAJ_TWISS;
    

    
    /** Accelerator sequence used for testing */
    public static final String     STR_ACCL_SEQ_ID = "HEBT2";
//    public static final String     STR_ACCL_SEQ_ID = "SCLMed";
    
    
    
//    /** Output file name */
//    static final private String         STR_FILENAME_OUTPUT = "Trajectory.txt";
//
//    /** The results output file stream */
//    static private PrintStream        PSTR_OUTPUT;

    
    
    /*
     * Global Methods
     */

    /**
     * Pull in the probe state data from the model.params file.  We are going to use
     * just the initial state as the first and only state in the trajectory.
     * 
     * @throws java.lang.Exception
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

//        File fileOutput = ResourceManager.getOutputFile(TestTrajectoryPersistence.class, STR_FILENAME_OUTPUT);
//            
//        PSTR_OUTPUT = new PrintStream(fileOutput);
//        
        try {
            Accelerator    accl  = ResourceManager.getTestAccelerator();
            AcceleratorSeq seq   = accl.getSequence(STR_ACCL_SEQ_ID);
            
            ParticleTracker algPart = AlgorithmFactory.createParticleTracker(seq);
            ParticleProbe   prbPart = ProbeFactory.createParticleProbe(seq, algPart);
            TRAJ_PART = createTrajectory(prbPart);

            TransferMapTracker  algXfer = AlgorithmFactory.createTransferMapTracker(seq);
            TransferMapProbe    prbXfer = ProbeFactory.getTransferMapProbe(seq, algXfer);
            TRAJ_XFER = createTrajectory(prbXfer);
            
            EnvTrackerAdapt algEnv = AlgorithmFactory.createEnvTrackerAdapt(seq);
            EnvelopeProbe   prbEnv = ProbeFactory.getEnvelopeProbe(seq, algEnv);
            TRAJ_ENV = createTrajectory(prbEnv);
            
            TwissTracker    algTws = AlgorithmFactory.createTwissTracker(seq);
            TwissProbe      prbTws = ProbeFactory.getTwissProbe(seq, algTws);
            TRAJ_TWISS = createTrajectory(prbTws);

        } catch (InstantiationException e) {

            e.printStackTrace();
            fail("Unable to create Trajectory - " + e.getMessage());
        }
    }
    
    /**
     * Forces the probe to create a trajectory object (by calling <code>Probe#initialize()</code>) then
     * adds the initial state of the probe to that trajectory object and returns it.
     *     
     * @param probe     probe object used to create the trajectory with one state
     * 
     * @return          trajectory for the given probe consisting of a single state (the initial state of probe)
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    private static <S extends ProbeState<S>> Trajectory<S> createTrajectory(Probe<S> probe) {
        probe.initialize();
        
        Trajectory<S>   traj  = probe.getTrajectory();
        S               state = probe.getInitialState();
        traj.addState(state);
        
        return traj;
    }

    /**
     * @throws java.lang.Exception
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    
    
    /*
     * Test Cases
     */
    
    /**
     *  Save the contained states of each trajectory container to a respective file.  This
     *  Test must pass in order for any other test to be successful. 
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    @Test
    public final void testTrajectorySave() {
        
        this.saveTrajectory("ParticleProbeState.xml", TRAJ_PART);
        this.saveTrajectory("TransferMapState.xml", TRAJ_XFER);
        this.saveTrajectory("EnvelopeProbeState.xml", TRAJ_ENV);
        this.saveTrajectory("TwissProbeState.xml", TRAJ_TWISS);
    }
    
    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testParticleTrajectoryPersistence() {
        String  strFileName = "ParticleTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, TRAJ_PART);
        
        // Recover the test trajectory from file
        Trajectory<ParticleProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<ParticleProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testTransferMapTrajectoryPersistence() {
        String  strFileName = "TransferMapTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, TRAJ_XFER);
        
        // Recover the test trajectory from file
        Trajectory<TransferMapState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<TransferMapState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testEnvelopeProbeTrajectoryPersistence() {
        String  strFileName = "EnvelopeProbeTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, TRAJ_ENV);
        
        // Recover the test trajectory from file
        Trajectory<EnvelopeProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<EnvelopeProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#loadFrom(xal.tools.data.DataAdaptor)}.
     */
    @Test
    public final void testTwissProbeTrajectoryPersistence() {
        String  strFileName = "TwissProbeTrajectory.xml";
        
        // Save the test trajectory
        this.saveTrajectory(strFileName, TRAJ_TWISS);
        
        // Recover the test trajectory from file
        Trajectory<TwissProbeState> trjRes = this.loadTrajectory(strFileName);
        if (trjRes == null)
            fail("Trajectory<TwisProbeState> did not work.");
        
        // Save to new file for comparison
        this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
    }

    @Test
    public final void testSaveRestoreSimulation() {
        try {
            
            // Get the test accelerator and create an online model
            Accelerator    accl  = ResourceManager.getTestAccelerator();
            AcceleratorSeq seq   = accl.getSequence(STR_ACCL_SEQ_ID);
            Scenario       model = Scenario.newScenarioFor(seq);
            
            // Create an envelope probe for simulation
            IAlgorithm      alg  = AlgorithmFactory.createEnvTrackerAdapt(seq);
            EnvelopeProbe   prb  = ProbeFactory.getEnvelopeProbe(seq, alg);
            prb.initialize();

            // Initialize the model and run it
            model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
            model.setProbe( prb );
            model.resync();
            model.run();
            
            // Get the simulation results, save them, then restore them
            String      strFileName = "SimulationTrajectory.xml";
            
            Trajectory<EnvelopeProbeState>   trjSim = model.getTrajectory();
            this.saveTrajectory(strFileName, trjSim);
            Trajectory<EnvelopeProbeState>   trjRes = this.loadTrajectory(strFileName);
            this.saveTrajectory(strFileName.replace(".xml", "Restored.xml"), trjRes);
            
        } catch (ModelException | InstantiationException e) {
            e.printStackTrace();
            fail("Unable to run model and/or store/restore results");
            
        }
        
        
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Saves the given trajectory data to XML data adaptor with the given file name (in the
     * test directory for this class).  A JUnit failure assertion is thrown if any error
     * occurs during the write.
     * 
     * @param strFileName   name of file to be written
     * @param traj          trajectory object that provides data
     * 
     * @return              <code>true</code> if successful, <code>false</code> if an exception occurred
     *
     * @since  Dec 29, 2015,   Christopher K. Allen
     */
    private final <S extends ProbeState<S>> boolean saveTrajectory(String strFileName, Trajectory<S> traj)  {
        try {
            XmlDataAdaptor  daptSink = XmlDataAdaptor.newEmptyDocumentAdaptor();
            
            traj.save(daptSink);
            
            File            fileSink = ResourceManager.getOutputFile(this.getClass(), strFileName);
            daptSink.writeTo(fileSink);
            
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            fail("Unable to write trajectory data for " + strFileName);
            return false;
        }
        
    }
    
    
    /**
     * Creates a new trajectory object by reading in the XML data in the given filename.
     * The data is loaded using an XML data adaptor object.  A JUnit failure is asserted if
     * an error occurs during the read or construction of the new trajectory object.
     *   
     * @param strFileName   name of the file containing the trajectory formatted data
     * 
     * @return              a new trajectory object of the correct type initialized with the data
     *                      in the given file, or <code>null</code> if a failure occurred.
     *
     * @since  Jan 5, 2016,   Christopher K. Allen
     */
    private final <S extends ProbeState<S>> Trajectory<S> loadTrajectory(String strFileName)  {
        try {
            File            fileSrc = ResourceManager.getOutputFile(this.getClass(), strFileName);
            XmlDataAdaptor  daptSrc = XmlDataAdaptor.adaptorForFile(fileSrc, false);
            
            Trajectory<S>   trajSrc = Trajectory.loadFrom(daptSrc);
            
            return trajSrc;
            
        } catch (IllegalArgumentException | ParseException | ResourceNotFoundException | MalformedURLException e) {
            e.printStackTrace();
            fail("Unable to load trajectory data for " + strFileName);
            
            return null;
        }
        
    }
    

}
