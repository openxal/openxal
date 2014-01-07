//
//  Rule.java
//  xal
//
//  Created by Tom Pelaia on 5/6/2009.
//  Copyright 2009 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.data.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


/** Rule for running an application with a specified wildcard pattern */
public class Rule implements DataListener {
	/** DataAdaptor label used in reading and writing */
	static public final String DATA_LABEL = "Rule";
	
	/** regular expression pattern against which to match file names */
	private Pattern _filePattern;
	
	/** file wildcard pattern */
	private String _pattern;
	
	/** command */
	private List<String> _commands;
	
	/** kind of application */
	private String _kind;
	
	/** indicates whether to exclude matching files */
	private boolean _excludes;
	
	
	/** Primary Constructor */
	public Rule( final String pattern, final String kind, final List<String> commands, final boolean excludes ) {
		setPattern( pattern );
		setKind( kind );
		setCommands( commands );
		setExcludes( excludes );
	}
	
	
	/** Constructor */
	public Rule( final String pattern, final String kind, final List<String> commands ) {
		this( pattern, kind, commands, false );
	}


	/** Constructor */
	public Rule( final String pattern, final String kind, final String ... commands ) {
		this( pattern, kind, toList( commands ) );
	}

	
	/** Constructor */
	public Rule() {
		this( "*.xyz", "xyz", "runxyz" );
	}


	/** Convert an array of String values to a list of strings */
	static private List<String> toList( final String ... values ) {
		final List<String> list = new ArrayList<>();

		for ( final String value : values ) {
			list.add( value );
		}

		return list;
	}
	
	
	/** create an instance from a data adaptor */
	static public Rule getInstance( final DataAdaptor adaptor ) {
		final Rule rule = new Rule( "", "", "" );
		rule.update( adaptor );
		return rule;
	}
	
	
	/** get the file wildcard pattern */
	public String getPattern() {
		return _pattern;
	}
	
	
	/** set the file wildcard pattern */
	public void setPattern( final String pattern ) {
		_pattern = pattern;
		final String regularExpression = pattern.replace( ".", "[.]" ).replace( "*", ".*" );
		_filePattern = Pattern.compile( regularExpression );
	}
	
	
	/** get the commands */
	public List<String> getCommands() {
		return _commands;
	}


	/** set the commands */
	public void setCommands( final List<String> commands ) {
		_commands = commands;
	}

	
	/** get the commands using the application's specific substitution for file path (%f) and name (%n) */
	public List<String> getCommands( final App application ) {
		final List<String> commands = new ArrayList<>( _commands.size() );
		for ( final String rawCommand : _commands ) {
			final String command = rawCommand.replace( "%f", application.getPath() ).replace( "%n", application.getLabel() );
			commands.add( command );
		}
		return commands;
	}


	/** insert a command at the specified index */
	public void insertCommandAtIndex( final String command, final int index ) {
		_commands.add( index, command );
	}


	/** delete the rule at the specified index */
	public void deleteCommandAtIndex( final int index ) {
		_commands.remove( index );
	}

	
	/** determine whether matching files are excluded */
	public boolean excludes() {
		return _excludes;
	}
	
	
	/** set whether matching files are excluded */
	public void setExcludes( final boolean excludes ) {
		_excludes = excludes;
	}
	
	
	/** get the kind */
	public String getKind() {
		return _kind;
	}
	
	
	/** set the kind */
	public void setKind( final String kind ) {
		_kind = kind;
	}
	
	
	/** determine whether this rule matches the specified application */
	public boolean matches( final App application ) {
		return matches( application.getFile() );
	}
	
	
	/** determine whether this rule matches the specified file */
	public boolean matches( final File file ) {
		return matchesFileName( file.getName() );
	}
	
	
	/** determine whether this rule matches the specified file name */
	public boolean matchesFileName( final String fileName ) {
		return _filePattern.matcher( fileName ).matches();
	}
	
    
    /** 
     * provides the name used to identify the class in an external data source.
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
		// fetch the rule's pattern
		setPattern( adaptor.stringValue( "pattern" ) );

		// commands are specified in one of two styles
		// 1) new style is a series of "command" elements nested inside of the Rules adaptor
		// 2) old style is a single command line specified as an attribute of the Rules adaptor
		final List<DataAdaptor> commandAdaptors = adaptor.childAdaptors( "command" );
		final int commandCount = commandAdaptors.size();
		if ( commandCount > 0 ) {
			final List<String> commands = new ArrayList<String>( commandCount );
			for ( final DataAdaptor commandAdaptor : commandAdaptors ) {
				commands.add( commandAdaptor.stringValue( "value" ) );
			}
			setCommands( commands );
		}
		else if ( adaptor.hasAttribute( "command" ) ) {		// old style
			// if the command is specified as a single line then split it by white space to get the command array
			final String commandLine = adaptor.stringValue( "command" );
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

		// fetch the rule kind and whether it is excluded
		setKind( adaptor.hasAttribute( "kind" ) ? adaptor.stringValue( "kind" ) : "" );
		setExcludes( adaptor.hasAttribute( "excludes" ) ? adaptor.booleanValue( "excludes" ) : false );
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "pattern", _pattern );

		for ( final String command : _commands ) {
			final DataAdaptor commandAdaptor = adaptor.createChild( "command" );
			commandAdaptor.setValue( "value", command );
		}

		adaptor.setValue( "kind", _kind );
		adaptor.setValue( "excludes", _excludes );
    }
}
