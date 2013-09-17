/*
 * Main.java
 *
 * Created on February 15, 2005, 11:33 AM
 */

package xal.app.ringmeasurement;

import java.util.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;

import xal.application.*;
import xal.smf.application.*;

/**
 *
 * @author  Paul Chu
 */
public class Main extends ApplicationAdaptor {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
// TODO: code application logic here
        try {
            System.out.println("Starting application...");
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public String applicationName() {
        return "ringMeasurement";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new RingDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new RingDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"rm"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"rm", "xml"};
    }
    
}
