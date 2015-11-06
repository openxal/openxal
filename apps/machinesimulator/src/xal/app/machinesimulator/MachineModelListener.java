/**
 * 
 */
package xal.app.machinesimulator;

/**
 * @author luxiaohan
 *MachineModelListener
 */
public interface MachineModelListener {
	
	/**event indicates that the sequence has changed*/
	public void modelSequenceChanged(final MachineModel model);
	
	/**event indicates that the scenario has changed*/
	public void modelScenarioChanged(final MachineModel model);

}
