/**
 * TraceIndexSelector.java
 *
 *  Created	: Jun 8, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.sel;

import xal.app.pta.tools.swing.BndNumberTextField;
import xal.app.pta.tools.swing.NumberTextField.FMT;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Allows user to select the current trace index (scan step index)
 * using a slider component and/or a text field.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 8, 2010
 * @author Christopher K. Allen
 */
public class TraceIndexSelector extends JPanel {

    
    /*
     * Inner Classes
     */
    
    /**
     * Responds to changes in the slider by the user.  The
     * text field is updated to the new value.
     *
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    private class SliderSynchronizer implements ChangeListener {

        /**
         * This method is basically a delegate to 
         * the <code>{@link ProcessWindowSelectorPanel#setTraceIndex(int)}</code>
         * method of the outer class.  We do screen the method invocation
         * for final values of the slider, ignoring all other change
         * events.
         *   
         * @since       Jun 7, 2010
         * @author  Christopher K. Allen
         *
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            
            // Only keep final values
            if (sldrIndex.getValueIsAdjusting())
                return;
            
            setDisplayValue( sldrIndex.getValue() );
        }
    }
    
    /**
     * Action response for the bounded text field displaying the 
     * trace index.  This action is invoked if the user changes 
     * the value, that is, changes the scan step index.  We respond
     * by setting the slider to the new value.
     *
     * @since  Jun 7, 2010
     * @author Christopher K. Allen
     */
    private class TextSynchronizer implements ActionListener {

        /**
         * This method is basically a delegate to 
         * the <code>{@link ProcessWindowSelectorPanel#setTraceIndex(int)}</code>
         * method of the outer class.  We do screen the method invocation
         *
         * @since       Jun 7, 2010
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Integer     intIndex = txtIndex.getDisplayValue().intValue() ;
            
            setDisplayValue( intIndex );
        }
        
    }
    
    /*
     * Global Constants
     */

    /**  long */
    private static final long serialVersionUID = 1L;

    
    /** The trace index label in the GUI */
    private static final JLabel         LBL_TRC_IND = new JLabel("Scan step index");
    
    /**  Slider minor tick count between major ticks */
    private static final int            CNT_MNR_TICKS = 5;

    /**  Slider major tick count */
    private static final int            CNT_MJR_TICKS = 10;
    
//    /** Tick label increment value */
//    private static final int            INT_LBL_INCR = 5;
//
    

    /*
     * Local Attributes
     */
    
    /** The actual slider object */
    private final JSlider               sldrIndex;
    
    /** Text display of the index */
    private final BndNumberTextField    txtIndex;
    
    /** Synchronizes other GUI components to the value of the slider */
    private final SliderSynchronizer    synSldr;
    
    /** Synchronizes other GUI components to the value of the text field */
    private final TextSynchronizer      synText;
    
    /** The collection of listeners interested in value changes for text field */
    private final List<ActionListener>  lstActions;

    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>TraceIndexSlider</code> object.
     *
     *
     * @since     Jun 8, 2010
     * @author    Christopher K. Allen
     */
    public TraceIndexSelector() {
        // Initialize the list of event listeners
        this.lstActions  = new LinkedList<ActionListener>();
        
        // Slider display/selector for the trace index
        this.sldrIndex = new JSlider(JSlider.HORIZONTAL);
        this.sldrIndex.setSnapToTicks(true);
        this.sldrIndex.setPaintLabels(true);
        this.sldrIndex.setPaintTicks(true);
        this.sldrIndex.setMinimum(0);
        this.sldrIndex.setMaximum(0);

        // Text field display for the trace index
        this.txtIndex = new BndNumberTextField(FMT.INT);

        // Create and set the synchronizers
        this.synSldr     = new SliderSynchronizer();
        this.synText     = new TextSynchronizer();
        this.sldrIndex.addChangeListener( this.synSldr );
        this.txtIndex.addActionListener( this.synText );
        
        // Layout the GUI
        this.layoutGuiComponents();
    }
    
    /**
     * Empty the event response objects upon class death.
     *
     * @since 	Jun 8, 2010
     * @author  Christopher K. Allen
     *
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        this.lstActions.clear();
    }
    
    /**
     * <p>
     * Sets whether or not this component is enabled. 
     * A component that is enabled may respond to user input, 
     * while a component that is not enabled cannot respond to user input. 
     * Some components may alter their visual representation 
     * when they are disabled in order to provide feedback to 
     * the user that they cannot take input.
     * <br/>
     * <br/>&middot; Disabling a component does not disable its children.
     * <br/>&middot; Disabling a lightweight component does not prevent it from receiving MouseEvents.
     * </p>
     *
     * @since   Jun 8, 2010
     * @author  Christopher K. Allen
     *
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean bolEnable) {
        this.sldrIndex.setEnabled(bolEnable);
        this.txtIndex.setEnabled(bolEnable);
    }


    /**
     * Add an event response that occurs whenever the
     * value of the index is changed. 
     *
     * @param actIndexChanged
     * 
     * @since  Jun 8, 2010
     * @author Christopher K. Allen
     */
    public void addActionListener(ActionListener actIndexChanged) {

        this.lstActions.add(actIndexChanged);
    }
    
    /**
     * Removes the given action from the list of objects interested
     * in receiving event notifications.
     *
     * @param actIndexChanged the event action to be removed
     * 
     * @return  <code>true</code> if the given action was found and removed from the list,
     *          <code>false</code> otherwise
     * 
     * @since  Jun 8, 2010
     * @author Christopher K. Allen
     */
    public boolean removeActionListener(ActionListener actIndexChanged) {
        boolean         bolResult = this.lstActions.remove(actIndexChanged);
        
        return bolResult;
    }
    
    /**
     * Set the range of values offered by the slider.
     *
     * @param intMin    minimum value of slider
     * @param intMax    maximum value of slider
     * 
     * @since  Jun 8, 2010
     * @author Christopher K. Allen
     */
    public void setRange(int intMin, int intMax) {
        
        // Set up the text field
        this.txtIndex.removeActionListener(this.synText);
        this.txtIndex.setMinValue(intMin);
        this.txtIndex.setMaxValue(intMax);
        this.txtIndex.addActionListener(this.synText);

        
        // Set up the slider
        int     cntVals  = intMax - intMin;
        int     cntMjSpc = cntVals/CNT_MJR_TICKS;
        int     cntMnSpc = cntMjSpc/CNT_MNR_TICKS;
        
            // Number of major ticks is greater than the number of values
        if (cntMjSpc == 0) {  
            cntMnSpc = 1;
            cntMjSpc = cntVals;
            
            // Number of minor ticks is greater than the number of values between majors
        } else if (cntMnSpc == 0){
            cntMnSpc = 1;
            
        } else {};
        
        this.sldrIndex.removeChangeListener(this.synSldr);
        this.sldrIndex.setMinimum(intMin);
        this.sldrIndex.setMaximum(intMax);
        this.sldrIndex.setMinorTickSpacing(cntMnSpc);
        this.sldrIndex.setMajorTickSpacing(cntMjSpc);
        
        this.sldrIndex.setLabelTable( this.sldrIndex.createStandardLabels(cntMjSpc, intMin) );
        this.sldrIndex.addChangeListener(this.synSldr);
    }
    
    
    /**
     * Set the trace index without notifying any of the 
     * user listeners. 
     * 
     * @param intIndex   index (i.e., the step number) of the desired trace
     * 
     * @since  May 25, 2010
     * @author Christopher K. Allen
     */
    public synchronized void setDisplayValueSilently(int intIndex) {

        // Prevent extraneous event broadcasts
        this.txtIndex.removeActionListener(this.synText);
        this.sldrIndex.removeChangeListener(this.synSldr);
        
        // Set the index value on the GUI components
        this.txtIndex.setDisplayValueSilently(intIndex);
        this.sldrIndex.setValue(intIndex);

        // Re-establish synchronization between GUI components
        this.txtIndex.addActionListener(this.synText);
        this.sldrIndex.addChangeListener(this.synSldr);
    }
    
    /**
     * Set the trace index on the GUI display and broadcast to
     * the event listeners that a new value exists.
     *
     * @param intIndex  the new trace index
     * 
     * @since  Jun 8, 2010
     * @author Christopher K. Allen
     */
    public synchronized void setDisplayValue(int intIndex) {
        
        // Change the values on the GUI display
        this.setDisplayValueSilently(intIndex);
        
        // Broadcast that a set index event has occurred
        ActionEvent     evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "setIndex");
        
        for (ActionListener action : this.lstActions) 
            action.actionPerformed(evt);
    }
    
    
    
    /*
     * Operations
     */
    
    
    /**
     * Returns the value of the trace index as is currently
     * shown on the GUI display. 
     *
     * @return  current value of the trace index
     * 
     * @since  Jun 8, 2010
     * @author Christopher K. Allen
     */
    public int getDisplayValue() {
        Integer intIndex = this.txtIndex.getDisplayValue().intValue();
        
        return intIndex;
    }
    
    /**
     * Clears the GUI display of selected trace index.
     *
     * 
     * @since  May 27, 2010
     * @author Christopher K. Allen
     */
    public void clearDisplay() {
        
        this.sldrIndex.setValue(0);
        this.txtIndex.clearDisplay();
    }
    
    
    
    /*
     * Support Methods
     */
    
    /**
     *  Arrange the GUI components on this panel.
     * 
     * @since  May 21, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        this.setLayout( new GridBagLayout() );

        GridBagConstraints      gbcLayout = new GridBagConstraints();
        gbcLayout.insets  = new Insets(0, 5, 0, 5);


        gbcLayout.gridx = 0;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
//        gbcLayout.anchor = GridBagConstraints.LINE_START;
        gbcLayout.fill  = GridBagConstraints.NONE;
        this.add( LBL_TRC_IND, gbcLayout);
        
        gbcLayout.gridx = 1;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
//        gbcLayout.anchor = GridBagConstraints.CENTER;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add( this.txtIndex, gbcLayout);

        gbcLayout.gridx = 2;
        gbcLayout.gridy = 0;
        gbcLayout.gridwidth = 1;
        gbcLayout.gridheight = 1;
        gbcLayout.insets.right = 5;
        gbcLayout.weightx = 0.1;
        gbcLayout.weighty = 0.1;
//        gbcLayout.anchor = GridBagConstraints.LINE_END;
        gbcLayout.fill  = GridBagConstraints.HORIZONTAL;
        this.add( this.sldrIndex, gbcLayout);
    }
}
