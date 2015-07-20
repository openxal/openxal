package xal.model;

import xal.tools.beam.PhaseMap;


/** 
 * The defining interface for atomic accelerator modeling components.
 * (for example, quadrupoles, foil, and others). According to the 
 * Element-Algorithm-Probe design pattern, the <code>IElement</code>
 * exposed objects may interact with physical aspects of the particle
 * beam which are expressed by the <code>IProbe</code> interface. 
 * <p>
 * These interactions between <code>IElement</code> objects and 
 * <code>IProbe</code> objects are defined by dynamics objects exposing
 * the <code>IAlgorithm</code> interface.  
 *
 * @author  Christopher K. Allen
 * @version $Id: Element.java, ver 2.0
 */

public interface IElement extends IComponent {
    

    
    /*
     *  Physical Constants
     */
    
    /** Speed of light in a vacuum (meters/second) */
    public final double LightSpeed = 299792458;   
    
    /** The unit electric charge (Farads) */
    public final double UnitCharge = 1.602e-19;
    
    /** Electric permittivity of free space (Farad/meter) */
    public final double Permittivity = 8.854187817e-12;
  
    /** Magnetic permeability of free space (Henries/meter) */
    public final double Permeability = 4.0*Math.PI*1.0e-7;
    


    
    
    /*
     * Dynamics
     */
    
    
    /**
     * Returns the time taken for the probe <code>probe</code> to propagate 
     * through a subsection of the element with length <code>dblLen</code>.
     * 
     *  @param  probe   propagating probe
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<b>Units: seconds</b> 
     */
    public double elapsedTime(IProbe probe, double dblLen);
    
    /** 
     *  Returns energy gain provided by a subsection of the element during the
     *  given length for the particular probe.
     *
     *  @param  probe   determine energy gain for this probe
     *  @param  dblLen  length of subsection to calculate energy gain 
     *
     *  @return         the energy gain provided by this element <b>Units: eV</b> 
     */
    public double energyGain(IProbe probe, double dblLen);
    
    /**
     *  Compute the transfer matrix for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.  That is, this method should 
     *  return the incremental transfer matrix.
     *
     *  @param  dblLen      length of sub-element
     *  @param  probe       probe containing parameters for the sub-sectional transfer matrix
     *
     *  @return             transfer map for an element of length dblLen
     *
     *  @exception  ModelException    unable to compute transfer map
     *
     *  @see    #transferMap(IProbe, double)
     */
    public PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;
};
