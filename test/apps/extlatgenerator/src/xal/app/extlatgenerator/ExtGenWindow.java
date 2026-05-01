/*
 * MyWindow.java
 *
 * Created on March 14, 2003, 10:25 AM
 */
 
package xal.app.extlatgenerator;

import javax.swing.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * MyWindow is a demo subclass of XalWindow used in the demo application.  This 
 * class demonstrates how to implement a subclass of XalWindow to server as the 
 * main window of a document.  The window contains only view definitions and 
 * no model references.  It defines how the window looks and how to represent 
 * the document in graphical form.
 *
 * @author  t6p
 */
public class ExtGenWindow extends AcceleratorWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    protected JTextArea textView;
    protected JTextField textField;
    
    
    /** Creates a new instance of MainWindow */
    public ExtGenWindow(XalDocument aDocument) {
        super(aDocument);
        setSize(500, 600);
        makeContent();
    }
    
    
    /** 
     * Getter of the text view that displays the document content.
     * @return The text area that displays the document text.
     */
    JTextArea getTextView() {
        return textView;
    }
    
    JTextField getTextField() {
        return textField;
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
        
        textField = new JTextField();
        getContentPane().add(textField, "South");
    }    
}
