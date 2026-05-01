/*
 * OpticsVersionException.java
 *
 * Created on Thu Jan 16 15:29:56 EDT 2014
 *
 * Copyright (c) 2014 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf.data;


/**
 * OpticsVersionException indicates a version conflict for the optics file attempted to be loaded. The optics file format must match the supported one as indicated by the version number.
 *
 * @author  tap
 * @since Jan 16, 2014
 */
public class OpticsVersionException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    
    /** 
     * Primary Constructor 
     * @param message exception message
     */
    protected OpticsVersionException( final String message ) {
		super( message );
    }
}

