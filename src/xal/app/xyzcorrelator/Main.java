package xal.app.xyzcorrelator;

import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;


/**
 * The application adaptor for the XYZ Correlator.
 */
public class Main extends ApplicationAdaptor {    
    public String[] readableDocumentTypes() {
        return new String[] {"xyz"};
    }
    
    
    public String[] writableDocumentTypes() {
        return readableDocumentTypes();
    }
 
    /** returns a new document to start working with  */ 
    public XalDocument newEmptyDocument() {
		return new XyzDocument( null );
    }
	
	
	/** build a document from the URL source */
    public XalDocument newDocument( final URL theUrl ) {
		return new XyzDocument( theUrl );
    }
    
    
	/** Get the name of this application */
    public String applicationName() {
        return "XYZ Correlator";
    }
	
    
    /** handle the event indicating that the application has finished launching */
    public void applicationFinishedLaunching() { 
		System.out.println( "XYZ Correlator finished launching..." );
    }
    
    
    static public void main( final String[] args ) {
		String strUrl = null;
		System.out.println( "Launching XYZ Correlator..." );
		// If started with an argument, use this to open a document
		setOptions( args );
		AcceleratorApplication.launch( new Main() );
    }


}
