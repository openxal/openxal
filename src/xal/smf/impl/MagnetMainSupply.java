/*
 * MagnetMainSupply.java
 *
 * Created on June 27, 2003, 3:54 PM
 */

package xal.smf.impl;

import xal.smf.*;
import xal.tools.data.*;
import xal.ca.*;


/**
 * MainMagnetSupply represents a power supply that is the main supply for a magnet.
 *
 * @author  tap
 */
public class MagnetMainSupply extends MagnetPowerSupply {
    // channel handles
    public static final String CYCLE_ENABLE_HANDLE = "cycleEnable"; 
    public static final String FIELD_SET_HANDLE = "fieldSet"; 
    public static final String FIELD_RB_HANDLE = "psFieldRB";
	public static final String FIELD_BOOK_HANDLE = "B_Book";		// MPS - field setpoint about which warnings and alarms are specified
    
    
    /** Creates a new instance of MainSupply */
    public MagnetMainSupply( final Accelerator anAccelerator ) {
        super( anAccelerator );
    }
    
    
    /**
     * Get the power supply type
     * @return The power supply type
     */
    public String getType() {
        return "main";
    }
    
    
    /**
     * Set the cycle enable state of the magnet power supply.  If enabled, the magnet will 
     * be cycled when the field is set.
     * @param enable True to enable cycling; false to disable cycling.
     */
    protected void setCycleEnable( final boolean enable ) throws ConnectionException, PutException {
        Channel cycleEnableChannel = getAndConnectChannel( CYCLE_ENABLE_HANDLE );
        
        int flag = (enable) ? 1 : 0;
        cycleEnableChannel.putVal( flag );
    }
    
    
    /** 
     * Get the field contribution from this power supply.
     * T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
     * @return the field contribution
     */
    public double getField() throws ConnectionException, GetException {
        Channel fieldRBChannel = getAndConnectChannel( FIELD_RB_HANDLE );
        
        return fieldRBChannel.getValDbl();
    }
    

    /** 
     * Set the field contribution from this power supply.  If cycle enable is 
     * set to true the field is cycled otherwise it is simply set to the specified
     * value.
     * @param newField is the new field level in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */  
    public void setField(double newField) throws ConnectionException, PutException {
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
	 * get the field upper display limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 * This is the preferred upper field limit for setting magnets as it represents an operational limit.
	 */
    public double upperDisplayFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).upperDisplayLimit().doubleValue();
    }
	
    
    /** 
	 * get the field lower display limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 * This is the preferred lower field limit for setting magnets as it represents an operational limit.
	 */  
    public double lowerDisplayFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).lowerDisplayLimit().doubleValue();
    }
    

    /** get the field upper settable limit 
        in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */
    public double upperFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).upperControlLimit().doubleValue();
    }

    
    /** get the field lower settable limit 
       in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
    */  
    public double lowerFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).lowerControlLimit().doubleValue();
    }
    
	
    /** 
	 * Get the field upper alarm limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
    public double upperAlarmFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).upperAlarmLimit().doubleValue();
    }
	
    
    /** 
	 * Get the field lower alarm limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */  
    public double lowerAlarmFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).lowerAlarmLimit().doubleValue();
    }
    
	
    /** 
	 * Get the field upper warning limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
    public double upperWarningFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).upperWarningLimit().doubleValue();
    }
	
    
    /** 
	 * Get the field lower warning limit in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */  
    public double lowerWarningFieldLimit() throws ConnectionException, GetException {
        return getAndConnectChannel( FIELD_SET_HANDLE ).lowerWarningLimit().doubleValue();
    }
    
    
    /**
     * Check if the electromagnet is supplied by this power supply.
     * @param node The electromagnet to check
     * @return true if the node is supplied by this supply and false otherwise
     */
    public boolean suppliesNode(AcceleratorNode node) {
        if ( node instanceof Electromagnet ) {
            return this == ((Electromagnet)node).getMainSupply();
        }
        else {
            return false;
        }
    }
}
