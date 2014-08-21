/*
 * ScoreDocument.java
 *
 * Created on 8/12/2003, 1:32 PM
 */

package xal.app.score;

import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.awt.Toolkit;
import java.sql.Timestamp;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.data.*;
import xal.extension.widgets.swing.*;
import xal.ca.*;
import xal.extension.logbook.ElogUtility;


/**
 * ScoreDocument is a custom XalDocument for the save compare restore application *
 * 
 * @author  jdg
 */
public class ScoreDocument extends XalDocument implements HandleErrorMessage{   
    // Members:
    
    /** Whether the systems are selected */
    protected boolean systemsSelected;

    /** whether the types are selected */
    protected boolean typesSelected;

    /** the snapshot information container*/
    protected ScoreSnapshot theSnapshot;

    /** table which displays the Score state */
    private JTable _scoreTable;
	
	/** The table model used here */
	private TableFactory _scoreTableFactory;
	
    /** A manager of the application setup/restore */
    private final ReadWrite readerWriter;

    /** a list containing the names of the all systems included in this dataset */
    protected TreeSet<Object> systemList;

    /** a list containing the names of all the types included in this dataset */
    protected TreeSet<Object> typeList;

    /** a list containing the names of the selected systems included in this dataset */
    protected ArrayList<String> selectedSystemList;

    /** a list containing the names of the selected types included in this dataset */
    protected ArrayList<String> selectedTypeList;

    /** A channel connection checking manager */
    protected ConnectionChecker connectionChecker;
    
    /** the fractional difference from the dsave value,
    * above which live values are displayed in red */
    protected double cuttoffFraction = 0.001;
    
    /** the data model to interface with the database */
    protected ScoreDataModel _model;
	
	
    /** Create a new empty document */
    public ScoreDocument() {
        super();
		systemsSelected = false;
		typesSelected = false;
		theSnapshot = null;
		readerWriter = new ReadWrite(this);
		connectionChecker = new ConnectionChecker(this);
		_model = new ScoreDataModel(this);
		selectedTypeList = new ArrayList<String>();
		selectedSystemList = new ArrayList<String>();
	}    

    // Member methods:
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public ScoreDocument(java.net.URL url) {
	this();
        if ( url == null )  return;
        setSource(url);
    }
      
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
    public void makeMainWindow() {
        mainWindow = new ScoreWindow(this, _model);
	// now that we have a window, let's read in the input file + set it up
	if(getSource() != null ) {
	    //	    readerWriter.readXMLFile(getSource());
	    readerWriter.restoreSetupFrom(getSource());
	}
    }

    
    /**
     * Save the document to the specified URL.
     * @param url URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
		String oldName, baseName, newName;
		URL newURL;
		oldName = url.toString();
		int delimeter = oldName.lastIndexOf("_"); // strip existing date on
		if(delimeter < 0)
			delimeter = oldName.lastIndexOf("."); // oh well, check for extension instead
		if(delimeter > 0)
			baseName = oldName.substring(0, delimeter);
		else 
			baseName = oldName;  // nothing to strip
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
		String datePart =  df.format(date);
		datePart = datePart.replaceAll(" ", "");
		datePart = datePart.replace('/','.');
		//df = DateFormat.getTimeInstance(DateFormat.SHORT);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		String timePart = sdf.format(date);
		timePart = timePart.replaceAll(" ", "");
		timePart = timePart.replace('/','.');
		
		newName = baseName + "_" + datePart + "-" + timePart + ".scr";
		
		String fileName = newName.substring(5,  (newName.length()));
		
		try {
			newURL = new URL(newName);
			File newFile = new File(fileName);
			setSource(newURL);   // let the framework know we switched url names!
			readerWriter.saveSetupTo( newURL );
			setHasChanges(false);
			postElogSaveEntry( fileName );
		}
		catch (MalformedURLException exc) {
			dumpErr("Oh no!, Trouble creating a new filename in SaveDocumentAs");
		}
    }
	
	
	/** Override the inherited method to simply display the application name */
	protected void generateDocumentTitle() {
		setTitle( null );	// the window title should just be the application name
	}
	
    
    /** set the ScoreSnapshot associated with this document */
    protected void setSnapshot(ScoreSnapshot snapshot) {
	    theSnapshot = snapshot;
    }
    
    /** post an elog entry */
    protected void postElogSaveEntry( final String name ) {
	    String msg = "Score save set taken:\n" + name + "\n" + theSnapshot.getComment();
	    if(msg.length() > 3999) msg = msg.substring(0, 4000);
	    try {
		    ElogUtility.defaultUtility().postEntry( Main.DEFAULT_LOGBOOK, "Score save taken", msg );
		    System.out.println(msg);		    
	    }
	    catch (Exception exc) {
		    System.err.println("Failed elog post on save " + exc.getMessage());
	    }
	    
    }
    
    /** configure commands to handle */
    public void customizeCommands( final Commander commander ) {
		// Action handler for the restore command        
        final Action restoreAction = new AbstractAction() {
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Restoring" );
				restoreIt();
			}
        };
        restoreAction.putValue( Action.NAME, "restore" );
        commander.registerAction( restoreAction );
		
		// Action handler for the sync data to  machine  + save command        
        final Action snapSaveAction = new AbstractAction() {
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Snapshot and save action" );
				snapshotIt();
			}
        };
        snapSaveAction.putValue( Action.NAME, "snapSave" );
        commander.registerAction( snapSaveAction );
		
		// Action handler for the edit red threshold action    
		final  Action redThresholdAction = new AbstractAction() {
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            public void actionPerformed( final ActionEvent event ) {
                System.out.println( "Red threshold action" );
				editPreferences();
			}
        };
        redThresholdAction.putValue( Action.NAME, "redThreshold" );
        commander.registerAction( redThresholdAction );
    }    
    
    
    /**
     * Convenience method for getting the main window cast to the proper subclass of XalWindow.
     * This allows me to avoid casting the window every time I reference it.
     * @return The main window cast to its dynamic runtime class
     */
    protected ScoreWindow myWindow() {
        return (ScoreWindow) mainWindow;
    }
	
	
	/** get the Score table */
	public JTable getScoreTable() {
		return _scoreTable;
	}
	

    /** make the tables from the selected types and system preferences */
    public void makeScoreTable() {
		_scoreTableFactory = new TableFactory( this );
		_scoreTable = _scoreTableFactory.makeTable();
		myWindow().makeScoreTable();
    }
	
	
	/** update the score table for the selected systems and types */
	public void updateScoreTable() {
		if ( _scoreTable == null )  makeScoreTable();
		_scoreTableFactory.updateTable( theSnapshot.getData().getDataTable(), selectedSystemList, selectedTypeList );
	}

    
    /** The method to restore machine settings to the selected set of PV settings */
    protected void restoreIt() {
		if(!systemsSelected || !typesSelected) 
			displayWarning("Restore Error", "Wait - you must first select desired systems + types\n before restoring settings");
		Restorer restorer = new Restorer(this, 4.);
	}
	
	/** The method to update the data in the live value columns
	 * could be used with a get-callback method instead of a monitor
	 * TODO = make this a standalone class ???
	 */
	protected void updateLiveData() {
		
		if(theSnapshot == null || theData() == null) {
			displayWarning("Sync Error", "You have not selected any data to work with yet ");
			return;
		}
		
		// the following commented code is for use if we switch to a callback 
		// get architecture:
		
		
		// get all the records for this table:
		Collection<GenericRecord> records = theSnapshot.getData().getDataTable().records();
		ScoreRecord record;
		
		// Grab all the live values & copy to saved
		Iterator<GenericRecord> itr2 = records.iterator();
		while (itr2.hasNext()) {
			record = (ScoreRecord) itr2.next();
			ChannelWrapper spChan = record.getSPChannel();
			ChannelWrapper rbChan = record.getRBChannel();
			if(spChan != null && spChan.isConnected()){
				spChan.gotValue = false;
				spChan.fireGetCallback();
			}
			if(rbChan != null && rbChan.isConnected())
			{
				rbChan.gotValue = false;
				rbChan.fireGetCallback();
			}
		}
		
		// wait a couple seconds to give channels a chance to connect if just started 
		
		connectionChecker.doCheck(4.);
		
		myWindow().tableTask();
		Date date = new Date();
		theSnapshot.setTimestamp( new Timestamp( date.getTime() ) );
		myWindow().updateSnapLabel( theSnapshot );
    }	
	
	
    /** Synchronize the live values to the reference data values */
    private void snapshotIt() {
		final String comment = SnapshotCommitController.showDialog( this );
		System.out.println( "Snapshot comment: " + comment );
		
		if ( comment == null ) {
			dumpErr( "Save was canceled by the user." );
			return;
		}
		
		if( theSnapshot == null || theSnapshot.getData() == null ) {
			displayWarning( "Sync Error", "You have not selected any data to work with yet " );
			return;
		}
		
		if ( comment.trim().length() < 1 ) {
			displayError( "Save Failed", "You must enter a comment for the new snapshot." );
			dumpErr( "You must enter a comment before saving a snapshot." );
			return;
		}
		
		try {
			// get all the records for this table:
			final Collection<GenericRecord> records = theSnapshot.getData().getDataTable().records();
			
			// Grab all the live values & copy to saved
			Iterator<GenericRecord> itr2 = records.iterator();
			while (itr2.hasNext()) {
				final ScoreRecord record = (ScoreRecord) itr2.next();
				final DataTypeAdaptor dataTypeAdaptor = record.getDataTypeAdaptor();
				ChannelWrapper spChan = record.getSPChannel();
				ChannelWrapper rbChan = record.getRBChannel();
				
				if(spChan != null && spChan.isConnected()){
					final Object value = dataTypeAdaptor.getValue( spChan );
					record.setValueForKey( dataTypeAdaptor.asString( value ), PVData.spSavedValKey );
				}
				if(rbChan != null && rbChan.isConnected())
				{
					final Object value = dataTypeAdaptor.getValue( rbChan );
					record.setValueForKey( dataTypeAdaptor.asString( value ), PVData.rbSavedValKey );
				}
			}
			
			// Update the Table saved value displays
			myWindow().tableTask();
			
			// update some info in the snapshot object for saving
			Date date = new Date();
			theSnapshot.setTimestamp( new Timestamp(date.getTime() ) );
			theSnapshot.setComment( comment );
			myWindow().updateSnapLabel( theSnapshot );
			_model.saveSnapshot( theSnapshot );
		}
		catch ( StateStoreException exception ) {
			exception.printStackTrace();
			displayError( "Save Failed", "Snapshot failed to be published.", exception );
			dumpErr( "Snapshot failed to be published." );
		}
    }
	
	
	/** method to run through the steps needed to get thiungs going and viewable after a new dataset is read */
	protected void setupFromSnapshot() {
		setChannels();
		connectionChecker.doCheck(3.);
		myWindow().updateSnapLabel( theSnapshot );
		setLists();
		myWindow().updateSelectionLists(false);
		setSelectedTypes(myWindow().typeJList.getSelectedValuesList().toArray());	
		setSelectedSystems(myWindow().systemJList.getSelectedValuesList().toArray());
		updateScoreTable();
		myWindow().selectAllTypesAndSystems();
		myWindow().showScoreTab();
	}
	
	
    /** Method to create channels for all the PV names in the dataTable 
     * toDo - move to PVData ???
     */
    protected void setChannels() {
		ScoreRecord record;
		String rbName, spName;
		if (theData().getDataTable() == null) return;
		//boolean syncStatus = Channel.getSyncRequest();
		//Channel.setSyncRequest(true);
		
		Collection<GenericRecord> records = theSnapshot.getData().getDataTable().records();
		Iterator<GenericRecord> itr = records.iterator();
		while (itr.hasNext()) {
			record = (ScoreRecord) itr.next();
			rbName = record.stringValueForKey(PVData.rbNameKey);
			spName = record.stringValueForKey(PVData.spNameKey);
			if(rbName != null && !rbName.equals("null") ){ 
				ChannelWrapper rbChannel = new ChannelWrapper(rbName);
				record.setRBChannel(rbChannel);
			}
			if(spName != null && !spName.equals("null")) {
				ChannelWrapper spChannel = new ChannelWrapper(spName);
				record.setSPChannel(spChannel);
			}
		}
		//Channel.pendIO(1.);
		Channel.flushIO();
		//Channel.setSyncRequest(syncStatus); // return as was
    }
	
    /** sets the lists containing the names of the systems and the types 
     * contained in this PV dataset */
    protected void setLists() {
		
		// first clear incase they were previously set
		if(typeList!= null) typeList.clear();
		if(systemList != null) systemList.clear();
		
		//populate the lists
		
		typeList = new TreeSet<Object>(theSnapshot.getData().getDataTable().getUniquePrimaryKeyValues("type"));
		systemList = new TreeSet<Object>(theSnapshot.getData().getDataTable().getUniquePrimaryKeyValues("system") );
		// make user select + hit button
		typesSelected = false;
		systemsSelected = false;
		
    }
	
    /** sets the selectedTypes */
    protected void setSelectedTypes(Object[] types) {
		int i = 0;
		selectedTypeList.clear();
		for (i=0; i < types.length; i++) selectedTypeList.add(i, types[i].toString());
		typesSelected = true;
		
		//if(typesSelected && systemsSelected ) makeTables(); 
    }
	
    /** sets the selectedSystems */
    protected void setSelectedSystems(Object[] systems) {
		
		int i = 0;
		selectedSystemList.clear();
		for (i=0; i < systems.length; i++)  selectedSystemList.add(i, systems[i].toString());
		systemsSelected = true;
		
		//if(typesSelected && systemsSelected ) makeTables(); 
    }
    /**
     * Edit preferences for the document.  Just set the cuttoff criteria for
     * red display
     */
    void editPreferences() {
	    
	    JFrame frame = new JFrame("Preferences");
	    frame.getContentPane().setLayout(new FlowLayout());
	    final DoubleInputTextField valueField = new DoubleInputTextField( (new  Double(cuttoffFraction)).toString());
	    frame.setLocationRelativeTo(myWindow());
	    frame.getContentPane().add(new JLabel("Cuttoff fraction for red display:"));
	    frame.getContentPane().add(valueField);
	    frame.setVisible(true);
	    frame.setSize(new Dimension(220, 100));
	    valueField.setDecimalFormat(new DecimalFormat("##.###"));
	    valueField.setMinimumSize(new Dimension(80, 25));
	    valueField.setPreferredSize(new Dimension(80, 25));
	    
	    valueField.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cuttoffFraction = valueField.getValue();
            }
			
		});
		
    }
    
    /** convenience method to get underlying data */
    protected PVData theData() { 
	    return theSnapshot.getData();
    }
    
    /** 
     * method to dump an error message and sound the alarm 
     * @param msg  error message 
     */
    public void dumpErr(String msg) {
		Toolkit.getDefaultToolkit().beep();
		myWindow().errorText.setText(msg);
		System.err.println(msg);	    
    }
}
