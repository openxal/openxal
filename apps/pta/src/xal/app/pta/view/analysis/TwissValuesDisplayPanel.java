/**
 * TwissValuesDisplayPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2014
 */
package xal.app.pta.view.analysis;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.tools.beam.Twiss;

/**
 * Displays the numeric values of Courant-Snyder parameter for the 
 * 3 phase planes. 
 *
 * @author Christopher K. Allen
 * @since  Oct 1, 2014
 * 
 * @see xal.app.pta.tools.analysis.SignalAnalyzer
 */
 public class TwissValuesDisplayPanel extends JPanel {

    /*
     * Global Constants
     */
    
    /**  Serialization version */
    private static final long       serialVersionUID = 1L;

    
    /** The display format of the field values */
    private static final NumberTextField.FMT        FMT_DISPLAY = FMT.DEC_4;
    
    /** String length of each label */
    private static final int        INT_LABEL_LEN = 10;
    
    /** (Default) Number of columns in the text field displays */
    private static final int        INT_TEXT_LEN = 7;
    
    
    /*
     * Local Attributes
     */
    
    /** Map of signal property to text field display component */
    private final Map<Twiss.PROP, NumberTextField>   mapPrpToDspl;
    
    

    /**
     * Create a new <code>SingleSignalDisplay</code> object.
     *
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public TwissValuesDisplayPanel() {
        this.mapPrpToDspl = new HashMap<>();
        
        this.guiBuildComponents();
        this.guiLayoutComponents();
    }
    
    /**
     * Displays the Courant-Snyder parameters of the given <code>Twiss</code> object. 
     *
     * @param twsPlane     set of Courant-Snyder parameters for some phase plane 
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public void     display(Twiss twsPlane) {
        
        // Display Courant-Snyder parameters in GUI
        for (Twiss.PROP prop : Twiss.PROP.values()) {
            Double          dblPrpVal  = prop.getPropertyValue( twsPlane );
            NumberTextField txtPrpDspl = this.mapPrpToDspl.get(prop);
            
            txtPrpDspl.setDisplayValueSilently(dblPrpVal);
        }
    }
    
    /**
     * Clears all signal property values leaving display fields blank.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public void     clearDisplay() {
        for (Twiss.PROP prop : Twiss.PROP.values()) {
            NumberTextField      txtPrpDspl = this.mapPrpToDspl.get(prop);
            
            txtPrpDspl.clearDisplay();
        }
    }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Creates all the GUI components used in the display.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private void guiBuildComponents() {
        
        for (Twiss.PROP prop : Twiss.PROP.values()) {
            NumberTextField txtPrpDspl = new NumberTextField(FMT_DISPLAY, INT_TEXT_LEN);
            
            txtPrpDspl.setEditable(false);
            txtPrpDspl.setBkgndEditColor(Color.WHITE);
            
            this.mapPrpToDspl.put(prop, txtPrpDspl);
        }
    }
    
    /**
     * Lays out all the GUI components on the user interface.
     *  
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private void guiLayoutComponents() {
        BoxLayout       mgrLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(mgrLayout);
        
        for (Twiss.PROP prop : Twiss.PROP.values()) {
            JTextField txtPrpDspl = this.mapPrpToDspl.get(prop);

            String     strPrpName = prop.getPropertyLabel();
            String     strPrpLbl  = this.pad( strPrpName );
            JLabel     lblPrpNm   = new JLabel( strPrpLbl );

            Box  boxPrpDspl = Box.createHorizontalBox();
            boxPrpDspl.add( lblPrpNm );
            boxPrpDspl.add( Box.createHorizontalStrut(5) );
            boxPrpDspl.add( txtPrpDspl );
            this.add(boxPrpDspl);
        }
    }
    
    /**
     * Pads the given string with whitespace until it
     * has the length 
     * <code>{@link TwissValuesDisplayPanel#INT_LABEL_LEN}</code>.
     *
     * @param strLabel      string to be padded (it's a field label)
     * 
     * @return              same string padded out to the size <code>INT_LABEL_LEN</code>
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    private String pad(String strLabel) {
        int                 szLbl    = strLabel.length();
        StringBuffer        bufLabel = new StringBuffer(strLabel);
        
        for (int i = szLbl; i < INT_LABEL_LEN; i++)
            bufLabel.append(" ");
        
        return bufLabel.toString();
    }
}