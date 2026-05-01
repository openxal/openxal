//
//  IProbeState.java
//  xal
//
//  Created by Thomas Pelaia on 2/9/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.model.probe.traj;

import xal.tools.data.IArchive;


/**
 * The interface required of all classes encapsulating
 * the state data of probes.
 *
 * @author Christopher K. Allen
 * @since   Apr 14, 2011
 */
public interface IProbeState extends IArchive {
    
	
    /** Speed of light in vacuum (meters/second) */
    public final double LightSpeed = 299792458;  
	
    /** 
	*  Set the charge of the particle species in the beam 
	*  
	*  @param  q       species particle charge (<b>Coulombs</b>)
	*/
    public void setSpeciesCharge(double q);
    
    
    /** 
	*  Set the rest energy of a single particle in the beam 
	*
	*  @param  Er      particle rest energy (<b>electron-volts</b>)
	*/
    public void setSpeciesRestEnergy(double Er);
	
	
	
    /** 
	*  Set the current position of the probe along the beamline.
	*
	*  @param  s       new probe position (<b>meters</b>)
	*
	*  @see    #getPosition
	*/
    public void setPosition(double s);
    
	
    /** 
	* Set the current probe time elapsed from the start of the probe tracking.
	*  
	* @param   dblTime     elapsed time in <b>seconds</b>
	*/
    public void setTime(double dblTime);
	
	
    /**
	*  Set the current kinetic energy of the probe.
     *
     *  @param  W       new probe kinetic energy (<b>electron-volts</b>)
     *
     *  @see    #getKineticEnergy
     */
    public void setKineticEnergy(double W);
	
    
    /**
	 * Set the lattice element id associated with this state.
     * 
     * @param id  element id of current lattice element
     */
    public void setElementId(String id);
    
	
	
    /*
     *  Data Query 
     */
	
	
    /** 
	*  Returns the charge of probe's particle species 
	*  
	*  @return     particle species charge (<b>Coulombs</b>)
	*/
    public double getSpeciesCharge();
    
	
    /** 
	*  Returns the rest energy of particle species 
	*
	*  @return     particle species rest energy (<b>electron-volts</b>)
	*/
    public double getSpeciesRestEnergy();
    
	
	
    /**
	* Returns the id of the lattice element associated with this state.
     * 
     * @return  string ID of associated lattice element
     */
    public String getElementId();
    
	
    /** 
	*  Returns the current beam-line position of the probe 
	*  
	*  @return     probe position (<b>meters</b>)
	*/
    public double getPosition();
    
	
    /** 
	* Return the time elapsed from the start of the probe tracking
	* 
	* @return      time elapsed since probe began tracking, in <b>seconds</b> 
	*/
    public double getTime();
    
	
    /**
	*  Return the kinetic energy of the probe.  Depending upon the probe type,
     *  this could be the actual kinetic energy of a single constituent particle,
     *  the average kinetic energy of an ensemble, the design energy, etc.
     *
     *  @return     probe kinetic energy    (<b>electron-volts</b>)
     */
    public double getKineticEnergy();
}
