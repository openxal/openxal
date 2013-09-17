package xal.app.beamatfoil;

import java.net.*;
import java.io.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import javax.swing.tree.DefaultTreeModel;

import xal.tools.scan.UpdatingEventController;
import xal.tools.xml.*;
import xal.tools.swing.*;
import xal.smf.*;
import xal.smf.impl.*;

/**
 *  This controller includes two panels for hor. and vert. positions and angle
 *  control.
 *
 *@author     shishlo
 */
public class HEBTOrbitController {

	//main panel
	private JPanel hebtOrbMainPanel = new JPanel();

	//Updating controller
	UpdatingEventController updatingController = null;
	
	HEBTOrbitCorrector hebtOrbCorrH = null; 
	HEBTOrbitCorrector hebtOrbCorrV = null; 
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the HEBTOrbitController object
	 *
	 *@param  updatingController_in  The Parameter
	 */
	public HEBTOrbitController(UpdatingEventController updatingController_in) {

		updatingController = updatingController_in;
		
		hebtOrbCorrH = new HEBTOrbitCorrector("HORIZONTAL - HEBT Beam at Foil");
		hebtOrbCorrV = new HEBTOrbitCorrector("VERTICAL - HEBT Beam at Foil");
		hebtOrbCorrH.setMessageText(getMessageText());
		hebtOrbCorrV.setMessageText(getMessageText());

		hebtOrbMainPanel.setLayout(new GridLayout(2, 1, 1, 1));
		hebtOrbMainPanel.add(hebtOrbCorrH.getPanel());
		hebtOrbMainPanel.add(hebtOrbCorrV.getPanel());
	}

	/**
	 *  Returns the panel attribute of the HEBTOrbitController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return hebtOrbMainPanel;
	}

	/**
	 *  Sets the accelerator sequence
	 *
	 *@param  accSeq  The new accelSeq value
	 */
	public void setAccelSeq(AcceleratorSeq accSeq) {
		java.util.List<AcceleratorNode> accNodes = accSeq.getNodesOfType(Electromagnet.s_strType);
		java.util.Iterator<AcceleratorNode>  itr =  accNodes.iterator();
		while(itr.hasNext()){
			Electromagnet emg = (Electromagnet) itr.next();
			if(emg.getStatus()){
				emg.setUseFieldReadback(false); 
			}
		}
		hebtOrbCorrH.setAccelSeq(accSeq,0);
		hebtOrbCorrV.setAccelSeq(accSeq,1);
	}
	
	
	/**
	 *  Description of the Method
	 */
	public void update() {
	}

	
	/**
	 *  Returns the sign for hor. correctors
	 */	
	 public DoubleInputTextField getSignXText(){
	  return hebtOrbCorrH.getSignText();
	 }
	 
	/**
	 *  Returns the sign for ver. correctors
	 */	
	 public DoubleInputTextField getSignYText(){
	  return hebtOrbCorrV.getSignText();
	 }
	
	/**
	 *  Sets the fontForAll attribute of the HEBTOrbitController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		hebtOrbCorrH.setFontForAll(fnt);
		hebtOrbCorrV.setFontForAll(fnt); 
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(XmlDataAdaptor da) {
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(XmlDataAdaptor da) {
	}

	/**
	 *  Returns the messageText attribute of the HEBTOrbitController object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

}
