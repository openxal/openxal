/*
 * TemplateWindow.java
 *
 * Created on Fri Oct 10 15:12:03 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.sclmonitor;

import xal.extension.application.smf.*;

import javax.swing.*;

/**
 * TemplateViewerWindow
 *
 * @author  somebody
 */
class SCLMonWindow extends AcceleratorWindow implements SwingConstants {
	static final long serialVersionUID = 1000;
	
	private SCLMonDocument myDocument;
	
	LLRFPanel llrfPane;
	
   /** Creates a new instance of MainWindow */
    public SCLMonWindow(final SCLMonDocument aDocument) {
        super(aDocument);
		myDocument = aDocument;
		
        setSize(900, 600);
        
		makeContent();

		if (myDocument.getAccelerator() != null) {
			createRFPane();
			myDocument.snapAction.setEnabled(true);
		}
        
    }
    
	/**
	 * Create the main window subviews.
	 */
	protected void makeContent() {
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		llrfPane = new LLRFPanel();
		tabbedPane.addTab("HOM Waveforms", llrfPane);
		this.getContentPane().add(tabbedPane);
	}
	
	protected void createRFPane() {
		llrfPane.setCavs(myDocument.rfCavs);
		llrfPane.initialize();
		
	}
	
	protected LLRFPanel getLLRFPane() {
		return llrfPane;
	}
}




