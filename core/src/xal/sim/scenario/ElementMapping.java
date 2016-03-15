/*
 * ElementMapping.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.ModelException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

/**
 * This is an abstract class to provide mapping from SMF nodes to online model elements
 * 
 * @author Ivo List
 * @since  Oct 3, 2013
 * @version Dec 5, 2014     Christopher K. Allen
 */
public abstract class ElementMapping {
    
	
	
    /*
     * Local Attributes
     */
    
    // CKA: Why we are not using a Map container directly?
    //  We could an equivalence class defined by AcceleratorNode#isKindOf()  (Comparable interface)
    /** The list of hardware type identifier string to modeling element class types. */
	protected List<Entry<String, Class<? extends IComponent>>> elementMapping = new ArrayList<>();
	
	/** indicates whether or not subsection axis coordinate have origin at sequence center */
	protected boolean bolSubsectionCtrOrigin = true;  // default set for SNS
	

    /** use type outs for debugging */
	protected boolean bolDebug = false;
	
	/** create center markers for thick magnets when true */
	protected boolean bolDivMags = true;
    
        
    
	
	/*
	 * Base Class Requirements
	 */
	
	/**
	 * Default converter should produce a general model element like a Marker.
	 * It is used when no other class associations have been found.
	 *   
	 * @return     default class type used when none is defined 
	 */
	public abstract Class<? extends IComponent> getDefaultElementType();

	/**
	 * Returns the default class type used to model accelerator sequences.
	 * This value is used when none has been defined for a specific hardware
	 * sequence (e.g., a super conduction cavity, coupled cavity linac, etc.).
	 * 
	 * @return     default class type used to model hardware sequences 
	 * 
	 * @since  Dec 5, 2014  @author Christopher K. Allen
	 */
	public abstract Class<? extends IComposite> getDefaultSequenceType();
	
	/**
	 * Different model may have different implementation of the drift element.
	 *   
	 * @return drift model element
	 * @throws ModelException 
	 */
	public abstract IComponent createDefaultDrift(String name, double len) throws ModelException;

	/**
	 * Creates a drift space within an RF cavity structure.  Such drifts have extra parameters
	 * needed to compute the probe's longitudinal phase advance.  These parameters are derived
	 * from the cavity's design parameters and do not need to be synchronized.
	 * 
	 * @param name     string identifier of the drift space
	 * @param len      length of drift space (meters)
	 * @param freq     design frequency of the enclosing RF cavity
	 * @param mode     structure mode coupling the cavities within the tank
	 *  
	 * @return         a new RF cavity drift space
	 *  
	 * @throws         ModelException  an exception occurred while constructing the element
	 *
	 * @since  Dec 3, 2014     @author Christopher K. Allen
	 */
	public abstract IComponent createRfCavityDrift(String name, double len, double freq, double mode) throws ModelException;
	
	
	/*
	 * Operations
	 */
	
	/**
	 * <p>
	 * Returns modeling element class type used to model the given node.
	 * Default implementation traverses a list and returns first association
	 * for the given hardware type.
	 * </p>
	 * <p>
	 * <h4>CKA NOTES:</h4>
	 * &middot; The implementation has changed to account for accelerator 
	 * sequences.  
	 * <br/>
	 * <br/>
	 * &middot; If the node being passed in is an <code>AcceleratorSeq</code>
	 * type them and an association exists in this mapping, then that modeling 
	 * element will be returned (but empty).
	 * <br/>
	 * <br/>
	 * &middot; If the given node is of type <code>AcceleratorSeq</code> and
	 * there is no association in this table this method returns the result of
	 * calling <code>{@link #getDefaultSequenceType()}</code>.
	 * <br/>
	 * <br/>
	 * &middot; If the given node is atomic (has no children) and there is no
	 * entry for it in the association table the result of calling 
	 * <code>{@link #getDefaultElementType()}</code>.
	 * </p> 
	 * 
	 * @param  node    hardware node being looked up
	 * 
	 * @return         class type for the modeling element associated with the given hardware 
	 * 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public Class<? extends IComponent> getModelElementType(AcceleratorNode node) {
		for (Entry<String, Class<? extends IComponent>> tc : elementMapping) {
			if (node.isKindOf(tc.getKey()))
				return tc.getValue();
		}
		
		if (node instanceof AcceleratorSeq)
		    return getDefaultSequenceType();
		else
		    return getDefaultElementType();
		// throw new RuntimeException("No converter for class "+element.getNode().getClass()+", type "+element.getNode().getType());
	}
	
	/**
	 * Returns the modeling element class type used to represent the given hardware accelerator
	 * sequence.   Currently there are no special entry types in the mapping table, it is
	 * searched just as in <code>{@link #getModelElementType(AcceleratorNode)}</code> then the
	 * result is cast to a an <code>IComposite</code> type.  If this casting fails a 
	 * <code>ClassCastException</code> is thrown.  However, unlike 
	 * <code>{@link #getModelElementType(AcceleratorNode)}</code>,
	 * if no entry is found a <code>null</code> value is returned.
	 * 
	 * @param smfSeq   accelerator hardware node to be cross referenced
	 * 
	 * @return         the modeling sequence class type used to represent the given accelerator node,
	 *                 or <code>null</code> if no class type entry is found 
	 * 
	 * @throws ClassCastException  the hardware sequence mapped to an incompatible model sequence element
	 *
	 * @author Christopher K. Allen
	 * @since  Dec 9, 2014
	 */
	@SuppressWarnings("unchecked")
    public Class<? extends IComposite> getModelSequenceType(AcceleratorSeq smfSeq) throws ClassCastException {
	    Class<? extends IComponent>    clsSeq = this.getModelElementType(smfSeq);
	    
	    if (clsSeq.equals(this.getDefaultElementType()))
	        return null;
	    else 
	        return (Class<? extends IComposite>) clsSeq;
	}

	/**
	 * Adds a converter to the list that's used by default implementation.
	 * 
	 * @param key node type
	 * @param value the converter
	 */
	protected void putMap(String key, Class<? extends IComponent> value) {
		elementMapping.add(new AbstractMap.SimpleImmutableEntry<String, Class<? extends IComponent>>(key, value));
	}
	
	
    /**
     * Set flag to force lattice generator to place a permanent marker in the middle of every
     * thick element.
     * 
     * @param halfmag <code>true</code> yes put the middle marker (default), else <code>false</code>
     *                for no middle markers.
     */
    public void setDivideMagnetFlag(boolean halfMag)    {
        this.bolDivMags = halfMag;
    }
    
    /**
     * Set flag to determine whether debugging information is sent to standard output.
     * 
     * @param bolDebug  <code>true</code> for debugging output, 
     *                  else <code>false</code> to stop debugging output.
     */
    public void setDebug(boolean bolDebug) {
        this.bolDebug = bolDebug;
    }
    
    /*
     * Attribute Queries
     */
    
    /**
     * If the value here is <code>true</code> then marker modeling elements are placed
     * at the center of thick magnets when the model lattice is created.
     * 
     * @return the flag to force lattice generator to place a permanent marker in the middle of every
     *         thick element.
     */
    public boolean isMagnetDivided()    {
        return bolDivMags;
    }
    
    /**
     * Get the debugging flag.  If <code>true</code> then debugging
     * information is being sent to the standard output.
     * 
     * @return  <code>true</code> if debugging information is being sent to standard output,
     *          <code>false</code> when in normal operation.
     */
    public boolean isDebugging() {
        return bolDebug;
    }   

    
    /**
     * Returns whether or the origin of the axis of subsection is at the center of the 
     * sequence, normally it is located at the entrance of the sequence.
     * 
     * @return  <code>true</code> if the axis origin is at the center of the sequence,
     *          <code>false</code> otherwise (likely at the sequence entrance)
     *
     * @since  Jan 29, 2015   by Christopher K. Allen
     */
    public boolean isSubsectionAxisOriginCentered() {
        return bolSubsectionCtrOrigin;
    }
}