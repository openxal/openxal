/*
 * Copyright (c) 2015 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package xal.plugin.jca.server;

import gov.aps.jca.CAException;
import gov.aps.jca.CAStatus;
import gov.aps.jca.CAStatusException;
import gov.aps.jca.cas.ProcessVariableEventCallback;
import gov.aps.jca.cas.ProcessVariableWriteCallback;
import gov.aps.jca.cas.ServerChannel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.STS;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.dbr.TimeStamp;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;

/**
 * {@link ServerMemoryProcessVariable} (PV) extends {@link MemoryProcessVariable} and overrides its {@link #write}
 * method, to disable writing to read-only channels.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class ServerMemoryProcessVariable extends MemoryProcessVariable {
    /**
     * Indicates if {@link ServerMemoryProcessVariable} is settable or not. False by default.
     */
    private boolean settable = false;
    
    /**
     * Creates and registers a PV (possibly readonly) on the channel server.
     * 
     * 
     * @param name	name of the PV.
	 * @param eventCallback	event callback, where to report value changes if <code>interest</code> is <code>true</code>.
	 * @param initialValue	initial value, array is expected.
	 * 
     * @see MemoryProcessVariable
     */
    public ServerMemoryProcessVariable(String name, ProcessVariableEventCallback eventCallback, Object initialValue, DefaultServerImpl channelServer) {
        super(name, eventCallback, getType(initialValue), initialValue);
        channelServer.registerProcessVaribale(this);
    }

    private static DBRType getType(Object initialValue) {
		Class<?> componentType = initialValue.getClass().getComponentType();
    	// no enums
	    if ( componentType == Byte.TYPE ) 
	        return DBRType.BYTE;	    
	    else if ( componentType == Short.TYPE ) 
	        return DBRType.SHORT;    
	    else if ( componentType == Integer.TYPE ) 
	        return DBRType.INT;
	    else if ( componentType == Float.TYPE ) 
	        return DBRType.FLOAT;
	    else if ( componentType == Double.TYPE ) 
	        return DBRType.DOUBLE;  
	    else if ( componentType == java.lang.String.class ) 
	        return DBRType.STRING;
	    else 
	    	return DBRType.UNKNOWN;
    }

	/**
     * Calls parent {@link MemoryProcessVariable.write} if settable is true. Else returns {@link CAStatus.NOWTACCESS}.
     */
    public synchronized CAStatus write(DBR value, ProcessVariableWriteCallback asyncWriteCallback) throws CAException {

        if (settable) {        	
    	    return super.write(value, asyncWriteCallback);    	    
        } else {
            return CAStatus.NOWTACCESS;
        }
    }

    /**
     * Sets PV's value. Meant only for {@link JcaServerChannel}.
     * 
     * @param value
     *            value to set.
     */
    public synchronized void setValue(Object value) {
    	try {
    		type = getType(value);
			super.write(new DBR(value) {		// calling write method, so that all monitors are called
					@Override
					public DBR convert(DBRType convertType) throws CAStatusException {	return null; }
				}, null);
		} catch (CAException e) {			
		}
    }

    /**
     * Creates channel when PV is attached by a client. Returns {@link ServerChannel} with overridden {@link
     * writeAccess()} to return true when PV is {@link #settable} and false otherwise.
     */
    @Override
    public ServerChannel createChannel(int cid, int sid, String userName, String hostName) throws CAException {
        return new ServerChannel(this, cid, sid, userName, hostName) {

            @Override
            public boolean writeAccess() {
                return settable;
            }

        };
    }

    /**
     * Returns PV's value. Meant only for {@link JcaServerChannel}.
     * 
     * @return value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns PV's timestamp.
     * 
     * @return timestamp
     */
    public TimeStamp getTimestamp()
    {
    	return timestamp;
    }
    
    @Override
    public void fillInDBR(DBR value) {
    	super.fillInDBR(value);
    	if (value instanceof STS) {
    		STS val = (STS)value;
    		val.setStatus(Status.NO_ALARM);
    		val.setSeverity(Severity.NO_ALARM);
    	}
    }
	
	public void setSettable(boolean settable) {
		this.settable = settable;
	}
}
