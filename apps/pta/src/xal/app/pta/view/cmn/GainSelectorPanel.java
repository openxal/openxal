
/**
 * Class GainSelectorPanel
 *
 * @author Christopher K. Allen
 * @since  May 6, 2014
 */
package xal.app.pta.view.cmn;

import xal.smf.impl.profile.ProfileDevice;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * Implements the user interface for selecting the DAQ
 * gain circuit as a group of radio buttons.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Dec 23, 2009
 * @author Christopher K. Allen
 */
public class GainSelectorPanel extends JPanel {

    /**
     * Response to a radio button push.
     *
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    class SelectGainAction implements ActionListener {

        /** The gain value */
        private final int           intGainNew;

        /**
         * Create a new <code>SelectGainAction</code> object 
         * with the given gain value.
         *
         * @param intGain   the (constant) gain value for this action
         *
         * @since     Dec 22, 2009
         * @author    Christopher K. Allen
         */
        public SelectGainAction(int intGain) {
            this.intGainNew = intGain;
        }

        /**
         * Sets the gain.
         *
         * @since   Dec 22, 2009
         * @author  Christopher K. Allen
         *
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            GainSelectorPanel.this.chooseDesiredGain(this.intGainNew);
        }
    }


    /*
     * Global Constants
     */
    /**  Serialization version*/
    private static final long serialVersionUID = 1L;


    /*
     * Parameter Values
     */

    /** The desired data acquisition gain circuit */
    private int     intGain;


    /*
     * GUI Components
     */

    /** Low gain button */
    private final JRadioButton    butLow;

    /** Med gain button */
    private final JRadioButton    butMed;

    /** High gain button */
    private final JRadioButton    butHigh;

    /** The gain button group */
    private final ButtonGroup     grpGain;


    /*
     * Registered action listeners 
     */

    /** List of action listeners (for button pressed events) */
    private final List<ActionListener>      lstLsnSelect;



    /*
     * Initialization
     */


    /**
     * Create a new <code>GainSelectorPanel</code> object.
     *
     *
     * @since     Dec 22, 2009
     * @author    Christopher K. Allen
     */
    public GainSelectorPanel() {
        this.lstLsnSelect = new LinkedList<ActionListener>();

        int intValLow = ProfileDevice.GAIN.LOW.getGainValue();
        this.butLow = new JRadioButton("Low"); //$NON-NLS-1$
        this.butLow.addActionListener( new SelectGainAction(intValLow) );

        int intValMed = ProfileDevice.GAIN.MED.getGainValue();
        this.butMed = new JRadioButton("Med"); //$NON-NLS-1$
        this.butMed.addActionListener( new SelectGainAction(intValMed) );

        int intValHigh = ProfileDevice.GAIN.HIGH.getGainValue();
        this.butHigh = new JRadioButton("High"); //$NON-NLS-1$
        this.butHigh.addActionListener( new SelectGainAction(intValHigh) );

        this.grpGain = new ButtonGroup();
        this.grpGain.add(this.butLow);
        this.grpGain.add(this.butMed);
        this.grpGain.add(this.butHigh);

        this.add(this.butLow);
        this.add(this.butMed);
        this.add(this.butHigh);
        this.add( new JLabel("    Signal Gain") ); //$NON-NLS-1$
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
     * Sets the displayed gain value from outside the GUI.
     *
     * @param gain  gain circuit to display as selected.
     * 
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    public void setGain(ProfileDevice.GAIN gain) {
        switch (gain) {
        case LOW:
            this.grpGain.setSelected(this.butLow.getModel(), true);
            return;
        case MED:
            this.grpGain.setSelected(this.butLow.getModel(), true);
            return;
        case HIGH:
            this.grpGain.setSelected(this.butHigh.getModel(), true);
            return;
        case UNKNOWN:
            this.grpGain.clearSelection();
            return;
        }
    }

    /**
     * Return the current gain circuit selected by the user.
     *
     * @return      desired DAQ gain setting
     * 
     * @since  Dec 23, 2009
     * @author Christopher K. Allen
     */
    public ProfileDevice.GAIN getGain() {
        ProfileDevice.GAIN    gain = ProfileDevice.GAIN.getGainFromValue(this.intGain);

        return gain;
    }

    /**
     * Sets the desired gain value from inside the GUI.
     * That is, this method is called by the button
     * actions.
     *
     * @param intGain       gain value [0,1,2]
     * 
     * @since  Dec 22, 2009
     * @author Christopher K. Allen
     */
    void   chooseDesiredGain(int intGain) {
        this.intGain = intGain;

        ActionEvent evtAction = new ActionEvent(this, intGain, "Gain Selected"); //$NON-NLS-1$
        for (ActionListener lsn : this.lstLsnSelect)
            lsn.actionPerformed(evtAction);
    }
}


