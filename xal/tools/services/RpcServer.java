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
    /** delimeter for encoding remote messages */
    final static private String REMOTE_MESSAGE_DELIMITER = "#";
    
    /** socket which listens for and dispatches remote requests */
    final private ServerSocket SERVER_SOCKET;
    
    /** set of active sockets serving remote requests */
    final private Set<Socket> REMOTE_SOCKETS;
    
    /** remote request handlers keyed by service name */
    final private Map<String,RemoteRequestHandler<?>> REMOTE_REQUEST_HANDLERS;
    
    
    /** Constructor */
    public RpcServer() throws java.io.IOException {
        REMOTE_REQUEST_HANDLERS = new Hashtable<String,RemoteRequestHandler<?>>();
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
                        final String[] messageParts = decodeRemoteMessage( message );
                        final String serviceName = messageParts[0];
                        final String methodName = messageParts[1];
                        final Number requestID = (Number)request.get( "id" );
                        final List<Object> params = (List<Object>)request.get( "params" );
                        
                        // todo: call the method on the handler
                        final RemoteRequestHandler<?> handler = REMOTE_REQUEST_HANDLERS.get( serviceName );
                        final Object result = handler.evaluateRequest( methodName, params );
                                                
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
    
    
    /** encode the service name and method name into the remote message */
    static String encodeRemoteMessage( final String serviceName, final String methodName ) {
        return serviceName + REMOTE_MESSAGE_DELIMITER + methodName;
    }
    
    
    /** decode the service name and method name from the remote message */
    static String[] decodeRemoteMessage( final String message ) {
        return message.split( REMOTE_MESSAGE_DELIMITER, 2 );
    }
}



/** Handles remote requests */
class RemoteRequestHandler<ProtocolType> {
    /** primitive type wrappers keyed by type */
    final static private Map<Class<?>,Class<?>> PRIMITIVE_TYPE_WRAPPERS;

    /** identifier of the service */
    final private String SERVICE_NAME;
    
    /** protocol of available methods */
    final private Class<ProtocolType> PROTOCOL;
    
    /** object to message */
    final private ProtocolType PROVIDER;
    
    /** cache of methods keyed by their signature */
    final private Map<String,Method> METHOD_CACHE;
        
    
    // static initializer
    static {
        PRIMITIVE_TYPE_WRAPPERS = populatePrimitiveTypeWrappers();
    }
    
    
    /** Constructor */
    public RemoteRequestHandler( final String serviceName, final Class<ProtocolType> protocol, final ProtocolType provider ) {
        SERVICE_NAME = serviceName;
        PROTOCOL = protocol;
        PROVIDER = provider;
        METHOD_CACHE = new Hashtable<String,Method>();
    }
    
    
    /** populate the table of primitive type wrappers */
    private static Map<Class<?>,Class<?>> populatePrimitiveTypeWrappers() {
        final Map<Class<?>,Class<?>> table = new Hashtable<Class<?>,Class<?>>();
        
        table.put( Integer.TYPE, Integer.class );
        table.put( Long.TYPE, Long.class );
        table.put( Short.TYPE, Short.class );
        table.put( Byte.TYPE, Byte.class );
        table.put( Character.TYPE, Character.class );
        table.put( Float.TYPE, Float.class );
        table.put( Double.TYPE, Double.class );
        table.put( Boolean.TYPE, Boolean.class );
        
        return table;
    }
    
    
    /** Evaluate the request */
    public Object evaluateRequest( final String methodName, final List<Object> params ) {
        final Object[] methodParams = new Object[ params.size() ];
        final Class<?>[] methodParamTypes = new Class<?>[ methodParams.length ];
        for ( int index = 0 ; index < methodParams.length ; index++ ) {
            final Object param = params.get( index );
            methodParams[index] = param;
            methodParamTypes[index] = param != null ? param.getClass() : null;
        }
                
        final Method method = getMethod( methodName, methodParamTypes );
        
        try {
            return method.invoke( PROVIDER, methodParams );
        }
        catch ( Exception exception ) {
            throw new RuntimeException( "Exception evaluating the remote request with the request handler.", exception );
        }
    }
    
    
    /** Get the method either from the cache or find and cache it if necessary */
    private Method getMethod( final String methodName, final Class<?>[] parameterTypes ) {
        final String methodSignature = getMethodSignature( methodName, parameterTypes );
        
        Method method = METHOD_CACHE.get( methodSignature );
        if ( method == null ) {
            method = findMethod( methodName, parameterTypes );
            METHOD_CACHE.put( methodSignature, method );
        }
        return method;
    }
    
    
    /** Get the method signature for the specified method name and parameter types */
    static private String getMethodSignature( final String methodName, final Class<?>[] parameterTypes ) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append( methodName );
        for ( final Class<?> parameterType : parameterTypes ) {
            final String parameterTypeID = parameterType != null ? parameterType.getName() : "";
            buffer.append( ":" );
            buffer.append( parameterTypeID );
        }
        return buffer.toString();
    }
    
    
    /** Find the best method in the protocol that matches the method name and parameters */
    private Method findMethod( final String methodName, final Class<?>[] parameterTypes ) {
        try {
            return PROTOCOL.getMethod( methodName, parameterTypes );
        }
        catch ( NoSuchMethodException exception ) {
            try {
                final Method[] methods = PROTOCOL.getMethods();
                final List<Method> methodCandidates = new ArrayList<Method>();
                int bestScore = 0;
                Method bestMethod = null;
                for ( final Method method : methods ) {
                    final int score = matchScore( method, methodName, parameterTypes );
                    if ( score > bestScore ) {
                        bestScore = score;
                        bestMethod = method;
                    }
                }
                
                if ( bestMethod != null ) {
                    return bestMethod;
                }
                else {
                    throw new RuntimeException( "No matching method found for <" + methodName + "" + parameterTypes + ">", exception );
                }                                
            }
            catch ( Exception searchException ) {
                throw new RuntimeException( "Exception evaluating the remote request with the request handler.", searchException );
            }
        }
    }
    
    
    /** Score the match between the method and the specified method name and parameter types. Higher scores are better and zero means no match. */
    static private int matchScore( final Method method, final String methodName, final Class<?>[] parameterTypes ) {
        int score = 0;
        
        final Class<?>[] methodParamTypes = method.getParameterTypes();
        
        if ( method.getName().equals( methodName ) && methodParamTypes.length == parameterTypes.length ) {
            score += 1;     // credit for matching the name and parameters length
        }
        else {
            return 0;   // no match
        }
        
        // test each parameter type for consistency
        for ( int index = 0 ; index < methodParamTypes.length ; index++ ) {
            final Class<?> methodParamType = methodParamTypes[index];
            final Class<?> parameterType = parameterTypes[index];
            
            if ( methodParamType.isPrimitive() ) {
                if ( parameterType == null ) {
                    return 0;   // no match since a primitive cannot be null
                }
                else if ( PRIMITIVE_TYPE_WRAPPERS.get( methodParamType ).equals( parameterType ) ) {
                    score += 1;     // primitive type's corresponding wrapper matches parameter type
                }
                else {
                    return 0;       // no match since the primitive must be mapped to its corresponding wrapper class
                }
            }
            else if ( methodParamType.equals( parameterType ) ) {
                score += 2;     // bonus for exact match
            }
            else if ( parameterType.isAssignableFrom( methodParamType ) ) {
                score += 1;     // types are consistent
            }
            else if ( parameterType == null ) {
                // null matches an object type so compatible, but no credit
            }
            else {
                return 0;   // no match for this parameter
            }
        }
        
        return score;
    }
}





