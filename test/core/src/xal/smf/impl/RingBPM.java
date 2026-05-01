package xal.smf.impl;

import xal.smf.impl.qualify.*;
import xal.ca.*;

/**
 * This class is for SNS Ring BPMs. The Ring BPMs can set up to 4 different
 * gains within its 1060 waveform points. They also have 2 modes: base-band and
 * 402.5 MHz.
 * 
 * @author chu
 * 
 */
public class RingBPM extends BPM {

	public static final String s_strType = "RBPM";

	/**
	 * BPM stage 1 length channel handle
	 */
	public static final String STAGE1_LEN_HANDLE = "Stage1Len";

	/**
	 * BPM stage 1 gain channel handle
	 */
	public static final String STAGE1_GAIN_HANDLE = "Stage1Gain";

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE1_METHOD_HANDLE = "Stage1Method";

	/**
	 * BPM stage 2 length channel handle
	 */
	public static final String STAGE2_LEN_HANDLE = "Stage2Len";

	/**
	 * BPM stage 2 gain channel handle
	 */
	public static final String STAGE2_GAIN_HANDLE = "Stage2Gain";

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE2_METHOD_HANDLE = "Stage2Method";

	/**
	 * BPM stage 3 length channel handle
	 */
	public static final String STAGE3_LEN_HANDLE = "Stage3Len";

	/**
	 * BPM stage 3 gain channel handle
	 */
	public static final String STAGE3_GAIN_HANDLE = "Stage3Gain";

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE3_METHOD_HANDLE = "Stage3Method";

	/**
	 * BPM stage 4 length channel handle
	 */
	public static final String STAGE4_LEN_HANDLE = "Stage4Len";

	/**
	 * BPM stage 4 gain channel handle
	 */
	public static final String STAGE4_GAIN_HANDLE = "Stage4Gain";

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE4_METHOD_HANDLE = "Stage4Method";

	/**
	 * BPM stage 1 length channel handle
	 */
	public static final String STAGE1_LEN_RB_HANDLE = "Stage1LenRB";

	private Channel stage1LenRBC = null;

	/**
	 * BPM stage 1 gain channel handle
	 */
	public static final String STAGE1_GAIN_RB_HANDLE = "Stage1GainRB";

	private Channel stage1GainRBC = null;

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE1_METHOD_RB_HANDLE = "Stage1MethodRB";

	private Channel stage1MethodRBC = null;

	/**
	 * BPM stage 2 length channel handle
	 */
	public static final String STAGE2_LEN_RB_HANDLE = "Stage2LenRB";

	private Channel stage2LenRBC = null;

	/**
	 * BPM stage 2 gain channel handle
	 */
	public static final String STAGE2_GAIN_RB_HANDLE = "Stage2GainRB";

	private Channel stage2GainRBC = null;

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE2_METHOD_RB_HANDLE = "Stage2MethodRB";

	private Channel stage2MethodRBC = null;

	/**
	 * BPM stage 3 length channel handle
	 */
	public static final String STAGE3_LEN_RB_HANDLE = "Stage3LenRB";

	private Channel stage3LenRBC = null;

	/**
	 * BPM stage 3 gain channel handle
	 */
	public static final String STAGE3_GAIN_RB_HANDLE = "Stage3GainRB";

	private Channel stage3GainRBC = null;

	/**
	 * BPM stage 1 method channel handle
	 */
	public static final String STAGE3_METHOD_RB_HANDLE = "Stage3MethodRB";

	private Channel stage3MethodRBC = null;

	/**
	 * BPM stage 4 length channel handle
	 */
	public static final String STAGE4_LEN_RB_HANDLE = "Stage4LenRB";

	private Channel stage4LenRBC = null;

	/**
	 * BPM stage 4 gain channel handle
	 */
	public static final String STAGE4_GAIN_RB_HANDLE = "Stage4GainRB";

	private Channel stage4GainRBC = null;

	/**
	 * BPM stage 4 method channel handle
	 */
	public static final String STAGE4_METHOD_RB_HANDLE = "Stage4MethodRB";

	private Channel stage4MethodRBC = null;


	/**
	 * RingBPM constructor.
	 */
	public RingBPM( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


	/**
	 * RingBPM constructor.
	 */
	public RingBPM( final String strId ) {
		this( strId, null );
	}


	static {
		registerType();
	}

	/**
	 * Register type for qualification. These are the types that are common to
	 * all instances. The <code>isKindOf</code> method handles the type
	 * qualification specific to an instance.
	 * 
	 * @see #isKindOf
	 */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( RingBPM.class, s_strType, "BPM" );
	}

	/**
	 * returns stage 1 length
	 */
	public int getStage1Len() throws ConnectionException, GetException {
		stage1LenRBC = lazilyGetAndConnect(STAGE1_LEN_RB_HANDLE, stage1LenRBC);
		return stage1LenRBC.getValInt();
	}

	/**
	 * returns stage 1 gain
	 */
	public int getStage1Gain() throws ConnectionException, GetException {
		stage1GainRBC = lazilyGetAndConnect(STAGE1_GAIN_RB_HANDLE, stage1GainRBC);
		return stage1GainRBC.getValInt();
	}

	/**
	 * returns stage 1 method
	 */
	public int getStage1Method() throws ConnectionException, GetException {
		stage1MethodRBC = lazilyGetAndConnect(STAGE1_METHOD_RB_HANDLE, stage1MethodRBC);
		return stage1MethodRBC.getValInt();
	}

	/**
	 * returns stage 2 length
	 */
	public int getStage2Len() throws ConnectionException, GetException {
		stage2LenRBC = lazilyGetAndConnect(STAGE2_LEN_RB_HANDLE, stage2LenRBC);
		return stage2LenRBC.getValInt();
	}

	/**
	 * returns stage 2 gain
	 */
	public int getStage2Gain() throws ConnectionException, GetException {
		stage2GainRBC = lazilyGetAndConnect(STAGE2_GAIN_RB_HANDLE, stage2GainRBC);
		return stage2GainRBC.getValInt();
	}

	/**
	 * returns stage 2 method
	 */
	public int getStage2Method() throws ConnectionException, GetException {
		stage2MethodRBC = lazilyGetAndConnect(STAGE2_METHOD_RB_HANDLE, stage2MethodRBC);
		return stage2MethodRBC.getValInt();
	}

	/**
	 * returns stage 3 length
	 */
	public int getStage3Len() throws ConnectionException, GetException {
		stage3LenRBC = lazilyGetAndConnect(STAGE3_LEN_RB_HANDLE, stage3LenRBC);
		return stage3LenRBC.getValInt();
	}

	/**
	 * returns stage 3 gain
	 */
	public int getStage3Gain() throws ConnectionException, GetException {
		stage3GainRBC = lazilyGetAndConnect(STAGE3_GAIN_RB_HANDLE, stage3GainRBC);
		return stage3GainRBC.getValInt();
	}

	/**
	 * returns stage 3 method
	 */
	public int getStage3Method() throws ConnectionException, GetException {
		stage3MethodRBC = lazilyGetAndConnect(STAGE3_METHOD_RB_HANDLE, stage3MethodRBC);
		return stage3MethodRBC.getValInt();
	}

	/**
	 * returns stage 4 length
	 */
	public int getStage4Len() throws ConnectionException, GetException {
		stage4LenRBC = lazilyGetAndConnect(STAGE4_LEN_RB_HANDLE, stage4LenRBC);
		return stage4LenRBC.getValInt();
	}

	/**
	 * returns stage 4 gain
	 */
	public int getStage4Gain() throws ConnectionException, GetException {
		stage4GainRBC = lazilyGetAndConnect(STAGE4_GAIN_RB_HANDLE, stage4GainRBC);
		return stage4GainRBC.getValInt();
	}

	/**
	 * returns stage 4 method
	 */
	public int getStage4Method() throws ConnectionException, GetException {
		stage4MethodRBC = lazilyGetAndConnect(STAGE4_METHOD_RB_HANDLE, stage4MethodRBC);
		return stage4MethodRBC.getValInt();
	}

	/**
	 * Set the stage 1 length.
	 * 
	 * @param len
	 *            The stage length
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage1Len(long len) throws ConnectionException, PutException {
		Channel stage1LenSetChannel = getAndConnectChannel(STAGE1_LEN_HANDLE);
		stage1LenSetChannel.putVal(len);
	}

	/**
	 * Set the stage 2 length.
	 * 
	 * @param len
	 *            The stage length
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage2Len(long len) throws ConnectionException, PutException {
		Channel stage2LenSetChannel = getAndConnectChannel(STAGE2_LEN_HANDLE);
		stage2LenSetChannel.putVal(len);
	}

	/**
	 * Set the stage 3 length.
	 * 
	 * @param len
	 *            The stage length
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage3Len(long len) throws ConnectionException, PutException {
		Channel stage3LenSetChannel = getAndConnectChannel(STAGE3_LEN_HANDLE);
		stage3LenSetChannel.putVal(len);
	}

	/**
	 * Set the stage 4 length.
	 * 
	 * @param len
	 *            The stage length
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage4Len(long len) throws ConnectionException, PutException {
		Channel stage4LenSetChannel = getAndConnectChannel(STAGE4_LEN_HANDLE);
		stage4LenSetChannel.putVal(len);
	}

	/**
	 * Set the stage 1 gain.
	 * 
	 * @param gain
	 *            The stage gain
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage1Gain(long gain) throws ConnectionException, PutException {
		Channel stage1GainSetChannel = getAndConnectChannel(STAGE1_GAIN_HANDLE);
		stage1GainSetChannel.putVal(gain);
	}

	/**
	 * Set the stage 2 gain.
	 * 
	 * @param gain
	 *            The stage gain
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage2Gain(long gain) throws ConnectionException, PutException {
		Channel stage2GainSetChannel = getAndConnectChannel(STAGE2_GAIN_HANDLE);
		stage2GainSetChannel.putVal(gain);
	}

	/**
	 * Set the stage 3 gain.
	 * 
	 * @param gain
	 *            The stage gain
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage3Gain(long gain) throws ConnectionException, PutException {
		Channel stage3GainSetChannel = getAndConnectChannel(STAGE3_GAIN_HANDLE);
		stage3GainSetChannel.putVal(gain);
	}

	/**
	 * Set the stage 4 gain.
	 * 
	 * @param gain
	 *            The stage gain
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage4Gain(long gain) throws ConnectionException, PutException {
		Channel stage4GainSetChannel = getAndConnectChannel(STAGE4_GAIN_HANDLE);
		stage4GainSetChannel.putVal(gain);
	}

	/**
	 * Set the stage 1 method.
	 * 
	 * @param method
	 *            The stage method
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage1Method(int method) throws ConnectionException, PutException {
		Channel stage1MethodSetChannel = getAndConnectChannel(STAGE1_METHOD_HANDLE);
		stage1MethodSetChannel.putVal(method);
	}

	/**
	 * Set the stage 2 method.
	 * 
	 * @param method
	 *            The stage method
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage2Method(int method) throws ConnectionException, PutException {
		Channel stage2MethodSetChannel = getAndConnectChannel(STAGE2_METHOD_HANDLE);
		stage2MethodSetChannel.putVal(method);
	}

	/**
	 * Set the stage 3 method.
	 * 
	 * @param method
	 *            The stage method
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage3Method(int method) throws ConnectionException, PutException {
		Channel stage3MethodSetChannel = getAndConnectChannel(STAGE3_METHOD_HANDLE);
		stage3MethodSetChannel.putVal(method);
	}

	/**
	 * Set the stage 4 method.
	 * 
	 * @param method
	 *            The stage method
	 * @throws xal.ca.ConnectionException
	 *             if the put channel cannot be connected
	 * @throws xal.ca.PutException
	 *             if the put channel set action fails
	 */
	public void setStage4Method(int method) throws ConnectionException, PutException {
		Channel stage4MethodSetChannel = getAndConnectChannel(STAGE4_METHOD_HANDLE);
		stage4MethodSetChannel.putVal(method);
	}

   /**
     * returns average X position for a certian stage
     * @param stage stage number
     */
	public double getXAvg(int stage) throws ConnectionException, GetException {
		if (stage > 4) { 
			System.out.println("Stage no. " + stage + " is larger than 4");
			return 0;
		}
		
		double xAvg = 0.;
		
		double[] xArray = getXTBT();
		int start = 0;
		int end = 0;
		
		switch(stage) {
			case 2:
				start = getStage1Len() + 2;
				end = start + getStage2Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			case 3:
				start = getStage1Len() + getStage2Len() + 5;
				end = start + getStage3Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			case 4:
				start = getStage1Len() + getStage2Len() + getStage3Len() + 8;
				end = start + getStage4Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			default:    // implicitly includes stage 1
				end = getStage1Len() - 1;
		}
		
		if (start >= xArray.length)
			start = xArray.length;
		if (end >= xArray.length)
			end = xArray.length;
				
		double sum = 0.;
		for (int i=start; i<end; i++) {
			sum = sum + xArray[i];
		}
		if ((end - start) > 0)
			xAvg = sum/(end - start);
		
		return xAvg;
	}
	
    /**
     * returns average Y position for a certian stage
     * @param stage stage number
     */
	public double getYAvg(int stage) throws ConnectionException, GetException {
		if (stage > 4) { 
			System.out.println("Stage no. " + stage + " is larger than 4");
			return 0;
		}
		
		double yAvg = 0.;
		
		double[] yArray = getYTBT();
		int start = 0;
		int end = 0;
		
		switch(stage) {
			case 2:
				start = getStage1Len() + 2;
				end = start + getStage2Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			case 3:
				start = getStage1Len() + getStage2Len() + 5;
				end = start + getStage3Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			case 4:
				start = getStage1Len() + getStage2Len() + getStage3Len() + 8;
				end = start + getStage4Len() - 1;
                break;  // tap added this break statement as it seems to be the intent
			default:
				end = getStage1Len() - 1;
		}
		
		if (start >= yArray.length)
			start = yArray.length;
		if (end >= yArray.length)
			end = yArray.length;
				
		double sum = 0.;
		for (int i=start; i<end; i++) {
			sum = sum + yArray[i];
		}
		if ((end - start) > 0)
			yAvg = sum/(end - start);
		
		return yAvg;
	}
}
