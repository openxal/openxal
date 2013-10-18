
 /**
 * RFWindow.java
 *
 * Created on March 15, 2006, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */
import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.*;

import xal.extension.application.XalDocument;
import xal.extension.smf.application.AcceleratorWindow;
import xal.tools.plot.*;
import xal.tools.apputils.*;
import xal.tools.swing.*;
import xal.extension.scan.*;
import xal.sim.scenario.Scenario;

public class RFWindow extends AcceleratorWindow {
    
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	protected JTabbedPane mainPane;
        protected JTextField errorText;
        protected final int MAX = 1024;
        
	RFDocument myDoc;
        
        protected JPanel controlPanel;
        protected JPanel rfPanel;
        protected JPanel beamPanel;
        protected JPanel tablePanel;
        protected JPanel linacPanel;        
        protected JProgressBar progressBar = new JProgressBar();               
     	
	/** Creates a new instance of MainWindow */
	public RFWindow(XalDocument aDocument) {
            
		super(aDocument);
		myDoc = (RFDocument) aDocument;                
		setSize(500,700);                 
                makeContent();
                pack();                  
        }

	/**
	 * Create the main window subviews.
	 */
        protected void makeContent() {

                Container container = getContentPane();
                
                controlPanel = myDoc.makeControlPanel();
                rfPanel = myDoc.makeRFPanel();
                beamPanel = myDoc.makeBeamPanel();
                tablePanel = new JPanel();
                linacPanel = new JPanel();
                errorText = new JTextField();                                
                mainPane = new JTabbedPane();
                mainPane.setVisible(true);
                
		progressBar.setStringPainted(true);
                progressBar.setIndeterminate(false);
		progressBar.setMinimum(0);
		progressBar.setMaximum(MAX);
                
                mainPane.add("Ring RF", controlPanel);                                                
                container.add(mainPane,BorderLayout.CENTER);
                container.add(progressBar,BorderLayout.SOUTH);
                
                mainPane.add("Ring LLRF", rfPanel);                                                
                container.add(mainPane,BorderLayout.CENTER);
                container.add(progressBar,BorderLayout.SOUTH);
                
                mainPane.add("Ring Beam", beamPanel);                                                
                container.add(mainPane,BorderLayout.CENTER);
                container.add(progressBar,BorderLayout.SOUTH);
                
                mainPane.add("Linac RF", tablePanel);                                                
                container.add(mainPane,BorderLayout.CENTER);
                container.add(progressBar,BorderLayout.SOUTH);
                
                mainPane.add("Linac Beam", linacPanel);                                                
                container.add(mainPane,BorderLayout.CENTER);
                container.add(progressBar,BorderLayout.SOUTH);
        }
        
        //get panel reference        	       
        JTabbedPane getMainPanel() {
                return mainPane;
        }
                      
        protected void freeCustomResources() {
                mainPane = null;
        }        
}
    
    