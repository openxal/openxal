/*
 * PermanentMagnet.java
 *
 * Created on January 30, 2002, 2:02 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/**
 * PermanentMagnet is the superclass of all permanent magnet classes.
 *
 * @author  tap
 */
abstract public class PermanentMagnet extends Magnet {

    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(PermanentMagnet.class, "pmag");
        typeManager.registerType(PermanentMagnet.class, "permanentmagnet");
    }
    
    
    /** Creates new PermanentMagnet */
    public PermanentMagnet(String strId) {
        super(strId);
    }
      
         
    /**
     * Since this is a permanent magnet we override the inherited method to 
     * advertise this characteristic.
     * @return true since all PermanentMagnet instances are permanent magnets.
     */
    public boolean isPermanent() {
        return true;
    }

    
    /** 
     * returns the field of the magnet (T /(m^ (n-1))), n=1 for dipole,
     * 2 for quad etc.
     */

    public double getField() {
        
        return getDesignField();
    }

    /** 
     * returns the integrated field of the magnet (T-m /(m^ (n-1))), 
     *  n=1 for dipole, 2 for quad etc.
     */
    public double getFieldInt() {
        
        return (getDesignField() * getEffLength());
    }
}
