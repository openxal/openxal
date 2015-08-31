/*
 * DoubleInputTextField.java
 *
 * @author  shishlo
 *
 * Created on September 23, 2003, 10:20 AM
 */

package xal.extension.widgets.swing;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.text.*;

/**
 * This is an input formatted text field for a double value.
 * This text field will have the special alert background color is the changes are
 * not committed. After posting changes the background color will be normal color again.
 * In addition to usual JTextField methods it provides 
 * <code> getValue() </code> and <code> setValue(double val) </code> methods
 * to get and to set double value to this container. User has to add one
 * or several ActionListeners as usual to interact with the external code.
 * By default the format of this text field is ###.###. To specify another 
 * format the method <code> setNumberFormat </code>should be used.
 */

public class DoubleInputTextField extends JTextField{
	private static final long serialVersionUID = 0L;

    private Color alertColor  = Color.orange ;
    private Color normalColor = null;

    private NumberFormat format = new DecimalFormat("####.###");
    private double val = 0.0;

    private DocumentListener docListener = null;

    private ActionListener innerListener = null;

    private FocusListener innerFocusListener = null;

    /** Creates new DoubleInputTextField */
    public DoubleInputTextField(){
        super();
	setListeners();
    }

    /** Creates new DoubleInputTextField with predefined number of columns */
    public DoubleInputTextField(int col){
        super(col);
	setListeners();
    }

    /** Creates new DoubleInputTextField with predefined text */
    public DoubleInputTextField(String text){
	super(text);
	try{
	    val = Double.parseDouble(text);
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }

    /** Creates new DoubleInputTextField with predefined text and number of columns */
    public DoubleInputTextField(String text,int col){
	super(text,col);
	try{
	    val = Double.parseDouble(text);
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }

    /** Creates new DoubleInputTextField with predefined value, 
     *  format, and number of columns 
     */
    public DoubleInputTextField(double valIn, NumberFormat formatIn, int col){
	super(col);
	if(formatIn != null){
	    format = formatIn;
	}
	try{
	    val = Double.parseDouble(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setListeners();
    }

    /** Returns the value */
    public double getValue(){
	return val;
    }
    
    /** Returns the integer value */
    public int getIntValue(){
        return ((int) getValue());
    }    

    /** Sets the value */
    public double setValue(double valIn){
	try{
	    val = Double.parseDouble(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
        postActionEvent();
	return val;	
    }
    
    /** Sets the integer value */
    public int setValue(int valIn){
        return ((int) setValue( (double) valIn));
    }    

    /** Sets the value quietly. The external listeners 
     *  do not receive the "action performed" call.
     */
    public double setValueQuietly(double valIn){
	try{
	    val = Double.parseDouble(format.format(valIn));
	}
	catch (NumberFormatException  exc){}
	setText(format.format(val));
	setBackground(normalColor);
	return val;	
    }


	/** 
	 * @deprecated use setNumberFormat() instead as constraining to DecimalFormat is not necessary and there should be
	 * no expectation that the internal number format is a DecimalFormat. This method is around temporarily for backward compatibility
	 * but will be removed in the near future.
	 * Set the number format
	 */
	@Deprecated
	public void setDecimalFormat( final DecimalFormat formatIn ) {
		setNumberFormat( formatIn );
	}

    /** Sets the format */
    public void setNumberFormat( final NumberFormat formatIn ){
		if(formatIn != null){
			format = formatIn;
		}
		try{
			val = Double.parseDouble(format.format(val));
			setValue(val);
		}
		catch (NumberFormatException  exc){
			setText("bad format");
		}
    }


	/** Returns the format */
	public NumberFormat getNumberFormat(){
		return format;
	}


    /** Sets the integer value quietly. The external listeners
     *  do not receive the "action performed" call.
     */
    public int setValueQuietly(int valIn){ 
        return ( (int) setValueQuietly((double) valIn) );
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
    protected void fireActionPerformed(){
	innerListener.actionPerformed(new ActionEvent(this,0,"changes"));
	super.fireActionPerformed();
    } 

    /** Overrides setDocument method of superclass 
     *  to keep existing document listener 
     */
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
			val = Double.parseDouble(getText());
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
			val = Double.parseDouble(getText());
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


