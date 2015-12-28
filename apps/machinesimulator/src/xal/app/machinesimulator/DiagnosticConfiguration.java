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
				snapshots.add( new DiagnosticSnapshot( diag.getNode().getId(),diag.getNames(), diag.getValues() ) );
			}
			else snapshots.add( new DiagnosticSnapshot( diag.getNode().getId(), diag.getNames() ) );
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

}
