package xal.model;

import xal.tools.beam.PhaseMap;


/** 
 * <p>
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
     * <p>
     * Returns the longitudinal phase advance of the given probe with respect to the RF phase
     * while propagation through this element section.  
     * Typically used to account for phase delay/advance in cavities incurred due to 
     * finite time while propagating throught the given distance.  For example  
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; &#8796; &phi;<sub>0</sub> - &Delta;&phi; 
     * <br/>
     * <br/>
     * where &Delta;&phi; =  2&pi;<i>f</i>&Delta;<i/>L</i>/&beta;<i>c</i> is the phase delay due 
     * to elapsed time &Delta;<i>t</i> = &Delta;<i>L</i>/&beta;<i>c</i>, &Delta;<i>L</i> is the
     * given axial distance through this element, &beta;<i>c</i> is the probe axial velocity, 
     * <i>f</i> is the cavity 
     * resonant frequency, and &phi;<sub>0</sub> is the operating phase of the cavity (w.r.t.
     * the synchronous particle).
     * </p>
     * 
     * @param probe         the probe progating through this element     
     * @param dblLen        distance the probe propagates through the element
     * 
     * @return              longitudinal phase advance of the probe through given distance
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen);
    
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
