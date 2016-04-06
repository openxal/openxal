/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.sim.scenario.ModelInput;
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
	public WhatIfConfiguration( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData, final List<ModelInput> modelInputs ){
		RECORDS = new ArrayList<NodePropertyRecord>();
		configRecords( sequence, loggedData, modelInputs );
	}
	
	/**select the specified nodes from the sequence*/
	private void configRecords( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData, final List<ModelInput> modelInputs ){
		for( AcceleratorNode node:sequence.getAllNodes() ){
			if ( node.getStatus() ){
				ModelInput modelInputRecord = filterModelInputs( node, modelInputs );
				if( node instanceof PermanentMagnet ){
					RECORDS.add( new NodePropertyRecord(node, PermanentMagnetPropertyAccessor.PROPERTY_FIELD , Double.NaN, modelInputRecord ) );
				}
				else if( node instanceof Electromagnet ){
					double loggedValue = ( loggedData == null ) ? Double.NaN : loggedData.getLoggedField( (Electromagnet) node ); 
					RECORDS.add( new NodePropertyRecord(node, ElectromagnetPropertyAccessor.PROPERTY_FIELD, loggedValue, modelInputRecord ) );
				}
				else if( node instanceof RfCavity ){
					RECORDS.add( new NodePropertyRecord(node, RfCavityPropertyAccessor.PROPERTY_AMPLITUDE, Double.NaN, modelInputRecord ) );
					RECORDS.add( new NodePropertyRecord(node, RfCavityPropertyAccessor.PROPERTY_PHASE, Double.NaN, modelInputRecord ) );
				}
			}


		}
		
	}
	
	/**
	 * filter the ModelInputs
	 * @param node the node to check if there is a corresponding modelInput record
	 * @param modelInputs the modelInput records
	 * @return the modelInput if there is one , or return null
	 */
	private ModelInput filterModelInputs( final AcceleratorNode node, final List<ModelInput> modelInputRecords ) {
		List<ModelInput> modelInputs;
		if ( modelInputRecords == null ) {
			modelInputs = new ArrayList<ModelInput>();
		}
		else modelInputs = modelInputRecords;
		
		ModelInput theModelInput = null;
		for ( final ModelInput modelInput : modelInputs ) {
			if ( modelInput.getAcceleratorNode().getId().equals( node.getId() ) ) {
				theModelInput = modelInput;
			}
		}
		return theModelInput;
	}
	
	/**
	 * get a list of AcceleratorNodeRecord
	 * @return a list of AcceleratorNodeRecord
	 */
	public List<NodePropertyRecord> getNodePropertyRecords(){
		return RECORDS;
	}

}
