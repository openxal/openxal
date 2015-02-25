/**
 * DeviceTreePanel.java
 *
 *  Created	: Aug 19, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.view.cmn;

import xal.app.pta.MainWindow;
import xal.app.pta.rscmgt.AppProperties;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Displays the supported SMF devices, allows the user to
 * select one or more of these devices, then fires an event
 * to registered listeners that a selection has occurred.
 * 
 * <p>
 * <b>Ported from XAL on Jul 18, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Aug 19, 2009
 * @author Christopher K. Allen
 */
public class DeviceSelectorPanel extends JPanel implements TreeSelectionListener  {

    /**
     * Classes wishing to register for selection events
     * should implement this interface.
     *
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public interface IDeviceSelectionListener {

        /**
         * A new device selection event has occurred.
         *
         * @param lstDevs       list of newly selected devices
         * 
         * @since  Nov 13, 2009
         * @author Christopher K. Allen
         */
        void    newDeviceSelection(List<AcceleratorNode> lstDevs);
    }


    /**
     * <p>
     * Displays the accelerator node selection tree and provides
     * the selection mechanism.
     * </p>
     * <p>
     * The objects contained in the tree nodes are sub-classes
     * of the <code>AcceleratorNode</code> class.  These objects
     * are passed to any registered 
     * <code>TreeSelectionListener</code> object in the 
     * <code>TreeSelectionEvent</code> argument.
     * </p>
     *
     * @since  Aug 24, 2009
     * @author Christopher K. Allen
     * 
     * @see TreeSelectionListener
     * @see TreeSelectionEvent
     */
    class TreePane extends JScrollPane {
        
        
        /*
         * Global Constants
         */
        
        /**  Serialization Version */
        private static final long serialVersionUID = 1L;

        
        
        /*
         * Instance Attributes
         */
        
        /** The visible tree */
        private JTree                       treeAccel;
        
        /** Controller for the tree */
        private TreeModel                   mdlCtrlr;
        
        /** The root node of the tree */
        private DefaultMutableTreeNode      nodeRoot;
        
        /** The map of accelerator device IDs to tree nodes */
        private final Map<String, MutableTreeNode>  mapSmf2Node;
        

        
        /*
         * Initialization
         */
    
        /**
         * Creates a new </code>TreePane</code> object,
         * initializing it to the given accelerator object,
         * and building the GUI panel.
         *
         * @param smfAccel      accelerator to display
         * 
         * @since  Aug 20, 2009
         * @author Christopher K. Allen
         */
        public TreePane(Accelerator smfAccel) {

            this.mapSmf2Node = new HashMap<String, MutableTreeNode>();
            
            this.freeTree(this.nodeRoot);
            this.nodeRoot = this.buildTree(smfAccel);
            this.mdlCtrlr = new DefaultTreeModel( this.nodeRoot );
            this.treeAccel = new JTree( this.mdlCtrlr );
            
            this.setViewportView(this.treeAccel);
            this.buildViewPane();
        }
        
        
        /*
         * Operations
         */

        
        /**
         * Resets the SMF accelerator displayed in the tree
         * to that given in the argument.
         *
         * @param smfAccel new accelerator to display
         * 
         * @since  Jan 26, 2010
         * @author Christopher K. Allen
         */
        public void resetAccelerator(Accelerator smfAccel) {

            this.mapSmf2Node.clear();
            
            this.nodeRoot = this.buildTree(smfAccel);
            this.mdlCtrlr = new DefaultTreeModel( this.nodeRoot );
            this.treeAccel = new JTree( this.mdlCtrlr );
            
            this.setViewportView(this.treeAccel);
        }
        
        /**
         * Proxy to the <code>JTree</code> component's
         * method by the same name.
         *
         * @param tsl   object to receive tree selection events
         * 
         * @since  Aug 26, 2009
         * @author Christopher K. Allen
         * 
         * @see JTree#addTreeSelectionListener(TreeSelectionListener)
         */
        public void addTreeSelectionListener(TreeSelectionListener tsl) {
            this.treeAccel.addTreeSelectionListener(tsl);
        }

        /**
         * Configure the device selection panel so that only
         * one device may be selected at a time.
         *
         * @param bolSngSelect      switch to single selection mode if true,
         *                          multiple device selection mode if false
         * 
         * @since  Nov 13, 2009
         * @author Christopher K. Allen
         */
        public void setSingleSelectionMode(boolean bolSngSelect) {
            TreeSelectionModel mdlTree = this.treeAccel.getSelectionModel();
            
            if (bolSngSelect) {
                mdlTree.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            } else {
                mdlTree.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            }
        }
        
        /**
         * Returns the tree paths currently selected by the user.
         *
         * @return      array of tree paths under selection
         * 
         * @since  Aug 28, 2009
         * @author Christopher K. Allen
         * 
         * @see JTree#getSelectionPaths()
         */
        public TreePath[] getSelectionPaths() {
            return this.treeAccel.getSelectionPaths();
        }
        
        /**
         * Returns the tree path currently selected by the user.
         * For use with single-selection mode trees.
         *
         * @return      Object at the end of the selected path
         * 
         * @since  Aug 28, 2009
         * @author Christopher K. Allen
         * 
         * @see JTree#getSelectionPaths()
         */
        public Object getSelectedPathComponent() {
            return this.treeAccel.getLastSelectedPathComponent();
        }
        
        
        /**
         * Expands the path to the given accelerator device in the tree.
         *
         * @param strDevId  ID of accelerator device to display as selected within the tree
         * 
         * @return          <code>true</code> if the device was present and selected,
         *                  <code>false</code> otherwise
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2012
         */
        public boolean  setDeviceSelected(String strDevId) {
            
            // Look up device tree node, check that it is indeed within the tree
            MutableTreeNode         nodeDevSel = this.mapSmf2Node.get(strDevId);

            if (nodeDevSel == null)
                return false;
            
            // Search the tree for the path, if the path was not found return
            TreePath    pathDev  = this.searchPathTo(nodeDevSel);
            
            if (pathDev == null)
                return false;
            
            // Expand the path
            this.treeAccel.addSelectionPath(pathDev);
            this.treeAccel.fireTreeExpanded(pathDev);
            return true;
        }
        
        /**
         * Clears all the selections in the tree.
         *
         * @author Christopher K. Allen
         * @since  Jul 10, 2012
         */
        public void clearTree() {
            this.treeAccel.clearSelection();
        }
        
        
        /*
         * Support Methods
         */
        
        /**
         * <p>
         * Frees the <code>JTree</code> object by completely
         * detaching the tree.  Presumably then there are no
         * references to any tree node and the Java garbage
         * collection should recover all the node resources.
         * </p>
         * <p>
         * This is a recursive function.  Thus, to initiate the
         * recursion process the method should be called on 
         * the root node of the tree.  That is, calling
         * this method on the root node of the tree will
         * free the entire tree.
         * </p>
         *
         * @param nodeCurrent       the root node of the tree on initial call,
         *                          the current node during recursion
         * 
         * @since  Aug 20, 2009
         * @author Christopher K. Allen
         */
        @SuppressWarnings("unchecked")
        private void freeTree(MutableTreeNode nodeCurrent) {
            if (nodeCurrent == null)
                return;
            
            // Detach the current node from its parent
            nodeCurrent.removeFromParent();

            // Do the same for each of its children (through recursion)
            Enumeration<MutableTreeNode> enumChildren  = nodeCurrent.children();
            while ( enumChildren.hasMoreElements() ) {
                MutableTreeNode nodeChild = enumChildren.nextElement();
                
                this.freeTree(nodeChild);
            }
        }
        
        /**
         * <p>
         * Build the tree object mimicking the structure of the given 
         * accelerator object.
         * </p>
         * <p>
         * This is a recursive function.  Thus, to initiate the
         * recursion process the method should be called on 
         * <code>Accelerator</code> object.  That is, call
         * this method on accelerator object to build entire tree.
         * </p>
         * <p>
         * <h4>NOTE:</h4>
         * &middot; This method requires {@link DeviceSelectorPanel#validDevice(AcceleratorNode)}
         * to test hardware device types.
         *
         * @param nodeSmfParent     Should be the SMF <code>Accelerator</code> object
         *                          to create the entire tree.
         * 
         * @return                  a tree node representing the given accelerator node,
         *                          with all its children
         *  
         * @since  Aug 20, 2009
         * @author Christopher K. Allen
         * 
         * @see DeviceSelectorPanel#validDevice(AcceleratorNode)
         */
        @SuppressWarnings("synthetic-access")
        private DefaultMutableTreeNode buildTree(AcceleratorSeq nodeSmfParent) {
            DefaultMutableTreeNode  nodeTreeParent = new DefaultMutableTreeNode( nodeSmfParent );
            this.mapSmf2Node.put(nodeSmfParent.getId(), nodeTreeParent);
            
            List<AcceleratorNode>   lstSmfChildNodes = nodeSmfParent.getNodes();
            for (AcceleratorNode nodeSmfChild : lstSmfChildNodes) {
                DefaultMutableTreeNode      nodeTreeChild;
                
                if (nodeSmfChild instanceof AcceleratorSeq) {
                    // We call this method on the sequence, building a sub-tree for it 
                    // If the sub-tree has children it is attached to the parent
                    AcceleratorSeq seqSmfChild = (AcceleratorSeq)nodeSmfChild;
                
                    nodeTreeChild = this.buildTree(seqSmfChild);
                    
                    if (nodeTreeChild.getChildCount() > 0)
                        nodeTreeParent.add(nodeTreeChild);
                    
                } else if ( validDevice(nodeSmfChild) ){
                    // We create a leaf node for the device and add it to the parent
                    nodeTreeChild = new DefaultMutableTreeNode( nodeSmfChild );
                    nodeTreeParent.add(nodeTreeChild);
                    this.mapSmf2Node.put(nodeSmfChild.getId(), nodeTreeChild);
                    
                } else {
                    // We do nothing
                    
                }
                
            }
            
            return nodeTreeParent;
        }
        
        /**
         * Returns a <code>TreePath</code> object representing the path from the tree root to the given tree
         * node.  
         *  
         * @param nodeDev   target node within the tree
         *  
         * @return          path from the tree root to the given target tree node, or <code>null</code> if 
         *                  node was not found or tree is corrupt
         *
         * @author Christopher K. Allen
         * @since  Apr 30, 2012
         */
        private TreePath    searchPathTo(MutableTreeNode  nodeDev) {
            
            // Create the stack of tree nodes with the root on the bottom and the current node on the top
            //      Put the root node as the only element to initialize search
            List<MutableTreeNode>   lstNodeStack    = new LinkedList<MutableTreeNode>();
            lstNodeStack.add(this.nodeRoot);

            // Do the (recursive) search and save the result flag
            boolean     bolResult = this.searchPathRecursively(lstNodeStack, nodeDev);
            
            
            // If the search was successful create and return the appropriate tree path
            if ( bolResult == true) {
                Object[]    arrObj = lstNodeStack.toArray();
                TreePath    pthDev = new TreePath(arrObj);

                return pthDev;
            };
            
            // Unable to find tree path, return null
            return null;
        }
        
        /**
         * This function implements a recursive search and the calling method must provide the list of 
         * tree nodes used for intermediate data storage.  In the initial call the list contains the root
         * node of the tree.
         * If this method returns a <code>null</code> value then something went wrong - likely the
         * given tree node is bad.         
         * 
         * @param pthCurrent    Current construction of the tree path between the root and the target element
         * @param lstNodeStack  Stack of tree nodes to the current search location within the tree, contains the root
         *                      node on the bottom of the stack and the current node on the top
         * @param nodeDev       The target element, the end of the tree path
         * @return
         *
         * @author Christopher K. Allen
         * @since  Jul 9, 2012
         */
        @SuppressWarnings("unchecked")
        private boolean searchPathRecursively(List<MutableTreeNode> lstNodeStack, MutableTreeNode nodeDev) {

            // Get the last node in the list - the parent for this iteration
            MutableTreeNode nodeParent = lstNodeStack.get( lstNodeStack.size() - 1 );

            // If the parent is the target node, we have found the correct path
            if (nodeParent.equals(nodeDev))
                return true;

            // For each child node in the parent node...
            for (Enumeration<TreeNode> enmChildren = nodeParent.children(); enmChildren.hasMoreElements(); ) {
                MutableTreeNode         nodeChild   = (MutableTreeNode) enmChildren.nextElement();

                // Add the child node to the list add create a new path to test
                lstNodeStack.add(nodeChild);

                // Search the new test path - and return if the node is found 
                if ( this.searchPathRecursively(lstNodeStack, nodeDev) )
                    return true;
                
                // else, the search came up empty and we remove the current child and move to the next  
                lstNodeStack.remove(nodeChild);
            }

            // We went through all the children and did not find a match
            return false;
        }

//        /**
//         * This function implements a recursive search and the calling method must provide the list of 
//         * tree nodes used for intermediate data storage.  In the initial call the list contains the root
//         * node of the tree.
//         * If this method returns a <code>null</code> value then something went wrong - likely the
//         * given tree node is bad.         
//         * 
//         * @param pthCurrent    Current construction of the tree path between the root and the target element
//         * @param lstNodeStack  Stack of tree nodes to the current search location within the tree, contains the root
//         *                      node on the bottom of the stack and the current node on the top
//         * @param nodeDev       The target element, the end of the tree path
//         * @return
//         *
//         * @author Christopher K. Allen
//         * @since  Jul 9, 2012
//         */
//        @SuppressWarnings("unchecked")
//        private TreePath    searchPathRecursively(TreePath pthCurrent, List<MutableTreeNode> lstNodeStack, MutableTreeNode nodeDev) {
//
//            // Get the last node in the list - the parent for this iteration
//            MutableTreeNode nodeParent = lstNodeStack.get( lstNodeStack.size() - 1 );
//
//            // For each child node in the parent node...
//            for (Enumeration<TreeNode> enmChildren = nodeParent.children(); enmChildren.hasMoreElements(); ) {
//                MutableTreeNode         nodeChild   = (MutableTreeNode) enmChildren.nextElement();
//
//                // Add the child node to the list add create a new path to test
//                lstNodeStack.add(nodeChild);
//
//                // If the child is the target node, we have found the correct path
//                if (nodeChild.equals(nodeDev)) {
//                    Object[]    arrObj = lstNodeStack.toArray();
//                    TreePath    pthDev = new TreePath(arrObj);
//
//                    return pthDev;
//
//                }
//
//                // if not, search the new test path - remove the child if the search came up empty and returned
//                this.searchPathRecursively(pthCurrent, lstNodeStack, nodeDev);
//                lstNodeStack.remove(nodeChild);
//            }
//
//            // Something went wrong, we should not have exited the search loop if the node was in the tree
//            return null;
//        }
        
        /**
         * Build the visible user interface.
         *
         * @since  Aug 26, 2009
         * @author Christopher K. Allen
         */
        private void buildViewPane() {

            // Tree scroll pane
            this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        }
        
    }
        
        
    
    /**
     * Displays the table of selected accelerator
     * nodes and maintains a list of selected nodes
     * for later retrieval.
     *
     * @since  Aug 26, 2009
     * @author Christopher K. Allen
     */
    private class TablePane extends JScrollPane {
    
        /**
         * Renders the color object on the table's key color column.  This cell renderer class
         * only knows how to render colors.
         *
         * @author Christopher K. Allen
         * @since   Aug 23, 2012
         */
        private class TableColorKeyRenderer implements TableCellRenderer {

//            /** Serialization version */
//            private static final long serialVersionUID = 1L;

            
            /**
             * Creates a renderer object for the given table value object.  The value object is expected to be
             * of the <code>Color</code> class.
             * 
             * @return      current the method returns an embellished <code>JPanel</code> object
             *  
             * @since Aug 23, 2012
             * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
             */
            @Override
            public Component getTableCellRendererComponent(JTable tblSrc, Object objVal, boolean bolSeld, boolean bolFocus, int iRow, int iCol) {
                
                Color   clrCell = (Color)objVal;

                JPanel  pnlCell = new JPanel();
                pnlCell.setBackground(clrCell);
                pnlCell.setForeground(clrCell);
                pnlCell.setOpaque(true);
                pnlCell.setBorder(new BevelBorder(BevelBorder.RAISED));
                
                return pnlCell;
            }
        }
        
        /**
         * The table model class for the <code>JTable</code>
         * component of the <code>TablePane</code> class.
         *
         * @since  Aug 24, 2009
         * @author Christopher K. Allen
         */
        private class TableController extends AbstractTableModel {
        
            
            /*
             * Global Constants
             */
            
            /**  Serialization Version */
            private static final long serialVersionUID = 1L;
        
            /** Column headers */
            private final String ARR_STR_HEADERS[] = {
                                                     "Device ID",
                                                     "Status",
                                                     "Key"
                                                      };
            /*
             * Global Attributes
             */
            
            /** Random number generator used for color map */
            private final Random                    RND_COLOR = new Random();
            
            
            
            /*
             * Instance Attributes
             */
            
            /** flag for displaying the device color column (used for legends) */
            private boolean                           bolColorKeyed;
            
            
            /** status condition of hardware */
            private final ArrayList<Boolean>          lstHwareStatus;
            
            /** the list of selected hardware */
            private final ArrayList<AcceleratorNode>   lstHwareNodes;
            
            /** the color map of devices to the legend color keys */
            private final Map<AcceleratorNode, Color>   mapDevClr;
            
            /** the color map of device ids to the legend color keys */
            private final Map<String, Color>            mapDevIdClr;
            
            
            
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
            public TableController() {
                super();
                this.bolColorKeyed  = false;
                
                this.lstHwareNodes  = new ArrayList<AcceleratorNode>();
                this.lstHwareStatus = new ArrayList<Boolean>();
                this.mapDevClr      = new HashMap<AcceleratorNode, Color>();
                this.mapDevIdClr    = new HashMap<String, Color>();
            }
        
            /**
             * Enable the color column for the device.
             *
             * @param bolColorKeyed     color column is displayed when <code>true</code>
             *                          and disabled when <code>false</code>
             *
             * @author Christopher K. Allen
             * @since  Aug 21, 2012
             */
            public void setColorKeyed(boolean bolColorKeyed) {
                this.bolColorKeyed = bolColorKeyed;
                this.fireTableStructureChanged();
            }
        
            
            /*
             * Operations
             */
        
            /**
             * Return the color key map for the current set of accelerator nodes.  Note that
             * this map is permanent, but not immutable.  If the selection list of nodes changes
             * then the map is affected accordingly.  Thus, objects monitoring the node selections
             * need only request this map once.
             *  
             * @return  map of (node,legend color) pairs
             *
             * @author Christopher K. Allen
             * @since  Aug 22, 2012
             */
            public final Map<AcceleratorNode, Color>  getDeviceColorMap() {
                return this.mapDevClr;
            }
            
            /**
             * Return the color key map for the current set of accelerator nodes by their string
             * IDs.  Note that
             * this map is permanent, but not immutable.  If the selection list of nodes changes
             * then the map is affected accordingly.  Thus, objects monitoring the node selections
             * need only request this map once.
             *  
             * @return  map of (node,legend color) pairs
             *
             * @author Christopher K. Allen
             * @since  Aug 24, 2012
             */
            public final Map<String, Color> getDeviceIdColorMap() {
                return this.mapDevIdClr;
            }
            
            /**
             * Removes all the table data.
             *
             * 
             * @since  Aug 24, 2009
             * @author Christopher K. Allen
             */
            public void clearTable() {
                this.lstHwareNodes.clear();
                this.lstHwareStatus.clear();
//                this.mapDevClr.clear();
//                this.mapDevIdClr.clear();
                this.fireTableDataChanged();
            }
        
            /**
             * Set the list of displayed <code>AcceleratorNode</code>
             * objects.
             *
             * @param lstNodes  ordered accelerator node list
             * 
             * @since  Aug 24, 2009
             * @author Christopher K. Allen
             */
            public void setNodeList(List<AcceleratorNode> lstNodes) {
                this.clearTable();
        
                for (AcceleratorNode node : lstNodes) {
                    this.lstHwareNodes.add(node);
                    this.lstHwareStatus.add( node.getStatus() );
                    
                    this.addNodeColor( node );
                }
                this.fireTableDataChanged();
            }
            
        
            
            /*
             * AbstractTableModel Overrides
             */
            
            /**
             * Get the class type of the column with 
             * index <arg>iCol</arg>.
             * 
             * @param  iCol index of target table column
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
                    return Boolean.class;
                    
                case 2:
                    return Color.class;
                }

                return super.getColumnClass(iCol);
            }
        
           /**
            * Returns the header of table column with index
            * <arg>iCol</arg>.
            * 
            * @param  iCol index of target table column
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
//               return super.isCellEditable(iRow, iCol);
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
             * @since 	Aug 24, 2009
             * @author  Christopher K. Allen
             *
             * @see javax.swing.table.TableModel#getColumnCount()
             */
            public int getColumnCount() {
                if (this.bolColorKeyed)
                    return ARR_STR_HEADERS.length;
                
                return ARR_STR_HEADERS.length - 1;
            }
        
            /**
             *
             * @since 	Aug 24, 2009
             * @author  Christopher K. Allen
             *
             * @see javax.swing.table.TableModel#getRowCount()
             */
            public int getRowCount() {
                return this.lstHwareNodes.size();
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
             * @since 	Aug 24, 2009
             * @author  Christopher K. Allen
             *
             * @see javax.swing.table.TableModel#getValueAt(int, int)
             */
            public Object getValueAt(int iRow, int iCol) {
        
                switch (iCol) {
                
                case 0:
                    AcceleratorNode smfNode = this.lstHwareNodes.get(iRow);
                    return smfNode;
                    
                case 1:
                    Boolean bolStatus = this.lstHwareStatus.get(iRow);
                    return bolStatus;
                    
                case 2:
                    AcceleratorNode smfKey  = this.lstHwareNodes.get(iRow);
                    Color           clrVal  = this.mapDevClr.get(smfKey);
                    return clrVal;
                    
                default:
                    return null;
                }
            }
            
            /*
             * Support Methods
             */
            
            /**
             * Creates a color for the given node in the map of (node,key color) 
             * pairs.  If the node already has a color nothing is done.
             *
             * @param smfNode   node to receive a color in the color map
             *
             * @author Christopher K. Allen
             * @since  Aug 23, 2012
             */
            private void addNodeColor(AcceleratorNode smfNode) {
                
                // Check if we have already created the color for this node
                Color   clrNode = this.mapDevClr.get(smfNode);
                if (clrNode != null)
                    return;
                
                // The node needs a color - create a random color
                clrNode = new Color( RND_COLOR.nextInt(255), RND_COLOR.nextInt(255), RND_COLOR.nextInt(255) );
                this.mapDevClr.put(smfNode, clrNode); 
                this.mapDevIdClr.put(smfNode.getId(), clrNode);
            }
            
        }       // End: Table Model Controller


        

        /*
         * Global Constants
         */
        
        /**  Serialization Version */
        private static final long serialVersionUID = 1L;
    
    
        
        /*
         * Instance Attributes
         */
        
        /** The selection table */
        private final JTable                      tblAccel;
        
        /** The custom selection table controller */
        private final TableController              mdlCtrlr;
    
        
        
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
        public TablePane() {
            super();
    
            this.mdlCtrlr = new TableController();
            this.tblAccel = new JTable( this.mdlCtrlr );
            
            this.setViewportView( this.tblAccel );
            this.tblAccel.setDefaultRenderer(Color.class, new TableColorKeyRenderer());
            this.buildViewPane();
        }
        
        /**
         * Display the color column for the device table.  This color can be used
         * for creating legends (for example, on plots).  To be used in conjunction
         * with <code></code>.
         *
         * @param bolColorKeyed     color column is displayed when <code>true</code>
         *                          and disabled when <code>false</code>
         *
         * @author Christopher K. Allen
         * @since  Aug 22, 2012
         * 
         * @see     TableController#setColorKeyed(boolean)
         */
        public void setColorKeyed(boolean bolColorKeyed) {
            this.mdlCtrlr.setColorKeyed(bolColorKeyed);
            this.tblAccel.getColumnModel().getColumn(2).setPreferredWidth(2);
        }
        
        
        /*
         * Operations
         */
        
        /**
         *
         * Return the color key map for the current set of selected accelerator nodes.  Note that
         * this map is permanent, but not immutable.  If the selection list of nodes changes
         * then the map is affected accordingly.  Thus, objects monitoring the node selections
         * need only as for this map once.
         *  
         * @return  map of (node,legend color) pairs
         *
         * @author Christopher K. Allen
         * @since  Aug 22, 2012
         * 
         * @see TableController#getDeviceColorMap()
         */
        public final Map<AcceleratorNode, Color> getDeviceColorMap() {
            return this.mdlCtrlr.getDeviceColorMap();
        }
        
        public final Map<String, Color>     getDeviceIdColorMap() {
            return this.mdlCtrlr.getDeviceIdColorMap();
        }
        
        /**
         * Clears the contents of the device
         * table.
         *
         * 
         * @since  Nov 18, 2009
         * @author Christopher K. Allen
         */
        public void clearTable() {
            this.mdlCtrlr.clearTable();
        }
        
        /**
         * Sets the ordered list of accelerator nodes that
         * are displayed in the table.
         * This method is actually a proxy to the same-named 
         * method of the contained table model.   
         *
         * @param lstNodes      ordered list of accelerator nodes
         * 
         * @since  Aug 26, 2009
         * @author Christopher K. Allen
         * 
         * @see TableController#setNodeList(List)
         */
        public void setNodeList(List<AcceleratorNode> lstNodes) {
            this.mdlCtrlr.setNodeList(lstNodes);
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
    
            // Table scroll pane
            this.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            this.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            // Table properties
            int szDevIdColWd = AppProperties.DEVSEL.DEVIDCOL_WD.getValue().asInteger();
            this.tblAccel.getColumnModel().getColumn(0).setPreferredWidth(szDevIdColWd);
            this.tblAccel.getColumnModel().getColumn(1).setPreferredWidth(2);
            
            this.tblAccel.setRowSelectionAllowed(false);
            this.tblAccel.setColumnSelectionAllowed(false);
        }
        
    }



    /*
     * Global Constant
     */
    
    /**  Serialization Version */
    private static final long serialVersionUID = 1L;

    
    
    /*
     * Instance Attribute
     */
    
    /** Single selection mode flag */
    private boolean                     bolSngSelect;
    
//    /** Display selected devices table visibility flag */
//    private boolean                     bolTblVisible;
    

    /** The associated XAL accelerator object */
    private Accelerator                                 smfAccel;
    
    /** Array of valid profile device types */
    private final Class<?>[]                            arrDevTypes;

    /** List of currently selected devices */
    private final List<AcceleratorNode>                 lstSelNodes;
    
    /** List of objects registered for device selection events */
    private final List<IDeviceSelectionListener>        lstSelHndlrs;
    
    
    /*
     * GUI Components
     */
    
    /** Scroll pane for tree view port */
    private TreePane                 paneTreeScroll;

    /** The Scroll pane for the table view port */
    private TablePane                 paneTblScroll;
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>DeviceTreePanel</code> object which
     * displays SMF accelerator hardware of the given types. 
     *
     * @param winMain           Main window of the application, provides accelerator
     * @param arrDevTypes       array of hardware types to display
     *
     * @since     Aug 19, 2009
     * @author    Christopher K. Allen
     */
    @SafeVarargs
    public DeviceSelectorPanel(MainWindow winMain, Class<? extends AcceleratorNode>... arrDevTypes) {
        this(winMain.getDocument().getAccelerator(), arrDevTypes);
//        this.bolSngSelect = false;
//        this.bolTblVisible = true;
//        
//        this.smfAccel     = winMain.getDocument().getAccelerator();
//        this.arrDevTypes  = arrDevTypes;
//
//        this.lstSelNodes = new LinkedList<AcceleratorNode>();
//        this.lstSelHndlrs = new LinkedList<IDeviceSelectionListener>();
//        
//        this.buildGuiComponents();
//        this.layoutGuiComponents();
    }

    /**
     * Create a new <code>DeviceTreePanel</code> object which
     * displays SMF accelerator hardware of the given types. 
     *
     * @param smfAccel          we choose devices from this accelerator
     * @param arrDevTypes       array of hardware types to display
     *
     * @since     Aug 19, 2009
     * @author    Christopher K. Allen
     */
    public DeviceSelectorPanel(Accelerator smfAccel, Class<?>... arrDevTypes) {
        this.bolSngSelect = false;
//        this.bolTblVisible = true;
        
        this.smfAccel     = smfAccel;
        this.arrDevTypes  = arrDevTypes;

        this.lstSelNodes = new LinkedList<AcceleratorNode>();
        this.lstSelHndlrs = new LinkedList<IDeviceSelectionListener>();
        
        this.buildGuiComponents();
        this.layoutGuiComponents();
    }

    /**
     * (Re)set the accelerator object from 
     * which the device nodes are selected.
     *
     * @param smfAccel  target accelerator 
     * 
     * @since  Aug 20, 2009
     * @author Christopher K. Allen
     */
    public void resetAccelerator(Accelerator smfAccel) {
        this.smfAccel = smfAccel;
        this.paneTreeScroll.resetAccelerator(smfAccel);
        this.paneTreeScroll.addTreeSelectionListener(this);
        this.paneTblScroll.clearTable();
    }
    
    /**
     * Register as a device selection event listener.
     *
     * @param snkDevSelHndlr    object to register event notifications
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public void registerDeviceSelectedListener(IDeviceSelectionListener snkDevSelHndlr) {
        this.lstSelHndlrs.add(snkDevSelHndlr);
    }
    
    /**
     * Configure the device selection panel so that only
     * one device may be selected at a time.
     *
     * @param bolSngSelect      switch to single selection mode if true,
     *                          multiple device selection mode if false
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    public void setSingleSelectionMode(boolean bolSngSelect) {
        this.paneTreeScroll.setSingleSelectionMode(bolSngSelect);
        this.bolSngSelect = bolSngSelect;
    }

    /**
     * Enable the selected device table making it visible
     * to the users. 
     *
     * @param bolTblVisible     make the table visible if <code>true</code>
     * 
     * @since  Apr 22, 2010
     * @author Christopher K. Allen
     */
    public void setDeviceTableVisible(boolean bolTblVisible) {
//        this.bolTblVisible = bolTblVisible;
        this.paneTblScroll.setVisible(bolTblVisible);
    }
    
    /**
     * Displays an additional column in the device table as a legend
     * key color.  The key map of device/color pairs can then be requested 
     * for creating legends.
     *
     * @param bolColorKeyed   create the extra color column if <code>true</code>,
     *                      remove the column if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Aug 21, 2012
     */
    public void setDeviceTableColorKeyed(boolean bolColorKeyed) {
        this.paneTblScroll.setColorKeyed(bolColorKeyed);
    }

    
    /*
     * Properties and States
     */
    
    /**
     * Returns the list of valid hardware devices
     * that are currently selected by the user.
     *
     * @return the list of currently selected devices. 
     *
     * @since  Sep 11, 2009
     * @author Christopher K. Allen
     */
    public List<AcceleratorNode> getSelectedDevices() {
        return this.lstSelNodes;
    }
    
    /**
     * Convenience method for returning the single selected device
     * when only one is expected, that is, when the device selection panel
     * is in single selection mode.  Calling this method in multi-selection
     * mode will return the first selected device. 
     *  
     * @return  the single selected device whenever the panel is in single selction mode,
     *          returns <code>null</code> if nothing is selected.
     *
     * @author Christopher K. Allen
     * @since  Oct 6, 2014
     */
    public AcceleratorNode      getSelectedDevice() {
        if (this.lstSelNodes.size() > 0)
            return this.lstSelNodes.get(0);
        else
            return null;
    }

    /**
     * Returns the device color map used to create legends for the
     * devices (for example, a plot).
     *
     * @return  the map of (device,color) pairs used on the device table
     *
     * @author Christopher K. Allen
     * @since  Aug 22, 2012
     */
    public Map<AcceleratorNode, Color> getDeviceColorMap() {
        return this.paneTblScroll.getDeviceColorMap();
    }
    
    /**
     * Returns the device color map used to create legends for the
     * devices (for example, a plot).  The map is keyed by the 
     * unique device ID rather than the device itself.
     *
     * @return  the map of (device ID,color) pairs used on the device table
     *
     * @author Christopher K. Allen
     * @since  Aug 24, 2012
     */
    public Map<String, Color> getDeviceIdColorMap() {
        return this.paneTblScroll.getDeviceIdColorMap();
    }
    
    /*
     * Operations
     */
    
    /**
     * Expands the path to the given accelerator device in the tree.  This method is 
     * just a proxy to the method <code>TreePane{@link #setDeviceSelected(String)}</code>.
     *
     * @param strDevId  ID of accelerator device to display as selected within the tree
     * 
     * @return          <code>true</code> if the device was present and selected,
     *                  <code>false</code> otherwise
     *
     * @author Christopher K. Allen
     * @since  Apr 30, 2012
     */
    public boolean  setDeviceSelected(String strDevId) {
        return this.paneTreeScroll.setDeviceSelected(strDevId);
    }
    
    /**
     * Expands the displayed tree paths to each device whose identifier is contained
     * in the given collection of device identifiers.  This method makes repeated calls
     * to the method <code>{@link #setDeviceSelected(String)}</code> and collects
     * the device identifiers where the path expansion failed.  The collection of 
     * failed devices is then returned.   
     *
     * @param setDevIds     collection of device identifiers
     * 
     * @return              collection of identifiers for devices whose tree path successfully expanded,
     *                      any others in the argument collection were probably not found in the tree display 
     *
     * @author Christopher K. Allen
     * @since  Jun 19, 2012
     */
    public Collection<String> setDevicesSelected(Collection<String> setDevIds) {
        Set<String>     setDevSuccess = new TreeSet<String>();
        
        for (String strDevId : setDevIds) 
            if (this.setDeviceSelected(strDevId) == true) 
                setDevSuccess.add(strDevId);
            
        return setDevSuccess;
    }
    
    /**
     * Clears all the selections in the device selection display.
     *
     * @author Christopher K. Allen
     * @since  Jul 10, 2012
     */
    public void clearSelections() {
        this.paneTreeScroll.clearTree();
        this.paneTblScroll.clearTable();
        this.lstSelNodes.clear();
    }

    
    /*
     * TreeSelectionListener Interface
     */
    
    /**
     * Response to the user selecting an element of the tree.
     *
     * @since   Aug 24, 2009
     * @author  Christopher K. Allen
     *
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent e) {

        // Clear the currently selected devices
        this.lstSelNodes.clear();       // this is a bad idea - we should use new instances
        this.paneTblScroll.clearTable();
        
        // If single selection, check if we have valid one 
        if (this.bolSngSelect) {
            
            Object      objLeaf = this.paneTreeScroll.getSelectedPathComponent();
            if (!(objLeaf instanceof DefaultMutableTreeNode)) {
                this.notifyDevSelListeners();
                return;
            }
            
            DefaultMutableTreeNode  nodeLeaf = (DefaultMutableTreeNode)objLeaf;
            if (!(nodeLeaf.isLeaf()) ) { 
                this.notifyDevSelListeners();
                return;
            }
            
            this.addValidNodesInBranch(this.lstSelNodes, nodeLeaf);
            this.paneTblScroll.setNodeList(this.lstSelNodes);
            this.notifyDevSelListeners();
            return;
        }

        // Else do multiple selections
        //  Search each selected tree path for valid device nodes
        TreePath[] tpSelected = this.paneTreeScroll.getSelectionPaths();

        for (TreePath path : tpSelected) {
            Object      objLeaf = path.getLastPathComponent();
            
            if ( objLeaf instanceof DefaultMutableTreeNode ) { 
                DefaultMutableTreeNode  nodeLeaf = (DefaultMutableTreeNode)objLeaf;
                this.addValidNodesInBranch(this.lstSelNodes, nodeLeaf);
            }
        }
        
        this.paneTblScroll.setNodeList(this.lstSelNodes);
        this.notifyDevSelListeners();
    }
    
    

//    /*
//     * IDocumentView Interface
//     */
//    
//    
//    /**
//     *  Responds to a change in the application's central
//     *  data. 
//     *  
//     * @param   docMain        the application data document 
//     *  
//     * @since 	Nov 12, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.IDocView#updateAccelerator(xal.app.pta.MainDocument)
//     */
//    public void updateAccelerator(MainDocument docMain) {
//        this.resetAccelerator(docMain.getAccelerator());
//    }
//    
//    /**
//     * Responds to a new device selection event.  Sets
//     * the selected lists of devices.
//     *
//     * @since   Nov 17, 2009
//     * @author  Christopher K. Allen
//     *
//     * @see xal.app.pta.view.IDocView#updateDevices(xal.app.pta.MainDocument)
//     */
//    @Override
//    public void updateDevices(MainDocument docMain) {
//
//    }
//

    
    
    /*
     * Support Methods
     */
    
    /**
     * Build the visible user interface.
     *
     * 
     * @since  Aug 20, 2009
     * @author Christopher K. Allen
     */
    private void buildGuiComponents() {
        
        
        // Create tree scroll pane
        this.paneTreeScroll = new TreePane( this.smfAccel );
        this.paneTreeScroll.addTreeSelectionListener( this );

//        int szTreeWd = AppProperties.DEVSEL.TREE_WD.getValue().asInteger();
//        int szTreeHt = AppProperties.DEVSEL.TREE_HT.getValue().asInteger();
//        Dimension       dimTreePane = new Dimension(szTreeWd, szTreeHt);
//        this.paneTreeScroll.setPreferredSize(dimTreePane);


        // Create table scroll pane
        this.paneTblScroll  = new TablePane();

//        int szTblWd = AppProperties.DEVSEL.TABLE_WD.getValue().asInteger();
//        int szTblHt = AppProperties.DEVSEL.TABLE_HT.getValue().asInteger();
//        Dimension       dimTblPane = new Dimension(szTblWd, szTblHt);
//        this.paneTblScroll.setPreferredSize(dimTblPane);

        
//        // Put it all together
//        this.setLayout( new GridBagLayout() );
//        GridBagConstraints gbc = new GridBagConstraints(); 
//        
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
//        gbc.fill  = GridBagConstraints.BOTH;
//        gbc.anchor = GridBagConstraints.LINE_START;
//        this.add(this.paneTreeScroll, gbc);
//
////        Component       cmpPad = Box.createHorizontalStrut(5);
////        gbc.gridx = 1;
////        gbc.gridy = 0;
////        gbc.fill  = GridBagConstraints.NONE;
////        gbc.anchor = GridBagConstraints.CENTER;
////        this.add(cmpPad, gbc);
//
//        gbc.gridx = 1;
//        gbc.gridy = 0;
//        gbc.weightx = 1.0;
//        gbc.weighty = 1.0;
//        gbc.fill  = GridBagConstraints.BOTH;
//        gbc.anchor = GridBagConstraints.LINE_END;
//        this.add(this.paneTblScroll, gbc);
//        
////        Box     boxHor = Box.createHorizontalBox();
////        boxHor.add(this.paneTreeScroll);
////        boxHor.add(Box.createHorizontalStrut(5));
////        boxHor.add(this.paneTblScroll);
//        
////        this.add( this.paneTreeScroll );
////        this.add( Box.createHorizontalStrut(10) );
////        this.add( this.paneTblScroll );
////        this.add(boxHor);
    }
    
    /**
     * Arrange the GUI components on the GUI
     * face.
     *
     * 
     * @since  May 14, 2010
     * @author Christopher K. Allen
     */
    private void layoutGuiComponents() {
        // Put it all together
        this.setLayout( new GridBagLayout() );
        GridBagConstraints gbc = new GridBagConstraints(); 
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.fill  = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_START;
        this.add(this.paneTreeScroll, gbc);

//        Component       cmpPad = Box.createHorizontalStrut(5);
//        gbc.gridx = 1;
//        gbc.gridy = 0;
//        gbc.fill  = GridBagConstraints.NONE;
//        gbc.anchor = GridBagConstraints.CENTER;
//        this.add(cmpPad, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 0.1;
        gbc.fill  = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.LINE_END;
        this.add(this.paneTblScroll, gbc);
        
//        Box     boxHor = Box.createHorizontalBox();
//        boxHor.add(this.paneTreeScroll);
//        boxHor.add(Box.createHorizontalStrut(5));
//        boxHor.add(this.paneTblScroll);
        
//        this.add( this.paneTreeScroll );
//        this.add( Box.createHorizontalStrut(10) );
//        this.add( this.paneTblScroll );
//        this.add(boxHor);
    }

    
    /**
     * <p>
     * Searches the tree structure under the given branch node
     * for all the valid nodes 
     * (see <code>{@link #validDevice(AcceleratorNode)}</code>)
     * and add them to the given node list.
     * </p>
     * <p>
     * This is a recursive method which first checks the current 
     * tree node (<var>nodeBranch</var>) to see if it's user
     * object is a valid device node, then calls itself on
     * all the child nodes attached to the given node.
     * </p>
     *
     * @param lstSmfNodes       list of valid device nodes (i.e., being filled)
     * @param nodeBranch        (current) branch node of tree being searched
     * 
     * @since  Sep 11, 2009
     * @author Christopher K. Allen
     * 
     * @see DeviceSelectorPanel#validDevice(AcceleratorNode)
     */
    private void addValidNodesInBranch(List<AcceleratorNode> lstSmfNodes, DefaultMutableTreeNode nodeBranch) {
        // Check if this node corresponds to an accelerator node and add it if so
        Object objSmf = nodeBranch.getUserObject();
        if (objSmf instanceof AcceleratorNode) {
            AcceleratorNode nodeSmf = (AcceleratorNode)objSmf;
            if (this.validDevice(nodeSmf))
                lstSmfNodes.add(nodeSmf);
        }
        
        // Get all the children
        Enumeration<?> enmChildren = nodeBranch.children();
        while (enmChildren.hasMoreElements()) {
            Object objChild = enmChildren.nextElement();
            
            if (objChild instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode nodeChild = (DefaultMutableTreeNode)objChild;
                
                this.addValidNodesInBranch(lstSmfNodes, nodeChild);
            }
        }
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
        for (Class<?> typeValidDev : this.arrDevTypes) {
            if ( typeValidDev.isInstance(nodeDev) )
                return true;
        }
        
        return false;
    }
    
    /**
     * Call the <code>IDeviceSelectionListener#newDeviceSelection(List<AcceleratorNode>)</code>
     * of all register device selection listeners.
     * 
     * @since  Nov 13, 2009
     * @author Christopher K. Allen
     */
    private void notifyDevSelListeners() {
        for (IDeviceSelectionListener hndlr : this.lstSelHndlrs) {
            hndlr.newDeviceSelection(this.getSelectedDevices());
        }
    }
    
}

