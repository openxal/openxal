/**
 * 
 */
package xal.app.machinesimulator;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author luxiaohan
 * record the values used for simulations
 */
public class NodePropertyHistoryRecord {
	
	/**accelerator node id*/
	final private String NODE_ID;
	/**property name*/
	final private String PROPERTY_NAME;
	/**the list of values to show*/
	final private Map<Date, Double> VALUES_SHOW;
	
	/**Constructor*/
	public NodePropertyHistoryRecord( final String nodeId, final String propertyName ){
		NODE_ID = nodeId;	
		PROPERTY_NAME = propertyName;
		VALUES_SHOW = new TreeMap<Date,Double>();
	}
	
	/**get the node id*/
	public String getNodeId(){
		return NODE_ID;
	}
	
	/**get property name*/
	public String getPropertyName(){
		return PROPERTY_NAME;
	}
	
	/**get the values used for simulations*/
	public Double[] getValues(){
		Double[] values = new Double[VALUES_SHOW.size()];
		VALUES_SHOW.values().toArray( values );
		
		return values;
	}
	
	/**add a value to the value list*/
	public void addValue( final Date time, final double value ){
		VALUES_SHOW.put( time, value );
	}
	
	/**remove the value with specified time key*/
	public void removeValue( final Date time ){
		VALUES_SHOW.remove( time );
	}
	

}
