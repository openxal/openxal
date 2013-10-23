/*
 * ChannelServer.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.ca;


/**
 * Abstract ChannelServer.
 * @author  tap
 */
abstract public class ChannelServer {
	/** Constructor */
	public ChannelServer() throws Exception {}


	/** Get a new instance of the Channel Server */
	static public ChannelServer getInstance() throws Exception {
		return ChannelFactory.defaultSystem().newChannelServer();
	}


	/** dispose of the context */
	public void destroy() throws Exception {
	}


    /** print information about this channel factory */
    abstract public void printInfo();


	/** Register a process variable for a double array  */
	abstract public ChannelServerPV registerPV( final String pv, final double[] initialArray );


	/** Register a process variable for a scalar double  */
	abstract public ChannelServerPV registerPV( final String pv, final double initialValue );


	/** Register a process variable for a float array  */
	abstract public ChannelServerPV registerPV( final String pv, final float[] initialArray );


	/** Register a process variable for a scalar float  */
	abstract public ChannelServerPV registerPV( final String pv, final float initialValue );


	/** Register a process variable for an int array  */
	abstract public ChannelServerPV registerPV( final String pv, final int[] initialArray );


	/** Register a process variable for a scalar int  */
	abstract public ChannelServerPV registerPV( final String pv, final int initialValue );


	/** Register a process variable for a short array  */
	abstract public ChannelServerPV registerPV( final String pv, final short[] initialArray );


	/** Register a process variable for a scalar short  */
	abstract public ChannelServerPV registerPV( final String pv, final short initialValue );
}
