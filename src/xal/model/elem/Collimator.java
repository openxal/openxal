/*
 * Marker.java
 *
 * Created on February 13, 2003, 7:41 PM
 */

package xal.model.elem;

import xal.model.IProbe;
import xal.model.PropagationException;
import xal.tools.beam.PhaseMap;




/**
 *  This class is used as a location marker in the modeling lattice.  Is has no
 *  length and no effect on the dynamics of any probe.
 *
 * @author  Christopher Allen
 */
public class Collimator extends ThinElement {
    
    
    /*
     *  Global Attributes
     */
    
    /** type string identifier for all Marker objects */
    public static final String          s_strType = "Collimator";
    
    /** identity phase map used by all Markers as the transfer map */
    private static final PhaseMap s_mapId = PhaseMap.identity();
    
    

    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of Marker 
     *
     *  @param  strId   identifier of this object
     *
     */
    public Collimator(String strId) {
        super(s_strType, strId);
    }
    
    /** 
     *  JavaBean constructor - creates a new uninitialized instance of Marker
     *
     *  <b>BE CAREFUL</b>
     */
    public Collimator() {
        super(s_strType);
    }

    
    
    
    /*
     *  IElement Interface
     */
    
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         the value zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
        return 0.0;
    }
    
    /**
     *  Returns energy gain which is zero.
     *
     *  @param  probe   dummy argument
     *
     *  @return         returns zero
     */
    @Override
    public double energyGain(IProbe probe) { return 0.0;  }
    
    /**  
     *  Returns the transfer map of this element which is the identity.
     *
     *  @param  probe   dummy argument
     *
     *  @return         the identity phase map
     *
     *  @exception  PropagationException  this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws PropagationException { return s_mapId; }
    
}