package xal.app.machinesimulator;

import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.RfCavity;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
/**
 * 
 * @author luxiaohan
 *get and set values of the specified property in an accelerator node
 */
public class AcceleratorNodeRecord {
	/**the accelerator node*/
	final private AcceleratorNode NODE;
	/**the specified scenario*/
	final private Scenario SCENARIO;
	/**magnet*/
	private Electromagnet magnet;
	/**rf cavity*/
	private RfCavity 	rfCavity;
	
	/**Constructor*/
	public AcceleratorNodeRecord( final AcceleratorNode node, final Scenario scenario) {
		NODE = node;
		SCENARIO = scenario;
		if( node instanceof Electromagnet) magnet = (Electromagnet)node;
		if( node instanceof RfCavity ) rfCavity = (RfCavity)node;
	}
	
	public AcceleratorNode getNode(){
		return NODE;
	}
	
	public Electromagnet getElectromagnet(){
		return magnet;
	}
	
	public double getTestValue(){
		double value;
		if(SCENARIO.getModelInput(magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD) == null) {
			value = magnet.getDfltField();
		}
		else value = SCENARIO.getModelInput(magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD).getDoubleValue();
		return value;
	}
	
	public void setTestValue( final double vule ){
		SCENARIO.setModelInput(magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, vule);
	}
	
	public RfCavity getRfCavity(){
		return rfCavity;	
	}
	


}
