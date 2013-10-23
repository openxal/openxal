package xal.extension.widgets.swing;

import javax.swing.*;
import java.text.*;


/** Convenient preconfigured formatted text field for handling double values */
public class DecimalField extends JFormattedTextField {
    /** variable required for serializable objects */
    final private static long serialVersionUID = 1L;
    
    
    /** Empty constructor */
    public DecimalField() {
        this( 0.0, 17, NumberFormat.getNumberInstance() );
    }
    
    
    /** Primary Constructor with value, column width and format */
    public DecimalField( final double value, final int columns, final NumberFormat format ) {
        super( format );
        setColumns( columns );
        setValue( value );
        setHorizontalAlignment( JTextField.RIGHT );
    }

    
    /** Constructor with value and column width */
    public DecimalField( final double value, final int columns ) {
        this( value, columns, NumberFormat.getNumberInstance() );
    }

    
    /** Get the value as a double */
    public double getDoubleValue() {
        return ((Number)getValue()).doubleValue();
    }
    
    public void clear() {
        setText(null);
    }
}
