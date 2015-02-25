/*
 * MTVDocument.java
 *
 * Created on 11/1/2005, 
 */

package xal.app.mtv;

import java.net.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;

/**
 * MyDocument the managing unit for objecs used by an xio instance
 * 
 * @author jdg
 */

public class MTVDocument extends AcceleratorDocument {

	/** the helper class to save and open documents to/from xml files */
        private SaveOpen saveOpen;
			
	private Hashtable<String,PVTableCell>	 hashtable_cells = new Hashtable<String,PVTableCell>();
	
	/** Create a new empty document */
	public MTVDocument() {
		//this(null);
		saveOpen = new SaveOpen(this);
	}

	/**
	 * Create a new document loaded from the URL file
	 * 
	 * @param url
	 *            The URL of the file to load into the new document.
	 */
	public MTVDocument(java.net.URL url) {
		this();
		if (url == null) {
			return;
		} else {
			System.out.println("Opening document: " + url.toString());
			setSource(url);
		}
	}

	/**
	 * Make a main window by instantiating the my custom window. set up any
	 * settings, tables etc. as defined in the input file (if any)
	 */
	public void makeMainWindow() {
		mainWindow = new MTVWindow(this);

		// now that we have a window, let's read in the input file + set it up
		if (getSource() != null)
			saveOpen.readSetupFrom(getSource());

	}

	
	public Hashtable<String,PVTableCell>	 getCellHashtable(){
		return hashtable_cells;
	}
	
	/**
	 * Save the document to the specified URL.
	 * @param url The URL to which the document should be saved.
	 */
	public void saveDocumentAs(URL url) {
		saveOpen.saveTo(url);
		setHasChanges(false);
	}

	/**
	 * Convenience method for getting the main window cast to the proper
	 * subclass of XalWindow. This allows me to avoid casting the window every
	 * time I reference it.
	 * 
	 * @return The main window cast to its dynamic runtime class
	 */
	protected MTVWindow myWindow() {
		return (MTVWindow) mainWindow;
	}

	/**
	 * only let the sequence get picked once per document
	 */
	public void selectedSequenceChanged() {
		myWindow().getSeqLabel().setText(
				"Selected Sequence is " + getSelectedSequence().getId());
		myWindow().updateTabs();

	}

	/**
	 * set the accelerator seq. combination to use and select device types +
	 * desired associated signals
	 */

	public void setTables() {

	}

	// ------- the commands to listen to ------

	public void customizeCommands(Commander commander) {

	}

}
