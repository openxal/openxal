/**
 * TestTwissProbe.java
 * 
 * Created      : December, 2006
 * Author       : Christopher K. Allen
 */
package xal.model.probe;


import junit.framework.JUnit4TestAdapter;

import org.junit.Before;
import org.junit.Test;

import xal.model.alg.TwissTracker;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.tools.beam.Twiss3D;
import xal.tools.math.r3.R3;


/**
 * Class for performing JUnit 4.x test on the <code>TwissProbe</code> class
 * in the <code>xal.model.probe</code> package.
 * 
 * @author Christopher K. Allen
 *
 */
public class TestTwissProbe {

    
    
    
    /*
     * Global Constants
     */
    
    
    //
    // File Names
    //
    
    /** archive save file */
    private static final String STR_FILE_SAVE = "./build/tests/xal/model/probe/TwissProbe_SaveTest.xml";
    
    /** archive load file */
    private static final String STR_FILE_LOAD = "./common/core/test/resources/xal/model/simdb-LI_MEBT1-twissprobe-pmq.probe";
    
    /** archive save/load file */
    private static final String STR_FILE_SAVELOAD = "./build/tests/xal/model/probe/TwissProbe_SaveLoadTest.xml";

    
    //
    // Numerical - Algorithm Parameters
    //
    
//    /** solution precision */
//    private static final double     ERROR_TOLERANCE = 1.0e-5;
    
    /** testing step length */
    private static final double     ALG_STEPSIZE = 0.01;
    
    /** emittance growth flag */
    private static final boolean    ALG_EMITGROWTH = true;
    
    /** debug mode flag */
    private static final boolean    ALG_DEBUGMODE = false;
    
    
    
//    //
//    //  Relativistic Parameters
//    //
//    
//    /** relativistic parameter for stationary beam */
//    private static final double      GAMMA_STAT = 1.0;
//    
//    /** relativistic parameter for MEBT */
//    private static final double      GAMMA_MEBT = 1.003193863;
//    
//    /** relativistic parameter for L3BT */
//    private static final double      GAMMA_L3BT = 1.192678;
    
    
    
    //
    // Beam Parameters
    //

    /** comment string  */
    private static final String     PROBE_COMMENT = "TestTwissProbe";

    /** starting position */
    private static final double     PROBE_S = 0.15;
    
    /** beam energy */
    private static final double     PROBE_W = 2.5e6;

    /** Bunch frequency */
    private static final double     BUNCH_FREQ = 3.24e8;
    
    /** beam current */
    private static final double     BUNCH_CURRENT = 0.025;

    
    
    /** beam centroid offset */
    private static final PhaseVector STATE_CENTROID = new PhaseVector(0.0, 0.0, 0.001, -0.010, 0.0, 0.0);
    
    /** rotation angle for ellipsoid */
    private static final double      STATE_ANGLE = 30.0*(Math.PI/180.0);
    
    
    
    
    /** JPARC MEBT x-plane Twiss parameters */
    private static final Twiss       TWISS_X = new Twiss(-1.2187, 0.13174, 3.1309642E-6);
    
    /** JPARC MEBT y-plane Twiss parameters */
    private static final Twiss       TWISS_Y = new Twiss(2.1885, 0.22344, 2.5075842000000002E-6);
    
    /** JPARC MEBT z-plane Twiss parameters */
    private static final Twiss       TWISS_Z = new Twiss(0.08, 0.7819530229746938, 3.106895634426948E-6);
    
    
    

    
    
    
    /*
     * Local Attributes
     */
    
    
    
    /*
     * Global Methods
     */

    
    /**
     * Return a JUnit 3.x version <code>TestBeamEllipsoid</code> instance that encapsulates 
     * this test suite.  This is a convenience method for attaching to old JUnit testing
     * frameworks, for example, using Eclipse.
     * 
     * @return  a JUnit 3.8 type test object adaptor
     */
    public static junit.framework.Test  getJUnitTest()  {
        return new JUnit4TestAdapter(TestTwissProbe.class);
    }
    
    /**
     * Create a <code>TwissProbe</code> object with the above class constant 
     * parameters.
     * 
     * @return  new <code>TwissProbe</code> instance 
     */
    public static TwissProbe    createTestProbe() {

        // Create the algorithm instance and initialize it
        TwissTracker    alg = new TwissTracker();
        alg.setStepSize(TestTwissProbe.ALG_STEPSIZE);
        alg.setEmittanceGrowth(TestTwissProbe.ALG_EMITGROWTH);
        alg.setDebugMode(TestTwissProbe.ALG_DEBUGMODE);
        
        // Create the probe instance and initialize it
        TwissProbe  probe   = new TwissProbe();

        probe.setAlgorithm( alg );
        
        probe.setComment(TestTwissProbe.PROBE_COMMENT);
        probe.setPosition(TestTwissProbe.PROBE_S);
        probe.setKineticEnergy(TestTwissProbe.PROBE_W);
        probe.setBunchFrequency(TestTwissProbe.BUNCH_FREQ);
        probe.setBeamCurrent(TestTwissProbe.BUNCH_CURRENT);
        probe.setBetatronPhase(new R3(0, 0, TestTwissProbe.STATE_ANGLE) );
        probe.setCentroid( TestTwissProbe.STATE_CENTROID );
        probe.setTwiss( new Twiss3D(TWISS_X, TWISS_Y, TWISS_Z) );
        
        return probe;
    }
    
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     *  Create a new <code>TestR3x3JacobiDecomposition</code> class for 
     *  JUnit 4.x testing of the <code>TableSchema</code> class. 
     */
    public TestTwissProbe() {
        super();
    }
    
    
    /**
     * Setup the test fixture by creating a the test matrices.
     */
    @Before public void setup() {
    }
    

    
    
    /*
     * Tests
     */
    
    /**
     * Test the ability of a <code>TwissProbe</code> to store itself.  
     */
    @Test   public void testArchiveSave()  {
//        
//        TwissProbe probe = TestTwissProbe.createTestProbe();
//        
//        try {
//            ProbeXmlWriter.writeXml(probe, TestTwissProbe.STR_FILE_SAVE);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            Assert.fail("TestTwissProbe#testArchiveSave() - unable to save probe to " + STR_FILE_SAVE);
//            return;
//            
//        }
    }
    
    /**
     * Test the ability to recover a <code>TwissProbe</code> object from
     * a data store.
     */
    @Test   public void testArchiveLoad()   {
//        
//        try {
//            Probe probe = ProbeXmlParser.parse(TestTwissProbe.STR_FILE_LOAD);
//            Assert.assertTrue(probe instanceof TwissProbe);
//            
//        } catch (ParsingException e) {
//            e.printStackTrace();
//            Assert.fail("TestTwissProbe#testArchiveLoad() - unable to parse file " + STR_FILE_LOAD);
//            return;
//            
//        }
    }
    
    /**
     * Test the ability of a <code>TwissProbe</code> to save itself then recover its state
     * from the file, i.e., data persistence.
     */
    @Test   public void testArchiveSaveRestore()    {
//        
//        TwissProbe probeInit = TestTwissProbe.createTestProbe();
//        TwissProbe probeRest;
//
//        // Save probe to file then recover it
//        try {
//            ProbeXmlWriter.writeXml(probeInit, TestTwissProbe.STR_FILE_SAVELOAD);
//
//            probeRest = (TwissProbe)ProbeXmlParser.parse(TestTwissProbe.STR_FILE_SAVELOAD);
//            
//        } catch (IOException e) {
//            e.printStackTrace();
//            Assert.fail("TestTwissProbe#testArchiveSaveRestore() - unable to save probe to " + STR_FILE_SAVELOAD);
//            return;
//
//        } catch (ParsingException e) {
//            e.printStackTrace();
//            Assert.fail("TestTwissProbe#testArchiveSaveRestore() - unable to parse file " + STR_FILE_SAVELOAD);
//            return;
//            
//        }
//        
//        // Check values of the algorithm
//        IAlgorithm alg = probeRest.getAlgorithm();
//        if (!(alg instanceof TwissTracker) )    {
//            Assert.fail("TestTestProbe#testArchiveSaveRestore() - algorithm type failure " + alg.getType());
//            return;
//        }
//        TwissTracker    algTwiss = (TwissTracker)alg;
//        Assert.assertEquals(TestTwissProbe.ALG_STEPSIZE, algTwiss.getStepSize(), 0.0);
//        Assert.assertEquals(TestTwissProbe.ALG_DEBUGMODE, algTwiss.getDebugMode());
//        Assert.assertEquals(TestTwissProbe.ALG_EMITGROWTH, algTwiss.getEmittanceGrowthFlag());
//        
//        // Check the values of the probe
//        Assert.assertEquals(TestTwissProbe.PROBE_S, probeRest.getPosition(), 0.0);
//        Assert.assertEquals(TestTwissProbe.PROBE_W, probeRest.getKineticEnergy(), 0.0);
//        Assert.assertEquals(TestTwissProbe.BUNCH_CURRENT, probeRest.getBeamCurrent(), 0.0);
//        Assert.assertEquals(TestTwissProbe.BUNCH_FREQ, probeRest.getBunchFrequency(), 0.0);
//        Assert.assertEquals(TestTwissProbe.STATE_ANGLE, probeRest.getBetatronPhase().getz(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_X.getAlpha(), probeRest.getTwiss(IND_3D.X).getAlpha(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_X.getBeta(), probeRest.getTwiss(IND_3D.X).getBeta(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_X.getEmittance(), probeRest.getTwiss(IND_3D.X).getEmittance(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Y.getAlpha(), probeRest.getTwiss(IND_3D.Y).getAlpha(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Y.getBeta(), probeRest.getTwiss(IND_3D.Y).getBeta(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Y.getEmittance(), probeRest.getTwiss(IND_3D.Y).getEmittance(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Z.getAlpha(), probeRest.getTwiss(IND_3D.Z).getAlpha(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Z.getBeta(), probeRest.getTwiss(IND_3D.Z).getBeta(), 0.0);
//        Assert.assertEquals(TestTwissProbe.TWISS_Z.getEmittance(), probeRest.getTwiss(IND_3D.Z).getEmittance(), 0.0);
    }
    
    
}
    
