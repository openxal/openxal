/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Descedant of <code>Digit</code> displaying a integer value digit.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class ValueDigit extends Digit
{
	private static final long serialVersionUID = 1L;
	
	public static int INCREASE_VALUE = -1;
	public static int DECREASE_VALUE = -2;

	private int value;
	private int oldValue;

	/**
	 * Constructor for ValueDigit.
	 *
	 * @param newValue accepts 0 to 9.
	 */
	public ValueDigit(int newValue) throws IllegalArgumentException {
		super();
		if (newValue>=0 && newValue<=9) {
			value=newValue;
			setText(String.valueOf(value));
		} else throw new IllegalArgumentException();
	}

	/**
	 * Sets the value displayed.
	 *
	 * @param newValue INCREASE_VALUE increases the value,
	 * DECREASE_VALUE decreases the value, else the number newValue is set.
	 */
	public void setValue(int newValue) throws IllegalArgumentException {
		if (newValue>=0 && newValue<=9) {
			oldValue=value;
			value=newValue;
		} else if (newValue==INCREASE_VALUE) {
			if (value==9) value=0;
			else value++;
		} else if (newValue==DECREASE_VALUE) {
			if (value==0) value=9;
			else value--;
		} else throw new IllegalArgumentException();
		setText(String.valueOf(value));
	}

	/**
	 * Gets the value displayed.
	 *
	 * @return int
	 */
	public int getValue() {
		return value;
	}


	/**
	 * This method was overriden to implement animated number digit
	 * scrolling.
	 */
    @Override
	protected void paintDigitTransition(BufferedImage oldImage,BufferedImage newImage, Graphics g, float parameter) {
		Graphics2D g2D = (Graphics2D)g;
		if ((value>oldValue && (value!=9 || oldValue!=0)) || (value==0 && oldValue==9)) {
			g2D.translate(0,(int)(getHeight()*(parameter-1.)));
			if (newImage!=null) g2D.drawImage(newImage,null,0,0);
			g2D.translate(0,getHeight());
			if (oldImage!=null) g2D.drawImage(oldImage,null,0,0);
      		g2D.translate(0,-(int)(getHeight()*(parameter-1.))-getHeight());
		} else {
			g2D.translate(0,(int)(getHeight()*(1.-parameter)));
			if (newImage!=null) g2D.drawImage(newImage,null,0,0);
			g2D.translate(0,-getHeight());
			if (oldImage!=null) g2D.drawImage(oldImage,null,0,0);
      		g2D.translate(0,getHeight()-(int)(getHeight()*(1.-parameter)));
		}
	}


}
