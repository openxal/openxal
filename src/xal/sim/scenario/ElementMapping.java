package xal.sim.scenario;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import xal.smf.AcceleratorNode;

public abstract class ElementMapping {
	protected List<Entry<String, Converter>> elementMapping = new ArrayList<>();
	
	public abstract Converter getDefaultConverter();
	
	public Converter getConverter(AcceleratorNode node)
	{		
		for (Entry<String, Converter> tc : elementMapping)
		{
			if (node.isKindOf(tc.getKey()))
				return tc.getValue();
		}
		return getDefaultConverter();
		//throw new RuntimeException("No converter for class "+element.getNode().getClass()+", type "+element.getNode().getType());		
	}

	protected void putMap(String key, Converter value) {
		elementMapping.add(new AbstractMap.SimpleImmutableEntry<String,Converter>(key, value));
	}
}