/*
 * WireWindow.java
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
 * The class used to start a separate thread and monitor
 * for total scan completion at 1Hz. It will then kill all
 * running threads after a wirescan is done.
 *
 * @author S. Bunch
 * @version 1.0
 * @see Thread
 */
public class ThreadKiller extends Thread
{
	private volatile Thread blinker;
	private int m_msecs = 1000;
	private WireWindow theWindow;
        private ArrayList<Integer> tempIndex;
	private boolean isAlive = false;

	/** The ThreadKiller constructor */
	public ThreadKiller(WireWindow ww) {
		theWindow = ww;
		tempIndex = new ArrayList<Integer>();
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
        * The thread will parse through each running wirescanner and check
	 * for completion status, if found it will then kill all threads
	 * associated with the completed scan.
        */
	public void run() {
		Thread thisThread = Thread.currentThread();
		int j = 0;
		
		int counter = theWindow.panelList.size();
		
		isAlive = true;
		
		while(blinker == thisThread && isAlive){
		        tempIndex.clear();
			/* Loop through all running scans to check for completion */
			for(int i = 0; i < theWindow.panelList.size(); i++) {
				/* If it's done, then kill threads and go on to the next one */
				if((theWindow.panelList.get(i)).isScanning == false) {
				    tempIndex.add(new Integer(i));
				    System.out.println("panellist size " + theWindow.panelList.size());
//				    if (theWindow.firstScan) {
					(theWindow.panelList.get(i)).posProdder.stopT();
//				    }
					j++;
					System.out.println("killing " + j);
				}
			}
			for(int i = 0; i < tempIndex.size(); i++) {
//			    theWindow.panelList.remove(((Integer)tempIndex.get(i)).intValue());
			    counter = counter - 1;
			}
			try {
				Thread.sleep(m_msecs);
			} catch (InterruptedException e) {
				System.out.println("Sleep interrupted during progress prodder");
				System.err.println( e.getMessage() );
				e.printStackTrace();
			}
			/* If we've done all the scans then kill this thread */
//			if( theWindow.panelList.size() < 1) {
//      				theWindow.doPostSigmaTable();
//				blinker = null;
//			}

			if( counter < 1) {
		                isAlive = false;
				
				if (theWindow.firstScan) 
					blinker = null;
			}

		}
		
		
		theWindow.msgField.setText("Scan completed!  Please wait a few seconds for data updating.");
		theWindow.clearPVUpdaters();
		theWindow.enableStartButton();
	}
}

