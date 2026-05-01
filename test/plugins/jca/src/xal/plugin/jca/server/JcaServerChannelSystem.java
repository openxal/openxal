package xal.plugin.jca.server;

import gov.aps.jca.cas.ServerContext;
import xal.ca.ChannelSystem;
import xal.ca.ChannelServer;

/**
 * JcaServerChannelSystem is the same as JcaSystem
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
class JcaServerChannelSystem extends ChannelSystem {

    /** Java Channel Access Context */
    private ServerContext JCA_CONTEXT;

 
    /** Constructor */
    public JcaServerChannelSystem(ServerContext JCA_CONTEXT) {
        this.JCA_CONTEXT = JCA_CONTEXT;
    }


	/** Create a new channel server */
	public ChannelServer newChannelServer() throws Exception {
		return null;
	}


    @Override
    public void setDebugMode(boolean debugFlag) {
        // not used
    }

    @Override
    public void flushIO() {
    }

    @Override
    public boolean pendIO(double timeout) {
        return true;
    }

    @Override
    public void pendEvent(double timeout) {
    }

    @Override
    public void printInfo() {
        JCA_CONTEXT.printInfo();
    }
}
