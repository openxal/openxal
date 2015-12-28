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
     *  Abstract Protocol for concrete ThinElements
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
        
    
    /**
     * <p>
     * Again, this is a kluge.
     * We return zero since the notion of frequency is not defined for every
     * element (perhaps if this element is the child of an RF cavity).  
     * For those elements that do create a phase advance they
     * need to override this method.
     * </p>
     * <p>
     * There is some legitimacy in returning zero since a thin element generally 
     * has no phase advance. That is, there is no propagation therefore no elapsed
     * time and no phase advance.  Only if there is energy gain must there be a
     * corresponding conjugate phase advance.  
     * </p>
     * 
     * @param probe     probe experiencing a phase advance through this element
     * 
     * @return          the change in phase while going through the element
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    protected double longitudinalPhaseAdvance(IProbe probe) {
        return 0.0;
    }
    
    /*
     *  IComponent Interface
     */
    
    /** 
     *  Return the length of this element 
     *
     *  @return     a value of zero
     */
    @Override
    public double getLength() { return 0.0; };

    
    /*
     *  IElement Interface
     */
    
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
     * Calculate the longitudinal phase advance through this element ignoring the
     * length parameter (or lack thereof). We simply return
     * 0 assume the zero length of this element allows no phase advance.  Of course
     * there are thin elements which do create a finite phase advance (e.g., an
     * RF gap), those element must override this method.
     *
     * @see xal.model.elem.Element#longitudinalPhaseAdvance(xal.model.IProbe, double)
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        return longitudinalPhaseAdvance(probe);
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