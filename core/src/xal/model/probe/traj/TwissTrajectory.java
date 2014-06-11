///*
// * TwissTrajectory.java
// * 
// * Created December, 2006
// * 
// * Christopher K. Allen
// */
//package xal.model.probe.traj;
//
//import xal.tools.beam.PhaseMatrix;
//
///**
// * Stores the trajectory of a TwissProbe.
// * 
// * @author Christopher K. Allen
// * @version $id:
// * 
// */
//public class TwissTrajectory extends BeamTrajectory {
//    /**
//     * Creates a new <code>ProbeState</code> object with the proper type for the trajectory.
//     * 
//     * @return      new, empty <code>EnvelopeProbeState</code> object
//     */
//    @Override
//    protected ProbeState newProbeState() {
//        return new TwissProbeState();
//    }
//    
//// 	/**
////	 * Returns the state response matrix calculated from the front face of
////	 * elemFrom to the back face of elemTo.
////	 * 
////	 * @param elemFrom String identifying starting lattice element
////	 * @param elemTo String identifying ending lattice element
////	 * @return state response matrix calculated from front face of elemFrom to
////	 * back face of elemTo
////	 */
////	public PhaseMatrix stateResponse(String elemFrom, String elemTo) {
////
////		// find starting index
////		int[] arrIndFrom = indicesForElement(elemFrom);
////
////		int[] arrIndTo = indicesForElement(elemTo);
////
////		if (arrIndFrom.length == 0 || arrIndTo.length == 0)
////			throw new IllegalArgumentException("unknown element id");
////
////		int indFrom, indTo;
////		indTo = arrIndTo[arrIndTo.length - 1]; // use last state before start element
////
////		TwissProbeState   stateTo =(TwissProbeState) stateWithIndex(indTo);
////		PhaseMatrix       matTo = stateTo.getResponseMatrix();
////		
////		indFrom = arrIndFrom[0] - 1;
////		if (indFrom < 0) return matTo; // response from beginning of machine
////		
////		TwissProbeState   stateFrom =	(TwissProbeState) stateWithIndex(indFrom);
////		PhaseMatrix       matFrom = stateFrom.getResponseMatrix();
////		
////		return matTo.times(matFrom.inverse());
////		
////	}       
//    
//}
