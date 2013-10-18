/*
 * ChannelFactoryPlugin.java
 *
 * Created on October 18, 2013
 */

package xal.ca;

import xal.jca.JcaChannelFactory;


/**
 * Concrete implementation of ChannelFactory that uses JCA.
 * @author  tap
 */
public class ChannelFactoryPlugin {
    /**
	 * Instantiate a new ChannelFactory
	 * @return a new channel factory
	 */
    static public ChannelFactory getChannelFactoryInstance() {
        return new JcaChannelFactory();
    }
}
