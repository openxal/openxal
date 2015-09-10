package xal.app.quadshaker;

import java.util.*;

import xal.ca.*;
import xal.tools.xml.*;
import xal.extension.scan.WrappedChannel;

import xal.smf.impl.*;

import xal.tools.text.ScientificNumberFormat;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class Corr_Element {

	private String name = "null";

	private double fieldInitial = 0.;

	private double liveField = 0.;

	private double lowerFieldLimit = -Double.MAX_VALUE;
	private double upperFieldLimit = Double.MAX_VALUE;

	//field value format
	private ScientificNumberFormat frmt = new ScientificNumberFormat( 5, 10, false );

	//position vs. B field coeff with keys as Quad_Elements
	//dimension [mm/T]
	private HashMap<Quad_Element, Double> coefsMap = new HashMap<Quad_Element, Double>();

	private Electromagnet corr = null;

	//state of the quad - if it is active we are going to shake it
	private Boolean isActive = new Boolean(false);

	/**
	 *  Constructor for the Corr_Element object
	 *
	 *@param  name_in  The Parameter
	 *@param  corr_in  The Parameter
	 */
	public Corr_Element(String name_in, Electromagnet corr_in) {
		name = name_in;
		corr = corr_in;
		corr.setUseFieldReadback(false);
	}


	/**
	 *  Returns the inLimits attribute of the Corr_Element object
	 *
	 *@param  val  The Parameter
	 *@return      The inLimits value
	 */
	public boolean isInLimits(double val) {
		boolean res = false;
		if(val < upperFieldLimit && val > lowerFieldLimit) {
			res = true;
		}
		return res;
	}

	/**
	 *  Description of the Method
	 *
	 *@exception  ConnectionException  The Exception
	 *@exception  GetException         The Exception
	 */
	public void memorizeField() throws ConnectionException, GetException {
		try {
			fieldInitial = corr.getField();
			lowerFieldLimit = corr.lowerFieldLimit();
			upperFieldLimit = corr.upperFieldLimit();
			//System.out.println("debug corr:" + name +
			//		" liveB=" + fieldInitial +
			//		" lowerB=" + lowerFieldLimit +
			//		" upperB=" + upperFieldLimit);
		} catch(ConnectionException exp) {
			liveField = 0.;
			throw exp;
		} catch(GetException exp) {
			liveField = 0.;
			throw exp;
		}
		liveField = fieldInitial;
	}

	/**
	 *  Description of the Method
	 *
	 *@exception  ConnectionException  The Exception
	 *@exception  PutException         The Exception
	 */
	public void restoreField() throws ConnectionException, PutException {
		try {
			corr.setField(fieldInitial);
		} catch(ConnectionException exp) {
			liveField = 0.;
			throw exp;
		} catch(PutException exp) {
			liveField = 0.;
			throw exp;
		}
		liveField = fieldInitial;
	}

	/**
	 *  Returns the magnet attribute of the Corr_Element object
	 *
	 *@return    The magnet value
	 */
	public Electromagnet getMagnet() {
		return corr;
	}

	/**
	 *  Returns the liveFieldAsString attribute of the Corr_Element object
	 *
	 *@return    The liveFieldAsString value
	 */
	public String getLiveFieldAsString() {
		return frmt.format(liveField);
	}


	/**
	 *  Returns the field from Memory attribute of the Corr_Element object
	 *
	 *@return    The field from memory value
	 */
	public double getFieldFromMemory() {
		return fieldInitial;
	}

	/**
	 *  Sets the liveField attribute of the Corr_Element object
	 *
	 *@param  liveField  The new liveField value
	 */
	public void setLiveField(double liveField) {
		this.liveField = liveField;
	}


	/**
	 *  Returns the liveField attribute of the Corr_Element object
	 *
	 *@return    The liveField value
	 */
	public double getLiveField() {
		return liveField;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  liveField                The Parameter
	 *@exception  ConnectionException  The Exception
	 *@exception  PutException         The Exception
	 */
	public void putLiveFieldToCA(double liveField) throws ConnectionException, PutException {
		try {
			corr.setField(liveField);
		} catch(ConnectionException exp) {
			liveField = 0.;
			throw exp;
		} catch(PutException exp) {
			liveField = 0.;
			throw exp;
		}
		this.liveField = liveField;
	}

	/**
	 *  Sets the coeff attribute of the Corr_Element object
	 *
	 *@param  coef     The new coeff value
	 *@param  quadElm  The new coeff value
	 */
	public void setCoeff(double coef, Quad_Element quadElm) {
		coefsMap.put(quadElm, new Double(coef));
	}

	/**
	 *  Returns the coeff attribute of the Corr_Element object
	 *
	 *@param  quadElm  The Parameter
	 *@return          The coeff value
	 */
	public double getCoeff(Quad_Element quadElm) {
		return (coefsMap.get(quadElm)).doubleValue();
	}

	/**
	 *  Description of the Method
	 *
	 *@param  quadElm  The Parameter
	 *@return          The Return Value
	 */
	public boolean hasQuad(Quad_Element quadElm) {
		return coefsMap.containsKey(quadElm);
	}

	/**
	 *  Returns the mapSize attribute of the Corr_Element object
	 *
	 *@return    The mapSize value
	 */
	public int getCoeffsMapSize() {
		return coefsMap.size();
	}

	/**
	 *  Description of the Method
	 */
	public void clearCoeffsMap() {
		coefsMap.clear();
	}


	/**
	 *  Returns the name attribute of the Corr_Element object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Returns the activeObj attribute of the Corr_Element object
	 *
	 *@return    The activeObj value
	 */
	public Boolean isActiveObj() {
		return isActive;
	}

	/**
	 *  Returns the active attribute of the Corr_Element object
	 *
	 *@return    The active value
	 */
	public boolean isActive() {
		return isActive.booleanValue();
	}

	/**
	 *  Sets the active attribute of the Corr_Element object
	 *
	 *@param  state  The new active value
	 */
	public void setActive(boolean state) {
		if(state != isActive.booleanValue()) {
			isActive = new Boolean(state);
		}
	}
}

