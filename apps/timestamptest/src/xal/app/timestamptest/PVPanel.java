/*
 * PVPanel.java
 *
 * Created on August 11, 2004, 3:37 PM
 */

package xal.app.timestamptest;

import java.awt.*;
import javax.swing.*;

/**
 * This class provides a JPanel for holding individual PV with PV name at top, timestamp printout in the 
 * middle and a "remove" button at the bottom 
 *
 * @author  Paul Chu
 */
public class PVPanel extends JPanel {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    String myPVName = "";
    JLabel pvNameLabel;
    JTextArea timestampArea = new JTextArea();
    JButton remove = new JButton("remove");
    
    /** 
     * Creates a new instance of PVPanel 
     * @param pvName PV name for this panel
     */
    public PVPanel(String pvName) {
        myPVName = pvName;
        
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(250, 400));
        
        pvNameLabel = new JLabel(myPVName);
        
        JScrollPane timestampPane = new JScrollPane(timestampArea);
        
        add(pvNameLabel, BorderLayout.NORTH);
        add(timestampPane, BorderLayout.CENTER);
//        add(remove, BorderLayout.SOUTH);
        
    }
    
    public String getPVName() {
        return myPVName;
    }
    
    public JTextArea getTextArea() {
        return timestampArea;
    }
}
