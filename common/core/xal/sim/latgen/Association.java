/**
 * Association.java
 *
 * @author Christopher K. Allen
 * @since  May 10, 2011
 *
 */

/**
 * Association.java
 *
 * @author  Christopher K. Allen
 * @since	May 10, 2011
 */
package xal.sim.latgen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import xal.model.IElement;
import xal.smf.AcceleratorNode;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataListener;
import xal.tools.data.IArchive;

/**
 * An <code>Association</code> object is a mapping between a hardware
 * device derived from <code>{@link AcceleratorNode}</code> to its
 * corresponding modeling element which exposes the 
 * <code>{@link IElement}</code> interface.
 *
 *
 * @author Christopher K. Allen
 * @since   May 10, 2011
 */
public class Association {

    
        
        
    
    /*
     * Local Attributes
     */
    
    /** The data type of the SMF hardware device */
    private Class<? extends AcceleratorNode>    typHware;
    
    /** the data type of the modeling element */
    private Class<? extends IElement>           typModel;


    /*
     * Initialization
     */
    
    
    
    /*
     * Attributes
     */
    
    /**
     * @return the typHware
     */
    public Class<? extends AcceleratorNode> getHardwareType() {
        return typHware;
    }

    /**
     * @return the typModel
     */
    public Class<? extends IElement> getModelType() {
        return typModel;
    }
    
    
    
}
