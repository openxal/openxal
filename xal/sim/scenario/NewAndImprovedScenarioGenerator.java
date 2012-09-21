/*
 * Created on Mar 10, 2004
 */
package xal.sim.scenario;

import java.util.Date;
import java.util.HashMap;

import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.IElectromagnet;
import xal.model.elem.RfCavityStruct;
import xal.model.source.RfCavityDataSource;
import xal.model.xml.LatticeXmlParser;

import xal.sim.slg.BCMonitor;
import xal.sim.slg.BLMonitor;
import xal.sim.slg.BPMonitor;
import xal.sim.slg.BSMonitor;
import xal.sim.slg.Dipole;
import xal.sim.slg.Drift;
import xal.sim.slg.EKicker;
import xal.sim.slg.Element;
import xal.sim.slg.HSteerer;
import xal.sim.slg.LatticeError;
import xal.sim.slg.LatticeFactory;
import xal.sim.slg.LatticeIterator;
import xal.sim.slg.Marker;
import xal.sim.slg.Octupole;
import xal.sim.slg.PermMarker;
import xal.sim.slg.Quadrupole;
import xal.sim.slg.RFGap;
import xal.sim.slg.Sextupole;
import xal.sim.slg.SkewQuad;
import xal.sim.slg.SkewSext;
import xal.sim.slg.Solenoid;
import xal.sim.slg.VSteerer;
import xal.sim.slg.Visitor;
import xal.sim.slg.WScanner;
import xal.sim.sync.SynchronizationManager;


import xal.smf.AcceleratorSeq;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.Magnet;
import xal.smf.impl.RfGap;
import xal.smf.impl.Bend;

/**
 * @author Craig McChesney
 */
public class NewAndImprovedScenarioGenerator
	extends Object
	implements Visitor {

		// Instance Variables ======================================================

		private xal.model.Lattice lattice;
		private int driftCount = 0;
		private Sector rootSeq;
		private Sector currentSeq;
		private SynchronizationManager syncManager;
		private AcceleratorSeq sequence;

		@SuppressWarnings( "rawtypes" )		// TODO: this map is never used (only referenced in code that has been commented out)
		private HashMap mapRfCavity = new HashMap();
	
		protected int dipoleInd = 0;

		// Constructors ============================================================

		/**
		 * Default constructor, creates an empty Lattice.
		 */
		protected NewAndImprovedScenarioGenerator(AcceleratorSeq aSequence) {
			sequence = aSequence;
			initialize();
		}
	
		// Public interface ========================================================

		/**
		 * Generates a Scenario from AcceleratorSeq supplied in the constructor.
		 * 
		 * @return a Secenario for the supplied accelerator sequence
		 * @throws ModelException if there is an error building the Scenario
		 */
		public Scenario generateScenario() throws ModelException {
		
			// make intermediate lattice
			xal.sim.slg.Lattice slgLattice = makeIntermediateLattice();

			// set attributes
			lattice.setId(slgLattice.getName());
			lattice.setVersion( " " );
			lattice.setAuthor("W.-D. Klotz");
			StringBuffer commentBuf = new StringBuffer();
			commentBuf.append(lattice.getAuthor());
			commentBuf.append(LatticeXmlParser.s_strAttrSep);
			commentBuf.append(new Date());
			commentBuf.append(LatticeXmlParser.s_strAttrSep);
			commentBuf.append(
				"document generated from " + xal.sim.slg.Lattice.version());
			lattice.setComments(commentBuf.toString());
			rootSeq = new Sector(slgLattice.getName());
			currentSeq = rootSeq;
			lattice.addChild(rootSeq);
			// the intermediate lattice doesn't specify a date attribute

			// visit each lattice node
			LatticeIterator lit = slgLattice.latticeIterator();
			while (lit.hasNext()) {
				lit.next().accept(this);
			}
		
			return new Scenario(sequence, lattice, syncManager);
		}

		// Private Supporting Methods ==============================================

		private void initialize() {
			lattice = new xal.model.Lattice();
			syncManager = new SynchronizationManager();
		}
	
		private xal.sim.slg.Lattice makeIntermediateLattice() 
				throws ModelException {
			xal.sim.slg.Lattice intermediateLattice = null;
			LatticeFactory factory = new LatticeFactory();
			factory.setDebug(false);
			factory.setVerbose(false);
			factory.setHalfMag(true);
			try {
				intermediateLattice = factory.getLattice(sequence);
				intermediateLattice.clearMarkers();
				intermediateLattice.joinDrifts();
			} catch (LatticeError e) {
				throw new ModelException("LatticeError making intermediate lattice");
			}
			return intermediateLattice;
		}

		// Visitor Interface =======================================================

		private void processNewElementMapping (Element elemSlg, xal.model.elem.Element elemXal) {
			Sector seqParent = sequenceFor(elemSlg.getAcceleratorNode());
			seqParent.addChild(elemXal);
			syncManager.synchronize(elemXal, elemSlg.getAcceleratorNode());			
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.BCMonitor)
		 */
		public void visit(BCMonitor e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.BPMonitor)
		 */
		public void visit(BPMonitor e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.BLMonitor)
		 */
		public void visit(BLMonitor e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}
		
		
		/*
		 * @see xal.slg.Visitor#visit(xal.slg.BSMonitor)
		 */
		public void visit( final BSMonitor element ) {
			final xal.model.elem.Marker xalMarker = new xal.model.elem.Marker( element.getName() );
			processNewElementMapping( element, xalMarker );
		}
		
		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Dipole)
		 * Note: This is for bend dipole.
		 */
		public void visit(Dipole e) {
		
			Bend magnet = (Bend) e.getAcceleratorNode();
			xal.model.elem.ThickDipole xalDipole = 
					new xal.model.elem.ThickDipole();
			xalDipole.setId(e.getName());
			xalDipole.setLength(e.getLength());
					xalDipole.setMagField(magnet.getDesignField());
			xalDipole.setKQuad(magnet.getQuadComponent());
					// get dipole entrance and exit angles and convert them from degrees to radians
			if (dipoleInd == 0) {
					   xalDipole.setEntranceAngle(magnet.getEntrRotAngle()*Math.PI/180.);
			   xalDipole.setExitAngle(0.);
			   dipoleInd = 1;
			} else {
			   xalDipole.setEntranceAngle(0.);
					   xalDipole.setExitAngle(magnet.getExitRotAngle()*Math.PI/180.);
			   dipoleInd = 0;
			}

			int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalDipole.setOrientation(orientation);
			processNewElementMapping(e, xalDipole);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Drift)
		 */
		public void visit(Drift e) {
			// append a drift counter to the drift name for an id
			String id = "DR" + (++driftCount);
			xal.model.elem.IdealDrift xalDrift =
				new xal.model.elem.IdealDrift(id, e.getLength());
			processNewElementMapping(e, xalDrift);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.HSteerer)
			 * @deprecated we use visit(Dipole) instead
		 */
		public void visit(HSteerer e) {
			xal.model.elem.IdealMagSteeringDipole xalDipole =
				new xal.model.elem.IdealMagSteeringDipole();
			xalDipole.setId(e.getName());
			Electromagnet magnet = (Electromagnet) e.getAcceleratorNode();
			xalDipole.setEffLength(magnet.getEffLength());
					xalDipole.setMagField(magnet.getDesignField());

			int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalDipole.setOrientation(orientation);
			processNewElementMapping(e, xalDipole);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Marker)
		 */
		public void visit(Marker e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Octupole)
		 */
		public void visit(Octupole e) {
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.PermMarker)
		 */
		public void visit(PermMarker e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Quadrupole)
		 */
		public void visit(Quadrupole e) {
/*			xal.model.elem.IdealMagQuad xalQuad =
				new xal.model.elem.IdealMagQuad();
			xalQuad.setId(e.getName());
			Magnet magnet = (Magnet) e.getAcceleratorNode();
			xalQuad.setLength(e.getLength());
			// need to initialize this because PermanentMagnets aren't synchronized
			xalQuad.setMagField(magnet.getDesignField());
			//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
			int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalQuad.setOrientation(orientation);
			processNewElementMapping(e, xalQuad);
*/		
			Magnet magnet = (Magnet) e.getAcceleratorNode();
			int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}

			// for skew quads
			if (orientation == IElectromagnet.ORIENT_NONE) {
				xal.model.elem.IdealMagSkewQuad xalQuad = 
					new xal.model.elem.IdealMagSkewQuad(e.getName(), orientation, 
							magnet.getDesignField(), e.getLength(), 45.);
				
				processNewElementMapping(e, xalQuad);
			}
			else {
				xal.model.elem.IdealMagQuad xalQuad =
					new xal.model.elem.IdealMagQuad();
				xalQuad.setId(e.getName());
				xalQuad.setLength(e.getLength());
				// need to initialize this because PermanentMagnets aren't synchronized
				xalQuad.setMagField(magnet.getDesignField());
				//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
				xalQuad.setOrientation(orientation);
				
				processNewElementMapping(e, xalQuad);
			}
			
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Solenoid)
		 */
		public void visit(Solenoid e) {
			xal.model.elem.IdealMagSolenoid xalSol =
				new xal.model.elem.IdealMagSolenoid();
			xalSol.setId(e.getName());
			Magnet magnet = (Magnet) e.getAcceleratorNode();
			xalSol.setLength(e.getLength());
			// need to initialize this because PermanentMagnets aren't synchronized
			xalSol.setMagField(magnet.getDesignField());
			//		xalQuad.setEffLength(magnet.getEffLength() * e.getLength() / magnet.getLength());
/*			int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalSol.setOrientation(orientation);
*/			processNewElementMapping(e, xalSol);
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.RFGap)
		 */
		public void visit(RFGap e) {
			xal.model.elem.IdealRfGap xalRfGap =
				new xal.model.elem.IdealRfGap();
			xalRfGap.setId(e.getName());
			RfGap rfgap = (RfGap) e.getAcceleratorNode();
			try {
				xalRfGap.initializeFrom(rfgap);
			} catch (ModelException excpt) {}
			processNewElementMapping(e, xalRfGap);
		}
		
//		private RfCavityStruct cavityFor(RFGap gapSlg) {
//			
//			// get AcceleratorNode gap
//			xal.smf.impl.RfGap gapSmf = 
//				(xal.smf.impl.RfGap) gapSlg.getAcceleratorNode();
//			
//			// get AcceleratorNode cavity for gap
//			xal.smf.impl.RfCavity cavitySmf =
//				(xal.smf.impl.RfCavity) gapSmf.getParent();
//				
//			// see if the model element cavity has been created yet
//			if (mapRfCavity.get(cavitySmf) == null) {
//				//create model element cavity and add map entry
//				RfCavityStruct cavityXal = new RfCavityStruct();
//				cavityXal.setId(cavitySmf.getId());
//				System.out.println("Created new cavity for: " + gapSmf.getId());
//				try {
//					cavityXal.initializeFrom((RfCavityDataSource) cavitySmf);
//				} catch (ModelException e) {
//					//TODO what to do here???
//					e.printStackTrace();
//				}
//				mapRfCavity.put(cavitySmf, cavityXal);
//				elementContainer.addChild(cavityXal);				
//			} else {
//				System.out.println("using existing cavity: " + ((RfCavityStruct)mapRfCavity.get(cavitySmf)).getId() + " for: " + gapSmf.getId());
//			}
//			
//			return (RfCavityStruct) mapRfCavity.get(cavitySmf);
//		}

		private Sector sequenceFor(xal.smf.AcceleratorNode nodeSmf) {
			
			if (nodeSmf == null) {
				return currentSeq;
			} 
			
			// get AcceleratorNode sequence for node
			xal.smf.AcceleratorSeq seqSmf = nodeSmf.getParent();
				
			// if not an RfCavityStruct sequence parent, add to the root seq
//			if (! (seqSmf instanceof xal.smf.impl.RfCavity)) {
				currentSeq = rootSeq;
				return rootSeq;
//			}
				
                        // TODO: this is not new and improved
			// see if the model element sequence has been created yet
//			if (mapRfCavity.get(seqSmf) == null) {
//				//create model element sequence and add map entry
//				Sector seqXal = null;
////				if (seqSmf instanceof xal.smf.impl.RfCavity) {
//					seqXal = new RfCavityStruct();
//					System.out.println("Created new cavity for: " + seqSmf.getId());
//					try {
//						seqXal.initializeFrom((RfCavityDataSource) seqSmf);
//					} catch (ModelException e) {
//						//TODO what to do here???
//						e.printStackTrace();
//					}
////				} else {
////					seqXal = new Sector();
////				}
//				seqXal.setId(seqSmf.getId());
//				mapRfCavity.put(seqSmf, seqXal);
//				Sector parentSeq = sequenceFor(seqSmf);
//				parentSeq.addChild(seqXal);				
//			} else {
//				System.out.println("using existing seq: " + 
//					((Sector)mapRfCavity.get(seqSmf)).getId() + 
//					" for: " + nodeSmf.getId());
//			}
//			currentSeq = (Sector) mapRfCavity.get(seqSmf);
//			return currentSeq;
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.Sextupole)
		 */
		public void visit(Sextupole e) {
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.SkewQuad)
		 */
		public void visit(SkewQuad e) {
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.SkewSext)
		 */
		public void visit(SkewSext e) {
		}

		/*
		 * @see xal.slg.Visitor#visit(xal.slg.VSteerer)
			 * @deprecated we use visit(Dipole) instead
		 */
		public void visit(VSteerer e) {
			xal.model.elem.IdealMagSteeringDipole xalDipole =
				new xal.model.elem.IdealMagSteeringDipole();
			xalDipole.setId(e.getName());
			Electromagnet magnet = (Electromagnet) e.getAcceleratorNode();
			xalDipole.setEffLength(magnet.getEffLength());
					xalDipole.setMagField(magnet.getDesignField());

					int orientation = IElectromagnet.ORIENT_NONE;
			if (magnet.isHorizontal()) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if (magnet.isVertical()) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalDipole.setOrientation(orientation);
			processNewElementMapping(e, xalDipole);
		}
		
		
		/*
		 * @see xal.slg.Visitor#visit(xal.slg.EKicker)
		 * @deprecated we use visit(Dipole) instead
		 */
		public void visit( final EKicker element ) {
			final xal.model.elem.IdealMagSteeringDipole xalDipole = new xal.model.elem.IdealMagSteeringDipole();
			xalDipole.setId( element.getName() );
			final Electromagnet magnet = (Electromagnet)element.getAcceleratorNode();
			xalDipole.setEffLength( magnet.getEffLength() );
			xalDipole.setMagField( magnet.getDesignField() );
			
			int orientation = IElectromagnet.ORIENT_NONE;
			if ( magnet.isHorizontal() ) {
				orientation = IElectromagnet.ORIENT_HOR;
			}
			if ( magnet.isVertical() ) {
				orientation = IElectromagnet.ORIENT_VER;
			}
			xalDipole.setOrientation( orientation );
			processNewElementMapping( element, xalDipole );
		}
		
		
		/*
		 * @see xal.slg.Visitor#visit(xal.slg.WScanner)
		 */
		public void visit(WScanner e) {
			xal.model.elem.Marker xalMarker =
				new xal.model.elem.Marker(e.getName());
			processNewElementMapping(e, xalMarker);
		}

}
