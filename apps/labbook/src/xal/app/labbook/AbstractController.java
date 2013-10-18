//
//  AbstractController.java
//  xal
//
//  Created by Pelaia II, Tom on 9/26/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.extension.bricks.*;
import xal.tools.messaging.MessageCenter;


/** base controller class */
public class AbstractController {
	/** window reference to window resources */
	final protected WindowReference	WINDOW_REFERENCE;
	
	/** proxy for forwarding controller events */
	final protected ControllerListener EVENT_PROXY;
	
	/** text indicating the result of the latest validation test */
	protected String _validationText;
	
	
    /** Create a new controller */
    public AbstractController( final MessageCenter messageCenter, final WindowReference windowReference ) {
		WINDOW_REFERENCE = windowReference;
		
		EVENT_PROXY = messageCenter.registerSource( this, ControllerListener.class );
    }
	
	
	/**
	 * Validate whether the summary is valid
	 * @return true if the summary is valid and false if not
	 */
	public boolean validate() {
		return true;
	}
	
	
	/**
	 * Get the validation text
	 * @return the validation text
	 */
	public String getValidationText() {
		return _validationText;
	}
	
	
	/** post a document change event */
	protected void postDocumentChangeEvent() {
		EVENT_PROXY.documentModified( this );
	}
}
