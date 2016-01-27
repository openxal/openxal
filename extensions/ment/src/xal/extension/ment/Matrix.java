/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.extension.ment;



import xal.model.probe.traj.Trajectory;
import xal.model.probe.traj.TransferMapState;
import xal.model.probe.TransferMapProbe;
import xal.model.alg.TransferMapTracker;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;

import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.calc.CalculationsOnMachines;

import xal.service.pvlogger.sim.PVLoggerDataSource;


/**
 *
 * @author tg4
 * 
 * @version Jan26, 2016 - ported to Open XAL by Christopher K. Allen
 */

public class Matrix{

    public double ax;
    public double bx;
    public double cx;
    public double dx;

    public double ay;
    public double by;
    public double cy;
    public double dy;

    private Trajectory<TransferMapState> trajectory;


    public Matrix(String sequenceId,final int pvlogid, final double Tkin, final double Trest, final double Charge) {


        PVLoggerDataSource initial_data = new PVLoggerDataSource(pvlogid);
        AcceleratorSeq sequence = XMLDataManager.loadDefaultAccelerator().getSequence(sequenceId);

        try {
            if ( sequence != null ) {


                Scenario model = Scenario.newScenarioFor(sequence);

                model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);

                Scenario model_pv = initial_data.setModelSource(sequence, model);

                TransferMapTracker ptracker = AlgorithmFactory.createTransferMapTracker(sequence);
                TransferMapProbe tr_map_probe = ProbeFactory.getTransferMapProbe(sequence, ptracker);

                tr_map_probe.setSpeciesRestEnergy(Trest);
                tr_map_probe.setKineticEnergy(Tkin);
                tr_map_probe.setSpeciesCharge(Charge);


                model_pv.setProbe(tr_map_probe);
                model_pv.resync();
                model_pv.run();

                trajectory = model_pv.getTrajectory();
            }
        }  catch (Exception exception) {}


    }


    public void setElemId(final String profileId, final String elemId){

        CalculationsOnMachines  comXferFac = new CalculationsOnMachines(trajectory);

        PhaseMatrix  matXfer = comXferFac.computeTransferMatrix(profileId, elemId);

        ax = matXfer.getElem(0, 0);
        bx = matXfer.getElem(0, 1);
        cx = matXfer.getElem(1, 0);
        dx = matXfer.getElem(1, 1);

        ay = matXfer.getElem(2, 2);
        by = matXfer.getElem(2, 3);
        cy = matXfer.getElem(3, 2);
        dy = matXfer.getElem(3, 3);

    }


}

