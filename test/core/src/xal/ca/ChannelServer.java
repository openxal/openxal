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
	/** 
	 * Constructor 
	 * @throws java.lang.Exception as appropriate
	 */
	public ChannelServer() throws Exception {}


	/** 
	 * Get a new instance of the Channel Server 
	 * @return a new ChannelServer instance
	 * @throws java.lang.Exception as appropriate
	 */
	static public ChannelServer getInstance() throws Exception {
		return ChannelFactory.defaultSystem().newChannelServer();
	}


	/** 
	 * Dispose of the context 
	 * @throws java.lang.Exception as appropriate
	 */
	public void destroy() throws Exception {
	}


    /** print information about this channel factory */
    abstract public void printInfo();


	/** 
	 * Register a process variable for a double array  
	 * @param pv process variable
	 * @param initialArray initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final double[] initialArray );


	/** 
	 * Register a process variable for a scalar double  
	 * @param pv process variable
	 * @param initialValue initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final double initialValue );


	/** 
	 * Register a process variable for a float array  
	 * @param pv process variable
	 * @param initialArray initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final float[] initialArray );


	/** 
	 * Register a process variable for a scalar float  
	 * @param pv process variable
	 * @param initialValue initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final float initialValue );


	/** 
	 * Register a process variable for an int array  
	 * @param pv process variable
	 * @param initialArray initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final int[] initialArray );


	/** 
	 * Register a process variable for a scalar int  
	 * @param pv process variable
	 * @param initialValue initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final int initialValue );


	/** 
	 * Register a process variable for a short array  
	 * @param pv process variable
	 * @param initialArray initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final short[] initialArray );


	/** 
	 * Register a process variable for a scalar short  
	 * @param pv process variable
	 * @param initialValue initial value
	 * @return a new ChannelServerPV
	 */
	abstract public ChannelServerPV registerPV( final String pv, final short initialValue );
}
