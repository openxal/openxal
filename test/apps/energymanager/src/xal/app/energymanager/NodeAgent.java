//
//  NodeAgent.java
//  xal
//
//  Created by Thomas Pelaia on 2/21/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.smf.*;
import xal.smf.impl.*;
import xal.tools.data.*;

import java.util.*;


/** agent for managing a node's connections */
abstract public class NodeAgent implements KeyedRecord {
	/** key for getting a node's ID */
	final static public String ID_KEY = "NODE_ID";
	
	/** key for getting a node's type */
	final static public String TYPE_KEY = "NODE_TYPE";
	
	/** key for getting a node's position */
	final static public String POSITION_KEY = "NODE_POSITION";
	
	/** the node for which the connections will be managed */
	final protected AcceleratorNode _node;
	
	/** live parameters */
	protected List<LiveParameter> _liveParameters;
	
	/** position cache */
	private Double _position;
	
	
	/** Primary Constructor */
	public NodeAgent( final AcceleratorSeq sequence, final AcceleratorNode node, final ParameterStore parameterStore ) {
		_node = node;
		_position = new Double( sequence.getPosition( node ) );
		
		populateLiveParameters( parameterStore );
	}
	
	
	/** dispose of this agent's resources */
	public void dispose() {
		for ( LiveParameter parameter : _liveParameters ) {
			parameter.dispose();
		}
	}
	
	
	/** populate live parameters */
	abstract protected void populateLiveParameters( ParameterStore parameterStore );
	
	
	/**
	 * Get the value associated with the specified key.
	 * @param key The key for which to get the associated value.
	 * @return The value as an Object.
	 */
    public Object valueForKey( final String key ) {
		if ( key.equals( TYPE_KEY ) ) {
			return _node.getType();
		}
		else if ( key.equals( ID_KEY ) ) {
			return _node.getId();
		}
		else if ( key.equals( POSITION_KEY ) ) {
			return _position;
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Get this agent's accelerator node.
	 * @return this agent's accelerator node
	 */
	public AcceleratorNode getNode() {
		return _node;
	}
	
	
	/**
	 * Get the ID of the wrapped node.
	 * @return the ID of the wrapped node.
	 */
	public String getID() {
		return _node.getId();
	}
	
	
	/**
	 * Get the live parameter corresponding to the specified index.
	 * @param index the index of the live parameter to get.
	 * @return the live parameter corresponding to the specified index
	 */
	public LiveParameter getLiveParameter( final int index ) {
		return _liveParameters.get( index );
	}
	
	
	/**
	 * Get this node's parameters.
	 * @return this node's parameters.
	 */
	public List<LiveParameter> getLiveParameters() {
		return _liveParameters;
	}
	
	
	/**
	 * Get this node agent's position reflecting the position of its node with respect to the selected sequence.
	 * @return this node agent's position
	 */
	public double getPosition() {
		return _position.doubleValue();
	}
	
	
	/**
	 * Export optics changes using the exporter.
	 * @param exporter the optics exporter to use for exporting this node's optics changes
	 */
	abstract public void exportOpticsChanges( final OpticsExporter exporter );
	
	
	/**
	 * Write the parameter values to the specified writer using the value map to override the initial value settings.
	 * @param writer the writer to which to write out the values
	 * @param valueMap the map of core parameter value overrides keyed by core parameter name
	 */
	public void exportParameters( final java.io.Writer writer, final Map<String, Double> valueMap ) throws java.io.IOException {
		writer.write( getID() );
		
		for ( LiveParameter parameter : _liveParameters ) {
			final String key = parameter.getCoreParameter().getName();
			double value;
			if ( valueMap.containsKey( key ) ) {
				final double caValue = valueMap.get( key ).doubleValue();
				value = parameter.toPhysical( caValue );
			}
			else {
				value = parameter.getInitialValue();
			}
			writer.write( parameter.getCoreParameter().getTypeAdaptor().toExportValueString( this, value ) );			
		}
		
		writer.write( "\n" );
	}
	
	
	/**
	 * Get a description of this object.
	 * @return a description of this object.
	 */
	public String toString() {
		return getID();
	}
}
