/*
 *  RingbpmviewerMain.java
 *
 *  Created on May 24, 2005, 10:25 AM
 */
package xal.app.ringbpmviewer;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.application.*;

/**
 *  RingbpmviewerMain is a concrete subclass of ApplicationAdaptor for the Ringbpmviewer
 *  application.
 *
 *@author     shishlo
 */

public class RingbpmviewerMain extends ApplicationAdaptor {

    /**
     *  Constructor
     */
    public RingbpmviewerMain() { }


    /**
     *  Returns the text file suffixes of files this application can open.
     *
     *@return    Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[]{"spv"};
    }


    /**
     *  Returns the text file suffixes of files this application can write.
     *
     *@return    Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[]{"rvw"};
    }


    /**
     *  Returns an instance of the Ringbpmviewer application document.
     *
     *@return    An instance of Ringbpmviewer application document.
     */
    public XalDocument newEmptyDocument() {
        return new RingbpmviewerDocument();
    }


    /**
     *  Returns an instance of the Ringbpmviewer application document corresponding
     *  to the specified URL.
     *
     *@param  url  The URL of the file to open.
     *@return      An instance of an Ringbpmviewer application document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new RingbpmviewerDocument(url);
    }


    /**
     *  Specifies the name of the Ringbpmviewer application.
     *
     *@return    Name of the application.
     */
    public String applicationName() {
        return "Ring BPMs Viewer";
    }


    /**
     *  Activates the preference panel for the Ringbpmviewer application.
     *
     *@param  document  The document whose preferences are being changed.
     */
    public void editPreferences(XalDocument document) {
        ((RingbpmviewerDocument) document).editPreferences();
    }


    /**
     *  Specifies whether the Ringbpmviewer application will send standard output
     *  and error to the console.
     *
     *@return    true or false.
     */
    public boolean usesConsole() {
        String usesConsoleProperty = System.getProperty("usesConsole");
        if (usesConsoleProperty != null) {
            return Boolean.valueOf(usesConsoleProperty).booleanValue();
        } else {
            return true;
        }
    }


    /**
     *  The main method of the application.
     *
     *@param  args  The command line arguments
     */
    public static void main(String[] args) {

        if (args.length == 0) {
            try {
                Application.launch(new RingbpmviewerMain());
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, exception.getMessage(),
                    exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        RingbpmviewerMain doc = new RingbpmviewerMain();

        URL[] predefConfURLArr = new URL[args.length];

        for (int i = 0; i < args.length; i++) {
            predefConfURLArr[i] = doc.getClass().getResource("config/" + args[i]);
        }

        try {
            Application.launch(doc, predefConfURLArr);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(),
                exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }
}

