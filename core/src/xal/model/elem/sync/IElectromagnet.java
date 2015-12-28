/*
 * IElectroMagnet.java
 *
 * Created on November 1, 2002, 4:53 PM
 */

package xal.model.elem.sync;

/**
 *  This interface defines the common properties of all electromagnet elements.
 *
 * @author  Christopher K. Allen
 */
public interface IElectromagnet  {
    
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
     *  @return     ORIENT_NONE - no electromagnet orientation (possible error)
     *              ORIENT_HOR  - magnet oriented for action in x (horizontal) plane
     *              ORIENT_VER  - magnet oriented for action in y (vertical) plane
     */
    public int getOrientation();

  
    /**
     *  Get the magnetic field strength of the electromagnet
     *
     *  @return     magnetic field (in <bold>Tesla</bold>).
     */
    public double getMagField();
    

    
    /**
     *  Return the orientation enumeration code.
     *
     *  @param  enmOrient   magnet orientation enumeration code
     *
     *  @see    #getOrientation
     */
    public void setOrientation(int enmOrient);

    /**
     *  Set the magnetic field strength of the electromagnet.
     *
     *  @param  dblField    magnetic field (in <bold>Tesla</bold>).
     */
    public void setMagField(double dblField);
    
};
