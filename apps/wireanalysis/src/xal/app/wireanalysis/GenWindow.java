/*
 * GenWindow.java
 *
 * Created on 06/16/2003 by cp3
 *
 */

package xal.app.wireanalysis;

import java.math.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;
import java.lang.*;
import java.text.NumberFormat;
import xal.extension.widgets.swing.*;
import xal.extension.application.*;
import java.net.URL;
import xal.tools.apputils.EdgeLayout;
/**
 * GenWindow is a subclass of XalWindow used in the ring optics application.
 * This class implement a subclass of XalWindow to server as the
 * main window of a document.  The window contains only view definitions and
 * no model references.  It defines how the window looks and how to represent
 * the document in graphical form.
 *
 * @author  cp3
 */

public class GenWindow extends XalWindow {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    private JPanel mainPanel;
    
    //  private Container mainPanel;
    
    /** Creates a new instance of MainWindow */
    public GenWindow(XalDocument aDocument) {
        super(aDocument);
		mainPanel = new JPanel();
		mainPanel.setVisible(true);
		BorderLayout layout = new BorderLayout();
		mainPanel.setLayout(layout);
		mainPanel.setPreferredSize(new Dimension(1200, 750));
        
		makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel wirescanpanel = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
        
        
		JPanel datapanel = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
		JPanel analysispanel = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
		JPanel modelpanel = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
		JPanel controlpanel = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
		
		ScanFace scanface = new ScanFace((GenDocument)document);
		DataFace dataface = new DataFace((GenDocument)document);
		AnalysisFace analysisface = new AnalysisFace((GenDocument)document);
		ModelFace modelface = new ModelFace((GenDocument)document);
        ControlFace controlface = new ControlFace((GenDocument)document, modelface);
        
		wirescanpanel.add(scanface);
		datapanel.add(dataface);
		analysispanel.add(analysisface);
		modelpanel.add(modelface);
        controlpanel.add(controlface);
        
		tabbedPane.addTab("Perform Wire Scan", wirescanpanel);
		tabbedPane.addTab("Load and View Data", datapanel);
		tabbedPane.addTab("Analyze Data", analysispanel);
		tabbedPane.addTab("Twiss Fit", modelpanel);
		tabbedPane.addTab("Twiss Control", controlpanel);
        
		mainPanel = new JPanel();
		mainPanel.setVisible(true);
		mainPanel.setPreferredSize(new Dimension(1200, 750));
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		//mainPanel.add(dataface);
		mainPanel.add(tabbedPane);
		//mainPanel.add(acclplotface);
		this.getContentPane().add(mainPanel);
		
		pack();
        
    }
    
}

























