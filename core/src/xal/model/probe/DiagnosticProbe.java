package xal.model.probe;

import xal.tools.data.DataAdaptor;
import xal.model.alg.DiagnosticTracker;
import xal.model.probe.traj.DiagnosticProbeState;
import xal.model.probe.traj.DiagnosticProbeTrajectory;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.ParsingException;

/**
 * Simple diagnostic probe for testing the Lattice framework.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class DiagnosticProbe extends Probe {

	// count the number of elements visited	
	private int elementsVisited = 0;
	
	/** probe trajectory */
	private Trajectory<DiagnosticProbeState> trajectory;


	// ************ constructors
	
	
    /** 
     *  Default constructor for DiagnosticProbe. 
     *  Creates a new (empty) instance of DiagnosticProbe.
     */
    public DiagnosticProbe() {
        super( new DiagnosticTracker() );        
    }
    
    public DiagnosticProbe(DiagnosticProbe copy) {
        super( copy );
        this.elementsVisited = copy.elementsVisited;
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
    	return elementsVisited;
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
    	elementsVisited = n;
    }
    
    /**
     * Increments the number of element traversed by 1.
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void incrementElementsVisited() {
    	++elementsVisited;
    }


	// ************ required Trajectory protocol
    
    /**
     * Creates a trajectory of the proper type for saving the probe's history.
     * 
     * @return a new, empty <code>Trajectory&lt;DiagnosticProbeState&gt;</code> 
     * 		for saving the probe's history. 
     * 
     * @author Jonathan Freed
     */ 
	@Override
	public Trajectory<DiagnosticProbeState> createTrajectory() {
		this.trajectory = new Trajectory<DiagnosticProbeState>();
		return this.trajectory;
	}
	
	@Override
	public DiagnosticProbeState createProbeState() {
		return new DiagnosticProbeState(this);
	}
	
	@Override
    public void applyState(ProbeState state) {
		if (! (state instanceof DiagnosticProbeState))
			throw new IllegalArgumentException("invalid probe state");
		super.applyState(state);
		setElementsVisited(((DiagnosticProbeState)state).getElementsVisited());
	}	
	
    @Override
    protected ProbeState readStateFrom(DataAdaptor container) throws ParsingException {
        DiagnosticProbeState state = new DiagnosticProbeState();
        state.load(container);
        return state;
    }

    /**
     * Retrieves the trajectory of the proper type for the probe
     * 
     * @return a <code>Trajectory&lt;DiagnosticProbeState&gt;</code> that is the 
     * 		trajectory of the probe
     * 
     * @author Jonathan Freed
     */ 
	@Override
	public Trajectory<DiagnosticProbeState> getTrajectory() {
		return this.trajectory;
	}
}
