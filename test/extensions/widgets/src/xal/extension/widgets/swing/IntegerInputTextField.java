/*
 * IntegerInputTextField.java
 *
 * @author  shishlo
 *
 * Created on September 23, 2003, 10:20 AM
 */

package xal.extension.widgets.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;


/**
 * This is an input formatted text field for an integer value.
 * This text field will have the special alert background color is the changes are
 * not committed. After posting changes the background color will be normal color again.
 * In addition to usual JTextField methods it provides 
 * <code> getValue() </code> and <code> setValue(int val) </code> methods
 * to get and to set integer value to this container. User has to add one
 * or several ActionListeners as usual to interact with the external code.
 * By default the format of this text field is ###. To specify another 
 * format the method <code> setDecimalFormat </code>should be used.
 */


public class IntegerInputTextField extends JTextField{
	private static final long serialVersionUID = 0L;

    private Color alertColor  = Color.orange ;
    private Color normalColor = null;

    private DecimalFormat format = new DecimalFormat("###");
    private int val = 0;

    private DocumentListener docListener = null;

    private ActionListener innerListener = null;

    private FocusListener innerFocusListener = null;

    /** Creates new IntegerInputTextField */
    public IntegerInputTextField(){
        super();
	setListeners();
    }

    /** Creates new IntegerInputTextField with predefined number of columns */
    public IntegerInputTextField(int col){
        super(col);
	setListeners();
    }

    /** Creates new IntegerInputTextField with predefined text */
    public IntegerInputTextField(String text){
	super(text);
	try{
	    val = Integer.parseInt(text);
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }

    /** Creates new IntegerInputTextField with predefined text and number of columns */
    public IntegerInputTextField(String text,int col){
	super(text,col);
	try{
	    val = Integer.parseInt(text);
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }

    /** Creates new IntegerInputTextField with predefined value, 
     *  format, and number of columns 
     */
    public IntegerInputTextField(int valIn, DecimalFormat formatIn, int col){
	super(col);
	if(formatIn != null){
	    format = formatIn;
	}
	try{
	    val = Integer.parseInt(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }


    /** Returns the value */
    public int getValue(){
	return val;
    }
    
    /**
     * Returns the number formatter currently used
     * by the input field.
     *
     * @return  decimal number formatter
     * 
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public DecimalFormat getDecimalFormat() {
        return this.format;
    }
    
    /**
     * Return the background color used when alerting
     * the user.
     *
     * @return  text field alert background color
     * 
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public Color        getAlertColor() {
        return this.alertColor;
    }
    
    /**
     * Return the normal background color of the
     * text field. 
     *
     * @return  background color of the text field
     * 
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public Color        getNormalColor() {
        return this.normalColor;
    }

    
    
    
    /** Sets the value */
    public int setValue(int valIn){
	try{
	    val = Integer.parseInt(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
        postActionEvent();
	return val;	
    }


    /** Sets the value quietly. The external listeners 
     *  do not receive the "action performed" call.
     */
    public int setValueQuietly(int valIn){
	try{
	    val = Integer.parseInt(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setBackground(normalColor);
	return val;	
    }

    /** Sets the format */
    public void setDecimalFormat(DecimalFormat formatIn){
	if(formatIn != null){
	    format = formatIn;
	}
	try{
	    val = Integer.parseInt(format.format(val));
	    setValue(val);
	}
	catch (NumberFormatException  exc){
	    setText("bad format");
	}
    }

    /** Sets the alert background color */
    public void setAlertBackground(Color alertColor){
	this.alertColor = alertColor;
    }

    /** Sets the normal background color */
    public void setNormalBackground(Color normalColor){
	this.normalColor = normalColor;
	setBackground(normalColor);
    }

    /** Fires ActionPerformed method of superclass */
    @Override
    protected void fireActionPerformed(){
	innerListener.actionPerformed(new ActionEvent(this,0,"changes"));
	super.fireActionPerformed();
    } 

    /** Overrides setDocument method of superclass 
     *  to keep existing document listener 
     */
    @Override
    public void setDocument(Document doc){
	if(doc == null ) return;
	Document docOld = getDocument();
	if(docOld != null) {
	    docOld.removeDocumentListener(docListener);
	}
        doc.addDocumentListener(docListener);
        super.setDocument(doc);
    }

    /** Remove inner FocusListener 
     */
    public void removeInnerFocusListener(){
	removeFocusListener(innerFocusListener);
    }

    /** Restore inner FocusListener 
     */
    public void restoreInnerFocusListener(){
	addFocusListener(innerFocusListener);
    }

    private void setListeners(){
        normalColor = getBackground();

	MouseAdapter mAdpt = new MouseAdapter(){
		@Override
        public void mousePressed(MouseEvent e){
		    if(!isEditable()) return;
                   setBackground(alertColor);
		}
	    };

	addMouseListener(mAdpt);

	//we need this empty listener to fire action
	ActionListener emptyListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		}
	    }; 

	addActionListener(emptyListener);

	innerListener = new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    try{
			val = Integer.parseInt(getText());
		    }
		    catch (NumberFormatException  exc){}
		    setText(format.format(val));
                    setBackground(normalColor);
		}
	    }; 


	innerFocusListener = new FocusListener(){
		public void focusGained(FocusEvent e){}
		public void focusLost(FocusEvent e) {
		    if(!isEditable()) return;
		    try{
			val = Integer.parseInt(getText());
		    }
		    catch (NumberFormatException  exc){}
		    setText(format.format(val));
                    setBackground(normalColor);
		    postActionEvent();
		} 
	    };

	addFocusListener(innerFocusListener); 

	docListener = new DocumentListener(){
		public void changedUpdate(DocumentEvent e){ setBackground(alertColor);}
		public void insertUpdate(DocumentEvent e) { setBackground(alertColor);}
		public void removeUpdate(DocumentEvent e) { setBackground(alertColor);}

	    };

	getDocument().addDocumentListener(docListener);
	
    }
}


