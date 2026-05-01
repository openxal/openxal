//
//  Optics.java
//  xal
//
//  Created by Tom Pelaia on 10/22/07.
//  Copyright 2007 __MyCompanyName__. All rights reserved.
//

package xal.app.opticseditor;

import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;

import java.io.File;
import java.net.URL;
import java.util.*;


/** Optics Model */
public class Optics {
	/** node records */
	final private List<NodeRecord> _nodeRecords;
	
	/** base URL */
	final private URL BASE_URL;
	
	/** hardware status URL */
	private URL _hardwareStatusURL;
	
	
	/** Constructor */
	public Optics( final URL baseURL, final String designURLSpec, final String hardwareStatusURLSpec ) {
		try {
			BASE_URL = baseURL;
			
			final Map<String, NodeRecord> records = new HashMap<String, NodeRecord>();

			if ( baseURL != null ) {
				if ( designURLSpec != null ) {
					final URL designURL = new URL( baseURL, designURLSpec );			
					final DataAdaptor designAdaptor = XmlDataAdaptor.adaptorForUrl( designURL, false );
					loadNodesFromOpticsAdaptor( designAdaptor, records );
					
					if ( hardwareStatusURLSpec != null ) {
						final URL hardwareStatusURL = new URL( baseURL, hardwareStatusURLSpec );
						_hardwareStatusURL = hardwareStatusURL;
						final DataAdaptor hardwareStatusAdaptor = XmlDataAdaptor.adaptorForUrl( hardwareStatusURL, false );
						loadNodesFromOpticsAdaptor( hardwareStatusAdaptor, records );
					}					
				}				
			}
			
			final List<NodeRecord> nodeRecords = new ArrayList<NodeRecord>( records.values() );
			Collections.sort( nodeRecords );
			_nodeRecords = nodeRecords;
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Exception loading optics.", exception );
		}
	}
	
	
	/** load the nodes design nodes from the Optics adaptor */
	private void loadNodesFromOpticsAdaptor( final DataAdaptor opticsAdaptor, final Map<String, NodeRecord> records ) {
		final DataAdaptor acceleratorAdaptor = opticsAdaptor.childAdaptor( "xdxf" );
		
		final List<DataAdaptor> sequenceAdaptors = acceleratorAdaptor.childAdaptors( "sequence" );
		for ( final DataAdaptor sequenceAdaptor : sequenceAdaptors ) {
			loadNodesFromSequenceAdaptor( sequenceAdaptor, records );
		}
	}
	
		
	/** load the nodes design nodes from the sequence adaptor */
	private void loadNodesFromSequenceAdaptor( final DataAdaptor sequenceAdaptor, final Map<String, NodeRecord> records ) {
		final String sequenceID = sequenceAdaptor.stringValue( "id" );
		
		final List<DataAdaptor> nodeAdaptors = sequenceAdaptor.childAdaptors( "node" );
		for ( final DataAdaptor nodeAdaptor : nodeAdaptors ) {
			final String nodeID = nodeAdaptor.stringValue( "id" );
			final boolean status = nodeAdaptor.hasAttribute( "status" ) ? nodeAdaptor.booleanValue( "status" ) : true;
			final boolean exclude = nodeAdaptor.hasAttribute( "exclude" ) ? nodeAdaptor.booleanValue( "exclude" ) : false;
			final String modComment = nodeAdaptor.hasAttribute( "mod-comment" ) ? nodeAdaptor.stringValue( "mod-comment" ) : null;
			
			if ( records.containsKey( nodeID ) ) {
				final NodeRecord record = records.get( nodeID );
				record.setStatus( status );
				record.setExclude( exclude );
				record.setModificationComment( modComment );
			}
			else {
				final NodeRecord record = new NodeRecord( nodeID, sequenceID, status, exclude, modComment );
				records.put( nodeID, record );
			}
		}
	}
	
	
	/** get the node records */
	public List<NodeRecord> getNodeRecords() {
		return _nodeRecords;
	}
	
	
	/** 
	 * save the hardware status file 
	 * @return true upon success and false otherwise
	 */
	protected boolean save( final OpticsEditorDocument document ) throws Exception {
		if ( _hardwareStatusURL == null ) {
			if ( !createHardwareStatusFile( document ) ) {
				return false;
			}
		}
		
		final XmlDataAdaptor hardwareStatusAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
		final DataAdaptor opticsAdaptor = hardwareStatusAdaptor.createChild( "xdxf" );
        opticsAdaptor.setValue( "version", "1.0.0" );
        opticsAdaptor.setValue( "date", new Date().toString() );
		writeSequences( opticsAdaptor );
		hardwareStatusAdaptor.writeToUrl( _hardwareStatusURL );
		
		return true;
	}
	
	
	/** create a hardware status file */
	private boolean createHardwareStatusFile( final OpticsEditorDocument document ) throws Exception {
		final XmlDataAdaptor baseAdaptor = XmlDataAdaptor.adaptorForUrl( BASE_URL, false );
		final DataAdaptor sourcesAdaptor = baseAdaptor.childAdaptor( "sources" );
		final DataAdaptor hardwareStatusRefAdaptor = sourcesAdaptor.createChild( "hardware_status" );
		hardwareStatusRefAdaptor.setValue( "name", "Hardware Status" );
		final String hardwareStatusURLSpec = document.newHardwareStatusURLSpec( BASE_URL );
		
		if ( hardwareStatusURLSpec != null ) {
			hardwareStatusRefAdaptor.setValue( "url", hardwareStatusURLSpec );
			_hardwareStatusURL = new URL( BASE_URL, hardwareStatusURLSpec );
			baseAdaptor.writeToUrl( BASE_URL );
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/** write the modified sequences to the optics adaptor */
	private void writeSequences( final DataAdaptor opticsAdaptor ) {
		// determine the modified node records and organize them by sequence
		final Map<String, List<NodeRecord>> modificationTable = new HashMap<String, List<NodeRecord>>();
		for ( final NodeRecord record : _nodeRecords ) {
			if ( record.isModified() ) {
				final String sequenceID = record.getSequenceID();
				if ( modificationTable.containsKey( sequenceID ) ) {
					modificationTable.get( sequenceID ).add( record );
				}
				else {
					final List<NodeRecord> nodeRecords = new ArrayList<NodeRecord>();
					nodeRecords.add( record );
					modificationTable.put( sequenceID, nodeRecords );
				}
			}
		}
		for ( final String sequenceID : modificationTable.keySet() ) {
			final DataAdaptor sequenceAdaptor = opticsAdaptor.createChild( "sequence" );
			sequenceAdaptor.setValue( "id", sequenceID );
			final List<NodeRecord> records = modificationTable.get( sequenceID );
			writeNodes( sequenceAdaptor, records );
		}
	}
	
	
	/** write the node records to the specified sequence adaptor */
	private void writeNodes( final DataAdaptor sequenceAdaptor, final List<NodeRecord> records ) {
		for ( final NodeRecord record : records ) {
			final DataAdaptor nodeAdaptor = sequenceAdaptor.createChild( "node" );
			record.writeTo( nodeAdaptor );
		}
	}
}
