package xal.app.pta.view.cmn;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractListModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

/**
 * GUI component providing the display of a list of selectable
 * SMF devices.  The user may choose a single instance amongst 
 * this list.  
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Jan 20, 2010
 * @author Christopher K. Allen
 */
public class DeviceSelectorList extends JPanel {

    /**
     * Swing <code>ListModel</code> implementation used to manage the
     * data displayed in <code>DeviceList</code> objects.
     *
     * @author Christopher K. Allen
     * @since   Mar 23, 2011
     */
    static class DeviceListModel extends AbstractListModel {

        /** The serialization version */
        private static final long serialVersionUID = 1L;

        /** The dynamics array of device IDs */
        private ArrayList<Object>       arrDevIds = new ArrayList<Object>();


        /*
         * Operations
         */

        /**
         * Specify the list of device IDs to display in the
         * <tt>JList</tt> list box.
         *
         * @param lstLabels collection of string labels
         *
         * @author Christopher K. Allen
         * @since  Mar 23, 2011
         */
        public void setDeviceList(Collection<String> lstLabels) {
            this.arrDevIds.addAll(lstLabels);
            this.fireIntervalAdded(this, 0, lstLabels.size() - 1);
        }

        /**
         * Removes all device ID labels in the list.
         *
         * @author Christopher K. Allen
         * @since  Mar 23, 2011
         */
        public void clear() {
            if (this.arrDevIds.size() == 0)
                return;

            int indLng = this.arrDevIds.size() - 1;

            this.arrDevIds.clear();
            this.fireIntervalRemoved(this, 0, indLng);
        }

        /*
         * ListModel Interface
         */


        /**
         * Returns the list element at the given index
         * 
         * @param index     index into the JList box
         * 
         * @return device id at the given index 
         * 
         * @since Mar 23, 2011
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public Object getElementAt(int index) {
            return this.arrDevIds.get(index);
        }

        /**
         * Returns the number of elements to display
         * in the JList box.
         * 
         * @return  number of device ids displayed
         * 
         * @since Mar 23, 2011
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize() {
            return this.arrDevIds.size();
        }

    }

    
    
    /*
     * Global Constants
     */

    /**  Serialization version */
    private static final long serialVersionUID = 1L;



    /*
     * Local Attributes
     */

    /** The GUI display list box */
    private final JList             lbxDevs;

    /** The list box data model */
    private final DeviceSelectorList.DeviceListModel   modDevs;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DeviceList</code> object.
     *
     *
     * @since     Jan 19, 2010
     * @author    Christopher K. Allen
     */
    public DeviceSelectorList() {
        JLabel lblTrgEvt = new JLabel("Device");

        this.modDevs = new DeviceListModel();
        this.lbxDevs = new JList( this.modDevs );
        this.lbxDevs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane paneTrgEvt = new JScrollPane(this.lbxDevs);

        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
        this.add(lblTrgEvt);
        this.add(paneTrgEvt);
    }

    /**
     * Set the list of displayed device IDs.  The current
     * selection is cleared.
     *
     * @param setDevIds     collection of profile device identifiers
     * 
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    public void     setDeviceList(Collection<String> setDevIds) {
        //            Object[]    arrDevIds = setDevIds.toArray();
        //
        //            this.lbxDevs.setListData(arrDevIds);
        //            this.lbxDevs.setSelectedIndex(-1);
        this.modDevs.setDeviceList(setDevIds);
        this.lbxDevs.setSelectedIndex(-1);
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
    public void     registerSelectionListener(ListSelectionListener lsnAction) {
        this.lbxDevs.addListSelectionListener(lsnAction);
    }


    /*
     * Operations
     */
    
    /**
     * Return the user-selected profile device from the 
     * GUI's list box.
     * 
     * @return      desired profile device, 
     *              or <code>null</code> if there was an initialization error
     *              with the list box 
     *
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    public String   getSelectedDevice() {
        Object      objSel = this.lbxDevs.getSelectedValue();

        if (objSel instanceof String)
            return (String)objSel;

        return null;
    }

    /**
     * Sets the profile deviced selected
     * field of the list.  This method will not
     * fire an action event.
     *
     * @param strDevId   the new selected profile device identifier
     * 
     * @since  Jan 20, 2010
     * @author Christopher K. Allen
     */
    public void     setSelectedDeviceSilently(String strDevId) {
        ListSelectionListener[]    arrLsns = this.lbxDevs.getListSelectionListeners();
        for (ListSelectionListener lsn : arrLsns)
            this.lbxDevs.removeListSelectionListener(lsn);

        this.lbxDevs.setSelectedValue(strDevId, true);

        for (ListSelectionListener lsn : arrLsns)
            this.lbxDevs.addListSelectionListener(lsn);
    }

    /**
     * Clears the display list.
     *
     * 
     * @since  Apr 27, 2010
     * @author Christopher K. Allen
     */
    public void     clear() {
        this.modDevs.clear();
    }

    /**
     * Clears the selection of the list box
     * (i.e., blank).
     *
     * 
     * @since  Jan 21, 2010
     * @author Christopher K. Allen
     */
    public void     clearSelection() {
        this.lbxDevs.setSelectedIndex(-1);
    }
}