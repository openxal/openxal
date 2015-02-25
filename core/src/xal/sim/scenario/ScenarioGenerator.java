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
import xal.model.xml.LatticeXmlParser;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Magnet;
import xal.smf.impl.Marker;

/**
 * Generates an on-line model scenario from XAL accelerator sequence.
 * 
 * @author Ivo List
 */
class ScenarioGenerator {
	public static final double EPS = 1.e-10d;
	
	private AcceleratorSeq sequence;
	private SynchronizationManager syncManager;
	private ElementMapping elementMapping;
	private boolean halfMag = true;
	private boolean debug = false;
	private boolean verbose = false;
	private PrintStream cout = System.out;
	
	/**
	 * Default constructor, creates an empty Lattice. Uses DefaultElementMapping.
	 * 
	 * @param aSequence accelerator sequence to create scenario for
	 */
	public ScenarioGenerator( final AcceleratorSeq aSequence ) {
		this(aSequence, aSequence.getAccelerator().getElementMapping());
	}

	
	/**
	 * Creates an empty Lattice, uses arbitrary ElementMapping.
	 * 
	 * @param aSequence accelerator sequence to create scenario for
	 * @param elementMapping element mapping
	 */
	public ScenarioGenerator( final AcceleratorSeq aSequence, final ElementMapping elementMapping ) {
		this.elementMapping = elementMapping;
		sequence = aSequence;		
	}

	/**
	 * Set flag to force lattice generator to place a permanent marker in the middle of every
	 * thick element.
	 * 
	 * @param halfmag <code>true</code> yes put the middle marker (default), else <code>false</code>
	 * for no middle markers.
	 */
	public void setHalfMag(boolean halfMag)
	{
		this.halfMag = halfMag;
	}
	
	/**
	 * @return the flag to force lattice generator to place a permanent marker in the middle of every
	 * thick element.
	 */
	public boolean getHalfMag()
	{
		return halfMag;
	}
	
	/**
	 * Set debugging output to stdout.
	 * @param debug <code>true</code> for output else <code>false</code>.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	/**
	 * @return the flag for debugging output to stdout.
	 * */
	public boolean getDebug() {
		return debug;
	}	

	/**
	 * Set debugging output to verbose.
	 * @param verbose <code>true</code> for verbose output else <code>false</code>.
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * @return the flag for debugging output to verbose.
	 */
	public boolean getVerbose() {
		return verbose;
	}

	
	
	/**
	 * Generates a Scenario from AcceleratorSeq supplied in the constructor using supplied ElementMapping.
	 * 
	 * @return a Scenario for the supplied accelerator sequence
	 * @throws ModelException if there is an error building the Scenario
	 */		
	public Scenario generateScenario() throws ModelException {
		// collects all elements		
		List<LatticeElement> elements = collectElements();

		// orders the lattice elements
		Collections.sort(elements);

		// split thick by thin
		List<LatticeElement> splitElements = splitElements(elements);

		// convert splitElements to mapped elements and adds drift spaces
		syncManager = new SynchronizationManager();
		Lattice lattice;		
		lattice = convertLatticeAndAddDrift(splitElements);
		return new Scenario(sequence, lattice, syncManager);
	}
	
	/**
	 *  <p>Collects all elements in the sequence into a single list, recording element's original position.</p>
	 *  <p>Adds begin and end marker.</p>
	 *  <p>If halfMag is set, also adds a center maker for each magnet.</p>
	 *  
	 * @return collected elements with begin, end and possibly center markers
	 */
	private List<LatticeElement> collectElements() {
		int originalPosition = 0; // used to record original position
		double sequenceLength = sequence.getLength(); // workaround for sequences that don't have length set
		List<LatticeElement> elements = new ArrayList<LatticeElement>();		
		
		elements.add(new LatticeElement(new Marker("BEGIN_" + sequence.getId()), 0.0, 
				elementMapping.getDefaultConverter(), originalPosition++));

		// generate elements for every node in the sequence which is marked as having good status
		for ( AcceleratorNode node : sequence.getAllNodes( true ) ) {
			if (node instanceof AcceleratorSeq) {
				elements.add(new LatticeElement(new Marker("BEGIN_" + node.getId()), sequence.getPosition(node), 
						elementMapping.getDefaultConverter(), originalPosition++));			
				continue; 
			}						
			
			LatticeElement element = new LatticeElement(node, sequence.getPosition(node), 
					elementMapping.getConverter(node), originalPosition++);
			elements.add(element);
			
			if (debug) {
				cout.println("collectElements: " + element.toString() + ", thin=" + element.isThin());						
			}
			
			if (element.getEndPosition() > sequenceLength) sequenceLength = element.getEndPosition();
			
			if (halfMag && node instanceof Magnet && !element.isThin()) {
			    
			    // CKA: NOTE that "ELEMENT_CENTER" is bound to Element#initializeFrom() in order \
			    //   to set Element's hardware ID attribute.  If changed you must modify both!
//				LatticeElement center = new LatticeElement(new Marker("ELEMENT_CENTER:" + node.getId()), element.getCenter(),
              LatticeElement center = new LatticeElement(new Marker(node.getId()), element.getCenter(),
						elementMapping.getDefaultConverter(), 0);
              center.setModelingElementId("ELEMENT_CENTER:" + node.getId());    // CKA Sep 5, 2014
				elements.add(center);
			}
		}
		elements.add(new LatticeElement(new Marker("END_" + sequence.getId()), sequenceLength,
				elementMapping.getDefaultConverter(), originalPosition++));
		
		return elements;
	}
	
	private void addSplitElement(List<LatticeElement> splitElements, LatticeElement element)
	{
		if (element.getLength() > EPS || element.getParts() <= 1) splitElements.add(element);
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
					if (debug && verbose) {
						cout.println("splitElements: replacing " + lastThick.toString() + " with");
					}
					
					LatticeElement secondPart = lastThick.split(currentElement);
					
					if (debug && verbose) {
						cout.println("\t" + lastThick.toString());
						if (secondPart != null) cout.println("\t" + secondPart.toString());
					}					
					if (lastThick.getEndPosition() <= currentElement.getCenter()) {
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
	 * <p>Visits each element and invokes conversion on it, using element mapper on it.</p>
	 * <p>Hooks synchronization manager to each element.</p>
	 * <p>Adds drifts between the elements.</p>
	 * 
	 * @param splitElements split elements
	 * @throws ModelException 
	 * */
	private Lattice convertLatticeAndAddDrift(List<LatticeElement> splitElements) throws ModelException {
		double position = sequence.getPosition(); // always 0.0		
		int driftCount = 0;
		
		Sector sector = new Sector();
		
		for (LatticeElement element : splitElements) {
			double driftLength = (element.isThin() ? element.getCenter() : element.getStartPosition()) - position;
			if (driftLength > EPS) {
				sector.addChild(elementMapping.createDrift("DR" + (++driftCount), driftLength));
				//sector.addChild(elementMapping.createDrift("DRFT", driftLength));
			}
			IComponent modelElement = element.convert();
			
			sector.addChild(modelElement);

			// CKA January 14, 2015
			// The modeling element is added to the synchronization manager
			//   We need to consider the cases of both a component element 
			//   and a composite element
			if (modelElement instanceof IElement) 
				syncManager.synchronize((IElement) modelElement, element.getNode());
			if (modelElement instanceof IComposite)
			    syncManager.synchronize((IComposite) modelElement, element.getNode());            
			position = element.getEndPosition();
			
			if (debug)
				cout.println(element.getNode().getId() + ": ==mapped to==>\t" + modelElement.getType()
						+ ": s= " + element.getCenter());			
		}
		
		Lattice lattice = new Lattice();
		lattice.setId(sequence.getId());
		lattice.setHardwareNodeId(sequence.getEntranceID());
		lattice.setVersion(" ");
		lattice.setAuthor("W.-D. Klotz");		
		lattice.setComments(lattice.getAuthor() + LatticeXmlParser.s_strAttrSep + new Date() 
			+ LatticeXmlParser.s_strAttrSep + "document generated from " /*+ lattice version */);
		lattice.addChild(sector);
		return lattice;
	}
}
