package eu.ess.jels.model.probe;

import xal.model.IAlgorithm;
import xal.model.probe.EnvelopeProbe;

/**
 * Simple extension of EnvelopeProbe that implements IGapPhaseProbe.
 * 
 * Other methods are here as a workaround, so that this probe still works with envelope algorithm.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 *
 */
public class GapEnvelopeProbe extends EnvelopeProbe implements IGapPhaseProbe {
	/** algorithm providing probe dynamics */
    private IAlgorithm  m_ifcAlg = null;
	
	public GapEnvelopeProbe() {
		super();
	}

	public GapEnvelopeProbe(GapEnvelopeProbe probe) {
		super(probe);
		setLastGapPhase(probe.getLastGapPhase());
		setLastGapPosition(probe.getLastGapPosition());
		 // Copy the algorithm object if we have one
        m_ifcAlg = null;
        final IAlgorithm algorithm = probe.getAlgorithm();
        if ( algorithm != null )   {
            setAlgorithm( algorithm.copy() );
        }
	}

	private double lastGapPhase;
	private double lastGapPosition;
	
	public double getLastGapPhase()
	{
		return lastGapPhase;
	}
	
	public void setLastGapPhase(double lastGapPhase)
	{
		this.lastGapPhase = lastGapPhase;
	}
	
	public double getLastGapPosition() {
		return lastGapPosition;
	}

	public void setLastGapPosition(double lastGapPosition) {
		this.lastGapPosition = lastGapPosition;
	}


    /**
     *  Return the algorithm defining the probes dynamics.
     *
     *  @return         interface to probe dynamics
     */
    public IAlgorithm getAlgorithm()    { return m_ifcAlg; };
	
    /**
     *  Set the algorithm defining the probes dynamics through elements
     *
     *  @param  ifcAlg   object exposing the IAlgorithm interface
     */
	@Override
    public boolean setAlgorithm(IAlgorithm ifcAlg) { 
        if (!ifcAlg.validProbe(new EnvelopeProbe())) return false;
        
        m_ifcAlg = ifcAlg;
        return true;
    }

	@Override
	public EnvelopeProbe copy() {
		super.copy();
		return new GapEnvelopeProbe( this );		
	};
	
}
