/*
 * IElectrostatic.java
 *
 * Created on November 1, 2002, 4:53 PM
 */

package xal.model.elem.sync;


/**
 *  This interface defines the common properties of all electrostatic elements.
 *
 * @author  Carla Benatti, adapted from Christopher K. Allen's IElectromagnet.java
 */
public interface IElectrostatic  {
    
    /*
     *  Orientation Enumeration
     */
    
    /** no dipole orientation given - indicates error condition for oriented elements */
    public final int    ORIENT_NONE = 0;
    
    /** dipole is oriented to provide action in the horizontal plane */
    public final int    ORIENT_HOR  = 1;
    
    /** dipole is oriented to provide action in the vertical plane */
    public final int    ORIENT_VER  = 2;
    
    
    
    /**
     *  Return the orientation enumeration code.
     *
     *  @return     ORIENT_NONE - no electrostatic orientation (possible error)
     *              ORIENT_HOR  - magnet oriented for action in x (horizontal) plane
     *              ORIENT_VER  - magnet oriented for action in y (vertical) plane
     */
    public int getOrientation();

  
    /**
     *  Get the voltage applied to the electrostatic device
     *
     *  @return     Voltage (in <bold>kV</bold>).
     */
    public double getVoltage();
    

    
    /**
     *  Return the orientation enumeration code.
     *
     *  @param  enmOrient   magnet orientation enumeration code
     *
     *  @see    #getOrientation
     */
    public void setOrientation(int enmOrient);

    /**
     *  Set the voltage applied to the electrostatic device.
     *
     *  @param  dblVoltage    Voltage (in <bold>kV</bold>).
     */
    public void setVoltage(double dblVoltage);
    
};
