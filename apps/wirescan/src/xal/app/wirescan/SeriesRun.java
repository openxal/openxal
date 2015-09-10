/*
 * SeriesRun.java
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
 * The class used to start a separate thread and run
 * scans in a series fashion.
 *
 * @author S. Bunch
 * @version 1.0
 * @see Thread
 */
public class SeriesRun  extends Thread
{
	private ArrayList<AcceleratorNode> theList;
	private ArrayList<WirePanel> theList2;

	/**
	 * The SeriesRun constructor.
	* @param thewires	The ArrayList of wirescanners to do scans with
	* @param thepanels	The ArrayList of WirePanels associated with each wirescanner
	 */
	public SeriesRun(ArrayList<AcceleratorNode> thewires, ArrayList<WirePanel> thepanels) {
		theList = thewires;
		theList2 = thepanels;
	}

	/**
	 * Required run action by the Thread class.
	 * The thread will start a wirescanner scan and then monitor for it's completion,
	* after completion it will start the next wirescanner scan in the list until all are
	* completed.
	 */
	public void run() {
		/* Loop through each wirescanner */
		for(int i = 0; i < theList.size(); i++) {
			int timeout = 0;
			/* Make sure it's ready to scan */
			while(timeout <=10) {
				if((theList2.get(i)).status.getText().startsWith("Ready")) { break;}
				if((theList2.get(i)).status.getText().startsWith("Scan")) { break;}
				if((theList2.get(i)).status.getText().startsWith("Found")) { break;}
				try{
					Thread.sleep(1000);
				}
				catch(InterruptedException ie){
					System.out.println("Sleep interrupted for wirescanner "
					                   + (theList.get(i)).getId() + " while checking if ready");
					System.err.println( ie.getMessage() );
					ie.printStackTrace();
				}
				timeout++;
			}
			/* It's good to go so let's start scanning */
			if(timeout <=10) {
				try{
					((ProfileMonitor) theList.get(i)).doScan();
				}
				catch(ConnectionException ce){
					System.out.println("Could not connect to "
					                   + (theList.get(i)).getId()
					                   + " BeginScan to start the scan");
					System.err.println( ce.getMessage() );
					ce.printStackTrace();
				}
				catch(PutException pe){
					System.out.println("Could not write to "
					                   + (theList.get(i)).getId()
					                   + " BeginScan to start the scan");
					System.err.println( pe.getMessage() );
					pe.printStackTrace();
				}
			}
			/* Not ready yet so we can't start a new scan */
			else {
				System.out.println("Timeout expired for " + (theList.get(i)).getId()
				                   + " while waiting for ready");
			}
			try{
				Thread.sleep(1000);
			}
			catch(InterruptedException ie){
				System.err.println( ie.getMessage() );
				ie.printStackTrace();
			}
			/* Monitor the current scan for completion before going on to the next wirescanner */
			while(( theList2.get(i)).isScanning && (timeout <=10)) {
				try{
					Thread.sleep(1000);
				}
				catch(InterruptedException ie){
					System.out.println("Sleep interrupted during series run "
					                   + "while waiting for next scan");
				}
			}
		}
	}
}

