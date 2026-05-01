/*
 * PVProdderDbl.java
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
 * a given JTextArea with a given PVUpdaterDbl at 1Hz.
 *
 * @author S. Bunch
 * @version 1.0
 * @see Thread
 * @see PVUpdaterDbl
 */
public class PVProdderDbl  extends Thread
{
	private volatile Thread blinker;
	private JTextArea theText;
	private PVUpdaterDbl thePV;
	private int m_msecs = 1000;

	/**
	 * The PVProdderDbl constructor.
	* @param txt	The JTextArea to update
	* @param pvu	The PVUpdaterDbl to monitor
	 */
	public PVProdderDbl(JTextArea txt, PVUpdaterDbl pvu) {
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
	 * then will get the current value from the PVUpdaterDbl
	 * and updates the JTextArea with the value
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while(blinker == thisThread){
			try {
				Thread.sleep(m_msecs);
			}
			catch (InterruptedException e) {
				System.err.println( e.getMessage() );
				e.printStackTrace();
			}
			theText.setText((thePV.getValue()).toString());
		}
	}
}

