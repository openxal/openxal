/*
 * Selector.java
 *
 * Created on July 17, 2002, 11:17 AM
 */

package xal.tools.reflect;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * Selector is a convenience class that makes much of the Method class easier
 * to use.  Unlike Method, a Selector instance is not associated with a 
 * particular class.  Binding happens upon invocation so the same selector can
 * be used with many classes that do not necessarily share the same interface 
 * or superclasses.
 * Also, Selector has convenience methods that allow fewer lines to be invoked.
 * All exceptions are Runtime exceptions thus freeing the developer from having
 * to deal with exceptions during rapid development.
 * This class is intended to be used to handle group actions from a node 
 * perspective rather than a channel perspective.  It can also be useful in 
 * creating powerful GUIs that allow users to script XAL.
 *
 * @author  tap
 */
public class Selector {
	/** name of the method */
    protected String _methodName;

	/** argument classes */
    protected Class<?>[] _argumentTypes;

    
    /** Constructor for a method that takes multiple arguments */
	@SuppressWarnings( "rawtypes" )		// cannot mix generics with arrays
    public Selector( final String methodName, final Class<?> ... argumentTypes ) {
        _methodName = methodName;
        int argumentCount = argumentTypes.length;
        if ( argumentTypes.length > 0 ) {
            _argumentTypes = new Class[argumentCount];
            System.arraycopy( argumentTypes, 0, _argumentTypes, 0, argumentCount );
        }
        else {
            _argumentTypes = new Class[0];
        }
    }
    
    
    /** Return the base name of the method */
    public String methodName() {
        return _methodName;
    }
    
    
    /** Test if the target will respond to the selector */
    public boolean invokesOn( final Object target ) {
        try {
            methodForObject(target);
            return true;
        }
        catch(Exception excpt) {
            return false;
        }
    }
    
    
    /** Test if the target will respond to the selector */
    public <TargetType> boolean invokesStaticOn( final Class<TargetType> targetClass ) {
        try {
            Method method = methodForClass( targetClass );
            return Modifier.isStatic( method.getModifiers() );
        }
        catch(Exception excpt) {
            return false;
        }
    }
    
    
    /** Invoke a no argument method on the target */
    public Object invoke(Object target)
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        return invoke(target, new Object[0]);
    }
    
    
    /** Invoke a single argument method on the target.  The single argument
     *  should not be an array.  If you need a single argument that is an array,
     *  you should use the multi-argument invoke method to avoid ambiguity.
     */
    public Object invoke(Object target, Object argument)
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        return invoke(target, new Object[]{argument});
    }
    
    
    /**
     * Invoke a mulit-argument method on the target.
     */
    public Object invoke(Object target, Object[] arguments)
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Method method = methodForObject(target);
        Object result = null;

        try {
            result = method.invoke(target, arguments);
        }
        catch(IllegalAccessException excpt) {
            throw new AccessException(excpt);
        }
        catch(InvocationTargetException excpt) {
            throw new InvocationException(excpt);
        }
        
        return result;
    }
    
    
    /** Invoke the appropriate static method on the specified class */
    public <TargetType> Object invokeStatic( final Class<TargetType> targetClass ) throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {        
        return invokeStatic( targetClass, new Object[0] );
    }
    
    
    /** Invoke the appropriate static method on the specified class */
    public <TargetType> Object invokeStatic( final Class<TargetType> targetClass, final Object argument ) throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {        
        return invokeStatic( targetClass, new Object[]{argument} );
    }
    
    
    /** Invoke the appropriate static method on the specified class */
    public <TargetType> Object invokeStatic( final Class<TargetType> targetClass, final Object[] arguments ) throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Method method = methodForClass( targetClass );
        Object result = null;
        
        try {
            result = method.invoke( null, arguments );
        }
        catch(IllegalAccessException excpt) {
            throw new AccessException(excpt);
        }
        catch(InvocationTargetException excpt) {
            throw new InvocationException(excpt);
        }
        
        return result;
    }
    
    
    /**
     * Invoke a no argument method on the target.  This is a convenience static
     * method that creates an internal Selector on the fly.
     */
    static public Object invokeMethod(String methodName, Object target)
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Selector selector = new Selector(methodName);
        return selector.invoke(target);
    }
    
    
    /**
     * Invoke a single argument method on the target.  This is a convenience static
     * method that creates an internal Selector on the fly.
     */
    static public Object invokeMethod(String methodName, Class<?> argumentType, Object target, Object argument)
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Selector selector = new Selector(methodName, argumentType);
        return selector.invoke(target, argument);
    }
    
    
    /**
     * Invoke a multi-argument method on the target.  This is a convenience static
     * method that creates an internal Selector on the fly.
     */
    static public Object invokeMethod( final String methodName, final Class<?>[] argumentTypes, final Object target, final Object[] arguments )
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Selector selector = new Selector(methodName, argumentTypes);
        return selector.invoke(target, arguments);
    }
    
    
    /** Invoke the static method with the specified arguments on the specified target class */
    static public <TargetType> Object invokeStaticMethod( final String methodName, final Class<?>[] argumentTypes, final Class<TargetType> targetClass, final Object[] arguments )
    throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        Selector selector = new Selector(methodName, argumentTypes);
        return selector.invokeStatic( targetClass, arguments );
    }
    
    
    /** Invoke the static method with the specified arguments on the specified target class */
    static public <TargetType> Object invokeStaticMethod( final String methodName, final Class<?> argumentType, final Class<TargetType> targetClass, final Object argument ) throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        final Selector selector = new Selector( methodName, argumentType );
        return selector.invokeStatic( targetClass, argument );
    }
    
    
    /** Invoke the static method with the specified arguments on the specified target class */
    static public <TargetType> Object invokeStaticMethod( final String methodName, final Class<TargetType> targetClass ) throws IllegalArgumentException, AccessException, InvocationException, MethodNotFoundException {
        final Selector selector = new Selector( methodName );
        return selector.invokeStatic( targetClass );
    }
    
    
    /** Return a Method instance that binds the selector to a target class. */
    public <TargetType> Method methodForClass( final Class<TargetType> targetClass ) throws MethodNotFoundException, SecurityException {
        Method method;
        
        try {
            method = targetClass.getMethod( _methodName, _argumentTypes );
        }
        catch(NoSuchMethodException except) {
            String message = "Method: \"" + _methodName + "\" with parameters: " +
            Arrays.asList(_argumentTypes) + " not found for class: " + targetClass + ".";
            throw new MethodNotFoundException(message);
        }
        
        return method;
    }
    
    
    /** Return a Method instance that binds the selector to a target object. */
    public Method methodForObject(Object target) throws MethodNotFoundException, SecurityException {
        return methodForClass(target.getClass());
    }
    
    
    
    /**
     * Exception thrown when no method could be found matching a target to 
     * the method name and argument types.
     */
    public class MethodNotFoundException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        
        public MethodNotFoundException(String message) {
            super(message);
        }
    }
    
    
    
    /**
     * Exception that wraps an exception thrown during invocation.
     */
    public class InvocationException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        
        private InvocationTargetException _exception;
        public InvocationException(InvocationTargetException exception) {
            _exception = exception;
        }
        
        public Throwable getTargetException() {
            return _exception.getTargetException();
        }
        
        public void printStackTrace() {
            _exception.printStackTrace();
        }
    }
    
    
    
    /**
     * Exception thrown when an attempt is made to invoke a method on a target 
     * such that the method is inaccessible on that target.
     */
    public class AccessException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;

        
        public AccessException(IllegalAccessException excpt) {
            super( excpt.getMessage() );
        }
    }
}