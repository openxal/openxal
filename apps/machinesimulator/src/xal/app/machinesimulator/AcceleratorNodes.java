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
import xal.smf.impl.RfCavity;

/**
 * @author luxiaohan
 *Select the specified nodes from the sequence
 */
public class AcceleratorNodes {
	
	/**the list of AcceleratorNodeRecord*/
	final private List<AcceleratorNodeRecord> RECORDS;
	/**the list of specified AcceleratorNodes*/
	final private List<AcceleratorNode> NODES;
	/**the scenario to decide which value to use*/
	final private Scenario SCENARIO;
	
	/**Constructor*/
	public AcceleratorNodes( final AcceleratorSeq sequence, final Scenario scenario){
		SCENARIO = scenario;
		RECORDS = new ArrayList<AcceleratorNodeRecord>();
		NODES = new ArrayList<AcceleratorNode>();
		configRecords( sequence );
	}
	/**select the specified nodes from the sequence*/
	private void configRecords( final AcceleratorSeq sequence ){
		for(AcceleratorNode node:sequence.getAllNodes()){
			if ( node instanceof Electromagnet ) NODES.add( node );//|| node instanceof RfCavity
		}
		
		for ( AcceleratorNode node:NODES ){
			RECORDS.add( new AcceleratorNodeRecord( node, SCENARIO ) );
		}
		
	}
	/**
	 * get a list of AcceleratorNodeRecord
	 * @return a list of AcceleratorNodeRecord
	 */
	public List<AcceleratorNodeRecord> getAcceleratorNodeRecords(){
		return RECORDS;
	}

}
