package xal.sim.scenario;

import xal.model.IComponent;
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
import xal.model.elem.IdealMagWedgeDipole2;
import xal.model.elem.Marker;
import xal.smf.impl.Bend;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;

public class OldScenarioMapping extends ElementMapping {

	private Converter defaultConverter;

	public OldScenarioMapping()
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
			public IComponent convert(PositionedElement element) {
			      IdealMagWedgeDipole2 dipole = new IdealMagWedgeDipole2();
			      Bend magnet = (Bend) element.getNode();
			      dipole.setPosition(element.getCenter(), element.getLength());			      									

//			                gov.sns.xal.model.elem.ThickDipole xalDipole = 
//					        new gov.sns.xal.model.elem.ThickDipole();
//			                xalDipole.setId(e.getName());
//			                xalDipole.setLength(e.getLength());
//			                xalDipole.setMagField(magnet.getDesignField());
//			                xalDipole.setKQuad(magnet.getQuadComponent());
//			                double angle = magnet.getDfltBendAngle()*Math.PI/180. * e.getLength() / magnet.getDfltPathLength();
//			                xalDipole.setReferenceBendAngle(angle);

					// Replace ThickDipole object with an IdealMagWedgeDipole2
					//    First retrieve all the physical parameters for a bending dipole
			                String strId     = element.getNode().getId();
			                double len_sect  = element.getLength();
			                double fld_mag0  = magnet.getDesignField();
			                double len_path0 = magnet.getDfltPathLength();
			                double ang_bend0 = magnet.getDfltBendAngle()* Math.PI/180.0;
			                double k_quad0   = magnet.getQuadComponent();

			                //      Now compute the dependent parameters
			                double R_bend0   = len_path0/ang_bend0;
			                double fld_ind0  = - k_quad0*R_bend0*R_bend0;

			                double ang_bend  = ang_bend0*(len_sect/len_path0); 
			                double len_path  = R_bend0*ang_bend;
			                
			                //      Set the parameters for the new model element
			                dipole.setId( strId );
			                dipole.setPhysicalLength( len_sect );
			                dipole.setDesignPathLength( len_path );
			                dipole.setMagField( fld_mag0 );
			                dipole.setFieldIndex( fld_ind0 );
			                dipole.setDesignBendAngle( ang_bend );

					// New method for setting the pole face angles.
					//   The angles are determined directly according to whether or not the 
					//   element has been split at the midpoint, rather than
					//   by the internal state of the ScenarioGenerator (i.e.,
					//   the dipoleInd attribute).  If so, the angles
					//   are set according to the element's sectional representation
					//   (e.g., upstream, downstream, entire component).
					//   The angles are converted to radians for the online model.
					//
					//    C.K. Allen, 9/2009
					//
			                double dblAngEntr = 0.0;
			                double dblAngExit = 0.0;

			                if (element.getParts() == 1) { // whole
			                    dblAngEntr = magnet.getEntrRotAngle()*Math.PI/180.0;
			                    dblAngExit = magnet.getExitRotAngle()*Math.PI/180.0;			                
			                } else if (element.getPartNr() == 0) {	// first						              
			                    dblAngEntr = magnet.getEntrRotAngle()*Math.PI/180.0;
			                    dblAngExit = 0.0;
			                } else if (element.getPartNr() >= element.getParts()-1) { // last			                
			                    dblAngEntr = 0.0;
			                    dblAngExit = magnet.getExitRotAngle()*Math.PI/180.0;
			                } else { // internal
			                    dblAngEntr = 0.0;
			                    dblAngExit = 0.0;
			                }
			                 
			                
			               dipole.setEntrPoleAngle(dblAngEntr);
			                dipole.setExitPoleAngle(dblAngExit);
			                
//			                // get dipole entrance and exit angles and convert them from degrees to radians
//					if (dipoleInd == 0) {
//			                   xalDipole.setEntranceAngle((magnet).getEntrRotAngle()*Math.PI/180.);
//					   xalDipole.setExitAngle(0.);
//					   dipoleInd = 1;
//					} else {
//					   xalDipole.setEntranceAngle(0.);
//			                   xalDipole.setExitAngle((magnet).getExitRotAngle()*Math.PI/180.);
//					   dipoleInd = 0;
//					}

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
