/**
 * SingleSignalDisplay.java
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

import xal.app.pta.rscmgt.AppProperties;
import xal.app.pta.tools.analysis.SignalAnalyzer;
import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;

/**
 * Displays the numeric fields of a single <code>{@link SignalAnalyzer}</code> 
 * object interpreted as a data structure.
 *
 * @author Christopher K. Allen
 * @since  Dec 13, 2011
 * 
 * @see xal.app.pta.tools.analysis.SignalAnalyzer
 */
 public class SingleSignalDisplay extends JPanel {

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
    
    
    /** Color of the text display under alarm condition */
    private static final Color      CLR_BM_ALARM = Color.RED;

    
    
    
    /*
     * Local Attributes
     */
    
    /** Map of signal property to text field display component */
    private final Map<SignalAnalyzer.PROP, NumberTextField>   mapPrp2Dspl;
    
    

    /**
     * Create a new <code>SingleSignalDisplay</code> object.
     *
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public SingleSignalDisplay() {
        this.mapPrp2Dspl = new HashMap<SignalAnalyzer.PROP, NumberTextField>();
        
        this.guiBuildComponents();
        this.guiLayoutComponents();
    }
    
    /**
     * Displays the signal properties of the given <code>WireScanner.Signal</code> object
     * as computed by a <code>SignalAnalyzer</code>. 
     *
     * @param anlMsmt     signal analysis of raw measurement signal 
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public void     display(SignalAnalyzer anlMsmt) {
        
        // Compute signal properties
        for (SignalAnalyzer.PROP prop : SignalAnalyzer.PROP.values()) {
            Double          dblPrpVal  = prop.getPropertyValue( anlMsmt );
            NumberTextField txtPrpDspl = this.mapPrp2Dspl.get(prop);
            
            txtPrpDspl.setDisplayValueSilently(dblPrpVal);
        }
        
        // Display alarms on beam quantities
        double  dblErrTol = AppProperties.NUMERIC.TOL_BM_QUAN.getValue().asDouble();
        
        for (SignalAnalyzer.ALARM alarm : SignalAnalyzer.ALARM.values()) 
            if ( !alarm.checkTolerance(anlMsmt, dblErrTol) ) {
                SignalAnalyzer.PROP prpAlarm   = alarm.getBeamPropery();
                NumberTextField     txtPrpDspl = this.mapPrp2Dspl.get(prpAlarm);
                
                txtPrpDspl.setForeground(CLR_BM_ALARM);
            }
    }
    
    /**
     * Clears all signal property values leaving display fields blank.
     * 
     * @since  Dec 13, 2011
     * @author Christopher K. Allen
     */
    public void     clearDisplay() {
        for (SignalAnalyzer.PROP prop : SignalAnalyzer.PROP.values()) {
            JTextField      txtPrpDspl = this.mapPrp2Dspl.get(prop);
            
            txtPrpDspl.setText("");
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
        
        for (SignalAnalyzer.PROP prop : SignalAnalyzer.PROP.values()) {
            NumberTextField txtPrpDspl = new NumberTextField(FMT_DISPLAY, INT_TEXT_LEN);
            
            txtPrpDspl.setEditable(false);
            txtPrpDspl.setBkgndEditColor(Color.WHITE);
            
            this.mapPrp2Dspl.put(prop, txtPrpDspl);
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
        
        for (SignalAnalyzer.PROP prop : SignalAnalyzer.PROP.values()) {
            JTextField txtPrpDspl = this.mapPrp2Dspl.get(prop);

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
     * <code>{@link SingleSignalDisplay#INT_LABEL_LEN}</code>.
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