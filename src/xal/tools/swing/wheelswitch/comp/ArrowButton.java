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

import xal.tools.swing.wheelswitch.util.PaintHelper;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;


/**
 * A simple resizable button identified by an arrow icon.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see com.cosylab.gui.components.SimpleButton
 */
public class ArrowButton extends SimpleButton
{
	/** Arrow icon pointing upwards. */
	public static int UP = 0;

	/** Arrow icon pointing downwards. */
	public static int DOWN = 1;

	/** Arrow icon pointing to the left. */
	public static int LEFT = 2;

	/** Arrow icon pointing to the right. */
	public static int RIGHT = 3;
	private Insets insets;
	private int orientation;

	/**
	 * Constructor for ArrowButton. Sets the orientation of the arrow icon.
	 *
	 * @param newOrientation int
	 */
	public ArrowButton(int newOrientation)
	{
		super();

		if (newOrientation != UP && newOrientation != DOWN
		    && newOrientation != LEFT && newOrientation != RIGHT) {
			throw (new IllegalArgumentException());
		}

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

		if (orientation == DOWN) {
			int[] xs = { left, (int)(maxWidth / 2), maxWidth - right };
			int[] ys = { top, maxHeight - bottom, top };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == UP) {
			int[] xs = { left, (int)(maxWidth / 2), maxWidth - right };
			int[] ys = { maxHeight - bottom, top, maxHeight - bottom };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == RIGHT) {
			int[] ys = { top, (int)(maxHeight / 2), maxHeight - bottom };
			int[] xs = { left, maxWidth - right, left };
			g.fillPolygon(xs, ys, 3);
		} else if (orientation == LEFT) {
			int[] ys = { top, (int)(maxHeight / 2), maxHeight - bottom };
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
		return orientation;
	}

	/**
	 * Sets the orientation of the arrow icon.
	 *
	 * @param newOrientation int
	 */
	public void setOrientation(int newOrientation)
	{
		int oldOrientation = orientation;
		orientation = newOrientation;
		firePropertyChange("orientation", oldOrientation, newOrientation);

		if (oldOrientation != newOrientation) {
			repaint();
		}
	}

	/**
	 * Run test applet.
	 *
	 * @param args command line parameters
	 */
	public static void main(String[] args)
	{
		JApplet applet = new JApplet() {
				public void init()
				{
					Container cp = this.getContentPane();
					JPanel panel = new JPanel();
					panel.setLayout(new GridLayout(1, 0));
					panel.add(new ArrowButton(ArrowButton.UP));
					panel.add(new ArrowButton(ArrowButton.DOWN));
					panel.add(new ArrowButton(ArrowButton.LEFT));
					panel.add(new ArrowButton(ArrowButton.RIGHT));
					cp.add(panel);
				}
			};

		JFrame frame = new JFrame("Arrow Button Demo");
		frame.getContentPane().add(applet);
		frame.setSize(300, 400);
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e)
				{
					System.exit(0);
				}
			});
		applet.init();
		applet.start();
		frame.setVisible(true);
	}
}

/* __oOo__ */
