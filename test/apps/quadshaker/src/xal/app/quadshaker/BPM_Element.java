package xal.app.quadshaker;

import xal.ca.*;
import xal.tools.xml.*;
import xal.extension.scan.WrappedChannel;
import xal.tools.data.DataAdaptor;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class BPM_Element implements Dev_Element {
	private String name = "null";

	//these objects wrpChX,wrpChY  will not be garbage collected
	//until you call stopMonitor() method
	private WrappedChannel wrpChX = new WrappedChannel();
	private WrappedChannel wrpChY = new WrappedChannel();

	private Boolean isActive = new Boolean(false);

	/**
	 *  Constructor for the BPM_Element object
	 */
	public BPM_Element() { }


	/**
	 *  Constructor for the BPM_Element object
	 *
	 *@param  name_in  The Parameter
	 */
	public BPM_Element(String name_in) {
		name = name_in;
	}

	/**
	 *  Returns the name attribute of the BPM_Element object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Sets the name attribute of the BPM_Element object
	 *
	 *@param  name  The new name value
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *  Returns the wrpChannelX attribute of the BPM_Element object
	 *
	 *@return    The wrpChannelX value
	 */
	public WrappedChannel getWrpChannelX() {
		return wrpChX;
	}

	/**
	 *  Returns the wrpChannelY attribute of the BPM_Element object
	 *
	 *@return    The wrpChannelY value
	 */
	public WrappedChannel getWrpChannelY() {
		return wrpChY;
	}

	/**
	 *  Description of the Method
	 */
	public void startMonitor() {
		wrpChX.startMonitor();
		wrpChX.startMonitor();
	}

	/**
	 *  Description of the Method
	 */
	public void stopMonitor() {
		wrpChX.stopMonitor();
		wrpChY.stopMonitor();
	}


	/**
	 *  Returns the x attribute of the BPM_Element object
	 *
	 *@return    The x value
	 */
	public double getX() {
		return wrpChX.getValue();
	}

	/**
	 *  Returns the y attribute of the BPM_Element object
	 *
	 *@return    The y value
	 */
	public double getY() {
		return wrpChY.getValue();
	}

	/**
	 *  Returns the activeObj attribute of the BPM_Element object
	 *
	 *@return    The activeObj value
	 */
	public Boolean isActiveObj() {
		return isActive;
	}

	/**
	 *  Returns the active attribute of the BPM_Element object
	 *
	 *@return    The active value
	 */
	public boolean isActive() {
		return isActive.booleanValue();
	}

	/**
	 *  Sets the active attribute of the BPM_Element object
	 *
	 *@param  state  The new active value
	 */
	public void setActive(boolean state) {
		if(state != isActive.booleanValue()) {
			isActive = new Boolean(state);
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(DataAdaptor da) {
		da.setValue("name", name);
		da.setValue("xPV", wrpChX.getChannelName());
		da.setValue("yPV", wrpChY.getChannelName());
		da.setValue("isActive", isActive.booleanValue());
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(DataAdaptor da) {
		name = da.stringValue("name");
		wrpChX.setChannelName(da.stringValue("xPV"));
		wrpChY.setChannelName(da.stringValue("yPV"));
		setActive(da.booleanValue("isActive"));
	}

}

