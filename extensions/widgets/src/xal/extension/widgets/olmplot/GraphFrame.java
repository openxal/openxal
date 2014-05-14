/**
 * PlotFrame.java
 *
 * @author Christopher K. Allen
 * @since  Nov 26, 2012
 *
 */

/**
 * PlotFrame.java
 *
 * @author  Christopher K. Allen
 * @since	Nov 26, 2012
 */
package xal.extension.widgets.olmplot;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import xal.extension.widgets.plot.FunctionGraphsJPanel;


/**
 * Enclosing Swing frame.  This class manages the main GUI frame for displaying
 * <code>{@link FunctionGraphsJPanel}</code> objects.
 *
 * @author Christopher K. Allen
 * @since   Nov 26, 2012
 */
public class GraphFrame {

    /*
     * Local Attributes
     */
    
    /** The main window frame for the plot - we are managing this */
    private JFrame                  frmMain;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>PlotFrame</code> object to display the
     * given <code>FunctionGraphsJPanel</code> in a frame with the
     * given title.
     * 
     * @param strTitle  title of the frame for the plot
     * @param pnlGraph  graph object containing plots
     *
     * @author  Christopher K. Allen
     * @since   Nov 21, 2012
     */
    public GraphFrame(String strTitle, FunctionGraphsJPanel pnlGraph) {
        
        // Create the frame for the graph panel
        JButton butQuit = new JButton("Quit");
        JPanel pnlMain = new JPanel();
        pnlMain.add(butQuit);
        pnlMain.add(pnlGraph);

        this.frmMain = new JFrame(strTitle);
        this.frmMain.getContentPane().add(pnlMain);
        this.frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frmMain.pack();

        // Add the event response for the quit button
        butQuit.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }
        );
    }

    
    /*
     * Operations
     */
    
    /**
     * Displays the graph with frame.
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2012
     */
    public void display() {
        this.frmMain.setVisible(true);
    }
    
    /**
     * Sets the preferred size of this component to a constant value. 
     * Subsequent calls to getPreferredSize will always return this value. 
     * Setting the preferred size to null restores the default behavior.
     * 
     * @param dimSize   The new preferred size, or <code>null</code>
     *
     * @author Christopher K. Allen
     * @since  Nov 27, 2012
     */
    public void setPreferredSize(Dimension dimSize) {
        this.frmMain.setPreferredSize(dimSize);
    }
    
}
