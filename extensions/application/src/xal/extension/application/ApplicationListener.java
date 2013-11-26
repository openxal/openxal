/*
 * ApplicationListener.java
 *
 * Created on March 25, 2003, 2:56 PM
 */

package xal.extension.application;

/**
 * Interface which listeners of application events must implement.
 *
 * @author  tap
 */
public interface ApplicationListener {
	/**
	 * Event indicating that the application will open the initial documents if any.  These documents
	 * may include a new empty document if appropriate or any documents passed at the command line.
	 */
	public void applicationWillOpenInitialDocuments();
	
	
    /**
     * Event indicating that an open document has closed.
     * @param document The document that has closed.
     */
    public void documentClosed( XalAbstractDocument document );
    
    
    /**
     * Event indicating that a new document has been created.
     * @param document The document that has been created.
     */
    public void documentCreated( XalAbstractDocument document );
    
    
    /** Event indicating that the application will quit. */
    public void applicationWillQuit();
}
