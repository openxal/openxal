/*
 * JcaSystem.java
 *
 * Created on August 27, 2002, 2:38 PM
 */

package xal.jca;

import xal.ca.ChannelSystem;

import gov.aps.jca.JCALibrary;
import gov.aps.jca.Context;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import java.util.prefs.Preferences;


/**
 * JcaSystem is a wrapper for public static methods of JcaChannel that are common to Channel.  It provides a means of calling those methods without 
 * knowing (at a high level) the subclass of Channel.  This is necessary since static abstract methods don't exist in Java.
 * @author  tap
 */
class JcaSystem extends ChannelSystem {
	protected Context _jcaContext;
	protected JCALibrary _library;
	
	
    /** Constructor */
    public JcaSystem() {
		this( null );
    }
	
	
    /** 
	 * Creates a new instance of JcaSystem 
	 * @param contextName one of "com.cosylab.epics.caj.CAJContext", "gov.aps.jca.jni.ThreadSafeContext" or "gov.aps.jca.jni.SingleThreadedContext"
	 */
    public JcaSystem( final String contextName ) {
		try {
			_library = JCALibrary.getInstance();
			
			final String contextType = ( contextName != null ) ? contextName : defaultJCAContextType();
			_jcaContext = _library.createContext( contextType );
		}
		catch(CAException exception) {
			exception.printStackTrace();
		}
    }
	
	
	/**
	 * Determine the user's preferred JCA Context otherwise defaulting to JCALibrary.CHANNEL_ACCESS_JAVA 
	 * @return the string identifying the JCA Context to use
	 */
	private String defaultJCAContextType() {
		final String userJCAContext = fetchUserJCAContext();
		return getJCAContextType( userJCAContext );
	}
	
	
	/** Determine the user's preferred JCA Context first checking for a Java property, then an environment variable and finally a user preference */
	private String fetchUserJCAContext() {
		// This try should not be required, but is added as a work around for some strange Matlab behavior (jdg, 1/05/05)
		try {
			// first check if the user has set a command line property
			final String contextProperty = System.getProperty( "xal.jca.Context" );
			if ( contextProperty != null ) {
				return contextProperty;
			}
			else {
				// check whether the user has set an environment variable
				final String contextEnvironment = System.getenv( "JCA_CONTEXT" );
				if ( contextEnvironment != null ) {
					return contextEnvironment;
				}
				else {
					// check the user's preferences
					final Preferences prefs = Preferences.userNodeForPackage( this.getClass() );
					final String preferredContext = prefs.get( "Context", "" );
					return preferredContext;
				}
			}
		}
		catch ( Exception exception ) {
			// check if the user has specified a JCA Context to use
			return System.getProperty( "xal.jca.Context" );
		}		
	}
	
	
	/**
	 * Get the context type given the user specified context type.
	 * @userContextType the user specified context type
	 * @return the user context type if not null or empty and JCALibrary.CHANNEL_ACCESS_JAVA otherwise
	 */
	private static String getJCAContextType( final String userContextType ) {
		return ( ( userContextType != null ) && ( userContextType.length() > 0 ) ) ? userContextType : JCALibrary.CHANNEL_ACCESS_JAVA;
	}
	
	
	/**
	 * Initialize the channel system
	 * @return true if the initialization was successful and false if not
	 */
	public boolean init() {
		try {
			// since Context.initialize() only can be called once and we have no way of knowing if
			// it has already been called, run testIO() as a way to safely induce initialization
			_jcaContext.testIO();
			return true;
		}
		catch(CAException exception) {
			System.err.println(exception);
			return false;
		}
	}
	
	
	/**
	 * Get the internal JCA context
	 * @return the JCA context.
	 */
	Context getJcaContext() {
		return _jcaContext;
	}
    
    
	/**
	 * Set the debug mode.
	 * @param debugFlag true for debug mode and false otherwise.
	 */
    synchronized public void setDebugMode( final boolean debugFlag ) {
        JcaChannel.setDebugMode( debugFlag );
    }
	
	
	/** Flush the IO buffers */
	public void flushIO() {
		try {
			_jcaContext.flushIO();
		}
		catch ( CAException exception ) {
			throw new RuntimeException( "Exception flushing IO requests.", exception );
		}
	}
    
    
	/**
	 * Process IO requests within the specified timeout.
	 * @param timeout The length of time in seconds we are willing to wait to process the requests.
	 * @return true if successful and false if unsuccessful.
	 */
    public boolean pendIO( final double timeout ) {
        try { 
            _jcaContext.pendIO( timeout );
        } 
        catch ( CAException exception ) { 
            return false; 
        }
		catch ( TimeoutException exception ) {
			return false;
		}
        
        return true;
    }
    
    
	/**
	 * Process all events within the specified timeout.
	 * @param timeout The length of time in seconds we are willing to wait to process the requests.
	 */
    public void pendEvent( final double timeout ) {
		try {
			_jcaContext.pendEvent( timeout );
		}
		catch( CAException exception ) {
			System.err.println( exception );
		}
    }
}
