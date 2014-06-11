//
//  DefaultAcceleratorWindow.java
//  xal
//
//  Created by Thomas Pelaia on 9/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application.smf;

import xal.extension.application.*;


/** provides a default implementation of the XAL Accelerator window */
public class DefaultAcceleratorWindow extends AcceleratorWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /** Constructor */
    public DefaultAcceleratorWindow( final XalDocument aDocument ) {
		super( aDocument );
    }	
    
	
    /**
	 * Indicates whether or not to display the toolbar.
	 * @return true to display the toolbar and false to hide it
     */
    public boolean usesToolbar() {
        return true;
    }	
}
