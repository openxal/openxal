package eu.ess.jels.model.probe;

/**
 * Special interface for probes, that rf gap elements can use in order to store information about rf gap phase.
 * 
 * @author Ivo List <ivo.list@cosylab.com>
 */
public interface IGapPhaseProbe {
	/**
	 * Sets the last phase. Usually used at the end of rf gap's calculation.
	 * @param lastrf gapPhase the phase
	 */
	void setLastGapPhase(double lastGapPhase);
	
	/**
	 * Returns the last phase. This can be used by the next rf gap at the beginning of the calculation.
	 * @return the phase
	 */
	double getLastGapPhase();
	
	/**
	 * Sets the last position of the rf gap. Used at the end of rf gap's calculation.
	 * @param lastrf gapPosition the position of current rf gap
	 */
	void setLastGapPosition(double lastGapPosition);
	
	/**
	 * Returns the last rf gap position. This can be used by the next rf gap at the beginning of the calculation.
	 * @return the position of the previous rf gap
	 */
	double getLastGapPosition();
	
}
