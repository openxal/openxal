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

import xal.extension.widgets.swing.wheelswitch.comp.SimpleButton;
import xal.extension.widgets.swing.wheelswitch.util.ColorHelper;
import xal.extension.widgets.swing.wheelswitch.util.PaintHelper;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.image.BufferedImage;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.border.Border;
import javax.swing.border.LineBorder;


/**
 * An extension of <code>SimpleButton</code> displaying one digit (character).
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public abstract class Digit extends SimpleButton
{
	private static final long serialVersionUID = 1L;

	/*
	 * Used for the animation of the switching of the displayed symbols
	 * on the digit.
	 */
	private class AnimationTask extends TimerTask
	{
		/**
		 * @see java.util.TimerTask#run()
		 */
        @Override
		public void run()
		{
			repaint();

			if (animationCompleted >= 1f) {
				cancel();
			}

			animationCompleted += 0.1f;
		}
	}

	private static Timer animationTimer = null;
	private static HashMap<Dimension,Map<String,BufferedImage>> images = null;
	private static HashMap<Dimension,BufferedImage[]> backgroundImages = null;
	private static Border enhancedPressedBorder = new LineBorder(ColorHelper
		    .getCosyControlShadow());
	private static Border pressedBorder = new LineBorder(ColorHelper
		    .getCosyControlShadow());
	private static Border enhancedSelectedBorder = new LineBorder(ColorHelper
		    .getCosyControlShadow());
	private static Border selectedBorder = new LineBorder(ColorHelper
		    .getCosyControlShadow());
	private static Border enhancedBorder = new LineBorder(ColorHelper
		    .getCosyControlDarkShadow());
	private static Border border = new LineBorder(ColorHelper.getControlShadow());
	private String newText = null;
	private String oldText = null;
	private boolean enhanced = false;
	private boolean sel = false;
	private boolean tilting = false;
	private float animationCompleted = 1.f;

	/**
	 * Constructor for Digit creates an empty Digit.
	 */
	public Digit()
	{
		super();

		if (images == null) {
			images = new HashMap<Dimension,Map<String,BufferedImage>>();
		}

		if (backgroundImages == null) {
			backgroundImages = new HashMap<Dimension,BufferedImage[]>();
		}

		setResizable(true);
		setFocusable(false);
		setColumns(1);
		init();
	}

	/**
	 * Sets the enhancment mode of the <code>Digit</code>. When enhanced, the
	 * digit is painted using anti-aliasing rendering hints.
	 *
	 * @param newEnhanced
	 */
    @Override
	public void setEnhanced(boolean newEnhanced)
	{
		if (newEnhanced == enhanced) {
			return;
		}

		enhanced = newEnhanced;
		init();
		repaint();
	}

	/**
	 * Checks whether the <code>Digit</code> is enhanced.
	 *
	 * @return boolean
	 */
    @Override
	public boolean isEnhanced()
	{
		return enhanced;
	}

	/**
	 * Sets or removes the selection from the <code>Digit</code>.
	 *
	 * @param newSelected
	 */
	public void setSelected(boolean newSelected)
	{
		if (sel == newSelected) {
			return;
		}

		sel = newSelected;

		//Debug.out(this+" selected="+newSelected);
		init();
		repaint();
	}

	/**
	 * Checks for selection of the <code>Digit</code>.
	 *
	 * @return true if the <code>Digit</code> is selected, false otherwise.
	 */
	public boolean isSelected()
	{
		return sel;
	}

	/**
	 * This method has been overriden to implement animated transitions between
	 * displayed text images. It sets the new text and starts the animation of
	 * the transition (if it is not already running).
	 *
	 * @see JLabel#setText(java.lang.String)
	 */
    @Override
	public synchronized void setText(String newText)
	{
		if (newText != null
		    && (newText.equals(getText()) || newText.length() != 1)) {
			return;
		}

		this.oldText = getText();
		this.newText = newText;
		super.setText(newText);

		if (enhanced) {
			if (animationCompleted >= 1f) {
				animationCompleted = 0f;

				if (animationTimer == null) {
					animationTimer = new Timer();
				}

				// TODO this is workaround for applet, has to be investigated
				try {
					animationTimer.schedule(new AnimationTask(), 0, 10);
				} catch (IllegalStateException e) {
					animationTimer = new Timer();
					animationTimer.schedule(new AnimationTask(), 0, 10);
				}
			}
		}
	}

	/**
	 * Sets the tilting.
	 *
	 * @param tilting The tilting to set
	 */
	public void setTilting(boolean tilting)
	{
		this.tilting = tilting;
	}

	/**
	 * Returns the tilting.
	 *
	 * @return boolean
	 */
	public boolean isTilting()
	{
		return tilting;
	}

	/**
	 * This method was overriden to implement enhanced anti-aliasing display
	 * features as well as animated transitions.
	 *
	 * @param g DOCUMENT ME!
	 */
    @Override
	public void paintComponent(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();
		Dimension size = getSize();

		if (enhanced) {
			Graphics2D g2D;
			BufferedImage image;
			Paint paint;
			boolean presel = (sel || isPressed());

			if (backgroundImages.get(size) == null) {
				backgroundImages.put(size, new BufferedImage[2]);
			}

			if ((backgroundImages.get(size)[0] == null && !presel)
			    || (backgroundImages.get(size)[1] == null && presel)) {
				image = new BufferedImage(width, height,
					    BufferedImage.TYPE_4BYTE_ABGR);
				g2D = image.createGraphics();

				if (presel) {
					paint = new GradientPaint(0f, 0f,
						    ColorHelper.getCosyControl(), (float)width,
						    (float)height, ColorHelper.getCosyControlShadow());
					g2D.setPaint(paint);
					g2D.fillRect(0, 0, width, height);
					g2D.setComposite(AlphaComposite.getInstance(
					        AlphaComposite.SRC_OVER, 0.5f));
					paint = new GradientPaint((float)width / 2f, 0f,
						    ColorHelper.getCosyControlShadow(),
						    (float)width / 2f, (float)height / 2f,
						    ColorHelper.getCosyControl());
					g2D.setPaint(paint);
					g2D.fillRect(0, 0, width, height / 2);
					paint = new GradientPaint((float)width / 2f,
						    (float)height / 2f, ColorHelper.getCosyControl(),
						    (float)width / 2f, (float)height,
						    ColorHelper.getCosyControlShadow());
					g2D.setPaint(paint);
					g2D.fillRect(0, height / 2, width, height);
					backgroundImages.get(getSize())[1] = image;
				} else {
					paint = new GradientPaint(0f, 0f,
						    ColorHelper.getCosyControlHighlight(),
						    (float)width, (float)height,
						    ColorHelper.getCosyControl());
					g2D.setPaint(paint);
					g2D.fillRect(0, 0, width, height);
					g2D.setComposite(AlphaComposite.getInstance(
					        AlphaComposite.SRC_OVER, 0.5f));
					paint = new GradientPaint((float)width / 2f, 0f,
						    ColorHelper.getCosyControl(), (float)width / 2f,
						    (float)height / 2f,
						    ColorHelper.getCosyControlHighlight());
					g2D.setPaint(paint);
					g2D.fillRect(0, 0, width, height / 2);
					paint = new GradientPaint((float)width / 2f,
						    (float)height / 2f,
						    ColorHelper.getCosyControlHighlight(),
						    (float)width / 2f, (float)height,
						    ColorHelper.getCosyControl());
					g2D.setPaint(paint);
					g2D.fillRect(0, height / 2, width, height);
					backgroundImages.get(size)[0] = image;
				}
			} else {
				if (presel) {
					image = backgroundImages.get(size)[1];
				} else {
					image = backgroundImages.get(size)[0];
				}
			}

			g2D = (Graphics2D)g;
			g2D.drawImage(image, null, 0, 0);
			g2D.addRenderingHints(PaintHelper.getAntialiasingHints());

			if (animationCompleted < 1.f) {
				if (images.get(size) == null) {
					images.put(size, new HashMap<String,BufferedImage>());
				}

				if(images.get(size).get(newText) == null) {
					image = new BufferedImage(width, height,
						    BufferedImage.TYPE_4BYTE_ABGR);

					Graphics2D gr = image.createGraphics();
					gr.addRenderingHints(PaintHelper.getAntialiasingHints());
					gr.setFont(getFont());
					super.paintComponent(gr);
					images.get(size).put(newText, image);
				}

				paintDigitTransition(images.get(getSize()).get(oldText),
						images.get(getSize()).get(newText),
						g2D, animationCompleted);
				super.paintBorder(g2D);
			} else {
				super.paintComponent(g2D);
			}
		} else {
			super.paintComponent(g);
		}

		if (tilting) {
			PaintHelper.paintRectangle(g, 0, 0, width - 1, height - 1,
			    ColorHelper.getEmergencyOutline(), 1);
		}
	}

	/**
	 * (Re)Initializes the <code>Digit</code>. Sets the border, background and
	 * foreground colors and opacity of the digit depending on the current
	 * selection state of the digit.
	 */
	private void init()
	{
		setHorizontalAlignment(0);

		if (enhanced) {
			setOpaque(false);
			setPressedForeground(ColorHelper.getText());
			setPressedBorder(enhancedPressedBorder);

			if (sel) {
				setForeground(ColorHelper.getText());
				setBorder(enhancedSelectedBorder);
			} else if (isEnabled()) {
				setForeground(ColorHelper.getControlText());
				setBorder(enhancedBorder);
			} else {
				setForeground(ColorHelper.getControlShadow());
				setBorder(enhancedBorder);
			}
		} else {
			setOpaque(true);
			setPressedBackground(ColorHelper.getTextHighlight());
			setPressedForeground(ColorHelper.getText());
			setPressedBorder(pressedBorder);

			if (sel) {
				setBackground(ColorHelper.getTextHighlight());
				setForeground(ColorHelper.getText());
				setBorder(selectedBorder);
			} else if (isEnabled()) {
				setBackground(ColorHelper.getCosyControlHighlight());
				setForeground(ColorHelper.getControlText());
				setBorder(border);
			} else {
				setBackground(ColorHelper.getCosyControl());
				setForeground(ColorHelper.getControlText());
				setBorder(border);
			}
		}
	}

	/**
	 * The method combines two images and paints them onto the selected
	 * <code>Graphics</code> object based on the value of parameter between 0
	 * and 1.f. When paramter equals 0, oldImage should be painted completely
	 * and newImage not at all, and opposite when parameter equals 1.f.
	 * Descedants of <code>Digit</code> should override this method to
	 * implement different types of image transitions.
	 *
	 * @param oldImage
	 * @param newImage
	 * @param g Graphics object on which to paint the two images.
	 * @param parameter float value between 0 and 1.f.
	 */
	protected void paintDigitTransition(BufferedImage oldImage,
	    BufferedImage newImage, Graphics g, float parameter)
	{
		super.paintComponent(g);

		Graphics2D g2D = (Graphics2D)g;
		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		        Math.abs(1.f - parameter)));

		if (oldImage != null) {
			g2D.drawImage(oldImage, null, 0, 0);
		}

		g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
		        parameter % 1));

		if (newImage != null) {
			g2D.drawImage(newImage, null, 0, 0);
		}
	}
}

/* __oOo__ */
