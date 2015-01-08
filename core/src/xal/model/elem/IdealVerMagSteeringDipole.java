/**
 * IdealVerMagSteeringDipole.java
 *
 * @author Christopher K. Allen
 * @since  May 13, 2011
 *
 */

/**
 * IdealVerMagSteeringDipole.java
 *
 * @author  Christopher K. Allen
 * @since	May 13, 2011
 */
package xal.model.elem;

import xal.model.elem.sync.IElectromagnet;


/**
 * Represents an ideal magnetic steering dipole in the vertical
 * direction.  All the functionality occurs in the base class
 * <code>{@link IdealMagSteeringDipole}</code>.  This purpose of this
 * class is to avoid the use of the enumeration constants in 
 * <code>{@link IElectromagnet}</code> which define the orientation.
 * They are brittle being of the legacy construction (with <code>int</code>
 * values) and the mechanism is problematic when generating a model from
 * the SMF <code>{@link Accelerator}</code> tree.
 *
 * @author Christopher K. Allen
 * @since   May 13, 2011
 */
public class IdealVerMagSteeringDipole extends IdealMagSteeringDipole {

    /**
     * Default constructor - creates a new uninitialized instance 
     * of <code>IdealVerMagSteeringDipole</code>.  Typically used 
     * by automatic lattice generation.
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealVerMagSteeringDipole() {
        super.setOrientation(ORIENT_VER);
    }

    /**
     * Create a new instance of <code>IdealVerMagSteeringDipole</code>
     * and specify its instance identifier.
     * 
     * @param   strId   string instance identifier of element
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealVerMagSteeringDipole(String strId) {
        super(strId);
        super.setOrientation(ORIENT_VER);
    }

    /**
     *  Creates a new instance of <code>IdealVerMagSteeringDipole</code>.  
     *  The action of the kicker is completely unspecified.  
     *
     *  @param  strId       string identifier of element
     *  @param  dblFld      field strength (in <b>Tesla</b>)
     *  @param  dblLenEff   effective length of dipole magnet
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealVerMagSteeringDipole(String strId, double dblLenEff, double dblFld) {
        super(strId, dblLenEff, IElectromagnet.ORIENT_VER, dblFld);
    }
    

}
