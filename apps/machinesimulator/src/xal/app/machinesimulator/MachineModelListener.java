/**
 * 
 */
package xal.app.machinesimulator;

import java.util.Date;
import java.util.List;
import java.util.Map;

import xal.smf.AcceleratorSeq;

/**
 * @author luxiaohan
 *MachineModelListener
 */
public interface MachineModelListener {
	
	/**event indicates that the sequence has changed*/
	public void modelSequenceChanged(final MachineModel model);
	
	/**event indicates that the scenario has changed*/
	public void modelScenarioChanged(final MachineModel model);
	
	/**event indicates that the history record select state changed*/
	public void historyRecordSelectStateChanged( final List<NodePropertyHistoryRecord> nodePropertyHistoryRecords,
			final Map<Date, String> columnName, final AcceleratorSeq seq, final List<BpmRecord> bpms );

}
