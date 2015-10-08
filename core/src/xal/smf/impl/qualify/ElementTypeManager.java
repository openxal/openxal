/*
 * ElementTypeManager.java
 *
 * Created on January 30, 2002, 5:02 PM
 */

package xal.smf.impl.qualify;

import xal.smf.AcceleratorNode;
import xal.smf.impl.GenericNode;

import java.util.*;


/**
 * ElementTypeManager keeps track of which types are associated with which subclass of AcceleratorNode.  It is used for filtering nodes by type.
 * @author  tap
 */
public class ElementTypeManager {
    // static variables
    final private static ElementTypeManager DEFAULT_MANAGER;

    // instance variables
    private Map<String,Collection<Class<?>>> _typeTable;
    
    
	// static initializer
    static {
        DEFAULT_MANAGER = new ElementTypeManager();
    }
    
    
    /** Creates new ElementTypeManager */
    public ElementTypeManager() {
        _typeTable = new Hashtable<String,Collection<Class<?>>>();
    }
    
    
    /**
     * get the default element type manager instance
     */
    public static ElementTypeManager defaultManager() {
        return DEFAULT_MANAGER;
    }

	
	/** Register the specified types to the specified class */
	public void registerTypes( final Class<? extends AcceleratorNode> theClass, final String... types ) {
		for ( final String type : types ) {
			registerType( theClass, type );
		}
	}


    /** Register the type to specified class to be of the specified type */
    public void registerType( final Class<? extends AcceleratorNode> theClass, final String type ) {
        final String lowerType = type.toLowerCase();
        Collection<Class<?>> classSet = getClassSet( lowerType );
        if ( classSet == null ) {
            classSet = new HashSet<Class<?>>();
            _typeTable.put( lowerType, classSet );
        }
        classSet.add( theClass );
    }
    
 
    /** 
     * Check if the node is of the specified type.  A node may belong to 
     * more than one type due to inheritance.
     */
    public boolean match( final AcceleratorNode node, final String type ) {
        return node.isKindOf( type );
    }
    
    
    /** Check if the class or one of its superclasses is associated with the type. */
    public <NodeType> boolean match( final Class<NodeType> theClass, final String type ) {
        final String lowerType = type.toLowerCase();
        final Collection<Class<?>> classSet = getClassSet( lowerType );
        
        if ( classSet == null ) {
            return false;   // no match since no classes registered
        }
        
        
        if ( classSet.contains( theClass ) ) {
            return true;
        }

        for ( Class<?> regClass : classSet ) {
            if ( regClass.isAssignableFrom( theClass ) ) {
                return true;    // theClass is a subclass of regClass
            }
        }
        
        return false;
    }
	
	
	/** get the set of all types */
	public Collection<String> getTypes() {
		return _typeTable.keySet();
	}
    
    
    /** Get the set of classes associated with the specified type */
    private Collection<Class<?>> getClassSet( final String type ) {
        return _typeTable.get( type );
    }
}
