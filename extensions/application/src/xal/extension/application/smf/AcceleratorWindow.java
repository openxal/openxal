/*
 * AcceleratorWindow.java
 *
 * Created on May 20, 2003, 12:37 PM
 */

package xal.extension.application.smf;

import xal.extension.application.*;

/**
 * AcceleratorWindow is the subclass of XalWindow that is specific to 
 * accelerator based applications.
 *
 * @author  tap
 */
abstract public class AcceleratorWindow extends XalWindow {
    
    /** Creates a new instance of AcceleratorWindow */
    public AcceleratorWindow(XalDocument aDocument) {
        super(aDocument);
    }
    
    
    /**
     * Subclasses should override this method to provide a custom Commander.
     * @return The commander with support for Accelerator based applications.
     */
    public Commander makeCommander() {
        return new AcceleratorCommander(Application.getApp().getCommander(), document);
    }
}
