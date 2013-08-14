/*
 * REKiTWindow.java
 *
 * Created on June 7, 2011
 *
 * Copyright (c) 2001-2011 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 */

package xal.app.rekit;

import java.math.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.NumberFormat;
import xal.tools.swing.*;
import xal.application.*;
import java.net.URL;
import xal.tools.apputils.EdgeLayout;

/**
 * REKiTWindow is a subclass of XalWindow used in the extraction kicker restoration application.  
 * This class implement a subclass of XalWindow to serve as the 
 * main window of a document.  The window contains only view definitions and 
 * no model references.  It defines how the window looks and how to represent 
 * the document in graphical form.
 *
 * @author  cp3, zoy
 */

public class REKiTWindow extends XalWindow {
	
    private static final long serialVersionUID = 1L;
    
	private REKiTDocument myDocument;
	protected JTabbedPane tabbedPane;
	JPanel mainPanel = new JPanel();
	AnalysisPanel analysisPanel = new AnalysisPanel();
	
	/** Creates a new instance of REKiTWindow */
	public REKiTWindow(REKiTDocument aDocument) {
		super(aDocument);
		myDocument = aDocument;
		setSize(990, 800);
		makeContent();
	}
	
	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {
	    
		mainPanel.add(analysisPanel);
	    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	    tabbedPane.addTab("Extraction Kicker Restoration", mainPanel);
		
	    getContentPane().add(tabbedPane);

	}
}