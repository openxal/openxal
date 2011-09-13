/*
 * AsynchronousMessageHandler.java
 *
 * Created on February 5, 2002, 1:55 PM
 */

package xal.tools.messaging;


import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;


/**
 * AsynchronousMessageHandler
 * Handle dispatch messages in an asynchronous way so that control is
 * returned to the sender immediately without waiting for recipients
 * to receive their messages.
 * Note that multiple recipients may also be notified concurrently.
 * @author  tap
 */
class AsynchronousMessageHandler<T> extends MessageHandler<T> implements java.io.Serializable {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    

    /** Creates new AsynchronousMessageHandler */
    public AsynchronousMessageHandler( final TargetDirectory directory, final Class<T> newInterface, final int threadPoolSize ) {
        this( directory, null, newInterface, threadPoolSize );
    }
    

    /** Creates new AsynchronousMessageHandler */
    public AsynchronousMessageHandler( final TargetDirectory directory, final Object source, final Class<T> newInterface, final int threadPoolSize ) {
        super( directory, source, newInterface, threadPoolSize );
    }
    
    
    /** 
	 * Implement InvocationHandler interface to invoke the specified method with the supplied arguments
	 * @param proxy merely provides identification and is not used here
	 * @param method method to invoke on the targets
	 * @param args arguments supplied to the method
	 */
	public Object invoke( final Object proxy, final Method method, final Object[] args ) {
        method.setAccessible( true );     // allow access to private, protected, default access methods
        Invoker invoker = new Invoker( method, args );
        Thread thread = new Thread( invoker );
        
        thread.start();     // execute the invocation asynchronously
        
        return null;
    }

    
    /**
     * identifies the message handler as asynchronous
     * overrides the abstract version
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
            catch(Exception exception) {
				final String message = "Error invoking method: " + method + " for protocol " + _protocol + " for source " + source;
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
                System.err.println( message );
                exception.printStackTrace();
            }
        }
    }
}



