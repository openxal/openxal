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
import xal.smf.AcceleratorNode;

/**
 * This is an abstract class to provide mapping from SMF nodes to online model elements
 * 
 * @author Ivo List
 *
 */
public abstract class ElementMapping {
	protected List<Entry<String, Class<? extends IComponent>>> elementMapping = new ArrayList<>();

	/**
	 * Default converter should produce a general model element like a Marker.
	 * It is used when no other converters have been found.
	 *   
	 * @return default converter
	 */
	public abstract Class<? extends IComponent> getDefaultConverter();

	/**
	 * Different model may have different implementation of the drift element.
	 *   
	 * @return drift model element
	 */
	public abstract IComponent createDrift(String name, double len);	
	
	/**
	 * Returns converter for the given node.
	 * Default implementation traverses a list and returns first good converter. 
	 * 
	 * @param node the SMF node
	 * @return converter for this node
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Class<? extends IComponent> getConverter(AcceleratorNode node) {
		for (Entry<String, Class<? extends IComponent>> tc : elementMapping) {
			if (node.isKindOf(tc.getKey()))
				return tc.getValue();
		}
		return getDefaultConverter();
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