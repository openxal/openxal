package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;


/** 
 * The implementation of the BPM class. This class contains the methods
 * members, attributes, and signal sets pertinant to modeling Beam
 * Position monitors.
 * 
 * @author  J. Galambos (jdg@ornl.gov)
 */

public class BPM extends AcceleratorNode {      
    /*
     *  Constants
     */
    
    public static final String      s_strType   = "BPM";
  
    /**
     * The container for the bpm information
     *
     */
    protected BPMBucket       bpmBucket; 

    // BPM channel handles
    /**
     * BPMs official xAvg channel handle
     */
    public static final String X_AVG_HANDLE = "xAvg";
    private Channel xAvgC = null;
    /**
     * BPMs official yAvg channel handle
     */
    public static final String Y_AVG_HANDLE = "yAvg";
    private Channel yAvgC = null;
    /**
     * BPMs official ampAvg channel handle
     */
    public static final String AMP_AVG_HANDLE = "amplitudeAvg";
    private Channel ampAvgC = null;
    /**
     * BPMs official phaseAvg channel handle
     */
    public static final String PHASE_AVG_HANDLE = "phaseAvg";
    private Channel phaseAvgC = null;
    /**
     * BPMs official x turn-by-turn channel handle
     */
    public static final String X_TBT_HANDLE = "xTBT";
    private Channel xTBTC = null;
    /**
     * BPMs official y turn-by-turn channel handle
     */
    public static final String Y_TBT_HANDLE = "yTBT";
    private Channel yTBTC = null;
    /**
     * BPMs official amplitude turn-by-turn channel handle
     */
    public static final String AMP_TBT_HANDLE = "ampTBT";
    private Channel ampTBTC = null;
    /**
     * BPMs official phase turn-by-turn channel handle
     */
    public static final String PHASE_TBT_HANDLE = "phaseTBT";
    private Channel phaseTBTC = null;
    /**
     * BPM official tAvgLen channel handle
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
		ElementTypeManager.defaultManager().registerTypes( BPM.class, s_strType );
    }


    /** Override to provide type signature */
    public String getType()   { return s_strType; }


	/** Constructor */
	public BPM( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
		setBPMBucket( new BPMBucket() );
	}


	/** Constructor */
    public BPM( final String strId )     {
        this( strId, null );
    }

    /** returnthe BPM Bucket */
    public BPMBucket  getBPMBucket()   { return bpmBucket; };   
    
    /**
     * Set the attribute bucket containing the bpm info
     */

    public void setBPMBucket(BPMBucket buc) 
        { bpmBucket = buc; super.addBucket(buc); };
 
    /**
     *
     * Override AcceleratorNode implementation to check for a BPMBucket
     */

    public void addBucket(AttributeBucket buc)  {

        if (buc.getClass().equals(BPMBucket.class))
              setBPMBucket((BPMBucket) buc);
        super.addBucket(buc);
    };  
    
    /** the measured horizontal (x) position array,  minipulse by minipulse  (m) */ 
    public double [] xTBT; 

    /** the measured vertical (y) position array,  minipulse by minipulse  (m) */ 
    public double [] yTBT; 

    /** the measured amplitude array, minipulse by minipulse (au) */ 
    public double [] ampTBT;   

    /** the measured phase array,  minipulse by minipulse  (deg) */ 
    public double [] phaseTBT;   

    /*
    /** the measured horizontal (x) position array,  raw values  (m) 
    public double [] xRaw; 

    /** the measured vertical (y) position array,  raw values  (m)  
    public double [] yRaw; 

    /** the measured phase array,  raw values (deg) 
    public double [] phaseRaw;   
    */

    /*
     *  Process variable Gets 
     */

    /**
     * returns average X position over macropulse (mm) accounting for alignment
     */
    public double   getXAvg()  throws ConnectionException, GetException {
	xAvgC = lazilyGetAndConnect(X_AVG_HANDLE, xAvgC);
        return xAvgC.getValDbl();
    }

    
    /**
     * returns average Y position over macropulse (mm) accounting for alignment
     */
    public double   getYAvg()  throws ConnectionException, GetException { 
	yAvgC = lazilyGetAndConnect(Y_AVG_HANDLE, yAvgC);
	return yAvgC.getValDbl();
    }
    
    
    /**
     * returns average bpm Amplitude signal over macropulse (au)    
     */
    public double   getAmpAvg()  throws ConnectionException, GetException { 
	ampAvgC = lazilyGetAndConnect(AMP_AVG_HANDLE, ampAvgC);
	return ampAvgC.getValDbl();
    }
    
    
    /**
     * returns average bpm phase signal over macropulse (au)    
     */
    public double   getPhaseAvg()  throws ConnectionException, GetException {
	phaseAvgC = lazilyGetAndConnect(PHASE_AVG_HANDLE, phaseAvgC);
	return phaseAvgC.getValDbl();
    }
    
    /**
     * returns bpm x turn-by-turn array    
     */
    public double[] getXTBT() throws ConnectionException, GetException {
        xTBTC = lazilyGetAndConnect(X_TBT_HANDLE, xTBTC);
	return xTBTC.getArrDbl();
    }    
    
    /**
     * returns bpm y turn-by-turn array    
     */
    public double[] getYTBT() throws ConnectionException, GetException {
        yTBTC = lazilyGetAndConnect(Y_TBT_HANDLE, yTBTC);
	return yTBTC.getArrDbl();
    }    
    
    /**
     * returns bpm amplitude turn-by-turn array    
     */
    public double[] getAmpTBT() throws ConnectionException, GetException {
        ampTBTC = lazilyGetAndConnect(AMP_TBT_HANDLE, ampTBTC);
	return ampTBTC.getArrDbl();
    }    
    
    /**
     * returns bpm phase turn-by-turn array    
     */
    public double[] getPhaseTBT() throws ConnectionException, GetException {
        phaseTBTC = lazilyGetAndConnect(PHASE_TBT_HANDLE, phaseTBTC);
	return phaseTBTC.getArrDbl();
    }    
    
    /**
     * returns length of the averaged period (micro-sec)
     */
    public double getTAvgLen() throws ConnectionException, GetException {
	tAvgLenC = lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
	return tAvgLenC.getValDbl();
    }
}
