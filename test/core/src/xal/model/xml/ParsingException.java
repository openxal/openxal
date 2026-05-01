package xal.model.xml;

import xal.XalException;

/**
 * Encapsulates description of error encountered parsing a <code>Lattice</code>.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */

public class ParsingException extends XalException {

    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    /**
	 * Creates exception with detail message.
	 * 
	 * @param msg description of error
	 */
	public ParsingException(String msg) {
		super(msg);
	}

}
