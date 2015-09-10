/*
 *  DevTreeNode.java
 *
 *  Created on Feb. 25 2009
 */
package xal.app.rfphaseshaker;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.tree.TreeNode;

import java.util.*;

import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorNode;

/**
 *  DevTreeNode represents a leaf in DevTree. It could be AccSeq, or RF, or BPM.
 *  If node is not AccSeq or AccNode it is a root node.
 *
 *@author     shishlo
 */

public class DevTreeNode implements TreeNode{

	public AcceleratorNode accNode = null;
	public AcceleratorSeq accSeq  = null;
	
	DevTreeNode parentNode = null;
	public Vector<DevTreeNode> children = new Vector<DevTreeNode>();
	
	public double position = 0;
	
	public boolean isOn = false;
	
	//the dictionary with parameters
	Hashtable<String,Number> hashT = new Hashtable<String,Number>();
	
	/**
	 *  Costructor
	 */
	public DevTreeNode() {
	}
	
	public Enumeration<DevTreeNode> children(){
		return children.elements();
	}
	
	public boolean getAllowsChildren(){
		if(accSeq != null) return true;
		return false;
	}
	
	public TreeNode getChildAt(int childIndex){
		return (TreeNode) children.get(childIndex);
	}

	public int getChildCount(){
		return children.size();
	}
	
	public int getIndex(TreeNode node){
		int res = -1;
		int index = 0;
		for(DevTreeNode ch_node:  children){
			if(ch_node.equals(node)){
				res = index;
			}
			index++;
		}
		return res;
	}
	
	public TreeNode getParent(){
		return parentNode; 
	}
	
	public boolean isLeaf(){
		if(accNode != null) return true;
		return false;
	}
}

