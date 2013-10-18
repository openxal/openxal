/*
 * TimeBasePanel.java
 *
 * Created on May 5, 2003, 2:10 PM
 */

package xal.app.scope;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;
import java.beans.*;

import xal.extension.widgets.swing.Wheelswitch;


/**
 * TimeBasePanel is the view for handling timing settings.
 *
 * @author  tap
 */
public class TimeBasePanel extends javax.swing.Box implements SwingConstants, SettingListener {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // constants
    final protected DecimalFormat offsetFormat = new DecimalFormat("#,##0");
    
    protected TimeDisplaySettings _displaySettings;
    protected TimeModel timeModel;
    
    // GUI controls
    protected ScopeScaleControl scaleControl;
    protected JRadioButton[] unitsButtons;
	protected Wheelswitch offsetWheel;

    
    /** Creates new form ChannelPanel */
    public TimeBasePanel(TimeDisplaySettings displaySettings, TimeModel aTimeModel) {
        super(VERTICAL);
        timeModel = aTimeModel;
        _displaySettings = displaySettings;
		_displaySettings.addSettingListener(this);
		
        initComponents();
    }
    
    
    /** Update the display to reflect the underlying model */
    protected void updateView(AbstractButton sender) {
        updateView();
    }
    
    
    /** Update the view with model information */
    protected void updateView() {
        double offset = _displaySettings.getLowerLimit();
		offsetWheel.setValue(offset);
        
        scaleControl.setValue( _displaySettings.getTimeDivision() );
        
        // refresh the units buttons states
        int[] unitsTypes = TimeModel.getAvailableUnitsTypes();
        for ( int unitIndex = 0 ; unitIndex < unitsButtons.length ; unitIndex++ ) {
            if ( timeModel.getUnitsType() == unitsTypes[unitIndex] ) {
                unitsButtons[unitIndex].setSelected(true);
            }
        }
        repaint();
    }
    
    
    /** 
     * Create and layout the components on the panel.
     */
    protected void initComponents() {
        //----------------- setup the panel
        TitledBorder border = new TitledBorder("Time Base");
        border.setBorder( new LineBorder(Color.black) );
        setBorder(border);
        
        //----------------- add a Time offset slider control
        // Since full cycle is about 16,000 turns, we should allow the user to 
        // shift the time by that much
		offsetWheel = new Wheelswitch();
		offsetWheel.setFormat("+#####.#");
		offsetWheel.setGraphMin(-50000.0);
		offsetWheel.setGraphMax(50000.0);
		offsetWheel.setValue( _displaySettings.getLowerLimit() );
        offsetWheel.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                double offset = offsetWheel.getValue();
                _displaySettings.setLowerLimit(offset);
            }
        });
        Box offsetPanel = new Box(VERTICAL);
        offsetPanel.setBorder( new TitledBorder("Time Offset") );
        offsetPanel.add(offsetWheel);
        offsetPanel.add( Box.createVerticalStrut(5) );      // add some vertical separation
        add(offsetPanel);
        add( Box.createVerticalStrut(5) );
        
        // add a row for which includes the scale control
        Box scaleRow = new Box(HORIZONTAL);
        
        //----------------- add a turns/div scale control
        scaleControl = new ScopeScaleControl("Time Units/Div", 0, 4);
        scaleControl.setValue( _displaySettings.getTimeDivision() );
        scaleControl.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                double scale = scaleControl.getValue();
                _displaySettings.setTimeDivision(scale);
            }
        });
        scaleRow.add(scaleControl);
        
        //----------------- add a control for selecting the time units
        Box unitsColumn = new Box(VERTICAL);
        Box unitsPanel = new Box(VERTICAL);
        unitsPanel.setBorder( new TitledBorder("Time Units") );
        ButtonGroup unitsButtonGroup = new ButtonGroup();
        int[] unitsTypes = TimeModel.getAvailableUnitsTypes();
        unitsButtons = new JRadioButton[unitsTypes.length];
        for ( int unitIndex = 0 ; unitIndex < unitsTypes.length ; unitIndex++ ) {
            final int unitsType = unitsTypes[unitIndex];
            JRadioButton unitButton = new JRadioButton(timeModel.getUnitsLabel(unitsType));
            unitsButtons[unitIndex] = unitButton;
            unitButton.addActionListener( new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    try {
                        timeModel.setUnitsType(unitsType);
                    }
                    catch(Exception exception) {
                        JOptionPane.showMessageDialog(TimeBasePanel.this, exception.getMessage(), exception.getClass().getName(), JOptionPane.ERROR_MESSAGE);
                    }
                    finally {
                        updateView();
                    }
                }
            });
            unitsPanel.add(unitButton);
            unitsButtonGroup.add(unitButton);
        }
        unitsColumn.add(unitsPanel);
        unitsColumn.add( Box.createVerticalGlue() );    // force the units panel to be top justified
        scaleRow.add(unitsColumn);
        
        add(scaleRow);
        
        // force the panel to resize so it will be drawn
        setSize(getPreferredSize());
        
        // update the view
        updateView();
    }
	
	
    /**
     * A setting from the sender has changed.
     * @param source The object whose setting changed.
     */
    public void settingChanged(Object source) {
		updateView();
	}
}


