//
//  FreshMessageHandler.java
//  xal
//
//  Created by Tom Pelaia on 5/22/08.
//  Copyright 2008 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.messaging;

import xal.tools.FreshProcessor;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;


/** Asynchronous Message Handler which posts only the most recent pending event and drops earlier ones. */
class FreshMessageHandler<T> extends MessageHandler<T> implements java.io.Serializable {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** event processor which processes the most recent pending event on of a single thread */
	final private FreshProcessor EVENT_PROCESSOR;
	
	
    /** Creates new AsynchronousMessageHandler */
    public FreshMessageHandler( final TargetDirectory directory, final Class<T> newInterface, final int threadPoolSize ) {
        this( directory, null, newInterface, threadPoolSize );
    }
    
	
    /** Creates new AsynchronousMessageHandler */
    public FreshMessageHandler( final TargetDirectory directory, final Object source, final Class<T> newInterface, final int threadPoolSize ) {
        super( directory, source, newInterface, threadPoolSize );
		
		EVENT_PROCESSOR = new FreshProcessor();
    }
	
	
	/** Subclasses should override this method to perform any cleanup prior to removal. */
	public void terminate() {
		EVENT_PROCESSOR.terminate();
	}
	
    
    /** 
	 * Implement InvocationHandler interface to invoke the specified method with the supplied arguments
	 * @param proxy merely provides identification and is not used here
	 * @param method method to invoke on the targets
	 * @param args arguments supplied to the method
	 */
	public Object invoke( final Object proxy, final Method method, final Object[] args ) {
        method.setAccessible( true );     // allow access to private, protected, default access methods
        final Invoker invoker = new Invoker( method, args );
        EVENT_PROCESSOR.post( invoker );
        
        return null;
    }
	
    
    /**
     * identifies the message handler as asynchronous
	 * @return false since this handler is asynchronous
     */
    final public boolean isSynchronous() {
        return false;
    }
    
    
    /** Helper class for executing the invoke method in a thread */
    private class Invoker implements Runnable {
        final private Method method;
        final private Object[] args;
        
		/** Constructor */
        public Invoker( final Method newMethod, final Object[] newArgs ) {
            method = newMethod;
            args = newArgs;
        }
		
		
		/** forward messages to the targets */
        public void run() {
            try {
				for( final Object target : targets() ) {
                    method.invoke( target, args );
                }
            }
            catch( Exception exception ) {
				final String message = "Error invoking method: " + method + " for protocol " + _protocol + " for source " + source;
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
                System.err.println( message );
                exception.printStackTrace();
            }
        }
    }	
}
