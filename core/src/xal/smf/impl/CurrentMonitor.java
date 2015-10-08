package xal.smf.impl;

import xal.ca.*;
import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;

/**
 * The implementation of the Current Monitor class. This class contains the
 * methods members, attributes, and signal sets pertinant to modeling Current
 * Monitors.
 * 
 * @author J. Galambos (jdg@ornl.gov)
 */

public class CurrentMonitor extends AcceleratorNode {
	/** standard type for instances of this class */
	public static final String s_strType = "BCM";

	static {
		registerType();
	}


	/*
	 * Register type for qualification
	 */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( CurrentMonitor.class, s_strType );
	}



	/** Override to provide type signature */
	public String getType() {
		return s_strType;
	}


	/** Primary Constructor */
	public CurrentMonitor( final String strId, final ChannelFactory channeFactory ) {
		super( strId, channeFactory );
	}


	/** Constructor */
	public CurrentMonitor( final String strId ) {
		this( strId, null );
	}


	public static final String Q_INTEGRAL_HANDLE = "Particles";

	private Channel qIntegralC = null;

	public static final String T_AVG_LEN_HANDLE = "DisplayLength";

	private Channel tAvgLenC = null;

	public static final String I_TBT_HANDLE = "currentTBT";

	private Channel iTBTC = null;

	public static final String T_DELAY_HANDLE = "tDelay";

	private Channel tDelayC = null;

	public static final String I_AVG_HANDLE = "currentAvg";

	private Channel iAvgC = null;

	public static final String I_MAX_HANDLE = "currentMax";

	private Channel iMaxC = null;


	/**
	 * Integrated current over macropulse
	 */
	public double getQIntegral() throws ConnectionException, GetException {
		qIntegralC = this.lazilyGetAndConnect(Q_INTEGRAL_HANDLE, qIntegralC);
		return qIntegralC.getValDbl();
	}

	/**
	 * Averaged pulse length
	 */
	public double getTAvgLen() throws ConnectionException, GetException {
		tAvgLenC = this.lazilyGetAndConnect(T_AVG_LEN_HANDLE, tAvgLenC);
		return tAvgLenC.getValDbl();
	}

	/**
	 * Turn by turn current
	 */
	public double[] getITBT() throws ConnectionException, GetException {
		iTBTC = this.lazilyGetAndConnect(I_TBT_HANDLE, iTBTC);
		return iTBTC.getArrDbl();
	}

	/**
	 * Get the portion of ITBT array with beam on only. This method is useful
	 * for rf cavity beam loading calculation
	 * 
	 * @return part of the beam current array with non-zero beam only
	 * @throws ConnectionException
	 * @throws GetException
	 */
	public double[] getITBTWithBeamOnly() throws ConnectionException,
			GetException {
		double[] fullArray = getITBT();
		double iMax = -100.;
		for (int i = 0; i < fullArray.length; i++) {
			if (fullArray[i] > iMax)
				iMax = fullArray[i];
		}
		double[] beamArray;
		// do calculation only if > 1.mA of peak current
		if (iMax > 1.) {
			int start = 0;
			int end = fullArray.length;
			int counter = 0;
			while (fullArray[counter] < iMax / 10.) {
				counter++;
			}
			start = counter;
			while (fullArray[counter] > iMax / 5.) {
				counter++;
			}
			end = counter;
			if (start != end) {
				beamArray = new double[end - start];
				System.arraycopy(fullArray, start, beamArray, 0, end - start);
			}
			// if cannot find obvious beam, set this array to size 1, and value
			// = 0.
			else {
				beamArray = new double[1];
			}
		} 
		// if < 1mA, just return 0.
		else {
			beamArray = new double[1];
		}

		return beamArray;
	}

	/**
	 * Time delay
	 */
	public double getTDelay() throws ConnectionException, GetException {
		tDelayC = this.lazilyGetAndConnect(T_DELAY_HANDLE, tDelayC);
		return tDelayC.getValDbl();
	};

	/**
	 * Avgerage beam current
	 */
	public double getIAvg() throws ConnectionException, GetException {
		iAvgC = this.lazilyGetAndConnect(I_AVG_HANDLE, iAvgC);
		return iAvgC.getValDbl();
	};

	/**
	 * Maximum beam current
	 */
	public double getIMax() throws ConnectionException, GetException {
		iMaxC = this.lazilyGetAndConnect(I_MAX_HANDLE, iMaxC);
		return iMaxC.getValDbl();
	};
}
