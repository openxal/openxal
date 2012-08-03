/*
 * MessageCenter.java
 *
 * Created on February 4, 2002, 5:03 PM
 */

package xal.tools.messaging;


import java.util.*;

/**
 * MessageCenter provides an interface to the messaging system with
 * lots of convenience methods for easy access to messaging features.
 *
 * @author  tap
 */
public class MessageCenter implements java.io.Serializable {
    /** required for serializable objects */
    private static final long serialVersionUID = 1L;
    
	/** forward events on the invoking thread */
	static final public int SYNCHRONOUS = 0;
	
	/** forward events from a new thread */
	static final public int ASYNCHRONOUS = 1;
	
	/** forward fresh events (drop old unprocessed pending events) on a common thread per protocol */
	static final public int FRESH = 2;
	
	/** default message center that can be accessed throughout the process */
    static final private MessageCenter DEFAULT_CENTER;
	
	/** The default thread pool size - currently not used */
    static final int DEFAULT_THREAD_POOL_SIZE = 5;
	
    /** table of registered handlers */
    private final MessageHandlerTable HANDLER_TABLE;
	
    /** directory of registered targets */
    private final TargetDirectory TARGET_DIRECTORY;
	
    /** name to identify the MessageCenter instance */
    private final String NAME;
	
    /** unimplemented, but will be used to reduce bottlenecks */
    private int _threadPoolSize;
	
    
	/** 
	 * Static initializer
	 * Create a default message center.
	 */
    static {
        DEFAULT_CENTER = new MessageCenter();
    }
	
    
    /** Creates new MessageCenter */
    public MessageCenter() {
        this( "default" );
    }
    
    
    /** 
	 * Create a new MessageCenter with given name
	 * @param newName The name of this Message Center.
	 */
    public MessageCenter( final String newName ) {
        this( newName, DEFAULT_THREAD_POOL_SIZE );
    }
    
    
    /** 
	 * Create a new MessageCenter with given thread pool size
	 * @param threadPoolSize The thread pool size of this message center 
	 */
    public MessageCenter( final int threadPoolSize ) {
        this( "default", threadPoolSize );
    }
    
    
    /** 
	 * Create a new MessageCenter with given name and thread pool size
	 * @param newName The name of this message center
	 * @param newThreadPoolSize The thread pool size of this message center 
	 */
    public MessageCenter( final String newName, final int newThreadPoolSize ) {
        NAME = newName;
        setThreadPoolSize( newThreadPoolSize );
        HANDLER_TABLE = new MessageHandlerTable();
        TARGET_DIRECTORY = new TargetDirectory();
    }
    
    
    /** 
	 * default message center instance 
	 * @return the default message center
	 */
    public static MessageCenter defaultCenter() {
        return DEFAULT_CENTER;
    }
    
    
    /** 
	 * Create a new MessageCenter
	 * @return a new message center
	 */
    public static MessageCenter newCenter() {
        return new MessageCenter();
    }    
    
    
    /** 
	 * Create a new MessageCenter
	 * @param newName the name to assign this message center
	 * @return a new message center
	 */
    public static MessageCenter newCenter( final String newName ) {
        return new MessageCenter( newName );
    }
    
    
    /** 
	 * Create a new MessageCenter
	 * @param newThreadPoolSize The thread pool size of this message center 
	 * @return a new message center
	 */
    public static MessageCenter newCenter( final int newThreadPoolSize ) {
        return new MessageCenter( newThreadPoolSize );
    }
    
    
    /** 
	 * Create a new MessageCenter
	 * @param newName The name of this message center
	 * @param newThreadPoolSize The thread pool size of this message center 
	 * @return a new message center
	 */
    public static MessageCenter newCenter( final String newName, final int newThreadPoolSize ) {
        return new MessageCenter( newName, newThreadPoolSize );
    }
    
    
    /** 
     * set the thread pool size for concurrent messaging 
     * to reduce bottlenecks.
     * Thread pools have not yet been implemented.
	 * @param threadPoolSize The thread pool size of this message center 
     */
    private void setThreadPoolSize( final int threadPoolSize ) {
        // need to implement this method
    }
    
    
    // target registration --------------------------------------------------------------
    
    /**
     * register target for messages from the source and for the specified interface
	 * @param target The target to receive messages
	 * @param source The source from which we wish to receive messages
	 * @param protocol The protocol identifying the message type to receive
     */
    synchronized public <T> void registerTarget( final T target, final Object source, final Class<T> protocol ) {
        if ( target == null ) {
            throw new NullTargetException( source, protocol );
        }
        
        if ( !protocol.isInstance( target ) ) {
            throw new UnimplementedProtocolException( target, protocol );
        }
        
        TARGET_DIRECTORY.registerTarget( target, source, protocol );
    }
    
    
    /**
     * register target for messages from any source which posts to the interface
  	 * @param target The target to receive messages
	 * @param protocol The protocol identifying the message type to receive
     */
    synchronized public <T> void registerTarget( final T target, final Class<T> protocol ) {
        registerTarget( target, null, protocol );
    }
    
    
    // unregistering targets ----------------------------------------------------------------
    
    /**
     * remove target from listening to specified source with specified protocol
	 * @param target The target receiving messages
	 * @param source The source from which we are receiving messages
	 * @param protocol The protocol identifying the message type being received
     */
    synchronized public <T> void removeTarget( final T target, final Object source, final Class<T> protocol ) {
        TARGET_DIRECTORY.removeTarget( target, source, protocol );
    }
	
	
    /** 
	 * Remove the target from every source that broadcasts the specified protocol 
	 * @param target The target receiving messages
	 * @param protocol The protocol identifying the message type being received
	 */
    synchronized public <T> void removeTargetFromAllSources( final T target, final Class<T> protocol ) {
		TARGET_DIRECTORY.removeTargetFromAllSources( target, protocol );
    }


    /**
     * Unregister the target from listening to the specified protocol from the specified collection
     * of sources.
	 * @param target The target receiving messages
	 * @param sources The sources from which we are receiving messages
	 * @param protocol The protocol identifying the message type being received
     */
    synchronized public <T> void removeTarget( final T target, final Collection<? extends Object> sources, final Class<T> protocol ) {
		for ( final Object source : sources ) {
			removeTarget( target, source, protocol );
		}
    }
    
    
    /**
     * Removes the target from listening for the specified protocol.
     * It only removes the target from listening to the protocol from anonymous sources.
     * For example, it unregisters a call of registerTarget(target, protocol).
     * This method does NOT remove the target from listenting to directly registered sources.
	 * @param target The target receiving messages
	 * @param protocol The protocol identifying the message type being received
     */
    synchronized public <T> void removeTarget( final T target, final Class<T> protocol ) {
        TARGET_DIRECTORY.removeTarget( target, protocol );
    }
    
    
    // registering sources --------------------------------------------------------------------
    
    /**
     * register the specified source to be associated with the specified event protocol
     * defaults to synchronous messaging
	 * @param source The source of the message
	 * @param protocol The type and interface of the messages the source will send
	 * @return The proxy (implementing protocol) to call to broadcast messages
     */
    synchronized public <T> T registerSource( final Object source, final Class<T> protocol ) {
        return registerSource( source, protocol, SYNCHRONOUS );
    }
    
    
    /**
     * Register the specified source to be associated with the specified event protocol and using synchronous (true) or asynchronous messaging (false)
	 * @param source The source of the message
	 * @param protocol The type and interface of the messages the source will send
	 * @param isSynchronous true to enable synchronous messaging and false for asynchronous messaging
	 * @return The proxy (implementing protocol) to call to broadcast messages
	 * @deprecated Use the version of this method that takes the synchronous type instead
     */
    @Deprecated synchronized public <T> T registerSource( final Object source, final Class<T> protocol, final boolean isSynchronous ) {
		final int synchronousType = isSynchronous ? SYNCHRONOUS : ASYNCHRONOUS;
		return registerSource( source, protocol, synchronousType );
    }
    
    
    /**
     * Register the specified source to be associated with the specified event protocol and processed as the synchronous type
	 * @param source The source of the message
	 * @param protocol The type and interface of the messages the source will send
	 * @param synchronousType one of SYNCHRONOUS, ASYNCHRONOUS or FRESH to indicate how events are processed
	 * @return The proxy (implementing protocol) to call to broadcast messages
     */
    synchronized public <T> T registerSource( final Object source, final Class<T> protocol, final int synchronousType ) {
        if ( source == null ) {
            throw new NullSourceException();
        }
        
        // see if the handler already exists
        MessageHandler<T> handler = HANDLER_TABLE.getHandler( source, protocol );
		
        if ( handler == null ) {
            handler = createHandler( source, protocol, synchronousType );
            HANDLER_TABLE.addHandler( handler );
        }
        
        return handler.getProxy();
    }
	
    
    /** 
	 * Create a handler given source, protocol, and synch/asynch status
	 * @param source The source on behalf of which messages will be broadcast
	 * @param protocol The interface/type of messages broadcast
	 * @param isSynchronous true for synchronous messaging and false otherwise
	 * @return the new message handler
	 */
    private <T> MessageHandler<T> createHandler( final Object source, final Class<T> protocol, final int synchronousType ) {
		switch( synchronousType ) {
			case SYNCHRONOUS:
				return new SynchronousMessageHandler<T>( TARGET_DIRECTORY, source, protocol, _threadPoolSize );
			case ASYNCHRONOUS:
				return new AsynchronousMessageHandler<T>( TARGET_DIRECTORY, source, protocol, _threadPoolSize );
			case FRESH:
				return new FreshMessageHandler<T>( TARGET_DIRECTORY, source, protocol, _threadPoolSize );
			default:
				return null;
		}
    }    
    
    
    /** 
	 * Remove source registration which means the proxy for the source/protocol pair
	 * is no longer able to broadcast messages.  The handler for this source/protocol pair 
	 * will be removed.
	 * @param source The source of messages 
	 * @param protocol The interface/type of messages
	 */
    synchronized public <T> void removeSource( final Object source, final Class<T> protocol ) {
		final MessageHandler<T> handler = HANDLER_TABLE.getHandler( source, protocol );
		handler.terminate();
        HANDLER_TABLE.removeHandler( handler );
    }
    
    
    // accessors ----------------------------------------------------------------------------
    
    /**
     * get the proxy for the specified source and protocol
	 * @param source The source on behalf of which messages will be broadcast
	 * @param protocol The interface/type of message implemented by the proxy
	 * @return The proxy (implementing protocol) to call on behalf of the source to broadcast messages
     */
    public <T> T getProxy( final Object source, final Class<T> protocol ) {
        MessageHandler<T> handler = HANDLER_TABLE.getHandler( source, protocol );
        
        return handler.getProxy();
    }
    
    
    /** 
	 * get the name of the MessageCenter instance
	 * @return the name of the message center
	 */
    public String name() {
        return NAME;
    }
    
    
    /** 
	 * Override toString() to return a description of the message center.  At this point
	 * it simply returns the name of the message center.
	 * @return the description of the MessageCenter instance 
	 */
    public String toString() {
        return name();
    }
    
	
    
    /** Generic class for all MessageCenter related exceptions  */
    public class MessageCenterException extends java.lang.RuntimeException {
        /** required for serializable objects */
        private static final long serialVersionUID = 1L;
        
        /** Creates new MessageCenterException */
        public MessageCenterException() {
            super();
        }
        
        
        public MessageCenterException( final String notice ) {
            super( notice );
        }
    }
	


    /** Exception when an attempt is made to register a null source */
    public class NullTargetException extends MessageCenterException {
        /** required for serializable objects */
        private static final long serialVersionUID = 1L;
        
        /** Creates new NullSourceException */
        public NullTargetException( final Object source, final Class<?> protocol ) {
            this( "Attempt to register a null target with MessageCenter: " + MessageCenter.this.name() +
            " for protocol: " + protocol.getName() +
            " on source: " + source);
        }

    
        public NullTargetException(String notice) {
            super(notice);
        }
        
        
        public NullTargetException() {
            super();
        }
    }
	
    
    
    /** Exception when an attempt is made to register a null source */
    public class NullSourceException extends MessageCenterException {
        /** required for serializable objects */
        private static final long serialVersionUID = 1L;

        /** Creates new NullSourceException */
        public NullSourceException() {
            this( "Attempt to register a null source with MessageCenter: " + MessageCenter.this.name() );
        }

    
        public NullSourceException( final String notice ) {
            super( notice );
        }    
    }
	
    
    
    /** Exception when an attempt is made to register a target for a protocol not implemented by its class. */
    public class UnimplementedProtocolException extends MessageCenterException {
        /** required for serializable objects */
        private static final long serialVersionUID = 1L;

        /** Creates new UnimplementedProtocolException */
        public UnimplementedProtocolException() {
            super();
        }
		
		
		/** Constructor */
        public UnimplementedProtocolException( final String notice ) {
            super( notice );
        }
		
		
		/** Constructor */
        public UnimplementedProtocolException( final Object target, final Class<?> protocol ) {
            this("Attempt to register a target, \"" + target +
                "\" with message center, \"" +  MessageCenter.this.name() + 
                "\" for the protocol, \"" + protocol.getName() + 
                "\", which the target's class, \"" + target.getClass().getName() +
                "\", does not implement!");
        }
    }
}







