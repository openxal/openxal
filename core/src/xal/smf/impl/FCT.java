package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;

/**
 * The implementation of the Fast Current Transformer class. This class contains the
 * methods members, attributes, and signal sets pertinant to modeling Fast Current
 * Transformers.
 * 
 * @author Na Wang (wangn@ihep.ac.cn)
 */

public class FCT extends AcceleratorNode {
	
    /*
     *  Constants
     */
	
	public static final String s_strType = "FCT";
	
	/**
     * The container for the fct information
     *
     */
    protected FCTBucket       fctBucket;
	
	// FCT channel handles
	/**
     * FCTs official ampAvg channel handle
     */
    public static final String AMP_AVG_HANDLE = "amplitudeAvg";
    private Channel ampAvgC = null;
    /**
     * FCTs official phaseAvg channel handle
     */
    public static final String PHASE_AVG_HANDLE = "phaseAvg";
    private Channel phaseAvgC = null;
	
	
	
	static {
		registerType();
	}

	/*
	 * Register type for qualification
	 */
	private static void registerType() {
		ElementTypeManager typeManager = ElementTypeManager.defaultManager();
		typeManager.registerType(FCT.class, s_strType);
	}

	/*
	 * Local Attributes
	 */
	
	/** Override to provide type signature */
	public String getType() {
		return s_strType;
	};
	
	/*
	 * User Interface
	 */
	
	public FCT(String strId, final ChannelFactory channelFactory) {
		super(strId, channelFactory );
		// TODO Auto-generated constructor stub
		setFCTBucket(new FCTBucket());
	}
	
	public FCT(String strId) {
		super(strId);
		// TODO Auto-generated constructor stub
		setFCTBucket(new FCTBucket());
	}
	
	/** returnthe FCT Bucket */
    public FCTBucket  getFCTBucket()   { return fctBucket; };   
    
    /**
     * Set the attribute bucket containing the fct info
     */

    public void setFCTBucket(FCTBucket buc) 
        { fctBucket = buc; super.addBucket(buc); };
 
    /**
     *
     * Override AcceleratorNode implementation to check for a FCTBucket
     */

    public void addBucket(AttributeBucket buc)  {

        if (buc.getClass().equals(FCTBucket.class))
              setFCTBucket((FCTBucket) buc);
        super.addBucket(buc);
    }; 
	
    /*
     *  Process variable Gets 
     */	
	/**
     * returns average FCT Amplitude signal over macropulse (au)    
     */
    public double   getAmpAvg()  throws ConnectionException, GetException { 
	ampAvgC = lazilyGetAndConnect(AMP_AVG_HANDLE, ampAvgC);
	return ampAvgC.getValDbl();
    }
    
    
    /**
     * returns average FCT phase signal over macropulse (au)    
     */
    public double   getPhaseAvg()  throws ConnectionException, GetException {
	phaseAvgC = lazilyGetAndConnect(PHASE_AVG_HANDLE, phaseAvgC);
	return phaseAvgC.getValDbl();
    }

	
}

