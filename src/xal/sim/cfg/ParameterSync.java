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
import xal.smf.AcceleratorNode;

/**
 * <p>
 * Simply put, this class pipes parameter values from hardware
 * devices to the modeling element.  
 * These synchronization objects are able to synchronize 
 * parameters of the hardware and modeling elements after 
 * construction using the method {@link #syncValue()}. That is, 
 * the parameter of the modeling element is updated, or synchronized, 
 * to the current value of the hardware.
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
    
//    /** The data type of the SMF hardware device */
//    final private Class<?>        clsHware;
//    
//    /** the data type of the modeling element */
//    final private Class<?>        clsModel;
    

    /** The method sourcing the parameter values (e.g., a "getter") */
    final private Method        mthSrc;
    
    /** The method sinking the parameter values (e.g., a "setter") */
    final private Method        mthSnk;
    

    /** The hardware device we are synchronizing to */
    final private AcceleratorNode     smfDev;
    
    /** The modeling element we are synchronize */
    final private IElement            mdlElem;
    

    
    /*
     * Initialization
     */
    
    
    /**
     * Creates a new association <code>ParameterSync</code> object according to
     * the given specifications.  
     * 
     * @param smfDev    the hardware device which we are synchronizing to
     * @param mdlElem   the modeling element being synchronized
     * @param prmTarget the parameter which we are synchronizing
     * 
     * @throws ClassNotFoundException   the parameter type is invalid (unknown)
     * @throws SecurityException        a setter/getter method for the parameter is unreachable
     * @throws NoSuchMethodException    a setter/getter method for the parameter does not exist
     *
     * @author  Christopher K. Allen
     * @since   May 17, 2011
     */
    public ParameterSync(AcceleratorNode smfDev, IElement mdlElem, ParameterMap prmTarget)
        throws ClassNotFoundException, SecurityException, NoSuchMethodException
    {
        this.smfDev  = smfDev;
        this.mdlElem = mdlElem;
        
        // Retrieve the class objects for the hardware, modeling element, and parameter type
        Class<?>    clsHware = smfDev.getClass();
        Class<?>    clsModel = mdlElem.getClass();
        Class<?>    clsParam = Class.forName( prmTarget.getDataType() );
        

        // Get the methods we will use to synchronize the model with the hardware
        this.mthSrc   = clsHware.getDeclaredMethod(prmTarget.getSourceName(), clsParam);
        this.mthSnk   = clsModel.getDeclaredMethod(prmTarget.getSinkName(), clsParam);
    }
    
    
//    /**
//     * Creates a new association <code>ParameterSync</code> object according to
//     * the given specifications.  The arguments are specification of the lattice
//     * generator configuration.  From those we can build the synchronization 
//     * mechanism.
//     * 
//     * @param assMain      the association class generating this synchronization
//     * @param prmTarget     the parameter of the association to which this synchronization is assigned
//     * 
//     * @throws GenerationException  Parameter source or sink method is unreachable/nonexistent 
//     * 
//     * @author  Christopher K. Allen
//     * @since   May 10, 2011
//     */
//    public ParameterSync(AssociationDef assMain, ParameterMap prmTarget) 
//        throws GenerationException 
//    {
//        this.smfDev  = null;
//        this.mdlElem = null;
//        
//        try {
//            
//            // Retrieve the class objects for the hardware, modeling element, and parameter type
//            Class<?>    clsHware = Class.forName( assMain.getHardwareClassName() );
//            Class<?>    clsModel = Class.forName( assMain.getModelClassName() );
//            Class<?>    clsParam = Class.forName( prmTarget.getDataType() );
//            
//            // Remember the types of the hardware and modeling element
//            this.clsHware = clsHware;
//            this.clsModel = clsModel;
//
//            // Get the methods we will use to synchronize the model with the hardware
//            this.mthSrc   = clsHware.getDeclaredMethod(prmTarget.getSourceName(), clsParam);
//            this.mthSnk   = clsModel.getDeclaredMethod(prmTarget.getSinkName(), clsParam);
//            
//        } catch (ClassNotFoundException e) {
//            throw new GenerationException("A class type is invalid: ", e);
//
//        } catch (SecurityException e) {
//            throw new GenerationException("Setter/getter method is unreachable", e);
//
//        } catch (NoSuchMethodException e) {
//            throw new GenerationException("Setter/getter method does not exist", e);
//
//        }
//    }
//    
//    public void applyTo(AcceleratorNode smfDev, IElement mdlElem) {
//        
//        if ( smfDev.getClass().equals(this.clsHware) || mdlElem.getClass().equals(this.clsModel) )
//            throw new IllegalArgumentException("Argument types must be " + this.clsHware.getName() +
//                    " and " + this.clsModel.getName()
//                     );
//        
//        this.smfDev  = smfDev;
//        this.mdlElem = mdlElem;
//            
//    }
    
    /*
     * Operations
     */
    
    /**
     * Synchronized the parameter of the model element with the
     * associated parameter (i.e., <code>this</code>) of the hardware 
     * element.
     *
     * @throws IllegalArgumentException  The methods defined in the parameter are not setters/getters
     * @throws IllegalAccessException    There is an access restriction on a setter or getter 
     * @throws InvocationTargetException The setter or getter threw an exception
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    public void syncValue()
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {

        // Invoke the "getter" method of the hardware object
        Object  objVal = this.mthSrc.invoke(this.smfDev, (Object[])null);

        // Invoke the "setter method of the model element
        this.mthSnk.invoke(this.mdlElem, objVal);

    }


}
