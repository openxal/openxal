/*
 * PatternGenerator.java
 *
 * Created on Wed May 12 10:15:33 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.patternfilter;

import java.util.regex.*;


/**
 * PatternGenerator is an interface for compiling a pattern from text.  An implementation
 * may generate a pattern based on the text and also other criteria e.g. "starts with", "contains", 
 * "ends with", etc.
 *
 * @author  tap
 */
public interface PatternGenerator {
	/**
	 * Compile a pattern based on the specified text and flags
	 * @param text the text from which to build the pattern
	 * @param flags the pattern flags used to compile the filter
	 * @return the compiled pattern
	 */
	public Pattern compilePattern(String text, int flags);
}

