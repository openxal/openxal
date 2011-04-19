package xal.model.probe.resp.traj;

import xal.tools.beam.PhaseMatrix;
import xal.tools.data.IDataAdaptor;
import xal.model.probe.resp.ParticlePerturb;
import xal.model.probe.traj.ProbeState;
import xal.model.xml.ParsingException;

/**
 * This class is currently Not Used.
 * 
 * Adds state variables particular to a particle to the
 * <code>ProbeState</code> base class.  Thus, the 
 * <code>ParticlePertubProbe</code> is the primary state
 * of a <code>ParticlePerturbProbe</code>.
 * 
 * @author Craig McChesney
 * 
 * @deprecated  CKA: I guess this class is deprecated by the comments, but
 *              it is referenced in the code.
 */

public class ParticlePerturbProbeState extends ProbeState {

	private PhaseMatrix m_matResp;

	// *************** constructors

	public ParticlePerturbProbeState() {
		m_matResp = PhaseMatrix.identity();
	}

	/**
	 * Create a ProbeState for the supplied probe.
	 * 
	 * @param probe Probe whose state to capture in a ProbeState
	 */
	public ParticlePerturbProbeState(ParticlePerturb probe) {
		super(probe);
		m_matResp = probe.getTransferMatrix();
	}

	// **************** accessing
	
	public PhaseMatrix getResponse() {
		return m_matResp;
	}

	@Override
    public String toString() {
		return super.toString() + " responseMatrix: " + m_matResp.toString();
	}

	// ************ I/O Support

	private static final String RESPONSE_LABEL = "response";
	private static final String VALUE_LABEL = "matrix";

	@Override
    protected void addPropertiesTo(IDataAdaptor container) {
		super.addPropertiesTo(container);

		IDataAdaptor partNode = container.createChild(RESPONSE_LABEL);
		partNode.setValue(VALUE_LABEL, m_matResp.toString());
	}

	@Override
    protected void readPropertiesFrom(IDataAdaptor container)
		throws ParsingException {
		super.readPropertiesFrom(container);

		IDataAdaptor child = container.childAdaptor(RESPONSE_LABEL);
		if (child == null)
			throw new ParsingException(
				"ParticlePerturbProbeState#readPropertiesFrom(): no child element = "
					+ RESPONSE_LABEL);

		m_matResp = PhaseMatrix.parse(child.stringValue(VALUE_LABEL));
	}

}
