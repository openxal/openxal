/*
 * Db2XalWindow.java
 *
 * Created on March 14, 2003, 10:25 AM
 */
 
package xal.app.db2xal;

import javax.swing.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * Db2XalWindow is a subclass of XalWindow for Db2Xal application.
 *
 * @author  Paul Chu
 */
public class Db2XalWindow extends XalWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    protected JTextArea textView;
    
    
    /** Creates a new instance of MainWindow */
    public Db2XalWindow(XalDocument aDocument) {
        super(aDocument);
        setSize(500, 600);
        makeContent();
    }
    
    
    /** 
     * Getter of the text view that displays the document content.
     * @return The text area that displays the document text.
     */
    protected JTextArea getTextView() {
        return textView;
    }
    
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        textView = new JTextArea();
        textView.setLineWrap(true);
        textView.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textView);
        getContentPane().add(scrollPane);
        
        JTextField textField = new JTextField();
        getContentPane().add(textField, "South");
    }    
}
