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

import xal.extension.widgets.swing.wheelswitch.util.PaintHelper;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

/**
 * A simple resizable button identified by an arrow icon.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see xal.extension.widgets.swing.wheelswitch.comp.SimpleButton
 */
public class ArrowButton extends SimpleButton
{
	private static final long serialVersionUID = 1L;

	public static enum Orientation {
        UP, DOWN, LEFT, RIGHT;
    }
	
	private Insets insets;
	private Orientation orientation;

	/**
	 * Constructor for ArrowButton. Sets the orientation of the arrow icon.
	 *
	 * @param newOrientation int
	 */
	public ArrowButton(Orientation newOrientation)
	{
		super();

        orientation = newOrientation;
		setMinimumSize(new Dimension(10, 10));
		setPreferredSize(new Dimension(20, 20));
		insets = null;
		setActionMode(CHAIN_ACTION_MODE);
	}

	/**
	 * Sets the insets of the arrow icon.
	 *
	 * @param newInsets Insets
	 */
	public void setArrowInsets(Insets newInsets)
	{
		Insets oldInsets = insets;
		insets = newInsets;
		firePropertyChange("arrowInsets", oldInsets, newInsets);

		if (insets != oldInsets) {
			repaint();
		}
	}

	/**
	 * Returns the insets of the arrow icon.
	 *
	 * @return Insets
	 */
	public Insets getArrowInsets()
	{
		return insets;
	}

	/**
	 * This method has been overriden to implement arrow icon painting.
	 *
	 * @param g Graphics
	 */
    @Override
	public void paintComponent(Graphics g)
	{
		((Graphics2D)g).addRenderingHints(PaintHelper.getAntialiasingHints());
		super.paintComponent(g);

		int maxHeight = getBounds().height;
		int maxWidth = getBounds().width;
		int top;
		int bottom;
		int left;
		int right;

		if (insets == null) {
			top = Math.min(maxWidth, maxHeight) / 4;
			bottom = Math.min(maxWidth, maxHeight) / 4;
			left = Math.min(maxWidth, maxHeight) / 4;
			right = Math.min(maxWidth, maxHeight) / 4;
		} else {
			top = insets.top;
			bottom = insets.bottom;
			left = insets.left;
			right = insets.right;
		}

		g.setColor(getForeground());

		if (orientation == Orientation.DOWN) {
			int[] xs = { left, maxWidth / 2, maxWidth - right };
			int[] ys = { top, maxHeight - bottom, top };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == Orientation.UP) {
			int[] xs = { left, maxWidth / 2, maxWidth - right };
			int[] ys = { maxHeight - bottom, top, maxHeight - bottom };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == Orientation.RIGHT) {
			int[] ys = { top, maxHeight / 2, maxHeight - bottom };
			int[] xs = { left, maxWidth - right, left };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == Orientation.LEFT) {
			int[] ys = { top, maxHeight / 2, maxHeight - bottom };
			int[] xs = { maxWidth - right, left, maxWidth - right };
			g.fillPolygon(xs, ys, 3);
		}
	}

	/**
	 * Returns the orientation of the arrow icon.
	 *
	 * @return int
	 */
	public int getOrientation()
	{
		return orientation.ordinal();
	}

	/**
	 * Sets the orientation of the arrow icon.
	 *
	 * @param newOrientation int
	 */
	public void setOrientation(int newOrientation)
	{
		int oldOrientation = orientation.ordinal();
		orientation = Orientation.values()[newOrientation];
		firePropertyChange("orientation", oldOrientation, newOrientation);

		if (oldOrientation != newOrientation) {
			repaint();
		}
	}
}

/* __oOo__ */
