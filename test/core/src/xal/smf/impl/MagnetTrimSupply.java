/*
 * MagnetTrimSupply.java
 *
 * Created on June 27, 2003, 3:52 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.tools.data.*;
import xal.ca.*;


/**
 * TrimMagnetSupply is a power supply that represents a trim magnet supply.
 *
 * @author  tap
 */
public class MagnetTrimSupply extends MagnetPowerSupply {
    // channel handles
    public static final String FIELD_SET_HANDLE = "trimSet"; 
    public static final String FIELD_RB_HANDLE = "trimRB";
    public static final String TRIM_CURRENT_SET_HANDLE = "trimI_Set"; 
    public static final String TRIM_CURRENT_RB_HANDLE = "trimI";     
    
    /** Creates a new instance of TrimMagnetSupply */
    public MagnetTrimSupply( final Accelerator anAccelerator ) {
        super( anAccelerator );
    }    
    
    
    /**
     * Get the power supply type
     * @return The power supply type
     */
    public String getType() {
        return "trim";
    }
    
    
    /** 
     * Get the trim field contribution from this power supply.
     * T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
     * @return the field contribution
     */
    public double getField() throws ConnectionException, GetException {
        Channel fieldRBChannel = getAndConnectChannel( FIELD_RB_HANDLE );
        
        return fieldRBChannel.getValDbl();
    }
    

    /** 
     * Set the trim field contribution from this power supply.
     * @param newField is the new field level in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */  
    public void setField( double newField ) throws ConnectionException, PutException {
        Channel fieldSetChannel = getAndConnectChannel( FIELD_SET_HANDLE );
        
        fieldSetChannel.putVal( newField );
    }
	
	
	/**
	 * Get the value to which the field is set.  Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getFieldSetting() throws ConnectionException, GetException {
        Channel fieldSetChannel = getAndConnectChannel( FIELD_SET_HANDLE );
        
        return fieldSetChannel.getValDbl();
	}

    
    /**
     * Get the magnet trim power supply current
     * @return the magnet trim power supply current in amperes
     * @throws xal.ca.ConnectionException if the readback channel cannot be connected
     * @throws xal.ca.GetException if the readback channel get action fails
     */
    public double getTrimCurrent() throws ConnectionException, GetException {
        Channel currentRBChannel = getAndConnectChannel( TRIM_CURRENT_RB_HANDLE );
            
	return currentRBChannel.getValDbl();
    }
    
	
    /**
     * Set the magnet trim power supply current.
     * @param current The current in amperes
     * @throws xal.ca.ConnectionException if the put channel cannot be connected
     * @throws xal.ca.PutException if the put channel set action fails
     */
    public void setTrimCurrent( final double current ) throws ConnectionException, PutException {
        Channel currentSetChannel = getAndConnectChannel( TRIM_CURRENT_SET_HANDLE );
        currentSetChannel.putVal(current);
    }
    

    /** get the field upper settable limit 
        in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */
    public double upperFieldLimit() throws ConnectionException, GetException {
        Channel fieldSetChannel = getAndConnectChannel( FIELD_SET_HANDLE );
        
        return fieldSetChannel.upperControlLimit().doubleValue();
    }

    
    /** get the field lower settable limit 
       in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */  
    public double lowerFieldLimit() throws ConnectionException, GetException {
        Channel fieldSetChannel = getAndConnectChannel( FIELD_SET_HANDLE );
            
        return fieldSetChannel.lowerControlLimit().doubleValue();
    }
    
    
    /**
     * Check if the electromagnet is supplied by this power supply.
     * @param node The electromagnet to check
     * @return true if the node is supplied by this supply and false otherwise
     */
    public boolean suppliesNode( final AcceleratorNode node ) {
        if ( node instanceof TrimmedMagnet ) {
            return this == ((TrimmedMagnet)node).getTrimSupply();
        }
        else {
            return false;
        }
    }
}
