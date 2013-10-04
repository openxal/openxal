/*
 * ElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package eu.ess.jels.scenario;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import xal.smf.AcceleratorNode;

/**
 * This is an abstract class to provide mapping from SMF nodes to online model elements
 * 
 * @author Ivo List
 *
 */
public abstract class ElementMapping {
	protected List<Entry<String, ElementConverter>> elementMapping = new ArrayList<>();

	/**
	 * Default converter should produce a general model element like a Marker.
	 * It is used when no other converters have been found.
	 *   
	 * @return default converter
	 */
	public abstract ElementConverter getDefaultConverter();

	/**
	 * Returns converter for the given node.
	 * Default implementation traverses a list and returns first good converter. 
	 * 
	 * @param node the SMF node
	 * @return converter for this node
	 */
	public ElementConverter getConverter(AcceleratorNode node) {
		for (Entry<String, ElementConverter> tc : elementMapping) {
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
	protected void putMap(String key, ElementConverter value) {
		elementMapping.add(new AbstractMap.SimpleImmutableEntry<String, ElementConverter>(key, value));
	}
}