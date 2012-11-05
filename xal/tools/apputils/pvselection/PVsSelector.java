/*
 * @(#)PVSelector.java          0.1 04/30/2003
 *
 * Copyright (c) 2001-2002 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.tools.apputils.pvselection;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.io.*;
import java.net.*;

import xal.ca.*;
import xal.ca.view.*;
import xal.smf.data.*;
import xal.smf.*;

/** 
 * PVSelector is a GUI component creating a tree-like PV selection and shows  
 * the selected PVs in a tree-like structure.
 */

public class PVsSelector extends JPanel{
	private static final long serialVersionUID = 0L;

    /** name for this PV selector - use it to differentiate PV sources
     * in the case you use more than one PV selector
     */
    private String myLabel;

    private PVTreeNode pvRoot = null;
    private PVsTreePanel pvTreePanel = null;

    /** the JTree to use for displaying the accelerator */
    private JTree tree;

    /** the tree node to build a JTree from */
    private XALTreeNode xalTree;

    /** scrollpane to hold the Tree selector */
    private JScrollPane scrollPane = new JScrollPane();

    /** the tree cell render */
    private SignalCellRenderer signalCellRenderer = new SignalCellRenderer();

    private JButton setPVButton      = new JButton("Add =>");
    private JButton removePVButton   = new JButton("Remove =>");
    private JRadioButton rawPVButton = new JRadioButton("Raw PV's name: ",false);
    private JTextField pvNameJText   = new JTextField(new ChannelNameDocument(),"",40);
    private JTextField messageJText  = new JTextField(60);

    //Add action listener
    private  java.awt.event.ActionListener addPVActionListener = null;

    //upper panel to provide ability to remove the message text field
    private JPanel uppPanel =  new JPanel();

    private Font fnt = null;

    private HandleNode selectedHandleNode = null;

    /** The constructor. Just creates objects we need internally.
     */
    public PVsSelector(){
	this(new PVTreeNode("ROOT"));
	PVTreeNode root = getPVNodeRoot();
        PVTreeNode pvs  = new PVTreeNode("List of PV names :");
	pvs.setPVNamesAllowed(true);
        root.add(pvs);
        ((DefaultTreeModel) pvTreePanel.getJTree().getModel()).reload();
    }

    /** The constructor. Just creates objects we need internally.
     * @param pvRootIn - the root node of the pv's tree
     */
    public PVsSelector(PVTreeNode pvRootIn) {
	pvRoot = pvRootIn;
	pvTreePanel = new PVsTreePanel(pvRoot);
	pvTreePanel.setEditMode();

	fnt = getFont();

	messageJText.setEditable(false);
	messageJText.setForeground(Color.red);

        pvNameJText.setEditable(false);

	setPVButton.setHorizontalAlignment(JButton.RIGHT);
	removePVButton.setHorizontalAlignment(JButton.RIGHT);

	// line up the labels, buttons and panels
	setLayout(new BorderLayout());

	//upper panel
        uppPanel.setLayout(new BorderLayout());
        uppPanel.add(messageJText,BorderLayout.NORTH);
        uppPanel.add(rawPVButton,BorderLayout.WEST);
        uppPanel.add(pvNameJText,BorderLayout.CENTER);

        //central panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(0,2,0,0));

        JPanel centerLeftPanel = new JPanel();
        centerLeftPanel.setLayout(new BorderLayout());
        centerLeftPanel.add(pvTreePanel,BorderLayout.CENTER);

        JPanel tmp = new JPanel();
        tmp.setLayout(new BorderLayout());
        tmp.add(setPVButton,BorderLayout.NORTH);
        tmp.add(removePVButton,BorderLayout.SOUTH);

        JPanel tmp_1 = new JPanel();
        tmp_1.setLayout(new BorderLayout());
        tmp_1.add(tmp,BorderLayout.NORTH);

        JPanel tmp_2 = new JPanel();
        tmp_2.setLayout(new BorderLayout());
        tmp_2.add(tmp_1,BorderLayout.EAST);

        JPanel tmp_3 = new JPanel();
        tmp_3.setLayout(new FlowLayout(FlowLayout.CENTER,0,0));
        tmp_3.add(tmp_2);

        centerLeftPanel.add(tmp_3,BorderLayout.WEST);

        centerPanel.add(scrollPane);
        centerPanel.add(centerLeftPanel);
   
	//finishing the panel
        add(uppPanel,BorderLayout.NORTH);
        add(centerPanel,BorderLayout.CENTER);

	//buttons tool tips
        setPVButton.setToolTipText("set PV name into the left PV tree");
        removePVButton.setToolTipText("delete PV name from the left PV tree");

	//button action listener definition
	addPVActionListener = new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showMessage(null);
                    PVTreeNode pvNode = PVTreeNode.getSelectedPVTreeNode(pvRoot);
                    if(pvNode == null){
			showMessage("Please, select a right place for new PV in the right tree!");
			return;                      
		    }
		    if(rawPVButton.isSelected()){
			String pv_name = pvNameJText.getText();
			if(pv_name.length() == 0){
			    showMessage("PV name field is empty.");
			    return;
			} 
			if(pvNode.isPVNamesAllowed() && !pvNode.isPVName()){
			    //check that it is not maximal number of PVs
			    if(pvNode.getPVNumberLimit() <= pvNode.getChildCount()){
				showMessage("This container can include only n="+
					    pvNode.getPVNumberLimit()+" PVs and you have n="+
					    pvNode.getLeafCount());
				return;                     
			    }
			    //create new node
			    PVTreeNode pvNodeNew = new PVTreeNode(pv_name);
			    Channel channel = ChannelFactory.defaultFactory().getChannel(pv_name);
                            pvNodeNew.setChannel(channel);
			    pvNodeNew.setAsPVName(true);
			    pvNodeNew.setCheckBoxVisible(pvNode.isCheckBoxVisible());
			    pvNode.add(pvNodeNew);
			    JTree pvTree = pvTreePanel.getJTree();
			    DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
			    treeModel.reload(pvNode);
			    pvNodeNew.setSwitchedOnOffListener(pvNode.getSwitchedOnOffListener());
			    pvNodeNew.setCreateRemoveListener(pvNode.getCreateRemoveListener());
			    pvNodeNew.setRenameListener(pvNode.getRenameListener());
			    pvNodeNew.creatingOccurred();
			    pvTree.scrollPathToVisible(new TreePath(pvNodeNew.getPath()));
			    return;
			}
			if(pvNode.isPVName()){
			    //change pv name for existing node
			    pvNode.setColor(null);
			    pvNode.setCheckBoxVisible(((PVTreeNode) pvNode.getParent()).isCheckBoxVisible());
			    Channel channel = ChannelFactory.defaultFactory().getChannel(pv_name);
                            pvNode.setChannel(channel);
			    pvNode.setName(pv_name);
			    JTree pvTree = pvTreePanel.getJTree();
			    DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
			    treeModel.reload(pvNode.getParent());                        
			    return;
			}
		    }
		    else{
			//raw PV button is not selected
			if(pvNode.isPVNamesAllowed() && !pvNode.isPVName()){
			    //check that it is not maximal number of PVs
			    if(pvNode.getPVNumberLimit() <= pvNode.getChildCount()){
				showMessage("This container can include only n="+
					    pvNode.getPVNumberLimit()+" PVs and you have n="+
					    pvNode.getLeafCount());
				return;                     
			    }
			    if(selectedHandleNode == null){
				showMessage("You did not select PV handler in the left accelerator tree.");
				return;
			    }
			    //create new node
			    PVTreeNode pvNodeNew = new PVTreeNode(selectedHandleNode.getSignalName());
                            pvNodeNew.setChannel(selectedHandleNode.getChannel());
			    pvNodeNew.setAsPVName(true);
			    pvNodeNew.setCheckBoxVisible(pvNode.isCheckBoxVisible());
			    pvNode.add(pvNodeNew);
			    JTree pvTree = pvTreePanel.getJTree();
			    DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
			    treeModel.reload(pvNode);
			    pvNodeNew.setSwitchedOnOffListener(pvNode.getSwitchedOnOffListener());
			    pvNodeNew.setCreateRemoveListener(pvNode.getCreateRemoveListener());
			    pvNodeNew.setRenameListener(pvNode.getRenameListener());
			    pvNodeNew.creatingOccurred();
			    pvTree.scrollPathToVisible(new TreePath(pvNodeNew.getPath()));
			    return;
			}
			if(pvNode.isPVName()){
			    //change pv name for existing node
			    pvNode.setColor(null);
			    pvNode.setCheckBoxVisible(((PVTreeNode) pvNode.getParent()).isCheckBoxVisible());
                            pvNode.setChannel(selectedHandleNode.getChannel());
			    pvNode.setName(selectedHandleNode.getSignalName());
			    JTree pvTree = pvTreePanel.getJTree();
			    DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
			    treeModel.reload(pvNode.getParent());                        
			    return;
			}
                        
		    }
		}
	    };

	setPVButton.addActionListener(addPVActionListener);

        pvNameJText.addActionListener(addPVActionListener);

	removePVButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showMessage(null);
                    PVTreeNode pvNode = PVTreeNode.getSelectedPVTreeNode(pvRoot);
                    if(pvNode == null || !pvNode.isPVName()){
			showMessage("Please, select PV in the right tree! You can delete PV only!");
			return;                      
		    }

                    PVTreeNode pvNode_Parent = (PVTreeNode) pvNode.getParent();                    
		    pvNode.removingOccurred();
                    pvNode_Parent.remove(pvNode); 
		    Channel channel = pvNode.getChannel();
		    String pv_name = null;
		    if(channel != null){
			pv_name = channel.channelName();
		    }
		    else{
			pv_name = pvNode.getName();
		    }
                    pvNameJText.setText(pv_name);
		    JTree pvTree = pvTreePanel.getJTree();
		    DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
		    treeModel.reload(pvNode_Parent);
		}
	    });

	rawPVButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    pvNameJText.setEditable(rawPVButton.isSelected());
		}
	    });


        ActionListener extTreeSelectionListener =  new ActionListener(){
		public void actionPerformed(ActionEvent e){
		    Object source  = e.getSource();
                    if(source instanceof PVTreeNode){
			PVTreeNode tn = (PVTreeNode) source;
			if(tn.isPVName()){
			    Channel channel = tn.getChannel();
			    if(channel != null){
                              pvNameJText.setText(null);
                              pvNameJText.setText(channel.channelName());
			    }
			    else{
                              pvNameJText.setText(null);
                              pvNameJText.setText(tn.getName());
			    }
			}
		    }
		}
	    };

	pvTreePanel.setExtTreeSelectionListener(extTreeSelectionListener);

	setAllFonts(fnt);
    }

    /** The constructor. Just craete objects we need internally.
     *
     * @param accel - The XAL accelerator object to build a tree from
     * @param pvRoot - the root node of the pv's tree
     */
  
    public PVsSelector(Accelerator accel, PVTreeNode pvRoot) {
	this(pvRoot); 
	setAccelerator(accel);
    }

    public PVTreeNode getPVNodeRoot(){
	return pvRoot;
    }

    public PVsTreePanel getPVsTreePanel(){
	return pvTreePanel;
    }

    public PVsTreePanel getNewPVsTreePanel(){
        PVsTreePanel pn = new PVsTreePanel(pvRoot);
        pn.setAllFonts(fnt);
        pn.setControlMode();
        pn.getJTree().setModel(pvTreePanel.getJTree().getModel());
	return pn;
    }

    public DefaultTreeModel getDefaultTreeModel(){
	JTree pvTree = pvTreePanel.getJTree();
	DefaultTreeModel treeModel = (DefaultTreeModel) pvTree.getModel();
	return treeModel;
    }


    public void removeMessageTextField(){
	uppPanel.remove(messageJText);
    }

    public void showMessage(String msg){
        if(msg != null){
	    messageJText.setText("Message : "+msg);
	    Toolkit.getDefaultToolkit().beep();
	}
	else{
	    messageJText.setText(null);
	}
    }

    /** Returns the message JTextField for this selector
     */
    public JTextField getMessageJTextField(){
	return messageJText;
    }

    /** set an accelertor to this PVsSelector 
     * @param accel - the accelerator sequence
     */
    public void setAccelerator(Accelerator accel) {
	xalTree = new XALTreeNode(accel, myLabel);
	tree = new JTree(xalTree);
	startItUp();
    }

    /** set  the name of this PVsSelector */
    public void setLabel(String label) {
	myLabel = label;
	if(xalTree != null){
	    xalTree.setUserObject(label);
	}
    }

    /** returns the name of this PVsSelector */
    public String getLabel() { return myLabel;}

    /** the method to actually crank up and manage the tree PV selector */
    private void startItUp() {
	selectedHandleNode = null;
	tree.expandPath(new TreePath(tree.getModel().getRoot()));
	tree.setBackground(scrollPane.getBackground());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setCellRenderer(signalCellRenderer);
	tree.setFont(fnt);
	scrollPane.setViewportView(tree);
	MouseListener ml = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    selectedHandleNode = null;
		    int selRow = tree.getRowForLocation(e.getX(), e.getY());
		    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		    if(selRow != -1) {
			pvNameJText.setText(null);
			Object value = selPath.getLastPathComponent();
			if(value instanceof HandleNode){
			    if(((HandleNode) value).isSignal()){
				selectedHandleNode = (HandleNode) value;
				String PVName = selectedHandleNode.getChannelName();
				pvNameJText.setText(PVName);
			    }
			}

		    }
		}
	    };

	tree.addMouseListener(ml);
    }
 
    public void setAcceleratorFileName(String nameOfXALFile){
	File flIn = new File(nameOfXALFile);
        if(flIn.exists()){
            String url = null;
	    try{
		url = flIn.toURI().toURL().toString();
	    } catch(MalformedURLException e){
		Toolkit.getDefaultToolkit().beep();
		return;                
	    }

	    XMLDataManager  dMgr = new XMLDataManager(url); 
	    Accelerator accel = null;
	    try {
		accel = dMgr.getAccelerator();
            
	    }
	    catch (Exception e){
		System.err.println("Cannot get accelerator: Exeption - " + e.getMessage());
		Toolkit.getDefaultToolkit().beep();
		return;
	    }
	    setAccelerator(accel);
        }
	else{
	    Toolkit.getDefaultToolkit().beep();
	    return;
	} 
    }


    public void setAllFonts(Font fntIn){
	fnt = fntIn;

	scrollPane.setFont(fnt);
        setPVButton.setFont(fnt);
	removePVButton.setFont(fnt);
	rawPVButton.setFont(fnt);
	pvNameJText.setFont(fnt);
	messageJText.setFont(fnt);

	pvTreePanel.setAllFonts(fnt);
	if(tree != null){
	    tree.setFont(fnt);
	}

    }

}
