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

import xal.tools.messaging.MessageCenter;
import xal.smf.*;
import xal.ca.Channel;
/** 
 * PVSelector is a GUI component creating a tree-like PV selection and shows  
 * the selected PV in a text box.  Note this is for single selection only.
 *
 * @version   0.1  30 Apr 2003
 * @author C.M. Chu
 */

public class PVSelector extends JPanel{
	/** serializable version ID required for classes that implement Serializable */
	private static final long serialVersionUID = 0L;

    /** textfield to hold selected PV name */
    public JTextField jText = new JTextField(40);

    /** the JTree to use for displaying the accelerator */
    public JTree tree;

    /** the JButton to set slected PV or just to escape */
    public JButton slectButton = new JButton("Set Selected PV or ESCAPE");

    /** the string container for the selected PV name */
    private String selectedPVName = null;

    /** scrollpane to hold the Tree selector */
    private JScrollPane scrollPane;

    /** the tree node to build a JTree from */
    private XALTreeNode xalTree;

    /** name for this PV selector - use it to differentiate PV sources
     * in the case you use more than one PV selector
     */
    private String myLabel;

    /** the proxy to call listeners of PV changes */
    private ActionListener actionListenerProxy = null;

    /** the action event */
    private ActionEvent actionEvent = null;

    /** will the listener hear the tree's element selection */
    private boolean treeSelectionListenerYes = false;

    /** the tree cell render */
    private SignalCellRenderer signalCellRenderer = new SignalCellRenderer();
    
    /** the selected channel */
    private Channel myChannel;

    /** The constructor. Just craete objects we need internally.
     *
     * @param label - the label to use to comment text box
     */
  
    public PVSelector(String label) {
	myLabel = label;

        actionEvent = new ActionEvent(this, 0, "pv_chosen"); 

	// line up the label, textField, Tree vertically:
        jText.setHorizontalAlignment(JTextField.CENTER); 
	setLayout(new BorderLayout());
	add(jText,BorderLayout.NORTH);

	scrollPane = new JScrollPane();
	add(scrollPane,BorderLayout.CENTER); 
	add(slectButton,BorderLayout.SOUTH); 

	slectButton.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    selectedPVName = jText.getText();
		    if(actionListenerProxy != null){
			actionListenerProxy.actionPerformed(actionEvent);
		    }
		}
	    });

      
    }


    /** The constructor. Just craete objects we need internally.
     *
     * @param accel - The XAL accelerator object to build a tree from
     * @param label - the label to use to comment text box
     */
  
    public PVSelector(Accelerator accel, String label) {
	this(label); 
	setAccelerator(accel);
    }

    /** set an accelertor to this PVSelector 
     * @param accel - the accelerator sequence
     */
    public void setAccelerator(Accelerator accel) {
	xalTree = new XALTreeNode(accel, myLabel);
	tree = new JTree(xalTree);
	startItUp();
    }

    /** set  the name of this PVSelector */
    public void setLabel(String label) {
	myLabel = label;
	if(xalTree != null){
	    xalTree.setUserObject(label);
	}
    }

    /** returns the name of this PVSelector */
    public String getLabel() { return myLabel;}

    /** returns the state of the tree selection listener */
    public boolean getTreeSelectionEventListenYes(){
	return treeSelectionListenerYes;
    }

    /** sets the state of the tree selection listener */
    public void setTreeSelectionEventListenYes(boolean treeSelectionListenerYes){
	this.treeSelectionListenerYes = treeSelectionListenerYes;
	if(treeSelectionListenerYes){
	    remove(slectButton);
	    validate();
	    repaint();
	}
	else{
	    remove(slectButton);
	    validate();
	    repaint();
	    add(slectButton,BorderLayout.SOUTH);
	}
    }

    /** the method to actually crank up and manage the tree PV selector */

    private void startItUp() {
	tree.expandPath(new TreePath(tree.getModel().getRoot()));
	tree.setBackground(scrollPane.getBackground());
	tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	tree.setCellRenderer(signalCellRenderer);

	scrollPane.setViewportView(tree);

	// catch when someone directly types in a PV to the textField 
	jText.addActionListener(new java.awt.event.ActionListener() {
		public void actionPerformed(java.awt.event.ActionEvent evt) {
		    selectedPVName = jText.getText();
		    if(actionListenerProxy != null){
			actionListenerProxy.actionPerformed(actionEvent);
		    }
		}
	    });

	MouseListener ml = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
		    int selRow = tree.getRowForLocation(e.getX(), e.getY());
		    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
		    if(selRow != -1) {
			if(e.getClickCount() == 1) {
			    jText.setText(null);
			    selectedPVName = null;
			    Object value = selPath.getLastPathComponent();
			    if(value instanceof HandleNode){
				if(((HandleNode) value).isSignal()){
				    // get full PV name instead of just handle
				    //String PVName = ((HandleNode)value).toString();
				    String PVName = ((HandleNode)value).getSignalName();
				    myChannel = ((HandleNode)value).getChannel();
				    jText.setText(null);
				    jText.setText(PVName);
				    selectedPVName = PVName;
				    if(treeSelectionListenerYes){
					if(actionListenerProxy != null){
					    actionListenerProxy.actionPerformed(actionEvent);
					}
				    }
				}
			    }
			}
			else if(e.getClickCount() == 2) {
			    if(!treeSelectionListenerYes){
				if(actionListenerProxy != null){
				    Object value = selPath.getLastPathComponent();
				    if(value instanceof HandleNode){
					if(((HandleNode) value).isSignal()){
					    actionListenerProxy.actionPerformed(actionEvent);
					}
				    }
			       
				}			    
			    }
			}
		    }
		}
	    };

	tree.addMouseListener(ml);
    }
 
    /** returns the reference to the command button */
    public JButton getSlectButton(){
	return slectButton;
    }
 
    /** convienience method to get selected name */
    public String getSelectedPVName(){
	return selectedPVName;
    };
  
    /** convienience method to get selected channel */
    public Channel getSelectedChannel(){
	return myChannel;
    };
  
    
    /** method to set a PVSectedListener */
    public void setPVSelectedListener(ActionListener actionListenerProxy ) {
        this.actionListenerProxy = actionListenerProxy;
    }

    /** method to get a PVSectedListener */
    public ActionListener getPVSelectedListener(ActionListener actionListenerProxy ) {
        return actionListenerProxy;
    }
    
    public String getPVText() {
      return jText.getText();
    }

}
