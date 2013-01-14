//
//  HostSetting.java
//  xal
//
//  Created by Thomas Pelaia on 9/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.data.*;


/** setting for a single host  */
public class HostSetting implements DataListener {
	/** the data adaptor label used for reading and writing this instance */
	static public final String DATA_LABEL = "HostSetting";
	
	/** name of the host */
	protected String _host;
	
	/** indicates whether the host is enabled */
	protected boolean _enabled;
	
	
	/** Primary Constructor */
	public HostSetting( final String host, final boolean enabled ) {
		_host = host;
		_enabled = enabled;
	}
	
	
	/** Copy Constructor */
	public HostSetting( final HostSetting setting ) {
		this( setting.getHost(), setting.isEnabled() );
	}
	
	
	/** Empty Constructor */
	public HostSetting() {
		this( "127.0.0.1", true );
	}
	
	
	/**
	 * get the host's address as a string
	 * @return the host's address as a string
	 */
	public String getHost() {
		return _host;
	}
	
	
	/**
	 * Set the host
	 * @param host the address of the host
	 */
	public void setHost( final String host ) {
		_host = host;
	}
	
	
	/**
	 * Determine if the host is enabled
	 * @return true if the host is enabled and false if not
	 */
	public boolean isEnabled() {
		return _enabled;
	}
	
	
	/**
	 * Set whether the host is enabled
	 * @param enabled true to enable the host and false to disable it
	 */
	public void setEnabled( final boolean enabled ) {
		_enabled = enabled;
	}
    
    
    /** 
	 * dataLabel() provides the name used to identify the class in an external data source.
	 * @return The tag for this data node.
	 */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
	 * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		_host = adaptor.stringValue( "host" );
		_enabled = adaptor.booleanValue( "enabled" );
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "host", _host );
		adaptor.setValue( "enabled", _enabled );
    }
}
