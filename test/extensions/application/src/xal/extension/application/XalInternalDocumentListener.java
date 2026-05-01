//
//  XalInternalDocumentListener.java
//  xal
//
//  Created by Thomas Pelaia on 3/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.application;


/** Interface for document event handlers. */
public interface XalInternalDocumentListener {
    /**
	 * Handle the title having changed for a document.
     * @param document The document whose title changed.
     * @param newTitle The new document title.
     */
    public void titleChanged( XalInternalDocument document, String newTitle );
    
    
    /**
	 * Handle a change in the whether a document has changes that need saving.
     * @param document The document whose change status changed
     * @param newHasChangesStatus The new "hasChanges" status of the document.
     */
    public void hasChangesChanged( XalInternalDocument document, boolean newHasChangesStatus );
    
    
    /**
	 * Handle a the event indicating that a document is about to close.
     * @param document The document that will close.
     */
    public void documentWillClose( XalInternalDocument document );
    
    
    /**
	 * Handle the event in which a document has closed.
     * @param document The document that has closed.
     */
    public void documentHasClosed( XalInternalDocument document );
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentActivated( XalInternalDocument document );
	
	
	/**
	 * Handle the document activated event.
	 * @param document the document that has been activated.
	 */
	public void documentDeactivated( XalInternalDocument document );
}
