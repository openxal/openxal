/*
 * TargetDirectory.java
 *
 * Created on February 6, 2002, 8:20 AM
 */

package xal.tools.messaging;


import java.util.*;

/**
 * TargetDirectory is a utility class for convenient storage and retrieval of targets keyed by source and protocol.
 * @author  tap
 */
class TargetDirectory implements java.io.Serializable {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /** Map of target sets keyed by source which are keyed by protocol name */
    final private Map<String,Map<Object,Set<Object>>> PROTOCOL_TABLE;
    

    /** Creates new TargetDirectory */
    public TargetDirectory() {
        PROTOCOL_TABLE = new Hashtable<String,Map<Object,Set<Object>>>();
    }

    
    /** get an unmodifiable set of targets */
    synchronized <T> Set<T> targets( final Object source, final Class<T> protocol ) {
        final Set<T> targetSet = targetSet( source, protocol );
        return Collections.unmodifiableSet( new HashSet<T>( targetSet ) );
    }
    
    
    /** get the set of targets keyed by source and protocol */
    @SuppressWarnings( "unchecked" )
    synchronized private <T> Set<T> targetSet( final Object source, final Class<T> protocol ) {
        Map<Object,Set<Object>> sourceTable;
        final String protocolKey = protocolKey( protocol );
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            sourceTable = PROTOCOL_TABLE.get( protocolKey );
        }
        else {
            return emptySet( protocol );
        }
        
        if ( sourceTable.containsKey( source ) ) {
            return (Set<T>)sourceTable.get( source );
        }
        else {
            return emptySet( protocol );
        }
    }
    
	
    /** an emptySet */
    private <T> Set<T> emptySet( final Class<T> protocol ) {
        return new HashSet<T>();
    }
    
    
    /** Register a target to listen to protocol messages from source */
    synchronized public <T> void registerTarget( final Object target, final Object source, final Class<T> protocol ) {
        Map<Object,Set<Object>> sourceTable;
        final String protocolKey = protocolKey( protocol );
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            sourceTable = PROTOCOL_TABLE.get( protocolKey );
        }
        else {
            sourceTable = new HashMap<Object,Set<Object>>();
            PROTOCOL_TABLE.put( protocolKey, sourceTable );
        }
        
        Set<Object> targetSet;
        if ( sourceTable.containsKey( source ) ) {
            targetSet = sourceTable.get( source );
        }
        else {
            targetSet = new HashSet<Object>();
            sourceTable.put( source, targetSet );
        }
        targetSet.add( target );
    }
    
    
    /** remove the target as a listener of protocol messages from source */
    synchronized public <T> void removeTarget( final Object target, final Object source, final Class<T> protocol ) {
        targetSet( source, protocol ).remove( target );
    }
    
    
    /** remove the target as a listener of protocol messages */
    synchronized public <T> void removeTarget( final Object target, final Class<T> protocol ) {
        targetSet( null, protocol ).remove( target );
    }
    
    
	/** Remove the target from all sources that message to the specified protocol */
	synchronized public <T> void removeTargetFromAllSources( final Object target, final Class<T> protocol ) {
        Map<Object,Set<Object>> sourceTable;
        final String protocolKey = protocolKey( protocol );
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            sourceTable = PROTOCOL_TABLE.get( protocolKey );
        }
        else {
            return;
        }
        
		// get the collection of target sets associated with every source in the table
		final Collection<Set<Object>> targetSets = sourceTable.values();
		
		// loop through the target sets and remove the target from each set
        for ( final Set<Object> targetSet : targetSets ) {
			targetSet.remove( target );
        }
	}
	
    
    private <T> String protocolKey( final Class<T> protocol ) {
        return protocol.getName();
    }
}
