/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.sim.scenario.Scenario;
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
	/**the scenario to decide which value to use*/
	final private Scenario SCENARIO;
	
	/**Constructor*/
	public WhatIfConfiguration( final AcceleratorSeq sequence, final Scenario scenario ){
		SCENARIO = scenario;
		RECORDS = new ArrayList<NodePropertyRecord>();
		configRecords( sequence );
	}
	
	/**select the specified nodes from the sequence*/
	private void configRecords( final AcceleratorSeq sequence ){
		for( AcceleratorNode node:sequence.getAllNodes() ){
			if ( node.getStatus() ){
				if( node instanceof PermanentMagnet ){
					RECORDS.add( new NodePropertyRecord(node, SCENARIO, PermanentMagnetPropertyAccessor.PROPERTY_FIELD ) );
				}
				if( node instanceof Electromagnet ){
					RECORDS.add( new NodePropertyRecord(node, SCENARIO, ElectromagnetPropertyAccessor.PROPERTY_FIELD ) );
				}
				
				if( node instanceof RfCavity ){
					RECORDS.add( new NodePropertyRecord(node, SCENARIO, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE ) );
					RECORDS.add( new NodePropertyRecord(node, SCENARIO, RfCavityPropertyAccessor.PROPERTY_PHASE ) );
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
