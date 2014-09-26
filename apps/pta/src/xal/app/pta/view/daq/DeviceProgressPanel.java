package xal.app.pta.view.daq;

import xal.app.pta.MainScanController.MOTION_STATE;
import xal.app.pta.rscmgt.AppProperties;
import xal.smf.AcceleratorNode;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Displays the table of profile diagnostic devices under
 * control.  Also displays progress bar during data
 * acquisition process.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Sep 16, 2009
 * @author Christopher K. Allen
 */
public class DeviceProgressPanel extends JPanel {

    
    /*
     * Global Constants
     */
    
    /**  Serialization Version */
    private static final long serialVersionUID = 1L;

    
//    /** list of recognized hardware device types */
//    private final Class<?> arrValidDevTypes[] = {
//                                ProfileMonitor.class,
//                                WireScanner.class,
//                                WireHarp.class
//                                };
    

    
    
    /** Table column headers */
    public final String ARR_STR_HEADERS[] = {
                                             "Device ID",
                                             "Progress",
                                             "MVT"
                                              };

    /**
     * This provides the rendering object for the progress bar (i.e.,
     * the <code>JProgressBar</code> object) in the second column
     * of the progress table.  This job is pretty simple since
     * the second column contains the progress bar object itself.
     *
     * @since  Nov 10, 2009
     * @author Christopher K. Allen
     */
    class ProgTableRenderer implements TableCellRenderer {

        /**
         * Returns the rendering swing component for the progress table's
         * second column.  
         * 
         * @return      the argument <code>value</code> should be the renderer
         *              itself
         *
         * @since 	Nov 10, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, 
                                                        Object value, 
                                                        boolean isSelected, 
                                                        boolean hasFocus,
                                                        int row, int column
                                                        ) 
        {
            if (value instanceof JProgressBar)
                return (JProgressBar)value;
            
            else if (value instanceof JTextArea)
                return (JTextArea)value;
            
            else if (value instanceof JComponent)
                return (JComponent) value;
            
            else
                return null;
        }
        
    }
    
    /**
     * The table model class for the <code>JTable</code>
     * component of the <code>TablePane</code> class.
     *
     * @since  Aug 24, 2009
     * @author Christopher K. Allen
     */
    class ProgTableController extends AbstractTableModel {
    

        /*
         * Global Constants
         */
        
        /**  Serialization version */
        private static final long serialVersionUID = 1L;

        
        /*
         * Instance Attributes
         */

        /** the list of selected hardware devices to monitor */
        private final ArrayList<AcceleratorNode>                lstDaqHware;
        
        /** progress condition of hardware */
        private final Map<AcceleratorNode, JProgressBar>        mapDevPBar;
        
        /** status condition of hardware (moving, stopped) */
        private final Map<AcceleratorNode, JTextArea>          mapDevMotion;
        
        
        
        /*
         * Initialization
         */
        
        /**
         * Create a new <code>DataTableModel</code> object.
         *
         *
         * @since     Aug 24, 2009
         * @author    Christopher K. Allen
         */
        public ProgTableController() {
            super();
            this.lstDaqHware  = new ArrayList<AcceleratorNode>();
            this.mapDevPBar   = new HashMap<AcceleratorNode, JProgressBar>();
            this.mapDevMotion = new HashMap<AcceleratorNode, JTextArea>(); 
        }
    
    
        
        /*
         * Operations
         */
    
        
        /**
         * Removes all the table data.
         *
         * 
         * @since  Aug 24, 2009
         * @author Christopher K. Allen
         */
        public void clearTable() {
            this.lstDaqHware.clear();
            this.mapDevPBar.clear();
            this.mapDevMotion.clear();
        }
        
    
        /**
         * Adds a data acquisition device to the 
         * the list of displayed devices.
         *
         * @param nodeDev   the new DACQ device to monitor
         * 
         * @since  Sep 18, 2009
         * @author Christopher K. Allen
         */
        public void addDaqHardware(AcceleratorNode nodeDev) {
            JProgressBar        cmpPBar    = new JProgressBar();
            JTextArea           cmpMotion  = new JTextArea();
            
            cmpMotion.setBackground(MOTION_STATE.HALTED.getColor());
            this.lstDaqHware.add( nodeDev );
            this.mapDevPBar.put(nodeDev, cmpPBar );
            this.mapDevMotion.put(nodeDev, cmpMotion );

            int iRow = this.lstDaqHware.size() - 1;
            this.fireTableRowsInserted(iRow, iRow);
        }
        
        /**
         * Set the entire list of displayed <code>AcceleratorNode</code>
         * data acquisition devices.
         *
         * @param lstNodes  ordered accelerator node list
         * 
         * @since  Aug 24, 2009
         * @author Christopher K. Allen
         */
        public void addAllDaqHardware(List<AcceleratorNode> lstNodes) {
            this.clearTable();
    
            for (AcceleratorNode node : lstNodes) {
                JProgressBar        cmpPBar    = new JProgressBar();
                JTextArea           cmpMotion  = new JTextArea();
                cmpMotion.setBackground(MOTION_STATE.HALTED.getColor());
                
                this.lstDaqHware.add(node);
                this.mapDevPBar.put(node, cmpPBar );
                this.mapDevMotion.put(node, cmpMotion);
            }
            this.fireTableDataChanged();
        }
    
        /**
         * Returns the progress bar corresponding to the given
         * hardware device (which should be displayed in the
         * table).  If the argument is an accelerator device
         * that is not displayed in the table, a <code>null</code>
         * value is returned. 
         *
         * @param nodeDev       accelerator hardware device reference
         * 
         * @return              progress bar associated with the hardware
         *                      element, <code>null</code> if not found
         * 
         * @since  Nov 4, 2009
         * @author Christopher K. Allen
         */
        public JProgressBar     getProgressBar(AcceleratorNode nodeDev) {
            JProgressBar  barDev = this.mapDevPBar.get(nodeDev);
            
            return barDev;
        }
        
        /**
         * Get the GUI component for displaying the motion state
         * of the given DAQ device.
         *
         * @param smfNode       DAQ device
         * 
         * @return      component displaying motion state
         * 
         * @since  Nov 18, 2009
         * @author Christopher K. Allen
         */
        public JTextArea        getMotionStateDisplay(AcceleratorNode smfNode) {
            JTextArea   cmpMotion = this.mapDevMotion.get(smfNode);
            
            return cmpMotion;
        }
        
        /**
         * To be called by the controlling object upon modifying
         * the row associated with the given device.
         *
         * @param nodeDev
         * 
         * @since  Nov 18, 2009
         * @author Christopher K. Allen
         */
        public void fireTableRowUpdated(AcceleratorNode nodeDev) {
            int index = this.lstDaqHware.indexOf(nodeDev);
            
            if (index > -1)
                this.fireTableRowsUpdated(index, index);
        }
        
        
        /*
         * AbstractTableModel Overrides
         */
        
        /**
         * Get the class type of the column with 
         * index <arg>iCol</arg>.
         * 
         * @param  iCol index of target tablecolumn
         * 
         * @return column <arg>iCol</arg> class type
         *
         * @since   Aug 24, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int iCol) {

            switch (iCol) {
            case 0:
                return AcceleratorNode.class;
                
            case 1:
                return JProgressBar.class;
                
            case 2:
                return JTextArea.class;
                
            default:
                return super.getColumnClass(iCol);
            }
        }
    
       /**
        * Returns the header of table column with index
        * <arg>iCol</arg>.
        * 
        * @param  iCol index of target tablecolumn
        * 
        * @return header for column <arg>iCol</arg> 
        * 
        * @since   Aug 24, 2009
        * @author  Christopher K. Allen
        *
        * @see javax.swing.table.AbstractTableModel#getColumnName(int)
        */
       @Override
       public String getColumnName(int iCol) {
           return ARR_STR_HEADERS[iCol];
       }
    
       /**
        * Return the edit capabilities for any cell in the table.
        * 
        * @return   true if user-modifiable, false otherwise
        *
        * @since   Aug 24, 2009
        * @author  Christopher K. Allen
        *
        * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
        */
       @Override
       public boolean isCellEditable(int iRow, int iCol) {
           return false;
       }
    
    
    
    
        /*
         * TableModel Interface
         */
        
        /**
         * Returns the number of table columns.
         * 
         * @return number of table columns
         * 
         * @since   Aug 24, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return ARR_STR_HEADERS.length;
        }
    
        /**
         *
         * @since   Aug 24, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return this.lstDaqHware.size();
        }
    
        /**
         * Returns the object in the table cell with
         * index (<arg>iRow,iCol</arg>).
         * 
         * @param iRow      cell row index
         * @param iCol      cell column index
         * 
         * @return  object occupied by cell (<arg>iRow,iCol</arg>)
         *
         * @since   Aug 24, 2009
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int iRow, int iCol) {
    
            AcceleratorNode nodeDev = this.lstDaqHware.get(iRow);
            
            switch (iCol) {
            
            case 0:
                return nodeDev;
                
            case 1:
                JProgressBar    barProg = this.mapDevPBar.get(nodeDev);
                return barProg;
                
            case 2:
                JTextArea       txtArea = this.mapDevMotion.get(nodeDev);
                return txtArea;
                
            default:
                return null;
            }
        }
        
    }       // End: Table Model Controller


    

    
    /*
     * Instance Attributes
     */
    
    /** The selection table */
    private final JTable                tblDacqStatus;
    
    /** The custom selection table controller */
    private final ProgTableController   mdlCtrlr;
    
    /** The scroll pane containing the table */
    private final JScrollPane           paneTblScrollr;

    /** The DAQ text status display */
    private final JTextField            txtDaqStatus;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>TablePane</code> object.
     *
     *
     * @since     Aug 26, 2009
     * @author    Christopher K. Allen
     */
    public DeviceProgressPanel() {
        super();

        this.mdlCtrlr = new ProgTableController();
        this.tblDacqStatus = new JTable( this.mdlCtrlr );
        this.paneTblScrollr = new JScrollPane( this.tblDacqStatus );
        
        this.txtDaqStatus = new JTextField();

//        this.setViewportView( this.tblDacqStatus );
        this.buildViewPane();
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Adds a new data acquisition device to the 
     * table for monitoring.
     *
     * @param nodeDev       new DACQ device to monitor
     * 
     * @since  Sep 18, 2009
     * @author Christopher K. Allen
     */
    public void addDaqDevice(AcceleratorNode nodeDev) {
        if (this.validDevice(nodeDev))
            this.mdlCtrlr.addDaqHardware(nodeDev);
        else
            this.updateComment(nodeDev.getId() + "is an invalid device");
    }
    
    /**
     * Sets the ordered list of accelerator nodes that
     * are monitored in the table (en masse).
     * This method is actually a proxy to the same-named 
     * method of the contained table model.   
     *
     * @param lstNodes      ordered list of accelerator nodes
     * 
     * @since  Aug 26, 2009
     * @author Christopher K. Allen
     */
    public void addAllDaqHardware(List<AcceleratorNode> lstNodes) {
        for (AcceleratorNode node : lstNodes) {
            if (!this.validDevice(node)) {
                this.updateComment(
                    "No progress monitoring: " + node.getId() + " is invalid"
                    );
                return;
            }
        }
        this.mdlCtrlr.addAllDaqHardware(lstNodes);
    }

    
    /**
     * Sets the string displayed in the text box of this panel
     * that is used for displaying data acquisition
     * status.
     *
     * @param strCmt    new status comment to display
     * 
     * @since  Oct 28, 2009
     * @author Christopher K. Allen
     */
    public void updateComment(String strCmt) {
        this.txtDaqStatus.setText(strCmt);
    }
    
    
    
    /**
     * Clears the progress display of all devices
     * and comments.
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public void clear() {
        this.updateComment("");
        this.mdlCtrlr.clearTable();
        this.repaint();
    }
    
    /**
     * <p>
     * For the given hardware device, set the current progress 
     * value to zero and the maximum value to the given argument 
     * value.  If there is no associated hardware device in this table
     * nothing is done.
     * </p>
     * <p>
     * Realize that the argument is the value that indicates 
     * the end of the process. 
     * </p>
     *
     * @param nodeDev       hardware device requiring progress initialization
     * @param intMaxVal     progress value indicating progress completion
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    public void     initProgress(AcceleratorNode nodeDev, int intMaxVal) {
        JProgressBar  barDev = this.mdlCtrlr.getProgressBar(nodeDev);
        JComponent    cmpMtn = this.mdlCtrlr.getMotionStateDisplay(nodeDev);
        
        if (barDev == null) 
            return;
        
        barDev.setMinimum(0);
        barDev.setMaximum(intMaxVal);
        barDev.setValue(0);
        cmpMtn.setBackground(MOTION_STATE.UNKNOWN.getColor());
        
        this.mdlCtrlr.fireTableDataChanged();
    }
    
    /**
     * Set the progress value for the given hardware device.
     * If there is no associated hardware device in this table
     * nothing is done. 
     *
     * @param smfDev       hardware device whose progress is to be updated
     * @param intVal        progress value for given hardware
     * 
     * @since  Nov 4, 2009
     * @author Christopher K. Allen
     */
    public void     setProgressValue(AcceleratorNode smfDev, int intVal) {
        JProgressBar  barDev = this.mdlCtrlr.getProgressBar(smfDev);

        if (barDev == null) 
            return;
        
        barDev.setValue(intVal);
        this.mdlCtrlr.fireTableDataChanged();
    }
    
    /**
     * Sets the given motion state displayed for the given
     * DAQ device.
     *
     * @param nodeDev
     * @param state
     * 
     * @since  Nov 18, 2009
     * @author Christopher K. Allen
     */
    public void setMotionState(AcceleratorNode nodeDev, MOTION_STATE state) {
        JComponent cmpMotion = this.mdlCtrlr.getMotionStateDisplay(nodeDev);
        
        cmpMotion.setBackground(state.getColor());
        this.mdlCtrlr.fireTableDataChanged();
    }
    

    
    
    /*
     * Support Methods
     */
    
    /**
     * Builds the visible user interface.
     * 
     * @since  Aug 26, 2009
     * @author Christopher K. Allen
     */
    private void buildViewPane() {

        // The status table
//        this.paneTblScrollr.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//        this.paneTblScrollr.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        int szDevIdColWd = AppProperties.DAQGUI.DEVIDCOL_WD.getValue().asInteger();
        int szPrgBarColWd = AppProperties.DAQGUI.PROGCOL_WD.getValue().asInteger();
        int szMotionColWd = AppProperties.DAQGUI.MOTIONCOL_WD.getValue().asInteger();
        this.tblDacqStatus.getColumnModel().getColumn(0).setPreferredWidth(szDevIdColWd);
        this.tblDacqStatus.getColumnModel().getColumn(1).setPreferredWidth(szPrgBarColWd);
        this.tblDacqStatus.getColumnModel().getColumn(2).setPreferredWidth(szMotionColWd);
        this.tblDacqStatus.getColumnModel().getColumn(1).setCellRenderer(new ProgTableRenderer());
        this.tblDacqStatus.getColumnModel().getColumn(2).setCellRenderer(new ProgTableRenderer());

//        int szTblWd = AppProperties.DAQGUI.TOTAL_WD.getValue().asInteger();
//        int szTblHt = AppProperties.DAQGUI.PRG_TBL_HT.getValue().asInteger();
//        Dimension       dimTblPane = new Dimension(szTblWd, szTblHt);
//        this.tblDacqStatus.setPreferredSize(dimTblPane);
//
//        this.tblDacqStatus.setVisible(true);
//        this.tblDacqStatus.setAutoscrolls(true);
//        this.tblDacqStatus.doLayout();
        
        // The status text box
        this.txtDaqStatus.setAutoscrolls(false);
        this.txtDaqStatus.setEditable(false);

        // Size
//        int szStatWd = AppProperties.DAQGUI.TOTAL_WD.getValue().asInteger();
//        int szStatHt = 25;
//        Dimension dimStat = new Dimension(szStatWd, szStatHt);
//        this.txtDacqStatus.setPreferredSize(dimStat);

        int szTblWd = AppProperties.DAQGUI.TOTAL_WD.getValue().asInteger();
        int szTblHt = AppProperties.DAQGUI.PRG_TBL_HT.getValue().asInteger();
        Dimension       dimTblPane = new Dimension(szTblWd, szTblHt);
        this.paneTblScrollr.setPreferredSize(dimTblPane);

        // Alignment
        Box     boxDaqPrg = Box.createVerticalBox();
        boxDaqPrg.add(this.paneTblScrollr);
        boxDaqPrg.add( Box.createVerticalStrut(10));
        boxDaqPrg.add(this.txtDaqStatus);

//        Box     boxDacqPrg = Box.createVerticalBox();
//        boxDacqPrg.add(this.tblDacqStatus);
//        boxDacqPrg.add( Box.createVerticalStrut(10));
//        boxDacqPrg.add(this.txtDacqStatus);
//
        
        this.add(boxDaqPrg);
    }
    
    /**
     * Check if the given hardware node is on the list
     * of valid device types. 
     *
     * @param nodeDev   hardware device under inspection
     * 
     * @return  true if hardware device is recognized,
     *          false otherwise
     * 
     * @since  Aug 21, 2009
     * @author Christopher K. Allen
     */
    private boolean     validDevice(AcceleratorNode nodeDev) {
//        for (Class<?> typeValidDev : this.arrValidDevTypes) {
//            if (nodeDev.getClass() == typeValidDev)
//                return true;
//        }
//        
//        return false;
        
        return true;
    }
}