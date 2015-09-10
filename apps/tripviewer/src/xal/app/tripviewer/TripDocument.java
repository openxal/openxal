/*
 * TemplateDocument.java
 *
 * Created on Fri Oct 10 14:08:21 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.tripviewer;

import xal.service.tripmonitor.*;
import xal.extension.application.*;
import xal.extension.bricks.WindowReference;
import xal.tools.database.*;

import java.net.URL;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.util.*;
import java.util.Date;
import java.sql.Connection;


/**
 * TripDocument
 * @author  t6p
 */
class TripDocument extends XalDocument {
	/** reference to the main window */
	protected WindowReference _mainWindowReference;
	
	/** controller of the history view */
	protected HistoryController _historyController;
	
	/** controller of the trip monitor service view */
	protected ServicesController _servicesController;
	
	
	/** Create a new empty document */
    public TripDocument() {
        setSource( null );
    }
    
    
    /** Make a main window by instantiating the my custom window. */
    public void makeMainWindow() {
		_mainWindowReference = getDefaultWindowReference( "MainWindow", this );
        mainWindow = (XalWindow)_mainWindowReference.getWindow();
		
		_historyController = new HistoryController( _mainWindowReference );
		_servicesController = new ServicesController( _mainWindowReference );
    }
	
	
	/** Free document resources. */
	final public void freeCustomResources() {
		_historyController.clearDatabaseConnection();
		super.freeCustomResources();
	}
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {}
}




