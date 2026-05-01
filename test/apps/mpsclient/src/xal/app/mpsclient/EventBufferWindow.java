/*
 * EventBufferWindow.java
 *
 * Created on Wed Apr 14 17:03:50 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.mpsclient;

import xal.extension.application.*;

import java.util.Iterator;
import java.util.Date;
import java.io.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


/**
 * EventBufferWindow
 *
 * @author  tap
 */
public class EventBufferWindow extends XalWindow implements SwingConstants {
    
    private static final long serialVersionUID = 1L;
    
	/** date formatter for displaying timestamps */
	static final protected DateFormat TIME_FORMAT;
	
	final protected RemoteMPSRecord _requestHandler;
	final protected int _mpsType;
	
	// data
	protected java.util.List<MPSEvent> _mpsEvents;
	
	// components
	protected Container _eventContainer;
	protected JFileChooser _fileChooser;
	
	
	/**
	 * static initializer
	 */
	static {
		TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss zz");
	}
	
	
	/**
	 * Constructor
	 */
	public EventBufferWindow(EventBufferDocument aDocument, RemoteMPSRecord handler, int mpsType) {
		super(aDocument);
		
		_requestHandler = handler;
		_mpsType = mpsType;
		
		_fileChooser = new JFileChooser();

		makeContents();
	}
    
	
	/**
	 * Dispose of custom window resources.  Subclasses should override this method
	 * to provide custom disposal of resources.  The default implementation does nothing.
	 */
	public void freeCustomResources() {
		_eventContainer.removeAll();
		_mpsEvents.clear();
		_mpsEvents = null;
	}
	
	
    /**
     * Do not use a toolbar.
	 * @return false
     */
    public boolean usesToolbar() {
        return true;
    }
	
	
	/**
	 * Make the window content
	 */
	protected void makeContents() {
        setSize(600, 800);
		
		Box mainView = new Box(BoxLayout.Y_AXIS);
		getContentPane().add(mainView);
		
		_eventContainer = new Box(BoxLayout.Y_AXIS);
		mainView.add( new JScrollPane(_eventContainer) );
		
		refreshEvents();
	}
	
    
    /**
     * Register actions specific to this window instance.
     * @param commander The commander with which to register the custom commands.
     */
    public void customizeCommands(Commander commander) {
		// define the "refresh events" action
        commander.registerAction( new AbstractAction("refresh-events") {
            
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
				refreshEvents();
            }
        });
		
		// define the "dump buffer" action
        commander.registerAction( new AbstractAction("dump-text") {
            
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
				dumpBuffer();
            }
        });
		
		// define the "dump HTML" action
        commander.registerAction( new AbstractAction("dump-html") {
            
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent event) {
				dumpHTML();
            }
        });
	}
	
	
	/**
	 * Convenience method for getting this window's document cast as an EventBufferDocument.
	 * @return this window's document
	 */
	protected EventBufferDocument bufferDocument() {
		return (EventBufferDocument)document;
	}
	
	
	
	/**
	 * Refresh the MPS event list by fetching the latest events.
	 */
	protected void refreshEvents() {
		_mpsEvents = _requestHandler.processMPSEvents(_mpsType);
		_eventContainer.removeAll();
		
		for ( Iterator<MPSEvent> iter = _mpsEvents.iterator() ; iter.hasNext() ; ) {
			MPSEvent event = iter.next();
			_eventContainer.add( new JLabel( event.getTimestamp().toString() ) );
			JTable eventTable = new JTable(new MPSEventTableModel(event));
			_eventContainer.add( eventTable.getTableHeader() );
			_eventContainer.add(eventTable);
			_eventContainer.add( Box.createVerticalStrut( eventTable.getRowHeight() ) );
		}
		_eventContainer.validate();
		//document.setHasChanges(true);
	}
	
	
	/**
	 * Generate an HTML description of the events and write them to the file.
	 * @param file The file to which to write the HTML.
	 */
	public void writeHTMLTo(File file) throws IOException {
		String text = getHTML();
		FileWriter writer = new FileWriter(file);
		writer.write(text, 0, text.length());
		writer.flush();
	}
	
	
	/**
	 * Generate a plain text description of the events and write them to the file.
	 * @param file The file to which to write the description.
	 */
	public void writeTextTo(File file) throws IOException {
		String text = getBufferText();
		FileWriter writer = new FileWriter(file);
		writer.write(text, 0, text.length());
		writer.flush();
	}
	
	
	/**
	 * Display a file chooser to the user and dump a plain text description of the events to the selected file.
	 */
	protected void dumpBuffer() {		
		try {
			_fileChooser.setSelectedFile( new File(_fileChooser.getCurrentDirectory(), "Untitled.txt") );
			int status = _fileChooser.showSaveDialog(this);
			switch(status) {
				case JFileChooser.APPROVE_OPTION:
					File selectedFile = _fileChooser.getSelectedFile();
					if ( selectedFile.exists() ) {
						int confirm = displayConfirmDialog("Warning", "The selected file:  " + selectedFile + " already exists! \n Overwrite selection?");
						if ( confirm == NO_OPTION )  return;
					}
					else {
						selectedFile.createNewFile();
					}
					writeTextTo(selectedFile);
					break;
				default:
					break;
			}
		}
		catch(Exception exception) {
			displayError("Save Error", "Error saving file: ", exception);
		}
	}
	
	
	/**
	 * Display a file chooser to the user and dump an HTML description of the events to the selected file.
	 */
	protected void dumpHTML() {		
		try {
			_fileChooser.setSelectedFile( new File(_fileChooser.getCurrentDirectory(), "Untitled.html") );
			int status = _fileChooser.showSaveDialog(this);
			switch(status) {
				case JFileChooser.APPROVE_OPTION:
					File selectedFile = _fileChooser.getSelectedFile();
					if ( selectedFile.exists() ) {
						int confirm = displayConfirmDialog("Warning", "The selected file:  " + selectedFile + " already exists! \n Overwrite selection?");
						if ( confirm == NO_OPTION )  return;
					}
					else {
						selectedFile.createNewFile();
					}
					writeHTMLTo(selectedFile);
					break;
				default:
					break;
			}
		}
		catch(Exception exception) {
			displayError("Save Error", "Error saving file: ", exception);
		}
	}
	
	
	/**
	 * Get a plain text description of the events in the buffer.
	 * @return a plain text description of the events in the buffer.
	 */
	protected String getBufferText() {
		StringBuffer textBuffer = new StringBuffer();
		textBuffer.append(_requestHandler.getMPSTypes().get(_mpsType).toString());
		textBuffer.append( " MPS Event Buffer - " + TIME_FORMAT.format(new Date()) + "\n\n" );
		for ( Iterator<MPSEvent> iter = _mpsEvents.iterator() ; iter.hasNext() ; ) {
			textBuffer.append(iter.next() + "\n");
		}
		
		return textBuffer.toString();
	}
	
	
	/**
	 * Get an HTML description of the events in the buffer.
	 * @return an HTML description of the events in the buffer.
	 */
	protected String getHTML() {
		StringBuffer textBuffer = new StringBuffer();
		String title = _requestHandler.getMPSTypes().get(_mpsType).toString() + " MPS Event Buffer";
		textBuffer.append("<html>\n<head>\n<title>" + title + " </title>");
		textBuffer.append("</head>\n<body>\n");
		textBuffer.append("<center><u> " + title + " </u></center><br>");
		textBuffer.append("<center> " + TIME_FORMAT.format(new Date()) + " </center><br>");
		textBuffer.append("<center><table border=\"1\" cellpadding=\"2\" cellspacing=\"2\">\n");
		textBuffer.append("<tr> <th>Mean Time</th> <th>Signal</th> <th>Timestamp</th> </tr>\n");
		for ( Iterator<MPSEvent> iter = _mpsEvents.iterator() ; iter.hasNext() ; ) {
			MPSEvent event = iter.next();
			java.util.List<SignalEvent> signalEvents = event.getSignalEvents();
			final int numRows = signalEvents.size(); 
			
			for ( int row = 0 ; row < numRows ; row++ ) {
				SignalEvent signalEvent = signalEvents.get(row);
				textBuffer.append("<tr>");
				
				if ( row == 0 ) {
					textBuffer.append("<td valign=\"top\" rowspan=\"" + numRows + "\">" + TIME_FORMAT.format(event.getTimestamp()) + "</td> ");
				}
				textBuffer.append("<td>" + signalEvent.getSignal() + "</td> ");
				textBuffer.append("<td>" + signalEvent.getTimestamp() + "</td> ");
				
				textBuffer.append("</tr>\n");
			}
			textBuffer.append("<tr> <td colspan=\"3\"></td> </tr>\n");
		}
		textBuffer.append("</table></center>\n");
		textBuffer.append("</body>\n</html>");
		
		return textBuffer.toString();
	}
}

