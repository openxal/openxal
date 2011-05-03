package xal.smf;

import xal.smf.impl.*;
import xal.tools.data.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.*;

import java.lang.ClassNotFoundException;
import java.lang.reflect.Constructor;


/**
 * Class factory for all AcceleratorNode objects. 
 * The factory is used in parsing XML files in XDXF format.  Every AcceleratorNode has a type code (in String format) which may be used to instantiate the class.
 * @author  Nikolay Malitsky, Christopher K. Allen, Tom Pelaia
 */

public final class AcceleratorNodeFactory {
	/** map of constructors keyed by node type */
	private Map<String,Constructor> _constructors = new HashMap();
	
	/** map of classes keyed by node type */
	private Map<String,Class> _classTable;
	
	
	/** Constructor */
	public AcceleratorNodeFactory() {
		_constructors = new HashMap<String,Constructor>();
		_classTable = new HashMap<String,Class>();
	}
	
	
    /**
     *  Associate the specified AcceleratorNode class with the specified node type
	 *  @param  deviceType  device type
	 *  @param  softType    software type (null indicates there is no software type)
     *  @param  nodeClass   Class class for the AcceleratorNode
     */
    public void registerNodeClass( final String deviceType, final String softType, final Class nodeClass )   {
		final String nodeType = softType != null ? deviceType + "." + softType : deviceType;
		registerNodeClass( nodeType, nodeClass );
    }
	
	
    /**
     *  Associate the specified AcceleratorNode class with the specified node type
	 *  @param  nodeType    fully qualified node type (e.g. deviceType.softType)
     *  @param  nodeClass   Class class for the AcceleratorNode
     */
    private void registerNodeClass( final String nodeType, final Class nodeClass )   {
        _classTable.put( nodeType, nodeClass );
		
        try {
            final Constructor constructor = nodeClass.getConstructor( new Class[] { String.class } );
            _constructors.put( nodeType, constructor );
        }
		catch ( NoSuchMethodException exception ) {
			final String message = "AcceleratorNodeFactory: class registeration failure for type: " + nodeType;
            System.err.println( message );
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
        }
		catch ( SecurityException exception )    {
			final String message = "AcceleratorNodeFactory: class registeration failure for type: " + nodeType;
            System.err.println( message );
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
        }
    }
	
	
    /** 
     * Creates the node with the specified node id and fully qualified node type.
	 * @param nodeID device ID
	 * @param nodeType fully qualified node type (e.g. deviceType.softType)
     */
    private AcceleratorNode createNode( final String nodeID, final String nodeType ) throws ClassNotFoundException {
        // Check if this node type is known; if not then substitute a generic node
        if ( !_constructors.containsKey( nodeType ) ) {
			final String message = "Unknown AcceleratorNode type : \"" + nodeType + "\" for ID: " + nodeID + ".  Will substitute a GenericNode!";
            System.err.println( message );
			Logger.getLogger("global").log( Level.WARNING, message );
            AcceleratorNode node = GenericNode.newNode( nodeType, nodeID );
            _classTable.put( nodeType, GenericNode.class );
            return node;
        }
        
        final Constructor constructor = _constructors.get( nodeType );
        final Object[] args = new Object[] { nodeID };
        
        try {
            return (AcceleratorNode)constructor.newInstance( args );
        } 
		catch (Throwable exception)   {
             throw new ClassNotFoundException( "Unknown AcceleratorNode type : " + nodeType );
        }
    }
    
    
    /** create an accelerator node based on a DataAdaptor */
    public AcceleratorNode createNode( final DataAdaptor adaptor ) throws ClassNotFoundException {
        final String nodeID = adaptor.stringValue( "id" );
        final String deviceType = adaptor.stringValue( "type" );
		final String softType = adaptor.hasAttribute( "softType" ) ? adaptor.stringValue( "softType" ) : null;
		final String nodeType = softType != null ? deviceType + "." + softType : deviceType;
		
        return createNode( nodeID, nodeType );
    }
}


