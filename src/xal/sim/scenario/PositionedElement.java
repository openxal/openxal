package xal.sim.scenario;

import xal.model.IComponent;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Bend;
import xal.smf.impl.Magnet;

class PositionedElement implements Comparable<PositionedElement> {
	private double position;
	private double length;
	private AcceleratorNode node;
	private int partnr = 0, parts = 1;
	private boolean thin = false;
	private Converter converter;
	private int originalPosition;
	
	public PositionedElement(AcceleratorNode node, double position, Converter converter, int originalPosition)
	{
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
		this.originalPosition = originalPosition;
	}		

	public double getStartPosition()
	{
		return position - 0.5*length;
	}
	
	public double getLength()
	{
		return length;
	}
	
	public AcceleratorNode getNode()
	{
		return node;
	}
	
	public double getCenter() {
		return position;
	}
	
	public double getEndPosition() {
		return position + 0.5*length;
	}
	
	public PositionedElement split(PositionedElement splitter) {
		double l1 = splitter.position - (position - 0.5*length);			
		double l2 = length - l1; //more stable			
		if (l1 < ElsScenarioGenerator.EPS || l2 < ElsScenarioGenerator.EPS) return null;
		double p1 = splitter.position - 0.5*l1;
		double p2 = splitter.position + 0.5*l2;			
		parts *= 2;
		partnr *= 2;			
		PositionedElement secondPart = new PositionedElement(node, p2, converter, originalPosition);			
		position = p1;
		length = l1;
		secondPart.length = l2;
		secondPart.parts = parts;
		secondPart.partnr = partnr+1;
		secondPart.thin = thin;			
		return secondPart;
	}
	
	public boolean isThin()
	{
		return length == 0.0 || converter.isThin();			
	}
	
	public IComponent convert()
	{
		return converter.convert(this);
	}

	@Override
	public int compareTo(PositionedElement e2) {
		double p1 = isThin() ? getCenter() : getStartPosition();
		double p2 = e2.isThin() ? e2.getCenter() : e2.getStartPosition();
		int d = Double.compare(p1, p2);
		if (d == 0) d = originalPosition - e2.originalPosition;
		return d;
	}

	public int getPartNr() {
		return partnr;
	}
	
	public int getParts() {
		return parts;
	}
}