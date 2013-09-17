/**
 *  This controller includes panel for beam at foil position 
 *  control.
 *
 *@author     shishlo
 */
 
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
 
public class RingFoilPosController {

	//main panel
	private JPanel ringFoilPosMainPanel = new JPanel();

	//Updating controller
	UpdatingEventController updatingController = null;
	
	RingFoilPosCorrector ringFoilPosCorr = null; 
	
	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the RingFoilPosController object
	 *
	 *@param  updatingController_in  The Parameter
	 */
	public RingFoilPosController(UpdatingEventController updatingController_in) {

		updatingController = updatingController_in;
		
		ringFoilPosCorr = new RingFoilPosCorrector("HORIZONTAL Ring Beam Position at Foil");
		ringFoilPosCorr.setMessageText(getMessageText());

		ringFoilPosMainPanel.setLayout(new BorderLayout());
		ringFoilPosMainPanel.add(ringFoilPosCorr.getPanel(),BorderLayout.CENTER);
	}

	/**
	 *  Returns the panel attribute of the RingFoilPosController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return ringFoilPosMainPanel;
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
		ringFoilPosCorr.setAccelSeq(accSeq);
	}
	
	
	/**
	 *  Description of the Method
	 */
	public void update() {
	}

	
	/**
	 *  Sets the fontForAll attribute of the RingFoilPosController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		ringFoilPosCorr.setFontForAll(fnt); 
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
	 *  Returns the messageText attribute of the RingFoilPosController object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

}
