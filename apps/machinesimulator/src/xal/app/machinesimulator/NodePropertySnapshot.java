/**
 * 
 */
package xal.app.machinesimulator;

import xal.smf.AcceleratorNode;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 * @author luxiaohan
 *record the value used for simulation
 */
public class NodePropertySnapshot implements DataListener {
	
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "NodePropertySnapshot";
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

	/** provides the name used to identify the class in an external data source. */
	public String dataLabel() {
		return DATA_LABEL;
	}

	/** Instructs the receiver to update its data based on the given adaptor. */
	public void update(DataAdaptor adaptor) {
		// TODO Auto-generated method stub
		
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write(DataAdaptor adaptor) {

	}

}
