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
	private String _command;
	
	/** kind of application */
	private String _kind;
	
	/** indicates whether to exclude matching files */
	private boolean _excludes;
	
	
	/** Primary Constructor */
	public Rule( final String pattern, final String kind, final String command, final boolean excludes ) {
		setPattern( pattern );
		setKind( kind );
		setCommand( command );
		setExcludes( excludes );
	}
	
	
	/** Constructor */
	public Rule( final String pattern, final String kind, final String command ) {
		this( pattern, kind, command, false );
	}
	
	
	/** Constructor */
	public Rule( final String pattern, final String command ) {
		this( pattern, "", command );
	}
	
	
	/** Constructor */
	public Rule() {
		this( "*.xyz", "runxyz" );
	}
	
	
	/** create an instance from a data adaptor */
	static public Rule getInstance( final DataAdaptor adaptor ) {
		final String pattern = adaptor.stringValue( "pattern" );
		final String command = adaptor.stringValue( "command" );
		final String kind = adaptor.hasAttribute( "kind" ) ? adaptor.stringValue( "kind" ) : "";
		final boolean excludes = adaptor.hasAttribute( "excludes" ) ? adaptor.booleanValue( "excludes" ) : false;
		return new Rule( pattern, kind, command, excludes );
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
	
	
	/** get the command */
	public String getCommand() {
		return _command;
	}
	
	
	/** get the command */
	public String getCommand( final App application ) {
		return _command.replace( "%f", application.getPath() );
	}
	
	
	/** set the command */
	public void setCommand( final String command ) {
		_command = command;
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
		setPattern( adaptor.stringValue( "pattern" ) );
		setCommand( adaptor.stringValue( "command" ) );
		setKind( adaptor.hasAttribute( "kind" ) ? adaptor.stringValue( "kind" ) : "" );
		setExcludes( adaptor.hasAttribute( "excludes" ) ? adaptor.booleanValue( "excludes" ) : false );
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "pattern", _pattern );
		adaptor.setValue( "command", _command );
		adaptor.setValue( "kind", _kind );
		adaptor.setValue( "excludes", _excludes );
    }
}
