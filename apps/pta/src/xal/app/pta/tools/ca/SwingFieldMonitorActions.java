/**
 * CmnMonitorActions.java
 *
 * @author Christopher K. Allen
 * @since  Oct 17, 2011
 *
 */

/**
 * CmnMonitorActions.java
 *
 * @author  Christopher K. Allen
 * @since	Oct 17, 2011
 */
package xal.app.pta.tools.ca;

import xal.app.pta.tools.swing.BndNumberTextField;
import xal.app.pta.tools.swing.BooleanIndicatorPanel;
import xal.app.pta.tools.swing.MultiStateSelectorPanel;
import xal.app.pta.tools.swing.NumberTextField;
import xal.ca.ChannelRecord;

/**
 * This class is a collection of enclosed classes which are all common actions
 * (type <code>SmfPvMonitor.IAction</code> for Swing components in the tools
 * package.  Mostly we have actions that update the displayed value of particular
 * Swing GUI component whenever the value of the PV changes.  
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Oct 17, 2011
 */
public class SwingFieldMonitorActions {
    
    
    /*
     * Internal Classes
     */
    
    /**
     * A PV monitor action which updates error indicator panels of
     * type <code>BooleanInicatorPanel.
     *
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     * 
     * @see BooleanIndicatorPanel
     */
    public static class BooleanFieldUpdateAction implements SmfPvMonitor.IAction {

        
        /*
         * Local Attributes
         */
        
        /** The error panel that we update */
        private final BooleanIndicatorPanel       pnlBool;
        
        /**
         * Create a new <code>ErrorFieldMonitor</code> object.
         *
         * @param pnlError      error indicator panel that we are updating
         *
         * @since     Jan 25, 2010
         * @author    Christopher K. Allen
         */
        public BooleanFieldUpdateAction(BooleanIndicatorPanel    pnlError) {
            this.pnlBool = pnlError;
        }

        /**
         * Responds to the change of the monitored PV value.  The boolean display
         * is updated with the new value of the PV.
         *
         * @since   Jan 25, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            Integer     intVal = val.intValue();
            
            this.pnlBool.setDisplayValue(intVal);
        }
    }
    
    
    
    /**
     * A PV monitor action which updates numeric display panels
     * of type <code>NumberTextField</code>.
     *
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     * 
     * @see NumberTextField
     * @see BndNumberTextField
     */
    public static class NumberFieldUpdateAction implements SmfPvMonitor.IAction {

        
        /*
         * Local Attributes
         */
        
        /** The error panel that we update */
        private final NumberTextField       txtNumber;
        
        /**
         * Create a new <code>ErrorFieldMonitor</code> object.
         *
         * @param txtNumber      error indicator panel that we are updating
         *
         * @since     Jan 25, 2010
         * @author    Christopher K. Allen
         */
        public NumberFieldUpdateAction(NumberTextField  txtNumber) {
            this.txtNumber = txtNumber;
        }

        /**
         * Responds to the change in the monitored PV value.  The number field display
         * is updated with the new value of the PV.
         *
         * @since   Jan 25, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            Integer     intVal = val.intValue();
            
            this.txtNumber.setDisplayValueSilently(intVal);
        }
    }
    

    /**
     * PV monitor action which updates multiple-state selectors.
     *
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     * 
     * @see MultiStateSelectorPanel
     */
    public static class MultiStateSelectorUpdateAction implements SmfPvMonitor.IAction {

        
        /*
         * Local Attributes
         */
        
        /** The panel which we are updating */
        private final MultiStateSelectorPanel   pnlMulSel;
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>MultiStateSelectorAction</code> object.
         *
         * @param pnlMulSel     the panel we are updating
         *
         * @since     Jan 25, 2010
         * @author    Christopher K. Allen
         */
        public MultiStateSelectorUpdateAction(MultiStateSelectorPanel pnlMulSel) {
            this.pnlMulSel = pnlMulSel;
        }
        
        /**
         * Responds to the change in the monitored PV value by updating the selector
         * panel display to the new PV value.
         *
         * @since   Jan 25, 2010
         * @author  Christopher K. Allen
         *
         * @see xal.app.pta.tools.ca.SmfPvMonitor.IAction#valueChanged(xal.ca.ChannelRecord, xal.app.pta.tools.ca.SmfPvMonitor)
         */
        @Override
        public void valueChanged(ChannelRecord val, SmfPvMonitor mon) {
            Integer     intVal = val.intValue();
            
            this.pnlMulSel.setDisplayState(intVal);
        }
        
    }
    
    
    
}
