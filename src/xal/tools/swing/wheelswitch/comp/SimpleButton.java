/*
 * Copyright (c) 2003 by Cosylab d.o.o.
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file license.html. If the license is not included you may find a copy at
 * http://www.cosylab.com/legal/abeans_license.htm or may write to Cosylab, d.o.o.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package xal.tools.swing.wheelswitch.comp;

import xal.tools.swing.wheelswitch.util.ColorHelper;
//import xal.tools.swing.wheelswitch.util.CosyUIElements;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.metal.MetalBorders;
import xal.tools.swing.wheelswitch.util.CosyUIElements;


/**
 * A very simple implementation of a button. It supports three action modes
 * identified by:     DEFAULT_ACTION_MODE - the button fires one event when
 * the user clicks (presses and releases) the left mouse     button inside the
 * area of the button.         CHAIN_ACTION_MODE - the button fires one event
 * when the user presses the left mouse button inside     the area of the
 * button and continues firing events     at a constant rate until the user
 * releases the button     or moves outside the button area. NULL_ACTION_MODE
 * - the button fires no events at all.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see com.cosylab.gui.components.GradientLabel
 */
public class SimpleButton extends GradientLabel
{
	private static Timer actionTimer = null;
	protected static final String MOUSE_PRESSED = "mousePressed";
	protected static final String MOUSE_CLICKED = "mouseClicked";
	protected static final String MOUSE_RELEASED = "mouseReleased";
	protected static final String MOUSE_CHAIN = "mouseChain";

	/** DOCUMENT ME! */
	public static final int NULL_ACTION_MODE = 0;

	/** DOCUMENT ME! */
	public static final int DEFAULT_ACTION_MODE = 1;

	/** DOCUMENT ME! */
	public static final int CHAIN_ACTION_MODE = 2;

	/** DOCUMENT ME! */
	public static final int FAST_ACTION_MODE = 3;
	private boolean pressed;
	private boolean rollover;
	private boolean rolloverEnabled;
	private int fireRate;
	private int actionMode;
	private MouseListener mouseListener = null;
	private boolean painting;
	private Color pressedBackground;
	private Color pressedForeground;
	private Color pressedBackgroundStart;
	private Color rolloverBackground;
	private Color rolloverForeground;
	private Color rolloverBackgroundStart;
	private Color background;
	private Color foreground;
	private Color backgroundStart;
	private Border pressedBorder;
	private Border border;
	private Border rolloverBorder;

	/**
	 * Creates a simple button with text.
	 *
	 * @param text java.lang.String to be displayed on the button.
	 */
	public SimpleButton(String text)
	{
		super(text);
		init();
	}

	/**
	 * Creates a simple button with Icon.
	 *
	 * @param icon javax.swing.Icon to be displayed on the button.
	 */
	public SimpleButton(Icon icon)
	{
		super(icon);
		init();
	}

	/**
	 * Creates a simple button without text .
	 */
	public SimpleButton()
	{
		this("");
	}

	private void init()
	{
		listenerList = new EventListenerList();
		addMouseListener(new DisplayMouseListener());
		addKeyListener(new SimpleKeyListener());
		setActionMode(DEFAULT_ACTION_MODE);
		background = ColorHelper.getControl();
		foreground = ColorHelper.getControlText();
		backgroundStart = ColorHelper.getControlShadow();
		border = new MetalBorders.Flush3DBorder();
		rolloverBackground = ColorHelper.getControl();
		rolloverForeground = ColorHelper.getControlText();
		rolloverBackgroundStart = ColorHelper.getControlShadow();
		rolloverBorder = new MetalBorders.Flush3DBorder();
		pressedBackground = ColorHelper.getControlShadow();
		pressedForeground = ColorHelper.getControlText();
		pressedBackgroundStart = ColorHelper.getControlDarkShadow();
		pressedBorder = CosyUIElements.getPlainBorder(false);
		fireRate = 20;

		setHorizontalAlignment(JLabel.CENTER);
		setHorizontalTextPosition(JLabel.CENTER);
		setVerticalAlignment(JLabel.CENTER);
		setVerticalTextPosition(JLabel.CENTER);
		setGradientEnabled(false);
		setFocusable(true);
		setEnabled(true);
		setOpaque(true);
		setVisible(true);
		setRequestFocusEnabled(true);
	}

	/**
	 * Sets the action mode of the SimpleButton. DEFAULT_ACTION_MODE fires
	 * ActionEvent when left mouse button is clicked. CHAIN_ACTION_MODE fires
	 * a series of ActionEvent while left mouse button is being pressed at the
	 * <code>fireRate</code>. FAST_ACTION_MODE fires ActionEvent when left
	 * mouse button is pressed. NULL_ACTION_MODE fires no ActionEvents on
	 * mouse actions but pressing the ENTER key still triggers ActionEvents.
	 *
	 * @param newMode to be set.
	 *
	 * @throws IllegalArgumentException if the value entered is not one of
	 *         DEFAULT_ACTION_MODE, CHAIN_ACTION_MODE, FAST_ACTION_MODE or
	 *         NULL_ACTION_MODE.
	 */
	public void setActionMode(int newMode)
	{
		if (newMode == actionMode) {
			return;
		}

		if (mouseListener != null) {
			removeMouseListener(mouseListener);
		}

		int oldMode = actionMode;

		switch (newMode) {
		case DEFAULT_ACTION_MODE:
			mouseListener = new DefaultMouseListener();

			break;

		case CHAIN_ACTION_MODE:
			mouseListener = new ChainMouseListener();

			break;

		case FAST_ACTION_MODE:
			mouseListener = new FastMouseListener();

			break;

		case NULL_ACTION_MODE:
			mouseListener = null;

			break;

		default:
			throw new IllegalArgumentException("actionMode");
		}

		if (mouseListener != null) {
			addMouseListener(mouseListener);
		}

		actionMode = newMode;
		firePropertyChange("actionMode", oldMode, newMode);
	}

	/**
	 * Returns the currently set action mode.
	 *
	 * @return int used action mode.
	 */
	public int getActionMode()
	{
		if (mouseListener == null) {
			return NULL_ACTION_MODE;
		}

		if (mouseListener.getClass() == DefaultMouseListener.class) {
			return DEFAULT_ACTION_MODE;
		}

		if (mouseListener.getClass() == ChainMouseListener.class) {
			return CHAIN_ACTION_MODE;
		}

		if (mouseListener.getClass() == FastMouseListener.class) {
			return FAST_ACTION_MODE;
		}

		return NULL_ACTION_MODE;
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @param newColor Color
	 *
	 * @see com.cosylab.gui.components.GradientLabel#setBackground(Color)
	 */
    @Override
	public void setBackground(Color newColor)
	{
		if (newColor == background) {
			return;
		}

		super.setBackground(newColor);
		background = newColor;
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @return Color
	 *
	 * @see java.awt.Component#getBackground()
	 */
    @Override
	public Color getBackground()
	{
		if (painting && pressed) {
			return pressedBackground;
		} else if (painting && rollover) {
			return rolloverBackground;
		} else {
			return background;
		}
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @param newColor Color
	 *
	 * @see javax.swing.JComponent#setForeground(Color)
	 */
    @Override
	public void setForeground(Color newColor)
	{
		if (foreground == newColor) {
			return;
		}

		super.setForeground(newColor);
		foreground = newColor;
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @return Color
	 *
	 * @see java.awt.Component#getForeground()
	 */
    @Override
	public Color getForeground()
	{
		if (painting && pressed) {
			return pressedForeground;
		} else if (painting && rollover) {
			return rolloverForeground;
		} else {
			return foreground;
		}
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @return Color
	 *
	 * @see com.cosylab.gui.components.GradientLabel#getBackgroundStart()
	 */
    @Override
	public Color getBackgroundStart()
	{
		if (painting && pressed) {
			return pressedBackgroundStart;
		} else if (painting && rollover) {
			return rolloverBackgroundStart;
		} else {
			return backgroundStart;
		}
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @param newColor Color
	 *
	 * @see com.cosylab.gui.components.GradientLabel#setBackgroundStart(Color)
	 */
    @Override
	public void setBackgroundStart(Color newColor)
	{
		if (backgroundStart == newColor) {
			return;
		}

		super.setBackgroundStart(newColor);
		backgroundStart = newColor;
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @param newBorder Border
	 *
	 * @see javax.swing.JComponent#setBorder(Border)
	 */
    @Override
	public void setBorder(Border newBorder)
	{
		if (border == newBorder) {
			return;
		}

		super.setBorder(newBorder);
		border = newBorder;
	}

	/**
	 * Overriden for rendering purposes.
	 *
	 * @return Border
	 *
	 * @see javax.swing.JComponent#getBorder()
	 */
    @Override
	public Border getBorder()
	{
		if (painting && pressed) {
			return pressedBorder;
		} else if (painting && rollover) {
			return rolloverBorder;
		} else {
			return border;
		}
	}

	/**
	 * Gets the background color displayed when the button  is pressed.
	 *
	 * @return Color
	 */
	public Color getPressedBackground()
	{
		return pressedBackground;
	}

	/**
	 * Sets the background color displayed when the button  is pressed.
	 *
	 * @param newColor Color
	 */
	public void setPressedBackground(Color newColor)
	{
		Color oldColor = pressedBackground;

		if (oldColor == newColor) {
			return;
		}

		pressedBackground = newColor;
		firePropertyChange("pressedBackground", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the foreground color displayed when the button  is pressed.
	 *
	 * @return Color
	 */
	public Color getPressedForeground()
	{
		return pressedForeground;
	}

	/**
	 * Sets the foreground color displayed when the button  is pressed.
	 *
	 * @param newColor Color
	 */
	public void setPressedForeground(Color newColor)
	{
		Color oldColor = pressedForeground;

		if (oldColor == newColor) {
			return;
		}

		pressedForeground = newColor;
		firePropertyChange("pressedForeground", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the starting background gradient color displayed  when the button
	 * is pressed.
	 *
	 * @return Color
	 */
	public Color getPressedBackgroundStart()
	{
		return pressedBackgroundStart;
	}

	/**
	 * Sets the starting background gradient color displayed  when the button
	 * is pressed.
	 *
	 * @param newColor Color
	 */
	public void setPressedBackgroundStart(Color newColor)
	{
		Color oldColor = pressedBackgroundStart;

		if (oldColor == newColor) {
			return;
		}

		pressedBackgroundStart = newColor;
		firePropertyChange("pressedBackgroundStart", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the border displayed when the button is pressed.
	 *
	 * @return Border
	 */
	public Border getPressedBorder()
	{
		return pressedBorder;
	}

	/**
	 * Sets the border displayed when the button is pressed.
	 *
	 * @param newBorder Border
	 */
	public void setPressedBorder(Border newBorder)
	{
		Border oldBorder = pressedBorder;

		if (oldBorder == newBorder) {
			return;
		}

		pressedBorder = newBorder;
		firePropertyChange("pressedBorder", oldBorder, newBorder);
		repaint();
	}

	/**
	 * Gets the background color displayed when the mouse  cursor is over the
	 * button and rollover is enabled.
	 *
	 * @return Color
	 */
	public Color getRolloverBackground()
	{
		return rolloverBackground;
	}

	/**
	 * Sets the background color displayed when the mouse  cursor is over the
	 * button and rollover is enabled.
	 *
	 * @param newColor Color
	 */
	public void setRolloverBackground(Color newColor)
	{
		Color oldColor = rolloverBackground;

		if (oldColor == newColor) {
			return;
		}

		rolloverBackground = newColor;
		firePropertyChange("rolloverBackground", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the foreground color displayed when the mouse  cursor is over the
	 * button and rollover is enabled.
	 *
	 * @return Color
	 */
	public Color getRolloverForeground()
	{
		return rolloverForeground;
	}

	/**
	 * Sets the foreground color displayed when the mouse  cursor is over the
	 * button and rollover is enabled.
	 *
	 * @param newColor Color
	 */
	public void setRolloverForeground(Color newColor)
	{
		Color oldColor = rolloverForeground;

		if (oldColor == newColor) {
			return;
		}

		rolloverForeground = newColor;
		firePropertyChange("rolloverForeground", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the start of background gradient color displayed  when the mouse
	 * cursor is over the button and rollover is enabled.
	 *
	 * @return Color
	 */
	public Color getRolloverBackgroundStart()
	{
		return rolloverBackgroundStart;
	}

	/**
	 * Sets the start of background gradient color displayed  when the mouse
	 * cursor is over the button and rollover is enabled.
	 *
	 * @param newColor Color
	 */
	public void setRolloverBackgroundStart(Color newColor)
	{
		Color oldColor = rolloverBackgroundStart;

		if (oldColor == newColor) {
			return;
		}

		rolloverBackgroundStart = newColor;
		firePropertyChange("rolloverBackgroundStart", oldColor, newColor);
		repaint();
	}

	/**
	 * Gets the border displayed when the mouse cursor is over the  button and
	 * rollover is enabled.
	 *
	 * @return Border
	 */
	public Border getRolloverBorder()
	{
		return rolloverBorder;
	}

	/**
	 * Sets the border displayed when the mouse cursor is over the  button and
	 * rollover is enabled.
	 *
	 * @param newBorder Border
	 */
	public void setRolloverBorder(Border newBorder)
	{
		Border oldBorder = rolloverBorder;

		if (oldBorder == newBorder) {
			return;
		}

		rolloverBorder = newBorder;
		firePropertyChange("rolloverBorder", oldBorder, newBorder);
		repaint();
	}

	/**
	 * Enables or disables visual indication when mouse is over this
	 * SimpleButton.
	 *
	 * @param enabled boolean whether this SimpleButton should indicate when
	 *        mouse is over it.
	 */
	public void setRolloverEnabled(boolean enabled)
	{
		boolean oldEnabled = rolloverEnabled;

		if (oldEnabled == enabled) {
			return;
		}

		rolloverEnabled = enabled;
		firePropertyChange("rolloverEnabled", oldEnabled, enabled);

		if (rollover && !enabled) {
			rollover = false;
			repaint();
		}
	}

	/**
	 * Returns wether this SimpleButton visually indicates when mouse is over
	 * it.
	 *
	 * @return boolean true if rollover indication is enabled, false otherwise.
	 */
	public boolean isRolloverEnabled()
	{
		return rolloverEnabled;
	}

	/**
	 * Sets the rate of event firing in the CHAIN_MODE
	 *
	 * @param newRate int time in miliseconds between successive events are
	 *        fired.
	 */
	public void setFireRate(int newRate)
	{
		int oldRate = fireRate;

		if (oldRate == newRate) {
			return;
		}

		fireRate = newRate;
		firePropertyChange("fireRate", oldRate, newRate);
	}

	/**
	 * Gets the rate of event firing in the CHAIN_MODE
	 *
	 * @return int
	 */
	public int getFireRate()
	{
		return fireRate;
	}

	/**
	 * Gets the property specifiing whether the button is being pressed.
	 *
	 * @return boolean
	 */
	public boolean isPressed()
	{
		return pressed;
	}

	/**
	 * Returns the property specifiing whether the button is being pressed.
	 *
	 * @param newPressed boolean
	 */
	protected void setPressed(boolean newPressed)
	{
		boolean oldPressed = pressed;

		if (oldPressed == newPressed) {
			return;
		}

		pressed = newPressed;
		firePropertyChange("pressed", oldPressed, newPressed);

		if (pressed) {
			requestFocus();
		}

		revalidate();
		repaint();
	}

	/**
	 * Gets the property specifiing whether the mouse is being moved over the
	 * button.
	 *
	 * @return boolean
	 */
	public boolean isRollover()
	{
		return rollover;
	}

	/**
	 * Sets the property specifiing whether the mouse is being moved over the
	 * button.
	 *
	 * @param newRollover boolean
	 */
	protected void setRollover(boolean newRollover)
	{
		if (rolloverEnabled) {
			boolean oldRollover = rollover;

			if (oldRollover == newRollover) {
				return;
			}

			rollover = newRollover;
			firePropertyChange("rollover", oldRollover, newRollover);
			revalidate();
			repaint();
		}
	}

	/**
	 * Fires an action event to all its listeners
	 *
	 * @param e ActionEvent
	 */
	protected void fireActionPerformed(ActionEvent e)
	{
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ActionListener.class) {
				((ActionListener)listeners[i + 1]).actionPerformed(e);
			}
		}
	}

	/**
	 * Adds an action listener to the list of registered  listeners for this
	 * button.
	 *
	 * @param l ActionListener
	 */
	public void addActionListener(ActionListener l)
	{
		listenerList.add(ActionListener.class, l);
	}

	/**
	 * Removes an action listener from the list of registered  listeners for
	 * this button.
	 *
	 * @param l ActionListener
	 */
	public void removeActionListener(ActionListener l)
	{
		listenerList.remove(ActionListener.class, l);
	}

	/**
	 * This method was overloaded to enable advanced  graphical features of the
	 * SimpleButton
	 *
	 * @param g Graphics
	 *
	 * @see com.cosylab.gui.components.GradientLabel#paintComponent(Graphics)
	 */
    @Override
	public void paintComponent(Graphics g)
	{
		painting = true;
		super.paintComponent(g);
		painting = false;
	}

	/**
	 * This method was overloaded to enable advanced  graphical features of the
	 * SimpleButton
	 *
	 * @param g Graphics
	 *
	 * @see javax.swing.JComponent#paintBorder(Graphics)
	 */
    @Override
	protected void paintBorder(Graphics g)
	{
		painting = true;
		super.paintBorder(g);
		painting = false;
	}

	/**
	 * DOCUMENT ME!
	 */
    @Override
	public void addNotify()
	{
		setRollover(false);
		setPressed(false);
		super.addNotify();
	}

	/**
	 * DOCUMENT ME!
	 */
    @Override
	public void removeNotify()
	{
		setRollover(false);
		setPressed(false);
		super.removeNotify();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param visible DOCUMENT ME!
	 */
    @Override
	public void setVisible(boolean visible)
	{
		if (isVisible() != visible) {
			setRollover(false);
			setPressed(false);
			super.setVisible(visible);
		}
	}

	/*
	 *
	 * @author jkamenik
	 *
	 *        Listens for key events, updates this SimpleButton and fires ActionEvents when
	 * ENTER is pressed.
	 */
	private class SimpleKeyListener extends KeyAdapter
	{
		/**
		 * Updates this SimpleButton and fires ActionPerformed event when ENTER
		 * is pressed.
		 *
		 * @param e KeyEvent
		 *
		 * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
		 */
        @Override
		public void keyPressed(KeyEvent e)
		{
			//Debug.out("keypressed");
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				if (!pressed) {
					setPressed(true);
				}

				fireActionPerformed(new ActionEvent(SimpleButton.this,
				        ActionEvent.ACTION_PERFORMED, MOUSE_PRESSED));
			}
		}
	}

	/*
	 *
	 * @author jkamenik
	 *
	 *        Listens for MouseEvents and updates the visualization of this SimpleButton.
	 */
	private class DisplayMouseListener extends MouseAdapter
	{
		/**
		 * Updates the visualisation of this SimpleButton when left mouse
		 * button is pressed.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
		 */
        @Override
		public void mousePressed(MouseEvent e)
		{
			if (SwingUtilities.isLeftMouseButton(e) && isEnabled()) {
				setPressed(true);
			}
		}

		/**
		 * Updates the visualisation of this SimpleButton when mouse button is
		 * released.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
		 */
        @Override
		public void mouseReleased(MouseEvent e)
		{
			if (isPressed()) {
				setPressed(false);
			}
		}

		/**
		 * Updates the visualisation of this SimpleButton when mouse enters.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
		 */
        @Override
		public void mouseEntered(MouseEvent e)
		{
			if (isEnabled()) {
				setRollover(true);
			}
		}

		/**
		 * Updates the visualisation of this SimpleButton when mouse exits.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
		 */
        @Override
		public void mouseExited(MouseEvent e)
		{
			if (isPressed()) {
				setPressed(false);
			}

			if (isRollover()) {
				setRollover(false);
			}
		}
	}

	/*
	 *
	 * @author jkamenik
	 *
	 * Listens for mouse events and fires ActionEvents when mouse is clicked.
	 */
	private class DefaultMouseListener extends MouseAdapter
	{
		/**
		 * Fires ActionEvents when left mouse button is clicked.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
		 */
        @Override
		public void mouseClicked(MouseEvent e)
		{
			if (!isEnabled()) {
				return;
			}

			if (SwingUtilities.isLeftMouseButton(e)) {
				fireActionPerformed(new ActionEvent(SimpleButton.this,
				        ActionEvent.ACTION_PERFORMED, MOUSE_CLICKED));
			}
		}
	}

	/*
	 *
	 * @author jkamenik
	 *
	 * Listens for mouse events and fires a series of ActionEvents while mouse is pressed.
	 */
	private class ChainMouseListener extends MouseAdapter
	{
		/**
		 * Starts firing EctionEvents when left mouse button is pressed.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
		 */
        @Override
		public void mousePressed(MouseEvent e)
		{
			if (!isEnabled()) {
				return;
			}

			if (SwingUtilities.isLeftMouseButton(e)) {
				fireActionPerformed(new ActionEvent(SimpleButton.this,
				        ActionEvent.ACTION_PERFORMED, MOUSE_PRESSED));
				actionTimer = new Timer();
				actionTimer.schedule(new TimerTask() {
                        @Override
						public void run()
						{
							fireActionPerformed(new ActionEvent(
							        SimpleButton.this,
							        ActionEvent.ACTION_PERFORMED, MOUSE_CHAIN));
						}
					}, 500, fireRate);
			}
		}

		/**
		 * Stops firing ActionEvents when mouseButton is released.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
		 */
        @Override
		public void mouseReleased(MouseEvent e)
		{
			if (actionTimer != null) {
				actionTimer.cancel();
				actionTimer = null;
			}
		}

		/**
		 * Stops firing ActionEvents when mouse exits.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
		 */
        @Override
		public void mouseExited(MouseEvent e)
		{
			if (actionTimer != null) {
				actionTimer.cancel();
				actionTimer = null;
			}
		}
	}

	/*
	 *
	 * @author jkamenik
	 *
	 * Listens for mouse events and fires ActionEvents when mouse is pressed.
	 */
	private class FastMouseListener extends MouseAdapter
	{
		/**
		 * Fires an ActionEvent when left mouse button is pressed.
		 *
		 * @param e MouseEvent
		 *
		 * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
		 */
        @Override
		public void mousePressed(MouseEvent e)
		{
			if (!isEnabled()) {
				return;
			}

			if (SwingUtilities.isLeftMouseButton(e)) {
				fireActionPerformed(new ActionEvent(SimpleButton.this,
				        ActionEvent.ACTION_PERFORMED, MOUSE_PRESSED));
			}
		}
	}
}

/* __oOo__ */
