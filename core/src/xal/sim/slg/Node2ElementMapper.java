/*
 * Node2ElementMapper.java
 *
 * Created on April 14, 2003, 11:08 PM
 */

package xal.sim.slg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import xal.smf.*;

/**
 * A visitor that generates the forward dictionary [(key,value)=(node,element)].
 *
 * @author  wdklotz
 */
public class Node2ElementMapper implements Visitor {
	private Map<AcceleratorNode,Element> node2ElementMap; //dictionary (key,value)=(node,element)
	private Map<String,AcceleratorNode> id2NodeMap; //dictionary (key,value)=(node ID,node)

	/** Creates a new instance of Node2ElementMapper */
	Node2ElementMapper() {
		node2ElementMap= new HashMap<AcceleratorNode,Element>();
		id2NodeMap= new HashMap<String,AcceleratorNode>();
	}
	/**
	 * Returns a set view of the mappings contained in this map.  
	 */
	Set<Map.Entry<AcceleratorNode,Element>> entrySet() {
		return node2ElementMap.entrySet();
	}

	/**
	 * Getter for the map property.
	 */
	Map<AcceleratorNode,Element> getMap() {
		return node2ElementMap;
	}

	public String NodeId2ElementId(String nodeId) throws LatticeError {
		try {
			AcceleratorNode node= id2NodeMap.get(nodeId);
			Element element= node2ElementMap.get(node);
			String elementId= element.getName();
			return elementId;
		} catch (Throwable t) {
			throw new LatticeError(nodeId+": not a lattice element!");
		}
	}

	public Element NodeId2Element(String nodeId) {
		AcceleratorNode node= id2NodeMap.get(nodeId);
		return node2ElementMap.get(node);
	}

	public String Node2ElementId(AcceleratorNode node) {
		Element element= node2ElementMap.get(node);
		String elementId= element.getName();
		return elementId;
	}

	public Element Node2Element(AcceleratorNode node) {
		return node2ElementMap.get(node);
	}

	/** visit a RFGap lattice element  */
	public void visit(RFGap e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}

	/** visit a PermMarker lattice element  */
	public void visit(PermMarker e) {
		StringTokenizer strtok= new StringTokenizer(e.getName(), ":");
		if (strtok.nextToken().equals("ELEMENT_CENTER")) {
			node2ElementMap.put(e.getAcceleratorNode(), e);
			id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
		}
	}

	/** visit a SkewSext lattice element  */
	public void visit(SkewSext e) {
	}

	/** visit a Octupole lattice element  */
	public void visit(Octupole e) {
	}

	/** visit a BCMonitor lattice element  */
	public void visit(BCMonitor e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}
	
	
	/** visit a HSteerer lattice element  */
	public void visit(HSteerer e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}

	/** visit a Dipole lattice element  */
	public void visit(Dipole e) {
	        node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}
	
	/** visit a EKicker lattice element  */
	public void visit( final EKicker element ) {
		node2ElementMap.put( element.getAcceleratorNode(), element );
		id2NodeMap.put( element.getAcceleratorNode().getId(), element.getAcceleratorNode() );
	}
	
	/** visit a VSteerer lattice element  */
	public void visit(VSteerer e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}

	/** visit a Drift lattice element  */
	public void visit(Drift e) {
	}

	/** visit a Quadrupole lattice element  */
	public void visit(Quadrupole e) {
	}

	/** visit a Quadrupole lattice element  */
	public void visit(EQuad e) {
	}

	/** visit a Solenoid lattice element  */
	public void visit(Solenoid e) {
	}

	/** visit a WScanner lattice element  */
	public void visit(WScanner e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}

	/** visit a BPMonitor lattice element  */
	public void visit(BPMonitor e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}

	/** visit a BLMonitor lattice element  */
	public void visit(BLMonitor e) {
		node2ElementMap.put(e.getAcceleratorNode(), e);
		id2NodeMap.put(e.getAcceleratorNode().getId(), e.getAcceleratorNode());
	}
	
	
	/** visit a BSMonitor lattice element  */
	public void visit( final BSMonitor element ) {
		node2ElementMap.put( element.getAcceleratorNode(), element );
		id2NodeMap.put( element.getAcceleratorNode().getId(), element.getAcceleratorNode() );
	}
	
	
	/** visit a SkewQuad lattice element  */
	public void visit(SkewQuad e) {
	}

	/** visit a Sextupole lattice element  */
	public void visit(Sextupole e) {
	}

	/** visit a Marker lattice element  */
	public void visit(Marker e) {
	}

	@Override
	public void visit(EDipole e) {
		
	}
}
