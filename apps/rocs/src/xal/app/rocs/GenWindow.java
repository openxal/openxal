/*
 * GenWindow.java
 *
 * Created on 06/16/2003 by cp3
 *
 */
 
package xal.app.rocs;
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
import xal.extension.smf.application.*;

/**
 * GenWindow is a subclass of XalWindow used in the ring optics application.  
 * This class implement a subclass of XalWindow to server as the 
 * main window of a document.  The window contains only view definitions and 
 * no model references.  It defines how the window looks and how to represent 
 * the document in graphical form.
 *
 * @author  cp3
 */

public class GenWindow extends AcceleratorWindow {

    private static final long serialVersionUID = 1L;
    
    private JPanel mainPanel;
    public JPanel messagepanel;
    //  private Container mainPanel;
    
    private JLabel messagelabel = new JLabel("Message Panel: ");
    public JTextField messagetext = new JTextField("No Messages");
   
    /** Creates a new instance of MainWindow */
    public GenWindow(XalDocument aDocument) {
        super(aDocument);
        setSize(650, 350);
	makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
	// make default startup gui components
	//mainPanel=this.getContentPane();
	mainPanel = new JPanel();
	
	mainPanel.setVisible(true);
	messagepanel = new JPanel();
	
	BorderLayout layout = new BorderLayout();
	mainPanel.setLayout(layout);
	//mainPanel=this.getContentPane();
	mainPanel.setSize(new Dimension(650, 750));
	this.getContentPane().add(mainPanel);
	initWindow();
    }   

    /**
     * Creates the tabbed panel of functions.
     */
    protected void initWindow(){
	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	JPanel p1 = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel p2 = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel p3 = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel p4 = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel tunepanel = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel chrompanel = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	JPanel phasepanel = new JPanel(){
        private static final long serialVersionUID = 1L;
    };
	
	messagepanel.setBackground(Color.GRAY);
	messagepanel.setPreferredSize(new Dimension(650, 40));
	messagetext.setPreferredSize(new Dimension(500,30));
	
	TuneFace tuneface = new TuneFace((GenDocument)document, this);
	ChromFace chromface = new ChromFace((GenDocument)document, this);
	PhaseFace phaseface = new PhaseFace((GenDocument)document, this);
	tunepanel.add(tuneface);
	chrompanel.add(chromface);
	phasepanel.add(phaseface);

	tabbedPane.addTab("Tune Settings", tunepanel);	
	tabbedPane.addTab("Chromaticity", chrompanel);
	tabbedPane.addTab("Arc Phase Advance", phasepanel);
	//tabbedPane.addTab("Beta Function", p3);
	//tabbedPane.addTab("Harmonic Correction", p4);

	messagepanel.add(messagelabel);
	messagepanel.add(messagetext);
	mainPanel.add(tabbedPane, BorderLayout.NORTH);
	mainPanel.add(messagepanel, BorderLayout.SOUTH);
	
	pack();
    }
}

























