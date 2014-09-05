/*
 * LatticeElement.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import xal.model.IComponent;
import xal.model.ModelException;
import xal.model.elem.ThinElement;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Bend;
import xal.smf.impl.Magnet;

/**
 * <p>
 * <h4>CKA NOTES:</h4>
 * &middot; I have modified this objects so it carries the additional attribute
 * of the <em>modeling element</em> identifier.  This is in contrast to the hardware node
 * identifier from which it maps.
 * <br/>
 * <br/>
 * &middot; The idea is that probes states produced by simulation will carry this attribute
 * <b>if</b> it has been set.  If not, then the probe state will have the same attribute
 * ID as the hardware node.
 * <br/>
 * <br/>
 * &middot; Note that probe states now carry two identifier attributes, one for the modeling
 * element, and one for the SMF hardware node from which it came.
 * </p>
 *
 * @author Ivo List
 * @author Christopher K. Allen
 * @since    Oct 3, 2013
 * @version  Sep 5, 2014
 */
public class LatticeElement implements Comparable<LatticeElement> {
	

	private double position;
	private double length;
	private double start, end;
	private AcceleratorNode node;
	private int partnr = 0, parts = 1;	
	private Class<? extends IComponent> elementClass;
	private int originalPosition;
	
	/** CKA Modeling element identifier, which can be different that the Accelerater node's ID */
	private String     strElemId;

	
	public LatticeElement(AcceleratorNode node, double position, Class<? extends IComponent> elementClass, int originalPosition) {
	    this.strElemId = null;
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

		this.elementClass = elementClass;
				
		if (isThin())
			start = end = position;
		else {
			start = position - 0.5*this.length;
			end = start + this.length;
		}
		
		this.originalPosition = originalPosition;
	}

	private LatticeElement(AcceleratorNode node, double start, double end, Class<? extends IComponent> elementClass, int originalPosition) {
	    this.strElemId = null;
		this.node = node;	
		this.elementClass = elementClass;
				
		this.start = start;
		this.end = end;
		this.length = 1.0;
		
		this.originalPosition = originalPosition;
	}

	
	/**
	 * Sets the (optional) string identifier for the modeling element that
	 * this object will create.
	 * 
     * @param strElemId     identifier for the modeling element created
     * 
     * @author Christopher K. Allen
     * @since  Sep 5, 2014
     */
    public void setModelingElementId(String strElemId) {
        this.strElemId = strElemId;
    }

    /**
     * Returns the identifier string to be used for the modeling element created
     * by this object.
     * 
     * @return  the element ID of the created object
     * 
     * @author Christopher K. Allen
     * @since  Sep 5, 2014
     */
    public String getModelingElementId() {
        return strElemId;
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
		LatticeElement secondPart = new LatticeElement(node, splitter.position, end, elementClass, originalPosition);
		end = splitter.position;
		secondPart.parts = parts;
		secondPart.partnr = partnr + 1;		
		return secondPart;
	}

	public boolean isThin() {
		return length == 0.0 || ThinElement.class.isAssignableFrom(elementClass);
	}

	public IComponent convert() throws ModelException {		 
		try {
			IComponent component = elementClass.newInstance();		
			component.initializeFrom(this);
			
			return component;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ModelException("Exception while instantiating class "+elementClass.getName()+" for node "+node.getId(), e);
		}
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