package xal.sim.scenario;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.elem.IElectromagnet;
import xal.model.elem.IdealEDipole;
import xal.model.elem.IdealEQuad;
import xal.model.elem.IdealMagQuad;
import xal.model.elem.IdealMagSextupole;
import xal.model.elem.IdealMagSkewQuad;
import xal.model.elem.IdealMagSkewQuad3;
import xal.model.elem.IdealMagSolenoid;
import xal.model.elem.IdealMagSteeringDipole;
import xal.model.elem.Marker;
import xal.model.elem.ThickDipole;
import xal.smf.impl.Bend;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;

public class NewScenarioMapping extends ElementMapping {

	private Converter defaultConverter;

	public NewScenarioMapping()
	{
		initialize();
	}
	
	@Override
	public Converter getDefaultConverter() {
		return defaultConverter;
	}
	
	public void initialize()
	{
		putMap("dh", new Converter() {
			
			@Override
			public IElement convert(PositionedElement element) {
				Bend magnet = (Bend)element.getNode();
				ThickDipole dipole = new ThickDipole();
				dipole.setId(element.getNode().getId());
				dipole.setLength(element.getLength());
				
				dipole.setMagField(magnet.getDesignField());
				dipole.setKQuad(magnet.getQuadComponent());

				// get dipole entrance and exit angles and convert them from degrees to radians
				if (element.getPartNr() == 0) {
					dipole.setEntranceAngle(magnet.getEntrRotAngle()*Math.PI/180.);
					dipole.setExitAngle(0.);				   
				} else {
				   dipole.setEntranceAngle(0.);
				   dipole.setExitAngle(magnet.getExitRotAngle()*Math.PI/180.);				   
				}

				int orientation = IElectromagnet.ORIENT_NONE;
				if (magnet.isHorizontal()) {
					orientation = IElectromagnet.ORIENT_HOR;
				}
				if (magnet.isVertical()) {
					orientation = IElectromagnet.ORIENT_VER;
				}
				dipole.setOrientation(orientation);
				return dipole;
			}
		});
		putMap(xal.smf.impl.EDipole.s_strType, new Converter() {
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealEDipole dipole = new IdealEDipole();
				dipole.setId("TODO"+element.getNode().getId());
				return dipole;
			}
		});
		putMap("QSC", new Converter() {
			
			@Override
			public IElement convert(PositionedElement element) {
				return new IdealMagSkewQuad3("TODO"+element.getNode().getId());
			}
		});
		Converter quadConverter = new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
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
					IdealMagSkewQuad quad = 
						new IdealMagSkewQuad(element.getNode().getId(), orientation, 
								magnet.getDesignField(), element.getLength(), 45.);
					return quad;
				}
				else {
					IdealMagQuad quad = new IdealMagQuad();
					quad.setId(element.getNode().getId());
					quad.setLength(element.getLength());
					// need to initialize this because PermanentMagnets aren't synchronized
					quad.setMagField(magnet.getDesignField());
					//		quad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
					quad.setOrientation(orientation);
					return quad;
				}
				
			}
		};
		putMap("q", quadConverter);
		putMap("qt", quadConverter);
		putMap(xal.smf.impl.EQuad.s_strType, new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealEQuad quad = new IdealEQuad();
				quad.setId("TODO"+element.getNode().getId());
				return quad;
			}
		});
		putMap("pq", new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealMagQuad quad = new IdealMagQuad();
				quad.setId("TODO"+element.getNode().getId());
				return quad;
			}
		});
		putMap("S", new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealMagSextupole sext = new IdealMagSextupole();
				sext.setId("TODO"+element.getNode().getId());
				return sext;
			}
		});
		putMap("SOL", new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealMagSolenoid sol = new IdealMagSolenoid();
				sol.setId(element.getNode().getId());
				Magnet magnet = (Magnet)element.getNode();
				sol.setLength(element.getLength());
				// need to initialize this because PermanentMagnets aren't synchronized
				sol.setMagField(magnet.getDesignField());
				return sol;
			}
		});
		putMap("rfgap", new Converter() { 
			
			@Override
			public IElement convert(PositionedElement element) {
				xal.model.elem.IdealRfGap rfGap = new xal.model.elem.IdealRfGap();
				rfGap.setId(element.getNode().getId());
				RfGap rfgap = (RfGap)element.getNode();
				try {
					rfGap.initializeFrom(rfgap);
				} catch (ModelException excpt) {}
				return rfGap;
			}
		});
		defaultConverter = new Converter() { 
			{
				thin = true;
			}
			@Override
			public IElement convert(PositionedElement element) {
				return new Marker(element.getNode().getId());
			}};
			
		putMap("bcm", defaultConverter);		
		Converter steeringMagnet = new Converter() { 
			{
				thin = true;
			}
			
			@Override
			public IElement convert(PositionedElement element) {
				IdealMagSteeringDipole dipole = new IdealMagSteeringDipole();
				dipole.setId(element.getNode().getId());
				Electromagnet magnet = (Electromagnet) element.getNode();
				dipole.setEffLength(magnet.getEffLength());
				dipole.setMagField(magnet.getDesignField());

				int orientation = IElectromagnet.ORIENT_NONE;
				if (magnet.isHorizontal()) {
					orientation = IElectromagnet.ORIENT_HOR;
				}
				if (magnet.isVertical()) {
					orientation = IElectromagnet.ORIENT_VER;
				}
				dipole.setOrientation(orientation);
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
