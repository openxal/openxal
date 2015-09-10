package xal.app.quadshaker;

import java.util.*;

import xal.ca.*;
import xal.tools.xml.*;
import xal.extension.scan.WrappedChannel;

import xal.tools.text.ScientificNumberFormat;
import xal.tools.data.DataAdaptor;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class Quad_Element implements Dev_Element {

	//The bpms table with all available bpms
	private BPMsTable bpmsTable = null;

	private String name = "null";

	//current in A
	private double current = 0.;

	//field in T/m is used in analysis
	private double field = 0.;

	private boolean itIsTrim = false;

	//these objects wrpChCurrent, wrpChRBField
	//will not be garbage collected until you call stopMonitor() method
	private WrappedChannel wrpChCurrent = new WrappedChannel();
	private WrappedChannel wrpChRBField = new WrappedChannel();

	//state of the quad - if it is active we are going to shake it
	private Boolean isActive = new Boolean(false);

	//data containers
	//vector with measurements (QuadMeasure instances)
	private Vector<QuadMeasure> quadMeasurementsV = new Vector<QuadMeasure>();

	//map with keys as BPM_Element objects and
	//coefficients d(X,Y)/d(B) for this quad
	private HashMap<BPM_Element, Double> coefXmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> coefYmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> coefErrXmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> coefErrYmap = new HashMap<BPM_Element, Double>();

	//map with keys as BPM_Element objects and
	//offsets for X,Y, ratio abs(Val)/err, and
	//TrMatrices' elements for X and Y
	//for this quad
	private HashMap<BPM_Element, Double> offsetXmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> offsetYmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> ratioXmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> ratioYmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> trM01Xmap = new HashMap<BPM_Element, Double>();
	private HashMap<BPM_Element, Double> trM23Ymap = new HashMap<BPM_Element, Double>();

	//resulting orbit in the center of the quad
	private boolean isPosXReady = false;
	private boolean isPosYReady = false;
	private double posX = 0.;
	private double posY = 0.;
	private double posErrX = 0.;
	private double posErrY = 0.;

	private ScientificNumberFormat numberFormat = new ScientificNumberFormat( 5, 10, false );

	/**
	 *  Constructor for the Quad_Element object
	 */
	public Quad_Element() { }

	/**
	 *  Constructor for the Quad_Element object
	 *
	 *@param  name_in  The Parameter
	 */
	public Quad_Element(String name_in) {
		name = name_in;
	}

	/**
	 *  Returns the name attribute of the Quad_Element object
	 *
	 *@return    The name value
	 */
	public String getName() {
		return name;
	}

	/**
	 *  Description of the Method
	 */
	public void memorizeState() {
		current = wrpChCurrent.getValue();
		field = wrpChRBField.getValue();
	}

	/**
	 *  Description of the Method
	 */
	public void restoreState() {
		wrpChCurrent.setValue(current);
	}

	/**
	 *  Returns the field attribute of the Quad_Element object
	 *
	 *@return    The field value
	 */
	public double getField() {
		return field;
	}

	/**
	 *  Returns the itTrim attribute of the Quad_Element object
	 *
	 *@return    The itTrim value
	 */
	public boolean isItTrim() {
		return itIsTrim;
	}

	/**
	 *  Returns the itTrim attribute of the Quad_Element object
	 *
	 *@param  itIsTrim  The Parameter
	 */
	public void isItTrim(boolean itIsTrim) {
		this.itIsTrim = itIsTrim;
	}


	/**
	 *  Returns the current attribute of the Quad_Element object
	 *
	 *@return    The current value
	 */
	public double getCurrent() {
		return current;
	}


	/**
	 *  Returns the posX attribute of the Quad_Element object
	 *
	 *@return    The posX value
	 */
	public double getPosX() {
		return posX;
	}

	/**
	 *  Returns the posY attribute of the Quad_Element object
	 *
	 *@return    The posY value
	 */
	public double getPosY() {
		return posY;
	}

	/**
	 *  Returns the posErrX attribute of the Quad_Element object
	 *
	 *@return    The posErrX value
	 */
	public double getPosErrX() {
		return posErrX;
	}

	/**
	 *  Returns the posErrY attribute of the Quad_Element object
	 *
	 *@return    The posErrY value
	 */
	public double getPosErrY() {
		return posErrY;
	}

	/**
	 *  Sets the positions attribute of the Quad_Element object
	 *
	 *@param  posX     The new positionX value
	 *@param  posErrX  The new positionX value
	 */
	public void setPosX(double posX, double posErrX) {
		this.posX = posX;
		this.posErrX = posErrX;
	}

	/**
	 *  Sets the positionY attribute of the Quad_Element object
	 *
	 *@param  posY     The new positionY value
	 *@param  posErrY  The new positionY value
	 */
	public void setPosY(double posY, double posErrY) {
		this.posY = posY;
		this.posErrY = posErrY;
	}

	/**
	 *  Sets the posXReady attribute of the Quad_Element object
	 *
	 *@param  isPosXReady  The new posXReady value
	 */
	public void setPosXReady(boolean isPosXReady) {
		this.isPosXReady = isPosXReady;
	}

	/**
	 *  Sets the posYReady attribute of the Quad_Element object
	 *
	 *@param  isPosYReady  The new posYReady value
	 */
	public void setPosYReady(boolean isPosYReady) {
		this.isPosYReady = isPosYReady;
	}

	/**
	 *  Returns the posXReady attribute of the Quad_Element object
	 *
	 *@return    The posXReady value
	 */
	public boolean isPosXReady() {
		return isPosXReady;
	}

	/**
	 *  Returns the posYReady attribute of the Quad_Element object
	 *
	 *@return    The posYReady value
	 */
	public boolean isPosYReady() {
		return isPosYReady;
	}

	/**
	 *  Adds a feature to the Measure attribute of the Quad_Element object
	 *
	 *@param  quadMeasure  The feature to be added to the Measure attribute
	 */
	public void addMeasure(QuadMeasure quadMeasure) {
		quadMeasurementsV.add(quadMeasure);
		quadMeasure.setQuadElement(this);
	}

	/**
	 *  Returns the measures attribute of the Quad_Element object
	 *
	 *@return    The measures value
	 */
	public Vector<QuadMeasure> getMeasures() {
		return quadMeasurementsV;
	}

	/**
	 *  Description of the Method
	 */
	public void clear() {
		quadMeasurementsV.clear();
		clearSensitivityCoefs();
		clearOffsetData();
	}

	/**
	 *  Sets the name attribute of the Quad_Element object
	 *
	 *@param  name  The new name value
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 *  Sets the bPMsTable attribute of the Quad_Element object
	 *
	 *@param  bpmsTable_in  The new bPMsTable value
	 */
	public void setBPMsTable(BPMsTable bpmsTable_in) {
		bpmsTable = bpmsTable_in;
	}

	/**
	 *  Returns the wrpChCurrent attribute of the Quad_Element object
	 *
	 *@return    The wrpChCurrent value
	 */
	public WrappedChannel getWrpChCurrent() {
		return wrpChCurrent;
	}

	/**
	 *  Returns the wrpChRBField attribute of the Quad_Element object
	 *
	 *@return    The wrpChRBField value
	 */
	public WrappedChannel getWrpChRBField() {
		return wrpChRBField;
	}

	/**
	 *  Description of the Method
	 */
	public void startMonitor() {
		wrpChCurrent.startMonitor();
		wrpChRBField.startMonitor();
	}

	/**
	 *  Description of the Method
	 */
	public void stopMonitor() {
		wrpChCurrent.stopMonitor();
		wrpChRBField.stopMonitor();
	}

	/**
	 *  Returns the activeObj attribute of the Quad_Element object
	 *
	 *@return    The activeObj value
	 */
	public Boolean isActiveObj() {
		return isActive;
	}

	/**
	 *  Returns the active attribute of the Quad_Element object
	 *
	 *@return    The active value
	 */
	public boolean isActive() {
		return isActive.booleanValue();
	}

	/**
	 *  Sets the active attribute of the Quad_Element object
	 *
	 *@param  state  The new active value
	 */
	public void setActive(boolean state) {
		if(state != isActive.booleanValue()) {
			isActive = new Boolean(state);
		}
	}

	/**
	 *  Adds a feature to the SensitivityCoef attribute of the Quad_Element object
	 *
	 *@param  bpmElm     The feature to be added to the SensitivityCoef attribute
	 *@param  xCoef      The feature to be added to the SensitivityCoef attribute
	 *@param  yCoef      The feature to be added to the SensitivityCoef attribute
	 *@param  xCoef_err  The feature to be added to the SensitivityCoef attribute
	 *@param  yCoef_err  The feature to be added to the SensitivityCoef attribute
	 */
	public void addSensitivityCoef(BPM_Element bpmElm, double xCoef, double xCoef_err, double yCoef, double yCoef_err) {
		coefXmap.put(bpmElm, new Double(xCoef));
		coefYmap.put(bpmElm, new Double(yCoef));
		coefErrXmap.put(bpmElm, new Double(xCoef_err));
		coefErrYmap.put(bpmElm, new Double(yCoef_err));
	}


	/**
	 *  Adds a feature to the OffsetData attribute of the Quad_Element object
	 *
	 *@param  bpmElm   The feature to be added to the OffsetData attribute
	 *@param  xOffset  The feature to be added to the OffsetData attribute
	 *@param  yOffset  The feature to be added to the OffsetData attribute
	 *@param  xRatio   The feature to be added to the OffsetData attribute
	 *@param  yRatio   The feature to be added to the OffsetData attribute
	 *@param  xM01     The feature to be added to the OffsetData attribute
	 *@param  yM23     The feature to be added to the OffsetData attribute
	 */
	public void addOffsetData(BPM_Element bpmElm,
			double xOffset, double yOffset,
			double xRatio, double yRatio,
			double xM01, double yM23) {
		offsetXmap.put(bpmElm, new Double(xOffset));
		offsetYmap.put(bpmElm, new Double(yOffset));
		ratioXmap.put(bpmElm, new Double(xRatio));
		ratioYmap.put(bpmElm, new Double(yRatio));
		trM01Xmap.put(bpmElm, new Double(xM01));
		trM23Ymap.put(bpmElm, new Double(yM23));
	}


	/**
	 *  Description of the Method
	 */
	public void clearSensitivityCoefs() {
		coefXmap.clear();
		coefYmap.clear();
		coefErrXmap.clear();
		coefErrYmap.clear();
		isPosXReady = false;
		isPosYReady = false;
	}

	/**
	 *  Description of the Method
	 */
	public void clearOffsetData() {
		offsetXmap.clear();
		offsetYmap.clear();
		ratioXmap.clear();
		ratioYmap.clear();
		trM01Xmap.clear();
		trM23Ymap.clear();
		isPosXReady = false;
		isPosYReady = false;
	}

	/**
	 *  Description of the Method
	 */
	public void clearPosData() {
		isPosXReady = false;
		isPosYReady = false;
	}


	/**
	 *  Returns the sensitivityCoefsX attribute of the Quad_Element object
	 *
	 *@return    The sensitivityCoefsX value
	 */
	public HashMap<BPM_Element, Double> getSensitivityCoefsX() {
		return coefXmap;
	}

	/**
	 *  Returns the sensitivityCoefsErrX attribute of the Quad_Element object
	 *
	 *@return    The sensitivityCoefsErrX value
	 */
	public HashMap<BPM_Element, Double> getSensitivityCoefsErrX() {
		return coefErrXmap;
	}

	/**
	 *  Returns the sensitivityCoefsY attribute of the Quad_Element object
	 *
	 *@return    The sensitivityCoefsY value
	 */
	public HashMap<BPM_Element, Double> getSensitivityCoefsY() {
		return coefYmap;
	}

	/**
	 *  Returns the sensitivityCoefsErrY attribute of the Quad_Element object
	 *
	 *@return    The sensitivityCoefsErrY value
	 */
	public HashMap<BPM_Element, Double> getSensitivityCoefsErrY() {
		return coefErrYmap;
	}

	/**
	 *  Returns the offsetMapX attribute of the Quad_Element object
	 *
	 *@return    The offsetMapX value
	 */
	public HashMap<BPM_Element, Double> getOffsetMapX() {
		return offsetXmap;
	}

	/**
	 *  Returns the offsetMapY attribute of the Quad_Element object
	 *
	 *@return    The offsetMapY value
	 */
	public HashMap<BPM_Element, Double> getOffsetMapY() {
		return offsetYmap;
	}


	/**
	 *  Returns the ratioMapX attribute of the Quad_Element object
	 *
	 *@return    The ratioMapX value
	 */
	public HashMap<BPM_Element, Double> getRatioMapX() {
		return ratioXmap;
	}

	/**
	 *  Returns the ratioMapY attribute of the Quad_Element object
	 *
	 *@return    The ratioMapY value
	 */
	public HashMap<BPM_Element, Double> getRatioMapY() {
		return ratioYmap;
	}

	/**
	 *  Returns the trM01MapX attribute of the Quad_Element object
	 *
	 *@return    The trM01MapX value
	 */
	public HashMap<BPM_Element, Double> getTrM01MapX() {
		return trM01Xmap;
	}

	/**
	 *  Returns the trM23MapY attribute of the Quad_Element object
	 *
	 *@return    The trM23MapY value
	 */
	public HashMap<BPM_Element, Double> getTrM23MapY() {
		return trM23Ymap;
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(DataAdaptor da) {
		da.setValue("name", name);
		da.setValue("isActive", isActive.booleanValue());
		da.setValue("itIsTrim", itIsTrim);
		da.setValue("currentPV", wrpChCurrent.getChannelName());
		da.setValue("fieldRbPV", wrpChRBField.getChannelName());
		da.setValue("current", current);
		da.setValue("field", field);

		DataAdaptor coefSetDA = da.createChild("COEFFICIENT_SET");

		Iterator<BPM_Element> itr = coefXmap.keySet().iterator();
		while(itr.hasNext()) {
			BPM_Element bpmElm =  itr.next();
			double x = (coefXmap.get(bpmElm)).doubleValue();
			double y = (coefYmap.get(bpmElm)).doubleValue();
			double x_err = (coefErrXmap.get(bpmElm)).doubleValue();
			double y_err = (coefErrYmap.get(bpmElm)).doubleValue();
			DataAdaptor coefDA = coefSetDA.createChild("COEFFICIENTS");
			coefDA.setValue("bpm", bpmElm.getName());
			coefDA.setValue("dx_di", numberFormat.format(x));
			coefDA.setValue("dy_di", numberFormat.format(y));
			coefDA.setValue("dx_di_err", numberFormat.format(x_err));
			coefDA.setValue("dy_di_err", numberFormat.format(y_err));
		}

		DataAdaptor offSetsDA = da.createChild("OFFSET_VALUES");

		itr = offsetXmap.keySet().iterator();
		while(itr.hasNext()) {
			BPM_Element bpmElm = itr.next();
			double x = (offsetXmap.get(bpmElm)).doubleValue();
			double y = (offsetYmap.get(bpmElm)).doubleValue();
			double ratioX = (ratioXmap.get(bpmElm)).doubleValue();
			double ratioY = (ratioYmap.get(bpmElm)).doubleValue();
			double trM01X = (trM01Xmap.get(bpmElm)).doubleValue();
			double trM23Y = (trM23Ymap.get(bpmElm)).doubleValue();
			DataAdaptor offsetDA =  offSetsDA.createChild("OFFSETs");
			offsetDA.setValue("bpm", bpmElm.getName());
			offsetDA.setValue("x", numberFormat.format(x));
			offsetDA.setValue("y", numberFormat.format(y));
			offsetDA.setValue("rX", numberFormat.format(ratioX));
			offsetDA.setValue("rY", numberFormat.format(ratioY));
			offsetDA.setValue("m01", numberFormat.format(trM01X));
			offsetDA.setValue("m23", numberFormat.format(trM23Y));
		}

		DataAdaptor measureSetDA = da.createChild("MEASUREMENT_SET");
		Iterator<QuadMeasure> newitr = quadMeasurementsV.iterator();
		while(newitr.hasNext()) {
			QuadMeasure quadMeasure = newitr.next();
			DataAdaptor measureDA = measureSetDA.createChild("MEASURE");
			quadMeasure.dumpData(measureDA);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(DataAdaptor da) {
		name = da.stringValue("name");
		isActive = new Boolean(da.booleanValue("isActive"));
		itIsTrim = da.booleanValue("itIsTrim");
		wrpChCurrent.setChannelName(da.stringValue("currentPV"));
		wrpChRBField.setChannelName(da.stringValue("fieldRbPV"));
		current = da.doubleValue("current");
		field = da.doubleValue("field");

		clearSensitivityCoefs();

		DataAdaptor coefSetDA =  da.childAdaptor("COEFFICIENT_SET");
        for (final DataAdaptor coefDA : coefSetDA.childAdaptors()){
			BPM_Element bpmElm = bpmsTable.getBPM(coefDA.stringValue("bpm"));
			if(bpmElm != null) {
				coefXmap.put(bpmElm, new Double(coefDA.doubleValue("dx_di")));
				coefYmap.put(bpmElm, new Double(coefDA.doubleValue("dy_di")));
				coefErrXmap.put(bpmElm, new Double(coefDA.doubleValue("dx_di_err")));
				coefErrYmap.put(bpmElm, new Double(coefDA.doubleValue("dy_di_err")));
			}
		}

		clearOffsetData();

		DataAdaptor offSetsDA =  da.childAdaptor("OFFSET_VALUES");
		if(offSetsDA != null) {
            for (final DataAdaptor offsetDA : offSetsDA.childAdaptors()){
				BPM_Element bpmElm = bpmsTable.getBPM(offsetDA.stringValue("bpm"));
				if(bpmElm != null) {
					offsetXmap.put(bpmElm, new Double(offsetDA.doubleValue("x")));
					offsetYmap.put(bpmElm, new Double(offsetDA.doubleValue("y")));

					double x = (coefXmap.get(bpmElm)).doubleValue();
					double y = (coefYmap.get(bpmElm)).doubleValue();
					double x_err = (coefErrXmap.get(bpmElm)).doubleValue();
					double y_err = (coefErrYmap.get(bpmElm)).doubleValue();

					if(x_err > 0. && y_err > 0.) {
						ratioXmap.put(bpmElm, new Double(Math.abs(x) / x_err));
						ratioYmap.put(bpmElm, new Double(Math.abs(y) / y_err));
					} else {
						ratioXmap.put(bpmElm, new Double(0.));
						ratioYmap.put(bpmElm, new Double(0.));
					}

					if(offsetDA.hasAttribute("m01")) {
						trM01Xmap.put(bpmElm, new Double(offsetDA.doubleValue("m01")));
					} else {
						trM01Xmap.put(bpmElm, new Double(0.));
					}

					if(offsetDA.hasAttribute("m23")) {
						trM23Ymap.put(bpmElm, new Double(offsetDA.doubleValue("m23")));
					} else {
						trM23Ymap.put(bpmElm, new Double(0.));
					}

				}
			}
		}

		quadMeasurementsV.clear();
		DataAdaptor measureSetDA = da.childAdaptor("MEASUREMENT_SET");
        
        for (final DataAdaptor measureDA : measureSetDA.childAdaptors()){
			QuadMeasure quadMeasure = new QuadMeasure(bpmsTable);
			addMeasure(quadMeasure);
			quadMeasure.readData(measureDA);
		}
	}

}

