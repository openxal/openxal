/*
 *  ScalarpvviewerWindow.java
 *
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.scalarpvviewer;

import javax.swing.*;
import java.util.*;
import java.awt.*;

import xal.extension.application.*;

/**
 *  ScalarpvviewerWindow is a subclass of XalWindow used in the Scalarpvviewer
 *  application. It has a main panel and the message text field for messages.
 *
 *@author     shishlo
 */

public class ScalarpvviewerWindow extends XalWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    private JPanel mainPanel = new JPanel(new BorderLayout());

    private JTextField messageTextField = new JTextField();


    /**
     *  Creates a new instance of ScalarpvviewerWindow
     *
     *@param  aDocument  The XalDocument for this application.
     */
    public ScalarpvviewerWindow(XalDocument aDocument) {
        super(aDocument);
        makeContent();
        pack();
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
     *  Dispose of ScalarpvviewerWindow resources. This method overrides an empty
     *  superclass method.
     */
    public void freeCustomResources() {
        mainPanel = null;
    }
}

