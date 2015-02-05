/*
 * Channel.java
 *
 * Created on October 17, 2001, 5:30 PM
 */
package xal.ca;

import xal.tools.messaging.MessageCenter;
import xal.tools.ArrayValue;
import xal.tools.transforms.ValueTransform;
import xal.tools.transforms.DataTransformFactory;


/**
 * Channel is an abstract high level XAL wrapper for a native process variable (PV) channel.
 * Subclasses provide native implementations.
 *
 * @author  Christopher K. Allen
 * @author Tom Pelaia
 * @version 1.1
 */
abstract public class Channel {
    /** Static variables */
    static protected ChannelSystem channelSystem;
    
    /**  Local Attributes */
    protected String          m_strId;                // channel name
    protected double          m_dblTmIO;              // pend IO timeout
    protected double          m_dblTmEvt;             // pend event timeout
    private ValueTransform valueTransform;            // transform between raw and physical values
    
    /** Notify listeners when connection is made or dropped */
    protected ConnectionListener connectionProxy;
    
    
    /** One Message Center for all Channel events */
    static protected MessageCenter messageCenter;

    
    /** hold connection status */
    protected volatile boolean connectionFlag;
	
	/** indicates whether this channel is marked as being valid */
	private boolean _valid;


    static {
        channelSystem = ChannelFactory.defaultSystem();
        messageCenter = new MessageCenter("Channel Message Center");
    }
    
    
	/** flush IO requests */
	public static void flushIO() {
		channelSystem.flushIO();
	}
	

    /**
     *  Flush the EPICS Channel Access request buffer and return as soon as complete or timeout has
     *  expired.  
     *
     *  Must use a pendX() function if synchronous request queuing is on!
     *
     *  Requests include Channel.connect() and Channel.getVal()
     *  @param    timeout      time to wait before giving up
     *  @return   false if time out occurs
     */
    public static boolean pendIO( final double timeout )    {
        return channelSystem.pendIO(timeout);
    }
    
    
    /**
     *  Flush the EPICS Channel Access request buffer and wait for asyncrhonous event.  This function
     *  blocks until the time out has expired!  Neither will it return until the channel access queue
     *  has been processed.
     *
     *  Must use a pendX() function if synchronous request queuing is on!
     *
     *  Requests include Channel.connectCallback Channel.getValCallback(), Channel.putValCallback and
     *  all monitor events.
     *  @param  timeout      time to wait before giving up
     */
    public static void pendEvent( final double timeout )  {
        channelSystem.pendEvent(timeout);
    }
    
    
    
    /***************************************************************************
     *  Constructors
     */
    
    /**  Creates empty Channel */
    protected Channel()    {
        this( null );
    }
    
    
    /** 
     *  Creates new Channel 
     *  @param  name     EPICS channel name
     */
    protected Channel(String name) {        
        this( name, ValueTransform.noOperationTransform );
    }
    
    
    /**
     * Create a new Channel
     * @param name The EPICS PV name
     * @param aTransform The transform to apply to PV values
     */
    protected Channel( String name, ValueTransform aTransform ) {
        // Initialize attributes
		_valid = true;		// by default a channel is valid unless marked otherwise
        connectionFlag = false;
        m_strId   = name;
        setValueTransform( aTransform );
    }
	
	
	/**
	 * From the default channel factory, get a channel for the specified signal name.
	 * @param signalName the PV for which to get the channel
	 * @return a channel for the specified PV
	 */
	static public Channel getInstance( final String signalName ) {
		return ChannelFactory.defaultFactory().getChannel( signalName );
	}
	
	
	/**
	 * From the default channel factory, get a channel for the specified signal name and value transform.
	 * @param signalName the PV for which to get the channel
	 * @param transform to transfrom the value between raw and physical
	 * @return a channel for the specified PV
	 */
	static public Channel getInstance( final String signalName, final ValueTransform transform ) {
		return ChannelFactory.defaultFactory().getChannel( signalName, transform );
	}


	/** 
	 * set whether this channel is valid 
	 * @param valid marks whether the channel is valid (true) or not (false)
	 */
	public void setValid( final boolean valid ) {
		_valid = valid;
	}


	/** 
	 * determine whether this channel is valid 
	 * @return true if it the channel is valid and false if not
	 */
	public boolean isValid() {
		return _valid;
	}

    
    /**
     * Set a value transform for this channel.
     * @param aTransform The transform to use for this channel.
     */
    void setValueTransform(ValueTransform aTransform) {
        valueTransform = aTransform;
    }
    
    
    /**
     * Get the value transform applied to this channel.
     * @return The value transform applied to this channel.
     */
    public ValueTransform getValueTransform() {
        return valueTransform;
    }
    
    
    /** 
	 * Add a listener of connection changes 
	 * @param listener to register for connection events
	 */
    public void addConnectionListener( final ConnectionListener listener ) {
        if ( connectionProxy == null ) {
            connectionProxy = messageCenter.registerSource(this, ConnectionListener.class);
        }
        messageCenter.registerTarget(listener, this, ConnectionListener.class);
		if ( isConnected() )  listener.connectionMade(this);	// immediately post to new listener
    }
    
    
    /** 
	 * Remove a listener of connection changes 
	 * @param listener to remove from receiving connection events
	 */
    public void removeConnectionListener( final ConnectionListener listener ) {
        messageCenter.removeTarget(listener, this, ConnectionListener.class);
    }
    
    
    /**
     * Return a unique identifier of this channel so as to distinguish 
     * channels which share the same PV but have different transforms.
     * @return A channel identifier built from the PV and value transform
     */
    public String getId() {
		return generateId( this.channelName(), valueTransform );
    }
	
	
	/** generate an ID for a channel transform pair */
	static String generateId( final String signal, final ValueTransform transform ) {
        if ( transform != null && transform != ValueTransform.noOperationTransform ) {
            return signal + "_transform" + transform.hashCode();
        }
        else {
            return signal;
        }
	}
    
    
    /**
     *  Returns EPICS channel name for process variable
     *  @return     string descriptor for EPICS channel
     */
    public String  channelName() { 
        return m_strId; 
    }

    
    /**
     *  Set the EPICS channel name for the connection
     *  @param  strNameChan     EPICS channel name
     */
    public void setChannelName(String strNameChan)  { 
        m_strId = strNameChan; 
    }
    
    
    public static synchronized void setDebugMode(boolean bDebug) {
        channelSystem.setDebugMode(bDebug);
    }
     
         
    /**
     *  Set the channel access Pend IO timeout
     *  @param  dblTm       I/O timeout
     */
    public void setIoTimeout(double dblTm)      { m_dblTmIO = dblTm; }

    /**
     *  Set the channel access Pend Event timeout
     *  @param  dblTm       event timeout
     */
    public void setEventTimeout(double dblTm)   { m_dblTmEvt = dblTm; }
    
    /**
     *  Get the channel access Pend IO timeout
     *  @return       I/O timeout
     */
    public double getIoTimeout()      { return m_dblTmIO; }

    /**
     *  Get the channel access Pend Event timeout
     *  @return       event timeout
     */
    public double getEventTimeout()   { return m_dblTmEvt; }
	
	
	/**
	 * Connect and wait the default timeout.
	 * @return true if the connection was made within the timeout and false if not
	 */
	public boolean connectAndWait() {
		return connectAndWait( m_dblTmIO );
	}
	
	
	/**
	 * Request a new connection and wait for it no longer than the timeout.
	 * @param timeout seconds to wait for a connection before giving up
	 * @return true if the connection was made within the timeout and false if not
	 */
	abstract public boolean connectAndWait( final double timeout );
		
	
	/**
	 * Request that the channel be connected.  Connections are made in the background
	 * so this method returns immediately upon making the request.  The connection will be
	 * made in the future as soon as possible.  A connection event will be sent to registered
	 * connection listeners when the connection has been established.
	 */
	abstract public void requestConnection();

    
    /**
     *  Terminate the network channel connection and clear all events associated
     *  with process variable
     */
    abstract public void disconnect();    
        
    
    /**
     *  Checks if channel is connected to process variable
     *  @return     true if connected
     */
    public boolean isConnected()    { 
        return connectionFlag;
    }

    
    /** 
	 * Checks for process variable channel connection and throws a ConnectionException if absent. 
	 * @throws xal.ca.ConnectionException accordingly
	 */
    public void checkConnection() throws ConnectionException  {
        if ( !connectAndWait() ) {
            throw new ConnectionException(this, "Channel Error - The channel \"" + m_strId + "\" must be connected to use this feature.");
        }
    }
    

    /**
     * Checks for process variable channel connection and throws a ConnectionException if absent after attempting a connection if necessary.
     * @param  methodName     name of method using connection
	 * @throws xal.ca.ConnectionException accordingly
     */
    protected void checkConnection( final String methodName ) throws ConnectionException  {
		checkConnection( methodName, true );
    }
    
	
    /**
	 * Checks for process variable channel connection and throws a ConnectionException if absent.
     * @param methodName     name of method using connection
	 * @param attemptConnection indicates whether or not to attempt a blocking connection request
	 * @throws xal.ca.ConnectionException accordingly
     */
    protected void checkConnection( final String methodName, final boolean attemptConnection ) throws ConnectionException  {
		if ( !isConnected() ) {
			if ( attemptConnection ) {
				connectAndWait();
				checkConnection( methodName, false );
			}
			else {
				throw new ConnectionException( this, "Channel#" + methodName + " - The channel \"" + m_strId + "\" must be connected to use this feature." );
			}
		}
    }
    
    
    /*
     *  Native Properties of Channel Process Variable
     */
    
    
    /** 
	 * get the Java class associated with the native type of this channel 
	 * @return the native element type
	 * @throws xal.ca.ConnectionException accordingly
	 */
    abstract public Class<?> elementType() throws ConnectionException;
    
    
    /**
     * Return size of value array associated with process variable
     * @return     number of values in process variable
	 * @throws xal.ca.ConnectionException accordingly
     */
    abstract public int elementCount() throws ConnectionException;

    
    /**
     *  Determine if channel has read access to process variable
     *  @return             true if channel has read access
     *
     *  @exception  ConnectionException     channel not connected
     */
    
    abstract public boolean readAccess() throws ConnectionException;
    

    /**
     *  Determine if channel has write access to process variable
     *  @return             true if channel has write access
     *
     *  @exception  ConnectionException     channel not connected
     */
    abstract public boolean writeAccess() throws ConnectionException;

    
    /** 
	 * Convenience method which returns the units for this channel. 
	 * @return the units
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException	accordingly
	 */
    abstract public String getUnits() throws ConnectionException, GetException;
    
    
    /** 
     * Get the lower and upper operation limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    abstract public String[] getOperationLimitPVs();
    
    
    /** 
     * Get the lower and upper warning limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    abstract public String[] getWarningLimitPVs();
    
    
    /** 
     * Get the lower and upper alarm limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    abstract public String[] getAlarmLimitPVs();
    
    
    /** 
     * Get the lower and upper drive limit PVs 
     * @return two element array of PVs with the lower and upper limit PVs 
     */
    abstract public String[] getDriveLimitPVs();
    

    /** 
	 * Convenience method which returns the upper display limit. 
	 * @return the raw upper display limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawUpperDisplayLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the lower display limit. 
	 * @return the raw lower display limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawLowerDisplayLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the upper alarm limit. 
	 * @return the raw upper alarm limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawUpperAlarmLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the lower alarm limit. 
	 * @return the raw lower alarm limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawLowerAlarmLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the upper warning limit. 
	 * @return the raw upper warning limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawUpperWarningLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the lower warning limit. 
	 * @return the raw lower warning limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawLowerWarningLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the upper control limit. 
	 * @return the raw upper control limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawUpperControlLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the lower control limit. 
	 * @return the raw lower control limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    abstract public Number rawLowerControlLimit() throws ConnectionException, GetException;
    
    
    /** 
	 * Convenience method which returns the upper display limit. 
	 * @return the upper display limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number upperDisplayLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawUpperDisplayLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the lower display limit. 
	 * @return the lower display limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number lowerDisplayLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawLowerDisplayLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the upper alarm limit. 
	 * @return the upper alarm limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number upperAlarmLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawUpperAlarmLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the lower alarm limit. 
	 * @return the lower alarm limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number lowerAlarmLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawLowerAlarmLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the upper warning limit. 
	 * @return the upper warning limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number upperWarningLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawUpperWarningLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the lower warning limit. 
	 * @return the lower warning limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number lowerWarningLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawLowerWarningLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the upper control limit. 
	 * @return upper control limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number upperControlLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawUpperControlLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /** 
	 * Convenience method which returns the lower control limit. 
	 * @return the lower control limit
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public Number lowerControlLimit() throws ConnectionException, GetException {
        ArrayValue rawValue = ArrayValue.numberStore( rawLowerControlLimit() );
        return valueTransform.convertFromRaw(rawValue);
    }
    
    
    /**
     *  Get channel value
     *  @return             value of the PV
     *
     *  @exception  ConnectionException channel not connected
     *  @exception  GetException        general channel access PV get failure
     */
    public byte getValByte() throws ConnectionException, GetException    {
        return getValueRecord().byteValue();
    }

    public int getValEnum() throws ConnectionException, GetException    {
        return getValueRecord().shortValue();
    }

    public int getValInt() throws ConnectionException, GetException    {
        return getValueRecord().intValue();
    }

    public float getValFlt() throws ConnectionException, GetException    {
        return getValueRecord().floatValue();
    }

    public double getValDbl() throws ConnectionException, GetException    {
        return getValueRecord().doubleValue();
    }
	
    public String getValString() throws ConnectionException, GetException    {
        return getStringValueRecord().stringValue();
    }
    
    

    /**
     *  Get channel value as array
     *  @return             value array of the PV
     *  @exception  ConnectionException channel not connected
     *  @exception  GetException        general channel access PV get failure
     */
    public byte[] getArrByte() throws ConnectionException, GetException    {
        return getValueRecord().byteArray();
    }
    
    public int[] getArrInt() throws ConnectionException, GetException    {
        return getValueRecord().intArray();
    }
    
    public float[] getArrFlt() throws ConnectionException, GetException    {
        return getValueRecord().floatArray();
    }
    
    public double[] getArrDbl() throws ConnectionException, GetException    {
        return getValueRecord().doubleArray();
    }
	
    public String[] getArrString() throws ConnectionException, GetException    {
        return getStringValueRecord().stringArray();
    }
    
    
    /**
     * Fetch the data value for the channel and return it as an ArrayValue.
	 * @return channel's array value
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    public ArrayValue getArrayValue() throws ConnectionException, GetException {
        return getValueRecord().arrayValue();
    }
    
    
    /**
     * Return a raw <code>ChannelRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of the PV.
	 * @return raw channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    abstract public ChannelRecord getRawValueRecord()  throws ConnectionException, GetException;
	
	
	/**
	 * Get a <code>ChannelRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
	abstract protected ChannelRecord getRawStringValueRecord()  throws ConnectionException, GetException;
	
	
	/**
	 * Get a <code>ChannelStatusRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
	abstract protected ChannelStatusRecord getRawStringStatusRecord()  throws ConnectionException, GetException;
	
	
	/**
	 * Get a <code>ChannelTimeRecord</code> representing the fetched record for the specified type.
	 * @return the channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
	abstract protected ChannelTimeRecord getRawStringTimeRecord()  throws ConnectionException, GetException;
	
    
    /**
     * Return a raw <code>ChannelStatusRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of
     * the PV along with status.
	 * @return raw channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    abstract public ChannelStatusRecord getRawStatusRecord()  throws ConnectionException, GetException;

    
    /**
     * Return a raw <code>ChannelTimeRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of
     * the PV along with status and timestamp.
	 * @return raw channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    abstract public ChannelTimeRecord getRawTimeRecord()  throws ConnectionException, GetException;

    
    /**
     * Return a <code>ChannelRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of the PV.
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    final public ChannelRecord getValueRecord()  throws ConnectionException, GetException {
        return getRawValueRecord().applyTransform( valueTransform );
    }
	
    
    /**
	 * Get a <code>ChannelRecord</code> representing the fetched record for the native type of this channel. 
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public ChannelRecord getStringValueRecord()  throws ConnectionException, GetException {
        return getRawStringValueRecord().applyTransform( valueTransform );
    }
	
    
    /** 
	 * Get a <code>ChannelStatusRecord</code> representing the fetched record for the native type of this channel. 
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public ChannelRecord getStringStatusRecord()  throws ConnectionException, GetException {
        return getRawStringStatusRecord().applyTransform( valueTransform );
    }
	
    
    /** 
	 * Get a <code>ChannelTimeRecord</code> representing the fetched record for the native type of this channel. 
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
    final public ChannelRecord getStringTimeRecord()  throws ConnectionException, GetException {
        return getRawStringTimeRecord().applyTransform( valueTransform );
    }
	
    
    /**
     * Return a <code>ChannelStatusRecord</code> representing the fetched record for the 
     * native type of this channel.  This is a convenient way to get the value of the PV along with status.
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    final public ChannelStatusRecord getStatusRecord()  throws ConnectionException, GetException {
        ChannelStatusRecord record = getRawStatusRecord();
        record.applyTransform( valueTransform );
        return record;
    }

    
    /**
     * Return a <code>ChannelTimeRecord</code> representing the fetched record for the native type of this channel.
     * This is a convenient way to get the value of the PV along with status and timestamp.
	 * @return channel record
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    final public ChannelTimeRecord getTimeRecord()  throws ConnectionException, GetException {
        ChannelTimeRecord record = getRawTimeRecord();
        record.applyTransform( valueTransform );
        return record;
    }
	
    
    /**
	 * Handle a callback for getting the raw value for the channel.
     * @param listener The receiver of the callback.
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    abstract protected void getRawValueCallback( final IEventSinkValue listener ) throws ConnectionException, GetException;
	
    
    /**
     * Handle a callback for getting the raw value for the channel.
     * @param listener The receiver of the callback.
	 * @param attemptConnection indicates whether or not to attempt a blocking connection if this channel is not connected
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
     */
    abstract protected void getRawValueCallback( final IEventSinkValue listener, final boolean attemptConnection ) throws ConnectionException, GetException;
	
	
	/** 
	 * Submit a non-blocking Get request with callback 
	 * @param listener to receive callback upon completion
	 * @param attemptConnection true to attempt connection and false not to attempt connection
	 * @throws xal.ca.ConnectionException accordingly
	 * @throws xal.ca.GetException accordingly
	 */
	abstract public void getRawValueTimeCallback( final IEventSinkValTime listener, final boolean attemptConnection ) throws ConnectionException, GetException;

    
    /**
	 *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValueCallback( final IEventSinkValue listener ) throws ConnectionException, GetException {
		getValueCallback( listener, true );
    }
    
    
    /**
	 * Get the value of the process variable via a callback to the specified listener.
     * @param  listener     receiver of the callback event.
	 * @param attemptConnection indicates whether or not to attempt a blocking connection if this channel is not connected
     * @throws  xal.ca.ConnectionException     channel is not connected
     * @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValueCallback( final IEventSinkValue listener, final boolean attemptConnection ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue( final ChannelRecord record, final Channel channel ) {
                listener.eventValue( record.applyTransform(valueTransform), Channel.this );
            }
        }, attemptConnection );
    }
	
    
    /**
	 * Get the value time record of the process variable via a callback to the specified listener.
     * @param  listener     receiver of the callback event.
	 * @param attemptConnection indicates whether or not to attempt a blocking connection if this channel is not connected
     * @throws  xal.ca.ConnectionException     channel is not connected
     * @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValueTimeCallback( final IEventSinkValTime listener, final boolean attemptConnection ) throws ConnectionException, GetException {
        getRawValueTimeCallback( new IEventSinkValTime() {
            public void eventValue( final ChannelTimeRecord record, final Channel channel ) {
				record.applyTransform( valueTransform );
                listener.eventValue( record, Channel.this );
            }
        }, attemptConnection );
    }

    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValByteCallback( final IEventSinkValByte listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventValue(record.applyTransform(valueTransform).byteValue(), Channel.this);
            }
        });
    }

    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValIntCallback( final IEventSinkValInt listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventValue(record.applyTransform(valueTransform).intValue(), Channel.this);
            }
        });
    }

    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValFltCallback( final IEventSinkValFlt listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
				final float value = record.applyTransform( valueTransform ).floatValue();
				listener.eventValue( value, Channel.this );
            }
        });
    }
    
    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getValDblCallback( final IEventSinkValDbl listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
				final double value = record.applyTransform( valueTransform ).doubleValue();
				listener.eventValue( value, Channel.this );
            }
        });
    }
    
    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getArrByteCallback( final IEventSinkArrByte listener) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventArray(record.applyTransform(valueTransform).byteArray(), Channel.this);
            }
        });
    }
    
    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getArrIntCallback( final IEventSinkArrInt listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventArray(record.applyTransform(valueTransform).intArray(), Channel.this);
            }
        });
    }
    
    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getArrFltCallback( final IEventSinkArrFlt listener ) throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventArray(record.applyTransform(valueTransform).floatArray(), Channel.this);
            }
        });
    }
    
    
    /**
     *  Get the value of the process variable via a callback to the specified listener.
     *  @param  listener     receiver of the callback event.
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.GetException            general channel access failure
     */
    final public void getArrDblCallback( final IEventSinkArrDbl listener )  throws ConnectionException, GetException {
        getRawValueCallback( new IEventSinkValue() {
            public void eventValue(final ChannelRecord record, Channel channel) {
                listener.eventArray(record.applyTransform(valueTransform).doubleArray(), Channel.this);
            }
        });
    }
    
    
    /**
     *  Setup a value-status-timestamp monitor on this channel 
     *  @param  listener     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired or'ed combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return A new monitor
     *  @throws xal.ca.ConnectionException     channel is not connected
     *  @throws xal.ca.MonitorException        general monitor failure
     */
    abstract public xal.ca.Monitor addMonitorValTime(IEventSinkValTime listener, int intMaskFire)
        throws ConnectionException, MonitorException;

    
    /**
     *  Setup a value-status monitor on this channel 
     *  @param  listener     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired or'ed combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return A new monitor
     *  @throws xal.ca.ConnectionException     channel is not connected
     *  @throws xal.ca.MonitorException        general monitor failure
     */
    abstract public xal.ca.Monitor addMonitorValStatus(IEventSinkValStatus listener, int intMaskFire)
        throws ConnectionException, MonitorException;
    
    
    /**
     *  Setup a value monitor on this channel 
     *  @param  listener     interface to data sink
     *  @param  intMaskFire code specifying when the monitor is fired or'ed combination of {Monitor.VALUE, Monitor.LOG, Monitor.ALARM}
     *  @return A new monitor
     *  @throws xal.ca.ConnectionException     channel is not connected
     *  @throws xal.ca.MonitorException        general monitor failure
     */
    abstract public xal.ca.Monitor addMonitorValue(IEventSinkValue listener, int intMaskFire)
        throws ConnectionException, MonitorException;
    

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(String newVal) throws ConnectionException, PutException    {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(byte newVal) throws ConnectionException, PutException   {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(short newVal) throws ConnectionException, PutException   {
        this.putValCallback(newVal, null);
    }
    
    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(int newVal) throws ConnectionException, PutException  {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(float newVal) throws ConnectionException, PutException   {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(double newVal) throws ConnectionException, PutException    {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(byte[] newVal) throws ConnectionException, PutException   {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(short[] newVal) throws ConnectionException, PutException    {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(int[] newVal) throws ConnectionException, PutException   {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(float[] newVal) throws ConnectionException, PutException    {
        this.putValCallback(newVal, null);
    }

    
    /**
     *  Synchronously put a value to the channel process variable.
     *  @param  newVal      value sent to process variable
     *  @throws  xal.ca.ConnectionException     channel is not connected
     *  @throws  xal.ca.PutException            channel access failure, including
     */
    public void putVal(double[] newVal) throws ConnectionException, PutException    {
        this.putValCallback(newVal, null);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(String newVal, PutListener listener) throws ConnectionException, PutException {
        String rawValue = valueTransform.convertToRaw( ArrayValue.stringStore(newVal) ).stringValue();
        putRawValCallback(rawValue, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException {
        byte rawValue = valueTransform.convertToRaw( ArrayValue.byteStore(newVal) ).byteValue();
        putRawValCallback(rawValue, listener);
    }

    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(short newVal, PutListener listener) throws ConnectionException, PutException {
        short rawValue = valueTransform.convertToRaw( ArrayValue.shortStore(newVal) ).shortValue();
        putRawValCallback(rawValue, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(int newVal, PutListener listener) throws ConnectionException, PutException {
        int rawValue = valueTransform.convertToRaw( ArrayValue.intStore(newVal) ).intValue();
        putRawValCallback(rawValue, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(float newVal, PutListener listener) throws ConnectionException, PutException {
        float rawValue = valueTransform.convertToRaw( ArrayValue.floatStore(newVal) ).floatValue();
        putRawValCallback(rawValue, listener);
    }

    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(double newVal, PutListener listener) throws ConnectionException, PutException {
        double rawValue = valueTransform.convertToRaw( ArrayValue.doubleStore(newVal) ).doubleValue();
        putRawValCallback(rawValue, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException {
        byte[] rawArray = valueTransform.convertToRaw( ArrayValue.byteStore(newVal) ).byteArray();
        putRawValCallback(rawArray, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException {
        short[] rawArray = valueTransform.convertToRaw( ArrayValue.shortStore(newVal) ).shortArray();
        putRawValCallback(rawArray, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException {
        int[] rawArray = valueTransform.convertToRaw( ArrayValue.intStore(newVal) ).intArray();
        putRawValCallback(rawArray, listener);
    }
    
    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException {
        float[] rawArray = valueTransform.convertToRaw( ArrayValue.floatStore(newVal) ).floatArray();
        putRawValCallback(rawArray, listener);
    }

    
    /**
     * Asynchronously put a value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    final public void putValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException {
        double[] rawArray = valueTransform.convertToRaw( ArrayValue.doubleStore(newVal) ).doubleArray();
        putRawValCallback(rawArray, listener);
    }

    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(String newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(byte newVal, PutListener listener) throws ConnectionException, PutException;

    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(short newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(int newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(float newVal, PutListener listener) throws ConnectionException, PutException;

    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(double newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(byte[] newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(short[] newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(int[] newVal, PutListener listener) throws ConnectionException, PutException;
    
    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(float[] newVal, PutListener listener) throws ConnectionException, PutException;

    
    /**
     * Asynchronously put a raw value to the channel process variable.  Fire the specified callback
     * when put is complete.
     * @param  newVal      value sent to process variable
     * @param  listener The receiver of the callback event
     * @throws xal.ca.ConnectionException     channel is not connected
     * @throws xal.ca.PutException        general put failure
     */
    abstract public void putRawValCallback(double[] newVal, PutListener listener) throws ConnectionException, PutException;
}

