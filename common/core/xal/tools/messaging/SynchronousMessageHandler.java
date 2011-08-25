/*
 * SynchronousMessageHandler.java
 *
 * Created on February 5, 2002, 1:54 PM
 */

package xal.tools.messaging;


import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;


/**
 * SynchronousMessageHandler
 * Handle dispatch messages in a synchronous way so that control is
 * returned to the sender only when all recipients have been notified.
 * Note that multiple recipients may be notified concurrently, however.
 * @author  tap
 */
class SynchronousMessageHandler<T> extends MessageHandler<T> implements java.io.Serializable {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /** Creates new SynchronousMessageHandler */
    public SynchronousMessageHandler( final TargetDirectory directory, final Class<T> newInterface, final int threadPoolSize ) {
        this( directory, null, newInterface, threadPoolSize );
    }
    

    /** Creates new SynchronousMessageHandler */
    public SynchronousMessageHandler( final TargetDirectory directory, final Object source, final Class<T> newInterface, final int threadPoolSize ) {
        super( directory, source, newInterface, threadPoolSize );
    }
    

    /** implement InvocationHandler interface to invoke the specified method with the given arguments */
	public Object invoke( final Object proxy, final Method method, final Object[] args ) throws IllegalArgumentException {
        try {
            method.setAccessible( true );     // allow access to private, protected, default access methods
			for( final Object target : targets() ) {
				method.invoke( target, args );
			}
        }
        catch( InvocationTargetException exception ) {
			final String message = "Error invoking method: " + method + " for protocol " + _protocol + " for source " + source;
			Logger.getLogger("global").log( Level.SEVERE, message, exception );
            System.err.println( message );
            exception.printStackTrace();
        }
        catch(IllegalAccessException exception) {
            System.err.println(exception);
        }
        catch(IllegalArgumentException exception) {
            throw exception;
        }
        
        return null;
    }
    

    /**
     * identifies the message handler as synchronous
     * overrides the abstract version
     */
    public boolean isSynchronous() {
        return true;
    }
}
