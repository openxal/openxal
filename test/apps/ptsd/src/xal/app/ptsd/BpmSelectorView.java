/**
 * BpmSelectorView.java
 *
 *  Created	: Jul 24, 2008
 *  Author      : Christopher K. Allen 
 */
package xal.app.ptsd;

import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;

/**
 *
 *
 * @since  Jul 24, 2008
 * @author Christopher K. Allen
 */
public class BpmSelectorView extends JFrame {

    
    
    /*
     * Internal Class -------------------------------------------
     */
    

    /**
     *
     *
     * @since  Jul 24, 2008
     * @author Christopher K. Allen
     */
    public class SelectorColumnModel extends DefaultTableColumnModel {

        
        /*
         * Global Constants
         */

        /**  Serialization version */
        private static final long serialVersionUID = 1L;
        
        
        
        /*
         * Initialization
         */
        
        
        /**
         * Create a new <code>SelectorColumnModel</code> object.
         *
         *
         * @since     Jul 24, 2008
         * @author    Christopher K. Allen
         */
        public SelectorColumnModel() {
            // TODO Auto-generated constructor stub
        }

    }

    

    /*
     * Internal Class -------------------------------------------
     */

    /**
     *
     *
     * @since  Jul 24, 2008
     * @author Christopher K. Allen
     */
    public static class SelectorTableModel extends AbstractTableModel {

        
        /*
         * Global Constants
         */

        /**  Serialization version */
        private static final long      serialVersionUID = 1L;

        /** Number of columns */
        private static final int       SELECTOR_COL_COUNT = 2;

        /** Column headers */
        private static final String[]  SELECTOR_COL_HEADER = {"BPM", "Active"};

        /** Column types */
        private static final Class<?>[]   SELECTOR_COL_CLASS = {String.class, Boolean.class};


        /*
         * Local Attributes
         */
        
        /** Application document object providing data for the table model */
        private final PtsdDocument    docData;

        
        
        /*
         * Initialization
         */
        
        
        /**
         * <p>
         * Create a new <code>SelectorTableModel</code> object attached
         * to the given accelerator sequence.
         * </p>
         * <p>
         * The table model is then initialized with the data contained
         * in the accelerator sequence under analysis.
         * </p>  
         *
         * @param docData       Application document object 
         *
         * @since     Jul 24, 2008
         * @author    Christopher K. Allen
         */
        public SelectorTableModel(PtsdDocument  docData) {
            this.docData = docData;
        }

        
        /*
         * Abstract Methods
         */
        
        /**
         * Return the column header for the given index.
         *
         * @since       Jul 24, 2008
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int columnIndex) {
            return SelectorTableModel.SELECTOR_COL_HEADER[columnIndex];
        }

        /**
         * Return the class type of the data in the given column.
         *
         * @since 	Jul 24, 2008
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return SelectorTableModel.SELECTOR_COL_CLASS[columnIndex];
            
        }

        /**
         * Return the number of columns in the active BPM selector
         * table.
         * 
         * @since 	Jul 24, 2008
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return SelectorTableModel.SELECTOR_COL_COUNT;
        }

        /**
         *
         * @since 	Jul 24, 2008
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return this.getDataSource().getBpmCount();
        }

        /**
         * Requirement of the <code>TableModel</code> interface.
         * Returns the data
         *
         * @since 	Jul 24, 2008
         * @author  Christopher K. Allen
         *
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            
            if (columnIndex == 0)       {
                String      strBpmId = this.getDataSource().getBpmId(rowIndex);
                return strBpmId;
                
            } else if ( columnIndex == 1) {
                boolean     bolSelect = this.getDataSource().isSelected(rowIndex);
                
                return bolSelect;
            }
            
            return null;
        }

        
        /*
         * Attribute Query
         */
        
        /**
         * Returns the main data object of the <code>PTSD</code>
         * application.  This method is a basically a proxy
         * to the data source held in the main document
         * component of the application.
         * 
         * @return      The main data object of the <code>PTSD</code> application
         *
         * @since  Jul 29, 2008
         * @author Christopher K. Allen
         *
         */
        public BpmDataManager getDataSource() {
            return this.docData.getDataSource();
        }


        /*
         * Support Methods
         */
        

    }


    
    
    
    /*
     * BpmSelectorView Class -------------------------------------------
     */
    
    
    /*
     * Global Constants
     */
    
    /**  Serialization version id */
    private static final long serialVersionUID = 1L;

    /**  Default table (model) title */
    private static final String TABLE_TITLE = "Active BPMs";

    
    /*
     * Local Attributes
     */
    
    
    /**
     * GUI Components
     */
    private final SelectorTableModel          stmDisplay;
    private final JTable                      tblDisplay;
    private final JScrollPane                 spnDisplay;
    
    /** The application data object */
    private final PtsdDocument                docData;
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>BpmSelectView</code> object.
     * 
     * @param docData The application document object
     *
     * @throws HeadlessException
     *
     * @since     Jul 24, 2008
     * @author    Christopher K. Allen
     */
    public BpmSelectorView(PtsdDocument docData) throws HeadlessException {
        this(TABLE_TITLE, docData);
    }

    /**
     * Create a new <code>BpmSelectView</code> object.
     *
     * @param title   Title of active BPM selection table
     * @param docData The application document object
     *  
     * @throws HeadlessException
     *
     * @since     Jul 24, 2008
     * @author    Christopher K. Allen
     */
    public BpmSelectorView(String title, PtsdDocument docData) throws HeadlessException {
        super(title);
        
        this.docData = docData;
        
        this.stmDisplay = new SelectorTableModel(docData);
        this.tblDisplay = new JTable(this.stmDisplay);
        this.spnDisplay = new JScrollPane(this.tblDisplay); 
    }

    
    
    
    
//    /**
//     * Create a new <code>BpmSelectView</code> object.
//     *
//     * @param gc
//     *
//     * @since     Jul 24, 2008
//     * @author    Christopher K. Allen
//     */
//    public BpmSelectorView(GraphicsConfiguration gc) {
//        super(gc);
//        // TODO Auto-generated constructor stub
//    }
//
//    /**
//     * Create a new <code>BpmSelectView</code> object.
//     *
//     * @param title
//     * @param gc
//     *
//     * @since     Jul 24, 2008
//     * @author    Christopher K. Allen
//     */
//    public BpmSelectorView(String title, GraphicsConfiguration gc) {
//        super(title, gc);
//        // TODO Auto-generated constructor stub
//    }
//
}
