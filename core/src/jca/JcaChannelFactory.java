/*
 * JcaChannelFactory.java
 *
 * Created on August 26, 2002, 1:25 PM
 */

package xal.jca;

import xal.ca.*;


/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * 
 * @author  tap
 */
public class JcaChannelFactory extends ChannelFactory {
	/** JCA channel system */
	protected JcaSystem _jcaSystem;
	
	/** cache of native JCA channels */
	protected JcaNativeChannelCache _nativeChannelCache;
	
	
	/** Constructor */
	public JcaChannelFactory() {
		_jcaSystem = new JcaSystem();
		_nativeChannelCache = new JcaNativeChannelCache( _jcaSystem );
	}
	
	
	/**
	 * Initialize the channel system
	 * @return true if the initialization was successful and false if not
	 */
	public boolean init() {
		return _jcaSystem.init();
	}
	
	
    /** 
	 * Create a JCA channel for the specified PV
	 * @param signalName The name of the PV signal
	 */
    protected Channel newChannel( final String signalName ) {
        return new JcaChannel( signalName, _jcaSystem.getJcaContext(), _nativeChannelCache );
    }
    
    
    /** 
	 * JcaSystem handles static behavior of Jca channels 
	 * @return the JCA channel system
	 */
    protected ChannelSystem channelSystem() {
        return _jcaSystem;
    }
}
