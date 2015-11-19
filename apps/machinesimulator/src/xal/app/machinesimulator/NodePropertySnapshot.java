/**
 * 
 */
package xal.app.machinesimulator;

import xal.smf.AcceleratorNode;

/**
 * @author luxiaohan
 *record the value used for simulation
 */
public class NodePropertySnapshot {
	
	
	/**accelerator node*/
	final private AcceleratorNode NODE;
	/**property name*/
	final private String PROPERTY_NAME;
	/**the value*/
	final private double VALUE;
	
	/**Constructor*/
	public NodePropertySnapshot( final AcceleratorNode node, final String propertyName, final double value ){
		NODE = node;		
		PROPERTY_NAME = propertyName;
		VALUE = value ;
	}
	
	/**get the sequence name which the node belong to*/
	public String getSequenceName(){
		return NODE.getParent().getId();
	}
	
	/**get the node*/
	public AcceleratorNode getAcceleratorNode(){
		return NODE;
	}
	
	/**get property name*/
	public String getPropertyName(){
		return PROPERTY_NAME;
	}
	
	/**get the value*/
	public double getValue(){	
		return VALUE;
	}

}
