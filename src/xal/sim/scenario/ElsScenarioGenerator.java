package xal.sim.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import xal.model.IComponent;
import xal.model.IElement;
import xal.model.Lattice;
import xal.model.Sector;
import xal.model.elem.IdealDrift;
import xal.model.xml.LatticeXmlParser;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Magnet;

public class ElsScenarioGenerator {
	public static final double EPS = 1.e-5d;
	
	private Lattice lattice;
	private AcceleratorSeq sequence;
	private SynchronizationManager syncManager;
	private ElementMapping elementMapping; 		
	
	public Scenario getScenario()
	{
		return new Scenario(sequence, lattice, syncManager);
	}
		
	public ElsScenarioGenerator(AcceleratorSeq aSequence) {
		this(aSequence, new NewScenarioMapping());
	}
	
	public ElsScenarioGenerator(AcceleratorSeq aSequence, ElementMapping elementMapping) {
		this.elementMapping = elementMapping;
		sequence = aSequence;
		lattice = new Lattice(sequence.getId());
		lattice.setVersion( " " );
		lattice.setAuthor("W.-D. Klotz");		
		lattice.setComments(lattice.getAuthor()+LatticeXmlParser.s_strAttrSep+new Date()+LatticeXmlParser.s_strAttrSep+"document generated from " /*lattice version*/);		
		syncManager = new SynchronizationManager();
		buildLattice();		
	}
	
	private void buildLattice() {
		int originalPosition = 0;
		List<PositionedElement> elements = new ArrayList<>();
		elements.add(new PositionedElement(new xal.smf.impl.Marker("BEGIN_"+sequence.getId()), 0.0, elementMapping.getDefaultConverter(), originalPosition++));
		for (AcceleratorNode node : sequence.getAllNodes())
		{			
			if (node instanceof AcceleratorSeq) continue; // skip 
			PositionedElement element = new PositionedElement(node, sequence.getPosition(node), elementMapping.getConverter(node), originalPosition++);			
			elements.add(element);
			if (node instanceof Magnet && !element.isThin()) {
				PositionedElement center =
						new PositionedElement(new xal.smf.impl.Marker("ELEMENT_CENTER:" + node.getId()), element.getCenter(), elementMapping.getDefaultConverter(), originalPosition++);				
				elements.add(center);
			}
		}
		elements.add(new PositionedElement(new xal.smf.impl.Marker("END_"+sequence.getId()), sequence.getLength(), elementMapping.getDefaultConverter(), originalPosition++));
		
		// order
		Collections.sort(elements);
		
		// split thick by thin
		List<PositionedElement> splitElements = new ArrayList<>();
		
		PositionedElement lastThick = null;
		for (PositionedElement currentElement : elements) {			
			if (lastThick != null) {
				if (lastThick.getEndPosition() <= currentElement.getStartPosition()) {
					splitElements.add(lastThick);
					lastThick = null;
					if (currentElement.isThin()) 
						splitElements.add(currentElement);
					else 
						lastThick = currentElement;
				}
				else if (currentElement.isThin()) {
					PositionedElement secondPart = lastThick.split(currentElement);
					splitElements.add(lastThick);
					splitElements.add(currentElement);
					lastThick = secondPart; /* this can be null if the element wasn't split */
				}
				else
					throw new RuntimeException("Two covering thick elements: " +
							lastThick.getNode().getId()+" from "+lastThick.getStartPosition()+" to "+lastThick.getEndPosition()+
							" and " + currentElement.getNode().getId()+ " from "+currentElement.getStartPosition()+ " to "+ currentElement.getEndPosition());				
			} else {
				if (currentElement.isThin()) {
					splitElements.add(currentElement);
				} else {
					lastThick = currentElement;
				}
			}
		}
		if (lastThick != null) splitElements.add(lastThick);
				
		// convert to mapped elements
		double position = sequence.getPosition();
		//int driftCount = 0;
		Sector sector = new Sector(sequence.getId());
		for (PositionedElement element : splitElements) {
			double driftLength = (element.isThin() ? element.getCenter() : element.getStartPosition()) - position;
			if (driftLength > EPS) {
				//sector.addChild(new IdealDrift("DR" + (++driftCount), driftLength));
				sector.addChild(new IdealDrift("DRFT", driftLength));
			}			
			IComponent modelElement = element.convert();
			sector.addChild(modelElement);
			if (modelElement instanceof IElement)
				syncManager.synchronize((IElement)modelElement, element.getNode());			
			position = element.getEndPosition();
		}
		lattice.addChild(sector);
	}
}
