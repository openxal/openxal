/*
 * MessageHandler.java
 *
 * Created on February 5, 2002, 11:40 AM
 */

package xal.tools.messaging;


import java.lang.reflect.*;
import java.util.*;


/**
 * MessageHandler is an abstract class whose subclasses receive and 
 * forward messages. It provides the foundation for handling messages.
 *
 * @author  tap
 */
abstract class MessageHandler<ProtocolType> implements InvocationHandler, java.io.Serializable {
	/** required for Serializable */
	static final private long serialVersionUID = 1L;
	
    protected Class<ProtocolType> _protocol;
    protected Object source;
    protected ProtocolType proxy;
    protected Thread[] threadPool;
    protected TargetDirectory targetDirectory;
    
    
    /** Creates new MessageHandler */
    public MessageHandler( final TargetDirectory newDirectory, final Class<ProtocolType> newProtocol, final int threadPoolSize ) {
        this( newDirectory, null, newProtocol, threadPoolSize );
    }
    
    
    /** Creates new MessageHandler */
    public MessageHandler( final TargetDirectory newDirectory, final Object newSource, final Class<ProtocolType> newProtocol, final int threadPoolSize ) {
        targetDirectory = newDirectory;
        source = newSource;
        _protocol = newProtocol;
        threadPool = new Thread[threadPoolSize];
        createProxy();
    }
	
	
	/** Subclasses should override this method to perform any cleanup prior to removal. */
	public void terminate() {}
    
    
    /** return the interface managed by this handler */
    public Class<ProtocolType> getProtocol() {
        return _protocol;
    }
    
    
    /** return the source of the messages */
    public Object getSource() {
        return source;
    }
    
    
    /** return the proxy that will forward messages to registered targets */
    public ProtocolType getProxy() {
        return proxy;
    }
    
    
    /** create the proxy for this handler to message */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private void createProxy() {
		ClassLoader loader = this.getClass().getClassLoader();
        Class[] protocols = new Class[] {_protocol};		// need to suppress the rawtypes as Generics aren't supported for array creation
        
        proxy = (ProtocolType)Proxy.newProxyInstance( loader, protocols, this );
    }
    
    
    /** 
     * subclasses must override whether they support synchronous or asynchronous messages
	 * @return true if the messaging is synchronous and false if not
     */
    abstract public boolean isSynchronous();
    
    
    /** implement InvocationHandler interface */
    /** invoke method */
    abstract public Object invoke( final Object proxy, final Method method, final Object[] args );
        
    
    /** get all targets associated with the source and protocol and just the protocol */
    protected Set<ProtocolType> targets() {
        final Set<ProtocolType> targetSet = new HashSet<ProtocolType>();
        
        // add targets directly associated with the protocol and the target
        final Set<ProtocolType> directTargets = targetDirectory.targets( source, _protocol );
        targetSet.addAll( directTargets );
        
        // add targets associated with the protocol but no target
        final Set<ProtocolType> anonymousTargets = targetDirectory.targets( null, _protocol );
        targetSet.addAll( anonymousTargets );

        return targetSet;
    }
}
