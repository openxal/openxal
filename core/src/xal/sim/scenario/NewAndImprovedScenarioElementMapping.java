/*
 * NewAndImprovedScenarioElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.elem.IElectromagnet;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealEDipole;
import xal.model.elem.IdealEQuad;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSkewQuad;
import xal.model.elem.IdealMagSolenoid;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.IdealRfGap;
import xal.model.elem.Marker;
import xal.model.elem.ThickDipole;
import xal.smf.impl.Bend;
import xal.smf.impl.EDipole;
import xal.smf.impl.EQuad;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;

/**
 * The element mapping as it's done in obsolete NewAndImprovedScenarioGenerator.
 * 
 * @author Ivo List
 *
 */
public class NewAndImprovedScenarioElementMapping extends ElementMapping {
	protected static ElementMapping instance;
	
	protected ElementConverter defaultConverter;

	protected NewAndImprovedScenarioElementMapping() {
		initialize();
	}

	public static ElementMapping getInstance()
	{
		if (instance == null) instance = new NewAndImprovedScenarioElementMapping();
		return instance;
	}
	
	@Override
	public ElementConverter getDefaultConverter() {
		return defaultConverter;
	}

	protected void initialize() {
		putMap("dh", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				Bend magnet = (Bend) element.getNode();
				ThickDipole dipole = new ThickDipole();				
				dipole.setMagField(magnet.getDesignField());
				dipole.setKQuad(magnet.getQuadComponent());

				// get dipole entrance and exit angles and convert them from degrees to radians
				if (element.getPartNr() == 0) 
					dipole.setEntranceAngle(magnet.getEntrRotAngle() * Math.PI / 180.);
				if (element.getParts()-1 == element.getPartNr())					
					dipole.setExitAngle(magnet.getExitRotAngle() * Math.PI / 180.);
								
				return dipole;
			}
		});
		putMap(xal.smf.impl.EDipole.s_strType, new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				EDipole magnet = (EDipole) element.getNode();
				IdealEDipole dipole = new IdealEDipole();
				int orientation = IElectromagnet.ORIENT_NONE;
				if (magnet.isHorizontal()) {
					orientation = IElectromagnet.ORIENT_HOR;
				}
				if (magnet.isVertical()) {
					orientation = IElectromagnet.ORIENT_VER;
				}
				dipole.setOrientation(orientation);
				dipole.setVoltage(magnet.getDesignField());
				return dipole;
			}
		});
		putMap("QSC", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				return new IdealDrift();
			}
		});
		ElementConverter quadConverter = new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				Magnet magnet = (Magnet) element.getNode();
				int orientation = IElectromagnet.ORIENT_NONE;
				if (magnet.isHorizontal()) {
					orientation = IElectromagnet.ORIENT_HOR;
				}
				if (magnet.isVertical()) {
					orientation = IElectromagnet.ORIENT_VER;
				}

				// for skew quads
				if (orientation == IElectromagnet.ORIENT_NONE) {
					IdealMagSkewQuad quad = new IdealMagSkewQuad(element.getNode().getId(), orientation, magnet.getDesignField(), element.getLength(), 45.);
					return quad;
				} else {
					IdealMagQuad quad = new IdealMagQuad();
					quad.setId(element.getNode().getId());
					quad.setLength(element.getLength());
					// need to initialize this because PermanentMagnets aren't synchronized
					quad.setMagField(magnet.getDesignField());
					// quad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
					quad.setOrientation(orientation);
					return quad;
				}

			}
		};
		putMap("q", quadConverter);
		putMap("qt", quadConverter);
		putMap(xal.smf.impl.EQuad.s_strType, new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				EQuad magnet = (EQuad) element.getNode();
				int orientation = IElectromagnet.ORIENT_NONE;
				if (magnet.isHorizontal()) {
					orientation = IElectromagnet.ORIENT_HOR;
				}
				if (magnet.isVertical()) {
					orientation = IElectromagnet.ORIENT_VER;
				}

				IdealEQuad quad = new IdealEQuad();
				// need to initialize this because PermanentMagnets aren't synchronized
				quad.setVoltage(magnet.getDesignField());
				//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
				quad.setOrientation(orientation);
				quad.setAperture(magnet.getAper().getAperX());
				return quad;
			}
		});
		putMap("pq", quadConverter);
		putMap("S", new ElementConverter() {

			@Override
			public IElement convert(LatticeElement element) {
				return new IdealDrift();
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
				return new Marker();
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
