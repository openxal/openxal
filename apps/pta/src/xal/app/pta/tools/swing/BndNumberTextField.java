/**
 * BndNumberTextField.java
 *
 *  Created	: Jan 7, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;


import javax.swing.text.Document;

/**
 * Displays numbers that are constrained within a 
 * specified interval. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 7, 2010
 * @author Christopher K. Allen
 */
public class BndNumberTextField extends NumberTextField {

    
    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    /*
     * Local Attributes
     */
    
    /** The current value - needed to reject bad inputs */
    private Number      numVal;
    
    /** Minimum value for display */
    private Number      numMin;
    
    /** Maximum value for the display */
    private Number      numMax;
    
    
    /*
     * Initialization
     */

    /**
     * Create a new <code>BndNumberTextField</code> object with
     * all default settings.
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField() {
        this(FMT.DEFAULT, null, 0, 0, 0, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl) {
        this(fmtDispl, null, 0, 0, 0, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param numMin    the minimum allowed value (inclusive) 
     * @param numMax    the maximum allowed value (inclusive)
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl, Number numMin, Number numMax) {
        this(fmtDispl, null, 0, numMin, numMax, null);
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param numMin    the minimum allowed value (inclusive) 
     * @param numMax    the maximum allowed value (inclusive)
     * @param numVal    the initial value shown in the display
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl, Number numMin, Number numMax, Number numVal) {
        this(fmtDispl, null, 0, numMin, numMax, numVal);
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param cntCols   number of columns in the text display
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl, int cntCols) {
        this(fmtDispl, null, cntCols, 0, 0, 0);
        this.clearDisplay();
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param cntCols   number of columns in the text display
     * @param numMin    the minimum allowed value (inclusive) 
     * @param numMax    the maximum allowed value (inclusive)
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl, int cntCols, Number numMin, Number numMax) {
        this(fmtDispl, null, cntCols, numMin, numMax, null);
    }

    /**
     * Create a new <code>BndNumberTextField</code> object.
     *
     * @param fmtDispl  the numeric formatter for displaying/parsing numbers 
     * @param docText   the text storage to use
     * @param cntCols   number of columns in the text display
     * @param numVal    the initial value shown in the display
     * @param numMin    the minimum allowed value (inclusive) 
     * @param numMax    the maximum allowed value (inclusive)
     *
     * @since     Jan 7, 2010
     * @author    Christopher K. Allen
     */
    public BndNumberTextField(FMT fmtDispl, Document docText, int cntCols, 
                               Number numMin, Number numMax, Number numVal) 
    {
        super(fmtDispl, docText, cntCols, numVal);
        
        this.numVal = numVal;
        this.numMin = numMin;
        this.numMax = numMax;
        
        this.setDisplayValueSilently(numVal);
    }

    
    
    /**
     * Set the lower boundary limit for input.
     *
     * @param numMin    lower input limit
     *
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public void setMinValue(Number numMin) {
        this.numMin = numMin;
    }


    /**
     * Set the upper boundary limit for input.
     *
     * @param numMax    upper input limit
     *
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public void setMaxValue(Number numMax) {
        this.numMax = numMax;
    }

    
    
    /*
     * Attribute Query
     */

    /**
     * Return the lower input limit. 
     *
     * @return minimum input value
     *
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public Number getMinValue() {
        return this.numMin;
    }


    /**
     * Return the upper input limit.
     *
     * @return maximum input value
     *
     * @since  Jan 5, 2010
     * @author Christopher K. Allen
     */
    public Number getMaxValue() {
        return this.numMax;
    }


    
    
    /**
     * Makes the string representation of an
     * interval between the given numbers.
     *
     * @return          interval label
     * 
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    public String      makeIntervalLabel() {
        
        StringBuffer    bufLabel = new StringBuffer();
        Number          numMin   = this.getMinValue();
        Number          numMax   = this.getMaxValue();
        
        bufLabel.append("[");
        bufLabel.append( this.formatNumber(numMin) );
        bufLabel.append(",");
        bufLabel.append( this.formatNumber(numMax) );
        bufLabel.append("]");
                        
        return bufLabel.toString();
    }
    
    /*
     * Enforce Value Limits
     */
    
    
    /**
     * Sets the value displayed in the text field.  If the 
     * given value lies outside the valid domain nothing 
     * is done.
     *
     * @param   numVal  new value displayed in text field
     * 
     * @since   Jan 5, 2010
     * @author  Christopher K. Allen
     * 
     * @see xal.app.pta.tools.swing.NumberTextField#setDisplayValue(Number)
     *
     */
    @Override
    public void setDisplayValue(Number numVal) {
        
        if ( this.isValidValue(numVal) ) {
            this.numVal = numVal;
            super.setDisplayValue(numVal);
        }
    }


    /**
     * Sets the value displayed in the text field without firing
     * an action event.  If the 
     * given value lies outside the valid domain nothing 
     * is done.
     *
     * @param   numVal new value displayed in text field
     * 
     * @since   Jan 5, 2010
     * @author  Christopher K. Allen
     *
     * @see gov.sns.tools.swing.IntegerInputTextField#setValueQuietly(int)
     */
    @Override
    public void setDisplayValueSilently(Number numVal) {
        
        if ( this.isValidValue(numVal) ) {
            this.numVal = numVal;
            super.setDisplayValueSilently(numVal);
        }
    }

    /**
     * We need to clear the stored current value.
     *
     * @since   Jan 11, 2010
     * @author  Christopher K. Allen
     *
     * @see xal.app.pta.tools.swing.NumberTextField#clearDisplay()
     */
    @Override
    public void clearDisplay() {
        this.numVal = null;
        super.clearDisplay();
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
    @Override
    protected void updateDisplay() {
        try{
            Number  numInput = super.getDisplayValue();
            
//            System.out.println("BndNumberTextField#updateDisplay: numInput = " + numInput);
            
            if ( this.isValidValue(numInput) ) {
                this.numVal = numInput;
                super.updateDisplay();
            
            } else { 
                super.setDisplayValueSilently(this.numVal);
                
            }
            
        } catch (NumberFormatException e) {
            setDisplayValueSilently(this.numVal);
            
        }
    }
    
    /**
     * Responds to any user action - presumably a change in 
     * value.  We first check if the value is an integer, then
     * if it lies in the input domain.  If so we call the super
     * class method, if either condition is not met nothing is 
     * done. 
     *
     * @since   Jan 5, 2010
     * @author  Christopher K. Allen
     *
     * @see gov.sns.tools.swing.IntegerInputTextField#fireActionPerformed()
     */
    @Override
    protected void fireActionPerformed() {
        
        try {
            Number  numInput = super.getDisplayValue();
            
//            System.out.println("BndNumberTextField#fireActionPerformed: numInput = " + numInput);
            
            if ( this.isValidValue(numInput) ) {
                this.numVal = numInput;
                super.fireActionPerformed();
            
            } else {
                super.setDisplayValueSilently(this.numVal);
                
            }
            
        } catch (NumberFormatException  exc) {
            this.setDisplayValueSilently(this.numVal);
            
        }
    }


    
    /*
     * Support Methods
     */
    
    /**
     * Check if the given value is in the interval
     * [<i>num<sub>min</sub></i>,<i>num<sub>max</sub></i>].
     * We first convert everything to <code>double</code>
     * type.
     *
     * @param numVal
     * @return
     * 
     * @since  Jan 7, 2010
     * @author Christopher K. Allen
     */
    private boolean isValidValue(Number numVal) {
        
        // Check for incomplete initialization
        if (numVal==null || this.numMin==null || this.numMax==null)
            return false;

        double  dblVal = numVal.doubleValue();
        double  dblMin = this.numMin.doubleValue();
        double  dblMax = this.numMax.doubleValue();
        
        if (dblMin <= dblVal && dblVal <= dblMax)
            return true;
        else
            return false;
    }

    /**
     * Provides a string representation of
     * the given number, suitable for labels
     * of text fields.
     *
     * @param   num     number of type <code>Integer</code> or <code>Double</code>
     * 
     * @return          string representation of number
     * 
     * @since  Jan 4, 2010
     * @author Christopher K. Allen
     */
    private String      formatNumber(Number num) {

        if (num instanceof Integer && num.intValue() < 1e5) {
            String  strVal = String.format("%d", num.intValue());
            
            return strVal;
        }
        
        if (num instanceof Double && num.doubleValue() < 1.0e5 && num.doubleValue() > 1.0e-4) {
            double      dblVal = num.doubleValue(); 
            
            return FMT.DEC.getFormatter().format(dblVal);
        }

        if (num instanceof Integer) {
            int     intVal = num.intValue();
            String  strVal = FMT.ENGR_3.getFormatter().format(intVal);
            
            return strVal;
            
        } else  {
            
            double      dblVal = num.doubleValue();
            String      strVal = FMT.ENGR_3.getFormatter().format(dblVal);
            
            return strVal;
        }
    }
    

    
}
