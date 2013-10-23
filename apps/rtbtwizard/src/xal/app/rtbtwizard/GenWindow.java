/*
 * GenWindow.java
 *
 * Created on 06/16/2003 by cp3
 *
 */

package xal.app.rtbtwizard;

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
    public JTabbedPane mainPane;
    
    
    //  private Container mainPanel;
    
    /** Creates a new instance of MainWindow */
    public GenWindow(XalDocument aDocument) {
        super(aDocument);
        
        mainPanel = new JPanel();
        mainPanel.setVisible(true);
        BorderLayout layout = new BorderLayout();
        mainPanel.setLayout(layout);
        mainPanel.setPreferredSize(new Dimension(960, 700));
        this.getContentPane().add(mainPanel);
        makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        mainPane = new JTabbedPane(JTabbedPane.TOP);
        JTabbedPane subPane = new JTabbedPane(JTabbedPane.TOP);
        
        JPanel p1 = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
        JPanel p2 = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
        JPanel p3 = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
        JPanel p4 = new JPanel(){
            
            /** ID for serializable version */
            private static final long serialVersionUID = 1L;
            
        };
        
        BeamPositionFace beamposface = new BeamPositionFace((GenDocument)document, mainPanel);
        final BeamOrbitFace beamOrbitFace = new BeamOrbitFace( (GenDocument)document );
        BeamSizeFace beamsizeface = new BeamSizeFace((GenDocument)document, mainPanel);
        ProfileFace profileface = new ProfileFace((GenDocument)document, mainPanel);
        DensityFace densityface = new DensityFace((GenDocument)document, mainPanel);
        final BeamArchiveFace beamArchiveFace = new BeamArchiveFace( (GenDocument)document );
		
        p1.add(beamposface);
        p2.add(beamsizeface);
        p3.add(profileface);
        p4.add(densityface);
        mainPane.addTab("Beam Position Tracking", p1);
        mainPane.addTab( "Beam Orbit Matching", beamOrbitFace.getView() );
        mainPane.addTab("Beam Size Tracking", p2);
        mainPane.addTab("Profile Analysis Tool", p3);
        mainPane.addTab("Peak Density Prediction", p4);
        mainPane.addTab( "Target Beam Archive", beamArchiveFace.getView() );
        mainPanel.add(mainPane);
        pack();
        
    }   
    
}

























