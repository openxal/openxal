/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;

/**
 * @author luxiaohan
 *get the bpms from the 
 */
public class BpmsConfiguration {
	
	/**the list of bpm*/
	final private List<BpmAgent> BPMS;
	
	/**Constructor*/
	public BpmsConfiguration( final AcceleratorSeq sequence ) {
		BPMS = new ArrayList<BpmAgent>();
		configure( sequence );
	}
	
	/**get the bpms from sequence*/
	private void configure( final AcceleratorSeq seq ) {
		for ( final AcceleratorNode node : seq.getAllNodes() ) {
			if ( node.getStatus() ) {
				if ( node instanceof BPM ) BPMS.add( new BpmAgent( (BPM)node ) );
			}
		}
	}
	
	/**get the list of bpm*/
	public List<BpmAgent> getBpms() {
		return BPMS;
	}
	
	public List<BpmRecord> recordBpms() {
		List<BpmRecord> bpmRecords = new ArrayList<BpmRecord>();
		for ( final BpmAgent bpm: BPMS ){
			if ( bpm.getCheckState() ){
				bpmRecords.add( new BpmRecord( bpm.getNode(), bpm.getPosition(), bpm.getXAvg(), bpm.getYAvg() ) );
			}
		}
		
		return bpmRecords;
	}

}
