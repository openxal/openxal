/*
 * CCL.java
 *
 * Created on Wed Dec 03 11:46:20 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;



/**
 * CCL class to represent the CCL.
 *
 * @author  tap
 */
public class CCL extends RfCavity {
    // ----- Constants ----------------------------------
    public static final String    s_strType = "CCL";
	
	
    static {
        registerType();
    }
	
    
    /**
     * Register CCL's type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(CCL.class, s_strType);
    }
	
	
	/**
	 * Constructor for CCL
	 */
    public CCL(String strId) {
        this(strId, 0);
    }
	
    
	/**
	 * Constructor for CCL
	 */
	public CCL(String strId, int intReserve) {
		super(strId, intReserve);
	}
    
        
    /** 
	 * Support the node type
	 * @return The CCL type
	 */
    public String getType() { 
		return s_strType; 
	}
}

