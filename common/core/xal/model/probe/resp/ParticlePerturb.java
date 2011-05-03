/*
 * ParticleResponse.java
 *
 * Created on December 6, 2002, 1:07 PM
 */

package xal.model.probe.resp;

import xal.tools.beam.PhaseMatrix;
import xal.tools.data.DataAdaptor;

import xal.model.probe.resp.traj.ParticlePerturbProbeState;
import xal.model.probe.resp.traj.ParticlePerturbProbeTrajectory;
import xal.model.probe.traj.ProbeState;
import xal.model.xml.ParsingException;

/**
 *
 * @author  Christopher Allen
 * @author Craig McChesney
 * 
 * @deprecated  This component of the online model is not used
 */
@Deprecated
public class ParticlePerturb extends Perturbation {
	
	
	private PhaseMatrix m_matResp = PhaseMatrix.identity();
	
	
	// *********** accessing
	
	
	/**
	 * Returns the current response matrix of the probe
	 *
	 * @return the response matrix of the probe 
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public PhaseMatrix getTransferMatrix() {
		return m_matResp;
	}
	
	/**
	 * Set the response matrix for the probe
	 *
	 * @param m    probe's new response matrix
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 15, 2011
	 */
	public void setTransferMatrix(PhaseMatrix m) {
		m_matResp = m;
	}
	
	
    // *********** Probe abstract methods
    
    
	/**
	 * Creates a Trajectory for saving the probe's history.
	 */
	@Override
	public ParticlePerturbProbeTrajectory createTrajectory() {
		return new ParticlePerturbProbeTrajectory();
	}
    
	/**
	 * Captures the probe's state.
	 */
	@Override
	public ParticlePerturbProbeState createProbeState() {
		return new ParticlePerturbProbeState(this);
	}
	
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        ParticlePerturbProbeState state = new ParticlePerturbProbeState();
        state.load(container);
        return state;
    }
}
