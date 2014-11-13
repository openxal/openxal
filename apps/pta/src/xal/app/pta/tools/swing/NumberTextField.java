/**
 * NumberTextField.java
 *
 *  Created	: Jan 5, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * <p>
 * This is a GUI text field for numeric values.  The text is formatted
 * for any type of numeric value.
 * </p>
 * <p>
 * The text field will have a special alerting background color while the user 
 * changes are uncommitted. After posting changes the background color will 
 * return to normal again.
 * </p>
 * <p>
 * In addition to usual JTextField methods it provides 
 * <code>getValue()</code> and <code>setValue(Number)</code> methods
 * to get and to set numeric values of the field. The user must add one
 * or several <code>ActionListeners</code>, as usual, to interact 
 * with external code.
 * </p>
 * <p>
 * By default the format of this text field is <i>###</i>. To specify another 
 * format the method <code>setDecimalFormat()</code>should be used.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Jan 5, 2010
 * @author Christopher K. Allen
 * @author Andrei Shishlo
 */
public class NumberTextField extends JTextField {

    /**
     * Enumeration of the supported numeric display formats.
     *
     * @since  Jan 7, 2010
     * @author Christopher K. Allen
     */
    public enum FMT {
        
        /** (Regional) default numeric format */
        DEFAULT(null),
        
        /** Integer format */
        INT("###,###"),
        
        /** Decimal format - force decimal point and use as many digits as needed */
        DEC("##0.0##"),
        
        /** Decimal format - use strictly three decimal places */
        DEC_3("##0.000"),
        
        /** Decimal format - use strictly four decimal places */
        DEC_4("##0.0000"),
        
        /** Scientific notation with two significant digits */
        SCI_2("0.##E0"),
        
        /** Scientific notation with three significant digits */
        SCI_3("0.###E0"),
        
        /** Engineering notation with two significant digits */
        ENGR_2("##0.##E0"),
        
        /** Engineering notation with three significant digits */
        ENGR_3("##0.###E0");
        
        
        /**
         * Return the format pattern for this 
         * enumeration constant. 
         *
         * @return      numeric format pattern for <code>DecimalFormat</code> objects
         *
         * @since  Jan 7, 2010
         * @author Christopher K. Allen
         */
        public String getFormatPattern() {
            return strFmtPatt;
        }
        
        /**
         * Returns the <code>NumberFormat</code>
         * object that formats numbers according to 
         * the pattern specified by the enumeration constant. 
         *
         * @return      number formatter to parse number strings and
         *              format numeric values into strings
         * 
         * @since  Jan 7, 2010
         * @author Christopher K. Allen
         */
        public NumberFormat     getFormatter() {
            
            return this.fmtNumber;
        }
        

        
        /*
         * Private
         */
        
        /** The format pattern string */
        final private String    strFmtPatt;
        
        /** The formatter object for this enumeration constant */
        final private NumberFormat      fmtNumber;
        
        /**
         * Create a new <code>FMT</code> object.
         *
         * @param strFmtPatt    format pattern for the enumeration
         *
         * @since     Jan 7, 2010
         * @author    Christopher K. Allen
         */
        private FMT(final String strFmtPatt) {
            this.strFmtPatt = strFmtPatt;
            
            if (strFmtPatt == null)
                this.fmtNumber = NumberFormat.getInstance();
            else
                this.fmtNumber = new DecimalFormat( strFmtPatt );
        }
    }
    
    

    
    /**
     * Responds to user activity in the
     * text field.  We react to user edits
     * by changing the color of the display background.
     *
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     * 
     * @see     NumberTextField#CLR_EDIT
     */
    class EditAction implements DocumentListener {

        /**
         * An attribute of the text field has changed - change color.
         * 
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
         */
        public void changedUpdate(DocumentEvent e){ 
            setBackground( getBkgndEditColor() );
        }

        /**
         * Text was inserted into the the text field.
         * 
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
         */
        public void insertUpdate(DocumentEvent e) { 
            setBackground( getBkgndEditColor() );
        }

        /**
         * Text was removed from the text field.
         *
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
         */
        public void removeUpdate(DocumentEvent e) { 
            setBackground( getBkgndEditColor() );
        }
    }
    
    /**
     * Responds to user focus events on the GUI text
     * field.  If focus is lost we update the text display. 
     *
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    class FocusAction implements FocusListener {
        
        /** The parent Swing component - i.e., enclosing class */
        private final NumberTextField        cmpParent;
        
        /**
         * Create a new <code>FocusAction</code> object.
         *
         * @param cmpParent     the parent container of this object.
         *
         * @since     Jan 6, 2010
         * @author    Christopher K. Allen
         */
        public FocusAction(NumberTextField cmpParent) {
            this.cmpParent = cmpParent;
        }
        
        /**
         * The text field gained user focus.  We do nothing.
         * 
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
         */
        public void focusGained(FocusEvent e) {
        }
        
        /**
         * The text field lost user focus. We update the text display
         * and post an action event occurrence.
         *
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
         */
        public void focusLost(FocusEvent e) {
            if(this.cmpParent.isEditable()) { 
                this.cmpParent.updateDisplay();
                this.cmpParent.setBackground( getBkgndNormalColor() );
//                this.cmpParent.postActionEvent();
            }
        } 
    }

    /**
     * Responds to user mouse events.  In particular,
     * if the mouse is pressed we change colors.
     *
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    class MouseAction extends MouseAdapter {
        
        /** The parent Swing component - i.e., enclosing class */
        private final NumberTextField        cmpParent;

        
        /**
         * Create a new <code>MouseAction</code> object.
         *
         * @param cmpParent     the parent containing this object
         *
         * @since     Jan 6, 2010
         * @author    Christopher K. Allen
         */
        public MouseAction(NumberTextField cmpParent) {
            this.cmpParent = cmpParent;
        }

        /**
         * Change the background color of the text field
         * to reflect the edit mode.
         * 
         * @since 	Jan 6, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e){
            if(this.cmpParent.isEditable()) 
                this.cmpParent.setBackground( getBkgndEditColor() );
        }
    };


    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    /*
     * Global Attributes
     */
    
    /** Default Background color for unsaved changes */
    private static final Color  CLR_EDIT  = /* Color.LIGHT_GRAY; */ new Color(200, 200, 255);
    
    /** Default Normal background color */ 
    private static final Color  CLR_NORML = Color.WHITE;
    
    
    /** The AWT framework instance */
    private static final Toolkit AWT_TOOLKIT = Toolkit.getDefaultToolkit();
    
    

    
    /*
     * Local Attributes
     */
    
    /** Number format seen in text field */
    private NumberFormat  fmtDispl;
    
    /** Color of background when editing */
    private Color         clrEdit;
    
    /** Color of background when normal */
    private Color         clrNorml;         
    

    /*
     * Initialization
     */
    

    /**
     * Create a new <code>NumberTextField</code> object with
     * all default settings.
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField() {
        this(FMT.DEFAULT, null, 0, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>NumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField(FMT fmtDispl) {
        this(fmtDispl, null, 0, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>NumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param numInit   the initial value shown in the display
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField(FMT fmtDispl, Number numInit) {
        this(fmtDispl, null, 0, numInit);
    }

    /**
     * Create a new <code>NumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param cntCols   number of columns in the text display
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField(FMT fmtDispl, int cntCols) {
        this(fmtDispl, null, cntCols, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>NumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param cntCols   number of columns in the text display
     * @param numInit   the initial value shown in the display
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField(FMT fmtDispl, int cntCols, Number numInit) {
        this(fmtDispl, null, cntCols, numInit);
    }

    /**
     * Create a new <code>NumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param docText   the text storage to use
     * @param cntCols   number of columns in the text display
     * @param numInit   the initial value shown in the display
     *
     * @since     Jan 5, 2010
     * @author    Christopher K. Allen
     */
    public NumberTextField(FMT fmtDispl, Document docText, int cntCols, Number numInit) {
        super(docText, null, cntCols);

        this.clrEdit  = CLR_EDIT;
        this.clrNorml = CLR_NORML;
        this.fmtDispl = fmtDispl.getFormatter();
        
        this.setDisplayValueSilently(numInit);
//        this.updateDisplay();
        this.initEventActions();
    }
    
    /*
     * Operations
     */
    
    /**
     * Removes all text from the display.  The 
     * numeric value becomes undefined.
     *
     * 
     * @since  Jan 8, 2010
     * @author Christopher K. Allen
     */
    public void clearDisplay() {
        super.setText("");
        super.setBackground( this.getBkgndNormalColor() );
    }
    
    /**
     * Set a custom display format for the numeric
     * values.
     *
     * @param fmtDspl   new format object to use for displaying values
     * 
     * @since  Jan 13, 2010
     * @author Christopher K. Allen
     */
    public void setDisplayFormat(NumberFormat fmtDspl) {
        this.fmtDispl = fmtDspl;
    }
    
    /**
     * Set the background color used for normal
     * text display.
     *
     * @param clrNorml  new background color
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setBkgndNormalColor(Color clrNorml) {
        this.clrNorml = clrNorml;
    }
    
    /**
     * Set the background color used during editing.
     *
     * @param clrEdit   new background color
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public void setBkgndEditColor(Color clrEdit) {
        this.clrEdit = clrEdit;
    }
    
    /**
     * Sets the numeric value seen on the text display.
     *
     * @param numVal    value to display
     * 
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    public void setDisplayValue(Number numVal){
        try {
            String  strVal = this.fmtDispl.format(numVal);
            
            super.setText(strVal);
            super.postActionEvent();
            
        } catch (IllegalArgumentException e) {
            // Do nothing
        }
    }


    /** 
     *  Sets the display value quietly. The external 
     *  event listeners do not receive the "action performed" 
     *  notification.
     *  
     * @param numVal    value to display
     *  
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    public void setDisplayValueSilently(Number numVal){
        
//        System.out.println("NumberTextField#setDisplayValueSilently: numVal = " + numVal);
        
        try {
            String  strVal = this.fmtDispl.format(numVal);
            
            super.setText(strVal);
            super.setBackground( this.getBkgndNormalColor() );
            
        } catch (IllegalArgumentException e) {
            // Do nothing
        }
    }

    /**
     * Returns the numeric value of the text shown
     * in the display.  The text is parsed according
     * to the current numeric format pattern.
     *
     * @return  numeric value of the text representation in display 
     * 
     * @throws NumberFormatException    since the display only allows proper formats
     *                                  to be entered, this should not occur
     * 
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    public Number getDisplayValue() throws NumberFormatException {
        try {
            String  strVal = super.getText();
            Number  numVal = this.fmtDispl.parse(strVal);
            
            return numVal;
            
        } catch (ParseException e) {
            throw new NumberFormatException(e.getMessage());
            
        } 
    }
    
    /**
     * Returns the background color used during user
     * editing.
     *
     * @return  background color during editing
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public Color getBkgndEditColor() {
        return this.clrEdit;
    }
    
    /**
     * Returns the background color used during normal
     * display of numeric text.
     *
     * @return  normal background color
     * 
     * @since  Apr 26, 2010
     * @author Christopher K. Allen
     */
    public Color getBkgndNormalColor() {
        return this.clrNorml;
    }
    
    
    /*
     * JTextField Overrides
     */
    
    /**
     * Notify all listeners that an event occurred.  We
     * get the input text, parse it into desired numeric format,
     * then replace the original text.  If we are unable to 
     * parse the text nothing is done.
     *
     * @since 	Jan 6, 2010
     * @author  Christopher K. Allen
     *
     * @see javax.swing.JTextField#fireActionPerformed()
     */
    @Override
    protected void fireActionPerformed(){
        
//        System.out.println("NumberTextField#fireActionPerformed: display value = " + getDisplayValue());
        this.updateDisplay();
        super.fireActionPerformed();
    } 

    
    /*
     * Subclass Methods
     */
    
    /**
     * We get the input text, parse it into desired numeric format,
     * then replace the original text.  If we are unable to 
     * parse the text nothing is done.
     *
     * 
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    protected void updateDisplay() {
        try{
            String  strInput = this.getText();
            Number  numInput = this.fmtDispl.parse(strInput);
            
//            System.out.println("NumberTextField#updateDisplay: numInput = " + numInput);
//            System.out.println();
            
//            String  strDispl = this.fmtDispl.format(numInput);

            // TODO: Clean this up
//            setText(strDispl);
            this.setDisplayValueSilently(numInput);
            setBackground( this.getBkgndNormalColor() );
            
        } catch (ParseException e) {
            AWT_TOOLKIT.beep();
        }
    }
    
    
    /*
     * Support Methods
     */

    /**
     * Creates and adds all the Swing event responses
     * to text field.
     * 
     * @since  Jan 6, 2010
     * @author Christopher K. Allen
     */
    private void initEventActions(){

        // We need this empty listener to fire actions
        ActionListener lsnEmpty = new ActionListener(){
            public void actionPerformed(ActionEvent e){
            }
        };

        this.addActionListener( lsnEmpty );
        this.addMouseListener( new MouseAction(this) );
        this.addFocusListener( new FocusAction(this) );
        this.getDocument().addDocumentListener( new EditAction() );
    }
    
    
}
