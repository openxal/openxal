/*
 * MonitorSrc.java
 *
 * Created on November 20, 2001, 10:52 AM
 */

 
package xal.plugin.jca;

import xal.ca.*;

import gov.aps.jca.CAException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.dbr.*;


/**
 * Monitor implementation for JCA.
 * @author Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.0
 */
abstract class JcaMonitor extends Monitor implements gov.aps.jca.event.MonitorListener {
    protected int _type;
    protected gov.aps.jca.Channel _jcaChannel;    // PV object associated with channel
    protected gov.aps.jca.Monitor _jcaMonitor;    // internal JCA monitor
    
	
    /** 
     *  Creates new Monitor
     *  @param  chan  Channel object to monitor
     *  @param  type  The type of data being monitored
     *  @param  intMaskFire code specifying when monitor event is fired
     */
    protected JcaMonitor( final Channel chan, final int type, final int intMaskEvent ) throws ConnectionException {
        super( chan, intMaskEvent );

		_type = type;        
        _jcaChannel = ((JcaChannel)chan)._jcaChannel;
        _jcaMonitor = null;
    }


	/** factory method to create a monitor which provides value, status and timestamp */
	static public JcaMonitor newValueTimeMonitor( final JcaChannel chan, final IEventSinkValTime ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		return JcaMonitorValTime.newMonitor( chan, ifcSink, intMaskFire );
	}


	/** factory method to create a monitor which provides value and status */
	static public JcaMonitorValStatus newValueStatusMonitor( final JcaChannel chan, final IEventSinkValStatus ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		return JcaMonitorValStatus.newMonitor( chan, ifcSink, intMaskFire );
	}


	/** factory method to create a monitor which provides value */
	static public JcaMonitorValue newValueMonitor( final JcaChannel chan, final IEventSinkValue ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		return JcaMonitorValue.newMonitor( chan, ifcSink, intMaskFire );
	}


    /**
     *  Derived monitor objects must override this event hook.  Derived class
     *  will catch the jca.MonitorEvent, convert to appropriate data type, and
     *  forward to the appropriate data sink interface (IEventSinkXxxXxx).
     */
    abstract public void monitorChanged( MonitorEvent event );
    
    
    /**
     *  Stop the monitoring of PV
     */
    public void clear() {
        if ( !m_bolMonitoring )  return;
        
        try { 
			_jcaMonitor.clear(); 
		} 
		catch ( CAException exception ) {}
        
        _jcaMonitor = null;
        m_bolMonitoring = false;
    }
    
    
    /**
     *  Start the channel monitoring
     *
     *  @exception  MonitorException    unable to setup the channel access monitor
     */
    protected void begin() throws MonitorException    {
        
        try {
			final int count = _jcaChannel.getElementCount();
			final DBRType dbrType = DBRType.forValue( _type );
            _jcaMonitor = _jcaChannel.addMonitor( dbrType, count, m_intMaskEvent, this );
			_jcaChannel.getContext().flushIO();
        } 
		catch ( CAException exception )   {
            throw new MonitorException( "Monitor::begin() - Incompatible types " + exception.getMessage() );
        }
        
        m_bolMonitoring = true;
    }
}



/**
 *  Class MonitorValTime
 *  Monitor a channel for any data type.
 *  The record returned has value, status and time information.
 */
class JcaMonitorValTime extends JcaMonitor {
    private IEventSinkValTime m_ifcSink;      // data sink for channel monitoring
    
	
    // create a mew monitor
    protected JcaMonitorValTime( final Channel chan, final int type, final IEventSinkValTime ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
        super( chan, type, intMaskFire );
        m_ifcSink = ifcSink;

        super.begin();
    }
    
    
    // capture an event, wrap the dbr into a channel record and notify the sink
    public void monitorChanged( final MonitorEvent evt ) {
        final DBR dbr = evt.getDBR();
		if ( dbr != null ) {
			synchronized(dbr) {
				final TimeAdaptor adaptor = new DbrTimeAdaptor( dbr );
				postTimeRecord( m_ifcSink, adaptor );
			}			
		}
    }

    
    // convenient way to create a new monitor
    static public JcaMonitorValTime newMonitor( final JcaChannel chan, final IEventSinkValTime ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		try {
			int type = chan.getTimeType();
			return new JcaMonitorValTime( chan, type, ifcSink, intMaskFire );
		}
		catch( GetException exception ) {
			throw new MonitorException( "Error creating a new monitor: " + exception.getMessage() );
		}
    }
}



/**
 *  Class MonitorValStatus
 *  Monitor a channel for any data type.
 *  The record returned has value, status and time information.
 */
class JcaMonitorValStatus extends JcaMonitor {
    private IEventSinkValStatus m_ifcSink;      // data sink for channel monitoring
    
    // create a mew monitor
    protected JcaMonitorValStatus( final Channel chan, final int type, final IEventSinkValStatus ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
        super( chan, type, intMaskFire );
        m_ifcSink = ifcSink;

        super.begin();
    }
    
    
    // capture an event, wrap the dbr into a channel record and notify the sink
    public void monitorChanged( final MonitorEvent evt ) {
        final DBR dbr = evt.getDBR();
		if ( dbr != null ) {
			synchronized(dbr) {
				final StatusAdaptor adaptor = new DbrStatusAdaptor( dbr );
				postStatusRecord( m_ifcSink, adaptor );
			}			
		}
    }

    
    // convenient way to create a new monitor
    static public JcaMonitorValStatus newMonitor( final JcaChannel chan, final IEventSinkValStatus ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		try {
			int type = chan.getTimeType();        
			return new JcaMonitorValStatus( chan, type, ifcSink, intMaskFire );
		}
		catch( GetException exception ) {
			throw new MonitorException( "Error creating a new monitor: " + exception.getMessage() );
		}
    }
}



/**
 *  Class MonitorValTime
 *  Monitor a channel for any data type.
 *  The record returned has value, status and time information.
 */
class JcaMonitorValue extends JcaMonitor {
    final private IEventSinkValue m_ifcSink;      // data sink for channel monitoring
    
	
    // create a mew monitor
    protected JcaMonitorValue( final Channel chan, final int type, final IEventSinkValue ifcSink, final int intMaskFire ) 
        throws ConnectionException, MonitorException {
        super( chan, type, intMaskFire );
        m_ifcSink = ifcSink;

        super.begin();
    }
    
    
    // capture an event, wrap the dbr into a channel record and notify the sink
    public void monitorChanged( final MonitorEvent evt ) {
        final DBR dbr = evt.getDBR();
		if ( dbr != null ) {
			synchronized(dbr) {
				final ValueAdaptor adaptor = new DbrValueAdaptor( dbr );
				postValueRecord( m_ifcSink, adaptor );
			}			
		}
    }

    
    // convenient way to create a new monitor
    static public JcaMonitorValue newMonitor( final JcaChannel chan, final IEventSinkValue ifcSink, final int intMaskFire ) throws ConnectionException, MonitorException {
		try {
			int type = chan.getTimeType();  
			return new JcaMonitorValue( chan, type, ifcSink, intMaskFire );
		}
		catch( GetException exception ) {
			throw new MonitorException( "Error creating a new monitor: " + exception.getMessage() );
		}
    }
}
