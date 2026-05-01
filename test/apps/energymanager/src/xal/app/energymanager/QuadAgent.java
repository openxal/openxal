//
//  QuadAgent.java
//  xal
//
//  Created by Thomas Pelaia on 4/25/05.
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


/** Wrap a quadrupole node. */
public class QuadAgent extends ElectromagnetAgent {
	/** the field adaptor */
	final static public ElectromagnetFieldAdaptor FIELD_ADAPTOR;
	
	
	// static initializer
	static {
		FIELD_ADAPTOR = ElectromagnetFieldAdaptor.getQuadrupoleFieldAdaptor();
	}

	
	/** Constructor */
	public QuadAgent( final AcceleratorSeq sequence, final Quadrupole node, final ParameterStore parameterStore ) {
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
		writer.write( "# Quadrupoles \n" );
		writer.write( "# Quadrupole  \tField(T/m) \n" );
	}
}