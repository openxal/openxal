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

	/** list of commands including arguments */
	private List<String> _commands;

	
	/** Empty Constructor */
	public HostConfiguration() {
		HOST_SETTINGS = new ArrayList<HostSetting>();
		ENABLED_HOST_SETTINGS = new ArrayList<HostSetting>();
		_commands = new ArrayList<>();
	}
	
	
	/** get the commands */
	public List<String> getCommands() {
		return _commands;
	}
	
	
	/** set the commands */
	public void setCommands( final List<String> commands ) {
		_commands = commands;
	}
	
	
	/** determine if a host is required for a command */
	public boolean isHostRequired() {
		// host is required if any command depends on host substitution
		for ( final String command : _commands ) {
			if ( command.contains( "%h" ) )  return true;
		}

		// no command depends on host substitution
		return false;
	}
	
	
	/** generate a host command based on a local executable expression by substituting the host for %h and the executable for %e */
	public List<String> getCommands( final String host, final List<String> executable ) {
		final List<String> substitutedCommands = new ArrayList<>();

		for ( final String command : _commands ) {
			if ( command.equals( "%e" ) ) {		// replace the command with the executable's list of commands
				substitutedCommands.addAll( executable );
			}
			else {			// substitute the host in each command wherever %h occurs
				substitutedCommands.add( command.replace( "%h", host ) );
			}
		}

		return substitutedCommands;
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
		final List<String> commands = new ArrayList<>(1);
		commands.add( "%e" );
		setCommands( commands );
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
		// commands are specified in one of two styles
		// 1) new style is a series of "command" elements nested inside of a single "commands" array element
		// 2) old style is a single command line specified with a single "commandTemplate" element
		if ( adaptor.hasAttribute( "commands" ) ) {
			final List<DataAdaptor> commandAdaptors = adaptor.childAdaptors( "commands" );
			final List<String> commands = new ArrayList<String>( commandAdaptors.size() );
			for ( final DataAdaptor commandAdaptor : commandAdaptors ) {
				commands.add( commandAdaptor.stringValue( "command" ) );
			}
			setCommands( commands );
		}
		else if ( adaptor.hasAttribute( "commandTemplate" ) ) {		// old style
			// if the command is specified as a single line then split it by white space to get the command array
			final String commandLine = adaptor.stringValue( "commandTemplate" );
			if ( commandLine != null && commandLine.length() > 0 ) {
				final String[] commandLineArray = commandLine.split( "\\w" );
				final List<String> commands = new ArrayList<>( commandLineArray.length );
				for ( final String command : commandLineArray ) {
					commands.add( command );
				}
				setCommands( commands );
			}
			else {
				setCommands( new ArrayList<String>() );
			}
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
		final DataAdaptor commandsAdaptor = adaptor.createChild( "commands" );
		for ( final String command : _commands ) {
			final DataAdaptor commandAdaptor = commandsAdaptor.createChild( "command" );
			commandAdaptor.setValue( "command", command );
		}
		adaptor.writeNodes( HOST_SETTINGS );
    }
}
