package xal.model.probe;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.model.alg.DiagnosticTracker;
import xal.model.probe.traj.DiagnosticProbeState;
import xal.model.probe.traj.Trajectory;

/**
 * Simple diagnostic probe for testing the Lattice framework.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class DiagnosticProbe extends Probe<DiagnosticProbeState> {


	// ************ constructors
	
	
    /** 
     *  Default constructor for DiagnosticProbe. 
     *  Creates a new (empty) instance of DiagnosticProbe.
     */
    public DiagnosticProbe() {
        super( new DiagnosticTracker() );        
    }
    
    public DiagnosticProbe(final DiagnosticProbe copy) {
        super( copy );
        this.setElementsVisited(copy.getElementsVisited());
    }


    @Override
    public DiagnosticProbe copy() {
        return new DiagnosticProbe( this );
    }
    // ************ diagnostic tracking methods
    
    
    /**
     * Returns the number of elements traversed by probe.
     *
     * @return number of model elements propagated by probe
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public int getElementsVisited() {
    	return this.stateCurrent.getElementsVisited();
    }
    
    /**
     * Set the element count to the given number.
     *
     * @param n     new value for the element traversed accumulator
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setElementsVisited(int n) {
    	this.stateCurrent.setElementsVisited(n);
    }
    
    /**
     * Increments the number of element traversed by 1.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void incrementElementsVisited() {
    	this.stateCurrent.incrementElementsVisited();
    }


	// ************ required Trajectory protocol
    
    /**
     * Creates a trajectory of the proper type for saving the probe's history.
     * 
     * @return a new, empty <code>Trajectory&lt;DiagnosticProbeState&gt;</code> 
     * 		for saving the probe's history. 
     * 
     * @author Jonathan M. Freed
     */ 
	@Override
	public Trajectory<DiagnosticProbeState> createTrajectory() {
		return new Trajectory<DiagnosticProbeState>(DiagnosticProbeState.class);
	}
	
	@Override
	public DiagnosticProbeState createProbeState() {
		return new DiagnosticProbeState(this);
	}
	
	/**
	 * Creates a new, empty <code>DiagnosticProbeState</code>.
	 * 
	 * @return a new, empty <code>DiagnosticProbeState</code>
	 * 
	 * @author Jonathan M. Freed
	 * @since Jul 1, 2014
	 */
	@Override
	public DiagnosticProbeState createEmptyProbeState(){
		return new DiagnosticProbeState();
	}
	
//	@Override
//    public void applyState(DiagnosticProbeState state) {
//		this.stateCurrent = state.copy();
////		if (! (state instanceof DiagnosticProbeState))
////			throw new IllegalArgumentException("invalid probe state");
////		super.applyState(state);
////		setElementsVisited(((DiagnosticProbeState)state).getElementsVisited());
//	}	
//	
    @Override
    protected DiagnosticProbeState readStateFrom(DataAdaptor container) throws DataFormatException {
        DiagnosticProbeState state = new DiagnosticProbeState();
        state.load(container);
        return state;
    }
}



