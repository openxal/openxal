/*
 * GenWindow.java
 *
 * Created on 05/11/2008 by cp3
 *
 */

package xal.app.beam_matcher;

import java.awt.*;
import javax.swing.*;
import xal.extension.application.*;

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
    
    /** serialization ID */
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
        mainPanel.setPreferredSize(new Dimension(1000, 800));
        
        makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {
        
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        JPanel matchingpanel = new JPanel(){
            /** serialization ID */
            private static final long serialVersionUID = 1L;
        };
        MatchingFace matchingface = new MatchingFace((GenDocument)document);
        matchingpanel.add(matchingface);
        tabbedPane.addTab("HEBT Beam Matching", matchingpanel);
        
        mainPanel = new JPanel();
        mainPanel.setVisible(true);
        mainPanel.setPreferredSize(new Dimension(1000, 800));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(tabbedPane);
        this.getContentPane().add(mainPanel);
        
        pack();
        
    }
    
}
