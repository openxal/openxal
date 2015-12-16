/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

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
	/**the channel number of the node map with node type*/
	final private Map<String, Integer> CHAN_NUM;
	
	/**Constructor*/
	public DiagnosticConfiguration( final AcceleratorSeq sequence ) {
		DIAGS = new ArrayList<DiagnosticAgent>();
		CHAN_NUM = new LinkedHashMap<String, Integer>();
		configure( sequence );
	}
	
	/**get the diagnostics from sequence*/
	private void configure( final AcceleratorSeq seq ) {
		for ( final AcceleratorNode node : seq.getAllNodes() ) {				
			if ( node.getStatus() ) {
				if ( node instanceof BPM ){
					DIAGS.add( new DiagnosticAgent( seq, node, BPM.X_AVG_HANDLE, BPM.Y_AVG_HANDLE, null ) );
					CHAN_NUM.put( node.getType(), 2 );
				}
			}
		}
	}
	
	/**get the channel number*/
	public Map<String, Integer> getChanNum() {
		return CHAN_NUM;
	}
	
	/**get the list of diagnostic*/
	public List<DiagnosticAgent> getBpms() {
		return DIAGS;
	}
	
	/**get the snapshot of the diagnostic values*/
	public List<DiagnosticSnapshot> snapshotValues () {
		List<DiagnosticSnapshot> snapshots = new ArrayList<DiagnosticSnapshot>();
		for ( final String type : CHAN_NUM.keySet() ) {
			for ( final DiagnosticAgent diag : DIAGS ) {
				if ( diag.getNode().getType().equals( type ) ){
					if ( diag.getCheckState() ) {
						snapshots.add( new DiagnosticSnapshot( diag.getNode(),diag.getNames(), diag.getValues() ) );
					}
					else snapshots.add( new DiagnosticSnapshot( diag.getNode(), diag.getNames() ) );
				}
			}
		}

		return snapshots;
	}
	
	/**record the values of the diagnostics*/
	public List<DiagnosticRecord> createDiagRecords() {
		List<DiagnosticRecord> diagRecords = new ArrayList<DiagnosticRecord>();
		for ( final String type : CHAN_NUM.keySet() ){
			for ( int chanNum = 0; chanNum < CHAN_NUM.get( type ); chanNum++ ){
				for ( final DiagnosticAgent diag: DIAGS ){
					if ( diag.getNode().getType().equals(type) && diag.getNames().get( chanNum ) !=null ) {
						diagRecords.add( new DiagnosticRecord( diag.getNode(), diag.getPosition(), diag.getNames().get( chanNum ) ) );					
					}
				}
			}
		}
		return diagRecords;
	}
	
	class DiagnosticSnapshot {
		/**the node*/
		final private AcceleratorNode NODE;
		/**the values*/
		final private Vector<Double> VALUES;
		/**the values' name*/
		final private Vector<String> NAMES;
		
		/**Constructor*/
		public DiagnosticSnapshot( final AcceleratorNode node, final Vector<String> names ) {
			NODE = node;
			NAMES = names;
			VALUES = new Vector<Double>();
			initialValues();
		}
		
		/** Constructor*/
		public DiagnosticSnapshot( final AcceleratorNode node, final Vector<String> names, final Vector<Double> values ) {
			NODE = node;
			VALUES = values;
			NAMES = names;
		}
		
		/**initialize the values to Double.NaN*/
		private void initialValues() {
			for ( int index = 0; index < VALUES.size(); index++ ){
				VALUES.add( Double.NaN );
			}
		}
		
		/**get the node*/
		public AcceleratorNode getNode() {
			return NODE;
		}
		
		/**get the values*/
		public Double[] getValues() {
			return VALUES.toArray( new Double[VALUES.size()] );
		}
		
		/**get the values' name*/
		public String[] getNames() {
			return NAMES.toArray( new String[NAMES.size()] );
		}
		
	}

}
