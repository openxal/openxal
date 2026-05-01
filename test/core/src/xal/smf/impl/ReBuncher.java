/*
 * ReBuncher.java
 *
 * Created on January 24, 2002, 5:17 PM
 */

package xal.smf.impl;

import xal.ca.ChannelFactory;
import xal.smf.impl.qualify.ElementTypeManager;


/**
 *
 * @author  tap
 */
public class ReBuncher extends RfCavity {
    /*
     *  Constants
     */
    
    public static final String s_strType = "Bnch";  // ??? no table entry for the type
  

    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( ReBuncher.class, s_strType, "rebuncher" );
    }
    

    /** Override to provide type signature */
    public String getType() { 
        return s_strType; 
    }


	/**
	 * Primary Constructor.
	 * @param strId node identifier
	 * @param channelFactory factory for generating channels
	 */
	public ReBuncher( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


    /**
     * I just added this comment - didn't do any work.
     * 
     * @param strId     identifier string of the DTL
     *
     * @author  Christopher K. Allen
     * @since   May 3, 2011
     */
    public ReBuncher( final String strId ) {
        this( strId, null );
    }
    
    
    
    /*
     *  Attributes
     */
}
