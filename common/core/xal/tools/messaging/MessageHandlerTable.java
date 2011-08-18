/*
 * MessageHandlerTable.java
 *
 * Created on February 5, 2002, 2:18 PM
 */

package xal.tools.messaging;

import java.util.*;
import java.util.logging.*;


/**
 * MessageHandlerTable is a utility class that serves to conveniently store
 * and retrieve MessageHandlers.  Handlers are accessed via source and protocol.
 * @author  tap
 */
class MessageHandlerTable implements java.io.Serializable {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    private Map protocolTable;

    
    /** Creates new MessageHandlerTable */
    public MessageHandlerTable() {
        protocolTable = new Hashtable();
    }
    
    
    /** add the handler to the table */
    public void addHandler( final MessageHandler handler ) {
        Class protocol = handler.getProtocol();
        Object protocolKey = protocolKey( protocol );
        Object source = handler.getSource();
        Map sourceTable;
        
        if ( protocolTable.containsKey( protocolKey ) ) {
            sourceTable = (Map)protocolTable.get( protocolKey );
        }
        else {
            sourceTable = new HashMap();
            protocolTable.put( protocolKey, sourceTable );
        }
        
        if ( sourceTable.containsKey( source ) ) {
            return;
        }
        
        sourceTable.put( source, handler );
    }
    
    
    /** remove the handler from the table */ 
    public void removeHandler( final MessageHandler handler ) {
        Class protocol = handler.getProtocol();
        Object source = handler.getSource();
        Object protocolKey = protocolKey( protocol );
        
        if ( protocolTable.containsKey( protocolKey ) ) {
            Map sourceTable = (Map)protocolTable.get( protocolKey );
            sourceTable.remove( source );
        }    
    }
    
    
    /** remove from the table the handler associated with the source and protocol */
    public <T> void removeHandler( final Object source, final Class<T> protocol ) {
        MessageHandler<T> handler = getHandler( source, protocol );
        if ( handler != null ) {
            removeHandler( handler );
        }
        else {
			final String ERROR_MESSAGE = "Error!  Attempt to remove nonexistant message handler for source: " + source + " and protocol: " + protocol.getName();
			Logger.getLogger("global").log( Level.WARNING, ERROR_MESSAGE );
            System.err.println( ERROR_MESSAGE );
        }
    }
    
    
    /** get all handler associated with the protocol */
    public Set getHandlers( final Class protocol ) {
        Object protocolKey = protocolKey( protocol );
        
        if ( protocolTable.containsKey(protocolKey) ) {
            Map sourceTable = (Map)protocolTable.get(protocolKey);
            return sourceTable.entrySet();
        }
        else {
            return new HashSet();
        }
     }
    
    
    // not implemented yet
    public Set getHandlers( final Object source ) {
        return null;
    }
    
    
    /** get the handler (if any) associated with the source and protocol */
    public <T> MessageHandler<T> getHandler( final Object source, final Class<T> protocol ) {
        Object protocolKey = protocolKey( protocol );
        
        if ( protocolTable.containsKey( protocolKey ) ) {
            Map sourceTable = (Map)protocolTable.get( protocolKey );
            return (MessageHandler<T>)sourceTable.get( source );
        }
        else {
            return null;
        }
    }
    
    
    private Object protocolKey( final Class protocol ) {
        return protocol.getName();
    }
}







