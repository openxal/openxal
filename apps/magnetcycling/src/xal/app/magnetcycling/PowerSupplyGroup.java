package xal.app.magnetcycling;

import java.util.*;

/**
 *  This is a container for PowerSupplyCycler instances. It does some operations
 *  over all PowerSupplyCycler instances.
 *
 *@author     shishlo
 */
public class PowerSupplyGroup {

	//defines inactivity period in the beginning of the cycling
	private double time_shift = 0.;

	//the accumulated running time
	private double run_time = 0.;

	//the name of the group
	private String group_name = "no name";

	//vector with PowerSupplyCycler instances
	private Vector<PowerSupplyCycler> powerSupplyCyclerV = new Vector<PowerSupplyCycler>();


	/**
	 *  Constructor for the PowerSupplyGroup object
	 */
	public PowerSupplyGroup() { }

	/**
	 *  Initializes all PowerSupplyCycler instances.
	 */
	public void init() {
		run_time = 0.;
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			psc.init();
		}
	}

	/**
	 *  Restores initial currents in power supplies that were memorized in init().
	 */
	public void restoreInitialCurrents() {
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			psc.restoreInitialCurrents();
		}
	}


	/**
	 *  Returns the time shift in seconds of the PowerSupplyGroup object
	 *
	 *@return    The time shift value in seconds
	 */
	public double getTimeShift() {
		return time_shift;
	}


	/**
	 *  Sets the time shift attribute of the PowerSupplyGroup object
	 *
	 *@param  time_shift  The new time shift value in seconds
	 */
	public void setTimeShift(double time_shift) {
		this.time_shift = time_shift;
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			psc.setTimeShift(time_shift);
		}
	}


	/**
	 *  Returns the max time attribute of the PowerSupplyGroup object
	 *
	 *@return    The max time value
	 */
	public double getMaxTime() {
		double max_time = 0.;
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			if(psc.getActive()) {
				double max_time_local = psc.getMaxTime();
				if(max_time < max_time_local) {
					max_time = max_time_local;
				}
			}
		}
		return max_time;
	}


	/**
	 *  Makes the step in time
	 *
	 *@param  time_step  The time step
	 */
	public void makeTimeStep(double time_step) {
		run_time = run_time + time_step;
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			psc.makeTimeStep(time_step);
		}
	}

	/**
	 *  Adds a graph point to the graph of set PVs in each PowerSupplyCycler
	 */
	public void accountGraphPoint() {
		for(int i = 0, n = powerSupplyCyclerV.size(); i < n; i++) {
			PowerSupplyCycler psc = powerSupplyCyclerV.get(i);
			psc.accountGraphPoint();
		}
	}


	/**
	 *  Returns the vector with powerSupplyCyclers of the PowerSupplyGroup object
	 *
	 *@return    The vector with powerSupplyCyclers instances
	 */
	public Vector<PowerSupplyCycler> getPowerSupplyCyclers() {
		return powerSupplyCyclerV;
	}


	/**
	 *  Adds a PowerSupplyCycler.
	 *
	 *@param  psc  a PowerSupplyCycler instance.
	 */
	public void addPowerSupplyCycler(PowerSupplyCycler psc) {
		if(psc != null) {
			powerSupplyCyclerV.add(psc);
		}
	}

	/**
	 *  Removes one of the PowerSupplyCyclers.
	 *
	 *@param  psc  The PowerSupplyCycler to remove.
	 */
	public void removePowerSupplyCycler(PowerSupplyCycler psc) {
		powerSupplyCyclerV.remove(psc);
	}

	/**
	 *  Removes all PowerSupplyCyclers.
	 */
	public void removePowerSupplyCyclers() {
		powerSupplyCyclerV.clear();
	}

	/**
	 *  Sets the name of the PowerSupplyGroup object
	 *
	 *@param  group_name  The new name
	 */
	public void setName(String group_name) {
		this.group_name = group_name;
	}

	/**
	 *  Returns the name of the PowerSupplyGroup object
	 *
	 *@return    The name
	 */
	public String getName() {
		return group_name;
	}


	/**
	 *  Returns the name of the PowerSupplyGroup object
	 *
	 *@return    The name
	 */
	public String toString() {
		return group_name;
	}
}

