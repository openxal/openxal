/*
 * MyDocument.java
 *
 * Created on March 19, 2003, 1:32 PM
 */

package xal.app.demobricks;

import java.net.*;
import java.io.*;
import java.awt.Color;
import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.logging.*;
import java.util.Vector;

import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.extension.widgets.plot.*;


/**
 * DemoDocument is a custom XalDocument for this application.
 * @author  t6p
 */
public class DemoDocument extends XalDocument {
    /** Create a new empty document */
    public DemoDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public DemoDocument( final URL url ) {
        setSource( url );
    }
    
    
    /**
     * Make a main window by instantiating the my custom window.  Set the text 
     * pane to use the textDocument variable as its document.
     */
	@SuppressWarnings( "unchecked" )		// Cast JList to String element type
    public void makeMainWindow() {
		final WindowReference windowReference = getDefaultWindowReference( "MainWindow", this );
        mainWindow = (XalWindow)windowReference.getWindow();
		
		final JList<String> magnetList = (JList<String>)windowReference.getView( "MagnetList" );
		final String[] magnets = { "Bend", "Quadrupole", "Sextupole", "Octupole", "Skew Quadrupole", "Vertical Corrector", "Horizontal Corrector", "Skew Dipole", "Skew Sextupole", "Sector Bend", "Rectangular Bend", "Chicane" };
		magnetList.setListData( magnets );
		
		final JButton runButton = (JButton)windowReference.getView( "RunButton" );
		runButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				showHelloDialog();
			}
		});
		
		
		final JButton starButton = (JButton)windowReference.getView( "StarButton" );
		starButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				JOptionPane.showMessageDialog( mainWindow, "Demonstrates how to assign to a button an icon from an application's resources." );
			}
		});
		
		final FunctionGraphsJPanel plot = (FunctionGraphsJPanel)windowReference.getView( "SinePlot" );
		makeSinePlot( plot );
	}
	
	
	/** make the plot */
	protected void makeSinePlot( final FunctionGraphsJPanel plot ) {
		final BasicGraphData graphData = new BasicGraphData();
		graphData.setGraphColor( Color.BLUE );
		graphData.setGraphProperty( plot.getLegendKeyString(), "Sine" );
		for ( double x = 0 ; x < 10.0 ; x+=0.2 ) {
			graphData.addPoint( x, Math.sin( x ) );
		}
		
		final Vector<BasicGraphData> series = new Vector<BasicGraphData>(1);
		series.add( graphData );
		plot.addGraphData( series );		
	}
	
	
	/** show the hello dialog */
	protected void showHelloDialog() {
		final WindowReference helloDialogRef = getDefaultWindowReference( "HelloDialog", mainWindow );
		final JDialog dialog = (JDialog)helloDialogRef.getWindow();
		
		final JButton okayButton = (JButton)helloDialogRef.getView( "OkayButton" );
		okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dialog.setVisible( false );
			}
		});
		
		dialog.setLocationRelativeTo( mainWindow );
		dialog.setVisible( true );
	}

    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
    }
}
