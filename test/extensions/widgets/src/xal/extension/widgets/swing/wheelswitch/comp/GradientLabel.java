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

package xal.extension.widgets.swing.wheelswitch.comp;

import xal.extension.widgets.swing.wheelswitch.util.ColorHelper;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.Icon;


/**
 * A label that paints a gradient as a background. User can specify gradient
 * colors (one of them is the default label background) and direction (the
 * default is from the upper left to the upper right corner) relative to the
 * label's proportions.
 *
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 * @version $id$
 */
public class GradientLabel extends ResizableTextLabel
{
	private static final long serialVersionUID = 1L;
	
	private Color backgroundOther = ColorHelper.getControlShadow();
	private BufferedImage daBuffer = null;
	private double endX = 1;
	private double endY = 0;
	private boolean gradientEnabled = true;
	private double startX = 0.2f;
	private double startY = 0;

	/**
	 * Constructs an empty GradientLabel
	 */
	public GradientLabel()
	{
		super();
		setOpaque(true);
	}

	/**
	 * Constructs a GradientLabel with the specified icon.
	 *
	 * @param image
	 */
	public GradientLabel(Icon image)
	{
		super(image);
		setOpaque(true);
	}

	/**
	 * Constructor for GradientLabel.
	 *
	 * @param image
	 * @param horizontalAlignment
	 */
	public GradientLabel(Icon image, int horizontalAlignment)
	{
		super(image, horizontalAlignment);
		setOpaque(true);
	}

	/**
	 * Constructs a GradientLabel with the specified text.
	 *
	 * @param text
	 */
	public GradientLabel(String text)
	{
		super(text);
		setOpaque(true);
	}

	/**
	 * Constructor for GradientLabel.
	 *
	 * @param text
	 * @param icon
	 * @param horizontalAlignment
	 */
	public GradientLabel(String text, Icon icon, int horizontalAlignment)
	{
		super(text, icon, horizontalAlignment);
		setOpaque(true);
	}

	/**
	 * Constructor for GradientLabel.
	 *
	 * @param text
	 * @param horizontalAlignment
	 */
	public GradientLabel(String text, int horizontalAlignment)
	{
		super(text, horizontalAlignment);
		setOpaque(true);
	}

	private void clearBuffer()
	{
		synchronized (this) {
			daBuffer = null;
		}
	}

	/**
	 * Returns the backgroundStart, this is the color that is used for the
	 * start of label's gradient. The end color is that of the label's
	 * background.
	 *
	 * @return Color
	 */
	public Color getBackgroundStart()
	{
		return backgroundOther;
	}

	/**
	 * Returns whether the gradient background is enabled.
	 *
	 * @return boolean
	 */
	public boolean isGradientEnabled()
	{
		return gradientEnabled;
	}

	/**
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
    @Override
	protected void paintComponent(Graphics g)
	{
		boolean oldOpaque = isOpaque();

		if (oldOpaque && (isGradientEnabled())) {
			int w = getWidth();
			int h = getHeight();

			synchronized (this) {
				if ((daBuffer == null) || (daBuffer.getWidth() != w)
				    || (daBuffer.getHeight() != h)) {
					daBuffer = new BufferedImage(w, h,
						    BufferedImage.TYPE_3BYTE_BGR);

					Graphics2D g2d = daBuffer.createGraphics();
					g2d.setPaint(new GradientPaint((int)(startX * w),(int)(startY * h),
					        getBackgroundStart(), (int)(endX * w), (int)(endY * h),
					        getBackground()));
					g2d.fillRect(0, 0, w, h);
				}

				g.drawImage(daBuffer, 0, 0, this);
			}
		}

		if (isGradientEnabled()) {
			setOpaque(false);
		}

		super.paintComponent(g);
		super.setOpaque(oldOpaque);
	}

	/**
	 * Sets the background color. This color is used for the gradient's end
	 * point.
	 *
	 * @param c The color to be set
	 *
	 * @see java.awt.Component#setBackground(Color)
	 * @see #setBackgroundStart(Color)
	 */
    @Override
	public void setBackground(Color c)
	{
		if (!c.equals(getBackground())) {
			clearBuffer();
			super.setBackground(c);
		}
	}

	/**
	 * Sets the backgroundStart color.
	 *
	 * @param newBackgroundStart The backgroundStart to set
	 *
	 * @see #getBackgroundStart()
	 */
	public void setBackgroundStart(Color newBackgroundStart)
	{
		if (newBackgroundStart != null
		    && !newBackgroundStart.equals(backgroundOther)) {
			Color oldBackgroundStart = backgroundOther;
			backgroundOther = newBackgroundStart;
			clearBuffer();
			repaint();
			firePropertyChange("backgroundStart", oldBackgroundStart,
			    newBackgroundStart);
		}
	}

	/**
	 * Sets the gradientEnabled property.
	 *
	 * @param gradientEnabled The gradientEnabled to set
	 *
	 * @see #isGradientEnabled()
	 */
	public void setGradientEnabled(boolean gradientEnabled)
	{
		this.gradientEnabled = gradientEnabled;
		clearBuffer();
	}

	/**
	 * Sets the start and end point of the label's gradient, relative to the
	 * label's size. Value 0 is the left/top edge of the label. 1 is the
	 * right/bottom edge of the label.
	 *
	 * @param startX the x coordinate of the start of the gradient
	 * @param startY the y coordinate of the start of the gradient
	 * @param endX the x coordinate of the end of the gradient
	 * @param endY the y coordinate of the end of the gradient
	 */
	public void setGradientPoints(double startX, double startY, double endX,
	    double endY)
	{
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		clearBuffer();
	}
}

/* __oOo__ */
