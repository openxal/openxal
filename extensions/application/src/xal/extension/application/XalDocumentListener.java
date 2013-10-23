/*
 * XalDocumentListener.java
 *
 * Created on March 20, 2003, 8:40 AM
 */

package xal.extension.application;

/**
 * Interface for common document event listeners.
 *
 * @author  tap
 */
public interface XalDocumentListener {
    
    /**
     * Handle the title having changed for a document.
     * @param document The document whose title changed.
     * @param newTitle The new document title.
     */
    public void titleChanged( XalDocument document, String newTitle );
    
    
    /**
     * Handle a change in the whether a document has changes that need saving.
     * @param document The document whose change status changed
     * @param newHasChangesStatus The new "hasChanges" status of the document.
     */
    public void hasChangesChanged( XalDocument document, boolean newHasChangesStatus );
    
    
    /**
     * Handle a the event indicating that a document is about to close.
     * @param document The document that will close.
     */
    public void documentWillClose( XalDocument document );
    
    
    /**
     * Handle the event in which a document has closed.
     * @param document The document that has closed.
     */
    public void documentHasClosed( XalDocument document );
}
