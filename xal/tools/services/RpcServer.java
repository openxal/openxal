/*
 * RpcServer.java
 *
 * Created on July 18, 2003, 10:23 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.services;

import xal.tools.json.*;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;


/**
 * RpcServer implements a server which handles remote requests against registered handlers.
 * @author  tap
 */
//public class RpcServer extends WebServer {
public class RpcServer {
    /** socket which listens for and dispatches remote requests */
    final private ServerSocket SERVER_SOCKET;
    
    /** set of active sockets serving remote requests */
    final private Set<Socket> REMOTE_SOCKETS;
    
    /** remote request handlers keyed by service name */
    final private Map<String,RemoteRequestHandler<?>> REMOTE_REQUEST_HANDLERS;
    
    
    /** Constructor */
    public RpcServer() throws java.io.IOException {
        REMOTE_REQUEST_HANDLERS = new HashMap<String,RemoteRequestHandler<?>>();
        SERVER_SOCKET = new ServerSocket( 0 );
        REMOTE_SOCKETS = new HashSet<Socket>();
    }
    
    
    /**
     * Get the port used by the web server.
	 * @return The port used by the web server.
     */
    public int getPort() {
        return SERVER_SOCKET.getLocalPort();
    }
    
    
    /**
     * Get the host address used for the web server.
	 * @return The host address used for the web server.
     */
    public String getHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException exception) {
			final String message = "Error getting the host name of the RPC Server.";
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
            System.err.println(exception);
            return null;
        }
    }
    
    
    /** start the server, listen for remote requests and dispatch them to the appropriate handlers */
    public void start() {
        new Thread( new Runnable() {
            public void run() {
                try {
                    while ( true ) {
                        final Socket remoteSocket = SERVER_SOCKET.accept();
                        REMOTE_SOCKETS.add( remoteSocket );
                        processRemoteEvents( remoteSocket );
                    }
                }
                catch ( SocketException exception ) {
                    // server being shutdown
                }
                catch ( IOException exception ) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }
    
    
    /** shutdown the server */
    public void shutdown() throws IOException {
        SERVER_SOCKET.close();
        
        for ( final Socket socket : REMOTE_SOCKETS ) {
            try {
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();
            }
            catch( Exception exception ) {
                exception.printStackTrace();
            }
        }
        
        for ( final Socket socket : REMOTE_SOCKETS ) {
            REMOTE_SOCKETS.remove( socket );
        }
    }
    
    
    public <ProtocolType> void addHandler( final String serviceName, final Class<ProtocolType> protocol, final ProtocolType provider ) {
        final RemoteRequestHandler<ProtocolType> handler = new RemoteRequestHandler<ProtocolType>( serviceName, protocol, provider );
        REMOTE_REQUEST_HANDLERS.put( serviceName, handler );
    }
    
    
    public void removeHandler( final String serviceName ) {
    }
    
    
    /** process remote socket events */
    @SuppressWarnings( "unchecked" )    // need to cast generic request object to Map
    private void processRemoteEvents( final Socket remoteSocket ) {
        new Thread( new Runnable() {
            public void run() {
                try {
                    final int BUFFER_SIZE = 4096;
                    final char[] streamBuffer = new char[BUFFER_SIZE];
                    final BufferedReader reader = new BufferedReader( new InputStreamReader( remoteSocket.getInputStream() ) );
                    final PrintWriter output = new PrintWriter( remoteSocket.getOutputStream() );
                                        
                    final StringBuilder inputBuffer = new StringBuilder();
                    do {
                        final int readCount = reader.read( streamBuffer, 0, BUFFER_SIZE );
                        
                        if ( readCount == -1 ) {     // the session has been closed
                            return;
                        }
                        else {
                            inputBuffer.append( streamBuffer, 0, readCount );
                        }
                    } while( reader.ready() );
                    
                    final String jsonRequest = inputBuffer.toString();
                                                                
                    final Object requestObject = JSONCoder.decode( jsonRequest );
                    if ( requestObject instanceof Map ) {
                        final Map<String,Object> request = (Map<String,Object>)requestObject;
                        final String message = (String)request.get( "message" );
                        final String serviceName = (String)request.get( "service" );    // this is a deviation from JSON-RPC spec
                        final Number requestID = (Number)request.get( "id" );
                        final List<Object> params = (List<Object>)request.get( "params" );
                        
                        // todo: call the method on the handler
                        final RemoteRequestHandler<?> handler = REMOTE_REQUEST_HANDLERS.get( serviceName );
                        final Object result = handler.evaluateRequest( message, params );
                                                
                        final Map<String,Object> response = new HashMap<String,Object>();
                        response.put( "result", result );
                        response.put( "error", null );
                        response.put( "id", requestID );
                        final String jsonResponse = JSONCoder.encode( response );
                        output.print( jsonResponse );
                        output.flush();
                    }
                }
                catch ( Exception exception ) {
                    exception.printStackTrace();
                }
            }
        }).start();
    }
}



/** Handles remote requests */
class RemoteRequestHandler<ProtocolType> {
    /** identifier of the service */
    final String SERVICE_NAME;
    
    /** protocol of available methods */
    final Class<ProtocolType> PROTOCOL;
    
    /** object to message */
    final ProtocolType PROVIDER;
    
    
    /** Constructor */
    public RemoteRequestHandler( final String serviceName, final Class<ProtocolType> protocol, final ProtocolType provider ) {
        SERVICE_NAME = serviceName;
        PROTOCOL = protocol;
        PROVIDER = provider;
    }
    
    
    /** Evaluate the request */
    public Object evaluateRequest( final String methodName, final List<Object> params ) {
        final Object[] methodParams = new Object[ params.size() ];
        final Class<?>[] methodParamTypes = new Class<?>[ methodParams.length ];
        for ( int index = 0 ; index < methodParams.length ; index++ ) {
            final Object param = params.get( index );
            methodParams[index] = param;
            methodParamTypes[index] = param != null ? param.getClass() : Object.class;
        }
        
        try {
            final Method method = PROTOCOL.getMethod( methodName, methodParamTypes );
            return method.invoke( PROVIDER, methodParams );
        }
        catch ( NoSuchMethodException exception ) {
            throw new RuntimeException( "No matching method found for <" + methodName + "" + params + ">", exception );
        }
        catch ( Exception exception ) {
            throw new RuntimeException( "Exception evaluating the remote request with the request handler.", exception );
        }
    }
}





