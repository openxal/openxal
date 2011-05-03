package xal.model.probe.traj;

import xal.tools.math.r3.R3;
import xal.tools.beam.CorrelationMatrix;
import xal.tools.beam.ens.Ensemble;
import xal.tools.data.DataAdaptor;
import xal.model.probe.EnsembleProbe;
import xal.model.xml.ParsingException;

/**
 * Encapsulates the state of an EnsembleProbe at a particular point in time.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class EnsembleProbeState extends BunchProbeState {
	


    /*
     * Global Constants
     */    

    /** element tag for ensemble data */    
    private static final String ENSEMBLE_LABEL = "ensemble";
    
    /** attribute tag for field calculation method */
    private static final String CALC_LABEL = "fldCalc";
    
    /** attribute tag for data file */
    private static final String FILE_LABEL = "file";
    
    
    
    /*
     * Local Attributes
     */
     
    /** field calculation method */
    private int         m_enmFldCalc;
    
    /** the particle ensemble */
    private Ensemble    m_ensPhase;
    
    

    /*
     * Initialization
     */    


    /**
     * Default constructor.  Create a new, empty <code>EnsembleProbeState<code> object.
     */    
    public EnsembleProbeState() {
        m_ensPhase = new Ensemble();
    }

    /**
     * Initializing Constructor.  Create a new <code>EnsembleProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>EnsembleProbe</code> containing initializing state information
     */
    public EnsembleProbeState(EnsembleProbe probe) {
        super(probe);
        this.setFieldCalculation( probe.getFieldCalculation() );
        this.setEnsemble( probe.getEnsemble() );
    }


    /**
     *  Set the field calculation method
     *
     *  @param  fc  field calculation method enumeration
     */
    public void setFieldCalculation(int fc) {
        m_enmFldCalc = fc;
    }
    
    /**
     *  Set the state to the <b>value</b> of the argument
     * 
     *  NOTE: the copy operation can be expansive for large <code>Ensemble</code>s
     * 
     *  @param  ens     <code>Ensemble</code> object to be copied
     */
    public void setEnsemble(Ensemble ens) {
        m_ensPhase = new Ensemble(ens);
    }





    /*
     *  Data Query
     */
    
    /**
     * Return the field calculation method
     */
    public int getFieldCalculation() {
        return m_enmFldCalc;
    }
    
    /**
     *  Return the Ensemble state object
     */
    public Ensemble getEnsemble() {
        return m_ensPhase;
    }
	
	
    /**
	 * Get the betatron phase for all three phase planes.
     * 
     * @return  vector (psix,psiy,psiz) of phases in radians
     */
    @Override
    public R3 getBetatronPhase() {
		throw new UnsupportedOperationException( "This class does not support this method at this time." );
	}
	
    
    /**
     *  Return the correlation matrix of the distribution
     *
     *  @return     symmetric 7x7 covariance matrix in homogeneous coordinates
     *
     *  @see    xal.tools.beam.CorrelationMatrix
     */
    public CorrelationMatrix phaseCorrelation() {
        return getEnsemble().phaseCorrelation();
    }
    
//    /**
//     *  Return the coordinates of the ensemble centroid.
//     *
//     *  @return     (homogeneous) phase space coordinates of ensemble centroid
//     */
//    public PhaseVector phaseMean() {
//        return getEnsemble().phaseMean();
//    }
    
    
    
    /*
     * Debugging
     */
     
     
    /**
     * Write out state information to a string.
     * 
     * @return     text version of internal state data
     */
    @Override
    public String toString() {
        return super.toString() + " calc: " + getFieldCalculation() + 
                " ens: " + getEnsemble().toString();
    }
        
        
        
	
    /*
     * Support Methods
     */ 
    
    
    /**
     * Save the state values particular to <code>EnsembleProbeState</code> objects
     * to the data sink.
     * 
     *  @param  container   data sink represented by <code>DataAdaptor</code> interface
     */
    @Override
    protected void addPropertiesTo(DataAdaptor container) {
        super.addPropertiesTo(container);
        
        DataAdaptor ensNode = container.createChild(ENSEMBLE_LABEL);
        ensNode.setValue(CALC_LABEL, getFieldCalculation());
        ensNode.setValue(FILE_LABEL, getEnsemble().toString());
    }
    
    /**
     * Recover the state values particular to <code>EnsembleProbeState</code> objects 
     * from the data source.
     *
     *  @param  container   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception ParsingException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container) 
            throws ParsingException {
        super.readPropertiesFrom(container);

        DataAdaptor ensNode = container.childAdaptor(ENSEMBLE_LABEL);
        if (ensNode == null)
            throw new ParsingException("EnsembleProbeState#readPropertiesFrom(): no child element = " + ENSEMBLE_LABEL);

        setFieldCalculation(ensNode.intValue(CALC_LABEL));
        setEnsemble(new Ensemble());
    }
    
}
