/*
 * PatternEventPoster.java
 *
 * Created on Wed May 12 09:10:52 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.pvlogbrowser.patternfilter;


import xal.tools.messaging.MessageCenter;
import xal.app.pvlogbrowser.apputils.Lock;

import java.util.regex.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.SwingUtilities;


/**
 * PatternEventPoster is a tool for monitoring text changes in a javax.swing.Document, constructing
 * a pattern from the text and then posting the pattern for use in filtering items by name.
 * Most often this will be used to monitor a text field and generate a pattern filter based on 
 * the user's input.  The pattern filter may be used to filter a JList or JTable or other list of 
 * named items in real time.
 *
 * @author  tap
 */
public class PatternEventPoster {
	protected MessageCenter _messageCenter;
	protected PatternChangeListener _proxy;
	
	final protected Object QUEUE_LOCK;
	final protected Lock PROCESS_LOCK;
	protected volatile String _queueText = "";
	protected volatile boolean _queueExists;
	
	protected DocumentHandler _documentHandler;
	protected Document _document;
	protected volatile Pattern _pattern;
	protected PatternGenerator _patternGenerator;
	protected int _patternFlags;
	
	
	/**
	 * Primary constructor
	 * @param document The document to monitor for text changes
	 * @param patternGenerator the generator used to convert the document text to a pattern
	 * @param patternFlags the pattern flags used to compile a pattern using Pattern.compile()
	 * @see java.util.regex.Pattern
	 */
	public PatternEventPoster( final Document document, final PatternGenerator patternGenerator, final int patternFlags ) {
		QUEUE_LOCK = new Object();
		PROCESS_LOCK = new Lock();
		
		_messageCenter = new MessageCenter( "Pattern Event Poster" );
		_proxy = (PatternChangeListener)_messageCenter.registerSource( this, PatternChangeListener.class );
		
		_queueText = "";
		_queueExists = false;
		
		_patternFlags = patternFlags;
		_patternGenerator = patternGenerator;
		_documentHandler = new DocumentHandler();
		setDocument( document );
	}
	
	
	/**
	 * Constructor which uses the pattern generator for checking if the matching text is 
	 * contained in the items.
	 * @param document The document to monitor for text changes
	 * @param patternFlags the pattern flags used to compile a pattern using Pattern.compile()
	 */
	public PatternEventPoster( final Document document, final int patternFlags ) {
		this( document, PatternGeneratorFactory.makeContainsGenerator(), patternFlags );
	}
	
	
	/**
	 * Constructor which uses the pattern generator for checking if the matching text is 
	 * contained in the items.  The pattern flags are set to zero, but the case sensitivity
	 * flag is specified using the boolean flag.
	 * @param document The document to monitor for text changes
	 * @param caseSensitive true to enforce case sensitive matching and false to ignore it
	 */
	public PatternEventPoster( final Document document, final boolean caseSensitive ) {
		this( document, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
	}
	
	
	/**
	 * Constructor which uses the pattern generator for checking if the matching text is 
	 * contained in the items.  The pattern flags are set to zero, but the case sensitivity
	 * flag is specified using the boolean flag.
	 * @param document The document to monitor for text changes
	 * @param patternGenerator the generator used to convert the document text to a pattern
	 * @param caseSensitive true to enforce case sensitive matching and false to ignore it
	 */
	public PatternEventPoster( final Document document, final PatternGenerator patternGenerator, final boolean caseSensitive ) {
		this( document, patternGenerator, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
	}
	
	
	/**
	 * Constructor which uses the pattern generator for checking if the matching text is 
	 * contained in the items.  The pattern flags are set to zero.
	 * @param document The document to monitor for text changes
	 */
	public PatternEventPoster( final Document document ) {
		this(document, 0);
	}
	
	
	/**
	 * Add a listener of pattern change events
	 * @param listener the listener to add for receiving pattern change events
	 */
	public void addPatternChangeListener( final PatternChangeListener listener ) {
		_messageCenter.registerTarget( listener, this, PatternChangeListener.class );
	}
	
	
	/**
	 * Remove a listener of pattern change events
	 * @param listener the listener to remove from receiving pattern change events
	 */
	public void removePatternChangeListener( final PatternChangeListener listener ) {
		_messageCenter.removeTarget( listener, this, PatternChangeListener.class );
	}
	
	
	/**
	 * Get the pattern
	 * @return the pattern based on the present text in the monitored document
	 */
	public Pattern getPattern() {
		return _pattern;
	}
	
	
	/**
	 * Update the pattern based on the latest document text and post the pattern change event 
	 * to the listeners.
	 */
	protected void updatePattern() {
		if ( PROCESS_LOCK.tryLock() ) {
			try {
				String text;
				synchronized( QUEUE_LOCK ) {
					text = _queueText;
					_queueExists = false;
				}
				// replace special characters with the appropriate regular expression
				text = text.replaceAll( "\\\\", "\\\\\\\\" );
				text = text.replaceAll( "\\(", "\\\\(" );
				text = text.replaceAll( "\\)", "\\\\)" );
				text = text.replaceAll( "\\[", "\\\\[" );
				
				_pattern = _patternGenerator.compilePattern( text, _patternFlags );							
				
				// be sure to post events in the event dispatch thread
				if ( SwingUtilities.isEventDispatchThread() ) {
					_proxy.patternChanged( PatternEventPoster.this, _pattern, false );					
				}
				else {
					try {
						SwingUtilities.invokeAndWait( new Runnable() {
							public void run() {
								_proxy.patternChanged( PatternEventPoster.this, _pattern, false );
							}
						});
					}
					catch ( Exception exception ) {
						throw new RuntimeException( exception );
					}					
				}
			}
			finally {
				PROCESS_LOCK.unlock();
				if ( _queueExists )  updatePattern();
			}
		}
	}
	
	
	/**
	 * Spawn a pattern update in a new thread.
	 */
	protected void spawnUpdatePattern() {
		new Thread( new Runnable() {
			public void run() {
				updatePattern();
			}
		}).start();
	}
	
	
	/**
	 * Update the text from the monitored document and then update the pattern accordingly
	 */
	protected void updateText() {
		synchronized( QUEUE_LOCK ) {
			_queueText = getDocumentText();
			_queueExists = true;
		}
	}
	
	
	/**
	 * Get the text from the monitored document.
	 * @return the text from the monitored document.
	 */
	protected String getDocumentText() {
		try {
			return _document.getText( 0, _document.getLength() );
		}
		catch( BadLocationException exception ) {
			return "";
		}
	}
	
	
	/**
	 * Set the document to monitor for the text that generates the pattern filters.
	 * @param document the new document to monitor for text patterns
	 */
	protected void setDocument( final Document document ) {
		if (_document != null ) {
			_document.removeDocumentListener( _documentHandler );
		}
		_document = document;
		if ( document != null ) {
			document.addDocumentListener( _documentHandler );
			updateText();
			updatePattern();
		}
	}
	
	
	
	/**
	 * Internal class for monitoring a document's text and handling the events by updating the 
	 * text which forces an update of the pattern filter.
	 */
	protected class DocumentHandler implements DocumentListener {
		/**
		 * Event indicating that the document's text has changed.
		 * @param event the document event
		 */
		public void changedUpdate( final DocumentEvent event ) {
			updateText();
			spawnUpdatePattern();
		}
		
		
		/**
		 * Event indicating that text has been inserted into the document.
		 * @param event the document event
		 */
		public void insertUpdate( final DocumentEvent event ) {
			updateText();
			spawnUpdatePattern();
		}
		
		
		/**
		 * Event indicating that text has been removed from the document.
		 * @param event the document event
		 */
		public void removeUpdate( final DocumentEvent event ) {
			updateText();
			spawnUpdatePattern();
		}
	}
}

