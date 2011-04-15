package xal.model;


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
     *  Particle Species Parameters
     */
    
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
     *  Probe State Parameters
     */
    
    /**
     * Returns the current lattice element of the probe.
     * 
     * @return <code>String</code> id of current lattice element
     */
    public String getCurrentElement();

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
     *  @return     probe relatistic factor (<b>unitless</b>)
     */
    public double   getGamma();
    


    /**
     * Set the current lattice element.
     * 
     * @param id <code>String</code> id of current lattice element.
     */
    public void setCurrentElement(String id);

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
	 */
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
