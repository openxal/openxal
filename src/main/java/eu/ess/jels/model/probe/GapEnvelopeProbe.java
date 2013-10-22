package eu.ess.jels.model.probe;

import xal.model.IAlgorithm;
import xal.model.probe.EnvelopeProbe;

public class GapEnvelopeProbe extends EnvelopeProbe implements IGapPhaseProbe {
	/** algorithm providing probe dynamics */
    private IAlgorithm  m_ifcAlg = null;
	
	public GapEnvelopeProbe() {
		super();
	}

	public GapEnvelopeProbe(EnvelopeProbe probe) {
		super(probe);
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
    };
	
}
