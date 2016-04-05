/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.PermanentMagnet;
import xal.smf.impl.RfCavity;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.smf.proxy.PermanentMagnetPropertyAccessor;
import xal.smf.proxy.RfCavityPropertyAccessor;

/**
 * @author luxiaohan
 *Select the specified nodes from the sequence
 */
public class WhatIfConfiguration {
	
	/**the list of AcceleratorNodeRecord*/
	final private List<NodePropertyRecord> RECORDS;
	
	/**Constructor*/
	public WhatIfConfiguration( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData ){
		RECORDS = new ArrayList<NodePropertyRecord>();
		configRecords( sequence, loggedData );
	}
	
	/**select the specified nodes from the sequence*/
	private void configRecords( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData ){
		for( AcceleratorNode node:sequence.getAllNodes() ){
			if ( node.getStatus() ){
				if( node instanceof PermanentMagnet ){
					RECORDS.add( new NodePropertyRecord(node, PermanentMagnetPropertyAccessor.PROPERTY_FIELD , Double.NaN ) );
				}
				else if( node instanceof Electromagnet ){
					double loggedValue = ( loggedData == null ) ? Double.NaN : loggedData.getLoggedField( (Electromagnet) node ); 
					RECORDS.add( new NodePropertyRecord(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, loggedValue ) );
				}
				else if( node instanceof RfCavity ){
					RECORDS.add( new NodePropertyRecord(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, Double.NaN ) );
					RECORDS.add( new NodePropertyRecord(node, RfCavityPropertyAccessor.PROPERTY_PHASE, Double.NaN ) );
				}
			}


		}
		
	}
	
	/**
	 * get a list of AcceleratorNodeRecord
	 * @return a list of AcceleratorNodeRecord
	 */
	public List<NodePropertyRecord> getNodePropertyRecords(){
		return RECORDS;
	}

}
