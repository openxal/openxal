/*
 * EnvelopeTracker.java
 */
 
package eu.ess.jels.model.alg;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.tools.beam.PhaseMatrix;
import Jama.Matrix;
import eu.ess.jels.model.probe.ElsProbe;

public class ElsTracker extends EnvelopeTracker {

    
    /*
     * Global Constants
     */
    

    // Versioning and definition
    /** string type identifier for algorithm */
    public static final String      s_strTypeId = ElsTracker.class.getName();
    
    /** current algorithm version */
    public static final int         s_intVersion = 4;
    
    /** probe type recognized by this algorithm */
    public static final Class<EnvelopeProbe>       s_clsProbeType = EnvelopeProbe.class;
    
    
    
    /*
     *  Local Attributes
     */
     
    /** 
     *  Creates a new instance of EnvelopeTracker 
     */
    public ElsTracker() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
        registerProbeType(ElsProbe.class);
    };
    
    /** 
     *  Creates a new, empty, instance of EnvelopeTracker.
     *  
     *  This method is a protected constructor meant only for child classes.
     *
     *  @param      strType         string type identifier of algorithm
     *  @param      intVersion      version of algorithm
     *  @param      clsProbeType    class object for probe handled by this algorithm.
     */
    protected ElsTracker(String strType, int intVersion, Class<? extends IProbe> clsProbeType) {
        super(strType, intVersion, clsProbeType);
        registerProbeType(ElsProbe.class);
    }
   
    /**
     * Copy constructor for EnvelopeTracker
     * 
     * @param       sourceTracker   Tracker that is being copied
     */
    protected ElsTracker(ElsTracker sourceTracker) {
        super(sourceTracker);
        registerProbeType(ElsProbe.class);
    }
    

    /**
     * Creates a deep copy of EnvelopeTracker
     */
    @Override
    public ElsTracker copy() {
        return new ElsTracker(this);
    }
    
    
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
         
        int     cntSteps;   // number of steps through element
        double  dblStep;    // step size through element
        
        //sako
        double elemPos = this.getElemPosition();
        double elemLen = elem.getLength();
        double propLen = elemLen - elemPos;
        
        if (propLen < 0) {
        	System.err.println("doPropagation, elemPos, elemLen = "+elemPos+" "+elemLen);
        	return;
        }
  
     /*   if(this.getSpaceChargeFlag())
            cntSteps = (int) Math.max(Math.ceil(propLen / getStepSize()), 1);
        else*/ 
            cntSteps = 1;
        
        dblStep = propLen / cntSteps;
        
        /*
        if(this.getSpaceChargeFlag())
            cntSteps = (int) Math.max(Math.ceil(elem.getLength() / getStepSize()), 1);
        else 
            cntSteps = 1;
        
        dblStep = elem.getLength() / cntSteps;
*/
        for (int i=0 ; i<cntSteps ; i++) {
            this.advanceState(probe, elem, dblStep);
            this.advanceProbe(probe, elem, dblStep);
        }
    }
    


    
    /*
     * IContextAware Interface
     */
    
    /** 
     *  Advances the probe state through a subsection of the element with the
     *  specified length.  Applies a space charge kick at the end of the element
     *  subsection for any probe having nonzero beam current.
     *
     *  @param  ifcElem     interface to the beam element
     *  @param  ifcProbe    interface to the probe
     *  @param  dblLen      length of element subsection to advance
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(IProbe ifcProbe, IElement ifcElem, double dblLen) 
        throws ModelException 
    {
        
        // Identify probe
        ElsProbe   probe = (ElsProbe)ifcProbe;
        
        // Get initial conditions of probe
        Matrix	envelope = probe.getEnvelope();
        
        // Compute the transfer matrix
        PhaseMatrix matrix  = ifcElem.transferMap(probe, dblLen).getFirstOrder(); 
        
        // Compute optics matrix...          
        Matrix optics = new Matrix(9, 9);
        
        optics.set(0,0,matrix.getElem(0,0)*matrix.getElem(0,0));
    	optics.set(0,1,-2.0*matrix.getElem(0,0)*matrix.getElem(0,1));
    	optics.set(0,2,matrix.getElem(0,1)*matrix.getElem(0,1));
    	optics.set(1,0,-matrix.getElem(0,0)*matrix.getElem(1,0));
    	optics.set(1,1,matrix.getElem(0,0)*matrix.getElem(1,1)+matrix.getElem(0,1)*matrix.getElem(1,0));
    	optics.set(1,2,-matrix.getElem(0,1)*matrix.getElem(1,1));
    	optics.set(2,0,matrix.getElem(1,0)*matrix.getElem(1,0));
    	optics.set(2,1,-2.0*matrix.getElem(1,0)*matrix.getElem(1,1));
    	optics.set(2,2,matrix.getElem(1,1)*matrix.getElem(1,1));

    	optics.set(3,3,matrix.getElem(2,2)*matrix.getElem(2,2));
    	optics.set(3,4,-2.0*matrix.getElem(2,2)*matrix.getElem(2,3));
    	optics.set(3,5,matrix.getElem(2,3)*matrix.getElem(2,3));
    	optics.set(4,3,-matrix.getElem(2,2)*matrix.getElem(3,2));
    	optics.set(4,4,matrix.getElem(2,2)*matrix.getElem(3,3)+matrix.getElem(2,3)*matrix.getElem(3,2));
    	optics.set(4,5,-matrix.getElem(2,3)*matrix.getElem(3,3));
    	optics.set(5,3,matrix.getElem(3,2)*matrix.getElem(3,2));
    	optics.set(5,4,-2.0*matrix.getElem(3,2)*matrix.getElem(3,3));
    	optics.set(5,5,matrix.getElem(3,3)*matrix.getElem(3,3));

    	optics.set(6,6,matrix.getElem(4,4)*matrix.getElem(4,4));
    	optics.set(6,7,-2.0*matrix.getElem(4,4)*matrix.getElem(4,5));
    	optics.set(6,8,matrix.getElem(4,5)*matrix.getElem(4,5));
    	optics.set(7,6,-matrix.getElem(4,4)*matrix.getElem(5,4));
    	optics.set(7,7,matrix.getElem(4,4)*matrix.getElem(5,5)+matrix.getElem(4,5)*matrix.getElem(5,4));
    	optics.set(7,8,-matrix.getElem(4,5)*matrix.getElem(5,5));
    	optics.set(8,6,matrix.getElem(5,4)*matrix.getElem(5,4));
    	optics.set(8,7,-2.0*matrix.getElem(5,4)*matrix.getElem(5,5));
    	optics.set(8,8,matrix.getElem(5,5)*matrix.getElem(5,5));

    	double det0 = matrix.getElem(0,0)*matrix.getElem(1,1)-matrix.getElem(0,1)*matrix.getElem(1,0);
    	optics = optics.times(1./det0);
    	
        Matrix	envelope1 = optics.times(envelope);
        
        // Advance the probe states 
        probe.setEnvelope(envelope1);
    };
    
}

