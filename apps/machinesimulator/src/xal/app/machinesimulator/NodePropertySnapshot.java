/**
 * 
 */
package xal.app.machinesimulator;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 * @author luxiaohan
 *record the value used for simulation
 */
public class NodePropertySnapshot implements DataListener {
	
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "NodePropertySnapshot";
	/**accelerator node id*/
	final private String NODE_ID;
	/**property name*/
	final private String PROPERTY_NAME;
	/**the value*/
	final private double VALUE;
	
	/**Constructor*/
	public NodePropertySnapshot( final String nodeId, final String propertyName, final double value ){
		NODE_ID = nodeId;		
		PROPERTY_NAME = propertyName;
		VALUE = value ;
	}
	
	/**Constructor with adaptor*/
	public NodePropertySnapshot ( final DataAdaptor adaptor ) {
		NODE_ID = adaptor.hasAttribute( "nodeId" ) ? adaptor.stringValue( "nodeId" ) : "*Data missing*";
		PROPERTY_NAME = adaptor.hasAttribute( "propertyName" ) ? adaptor.stringValue( "propertyName" ) : "*Data missing*";
		VALUE = adaptor.hasAttribute( "value" ) ? adaptor.doubleValue( "value" ) : Double.NaN;
	}
	
	/**get the node id*/
	public String getNodeId(){
		return NODE_ID;
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
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write(DataAdaptor adaptor) {
		adaptor.setValue( "nodeId", NODE_ID );
		adaptor.setValue( "propertyName", PROPERTY_NAME );
		adaptor.setValue( "value", VALUE );
	}

}
