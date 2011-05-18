/**
 * SynchronizationMode.java
 *
 * @author Christopher K. Allen
 * @since  May 17, 2011
 *
 */

/**
 * SynchronizationMode.java
 *
 * @author  Christopher K. Allen
 * @since	May 17, 2011
 */
package xal.sim.cfg;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

import xal.model.IElement;
import xal.smf.AcceleratorNode;

/**
 * <p>
 * Creates and operates a synchronization mode between a 
 * hardware device and a modeling element.  The mode consists
 * of the hardware and modeling element to synchronize, and
 * the set of (active) synchronization parameters 
 * (i.e., the class <code>{@link ParameterSync}</code>) defining
 * the mode.
 * </p>
 * <p>
 * The synchronization mode is synchronized (to the hardware object
 * state) using the method <code>{@link #syncMode()}</code>
 * </p>
 *
 * @author Christopher K. Allen
 * @since   May 17, 2011
 */
public class SynchronizationMode {

    
    /*
     * Local Attributes
     */
    
    /** class name of the SMF hardware device */
    final private String                strModeId;
    
    /** The hardware device */
    final private AcceleratorNode       smfDev;
    
    /** The modeling element being synchronized */
    final private IElement              mdlElem;
    
    
    /** list of (Hardware,model) parameter mappings for element initialization */
    final private List<ParameterSync>  lstSyncPrm;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Creates an active synchronization mode for the given hardware object, modeling
     * element, and synchronization map.
     * 
     * @param smfDev    hardware device 
     * @param mdlElem   modeling element being synchronized
     * @param mapSync   map defining the synchronization mode
     * 
     * @throws ClassNotFoundException   a parameter type in the map is invalid (unknown)
     * @throws NoSuchMethodException    a setter/getter method for map parameter does not exist
     * @throws SecurityException        a setter/getter method in the map is unreachable
     *
     * @author  Christopher K. Allen
     * @since   May 18, 2011
     */
    public SynchronizationMode(AcceleratorNode smfDev, IElement mdlElem, SynchronizationMap mapSync) 
        throws SecurityException, ClassNotFoundException, NoSuchMethodException 
    {
    
        this.smfDev    = smfDev;
        this.mdlElem   = mdlElem;
        this.strModeId = mapSync.getModeId();
        
        this.lstSyncPrm = this.buildSyncList(mapSync);
    }
    
    
    /*
     * Attributes
     */
    
    /**
     * Returns the string mode identifier of this synchronization
     * object.
     *
     * @return  the synchronization mode identifier
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public String   getSyncModeId() {
        return this.strModeId;
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Synchronizes all the parameters of the modeling element defined in this mode to the
     * hardware device.  
     *
     * @throws IllegalArgumentException The methods defined in the parameter are not setters/getters
     * @throws IllegalAccessException   There is an access restriction on a setter or getter
     * @throws InvocationTargetException The setter or getter threw an exception
     * 
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    public void syncMode() 
        throws IllegalArgumentException, IllegalAccessException, InvocationTargetException 
    {
        for (ParameterSync sync : this.lstSyncPrm)
            sync.syncValue();
    }
    
    
    /*
     * Support Methods
     */
    
    
    /**
     * Creates the list of active parameter synchronizations from the given  
     * synchronization map.  It is assumed that the hardware device object and
     * the model element object have already been defined as class object
     * attributes.
     *
     * @param mapSync  synchronization map from which this (active) synchronization
     *                 mode will be created
     * 
     * @return  list of active parameter synchronizations for this synchronization mode
     * 
     * @throws ClassNotFoundException a parameter type is invalid (unknown)
     * @throws SecurityException      a setter/getter method for the parameter is unreachable
     * @throws NoSuchMethodException  a setter/getter method for the parameter does not exist
     *
     * @author Christopher K. Allen
     * @since  May 18, 2011
     */
    private List<ParameterSync> buildSyncList(SynchronizationMap mapSync) 
        throws SecurityException, ClassNotFoundException, NoSuchMethodException 
    {
        AcceleratorNode     smfDev      = this.smfDev;
        IElement            mdlElem     = this.mdlElem;
        List<ParameterSync> lstPrmSyncs = new LinkedList<ParameterSync>();
        
        for (ParameterMap prm : mapSync.getSyncParameters()) {
            ParameterSync   sync = new ParameterSync(smfDev, mdlElem, prm);
            
            lstPrmSyncs.add(sync);
        }
        
        return lstPrmSyncs;
    }
}
