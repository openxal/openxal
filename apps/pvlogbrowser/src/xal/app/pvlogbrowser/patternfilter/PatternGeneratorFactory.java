/*
 * PatternGeneratorFactory.java
 *
 * Created on Wed May 12 10:18:07 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.patternfilter;

import java.util.regex.*;


/**
 * PatternGeneratorFactory is a factory of pattern generators.
 *
 * @author  tap
 */
public class PatternGeneratorFactory {
	/**
	 * Constructs a pattern generator which checks whether the tested text contains the 
	 * the pattern text.
	 */
	static public PatternGenerator makeContainsGenerator() {
		return new PatternGenerator() {
			/**
			 * Compile a pattern based on the specified text and flags
			 * @param text the text from which to build the pattern
			 * @param flags the pattern flags used to compile the filter
			 * @return the compiled pattern
			 */
			public Pattern compilePattern(String text, int flags) {
				return Pattern.compile(".*" + text + ".*", flags);
			}
		};
	}
	
	
	/**
	 * Constructs a pattern generator which checks whether the tested text begins with the 
	 * the pattern text.
	 */
	static public PatternGenerator makeBeginsWithGenerator() {
		return new PatternGenerator() {
			/**
			 * Compile a pattern based on the specified text and flags
			 * @param text the text from which to build the pattern
			 * @param flags the pattern flags used to compile the filter
			 * @return the compiled pattern
			 */
			public Pattern compilePattern(String text, int flags) {
				return Pattern.compile(text + ".*", flags);
			}
		};
	}
	
	
	/**
	 * Constructs a pattern generator which checks whether the tested text ends with the 
	 * the pattern text.
	 */
	static public PatternGenerator makeEndsWithGenerator() {
		return new PatternGenerator() {
			/**
			 * Compile a pattern based on the specified text and flags
			 * @param text the text from which to build the pattern
			 * @param flags the pattern flags used to compile the filter
			 * @return the compiled pattern
			 */
			public Pattern compilePattern(String text, int flags) {
				return Pattern.compile(".*" + text, flags);
			}
		};
	}
}

