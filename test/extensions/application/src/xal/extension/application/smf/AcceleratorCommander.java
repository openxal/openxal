/*
 * AcceleratorCommander.java
 *
 * Created on May 20, 2003, 12:37 PM
 */

package xal.extension.application.smf;

import xal.extension.application.*;
import xal.tools.ResourceManager;

import java.net.URL;


/**
 * AcceleratorCommander subclasses Commander to provide an accelerator specific 
 * menu in the main menubar.
 * @author  tap
 */
public class AcceleratorCommander extends Commander {
    
    /** Creates a new instance of AcceleratorCommander */
    protected AcceleratorCommander( final Commander appCommander, final XalDocument document ) {
        super( appCommander, document );
    }
    
    
    /** Inherit from Commander */
    protected AcceleratorCommander( final AcceleratorApplication application ) {
		super( application );
	}
    
    
    /**
     * Load the default bundle
     * Override the super class version to include the accelerator specific additions.
     */
    protected void loadDefaultBundle() {
        super.loadDefaultBundle();

		// need to reference this class directly since subclasses would otherwise override the class
		final URL resourceURL = ResourceManager.getResourceURL( AcceleratorCommander.class, MENU_DEFINITION_RESOURCE );
		loadBundle( resourceURL );
    }
    
    
    /** 
     * This method overrides the inherited method to add the accelerator handler 
     * which dynamically generates the Accelerator menu items.
     * @param document The document for which some commands may need to be associated
     */
    protected void registerCustomCommands(XalDocument document) {
        AcceleratorDocument acceleratorDocument = (AcceleratorDocument)document;
        
        registerAction( AcceleratorActionFactory.loadDefaultAcceleratorAction( acceleratorDocument ) );
        registerAction( AcceleratorActionFactory.loadAcceleratorAction( acceleratorDocument ) );
        registerMenuHandler( AcceleratorActionFactory.sequenceHandler( acceleratorDocument ), "sequences-handler" );
    }
}
