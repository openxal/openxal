package xal.tools.beam;

/**
 * @author CKAllen
 * @author Craig McChesney
 * @version $id:
 * 
 * @deprecated  Unfinished and not used anywhere
 */
@Deprecated
public class RmsEnvelope {

	/** Beam current */
	private double m_dblBmI = 0.0;
    
	/** Beam charge */
	private double m_dblBmQ = 0.0;
    
    /** envelope state - covariance matrix in homogeneous phase coordinates */
    private PhaseMatrix m_matSigma;
    
    
    // ********* constructors
    
    
    public RmsEnvelope() {
    	m_matSigma = new PhaseMatrix();
    	m_matSigma.setElem(6,6, 1.0);
    }
    
    /**
     * Copy constructor.
     */
    public RmsEnvelope(RmsEnvelope e) {
    	setCorrelation(new PhaseMatrix(e.getSigma()));
    }
    
    
    // ********* initialization and accessing

    /** 
     *  Returns the covariance matrix (sigma matrix) in homogeneous
     *  phase space coordinates.
     */
    public PhaseMatrix phaseCorrelation() { 
    	return this.getSigma(); 
    };  
    
    public PhaseMatrix getSigma() {
    	return m_matSigma;
    }
    
    public void setCorrelation(PhaseMatrix m) {
    	m_matSigma = m;
    }  
       

	// ********* envelope analysis and manipulation
	
	
}
