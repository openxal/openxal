/*
 * Created on Oct 23, 2003
 */
package xal.sim.scenario;

import xal.smf.AcceleratorNode;

/**
 * Serves as a input variable for the on-line model.
 * 
 * @author Craig McChesney
 */
public class ModelInput {
	
	// Instance Variables ======================================================
	
	private AcceleratorNode node;
	private String property;
	private double value;
	
	
	// Constructors ============================================================
	
	public ModelInput(AcceleratorNode aNode, String propName) {
		node = aNode;
		property = propName;
	}
	
	public ModelInput(AcceleratorNode aNode, String propName, double val) {
		node = aNode;
		property = propName;
		value = val;
	}
	
	
	// State ===================================================================
	
	public void setDoubleValue(double val) {
		value = val;
	}
	
	public double getDoubleValue() {
		return value;
	}
	
	public String getProperty() {
		return property;
	}
	
	public AcceleratorNode getAcceleratorNode() {
		return node;
	}

}
