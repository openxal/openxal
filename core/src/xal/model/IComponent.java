/*
 * Created on May 24, 2004
 *
 */
package xal.model;

import xal.sim.scenario.LatticeElement;


/**
 * Base interface for any structural modeling object.  Interfaces derived from
 * this base interface are assumed to model some aspect of the accelerator 
 * hardware.
 * 
 * @author Christopher K. Allen
 *
 */
public interface IComponent {


    /*
     * Initialization
     */
    
    /**
     * <p>
     * Initializes the components parameters from the given hardware
     * node proxy.  
     * </p>
     * <p>
     * <h4>CKA NOTES</h4>
     * &middot; Since we are expected to do this by accessing the
     * SMF hardware node associated with the proxy, we are now coupled
     * with the SMF component of Open XAL. The objective has been to 
     * move away from this condition.
     * <br/>
     * <br/>
     * &middot; This system must be refactored to decouple the online model and
     * SMF.
     * </p> 
     * 
     * @param latticeElement the SMF node to convert
     */
    public void initializeFrom(LatticeElement latticeElement);

    
    /*
     *  Attributes
     */
    
    /**
     *  Get the string type identifier for the composite element
     *
     *  @return     type identifier for this element
     */
    public String   getType();
    
    /**
     *  Get the string identifier of the composite element
     *
     *  @return     string identifier
     */
    public String  getId();
    
    /**
     * Get the string identifier of the hardware node being modeled
     * by this element.
     * 
     * @return      SMF hardware node this element maps to
     */
    public String   getHardwareNodeId();
    
    
    /*
     * Structure
     */
    
    /**
     *  Return the total length of the composite element.
     *
     *  @return     length of the element (in <b>meters</b>)
     */
    public double   getLength();
    
    /**
     * <p>
     * Return the center position of this component within the immediate parent composite
     * element.  If there is no parent then this method should return zero.
     * </p>
     * <p>
     * This value is typically a "hardware property," especially if this element models
     * a hardware node.  That is, the value is specified in the
     * description of the hardware node and, thus, carries through to the modeling
     * element.  The situation is different than the property 
     * <code>{@link #getLatticePosition()}</code> where the position is completely 
     * dependent upon where the modeling element lies within the overall lattice structure.
     * </p>
     * 
     * @return  center position of this element within the immediate parent container (meters)
     * 
     * @since Dec 3, 2015   by Christopher K. Allen
     */
    public double getPosition(); 
    
    /**
     * <p>
     * Return the (center) position of this component within the global lattice structure to which it
     * belongs.  Note the difference between this parameter and that returned by
     * <code>IComponent{@link #getLength()}</code> which returns the position with
     * respect to the direct parent.
     * </p>
     * <p>
     * The returned value is not usually a design parameter, in particular if composites are
     * pasted together or otherwise form a larger tree structure.  It should be computed
     * according to the current structure of the global composite structure.
     * Thus, moving this element in the lattice should change this value. 
     * </p> 
     * 
     * @return  the center position of this component within the entire lattice containing this 
     *          element (not just the parent)
     *
     * @since  Dec 3, 2015,   Christopher K. Allen
     */
    public double   getLatticePosition();
    
    
    /**
     * Returns the composite structure (if any) that owns this component.
     * 
     * @return  higher level composite structure built from this component
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    public IComposite  getParent();
    
    /**
     * Sets the parent structure containing this component.  The parent is
     * assumed to be a composite structure built from component elements.
     * 
     * @param cpsParent the composite structure built from this component
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    public void setParent(IComposite cpsParent);
    

	/*
     * Dynamics
     */
    
    /** 
     * Propagates the Probe object through this component. 
     *
     *  @param  probe   probe to be propagate - its state is advanced 
     *
     *  @exception  ModelException    error advancing the probe state
     */
    void propagate(IProbe probe) throws ModelException;
    
    /**
     * <p>
     * Position dependent tracking within an IElement 
     * </p>
     * 
     * @author H. Sako, 
     */
    void propagate(IProbe probe, double d) throws ModelException;


    /** 
     * <p>
     * Propagates the Probe object through this component.
     * </p> 
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * </p>
     * 
     *  @param  probe   probe to be propagate - its state is advanced 
     *
     *  @exception  ModelException    error advancing the probe state
     */
    void backPropagate(IProbe probe) throws ModelException;
    
    /**
     * <p>
     * Position dependent tracking within an IElement 
     * </p>
     * <p> 
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     *  
     * @param  probe   probe to be propagate - its state is advanced
     * @param d        position within element
     *  
     * @exception  ModelException    error advancing the probe state
     */
    void backPropagate(IProbe probe, double d) throws ModelException;
 
}
