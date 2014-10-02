package xal.app.mtv;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

/**
 * The main program for the xio  app.
 * @author J. Galambos
 *
 */
public class Main extends ApplicationAdaptor {

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

    // --------- Document management -------------------------------------------
    
    public String[] readableDocumentTypes() {
        return new String[] {"mtv"};
    }
    
    
    public String[] writableDocumentTypes() {
        return new String[] {"mtv"};
    }
 
    /** this returns a new document to start working with  */ 
    public XalDocument newEmptyDocument() {
      MTVDocument md;

	// even if url == null, use it. A default accelerator will be created.

	return md = new MTVDocument(url);
    }
        
    public XalDocument newDocument(java.net.URL theUrl) {
	MTVDocument md = new MTVDocument(theUrl);
	return md;
    }
    
    // --------- Global application management ---------------------------------
    
    
    public String applicationName() {
        return "mtv";
    }


    public boolean usesConsole() {
        return false;
    }
    
    //  -------- specific to mpspostmort application ---------

    /** The url to use to create the startup document with */

    private URL url;
    
    /**
     * Register actions for the Special menu items
     */
    public void customizeCommands(Commander commander) {
    }
    
    
    // --------- Application events --------------------------------------------
    
    public void applicationFinishedLaunching() { 
	System.out.println("mtv finished launching...");
    }
    
     /** The main method of the application. */
    static public void main(String[] args) {
        try {
		URL[] urls = new URL[args.length];
		for ( int index = 0 ; index < args.length ; index++ ) {
			urls[index] = new File(args[index]).toURI().toURL();
		}
		AcceleratorApplication.launch(new Main(), urls );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
	    Application.displayApplicationError("Launch Exception", "Launch Exception", exception);
	    System.exit(-1);
        }
    }


}
