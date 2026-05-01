/**
 * A class to do checks on the connection status
 * of the PVs in the tables (both connections for monitors + value puts)
 *
 */

package xal.app.score;

import xal.ca.*;
import xal.tools.data.*;

import java.text.*;
import java.util.*;
import java.lang.*;
import javax.swing.*;

public class ConnectionChecker implements Runnable {
	
    /** the ScoreDocument this checker belongs to */
    private ScoreDocument theDoc;
	
    /** the thread this works in */
    private Thread thread;
	
	/** a list containing the channel names that are not connected */
    private ArrayList<String> notConnected;
    
	/** the time to wait before giving up on connections (sec)*/
    private double timeOut;
    
    /** the number of checks to make (calculated from timeOut */
    private int nTrys;
	
    /* minimum time to wait between channelConnection checks (sec) */
    static public final double dwellTime = 0.5;
	
    /** A flag indicating some channels are not connected */
    private boolean allOK = false;
	
    /** 
     * Constructor 
     * @param doc - the ScoreDocument to check connections
     */	
    public ConnectionChecker(ScoreDocument doc) {
		theDoc = doc;
    }
	
    /** return the array of not connected PV names from the last check */
    protected ArrayList<String> getNotconnectedPVs() { return notConnected;}
	
    /** method to start a check 
     * @param to - timeout period for connection check (sec)
     */
    public void doCheck(double to) {
		System.out.println("Starting a check");
		// do connection checs in a seperate thread so we do not block other actions
		timeOut = to;
		nTrys = (int) (timeOut/dwellTime);
		thread = new Thread(this, "ConnectChecker");
		thread.start();
    }
	
    /* monitor connection status until timeout */
	
    public void run() {
		startConnectionStatus();
		int i=0;
		while (i< nTrys) {
			notConnected = getNotConnectedPVs();
			if(notConnected.size() == 0) {
				allOK = true;
				break;
			}
			else {
				try {
					Thread.sleep((int) (1000 * dwellTime));
				}
				catch (InterruptedException e) {}
				boolean blink = theDoc.myWindow().errorText.isVisible();
				theDoc.myWindow().errorText.setVisible(!blink);   
				i++;
			}
		}
		
		reportStatus();
    }
	
    /** Check the document tables monitor connections and
	 report if any are not connected */
	
    public void startConnectionStatus() {
	    
		allOK = false;
		
		String errText = "Checking on connections";
		theDoc.myWindow().errorText.setText(errText);
		System.out.println(errText);	
    }
	
	
    /** Report the status of connections after checking is done */
    private void reportStatus() {
		String name;
		theDoc.myWindow().errorText.setVisible(true);
		System.err.println("Connection Status Check at " + new Date());
		
		if(notConnected == null) {
			theDoc.dumpErr("What the ..?, In ConnectionChecker, tried to updateConnectionStatus with a null notConnected map");
			return;
		}
		
		if (allOK) 
			System.err.println("All connections are OK");
		else {
			theDoc.dumpErr("Some channels are not connected - see console output");
		    System.err.println("The following channels are not connected");		
		    Iterator<String> itr = notConnected.iterator();
		    while(itr.hasNext()) {
				name = itr.next();
				System.err.println(name);
		    }
		}
    }
    
    
    /** get a list of PVs that are not connected */
    private ArrayList<String> getNotConnectedPVs() {
	    
		ArrayList<String> badPVs = new ArrayList<String>();
		// get all the records for this table:
		Collection<GenericRecord> records = theDoc.theData().getDataTable().records();
		ScoreRecord record;
		
		// Grab all the live values & copy to saved
		Iterator<GenericRecord> itr2 = records.iterator();
		while (itr2.hasNext()) {
			record = (ScoreRecord)itr2.next();
			ChannelWrapper spChan = record.getSPChannel();
			ChannelWrapper rbChan = record.getRBChannel();
			if(spChan != null && !spChan.isConnected()){
				badPVs.add(spChan.getId());
			}
			if(rbChan != null && !rbChan.isConnected()){
				badPVs.add(rbChan.getId());
			}
		}
		return badPVs;
    }	    
}

