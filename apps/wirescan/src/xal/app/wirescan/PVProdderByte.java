/*
 * PVProdderByte.java
 */

package xal.app.wirescan;

import java.awt.Dimension;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTable;

import xal.extension.application.*;
import xal.ca.*;
import xal.tools.apputils.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * The class used to start a separate thread and update
 * a given JTextArea with a given PVUpdaterByteArry at 1Hz
 * @author S. Bunch
 * @version 1.0
 * @see Thread
 * @see PVUpdaterByteArry
 */
public class PVProdderByte  extends Thread
{
	private volatile Thread blinker;
	private JTextArea theText;
	private PVUpdaterByteArry thePV;
	private int m_msecs = 1000;
	private WirePanel theWP;

	/**
	 * The PVProdderByte constructor.
	 * @param txt	The JTextArea to update
	 * @param pvu	The PVUpdaterByteArry to monitor
	 */
	public PVProdderByte(JTextArea txt, PVUpdaterByteArry pvu, WirePanel wp) {
		theWP = wp;
		theText = txt;
		thePV = pvu;
	}

	/** The routine to start the Thread */
	public void start() {
		blinker = new Thread(this);
		blinker.start();
	}

	/** The routine to stop the Thread */
	public void stopT() {
		blinker = null;
	}

	/**
	 * Required run action by the Thread class.
	 * The thread will sleep for a specified amount of time
	 * then will get the current value from the PVUpdaterByteArry
	 * and updates the JTextArea with the value.
	* In addition, it will set isScanning to false to inform other processes
	* that this wirescanner is finished scanning.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while(blinker == thisThread){
			if( thePV.getValue().startsWith("...Scan")) {
				theWP.status.setBackground(Color.GREEN);
				theWP.isRunning = true;
			}
			if( thePV.getValue().startsWith("There")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("Power Up")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("TIMEOUT")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("A LIMIT")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("Position")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("Hit")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("Not")) {theWP.status.setBackground(Color.RED);}
			if( thePV.getValue().startsWith("Ready")) {theWP.status.setBackground(Color.GREEN);}
			if( thePV.getValue().startsWith("Scan")) {theWP.status.setBackground(Color.GREEN);}
			if( thePV.getValue().startsWith("Searching")) {theWP.status.setBackground(Color.GREEN);}
			if( thePV.getValue().startsWith("Found")) {theWP.status.setBackground(Color.GREEN);}
			if(theWP.isRunning == true) {
				Double temp = theWP.posUpdate.getValue();
				boolean temp2 = (thePV.getValue().startsWith("Scan")  && (temp.doubleValue() < 1.) );
				if(thePV.getValue().startsWith("Ready") || temp2) {
					theWP.isScanning = false;
					try {
						Thread.sleep(5000);
					}
					catch (InterruptedException e) {
						System.err.println( e.getMessage() );
						e.printStackTrace();
					}
					theWP.doPostGraph();
					theWP.getAreas();
					theWP.getAmpls();
					theWP.getMeans();
					theWP.getSigmas();
					theWP.getOffsets();
					theWP.getSlopes();
					theWP.doPostTable();
                                        theWP.getWireWindow().updatePostSigmaTable();
                                        theWP.getWireWindow().msgField.setText("Final data update finished");
					blinker = null;
				}
			}
			try {
				Thread.sleep(m_msecs);
			}
			catch (InterruptedException e) {
				System.err.println( e.getMessage() );
				e.printStackTrace();
			}
			theText.setText(thePV.getValue());
		}
	}
}

