/*
 * MyWindow.java
 *
 * Created on March 14, 2003, 10:25 AM
 */

package xal.app.acceleratordemo;

import javax.swing.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.smf.application.*;

/**
 * DemoWindow is a demo subclass of AcceleratorWindow used in the demo application.  This 
 * class demonstrates how to implement a subclass of AcceleratorWindow to serve as the 
 * main window of an accelerator document.  The window contains only view definitions and 
 * no model references.  It defines how the window looks and how to represent 
 * the document in graphical form.
 *
 * @author  t6p
 */
public class DemoWindow extends AcceleratorWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    protected JTextArea textView;
    
    
    /** Creates a new instance of MainWindow */
    public DemoWindow(XalDocument aDocument) {
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
    
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        textView = new JTextArea();
        textView.setLineWrap(true);
        textView.setWrapStyleWord(true);
        textView.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textView);
        getContentPane().add(scrollPane);
        
        JTextField textField = new JTextField();
        getContentPane().add(textField, "South");
    }    
}
