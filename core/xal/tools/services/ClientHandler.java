/*
 * ClientHandler.java
 *
 * Created on July 18, 2003, 9:54 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.services;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;

//import org.apache.xmlrpc.XmlRpcClient;


/**
 * ClientHandler handles messages sent to the proxy by forwarding them to the service associated with the proxy.
 * @author  tap
 */
 class ClientHandler<ProxyType> implements InvocationHandler {
    final protected Class PROTOCOL;
    final protected String SERVICE_NAME;
    final protected ProxyType PROXY;
//    final protected XmlRpcClient REMOTE_CLIENT;
    
    
    /** 
	 * Creates a new ClientHandler to handle service requests.
	 * @param host  The host where the service is running.
	 * @param port  The port through which the service is provided.
	 * @param name  The name of the service.
	 * @param newProtocol  The interface the service provides.
	 */
    public ClientHandler( final String host, final int port, final String name, final Class<ProxyType> newProtocol ) {
        PROTOCOL = newProtocol;
        SERVICE_NAME = name;
        PROXY = createProxy();
    }
    
    
    /** 
     * Get the interface managed by this handler.
     * @return The interface managed by this handler.
     */
    public Class getProtocol() {
        return PROTOCOL;
    }
    
    
    /** 
     * Get the name of the remote service.
     * @return The name of the remote service.
     */
    public String getServiceName() {
        return SERVICE_NAME;
    }
    
    
    /**
     * Get the host name of the remote service.
     * @return The host name of the remote service.
     */
    public String getHost() {
//        return REMOTE_CLIENT.getURL().getHost();
        return null;
    }
    
    
    /**
     * Get the port of the remote service.
     * @return The port of the remote service.
     */
    public int getPort() {
//        return REMOTE_CLIENT.getURL().getPort();
        return 0;
    }
    
    
    /** 
     * Get the proxy that will forward requests to the remote service.
     * @return The proxy that will forward requests to the remote service.
     */
    public ProxyType getProxy() {
        return PROXY;
    }
    
    
    /** 
	 * Create the proxy for this handler to message. 
	 * @return The proxy that will forward requests to the remote service.
	 */
     @SuppressWarnings( "unchecked" )   // we have not choice but to cast since newProxyInstance does not support generics
    private ProxyType createProxy() {
		ClassLoader loader = this.getClass().getClassLoader();
        Class[] protocols = new Class[] {PROTOCOL};
        
        return (ProxyType)Proxy.newProxyInstance( loader, protocols, this );
    }
	
    
    /** 
     * Invoke the specified method on the proxy to implement the InvocationHandler interface.
     * The method is evaluated by calling the remote method using XML-RPC.
     * @param proxy The instance on which the method is invoked.  This argument is unused.
     * @param method The method to implement.
     * @param args The array of arguments to pass to the method.
     * @return The result of the method invokation.
	 * @throws xal.tools.services.RemoteMessageException if an exception occurs while invoking this remote message.
     */
    synchronized public Object invoke( final Object proxy, final Method method, final Object[] args ) throws RemoteMessageException {
        try {
            Vector<Object> params;
			if ( args != null ) {
				params = new Vector<Object>(args.length);
				for ( int index = 0 ; index < args.length ; index++ ) {
					params.add( args[index] );
				}
			}
			else {
				params = new Vector<Object>(0);
			}
            
            final String message = SERVICE_NAME + "." + method.getName();
            return null;
        }
        catch(IllegalArgumentException exception) {
            throw exception;
        }
    }
}
