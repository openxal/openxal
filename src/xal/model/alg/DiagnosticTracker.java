package xal.model.alg;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.DiagnosticProbe;

/**
 * Simple algorithm for testing the model framework.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public class DiagnosticTracker extends Tracker {

    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = DiagnosticTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<DiagnosticProbe>       s_clsProbeType = DiagnosticProbe.class;
    
    
    
    /*
     * Initialization
     */
    
    
    /** 
     *  Creates a new instance of ParticleTracker 
     */
    public DiagnosticTracker() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }
    
    /**
     * Copy constructor for DiagnosticTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public DiagnosticTracker( DiagnosticTracker sourceTracker ) {
        super( sourceTracker );
    }
    
    /**
     * Create a deep copy of DiagnosticTracker
     */
    @Override
    public DiagnosticTracker copy() {
        return new DiagnosticTracker( this );
    }
    
    // ************* Tracker abstract protocol
    
    

    /**
    * Propagates the probe through the element.
    *
    *  @param  probe   probe to propagate
    *  @param  elem    element acting on probe
    *
    *  @exception  ModelException  invalid probe type or error in advancing probe
    */
    @Override
    public void doPropagation(IProbe probe, IElement elem) throws ModelException {
        
      int nSteps         = this.compStepCount(elem);
      double dblStepSize = elem.getLength() / nSteps;
      for (int i=0 ; i<nSteps ; i++) {
        this.advanceState(probe, elem, dblStepSize);
        this.advanceProbe(probe, elem, dblStepSize);
        probe.update();
      }
    }
    
    
    /*
     * Internal Support
     */


//    /**
//     * Returns the length of the next element subsection to process, always
//     * the length of the element for a diagnostic probe.
//     * 
//     * @param elem The element currently acting on the probe
//     * @param pos Position of probe within specified element, should always be 0
//     */
//    protected double nextIntervalFrom(IElement elem, double pos) {
//    	return elem.getLength();
//    }
    
    /**
     * Returns the number of sections to break the specified element in to
     * for propagation.  Always one for a diagnostic probe.
     * 
     * @param elem Element currently acting on probe
     * 
     * @return one
     */
    protected int compStepCount(IElement elem) {
    	return 1;
    }
    
    /**
     * Advance the supplied probe through a subsection of the specified length in
     * the specified element.
     * 
     * @param probe Probe being acted on by element
     * @param elem Element acting on probe
     * @param dblLen length of element subsection to advance probe through
     */
    protected void advanceState(IProbe probe, IElement elem, double dblLen)
        throws ModelException {
            System.out.println("probe visiting: " + elem.getId());
            ((DiagnosticProbe)probe).incrementElementsVisited();
    }

}
