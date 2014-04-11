/*
 * DefaultElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package se.lu.esss.ics.jels.model.elem.els;

import xal.model.IComponent;
import xal.model.elem.Marker;
import xal.sim.scenario.ElementMapping;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class ElsElementMapping extends ElementMapping {
	protected static ElementMapping instance;	

	protected ElsElementMapping() {
		initialize();
	}
	
	/**
	 *  Returns the default element mapping.
	 *  
	 * @return the default element mapping
	 */
	public static ElementMapping getInstance()
	{
		if (instance == null) instance = new ElsElementMapping();
		return instance;
	}
	
	
	@Override
	public Class<? extends IComponent> getDefaultConverter() {
		return Marker.class;
	}

	@Override
	public IComponent createDrift(String name, double len) {
		return new IdealDrift(name, len);
	}
	
	protected void initialize() {
		putMap("dh", IdealMagWedgeDipole2.class);
		putMap("q", IdealMagQuad.class);
		putMap("qt", IdealMagQuad.class);
		putMap("pq", IdealMagQuad.class);
		putMap("rfgap", IdealRfGap.class);
		putMap("marker", Marker.class);
	}

}
