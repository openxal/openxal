/**
 * TextEditorPane.java
 *
 *  Created	: Aug 17, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentListener;

/**
 * Implements a simple text editor supporting the
 * <code>DataListener</code> interface.  The editor uses
 * a <code>PersistentDocument</code> object as the internal
 * document; the <code>DataListener</code> interface methods
 * here are just a proxy to those of the internal document.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Aug 17, 2009
 * @author Christopher K. Allen
 * 
 * @see javax.swing.JTextPane
 * @see PersistentDocument
 */
public class TextEditorPane extends JScrollPane implements DataListener {

    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
//    /**  Attribute used by the data adaptors to save/retrieve editor text */
//    private static final String STR_ATTR_TEXT = "txt";


    
    
    /*
     * Instance Attributes
     */
    
    /** the document model used by this window */
    private final PersistentDocument          docText;
    
    /** the the text view port of the note pad */ 
    private final JTextPane                   paneText;
    
    
    /*
     * Initialization
     */

    
    /**
     * Create a new <code>TextEditorPane</code> object.
     *
     * @since     Nov 19, 2009
     * @author    Christopher K. Allen
     */
    public TextEditorPane() {
        this(new JTextPane() );
    }
    
    /**
     * Create a new <code>TextEditorPane</code> object.
     *
     * @param paneText          text pane used in viewport 
     *
     * @since     Aug 17, 2009
     * @author    Christopher K. Allen
     */
    private TextEditorPane(JTextPane paneText) {
        super( paneText );
        this.paneText  = paneText;
        this.docText = new PersistentDocument();
        this.paneText.setStyledDocument(this.docText);
//        this.mdlTxtDoc = paneText.getStyledDocument();
        
        this.initialize();
    }

    
    /*
     * Attributes
     */
    
//    /**
//     * Returns the back-reference to the main application.
//     *
//     * @return  main application instance 
//     * 
//     * @since  Nov 20, 2009
//     * @author Christopher K. Allen
//     */
//    public MainApplication   getApp() {
//        return this.appMain;
//    }
//    
//    /**
//     * Returns the back-reference to the application main window.
//     *
//     * @return  application main window
//     * 
//     * @since  Nov 20, 2009
//     * @author Christopher K. Allen
//     */
//    public MainWindow        getMainWindow() {
//        return this.docMain;
//    }
//    
//    
//    /**
//     * Return the main logger object for the 
//     * application.
//     *
//     * @return  application logger object
//     * 
//     * @since  Nov 25, 2009
//     * @author Christopher K. Allen
//     */
//    public IEventLogger getLogger() {
//        return MainApplication.getEventLogger();
//    }

    

    
    /*
     * Operations
     */
    
    /**
     * Returns the text contained in this view's
     * editor as a string.
     *
     * @return  text in view editor
     * 
     * @since  Nov 19, 2009
     * @author Christopher K. Allen
     */
    public String       getText() {
        return this.paneText.getText();
    }

    
    /**
     * Sets the text displayed in this view's 
     * editor panel.
     *
     * @param strText   text to display
     * 
     * @since  Nov 19, 2009
     * @author Christopher K. Allen
     */
    public void setText(String strText) {
        this.paneText.setText(strText);
    }
    
    /**
     * Adds a event response to any edits in the 
     * <code>Document</code> object (of the internal text pane).
     *
     * @param lsnUpdates        response actions to edit events
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public void addEditListener(DocumentListener lsnUpdates) {
        this.paneText.getDocument().addDocumentListener(lsnUpdates);
    }
    
    /**
     * Removes the given edit responder, i.e., it will no
     * longer receive edit notifications.
     *
     * @param lsnUpdates        edit action object to be removed from
     *                          notification list
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public void removeEditListener(DocumentListener lsnUpdates) {
        this.paneText.getDocument().removeDocumentListener(lsnUpdates);
    }
    
    /**
     * Returns the document object for this text 
     * editor.
     *
     * @return  the document being managed by this editor
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public PersistentDocument getDocument() {
        return this.docText;
    }

    
    /*
     * DataListener Interface
     */
    
    /**
     * Returns the label used by <code>DataAdaptors</code>
     * to identify the data produced/consumed by this object.
     * 
     * @return  data identifier label
     *
     * @since   Apr 27, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.tools.data.DataListener#dataLabel()
     */
    @Override
    public String dataLabel() {
//        return this.getClass().getName();
        return this.docText.dataLabel();
    }

   /**
    * Updates the the state of this object using
    * the data source provided.
    * 
    * @param    daptSrc data source containing new object state
    *
    * @since   Apr 27, 2010
    * @author  Christopher K. Allen
    *
    * @see xal.tools.data.DataListener#update(xal.tools.data.DataAdaptor)
    */
   @Override
   public void update(DataAdaptor daptSrc) {
//       DataAdaptor      daptChild = daptSrc.childAdaptor( this.dataLabel() );
//       
//       if (daptChild.hasAttribute(STR_ATTR_TEXT)) {
//           String       strText = daptChild.stringValue(STR_ATTR_TEXT);
//           
//           this.setText(strText);
//       }
       this.docText.update(daptSrc);
   }

   /**
    * Writes out the state data of this object to the data
    * sink provided.  Once saved the state of the object can
    * be restored using the compliment method
    * <code>{@link TextEditorPane#update(DataAdaptor)}</code>.
    * 
    * @param    daptSnk data sink to receive object state data
    *
    * @since   Apr 27, 2010
    * @author  Christopher K. Allen
    *
    * @see xal.tools.data.DataListener#write(xal.tools.data.DataAdaptor)
    */
   @Override
   public void write(DataAdaptor daptSnk) {

//       DataAdaptor      daptChild = daptSnk.createChild( this.dataLabel() );
//       daptChild.setValue(STR_ATTR_TEXT, this.getText());
       this.docText.write(daptSnk);
   }
   
   
    /*
     * Support Methods
     */

//    /**
//     * Return the document model for the text editor
//     * used by this GUI component.
//     *
//     * @return  the SWING document model controlling the editor
//     * 
//     * @since  Nov 23, 2009
//     * @author Christopher K. Allen
//     */
//    protected StyledDocument    getStyle() {
//        return this.docText;
//    }
    
    /**
     * Initializes the GUI for the view.
     *
     * 
     * @since  Aug 17, 2009
     * @author Christopher K. Allen
     */
    private void initialize()   {
//        SimpleAttributeSet attrs   = new SimpleAttributeSet();
//
//        StyleConstants.setForeground(attrs, Color.blue);
//        StyleConstants.setFontFamily(attrs, "Serif");
//        this.paneText.setCharacterAttributes(attrs, false);
        
        this.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }


}
