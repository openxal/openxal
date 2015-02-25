/**
 * BadStructDefinition.java
 *
 * @author Christopher K. Allen
 * @since  Feb 4, 2011
 *
 */

package xal.smf.scada;

import xal.smf.scada.ScadaRecord.IFieldDescriptor;


/**
 * Runtime exception thrown when, during structure access operations,
 * it is found that the definition of the data structure (through
 * the array of <code>{@link IFieldDescriptor}</code>s objects) does
 * not match the actual data structure implementation.
 *
 * @since  Mar 4, 2010
 * @author Christopher K. Allen
 */
public class BadStructException extends RuntimeException {
    
    /**  Serialization version*/
    private static final long serialVersionUID = 1L;

    /**
     * Create a new <code>BadStructDefinition</code> object.
     *
     *
     * @since     Mar 4, 2010
     * @author    Christopher K. Allen
     */
    public BadStructException() {
        super();
    }
    
    /**
     * Create a new <code>BadStructDefinition</code> object with
     * a descriptive message.
     *
     * @param strMsg        message describing the exception cause 
     *
     * @since     Mar 4, 2010
     * @author    Christopher K. Allen
     */
    public BadStructException(String strMsg) {
        super(strMsg);
    }
    
    /**
     * Create a new <code>BadStructDefinition</code> object with
     * the source exception (the present exception being the
     * last in a chain).
     *
     * @param e     root exception
     *
     * @since     Mar 4, 2010
     * @author    Christopher K. Allen
     */
    public BadStructException(Exception e)  {
        super(e);
    }
    
    /**
     * Create a new <code>BadStructDefinition</code> object.
     *
     * @param strMsg        message describing the exception cause 
     * @param e             root exception
     *
     * @since     Mar 4, 2010
     * @author    Christopher K. Allen
     */
    public BadStructException(String strMsg, Exception e) {
        super(strMsg, e);
    }
}

