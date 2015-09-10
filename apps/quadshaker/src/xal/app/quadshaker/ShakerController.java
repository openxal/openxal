package xal.app.quadshaker;

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
import xal.tools.data.DataAdaptor;


import xal.extension.scan.UpdatingEventController;
import xal.tools.xml.*;

/**
 *  This controller includes two other controllers ShakerRunController and
 *  ShakerObserverController. ShakerRunController controls the running
 *  prosedure, and ShakerObserverController shows the intemediate results.
 *
 *@author     shishlo
 */
public class ShakerController {

	//main panel
	private JPanel shakerMainPanel = new JPanel();

	//Updating controller
	UpdatingEventController updatingController = null;

	//the tabbed panel that will keep two panels
	//one for controller and the second for watch results
	private JTabbedPane shakerTabbedPanel = new JTabbedPane();

	//controllers for running the shake procedure and observe temporary resilts
	private ShakerRunController shakerRunController = null;
	private ShakerObserverController shakerObserverController = null;

	//Tables and List models for BPMs and Quads
	private QuadsTable quadsTable = null;
	private BPMsTable bpmsTable = null;

	//message text field. It is actually message text field from Window
	private JTextField messageTextLocal = new JTextField();

	/**
	 *  Constructor for the ShakerController object
	 *
	 *@param  updatingController_in  The Parameter
	 */
	public ShakerController(UpdatingEventController updatingController_in) {

		updatingController = updatingController_in;

		shakerRunController = new ShakerRunController(updatingController);
		shakerObserverController = new ShakerObserverController(updatingController);

		shakerTabbedPanel.add("Running Control", shakerRunController.getPanel());
		shakerTabbedPanel.add("Observer", shakerObserverController.getPanel());
		shakerRunController.getMessageText().setDocument(messageTextLocal.getDocument());
		shakerObserverController.getMessageText().setDocument(messageTextLocal.getDocument());

		shakerTabbedPanel.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					messageTextLocal.setText(null);
					messageTextLocal.setForeground(Color.red);
					update();
				}
			});

		shakerMainPanel.setLayout(new BorderLayout());
		shakerMainPanel.add(shakerTabbedPanel, BorderLayout.CENTER);
	}

	/**
	 *  Returns the panel attribute of the ShakerController object
	 *
	 *@return    The panel value
	 */
	public JPanel getPanel() {
		return shakerMainPanel;
	}

	/**
	 *  Sets the tableModels attribute of the ShakerController object
	 *
	 *@param  quadsTable  The new tableModels value
	 *@param  bpmsTable   The new tableModels value
	 */
	public void setTableModels(QuadsTable quadsTable, BPMsTable bpmsTable) {
		this.quadsTable = quadsTable;
		this.bpmsTable = bpmsTable;
		shakerRunController.setTableModels(quadsTable, bpmsTable);
		shakerObserverController.setTableModels(quadsTable, bpmsTable);
	}

	/**
	 *  Description of the Method
	 */
	public void update() {
		int index = shakerTabbedPanel.getSelectedIndex();
		if(index == 0) {
			shakerRunController.update();
		}
		if(index == 1) {
			shakerObserverController.update();
		}
	}

	/**
	 *  Sets the fontForAll attribute of the ShakerController object
	 *
	 *@param  fnt  The new fontForAll value
	 */
	public void setFontForAll(Font fnt) {
		shakerRunController.setFontForAll(fnt);
		shakerObserverController.setFontForAll(fnt);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void dumpData(DataAdaptor da) {
		shakerRunController.dumpData(da);
	}

	/**
	 *  Description of the Method
	 *
	 *@param  da  The Parameter
	 */
	public void readData(DataAdaptor da) {
		shakerRunController.readData(da);
	}

	/**
	 *  Returns the shakerRunController attribute of the ShakerController object
	 *
	 *@return    The shakerRunController value
	 */
	public ShakerRunController getShakerRunController() {
		return shakerRunController;
	}


	/**
	 *  Returns the messageText attribute of the ShakerController object
	 *
	 *@return    The messageText value
	 */
	public JTextField getMessageText() {
		return messageTextLocal;
	}

}

