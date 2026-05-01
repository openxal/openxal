/*
 * ThinElement.java
 *
 * Created on October 22, 2002, 2:39 PM
 */

package xal.model.elem;

import xal.tools.beam.PhaseMap;

import xal.model.IProbe;
import xal.model.ModelException;


/**
 *  Classes derived from ThinElement are modeled as having no length.  Thus, space 
 *  charge effects are neglected here.  The IElement interface method getLength()
 *  always returns zero.  However, a ThinElement may have an "effective length" which
 *  is a parameter used to determine it's effect on the beam, that is, it is used to
 *  compute the transfer matrix.
 *
 * @author  Christopher Allen
 */
public abstract class ThinElement extends Element {
    

    /*
     *  Initialization
     */
    
 
    /** 
     * Creates a new instance of ThinElement specifying the element type
     * identifier.
     *
     *  @param  strType     type identifier string of element
     */
    public ThinElement(String strType) {
        super(strType);
    };
    
    /** 
     * Creates a new instance of ThinElement specifying with the element
     * type identifier and the instance identifier.
     *
     *  @param  strType     type string of element
     *  @param  strId       string identifier of the element
     */
    public ThinElement(String strType, String strId) {
        super(strType, strId);
    };
    
    
    /*
     *  Abstact Protocol for concrete ThinElements
     */
     
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         elapsed time through element <b>Units: seconds</b> 
     */
    protected abstract double elapsedTime(IProbe probe);
    
    /**
     * Calculate the energy gain for this element on the supplied probe.
     *
     * @return this element's energy gain
     */
    protected abstract double energyGain(IProbe probe);
    
    /**
     * Compute the transfer matrix of this element.
     * 
     * @return transfer map for this element
     */
    protected abstract PhaseMap transferMap(IProbe probe) throws ModelException;
        
    
    /*
     *  IElement Interface
     */
    
    /** 
     *  Return the length of this element 
     *
     *  @return     a value of zero
     */
    @Override
    public double getLength() { return 0.0; };
    
    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     * 
     *  @param  probe   propagating probe
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<b>Units: seconds</b> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return elapsedTime(probe);
    }
    
    /**
     * Calculate the energy gain for this element.  Because this is a thin element
     * with no length, the length parameter is ignored.
     * 
     * @param probe  Probe for which energy gain is to be computed
     * 
     * @return  the energy gain for this element on the particular probe
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
    	return energyGain(probe);
    }
    
    /**
     *  Compute the transfer matrix for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.  Because this is a thin
     *  element (with no length), the length parameter is ignored in computing the
     *  transfer matrix.
     *
     *  @param  dblLen      length of subelement
     *  @param  probe       probe containing parameters for the subsectional transfer matrix
     *
     *  @return             transfer map for an element of length dblLen
     *
     *  @exception  ModelException    unable to compute transfer map
     *
     *  @see    #transferMap()
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dblLen) 
    	throws ModelException
    {
    	return transferMap(probe);
    }

};