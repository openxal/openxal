/*
 *  EmittanceWindow.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.emittanceanalysis;

import javax.swing.*;
import java.util.*;
import java.awt.*;

import xal.extension.application.*;

/**
 *  EmittanceWindow is a subclass of XalWindow used in the Emittance Analysis
 *  application. It has a main panel and the message text field for messages.
 *
 *@author     shishlo
 *@version    1.0
 */

public class EmittanceWindow extends XalWindow {
	private static final long serialVersionUID = 1L;

    private JPanel mainPanel = new JPanel( new BorderLayout() );

    private JTextField messageTextField = new JTextField();


    /**
     *  Creates a new instance of EmittanceWindow
     *
     *@param  aDocument  The XML data adapter with configuration data
     */
    public EmittanceWindow( XalDocument aDocument ) {
        super( aDocument );
        makeContent();
        pack();
    }


    /**
     *  Getter of the text view that displays the document content.
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
        return (JComponent) mainPanel.getComponent( 0 );
    }


    /**
     *  Sets the JComponent that will be visible in the main window.
     *
     *@param  component  The new jComponent value
     */
    void setJComponent( JComponent component ) {
        mainPanel.removeAll();
        if ( component != null ) {
            mainPanel.add( component, BorderLayout.CENTER );
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
        return mainPanel.getComponent( 0 ).getName();
    }


    /**
     *  Returns the message text field.
     *
     *@return    The message text field.
     */
    JTextField getMessageTextField() {
        return messageTextField;
    }


    /**  Create the main window subviews. */
    protected void makeContent() {
        messageTextField.setForeground( Color.red );

        JScrollPane scrollPane = new JScrollPane( mainPanel );

        JPanel centerPanel = new JPanel( new BorderLayout() );
        centerPanel.add( scrollPane, BorderLayout.CENTER );
        centerPanel.add( messageTextField, BorderLayout.SOUTH );

        getContentPane().add( centerPanel, BorderLayout.CENTER );
    }


    /**
     *  Dispose of EmittanceWindow resources. This method overrides an empty
     *  superclass method.
     */
    public void freeCustomResources() {
        mainPanel = null;
    }
}

