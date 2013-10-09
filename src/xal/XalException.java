/*
 * XalException.java
 *
 * Created on October 19, 2001, 10:56 AM
 */

package xal;

/**
 * General XAL base exception.
 *
 * @author  Christopher K. Allen
 * @since   Oct 19, 2001
 * @version 2.0
 */
public class XalException extends java.lang.Exception {

    
    /**  Serialization Id */
    private static final long serialVersionUID = 1L;


    /**
     * Creates new <code>XalException</code> without detail message.
     */
    public XalException() {
        super();
    }


    /**
     * Constructs an <code>XalException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public XalException(String msg) {
        super(msg);
    }


    /**
     * Create a new <code>XalException</code> object which
     * is spawned (in principle) by the given exception object
     * and has the given explanation message.
     * 
     * @param strMsg    explanation message for the exception
     * @param excSrc    originating cause for the exception
     *
     * @author  Christopher K. Allen
     * @since   Apr 27, 2011
     */
    public XalException(String strMsg, Throwable excSrc) {
        super(strMsg, excSrc);
    }


    /**
     * Create a new <code>XalException</code> object which
     * is spawned (in principle) by the given exception object.
     * 
     * @param excSrc    originating cause for the exception
     *
     * @author  Christopher K. Allen
     * @since   Apr 27, 2011
     */
    public XalException(Throwable excSrc) {
        super(excSrc);
    }
    
    
}


