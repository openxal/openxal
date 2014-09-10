package xal.app.beam_matcher;

import java.util.ArrayList;
import java.util.List;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.beam.Twiss;
import xal.tools.beam.CovarianceMatrix;
import xal.model.ModelException;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;


public class ModelWireScanData {
    
    private static final String STR_ID = "HEBT1";
    Accelerator accl;
    Scenario model;
    private AcceleratorSeq  seq;
    
    double bs_h1, bs_h2;
    
    public ModelWireScanData() {
        GenDocument main;
        main = GenDocument.getInstance();
        accl = main.getAccelerator();
        model= main.getModel();
        
        
    }
    /**
     * @param First Quadrupole number
     * @param	Second Quadrupole number
     * @param	Third Quadrupole number
     * @param	Fourth Qudarupole number
     * @param	% to change 1st Quad by
     * @param	% to change 2nd Quad by
     * @param	% to change 3rd Quad by
     * @param	% to change 4th Quad by
     *
     */
    double targetHSize1, targetVSize1, targetDSize1;
    public Scenario virtualAcceleratorOne(int quadNum1, int quadNum2, int quadNum3, int quadNum4, double dblPct1,
                                          double dblPct2, double dblPct3, double dblPct4) throws ModelException, GetException, ConnectionException {
        
        /*
         System.out.println("Displaying beam sizes for wire scanner number " + quadNum);
         model.run();
         //		ArrayList<Double> lstSizes = this.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
         //		ArrayList<Double> lstSizes2 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS02");
         //		ArrayList<Double> lstSizes3 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS03");
         //		ArrayList<Double> lstSizes4 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS04");
         
         /*
         System.out.println("horizontal beam size at wire scanner " + quadNum + " is: " + bs_h1);
         
         System.out.println("");
         System.out.println("Just Ran " + model);
         System.out.println(lstSizes);
         System.out.println(lstSizes2);
         System.out.println(lstSizes3);
         System.out.println(lstSizes4);
         */
        
        AcceleratorHardware hware = new AcceleratorHardware();
        
        List<AcceleratorNode> quads = hware.getAllQuadrupoles(STR_ID);
        AcceleratorNode quad1 = quads.get(quadNum1 - 1);
        AcceleratorNode quad2 = quads.get(quadNum2 - 1);
        AcceleratorNode quad3 = quads.get(quadNum3 - 1);
        AcceleratorNode quad4 = quads.get(quadNum4 - 1);
        
        
        ModelMagneticField ModelBField = new ModelMagneticField();
        ModelBField.changeQuadValue(quad1,  dblPct1);
        ModelBField.changeQuadValue(quad2,  dblPct2);
        ModelBField.changeQuadValue(quad3,  dblPct3);
        ModelBField.changeQuadValue(quad4,  dblPct4);
        
        model.resetProbe();
        model.run();
        
        /*
         lstSizes = this.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
         lstSizes2 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS02");
         lstSizes3 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS03");
         lstSizes4 = this.extractWsOneBeamSize(model, "HEBT_Diag:WS04");
         
         
         System.out.println(" ");
         System.out.println("reran the model and got...");
         System.out.println(lstSizes);
         System.out.println(lstSizes2);
         System.out.println(lstSizes3);
         System.out.println(lstSizes4);
         */
        
        return model;
    }
    
    
    /*
     * Debugging
     */
    public ArrayList<Double> extractWsOneBeamSize(Scenario model, String STR_WS_ID) {
//        Trajectory<? extends ProbeState<?>> traj = model.getTrajectory();
//        
//        ProbeState<?> targ = traj.stateForElement(STR_WS_ID);
//        
//        EnvelopeProbeState	state = (EnvelopeProbeState)targ;
        Trajectory<EnvelopeProbeState> traj = model.getTrajectory();
        EnvelopeProbeState             state = traj.stateForElement(STR_WS_ID);
        
        CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
        Twiss[] arrTwiss = covarianceMatrix.computeTwiss();
        
        targetHSize1 = arrTwiss[0].getEnvelopeRadius();
        targetVSize1 = arrTwiss[1].getEnvelopeRadius();
        targetDSize1 = arrTwiss[2].getEnvelopeRadius();
        
        //	System.out.println(targetHSize + " "+ targetVSize + " " + targetDSize);
        ArrayList<Double> wS01OnlineModel = new ArrayList<Double>();
        wS01OnlineModel.add(targetHSize1);
        wS01OnlineModel.add(targetVSize1);
        wS01OnlineModel.add(targetDSize1);
        
        
        return wS01OnlineModel;
        
    }
    /* test code for the derivative */
    public double getbs_h1() throws ModelException {
        model.run();
        ArrayList<Double> lstSizes = this.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
        
        /* test code for the derivative */
        bs_h1 = lstSizes.get(0);
        
        System.out.println("Test 1: " + bs_h1);
        return bs_h1;
    }
    /* test code for the derivative
     * TODO: FIX THIS BROKEN CODE
     * */
    public double getbs_h2() throws ModelException {
        model.run();
        ArrayList<Double> lstSizes = this.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
        
        /* test code for the derivative */
        bs_h2 = lstSizes.get(0);
        
        System.out.println("Test 2: " + bs_h2);
        return bs_h2;
    }
    
}
