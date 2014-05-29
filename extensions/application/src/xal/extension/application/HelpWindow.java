/*
 * HelpWindow.java
 *
 * Created on March 26, 2002, 2:03 PM
 */

package xal.extension.application;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.Cursor;
import java.awt.event.*;
import java.awt.Component;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.logging.*;


/**
 * The help window that displays documentation on the application.  There is 
 * only one help window for the entire application.
 *
 * @author  tap
 */
class HelpWindow extends JFrame implements SwingConstants {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

	/** name for the help starting point resource which may or may not exist */
	static public final String HELP_START_RESOURCE = "Help.html";


    // -------- static variables -----------------------------------------------
	final static private URL _homePage;
    static private HelpWindow _helpWindow;
    static private JTextPane _textPane;
    
    // -------- instance variables ---------------------------------------------
    private boolean _neverShown;
	private LinkedList<URL> _pageHistory;
	private int _pageHistoryIndex;
	
	// visual components
	private JButton _backButton;
	private JButton _forwardButton;
	private JButton _homeButton;
    
    
    static {
        _homePage = getHelpSource();
    }
    

    /** Creates new form HelpWindow */
    public HelpWindow() {
		_pageHistory = new LinkedList<URL>();
		
        makeView();
        _neverShown = true;
        setTitle( Application.getAdaptor().applicationName() + " - Help" );
        loadHome();
    }
    
    
    /**
     * Load the help contents from a file into a text pane of the help window.
     * @param helpSource The URL of the help source contents.
     */
    private void loadHome() {
        try {
            _textPane.setPage( _homePage );
            _textPane.setEditable( false );
			_pageHistory.add( _homePage );
			_pageHistoryIndex = 0;
			updateView();
        }
        catch( java.io.IOException exception ) {
			Logger.getLogger("global").log( Level.SEVERE, "Error loading the help page.", exception );
            System.err.println( exception );
            exception.printStackTrace();
            JOptionPane.showMessageDialog( this, exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE );
        }
    }
	
	
	/**
	 * Test whether we can navigate forward in history.
	 * @return true if we can navigate forward in history and false if not.
	 */
	public boolean canNavigateForward() {
		return _pageHistoryIndex < ( _pageHistory.size() - 1 );
	}
	
	
	/**
	 * Test whether we can navigate back in history.
	 * @return true if we can navigate back in history and false if not.
	 */
	public boolean canNavigateBack() {
		return _pageHistoryIndex > 0;
	}
	
	
	/**
	 * Load the page whose history index is the specified index.
	 * @param index The history index of the page to load.
	 */
	public void goToPage( final int index ) {
		final URL link = _pageHistory.get( index );
		
		final Cursor lastCursor = getCursor();
		try {
			setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
			_textPane.setPage( link );
			EditorKit editorKit = _textPane.getEditorKit();
			_pageHistoryIndex = index;
			updateView();
		}
		catch(java.io.IOException exception) {
			final String message = "Help is unable to hyperlink to " + link;
			Logger.getLogger("global").log( Level.WARNING, message, exception );
			System.err.println( message );
			exception.printStackTrace();
			Application.displayError( "Link Error", "Error accessing link:", exception );
		}
		finally {
			setCursor( lastCursor );
		}
	}
	
	
	/**
	 * Load the page corresponding to the link whose history index is offset from the present history 
	 * index by the specified amount.
	 * @param increment the offset from the present history index.
	 */
	public void incrementPage( final int increment ) {
		goToPage( _pageHistoryIndex + increment );
	}
	
	
	/**
	 * Load the contents of the specified link into the text pane.
	 * @param link The URL of the link to load.
	 */
	public void loadLink( final URL link ) {
		_pageHistory = new LinkedList<URL>( _pageHistory.subList( 0, _pageHistoryIndex + 1 ) );
		_pageHistory.add( link );
		goToPage( _pageHistoryIndex + 1 );
	}
    
    
    /**
     * Enable the HelpWindow if and only if there is a URL loaded for the help page.
     * @return true if the HelpWindow is enabled and false otherwise.
     */
    static boolean isAvailable() {
        return _homePage != null;
    }
    
    
    /**
     * Get the source URL for the help contents.
     * @return The URL of the help contents.
     */
    static private URL getHelpSource() {
        return Application.getAdaptor().getResourceURL( HELP_START_RESOURCE );
    }
    
    
    /**
     * Show the help window near the specified component if the help window has 
     * never been shown before.  Once it has been shown, then show the help 
     * window in the last place the user left it.  However, if the help window is on a 
     * different screen than the sender, bring the help window back relative to the sender.
     * @param sender The component near which the help window should be shown
     */
    private void showWindowNear( final Component sender ) {
        if ( _neverShown ) {
            setLocationRelativeTo( sender );
            _neverShown = false;
        }
        else if ( !sender.getGraphicsConfiguration().getDevice().getIDstring().equals(getGraphicsConfiguration().getDevice().getIDstring()) ) {
            // if the help window is on a different screen bring it to the same screen as the sender
            setVisible( false );
            setLocationRelativeTo( sender );
        }
        
        setState( java.awt.Frame.NORMAL );    // don't iconify this window
        setVisible( true );   // make the window visible
    }
    
    
    /**
     * Static method for showing the single help window instance near the 
     * specified component.  It simply calls showWindowNear() on the single instance 
     * of the help window.  See that method for details on the behavior of this 
     * method.
     * @param sender The component near which the help window should be shown
     */
    static public void showNear( final Component sender ) {
		if ( _helpWindow == null ) {
			_helpWindow = new HelpWindow();
		}
		_helpWindow.showWindowNear( sender );
    }
	
	
	/**
	 * Update the view to reflect the present state.
	 */
	private void updateView() {
		_backButton.setEnabled( canNavigateBack() );
		_forwardButton.setEnabled( canNavigateForward() );
	}
    
    
    /**
     * Make the view that displays the help contents within the help window.
     */
    private void makeView() {		
        JScrollPane scrollPane = new JScrollPane();
        _textPane = new JTextPane();
		
        setTitle("Help");
		
		Box buttonRow = new Box( BoxLayout.X_AXIS );
		
		_backButton = new JButton( "<" );
		_backButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				incrementPage( -1 );
			}
		});
		
		_forwardButton = new JButton( ">" );
		_forwardButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				incrementPage( 1 );
			}
		});
		
		_homeButton = new JButton( "Home" );
		_homeButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				loadLink( _homePage );
			}
		});
		
		buttonRow.add( _backButton );
		buttonRow.add( _forwardButton );
		buttonRow.add(  Box.createHorizontalStrut(5) );
		buttonRow.add( _homeButton );
		buttonRow.add( Box.createHorizontalGlue() );
		
        scrollPane.setPreferredSize( new java.awt.Dimension( 600, 400 ) );
        _textPane.setEditable( false );
        _textPane.setFont( new java.awt.Font( "TimesNewRoman", 0, 12 ) );
        _textPane.setPreferredSize( new java.awt.Dimension( 6, 6 ) );
        
        _textPane.addHyperlinkListener( new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate( javax.swing.event.HyperlinkEvent event ) {
                handleHyperlink( event );
            }
        });
		
        scrollPane.setViewportView( _textPane );
		
        JSplitPane mainView = new JSplitPane( JSplitPane.VERTICAL_SPLIT, true, buttonRow, scrollPane );
		mainView.setDividerSize( 1 );
		mainView.setResizeWeight( 0.0 );
        mainView.setOneTouchExpandable( false );
		mainView.setEnabled( false );
		getContentPane().add( mainView );

        pack();
    }

    
    /**
     * Handle the hyperlink event generated when the user activates a hyperlink
     * within the help contents.  The event is handled by displaying the target
     * of the hyperlink in the help window.
     */
    private void handleHyperlink( final javax.swing.event.HyperlinkEvent event ) {
		if ( event instanceof HTMLFrameHyperlinkEvent ) {
			Cursor lastCursor = getCursor();
			setCursor( new Cursor( Cursor.WAIT_CURSOR ) );
			HTMLDocument document = (HTMLDocument)_textPane.getDocument();
			document.processHTMLFrameHyperlinkEvent( (HTMLFrameHyperlinkEvent)event );
			setCursor( lastCursor );
		}
		else if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED ) {
			loadLink( event.getURL() );
		}
    }
}
