package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;


/** 
 * The implementation of the BLM class. This class contains the methods
 * members, attributes, and signal sets pertinant to modeling Beam
 * Loss monitors.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

public class BLM extends AcceleratorNode {
      
    /*
     *  Constants
     */
    
    public static final String      s_strType   = "BLM";
  

    // BLM channel handles
    /**
     * BLMs official avg channel handle
     */
    public static final String LOSS_AVG_HANDLE = "lossAvg";
    private Channel lossAvgC = null;

    /**
     * BLMs official integrated channel handle
     */
    public static final String LOSS_INT_HANDLE = "lossInt";
    private Channel lossIntC = null;
    
   /**
     * BLM official tAvgLen channel handle
     */
    public static final String T_AVG_LEN_HANDLE = "tAvgLen";
    private Channel tAvgLenC = null;

    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager.defaultManager().registerTypes( BLM.class, s_strType );
    }


    /*
     *  Local Attributes
     */
        
    
    
    /** Override to provide type signature */
    public String getType()   { return s_strType; };
  
  

	/** Constructor */
    public BLM( final String strId, final ChannelFactory channelFactory ) {
        super( strId, channelFactory );
    }



	/** Constructor */
	public BLM( final String strId ) {
		this( strId, null );
	}


    /*
     *  Process variable Gets
     */

    /**
     * returns average loss
     */
    public double   getLossAvg()  throws ConnectionException, GetException {
	lossAvgC = lazilyGetAndConnect(LOSS_AVG_HANDLE, lossAvgC);
        return lossAvgC.getValDbl();
    }

    /**
     * returns integreated loss
     */
    public double   getLossInt()  throws ConnectionException, GetException {
	lossIntC = lazilyGetAndConnect(LOSS_INT_HANDLE, lossIntC);
        return lossIntC.getValDbl();
    }

    /**
     * returns length of the averaged period (micro-sec)
     */
    public double getTAvgLen() throws ConnectionException, GetException {
	tAvgLenC = lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
	return tAvgLenC.getValDbl();
    }
}
