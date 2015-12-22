/**
 * 
 */
package xal.app.machinesimulator;

import java.util.ArrayList;
import java.util.List;

import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.BPM;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

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
		DIAGS.addAll( registerDevices( seq, BPM.s_strType, 0.001, BPM.X_AVG_HANDLE, BPM.Y_AVG_HANDLE, null ) );
	}
	
	/**
	 * register a new type of diagnostic device from the specified sequence
	 * @param seq the specified sequence
	 * @param type the specified type to register
	 * @param scale the scale to configure the values
	 * @param handles the existing channel handles of (x,y,z) 
	 * @return the list of diagnostic device
	 */
	private List<DiagnosticAgent> registerDevices( final AcceleratorSeq seq, final String type,
			final double scale, final String xHandle, final String yHandle, final String zHandle ) {
		List<DiagnosticAgent> diags = new ArrayList<DiagnosticAgent>();
		for ( final AcceleratorNode node : seq.getAllNodes() ) {
			if ( node.getStatus() ) {
				if ( node.getType().equals( type ) ) {
					DiagnosticAgent agent = new DiagnosticAgent( seq, node, xHandle, yHandle, zHandle );
					agent.setCommonScale( scale );
					diags.add( agent );
				}
			}
		}
		return diags;
	}
	
	/**get the list of diagnostic*/
	public List<DiagnosticAgent> getDiagnosticAgents() {
		return DIAGS;
	}
	
	/**get the snapshot of the diagnostic values*/
	public List<DiagnosticSnapshot> snapshotValues () {
		List<DiagnosticSnapshot> snapshots = new ArrayList<DiagnosticSnapshot>();
		for ( final DiagnosticAgent diag : DIAGS ) {
			if ( diag.getCheckState() ) {
				snapshots.add( new DiagnosticSnapshot( diag.getNode(),diag.getNames(), diag.getValues() ) );
			}
			else snapshots.add( new DiagnosticSnapshot( diag.getNode(), diag.getNames() ) );
		}

		return snapshots;
	}
	
	/**record the values of the diagnostics*/
	public List<DiagnosticRecord> createDiagRecords() {
		List<DiagnosticRecord> diagRecords = new ArrayList<DiagnosticRecord>();
		for ( final DiagnosticAgent diag: DIAGS ){
			for ( int chanNum = 0; chanNum < diag.getNames().length; chanNum++ ){
				if ( diag.getNames()[chanNum] !=null ) {
					diagRecords.add( new DiagnosticRecord( diag.getNode(), diag.getPosition(), diag.getNames()[chanNum] ) );					
				}
			}
		}
		return diagRecords;
	}
	
	class DiagnosticSnapshot implements DataListener {
		/** the data adaptor label used for reading and writing this document */
		static public final String DATA_LABEL = "DiagnosticSnapshot"; 
		/**the node*/
		final private AcceleratorNode NODE;
		/**the values*/
		final private Double[] VALUES;
		/**the values' name*/
		final private String[] NAMES;
		
		/**Constructor*/
		public DiagnosticSnapshot( final AcceleratorNode node, final String[] names ) {
			NODE = node;
			NAMES = names;
			VALUES = new Double[names.length];
			initialValues();
		}
		
		/** Constructor*/
		public DiagnosticSnapshot( final AcceleratorNode node, final String[] names, final Double[] values ) {
			NODE = node;
			VALUES = values;
			NAMES = names;
		}
		
		/**initialize the values to Double.NaN*/
		private void initialValues() {
			for ( int index = 0; index < VALUES.length; index++ ){
				VALUES[index] =  Double.NaN;
			}
		}
		
		/**get the node*/
		public AcceleratorNode getNode() {
			return NODE;
		}
		
		/**get the values*/
		public Double[] getValues() {
			return VALUES;
		}
		
		/**get the values' name*/
		public String[] getNames() {
			return NAMES;
		}
		

		/** provides the name used to identify the class in an external data source. */
		public String dataLabel() {
			return DATA_LABEL;
		}

		/** Instructs the receiver to update its data based on the given adaptor. */
		public void update(DataAdaptor adaptor) {
			// TODO Auto-generated method stub
			
		}

		/** Instructs the receiver to write its data to the adaptor for external storage. */
		public void write(DataAdaptor adaptor) {

		}
		
	}

}
