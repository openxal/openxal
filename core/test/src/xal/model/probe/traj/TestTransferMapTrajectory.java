/**
 * TestTransferMapTrajectory.java
 *
 * Author  : Christopher K. Allen
 * Since   : Apr 3, 2014
 */
package xal.model.probe.traj;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.IComponent;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.alg.TransferMapTracker;
import xal.model.probe.TransferMapProbe;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.test.ResourceManager;
import xal.test.ResourceTools;
import xal.tools.beam.PhaseMatrix;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Apr 3, 2014
 */
public class TestTransferMapTrajectory {

    /*
     * Global Constants
     */
    
    /** Flag used for indicating whether to type out to stout or file */
    private static final boolean        BOL_TYPE_STOUT = false;
    

    /*
     * Global Attributes
     */
    
    /** The results output file stream */
    static private PrintStream        PSTR_OUTPUT;

    
    private static Accelerator     ACCEL;
    
    private static Scenario         MODEL;
    
    private static TransferMapProbe     PROBE;
    
    private static Trajectory<TransferMapState>    TRAJ;
    
    
    @BeforeClass
    public static void SetupClass() throws ModelException {
        
        if (BOL_TYPE_STOUT) 
            PSTR_OUTPUT = System.out;
        else
            PSTR_OUTPUT = ResourceTools.createOutputStream(TestTrajectory.class);

//        ACCEL  = XMLDataManager.loadDefaultAccelerator();
        ACCEL  = ResourceManager.getTestAccelerator();

        ArrayList<AcceleratorSeq> lst = new ArrayList<AcceleratorSeq>();
        AcceleratorSeq hebt1 = ACCEL.getSequence("HEBT1");
        AcceleratorSeq hebt2 = ACCEL.getSequence("HEBT2");

        lst.add(hebt1);
        lst.add(hebt2);

        //                AcceleratorSeqCombo seq = new AcceleratorSeqCombo("LINAC", lst);
        AcceleratorSeq seq = ACCEL.getSequence("SCLMed");

        //                MODEL = Scenario.newScenarioFor(hebt1)
        MODEL = Scenario.newScenarioFor(seq);
        MODEL.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

        TransferMapTracker ptracker = new TransferMapTracker();
        PROBE = ProbeFactory.getTransferMapProbe(seq, ptracker);
        //                PROBE = ProbeFactory.getTransferMapProbe(hebt1, ptracker)

        //                print "Probe update policy = ", ptracker.getProbeUpdatePolicy()
        //                ptracker.setProbeUpdatePolicy(Tracker.UPDATE_ENTRANCE)
        //                print "New probe update policy = ", ptracker.getProbeUpdatePolicy()

        MODEL.setProbe(PROBE);
        MODEL.resync();
        MODEL.run();

        TRAJ = MODEL.getTrajectory();
    }

    
    @AfterClass
    public static void TeardownClass() {
        
    }
    
    
    
    @Test
    public void printElementsInLattice() {
        PSTR_OUTPUT.print("\n\nELEMENTS IN LATTICE\n");
        int cnt = 0;
        Lattice latModel  = MODEL.getLattice();
        Iterator<?>  iter = latModel.globalIterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            IComponent elem = (IComponent)obj;
            PSTR_OUTPUT.println(cnt + "   " + elem.getId() );
            cnt = cnt + 1;
        }

    }
    
    @Test
    public void printElementsInTrajectory() {
        PSTR_OUTPUT.print("\n\nSTATES BY ELEMENT IN TRAJECTORY\n");
        int cnt = 0;
        Iterator<TransferMapState>  iter = TRAJ.iterator();
        while (iter.hasNext()) {
            TransferMapState state = iter.next();
            
            String  strElemId = state.getElementId();
            double  dblPos    = state.getPosition();
            double  dblKin    = state.getKineticEnergy();
                    
            PSTR_OUTPUT.println(cnt + "   " + strElemId + "   " + dblPos + "   " + dblKin);
            cnt = cnt + 1;
        }

    }
    

    /**
     * Test method for {@link xal.model.probe.traj.TransferMapTrajectory#getTransferMatrix(java.lang.String, java.lang.String)}.
     */
    @Test
    public void testGetTransferMatrix() {
//        CalculationsOnMachines  prcTran = new CalculationsOnMachines(TRAJ);
        
        PSTR_OUTPUT.print("\n\nSTATE-BY-STATE TRANSFER MATRICES IN TRAJECTORY\n");
        int cnt    = 0;
        TransferMapState state1 = TRAJ.initialState();
        Iterator<TransferMapState> iter = TRAJ.iterator();
        while ( iter.hasNext() ) {
            TransferMapState state2 = iter.next(); 
            String strId1 = state1.getElementId();
            String strId2 = state2.getElementId();
//            PhaseMatrix matXfer = TRAJ.getTransferMatrix(strId1, strId2);
//            PhaseMatrix matXfer = TRAJ.getTransferMatrix(state1, state2);
            PhaseMatrix matXfer1 = state1.getTransferMap().getFirstOrder();
            PhaseMatrix matXfer2 = state2.getTransferMap().getFirstOrder();
            PhaseMatrix matXfer  = matXfer2.times( matXfer1.inverse() );
            
            PSTR_OUTPUT.println(cnt + "     " + strId1 + " to " + strId2 + "     " + matXfer.toStringMatrix() );
            
            cnt = cnt + 1;
            state1 = state2;
        }
    }

    @Test 
    public void testGetFullTrajectoryTransferMatrix() {
        PSTR_OUTPUT.print("\n\nENTRANCE-TO-ELEMENT TRANSFER MATRICES IN TRAJECTORY\n");
        int cnt    = 0;
        Iterator<TransferMapState> iter =  TRAJ.iterator();
        while ( iter.hasNext() ) {
            TransferMapState state = iter.next(); 
            String strId1 = state.getElementId();
//            PhaseMatrix matXfer = TRAJ.getTransferMatrix(strId1, strId2);
            PhaseMatrix matXfer = state.getTransferMap().getFirstOrder();
            
            PSTR_OUTPUT.println(cnt + "     " + strId1 + "     " + matXfer.toStringMatrix() );
            
            cnt = cnt + 1;
        }
    }

    
//    /**
//     * Test method for {@link xal.model.probe.traj.TransferMapTrajectory#getTransferMatrix(java.lang.String, java.lang.String)}.
//     */
//    @Test
//    public void testGetTransferMatrixEntrToEntr() {
//        PSTR_OUTPUT.print("\n\nENTR-TO-ENTR STATE-BY-STATE TRANSFER MATRICES IN TRAJECTORY\n");
//        int cnt    = 0;
//        TransferMapState state1 = (TransferMapState) TRAJ.initialState();
//        Iterator<TransferMapState> iter = (Iterator<TransferMapState>) TRAJ.stateIterator();
//        while ( iter.hasNext() ) {
//            TransferMapState state2 = iter.next(); 
//            String strId1 = state1.getElementId();
//            String strId2 = state2.getElementId();
//            PhaseMatrix matXfer = TRAJ.getTransferMatrixEntrToEntr(strId1, strId2);
////            PhaseMatrix matXfer = TRAJ.getTransferMatrix(state1, state2);
//            
//            PSTR_OUTPUT.println(cnt + "     " + strId1 + " to " + strId2 + "     " + matXfer.toStringMatrix() );
//            
//            cnt = cnt + 1;
//            state1 = state2;
//        }
//    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#stateForElement(java.lang.String)}.
     */
    @Test
    public void testStateForElement() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement(java.lang.String)}.
     */
    @Test
    public void testStatesForElement() {
//        fail("Not yet implemented");
    }

    /**
     * Test method for {@link xal.model.probe.traj.Trajectory#statesForElement_new(java.lang.String)}.
     */
    @Test
    public void testStatesForElement_new() {
//        fail("Not yet implemented");
    }

}
