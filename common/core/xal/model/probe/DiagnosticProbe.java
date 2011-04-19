package xal.model.probe;

import xal.tools.data.IDataAdaptor;
import xal.model.alg.DiagnosticTracker;
import xal.model.probe.traj.DiagnosticProbeState;
import xal.model.probe.traj.DiagnosticProbeTrajectory;
import xal.model.probe.traj.ProbeState;
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


	// ************ constructors
	
	
    /** 
     *  Default constructor for DiagnosticProbe. 
     *  Creates a new (empty) instance of DiagnosticProbe.
     */
    public DiagnosticProbe() {
        super( new DiagnosticTracker() );        
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
	
	@Override
	public DiagnosticProbeTrajectory createTrajectory() {
		return new DiagnosticProbeTrajectory();
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
    protected ProbeState readStateFrom(IDataAdaptor container) throws ParsingException {
        DiagnosticProbeState state = new DiagnosticProbeState();
        state.load(container);
        return state;
    }
}
