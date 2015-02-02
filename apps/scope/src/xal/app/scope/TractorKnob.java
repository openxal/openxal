/*
 * TractorKnob.java
 *
 * Created on August 8, 2003, 10:34 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.geom.Line2D;

import xal.tools.messaging.MessageCenter;


/**
 * TractorKnob is a control for a single long value.  The user can drag along 
 * the "tractor" portion of the control to change the value.  This control is
 * appropriate for value control where the range is large, but fine precision is 
 * also needed.The value indicates the appearance of the knob.  The knob's 
 * apparent motion is periodic and the represented value may involve several 
 * winding cycles.
 *
 * @author  tap
 */
public class TractorKnob extends Box implements SwingConstants {
	/** constant required to keep serializable happy */
	final static private long serialVersionUID = 1L;

    protected ThumbWheelModel model;
    final private MessageCenter MESSAGE_CENTER;
    final private TractorListener TRACTOR_EVENT_PROXY;
    
    protected JButton incButton, decButton;
    protected ThumbWheel thumbWheel;
    
    
    /** 
     * Creates a new instance of TractorKnob.
     * @param axis The axis orientation of this control: SwingConstants.VERTICAL or SwingConstants.HORIZONTAL
     * @param value The initial value of the control
     * @param minValue The minimum value allowed
     * @param maxValue The maximum value allowed
     */
    public TractorKnob(int axis, long value, long minValue, long maxValue) {
        super(axis);
        
        MESSAGE_CENTER = new MessageCenter();
        TRACTOR_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, TractorListener.class);
        
        model = new ThumbWheelModel(value, minValue, maxValue);
        model.addThumbWheelListener( new ThumbWheelListener() {
            public void phaseChanged(ThumbWheelModel aModel, long phase) {
                // rebroadcast the event to the tractor knob listeners
                TRACTOR_EVENT_PROXY.valueChanged(TractorKnob.this, phase);
            }
        });
        initComponents(axis);
    }
    
    
    /**
     * Add the specified listener as a receiver of tractor events.
     * @param listener The object to receive tractor events.
     */
    public void addTractorListener(TractorListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, TractorListener.class);
    }
    
    
    /**
     * Remove the specified listener as a receiver of tractor events.
     * @param listener The object to be removed as a receiver of tractor events.
     */
    public void removeTractorListener(TractorListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, TractorListener.class);
    }
    
    
    /**
     * Initialize the sub-components of this control.
     * @param axis The axis orientation of this control.
     */
    protected void initComponents(final int axis) {
        setBorder( new BevelBorder(BevelBorder.RAISED) );
        
        decButton = new JButton("-");
        decButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                model.changePhase(-1);
            }
        });
        decButton.setMargin( new Insets(1, 2, 1, 2) );
        
        incButton = new JButton("+");
        incButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                model.changePhase(1);
            }
        });
        incButton.setMargin( new Insets(1, 2, 1, 2) );
        
        if ( axis == VERTICAL ) {
            add(incButton);
            thumbWheel = new VerticalThumbWheel(model);
            add(thumbWheel);
            add(decButton);        
        }
        else {
            add(decButton);
            thumbWheel = new HorizontalThumbWheel(model);
            add(thumbWheel);
            add(incButton);
        }
    }
    
    
    /**
     * Override setEnabled to enable/disable the sub-components so as
     * to enable/disable user control.
     * @param state The enable/disable state: true to enable and false to disable.
     */
    public void setEnabled(boolean state) {
        super.setEnabled(state);
        incButton.setEnabled(state);
        decButton.setEnabled(state);
        thumbWheel.setEnabled(state);
    }
    
    
    /**
     * Set the value of the control to the specified value.
     * @param newValue The new value of the control.
     */
    public void setValue(long newValue) {
        model.setPhase(newValue);
    }
    
    
    /**
     * Get the value of the control.
     * @return The value of the control.
     */
    public long getValue() {
        return model.getPhase();
    }
}



/**
 * ThumbWheelModel is the internal model for managing the phase that gets 
 * displayed by a "tractor" view.
 * When the phase changes it broadcasts the event to its listeners.
 */
class ThumbWheelModel {
    protected volatile long phase;
    protected long maxPhase, minPhase;
    
    final private MessageCenter MESSAGE_CENTER;
    final private ThumbWheelListener THUMB_WHEEL_EVENT_PROXY;
    
    
    /**
     * Create a thumb wheel model instance.
     */
    public ThumbWheelModel(long aPhase, long aMinPhase, long aMaxPhase) {
        phase = aPhase;
        minPhase = aMinPhase;
        maxPhase = aMaxPhase;
        
        MESSAGE_CENTER = new MessageCenter();
        THUMB_WHEEL_EVENT_PROXY = MESSAGE_CENTER.registerSource(this, ThumbWheelListener.class);
    }
    
    
    /**
     * Add a thumb wheel listener.
     * @param listener The listener of thumb wheel events.
     */
    void addThumbWheelListener(ThumbWheelListener listener) {
        MESSAGE_CENTER.registerTarget(listener, this, ThumbWheelListener.class);
    }
    
    
    /**
     * Remove a thumb wheel listener.
     * @param listener The listener to remove from receiving thumb wheel events.
     */
    void removeThumbWheelListener(ThumbWheelListener listener) {
        MESSAGE_CENTER.removeTarget(listener, this, ThumbWheelListener.class);
    }
    
    
    /**
     * Set a new phase by chaning the phase by the specified amount.  The phase
     * is constrained to remain within the min and max limits.
     * @param delta The amount to add to the current phase.
     */
    public void changePhase(final int delta) {
        setPhase(phase + delta);
    }
    
    
    /**
     * Get the current phase.
     * @return The current phase.
     */
    public long getPhase() {
        return phase;
    }
    
    
    /**
     * Set the phase to the specified value.  The phase is constrained to 
     * remain within the min and max limits.
     * @param requestPhase The new phase requested to be set.
     */
    public void setPhase(final long requestPhase) {
        long newPhase = requestPhase;
        
        if ( requestPhase < minPhase ) {
            newPhase = minPhase;
            Toolkit.getDefaultToolkit().beep();
        }
        else if ( requestPhase > maxPhase ) {
            newPhase = maxPhase;
            Toolkit.getDefaultToolkit().beep();
        }
        
        // only notify listeners if the phase really changed
        if ( phase != newPhase ) {
            phase = newPhase;
            THUMB_WHEEL_EVENT_PROXY.phaseChanged(this, phase);
        }
    }
}



/**
 * ThumbWheel is the abstract superclass of the actual thumbwheel view which the
 * user can drag in either direction to change the model's phase.  The view 
 * monitors and reflects the phase of the model.
 */
abstract class ThumbWheel extends JPanel implements ThumbWheelListener, SwingConstants {
	/** include the serial version ID as required for serializable */
	private static final long serialVersionUID = 1L;

    protected ThumbWheelModel model;
    protected volatile int lastPos;
    protected volatile boolean active;
    
    
    /**
     * Constructor of the thumb wheel to control and visually represent the model.
     * @param aModel The model represented by this view.
     */
    public ThumbWheel(ThumbWheelModel aModel) {
        lastPos = 0;
        active = false;
        
        model = aModel;
        model.addThumbWheelListener(this);
        
        setupMouseEvents();
    }
    
    
    /**
     * Setup the view's mouse event handling so the phase tracks the user's 
     * lead when the user clicks and drags the wheel.
     */
    protected void setupMouseEvents() {   
        addMouseWheelListener( new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent event) {
				if ( isEnabled() ) {
					model.changePhase( -event.getWheelRotation() );
				}
            }
        });
    }
    
    
    /**
     * Repaints the thumb wheel to reflect the model's new phase.  The thumb
     * wheel represents the phase by displaying a pattern offset by the 
     * specified phase.  The representation is periodic so that several 
     * cycles of motion can be used to achieve large values.
     * @param model The model with the phase change.
     * @param phase The new phase.
     */
    public void phaseChanged(ThumbWheelModel model, long phase) {
        RepaintManager.currentManager(this).markCompletelyDirty(this);
    }
}



/**
 * HorizontalThumbWheel is a thumb wheel view in which the thumb wheel moves
 * horizontally.
 */
class HorizontalThumbWheel extends ThumbWheel {
	/** constant required to keep serializable happy */
	final static private long serialVersionUID = 1L;

    /**
     * Constructor of the thumb wheel to control and visually represent the model.
     * @param aModel The model represented by this view.
     */
    public HorizontalThumbWheel(ThumbWheelModel aModel) {
        super(aModel);
    }
    
    
    /**
     * Setup the view's mouse event handling so the phase tracks the user's 
     * lead when the user clicks and drags the wheel.
     */
    protected void setupMouseEvents() {
        super.setupMouseEvents();
        
        addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                lastPos = event.getX();
                active = true;
            }
            
            public void mouseReleased(MouseEvent event) {
                active = false;
            }
        });
        
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                if (active && isEnabled()) {
                    model.changePhase(event.getX() - lastPos);
                    lastPos = event.getX();
                }
            }
        });
    }
    
    
    /*
     * Display the phase by painting the wheel with vertical stripes of 
     * alternating color.  The phase of the stripes reflect the phase of the 
     * model.
     */
    public void paint(java.awt.Graphics graphics) {
        int height = getHeight();
        int width = getWidth();
                
        final int rectLength = 20;
        int x = -width + (int)( model.getPhase()  % width );
        while(x < width) {
            graphics.setColor(Color.darkGray);
            graphics.fillRect(x, 0, rectLength, height);
            x += rectLength;
            graphics.setColor(Color.gray);
            graphics.fillRect(x, 0, rectLength, height);
            x += rectLength;
        }
    }
}



/**
 * VerticalThumbWheel is a thumb wheel view in which the thumb wheel moves
 * vertically.
 */
class VerticalThumbWheel extends ThumbWheel {
	/** constant required to keep serializable happy */
	final static private long serialVersionUID = 1L;

    /**
     * Constructor of the thumb wheel to control and visually represent the model.
     * @param aModel The model represented by this view.
     */
    public VerticalThumbWheel( final ThumbWheelModel aModel ) {
        super(aModel);
    }
    
    
    /**
     * Setup the view's mouse event handling so the phase tracks the user's 
     * lead when the user clicks and drags the wheel.
     */
    protected void setupMouseEvents() {
        super.setupMouseEvents();
        
        addMouseListener( new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                lastPos = event.getY();
                active = true;
            }
            
            public void mouseReleased(MouseEvent event) {
                active = false;
            }
        });
        
        addMouseMotionListener( new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent event) {
                if (active && isEnabled()) {
                    model.changePhase(lastPos - event.getY());
                    lastPos = event.getY();
                }
            }
        });
    }
    
    
    /*
     * Display the phase by painting the wheel with horizontal stripes of 
     * alternating color.  The phase of the stripes reflect the phase of the 
     * model.
     */
    public void paint(java.awt.Graphics graphics) {
        int height = getHeight();
        int width = getWidth();
        
        final int rectLength = 20;
        int y = -height - (int)( model.getPhase()  % height );
        while(y < height) {
            graphics.setColor(Color.darkGray);
            graphics.fillRect(0, y, width, rectLength);
            y += rectLength;
            graphics.setColor(Color.gray);
            graphics.fillRect(0, y, width, rectLength);
            y += rectLength;
        }
    }
}



/**
 * Internal interface for thumb wheel events notification.
 */
interface ThumbWheelListener {
    /**
     * Message indicating that the thumbwheel phase has changed (i.e.
     * the value being monitored has changed).  The phase indicates the
     * appearance of the knob.  The knob's apparent motion is periodic
     * and the represented value may involve several rotation periods.
     * @param model The thumb wheel model posting this event.
     * @param phase The new thumb wheel phase.
     */
    public void phaseChanged(ThumbWheelModel model, long phase);
}


