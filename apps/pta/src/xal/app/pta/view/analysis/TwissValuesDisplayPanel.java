/**
 * TwissValuesDisplayPanel.java
 *
 * Author  : Christopher K. Allen
 * Since   : Oct 1, 2014
 */
package xal.app.pta.view.analysis;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
    private static final NumberTextField.FMT        FMT_DISPLAY = FMT.ENGR_3;
    
    /** String length of each label */
    private static final int        INT_LABEL_LEN = 11;
    
    /** (Default) Number of columns in the text field displays */
    private static final int        INT_TEXT_LEN = 9;
    
    
    /*
     * Local Attributes
     */
    
    /** Map of signal property to text field display component */
    private final Map<Twiss.PROP, NumberTextField>   mapPrpToDspl;
    

    /*
     * Initialize
     */
    
    /**
     * Creates a new <code>TwissValuesDispayPanel</code> with empty 
     * text fields.
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
     * Enables or disables the edit capabilities of the text fields
     * used to display the Courant-Snyder parameters.
     * 
     * @param bolEdit   enables text box editing if <code>true</code>,
     *                  disables if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public void setEditable(boolean bolEdit) {
        
        if (bolEdit) 
            for (Twiss.PROP prop : Twiss.PROP.values()) {
                NumberTextField     txtProp = this.mapPrpToDspl.get(prop);
                
                txtProp.setEditable(true);
                txtProp.setBackground(Color.WHITE);
            }
        else
            for (Twiss.PROP prop : Twiss.PROP.values()) {
                NumberTextField     txtProp = this.mapPrpToDspl.get(prop);
                
                txtProp.setEditable(false);
                txtProp.setBackground(Color.GRAY);
            }
    }
    
    
    /*
     * Operations
     */
    
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
     * Retrieves the currently displayed Courant-Snyder parameters from the text
     * fields, creates a new <code>Twiss</code> object from them and returns it.
     *   
     * @return  a Courant-Snyder parameter set corresponding to the currently displayed values
     *
     * @author Christopher K. Allen
     * @since  Oct 3, 2014
     */
    public Twiss    getDisplayValues() {
        
        double  dblAlpha = this.mapPrpToDspl.get(Twiss.PROP.ALPHA).getDisplayValue().doubleValue();
        double  dblBeta  = this.mapPrpToDspl.get(Twiss.PROP.BETA).getDisplayValue().doubleValue();
        double  dblEmit  = this.mapPrpToDspl.get(Twiss.PROP.EMIT).getDisplayValue().doubleValue();
        
        Twiss   twsVals = new Twiss(dblAlpha, dblBeta, dblEmit);

        return twsVals;
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
            txtPrpDspl.setBkgndEditColor(Color.GRAY);
            
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
//        BoxLayout       mgrLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
//        this.setLayout(mgrLayout);
        this.setLayout( new GridBagLayout() );
        
        GridBagConstraints       gbcLayout = new GridBagConstraints();

        gbcLayout.insets = new Insets(0,0,5,5);
        
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth  = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.fill    = GridBagConstraints.NONE;
        gbcLayout.weightx = 0.0;
        gbcLayout.weighty = 0.0;
        gbcLayout.anchor = GridBagConstraints.LINE_END;
        
        for (Twiss.PROP prop : Twiss.PROP.values()) {
            NumberTextField txtPrpDspl = this.mapPrpToDspl.get(prop);

            String     strPrpName = " " + prop.getPropertyLabel();
            String     strPrpLbl  = this.pad( strPrpName );
            JLabel     lblPrpNm   = new JLabel( strPrpLbl );
            

//            Box  boxPrpDspl = Box.createHorizontalBox();
//            boxPrpDspl.add( lblPrpNm );
//            boxPrpDspl.add( Box.createHorizontalStrut(5) );
//            boxPrpDspl.add( txtPrpDspl );
//            this.add(boxPrpDspl);

            gbcLayout.gridx  = 0;
            gbcLayout.fill   = GridBagConstraints.NONE;
            gbcLayout.anchor = GridBagConstraints.LINE_END; 
            this.add( lblPrpNm, gbcLayout );
            
            gbcLayout.gridx   = 1;
            gbcLayout.fill    = GridBagConstraints.HORIZONTAL;
            gbcLayout.anchor  = GridBagConstraints.LINE_START;
            this.add( txtPrpDspl, gbcLayout );
            
            gbcLayout.gridy++;
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