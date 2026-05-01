/*
 * ScaleControl.java
 *
 * Created on February 14, 2003, 9:16 AM
 */

package xal.app.scope;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.text.DecimalFormat;
import java.util.*;

import xal.tools.messaging.MessageCenter;

/**
 * ScopeScaleControl is a class that displays a GUI component for setting and displaying a value with 
 * a slider.  A text field displays the value as it is being adjusted.
 *
 * @author  tap
 */
public class ScopeScaleControl extends Box implements SwingConstants {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // constants
    static final protected DecimalFormat scaleLabelFormat = new DecimalFormat("0.0E0");
	static final protected DecimalFormat scaleFieldFormat = new DecimalFormat("0.0######");
    
    /** event broadcast */
    final private MessageCenter MESSAGE_CENTER;
    final private ChangeListener CHANGE_PROXY;
    
    /** properties */
    protected String title;     // title of the control
    protected int orientation;  // horizontal or vertical orientation
    protected int lowerPower;   // lower power of 10
    protected int upperPower;   // upper power of 10
    
    /** GUI components */
    protected JTextField valueField;    // text field for changing the value
    protected JSlider valueSlider;      // slider for changing the value
    
    
    /** Creates a new instance of ScaleControl */
    public ScopeScaleControl() {
        this("Scale", -2, 2);
    }
    
    
    /** Creates a new instance of ScaleControl */
    public ScopeScaleControl(String aTitle, int aLowerScale, int anUpperScale) {
        this(aTitle, VERTICAL, aLowerScale, anUpperScale);
    }

    
    /** Creates a new instance of ScaleControl.  Only vertical orientation is presently supported. */
    public ScopeScaleControl(String aTitle, int anOrientation, int aLowerPower, int anUpperPower) {
        super(VERTICAL);
        
        // set properties
        title = aTitle;
        orientation = anOrientation;
        lowerPower = aLowerPower;
        upperPower = anUpperPower;
        
        // setup for events
        MESSAGE_CENTER = new MessageCenter("Scale Control");
        CHANGE_PROXY = MESSAGE_CENTER.registerSource(this, ChangeListener.class);
        
        // initialize the view
        initView();
    }
    
    
    /** Add a listener of value changes */
    public void addChangeListener(ChangeListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, ChangeListener.class);
    }
    
    
    /** Remove a listener of value changes */
    public void removeChangeListener(ChangeListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, ChangeListener.class);
    }
    
    
    /**
     * Enable or disable the control which is equivalent to enabling or disabling
     * the slider.
     * @param enableState True to enable the control and false to disable the control.
     */
    public void setEnabled(boolean enableState) {
        valueSlider.setEnabled(enableState);
    }
    
    
    /**
     * Get whether the control is enabled.
     * @return true if the slider is enabled, false otherwise.
     */
    public boolean isEnabled() {
        return valueSlider.isEnabled();
    }
    
    
    /** get the value */
    public double getValue() {
        return valueFromSlider();
    }
    
    
    /** set the value */
    public void setValue(double newValue) {
        double power = Math.log(newValue) / Math.log(10);
        int tickValue = (int)Math.round( 3 * (power - lowerPower) );
        valueSlider.setValue(tickValue);
    }
    
    
    /** Returns true if the value slider is changing and false if it is resting. */
    public boolean getValueIsAdjusting() {
        return valueSlider.getValueIsAdjusting();
    }
    
    
    /** Get the value corresponding to the tick */
    protected double valueForTick(int tick) {
        double power = lowerPower + Math.floor(tick / 3);
        double value = Math.pow(10, power);
        int segment = tick % 3;
        
        switch(segment) {
            case 0:
                break;
            case 1:
                value *= 2;
                break;
            case 2:
                value *= 5;
                break;
        }
        
        return value;
    }
    
    
    /** Get the value from the valueSlider control */
    protected double valueFromSlider() {
        int tickValue = valueSlider.getValue();
        return valueForTick(tickValue);
    }
    
    
    /** Update the value field */
    protected void updateValueField() {
        valueField.setText( scaleFieldFormat.format( getValue() ) );
    }
    
    
    /** initialize the view for the control */
    public void initView() {
        // add a border
        setBorder( new TitledBorder(title) );
        
        // Setup for the value slider
        int numValues = 3 * (upperPower - lowerPower);
        final Dictionary<Integer,JComponent> valueLabels = new Hashtable<>(1 + upperPower - lowerPower);
        
        for ( int index = lowerPower ; index <= upperPower ; index++ ) {
            int tick = (index - lowerPower) * 3;
            double tickValue = Math.pow(10, index);
            valueLabels.put( new Integer(tick), new JLabel( scaleLabelFormat.format(tickValue) ) );
        }
        
        valueSlider = new JSlider(0, numValues, 0);
        valueSlider.setSnapToTicks(true);
        valueSlider.setMinorTickSpacing(1);
        valueSlider.setMajorTickSpacing(3);
        valueSlider.setPaintTicks(true);
        valueSlider.setLabelTable( valueLabels );
        valueSlider.setPaintLabels(true);
        valueSlider.setOrientation(orientation);
        valueSlider.addChangeListener( new ValueChangeMonitor() );
        add(valueSlider);
        
        // add some vertical separation
        add(Box.createVerticalStrut(5));
        
        // Add a text field to display/enter the value
        valueField = new JTextField(8);
        valueField.setMaximumSize( valueField.getPreferredSize() );
        valueField.setEditable(false);
        valueField.setHorizontalAlignment(RIGHT);
        add(valueField);
        
        // set the dimensions of the control
        java.awt.Component[] components = getComponents();
        int controlWidth = 0;
        int controlHeight = 0;
        for ( int index = 0 ; index < components.length ; index++ ) {
            controlWidth = Math.max( controlWidth, components[index].getPreferredSize().width );
            controlHeight += components[index].getPreferredSize().height;
        }
        setPreferredSize( new Dimension(controlWidth, controlHeight) );
        
        // set the initial scale
        setValue(1.0);  // this is a reasonable default scale
    }
    
    
    
    /** Internal listener class for changes to the value from the value slider */
    protected class ValueChangeMonitor implements ChangeListener {
        public void stateChanged(ChangeEvent event) {
            updateValueField();
            CHANGE_PROXY.stateChanged( new ChangeEvent(ScopeScaleControl.this) );        
        }
    }
}


