/**
 * 
 */
package xal.app.machinesimulator;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 * @author luxiaohan
 *record the values of one diagnostic node
 */

public class DiagnosticSnapshot implements DataListener {
	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "DiagnosticSnapshot"; 
	/**the node id*/
	final private String NODE_ID;
	/**the values*/
	final private double[] VALUES;
	/**the values' name*/
	final private String[] NAMES;
	
	/**Constructor*/
	public DiagnosticSnapshot( final String nodeId, final String[] names ) {
		NODE_ID = nodeId;
		NAMES = names;
		VALUES = new double[names.length];
		initialValues();
	}
	
	/** Constructor*/
	public DiagnosticSnapshot( final String nodeId, final String[] names, final double[] values ) {
		NODE_ID = nodeId;
		VALUES = values;
		NAMES = names;
	}
	
	/**Constructor with dataAdaptor*/
	public DiagnosticSnapshot( final DataAdaptor adaptor ) {
		NODE_ID = adaptor.hasAttribute( "nodeId" ) ? adaptor.stringValue( "nodeId" ) : "*Data missing*";
		NAMES = new String[3];
		VALUES = new double[3];
		update( adaptor );
	}
	
	/**initialize the values to Double.NaN*/
	private void initialValues() {
		for ( int index = 0; index < VALUES.length; index++ ){
			VALUES[index] =  Double.NaN;
		}
	}
	
	/**get the node id*/
	public String getNodeId() {
		return NODE_ID;
	}
	
	/**get the values*/
	public double[] getValues() {
		return VALUES;
	}
	
	/**get the values' name*/
	public String[] getValueNames() {
		return NAMES;
	}
	

	/** provides the name used to identify the class in an external data source. */
	public String dataLabel() {
		return DATA_LABEL;
	}

	/** Instructs the receiver to update its data based on the given adaptor. */
	public void update(DataAdaptor adaptor) {
		for ( int index = 0; index < NAMES.length; index++ ) {
			if ( adaptor.hasAttribute( "valueNames."+index ) ) {
				NAMES[index] = adaptor.stringValue( "valueNames."+index );
				VALUES[index] = adaptor.doubleValue( "values."+index );
			}
		}
		
	}

	/** Instructs the receiver to write its data to the adaptor for external storage. */
	public void write(DataAdaptor adaptor) {
		adaptor.setValue( "nodeId", NODE_ID );
		
		for ( int index = 0; index < NAMES.length; index++ ){
			if ( NAMES[index] != null ) {
				adaptor.setValue( "valueNames."+index, NAMES[index] );
				adaptor.setValue( "values."+index, VALUES[index] );
			}
		}
	}
	
}
