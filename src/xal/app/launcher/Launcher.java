//
//  Launcher.java
//  xal
//
//  Created by Thomas Pelaia on 9/8/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.messaging.MessageCenter;
import xal.tools.data.*;

import java.util.*;
import java.io.*;
import java.net.URL;


/** Handles launching of an application */
public class Launcher implements DataListener {
	/** the data adaptor label used for reading and writing this instance */
	static public final String DATA_LABEL = "Launcher";
	
	/** generator of the hosts */
	protected HostGenerator _hostGenerator;
	
	/** Proxy for posting messages to the launch board */
	protected final LaunchBoardListener MESSAGE_BOARD_PROXY;
	
	/** environment used to launch applications */
	private final Map<String,String> APP_ENVIRONMENT;
	
	/** host configuration */
	protected HostConfiguration _hostConfiguration;
	
	
	/** 
	 * Constructor 
	 * @param hostGenerator the generator which selects the host for each run
	 */
	public Launcher( final HostGenerator hostGenerator ) {
		MESSAGE_BOARD_PROXY = MessageCenter.defaultCenter().registerSource( this, LaunchBoardListener.class );
		
		_hostConfiguration = new HostConfiguration();
		setHostGenerator( hostGenerator );
		
		APP_ENVIRONMENT = determineEnvironment();
	}
	
	
	/** Constructor */
	public Launcher() {
		this( null );
	}
	
	
	/** 
	 * Determine the Class Path to use for application environments.
	 * 
	 * In order ot make sure Jython/JRuby scripts use the same OpenXAL version, we
	 * simply force the xal-shared library to be first in the CLASSPATH variable.
	 * 
	 * This means if a user already has xal-shared.jar in his CLASSPATH it will now
	 * show up twice, but that should not cause any harm.
	 */
	private Map<String,String> determineEnvironment() {
		try {
			// grab the current environment
			final Map<String,String> environment = new HashMap<String,String>( System.getenv() );

			// check whether the current environment has a valid CLASSPATH to the XAL core and lib jars and if so don't modify the environment
			final String currentClassPath = environment.get( "CLASSPATH" );

			// URL to xal-shared.jar (or whichever jar holds xal.tools.messaging.MessageCenter)
			final URL xalLibJar = MessageCenter.class.getProtectionDomain().getCodeSource().getLocation();
			final File xalLibJarFile = new File( xalLibJar.toURI() );

			final String newClassPath;
			if ( currentClassPath == null ) newClassPath = xalLibJarFile.getAbsolutePath();
			else newClassPath = xalLibJarFile.getAbsolutePath() + File.pathSeparator + currentClassPath;
			environment.put( "CLASSPATH", newClassPath );

			System.out.println( "CLASSPATH: " + environment.get( "CLASSPATH" ) );

			return environment;
			}
		catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Get the host configuration
	 * @return the host configuration
	 */
	public HostConfiguration getHostConfiguration() {
		return _hostConfiguration;
	}
	
	
	/**
	 * Get the host generator
	 * @return the host generator
	 */
	public HostGenerator getHostGenerator() {
		return _hostGenerator;
	}
	
	
	/**
	 * Set the host generator
	 * @param hostGenerator the new host generator
	 */
	public void setHostGenerator( final HostGenerator hostGenerator ) {
		if ( hostGenerator == null ) {
			setHostGenerator( new RoundRobinHostGenerator( _hostConfiguration.getEnabledHosts() ) );
		}
		else {
			_hostGenerator = hostGenerator;
		}
	}
	
	
	/**
	 * Execute the specified command
	 * @param command the command to execute
	 */
	private Process process( final List<String> commands ) throws java.io.IOException {
		System.out.println( "Executing: " + commands );
		/*
		if ( APP_ENVIRONMENT != null ) {
			System.out.println( "With Environment: " );
			for ( final String variable : APP_ENVIRONMENT ) {
				System.out.println( variable );
			}
		}
		*/
		
		MESSAGE_BOARD_PROXY.postMessage( this, "Executing: " + commands );

		final ProcessBuilder executor = new ProcessBuilder( commands );
		final Map<String,String> environment = executor.environment();
		if ( APP_ENVIRONMENT != null ) {
			environment.putAll( APP_ENVIRONMENT );
		}
		
		final Process process = executor.start();
		
		// We must flush the subprocess's standard out and standard err streams or else the subprocess will hang if either of its stream buffers becomes full.
		// Closing the streams also seems to work, but surprisingly results in much slower IO for the child process.
		BufferFlusher.monitorAndFlushStandardStreams( process );
		
		MESSAGE_BOARD_PROXY.postMessage( this, "Executed:  " + commands );
		System.out.println( "Executed: " + commands );
		
		return process;
	}
	
	
	/**
	 * Launch the specified application
	 * @param application the application to launch
	 */
	public Process launch( final App application ) throws java.io.IOException {		
		final String host = _hostGenerator.nextHost();
		if ( host == null && _hostConfiguration.isHostRequired() ) {
			MESSAGE_BOARD_PROXY.postErrorMessage( this, "Could not connect to any of the specified hosts." );
			return null;
		}

		application.setLastLaunchTime( new Date() );

		final List<String> appCommands = application.getCommands();
		final List<String> commands = _hostConfiguration.getCommands( host, appCommands );
		return process( commands );
	}
 	
	
	/** preconfigure when initializing without a document file */
	public void preConfigure() {
		_hostConfiguration.preConfigure();
	}
	
    
    /** 
	 * provides the name used to identify the class in an external data source.
	 * @return The tag for this data node.
	 */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		final DataAdaptor configurationAdaptor = adaptor.childAdaptor( HostConfiguration.DATA_LABEL );
		_hostConfiguration.update( configurationAdaptor );
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.writeNode( _hostConfiguration );
    }
}



/** BufferFlusher is used to flush the streams of the child processes.  If the streams are not flushed, the child process can freeze when the buffer is size is reached. */
class BufferFlusher implements Runnable {
	/** The stream to flush */
	final private InputStream _stream;
	
	/** The child process */
	final private Process _process;
	
	/** flag specifying whether the child process is still alive */
	private volatile boolean _alive;

	/** true indicates debug flag (xal.debug) set to true and false otherwise */
	final private boolean DEBUG;
	
	
	/**
	 * Constructor
	 */
	public BufferFlusher(final InputStream stream, final Process process) {
		_stream = stream;
		_process = process;

		final String debugFlag = System.getProperty( "xal.debug" );
		DEBUG = debugFlag != null && Boolean.parseBoolean( debugFlag );
	}
	
	
	/**
	 * Create a thread to monitor and flush the process' input streams as needed.
	 * Both standard input and standard error are flushed.
	 * @param process the process whose streams must be flushed
	 */
	static public void monitorAndFlushStandardStreams(final Process process) {
		new Thread( new BufferFlusher(process.getInputStream(), process) ).start();
		new Thread( new BufferFlusher(process.getErrorStream(), process) ).start();
	}
	
	
	/** code to execute */
	public void run() {
		final int BUFFER_SIZE = 1024;
		final byte[] buffer = new byte[BUFFER_SIZE];
		monitorProcessLife();
		while(_alive) {
			try {
				synchronized(_stream) {
					// must double check that the subprocess is alive to avoid reading a closed stream
					if(_alive) {
						int count = _stream.read( buffer, 0, BUFFER_SIZE );
						if ( count > 0 ) {
							// output the text if in debug mode otherwise just ignore it to avoid overwhelming input from every launched and running application
							if ( DEBUG ) {
								final String output = new String( buffer, 0, count );
								System.out.print( output );
							}
						}
						else {
							return;		// end of buffer reached
						}
					}
				}
			}
			catch(IOException exception) {		// thrown when the stream is closed or cannot be read
				System.err.println("Exception while flushing the child process' stream:  " + exception);
				return;
			}
		}
	}
	
	
	/** Monitor the process in a separate thread waiting for the process to exit.  If the process exits, set the <code>_alive</code> flag to false and close the stream. */
	private void monitorProcessLife() {
		_alive = true;
		new Thread( new Runnable() {
			public void run() {
				try {
					_process.waitFor();
					synchronized(_stream) {
						_alive = false;
						_stream.close();
					}
				}
				catch(InterruptedException exception) {}
				catch(IOException exception) {}
			}
		}).start();
	}
}
