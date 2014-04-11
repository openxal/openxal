package xal.sim.scenario;

import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;


public class PluginElementMapping {
	/**
	 * This method is used to replace default element mapping with JELS element mapping.
	 * 
	 * @return JElsElementMapping
	 * */
	public static ElementMapping getInstance()
	{
		return new JElsElementMapping();
	}
}
