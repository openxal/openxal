/*
 *  BeamAtFoilWindow.java
 *
 *  Created on July 18, 2008
 */
package xal.app.beamatfoil;

import javax.swing.*;
import java.util.*;
import java.awt.*;

import xal.application.*;

/**
 *  BeamAtFoilWindow is a subclass of XalWindow used in the BeamAtFoil
 *  application. It has a main panel and the message text field for messages.
 *
 *@author     shishlo
 */

public class BeamAtFoilWindow extends XalWindow {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

  private JPanel mainPanel = new JPanel(new BorderLayout());

  private JPanel timePanel = new JPanel(new BorderLayout());

  private JTextField messageTextField = new JTextField();


  /**
   *  Creates a new instance of BeamAtFoilWindow
   *
   *@param  aDocument  The XalDocument for this application.
   */
  public BeamAtFoilWindow(XalDocument aDocument) {
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

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(mainPanel, BorderLayout.CENTER);

    JPanel tmpP = new JPanel(new BorderLayout());
    tmpP.add(messageTextField, BorderLayout.CENTER);
    tmpP.add(timePanel, BorderLayout.WEST);
    centerPanel.add(tmpP, BorderLayout.SOUTH);

    getContentPane().add(centerPanel, BorderLayout.CENTER);
  }


  /**
   *  Adds a time stamp text to the message field
   *  object
   *
   *@param  time_txt  The time stamp text
   */
  protected void addTimeStamp(JTextField time_txt) {
    timePanel.add(time_txt, BorderLayout.CENTER);
  }


  /**
   *  Dispose of BeamAtFoilWindow resources. This method overrides an empty
   *  superclass method.
   */
  protected void freeCustomResources() {
    mainPanel = null;
  }
}

