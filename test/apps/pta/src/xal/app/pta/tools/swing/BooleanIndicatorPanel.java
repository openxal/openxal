/**
 * BooleanIndicatorPanel.java
 *
 *  Created	: Jan 25, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

/**
 * Component for displaying the boolean states.
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 22, 2010
 * @author Christopher K. Allen
 */
public class BooleanIndicatorPanel extends JPanel {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long serialVersionUID = 1L;
    
    
    
    /** Default Color displayed for ON condition */
    public static final Color    CLR_ON = Color.RED;
    
    /** Default color displayed for normal operation */
    public static final Color    CLR_OFF = Color.GREEN;
    
    /** Color when undefined */
    public static final Color    CLR_UNSET = Color.GRAY;
    
    
    
    /** Size of the error condition display */
    private static final Dimension DIM_ERR_PNL = new Dimension(18,12);
    
    
    
    
    /*
     * Local Attributes
     */
    
    /** Label of the component */
    private final JLabel          lblParam;
    
    /** Value of error condition */
    private final Integer         intValOn;
    
    /** Value of the normal condition */
    private final Integer         intValOff;

    
    /** Error condition indicator pane */
    private JPanel              pnlIndicate;
    
    
    /** The ON state indicator color */
    private Color               clrOn;
    
    /** The OFF state indicator color */
    private Color               clrOff;
    
    /** The unset (known) indicator color */
    private Color               clrUnset;
    
    
//    /** The neutral background color */
//    private Color               clrNtrl;
    
    
    /**
     * Create a new <code>BooleanIndicatorPanel</code> object.
     *
     * @param strLabel      label of the box 
     * @param intValOn      numeric value of ON
     * @param intValOff     numeric value of OFF
     *
     * @since     Jan 22, 2010
     * @author    Christopher K. Allen
     */
    public BooleanIndicatorPanel(String strLabel, Integer intValOn, Integer intValOff) {
        this.intValOn  = intValOn;
        this.intValOff = intValOff;
        
        this.lblParam = new JLabel(strLabel);

        this.initColors();
        this.buildErrorPanel();
        this.buildGui();
    }
    
    /**
     * Set the <b>ON</code> value indicator display color.
     * 
     * @param clrOn     new display color for ON state
     * 
     * @since  Jan 27, 2010
     * @author Christopher K. Allen
     */
    public void setOnColor(Color clrOn) {
        this.clrOn = clrOn;
    }
    
    /**
     * Set the <b>OFF</b> value indicator display color.
     * 
     * @param clrOff     new display color for OFF state
     * 
     * @since  Jan 27, 2010
     * @author Christopher K. Allen
     */
    public void setOffColor(Color clrOff) {
        this.clrOff = clrOff;
    }
    
    /**
     * Set the <b>UNSET</code> value indicator display color.
     * 
     * @param clrUnset     new display color for "Unknown" state
     * 
     * @since  Jan 27, 2010
     * @author Christopher K. Allen
     */
    public void setUnsetColor(Color clrUnset) {
        this.clrUnset = clrUnset;
    }
    
    /**
     * Returns the value used to represent <tt>ERROR</tt>.
     *
     * @return      the numeric value of <code>ERROR</code>
     * 
     * @since  Jan 16, 2010
     * @author Christopher K. Allen
     */
    public Integer  getOnValue() {
        return this.intValOn;
    }
    
    /**
     * Returns the value used to represent <tt>NORMAL</tt>.
     *
     * @return      the numeric value of <code>NORMAL</code>
     * 
     * @since  Jan 16, 2010
     * @author Christopher K. Allen
     */
    public Integer  getOffValue() {
        return this.intValOff;
    }

    /**
     * Checks the button if the given value is equal to 
     * the numeric value for <code>false</code>, unchecks the
     * button if the given value is equal to the numeric value
     * for <code>true</code>, and does nothing otherwise.
     *
     * @param intVal        value to be evaluated as <code>true</code> or <code>false</code>
     * 
     * @since  Jan 15, 2010
     * @author Christopher K. Allen
     */
    public void     setDisplayValue(Integer intVal) {
        
        if (intVal == this.intValOn) {
            this.pnlIndicate.setBackground(this.clrOn);
        
        } else if (intVal == this.intValOff) {
            this.pnlIndicate.setBackground(this.clrOff);
            
        } else {
            this.pnlIndicate.setBackground(this.clrUnset);
            
        }
    }
    
    /**
     * Sets indicator to neutral.
     *
     * 
     * @since  Jan 21, 2010
     * @author Christopher K. Allen
     */
    public void     clearDisplay() {
        this.pnlIndicate.setBackground(this.clrUnset);
    }

    
    /*
     * Support
     */
    
    /**
     * Initialize the colors used in the indicator
     * panel to the default values.
     *
     * 
     * @since  Jan 27, 2010
     * @author Christopher K. Allen
     */
    private void initColors() {
        this.clrOn    = CLR_ON;
        this.clrOff   = CLR_OFF;
        this.clrUnset = CLR_UNSET;
        
//        this.clrNtrl = this.getBackground();
    }
    
    /**
     * Builds the GUI of the component.
     *
     * 
     * @since  Jan 22, 2010
     * @author Christopher K. Allen
     */
    private void buildErrorPanel() {
        this.pnlIndicate = new JPanel();
        this.pnlIndicate.setPreferredSize(DIM_ERR_PNL);
        this.pnlIndicate.setBorder( new BevelBorder(BevelBorder.RAISED) ); 
        this.pnlIndicate.setBackground(this.clrUnset);

//        this.clrNtrl = this.pnlIndicate.getBackground();
    }
    
    /**
     * Build the visible GUI panel.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void buildGui() {
        Box         boxDisplay = Box.createHorizontalBox();
        
        boxDisplay.add(this.pnlIndicate);
        boxDisplay.add( Box.createHorizontalStrut(10) );
        boxDisplay.add(this.lblParam);
        
        this.add(boxDisplay);
    }
}


