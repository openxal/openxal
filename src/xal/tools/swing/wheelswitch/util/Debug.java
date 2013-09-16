/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package com.cosylab.util;

/**
 * This class generalizes the debug output by providing additional
 * functionality to the System.out.println output, such as enabling, disabling
 * debug or indenting the output.<br>
 * All methods and fields are declared as static to simplify use.<br>
 * Indentation can be used to perform stack traces of the methods order of  execution.<br>
 * This class is intended to duplicate the C/C++ macro IFDEF functionality.
 * All debug routines will only be included in the compiled classes if
 * debugging is enabled via the ENABLE_DEBUG flag. If this flag is enabled,
 * most compilers, though not neccessary all, will simply ignore any
 * references to the Debug class. ENABLE_DEBUG flag cannot be changed during
 * runtime.
 *
 * @author <a href="mailto:ales.pucelj@cosylab.com">Ales Pucelj</a>
 * @version $id$
 */
public final class Debug
{
	// Global flag to turn debug on or off

	// Global flag to turn debug on or off
	public static final boolean ENABLE_DEBUG = false;

	// Ammount of indent
	private static int indent = 0;

	// Maximum number of predefined indents (represented as Strings)
	private static final int MAX_INDENTS = 10;

	// Indent size (default = 2 whitespaces)
	private static final String INDENTATION = "  ";

	// Cache of precalculated indents
	private static final String[] indents = new String[MAX_INDENTS];

	static {
		if (ENABLE_DEBUG) {
			for (int i = 0; i < MAX_INDENTS; i++) {
				indents[i] = "";

				for (int j = 0; j < i; j++) {
					indents[i] += INDENTATION;
				}
			}
		}
	}

	/**
	 * Returns a string that consists of nINDENTSIZE whitespaces.
	 *
	 * @param n n Indent ammount.
	 *
	 * @return String Indentation string.
	 */
	private static final String getIndentation(int n)
	{
		if (ENABLE_DEBUG) {
			String result = "";

			while (n >= MAX_INDENTS) {
				result = result + indents[MAX_INDENTS - 1];
				n -= MAX_INDENTS;
			}

			return result + indents[n];
		}

		return "";
	}

	private static synchronized final void dump(final String s)
	{
		System.out.print(getIndentation(indent));
		System.out.println(s);
	}

	/**
	 * Outputs the debug string if debug output is enabled.
	 *
	 * @param s String to output.
	 */
	public static final void out(final String s)
	{
		if (ENABLE_DEBUG) {
			dump(s);
		}
	}

	/**
	 * Outputs the exception message if debug output is enabled.
	 *
	 * @param throwable Exception to display.
	 */
	public static final void out(final Throwable throwable)
	{
		if (ENABLE_DEBUG) {
			dump(throwable.toString());
		}
	}

	/**
	 * Outputs the debug string and class name if debug output is enabled.
	 * Output format will be in the form of "source.getClass().getName()": "s"
	 *
	 * @param source Instance of object that generated this output.
	 * @param s String to output.
	 */
	public static final void out(final Object source, final String s)
	{
		if (ENABLE_DEBUG) {
			dump(source+": "+s);
		}
	}

	/**
	 * Creates indented debug output. Each call to start() method will increase
	 * the indentation by predefined ammount and each call to the end method
	 * will decrease it.
	 *
	 * @param s String to output.
	 */
	public static synchronized final void start(final String s)
	{
		if (ENABLE_DEBUG) {
			dump(s);

			indent++;
		}
	}

	/**
	 * Creates indented debug output. Each call to start() method will increase
	 * the indentation by predefined ammount and each call to the end method
	 * will decrease it.
	 *
	 * @param s String to output.
	 */
	public static synchronized final void end(final String s)
	{
		if (ENABLE_DEBUG) {
			indent--;

			if (indent < 0) {
				indent = 0;
			}

			dump(s);
		}
	}

	/**
	 * Run test applet.
	 *
	 * @param args command line parameters
	 */
	public static void main(String[] args)
	{
		Debug.out("Debug has occured");

		Debug.out("Hello world", "Debug happens");

		Debug.start("So it begins");

		Debug.out("Indented statement");

		Debug.end("And so it ends");
	}
}

/* __oOo__ */
