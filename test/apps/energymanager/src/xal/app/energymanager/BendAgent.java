//
//  BendAgent.java
//  xal
//
//  Created by Thomas Pelaia on 11/2/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.ca.Channel;
import xal.tools.data.*;
import xal.smf.attr.*;

import java.util.*;
import java.util.logging.*;


/** Wrap the Bend node. */
public class BendAgent extends ElectromagnetAgent {
	/** the field adaptor */
	final static public ElectromagnetFieldAdaptor FIELD_ADAPTOR;
	
	
	// static initializer
	static {
		FIELD_ADAPTOR = ElectromagnetFieldAdaptor.getBendFieldAdaptor();
	}
	
	
	/** Constructor */
	public BendAgent( final AcceleratorSeq sequence, final Bend node, final ParameterStore parameterStore ) {
		super( sequence, node, parameterStore );
	}
	
	
	/**
	 * Get the electromagnet field adaptor.
	 * @return the electromagnet field adaptor
	 */
	protected ElectromagnetFieldAdaptor getFieldAdaptor() {
		return FIELD_ADAPTOR;
	}
	
	
	/**
	 * Write the header for live parameters.
	 * @param writer the writer to which the header should be written
	 */
	static public void exportParameterHeader( final java.io.Writer writer ) throws java.io.IOException {
		writer.write( "\n########## \n" );
		writer.write( "# Bends \n" );
		writer.write( "# Bend  \tField(T) \n" );
	}
}