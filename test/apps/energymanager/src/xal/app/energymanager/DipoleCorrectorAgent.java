//
// DipoleCorrectorAgent.java
// 
//
// Created by Tom Pelaia on 2/23/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
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
public class DipoleCorrectorAgent extends ElectromagnetAgent {
	/** the field adaptor */
	final static public ElectromagnetFieldAdaptor FIELD_ADAPTOR;
	
	
	// static initializer
	static {
		FIELD_ADAPTOR = ElectromagnetFieldAdaptor.getDipoleCorrectorFieldAdaptor();
	}
	
	
	/** Constructor */
	public DipoleCorrectorAgent( final AcceleratorSeq sequence, final Dipole node, final ParameterStore parameterStore ) {
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
	 * Override optics exporting to do nothing since the design corrector values are always zero.
	 * @param exporter the optics exporter to use for exporting this node's optics changes
	 */
	public void exportOpticsChanges( final OpticsExporter exporter ) {}
}