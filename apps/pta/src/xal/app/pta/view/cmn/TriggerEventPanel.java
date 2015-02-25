/**
 * TriggerEventPanel.java
 *
 *
 * @author Christopher K. Allen
 * @since  May 6, 2014
 */

package xal.app.pta.view.cmn;

import xal.app.pta.rscmgt.DeviceProperties;
import xal.app.pta.view.devcfg.TrgrConfigPanel;
import xal.smf.impl.profile.ProfileDevice;

import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * GUI component providing the selection of the 
 * supported timing triggering event configuration
 * parameter.
 *
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jan 20, 2010
 * @author Christopher K. Allen
 */
public class TriggerEventPanel extends JPanel {

    /*
     * Global Constants
     */

    /**  Serialization version */
    private static final long serialVersionUID = 1L;

    /*
     * Local Attributes
     */

    /** The GUI display */
    private final JComboBox<ProfileDevice.TRGEVT>   cbxTrigEvt;




    /**
     * Create a new <code>TriggerEventPanel</code> object.
     *
     *
     * @since     Jan 19, 2010
     * @author    Christopher K. Allen
     */
    public TriggerEventPanel() {
        String strLabel  = DeviceProperties.getLabel(TrgrConfigPanel.FD_TRG_EVT);

        JLabel lblTrgEvt = new JLabel(strLabel);

        this.cbxTrigEvt = new JComboBox<ProfileDevice.TRGEVT>();
        this.cbxTrigEvt.setEditable(false);

        for (ProfileDevice.TRGEVT evt : ProfileDevice.TRGEVT.values()) {
            this.cbxTrigEvt.addItem(evt);
        }

        this.add(this.cbxTrigEvt);
        this.add(lblTrgEvt);
    }

    /**
     * Registers an item selected event listener which should
     * respond by setting the appropriate triggering event
     * in the configuration parameters.
     *
     * @param lsnAction     item selected event action
     * 
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    public void     registerSelectionListener(ActionListener lsnAction) {
        this.cbxTrigEvt.addActionListener(lsnAction);
    }


    /**
     * Return the user-selected trigger event from the 
     * GUI's combo box.
     * 
     * @return      desired timing trigger event, 
     *              or <code>null</code> if there was an initialization error
     *              with the combo box 
     *
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    public ProfileDevice.TRGEVT        getTriggerEvent() {
        Object      objSel = this.cbxTrigEvt.getSelectedItem();

        if (objSel instanceof ProfileDevice.TRGEVT)
            return (ProfileDevice.TRGEVT)objSel;

        return null;
    }

    /**
     * Sets the timing trigger event seen in the selected
     * field of the combo box.  This method will not
     * fire an action event.
     *
     * @param evt   the desired triggering event
     * 
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    public void     setTriggerEventSilently(ProfileDevice.TRGEVT evt) {
        ActionListener[]    arrLsns = this.cbxTrigEvt.getActionListeners();
        for (ActionListener lsn : arrLsns)
            this.cbxTrigEvt.removeActionListener(lsn);

        this.cbxTrigEvt.setSelectedItem(evt);

        for (ActionListener lsn : arrLsns)
            this.cbxTrigEvt.addActionListener(lsn);
    }

    /**
     * Clears the selection of the combo box
     * (i.e., blank).
     *
     * 
     * @since  Jan 21, 2010
     * @author Christopher K. Allen
     */
    public void     clearTriggerEvent() {
        this.cbxTrigEvt.setSelectedIndex(-1);
    }

}



