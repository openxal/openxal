package xal.app.quadshaker;

import java.util.*;

import xal.ca.*;
import xal.tools.xml.*;
import xal.extension.scan.WrappedChannel;


public class Dipole_Element{

	private String name = "null";

	//state of the quad - if it is active we are going to shake it
	private Boolean isActive = new Boolean(false);

	/**
	 *  Constructor for the Dipole_Element object
	 *
	 *@param  name_in  The Parameter
	 */
	public Dipole_Element(String name_in) {
		name = name_in;
	}

	/**
	 *  Returns the name attribute of the Dipole_Element object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Returns the activeObj attribute of the Dipole_Element object
	 *
	 *@return    The activeObj value
	 */
	public Boolean isActiveObj() {
		return isActive;
	}

	/**
	 *  Returns the active attribute of the Dipole_Element object
	 *
	 *@return    The active value
	 */
	public boolean isActive() {
		return isActive.booleanValue();
	}

	/**
	 *  Sets the active attribute of the Dipole_Element object
	 *
	 *@param  state  The new active value
	 */
	public void setActive(boolean state) {
		if(state != isActive.booleanValue()) {
			isActive = new Boolean(state);
		}
	}
}

