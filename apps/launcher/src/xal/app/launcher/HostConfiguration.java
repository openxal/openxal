//
//  HostConfiguration.java
//  xal
//
//  Created by Thomas Pelaia on 9/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

import java.util.ArrayList;
import java.util.List;


/** host configuration */
public class HostConfiguration implements DataListener {
	/** the data adaptor label used for reading and writing this instance */
	static public final String DATA_LABEL = "HostConfiguration";
	
	/** host settings */
	final protected List<HostSetting> HOST_SETTINGS;
	
	/** enabled host settings */
	final private List<HostSetting> ENABLED_HOST_SETTINGS;
	
	/** template for executing commands on a host */
	private String _commandTemplate;
	
	
	/** Empty Constructor */
	public HostConfiguration() {
		HOST_SETTINGS = new ArrayList<HostSetting>();
		ENABLED_HOST_SETTINGS = new ArrayList<HostSetting>();
	}
	
	
	/** get the command template */
	public String getCommandTemplate() {
		return _commandTemplate;
	}
	
	
	/** set the command template */
	public void setCommandTemplate( final String template ) {
		_commandTemplate = template;
	}
	
	
	/** determine if a host is required for a command */
	public boolean isHostRequired() {
		return _commandTemplate.contains( "%h" );
	}
	
	
	/** generate a host command based on a local executable expression by substituting the host for %h and the executable for %e */
	public String getCommand( final String host, final String executable ) {
		return _commandTemplate.replace( "%h", host ).replace( "%e", executable );
	}
	
	
	/**
	 * Get the list of host settings
	 * @return host settings
	 */
	public List<HostSetting> getHostSettings() {
		return HOST_SETTINGS;
	}
	
	
	/**
	 * Get the hosts which are enabled
	 * @return list of enabled hosts
	 */
	public List<HostSetting> getEnabledHosts() {
		return ENABLED_HOST_SETTINGS;
	}
	
	
	/** Refresh the list of enabled hosts */
	public void refreshEnabledHosts() {
		final List<HostSetting> enabledHosts = new ArrayList<HostSetting>( HOST_SETTINGS.size() );
		
		for ( final HostSetting setting : HOST_SETTINGS ) {
			if ( setting.isEnabled() ) {
				enabledHosts.add( setting );
			}
		}
		
		ENABLED_HOST_SETTINGS.clear();
		ENABLED_HOST_SETTINGS.addAll( enabledHosts );
	}
  	
	
	/** preconfigure when initializing without a document file */
	public void preConfigure() {
		setCommandTemplate( "%e" );
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
		if ( adaptor.hasAttribute( "commandTemplate" ) ) {
			setCommandTemplate( adaptor.stringValue( "commandTemplate" ) );
		}
		
		final List<DataAdaptor> settingAdaptors = adaptor.childAdaptors( HostSetting.DATA_LABEL );
		HOST_SETTINGS.clear();
		for ( final DataAdaptor settingAdaptor : settingAdaptors ) {
			final HostSetting setting = new HostSetting();
			setting.update( settingAdaptor );
			HOST_SETTINGS.add( setting );
		}
		refreshEnabledHosts();
    }
    
    
    /**
	 * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "commandTemplate", _commandTemplate );
		adaptor.writeNodes( HOST_SETTINGS );
    }
}
