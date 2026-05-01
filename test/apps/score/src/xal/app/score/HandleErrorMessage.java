/*
 * HandleErrorMessage.java
 *
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import java.lang.String;

/**
 * This interface provodes a method to handle an error message any way you see fit
 *
 * @author  jdg
 */
public interface HandleErrorMessage {		
	
	/**
	 * Dump an error message in a useful way
	 * @param msg the error message
	 */
	public void dumpErr(final String msg);
}

