/**
 * MultiStateSelectorPanel.java
 *
 *  Created	: Jan 22, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.tools.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * <p>
 * Implements the user interface for selecting multiple 
 * choices of state as a group of radio buttons.
 * </p>
 * <p>
 * A non-null
 * error value causes the display foreground to change color
 * when the state occurs. 
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 23, 2009
 * @author Christopher K. Allen
 */
public class MultiStateSelectorPanel extends JPanel {
    
    /**
     * Response to a radio button push.
     *
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    class SelectStateAction implements ActionListener {

        /** The gain value */
        private final Integer           intValState;
        
//        /** The button instance */
//        private final JRadioButton      butState;
//        
//        
//        /**
//         * Create a new <code>SelectStateAction</code> object 
//         * with the given gain value.
//         *
//         * @param butState      the radio button with the given state 
//         * @param intValState   the (constant) state value for this action
//         *
//         * @since     Dec 22, 2009
//         * @author    Christopher K. Allen
//         */
//        public SelectStateAction(JRadioButton butState,Integer intValState) {
//            this.butState    = butState;
//            this.intValState = intValState;
//        }
        
        /**
         * Create a new <code>SelectStateAction</code> object 
         * with the given gain value.
         *
         * @param intValState   the (constant) state value for this action
         *
         * @since     Dec 22, 2009
         * @author    Christopher K. Allen
         */
        public SelectStateAction(Integer intValState) {
            this.intValState = intValState;
        }
        
        /**
         * Sets the gain.
         *
         * @since   Dec 22, 2009
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void actionPerformed(ActionEvent e) {
           
            setStateAction(this.intValState);
        }
    }

    
    /*
     * Global Constants
     */

    /**  Serialization version*/
    private static final long serialVersionUID = 1L;
    
    
//    /** Normal foreground color (after <code>setEditable()</code> call) */
//    private static final Color          CLR_NORML = Color.DARK_GRAY;
    
    /** Error state foreground color */
    private static final Color          CLR_ERROR = Color.RED;

    

    /*
     * Parameter Values
     */
    
    /** The desired data acquisition gain circuit */
    private Integer             intState;
    
    /** Optional error value */
    private final Integer       intValErr;     
    
    
    
    /*
     * GUI Components
     */
    
    /** The normal foreground color */
    private final Color                         clrFgNml;
    
    /** The gain button group */
    private final ButtonGroup                   grpStateButts;
    
    /** the map of state value to state button */
    private final Map<Integer, JRadioButton>    mapValToButt;
    
    
    /*
     * Registered action listeners 
     */
    
    /** List of action listeners (for button pressed events) */
    private final List<ActionListener>      lstLsnSelect;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new <code>GainSelectorPanel</code> object.  A non-null
     * error value causes the display foreground to change color
     * when the state occurs.
     *
     * @param arrLabels array of button labels (one for each button)
     * @param arrVals   array of state values (one for each button)
     * @param intErrVal optional value of an exception error state.   
     *
     * @since     Dec 22, 2009
     * @author    Christopher K. Allen
     */
    public MultiStateSelectorPanel(String[] arrLabels, Integer[] arrVals, Integer intErrVal) {
        this.intValErr = intErrVal;
        this.clrFgNml  = this.getForeground();
        
        this.lstLsnSelect  = new LinkedList<ActionListener>();
        this.mapValToButt  = new HashMap<Integer, JRadioButton>();
        this.grpStateButts = new ButtonGroup();
        
        
        this.buildComponent(arrLabels, arrVals);
    }
    
    /**
     * Create a new <code>MultiStateSelectorPanel</code> object.
     *
     * @param arrLabels array of button labels (one for each button)
     * @param arrVals   array of state values (one for each button)
     *
     * @since     Jan 26, 2010
     * @author    Christopher K. Allen
     */
    public MultiStateSelectorPanel(String[] arrLabels, Integer[] arrVals) {
        this(arrLabels, arrVals, null);
    }
    

    /**
     * Register the given object to receive button selected
     * events.
     *
     * @param lsnSelect     object to receive event notifications
     * 
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    public void registerSelectionListener(ActionListener lsnSelect) {
        this.lstLsnSelect.add(lsnSelect);
    }
    
    /**
     * Set the user selection feature on or off.
     *
     * @param bolEdit   <code>true</code> enables user interaction, 
     *                  <code>false</code> disables it
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    public void setEditable(boolean bolEdit) {
        Color   clrFgnd = this.getForeground();

        for (Map.Entry<Integer, JRadioButton> entry : this.mapValToButt.entrySet()) {
            JRadioButton        but = entry.getValue();
  
            but.setEnabled(bolEdit);
            but.setForeground(clrFgnd);
//            but.setForeground(CLR_NORML);
        }
    }
    
    /**
     * Sets the displayed state value from outside the GUI.
     *
     * @param intValState  value of new state
     * 
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    public void setDisplayState(Integer intValState) {

        
        for (Integer intKey : this.mapValToButt.keySet())
            if (intKey == intValState) {
                this.intState = intValState;
                
                JRadioButton    butSel = this.mapValToButt.get(intKey);
                this.grpStateButts.setSelected(butSel.getModel(), true);
                this.checkErrorDisplay(intValState);
                return;
            }
        
        this.grpStateButts.clearSelection();
    }
    
    /**
     * Return the current state selected by the user.
     *
     * @return      value of currently selected state
     * 
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    public Integer      getDisplayState() {
        return this.intState;
    }
    
    /**
     * Clears the display so that none of the 
     * states are selected.
     *
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    public void  clearDisplay() {
        this.grpStateButts.clearSelection();
        this.clearForeground();
    }
    
    /**
     * Selects the desired state value from inside the GUI.
     * Specifically, this method is called by the button
     * event handler.
     *
     * @param intValState       gain value [0,1,2]
     * 
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    private void    setStateAction(Integer   intValState) {
        this.intState = intValState;
        this.checkErrorDisplay(intValState);
        
        ActionEvent evtAction = new ActionEvent(this, intValState, "State Selected");
        for (ActionListener lsn : this.lstLsnSelect)
            lsn.actionPerformed(evtAction);
    }
    
    
    
    /*
     * Support
     */
    
    /**
     * Create the state radio buttons and put them
     * into the (value,button) map and the button group.
     *
     * @param arrLabels button labels
     * @param arrVals   value of each state
     * 
     * @since  Jan 22, 2010
     * @author Christopher K. Allen
     */
    private void buildComponent(String[] arrLabels, Integer[] arrVals) {
        
        int i=0; for (Integer intVal : arrVals) {
            String              strLabel  = arrLabels[i++]; 
            JRadioButton        butState  = new JRadioButton( strLabel );
            SelectStateAction   lsnSelect = new SelectStateAction(intVal);
            
            butState.addActionListener( lsnSelect );

            this.mapValToButt.put(intVal, butState);
            this.grpStateButts.add(butState);
            this.add(butState);
        }
    }
    
    
    /**
     * Checks the given value to see if it is the error
     * value.  If so, the radio button with the corresponding
     * state value is fetched and the foreground is set to the
     * error color.
     *
     * @param intValChk the state value to check
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void checkErrorDisplay(Integer intValChk) {
        this.clearForeground();
        
        if (this.intValErr!=null && intValChk==this.intValErr) {
            JRadioButton        butState = this.mapValToButt.get(intValChk);
            
            butState.setForeground(CLR_ERROR);
        }
    }
    
    /**
     * Returns the foreground color to normal
     * for each button.
     *
     * 
     * @since  Jan 26, 2010
     * @author Christopher K. Allen
     */
    private void clearForeground() {
        for (Map.Entry<Integer, JRadioButton> entry : this.mapValToButt.entrySet()) {
            JRadioButton        butState = entry.getValue();
            
            butState.setForeground(this.clrFgNml);
        }
    }
}

