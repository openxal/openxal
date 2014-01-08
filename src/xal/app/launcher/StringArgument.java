//
//  StringArgument.java
//  xal
//
//  Created by Tom Pelaia on 1/6/2014.
//  Copyright 2014 Oak Ridge National Lab. All rights reserved.
//


package xal.app.launcher;

import java.util.*;


/** string argument which is editable and suitable for a table model */
class StringArgument {
	/** value of the argument */
	private String _value;


	/** Constructor */
	public StringArgument( final String value ) {
		_value = value;
	}


	/** convert a list of arguments to a list of strings */
	static public List<String> toStrings( final List<StringArgument> arguments ) {
		final List<String> strings = new ArrayList<>();

		for ( final StringArgument argument : arguments ) {
			strings.add( argument.getValue() );
		}

		return strings;
	}


	/** convert a list of strings to a list of string arguments */
	static public List<StringArgument> toArguments( final List<String> strings ) {
		final List<StringArgument> arguments = new ArrayList<>();

		for ( final String string : strings ) {
			arguments.add( new StringArgument( string ) );
		}

		return arguments;
	}


	/** Get the argument's value */
	public String getValue() {
		return _value;
	}


	/** set the argument's value */
	public void setValue( final String value ) {
		_value = value;
	}


	/** Get the string value */
	public String toString() {
		return _value;
	}
}
