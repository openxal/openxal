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
 *get the diagnostic device from the sequence 
 */
public class DiagnosticConfiguration {
	
	/**the list of diagnostic device*/
	final private List<DiagnosticAgent> DIAGS;
	
	/**Constructor*/
	public DiagnosticConfiguration( final AcceleratorSeq sequence ) {
		DIAGS = new ArrayList<DiagnosticAgent>();
		configure( sequence );
	}
	
	/**get the diagnostics from sequence*/
	private void configure( final AcceleratorSeq seq ) {
		for ( final AcceleratorNode node : seq.getAllNodes() ) {			
			if ( node.getStatus() ) {
				if ( node instanceof BPM ) DIAGS.add( new DiagnosticAgent( (BPM)node ) );
			}
		}
	}
	
	/**get the list of diagnostic*/
	public List<DiagnosticAgent> getBpms() {
		return DIAGS;
	}
	
	/**get the snapshot of the diagnostic values*/
	public List<DiagnosticSnapshot> snapshotValues () {
		List<DiagnosticSnapshot> snapshots = new ArrayList<DiagnosticSnapshot>();
		for ( final DiagnosticAgent diag : DIAGS ) {
			if ( diag.getCheckState() ) {
				snapshots.add( new DiagnosticSnapshot( diag.getNode(), diag.getXAvg(), diag.getYAvg() ) );
			}
			else snapshots.add( new DiagnosticSnapshot( diag.getNode(), Double.NaN, Double.NaN ) );
		}
		return snapshots;
	}
	
	/**record the values of the diagnostics*/
	public List<DiagnosticRecord> createDiagRecords() {
		List<DiagnosticRecord> bpmRecords = new ArrayList<DiagnosticRecord>();
		for ( final DiagnosticAgent diag: DIAGS ){
			bpmRecords.add( new DiagnosticRecord( diag.getNode(), diag.getPosition(), diag.getXAvgName() ) );
		}
		for (final DiagnosticAgent diag: DIAGS) {
			bpmRecords.add( new DiagnosticRecord( diag.getNode(), diag.getPosition(), diag.getYAvgName() ) );
		}
		
		return bpmRecords;
	}
	
	class DiagnosticSnapshot {
		/**the node*/
		final private AcceleratorNode NODE;
		/**the x average position*/
		final private Double X_AVG;
		/**the y average position*/
		final private Double Y_AVG;
		
		/** Constructor*/
		public DiagnosticSnapshot( final AcceleratorNode node, final Double xAvg, final Double yAvg ) {
			NODE = node;
			X_AVG = xAvg;
			Y_AVG = yAvg;
		}
		
		/**get the node*/
		public AcceleratorNode getNode() {
			return NODE;
		}
		
		/**get xAvg*/
		public Double getXAvg() {
			return X_AVG;
		}
		
		/**get yAvg*/
		public Double getYAvg() {
			return Y_AVG;
		}
		
	}

}
