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
     *  Element Identification
     */
    
    /**
     *  Get the string type identifier for the composite element
     *
     *  @return     type identifier for this element
     */
    String   getType();
    
    /**
     *  Get the string identifier of the composite element
     *
     *  @return     string identifier
     */
    String  getId();
    
    /**
     * Get the string identifier of the hardware node being modeled
     * by this element.
     * 
     * @return      SMF hardware node this element maps to
     */
    public String   getHardwareNodeId();
    

	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param latticeElement the SMF node to convert
	 */
	public void initializeFrom(LatticeElement latticeElement);

    /*
     * Component Operations
     */
    
    /**
     *  Return the total length of the composite element.
     *
     *  @return     length of the element (in <b>meters</b>)
     */
    double   getLength();
    
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
