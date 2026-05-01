/**
 * ProfileTraitsDisplayPanel.java
 *
 *  Created	: Apr 23, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.analysis;

import xal.app.pta.tools.swing.NumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;
import xal.smf.impl.WireScanner;
import xal.smf.impl.profile.ProfileDevice.ANGLE;
import xal.smf.impl.profile.SignalAttrSet;
import xal.smf.impl.profile.SignalAttrs;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Apr 23, 2010
 * @author Christopher K. Allen
 */
public class ProfileAttrsDisplayPanel extends JPanel {

    
    /*
     * Internal Classes
     */
    
    /**
     * Enumeration of the available data plot layouts.
     *
     * @since  Jul 16, 2009
     * @author Christopher K. Allen
     */
    public enum LAYOUT {
        
        /** layout the data plots horizontally */
        HOR(BoxLayout.X_AXIS, GridBagConstraints.HORIZONTAL),
        
        /** layout the data plots vertically */
        VER(BoxLayout.Y_AXIS, GridBagConstraints.VERTICAL);
        
        
        /**
         * Returns the <code>BoxLayout</code> layout direction 
         * constant value for the data plot.
         *
         * @return      the <code>BoxLayout</code> layout constant 
         * 
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public int getBoxLayoutValue() {
            return this.intBoxLoutVal;
        }
        
        /**
         * Returns the <code>BoxLayout</code> layout direction 
         * constant value for the data plot.
         *
         * @return      the <code>BoxLayout</code> layout constant 
         * 
         * @since  Jul 16, 2009
         * @author Christopher K. Allen
         */
        public int getGridBagValue() {
            return this.intBoxLoutVal;
        }
        
        // Private Stuff
        
        /** the <code>BoxLayout</code> layout constant */
        private final int       intBoxLoutVal;
        
        /**
         * Creates the <code>LAYOUT</code> enumeration constants
         * with their given layout values.
         * 
         * @param intBoxLoutVal     position when using a box layout
         * @param intGridBagVal     grid position when using a grid bag layout
         *
         * @author  Christopher K. Allen
         * @since   Oct 3, 2011
         */
        private LAYOUT(int intBoxLoutVal, int intGridBagVal) {
            this.intBoxLoutVal = intBoxLoutVal;
//            this.intGridBagVal = intGridBagVal;
        }
    }
    
    
    /**
     * Displays the numeric fields of a 
     * <code>{@link WireScanner.SignalAttrs}</code>
     * data structure.
     *
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     * 
     * @see xal.smf.impl.WireScanner.SignalAttrs
     */
    static class SingleProfileDisplay extends JPanel {

        /*
         * Global Constants
         */
        
        /**  Serialization version */
        private static final long       serialVersionUID = 1L;
        
        /** String length of each label */
        private static final int        INT_LABEL_LEN = 10;
        
        /** (Default) Number of columns in the text field displays */
        private static final int        INT_TEXT_LEN = 7;
        
        /** The display format of the field values */
        private static final NumberTextField.FMT        FMT_DISPLAY = FMT.DEC_4;
        
        
        
        /*
         * Local Attributes
         */
        
        /** Map of field name to text field display component */
        private final Map<SignalAttrs.ATTRS, NumberTextField>   mapPrp2Dspl;


        /**
         * Create a new <code>SingleProfileDisplay</code> object.
         *
         *
         * @since     Apr 22, 2010
         * @author    Christopher K. Allen
         */
        public SingleProfileDisplay() {
            this.mapPrp2Dspl = new HashMap<SignalAttrs.ATTRS, NumberTextField>();
            
            this.guiBuildComponents();
            this.guiLayout();
        }
        
        /**
         * Displays the property values of the given
         * <code>WireScanner$SignalAttrs</code> object. 
         *
         * @param prpSignal     signal property set
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        public void     display(SignalAttrs prpSignal) {
            for (SignalAttrs.ATTRS prop : SignalAttrs.ATTRS.values()) {
                Double          dblPrpVal  = prop.getFieldValue(prpSignal);
                NumberTextField txtPrpDspl = this.mapPrp2Dspl.get(prop);
                
                txtPrpDspl.setDisplayValueSilently(dblPrpVal);
            }
        }
        
        /**
         * Clears out all signal property values leaving
         * display fields blank.
         *
         * 
         * @since  Apr 23, 2010
         * @author Christopher K. Allen
         */
        public void     clearDisplay() {
            for (SignalAttrs.ATTRS prop : SignalAttrs.ATTRS.values()) {
                JTextField      txtPrpDspl = this.mapPrp2Dspl.get(prop);
                
                txtPrpDspl.setText("");
            }
        }
        
        
        /*
         * Support Methods
         */
        
        
        
        /**
         * Creates all the GUI components and lays out
         * the display.
         *
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        private void guiBuildComponents() {
            
            for (SignalAttrs.ATTRS prop : SignalAttrs.ATTRS.values()) {
                NumberTextField txtPrpDspl = new NumberTextField(FMT_DISPLAY, INT_TEXT_LEN);
                
                txtPrpDspl.setEditable(false);
//                txtPrpDspl.setBkgndEditColor(Color.LIGHT_GRAY);
                txtPrpDspl.setBkgndEditColor(Color.WHITE);
                
                this.mapPrp2Dspl.put(prop, txtPrpDspl);
            }
        }
        
        /**
         * Creates all the GUI components and lays out
         * the display.
         *
         * 
         * @since  Apr 22, 2010
         * @author Christopher K. Allen
         */
        private void guiLayout() {
            BoxLayout       mgrLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
            this.setLayout(mgrLayout);
            
            for (SignalAttrs.ATTRS prop : SignalAttrs.ATTRS.values()) {
                JTextField txtPrpDspl = this.mapPrp2Dspl.get(prop);

                String     strPrpName = prop.getFieldName();
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
         * <code>{@link SingleProfileDisplay#INT_LABEL_LEN}</code>.
         *
         * @param strLabel      string to be padded (it's a field label)
         * 
         * @return              same string padded out to the size <code>INT_LABEL_LEN</code>
         * 
         * @since  Apr 26, 2010
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

    
    /*
     * Global Constants
     */
    
    /**  Serialization Version */
    private static final long serialVersionUID = 1L;


    
    /*
     * Local Attributes
     */
    
    /** The display layout */
    private final LAYOUT                                           layout;
    
    /** Label of each signal properties data set */
    private final Map<WireScanner.ANGLE, JLabel>                   mapPrj2Lbl;                           
    
    /** The individual signal properties for each projection plane */
    private final Map<WireScanner.ANGLE, SingleProfileDisplay>     mapPrj2Dspl;

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>ProfileTraitsDisplayPanel</code> object.
     *
     * @param layout        directs how the display will be layed out
     *
     * @since     Apr 23, 2010
     * @author    Christopher K. Allen
     */
    public ProfileAttrsDisplayPanel(LAYOUT layout) {
        this.layout  = layout;
        this.mapPrj2Lbl  = new HashMap<WireScanner.ANGLE, JLabel>();
        this.mapPrj2Dspl = new HashMap<WireScanner.ANGLE, SingleProfileDisplay>();
        
        this.buildGuiComponents();
        this.layoutGuiComponents();
        this.setDisplayDatasetTitle(false);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Toggle the display of projection plane titles for 
     * each data set.  
     *
     * @param bolDsplTtls     the titles are drawn if <code>true</code>,
     *                        otherwise no titles are drawn (the default)     
     * 
     * @since  May 27, 2010
     * @author Christopher K. Allen
     */
    public void setDisplayDatasetTitle(boolean bolDsplTtls) {
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
            JLabel      lblData = this.mapPrj2Lbl.get(angle);
            
            lblData.setVisible(bolDsplTtls);
        }
    }
    
    /**
     * Displays the signal properties of the various projections 
     * contained in the given data structure.
     *
     * @param setSigPrps        data structure of signal properties  
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    public void display(SignalAttrSet setSigPrps) {
        
        for (ANGLE angle : ANGLE.values()) {
            SingleProfileDisplay        pnlPrpDspl = this.mapPrj2Dspl.get(angle);
            SignalAttrs                 datSigPrps = angle.getSignalAttrs(setSigPrps);
            
            pnlPrpDspl.display(datSigPrps);
        }
    }
    
    /**
     * Clears out all data in all display panels.
     *
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    public void clearDisplay() {
        
        for (ANGLE angle : ANGLE.values()) {
            SingleProfileDisplay     pnlPrpDspl = this.mapPrj2Dspl.get(angle);
            
            pnlPrpDspl.clearDisplay();
        }
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Builds all the GUI components of the panel.  Then
     *
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {

        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
            JLabel                   lblData    = new JLabel( angle.getLabel() );
            SingleProfileDisplay     pnlPrjDspl = new SingleProfileDisplay();

            this.mapPrj2Lbl.put(angle, lblData);
            this.mapPrj2Dspl.put(angle, pnlPrjDspl);
        }
    }
    
    /**
     * Lays out the GUI components over the
     * visible panel.
     *
     * 
     * @since  Apr 23, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);

        
        // Add the data set titles
        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = this.layout.getGridBagValue();
        
        
        // Add the statistics for each plane
        for (WireScanner.ANGLE angle : WireScanner.ANGLE.values()) {
            JLabel                      lblTitle   = this.mapPrj2Lbl.get(angle);
            SingleProfileDisplay        pnlPrjDspl = this.mapPrj2Dspl.get(angle);

            Box                         box = Box.createVerticalBox();
            box.add(lblTitle);
            box.add(pnlPrjDspl);
            
            this.add(box, gbcLayout);
            
            if (this.layout == LAYOUT.HOR) {
                gbcLayout.gridx++;

            } else if (this.layout == LAYOUT.VER) {
                gbcLayout.gridy++;

            } else {};

        }
    }
    
}

//BoxLayout       mgrLayout = new BoxLayout(this, this.layout.getLayoutConstant());
//this.setLayout(mgrLayout);
//
//this.add( new DataSetTitles(this.layout) );
//
//for (WireScanner.Data.ANGLE angle : WireScanner.Data.ANGLE.values()) {
//  SingleProfileDisplay     pnlPrjDspl = this.mapPrj2Dspl.get(angle);
//  
//  this.add(pnlPrjDspl);
//
//  if (this.layout == LAYOUT.HOR) {
//      this.add( Box.createHorizontalStrut(7) );
//      this.add( Box.createGlue() );
//      
//  } else if (this.layout == LAYOUT.VER) {
//      this.add( Box.createVerticalStrut(7) );
//      this.add( Box.createGlue() );
//      
//  } else {};
//      
//}


