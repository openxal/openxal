package xal.model.probe.resp.traj;

import xal.tools.beam.PhaseMatrix;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;

/**
 * @author Craig McChesney
 * 
 * @deprecated This class isn't used in any application, let's avoid it
 */
@Deprecated
public class ParticlePerturbProbeTrajectory extends Trajectory {

	// ******** abstract methods from Trajectory

	/**
	 * Creates a ProbeState of the appropriate species.
	 * 
	 * @see gov.sns.xal.model.probe.traj.Trajectory#newProbeState()
	 */
	@Override
    protected ProbeState newProbeState() {
		return new ParticlePerturbProbeState();
	}

	/**
	 * Returns the state response matrix calculated from the front face of
	 * elemFrom to the back face of elemTo.
	 * 
	 * @param elemFrom String identifying starting lattice element
	 * @param elemTo String identifying ending lattice element
	 * @return state response matrix calculated from front face of elemFrom to
	 * back face of elemTo
	 */
	public PhaseMatrix stateResponse(String elemFrom, String elemTo) {

		// find starting index
		int[] arrIndFrom = indicesForElement(elemFrom);

		int[] arrIndTo = indicesForElement(elemTo);

		if (arrIndFrom.length == 0 || arrIndTo.length == 0)
			throw new IllegalArgumentException("unknown element id");

		int indFrom, indTo;
		indTo = arrIndTo[arrIndTo.length - 1]; // use last state before start element

		ParticlePerturbProbeState stateTo =
			(ParticlePerturbProbeState) stateWithIndex(indTo);
		PhaseMatrix matTo = stateTo.getResponse();
		
		indFrom = arrIndFrom[0] - 1;
		if (indFrom < 0) return matTo; // response from beginning of machine
		
		ParticlePerturbProbeState stateFrom =
			(ParticlePerturbProbeState) stateWithIndex(indFrom);
		PhaseMatrix matFrom = stateFrom.getResponse();
		
		return matTo.times(matFrom.inverse());
		
	}

}
