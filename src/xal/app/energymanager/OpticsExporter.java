//
//  OpticsExporter.java
//  xal
//
//  Created by Thomas Pelaia on 7/19/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.tools.data.*;
import xal.tools.xml.*;
import xal.smf.*;

import java.io.*;
import java.util.*;
import java.text.*;


/** Exports the parameter settings as an optics extra file. */
public class OpticsExporter implements DataListener {
	/** the model with the optics to export */
	final protected EnergyManager _model;
	
	/** document adaptor */
	protected DataAdaptor _documentAdaptor;
	
	/** sequence adaptors keyed by sequence */
	protected Map< AcceleratorSeq, DataAdaptor> _sequenceAdaptors;
	
	
	/**
	 * Constructor
	 * @param model the model to export
	 */
	protected OpticsExporter( final EnergyManager model ) {
		_model = model;
	}
	
	
	/**
	 * Get a new node adaptor for the specified sequence.
	 * @param sequence the sequence for which to get a new child adaptor.
	 * @param tag the tag for the new node adaptor
	 * @return a new child adaptor for the specified sequence
	 */
	synchronized public DataAdaptor getChildAdaptor( final AcceleratorSeq sequence, final String tag ) {
		if ( !_sequenceAdaptors.containsKey( sequence ) ) {
			final DataAdaptor adaptor = _documentAdaptor.createChild( "sequence" );
			adaptor.setValue( "id", sequence.getId() );
			_sequenceAdaptors.put( sequence, adaptor );
		}
		
		final DataAdaptor sequenceAdaptor = _sequenceAdaptors.get( sequence );
		
		return sequenceAdaptor.createChild( tag );
	}
	
	
    /** 
	* Provides the name used to identify the class in an external data source.
	* @return a tag that identifies the receiver's type
	*/
    public String dataLabel() {
		return "xdxf";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    synchronized public void write( final DataAdaptor adaptor ) {
		_documentAdaptor = adaptor;
		_sequenceAdaptors = new HashMap<AcceleratorSeq, DataAdaptor>();
		
		adaptor.setValue( "system", "sns" );
        adaptor.setValue( "ver", "2.0.0" );
		
        final String dateString = new SimpleDateFormat( "MM.dd.yyyy" ).format( new Date() );
        adaptor.setValue( "date", dateString );
		
		final Iterator<NodeAgent> nodeIter = _model.getNodeAgents().iterator();
		while ( nodeIter.hasNext() ) {
			final NodeAgent nodeAgent = nodeIter.next();
			nodeAgent.exportOpticsChanges( this );
		}
	}
	
	
	/**
	 * Export the model optics changes to an optics file.
	 */
	static public void exportChanges( final EnergyManager model, final Writer writer ) {
		XmlDataAdaptor.newDocumentAdaptor( new OpticsExporter( model ), "xdxf.dtd" ).writeTo( writer );
	}
}
