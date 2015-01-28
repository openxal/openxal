/*
 * JcaChannel.java
 *
 * Created on August 26, 2002, 10:02 AM
 */
package xal.plugin.jca;

import xal.tools.messaging.MessageCenter;
import xal.tools.ArrayValue;
import xal.ca.*;

import gov.aps.jca.CAException;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.Context;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.*;

import java.util.logging.*;


/**
 *  Objectizes the Java Channel Access (jca) library by Boucher.  In particular, the jca.PV object
 *  and static jca.Ca are encapsulated.  The the jca.PV and jca.Ca operations are collected
 *  and exposed as necessary to perform rudimentary process variable puts, gets, and monitors.  The
 *  user may request a reference to the associated PV object to perform more complicated operations
 *  as appropriate.
 *
 * @author Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.1
 */
class JcaChannel extends Channel {
    //  Global Variables
    static protected boolean      s_bolCaInit;        // channel access initialized
    static protected boolean      s_bolCaLock;        // channel access library lock    
    static protected long         s_lngCntRef;        // channel instance reference count
    static protected boolean      s_bolDebug;         // Forte debug mode (do not initialize jca)

    
    //  Constants
    public static final double      c_dblDefTimeIO = 5.0;       // default pend IO timeout
    public static final double      c_dblDefTimeEvent = 0.1;    // default pend event timeout
    
	
    //  Database Request (DBR) Data Types
    public static final int STRING;
    public static final int SHORT;
    public static final int FLOAT;
    public static final int ENUM;
    public static final int BYTE;
    public static final int INT;
    public static final int DOUBLE;
    
    
    //  Local Attributes
	
	/** JCA Channel */    
    protected gov.aps.jca.Channel _jcaChannel;
	
	/** cache of native JCA channels JCA won't allow us to connect to more than one channel for the same PV signal. */
	protected JcaNativeChannelCache _jcaNativeChannelCache;
	
	/** JCA Context */
	protected Context _jcaContext;
	
	/** indicates whether this channel ever initialized CA */
    protected boolean  hasInitializedCa;
	
	/** connection lock for wait and notify actions */
	protected Object _connectionLock;
        
    
    /**  Class loader initialization - Set channel access initialization flag */
    static {
        s_bolCaInit = false;
        s_bolCaLock = false;
        s_lngCntRef  = 0;
		
		STRING	= DBRType.STRING.getValue();
		SHORT   = DBRType.SHORT.getValue();
		FLOAT   = DBRType.FLOAT.getValue();
		ENUM    = DBRType.ENUM.getValue();
		BYTE    = DBRType.BYTE.getValue();
		INT     = DBRType.INT.getValue();
		DOUBLE  = DBRType.DOUBLE.getValue();
    }
    
    
    /**
     *  JcaChannel empty constructor.
     */
    JcaChannel() {
        this( null, null, null );
    }
    
    
    /** 
     * Constructor.
	 * @param nativeChannelCache  a cache of native JCA channels
     * @param signalName  EPICS PV name
	 * @param jcaContext  the JCA Context within which to create the channel
     */    
    JcaChannel( final String signalName, final Context jcaContext, final JcaNativeChannelCache jcaNativeChannelCache ) {
        super( signalName );
        
        hasInitializedCa = false;   // since we only load and initialize Channel Access on demand
		_jcaNativeChannelCache = jcaNativeChannelCache;
        _jcaContext = jcaContext;
		_jcaChannel = null;
		_connectionLock = new Object();
        
        m_dblTmIO  = c_dblDefTimeIO;
        m_dblTmEvt = c_dblDefTimeEvent;
    }
    
    
    /**
     *  Channel access library lock.  Locks CA library into memory once it is initialized.
     *  CA library will not release until Java virtual machine terminates.
     */
    public static synchronized void caLock()    {}
    
    
    /**
     *  Unlock the Channel Access library.  CA library may be released if there are no
     *  Channel instances in the Java virtual machine.
     */
	public static synchronized void caUnlock() {}

     
     /**
      * Set Forte debug mode.  
      * The jca shared library (jca.dll on Windows) is normally loaded whenever the first Channel object
      * is instantiated.  This is done with a call to jca.Ca.init() with the Channel.caAddRef() method.
      * Loading the jca shared library seems to confuse the Forte debugger.  
      * Use this method to set the Channel Forte debug mode to true.  When in debug mode the jca shared 
      * library is never loaded.  Thus, Channel objects may be instatiated, but they cannot be used to
      * connect to EPICS channel access.
      * @param  bDebug      debug flag (on or off)
      */
     public static synchronized void setDebugMode(boolean bDebug) {
         if (s_bolDebug==true && bDebug==false) // turning off debug mode
             if (s_lngCntRef > 0) {             // must check if any channels were instantiated
                s_bolCaInit = true;             // initialize CA if so
             }
         
         s_bolDebug = bDebug;
    }             
     
     
    /**
     *  Check if EPICS Channel Access libaray has been initialized and if not, do so.
     */
    private static synchronized void caAddRef()    {
        // Check if Channel Access needs to be initialized
        if (!s_bolCaInit) 
            if (!s_bolDebug) {
                s_bolCaInit = true;
            }
     
        s_lngCntRef++;
    }
    
    
    /**
     *  Check if EPICS Channel Access library is still needed and if not, release it.
     */
    private static synchronized void caRelease() {
        // Error check 
        if (s_lngCntRef == 0)  return;      // inadvertant (unbalanced) call
        
        // Check if channel access library is still needed and if not, release it
        s_lngCntRef--;
        if (s_lngCntRef > 0) return;        // still active channels
        if (s_bolCaLock)     return;        // CA library is locked into memory
        if (s_bolCaInit == true) {          // CA library is in memory and there are no more active channels
            s_bolCaInit = false;
            //Ca.exit();
        }
    }

    
    /**
     *  Check if Channel Access library can be released
     */
    protected void finalize() throws Throwable   {
        if ( hasInitializedCa ) {
            caRelease();
        }
        super.finalize();
    }

    
    /**
     *  Set the channel access Pend IO timeout
     *  @param  dblTm       I/O timeout
     */
    public void setIoTimeout(double dblTm)      { m_dblTmIO = dblTm; };

    
    /**
     *  Set the channel access Pend Event timeout
     *  @param  dblTm       event timeout
     */
    public void setEventTimeout(double dblTm)   { m_dblTmEvt = dblTm; };
    
	
    /**
     *  Get the channel access Pend IO timeout
     *  @return       I/O timeout
     */
    public double getIoTimeout()      { return m_dblTmIO; };
	
	
    /**
     *  Get the channel access Pend Event timeout
     *  @return       event timeout
     */
    public double getEventTimeout()   { return m_dblTmEvt; };
    
    
    
    /** Initialize channel access and increment instance count */
    protected void initChannelAccess() {
        if ( !hasInitializedCa ) {
            caAddRef();    // Increment Channel instance counter
            hasInitializedCa = true;
        }
    }
    
    
    /*
     *  Channel Access Connection
     */
    
    /** 
	 * Notify connection listeners that connection has changed 
	 */
    private ConnectionListener newConnectionListener() {
		return new ConnectionListener() {
			public void connectionChanged( final ConnectionEvent event ) {
				// make sure we don't post a connection event until the channel has been assigned
				synchronized ( _connectionLock ) {
					if( event.isConnected() ) {
						processConnectionEvent();
					}
					else {
						connectionFlag = false;
						if ( connectionProxy != null ) {
							connectionProxy.connectionDropped( JcaChannel.this );
						}
					}
				}
			}
		};
    }
	
	
	/** Process a connection event */
	private void processConnectionEvent() {
		connectionFlag = true;
		proceedFromConnection();
		if ( connectionProxy != null ) {
			connectionProxy.connectionMade( this );
		}		
	}
	
	
	/**
	 * Request a new connection and wait for it no longer than the timeout.
	 * @param timeout seconds to wait for a connection before giving up
	 * @return true if the connection was made within the timeout and false if not
	 */
	public boolean connectAndWait( final double timeout ) {
		if ( m_strId == null )  return false;		// check whether this channel's name has been specified
		requestConnection();
		flushIO();
		if ( this.isConnected() )  return true;		// check if we have a connection
        waitForConnection( timeout );
		return isConnected();
	}
	
	
	/**
	 * Request that the channel be connected.  Connections are made in the background
	 * so this method returns immediately upon making the request.  The connection will be
	 * made in the future as soon as possible.  A connection event will be sent to registered
	 * connection listeners when the connection has been established.
	 */
	public void requestConnection() {
        if ( m_strId == null || isConnected() )  return;	// determine if there is any point in attempting a connection
        
        // initialize channel access if necessary and increment instance counter
        initChannelAccess();
        
        // Make connection PV 
        if ( _jcaChannel == null ) {
			try {
				// make sure we don't post a connection event until the channel has been assigned
				synchronized ( _connectionLock ) {
					_jcaChannel = _jcaNativeChannelCache.getChannel( m_strId );
					_jcaChannel.addConnectionListener( newConnectionListener() );
					if ( _jcaChannel.getConnectionState() == gov.aps.jca.Channel.CONNECTED ) {
						processConnectionEvent();
					}
				}
			}
			catch( CAException exception ) {
				final String message = "Error attempting to connect to: " + m_strId;
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
			}
        }
	}
	
	
	/**
	 * Attempt to connect only if this channel has never been connected in the past.
	 */
	private void connectIfNeverConnected() {
		if ( !hasEverBeenConnected() )  connectAndWait();
	}
    
    
    /**
     * Wait until a connection is made or the attempt to connect has timed out.
	 * @param timeout seconds to wait for the connection before giving up
     */
    synchronized private void waitForConnection( final double timeout ) {
        if ( connectionFlag )  return;  // no need to wait
        try {
			synchronized(_connectionLock) {
				_connectionLock.wait( (long)( 1000 * timeout ) );
			}
        }
        catch(InterruptedException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error waiting for connection to: " + m_strId, exception );
			exception.printStackTrace();
        }
    }
    
    
    /**
     * Proceed forward since the connection has been made.
     */
    private void proceedFromConnection() {
		synchronized(_connectionLock) {
			_connectionLock.notify();
		}
    }
        
    
    /**
     *  Terminate the network channel connection and clear all events associated
     *  with process variable
     */
    public void disconnect() {
		try {
			if ( !isConnected() )  return;
			
			try {
				_jcaChannel.destroy();
			} 
			catch (CAException exception)  {
				Logger.getLogger("global").log( Level.SEVERE, "Error disconnecting: " + m_strId, exception );
			}			
		}
		finally {
			_jcaChannel = null;
		}
    }
	
	
	/**
	 * Determine if the channel has ever been connected regardless of its present connection state.
	 * @return true if the channel has ever been connected and false if not.
	 */
	private boolean hasEverBeenConnected() {
		final gov.aps.jca.Channel.ConnectionState state = _jcaChannel.getConnectionState();
		return _jcaChannel != null &&  ( state == gov.aps.jca.Channel.CONNECTED || state == gov.aps.jca.Channel.DISCONNECTED );
	}
    
	
    /**
	 *  Checks if this channel has ever been connected.
     *  @param  strFuncName     name of function using connection
     */
    private void checkIfEverConnected( final String methodName ) throws ConnectionException  {
        if ( !hasEverBeenConnected() ) {
            throw new ConnectionException( this, "Channel::" + methodName + " - The channel \"" + m_strId + "\" must be connected at least once in the past to use this feature.");
        }
    }
    
    
    /**
     *  Get state of current process variable connection
     *  @return     EPICS channel access state code
     */
    public int state() throws ConnectionException    {
        checkIfEverConnected( "state()" );
        
        return _jcaChannel.getConnectionState().getValue();
    }
    
    
    /** get the Java class associated with the native type of this channel */
    public Class<?> elementType() throws ConnectionException {
        checkIfEverConnected( "elementType()" );
        
        return DbrValueAdaptor.elementType( getJcaType() );
    }
    
    
    /**
     *  Return native type of process variable associated with channel
     *  @return     jca.DBR type code of process variable
     */
    public int nativeType() throws ConnectionException  {
        checkIfEverConnected( "nativeType()" );
        
        // Get the type code
		return _jcaChannel.getFieldType().getValue();
    }
	
	
	/**
	 * Get the JCA field type of this channel
	 * @return the field type of the JCA channel
	 */
	private DBRType getJcaType() throws ConnectionException {
        checkIfEverConnected( "getJcaType()" );
		
		return _jcaChannel.getFieldType();
	}

    
    /**
     *  Return size of value array associated with process variable
     *  @return     number of values in process variable
     */
    public int elementCount() throws ConnectionException    {
        checkIfEverConnected( "elementCount()" );
        
       // Get the element count
		return _jcaChannel.getElementCount();
    }

    
    /**
     *  Determine if channel has read access to process variable
     *  @return             true if channel has read access
     *
     *  @exception  ConnectionException     channel not connected
     */
    public boolean  readAccess() throws ConnectionException    {
        checkIfEverConnected( "readAccess()" );

        // Get read access
		return _jcaChannel.getReadAccess();
    }
    
	
    /**
     *  Determine if channel has write access to process variable
     *  @return             true if channel has write access
     *
     *  @exception  ConnectionException     channel not connected
     */
    
    public boolean  writeAccess() throws ConnectionException    {
        checkIfEverConnected( "writeAccess()" );

        // Get write access
        return _jcaChannel.getWriteAccess();
    }
    
	
    /**
     *  Get the IOC host name which supports the process variable
     *  @return     string containing network name of host
     */
    public String hostName() throws ConnectionException    {
        this.checkConnection( "hostName()" );
        
        // Get the host name
		return _jcaChannel.getHostName();
    }
	
    
    /**
     * Get the native value-status DBR type of this channel.
     * @return The native DBR type of this channel.
     */
    protected int getStatusType() throws ConnectionException, GetException {
        connectIfNeverConnected();

        final DBRType nativeType = getJcaType();
        
        if ( nativeType.isBYTE() ) {
			return DBRType.STS_BYTE.getValue();
		}
		else if ( nativeType.isENUM() ) {
			return DBRType.STS_ENUM.getValue();
		}
		else if ( nativeType.isSHORT() ) {
			return DBRType.STS_SHORT.getValue();
		}
		else if ( nativeType.isINT() ) {
			return DBRType.STS_INT.getValue();
		}
		else if ( nativeType.isFLOAT() ) {
			return DBRType.STS_FLOAT.getValue();
		}
		else if ( nativeType.isDOUBLE() ) {
			return DBRType.STS_DOUBLE.getValue();
		}
		else if ( nativeType.isSTRING() ) {
			return DBRType.STS_STRING.getValue();
		}
		else {
			throw new GetException( "No status type for type code: " + nativeType + " for pv: " + m_strId );
		}
    }
    

    /**
     * Get the native DBR value-status-timestamp type of this channel.
     * @return The native DBR type of this channel.
     */
    protected int getTimeType() throws ConnectionException, GetException {
		final DBRType dbrType = getTimeDBRType();
		return dbrType.getValue();		
    }
	
	
	/** Get the native time DBR Type */
	private DBRType getTimeDBRType() throws ConnectionException, GetException {
        connectIfNeverConnected();
		
        final DBRType nativeType = getJcaType();
        
        if ( nativeType.isBYTE() ) {
			return DBRType.TIME_BYTE;
		}
		else if ( nativeType.isENUM() ) {
			return DBRType.TIME_ENUM;
		}
		else if ( nativeType.isSHORT() ) {
			return DBRType.TIME_SHORT;
		}
		else if ( nativeType.isINT() ) {
			return DBRType.TIME_INT;
		}
		else if ( nativeType.isFLOAT() ) {
			return DBRType.TIME_FLOAT;
		}
		else if ( nativeType.isDOUBLE() ) {
			return DBRType.TIME_DOUBLE;
		}
		else if ( nativeType.isSTRING() ) {
			return DBRType.TIME_STRING;
		}
		else {
			throw new GetException( "No time type for type code: " + nativeType + " for pv: " + m_strId );
		}
	}
    
    
    /**
     * Make a new DBR for the native type of this channel.
     * @return a native DBR for this channel.
     */
    DBR makeValueDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();
		
        // create a monitor of the correct type
		if ( fieldType == DOUBLE ) {
			return new DBR_Double( elementCount );
		}
		else if ( fieldType == FLOAT ) {
			return new DBR_Float( elementCount );
		}
		else if ( fieldType == INT ) {
			return new DBR_Int( elementCount );
		}
		else if ( fieldType == SHORT ) {
			return new DBR_Short( elementCount );
		}
		else if ( fieldType == ENUM ) {
			return new DBR_Enum( elementCount );
		}
		else if ( fieldType == BYTE ) {
			return new DBR_Byte( elementCount );
		}
		else if ( fieldType == STRING ) {
			return new DBR_String( elementCount );
		}
		else {
			return null;
		}
    }
    
    
    /**
     * Make a new value/status DBR for the native type of this channel.
     * @return a native DBR for this channel.
     */
    DBR makeStatusDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();
		
        // create a monitor of the correct type
		if ( fieldType == DOUBLE ) {
			return new DBR_STS_Double( elementCount );
		}
		else if ( fieldType == FLOAT ) {
			return new DBR_STS_Float( elementCount );
		}
		else if ( fieldType == INT ) {
			return new DBR_STS_Int( elementCount );
		}
		else if ( fieldType == SHORT ) {
			return new DBR_STS_Short( elementCount );
		}
		else if ( fieldType == ENUM ) {
			return new DBR_STS_Enum( elementCount );
		}
		else if ( fieldType == BYTE ) {
			return new DBR_STS_Byte( elementCount );
		}
		else if ( fieldType == STRING ) {
			return new DBR_STS_String( elementCount );
		}
		else {
			return null;
		}
    }
    
    
    /**
     * Make a new value/status/timestamp DBR for the native type of this channel.
     * @return a native DBR for this channel.
     */
    DBR makeTimeDBR() throws ConnectionException {
        int fieldType = nativeType();
        int elementCount = elementCount();
		
        // create a monitor of the correct type
		if ( fieldType == DOUBLE ) {
			return new DBR_TIME_Double( elementCount );
		}
		else if ( fieldType == FLOAT ) {
			return new DBR_TIME_Float( elementCount );
		}
		else if ( fieldType == INT ) {
			return new DBR_TIME_Int( elementCount );
		}
		else if ( fieldType == SHORT ) {
			return new DBR_TIME_Short( elementCount );
		}
		else if ( fieldType == ENUM ) {
			return new DBR_TIME_Enum( elementCount );
		}
		else if ( fieldType == BYTE ) {
			return new DBR_TIME_Byte( elementCount );
		}
		else if ( fieldType == STRING ) {
			return new DBR_TIME_String( elementCount );
		}
		else {
			return null;
		}
    }
    
    
    /** Convenience method which returns the units for this channel. */
    public String getUnits() throws ConnectionException, GetException {
        return getCtrlInfo().getUnits();
    }
    
    
    /** 
     * Get the lower and upper operation limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    public String[] getOperationLimitPVs() {
        return constructLimitPVs( "LOPR", "HOPR" );
    }
    
    
    /** 
     * Get the lower and upper warning limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    public String[] getWarningLimitPVs() {
        return constructLimitPVs( "LOW", "HIGH" );
    }
    
    
    /** 
     * Get the lower and upper alarm limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    public String[] getAlarmLimitPVs() {
        return constructLimitPVs( "LOLO", "HIHI" );
    }
    
    
    /** 
     * Get the lower and upper drive limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    public String[] getDriveLimitPVs() {
        return constructLimitPVs( "DRVL", "DRVH" );
    }
    
    
    /** 
     * Construct the lower and upper limit PVs from the lower and upper suffixes
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    private String[] constructLimitPVs( final String lowerSuffix, final String upperSuffix ) {
        final String[] rangePVs = new String[2];
        rangePVs[0] = channelName() + "." + lowerSuffix;
        rangePVs[1] = channelName() + "." + upperSuffix;
        return rangePVs;
    }
    
    
    /** Convenience method which returns the upper display limit. */
    public Number rawUpperDisplayLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperDispLimit();
    }
    
    
    /** Convenience method which returns the lower display limit. */
    public Number rawLowerDisplayLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerDispLimit();
    }
    
    
    /** Convenience method which returns the upper alarm limit. */
    public Number rawUpperAlarmLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperAlarmLimit();
    }
    
    
    /** Convenience method which returns the lower alarm limit. */
    public Number rawLowerAlarmLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerAlarmLimit();
    }
    
    
    /** Convenience method which returns the upper warning limit. */
    public Number rawUpperWarningLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperWarningLimit();
    }
    
    
    /** Convenience method which returns the lower warning limit. */
    public Number rawLowerWarningLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerWarningLimit();
    }
    
    
    /** Convenience method which returns the upper control limit. */
    public Number rawUpperControlLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getUpperCtrlLimit();
    }
    
    
    /** Convenience method which returns the lower control limit. */
    public Number rawLowerControlLimit() throws ConnectionException, GetException {
        return getCtrlInfo().getLowerCtrlLimit();
    }
    
    
    /**
     * Returns a DBR_CTRL instance of the appropriate for this channel.
     * The DBR_CTRL record contains valuable information about the channel 
     * such as the units and upper and lower limits for alarm, display, warning
     * and control.  All of these items are returned as a DBData instance.
     * Examples:
     *<code>
     *      String units = channel.getCtrlInfo().units();
     *      double upperDisplayLimit = channel.getCtrlInfo().upperDispLimit().doubleValue();
     *</code>
     */
    protected CTRL getCtrlInfo()  throws ConnectionException, GetException {
        connectIfNeverConnected();

        int pvType = nativeType();
        int controlType = -1;
		
        final DBRType nativeType = getJcaType();
        
        if ( nativeType.isBYTE() ) {
			controlType = DBRType.CTRL_BYTE.getValue();
		}
		else if ( nativeType.isENUM() ) {
			// there appears to be no ENUM control record
			throw new GetException("No control record for ENUM type for pv: " + m_strId);
		}
		else if ( nativeType.isSHORT() ) {
			controlType = DBRType.CTRL_SHORT.getValue();
		}
		else if ( nativeType.isINT() ) {
			controlType = DBRType.CTRL_INT.getValue();
		}
		else if ( nativeType.isFLOAT() ) {
			controlType = DBRType.CTRL_FLOAT.getValue();
		}
		else if ( nativeType.isDOUBLE() ) {
			controlType = DBRType.CTRL_DOUBLE.getValue();
		}
		else {
			String message = "No control record for type code: " + nativeType + " for pv: " + m_strId;
			throw new GetException(message);
		}
        
        CTRL dbr = (CTRL)getVal(controlType);
        
        return dbr;
    }
    
 
    /**
     * This convenience method returns a data value object for a general 
     * data type.  It always attempts a connect before fetching data.
     * For primitive data types, it returns an array of values.
     */
    public Object getValue() throws ConnectionException, GetException {
        if ( !connectAndWait() )  throw new ConnectionException();
        
        int dataType = nativeType();		
		return getVal( dataType ).getValue();
    }
    
    
    /**
	 * Get a <code>ChannelRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
     */
    protected ChannelRecord getRawStringValueRecord()  throws ConnectionException, GetException {
		return getRawValueRecord( STRING );
    }
    
    
    /**
	 * Get a <code>ChannelRecord</code> representing the fetched record for the specified type.
	 * @param pvType the type of PV to fetch
	 * @return the channel record
     */
    protected ChannelRecord getRawValueRecord( final int pvType )  throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal( pvType );
        ChannelRecord record;
        
        synchronized( dbr ) {
            ValueAdaptor adaptor = new DbrValueAdaptor( dbr );
            record = new ChannelRecord( adaptor );
        }
        
        return record;
    }
    
    
    /**
     * Return a <code>ChannelRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of
     * the PV.
     */
    public ChannelRecord getRawValueRecord()  throws ConnectionException, GetException {
        connectAndWait();
		return getRawValueRecord( nativeType() );
    }
    
    
    /**
	 * Get a <code>ChannelStatusRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
     */
    protected ChannelStatusRecord getRawStringStatusRecord()  throws ConnectionException, GetException {
		return getRawStatusRecord( DBRType.STS_STRING.getValue() );
    }
    
	
    /** Get a <code>ChannelStatusRecord</code> representing the fetched record for the specified type of this channel. */
    protected ChannelStatusRecord getRawStatusRecord( final int type )  throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal( type );
        ChannelStatusRecord record;
        
        synchronized( dbr ) {
            StatusAdaptor adaptor = new DbrStatusAdaptor( dbr );
            record = new ChannelStatusRecord( adaptor );
        }
        
        return record;
    }
    
    
    /** Get a <code>ChannelStatusRecord</code> representing the fetched record for the native type of this channel. */
    public ChannelStatusRecord getRawStatusRecord()  throws ConnectionException, GetException {
        connectAndWait();
		return getRawStatusRecord( getStatusType() );
    }
    
    
    /**
	 * Get a <code>ChannelTimeRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
     */
    protected ChannelTimeRecord getRawStringTimeRecord()  throws ConnectionException, GetException {
		return getRawTimeRecord( DBRType.TIME_STRING.getValue() );
    }
    
	
    /** Return a <code>ChannelTimeRecord</code> representing the fetched record for the specified type of this channel. */
    public ChannelTimeRecord getRawTimeRecord( final int type )  throws ConnectionException, GetException {
        connectAndWait();
        DBR dbr = this.getVal( type );
        ChannelTimeRecord record;
        
        synchronized(dbr) {
            TimeAdaptor adaptor = new DbrTimeAdaptor( dbr );
            record = new ChannelTimeRecord( adaptor );
        }
        
        return record;
    }
    
	
    /** Return a <code>ChannelTimeRecord</code> representing the fetched record for the native type of this channel. */
    public ChannelTimeRecord getRawTimeRecord()  throws ConnectionException, GetException {
        connectAndWait();
		return getRawTimeRecord( getTimeType() );
    }
	
    
    /**
     *  Gets the value of a PV as a database request object
     *  @param  type     DBR type code of returned object
     *  @return DBR object containing PV value
     */
    private DBR getVal( final int type ) throws ConnectionException, GetException    {
        this.checkConnection("getVal()");
        int count = this.elementCount();

        return this.getVal( type, count );
    }
    
    
    /**
     *  Return process variable values in specific type and number
     *  @param  type      DBR type code of returned value
     *  @param  count     number of values to return
     *  @return           DBR containing process variable values
     *  @exception  ConnectionException channel not connected
     *  @exception  GetException        channel access get failure
     */
    private DBR getVal( final int type, final int count ) throws ConnectionException, GetException {
        this.checkConnection("getVal()");
        
        try {
            DBR dbr = _jcaChannel.get( DBRType.forValue( type ), count );
			flushGetIO();
			return dbr;
        } 
		catch ( CAException exception )    {
			Logger.getLogger("global").log( Level.WARNING, "Error getting value from: " + m_strId, exception );
            throw new RuntimeException( exception );
		}
    }
    
    
    /**
     *  Get value of process variable asynchronously with a "get monitor" object.  Value of 
     *  process variable is sent to the event listener indicated by IEventSinkValue.
     *  @param  listener Listener of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @exception  xal.ca.GetException            general channel access failure
     */
    public void getRawValueCallback( final IEventSinkValue listener ) throws ConnectionException, GetException {
		getRawValueCallback( listener, true );
    }
    
    
    /**
	 *  Get value of process variable asynchronously with a "get monitor" object.  Value of 
     *  process variable is sent to the event listener indicated by IEventSinkValue.
     *  @param  listener Listener of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @exception  xal.ca.GetException            general channel access failure
     */
    public void getRawValueCallback( final IEventSinkValue listener, final boolean attemptConnection ) throws ConnectionException, GetException {
		checkConnection( "getValueCallback()", attemptConnection );
        
        new Getback( this, listener );
		if ( listener == null )  flushGetIO();
    }
	
	
	/** Submit a non-blocking Get request with callback */
	public void getRawValueTimeCallback( final IEventSinkValTime listener, final boolean attemptConnection ) throws ConnectionException, GetException {
		checkConnection( "getRawValueTimeCallback()", attemptConnection );
		
		try {
			final DBRType timeDBRType = getTimeDBRType();
			
			_jcaChannel.get( timeDBRType, elementCount(), new gov.aps.jca.event.GetListener() {
				public void getCompleted( final gov.aps.jca.event.GetEvent event ) {
					final DbrTimeAdaptor adaptor = new DbrTimeAdaptor( event.getDBR() );
					listener.eventValue( new ChannelTimeRecord( adaptor ), JcaChannel.this );
				}
			});
		}
		catch( gov.aps.jca.CAException exception ) {
			throw new RuntimeException( "Exception getting the time DBR type for " + m_strId, exception );
		}
	}
	
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback( final String newVal, final xal.ca.PutListener listener ) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );
        
        try {
            _jcaChannel.put( newVal, new PutNotifier( this, listener ) );
			if ( listener == null )  flushPutIO();
        } 
		catch ( CAException exception )    {
			Logger.getLogger( "global" ).log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException( "JcaChannel.putValCallback(): " + exception.getMessage() );
        }         
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback( final byte newVal, final xal.ca.PutListener listener ) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put( newVal, new PutNotifier(this, listener) );
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }         
    }
    

    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback( final short newVal, final xal.ca.PutListener listener ) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put( newVal, new PutNotifier(this, listener) );
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        } 
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback( final int newVal, final xal.ca.PutListener listener ) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put( newVal, new PutNotifier( this, listener ) );
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }         
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback( final float newVal, final xal.ca.PutListener listener ) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put( newVal, new PutNotifier( this, listener ) );
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }         
    }

    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(double newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put( newVal, new PutNotifier( this, listener ) );
			if ( listener == null )  flushPutIO();
        }
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): " + exception.getMessage());
        }         
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(byte[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put(newVal, new PutNotifier(this, listener));
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }        
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(short[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put(newVal, new PutNotifier(this, listener));
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        } 
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(int[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put(newVal, new PutNotifier(this, listener));
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }         
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(float[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put(newVal, new PutNotifier(this, listener));
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        }         
    }
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    public void putRawValCallback(double[] newVal, xal.ca.PutListener listener) throws ConnectionException, PutException {
        this.checkConnection( "putValCallback()" );  
        
        try {
            _jcaChannel.put(newVal, new PutNotifier(this, listener));
			if ( listener == null )  flushPutIO();
        } 
		catch (CAException exception)    {
			Logger.getLogger("global").log( Level.WARNING, "Error putting value to: " + m_strId, exception );
            throw new PutException("JcaChannel.putValCallback(): Incompatible types - " + exception.getMessage());
        } 
        
    }    
    
    
    /**
     *  Setup a monitor on this channel 
     *  @param  ifcSink     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired
     *                      or'ed combination of 
     *                      {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return             MonitorSrc object associated with this event
     */
    public xal.ca.Monitor addMonitorValTime(IEventSinkValTime ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValTime()");
        return JcaMonitor.newValueTimeMonitor( this, ifcSink, intMaskFire );
    }

    
    /**
     *  Setup a monitor on this channel 
     *  @param  ifcSink     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired
     *                      or'ed combination of 
     *                      {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return             MonitorSrc object associated with this event
     */
    public xal.ca.Monitor addMonitorValStatus(IEventSinkValStatus ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValStatus()");
        return JcaMonitor.newValueStatusMonitor( this, ifcSink, intMaskFire );
    }
    
    
    /**
     *  Setup a monitor on this channel 
     *  @param  ifcSink     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired
     *                      or'ed combination of 
     *                      {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return             MonitorSrc object associated with this event
     */
    public xal.ca.Monitor addMonitorValue(IEventSinkValue ifcSink, int intMaskFire) throws ConnectionException, MonitorException {
        this.checkConnection("addMonitorValue()");
        return JcaMonitor.newValueMonitor( this, ifcSink, intMaskFire );
    }
    
    
    /** Flushes the channel access request buffer for events */
     private void flushEvent() {
		try {
			_jcaContext.pendEvent(m_dblTmEvt);
		}
		catch(CAException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error flushing the channel access request buffer.", exception );
			exception.printStackTrace();
		}
     }
     
     
    /**
     *  Flushes the channel access request buffer for get operations
     *  @exception  GetException        a pendIO time out occurred
     */
     private void flushGetIO() throws GetException    {
		try {
			_jcaContext.pendIO( m_dblTmIO );
		}
		catch( CAException exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.SEVERE, "Error flushing the channel access GET I/O buffer.", exception );
			throw new GetException( "JcaChannel.flushGetIO() - channel access time out occurred" );
		}
		catch( TimeoutException exception ) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.SEVERE, "Error flushing the channel access GET I/O buffer.", exception );
			throw new GetException( "JcaChannel.flushGetIO() - channel access time out occurred" );
		}
     }
     
     
    /**
     *  Flushs the channel access request buffer for put operations
     *  @exception  PutException        a pendIO time out occurred
     */
     private void flushPutIO() throws PutException    {
		try {
			_jcaContext.pendIO( m_dblTmIO );
		}
		catch(CAException exception) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.SEVERE, "Error flushing the channel access PUT I/O buffer.", exception );
            throw new PutException( "JcaChannel.flushPutIO() - channel access time out occurred" );
		}
		catch(TimeoutException exception) {
			exception.printStackTrace();
			Logger.getLogger("global").log( Level.SEVERE, "Error flushing the channel access PUT I/O buffer.", exception );
            throw new PutException( "JcaChannel.flushPutIO() - channel access time out occurred" );
		}
     }
}

