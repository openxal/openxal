/*
 * Created on July 19, 2013
 */
package xal.tools.beam;

import xal.model.probe.traj.EnvelopeTrajectory;
import xal.tools.data.DataAdaptor;
import xal.model.probe.traj.IPhaseState;
import xal.tools.math.r3.R3;
import Jama.Matrix;

/**
 * @author syk
 */
public class LinacCalculations extends BeamParameters {

    private EnvelopeTrajectory _trajectory;
    
    public LinacCalculations(EnvelopeTrajectory envelopeTraj) {
        super();
        _trajectory = envelopeTraj;
    }
    
    public R3 getBetatronPhase() {
        return null;
    }
    
    public Twiss[] getTwiss() {
        return null;
    }

}
