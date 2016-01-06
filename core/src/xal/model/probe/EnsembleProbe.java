/*
 * EnsembleProbe.java
 *
 * Created on September 17, 2002, 9:29 PM
 */

package xal.model.probe;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.ens.Ensemble;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.r3.R3;
import xal.model.probe.traj.EnsembleProbeState;
import xal.model.probe.traj.Trajectory;

/**
 * Represents an ensemble of particles.  This <code>IProbe</code> type maintains an
 * <code>Ensemble</code> object which is a collection of <code>Particle</code>s.  Thus,
 * this probe designed for multi-particle simulation.
 *
 * @author  Christopher Allen
 */
public class EnsembleProbe extends BunchProbe<EnsembleProbeState> {
    
    /*
     *  Global Attributes
     */
    
    /** no field calculation scheme specified */
    public final static int     FLDCALC_NONE = 0;
    
    /** use a full potential summation of each particle */
    public final static int     FLDCALC_SUMMATION = 1;
    
    /** use grid finite difference scheme */
    public final static int     FLDCALC_GRIDFD = 2;
    
    /** use grid Fourier transform method */
    public final static int     FLDCALC_GRIDFT = 3;
    
    
    /*
     *  Abstract Method Implementations
     */
    
    
    
    // ************* Probe Trajectory Support
    
    /**
     * Creates a state snapshot of this probe's state and returns it as a 
     * <code>ProbeState</code> object.
     * 
     * @return a <code>EnsembleProbeState</code> encapsulating the probe's current state
     */
    @Override
    public EnsembleProbeState createProbeState() {
        return new EnsembleProbeState(this);
    }
    
	/**
	 * Creates a new, empty <code>EnsembleProbeState</code>.
	 * 
	 * @return a new, empty <code>EnsembleProbeState</code>
	 * 
	 * @author Jonathan M. Freed
	 * @since Jul 1, 2014
	 */
	@Override
	public EnsembleProbeState createEmptyProbeState(){
		return new EnsembleProbeState();
	}
    
    /**
     * Creates a trajectory of the proper type for saving the probe's history.
     * 
     * @return  a new, empty <code>Trajectory&lt;EnsembleProbeState&gt;</code> 
     * 		for saving the probe's history
     * 
     * @author Jonathan M. Freed
     */
    @Override
    public Trajectory<EnsembleProbeState> createTrajectory() {
        return new Trajectory<EnsembleProbeState>(EnsembleProbeState.class);
    }

    // BunchProbe Base Support =================================================
    
    /**
     *  Return the coordinates of the ensemble centroid.
     *
     *  @return     (homogeneous) phase space coordinates of ensemble centroid
     */
    public PhaseVector  phaseMean()   {
        return this.stateCurrent.phaseMean();
    }
    
    /**
     *  Return the correlation matrix of the distribution
     *
     *  @return     symmetric 7x7 covariance matrix in homogeneous coordinates
     *
     *  @see    xal.tools.beam.PhaseMatrix
     */
    public CovarianceMatrix  getCorrelation()    {
    	return this.stateCurrent.phaseCovariance();
    }
    
    

    
    /*
     *  EnsembleProbe Initialization
     */
    
    /** 
     *  Creates a new (empty) instance of EnsembleProbe 
     */
    public EnsembleProbe() {
        super( );
        
        this.setEnsemble(new Ensemble());
    };
    
    /**
     *  Copy Constructor.  Create a new instance of <code>EnsembleProbe</code>
     *  which is a deep copy of the argument
     * 
     *  NOTE: the copy operation can be expansive for large <code>Ensemble</code>s
     * 
     *  @param  probe   object to be copied
     */
    public EnsembleProbe(final EnsembleProbe probe)   {
        super(probe);
        
        this.setEnsemble( new Ensemble( probe.getEnsemble() ) );
    };
    
    @Override
    public EnsembleProbe copy() {
        return new EnsembleProbe( this );
    }
    /**
     *  Set the field calculation method
     *
     *  @param  enmFldCalc  field calculation method enumeration
     */
    public void setFieldCalculation(int enmFldCalc)  { 
    	this.stateCurrent.setFieldCalculation(enmFldCalc);
    }
    
    /**
     *  Set the EnsembleProbe state to the value of the argument
     * 
     *  NOTE: the copy operation can be expansive for large <code>Ensemble</code>s
     * 
     *  @param  ens     <code>Ensemble</code> object to be copied
     */
    public void setEnsemble(Ensemble ens)   { 
        this.stateCurrent.setEnsemble(ens);
    }

    
    
    /*
     *  Data Query
     */
    
    /**
     * Return the field calculation method
     */
    public int getFieldCalculation() { 
    	return this.stateCurrent.getFieldCalculation();
    }
    
    /**
     *  Return the Ensemble state object
     */
    public Ensemble getEnsemble() { 
    	return this.stateCurrent.getEnsemble();
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
        return this.stateCurrent.electricField(ptFld);
    }
    
    
    
    /*
     *  Trajectory Support
     */
 


//    /**
//     * Apply the contents of ProbeState to update my current state.  The argument
//     * supplying the new state should be of concrete type <code>EnsembleProbeState</code>.
//     * 
//     * @param state     <code>ProbeState</code> object containing new probe state data
//     * 
//     * @exception   IllegalArgumentException    wrong <code>ProbeState</code> sub-type for this probe
//     */
//    @Override
//    public void applyState(EnsembleProbeState state) {
//        if (!(state instanceof EnsembleProbeState))
//            throw new IllegalArgumentException("invalid probe state");
//        super.applyState(state);
//        setFieldCalculation(((EnsembleProbeState)state).getFieldCalculation());
//        setEnsemble(((EnsembleProbeState)state).getEnsemble());
//    }
    
    @Override
    protected EnsembleProbeState readStateFrom(DataAdaptor container) throws DataFormatException {
        EnsembleProbeState state = new EnsembleProbeState();
        state.load(container);
        return state;
    }

}
