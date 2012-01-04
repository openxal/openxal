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

import xal.tools.json.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;


/**
 * ClientHandler handles messages sent to the proxy by forwarding them to the service associated with the proxy.
 * @author  tap
 */
class ClientHandler<ProxyType> implements InvocationHandler {
    final private Class PROTOCOL;
    final private String SERVICE_NAME;
    final private ProxyType PROXY;
    final private String REMOTE_HOST;
    final private int REMOTE_PORT;
    
    
    /** 
	 * Creates a new ClientHandler to handle service requests.
	 * @param host  The host where the service is running.
	 * @param port  The port through which the service is provided.
	 * @param name  The name of the service.
	 * @param newProtocol  The interface the service provides.
	 */
    public ClientHandler( final String host, final int port, final String name, final Class<ProxyType> newProtocol ) {
        REMOTE_HOST = host;
        REMOTE_PORT = port;
        SERVICE_NAME = name;
        PROTOCOL = newProtocol;
        
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
    @SuppressWarnings( "unchecked" )    // must cast generic response object to Map
    synchronized public Object invoke( final Object proxy, final Method method, final Object[] args ) throws RemoteMessageException {
        try {
            final List<Object> params = new ArrayList<Object>();
			if ( args != null ) {
                for ( final Object arg : args ) {
                    params.add( arg );
                }
			}
            
            final String methodName = method.getName();
            final Map<String,Object> request = new HashMap<String,Object>();
            final String message = RpcServer.encodeRemoteMessage( SERVICE_NAME, methodName );
            request.put( "message", message );
            request.put( "params", params );
            request.put( "id", new Integer( 1 ) );  // no need to provide a unique id if we don't recycle sockets
            final String jsonRequest = JSONCoder.encode( request );
            
            final Socket remoteSocket = new Socket( REMOTE_HOST, REMOTE_PORT );     // create a new cycle for each request (in the future we may consider recycling sockets)
            final PrintWriter writer = new PrintWriter( remoteSocket.getOutputStream() );
            writer.write( jsonRequest );
            writer.flush();
            
            final int BUFFER_SIZE = 4096;
            final char[] streamBuffer = new char[BUFFER_SIZE];
            final BufferedReader reader = new BufferedReader( new InputStreamReader( remoteSocket.getInputStream() ) );
            final StringBuilder inputBuffer = new StringBuilder();
            do {
                final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );
                
                if ( readCount == -1 ) {     // the session has been closed
                    throw new RuntimeException( "Remote session has unexpectedly closed." );
                }
                else {
                    inputBuffer.append( streamBuffer, 0, readCount );
                }
            } while ( reader.ready()  );
            
            final String jsonResponse = inputBuffer.toString();
            Object result = null;
            if ( jsonRequest != null ) {                
                final Object responseObject = JSONCoder.decode( jsonResponse );
                if ( responseObject instanceof Map ) {
                    final Map<String,Object> response = (Map<String,Object>)responseObject;
                    result = response.get( "result" );
                }
            }
            
            remoteSocket.close();

            return result;
        }
        catch ( IOException exception ) {
            throw new RuntimeException( "IO Exception initiating a remote service request.", exception );
        }
        catch ( IllegalArgumentException exception ) {
            throw exception;
        }
        catch ( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception performing invocation for remote request.", exception );
        }
    }
}
