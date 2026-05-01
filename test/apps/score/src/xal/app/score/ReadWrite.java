/*
 * ReaderWriter.java
 *
 * Created on 8/12/2003, 1:32 PM
 */

package xal.app.score;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.sql.Timestamp;

import xal.tools.xml.*;
import xal.tools.data.*;

/**
 * This class handles reading and wwriting score data to and from xml files.
 * This functionality is no longer used - rather a database  storage of
 * score data is preferred. This functionality is kept for backward
 * compatibility. It has not been kept up, but may still work.
 * @author  jdg
 */
public class ReadWrite {   

    // Members:

    private ScoreDocument theDoc;

    /** the contstructor */
    public ReadWrite(ScoreDocument doc) {
	theDoc = doc;
    }


    // methods

   /** parse the app setup info from a save file */

    public void  restoreSetupFrom(URL url) {
	readXMLFile(url);
	theDoc.setupFromSnapshot();
    }

    /** read input dataTable from the default main file 
     * @param url - the url of the file to read
     */

    public void  readXMLFile(URL url) {
	ScoreSnapshot ss = new ScoreSnapshot();
	XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(url, false);
	DataAdaptor da1 = xda.childAdaptor("score");
	DataAdaptor daLevel2a = da1.childAdaptor("title");
	ss.setType(daLevel2a.stringValue("name"));
	
	
/*	long time =  daLevel2a.longValue("date");
	if(time > 0)
	    ss.setTimestamp( new Timestamp(time) );
	else ss.setTimestamp(null);
*/
	// need to parse the SQL timestamp from a string
	if (daLevel2a.stringValue("date") != null) {
		Timestamp ts = Timestamp.valueOf(daLevel2a.stringValue("date"));
		ss.setTimestamp(ts);
	} else {
		ss.setTimestamp(null);
	}
	
	
	String comment = daLevel2a.stringValue("comment");
	if(comment != null) {
	    ss.setComment(comment);
	}

	ss.setData(new PVData(ss.getType()));
	DataAdaptor daLevel2b = da1.childAdaptor("data");
	ss.getData().getDataTable().dataHandler().update(daLevel2b);

	// check to see if this is an old depreciated file type. If so convert.:
	try {
		if(ss.getData().getDataTable().getRecordClass() == Class.forName("xal.tools.data.GenericRecord") ) {
			System.out.println("Warning: updating depreciated old dataType class");
			// create an empty new table with right schema:
			DataTable newTable = theDoc.theData().newDataTable("main");
			theDoc.theData().setDataTable(newTable);
			// fill up the new empty table
			DataTable tempTable = new DataTable(daLevel2b);
			Collection<GenericRecord> records = tempTable.records();
			Iterator<GenericRecord> itr = records.iterator();
			String rbPV, spPV, sName, tName;
			while (itr.hasNext()) {
				final GenericRecord record = itr.next();
				rbPV = record.stringValueForKey(PVData.rbNameKey);
				spPV = record.stringValueForKey(PVData.spNameKey);
				sName = record.stringValueForKey(PVData.systemKey);
				tName = record.stringValueForKey(PVData.typeKey);
				final String dataType = record.stringValueForKey( PVData.DATA_TYPE_KEY );
				final String spVal = record.stringValueForKey(PVData.spSavedValKey);
				final String rbVal = record.stringValueForKey(PVData.rbSavedValKey);	
				theDoc.theData().addRecord(sName, tName, dataType, spPV, spVal, rbPV, rbVal);
			}
			ss.getData().setDataTable(newTable);
		  }

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Trouble reading a DataTable with unkown record types");
	}
	theDoc.setSnapshot(ss);
    }


    /** method to write the application setup to a file 
     * @param url - the url of the file to write to (will overwrite)
     */

    public void saveSetupTo(URL url) {
		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor daLevel1 = xda.createChild("score");
		DataAdaptor daLevel2a = daLevel1.createChild("title");
		daLevel2a.setValue("name", theDoc.theSnapshot.getType());
		daLevel2a.setValue("date", theDoc.theSnapshot.getTimestamp());
		daLevel2a.setValue( "comment", theDoc.theSnapshot.getComment() );
		
		DataAdaptor daLevel2b = daLevel1.createChild("data");
		theDoc.theData().getDataTable().dataHandler().write(daLevel2b);
		xda.writeToUrl(url);
    }


}
