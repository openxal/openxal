/*
 *  DevTree.java
 *
 *  Created on Feb. 25 2009
 */
package xal.app.rfphaseshaker;

import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import java.util.*;

import xal.tools.text.ScientificNumberFormat;


/**
 *  DevTree is a device tree class to handle accelerator lines and accelerator nodes as leafs.
 *
 *@author     shishlo
 */

public class DevTree extends JTree {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

	private DevTreeNode rootTreeNode = null;
	
	private DefaultTreeModel treeModel = null;
	
	private JRadioButton treeButton = new JRadioButton(); 
	
	ScientificNumberFormat formatter = new ScientificNumberFormat( 5, 8, false );
	
	/**
	 *  Costructor
	 */
	public DevTree(DevTreeNode rootTreeNode_in) {
		super();
		rootTreeNode = rootTreeNode_in;
		
		treeModel = new DefaultTreeModel(rootTreeNode);
		setModel(treeModel);
		
		//set up RENDERER
		treeButton.setBackground(Color.white);
		TreeCellRenderer render = new TreeCellRenderer(){
			public Component getTreeCellRendererComponent(
				JTree tree, 
				Object value, 
				boolean selected, 
				boolean expanded, 
				boolean leaf, 
				int row, 
				boolean hasFocus)
			{
				DevTreeNode treeNode = (DevTreeNode) value;
				treeButton.setSelected(treeNode.isOn);
				if(treeNode.accNode != null){
					treeButton.setText(treeNode.accNode.getId()+"  L[m]="+formatter.format(treeNode.position));
				} else if(treeNode.accSeq != null){
					treeButton.setText(treeNode.accSeq.getId()+"  L[m]="+formatter.format(treeNode.position));
				} else {
					treeButton.setText("None");
				}				
				return treeButton;
			}
		};
		
		final JTree tree = this;
		
		//set up mouse listener
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(isEnabled() == false) return;
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
				if(selRow != -1) {
					DevTreeNode treeNode = (DevTreeNode) selPath.getLastPathComponent();
					treeNode.isOn = !treeNode.isOn;
					if(treeNode.accSeq != null){
						for(DevTreeNode chTreeNode : treeNode.children){
							chTreeNode.isOn = treeNode.isOn;
						}
					}
					if(treeNode.accNode != null){
						if(treeNode.isOn == true) treeNode.parentNode.isOn = true;
					}
				}
				tree.treeDidChange();
			}
		};
		addMouseListener(ml);		
		
		setCellRenderer(render); 
		setRootVisible(false); 
		setShowsRootHandles(true);
	}
	
	public void setFontForAll(Font fnt) {
		treeButton.setFont(fnt);
		setFont(fnt);
		treeDidChange();
	}
}

