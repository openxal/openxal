/*
 * DefaultElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.ModelException;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealEDipole;
import xal.model.elem.IdealEQuad;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSextupole;
import xal.model.elem.IdealMagSkewQuad3;
import xal.model.elem.IdealMagSolenoid;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealMagWedgeDipole2;
import xal.model.elem.IdealRfGap;
import xal.model.elem.Marker;
import xal.smf.impl.Bend;
import xal.smf.impl.EDipole;
import xal.smf.impl.EQuad;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class DefaultElementMapping extends ElementMapping {
	protected static ElementMapping instance;
	
	protected ElementConverter defaultConverter;

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
	
	
	@Override
	public ElementConverter getDefaultConverter() {
		return defaultConverter;
	}


	@Override
	public IComponent createDrift(String name, double len) {
		return new IdealDrift(name, len);
	}
	
	protected void initialize() {
		putMap("dh", new ElementConverter() {

			@Override
			public IComponent convert(LatticeElement element) {
				IdealMagWedgeDipole2 dipole = new IdealMagWedgeDipole2();
				Bend magnet = (Bend) element.getNode();
				dipole.setPosition(element.getCenter(), element.getLength());

				// gov.sns.xal.model.elem.ThickDipole xalDipole =
				// new gov.sns.xal.model.elem.ThickDipole();
				// xalDipole.setId(element.getNode().getId());
				// xalDipole.setLength(element.getLength());
				// xalDipole.setMagField(magnet.getDesignField());
				// xalDipole.setKQuad(magnet.getQuadComponent());
				// double angle = magnet.getDfltBendAngle()*Math.PI/180. * element.getLength() / magnet.getDfltPathLength();
				// xalDipole.setReferenceBendAngle(angle);

				// Replace ThickDipole object with an IdealMagWedgeDipole2
				// First retrieve all the physical parameters for a bending dipole				
				double len_sect = element.getLength();
				double fld_mag0 = magnet.getDesignField();
				double len_path0 = magnet.getDfltPathLength();
				double ang_bend0 = magnet.getDfltBendAngle() * Math.PI / 180.0;
				double k_quad0 = magnet.getQuadComponent();

				// Now compute the dependent parameters
				double R_bend0 = len_path0 / ang_bend0;
				double fld_ind0 = -k_quad0 * R_bend0 * R_bend0;

				double ang_bend = ang_bend0 * (len_sect / len_path0);
				double len_path = R_bend0 * ang_bend;

				// Set the parameters for the new model element				
				dipole.setPhysicalLength(len_sect);
				dipole.setDesignPathLength(len_path);
				dipole.setMagField(fld_mag0);
				dipole.setFieldIndex(fld_ind0);
				dipole.setDesignBendAngle(ang_bend);
								
				if (element.getPartNr() == 0) // first piece
					dipole.setEntrPoleAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
				if (element.getParts()-1 == element.getPartNr()) // last piece					
					dipole.setExitPoleAngle(magnet.getExitRotAngle() * Math.PI / 180.);
				
				return dipole;
			}
		});
		putMap(xal.smf.impl.EDipole.s_strType, new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {				
				EDipole magnet = (EDipole) element.getNode();				
				IdealEDipole dipole = new IdealEDipole();				
				
				// need to initialize this because PermanentMagnets aren't synchronized
				dipole.setVoltage(magnet.getDesignField());
				return dipole;
			}
		});
		putMap("QSC", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				Magnet magnet = (Magnet)element.getNode();
				IdealMagSkewQuad3 skwQuad = new IdealMagSkewQuad3(element.getNode().getId(), magnet.getDesignField(), element.getLength());				
				return skwQuad;
			}
		});
		ElementConverter quadConverter = new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				Magnet magnet = (Magnet) element.getNode();
				IdealMagQuad quad = new IdealMagQuad();
				// need to initialize this because PermanentMagnets aren't synchronized
				quad.setMagField(magnet.getDesignField());
				// quad.setEffLength(magnet.getEffLength() * element.getLength() / magnet.getLength());				
				return quad;			
			}
		};
		putMap("q", quadConverter);
		putMap("qt", quadConverter);
		putMap(xal.smf.impl.EQuad.s_strType, new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {				
				EQuad magnet = (EQuad) element.getNode();
								 
				IdealEQuad quad = new IdealEQuad();
				
				// need to initialize this because PermanentMagnets aren't synchronized
				quad.setVoltage(magnet.getDesignField());
				//      quad.setEffLength(magnet.getEffLength() * element.getLength() / magnet.getLength());				
				quad.setAperture(magnet.getAper().getAperX());
				return quad;
			}
		});
		putMap("pq", quadConverter);
		putMap("S", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				IdealMagSextupole sextupole = new IdealMagSextupole();				
				return sextupole;
			}
		});
		putMap("SOL", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				IdealMagSolenoid sol = new IdealMagSolenoid();
				Magnet magnet = (Magnet) element.getNode();
				// need to initialize this because PermanentMagnets aren't synchronized
				sol.setMagField(magnet.getDesignField());
				return sol;
			}
		});
		putMap("rfgap", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				IdealRfGap rfGap = new IdealRfGap();
				RfGap rfgap = (RfGap) element.getNode();
				try {
					rfGap.initializeFrom(rfgap);
				} catch (ModelException excpt) {
				}
				return rfGap;
			}
		});
		defaultConverter = new ElementConverter() {
			{
				thin = true;
			}

			@Override
			public IElement convert(LatticeElement element) {
				return new Marker(element.getNode().getId());
			}
		};

		putMap("bcm", defaultConverter);
		ElementConverter steeringMagnet = new ElementConverter() {
			{
				thin = true;
			}

			@Override
			public IElement convert(LatticeElement element) {
				IdealMagSteeringDipole dipole = new IdealMagSteeringDipole();
				Electromagnet magnet = (Electromagnet) element.getNode();
				dipole.setEffLength(magnet.getEffLength());
				dipole.setMagField(magnet.getDesignField());
				return dipole;
			}
		};
		putMap("dch", steeringMagnet);
		putMap("dcv", steeringMagnet);
		putMap("EKick", steeringMagnet);

		putMap("bpm", defaultConverter);
		putMap("bsm", defaultConverter);
		putMap("blm", defaultConverter);
		putMap("ws", defaultConverter);
		putMap("marker", defaultConverter);
	}
}
