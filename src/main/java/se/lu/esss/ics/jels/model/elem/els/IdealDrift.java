package se.lu.esss.ics.jels.model.elem.els;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.ThickElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;


public class IdealDrift extends ThickElement {
	/** Debugging flag */
	final boolean debug = false;
    	
    /*
     *  Global Attributes
     */
    
    /** string type identifier for all IdealDrift objects */
    public static final String      s_strType = "IdealDrift";
    
    /*
     * Local Attributes
     */            
    
    /*
     * Initialization
     */
    
    /** 
     *  Creates a new instance of IdealDrift 
     *
     *  @param  strId     string identifier for the element
     *  @param  dblLen    length of the drift
     */
    public IdealDrift(String strId, double dblLen) {
        super(s_strType, strId, dblLen);
    };
    
    /** 
     *  JavaBean constructor - creates a new unitialized instance of IdealDrift
     *
     *  <b>BE CAREFUL</b>
     */
    public IdealDrift() {
        super(s_strType);
    };
    
    /*
     *  ThickElement Abstract Functions
     */
     
    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     * 
     *  @param  probe   propagating probe
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return super.compDriftingTime(probe, dblLen);
    }
    
    /**
     *  Return the energy gain imparted to a probe object.
     *
     *  @param  dblLen  dummy argument
     *  @param  probe   dummy argument
     *
     *  @return         returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen)    { return 0.0; };
    
    /**
     *  Computes the partial tranfer map for an ideal drift space.  Computes the 
     *  transfer map for a drift of length <code>dblLen</code>.
     *
     *  @param  dblLen  length of drift
     *  @param  probe   requires rest and kinetic energy from probe
     *
     *  @return         transfer map of an ideal drift space for this probe
     *
     *  @exception  ModelException    should not be thrown
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException  {    	
            // Build transfer matrix
            PhaseMatrix  matPhi  = PhaseMatrix.identity();
            
            matPhi.setElem(0, 1, dblLen);
            matPhi.setElem(2, 3, dblLen);
            matPhi.setElem(4, 5, dblLen/Math.pow(probe.getGamma(), 2));
            
            return new PhaseMap(matPhi);        
    }  
};
