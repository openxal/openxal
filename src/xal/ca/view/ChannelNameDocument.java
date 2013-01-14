/*
 * ChannelField.java
 *
 * Created on May 30, 2003, 1:20 PM
 */

package xal.ca.view;

import javax.swing.text.*;
import java.awt.Toolkit;

/**
 * ChannelNameDocument is a javax.swing.text.Document implementation useful for 
 * filtering out invalid characters of a channel name.  For example channel names
 * cannot have spaces or non-printing characters.  This is useful when implementing
 * intelligent text fields that allow a user to enter a channel name.  Invalid 
 * characters are thrown away and a system beep warning is heard in its place.
 *
 * @author  tap
 */
public class ChannelNameDocument extends PlainDocument {
	private static final long serialVersionUID = 0L;

    /** Creates a new instance of ChannelField */
    public ChannelNameDocument() {
    }

    
    /**
     * Override the inherited method to filter out characters that are not alpha-numeric or a colon, 
     * dash, underscore or period.
     * @throws javax.swing.text.BadLocationException under the same conditions the inherited method would.
     */
    public void insertString(int offset, String string, AttributeSet attributes) throws BadLocationException {
        if ( string == null ) {
            super.insertString(offset, null, attributes);
	    return;
        }
        
        StringBuffer goodString = new StringBuffer();
        for ( int index = 0 ; index < string.length() ; index++ ) {
            char nextChar = string.charAt(index);
            if ( Character.isLetterOrDigit(nextChar) || nextChar == ':' || nextChar == '_' || nextChar == '-' || nextChar == '.') {
                goodString.append(nextChar);
            }
            else {
                Toolkit.getDefaultToolkit().beep();
            }
        }
        super.insertString(offset, goodString.toString(), attributes);
    }
}
