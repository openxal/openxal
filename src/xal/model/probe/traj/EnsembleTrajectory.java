package xal.model.probe.traj;


/**
 * Saves the history of an EnsembleProbe.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class EnsembleTrajectory extends BeamTrajectory {
    /**
     * Creates a new <code>ProbeState</code> object with the proper type for the trajectory.
     * 
     * @return      new, empty <code>EnsembleProbeState</code> object
     */
    @Override
    protected ProbeState newProbeState() {
        return new EnsembleProbeState();
    }
}
