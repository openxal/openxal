/*
 * DefaultElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import xal.model.IComponent;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealEDipole;
import xal.model.elem.IdealEQuad;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSextupole;
import xal.model.elem.IdealMagSkewQuad3;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealMagWedgeDipole2;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.IdealRfGap;
import xal.model.elem.Marker;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class DefaultElementMapping extends ElementMapping {
	protected static ElementMapping instance;	

	protected DefaultElementMapping() {
		initialize();
	}
	
	/**
	 *  Returns the default element mapping.
	 *  
	 * @return the default element mapping
	 */
	public static ElementMapping getInstance()
	{
		if (instance == null) instance = new DefaultElementMapping();
		return instance;
	}
	
	
	/*
	 * ElementMapping Requirements
	 */
	
	@Override
	public Class<? extends IComponent> getDefaultClassType() {
		return Marker.class;
	}


	/**
	 * Creates a new, general drift space.
	 *
	 * @see xal.sim.scenario.ElementMapping#createDrift(java.lang.String, double)
	 *
	 * @since  Dec 3, 2014
	 */
	@Override
	public IComponent createDrift(String name, double len) {
		return new IdealDrift(name, len);
	}
	
	/**
	 * Creates a drift space within an RF cavity.
     *
     * @see xal.sim.scenario.ElementMapping#createCavityDrift(java.lang.String, double, double, double)
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    @Override
    public IComponent createCavityDrift(String name, double len, double freq, double mode) {
        return new IdealRfCavityDrift(name, len, freq, mode);
    }

    protected void initialize() {
		putMap("dh",IdealMagWedgeDipole2.class);
		putMap(xal.smf.impl.EDipole.s_strType, IdealEDipole.class);
		putMap("QSC", IdealMagSkewQuad3.class);		
		putMap("q", IdealMagQuad.class);
		putMap("qt", IdealMagQuad.class);
		putMap(xal.smf.impl.EQuad.s_strType, IdealEQuad.class);
		putMap("pq", IdealMagQuad.class);
		putMap("S", IdealMagSextupole.class);
		putMap("SOL", IdealMagQuad.class);
		putMap("rfgap", IdealRfGap.class);

		putMap("bcm", Marker.class);

		putMap("dch", IdealMagSteeringDipole.class);
		putMap("dcv", IdealMagSteeringDipole.class);
		putMap("EKick", IdealMagSteeringDipole.class);

		putMap("bpm",  Marker.class);
		putMap("bsm",  Marker.class);
		putMap("blm",  Marker.class);
		putMap("ws",  Marker.class);
		putMap("marker",  Marker.class);
	}
}
