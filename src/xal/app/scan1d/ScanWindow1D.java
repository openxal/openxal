/*
 * ScanWindow1D.java
 *
 * Created on July 31, 2003, 10:25 AM
 */

package xal.app.scan1d;

import javax.swing.*;
import java.awt.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * ScanWindow1D is a subclass of XalWindow used in the 1D scan. It has mainPanel
 * and the message text field for messages. 
 *
 * @author  shishlo
 */

public class ScanWindow1D extends AcceleratorWindow {
	private static final long serialVersionUID = 0L;
	
    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JTextField messageTextField = new JTextField();
    
    /** Creates a new instance of ScanWindow1D */
    public ScanWindow1D(XalDocument aDocument) {
        super(aDocument);
        makeContent();
        pack();
        //setSize(500, 600);
    }
    
    
    /** 
     * Getter of the text view that displays the document content.
     * @return The main panel of the window.
     */
    JPanel getMainPanel() {
        return mainPanel;
    }

    /** 
     * Returns the currently displaying JComponent.
     * @return The currently displaying JComponent.
     */
    JComponent getJComponent(){
        return (JComponent) mainPanel.getComponent(0); 
    }

    /** 
     * Sets the JComponent that will be visible in the main window.
     */
    void setJComponent(JComponent component){
	mainPanel.removeAll();
	if(component != null){
	    mainPanel.add(component,BorderLayout.CENTER);
	}
	getContentPane().validate();
        getContentPane().repaint();
    }

    /** 
     * Returns the name of the currently displaying JComponent.
     * @return The name of the currently displaying JComponent.
     */
    String getComponentName(){
        return mainPanel.getComponent(0).getName(); 
    }

    /** 
     * Returns the message text field.
     * @return The message text field.
     */
    JTextField getMessageTextField(){
        return messageTextField;
    }    
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        messageTextField.setForeground(Color.red);


        JScrollPane scrollPane = new JScrollPane(mainPanel);

	JPanel centerPanel = new JPanel(new BorderLayout());
	centerPanel.add(scrollPane,BorderLayout.CENTER);
	centerPanel.add(messageTextField,BorderLayout.SOUTH);

        getContentPane().add(centerPanel,BorderLayout.CENTER);
    } 

    /**
     * Dispose of ScanWindow1D resources.  
     * This method overrides an empty superclass method.
     */
    public void freeCustomResources(){
	mainPanel = null;  
    }  
}
