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
import java.util.concurrent.*;
import java.util.logging.*;
import java.lang.reflect.*;
import java.lang.reflect.Proxy;


/**
 * ClientHandler handles messages sent to the proxy by forwarding them to the service associated with the proxy.
 * @author  tap
 */
class ClientHandler<ProxyType> implements InvocationHandler {
    /** queue for processing requests */
    final static private ExecutorService REQUEST_QUEUE;
    
    /** single thread queue for sending remote messages */
    final private ExecutorService REMOTE_SEND_QUEUE;
    
    /** single thread queue for receiving remote messages */
    final private ExecutorService REMOTE_RECEIVE_QUEUE;
    
    /** socket for sending and receiving remote messages */
    final private Socket REMOTE_SOCKET;
    
    /** protocol implemented by the remote service and dispatched through the proxy */
    final private Class<ProxyType> PROTOCOL;
    
    /** name of the remote service */
    final private String SERVICE_NAME;
    
    /** proxy which forwards invocations to the remote service */
    final private ProxyType PROXY;
    
    /** remote host */
    final private String REMOTE_HOST;
    
    /** remote port */
    final private int REMOTE_PORT;
    
    /** request ID counter is incremented to provide a unique ID for each request */
    private volatile int _requestIDCounter;
    
    /** pending results keyed by request ID */
    final private Map<Long,PendingResult> PENDING_RESULTS;
    
    
    // static initializer
    static {
        REQUEST_QUEUE = Executors.newCachedThreadPool();
    }
    
    
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
        
        PENDING_RESULTS = Collections.synchronizedMap( new HashMap<Long,PendingResult>() );
        
        REMOTE_SEND_QUEUE = Executors.newSingleThreadExecutor();
        REMOTE_RECEIVE_QUEUE = Executors.newSingleThreadExecutor();
        REMOTE_SOCKET = makeRemoteSocket( host, port );
        
        _requestIDCounter = 0;
    }
    
    
    /** make a new remote socket */
    static private Socket makeRemoteSocket( final String host, final int port ) {
        try {
            return new Socket( host, port );
        }
        catch( UnknownHostException exception ) {
            throw new RuntimeException( "Attempt to open a socket to an unknown host.", exception );
        }
        catch( IOException exception ) {
            throw new RuntimeException( "IO Exception attempting to open a new socket.", exception );
        }
    }
    
    
    /** Get the next request ID and increment it */
    private int getNextRequestID() {
        return _requestIDCounter++;
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
        return REMOTE_HOST;
    }
    
    
    /**
     * Get the port of the remote service.
     * @return The port of the remote service.
     */
    public int getPort() {
        return REMOTE_PORT;
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
    
    
    /** dispose of resources */
    public void dispose() {
        if ( !REMOTE_SOCKET.isClosed() ) {
            try {
                REMOTE_SOCKET.close();
            }
            catch( Exception exception ) {
                throw new RuntimeException( "Excepting closing remote client socket.", exception );
            }
        }
    }
    
    
    /** dispose of resources upon collection */
    public void finalize() {
        dispose();
    }
    
    
    /** listen for incoming messages */
    private void listenForRemoteMessages() {
        REMOTE_RECEIVE_QUEUE.submit( new Runnable() {
            public void run() {
                try {
                    processRemoteResponse();
                }
                catch( Exception exception ) {
                    exception.printStackTrace();
                }
            }
        });
    }
    
    
    /** process the remote response */
    @SuppressWarnings( "unchecked" )    // no way to know response Object type at compile time
    private void processRemoteResponse() throws java.net.SocketException, java.io.IOException {
        final int BUFFER_SIZE = REMOTE_SOCKET.getReceiveBufferSize();
        final char[] streamBuffer = new char[BUFFER_SIZE];
        final BufferedReader reader = new BufferedReader( new InputStreamReader( REMOTE_SOCKET.getInputStream() ) );
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
        if ( jsonResponse != null ) {
            final Object responseObject = JSONCoder.decode( jsonResponse );
            if ( responseObject instanceof Map ) {
                final Map<String,Object> response = (Map<String,Object>)responseObject;
                final Object result = response.get( "result" );
                final RuntimeException remoteException = (RuntimeException)response.get( "error" );
                final Long requestID = (Long)response.get( "id" );
                
                final PendingResult pendingResult = PENDING_RESULTS.get( requestID );
                
                if ( pendingResult != null ) {
                    synchronized( pendingResult ) {
                        pendingResult.setValue( result );
                        pendingResult.setRemoteException( remoteException );
                        pendingResult.notify();
                    }
                }
            }
        }
    }
    
    
    /** Submit the remote request */
    private void submitRemoteRequest( final String jsonRequest ) {
        REMOTE_SEND_QUEUE.submit( new Runnable() {
            public void run() {
                try {
                    final PrintWriter writer = new PrintWriter( REMOTE_SOCKET.getOutputStream() );
                    writer.write( jsonRequest );
                    writer.flush();
                }
                catch( Exception exception ) {
                    exception.printStackTrace();
                }
            }
        });
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
            
            final long requestID = getNextRequestID();
            
            final String methodName = method.getName();
            final Map<String,Object> request = new HashMap<String,Object>();
            final String message = RpcServer.encodeRemoteMessage( SERVICE_NAME, methodName );
            request.put( "message", message );
            request.put( "params", params );
            request.put( "id", requestID );
            final String jsonRequest = JSONCoder.encode( request );
            
            // methods marked with the OneWay annotation return immediately and do not wait for a response from the service
            final boolean waitForResponse = !method.isAnnotationPresent( OneWay.class );

            
            // configure a pending result whose value will be set upon receiving a response from the remote service
            final PendingResult pendingResult = new PendingResult();
            if ( waitForResponse ) {
                PENDING_RESULTS.put( requestID, pendingResult );
            }
            
            submitRemoteRequest( jsonRequest );
            
            if ( waitForResponse ) {
                listenForRemoteMessages();
                
                synchronized( pendingResult ) {
                    pendingResult.wait();
                }
                
                PENDING_RESULTS.remove( requestID );
                
                final RuntimeException remoteException = pendingResult.getRemoteException();
                if ( remoteException == null ) {
                    return pendingResult.getValue();
                }
                else {
                    throw new RemoteMessageException( "Exception thrown during execution of the remote request on the remote service.", remoteException );
                }
            }
            else {
                return null;
            }
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



/** pending result */
class PendingResult {
    /** result value */
    private Object _value;
    
    /** remote exception */
    private RuntimeException _remoteException;
    
    
    /** set the result's value */
    public void setValue( final Object value ) {
        _value = value;
    }
    
    
    /** get the result's value */
    public Object getValue() {
        return _value;
    }
    
    
    /** set the error message */
    public void setRemoteException( final RuntimeException exception ) {
        _remoteException = exception;
    }
    
    
    /** get the error message */
    public RuntimeException getRemoteException() {
        return _remoteException;
    }
}
