/**
 * PropertyPanel.java
 *
 * @author Christopher K. Allen
 * @since  Jan 12, 2011
 *
 */

/**
 * PropertyPanel.java
 *
 * @author  Christopher K. Allen
 * @since	Jan 12, 2011
 */
package xal.app.pta.tools.property;

import xal.app.pta.rscmgt.AppProperties;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 * Displays sets of (application) properties and allows
 * for modification.  The property set is displayed vertically
 * on the GUI.
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @author Christopher K. Allen
 * @since   Jan 12, 2011
 */
public class PropertyPanel extends JPanel {

    
    
    /**
     * Global Constants
     */
    
    /** Serialization version */
    private static final long serialVersionUID = 1L;

    
    /**  Number of columns in the input text fields */
    protected static final int CNT_COLS = 20;

    /**  Size of the horizontal struts separating text fields from labels */
    protected static final int INT_PAD_HOR = 7;

    /**  Size of the vertical struts separating the GUI components */
    protected static final int INT_PAD_VER = 10;


    /**
     * Responds to edits of a <code>JTextField</code> object
     * bound to a <code>Property</code> object.  The property
     * value is updated to the new value displayed in the
     * text field.
     *
     * @author Christopher K. Allen
     * @since   Jan 13, 2011
     */
    private class UpdateAction implements ActionListener {

        
        /*
         * Local Attributes
         */
        
        /** The user input containing the property value */
        private final JTextField        txt;
        
        /** The property we are managing */
        private final Property          prp;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new input event handler for the given
         * text field.
         * 
         * @param txt  text field supplying new property values
         * @param prp  property to bind to text field
         *
         * @author  Christopher K. Allen
         * @since   Jan 13, 2011
         */
        public UpdateAction(JTextField txt, Property prp) {
            this.txt = txt;
            this.prp = prp;
        }




        /**
         * Set the value of the property to that in the text box.
         * 
         * @since Jan 13, 2011
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent arg0) {
            String  strVal = this.txt.getText();
            
            this.prp.set(strVal);
        }
        
    }
    
    
    /*
     * Local Attributes
     */
    
    
    /*
     * Data
     */
    
    /** Title of the panel */
    private final String                    strTitle;
    
    /** Parameter set data structure used to pass parameter values */
    private final List<Property>            lstProps;
    
    /** Map of the device parameters to manage */
    private final Map<Property, JTextField> mapFields;

    
    
    /*
     * GUI Components
     */

    /** The layout manager constraint object */
    private GridBagConstraints       gbcTxtFlds;

    /** Apply configuration parameters button */
    private JButton                 butApply;

    
    
    
    /*
     * Initialization
     */

    
    /**
     * Create a new <code>PropertyPanel</code> object with the
     * given title and displaying the given list of properties.
     * 
     * @param strTitle      title displayed on the panel
     * @param lstProps      list of properties whose values are displayed and modified.
     *
     * @author  Christopher K. Allen
     * @since   Jan 12, 2011
     */
    public PropertyPanel(String strTitle, List<Property> lstProps) {
        this.strTitle  = strTitle;
        this.lstProps  = lstProps;
        this.mapFields = new HashMap<Property, JTextField>();
        
        this.initLayout();
        this.initTextFields();
        this.initApplyButton();
        this.buildGuiPanel();
    }
    
    /**
    /**
     * Create a new <code>PropertyPanel</code> object with the
     * given title and displaying the given set of properties.
     * 
     * @param strTitle      title displayed on the panel
     * @param arrIProps     array interfaces to properties whose values are displayed and modified.
     *
     * @author  Christopher K. Allen
     * @since   Jan 13, 2011
     */
    public PropertyPanel(String strTitle, Property.IProperty[] arrIProps) {
        this(strTitle, Property.extractProperties(arrIProps));
    }
    
    
    /*
     * Operations
     */

    /**
     * Returns the title of the properties panel.
     * 
     * @return the title displayed on the GUI panel.
     */
    public String getTitle() {
        return this.strTitle;
    }


    /**
     * Updates all the properties in the panel to the values
     * currently displayed in the text fields bound to the properties.
     *
     * @author Christopher K. Allen
     * @since  Jan 13, 2011
     */
    public void updateProperties() {
        
        for (Map.Entry<Property, JTextField> pair : this.mapFields.entrySet()) {
            Property    prop = pair.getKey();
            JTextField  text = pair.getValue();
            
            String      strVal = text.getText();
            prop.set(strVal);
        }
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Initialize the GUI layout of this panel.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void        initLayout() {
        
        // Create the input field container
        this.setLayout( new GridBagLayout() );
        
        this.gbcTxtFlds = new GridBagConstraints();
        this.gbcTxtFlds.anchor  = GridBagConstraints.LINE_START;
        this.gbcTxtFlds.fill    = GridBagConstraints.HORIZONTAL;
        this.gbcTxtFlds.weightx = 0.5;
        this.gbcTxtFlds.gridx = 0;
        this.gbcTxtFlds.gridy = 0;
    }

    /**
     * Create the GUI input text fields, in order, for each configuration
     * parameter.
     *
     * 
     * @since  Jan 13, 2010
     * @author Christopher K. Allen
     */
    private void initTextFields() {
        
        int     cntCols = AppProperties.TEXTFLD.COLS.getValue().asInteger();
        
        for (Property prop : this.lstProps) {
    
            // Create the text field and set the display format
            String        strVal    = prop.asString();
            JTextField    txtProp   = new JTextField(strVal, cntCols);
            UpdateAction  actUpdate = new UpdateAction(txtProp, prop);
            
            txtProp.addActionListener(actUpdate);
            this.mapFields.put(prop, txtProp);
        }
    }
    
    /**
     * Create the <tt>apply/update</tt> button and its
     * resources.
     *
     * @author Christopher K. Allen
     * @since  Jan 13, 2011
     */
    private void initApplyButton() {
        
        // Set up the apply button
//        String          strUpdateIconPath = AppProperties.APPLY.ICON.getValue().asString();
//        String          strUpdateLbl      = AppProperties.APPLY.LABEL.getValue().asString();
//        String          strUpdateLblPad   = String.format(" %s", strUpdateLbl); 
//        ImageIcon       icnUpdate          = PtaResourceManager.getImageIcon(strUpdateIconPath);
        
        String          strUpdateLblPad = "  Apply";
        ImageIcon       icnUpdate       = AppProperties.ICON.APPLY.getValue().asIcon();
        
        this.butApply = new JButton( strUpdateLblPad, icnUpdate);
        
        // Sets the apply parameters event
        ActionListener actApply = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProperties();
            }
        };
        
        // Add action to force apply button
        this.butApply.addActionListener(actApply);
    }

    /**
     * Constructs the visible GUI panel.
     *
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    private void buildGuiPanel() {
        
        // Create a [label - input] entry for each field
        for (Map.Entry<Property, JTextField> entry : this.mapFields.entrySet()) {
            Property    prpPref = entry.getKey();
            JTextField  txtPref = entry.getValue();

            String      strLabel = prpPref.getName() + "    ";
            JLabel      lblPref = new JLabel(strLabel);
            Box         boxPref = Box.createHorizontalBox();
            
            boxPref.add(txtPref);
            boxPref.add(Box.createHorizontalStrut(INT_PAD_HOR));
            boxPref.add(lblPref);
            
            this.appendGuiComponent(boxPref);
        }
        
//        // Add the update button
//        this.appendApplyButton();

        // Add a border with title
        this.setBorder( new TitledBorder( this.getTitle() ) );
    }


    /**
     * Appends the given GUI component to the 
     * bottom of the panel.  Also adds a vertical
     * strut to enforce the padding specified in the
     * Device Configuration File.
     *
     * @param cmp       GUI component being appended
     * 
     * @since  Jan 28, 2010
     * @author Christopher K. Allen
     */
    private void appendGuiComponent(Component cmp) {
        this.add(cmp, this.gbcTxtFlds);
        this.gbcTxtFlds.gridy++;
        
        Component cmpStrut = Box.createVerticalStrut( INT_PAD_VER ); 
        this.add( cmpStrut, this.gbcTxtFlds);
        this.gbcTxtFlds.gridy++;
    }
    
    /**
     * Adds the global update button
     * to the bottom of the panel.
     *
     * 
     * @since  Jan 29, 2010
     * @author Christopher K. Allen
     */
    @SuppressWarnings("unused")
    private void appendApplyButton() {
        
        GridBagConstraints      gbc = (GridBagConstraints) this.gbcTxtFlds.clone();
        
        // Add the force apply button
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.weighty = 1.0;
        this.add(this.butApply, gbc);
        
        this.gbcTxtFlds.gridy++;
    }


}
