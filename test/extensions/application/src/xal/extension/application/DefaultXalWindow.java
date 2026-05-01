//
//  DefaultXalWindow.java
//  xal
//
//  Created by Thomas Pelaia on 7/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application;


/** provides a default implementation of the XAL window */
public class DefaultXalWindow extends XalWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    
    /** Constructor */
    public DefaultXalWindow( final XalDocument aDocument ) {
		this( aDocument, true );
    }	
    
    
    /** Constructor */
    public DefaultXalWindow( final XalDocument aDocument, final boolean displaysToolbar ) {
		super( aDocument, displaysToolbar );
    }	
}
