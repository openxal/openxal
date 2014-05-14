/** A class to handle setting restoration. This will find all records 
 * that have a setting value and then try setting it.
 * Also check to see if any restore setting attempts did not succeed.
 */


package xal.app.score;

import xal.ca.*;
import xal.extension.widgets.swing.KeyValueTableModel;
import xal.tools.data.*;
//import xal.extension.logbook.ElogUtility;

import java.text.*;
import java.util.*;
import java.lang.*;
import javax.swing.*;

public class Restorer implements Runnable, PutListener {

    /** the document this resorer belongs to */
    private ScoreDocument theDoc;

    /** the thread this works in */
    private Thread thread;

    /** List of channels that have been sent out for restoring */
    private ArrayList<String> sentChannels;

    /** List of channels that have been restored successfully */
    private ArrayList<String> receivedChannels;
	
    /* the time to wait between channelConnection checks (sec) */
    static public final double dwellTime = 0.5;

    /** the number of loops to make to see if the connections are OK
     * calculated from the input timeOut period 
     */
    private int nTrys;

    /** A flag indicating some channels are not connected */
    private boolean allOK = false;

    /** Flag indicating something did not go perfectly */
    private boolean glitchOccurred = false;

    /** holder for PVs that we cannot connect to */
    private	ArrayList<String> badPVs = new ArrayList<String>();
    
    /** holder for bad value PVs */
    private ArrayList<String> badValuePVs = new ArrayList<String>();

    /** holder for selected records to restore */
    private Collection<ScoreRecord> selectedRecords;

    /** The number of selected records to restore */
    private int nRecords;
    
    /** time the restore action started */
    private Date date;
    
    private DataTable dataTable;
	
	
    /** the constructor
     * @param aDoc - the Document this reatorer acts on
     * @param timeOut - the time out period to wait before giving up on restores (sec)
     */
    public Restorer( final ScoreDocument aDoc, final double timeOut ) {
		theDoc = aDoc;
		dataTable = theDoc.theSnapshot.getData().getDataTable();
		selectedRecords = getSelectedRecords();
		nRecords = selectedRecords.size();
		
		if (nRecords == 0) {
			theDoc.dumpErr("You first must select some systems + types");
		}
		else {
			thread = new Thread(this, "ConnectChecker");
			sentChannels = new ArrayList<String>();
			receivedChannels = new ArrayList<String>();
			nTrys = (int) (timeOut/dwellTime);
			thread.start();
		}
    }
    
	
    /** get a collection of PVData records that have the selected rows in the selected table tab, that also have setpoints in them */
    @SuppressWarnings( "unchecked")
    //Suppressed warning on table.getModel() because it cannot be cast as KeyValueFilteredTableModel<ScoreRecord>
    private List<ScoreRecord> getSelectedRecords (){
	    final List<ScoreRecord> records = new ArrayList<ScoreRecord>();	 
	    final JTable table = theDoc.getScoreTable();
	    if( table != null ) {
		    final KeyValueTableModel<ScoreRecord> tableModel = (KeyValueTableModel<ScoreRecord>)table.getModel();
			final int[] selectedRows = table.getSelectedRows();
			for ( final int tableRow : selectedRows ) {
				final int modelRow = table.convertRowIndexToModel( tableRow );
				final ScoreRecord record = tableModel.getRecordAtRow( modelRow );
				if( record != null ) {
					final String spName = record.stringValueForKey( PVData.spNameKey );
					final Object spVal = record.valueForKey( PVData.spSavedValKey );
					if( spName != null && spVal != null ) {
						records.add( record );
					}
				}
		    }
	    }
	    return records;	
    }        
	
	
    public void run() {
		System.err.println("Starting a restore at " + new Date());
		
		// first give a chance to bail:
		
		String warningMsg = "Do you Really want to restore the " + (new Integer(nRecords)).toString() + " selected records?";
		int cont = JOptionPane.showConfirmDialog(theDoc.myWindow(), warningMsg, "Just Checking", JOptionPane.YES_NO_OPTION);
		
		if(cont == JOptionPane.NO_OPTION) return;
		
		theDoc.myWindow().errorText.setText("Attempting a restore");
		
		date = new Date();
		// fire out the setpoints:
		receivedChannels.clear();
		fireRestores();
		
		// Check on how things went:
		int i=0;
		while (i< nTrys) {
			if(allOK) {
				break;
			}
			else {
				try {
					Thread.sleep((int) (1000 * dwellTime));
					checkRestoreStatus();
				}
				catch (InterruptedException e) {}
				i++;
				boolean blink = theDoc.myWindow().errorText.isVisible();
				theDoc.myWindow().errorText.setVisible(!blink);   		
			}
		}
		reportStatus();
		postElogResoreEntry();	
    }

    /** loop through the setpoint PVs and send out
     * values associated with setpoint PVs  in the table
     */

    private void fireRestores() {
		badPVs.clear();
		badValuePVs.clear();
		glitchOccurred = false;

		// loop through tables and find channels that need setting:
		Iterator<ScoreRecord> itr = selectedRecords.iterator();
		while (itr.hasNext()) {
			final ScoreRecord record = itr.next();
			final DataTypeAdaptor dataTypeAdaptor = record.getDataTypeAdaptor();
			final ChannelWrapper channel = record.getSPChannel();
			if( channel != null &&  channel.isConnected() ) {
				final String name = channel.getId();
				final boolean useRBVal = record.booleanValueForKey( PVData.restoreRBValKey );
				final Object spVal = useRBVal ? record.getReadbackValue() : record.getSavepointValue(); 
				if( !dataTypeAdaptor.isValidCAValue( spVal ) ) {
					glitchOccurred = true;
					badValuePVs.add(name);
				}
				else {
					try {
						if(channel.getChannel().writeAccess() == false) {
							System.err.println("No WRITE access to " + name);
							glitchOccurred = true;
						}
						else {
							try {
								// OK we passed all the tests, let's do it:
								synchronized(sentChannels) {
									if(sentChannels.contains(name)) {
										glitchOccurred = true;
										System.err.println("Tried to set " + name + " more than once! only will do the first attempt");
									}
									else {
										dataTypeAdaptor.putValCallback( channel.getChannel(), spVal, this );
										sentChannels.add(name);
									}
								}
							}
							catch (Exception evt) {
								evt.printStackTrace();
							}
						}
					} 
					catch (ConnectionException eyah) {
						System.err.println("PV " + name +" is not connected, will ignore this restore request");
						glitchOccurred = true;
					}
				}
			}
			else
			   if (channel != null) badPVs.add(channel.getId());
		}
		
		// force channel access to process the put commands
		Channel.flushIO();
		
		if(badPVs.size() >0) {
		// opps got some bad pvs, report these and indicate in the data structure that they are no good.
			System.err.println(" The following setpoint PVs cannot be connected to and will not be restored:");
			glitchOccurred = true;
			Iterator<String> itrBad = badPVs.iterator();
			while (itrBad.hasNext() ) {
				final String name = itrBad.next();
				System.err.println(name);
			}
		}
    }
    
    protected void postElogResoreEntry() {
	    String msg = "Score restore done at " + date.toString() + "\n";
	    msg += "The following PVs were set:\n";
	    Iterator<String> itr= receivedChannels.iterator();
	    while (itr.hasNext()) msg += itr.next() + "\n";
	    if(badPVs.size() > 0) {
		    msg += "The following were not connected (nor attempted):\n";
		    itr= badPVs.iterator();
		    while (itr.hasNext()) msg +=  itr.next() + "\n";
	    }	    
	    if(msg.length() > 3999) msg = msg.substring(0, 4000);	    	    
	    
	    try {
		    //ElogUtility.defaultUtility().postEntry( Main.DEFAULT_LOGBOOK, "Score restore done", msg );
		    System.out.println(msg);
	    }
	    catch (Exception exc) {
		    System.err.println("Failed elog post on save " + exc.getMessage());
	    }
    }

    /** callback routine for channel puts.
     * keep track of how many requested puts made it */

    public void putCompleted(Channel chan) {
	// This one made it home - remove it from the list:

	synchronized (receivedChannels) {
	    receivedChannels.add(chan.getId());
	}
    }

    /** report status of the put attempts */
    private void reportStatus() {
	String key;
	theDoc.myWindow().errorText.setVisible(true); 
	System.err.println("Restore status at " + new Date());

	if(allOK && !glitchOccurred) {
		System.err.println("All PV restore trys were OK");
	}
	else {
		theDoc.dumpErr("Dang! some restore attempts failed. See console for details.");	
		if(sentChannels.size() > 0) {
			System.err.println("The following PV restores did not succeed");
			Iterator<String> itr = sentChannels.iterator();
			while(itr.hasNext()) {
			    key = itr.next();
			    System.err.println(key);
			}
		}
	}
    }

    /** check on the status of the PVs that were sent for restoration */
    private void checkRestoreStatus() {
	int i;
	synchronized(receivedChannels) {
	    Iterator<String> itr = receivedChannels.iterator();
	    while( itr.hasNext()) {
		i = sentChannels.indexOf(itr.next());
		if( i != -1) sentChannels.remove(i);
	    }
	}
	if (sentChannels.size() == 0) allOK = true; 
    }

}
