/*
 * Getback.java
 *
 * Created on December 19, 2001, 3:34 PM
 */
package xal.plugin.jca;


import xal.ca.*;
import gov.aps.jca.CAException;
import gov.aps.jca.event.GetEvent;
import gov.aps.jca.event.GetListener;


/**
 * Getback is a wrapper for JCA "get" callbacks.  It handles the JCA callback 
 * natively and then forwards the result appropriate to an IEventSinkValue receiver.
 *
 * @author  Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.0
 */
class Getback implements GetListener {
    final protected JcaChannel channel;       // Channel to monitor
    final protected IEventSinkValue listener; // listener for the callback
    
    
    /** 
     *  Create new Getback 
     *  @param  chan        channel to get pv value
     *  @param  dbr         jca.dbr.DBR object that will contain returned value
     *  @throws  xal.ca.ConnectionException  if channel is not connected
     */
    protected Getback(JcaChannel chan, IEventSinkValue aListener) throws ConnectionException, GetException {        
        channel = chan;
        listener = aListener;
		
        // launch the get callback operation
        get();
   }
    
    
    /**
     *  Get the process variable value
     *  @throws xal.ca.GetException if channel access failure
     */
    protected void get() throws GetException, ConnectionException {
        try {
            channel._jcaChannel.get(this);
        } 
		catch ( CAException exception )   {
            throw new GetException( "Get exception in GetBack: " + exception.getMessage() );
        }
    }

    
    /**
     * Derived objects must override this event hook.  Derived class
     * will catch the jca.getCompleted, convert to appropriate data type, and
     * forward to the appropriate data sink interface (IEventSinkXxxXxx).
     * @param event The jca get event.
     */    
    public void getCompleted( final GetEvent event ) {
        DbrValueAdaptor adaptor = new DbrValueAdaptor( event.getDBR() );
        listener.eventValue( new ChannelRecord(adaptor), channel );
    }
    
    
    /**
     * Return the associated Channel object
     * @return The channel being monitored
     */
    public Channel getChannel() {
        return channel;
    }
}
