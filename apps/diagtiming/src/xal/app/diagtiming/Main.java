package xal.app.diagtiming;

import javax.swing.*;
import java.net.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;

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
            AcceleratorApplication.launch( new Main() );
        }
        catch(Exception exception) {
            System.err.println( exception.getMessage() );
            exception.printStackTrace();
            JOptionPane.showMessageDialog(null, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
        }
    }

    public String applicationName() {
        return "diagnosticTiming";
    }
    
    public XalDocument newDocument(java.net.URL url) {
        return new DiagTimingDocument(url);
    }
    
    public XalDocument newEmptyDocument() {
        return new DiagTimingDocument();
    }
    
    public String[] writableDocumentTypes() {
        return new String[] {"dt"};
    }
    
    public String[] readableDocumentTypes() {
        return new String[] {"dt", "xml"};
    }

}
