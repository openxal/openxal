/*
 *  BpmViewerWindow.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.bpmviewer;

import javax.swing.*;
import java.util.*;
import java.awt.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 *  BpmViewerWindow is a subclass of XalWindow used in the bpmViewer
 *  application. It has a main panel and the message text field for messages.
 *
 *@author     shishlo
 *@version    July 12, 2004
 */

public class BpmViewerWindow extends AcceleratorWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    private JPanel mainPanel = new JPanel(new BorderLayout());

    private JTextField messageTextField = new JTextField();


    /**
     *  Creates a new instance of BpmViewerWindow
     *
     *@param  aDocument  The XalDocument for this application.
     */
    public BpmViewerWindow(XalDocument aDocument) {
        super(aDocument);
        makeContent();
        //pack();
    }


    /**
     *  Returns the main panel of the window.
     *
     *@return    The main panel of the window.
     */
    JPanel getMainPanel() {
        return mainPanel;
    }


    /**
     *  Returns the currently displaying JComponent.
     *
     *@return    The currently displaying JComponent.
     */
    JComponent getJComponent() {
        return (JComponent) mainPanel.getComponent(0);
    }


    /**
     *  Sets the JComponent that will be visible in the main window.
     *
     *@param  component  The new jComponent value
     */
    void setJComponent(JComponent component) {
        mainPanel.removeAll();
        if (component != null) {
            mainPanel.add(component, BorderLayout.CENTER);
        }
        getContentPane().validate();
        getContentPane().repaint();
    }


    /**
     *  Returns the name of the currently displaying JComponent.
     *
     *@return    The name of the currently displaying JComponent.
     */
    String getComponentName() {
        return mainPanel.getComponent(0).getName();
    }


    /**
     *  Returns the message text field.
     *
     *@return    The message text field.
     */
    JTextField getMessageTextField() {
        return messageTextField;
    }


    /**
     *  Create the main window subviews.
     */
    protected void makeContent() {
        messageTextField.setForeground(Color.red);

        JScrollPane scrollPane = new JScrollPane(mainPanel);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(messageTextField, BorderLayout.SOUTH);

        getContentPane().add(centerPanel, BorderLayout.CENTER);
    }


    /**
     *  Dispose of BpmViewerWindow resources. This method overrides an empty
     *  superclass method.
     */
    public void freeCustomResources() {
        mainPanel = null;
    }
}

