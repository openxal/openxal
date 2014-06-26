package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;
import xal.model.probe.DiagnosticProbe;
import xal.model.xml.ParsingException;

/**
 * Encapsulates the state of a <code>DiagnosticProbe</code> at a particular
 * point in time.
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 * 
 * @version June 26, 2014
 * 
 */
public class DiagnosticProbeState extends ProbeState<DiagnosticProbeState> {

    
    /*
     * Global Constants
     */
    
    /** DataAdaptor node label */
    private static final String DIAG_LABEL = "diagnostic";
    
    /** DataAdaptor attribute tag */
    private static final String COUNT_LABEL = "count";
    

    /*
     * Local Attributes
     */
    
    /** The accumulator for counting the model elements traversed by the probe at this state */
	private int elementsVisited;
	
	
	
	/*
	 * Base Class Interface
	 */
	
	/**
	 * Makes a clone of this probe state and returns it.
	 * 
	 * @see xal.model.probe.traj.ProbeState#copy()
	 *
	 * @author Christopher K. Allen
	 * @since  Jun 26, 2014
	 */
	public DiagnosticProbeState copy() {
	    return new DiagnosticProbeState(this);
	}

	
	/*
	 * Initialization
	 */
	
	/**
	 * Creates a new <code>DiagnosticProbeState</code>.
	 *
	 * @author  Christopher K. Allen
	 * @since   Apr 19, 2011
	 */
	public DiagnosticProbeState() {
	}
	
    /**
     * Copy constructor for DiagnosticProbeState.  Initializes the new
     * <code>DisagnosticProbeState</code> objects with the state attributes
     * of the given <code>DiagnosticProbeState</code>.
     *
     * @param diagnosticProbeState     initializing state
     *
     * @author Christopher K. Allen, Jonathan M. Freed
     * @since  Jun 26, 2014
     */
	public DiagnosticProbeState(DiagnosticProbeState diagnosticProbeState){
		super(diagnosticProbeState);
		
		this.elementsVisited = diagnosticProbeState.elementsVisited;
	}
	
    /**
     * Creates a new <code>DiagnosticProbeState</code> with the
     * state initialized from the given <code>DiagnosticProbe</code>.
     *
     * @author  Christopher K. Allen
     * @since   Apr 19, 2011
     */
	public DiagnosticProbeState(DiagnosticProbe probe) {
		super(probe);
		this.elementsVisited = probe.getElementsVisited();
	}
	
	

	/*
	 * Property Accessors
	 */
	
    /**
     * Returns the number of elements traversed by probe at this state.
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

	
	// ************* I/O support

	
	@Override
    protected void addPropertiesTo(DataAdaptor container) {
		super.addPropertiesTo(container);
        
        DataAdaptor diagNode = container.createChild(DIAG_LABEL);
		diagNode.setValue(COUNT_LABEL, String.valueOf(getElementsVisited()));
	}
	
	@Override
    protected void readPropertiesFrom(DataAdaptor container) 
			throws ParsingException {
        super.readPropertiesFrom(container);
        
        DataAdaptor diagNode = container.childAdaptor(DIAG_LABEL);
        if (diagNode == null)
            throw new ParsingException("DiagnosticProbeState#readPropertiesFrom(): no child element = " + DIAG_LABEL);

		setElementsVisited(diagNode.intValue(COUNT_LABEL));
	}
	
	
	// ************** Object overrides
	
	
	@Override
    public String toString() {
		return super.toString() + " count: " + getElementsVisited();
	}
		
}
