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
    
    /** table keyed by protocol name and mapped to the source table */
    final private Map<String,Map<Object,MessageHandler<?>>> PROTOCOL_TABLE;

    
    /** Creates new MessageHandlerTable */
    public MessageHandlerTable() {
        PROTOCOL_TABLE = new Hashtable<String,Map<Object,MessageHandler<?>>>();
    }
    
    
    /** add the handler to the table */
    public <ProtocolType> void addHandler( final MessageHandler<ProtocolType> handler ) {
        final Class<ProtocolType> protocol = handler.getProtocol();
        final String protocolKey = protocolKey( protocol );
        final Object source = handler.getSource();
        Map<Object,MessageHandler<?>> sourceTable;     // table of handlers keyed by source
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            sourceTable = PROTOCOL_TABLE.get( protocolKey );
        }
        else {
            sourceTable = new HashMap<Object,MessageHandler<?>>();
            PROTOCOL_TABLE.put( protocolKey, sourceTable );
        }
        
        if ( sourceTable.containsKey( source ) ) {
            return;
        }
        
        sourceTable.put( source, handler );
    }
    
    
    /** remove the handler from the table */ 
    public <ProtocolType> void removeHandler( final MessageHandler<ProtocolType> handler ) {
        final Class<ProtocolType> protocol = handler.getProtocol();
        final String protocolKey = protocolKey( protocol );
        final Object source = handler.getSource();
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            final Map<Object,MessageHandler<?>> sourceTable = PROTOCOL_TABLE.get( protocolKey );
            sourceTable.remove( source );
        }    
    }
    
    
    /** remove from the table the handler associated with the source and protocol */
    public <T> void removeHandler( final Object source, final Class<T> protocol ) {
        final MessageHandler<T> handler = getHandler( source, protocol );
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
    public <ProtocolType> Set<Map.Entry<Object,MessageHandler<?>>> getHandlers( final Class<ProtocolType> protocol ) {
        final String protocolKey = protocolKey( protocol );
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            final Map<Object,MessageHandler<?>> sourceTable = PROTOCOL_TABLE.get( protocolKey );
            return sourceTable.entrySet();
        }
        else {
            return new HashSet<Map.Entry<Object,MessageHandler<?>>>();
        }
	}

    
    /** get the handler (if any) associated with the source and protocol */
    @SuppressWarnings( "unchecked" )
    public <T> MessageHandler<T> getHandler( final Object source, final Class<T> protocol ) {
        Object protocolKey = protocolKey( protocol );
        
        if ( PROTOCOL_TABLE.containsKey( protocolKey ) ) {
            final Map<Object,MessageHandler<?>> sourceTable = PROTOCOL_TABLE.get( protocolKey );
            return (MessageHandler<T>)sourceTable.get( source );
        }
        else {
            return null;
        }
    }
    
    
    private <ProtocolType> String protocolKey( final Class<ProtocolType> protocol ) {
        return protocol.getName();
    }
}







