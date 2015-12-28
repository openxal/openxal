/**
 * IdealHorMagSteeringDipole.java
 *
 * @author Christopher K. Allen
 * @since  May 13, 2011
 *
 */

/**
 * IdealHorMagSteeringDipole.java
 *
 * @author  Christopher K. Allen
 * @since	May 13, 2011
 */
package xal.model.elem;

import xal.model.elem.sync.IElectromagnet;
import xal.smf.Accelerator;

/**
 * Represents an ideal magnetic steering dipole in the horizontal
 * direction.  All the functionality occurs in the base class
 * <code>{@link IdealMagSteeringDipole}</code>.  This purpose of this
 * class is to avoid the use of the enumeration constants in 
 * <code>{@link IElectromagnet}</code> which define the orientation.
 * They are brittle being of the legacy construction (with <code>int</code>
 * values) and the mechanism is problematic when generating a model from
 * the SMF <code>{@link Accelerator}</code> tree.
 * 
 *
 * @author Christopher K. Allen
 * @since   May 13, 2011
 */
public class IdealHorMagSteeringDipole extends IdealMagSteeringDipole {

    
    /*
     * Initialization
     */
    
    /**
     * Default constructor - creates a new uninitialized instance 
     * of <code>IdealHorMagSteeringDipole</code>.  Typically used 
     * by automatic lattice generation.
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealHorMagSteeringDipole() {
        super.setOrientation(ORIENT_HOR);
    }

    /**
     * Create a new instance of <code>IdealHorMagSteeringDipole</code>
     * and specify its instance identifier.
     * 
     * @param   strId   string instance identifier of element
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealHorMagSteeringDipole(String strId) {
        super(strId);
        super.setOrientation(ORIENT_HOR);
    }

    /**
     *  Creates a new instance of <code>IdealHorMagSteeringDipole</code>.  
     *  The action of the kicker is completely unspecified.  
     *
     *  @param  strId       string identifier of element
     *  @param  dblFld      field strength (in <b>Tesla</b>)
     *  @param  dblLenEff   effective length of dipole magnet
     *
     * @author  Christopher K. Allen
     * @since   May 13, 2011
     */
    public IdealHorMagSteeringDipole(String strId, double dblLenEff, double dblFld) {
        super(strId, dblLenEff, IElectromagnet.ORIENT_HOR, dblFld);
    }

}
