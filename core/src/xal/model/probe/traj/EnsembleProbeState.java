package xal.model.probe.traj;

import xal.model.probe.EnsembleProbe;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.ens.Ensemble;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;

/**
 * Encapsulates the state of an EnsembleProbe at a particular point in time.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class EnsembleProbeState extends BunchProbeState<EnsembleProbeState> {
	


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
     * Default constructor.  Create a new, empty <code>EnsembleProbeState</code> object.
     */    
    public EnsembleProbeState() {
        m_ensPhase = new Ensemble();
    }
    
    /**
     * Copy constructor for EnsembleProbeState.  Initializes the new
     * <code>EnsembleProbeState</code> objects with the state attributes
     * of the given <code>EnsembleProbeState</code>.
     *
     * @param ensembleProbeState     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
    public EnsembleProbeState(final EnsembleProbeState ensembleProbeState){
    	super(ensembleProbeState);
    	
    	this.m_enmFldCalc	= ensembleProbeState.m_enmFldCalc;

    	this.m_ensPhase		= ensembleProbeState.m_ensPhase.deepCopy();
    }

    /**
     * Initializing Constructor.  Create a new <code>EnsembleProbeState</code> object and
     * initialize it to the state of the probe argument.
     * 
     * @param probe     <code>EnsembleProbe</code> containing initializing state information
     */
    public EnsembleProbeState(final EnsembleProbe probe) {
        super(probe);
        this.setFieldCalculation( probe.getFieldCalculation() );

        this.setEnsemble( probe.getEnsemble().deepCopy() );
    }
    
    
    /*
     * Property Accessors
     */
    
    
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
     *  Get the electric field at a point in R3 from the ensemble.
     *
     *  @param  ptFld       field point to evaluation ensemble field
     *  
     *  @return             electric field at field point
     *
     */
    public R3   electricField(R3 ptFld) {
    	R3      vecE = new R3();
        return vecE;
    }

    
    /*
     * Computed Quantities
     */
    
    /**
     *  Return the coordinates of the ensemble centroid.
     *
     *  @return     (homogeneous) phase space coordinates of ensemble centroid
     */
    public PhaseVector  phaseMean()   {
    	return getEnsemble().phaseMean();
    }
    
    /**
     *  Return the correlation matrix of the distribution
     *
     *  @return     symmetric 7x7 covariance matrix in homogeneous coordinates
     *
     *  @see    xal.tools.beam.CovarianceMatrix
     */
    public CovarianceMatrix phaseCovariance() {
        return getEnsemble().phaseCovariance();
    }
    
    
    /*
     * ProbeState Overrides
     */
    
    /**
     * Implements the clone operation required by the base class
     * <code>ProbeState</code>
     *
     * @see xal.model.probe.traj.ProbeState#copy()
     *
     * @author Christopher K. Allen
     * @since  Jun 27, 2014
     */
    @Override
    public EnsembleProbeState   copy() {
        return new EnsembleProbeState(this);
    }

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
     *  @exception DataFormatException     state information in data source is malformatted
     */
    @Override
    protected void readPropertiesFrom(DataAdaptor container) 
            throws DataFormatException {
        super.readPropertiesFrom(container);

        DataAdaptor ensNode = container.childAdaptor(ENSEMBLE_LABEL);
        if (ensNode == null)
            throw new DataFormatException("EnsembleProbeState#readPropertiesFrom(): no child element = " + ENSEMBLE_LABEL);

        setFieldCalculation(ensNode.intValue(CALC_LABEL));
        setEnsemble(new Ensemble());
    }
    

    /*
     * Object Overrides
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
        
}
