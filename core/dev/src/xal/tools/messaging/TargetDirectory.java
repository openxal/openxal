/*
 * TargetDirectory.java
 *
 * Created on February 6, 2002, 8:20 AM
 */

package xal.tools.messaging;


import java.util.*;

/**
 * TargetDirectory is a utility class for convenient storage and retrieval
 * of targets keyed by source and protocol.
 * @author  tap
 */
class TargetDirectory implements java.io.Serializable {
    private Map protocolTable;
    

    /** Creates new TargetDirectory */
    public TargetDirectory() {
        protocolTable = new Hashtable();
    }

    
    /** get a safe, unmodifiable set of targets */
    synchronized Set targets( final Object source, final Class protocol ) {
        Set targetSet = targetSet(source, protocol);
        return Collections.unmodifiableSet( new HashSet(targetSet) );
    }
    
    
    /** get the set of targets keyed by source and protocol */
    synchronized private Set targetSet(Object source, Class protocol) {
        Map sourceTable;
        Object protocolKey = protocolKey(protocol);
        
        if ( protocolTable.containsKey(protocolKey) ) {
            sourceTable = (Map)protocolTable.get(protocolKey);
        }
        else {
            return emptySet();
        }
        
        if ( sourceTable.containsKey(source) ) {
            return (Set)sourceTable.get(source);
        }
        else {
            return emptySet();
        }
    }
    
	
    /** an emptySet */
    private Set emptySet() {
        return new HashSet();
    }
    
    
    /** Register a target to listen to protocol messages from source */
    synchronized public void registerTarget( Object target, Object source, Class protocol ) {
        Map sourceTable;
        Object protocolKey = protocolKey(protocol);
        
        if ( protocolTable.containsKey(protocolKey) ) {
            sourceTable = (Map)protocolTable.get(protocolKey);
        }
        else {
            sourceTable = new HashMap();
            protocolTable.put(protocolKey, sourceTable);
        }
        
        Set targetSet;
        if ( sourceTable.containsKey(source) ) {
            targetSet = (Set)sourceTable.get(source);
        }
        else {
            targetSet = new HashSet();
            sourceTable.put(source, targetSet);
        }
        targetSet.add(target);
    }
    
    
    /** remove the target as a listener of protocol messages from source */
    synchronized public void removeTarget(Object target, Object source, Class protocol) {
        targetSet(source, protocol).remove(target);
    }
    
    
    /** remove the target as a listener of protocol messages */
    synchronized public void removeTarget(Object target, Class protocol) {
        targetSet(null, protocol).remove(target);
    }
    
    
	/** Remove the target from all sources that message to the specified protocol */
	synchronized public void removeTargetFromAllSources(Object target, Class protocol) {
        Map sourceTable;
        Object protocolKey = protocolKey(protocol);
        
        if ( protocolTable.containsKey(protocolKey) ) {
            sourceTable = (Map)protocolTable.get(protocolKey);
        }
        else {
            return;
        }
        
		// get the collection of target sets associated with every source in the table
		Collection targetSets = sourceTable.values();
		
		// loop through the target sets and remove the target from each set
		Iterator targetSetIter = targetSets.iterator();
		while ( targetSetIter.hasNext() ) {
			Set targetSet = (Set)targetSetIter.next();
			targetSet.remove(target);
		}
	}
	
    
    private Object protocolKey(Class protocol) {
        return protocol.getName();
    }
}
