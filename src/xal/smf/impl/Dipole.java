package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;


/** 
 * The implementation of the Dipole element. This class contains
 * the basic members and methods of main dipoles. Note that
 * there are other classes (e.g. dipoleCorr) that extend this class.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

abstract public class Dipole extends Electromagnet {
	// static initializer
    static {
        registerType();
    }

    
    /** Register type for qualification */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Dipole.class, "dipole");
        typeManager.registerType(Dipole.class, MagnetType.DIPOLE);
    }
  
    
    /**
     * Constructor
	 * @param strID the dipole's unique ID
     */    
    public Dipole( final String strID )     { 
        super( strID );
        
    }
    

    /**
	 * Determine if this magnet has the specified pole
	 * @param pole the pole against which to compare this magnet's pole
	 */
    public boolean isPole( final String pole ) {
        return pole.equals( MagnetType.DIPOLE );
    }
    
    
    /** returns design bend angle of the dipole (deg) */
    public double getBendAngle() {
        return m_bucMagnet.getBendAngle();  // 
    }
}
