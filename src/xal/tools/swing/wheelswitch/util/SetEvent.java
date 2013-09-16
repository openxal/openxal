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

package xal.tools.swing.wheelswitch;

import java.util.EventObject;


/**
 * Notifys listeners that user set action was performed on number displayer.
 *
 * @author Ales Pucelj
 * @version $$id$$
 */
public class SetEvent extends EventObject
{
	private final Number value;

	/**
	 * Constructor for SliderSetEvent.
	 *
	 * @param source Object
	 * @param newValue double
	 */
	public SetEvent(Object source, double newValue)
	{
		super(source);
		value = new Double(newValue);
	}

	/**
	 * Constructor for SliderSetEvent.
	 *
	 * @param source Object
	 * @param newValue Object
	 *
	 * @throws NullPointerException DOCUMENT ME!
	 */
	public SetEvent(Object source, Number newValue)
	{
		super(source);

		if (newValue == null) {
			throw new NullPointerException("newValue == null");
		}

		value = newValue;
	}

	/**
	 * Returns value which was set by user.
	 *
	 * @return Number
	 */
	public Number getValue()
	{
		return value;
	}

	/**
	 * Returns set value as double.
	 *
	 * @return double
	 */
	public double getDoubleValue()
	{
		return value.doubleValue();
	}

	/**
	 * Returns set value as long.
	 *
	 * @return double
	 */
	public long getLongValue()
	{
		return value.longValue();
	}
}

/* __oOo__ */
