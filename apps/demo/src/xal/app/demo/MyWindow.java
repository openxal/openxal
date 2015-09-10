/*
 * MyWindow.java
 *
 * Created on March 14, 2003, 10:25 AM
 */

package xal.app.demo;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import xal.extension.application.*;

/**
 * MyWindow is a demo subclass of XalWindow used in the demo application.  This 
 * class demonstrates how to implement a subclass of XalWindow to server as the 
 * main window of a document.  The window contains only view definitions and 
 * no model references.  It defines how the window looks and how to represent 
 * the document in graphical form.
 *
 * @author  t6p
 */
public class MyWindow extends XalWindow {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
    protected JTextArea textView;
    
    
    /** Creates a new instance of MainWindow */
    public MyWindow(XalDocument aDocument) {
        super(aDocument);
        setSize( 600, 600 );
        makeContent();
    }
    
    
    /**
     * Register actions specific to this window instance. 
     * This code demonstrates how to define custom actions for menus and the toolbar for
	 * a particular window instance.
     * This method is optional.  You may similarly define actions in the document class
     * if those actions are document specific and also for the entire application if 
	 * the actions are application wide.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands( final Commander commander ) {
        // define a toggle "edit" action
		final ToggleButtonModel editModel = new ToggleButtonModel();
		editModel.setSelected(true);
		editModel.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
				textView.setEditable( editModel.isSelected() );
				Logger.getLogger("global").log( Level.INFO, "Toggle whether text is editable." );
                System.out.println("toggled editable...");				
            }
		});
        commander.registerModel("toggle-editable", editModel);
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
        JScrollPane scrollPane = new JScrollPane(textView);
        getContentPane().add(scrollPane);
        
        JTextField textField = new JTextField();
        getContentPane().add(textField, "South");
    }    
}
