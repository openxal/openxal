/*
 * ClientHandler.java
 *
 * Created on July 18, 2003, 9:54 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;

import xal.tools.coding.*;

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
    /** protocol implemented by the remote service and dispatched through the proxy */
    final private Class<ProxyType> SERVICE_PROTOCOL;

    /** name of the remote service */
    final private String SERVICE_NAME;

    /** proxy which forwards invocations to the remote service */
    final private ProxyType PROXY;

    /** remote host */
    final private String REMOTE_HOST;

    /** remote port */
    final private int REMOTE_PORT;

	/** message processors which are available */
	final private ConcurrentLinkedQueue<SerialRemoteMessageProcessor> MESSAGE_PROCESSORS;

    /** request ID counter is incremented to provide a unique ID for each request */
    private volatile int _requestIDCounter;

    /** coder for encoding and decoding messages for remote transport */
    final private Coder MESSAGE_CODER;


    /**
	 * Creates a new ClientHandler to handle service requests.
	 * @param host  The host where the service is running.
	 * @param port  The port through which the service is provided.
	 * @param name  The name of the service.
	 * @param newProtocol  The interface the service provides.
     * @param messageCoder coder for encoding and decoding messages for remote transport
	 */
    public ClientHandler( final String host, final int port, final String name, final Class<ProxyType> newProtocol, final Coder messageCoder ) {
        REMOTE_HOST = host;
        REMOTE_PORT = port;
        SERVICE_NAME = name;
        SERVICE_PROTOCOL = newProtocol;
        MESSAGE_CODER = messageCoder;

        PROXY = createProxy();

		MESSAGE_PROCESSORS = new ConcurrentLinkedQueue<SerialRemoteMessageProcessor>();

        _requestIDCounter = 0;
    }


    /** Get the next request ID and increment it */
    private int getNextRequestID() {
        return _requestIDCounter++;
    }


    /**
     * Get the interface managed by this handler.
     * @return The interface managed by this handler.
     */
    public Class<?> getProtocol() {
        return SERVICE_PROTOCOL;
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
    @SuppressWarnings( { "unchecked", "rawtypes" } )   // we have not choice but to cast since newProxyInstance does not support generics
    private ProxyType createProxy() {
		ClassLoader loader = this.getClass().getClassLoader();
        Class[] protocols = new Class[] { SERVICE_PROTOCOL, ServiceState.class };

        return (ProxyType)Proxy.newProxyInstance( loader, protocols, this );
    }


    /** dispose of resources */
    public void dispose() {
		final List<SerialRemoteMessageProcessor> processors = new ArrayList<SerialRemoteMessageProcessor>();

		synchronized( MESSAGE_PROCESSORS ) {
			processors.addAll( MESSAGE_PROCESSORS );
			MESSAGE_PROCESSORS.clear();
		}

		for ( final SerialRemoteMessageProcessor processor : processors ) {
			processor.dispose();
		}
    }


    /** dispose of resources upon collection */
    protected void finalize() throws Throwable {
        dispose();
		super.finalize();
    }


	/** get the next remote message processor which is free for processing a fresh message */
	private SerialRemoteMessageProcessor nextRemoteMessageProcessor() {
		SerialRemoteMessageProcessor processor = null;

		synchronized( MESSAGE_PROCESSORS ) {
			processor = MESSAGE_PROCESSORS.poll();
		}

		if ( processor != null ) {
			return processor;
		}
		else {
			return new SerialRemoteMessageProcessor( REMOTE_HOST, REMOTE_PORT, MESSAGE_CODER );
		}
	}


	/** recycle a message processor which is no longer in use */
	private void recycleRemoteMessageProcessor( final SerialRemoteMessageProcessor processor ) {
		synchronized( MESSAGE_PROCESSORS ) {
			MESSAGE_PROCESSORS.add( processor );
		}
	}


    /**
     * Invoke the specified method on the proxy to implement the InvocationHandler interface.
     * The method is evaluated by calling the remote method using JSON-RPC.
     * @param proxy The instance on which the method is invoked.  This argument is unused.
     * @param method The method to implement.
     * @param args The array of arguments to pass to the method.
     * @return The result of the method invokation.
	 * @throws xal.extension.service.RemoteMessageException if an exception occurs while invoking this remote message.
     */
    @SuppressWarnings( "unchecked" )    // must cast generic response object to Map
	public Object invoke( final Object proxy, final Method method, final Object[] args ) throws RemoteMessageException, RemoteServiceDroppedException {
		try {
			SERVICE_PROTOCOL.getMethod( method.getName(), method.getParameterTypes() );		// test whether the remote service implements the method
			return performRemoteServiceCall( method, args );
		}
		catch( NoSuchMethodException exception ) {
			return performServiceStateCall( method, args );
		}
    }


	/** perform the remote service call */
	private Object performRemoteServiceCall( final Method method, final Object[] args ) throws RemoteMessageException, RemoteServiceDroppedException {
        try {
            final long requestID = getNextRequestID();
			final Object[] params = args != null ? args : new Object[0];

            final String methodName = method.getName();
            final Map<String,Object> request = new HashMap<String,Object>();
            final String message = RpcServer.encodeRemoteMessage( SERVICE_NAME, methodName );
            request.put( "message", message );
            request.put( "params", params );
            request.put( "id", requestID );
            final String jsonRequest = MESSAGE_CODER.encode( request );

            // methods marked with the OneWay annotation return immediately and do not wait for a response from the service
            final boolean waitForResponse = !method.isAnnotationPresent( OneWay.class );

            // submit the request and wait for the response if expected
			final SerialRemoteMessageProcessor processor = nextRemoteMessageProcessor();	// get the next available processor from the stack
            final PendingResult pendingResult = processor.submitRemoteRequest( jsonRequest, waitForResponse );
			if ( !processor.isClosed() )  recycleRemoteMessageProcessor( processor );		// push the processor back onto the stack if it is still viable

            if ( pendingResult != null ) {
                final RuntimeException remoteException = pendingResult.getRemoteException();
                if ( remoteException == null ) {
                    return pendingResult.getValue();
                }
                else {
					if ( remoteException instanceof RemoteServiceDroppedException ) {
						throw remoteException;		// just rethrow it since it is a service connection issue and not an issue generated by the remote service itself
					}
					else {
						throw new RemoteMessageException( "Exception thrown during execution of the remote request on the remote service.", remoteException );
					}
                }
            }
            else {
                return null;
            }
        }
        catch ( IllegalArgumentException exception ) {
            throw exception;
        }
		catch ( RemoteMessageException exception ) {
			throw exception;
		}
		catch ( RemoteServiceDroppedException exception ) {
			throw exception;
		}
        catch ( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception performing invocation for remote request.", exception );
        }
	}


	/** perform the service state call on the local client handler */
	private Object performServiceStateCall( final Method method, final Object[] args ) {
		try {
			return method.invoke( newServiceState(), args );
		}
		catch( Exception exception ) {
            exception.printStackTrace();
            throw new RuntimeException( "Exception performing local service state call on proxy to remote.", exception );
		}
	}


	/** Create new service state instance to implement the service state interface */
	private ServiceState newServiceState() {
		return new ServiceState() {
			/**
			 * Get the name of the remote service.
			 * @return The name of the remote service.
			 */
			public String getServiceName() {
				return ClientHandler.this.getServiceName();
			}

			
			/**
			 * Get the host name of the remote service.
			 * @return The host name of the remote service.
			 */
			public String getServiceHost() {
				return ClientHandler.this.getHost();
			}


			/**
			 * Get the port of the remote service.
			 * @return The port of the remote service.
			 */
			public int getServicePort() {
				return ClientHandler.this.getPort();
			}


			/** dispose of this proxy's resources */
			public void disposeServiceResources() {
				ClientHandler.this.dispose();
			}
		};
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



/** Remote message processor that can handle serial (noncurrent) requests over the same socket. */
class SerialRemoteMessageProcessor {
    /** socket for sending and receiving remote messages */
    final private Socket REMOTE_SOCKET;

    /** coder for encoding and decoding messages for remote transport */
    final private Coder MESSAGE_CODER;


    /**
	 * Creates a new ClientHandler to handle service requests.
	 * @param host  The host where the service is running.
	 * @param port  The port through which the service is provided.
     * @param messageCoder coder for encoding and decoding messages for remote transport
	 */
    public SerialRemoteMessageProcessor( final String host, final int port, final Coder messageCoder ) {
        MESSAGE_CODER = messageCoder;

        REMOTE_SOCKET = makeRemoteSocket( host, port );

		try {
			WebSocketIO.performHandshake( REMOTE_SOCKET );
		}
		catch ( Exception exception ) {
			throw new RuntimeException( "Exception creating new remote socket.", exception );
		}
    }


    /** make a new remote socket */
    static private Socket makeRemoteSocket( final String host, final int port ) {
        try {
            final Socket remoteSocket = new Socket( host, port );
            remoteSocket.setKeepAlive( true );
            return remoteSocket;
        }
        catch( UnknownHostException exception ) {
            throw new RemoteServiceDroppedException( "Attempt to open a socket to an unknown host.", exception );
        }
        catch( IOException exception ) {
            throw new RemoteServiceDroppedException( "IO Exception attempting to open a new socket.", exception );
        }
		catch( Exception exception ) {
			throw new RemoteServiceDroppedException( "Exceptiong attempting to establish a new remote socket.", exception );
		}
    }


	/** determine whether the socket is closed */
	public boolean isClosed() {
		return REMOTE_SOCKET.isClosed();
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
    protected void finalize() throws Throwable {
		try {
			dispose();
		}
		finally {
			super.finalize();
		}
    }


    /** process the remote response */
    @SuppressWarnings( "unchecked" )    // no way to know response Object type at compile time
    private void processRemoteResponse( final PendingResult pendingResult ) throws java.net.SocketException, java.io.IOException {
		try {
			final String jsonResponse = WebSocketIO.readMessage( REMOTE_SOCKET );
			if ( jsonResponse != null ) {
				final Object responseObject = MESSAGE_CODER.decode( jsonResponse );
				if ( responseObject instanceof Map ) {
					final Map<String,Object> response = (Map<String,Object>)responseObject;
					final Object result = response.get( "result" );
					final RuntimeException remoteException = (RuntimeException)response.get( "error" );

					pendingResult.setValue( result );
					pendingResult.setRemoteException( remoteException );
				}
			}
		}
		catch( WebSocketIO.SocketPrematurelyClosedException exception ) {
			cleanupClosedSocket( pendingResult, new RemoteServiceDroppedException( "The remote socket has closed while reading the remote response..." ) );
		}
    }


	/** cleanup after discovering the socket has closed */
	private void cleanupClosedSocket( final PendingResult pendingResult, final Exception exception ) {
		// encapsulate the exception in a runtime exception if necessary since that is what gets passed back to the calling method
		final RuntimeException resultException = exception instanceof RuntimeException ? (RuntimeException)exception : new RuntimeException( exception );

		// assign the exception to the pending result
		if ( pendingResult != null ) {
			pendingResult.setRemoteException( resultException );
		}
	}


    /** Submit the remote request */
    public PendingResult submitRemoteRequest( final String jsonRequest, final boolean hasResponse ) {
		try {
			WebSocketIO.sendMessage( REMOTE_SOCKET, jsonRequest );

			if ( hasResponse ) {
				final PendingResult pendingResult = new PendingResult();

				try {
					processRemoteResponse( pendingResult );
					return pendingResult;
				}
				catch( SocketException exception ) {
					return pendingResult;	// no need to flood output when we expect socket exceptions when remote services drop
				}
				catch( Exception exception ) {
					exception.printStackTrace();
					return pendingResult;
				}
				finally {
					// if the socket closes, cleanup the connection resources and forward the exception to the client
					if ( REMOTE_SOCKET.isClosed() ) {
						cleanupClosedSocket( pendingResult, new RemoteServiceDroppedException( "The remote socket has closed while processing the remote response..." ) );
					}
				}
			}
			else {
				return null;
			}
		}
		catch( SocketException exception ) {
			if ( !REMOTE_SOCKET.isClosed() ) {
				try {
					REMOTE_SOCKET.close();
				}
				catch( Exception closeException ) {}
			}
			if ( hasResponse ) {
				final PendingResult pendingResult = new PendingResult();
				cleanupClosedSocket( new PendingResult(), new RemoteServiceDroppedException( "The remote socket has closed while processing the remote response..." ) );
				return pendingResult;
			}
			else {
				return null;
			}
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
    }
}
