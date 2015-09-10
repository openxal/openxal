//
//  SummaryController.java
//  xal
//
//  Created by Thomas Pelaia on 9/19/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.extension.bricks.WindowReference;
import xal.extension.logbook.ElogUtility;
import xal.tools.messaging.MessageCenter;
import xal.service.pvlogger.RemoteLoggingCenter;

import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;


/** Controller for managing the summary view */
public class SummaryController extends AbstractController {
    /** document for the text pane in the main window. */
    final protected PlainDocument SUMMARY_DOCUMENT;
	
	/** text view */
	final protected JTextArea SUMMARY_VIEW;
	
	/** entry title field */
	final protected JTextField TITLE_FIELD;
	
	/** check box indicating whether the entry is a summary */
	final protected JCheckBox SUMMARY_CHECKBOX;
	
	/** remote PV Logger*/
	final protected RemoteLoggingCenter REMOTE_PV_LOGGER;
	
	
    /** Create a new controller */
    public SummaryController( final MessageCenter messageCenter, final WindowReference windowReference ) {
		super( messageCenter, windowReference );
		
        SUMMARY_DOCUMENT = makeSummaryTextDocument();
		SUMMARY_VIEW = makeSummaryTextView();
		SUMMARY_CHECKBOX = (JCheckBox)windowReference.getView( "ShiftSummaryCheckBox" );
		
		TITLE_FIELD = (JTextField)windowReference.getView( "TitleField" );
		
		supportMachineSummary( windowReference );
		
		REMOTE_PV_LOGGER = new RemoteLoggingCenter();
		supportPVLoggerSnapshot( windowReference );
    }
	
	
	/**
	 * Get the title
	 * @return the entry title
	 */
	public String getTitle() {
		return TITLE_FIELD.getText();
	}
	
	
	/**
	 * Get the summary text
	 * @return the summary text
	 */
	public String getSummary() {
		return SUMMARY_VIEW.getText();
	}
	
	
	/**
	 * Determine if the entry is marked as a shift summary
	 * @return true if the entry is marked as a shift summary and false if not
	 */
	public boolean isShiftSummary() {
		return SUMMARY_CHECKBOX.isSelected();
	}
	
	
	/**
	 * Validate whether the summary is valid
	 * @return true if the summary is valid and false if not
	 */
	public boolean validate() {
		final int titleLength = getTitle().length();
		if ( titleLength == 0 ) {
			_validationText = "Summary title must have at least one character.";
			return false;
		}
		else if ( titleLength > ElogUtility.DEFAULT_MAX_TITLE_SIZE ) {
			_validationText = "Summary title length must be less than " + ElogUtility.DEFAULT_MAX_TITLE_SIZE + " characters.  This title has " + titleLength;
			return false;
		}
		
		final int summaryLength = getSummary().length();
		if ( summaryLength == 0 ) {
			_validationText = "Summary must have at least one character.";
			return false;
		}
		else if ( summaryLength > ElogUtility.DEFAULT_MAX_BODY_SIZE ) {
			_validationText = "Summary length must be less than " + ElogUtility.DEFAULT_MAX_BODY_SIZE + " characters.  This summary has " + summaryLength;
			return false;
		}
		
		_validationText = "Okay";
		return true;
	}
	
	
	/** create a text view */
	private JTextArea makeSummaryTextView() {
		final JTextArea textView = (JTextArea)WINDOW_REFERENCE.getView( "SummaryTextArea" );
		
		textView.setLineWrap( true );
		textView.setWrapStyleWord( true );
		textView.setDocument( SUMMARY_DOCUMENT );
		
		return textView;
	}
    
	
    /** Instantiate a new PlainDocument that servers as the document for the text pane. */
    private PlainDocument makeSummaryTextDocument() {
		final PlainDocument textDocument = new PlainDocument();
		
        textDocument.addDocumentListener( new DocumentListener() {
            public void changedUpdate( final DocumentEvent event ) {
				postDocumentChangeEvent();
            }
            public void removeUpdate( final DocumentEvent event ) {
				postDocumentChangeEvent();
            }
            public void insertUpdate( final DocumentEvent event ) {
				postDocumentChangeEvent();
            }
        });
		
		return textDocument;
    }
	
	
	/** support machine summary */
	protected void supportMachineSummary( final WindowReference windowReference ) {
		final MachineSummarizer summarizer = new MachineSummarizer();
		final JButton summarizeButton = (JButton)windowReference.getView( "MachineSummarizeButton" );
		summarizeButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				final String machineSummary = summarizer.getMachineSummary();
				final StringSelection textSelection = new StringSelection( machineSummary );
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents( textSelection, null );
				SUMMARY_VIEW.paste();
			}
		});
	}
	
	
	/** support PV Logger snapshot */
	protected void supportPVLoggerSnapshot( final WindowReference windowReference ) {
		final JButton snapshotButton = (JButton)windowReference.getView( "PVLoggerSnapshotButton" );
		snapshotButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) { takeAndPublishSnapshot( windowReference ); }
		});
	}
	
	
	/** take and publish a PV Loggers snapshot using the default PV Logger service */
	private void takeAndPublishSnapshot( final WindowReference windowReference ) {
		final java.awt.Window window = windowReference.getWindow();
		final String comment = JOptionPane.showInputDialog( window, "Snapshot Comment: ", "Lab Book Snapshot" );
		if ( comment != null ) {
			final long snapshotID = REMOTE_PV_LOGGER.takeAndPublishSnapshot( "default", comment );
			if ( snapshotID > 0 ) {
				final String snapshotText = "PV Logger Snapshot (" + comment + ") ID: " + snapshotID;
				final StringSelection textSelection = new StringSelection( snapshotText );
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents( textSelection, null );
				SUMMARY_VIEW.paste();
			}
			else if ( snapshotID == -1 ) {
				JOptionPane.showMessageDialog( window, "Could not locate the default PV Logger service.", "Snapshot Exception", JOptionPane.WARNING_MESSAGE );
			}
			else {
				JOptionPane.showMessageDialog( window, "Exception while taking a snapshot.", "Snapshot Exception", JOptionPane.WARNING_MESSAGE );
			}
		}
	}
}
