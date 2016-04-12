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
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

/**
 * @author luxiaohan
 *Select the specified nodes from the sequence
 */
public class WhatIfConfiguration implements DataListener {
	
 	/** the data adaptor label used for reading and writing this document */
	static public final String DATA_LABEL = "WhatIfConfiguration";	
	/**the list of AcceleratorNodeRecord*/
	final private List<NodePropertyRecord> RECORDS;
    /**the sequence*/
    private AcceleratorSeq sequence;
	/**the pvlogger data*/
	private PVLoggerDataSource pvLoggerDataSource;
	
	/**Constructor*/
	public WhatIfConfiguration( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData ){
        this.sequence = sequence;
		RECORDS = new ArrayList<>();
		pvLoggerDataSource = loggedData;
		configRecords( sequence, loggedData );
	}
	
	/**Constructor with adaptor*/
	public WhatIfConfiguration( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData, final DataAdaptor adaptor ){
        this.sequence = sequence;
		RECORDS = new ArrayList<>();
		pvLoggerDataSource = loggedData;
        update( adaptor );
	}
	
	/**select the specified nodes from the sequence*/
	private void configRecords( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData ){
		for( AcceleratorNode node : sequence.getAllNodes() ){
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
        
        /**restore the nodePropertyRecord from adaptor*/
	private void configRecords( final AcceleratorSeq sequence, final PVLoggerDataSource loggedData, final List<DataAdaptor> adaptors ){
		for( final DataAdaptor adaptor : adaptors ){
            AcceleratorNode node = sequence.getNodeWithId( adaptor.stringValue( "nodeId" ) );
            double loggedValue = ( loggedData != null && node instanceof Electromagnet ) ? loggedData.getLoggedField( (Electromagnet) node ) : Double.NaN ;
            if ( node != null ) RECORDS.add( new NodePropertyRecord( node, adaptor.stringValue( "propertyName" ), loggedValue, adaptor ) );
		}
	}
	
	/**
	 * get a list of AcceleratorNodeRecord
	 * @return a list of AcceleratorNodeRecord
	 */
	public List<NodePropertyRecord> getNodePropertyRecords(){
		return RECORDS;
	}
        
    /**refresh the nodePropertyRecord when the model scenario changed*/
    public void refresh( final PVLoggerDataSource loggedDataSource ) {
        pvLoggerDataSource = loggedDataSource;
        for ( final NodePropertyRecord record : RECORDS ) {
            boolean state = pvLoggerDataSource != null && record.getAcceleratorNode() instanceof Electromagnet;
           double loggedValue = ( state ) ? pvLoggerDataSource.getLoggedField( (Electromagnet) record.getAcceleratorNode() ) : Double.NaN;
           record.refresh( loggedValue );
        }
    }
	
	/** provides the name used to identify the class in an external data source. */
        @Override
	public String dataLabel() {
		return DATA_LABEL;
	}
	
	/** Instructs the receiver to update its data based on the given adaptor. */
        @Override
	public void update( DataAdaptor adaptor ) {
        if ( adaptor != null && adaptor.hasAttribute( "sequenceId" ) && adaptor.stringValue( "sequenceId" ).equals( sequence.getId() ) ) {
            final List<DataAdaptor> nodeProRecordAdaptors = adaptor.childAdaptors( NodePropertyRecord.DATA_LABEL );
            configRecords( sequence, pvLoggerDataSource, nodeProRecordAdaptors );
        }
        else configRecords( sequence, pvLoggerDataSource );
	}
	
	/** Instructs the receiver to write its data to the adaptor for external storage. */
        @Override
	public void write( DataAdaptor adaptor ) {
        adaptor.setValue( "sequenceId", sequence.getId() );
	    adaptor.writeNodes( RECORDS );
	}

}
