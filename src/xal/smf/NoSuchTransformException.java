/*
 * NoSuchTransformException.java
 *
 * Created on July 8, 2003, 5:13 PM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf;


/**
 * NoSuchTransformException is thrown when the TransformFactory cannot generate
 * a transform of the type requested.
 *
 * @see TransformFactory
 * @author  tap
 */
public class NoSuchTransformException extends RuntimeException {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    
    /** Creates a new instance of NoSuchTransformException */
    public NoSuchTransformException(String type) {
        super("No generator for a transform of type, " + type + ", could be found.");
    }
}
