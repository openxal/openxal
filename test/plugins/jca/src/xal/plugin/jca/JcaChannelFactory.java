/*
 * JcaChannelFactory.java
 *
 * Created on August 26, 2002, 1:25 PM
 */

package xal.plugin.jca;

import xal.ca.*;


/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * 
 * @author  tap
 */
public class JcaChannelFactory extends ChannelFactory {
	/** JCA channel system */
	final private JcaSystem JCA_SYSTEM;
	
	/** cache of native JCA channels */
	final private JcaNativeChannelCache NATIVE_CHANNEL_CACHE;
	
	
	/** Constructor */
	public JcaChannelFactory() {
		JCA_SYSTEM = new JcaSystem();
		NATIVE_CHANNEL_CACHE = new JcaNativeChannelCache( JCA_SYSTEM );
	}
	
	
	/**
	 * Initialize the channel system
	 * @return true if the initialization was successful and false if not
	 */
	public boolean init() {
		return JCA_SYSTEM.init();
	}
	
	
    /** 
	 * Create a JCA channel for the specified PV
	 * @param signalName The name of the PV signal
	 */
    protected Channel newChannel( final String signalName ) {
        return new JcaChannel( signalName, JCA_SYSTEM.getJcaContext(), NATIVE_CHANNEL_CACHE );
    }
    
    
    /** 
	 * JcaSystem handles static behavior of Jca channels 
	 * @return the JCA channel system
	 */
    protected ChannelSystem channelSystem() {
        return JCA_SYSTEM;
    }
    
    
    /** print information about this channel factory */
    public void printInfo() {
        JCA_SYSTEM.printInfo();
    }
}
