package xal.model.probe.traj;


/**
 * Saves the history of a <code>DiagnosticProbe</code>.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class DiagnosticProbeTrajectory extends Trajectory {


	// ************** required Trajectory protocol
	
	/**
	 * Creates a new ProbeState of the appropriate species for this Trajectory.
	 */
	@Override
    protected ProbeState newProbeState() {
		return new DiagnosticProbeState();
	}
	
}
