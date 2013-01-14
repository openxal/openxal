//
//  DefaultXalWindow.java
//  xal
//
//  Created by Thomas Pelaia on 7/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.application;


/** provides a default implementation of the XAL window */
public class DefaultXalWindow extends XalWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /** indicates whether to display a toolbar */
    private final boolean DISPLAYS_TOOLBAR;
    
    
    /** Constructor */
    public DefaultXalWindow( final XalDocument aDocument ) {
		this( aDocument, true );
    }	
    
    
    /** Constructor */
    public DefaultXalWindow( final XalDocument aDocument, final boolean displaysToolbar ) {
		super( aDocument );
        DISPLAYS_TOOLBAR = displaysToolbar;
    }	
    
	
    /**
	 * Indicates whether or not to display the toolbar.
	 * @return true to display the toolbar and false to hide it
     */
    public boolean usesToolbar() {
        return DISPLAYS_TOOLBAR;
    }
}
