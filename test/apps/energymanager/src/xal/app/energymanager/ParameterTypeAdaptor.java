//
//  ParameterTypeAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 6/1/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.ca.*;


/** Adaptor to handle specific parameter types. */
public interface ParameterTypeAdaptor {
	/**
	 * Get an identifier of this parameter type.
	 * @return a unique string identifying this parameter type.
	 */
	public String getID();
	
	
	/**
	 * Get the parameter name.
	 * @return the parameter name.
	 */
	public String getName();
	
	
	/**
     * Get the design value for the parameter.
	 * @param nodeAgent the node agent from which to get the design value.
	 * @return the design value
	 */
	public double getDesignValue( NodeAgent nodeAgent );
	
	
	/**
	 * Get the lower and upper design limits for the parameter.
	 * @param nodeAgent the node agent from which to get the design limits.
	 * @return the design limits array [lower, upper] in that order
	 */
	public double[] getDesignLimits( NodeAgent nodeAgent, double designValue );
	
	
	/**
	 * Get the readback channel for the specified node.
	 * @param nodeAgent the node agent for which to get the readback channel.
	 * @return the readback channel.
	 */
	public Channel getReadbackChannel( NodeAgent nodeAgent );
	
	
	/**
	 * Get the control channel for the specified node.
	 * @param nodeAgent the node agent for which to get the control channel.
	 */
	public Channel getControlChannel( NodeAgent nodeAgent );
	
	
	/**
	 * Upload a value to the control system.
	 * @param parameter the parameter whose initial value should be uploaded.
	 * @param value the value to upload
	 * @return true if the request is successful and false if not
	 */
	public boolean uploadValue( LiveParameter parameter, double value );
	
	
	/**
	 * Get the online model property accessor for the parameter.
	 * @return the parameter's online model property accessor
	 */
	public String getPropertyAccessor();
	
	
	/**
	 * Convert the specified CA value to a physical value.
	 * @param nodeAgent the node agent to use for the conversion.
	 * @param caValue the CA value to convert
	 */
	public double toPhysical( NodeAgent nodeAgent, double caValue );
	
	
	/**
	 * Convert the specified physical value to a CA value.
	 * @param nodeAgent the node agent to use for the conversion.
	 * @param value the physical value to convert to CA
	 */
	public double toCA( NodeAgent nodeAgent, double value );
	
	
	/**
	 * The value as a string for purposes of exporting.
	 * @param nodeAgent the node agent for the parameter
	 * @param value the parameter's value
	 * @return the string representation of the value appropriate for export
	 */
	public String toExportValueString( final NodeAgent nodeAgent, final double value );
}



