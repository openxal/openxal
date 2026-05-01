package xal.app.pta.tools.swing;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.PlainDocument;

/**
 * Supports the <code>DataListener</code> interface for Swing
 * <code>DefaultStyledDocument</code> objects.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  May 7, 2010
 * @author Christopher K. Allen
 */
public class PersistentDocument extends DefaultStyledDocument implements DataListener {

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    
    /**  Attribute used by the data adaptors to save/retrieve editor text */
    private static final String STR_ATTR_TEXT = "txt";

    

    /*
     * Operations
     */
    
    /**
     * Returns all the text in the document. Implemented with
     * <code>{@link PlainDocument#getText(int, int)}</code>
     * with the arguments <code>(0, length)</code> where
     * <code>length</code> is the number of characters 
     * in the document.
     *
     * @return      all the characters in the document      
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public String   getText() {
        
        try {
            int    cntChars = this.getLength();
            String strText  = this.getText(0, cntChars);
            
            return strText;
            
        } catch (BadLocationException e) { // this isn't going to happen
        }
        
        return "";
    }
    
    /**
     * Sets all the contexts of the document to the 
     * given string.  Anything in the document prior
     * to the method call is deleted.
     *
     * @param strText       new document content
     * 
     * @since  May 7, 2010
     * @author Christopher K. Allen
     */
    public void     setText(String strText) {
        try {
            int  cntChars = this.getLength();
            this.remove(0, cntChars);
            this.insertString(0, strText, null);
        } catch (BadLocationException e) {
        }
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
        return this.getClass().getName();
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
       DataAdaptor      daptChild = daptSrc.childAdaptor( this.dataLabel() );
       
       if (daptChild.hasAttribute(STR_ATTR_TEXT)) {
           String       strText = daptChild.stringValue(STR_ATTR_TEXT);
           
           this.setText(strText);
       }
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

       DataAdaptor      daptChild = daptSnk.createChild( this.dataLabel() );
       daptChild.setValue(STR_ATTR_TEXT, this.getText());
   }
}