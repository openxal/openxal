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

package xal.extension.widgets.swing.wheelswitch;

import xal.extension.widgets.swing.wheelswitch.util.PrintfFormat;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Formats <code>java.lang.String</code>s to be displayed as digits in  the
 * <code>Wheelswitch</code>. The format is specified in a format
 * <code>java.lang.String</code>. <code>WheelswitchFormatter</code> also
 * stores the value, its bounds (minimum and maximum) and also the unit
 * displayed by the <code>Wheelswitch</code>.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class WheelswitchFormatter
{
    private static final Logger logger = Logger.getLogger("global");
	protected PrintfFormat defFormatter;
	protected PrintfFormat formatter;
	protected String formatString = null;
	protected String generatedFormatString = null;
	protected String unit = null;
	protected String valueString = "0.00";
	protected double maximum;
	protected double minimum;
	protected double value = 0.;
	private int maximumDigits = 10;
            
	/**
	 * Constructs the PlainWheelswitchFormatter and sets the format  string.
	 *
	 * @param newFormatString
	 *
	 * @see #setFormat(String)
	 */
	public WheelswitchFormatter(String newFormatString)
	{
		super();
		defFormatter = new PrintfFormat("%3.2e");
		setMaximum(Double.POSITIVE_INFINITY);
		setMinimum(Double.NEGATIVE_INFINITY);
		setFormat(newFormatString);
	}

	/**
	 * Constructs the PlainWheelswitchFormatter with no format string.
	 *
	 * @see #setFormat(String)
	 */
	public WheelswitchFormatter()
	{
		this(null);
	}

	/**
	 * Sets the format string specifiing the format of <code>Wheelswitch</code>
	 * display. The format is first checked for validity by the
	 * <code>checkFormat(String)</code> method.
	 *
	 * @param newFormatString
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
	 *
	 * @see Double#parseDouble(String)
	 * @see NumberFormatException
	 */
	public final void setFormat(String newFormatString)
		throws IllegalArgumentException
	{
		if ((newFormatString == null && formatString == null) ||
                (formatString != null && formatString.equals(newFormatString))) {
			return;
		} else if (newFormatString == null || newFormatString.isEmpty()) {
			formatString = null;
			generatedFormatString = null;
			formatter = new PrintfFormat(transformFormat(generateFormat()));
		} else if (checkFormat(newFormatString)) {
			formatString = newFormatString;
			formatter = new PrintfFormat(transformFormat(newFormatString));
		} else {
			throw (new IllegalArgumentException(
			    "Invalid format string entered."));
		}
     
		setString(String.valueOf(value));
	}

	/**
	 * Gets the currently stored format string.
	 *
	 * @return DOCUMENT ME!
	 */
	public String getFormat()
	{
		return formatString;
	}

	/**
	 * Sets a new maximum allowed value.
	 *
	 * @param newMaximum DOCUMENT ME!
	 */
	public final void setMaximum(double newMaximum)
	{
		if (newMaximum < minimum) {
			newMaximum = minimum;
		}

		maximum = newMaximum;

		if (formatString == null) {
			generatedFormatString = null;
			formatter = new PrintfFormat(transformFormat(generateFormat()));
		}
	}

	/**
	 * Gets the current maximum allowed value.
	 *
	 * @return double
	 */
	public double getMaximum()
	{
		return maximum;
	}

	/**
	 * Sets a new minimum allowed value.
	 *
	 * @param newMinimum DOCUMENT ME!
	 */
	public final void setMinimum(double newMinimum)
	{
		if (newMinimum > maximum) {
			newMinimum = maximum;
		}

		minimum = newMinimum;

		if (formatString == null) {
			generatedFormatString = null;
			formatter = new PrintfFormat(transformFormat(generateFormat()));
		}
	}

	/**
	 * Gets the current minimum allowed value.
	 *
	 * @return double
	 */
	public double getMinimum()
	{
		return minimum;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param newValueString String representing the number to be formatted.
	 */
	public void setString(String newValueString)
	{
		logger.log(Level.FINE, "WheelswitchFormatter#setString(): newValueString={0}", newValueString);
		logger.log(Level.FINE, "WheelswitchFormatter#setString(): formatString={0}", formatString);

		double newValue = Double.parseDouble(newValueString);

		if (newValue > maximum || newValue < minimum) {
			setValue(newValue);

			return;
		}

		String tempFormatString = formatString;

		if (formatString == null) {
			tempFormatString = generateFormat();
		}

		int newExpIndex = Math.max(newValueString.indexOf("E"),
			    newValueString.indexOf("e"));

		if (newExpIndex < 0) {
			newExpIndex = newValueString.length();
		}

		int formatExpIndex = tempFormatString.indexOf("E");

		if (formatExpIndex < 0) {
			formatExpIndex = tempFormatString.length();
		}

		int newDotIndex = Math.max(newValueString.indexOf("."),
			    newValueString.indexOf(","));

		if (newDotIndex < 0) {
			newDotIndex = newExpIndex;
		}

		int formatDotIndex = tempFormatString.indexOf(".");

		if (formatDotIndex < 0) {
			formatDotIndex = formatExpIndex;
		}

		int newSignTag = 0;

		if (newValueString.charAt(0) == '+') {
			newSignTag++;
		} else if (newValueString.charAt(0) == '-') {
			newSignTag--;
		}

		int formatSignTag = 0;

		if (tempFormatString.charAt(0) == '+') {
			formatSignTag++;
		}

		int formatExpSignTag = 0;

		if (formatExpIndex + 1 < tempFormatString.length()
		    && tempFormatString.charAt(formatExpIndex + 1) == '+') {
			formatExpSignTag++;
		}

		int newExpValue = 0;

		if (newExpIndex < newValueString.length() - 1) {
			newExpValue += (int)Double.parseDouble(newValueString.substring(newExpIndex
			        + 1));
		}

		newExpValue += newDotIndex - formatDotIndex;

		if (newSignTag != 0) {
			newExpValue--;
		}

		if (formatSignTag != 0) {
			newExpValue++;
		}

		newValueString = newValueString.substring(0, newDotIndex)
			+ ((newDotIndex < newExpIndex - 1)
			? (newValueString.substring(newDotIndex + 1, newExpIndex)) : (""));

		if (newSignTag != 0) {
			newValueString = newValueString.substring(1);
		}

		while (newValueString.charAt(0) == '0' && newValueString.length() > 1) {
			newValueString = newValueString.substring(1);
			newExpValue--;
		}

		if (newValue == 0.) {
			newExpValue = 0;
		}

		while (newValueString.length() < formatExpIndex - formatSignTag
		    - ((formatDotIndex < formatExpIndex) ? (1) : (0))) {
			newValueString += "0";
		}

		if (formatExpIndex + formatExpSignTag + 1 < tempFormatString.length()) {
			String exponent = String.valueOf(newExpValue);

			int newExpSignTag = 1;

			if (newExpValue < 0) {
				newExpSignTag = -1;
				exponent = exponent.substring(1);
			}

			newValueString = newValueString.substring(0,
				    formatDotIndex - formatSignTag) + "."
				+ newValueString.substring(formatDotIndex - formatSignTag);

			if (newValue != 0.) {
				newValueString = formatter.sprintf(Double.parseDouble(
					        newValueString)).replaceAll(",", ".");

				if (newValueString.charAt(0) == '+') {
					newValueString = newValueString.substring(1);
				}
			}

			if (newSignTag == -1) {
				newValueString = "-" + newValueString;
			} else if (formatSignTag == 1) {
				newValueString = "+" + newValueString;
			}

			newValueString += "E";

			if (newExpSignTag == -1) {
				newValueString += "-";
			} else if (formatExpSignTag == 1) {
				newValueString += "+";
			}

			int exponentLength = tempFormatString.length() - formatExpIndex
				- formatExpSignTag - 1;

			while (exponent.length() < exponentLength) {
				exponent = "0" + exponent;
			}

			newValueString += exponent;
		} else {
			while (newExpValue > 0) {
				newValueString += "0";
				formatDotIndex++;
				newExpValue--;
			}

			while (newExpValue < 0) {
				newValueString = "0" + newValueString;
				newExpValue++;
			}

			newValueString = newValueString.substring(0,
				    formatDotIndex - formatSignTag) + "."
				+ newValueString.substring(formatDotIndex - formatSignTag);

			if (newValueString.charAt(0) != '0') {
				newValueString = formatter.sprintf(Double.parseDouble(
					        newValueString)).replaceAll(",", ".");

				if (newValueString.charAt(0) == '+') {
					newValueString = newValueString.substring(1);
				}
			} else {
				newValueString = "1" + newValueString.substring(1);
				newValueString = formatter.sprintf(Double.parseDouble(
					        newValueString)).replaceAll(",", ".");

				if (newValueString.charAt(0) == '+') {
					newValueString = newValueString.substring(1);
				}

				newValueString = "0" + newValueString.substring(1);
			}

			if (newSignTag == -1) {
				newValueString = "-" + newValueString;
			} else if (formatSignTag == 1) {
				newValueString = "+" + newValueString;
			}
		}

		value = newValue;
		valueString = newValueString;

		logger.log(Level.FINE, "WheelswitchFormatter#setString(): value={0}", value);
		logger.log(Level.FINE, "WheelswitchFormatter#setString(): finalValueString={0}", valueString);
	}

	/**
	 * Gets the formatted string representing the currently stored value.
	 *
	 * @return String
	 */
	public String getString()
	{
		String retVal = valueString;

		if (maximumDigits > 0 && maximumDigits < valueString.length()) {
			retVal = defFormatter.sprintf(value);
		}

		return retVal;
	}

	/**
	 * Sets a new value and stores its formatted string. It only accepts values
	 * within bounds (maximum and minimum).
	 *
	 * @param newValue DOCUMENT ME!
	 *
	 * @see #getMaximum()
	 * @see #getMinimum()
	 */
	public void setValue(double newValue)
	{
		//		System.out.println("SET VALUE: " + newValue);
		if (newValue == value) {
			return;
		} else if (newValue <= maximum && newValue >= minimum) {
			value = newValue;
		} else if (newValue > maximum) {
			value = maximum;
		} else if (newValue < minimum) {
			value = minimum;
		}

		setString(String.valueOf(value));
	}

	/**
	 * Gets the currently stored value.
	 *
	 * @return double
	 */
	public double getValue()
	{
		return value;
	}

	/**
	 * Checks the number format string. The format string should only  consist
	 * of characters '#' reperesenting a single number digit, '+' representing
	 * a sign digit, '.' representing the decimal symbol  digit and 'E'
	 * representing the exponent denominator. These  characters should be
	 * arranged in such way that substitution of  the characters '#' with any
	 * numerical digits would result in a  correct double expression
	 * acceptible by <code>Double.parseDouble()</code>.
	 *
	 * @param format
	 *
	 * @return true if a correct format string was entered, false otherwise.
	 */
	public static boolean checkFormat(String format)
	{
		boolean dotted = false;
		boolean edotted = false;

		for (int i = 0; i < format.length(); i++) {
			if (format.charAt(i) != '#' && format.charAt(i) != '.'
			    && format.charAt(i) != 'E' && format.charAt(i) != '+') {
				return false;
			}

			if (format.charAt(i) == '.') {
				if (dotted) {
					return false;
				} else {
					dotted = true;
				}
			}

			if (format.charAt(i) == 'E') {
				if (edotted) {
					return false;
				} else {
					dotted = edotted = true;
				}

				if (i == format.length() - 1) {
					return false;
				}
			}

			if (format.charAt(i) == '+') {
				if (i != 0 && format.charAt(i - 1) != 'E') {
					return false;
				}

				if (i == format.length() - 1) {
					return false;
				}

				if (format.charAt(i + 1) == 'E') {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * A conviniance method for transforming between the  Printf type format
	 * strings and wheelswitch type format strings. The transform can be used
	 * in either direction. The method does not check for validity of the
	 * supplied parameter string and may return bogus results if invalid
	 * format strings are passed to it.
	 *
	 * @param format DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws NullPointerException DOCUMENT ME!
	 * @throws IllegalArgumentException DOCUMENT ME!
	 */
	public static String transformFormat(String format)
	{
		String retVal = new String();

		if (format == null) {
			throw new NullPointerException("format");
		}

		if (checkFormat(format)) {
			int expIndex = format.indexOf('E');

			if (expIndex == -1) {
				expIndex = format.indexOf('e');
			}

			if (expIndex == -1) {
				expIndex = format.length();
			}

			int dotIndex = format.indexOf('.');

			if (dotIndex == -1) {
				dotIndex = expIndex;
			}

			if (expIndex > dotIndex) {
				expIndex--;
			}

			int signTag = ((format.charAt(0) == '+') ? (1) : (0));
			retVal = "%";

			if (signTag == 1) {
				retVal += "+";
			}

			retVal += String.valueOf(expIndex - signTag) + "."
			+ String.valueOf(expIndex - dotIndex) + "f";
		} else {
			int startIndex = format.indexOf('%');
			boolean signTag = format.charAt(startIndex + 1) == '+';
			boolean exp = false;

			if (signTag) {
				startIndex++;
			}

			int finishIndex = format.indexOf('f', startIndex);

			if (finishIndex < 0) {
				finishIndex = format.indexOf('d', startIndex);
			}

			if (finishIndex < 0) {
				finishIndex = format.indexOf('i', startIndex);
			}

			if (finishIndex < 0) {
				finishIndex = format.indexOf('e', startIndex);
				exp = true;
			}

			if (finishIndex < 0) {
				finishIndex = format.indexOf('E', startIndex);
				exp = true;
			}

			if (finishIndex < 0) {
				throw new IllegalArgumentException(
				    "Unsuported or invalid format string '" + format + "'!");
			}

			int dotIndex = format.indexOf('.', startIndex);

			if (dotIndex == -1 || dotIndex > finishIndex) {
				dotIndex = finishIndex;
			}

			int lengthDecimal = dotIndex + 1 >= finishIndex ? 0
				: Integer.parseInt(format.substring(dotIndex + 1, finishIndex));
			int lengthAll = startIndex + 1 == dotIndex ? lengthDecimal + 1
				: Integer.parseInt(format.substring(startIndex + 1, dotIndex));
			int lengthInteger = Math.max(1, lengthAll - lengthDecimal);

			if (signTag) {
				retVal += "+";
			}

			for (int i = 0; i < lengthInteger; i++) {
				retVal += "#";
			}

			if (dotIndex < finishIndex) {
				retVal += ".";

				for (int i = 0; i < lengthDecimal; i++) {
					retVal += "#";
				}
			}

			if (exp) {
				retVal += "E";

				if (finishIndex + 1 == format.length()) {
					retVal += "#";
				} else {
					if (format.charAt(finishIndex + 1) == '+') {
						retVal += "+";
						finishIndex++;
					}

					finishIndex++;

					int num = 0;

					while (finishIndex + num < format.length()
					    && Character.isDigit(format.charAt(finishIndex + num))) {
						num++;
					}

					int val = Integer.parseInt(format.substring(finishIndex,
						        finishIndex + num));

					for (int i = 0; i < val; i++) {
						retVal += "#";
					}
				}
			}
		}

		return retVal;
	}

	/**
	 * Sets the maximum allowed number of digits to represent the value.
	 *
	 * @param i
	 */
	public void setMaximumDigits(int i)
	{
		maximumDigits = i;
	}

	/**
	 * Sets the maximum allowed number of digits to represent the value.
	 *
	 * @return number of allowed digits.
	 */
	public int getMaximumDigits()
	{
		return maximumDigits;
	}

	/**
	 * Sets the unit.
	 *
	 * @param unit The unit to set
	 */
	public void setUnit(String unit)
	{
		this.unit = unit;
	}

	/**
	 * Returns the unit.
	 *
	 * @return String
	 */
	public String getUnit()
	{
		return unit;
	}

	/*
	 * A format string generator used when no format string is set
	 * to the wheelswitch. The format string is determined from value bounds
	 * so that all possible values in range can be displayed with the same number
	 * and position of digits.
	 */
	protected String generateFormat()
	{
		if (generatedFormatString == null) {
			if (maximum == Double.POSITIVE_INFINITY
			    || minimum == Double.NEGATIVE_INFINITY) {
				generatedFormatString = "+#.##E+###";
			} else {
				double abs = Math.max(Math.abs(minimum), Math.abs(maximum));
				int integer = 0;

				while (Math.floor(abs) > 0) {
					abs /= 10;
					integer++;
				}

				integer = Math.max(integer, 1);

				abs = Math.min(Math.abs(minimum), Math.abs(maximum));

				int decimal = 0;

				while (Math.floor(abs) == 0 && abs != 0) {
					decimal++;
					abs *= 10;
				}

				generatedFormatString = new String();

				if (minimum < 0) {
					generatedFormatString += "+";
				}

				for (int i = 0; i < integer; i++) {
					generatedFormatString += "#";
				}

				if (decimal > 0) {
					generatedFormatString += ".";
				}

				for (int i = 0; i < decimal; i++) {
					generatedFormatString += "#";
				}
			}
		}

		return generatedFormatString;
	}
}

/* __oOo__ */
