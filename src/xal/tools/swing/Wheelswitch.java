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

package xal.tools.swing;

import xal.tools.swing.wheelswitch.util.SetEvent;
import xal.tools.swing.wheelswitch.util.SetListener;

import xal.tools.swing.wheelswitch.util.ColorHelper;
import xal.tools.swing.wheelswitch.Digit;
import xal.tools.swing.wheelswitch.StaticDigit;
import xal.tools.swing.wheelswitch.UpDownButton;
import xal.tools.swing.wheelswitch.UpDownListener;
import xal.tools.swing.wheelswitch.ValueDigit;
import xal.tools.swing.wheelswitch.WheelswitchFormatter;
import xal.tools.swing.wheelswitch.WheelswitchLayout;

import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;


/**
 * Descedant of <code>javax.swing.JPanel</code> that contains a row of digits
 * and optionally a two way up-down button. It can be used for displaying and
 * modifying a single formatted <code>double</code> value with an optional
 * unit string (also in digits) displyed next to the value. Value manipulation
 * and display formatting is handled by the <code>WheelswitchFormatter</code>.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see UpDownButton
 * @see Digit
 * @see WheelswitchFormatter
 */
public class Wheelswitch extends JPanel
{
	/*
	 * An implementation of FocusListener removes digit selection
	 * when focus is lost.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 */
	protected class FocusHandler extends FocusAdapter
	{
		/**
		 * Adds default digit selection when focus is gained and no digit is
		 * selected.
		 *
		 * @param e
		 */
        @Override
		public void focusGained(FocusEvent e)
		{
			if (getSelectedDigit() == -1) {
				setSelectedDigit(0);
			}
		}

		/**
		 * Removes digit selection when focus is lost.
		 *
		 * @param e
		 */
        @Override
		public void focusLost(FocusEvent e)
		{
			if ((e.getOppositeComponent() != upDownButton)
			    || (e.getOppositeComponent() != Wheelswitch.this)) {
				setSelectedDigit(-1);
			}
		}
	}

	/*
	 * An implementation of KeyListener used for handling
	 * key commands for the wheelswitch.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 */
	protected class KeyHandler extends KeyAdapter
	{
		/**
		 * Updates the digits in this Wheelswitch when key is pressed.
		 *
		 * @param e
		 */
        @Override
		public void keyPressed(KeyEvent e)
		{
			if (editable && isEnabled()) {
				int i = getSelectedDigit();

				if (i >= 0) {
					if ((e.getKeyChar() >= '0') && (e.getKeyChar() <= '9')) {
						setDigitValue(i,
						    Integer.parseInt(String.valueOf(e.getKeyChar())));
						setSelectedDigit(INCREASE_SELECTION);
					} else if (e.getKeyCode() == KeyEvent.VK_UP) {
						setDigitValue(i, ValueDigit.INCREASE_VALUE);
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						setDigitValue(i, ValueDigit.DECREASE_VALUE);
					} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						setSelectedDigit(INCREASE_SELECTION);
					} else if ((e.getKeyCode() == KeyEvent.VK_LEFT)
					    || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
						setSelectedDigit(DECREASE_SELECTION);
					}
				} else {
					setSelectedDigit(0);
				}
			}
		}
	}

	/*
	 * An implementation of KeyListener used for handling
	 * key commands for the wheelswitch.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 */
	protected class MouseHandler extends MouseAdapter
	{
		/**
		 * Updates digit selection in this Wheelswitch when left mouse button
		 * is pressed.
		 *
		 * @param e
		 */
        @Override
		public void mousePressed(MouseEvent e)
		{
			if (e.getButton() == MouseEvent.BUTTON1) {
				setSelectedDigit(digits.indexOf(e.getSource()));
				requestFocusInWindow();
			}
		}
	}

	/*
	 * An implementation of the MouseWheelListener used for handling
	 * mouse wheel events inside the wheelswitch.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 */
	protected class MouseWheelHandler implements MouseWheelListener
	{
		/**
		 * Updates the value of the Wheelswitch when UpDownButton is pressed.
		 *
		 * @param e DOCUMENT ME!
		 */
        @Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			logger.log(Level.FINE, "mousewheel");

			if (editable && isEnabled()) {
				int i = getSelectedDigit();

				if (i >= 0) {
					if (e.getWheelRotation() > 0) {
						setDigitValue(i, ValueDigit.DECREASE_VALUE);
					} else {
						setDigitValue(i, ValueDigit.INCREASE_VALUE);
					}
				}
			}
		}
	}

	/*
	 * An extension of Timer used for periodic tilting of the Wheelswitch.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 *
	 */
	protected class TiltHandler extends Timer
	{
		private class TiltTask extends TimerTask
		{
			/**
			 * DOCUMENT ME!
			 */
            @Override
			public void run()
			{
				if (numberOfTilts >= MAX_NUMBER_OF_TILTS) {
					cancel();

					for (int i = 0; i < digits.size(); i++) {
						((Digit)digits.get(i)).setTilting(false);
					}
				} else {
					numberOfTilts++;

					for (int i = 0; i < digits.size(); i++) {
						((Digit)digits.get(i)).setTilting(!((Digit)digits.get(i))
						    .isTilting());
					}
				}

				repaint();
			}
		}

//		private AudioClip clip1;
//		private AudioClip clip2;
		private final int MAX_NUMBER_OF_TILTS = 3;
		private final long TILT_RATE = 200;
		private int numberOfTilts = MAX_NUMBER_OF_TILTS;

		/**
		 * Scedules a new tilting task if the user value equals any of the
		 * bounds.
		 */
		public void tilt()
		{
			if ((formatter.getValue() < formatter.getMaximum())
			    && (formatter.getValue() > formatter.getMinimum())
			    || !tiltingEnabled) {
				//	would be a nice demonstration feature
				//					if (enhanced && clip1==null) {
				//						clip1 = 	Applet.newAudioClip(ClassLoader.getSystemResource("Resources/tick.wav"));
				//					}        
				//					if (enhanced) clip1.play();
				return;
			}

			//			would be a nice demonstration feature
			//			if (enhanced && clip2==null) {
			//				clip2 = 	Applet.newAudioClip(ClassLoader.getSystemResource("Resources/error.wav"));
			//			}
			//			if (enhanced) clip2.play();
			if (numberOfTilts >= MAX_NUMBER_OF_TILTS) {
				numberOfTilts = 0;
				schedule(new TiltTask(), 0, TILT_RATE);
			} else {
				numberOfTilts = 0;
			}
		}
	}

	/*
	 * An implementation of UpDownListener used for handling
	 * events from the up-down button in the wheelswitch.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 */
	protected class UpDownActionHandler implements UpDownListener
	{
		/**
		 * Updates the value of the Wheelswitch when UpDownButton is pressed.
		 *
		 * @param e
		 */
        @Override
		public void upDownChanged(ChangeEvent e)
		{
			if (editable && isEnabled()) {
				int i = getSelectedDigit();

				if (i >= 0) {
					//Luckily UpDownButton.UP_PRESSED equals ValueDigit.INCREASE_VALUE
					//likewise UpDownButton.DOWN_PRESSED equals ValueDigit.DECREASE_VALUE
					setDigitValue(i, ((UpDownButton)e.getSource()).getValue());
				} else {
					// TODO Jernej should tell if this is OK
					setSelectedDigit(digits.size() - 1);
				}

				//requestFocusInWindow();
			}
		}
	}

	protected static int INCREASE_SELECTION = -11;
	protected static int DECREASE_SELECTION = -12;

	/** DOCUMENT ME! */
	public static final String VALUE = "value";

	/** DOCUMENT ME! */
	public static final String EDITABLE = "editable";
    private static final Logger logger = Logger.getLogger("global");
	protected FocusHandler focusHandler;
	protected KeyHandler keyHandler;
	protected MouseHandler mouseHandler;
	protected TiltHandler tiltHandler;
	private Dimension minimumSize = null;
	private Dimension preferredSize = null;
	private java.util.List digits;
	private java.util.List unitDigits;
	private UpDownButton upDownButton;
	private WheelswitchFormatter formatter;
	private boolean editable = true;
	private boolean enhanced = true;
	private boolean tiltingEnabled;
	private int selectedDigit = -1;
    
	/**
	 * Constructor for <code>Wheelswitch</code> creates a new Wheelswitch with
	 * the specified value, format and unit. No minimum or maximum values are
	 * set.
	 *
	 * @param newFormat
	 * @param newValue
	 * @param newUnit
	 */
	public Wheelswitch(String newFormat, double newValue, String newUnit)
	{
		super();
		listenerList = new EventListenerList();
		mouseHandler = new MouseHandler();
		keyHandler = new KeyHandler();
		focusHandler = new FocusHandler();
		tiltHandler = new TiltHandler();

		digits = new ArrayList();
		unitDigits = new ArrayList();

		upDownButton = new UpDownButton();
		upDownButton.setName("upDownButton");
		upDownButton.setEnabled(isEnabled());
		upDownButton.addUpDownListener(new UpDownActionHandler());
		upDownButton.addFocusListener(focusHandler);
		upDownButton.addKeyListener(keyHandler);

		formatter = new WheelswitchFormatter(newFormat);
		formatter.setValue(newValue);
		formatter.setUnit(newUnit);

		addKeyListener(keyHandler);
		addMouseWheelListener(new MouseWheelHandler());
		addFocusListener(focusHandler);

		setFocusable(true);
		setBackground(ColorHelper.getCosyControl());
		setLayout(new WheelswitchLayout());

		setupValueDigits();
		setupUnitDigits();
		setupLayout();
		validate();
		repaint();
	}

	/**
	 * Constructor for <code>Wheelswitch</code> setting only the value. No
	 * format or unit are set.
	 *
	 * @param newValue
	 *
	 * @see #Wheelswitch(String, double, String)
	 */
	public Wheelswitch(double newValue)
	{
		this(null, newValue, null);
	}

	/**
	 * Constructor for Wheelswitch which sets no format or unit and the value
	 * is set to zero.
	 *
	 * @see #Wheelswitch(String, double, String)
	 */
	public Wheelswitch()
	{
		this(null, 0, null);
	}

	/**
	 * Sets the editability of the wheelswitch.
	 *
	 * @param newEditable
	 */
	public void setEditable(boolean newEditable)
	{
		if (newEditable == editable) {
			return;
		}

		boolean oldEditable = editable;
		editable = newEditable;
		firePropertyChange(EDITABLE, oldEditable, newEditable);

		if (!isEnabled()) {
			return;
		}

		setupLayout();
		validate();
		repaint();
	}

	/**
	 * Returns whether the wheelswitch can be edited by the user.
	 *
	 * @return boolean
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Sets the enhanced property of the wheelswitch. When enhanced the
	 * wheelswitch animates its digits when changing the value displayed.
	 *
	 * @see Digit#setEnhanced(boolean)
	 */
	public void setEnhanced(boolean enhanced)
	{
		if (this.enhanced == enhanced) {
			return;
		}

		firePropertyChange("enhanced", this.enhanced, enhanced);
		this.enhanced = enhanced;

		for (int i = 0; i < digits.size(); i++) {
			((Digit)digits.get(i)).setEnhanced(enhanced);
		}

		for (int i = 0; i < unitDigits.size(); i++) {
			((Digit)unitDigits.get(i)).setEnhanced(enhanced);
		}
	}

	/**
	 * Gets the enhancement mode of the <code>Wheelswitch</code>.
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isEnhanced()
	{
		return enhanced;
	}

	/**
	 * Sets the format of the value display. Format style is Wheelswitch
	 * specific.
	 *
	 * @param newFormat
	 *
	 * @throws NullPointerException DOCUMENT ME!
	 *
	 * @see WheelswitchFormatter#setFormat(String)
	 */
	public void setFormat(String newFormat)
	{
		if (newFormat == null) {
			throw new NullPointerException("newFormat");
		}

		if ((formatter.getFormat() != null)
		    && formatter.getFormat().equals(newFormat)) {
			return;
		}

		String oldFormat = formatter.getFormat();

		try {
			formatter.setFormat(newFormat);
		} catch (IllegalArgumentException e) {
			logger.log(Level.WARNING, "Exception setting new format: " + newFormat, e);

			return;
		}

		firePropertyChange("format", oldFormat, newFormat);

		if (!isEnabled()) {
			return;
		}

		setupValueDigits();
		setupLayout();
		validate();
		repaint();
	}

	/**
	 * Gets the format of the display.
	 *
	 * @return java.lang.String
	 *
	 * @see WheelswitchFormatter#getFormat()
	 */
	public String getFormat()
	{
		return formatter.getFormat();
	}

	/**
	 * Sets the maximum allowed value.
	 *
	 * @param newValue
	 *
	 * @see WheelswitchFormatter#setMaximum(double)
	 */
	public void setGraphMax(double newValue)
	{
		double oldValue = formatter.getMaximum();

		if (oldValue == newValue) {
			return;
		}

		formatter.setMaximum(newValue);

		if (getValue() > formatter.getMaximum()) {
			setValue(formatter.getMaximum());
		}

		firePropertyChange("graphMax", oldValue, newValue);
	}

	/**
	 * Gets the maximum alowed value.
	 *
	 * @return double
	 *
	 * @see WheelswitchFormatter#getMaximum()
	 */
	public double getGraphMax()
	{
		return formatter.getMaximum();
	}

	/**
	 * Sets the minimum allowed value.
	 *
	 * @param newValue
	 *
	 * @see WheelswitchFormatter#setMinimum(double)
	 */
	public void setGraphMin(double newValue)
	{
		double oldValue = formatter.getMinimum();

		if (oldValue == newValue) {
			return;
		}

		formatter.setMinimum(newValue);

		if (getValue() < formatter.getMinimum()) {
			setValue(formatter.getMinimum());
		}

		firePropertyChange("graphMin", oldValue, newValue);
	}

	/**
	 * Gets the minimum alowed value.
	 *
	 * @return double
	 *
	 * @see WheelswitchFormatter#getMaximum()
	 */
	public double getGraphMin()
	{
		return formatter.getMinimum();
	}

	/**
	 * Sets the maximum and minimum allowed values.
	 *
	 * @param max
	 * @param min
	 */
	public void setMaxMin(double max, double min)
	{
		setGraphMax(max);
		setGraphMin(min);
	}

	/**
	 * This method has been overriden to implement correct layout and resizing
	 * features.
	 *
	 * @see java.awt.Component#getMinimumSize()
	 */
    @Override
	public Dimension getMinimumSize()
	{
		if (minimumSize == null) {
			int height = 0;
			int width = 0;

			for (int i = 0; i < getComponentCount(); i++) {
				width += getComponent(i).getMinimumSize().width;
				height = Math.max(height,
					    getComponent(i).getMinimumSize().height);
			}

			minimumSize = new Dimension(width, height);
		}

		return minimumSize;
	}

	/**
	 * This method has been overriden to implement correct layout and resizing
	 * features.
	 *
	 * @see java.awt.Component#getPreferredSize()
	 */
    @Override
	public Dimension getPreferredSize()
	{
		if (preferredSize == null) {
			int height = 0;
			int width = 0;

			for (int i = 0; i < getComponentCount(); i++) {
				width += getComponent(i).getPreferredSize().width;
				height = Math.max(height,
					    getComponent(i).getMinimumSize().height);
			}

			preferredSize = new Dimension(width, height);
		}

		return preferredSize;
	}

	/**
	 * Sets the tilitng enabled property.
	 *
	 * @param b whether the component should tilt when value is out of bounds.
	 */
	public void setTiltingEnabled(boolean b)
	{
		if (tiltingEnabled == b) {
			return;
		}

		tiltingEnabled = b;
		firePropertyChange("tiltingEnabled", !b, b);
	}

	/**
	 * Returns whether the component should indicate value out of bounds
	 * condition by visually tilting its border.
	 *
	 * @return boolean
	 */
	public boolean isTiltingEnabled()
	{
		return tiltingEnabled;
	}

	/**
	 * Sets the unit to be displayed next to the value.
	 *
	 * @param newUnit
	 *
	 * @see WheelswitchFormatter#setUnit(String)
	 */
	public void setUnit(String newUnit)
	{
		String oldUnit = formatter.getUnit();

		if (oldUnit == newUnit) {
			return;
		}

		formatter.setUnit(newUnit);
		firePropertyChange("unit", oldUnit, newUnit);

		if (!isEnabled()) {
			return;
		}

		setupUnitDigits();
		setupLayout();
		doLayout();
		repaint();
	}

	/**
	 * Gets the unit displayed next to the value.
	 *
	 * @return java.lang.String
	 *
	 * @see WheelswitchFormatter#getUnit()
	 */
	public String getUnit()
	{
		return formatter.getUnit();
	}

	/**
	 * Sets the value and displays it in the wheelswitch. The method may also
	 * change the current digit selection if neccessary in order to point to
	 * the same decimal digit of the displayed value.
	 *
	 * @param newValue
	 *
	 * @see WheelswitchFormatter#setValue(double)
	 */
	public void setValue(double newValue)
	{
		double oldValue = formatter.getValue();

		if (oldValue == newValue) {
			return;
		}

		firePropertyChange(VALUE, oldValue, newValue);

		int oldDigitSelection = getSelectedDigit();
		int decimalSelection = parseDecimalPosition(oldDigitSelection);

		String oldStringValue = formatter.getString();
		formatter.setValue(newValue);

		String newStringValue = formatter.getString();

		if (!isEnabled()) {
			return;
		}

		process(oldStringValue, newStringValue);

		int newDigitSelection = parseDigitPosition(decimalSelection);

		if ((newDigitSelection < digits.size()) && (newDigitSelection >= 0)) {
			setSelectedDigit(newDigitSelection);
		} else {
			setSelectedDigit(-1);
		}

		tiltHandler.tilt();
	}

	/**
	 * Gets the value displayed by the <code>Wheelswitch</code> and stored by
	 * the <code>formatter</code>.
	 *
	 * @return double
	 *
	 * @see WheelswitchFormatter#getValue()
	 */
	public double getValue()
	{
		return formatter.getValue();
	}

	/**
	 * Adds an <code>SetListener</code> to the array of listeners currently
	 * registered for listening to the value sets of the
	 * <code>Wheelswitch</code>. These listeners are notified whenever the
	 * user sets a new value.
	 *
	 * @param l
	 *
	 * @see com.cosylab.events.SetListener
	 */
	public void addSetListener(SetListener l)
	{
		listenerList.add(SetListener.class, l);
	}


	/**
	 * Overriden to implement digit and upDownButton enabling/disabling.
	 *
	 * @see java.awt.Component#setEnabled(boolean)
	 */
    @Override
	public void setEnabled(boolean arg0)
	{
		super.setEnabled(arg0);

		setupValueDigits();
		setupLayout();
		validate();
		repaint();

		for (int i = 0; i < digits.size(); i++) {
			((Digit)digits.get(i)).setEnabled(arg0);
		}

		for (int i = 0; i < unitDigits.size(); i++) {
			((Digit)unitDigits.get(i)).setEnabled(arg0);
		}

		upDownButton.setEnabled(arg0);
	}

	/**
	 * Sets the maximum number of value digits allowed to be  displayed in the
	 * wheelswitch. The default value is 0 and is ignored.
	 *
	 * @param bound is ignored if less or equal zero.
	 */
	public void setMaximumDigits(int bound)
	{
		int oldBound = formatter.getMaximumDigits();
		formatter.setMaximumDigits(bound);
		firePropertyChange("maximumDigits", oldBound, bound);
	}

	/**
	 * Removes an <code>SetListener</code> from the array of listeners
	 * currently registered for listening to the value sets of the
	 * <code>Wheelswitch</code>.
	 *
	 * @param l DOCUMENT ME!
	 */
	public void removeSetListener(SetListener l)
	{
		listenerList.remove(SetListener.class, l);
	}

	/*
	 * Sets the value at the i-th digit
	 */
	protected void setDigitValue(int i, int newValue)
	{
		if (digits.get(i) instanceof StaticDigit) {
			return;
		}

		String oldStringValue = formatter.getString();
		String newStringValue = oldStringValue;

		//flip increase/decrease for negative values
		if ((newValue == ValueDigit.INCREASE_VALUE)
		    || (newValue == ValueDigit.DECREASE_VALUE)) {
			int expIndex = oldStringValue.indexOf('E');

			if (expIndex == -1) {
				expIndex = oldStringValue.length();
			}

			if (((oldStringValue.charAt(0) == '-') && (i < expIndex))
			    ^ ((i > expIndex) && (expIndex < (oldStringValue.length() - 1))
			    && (oldStringValue.charAt(expIndex + 1) == '-'))) {
				if (newValue == ValueDigit.INCREASE_VALUE) {
					newValue = ValueDigit.DECREASE_VALUE;
				} else {
					newValue = ValueDigit.INCREASE_VALUE;
				}
			}
		}

		if ((newValue >= 0) && (newValue <= 9)) {
			newStringValue = oldStringValue.substring(0, i)
				+ String.valueOf(newValue) + oldStringValue.substring(i + 1);
		} else if (newValue == ValueDigit.INCREASE_VALUE) {
			int j;

			for (j = i;
			    ((j >= 0) && (oldStringValue.charAt(j) != 'E')
			    && (oldStringValue.charAt(j) != '+')
			    && (oldStringValue.charAt(j) != '-') && (newValue != 0));
			    j--) {
				if (oldStringValue.charAt(j) == '.') {
					continue;
				} else if (oldStringValue.charAt(j) != '9') {
					newStringValue = newStringValue.substring(0, j)
						+ String.valueOf(Integer.parseInt(
						        newStringValue.substring(j, j + 1)) + 1)
						+ newStringValue.substring(j + 1);
					newValue = 0;
				} else {
					newStringValue = newStringValue.substring(0, j) + "0"
						+ newStringValue.substring(j + 1);
				}
			}

			if (newValue != 0) {
				if (oldStringValue.charAt(0) == '.') {
					newStringValue = "1" + newStringValue;
				} else if ((oldStringValue.charAt(j + 1) == '.')
				    && ((oldStringValue.charAt(j) == '+')
				    || (oldStringValue.charAt(j) == '-')
				    || (oldStringValue.charAt(j) == 'E'))) {
					newStringValue = newStringValue.substring(0, 1) + "1"
						+ newStringValue.substring(1);
				} else {
					newStringValue = newStringValue.substring(0, j + 1) + "10"
						+ newStringValue.substring(j + 2);
				}
			}
		} else if (newValue == ValueDigit.DECREASE_VALUE) {
			boolean signTag = false;

			for (int j = i;
			    ((j >= 0) && (oldStringValue.charAt(j) != 'E')
			    && (oldStringValue.charAt(j) != '+')
			    && (oldStringValue.charAt(j) != '-')); j--) {
				if ((oldStringValue.charAt(j) != '.')
				    && (oldStringValue.charAt(j) != '0')) {
					signTag = true;
				}
			}

			if (signTag) {
				for (int j = i;
				    ((j >= 0) && (oldStringValue.charAt(j) != 'E')
				    && (oldStringValue.charAt(j) != '+')
				    && (oldStringValue.charAt(j) != '-') && (newValue != 0));
				    j--) {
					if (oldStringValue.charAt(j) == '.') {
						;
					} else if (oldStringValue.charAt(j) != '0') {
						newStringValue = newStringValue.substring(0, j)
							+ String.valueOf(Integer.parseInt(
							        newStringValue.substring(j, j + 1)) - 1)
							+ newStringValue.substring(j + 1);
						newValue = 0;
					} else {
						newStringValue = newStringValue.substring(0, j) + "9"
							+ newStringValue.substring(j + 1);
					}
				}
			} else {
				int indexOfE = oldStringValue.indexOf('E');

				if (i > indexOfE) {
					if (oldStringValue.charAt(indexOfE + 1) == '+') {
						newStringValue = newStringValue.substring(0,
							    indexOfE + 1) + "-"
							+ newStringValue.substring(indexOfE + 2, i) + "1"
							+ newStringValue.substring(i + 1);
					} else if (oldStringValue.charAt(indexOfE + 1) == '-') {
						newStringValue = newStringValue.substring(0,
							    indexOfE + 1) + "+"
							+ newStringValue.substring(indexOfE + 2, i) + "1"
							+ newStringValue.substring(i + 1);
					} else {
						newStringValue = newStringValue.substring(0,
							    indexOfE + 1) + "-"
							+ newStringValue.substring(indexOfE + 1, i) + "1"
							+ newStringValue.substring(i + 1);
					}
				} else {
					if (oldStringValue.charAt(0) == '+') {
						newStringValue = "-" + newStringValue.substring(1, i)
							+ "1" + newStringValue.substring(i + 1);
					} else if (oldStringValue.charAt(0) == '-') {
						newStringValue = "+" + newStringValue.substring(1, i)
							+ "1" + newStringValue.substring(i + 1);
					} else {
						newStringValue = "-" + newStringValue.substring(0, i)
							+ "1" + newStringValue.substring(i + 1);
					}
				}
			}
		}

		setStringValue(newStringValue);
	}

	/*
	 * Sets a new Digit selection.
	 * @param i new digit selection.
	 */
	protected void setSelectedDigit(int i)
	{
		if (editable) {
			if (i == selectedDigit) {
				return;
			}

			if ((selectedDigit >= 0) && (selectedDigit < digits.size())) {
				((Digit)digits.get(selectedDigit)).setSelected(false);
			}

			if ((i == INCREASE_SELECTION)
			    || ((i >= 0) && (i < digits.size())
			    && digits.get(i) instanceof StaticDigit)) {
				if (i == INCREASE_SELECTION) {
					i = selectedDigit;
				}

				while ((++i < digits.size())
				    && digits.get(i) instanceof StaticDigit) {
					;
				}

				if (i == digits.size()) {
					i = -1;

					while (digits.get(++i) instanceof StaticDigit) {
						;
					}
				}
			} else if (i == DECREASE_SELECTION) {
				i = selectedDigit;

				while ((--i >= 0) && digits.get(i) instanceof StaticDigit) {
					;
				}

				if (i < 0) {
					i = digits.size();

					while (digits.get(--i) instanceof StaticDigit) {
						;
					}
				}
			}

			if ((i >= 0) && (i < digits.size())) {
				((Digit)digits.get(i)).setSelected(true);
			}

			selectedDigit = i;
		}
	}

	/*
	 * Returns the currently selected digit.
	 * @return int selection index
	 */
	protected int getSelectedDigit()
	{
		return selectedDigit;
	}

	/*
	 * Fires a <code>SetEvent</code> to all currently
	 * registered <code>SetListener</code>s of the <code>Wheelswitch</code>.
	 */
	protected void fireSetPerformed(double newValue)
	{
		Object[] listeners = listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == SetListener.class) {
				((SetListener)listeners[i + 1]).setPerformed(new SetEvent(
				        this, newValue));
			}
		}
	}

	/*
	 * (Re)initializes existing value digits inside the wheelswitch.
	 */
	protected void initDigits()
	{
		String stringValue = formatter.getString();
		char digitValue;

		for (int i = 0; i < stringValue.length(); i++) {
			digitValue = stringValue.charAt(i);

			if ((digitValue == '+') || (digitValue == '-')
			    || (digitValue == 'E') || (digitValue == 'e')
			    || (digitValue == '.')) {
				;
			} else {
				if (digits.get(i) instanceof ValueDigit) {
					((ValueDigit)digits.get(i)).setValue(Integer.parseInt(
					        stringValue.substring(i, i + 1)));
				} else {
					logger.log(Level.WARNING,
					    "Wheelswitch#initDigits(): digits improperly synchronized");
					setupValueDigits();
					setupLayout();
					validate();
					repaint();

					return;
				}
			}
		}
	}

	/*
	 * Repositions the components inside the wheelswitch.
	 */
	protected void setupLayout()
	{
		removeAll();

		//  by mkadunc
		//		add(Box.createHorizontalStrut(5));
		for (int i = 0; i < digits.size(); i++) {
			if (((Digit)digits.get(i)).getText().indexOf('E') != -1) {
				add(Box.createHorizontalStrut(3));
			}

			add((Digit)digits.get(i));
		}

		if (unitDigits.isEmpty()) {
			add(Box.createHorizontalStrut(5));

			for (int i = 0; i < unitDigits.size(); i++) {
				add((Digit)unitDigits.get(i));
			}
		}

		if (editable) {
			add(Box.createHorizontalStrut(5));
			add(upDownButton);
		}

		preferredSize = null;
		minimumSize = null;

		//requestFocusInWindow();
	}

	/*
	 * Constructs unit digits from scratch.
	 */
	protected void setupUnitDigits()
	{
		unitDigits.clear();

		String unit = formatter.getUnit();

		if (unit == null) {
			return;
		}

		Digit digit;

		for (int i = 0; i < unit.length(); i++) {
			digit = new StaticDigit(unit.substring(i, i + 1));
			digit.setEnhanced(enhanced);
			digit.setEnabled(isEnabled());
			unitDigits.add(digit);
		}
	}

	/*
	 * Contructs value digits from scratch.
	 */
	protected void setupValueDigits()
	{
		digits.clear();
		selectedDigit = -1;

		String stringValue = formatter.getString();
		Digit digit;

		for (int i = 0; i < stringValue.length(); i++) {
			char digitValue = stringValue.charAt(i);

			if ((digitValue >= '0') && (digitValue <= '9')) {
				digit = new ValueDigit(Integer.parseInt(stringValue.substring(
					            i, i + 1)));
			} else {
				digit = new StaticDigit(stringValue.substring(i, i + 1));
			}

			digit.addMouseListener(mouseHandler);
			digit.setEnhanced(enhanced);
			digit.setEnabled(isEnabled());
			digits.add(digit);
		}
	}

	/*
	 * Called by setDigitValue(int,int) when the user modifies the digits.
	 * Sets a new string value.
	 */
	private void setStringValue(String newStringValue)
	{
		double oldValue = formatter.getValue();
		String oldStringValue = formatter.getString();
		formatter.setString(newStringValue);

		double newValue = formatter.getValue();
		newStringValue = formatter.getString();

		if (oldValue != newValue) {
			firePropertyChange(VALUE, oldValue, newValue);
			fireSetPerformed(newValue);
		}

		if (!isEnabled()) {
			return;
		}

		int oldDigitSelection = getSelectedDigit();
		int decimalSelection = parseDecimalPosition(oldDigitSelection);

		process(oldStringValue, newStringValue);

		int newDigitSelection = parseDigitPosition(decimalSelection);

		if ((newDigitSelection < digits.size()) && (newDigitSelection >= 0)) {
			setSelectedDigit(newDigitSelection);
		} else {
			setSelectedDigit(-1);
		}

		tiltHandler.tilt();
	}

	/*
	 * Called by setStringValue(String). Computes the decimal position represented
	 * by the digit position
	 */
	private int parseDecimalPosition(int digitPosition)
	{
		if (digitPosition == -1) {
			return Integer.MAX_VALUE;
		}

		String digits = formatter.getString();
		int decimalPosition = 0;
		int expIndex = digits.indexOf('E');
		int dotIndex = digits.indexOf('.');

		if ((expIndex == -1) || (digitPosition < expIndex)) {
			if (dotIndex != -1) {
				decimalPosition += (dotIndex - digitPosition);
			} else if (expIndex != -1) {
				decimalPosition += (expIndex - digitPosition);
			} else {
				decimalPosition += (digits.length() - digitPosition);
			}

			if (decimalPosition > 0) {
				decimalPosition--;
			}

			if (expIndex != -1) {
				int exponent = (int)Double.parseDouble(digits.substring(expIndex
					        + 1));
				decimalPosition += exponent;
			}
		} else {
			//multiples of hundreds are returned for exponent digits
			decimalPosition = 100 * (digits.length() - digitPosition);
		}

		return decimalPosition;
	}

	/*
	 * Called by setStringValue(String) Computes the digit position given the decimal
	 * position
	 */
	private int parseDigitPosition(int decimalPosition)
	{
		if (decimalPosition == Integer.MAX_VALUE) {
			return -1;
		}

		String digits = formatter.getString();
		int digitPosition = 0;
		int expIndex = digits.indexOf('E');
		int dotIndex = digits.indexOf('.');

		if (((decimalPosition % 100) == 0) && (decimalPosition != 0)) {
			decimalPosition /= 100;

			if ((expIndex == -1)
			    || ((digits.length() - expIndex) < decimalPosition)) {
				return Integer.MAX_VALUE;
			}

			digitPosition = digits.length() - decimalPosition;

			if ((digits.charAt(digitPosition) == '+')
			    || (digits.charAt(digitPosition) == '-')) {
				return Integer.MAX_VALUE;
			}

			return digitPosition;
		} else {
			if (dotIndex != -1) {
				digitPosition += (dotIndex - decimalPosition);
			} else if (expIndex != -1) {
				digitPosition += (expIndex - decimalPosition - 1);
			} else {
				digitPosition += (digits.length() - decimalPosition - 1);
			}

			if (expIndex != -1) {
				int exponent = (int)Double.parseDouble(digits.substring(expIndex
					        + 1));
				digitPosition += exponent;
			}

			if ((expIndex != -1) && (digitPosition >= expIndex)) {
				return Integer.MAX_VALUE;
			} else if (digitPosition >= digits.length()) {
				return Integer.MAX_VALUE;
			}
		}

		if (digitPosition <= dotIndex) {
			digitPosition--;
		}

		if ((digitPosition < 0)
		    || ((digitPosition == 0)
		    && ((digits.charAt(0) == '+') || (digits.charAt(0) == '-')))) {
			return Integer.MAX_VALUE;
		}

		return digitPosition;
	}

	private void process(String oldStringValue, String newStringValue)
	{
		if ((oldStringValue.length() == newStringValue.length())
		    && (oldStringValue.indexOf(".") == newStringValue.indexOf("."))
		    && (oldStringValue.indexOf("E") == newStringValue.indexOf("E"))
		    && (oldStringValue.indexOf("e") == newStringValue.indexOf("e"))
		    && (oldStringValue.indexOf("+") == newStringValue.indexOf("+"))
		    && (oldStringValue.indexOf("-") == newStringValue.indexOf("-"))
		    && (oldStringValue.indexOf("+", oldStringValue.indexOf("E")) == newStringValue
		    .indexOf("+", newStringValue.indexOf("E")))
		    && (oldStringValue.indexOf("+", oldStringValue.indexOf("e")) == newStringValue
		    .indexOf("+", newStringValue.indexOf("e")))
		    && (oldStringValue.indexOf("-", oldStringValue.indexOf("e")) == newStringValue
		    .indexOf("-", newStringValue.indexOf("e")))
		    && (oldStringValue.indexOf("-", oldStringValue.indexOf("E")) == newStringValue
		    .indexOf("-", newStringValue.indexOf("E")))) {
			initDigits();
		} else {
			setupValueDigits();
			setupLayout();
			validate();
			repaint();
		}
	}
}

/* __oOo__ */
