package xal.app.beamatfoil;

import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.swing.FortranNumberFormat;
import xal.smf.impl.Electromagnet;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class Corr_Element {

	private String name = "null";

	private double fieldInitial = 0.;
	private double intermedField = 0.;
	private double liveField = 0.;
	
	private double lowerFieldLimit = -Double.MAX_VALUE;
	private double upperFieldLimit = Double.MAX_VALUE;

	//field value format
	private final FortranNumberFormat frmt = new FortranNumberFormat("G10.4");

	//position and angle vs. B field coeff 
	//dimension [mm/T] and [mrad/T]
	private double position_coeff = 0.;
	private double angle_coeff = 0.;

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
			if(corr.getId().equals("HEBT_Mag:DCH30")){
				upperFieldLimit = - lowerFieldLimit;
			}
			//System.out.println("debug corr:" + name +
			//		" liveB=" + frmt.format(fieldInitial) +
			//		" lowerB=" + frmt.format(lowerFieldLimit) +
			//		" upperB=" + frmt.format(upperFieldLimit));
		} catch(ConnectionException exp) {
			liveField = 0.;
			throw exp;
		} catch(GetException exp) {
			liveField = 0.;
			throw exp;
		}
		liveField = fieldInitial;
		intermedField = fieldInitial;
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
		intermedField = fieldInitial;
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
	 *  Returns the formatted value as string
	 *
	 *@return    The formatted value as string
	 */
	public String format(double val) {
		return frmt.format(val);
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
	 *  Sets the IntermedField attribute of the Corr_Element object
	 *
	 *@param  intermedField  The new IntermedField value
	 */
	public void setIntermedField(double intermedField) {
		this.intermedField = intermedField;
	}


	/**
	 *  Returns the IntermedField attribute of the Corr_Element object
	 *
	 *@return    The IntermedField value
	 */
	public double getIntermedField() {
		return intermedField;
	}	

	/**
	 *  Returns the lower field limit
	 *
	 *@return    The lower field limit value
	 */	
	public double getLowerFieldLimit(){
		return lowerFieldLimit;
	}
	
	/**
	 *  Returns the upper field limit
	 *
	 *@return    The upper field limit value
	 */		
	public double getUpperFieldLimit(){
		return upperFieldLimit;
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
	 *  Sets the position coeff attribute of the Corr_Element object
	 *
	 *@param  coef     The new coeff value
	 */
	public void setPosCoeff(double coef) {
	  position_coeff = coef;
	}
	
	/**
	 *  Gets the position coeff attribute of the Corr_Element object
	 */
	public double getPosCoeff() {
	  return position_coeff;
	}
	
	/**
	 *  Sets the angle coeff attribute of the Corr_Element object
	 *
	 *@param  coef     The new coeff value
	 */
	public void setAngleCoeff(double coef) {
	  angle_coeff = coef;
	}
	
	/**
	 *  Gets the angle coeff attribute of the Corr_Element object
	 */
	public double getAngleCoeff() {
	  return angle_coeff;
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

