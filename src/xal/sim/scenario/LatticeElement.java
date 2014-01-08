/*
 * LatticeElement.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import xal.model.IComponent;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Bend;
import xal.smf.impl.Magnet;

class LatticeElement implements Comparable<LatticeElement> {
	

	private double position;
	private double length;
	private double start, end;
	private AcceleratorNode node;
	private int partnr = 0, parts = 1;	
	private ElementConverter converter;
	private int originalPosition;

	
	public LatticeElement(AcceleratorNode node, double position, ElementConverter converter, int originalPosition) {
		this.node = node;
		this.position = position;

		double length = node.getLength();
		double effLength = 0.0;
		if (node instanceof Magnet) {
			if (node instanceof Bend)
				effLength = ((Bend) node).getDfltPathLength();
			else
				effLength = ((Magnet) node).getEffLength();
		} else if (node instanceof xal.smf.impl.Electrostatic)
			effLength = length;
		this.length = effLength;

		this.converter = converter;
				
		if (isThin())
			start = end = position;
		else {
			start = position - 0.5*this.length;
			end = start + this.length;
		}
		
		this.originalPosition = originalPosition;
	}

	private LatticeElement(AcceleratorNode node, double start, double end, ElementConverter converter, int originalPosition) {
		this.node = node;	
		this.converter = converter;
				
		this.start = start;
		this.end = end;
		this.length = 1.0;
		
		this.originalPosition = originalPosition;
	}

	
	public double getStartPosition() {
		return start;
	}

	public double getLength() {
		if (isThin())
			return length;
		else
			return end - start;
	}

	public AcceleratorNode getNode() {
		return node;
	}

	public double getCenter() {
		if (isThin())
			return position;
		else
			return start + 0.5 * (end - start);
	}

	public double getEndPosition() {
		return end;
	}

	public LatticeElement split(LatticeElement splitter) {
		if (splitter.position == start || splitter.position == end) return null;
		parts *= 2;
		partnr *= 2;
		LatticeElement secondPart = new LatticeElement(node, splitter.position, end, converter, originalPosition);
		end = splitter.position;
		secondPart.parts = parts;
		secondPart.partnr = partnr + 1;		
		return secondPart;
	}

	public boolean isThin() {
		return length == 0.0 || converter.isThin();
	}

	public IComponent convert() {
		return converter.doConvert(this);
	}

	@Override
	public int compareTo(LatticeElement e2) {
		double p1 = isThin() ? position : start;
		double p2 = e2.isThin() ? e2.position : e2.start;
		int d = Double.compare(p1, p2);
		if (d == 0) {
			if (isThin() && e2.isThin())
				d = originalPosition - e2.originalPosition;
			else
				d = isThin() ? -1 : 1;
		}
		return d;
	}

	public int getPartNr() {
		return partnr;
	}

	public int getParts() {
		return parts;
	}
	
	@Override
	public String toString() { 		
		return getNode().getId() + " I=["+getStartPosition()+","+getEndPosition()+"]" +
				", p=" + getCenter() + ", l= " + getLength();
	}
}