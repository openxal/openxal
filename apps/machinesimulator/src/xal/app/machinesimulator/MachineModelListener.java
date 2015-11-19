/**
 * 
 */
package xal.app.machinesimulator;

import java.util.List;

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
	public void historyRecordSelectStateChanged( final List<NodePropertySnapshot> nodePropertySnapshots, final String name );

}
