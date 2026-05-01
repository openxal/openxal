/*
 *  EmittanceMain
 *  EmittanceMain.java
 *
 *  Created on July 31, 2003, 10:25 AM
 */
package xal.app.emittanceanalysis;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.extension.application.*;

/**
 *  EmittanceMain is a concrete subclass of ApplicationAdaptor for the Emittance
 *  Analysis application.
 *
 *@author     shishlo
 *@version    1.0
 */

public class EmittanceMain extends ApplicationAdaptor {

    /**
     *  Constructor
     */
    public EmittanceMain() { }


    /**
     *  Returns the text file suffixes of files this application can open.
     *
     *@return    Suffixes of readable files
     */
    public String[] readableDocumentTypes() {
        return new String[]{"emt"};
    }


    /**
     *  Returns the text file suffixes of files this application can write.
     *
     *@return    Suffixes of writable files
     */
    public String[] writableDocumentTypes() {
        return new String[]{"emt"};
    }


    /**
     *  Returns an instance of the Emittance Analysis application document.
     *
     *@return    An instance of Emittance Analysis application document.
     */
    public XalDocument newEmptyDocument() {
        return new EmittanceDocument();
    }


    /**
     *  Returns an instance of the Emittance Analysis application document
     *  corresponding to the specified URL.
     *
     *@param  url  The URL of the file to open.
     *@return      An instance of an Emittance Analysis application document.
     */
    public XalDocument newDocument(java.net.URL url) {
        return new EmittanceDocument(url);
    }


    /**
     *  Specifies the name of the Emittance Analysis application application.
     *
     *@return    Name of the application.
     */
    public String applicationName() {
        return "Emittance Analysis";
    }


    /**
     *  Activates the preference panel for the Emittance Analysis application.
     *
     *@param  document  The document whose preferences are being changed.
     */
    public void editPreferences(XalDocument document) {
        ((EmittanceDocument) document).editPreferences();
    }


    /**
     *  Specifies whether the Emittance Analysis application will send standard
     *  output and error to the console.
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
                Application.launch(new EmittanceMain());
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
                exception.printStackTrace();
                JOptionPane.showMessageDialog(null, exception.getMessage(),
                    exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
            }
            return;
        }

        EmittanceMain doc = new EmittanceMain();

        URL[] predefConfURLArr = new URL[args.length];

        for (int i = 0; i < args.length; i++) {
            predefConfURLArr[i] = doc.getClass().getResource("resources/" + args[i]);
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

