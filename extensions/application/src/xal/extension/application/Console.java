/*
 * Console.java
 *
 * Created on March 18, 2003, 1:42 PM
 */

package xal.extension.application;

import xal.tools.IconLib;

import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Container;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;


/**
 * The Console captures standard output and standard error streams.  Both are displayed in a console window.  One console serves the entire application.
 * Standard output appears in black text while standard error appears in red text.
 * @author  t6p
 */
class Console {
	/** console character limit */
	final static private int CHAR_LIMIT = 250000;
	
    /** the console instance */
    final static private Console CONSOLE;
	
	/** preference key for logging */
	final static private String LOGGING_KEY = "LogOutput";
	
	/** logging preferences */
	final static private Preferences LOG_PREFS;
    
	/** file writer where log files are stored */
	private Writer _logWriter;
	
	/** indicates whether the output should be logged to a file */
	private boolean _logsOutput;
	
    // stream variables
    final private PrintStream _standardOut;
    final private PrintStream _standardErr;
    final private ConsoleOutHandler _outStream;
    final private ConsoleErrHandler _errStream;
    
    // view variables
    private JFrame _frame;
    private boolean _neverShown;
    private JTextPane _textView;
    private Style _outStyle;
    private Style _errStyle;
    private DefaultStyledDocument _document;
    
	
	// static initializer
    static {
		LOG_PREFS = xal.tools.apputils.Preferences.nodeForPackage( Console.class );
        CONSOLE = new Console();
    }
    
	
    /** Constructor */
    public Console() {
        _neverShown = true;
        _outStream = new ConsoleOutHandler();
        _errStream = new ConsoleErrHandler();
        _standardOut = System.out;
        _standardErr = System.err;
		
		_logsOutput = LOG_PREFS.getBoolean( LOGGING_KEY, false );
		if ( _logsOutput )  configureLogs();

        makeTextView();
        makeFrame();		
    }
	
	
	/** configure the logs for recording output */
	private void configureLogs() {
		if ( _logWriter == null ) {
			try {
				final String homePath = System.getProperty( "user.home" );
				final Date now = new Date();
				final String year = new SimpleDateFormat( "yyyy" ).format( now );
				final String appName = Application.getAdaptor().applicationName();
				// log directory is of the form ~/.xal/logs/${current year}/${appname}
				final File logDirectory = new File( new File( new File( new File( new File( homePath ), ".xal" ), "logs" ), String.valueOf( year ) ), appName );
				if ( !logDirectory.exists() ) {
					logDirectory.mkdirs();
				}
				final File logFile = new File( logDirectory, appName + "_" + new SimpleDateFormat( "yyyyMMdd'_'HHmmss'_'SSS" ).format( now ) + ".log" );
				_logWriter = new BufferedWriter( new FileWriter( logFile ) );
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}			
		}
	}
    
    
    /** Make the frame for the console. */
    private void makeFrame() {
        _frame = new JFrame( "Console" );
        _frame.setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
        _frame.setSize( 640, 480 );
        _frame.setTitle( Application.getAdaptor().applicationName() + " - Console" );
        _frame.getContentPane().setLayout( new BorderLayout() );
		
		generateContentsFor( _frame.getContentPane() );
    }
	
	
	/**
	 * Make contents and add them to the container.
	 * @param containter the container to which the contents are added.
	 */
	private void generateContentsFor( final Container container ) {
        final Box buttonBar = new Box( BoxLayout.X_AXIS );
        final JButton clearButton = new JButton();
		clearButton.setIcon( IconLib.getIcon( "custom", "Clear24.gif" ) );
		clearButton.setToolTipText( "Clear the console..." );
		
        clearButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                try {
                    _document.remove( 0, _document.getLength() );
                }
                catch( Exception exception ) {
                }
            }
        });

        buttonBar.add( clearButton );
        buttonBar.add( Box.createGlue() );

		final JCheckBox logCheckBox = new JCheckBox( "Persistent Log" );
		logCheckBox.setToolTipText( "Enable/disable persistent logging for all applications. Launch an application using -Dxal.admin=true to enable this option." );
		logCheckBox.setSelected( _logsOutput );
		
		logCheckBox.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				_logsOutput = logCheckBox.isSelected();
				if ( _logsOutput )  configureLogs();
				LOG_PREFS.putBoolean( LOGGING_KEY, _logsOutput );
			}
		});
		buttonBar.add( logCheckBox );
		// if the application was launched with the -Dxal.admin=true flag, then allow the user to change the logging flag
		logCheckBox.setEnabled( Boolean.parseBoolean( System.getProperty( "xal.admin" ) ) );
		
        container.add( buttonBar, "North" );
        
        Box box = new Box( BoxLayout.Y_AXIS );
        container.add( box, "Center" );
        
        JScrollPane scrollPane = new JScrollPane( _textView );
        box.add( scrollPane, "Center" );		
	}
    
    
    /**
     * Make the view that holds the text.  The text is set to be un-editable.
     */
    private void makeTextView() {
        StyleContext context = new StyleContext();
        _document = new DefaultStyledDocument( context );
        _textView = new JTextPane( _document );
        _textView.setEditable( false );
        
        _outStyle = context.addStyle( null, null );
        StyleConstants.setForeground( _outStyle, Color.black );
        
        _errStyle = context.addStyle( null, null );
        StyleConstants.setForeground( _errStyle, Color.red );
    }
    
    
    /**
     * Sets the console to capture standard output.
     */
    static void captureOutput() {
        System.setOut( new PrintStream( CONSOLE._outStream ) );
    }
    
    
    /**
     * Sets the console to capture standard error.
     */
    static void captureErr() {
        System.setErr( new PrintStream( CONSOLE._errStream ) );
    }
    
    
    /**
     * Show the console.  If the console has never been shown before, place it relative to the sender, otherwise simply show it where it was last placed by the user.
     * However, if the console window is on a different screen than the sender, bring the console window back and display it relative to the sender.
	 * @param sender The component relative to which the console should be positioned
     */
    static void showNear( final java.awt.Component sender ) {
        if ( CONSOLE._neverShown ) {
            CONSOLE._frame.setLocationRelativeTo( sender );
            CONSOLE._neverShown = false;
        }
        else if ( !sender.getGraphicsConfiguration().getDevice().getIDstring().equals(CONSOLE._frame.getGraphicsConfiguration().getDevice().getIDstring()) ) {
            // if the console window is on a different screen bring it to the same screen as the sender
            CONSOLE._frame.setVisible( false );
            CONSOLE._frame.setLocationRelativeTo( sender );
        }
        CONSOLE._frame.setVisible( true );
    }
    
    
    /** Hide the console. */
    static void hide() {
        CONSOLE._frame.setVisible( false );
    }
    
    
    
    /** The internal class whose instance handles the output stream.  The output is inserted into the text pane's document as black text. */
    protected class ConsoleOutHandler extends OutputStream {
        /**
         * Write output to both standard out and the Console view
         * @param character The character to write
         */
        public void write( final int character ) {
            try {
                _standardOut.write( character );
                _document.insertString( _document.getLength(), String.valueOf( (char)character ), _outStyle );
				if ( _document.getLength() > CHAR_LIMIT ) {
					_document.remove( 0, CHAR_LIMIT / 10 );		// shed the first 10 percent
				}
				if ( _logsOutput ) {
					_logWriter.write( character );
					_logWriter.flush();
				}
            }
            catch( Exception exception ) {}
        }
    }
    
    
    /** The internal class whose instance handles the error stream.  The output is inserted into the text pane's document as red text. */
    protected class ConsoleErrHandler extends OutputStream {
        /**
         * Write output to both standard err and the Console view
         * @param character The character to write
         */
        public void write( final int character ) {
            try {
                _standardErr.write(character);
                _document.insertString( _document.getLength(), String.valueOf( (char)character ), _errStyle );
				if ( _document.getLength() > CHAR_LIMIT ) {
					_document.remove( 0, CHAR_LIMIT / 10 );		// shed the first 10 percent
				}
				if ( _logsOutput ) {
					_logWriter.write( character );
					_logWriter.flush();
				}
            }
            catch( Exception exception ) {}
        }
    }
}
