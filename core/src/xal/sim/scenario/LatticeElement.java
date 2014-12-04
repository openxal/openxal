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
 * This is a proxy for SMF hardware nodes which generates its modeling element 
 * counterpart.  Currently it represents one atomic hardware node, but I believe
 * it can represent many modeling elements (see <code>{@link #getParts()}</code>).
 * </p>
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
 * <br/>
 * <br/>
 * &middot; The modeling element parameter initialization is done by calling the 
 * <code>{@link IComponent#initializeFrom(LatticeElement)}</code>.  This design
 * effectively couples the <code>xal.model</code> online model subsystem to the 
 * <code>xal.smf</code> hardware representation subsystem.  These
 * systems should be independent, able to function without each other.
 * <br/>
 * <br/>
 * &middot; I cannot really tell exactly what is happening as there was no commenting.  I have
 * added some comments wherever I have gone over the code, hopefully they are in the ball park.
 * </p>
 *
 * @author Ivo List
 * @author Christopher K. Allen
 * @since    Oct 3, 2013
 * @version  Sep 5, 2014
 */
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 4, 2014
 */
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Dec 4, 2014
 */
public class LatticeElement implements Comparable<LatticeElement> {
	

	private double position;
	private double length;
	private double start, end;
	private AcceleratorNode node;
	private int partnr = 0, parts = 1;	
	private Class<? extends IComponent> elementClass;
	private int originalPosition;
	
	/** CKA Modeling element identifier, which can be different that the Accelerator node's ID */
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

	/**
	 * Appears to split this hardware proxy element into two parts, presumably at the 
	 * position of the given proxy element.  The number of parts is doubled and I can't 
	 * figure this out??  I assume "parts" is the number of modeling elements used
	 * to represent the hardware.
	 * 
	 * @param splitter     the hardware proxy element doing the splitting
	 *  
	 * @return             a new proxy element apparently at the given proxy element's location??? 
	 *
	 * @author Christopher K. Allen
	 * @since  Dec 4, 2014
	 */
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

	/**
	 * Determines whether or not the hardware node will be represented with a
	 * thin modeling element.  Looks at both the length of the hardware node
	 * and the class type of the modeling element used to represent it.
	 * 
	 * @return     <code>true</code> if a modeling element derived from 
	 *             <code>ThinElement</code> will be returned, <code>false</code> otherwise
	 *
	 * @since  Dec 4, 2014
	 */
	public boolean isThin() {
		return length == 0.0 || ThinElement.class.isAssignableFrom(elementClass);
	}

	/**
	 * <p>
	 * Creates and initializes a new modeling element represented by this
	 * object.  Java reflection is used to create a new instance from the element's
	 * class type.  There must be a zero constructor for the element.
	 * </p>  
	 * <p>
	 * <h4>CKA Notes</h4>
	 * &middot; The parameter initialization is done by calling the 
	 * <code>{@link IComponent#initializeFrom(LatticeElement)}</code>.  This design
	 * effectively couples the <code>xal.model</code> online model subsystem to the 
	 * <code>xal.smf</code> hardware representation subsystem.  These
	 * systems should be independent, able to function without each other.
	 * </p>
	 * 
	 * @return                 a new modeling element for the hardware proxied by this object
	 * 
	 * @throws ModelException  Java reflect threw an <code>InstantiationException</code>
	 *
	 * @since  Dec 4, 2014
	 */
	public IComponent convert() throws ModelException {		 
		try {
			IComponent component = elementClass.newInstance();		
			component.initializeFrom(this);
			
			return component;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ModelException("Exception while instantiating class "+elementClass.getName()+" for node "+node.getId(), e);
		}
	}

	/**
	 * <p>
	 * Returns the current number of modeling elements used to represent the hardware
	 * node.
	 * </p>  
	 * Note that this number doubles every time that <code>{@link #split(LatticeElement)}</code>
	 * is called. You would think it increases by one given the explanation I provided??
	 * </p>
	 * 
	 * @return     I think it is the number modeling elements that map back to the associate hardware node,
	 *             but I am not sure
	 *
	 * @since  Dec 4, 2014
	 */
	public int getParts() {
		return parts;
	}
	
    /**
     * I don't know.
     * 
     * @return
     *
     * @author Christopher K. Allen
     * @since  Dec 4, 2014
     */
    public int getPartNr() {
        return partnr;
    }

	
	/*
	 * Comparable Interface
	 */
	
    /**
     * Compare by looking at hardware node positions.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     *
     * @since  Dec 4, 2014
     */
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

    /*
     * Object Overrides
     */
    
	/**
	 * Writes out entrance position, exit position, center position,
	 * and length.
	 *
	 * @see java.lang.Object#toString()
	 *
	 * @since  Dec 4, 2014
	 */
	@Override
	public String toString() { 		
		return getNode().getId() + " I=["+getStartPosition()+","+getEndPosition()+"]" +
				", p=" + getCenter() + ", l= " + getLength();
	}
}