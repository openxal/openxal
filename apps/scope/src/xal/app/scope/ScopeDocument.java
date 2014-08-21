/*
 * ScopeDocument.java
 *
 * Created on December 19, 2002, 8:30 AM
 */

package xal.app.scope;

import xal.extension.application.*;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;

import java.util.Date;
import java.util.Iterator;


/**
 * ScopeDocument represents a single document of the multi-document application
 *
 * @author  tap
 */
public class ScopeDocument extends XalDocument implements DataListener, SettingListener {
	/** Preference controller for the scope */
	protected PreferenceController preferenceController;
	
    protected ScopeModel model;
    private DataAdaptor windowAdaptor;
	
    
    /** Creates a new instance of Document */
    public ScopeDocument() {
        this(null);
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public ScopeDocument(final java.net.URL url) {
        setSource(url);
		
		model = new ScopeModel();		
        addXalDocumentListener(model);
		
        if ( url != null ) {
            System.out.println("Opening document: " + url.toString());
            DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl(url, false);
            update(documentAdaptor.childAdaptor("ScopeDocument"));
            setHasChanges(false);
        }
		
		model.startMonitor();
        model.addSettingListener(this);     // listen for setting changes
    }
	
	
	/**
	 * Dispose of document resources
	 */
	public void freeCustomResources() {
		model = null;
		windowAdaptor = null;
	}
    
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(java.net.URL url) {
        try {
            XmlDataAdaptor documentAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
            documentAdaptor.writeNode(this);
            documentAdaptor.writeToUrl(url);
            setHasChanges(false);
        }
        catch(XmlDataAdaptor.WriteException exception) {
            exception.printStackTrace();
            displayError("Save Failed!", "Save failed due to an internal write exception!", exception);
        }
        catch(Exception exception) {
            exception.printStackTrace();
            displayError("Save Failed!", "Save failed due to an internal exception!", exception);
        }
    }
    
    
    /** 
     * dataLabel() provides the name used to identify the class in an 
     * external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return "ScopeDocument";
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update(DataAdaptor adaptor) {
        DataAdaptor modelAdaptor = adaptor.childAdaptor( model.dataLabel() );
        model.update(modelAdaptor);
        
        windowAdaptor = adaptor.childAdaptor( MainWindow.dataLabel );
        if ( mainWindow != null ) {
            getDocumentWindow().update(windowAdaptor);
        }
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external
     * storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("version", "2.7.0");
        adaptor.setValue("date", new Date().toString());
        adaptor.writeNode(model);
        adaptor.writeNode( getDocumentWindow() );
    }
    
    
    /**
     * When called this method indicates that a setting has changed in the source.
     * @param source The source whose setting has changed.
     */
    public void settingChanged(Object source) {
        setHasChanges(true);
    }
    
    
    /**
     * Make a main window by instantiating the custom window.
     */
    public void makeMainWindow() {
        mainWindow = new MainWindow(this, windowAdaptor);
        setHasChanges(false);
        getDocumentWindow().addSettingListener(this);     // listen for setting changes
    }
    
    
    /**
     * Override this method to show your application's preference panel.  The preference panel may optionally be document specific or application wide 
     * depending on the application's specific implementation. The default implementaion displays a warning dialog box that now preference panel exists.
     */
    public void editPreferences() {
		if ( preferenceController == null ) {
			preferenceController = new PreferenceController(this);
		}
		preferenceController.show();
    }
    
    
    /**
     * Get the main window cast as MainWindow for convenience
     */
    public MainWindow getDocumentWindow() {
        return (MainWindow)mainWindow;
    }
	
    
    /** Model accessor */
    public ScopeModel getModel() {
        return model;
    }
}
