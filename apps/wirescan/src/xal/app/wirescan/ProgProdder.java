/*
 * ProgProdder.java
 */

package xal.app.wirescan;

import java.awt.Dimension;
import java.awt.Component;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import xal.extension.application.*;
import xal.ca.*;
import xal.tools.apputils.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * The class used to start a separate thread and update
 * a given JProgressBar with a given PVUpdaterDbl at 0.5Hz.
 *
 * @author S. Bunch
 * @version 1.0
 * @see Thread
 * @see PVUpdaterDbl
 */
public class ProgProdder  extends Thread
{
	private volatile Thread blinker;
	private JProgressBar progBar;
	private PVUpdaterDbl thePV;
	private int m_msecs = 500;
	private WireTableModel tableModel;
	
//	private boolean gotProgress = true;
//	private boolean isRunning = false;
	/**
        * The ProgProdder constructor.
	 * @param progbar	The JProgressBar to update
	 * @param pvu		The PVUpdaterDbl to monitor
        */
	public ProgProdder(JProgressBar progbar, PVUpdaterDbl pvu, WireTableModel wtm) {
		progBar = progbar;
		thePV = pvu;
		tableModel = wtm;
	}

	/** The routine to start the Thread */
	public void start() {
//		gotProgress = true;
//		isRunning = true;
		blinker = new Thread(this);
		blinker.start();
	}

	/** The routine to stop the Thread */
	public void stopT() {
//	        isRunning = false;
		blinker = null;
	}

	/**
	 * Required run action by the Thread class.
	 * The thread will sleep for a specified amount of time
	 * then will get the current value from the PVUpdaterDbl
	 * and updates the JProgressBar with the value
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		boolean gotProgress = true;
		boolean isRunning = false;
		while(gotProgress == true){
			try {
				Thread.sleep(m_msecs);
			} catch (InterruptedException e) {
				System.out.println("Sleep interrupted during progress prodder");
				System.err.println( e.getMessage() );
				e.printStackTrace();
			}
			tableModel.setCurrentValueAt(progBar, (thePV.getValue()).intValue());
			if((thePV.getValue()).intValue() > 1) { isRunning = true; }
			if(isRunning == true) {
				if((thePV.getValue()).intValue() < 1) { gotProgress = false; }
			}
		}
	}
}

