/*
 * AcceleratorCommander.java
 *
 * Created on May 20, 2003, 12:37 PM
 */

package xal.smf.application;

import xal.application.*;

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
        loadBundle( "xal.resources.smf.application.menudef" );
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
