/**
 * 
 */
package xal.app.machinesimulator;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import xal.smf.AcceleratorNode;

/**
 * @author luxiaohan
 *Record diagnostic data 
 */
public class DiagnosticRecord {
	/**the node to record*/
	final private AcceleratorNode NODE;
	/**the position*/
	final private Double POSITION;
	/**the value to record*/
	final private Map<Date, Double> VALUES;
	/**the value name*/
	final private String VALUE_NAME;
	
	public DiagnosticRecord( final AcceleratorNode node, final Double pos, final String name ) {
		NODE = node;
		POSITION = pos;
		VALUE_NAME = name;
		VALUES = new TreeMap<Date,Double>();
	}
	
	/**get the node*/
	public AcceleratorNode getNode() {
		return NODE;
	}
	
	/**get the bpm position*/
	public Double getPosition() {
		return POSITION;
	}
	
	public String getValueName() {
		return VALUE_NAME;
	}
	
	/**returns the values*/
	public Double[] getValues() {
		Double[] values = new Double[VALUES.size()];
		VALUES.values().toArray(values);
		return values;
	}
	
	/**add a value to the value list*/
	public void addValue( final Date time, final double value ){
		VALUES.put( time, value );
	}
	
	/**remove the value with specified time key*/
	public void removeValue( final Date time ){
		VALUES.remove( time );
	}

}
