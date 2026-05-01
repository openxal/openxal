//
//  ElectromagnetFieldAdaptor.java
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


/** parameter adaptor for electromagnet field */
public class ElectromagnetFieldAdaptor implements ParameterTypeAdaptor {
	/** shared quadrupole field adaptor */
	final static ElectromagnetFieldAdaptor QUADRUPOLE_FIELD_ADAPTOR;
	
	/** shared bend field adaptor */
	final static ElectromagnetFieldAdaptor BEND_FIELD_ADAPTOR;
	
	/** shared dipole corrector field adaptor */
	final static ElectromagnetFieldAdaptor DIPOLE_CORRECTOR_FIELD_ADAPTOR;
	
	/** name */
	final static public String NAME;
	
	/** unique parameter type adaptor identifier */
	final protected String ID;
		
	
	// static initializer
	static {
		NAME = "Magnetic Field";
		QUADRUPOLE_FIELD_ADAPTOR = new ElectromagnetFieldAdaptor( "Quadrupole Field" );
		BEND_FIELD_ADAPTOR = new ElectromagnetFieldAdaptor( "Bend Field" );
        DIPOLE_CORRECTOR_FIELD_ADAPTOR = new ElectromagnetFieldAdaptor( "Dipole Corrector Field" );
	}
	
	
	/** Constructor */
	protected ElectromagnetFieldAdaptor( final String id ) {
		ID = id;
	}
	
	
	/**
	 * get a quadrupole field adaptor instance
	 * @return an instance of the quadrupole field adaptor
	 */
	static public ElectromagnetFieldAdaptor getQuadrupoleFieldAdaptor() {
		return QUADRUPOLE_FIELD_ADAPTOR;
	}
	
	
	/**
	 * get a bend adaptor field instance
	 * @return an instance of the bend field adaptor
	 */
	static public ElectromagnetFieldAdaptor getBendFieldAdaptor() {
		return BEND_FIELD_ADAPTOR;
	}
	
	
	/**
	 * get a dipole corrector field adaptor instance
	 * @return an instance of the dipole corrector field adaptor
	 */
	static public ElectromagnetFieldAdaptor getDipoleCorrectorFieldAdaptor() {
		return DIPOLE_CORRECTOR_FIELD_ADAPTOR;
	}
	
	
	/**
	 * Get an identifier of this parameter type.
	 * @return a unique string identifying this parameter type.
	 */
	public String getID() {
		return ID;
	}
	
	
	/**
	 * Get the parameter name.
	 * @return the parameter name.
	 */
	public String getName() {
		return NAME;
	}
	
	
	/**
	 * Get the design value for the parameter.
	 * @param nodeAgent the node agent from which to get the design value.
	 * @return the design value
	 */
	public double getDesignValue( final NodeAgent nodeAgent ) {
		return ((Electromagnet)nodeAgent.getNode()).getDesignField();
	}
	
	
	/**
	 * Get the lower and upper design limits for the parameter.  Assume 20% cushion from design value.
	 * @param nodeAgent the node agent from which to get the design limits.
	 * @return the design limits array [lower, upper] in that order
	 */
	public double[] getDesignLimits( final NodeAgent nodeAgent, final double designValue ) {
		return ( designValue >= 0 ) ? new double[] { 0.0, 1.2 * designValue } : new double[] { 1.2 * designValue, 0.0 };
	}
	
	
	/**
	 * Get the readback channel for the specified node.
	 * @param nodeAgent the node agent for which to get the readback channel.
	 * @return the readback channel.
	 */
	public Channel getReadbackChannel( final NodeAgent nodeAgent ) {
		return nodeAgent.getNode().getChannel( Electromagnet.FIELD_RB_HANDLE );
	}
	
	
	/**
	 * Get the control channel for the specified node.
	 * @param nodeAgent the node agent for which to get the control channel.
	 */
	public Channel getControlChannel( final NodeAgent nodeAgent ) {
		return nodeAgent.getNode().getChannel( MagnetMainSupply.FIELD_SET_HANDLE );
	}
	
	
	/**
	 * Upload a value to the control system.
	 * @param parameter the parameter whose initial value should be uploaded.
	 * @param value the value to upload
	 * @return true if the request is successful and false if not
	 */
	public boolean uploadValue( final LiveParameter parameter, final double value ) {
		final Electromagnet bend = (Electromagnet)parameter.getNode();
		final Channel controlChannel = getControlChannel( parameter.getNodeAgent() );
        
        final Channel bookChannel = parameter.getNode().findChannel( MagnetMainSupply.FIELD_BOOK_HANDLE );
        		
		if ( !controlChannel.isConnected() ) {
			Logger.getLogger( "global" ).log( Level.WARNING, controlChannel + " is not connected!" );
			return false;
		}
		else if ( bookChannel != null && !bookChannel.isConnected() ) {
			Logger.getLogger( "global" ).log( Level.WARNING, bookChannel + " is not connected!" );
			return false;
		}
		else {
			try {
				final double caValue = toCA( parameter.getNodeAgent(), parameter.getInitialValue() );
				controlChannel.putVal( caValue );
				if ( bookChannel != null )  bookChannel.putVal( caValue );
				return true;				
			}
			catch( xal.ca.PutException exception ) {
				exception.printStackTrace();
				Logger.getLogger( "global" ).log( Level.SEVERE, "Quadrupole field upload request failed for " + parameter.getNode(), exception );
				return false;
			}
			catch( xal.ca.ConnectionException exception ) {
				exception.printStackTrace();
				Logger.getLogger( "global" ).log( Level.SEVERE, "Quadrupole field upload request failed for " + parameter.getNode(), exception );
				return false;
			}
		}
	}
	
	
	/**
	 * Get the online model property accessor for the parameter.
	 * @return the parameter's online model property accessor
	 */
	public String getPropertyAccessor() {
		return ElectromagnetPropertyAccessor.PROPERTY_FIELD;
	}
	
	
	/**
	 * Convert the specified CA value to a physical value.
	 * @param nodeAgent the node agent to use for the conversion.
	 * @param caValue the CA value to convert
	 */
	public double toPhysical( final NodeAgent nodeAgent, final double caValue ) {
		return ((Electromagnet)nodeAgent.getNode()).toFieldFromCA( caValue );
	}
	
	
	/**
	 * Convert the specified physical value to a CA value.
	 * @param nodeAgent the node agent to use for the conversion.
	 * @param value the physical value to convert to CA
	 */
	public double toCA( final NodeAgent nodeAgent, final double value ) {
		return ((Electromagnet)nodeAgent.getNode()).toCAFromField( value );
	}
	
	
	/**
	 * The value as a string for purposes of exporting.
	 * @param nodeAgent the node agent for the parameter
	 * @param value the parameter's value
	 * @return the string representation of the value appropriate for export
	 */
	public String toExportValueString( final NodeAgent nodeAgent, final double value ) {
		return "  \t" + value;
	}	
}
