/**
 * Controller.java
 *
 *  Created	: Jul 22, 2008
 *  Author      : Christopher K. Allen 
 */
package xal.app.ptsd;

import xal.extension.application.XalDocumentView;
import xal.extension.application.XalWindow;

/**
 *  Controller component for the <code>PULPTAFE</code> application.
 *
 * @since  Jul 22, 2008
 * @author Christopher K. Allen
 */
public class Controller {

//    /** Construct the main window and associate it with this document. */
//    void setupMainWindow() {
//        makeMainWindow();
//        addXalDocumentListener( mainWindow );
//        mainWindow.titleChanged(this, title);
//    }
//
//
//    /**
//     * This method is a request to close a document.  It may be called when, for 
//     * example, the user selects "Close" from the File menu, or when the user closes the 
//     * window with the close button, or when the application quits.  This request
//     * starts a series of events which closes the document.  Xal document 
//     * listeners are notified that the document will close.  They may perform 
//     * any cleanup as necessary before the document closes.  Then the listeners 
//     * are informed that the document has closed.  The application removes 
//     * the document from its list of open documents and informs its listeners 
//     * that the document has been closed.  If there are any unsaved changes, the 
//     * user is given an opportunity to not close the document so they can save 
//     * the changes.
//     */
//    protected boolean closeDocument() {
//        if ( warnUserOfUnsavedChangesWhenClosing() && hasChanges() ) {
//            if ( !mainWindow.userPermitsCloseWithUnsavedChanges() )  return false;
//        }
//        documentListenerProxy.documentWillClose(this);
//        willClose();
//        documentListenerProxy.documentHasClosed(this);
//
//        freeResources();
//
//        return true;
//    }
//
//
//    /**
//     * Free document resources.
//     */
//    final protected void freeResources() {
//        super.freeResources();
//
//        documentListenerProxy = null;
//        mainWindow = null;              
//    }
//
//
//    /**
//     * Get the main window for this document.
//     * @return The main window for this document.
//     */
//    public XalWindow getMainWindow() {
//        return mainWindow;
//    }
//
//
//    /**
//     * Implement the method for XalAbstractDocument.
//     * @return The main window for this document.
//     */
//    public XalDocumentView getDocumentView() {
//        return mainWindow;
//    }


}
