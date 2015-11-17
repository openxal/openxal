/**
 * 
 */
package xal.app.machinesimulator;

import xal.model.probe.traj.ProbeState;
import xal.tools.beam.Twiss;
import xal.tools.math.r3.R3;

/**
 * @author luxiaohan
 *
 */
public class MachineSimulationHistoryRecord {
	/**new simulation record */
	final private MachineSimulationRecord NEW_RECORD;
	/**old simulation record */
	final private MachineSimulationRecord OLD_RECORD;
	
	/**Constructor*/
	public MachineSimulationHistoryRecord( final MachineSimulationRecord newRecord, final MachineSimulationRecord oldRecord ){
		NEW_RECORD = newRecord;
		OLD_RECORD = oldRecord;
	}
	
	/** get the new wrapped probe state */
	public ProbeState<?> getProbeState() {
		return NEW_RECORD.getProbeState();
	}
	
	/** get the old wrapped probe state */
	public ProbeState<?> getOldProbeState() {
		return OLD_RECORD.getProbeState();
	}
	
	/** Get the state's element ID */
	public String getElementID() {
		return NEW_RECORD.getElementID();
	}
	
	/** Get the old state's element ID */
	public String getOldElementID() {
		return OLD_RECORD.getElementID();
	}


	/** Get the state's beamline position */
	public double getPosition() {
		return NEW_RECORD.getPosition();
	}
	
	/** Get the old state's beamline position */
	public double getOldPosition() {
		return OLD_RECORD.getPosition();
	}


	/** get the state's twiss parameters */
	public Twiss[] getTwissParameters() {
		return NEW_RECORD.getTwissParameters();
	}
	
	/** get the old state's twiss parameters */
	public Twiss[] getOldTwissParameters() {
		return OLD_RECORD.getTwissParameters();
	}


	/** get the state's betatron phase */
	public R3 getBetatronPhase() {
		return NEW_RECORD.getBetatronPhase();
	}
	
	/** get the old state's betatron phase */
	public R3 getOldBetatronPhase() {
		return OLD_RECORD.getBetatronPhase();
	}

}
