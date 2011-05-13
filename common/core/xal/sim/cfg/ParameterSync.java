/**
 * ParameterSync.java
 *
 * @author Christopher K. Allen
 * @since  May 11, 2011
 *
 */

/**
 * ParameterSync.java
 *
 * @author  Christopher K. Allen
 * @since	May 11, 2011
 */
package xal.sim.cfg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xal.model.IElement;
import xal.sim.latgen.GenerationException;
import xal.smf.AcceleratorNode;

/**
 * <p>
 * This class pipes the parameter value through, from the hardware
 * device to the modeling element using the method 
 * <code>{@link #syncValue(AcceleratorNode, IElement)}</code>.  
 * That is, the parameter of the 
 * modeling element is updated, or synchronized, to the current value 
 * of the hardware.
 * </p>
 * 
 *
 * @author Christopher K. Allen
 * @since   May 11, 2011
 */
public class ParameterSync {

    
    /*
     * Local Attributes
     */
    
    /** The associate class to which this synchronization object originates */
    final private Association   assOwner;
    
    /** The data type of the parameter we are synchronizing. */
    final private Class<?>      typPrm;
    
    /** The method sourcing the parameter values (e.g., a "getter") */
    final private Method        mthSrc;
    
    /** The method sinking the parameter values (e.g., a "setter") */
    final private Method        mthSnk;
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new association <code>ParameterSync</code> object according to
     * the given specifications.  The arguments are specification of the lattice
     * generator configuration.  From those we can build the synchronization 
     * mechanism.
     * 
     * @param assOwner      the association class generating this synchronization
     * @param prmTarget     the parameter of the association to which this synchronization is assigned
     * 
     * @throws GenerationException  Parameter source or sink method is unreachable/nonexistent 
     * 
     * @author  Christopher K. Allen
     * @since   May 10, 2011
     */
    public ParameterSync(Association assOwner, ParameterMap prmTarget) 
        throws GenerationException 
    {
        Class<? extends AcceleratorNode>    typHware = assOwner.getHardwareType();
        Class<? extends IElement>           typModel = assOwner.getModelType();
        
        try {
            this.assOwner = assOwner;
            this.typPrm   = prmTarget.getDataType();
            this.mthSrc   = typHware.getDeclaredMethod(prmTarget.getSourceName(), this.typPrm);
            this.mthSnk   = typModel.getDeclaredMethod(prmTarget.getSinkName(), this.typPrm);

        } catch (SecurityException e) {
            throw new GenerationException("Setter/getter method is unreachable", e);

        } catch (NoSuchMethodException e) {
            throw new GenerationException("Setter/getter method does not exist", e);

        }
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Synchronized the parameter of the model element with the
     * associated parameter (i.e., <code>this</code>) of the hardware 
     * element.
     *
     * @param smfNode   The SMF hardware device
     * @param modElem   The modeling element
     * 
     * @throws IllegalArgumentException  The arguments are of the wrong type
     * @throws IllegalAccessException    There is an access restriction on a setter or getter 
     * @throws InvocationTargetException The setter or getter threw an exception
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    public void syncValue(AcceleratorNode smfNode, IElement modElem)
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        Class<? extends AcceleratorNode>    typHware = this.assOwner.getHardwareType();
        Class<? extends IElement>           typModel = this.assOwner.getModelType();
        
        if ( !smfNode.getClass().equals(typHware) || !modElem.getClass().equals(typModel) )
            throw new IllegalArgumentException("Argument types must be " + typHware.getName() +
                                               " and " + typModel.getName()
                                                );

        // Invoke the "getter" method of the hardware object
        Object  objVal = this.mthSrc.invoke(smfNode, (Object[])null);

        // Invoke the "setter method of the model element
        this.mthSnk.invoke(modElem, objVal);

    }


}
