/*
 * GenWindow.java
 *
 * Created on 06/16/2003 by cp3
 *
 */
 
package xal.app.ringinjection;
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
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

    private JPanel mainPanel;
    public JTabbedPane mainPane;
 
    //  private Container mainPanel;

    /** Creates a new instance of MainWindow */
    public GenWindow(XalDocument aDocument) {
        super(aDocument);

	mainPanel = new JPanel();
	mainPanel.setVisible(true);
	BorderLayout layout = new BorderLayout();
	mainPanel.setLayout(layout);
	mainPanel.setPreferredSize(new Dimension(1000, 550));
	this.getContentPane().add(mainPanel);
	makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
	mainPane = new JTabbedPane(JTabbedPane.TOP);
	JTabbedPane subPane = new JTabbedPane(JTabbedPane.TOP);	

	JPanel p1 = new JPanel();
	JPanel p2 = new JPanel();
	JPanel p3 = new JPanel();
	JPanel p4 = new JPanel();
	JPanel p1sub1 = new JPanel();
	JPanel p1sub2 = new JPanel();
	
	BPMFace bpmface = new BPMFace((GenDocument)document, mainPanel);
	OneTurnFace oneturnface = new OneTurnFace((GenDocument)document, mainPanel);
	SteererFace steererface = new SteererFace((GenDocument)document);
	bpmface.addInjSpotListener(steererface);
	oneturnface.addInjSpotListener(steererface);
	subPane.add("Turn-by-Turn Analysis", p1sub1);
	p1sub1.add(bpmface);
	subPane.add("One-Turn Analysis", p1sub2);
	p1sub2.add(oneturnface);
	p1.setLayout(new BorderLayout());
	p1.add(subPane);
	//p1.add(bpmface);
	p2.add(steererface);
	mainPane.addTab("Injection Spot Measurement", p1);	
	mainPane.addTab("Injection Spot Control", p2);
	mainPanel.add(mainPane);
	pack();
	
    }   

}

























