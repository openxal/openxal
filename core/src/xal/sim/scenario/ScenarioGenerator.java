/*
 * ScenarioGenerator2.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.ModelException;
import xal.model.Sector;
import xal.model.elem.ElementSeq;
import xal.model.elem.IdealRfCavity;
import xal.model.xml.LatticeXmlParser;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Magnet;
import xal.smf.impl.Marker;
import xal.smf.impl.RfCavity;

/**
 * Generates an on-line model scenario from XAL accelerator smfSequence.
 * 
 * @author Ivo List
 * @author Christopher K. Allen
 * @since Oct 3, 2013
 * @version Dec 5, 2014
 */
class ScenarioGenerator {
    
    /*
     * Global Constants
     */
    
    /** Small number - usually the minimum drift length */
	public static final double EPS = 1.e-10d;
	
	
	/*
	 * Local Attributes
	 */
	
	//
	// External Objects
	
	/** the accelerator sequence being modeled */
	private AcceleratorSeq smfSequence;
	
	/** The associative mapping being hardware nodes and modeling elements */
    private ElementMapping mapNodeToModCls;

    
    //	private SynchronizationManager syncManager;
    
    
    //
    //  Internal State
    
	private boolean bolDivMags = true;
	private boolean bolDebug = false;
	private boolean bolVerbose = false;
	private PrintStream cout = System.out;
	
	
	/**
	 * Default constructor, creates an empty Lattice. Uses DefaultElementMapping.
	 * 
	 * @param aSequence accelerator smfSequence to create scenario for
	 */
	public ScenarioGenerator( final AcceleratorSeq aSequence ) {
		this(aSequence, aSequence.getAccelerator().getElementMapping());
	}

	
	/**
	 * Creates an empty Lattice, uses arbitrary ElementMapping.
	 * 
	 * @param aSequence accelerator smfSequence to create scenario for
	 * @param mapNodeToModCls element mapping
	 */
	public ScenarioGenerator( final AcceleratorSeq aSequence, final ElementMapping elementMapping ) {
		this.mapNodeToModCls = elementMapping;
		smfSequence = aSequence;		
	}

	/**
	 * Set flag to force lattice generator to place a permanent marker in the middle of every
	 * thick element.
	 * 
	 * @param halfmag <code>true</code> yes put the middle marker (default), else <code>false</code>
	 * for no middle markers.
	 */
	public void setDivideMagnetFlag(boolean halfMag)	{
		this.bolDivMags = halfMag;
	}
	
    
    /**
     * Set debugging output to stdout.
     * 
     * @param bolDebug <code>true</code> for output else <code>false</code>.
     */
    public void setDebug(boolean bolDebug) {
        this.bolDebug = bolDebug;
    }
    
    /**
     * Set debugging output to bolVerbose.
     * 
     * @param bolVerbose <code>true</code> for bolVerbose output else <code>false</code>.
     */
    public void setVerbose(boolean bolVerbose) {
        this.bolVerbose = bolVerbose;
    }
    
    
    /*
     * Attribute Queries
     */
    
	/**
	 * @return the flag to force lattice generator to place a permanent marker in the middle of every
	 *         thick element.
	 */
	public boolean isMagnetDivided()	{
		return bolDivMags;
	}
	
	/**
	 * @return the flag for debugging output to stdout.
	 * */
	public boolean isDebugging() {
		return bolDebug;
	}	

	/**
	 * @return the flag for debugging output to bolVerbose.
	 */
	public boolean getVerboseFlag() {
		return bolVerbose;
	}

	
	
	/**
	 * Generates a Scenario from AcceleratorSeq supplied in the constructor using supplied ElementMapping.
	 * 
	 * @return a Scenario for the supplied accelerator smfSequence
	 * @throws ModelException if there is an error building the Scenario
	 */		
	public Scenario generateScenario_old() throws ModelException {
		// collects all elements		
		List<LatticeElement> elements = collectElements(this.smfSequence);

		// orders the lattice elements
		Collections.sort(elements);

		// split thick by thin
		List<LatticeElement> splitElements = splitElements(elements);

		// convert splitElements to mapped elements and adds drift spaces
		SynchronizationManager syncMgr = new SynchronizationManager();
		
		Lattice                lattice = convertLatticeAndAddDrift(splitElements, syncMgr, this.smfSequence);
		Scenario               model   = new Scenario(smfSequence, lattice, syncMgr);
		
		return model;
	}
	
    /**
     * CKA
     * <p>
     * Generates a Scenario from the given AcceleratorSeq supplied in the 
     * constructor using supplied ElementMapping.
     * </p>
     * <p>
     * This method will create a hierarchical model with the analogous structure
     * of the hardware object being provided.  That is, the model returns is not a
     * "flattened" version of the provided hardware representation. For example, 
     * if the hardware sequence contains nested sequences such as an <code>
     * AcceleratorSeqCombo</code> object would, then the returned model has nested
     * <code>ElementSeq</code> objects representing them.  This is convenient,
     * and necessary, for correct treatment of RF cavities.
     * </p>
     * 
     * @param   smfSeq      the hardware sequence to be modeled.
     * 
     * @return              new model Scenario for the supplied accelerator sequence
     * 
     * @throws ModelException if there is an error building the Scenario
     * 
     * @since Dec 5, 2014     @author Christopher K. Allen
     */     
    public Scenario generateScenario(AcceleratorSeq smfSeq) throws ModelException {

        // Create a synchronization manager for the model
        SynchronizationManager mgrSync = new SynchronizationManager();
        
        // Create a model lattice generator object for the given accelerator hardware sequence
        LatticeSequence latSeq = new LatticeSequence(smfSeq, this.mapNodeToModCls);
        
        // Create a model lattice for the given accelerator hardware sequence
        latSeq.setDebug( this.isDebugging() );
        latSeq.setDivideMagnetFlag( this.isMagnetDivided() );
        
        Lattice         mdlLat = latSeq.createModelLattice(mgrSync);
        

        // convert splitElements to mapped elements and adds drift spaces
        Scenario        mdlScenario   = new Scenario(smfSequence, mdlLat, mgrSync);
        
        return mdlScenario;
    }
    
    
    /*
     * Support Methods
     */
    
//    /**
//     * <p>
//     * CKA : I have modified the original method so that it is now recursive.  It calls
//     * itself whenever it finds a hardware node within the given accelerator sequence
//     * which is actually another accelerator sequence.  In that case another 
//     * lattice sequence is created (which is also a lattice element) and added to the
//     * current lattice sequence as a lattice element.
//     * </p>
//     * <p>
//     * Collects all elements in the smfSequence into a single list, recording element's original position.
//     * </p>
//     *  <p>
//     *  Adds begin and end marker.
//     *  </p>
//     *  <p>
//     *  If <code>bolDivMags</code> is set, also adds a center marker for each magnet.
//     *  </p>
//     *  
//     * @return collected elements with begin, end, and possibly center markers
//     */
//    private LatticeSequence generateLatticeSeq(SynchronizationManager mgrSync, AcceleratorSeq smfSeq) {
//
//        // Loop variables
//        int     indPosition = 0;                  // used to record original position 
//        double  dblLenSeq   = smfSeq.getLength(); // workaround for sequences that don't have length set
//        
//        // The returned modeling element sequence
//        //  First check for an RfCavity, this is a special case which requires a 
//        //  special element sequence
//        String                      strSeqId  = smfSeq.getId();
//        double                      dblSeqPos = smfSeq.getParent().getPosition(smfSeq);
//        Class<? extends IComposite> clsSeqTyp = this.mapNodeToModCls.getModelSequenceType(smfSeq);
//
//        LatticeSequence latSeqParent = new LatticeSequence(smfSeq, dblSeqPos, clsSeqTyp, 0);
//        
//        
//        // This is pretty klugey: (See below for the complement action) 
//        //  We are creating a nonexistent marker hardware object,
//        //  then creating a lattice element association, so that we may 
//        //  ensure the drift space from the beginning of the sequence to the first 
//        //  hardware node is modeled correctly.  There must be a better way??
//        AcceleratorNode             smfBegMrkr = new Marker( "BEGIN_" + strSeqId );
//        Class<? extends IComponent> clsMrkrTyp = this.mapNodeToModCls.getDefaultElementType();
//        LatticeElement              latBegElem = new LatticeElement(smfBegMrkr, 0, clsMrkrTyp, indPosition++);
//
//        latSeqParent.addLatticeElement(latBegElem);
//
//        
//        // Now we generate association objects for every hardware node (including sequences)
//        //  in the accelerator sequence which is marked as having good status
//        for ( AcceleratorNode smfNode : smfSeq.getNodes( true ) ) {
//
//            
////            // The RF cavity is currently a special case
////            //     Since the RF cavity is an accelerator sequence must check for it first
////            if (smfNode instanceof RfCavity) {
////                RfCavity   smfRfCav = (RfCavity)smfNode;
////                
////            }
//            
//            // Recursively call this method to drill down and get any sub sequences
//            if (smfNode instanceof AcceleratorSeq) {
//                AcceleratorSeq    smfSubSeq = (AcceleratorSeq)smfNode;
//                
//                LatticeSequence latSeqChild = this.generateLatticeSeq(mgrSync, smfSubSeq);
//                
//                latSeqParent.addLatticeElement(latSeqChild);;
//                continue;
//            }
//            
//            // Create a new lattice element for the accelerator node and add it to the lattice sequence
//            double                      dblNodePos = smfSeq.getPosition(smfNode);
//            Class<? extends IComponent> clsElemTyp = this.mapNodeToModCls.getModelElementType(smfNode);
//            LatticeElement              latElem    = new LatticeElement(smfNode, dblNodePos, clsElemTyp, indPosition++);
//
//            latSeqParent.addLatticeElement(latElem);
//            
//            if (bolDebug) {
//                cout.println("ScenarioGenerator#generateLatticeSequence(): " + latElem.toString() + ", thin=" + latElem.isThin());                      
//            }
//            
//            if (latElem.getEndPosition() > dblLenSeq) 
//                dblLenSeq = latElem.getEndPosition();
//            
//            
//            // We need to add a thin element marker to the center of any magnet if the
//            //  divide magnets flag is true.  Thus, check if the current hardware node
//            //  is a magnet, the flag is true, and the hardware node modeling element is
//            //  not a thin element.  If so, add the center marker.
//            if (bolDivMags && (smfNode instanceof Magnet) && !latElem.isThin()) {
//
//                String                      strNodeId   = smfNode.getId();
//                double                      dblPosCtr   = latElem.getCenterPosition();
//                AcceleratorNode             smfCntrMrkr = new Marker( strNodeId + "-Center" );
//                
//                LatticeElement  latCtrElem  = new LatticeElement(smfCntrMrkr, dblPosCtr, clsMrkrTyp, 0); 
//                latCtrElem.setModelingElementId("CENTER:" + smfNode.getId());    // CKA Sep 5, 2014
//                latSeqParent.addLatticeElement(latCtrElem);
//            }
//        }
//        
//        // This is pretty klugey, but c'est lie vie. We are creating a nonexistent marker
//        //  hardware object, then creating a lattice element association, so that we may 
//        //  ensure the drift space from the last element to the end of the sequence is modeled
//        //  correctly.  There must be a better way??
//        AcceleratorNode             smfEndMrkr = new Marker( "END_" + strSeqId );
//        LatticeElement              latEndElem = new LatticeElement(smfEndMrkr, dblLenSeq, clsMrkrTyp, indPosition++);
//
//        latSeqParent.addLatticeElement(latEndElem);
//        
////      elements.add(new LatticeElement(new Marker("END_" + smfSequence.getId()), sequenceLength,
////              mapNodeToModCls.getDefaultConverter(), originalPosition++));
//        
//        return latSeqParent;
//    }
    
    
	
	/*
	 * Support Methods
	 */
	
	/**
	 *  <p>Collects all elements in the smfSequence into a single list, recording element's original position.</p>
	 *  <p>Adds begin and end marker.</p>
	 *  <p>If bolDivMags is set, also adds a center maker for each magnet.</p>
	 *  
	 * @return collected elements with begin, end and possibly center markers
	 */
	private List<LatticeElement> collectElements(AcceleratorSeq sequence) {
		int originalPosition = 0; // used to record original position
		
		// This value is never used?
		double sequenceLength = sequence.getLength(); // workaround for sequences that don't have length set
		
		// The returned collection
		List<LatticeElement> lstElems = new ArrayList<LatticeElement>();		
		
//		lstElems.add(new LatticeElement(new Marker("BEGIN_" + sequence.getId()), 0.0, 
//				mapNodeToModCls.getDefaultClassType(), originalPosition++));

		// generate elements for every node in the smfSequence which is marked as having good status
//		for ( AcceleratorNode node : sequence.getAllNodes( true ) ) {
	    for ( AcceleratorNode node : sequence.getNodes( true ) ) {

//		    if (node instanceof AcceleratorSeq) {
//				elements.add(new LatticeElement(new Marker("BEGIN_" + node.getId()), smfSequence.getPosition(node), 
//						mapNodeToModCls.getDefaultConverter(), originalPosition++));			
//				continue; 
//			}
		    
	        // The RF cavity is currently a special case
	        //     Since the RF cavity is an accelerator sequence must check for it first
	        if (node instanceof RfCavity) {
	            RfCavity   smfRfCav = (RfCavity)node;
	            
	        }
	        
		    // Recursively call this method to drill down and get any sub sequences
		    if (node instanceof AcceleratorSeq) {
		        AcceleratorSeq    smfSubSeq = (AcceleratorSeq)node;
		        
		        List<LatticeElement> lstSeqElems = this.collectElements(smfSubSeq);
		        lstElems.addAll(lstSeqElems);
		        continue;
		    }
			
			LatticeElement element = new LatticeElement(node, 
			                                            sequence.getPosition(node), 
			                                            mapNodeToModCls.getModelElementType(node), 
			                                            originalPosition++
			                                            );
			lstElems.add(element);
			
			if (bolDebug) {
				cout.println("collectElements: " + element.toString() + ", thin=" + element.isThin());						
			}
			
			if (element.getEndPosition() > sequenceLength) 
			    sequenceLength = element.getEndPosition();
			
			if (bolDivMags && node instanceof Magnet && !element.isThin()) {

			    // CKA Oct, 2014
			    //				LatticeElement center = new LatticeElement(new Marker("ELEMENT_CENTER:" + node.getId()), element.getCenter(),
			    LatticeElement center = new LatticeElement(new Marker(node.getId()), 
			            element.getCenterPosition(),
			            mapNodeToModCls.getDefaultElementType(), 
			            0
			            );
			    center.setModelingElementId("ELEMENT_CENTER:" + node.getId());    // CKA Sep 5, 2014
			    lstElems.add(center);
			}
		}
//		elements.add(new LatticeElement(new Marker("END_" + smfSequence.getId()), sequenceLength,
//				mapNodeToModCls.getDefaultConverter(), originalPosition++));
		
		return lstElems;
	}
	
	/**
	 * <p>Splits all thick elements by thin ones, keeping the order.</p>
	 * <p>The method also checks if two thick elements cover each other, which should not happen.</p>
	 * <p>Uses a simple scan line algorithm.</p>
	 * 
	 * @param elements list of elements to be split
	 * @return list of split elements
	 */
	private List<LatticeElement> splitElements(List<LatticeElement> elements) throws ModelException {
		List<LatticeElement> splitElements = new ArrayList<>();

		LatticeElement lastThick = null;
		for (LatticeElement currentElement : elements) {
			// loop invariant: 
			//   scanline is at currentElement.startPosition()
			//   all the intersections before scanline have already been accounted for
			//   variable lastThick has the thick element before or under the scanline, if there is one 
			if (lastThick != null) {
				if (lastThick.getEndPosition() - currentElement.getStartPosition() <= EPS) { // we passed lastThick element
					addSplitElement(splitElements, lastThick);
					lastThick = null;
					if (currentElement.isThin())
						addSplitElement(splitElements, currentElement);						
					else
						lastThick = currentElement;
				} else if (currentElement.isThin()) { // we have a thick & thin element intersection
					if (bolDebug && bolVerbose) {
						cout.println("splitElements: replacing " + lastThick.toString() + " with");
					}
					
					LatticeElement secondPart = lastThick.splitElementAt(currentElement);
					
					if (bolDebug && bolVerbose) {
						cout.println("\t" + lastThick.toString());
						if (secondPart != null) cout.println("\t" + secondPart.toString());
					}					
					if (lastThick.getEndPosition() <= currentElement.getCenterPosition()) {
						addSplitElement(splitElements, lastThick);						
						lastThick = null;
					}
					splitElements.add(currentElement);
					if (secondPart != null)
						lastThick = secondPart;  
				} else { // we have thick & thick intersection					
					throw new ModelException("Two covering thick elements: " + lastThick.toString() + 
							" and " + currentElement.toString());							
				}
			} else { // there was no element under the scanline
				if (currentElement.isThin()) {
					splitElements.add(currentElement);
				} else {
					lastThick = currentElement;
				}
			}
		}
		
		// scanline over the last element
		if (lastThick != null)
			addSplitElement(splitElements, lastThick);			
		
		return splitElements;
	}
	
    /**
     * Called by <code>{@link #splitElements(List)}</code> to add the given proxy
     * element into the given list of split element.  The given element must meet 1
     * of 2 criteria: 1) it has finite length or 2) it represents no more than one
     * modeling element.
     *  
     * @param splitElements     list of elements being split
     * @param element           element to be added to above list
     *
     * @since  Dec 5, 2014
     */
    private void addSplitElement(List<LatticeElement> splitElements, LatticeElement element)
    {
        if (element.getLength() > EPS || element.getParts() <= 1) splitElements.add(element);
    }
    
	/**
	 * <p>Visits each element and invokes conversion on it, using element mapper on it.</p>
	 * <p>Hooks synchronization manager to each element.</p>
	 * <p>Adds drifts between the elements.</p>
	 * 
	 * @param splitElements    list of split hardware-element associations
	 * @param syncMgr          manager used to synchronize modeling elements with live hardware
	 * @param smfAccelSeq      the accelerator sequence being modeled
	 * 
	 * @throws ModelException  not sure why this is thrown 
	 * */
	private Lattice convertLatticeAndAddDrift(List<LatticeElement> splitElements, SynchronizationManager syncMgr, AcceleratorSeq smfAccelSeq) throws ModelException {
		double position = smfAccelSeq.getPosition(); // always 0.0		
		int driftCount = 0;
		
		Sector sector = new Sector();
		
		for (LatticeElement element : splitElements) {
			
		    double driftLength = (element.isThin() ? element.getCenterPosition() : element.getStartPosition()) - position;
			
			if (driftLength > EPS) {
			    String       strDriftId = "DR" + (++driftCount);
			    IComponent   modDrift   = mapNodeToModCls.createDefaultDrift(strDriftId, driftLength);
				sector.addChild(modDrift);
				//sector.addChild(mapNodeToModCls.createDrift("DRFT", driftLength));
			}
			
			IComponent modelElement = element.createModelingElement();
			sector.addChild(modelElement);
			if (modelElement instanceof IElement) 
				syncMgr.synchronize((IElement) modelElement, element.getHardwareNode());			
			position = element.getEndPosition();
			
			if (bolDebug)
				cout.println(element.getHardwareNode().getId() + ": ==mapped to==>\t" + modelElement.getType()
						+ ": s= " + element.getCenterPosition());			
		}
		
		// Create new lattice modeling object
		Lattice lattice = new Lattice();
		lattice.setId(smfAccelSeq.getId());
		lattice.setHardwareNodeId(smfAccelSeq.getEntranceID());
		lattice.setVersion("Version soft type: " + smfAccelSeq.getSoftType());
//		lattice.setAuthor("W.-D. Klotz");		
		lattice.setComments(lattice.getAuthor() + LatticeXmlParser.s_strAttrSep + new Date() 
			+ LatticeXmlParser.s_strAttrSep + "document generated from " /*+ lattice version */);
		
		lattice.addChild(sector);
		return lattice;
	}
}
