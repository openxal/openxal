/*
 * Main.java
 *
 * Created on March 15, 2006, 1:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JOptionPane;

import xal.extension.application.ApplicationAdaptor;
import xal.extension.application.XalDocument;
import xal.extension.application.smf.AcceleratorApplication;

public class Main extends ApplicationAdaptor {
    private URL url;
    
    //-------------Constructors-------------
    public Main() {url = null;}

    public Main(String str) {

	    try{
		url = new URL(str);
	    }
	    catch (MalformedURLException exception) {
		System.err.println(exception);
	    }
    }

    /** The main method of the application. */
    static public void main(String[] args) {
        try {
            System.out.println("Starting application...");
	    setOptions(args);
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), 
                             exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }

    public String applicationName() {
        return "rfsimulator";
    }
    
    public XalDocument newDocument(URL url) {
        return new RFDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new RFDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"rf"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"rf"};
    }
}
