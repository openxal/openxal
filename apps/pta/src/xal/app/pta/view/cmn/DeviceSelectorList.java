package xal.app.pta.view.cmn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
 * SMF devices by device ID (presumably).  The default behavior is to allow only a single
 * selection, however, a multiple selection mode may be specified by invoking the
 * <code>{@link #setMultiSelectionMode(boolean)}</code> method. 
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 *
 * @since  Jan 20, 2010
 * @version Sep30, 2014
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
    static class DeviceListModel extends AbstractListModel<Object> {

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
    private final JList<Object>                       lbxDevs;

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
        this.lbxDevs = new JList<Object>( this.modDevs );
        this.lbxDevs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane paneTrgEvt = new JScrollPane(this.lbxDevs);

        this.setLayout( new BoxLayout(this, BoxLayout.Y_AXIS) );
        this.add(lblTrgEvt);
        this.add(paneTrgEvt);
    }

    /**
     * Sets the selection mode of the device list box to single selection if
     * argument is <code>false</code> and multi-mode if argument is <code>true</code>.
     * 
     * @param bolMultiSel   selection of devices is unrestricted if <code>true</code>,
     *                      only one device may be selected if <code>false</code>.
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    public void setMultiSelectionMode(boolean bolMultiSel) {
        if (bolMultiSel == true)
            this.lbxDevs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        else
            this.lbxDevs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
     * GUI's list box.  This is a convenience method for use when
     * only one selection is expected.  It will return the first selection
     * in the list box if multiple selection are possible.
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
     * Returns all the selections within the list box.  If only single
     * selections are possible then the list will contain that single
     * selection.
     * 
     * @return  all currently selected entries in the list box
     *
     * @author Christopher K. Allen
     * @since  Sep 30, 2014
     */
    public List<String> getSelectedDevices() {
        List<Object>    lstObjSel = this.lbxDevs.getSelectedValuesList();
        
        List<String> lstDevIds = new LinkedList<>();
        for (Object objSel : lstObjSel) {
            if (objSel instanceof String)
                lstDevIds.add( (String) objSel );
        }
        
        return lstDevIds;
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