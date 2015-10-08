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

import xal.ca.ChannelFactory;
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
        ElementTypeManager.defaultManager().registerTypes( CCL.class, s_strType );
    }


	/**
	 * Primary Constructor for CCL
	 */
	public CCL( final String strId, final ChannelFactory channelFactory, final int intReserve) {
		super( strId, channelFactory, intReserve );
	}


	/**
	 * Constructor for CCL
	 */
	public CCL( final String strId, final ChannelFactory channelFactory ) {
		this( strId, channelFactory, 0 );
	}


	/**
	 * Constructor for CCL
	 */
    public CCL( final String strId ) {
        this( strId, 0 );
    }
	
    
	/**
	 * Constructor for CCL
	 */
	public CCL( final String strId, final int intReserve ) {
		this( strId, null, intReserve );
	}
    
        
    /** 
	 * Support the node type
	 * @return The CCL type
	 */
    public String getType() { 
		return s_strType; 
	}
}

