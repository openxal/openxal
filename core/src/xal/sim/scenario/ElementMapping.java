/*
 * ElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import xal.model.IComponent;
import xal.model.ModelException;
import xal.smf.AcceleratorNode;

/**
 * This is an abstract class to provide mapping from SMF nodes to online model elements
 * 
 * @author Ivo List
 *
 */
public abstract class ElementMapping {
    
    /*
     * Local Attributes
     */
    
    // CKA: Why we are not using a Map container directly?
    /** The list of hardware type identifier string to modeling element class types. */
	protected List<Entry<String, Class<? extends IComponent>>> elementMapping = new ArrayList<>();

	
	/*
	 * Base Class Requirements
	 */
	
	/**
	 * Default converter should produce a general model element like a Marker.
	 * It is used when no other converters have been found.
	 *   
	 * @return default converter
	 */
	public abstract Class<? extends IComponent> getDefaultClassType();

	/**
	 * Different model may have different implementation of the drift element.
	 *   
	 * @return drift model element
	 * @throws ModelException 
	 */
	public abstract IComponent createDrift(String name, double len) throws ModelException;

	/**
	 * Creates a drift space within an RF cavity structure.  Such drifts have extra parameters
	 * needed to compute the probe's longitudinal phase advance.  These parameters are derived
	 * from the cavity's design parameters and do not need to be synchronized.
	 * 
	 * @param name     string identifier of the drift space
	 * @param len      length of drift space (meters)
	 * @param freq     design frequency of the enclosing RF cavity
	 * @param mode     structure mode coupling the cavities within the tank
	 *  
	 * @return         a new RF cavity drift space
	 *  
	 * @throws         ModelException  an exception occurred while constructing the element
	 *
	 * @since  Dec 3, 2014     @author Christopher K. Allen
	 */
	public abstract IComponent createCavityDrift(String name, double len, double freq, double mode) throws ModelException;
	
	
	/*
	 * Operations
	 */
	
	/**
	 * Returns converter for the given node.
	 * Default implementation traverses a list and returns first good converter. 
	 * 
	 * @param node the SMF node
	 * @return converter for this node
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Class<? extends IComponent> getClassType(AcceleratorNode node) {
		for (Entry<String, Class<? extends IComponent>> tc : elementMapping) {
			if (node.isKindOf(tc.getKey()))
				return tc.getValue();
		}
		return getDefaultClassType();
		// throw new RuntimeException("No converter for class "+element.getNode().getClass()+", type "+element.getNode().getType());
	}

	/**
	 * Adds a converter to the list that's used by default implementation.
	 * 
	 * @param key node type
	 * @param value the converter
	 */
	protected void putMap(String key, Class<? extends IComponent> value) {
		elementMapping.add(new AbstractMap.SimpleImmutableEntry<String, Class<? extends IComponent>>(key, value));
	}
}