//
//  ParameterVariable.java
//  xal
//
//  Created by Thomas Pelaia on 5/27/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import xal.extension.solver.*;
import xal.tools.data.*;
import xal.smf.*;

import java.util.*;


/** subclass of Variable that represents a live parameter */
public class LiveParameterVariable extends Variable implements KeyedRecord {
	/** the core parameter */
	final protected CoreParameter _parameter;
	
	/** accelerator nodes associated with the core parameter */
	final protected List<AcceleratorNode> _nodes;
	
	
	/**
	 * Constructor
	 * @param parameter  the core parameter for which this variable represents
	 */
	public LiveParameterVariable( final CoreParameter parameter ) {
		super( parameter.getName(), parameter.getInitialValue(), parameter.getLowerLimit(), parameter.getUpperLimit() );
		
		_parameter = parameter;
		_nodes = new ArrayList<>();
		
		gatherNodes();
	}
	
	
	/** gather the nodes associated with the core parameter */
	protected void gatherNodes() {
		final Iterator<LiveParameter> liveParameterIter = _parameter.getLiveParameters().iterator();
		while( liveParameterIter.hasNext() ) {
			final LiveParameter liveParameter = liveParameterIter.next();
			_nodes.add( liveParameter.getNode() );
		}
	}
	
	
	/**
	 * Get the associated core paramter.
	 * @return the associated core parameter
	 */
	public CoreParameter getParameter() {
		return _parameter;
	}
	
	
	/**
	 * Get this variable's associated accelerator nodes.
	 * @return this variable's associated nodes
	 */
	public List<AcceleratorNode> getNodes() {
		return _nodes;
	}
	
	
	/**
	 * Get the list of live parameters.
	 * @return the live parameters associated with this variable.
	 */
	public List<LiveParameter> getLiveParameters() {
		return _parameter.getLiveParameters();
	}
	
	
	/**
	 * Get the property accessor field.
	 * @return the property accessor field.
	 */
	public String getAccessorField() {
		return _parameter.getAccessorField();
	}
	
	
	/**
	 * Get the value associated with the specified key.  Implemented by forwarding to the parameter.
	 * @param key The key for which to get the associated value.
	 * @return The key's associated value as an Object.
	 */
    public Object valueForKey( final String key ) {
		return _parameter.valueForKey( key );
	}
}


