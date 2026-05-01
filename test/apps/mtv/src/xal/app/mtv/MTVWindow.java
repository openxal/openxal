/*
* MyWindow.java
*
* Created on April 14, 2003, 10:25 AM
*/

package xal.app.mtv;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;

/**
* The window representation / view of an xiodiag document
*
* @author  jdg
*/
public class MTVWindow extends AcceleratorWindow {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	
	
	/** the frame to put all the tables into */
	private JTabbedPane tabbedPanel;
	
	/** a label describing the selected sequence */
	private JLabel seqLabel;
	
	private MagnetPanel magPanel;
	private PVsPanel pvsPanel;
	
	private MTVDocument theDoc;
	
	protected WheelPanel wheelPanel;
	
	public MagnetPanel getMagPanel() { return magPanel;}
	
	/** Creates a new instance of MainWindow */
	public MTVWindow(MTVDocument aDocument) {
		// initialize the document
		super(aDocument);
		theDoc = aDocument;
		//set up the window
		setSize(500, 600);
		makeContent();
	}
	
	/**
	* Create the main window subviews.
	*/
	protected void makeContent() {
		// make default startup gui components
		//	tableDisplayPanel = new JPanel(new FlowLayout());
		seqLabel = new JLabel("No Seqence Selected");
		this.getContentPane().add(seqLabel, BorderLayout.NORTH);
		
		tabbedPanel = new JTabbedPane();
		tabbedPanel.setVisible(true);
		this.getContentPane().add(tabbedPanel, BorderLayout.CENTER);
		addTabs();
		
		wheelPanel = new WheelPanel(theDoc);
		this.getContentPane().add(wheelPanel, BorderLayout.SOUTH);
	}
	
	/** create the tabed panels for different device types */
	private void addTabs() {
		magPanel = new MagnetPanel(theDoc);
		pvsPanel = new PVsPanel(theDoc);
		tabbedPanel.add("Magnets", magPanel);
		tabbedPanel.add("Arbitrary PVs", pvsPanel);
	}
	
	/** reset the main tabs */
	protected void updateTabs() {
		magPanel.updateMagnetTypes();
		magPanel.updateMagnetPanel();
		magPanel.updateMagnetTable(); 
	}
	
	/** return the frame to display tables in */
	
	public JTabbedPane getTablePanel() { return tabbedPanel;}
	
	/** return the sequence label */
	public JLabel getSeqLabel() { return seqLabel;}
	
	/** return the document associated with this wiondow */
	public MTVDocument getDocument() {
		return (MTVDocument) document;
	}
	
	
}
