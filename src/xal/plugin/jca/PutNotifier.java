/*
 * PutNotifier.java
 *
 * Created on September 18, 2002, 12:32 PM
 */

package xal.plugin.jca;

import xal.ca.PutListener;
import xal.ca.Channel;


/**
 *
 * @author  tap
 */
class PutNotifier implements gov.aps.jca.event.PutListener {
    final protected PutListener _listener;
    final protected Channel _channel;
    
	
    /** Creates a new instance of PutNotifier */
    public PutNotifier( final Channel channel, final PutListener listener ) {
        _channel = channel;
        _listener = listener;
    }
    
    
    /** jca.event.PutListener Interface Implementation */
    public void putCompleted( final gov.aps.jca.event.PutEvent putEvent ) {
        if ( _listener != null ) {
            _listener.putCompleted( _channel );
        }
    }
}
