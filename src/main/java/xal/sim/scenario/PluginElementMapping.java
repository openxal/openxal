package xal.sim.scenario;

import eu.ess.jels.model.elem.jels.JElsElementMapping;


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
