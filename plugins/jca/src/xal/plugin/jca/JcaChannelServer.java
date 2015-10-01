/*
 * JcaChannelServer.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.plugin.jca;

import xal.ca.*;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;
import com.cosylab.epics.caj.cas.util.examples.CounterProcessVariable;

import gov.aps.jca.JCALibrary;
import gov.aps.jca.cas.ServerContext;
import gov.aps.jca.dbr.DBRType;

/**
 * Concrete implementation of ChannelServer backed by JCA.
 * @author  tap
 */
@Deprecated
public class JcaChannelServer extends ChannelServer {
	/** JCA channel system */
	final private JcaSystem JCA_SYSTEM;

	/** CA Server context */
	final private ServerContext CONTEXT;

	/** CA Server */
	final private DefaultServerImpl SERVER;


	/** Constructor */
	public JcaChannelServer() throws Exception {
		JCA_SYSTEM = new JcaSystem();

		// Create server implmentation
		SERVER = new DefaultServerImpl();

		// Create a context with default configuration values.
		CONTEXT = JCALibrary.getInstance().createServerContext( JCALibrary.CHANNEL_ACCESS_SERVER_JAVA, SERVER );
	}


	/** dispose of the context */
	public void destroy() throws Exception {
		if ( CONTEXT != null ) {
			CONTEXT.destroy();
		}
	}


	/** Register a process variable for a double array  */
	public JcaChannelServerPV registerPV( final String pv, final double[] initialArray ) {
		final MemoryProcessVariable memoryProcessVariable = new MemoryProcessVariable( pv, null, DBRType.DOUBLE, initialArray );
		SERVER.registerProcessVaribale( memoryProcessVariable );
		return new JcaChannelServerPV( memoryProcessVariable );
	}


	/** Register a process variable for a scalar double  */
	public JcaChannelServerPV registerPV( final String pv, final double initialValue ) {
		final double[] initialArray = new double[] { initialValue };
		return registerPV( pv, initialArray );
	}


	/** Register a process variable for a float array  */
	public JcaChannelServerPV registerPV( final String pv, final float[] initialArray ) {
		final MemoryProcessVariable memoryProcessVariable = new MemoryProcessVariable( pv, null, DBRType.FLOAT, initialArray );
		SERVER.registerProcessVaribale( memoryProcessVariable );
		return new JcaChannelServerPV( memoryProcessVariable );
	}


	/** Register a process variable for a scalar float  */
	public JcaChannelServerPV registerPV( final String pv, final float initialValue ) {
		final float[] initialArray = new float[] { initialValue };
		return registerPV( pv, initialArray );
	}


	/** Register a process variable for an int array  */
	public JcaChannelServerPV registerPV( final String pv, final int[] initialArray ) {
		final MemoryProcessVariable memoryProcessVariable = new MemoryProcessVariable( pv, null, DBRType.INT, initialArray );
		SERVER.registerProcessVaribale( memoryProcessVariable );
		return new JcaChannelServerPV( memoryProcessVariable );
	}


	/** Register a process variable for a scalar int  */
	public JcaChannelServerPV registerPV( final String pv, final int initialValue ) {
		final int[] initialArray = new int[] { initialValue };
		return registerPV( pv, initialArray );
	}


	/** Register a process variable for a short array  */
	public JcaChannelServerPV registerPV( final String pv, final short[] initialArray ) {
		final MemoryProcessVariable memoryProcessVariable = new MemoryProcessVariable( pv, null, DBRType.SHORT, initialArray );
		SERVER.registerProcessVaribale( memoryProcessVariable );
		return new JcaChannelServerPV( memoryProcessVariable );
	}


	/** Register a process variable for a scalar short  */
	public JcaChannelServerPV registerPV( final String pv, final short initialValue ) {
		final short[] initialArray = new short[] { initialValue };
		return registerPV( pv, initialArray );
	}


    /** print information about this channel factory */
    public void printInfo() {
        System.out.println( CONTEXT.getVersion().getVersionString() );
        CONTEXT.printInfo();
    }
}
