/*
 * RefreshRateDialog.java
 *
 * Created on June 10, 2003, 10:25 AM
 */

package xal.app.scope;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

/**
 * RefreshRateDialog allows the user to change the refresh rate of the Scope
 * display measured in Hertz.
 *
 * @author  tap
 */
public class RefreshRateDialog extends JDialog implements SwingConstants {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    //--------- static instance
    final static protected RefreshRateDialog rateDialog;
    
    //--------- constant instance variables
    final protected DecimalFormat formatter;
    
    //--------- model
    protected ScopeModel model;
    
    //--------- controls
    protected JTextField rateField;
    protected JSlider rateSlider;
        
    
    static {
        rateDialog = new RefreshRateDialog();
    }
    
    
    /** Creates a new instance of RefreshRateDialog */
    protected RefreshRateDialog() {
        super();
        setTitle("Refresh Rate");
        setModal(true);
        
        formatter = new DecimalFormat("0");
                
        initComponents();
    }
    
    
    /**
     * Show the rate dialog box with values appropriate to the target model.
     * @param owner The window where the request initiated.
     * @param aModel The model to be controlled.
     */
    static public void show(JFrame owner, ScopeModel aModel) {
        rateDialog.model = aModel;
        rateDialog.setLocationRelativeTo(owner);
        rateDialog.updateView();
        rateDialog.setVisible( true );
    }
    
    
    /** 
     * Update the view to reflect model information.
     */
    protected void updateView() {
        double refreshRate = 1 / model.getSweepPeriod();
        rateField.setText( formatter.format(refreshRate) );
        rateSlider.setValue( (int)refreshRate );
        repaint();
    }
    
    
    /**
     * Create the views and controls that appear in the dialog box.
     */
    protected void initComponents() {
        //----------------- add a Refresh rate slider control
        rateSlider = new JSlider(HORIZONTAL, 0, 100, 1);
        rateSlider.setMajorTickSpacing(10);
        rateSlider.setMinorTickSpacing(1);
        rateSlider.setPaintTicks(true);
        rateSlider.setSnapToTicks(true);
        rateSlider.setPaintLabels(true);
        rateSlider.addChangeListener( new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                double refreshRate = ((JSlider)event.getSource()).getValue();
                if ( refreshRate > 0 ) {
                    model.setSweepPeriod(1/refreshRate);
                }
                updateView();
            }
        });
        Box mainPanel = new Box(VERTICAL);
        mainPanel.setPreferredSize( new Dimension(300, 100) );
        mainPanel.setBorder( new TitledBorder("Refresh Rate (Hz)") );
        mainPanel.add(rateSlider);
        mainPanel.add(Box.createVerticalStrut(5));      // add some vertical separation
        rateField = new JTextField(6);
        rateField.setEditable(false);
        rateField.setHorizontalAlignment(RIGHT);
        mainPanel.add(rateField);
        
        getContentPane().add(mainPanel);
        pack();
        setResizable(false);
    }
}



