/*
 * EnvelopeTracker.java
 */
 
package se.lu.esss.ics.jels.model.alg;

import se.lu.esss.ics.jels.model.probe.ElsProbe;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.probe.EnvelopeProbe;
import xal.tools.beam.PhaseMatrix;
import Jama.Matrix;


/**
 * Propagation of probe as implemented in ELS.
 * 
 *  ...
 * 
 * @author Emanuele Laface, Ivo List <ivo.list@cosylab.com>
 *
 */
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
            //cntSteps = (int) Math.max(Math.ceil(propLen / getStepSize()), 1);
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
        
        double[] det = new double[3];
    	
        for (int i = 0; i<3; i++)
    	{
    		double M11 = matrix.getElem(2*i+0,2*i+0);
    		double M12 = matrix.getElem(2*i+0,2*i+1);
    		double M21 = matrix.getElem(2*i+1,2*i+0);
    		double M22 = matrix.getElem(2*i+1,2*i+1);
    		det[i] = M11*M22-M21*M12;
    		
    		optics.set(3*i+0, 3*i+0, Math.pow(M11,2));
    		optics.set(3*i+0, 3*i+1, -2.0*M11*M12);
    		optics.set(3*i+0, 3*i+2, Math.pow(M12,2));
    		optics.set(3*i+1, 3*i+0, -M11*M21);
    		optics.set(3*i+1, 3*i+1, M11*M22+M12*M21);
    		optics.set(3*i+1, 3*i+2, -M12*M22);
    		optics.set(3*i+2, 3*i+0, Math.pow(M21,2));
    		optics.set(3*i+2, 3*i+1, -2.0*M21*M22);
    		optics.set(3*i+2, 3*i+2, Math.pow(M22,2));
    	}
    	
   		optics = optics.times(1./det[0]);
    	    
        Matrix	envelope1 = optics.times(envelope);        
               
        // Advance the probe states 
        probe.setEnvelope(envelope1);
    };
    
}

