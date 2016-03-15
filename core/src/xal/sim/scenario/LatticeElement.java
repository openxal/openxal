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
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Bend;
import xal.smf.impl.Magnet;

/**
 * <h3>CKA NOTES:</h3>
 * <p>
 * This class is essentially an association class between accelerator hardware
 * nodes and modeling elements of the XAL online model.
 * </p>
 * <p> 
 * This can also be a proxy for SMF hardware nodes and can generates its modeling element 
 * counterpart.  Currently it represents one atomic hardware node, but I believe
 * it can represent many modeling elements (see <code>{@link #getParts()}</code>).
 * </p>
 * <p>
 * &middot; I have modified this objects so it carries the additional attribute
 * of the <em>modeling element</em> identifier.  This is in contrast to the hardware node
 * identifier from which it maps.
 * <br>
 * <br>
 * &middot; The idea is that probes states produced by simulation will carry this attribute
 * <b>if</b> it has been set.  If not, then the probe state will have the same attribute
 * ID as the hardware node.
 * <br>
 * <br>
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
public class LatticeElement implements Comparable<LatticeElement> {
    /*
     * Local Attributes
     */
    
    //
    // Accelerator Node
    //
    
    /** the associated hardware node */
    private AcceleratorNode smfNode;

    /** original index position of the hardware within its accelerator sequence - this is used to sort thin elements */
    private int        indNodeOrigPos;
    

    //
    //  Modeling Element
    //
    
    /** CKA: Modeling element identifier, which can be different that the Accelerator node's ID */
    private String     strElemId;
    
    
    /** flag indicating that this is actually an "artificial" element with no hardware counterpart */
    private boolean     bolArtificalElem;
    

    /** the associated modeling element class type */
    private Class<? extends IComponent> clsModElemType;
    

    /** length of the modeling element */
    private double dblElemLen;
    
    /** center position of the modeling element within its parent sequence */
	protected double dblElemCntrPos;
	
	/** axial position of the modeling element entrance */
	protected double dblElemEntrPos; 
	
    /** axial position of the modeling element exit */
	protected double dblElemExitPos;
	

	
	//
	// State Variables
	//

	private IComponent component;
	private LatticeElement firstSlice;
	private LatticeElement nextSlice;

	
	/*
	 * Initialization
	 */
	
	/**
	 * Initializing constructor for <code>LatticeElement</code>. The hardware node
	 * entrance and exit positions are initialized using the given center position
	 * and length attribute.
	 *
     * @param smfNode             associated hardware node
     * @param dblPosCtr         center position of hardware node within accelerator sequence
     * @param clsModElemType     class type of the modeling element for associated hardware
     * @param originalPosition index position of the hardware node within its sequence (used to sort elements)
	 *
	 * @since  Dec 8, 2014
	 */
	public LatticeElement(AcceleratorNode smfNode, double dblPosCtr, Class<? extends IComponent> clsModElemType, int originalPosition) {
	    this.strElemId = smfNode.getId();
		this.smfNode = smfNode;
        this.clsModElemType = clsModElemType;
        
        this.indNodeOrigPos = originalPosition;
		this.dblElemCntrPos = dblPosCtr;

		// Determine the element length - special cases
		double dblLenElem  = smfNode.getLength();
		double dblLenEffec = 0.0;

		if (smfNode instanceof Magnet) {

		    if (smfNode instanceof Bend)
				dblLenEffec = ((Bend) smfNode).getDfltPathLength();
			else
				dblLenEffec = ((Magnet) smfNode).getEffLength();
		    
		} else if (smfNode instanceof xal.smf.impl.Electrostatic) {
			dblLenEffec = dblLenElem;
			
		} else if (smfNode instanceof AcceleratorSeq) {
		    dblLenEffec = dblLenElem;
		    
		}
		this.dblElemLen = dblLenEffec;

		// Set the entrance and exit positions according to the determined length
		if (isThin())
			dblElemEntrPos = dblElemExitPos = dblPosCtr;
		else {
			dblElemEntrPos = dblPosCtr - 0.5*this.dblElemLen;
			dblElemExitPos = dblElemEntrPos + this.dblElemLen;
		}
		
		firstSlice = this;
	}

	/**
	 * Initializing constructor for <code>LatticeElement</code>.  The entrance and exit
	 * positions are given directly for this constructor, which is called only within
	 * this class.  This constructor is used when splitting lattice elements.
	 *
	 * @param smfNode             associated hardware node
	 * @param dblPosStart            entrance location of this lattice element within sequence
	 * @param dblPosEnd              exit location of this lattice element within sequence
	 * @param clsModElemType     class type of the modeling element for associated hardware
	 * @param originalPosition original index position of hardware node within its sequence
	 *
	 * @since  Dec 8, 2014
	 */
	private LatticeElement(AcceleratorNode smfNode, double dblPosStart, double dblPosEnd, Class<? extends IComponent> clsModElemType, int originalPosition) {
	    this.strElemId = smfNode.getId();
		this.smfNode = smfNode;	
		this.clsModElemType = clsModElemType;
		
		this.bolArtificalElem = false;
				
		this.dblElemEntrPos = dblPosStart;
		this.dblElemExitPos = dblPosEnd;
		this.dblElemCntrPos = (dblPosStart + dblPosEnd)/2.0;
		this.dblElemLen = dblPosEnd - dblPosStart;
		
		this.indNodeOrigPos = originalPosition;
	}
	
	
	/**
	 * Sets the (optional) string identifier for the modeling element that
	 * this object will create.  Typically used when splitting up modeling
	 * elements associated with the proxied hardware node.
	 * 
     * @param strElemId     identifier for the modeling element to be created
     * 
     * @since  Sep 5, 2014
     */
    public void setModelingElementId(String strElemId) {
        this.strElemId = strElemId;
    }
    
    
    /*
     * Attribute Queries
     */

    /**
     * Returns the hardware node associated with this lattice element
     * proxy.
     * 
     * @return accelerator hardware node proxied by this lattice element 
     *
     * @since  Dec 9, 2014  
     */
    public AcceleratorNode getHardwareNode() {
        return smfNode;
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
    
    /**
     * Returns the class type of the modeling element used to represent the
     * associated hardware node.
     * 
     * @return  class type of the modeling element to be created
     *
     * @author Christopher K. Allen
     * @since  Dec 9, 2014
     */
    public Class<? extends IComponent>  getModelingClass() {
        return this.clsModElemType;
    }

    /**
     * <p>
     * Returns the length associated with the <em>hardware node</em>.  This length is
     * depends upon the length of the hardware node and how many times it has
     * been split to create the appropriate modeling element.
     * </p>
     * <p>
     * Note that the <em>effective</em> length of the associated hardware node
     * is used.
     * If the hardware is a bending magnet then this is the path length.  If
     * the hardware node <em>is not</em> a magnet then the physical length is
     * returned.  These values are determined in the <code>LatticeElement</code>
     * constructor.
     * </p>
     * 
     * @return  length of this lattice element based upon hardware length and splitting 
     *
     * @since  Dec 9, 2014
     */
    public double getLength() {
        if (isThin())
            return dblElemLen;
        else
            return dblElemExitPos - dblElemEntrPos;
    }

    /**
     * Returns the entrance location within its parent element sequence
     * of the modeling element to be created. 
     * This value is derived from the associated hardware position and the splitting
     * of the representative lattice elements.
     *  
     * @return  entrance position of the modeling element w.r.t. the parent sequence
     *
     * @since  Dec 9, 2014
     */
    public double getStartPosition() {
        return dblElemEntrPos;
    }

    /**
     * Returns the center location of the modeling element to be created. 
     * This value is derived from the associated hardware position and the splitting
     * of the representative lattice elements.
     * 
     * @return axial center position within the parent sequence 
     *
     * @since  Dec 9, 2014 
     */
    public double getCenterPosition() {
        if (isThin())
            return dblElemCntrPos;
        else
            return dblElemEntrPos + 0.5 * (dblElemExitPos - dblElemEntrPos);
    }

    /**
     * Returns the exit location of the modeling element to be created within its
     * parent accelerator sequence.
     * 
     * @return  exit position of the modeling element w.r.t. the parent sequence
     *
     * @since  Dec 9, 2014
     */
	public double getEndPosition() {
		return dblElemExitPos;
	}

    /**
     * Determines whether or not the hardware accelerator node will be represented with a
     * thin modeling element.  Looks at both the <em>effective length</em>
     * of the hardware node  and the class type of the modeling element used to 
     * represent it.
     * 
     * @return     <code>true</code> if a modeling element derived from 
     *             <code>ThinElement</code> will be returned, <code>false</code> otherwise
     *
     * @since  Dec 4, 2014
     */
    public boolean isThin() {
        return dblElemLen == 0.0 || ThinElement.class.isAssignableFrom(clsModElemType);
    }
    
    /**
     * Indicates whether or not this lattice element is artificial or not. 
     * An element is <i>artificial</i> if there is no hardware representation for
     * it in the XDXF file.  It was probably created as a placeholder within the
     * lattice generation process.  
     * 
     * @return  <code>true</code> if this element has no corresponding SMF hardware node, 
     *          <code>false</code> if this element is artifical
     *
     * @since  Jan 30, 2015   by Christopher K. Allen
     */
    public boolean isArtificial() {
        return this.bolArtificalElem;
    }
    
    /**
     * Determines whether or not the given lattice element contains this element
     * with respect to the axial positions.  Specifically, if the entrance location 
     * of this element is greater than or equal to the entrance location of the given
     * element, and the exit location of this element is less than or equal to the
     * exit location of the given element, this method returns <code>true</code>.
     * 
     * @param lem   lattice element to compare against
     * 
     * @return      <code>true</code> if this element is contained within the axial 
     *              position occupied by the given element, <code>false</code> otherwise
     *
     * @since  Jan 28, 2015   by Christopher K. Allen
     */
    public boolean isContainedIn(LatticeElement lem) {
        if (   this.getStartPosition() >= lem.getStartPosition()
            && this.getEndPosition()   <= lem.getEndPosition()
            )
            return true;
        else
            return false;
    }
    
    /*
     * Operations
     */
    
    /**
     * Translates the element by the given amount along the beamline
     * axis (of which sequence it belongs).
     * 
     * @param dblOffset distance by which this element is translated 
     *                  (either positive or negative)
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    public void axialTranslation(double dblOffset) {
        this.dblElemEntrPos += dblOffset;
        this.dblElemCntrPos += dblOffset;
        this.dblElemExitPos += dblOffset;
    }
    
	/**
	 * <p>
	 * Appears to split this lattice element into two parts, presumably at the 
	 * center position of the given element, returning the second part.  
	 * </p>
	 * <p>
	 * The number of parts is doubled and I can't 
	 * figure this out??  I assume "parts" is the number of modeling elements used
	 * to represent the hardware.
	 * </p>  
	 * <p>
	 * In any event, this lattice element is modified,
	 * its position now lives only up to the center position of the given element.
	 * </p>
	 * <p>
	 * I am guessing that the given lattice element should be a proxy for a thin
	 * element, but it is not enforced here.
	 * </p>
	 * 
	 * @param elemSplitPos the lattice element defining the splitting position
	 *  
	 * @return             a new lattice element which is the second part of this original lattice element 
	 *
	 * @author Christopher K. Allen
	 * @since  Dec 4, 2014
	 */
	public LatticeElement splitElementAt(final LatticeElement elemSplitPos) {
		if (elemSplitPos.dblElemCntrPos == dblElemEntrPos || elemSplitPos.dblElemCntrPos == dblElemExitPos) return null;
		LatticeElement secondPart = new LatticeElement(smfNode, elemSplitPos.dblElemCntrPos, dblElemExitPos, clsModElemType, indNodeOrigPos);
		dblElemExitPos = elemSplitPos.dblElemCntrPos;
		
		secondPart.firstSlice = firstSlice;
		secondPart.nextSlice = nextSlice;
		nextSlice = secondPart;
		
		return secondPart;
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
	 * @return    a new modeling element for the hardware proxied by this object
	 * 
	 * @throws ModelException  Java reflection threw an <code>InstantiationException</code>
	 *
	 * @since  Dec 4, 2014
	 */
	public IComponent createModelingElement() throws ModelException {
	    if (component == null) {
			try {
				component = clsModElemType.newInstance();		
				component.initializeFrom(this);
				
				return component;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ModelException("Exception while instantiating class "+clsModElemType.getName()+" for node "+smfNode.getId(), e);
			}		
		}
		return component;
	}

	
	/*
	 * Comparable Interface
	 */
	
    /**
     * Compare by looking at hardware node positions.  If the positions
     * are equal and the elements are both thin then we look at the
     * position index within the sequence.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     *
     * @since  Dec 4, 2014
     */
    @Override
    public int compareTo(LatticeElement e2) {
        double p1 = isThin() ? dblElemCntrPos : dblElemEntrPos;
        double p2 = e2.isThin() ? e2.dblElemCntrPos : e2.dblElemEntrPos;
        int d = Double.compare(p1, p2);
        if (d == 0) {
            if (isThin() && e2.isThin())
                d = indNodeOrigPos - e2.indNodeOrigPos;
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
		String strDescr =  this.getModelingElementId();
		
		if (this.getHardwareNode() != null)
		           strDescr += ": Hardware ID=" + this.getHardwareNode().getId();
		strDescr += " I=[" + getStartPosition() + 
		            "," + getEndPosition() + "]" +
		            ", p=" + getCenterPosition() + 
		            ", l= " + getLength();
		
		return strDescr;
	}
	
	
	public LatticeElement getFirstSlice() {
		return firstSlice;
	}
	
	public LatticeElement getNextSlice() {
		return nextSlice;
	}
	
	
	public boolean isFirstSlice() {
		return firstSlice == this;
	}
	
	public boolean isLastSlice() {
		return nextSlice == null; 
	}
}