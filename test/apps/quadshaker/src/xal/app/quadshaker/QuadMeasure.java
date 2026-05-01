package xal.app.quadshaker;

import java.util.*;
import javax.swing.*;

import xal.ca.*;
import xal.extension.scan.WrappedChannel;
import xal.tools.xml.*;
import xal.tools.data.DataAdaptor;

/**
 *  Description of the Class
 *
 *@author     shishlo
 */
public class QuadMeasure {

	private Quad_Element quadElm = null;
	private BPMsTable bpmsTable = null;

	private double current = 0.;
	private double rbField = 0.;

	private Vector<BPM_Element> bpmsV = new Vector<BPM_Element>();

	private Vector<Double> xPosV = new Vector<Double>();

	private Vector<Double> yPosV = new Vector<Double>();

	/**
	 *  Constructor for the QuadMeasure object
	 *
	 *@param  bpmsTable_in  The Parameter
	 */
	public QuadMeasure(BPMsTable bpmsTable_in) {
		bpmsTable = bpmsTable_in;
	}

	/**
	 *  Sets the quadElement attribute of the QuadMeasure object
	 *
	 *@param  quadElm  The new quadElement value
	 */
	public void setQuadElement(Quad_Element quadElm) {
		this.quadElm = quadElm;
	}

	/**
	 *  Sets the current attribute of the QuadMeasure object
	 *
	 *@param  current  The new current value
	 */
	public void setCurrent(double current) {
		this.current = current;
	}

	/**
	 *  Description of the Method
	 */
	public void init() {
		bpmsV.clear();
		xPosV.clear();
		yPosV.clear();
	}

	/**
	 *  Returns the current attribute of the QuadMeasure object
	 *
	 *@return    The current value
	 */
	public double getCurrent() {
		return current;
	}

	/**
	 *  Returns the rBField attribute of the QuadMeasure object
	 *
	 *@return    The rBField value
	 */
	public double getRBField() {
		return rbField;
	}

	/**
	 *  Returns the size attribute of the QuadMeasure object
	 *
	 *@return    The size value
	 */
	public int getSize() {
		return bpmsV.size();
	}

	/**
	 *  Returns the bPM_Element attribute of the QuadMeasure object
	 *
	 *@param  ind  The Parameter
	 *@return      The bPM_Element value
	 */
	public BPM_Element getBPM_Element(int ind) {
		return bpmsV.get(ind);
	}

	/**
	 *  Returns the quad_Element attribute of the QuadMeasure object
	 *
	 *@return    The quad_Element value
	 */
	public Quad_Element getQuad_Element() {
		return quadElm;
	}

	/**
	 *  Sets the currentToEPICS attribute of the QuadMeasure object
	 */
	public void setCurrentToEPICS() {
		quadElm.getWrpChCurrent().setValue(current);
	}

	/**
	 *  Returns the xPos attribute of the QuadMeasure object
	 *
	 *@param  ind  The Parameter
	 *@return      The xPos value
	 */
	public double getXPos(int ind) {
		return (xPosV.get(ind)).doubleValue();
	}

	/**
	 *  Returns the yPos attribute of the QuadMeasure object
	 *
	 *@param  ind  The Parameter
	 *@return      The yPos value
	 */
	public double getYPos(int ind) {
		return ( yPosV.get(ind)).doubleValue();
	}

	/**
	 *  Description of the Method
	 */
	public void measure() {
		rbField = quadElm.getWrpChRBField().getValue();
		DefaultListModel<BPM_Element> bpmsListModel = bpmsTable.getListModel();
		for(int i = 0, n = bpmsListModel.size(); i < n; i++) {
			BPM_Element bpmElm = bpmsListModel.get(i);
			if(bpmElm.isActive()) {
				bpmsV.add(bpmElm);
				xPosV.add(new Double(bpmElm.getWrpChannelX().getValue()));
				yPosV.add(new Double(bpmElm.getWrpChannelY().getValue()));
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(DataAdaptor da) {
		da.setValue("current", current);
		da.setValue("rbField", rbField);
		for(int i = 0, n = bpmsV.size(); i < n; i++) {
			BPM_Element bpmElm =  bpmsV.get(i);
			DataAdaptor xyDA = da.createChild("BPM_and_XY");
			xyDA.setValue("bpm", bpmElm.getName());
			xyDA.setValue("x", ( xPosV.get(i)).doubleValue());
			xyDA.setValue("y", ( yPosV.get(i)).doubleValue());
		}
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(DataAdaptor da) {
		current = da.doubleValue("current");
		rbField = da.doubleValue("rbField");

		bpmsV.clear();
		xPosV.clear();
		yPosV.clear();
        
        for (final DataAdaptor xyDA : da.childAdaptors()) {
			BPM_Element bpmElm = bpmsTable.getBPM(xyDA.stringValue("bpm"));
			if(bpmElm != null) {
				bpmsV.add(bpmElm);
				xPosV.add(new Double(xyDA.doubleValue("x")));
				yPosV.add(new Double(xyDA.doubleValue("y")));
			}
		}
	}
}

