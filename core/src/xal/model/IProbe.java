package xal.model;

import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.tools.data.IArchive;


/** 
 * The root interface of physical objects and mathematical abstractions
 * used in UAL applications (for example: Bunch, Particle, Twiss, Taylor 
 * map, and others). According to the Element-Algorithm-Probe analysis 
 * pattern, the Probe objects interact with accelerator structures, 
 * Element's. These interactions are implemented as the separate 
 * classes (Tracker, Mapper, etc.) that support the Algorithm interface.
 * 
 * @author  Nikolay Malitsky, Christopher K. Allen
 * @version $Id: IProbe.java 2 2006-08-17 12:20:30 +0000 (Thursday, 17 8 2006) t6p $
 */

public interface IProbe {

    /*
     *  Physical Constants
     */
    
    /** Speed of light in vacuum (meters/second) */
    public final double LightSpeed = 299792458;   
    
    /** The unit electric charge (Farads) */
    public final double UnitCharge = 1.602e-19;
    
    /** Electric permittivity of free space (Farad/meter) */
    public final double Permittivity = 8.854187817e-12;
  
    
    /*
     * Probe History
     */
    
    /**
     * Returns the state history of the probe.  The returned object is essentially
     * the simulation results for the online model.  The <code>Trajectory</code>
     * object contains a complete collection of all the probe state object generated
     * during the simulation. 
     * 
     * @return  the simulation results for the probe
     *
     * @author Christopher K. Allen
     * @since  Nov 24, 2014
     */
    public Trajectory<? extends ProbeState<?>> getTrajectory();
    
    
    /*
     *  Particle Species Parameters
     */

	/**
	 * returns the species name
	 * @return species name
	 */
	public String getSpeciesName();

	
    /**
     *  Returns the charge of probe's particle species 
     *  
     *  @return     particle species charge in units of positive electron charge
     */
    public double getSpeciesCharge();
	
    
    /** 
     *  Returns the rest energy of particle species 
     *
     *  @return     particle species rest energy in electron volts
     */
    public double getSpeciesRestEnergy();
    
    

    
    /*
     *  Probe Current State Parameters
     */
    
    /**
     * Returns the current lattice element of the probe.
     * 
     * @return <code>String</code> id of current lattice element
     */
    public String getCurrentElement();
    
    /**
     * Get the type identifier string of the modeling element where this
     * probe currently exists. Note that all modeling elements have a static string
     * identifier defined in their class definition.
     * 
     * @param strTypeId     static identifier string of the modeling element class
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public String getCurrentElementTypeId();
    
    /**
     * Returns the identifier of the hardware being modeling
     * by the current element.
     * 
     * @return  hardware ID of the current modeling element
     *
     * @author Christopher K. Allen
     * @since  Sep 3, 2014
     */
    public String getCurrentHardwareId();

    /** 
     *  Returns the current beam-line position of the probe 
     *  
     *  @return     probe position (<b>meters</b>)
     */
    public double   getPosition();
    
    /**
     * Return the time elapsed since the probe began propagation.
     * 
     * @return      elapsed time in <b>seconds</b>
     */
    public double   getTime();
    
    /**
     * <p>
     * Returns the longitudinal phase of this probe with respect to the RF phase.  
     * Typically used to account for phase delay/advance in cavities incurred due to 
     * finite propagation time.  For example  
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; &#8796; &phi;<sub>0</sub> - &Delta;&phi; 
     * <br/>
     * <br/>
     * where &Delta;&phi; =  2&pi;<i>f</i>&Delta;<i/>t</i> is the phase delay due 
     * to elapsed time &Delta;<i>t</i>, <i>f</i> is the cavity 
     * resonant frequency, and &phi;<sub>0</sub> is the operating phase of the cavity (w.r.t.
     * the synchronous particle).
     * </p>
     * 
     * @return      the probe phase &phi; with respect to the machine RF frequency
     * 
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    public double   getLongitinalPhase();
    
    /**
     *  Return the kinetic energy of the probe.  Depending upon the probe type,
     *  this could be the actual kinetic energy of a single constituent particle,
     *  the average kinetic energy of an ensemble, the design energy, etc.
     *
     *  @return     probe kinetic energy    (<b>electron-volts</b>)
     */
    public double   getKineticEnergy();
    
    /** 
     *  Returns the probe velocity normalized to the speed of light. 
     *
     *  @return     normalized probe velocity v/c (<b>unitless</b>
     */
    public double   getBeta();
    
    /** 
     *  Returns the relativistic parameter corresponding to the probe 
     *  velocity.
     *  The relativistic factor is given by the formulae
     *      gamma =  (Kinetic Energy/Rest Energy) + 1 
     *            = sqrt[1/(1-v^2/c^2)]
     *
     *  @return     probe relativistic factor (<b>unitless</b>)
     */
    public double   getGamma();
    
//    /**
//     * Returns the time at which the probe being tracked exited the last RF gap.
//     * 
//     * @return      probe time at which the last RF gap was exited (in seconds)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 24, 2014
//     */
//    public double   getRfGapExitTime();
//    
//    /**
//     * Returns the machine RF phase at the last gap through which the probe propagated.
//     * This value accounts for the RF cavity structure, specifically the phase shifts
//     * due to coupling between coupled cavity structures.
//     *  
//     * @return  phase shift experienced by probe when traversing coupled cavities
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 25, 2014
//     * 
//     * @deprecated  This method was part of an old design
//     */
//    @Deprecated
//    public double   getCoupledCavityPhase();
    
    /**
     * Looks up the last probe state created by a modeling
     * element of the given identifier.  The history of the probe (i.e., a
     * <code>Trajectory</code> object is searched for the last state with
     * the modeling type ID equal to the given value.  
     * 
     * @param strElemTypeId     modeling element type identification string
     * 
     * @return                  the last state created by such a modeling element
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public ProbeState<?>   lookupLastStateFor(String strElemTypeId);
    
    /*
     * State Initialization
     */
    
    /**
     * Set the current lattice element.
     * 
     * @param id <code>String</code> id of current lattice element.
     */
    public void setCurrentElement(String id);
    
    /**
     * Sets the type identifier string of the modeling element where this
     * probe currently exists. Note that all modeling elements have a static string
     * identifier defined in their class definition.
     * 
     * @param strTypeId     static identifier string of the modeling element class
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public void setCurrentElementTypeId(String strTypeId);
    
    /**
     * Sets the string identifier of the hardware node modeled by
     * the current element.
     * 
     * @param strSmfId  hardware ID of the current modeling element
     *
     * @author Christopher K. Allen
     * @since  Sep 3, 2014
     */
    public void setCurrentHardwareId(String strSmfId);

    /** 
     *  Set the current position of the probe along the beamline.
     *
     *  @param  dblPos       new probe position (<b>meters</b>)
     */
    public void setPosition(double dblPos);
    
    /** 
     * Set the current time since the probe began propagating
     * 
     * @param   dblTime     new probe current time in <b>seconds</b>
     * 
     * @author jdg
     */
    public void setTime(double dblTime);

    /**
     * <p>
     * Set the longitudinal phase of this probe with respect to the RF phase.  
     * Typically used to account for phase delay/advance in cavities incurred due to 
     * finite propagation time.  For example  
     * <br/>
     * <br/>
     * &nbsp; &nbsp; &phi; &#8796; &phi;<sub>0</sub> - &Delta;&phi; 
     * <br/>
     * <br/>
     * where &Delta;&phi; =  2&pi;<i>f</i>&Delta;<i/>t</i> is the phase delay due 
     * to elapsed time &Delta;<i>t</i>, <i>f</i> is the cavity 
     * resonant frequency, and &phi;<sub>0</sub> is the operating phase of the cavity (w.r.t.
     * the synchronous particle).
     * </p>
     * 
     * @param dblPhsLng     the phase delay &Delta;&phi; incurred from probe
     *                          propagate between RF cavities
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    public void setLongitudinalPhase(double dblPhsLng);
    
    /**
     *  Set the current kinetic energy of the probe.
     *
     *  @param  dblW       new probe kinetic energy (<b>electron-volts</b>)
     *
     *  @see    #getKineticEnergy
     */
    public void setKineticEnergy(double dblW);
    
    
    /**
     *  Set the species charge.
     *
     *  @param  dblQ       new species charge (<b>e.u.</b>)
     *
     *  @see    #getSpeciesCharge
     */
    public void setSpeciesCharge(double dblQ);
    
    /**
     *  Set the species rest energy.
     *
     *  @param  m       new species rest energy (<b>eV</b>)
     *
     *  @see    #getSpeciesRestEnergy
     */
    public void setSpeciesRestEnergy(double m);
   
//    /**
//     * Sets the time at which the currently tracked probe exited the
//     * last RF gap structure it propagated through.
//     * 
//     * @param dblRfGapExitTime      gap exit time (in seconds)
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 24, 2014
//     */
//    public void setRfGapExitTime(double dblRfGapExitTime);
//
//    /**
//     * Returns the RF phase at the last gap through which the probe propagated.
//     * This value accounts for the RF cavity structure, specifically the phase shifts
//     * due to coupling between coupled cavity structures.
//     *  
//     * @return  phase shift experienced by probe when traversing coupled cavities
//     *
//     * @author Christopher K. Allen
//     * @since  Nov 25, 2014
//     * 
//     * @deprecated This was part of an old design
//     */
//    @Deprecated
//    public void setCoupledCavityPhaseShift(double dblCavPhsShft);
    
    
    /*
     * Propagation
     */
     
//    /**
//     * Set the element to start propagation from.
//     * 
//     * @param id <code>String</code> id of the element from which to start propagation
//     */
//    public void setStartElementId(String id);
//    
//    /**
//     * Set the element to stop propagation at.
//     * 
//     * @param id <code>String</code> id of the element at which to stop propagation
//     */
//    public void setStopElementId(String id);
//    
    /**
     *  Initialize the probe for propagation.
     *      Reset any necessary state variables
     *      Optionally set up a trajectory variable
     *
     *  @exception  ModelException  an error occurred while trying to initialize the probe
     */
    public void initialize() throws ModelException;
    
    /**
     *  Update all data and connections.
     *      Recompute any parameters consistent with new probe state
     *      Optionally store probe state to save trajectory history
     *      Resynchronize probe with data sources if they exist
     *
     *  @exception  ModelException  an error occurred while trying to update the probe
     */
    public void update() throws ModelException;
	
	
	/**
	 * Perform any required post processing upon completion of algorithm processing.
	 * 
	 *  @deprecated    This has a zero implementation in <code>Probe</code> but never does
	 *                 anything.
	 */
    @Deprecated
	public void performPostProcessing();
    


    /*
     * Other Interfaces
     */

    /**
     *  Return the dynamics algorithm associated with this probe.
     */
    public IAlgorithm getAlgorithm();

    
    /**
     * Return the <code>IArchive</code> interface for archiving
     * this algorithm object.
     * 
     * @author Christopher Allen    11/17/03
     */
    public IArchive getArchive();

    
    
};
