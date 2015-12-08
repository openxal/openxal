/**
 * 
 */
package xal.app.machinesimulator;

import xal.smf.impl.BPM;

/**
 * @author luxiaohan
 *Record bpm data
 */
public class BpmRecord {
	/**the bpm to record*/
	final private BPM BPM;
	/**the position*/
	final private Double POSITION;
	/**x average value*/
	final private Double X_AVG;
	/**y average vlaue*/
	final private Double Y_AVG;
	
	public BpmRecord( final BPM bpm, final Double pos, final Double xValue, final Double yValue ) {
		BPM = bpm;
		POSITION = pos;
		X_AVG = xValue;
		Y_AVG = yValue;
	}
	
	/**get the bpm node*/
	public BPM getNode() {
		return BPM;
	}
	
	/**get the bpm position*/
	public Double getPosition() {
		return POSITION;
	}
	
	/**returns average X position*/
	public Double getXAvg() {
		return X_AVG;
	}
	
	/**returns average Y position*/
	public double getYAvg() {
		return Y_AVG;
	}

}
