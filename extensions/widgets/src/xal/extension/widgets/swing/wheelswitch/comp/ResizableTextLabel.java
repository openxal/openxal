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

import xal.extension.widgets.swing.wheelswitch.util.FontHelper;
import xal.extension.widgets.swing.wheelswitch.util.PaintHelper;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.Icon;
import javax.swing.JLabel;


/**
 * A text area in which the text can dynamicaly adjust its font size to fill
 * the whole area available.
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see javax.swing.JLabel
 */
public class ResizableTextLabel extends JLabel
{
	private static final long serialVersionUID = 1L;
	
	private boolean resizable;
	private boolean enhanced;
	private int columns;
	private Dimension preferredSize;
	private Dimension minimumSize;
	private Font userFont;

	/**
	 * Helper class that notifies the resizable text label to  resize its font
	 * when itself is being resized.
	 *
	 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
	 * @version $id$
	 *
	 * @see java.awt.event.ComponentAdapter
	 */
	protected class ResizableAdapter extends ComponentAdapter
	{
		/**
		 * Invoked when the label is being resized.
		 *
		 * @param e ComponentEvent
		 *
		 * @see java.awt.event.ComponentListener#componentResized(ComponentEvent)
		 */
        @Override
		public void componentResized(ComponentEvent e)
		{
			if (resizable) {
				resize();
			}
		}
	}

	/**
	 * Creates a resizable text label with text, icon and  predefined
	 * horizontal text aligment and resizable  font setting.
	 *
	 * @param text java.lang.String text to be displayed in  the label.
	 * @param icon Icon image to be displayed in the label.
	 * @param horizontalAlignment int horizontal aligment of the text in the
	 *        label.
	 */
	public ResizableTextLabel(String text, Icon icon, int horizontalAlignment)
	{
		super(text, icon, horizontalAlignment);
		addComponentListener(new ResizableAdapter());
		setFont(FontHelper.getDefaultFont());
	}

	/**
	 * Creates a resizable text label with text and predefined  horizontal text
	 * aligment.
	 *
	 * @param arg0 java.lang.String text to be displayed in  the label.
	 * @param arg1 int horizontal aligment of the text in the  label.
	 */
	public ResizableTextLabel(String arg0, int arg1)
	{
		this(arg0, null, arg1);
	}

	/**
	 * Creates a resizable text label with text.
	 *
	 * @param arg0 java.lang.String text to be displayed in  the label.
	 */
	public ResizableTextLabel(String arg0)
	{
		this(arg0, null, JLabel.LEADING);
	}

	/**
	 * Creates a resizable text label with icon and  predefined horizontal text
	 * aligment and resizable  font setting.
	 *
	 * @param image Icon image to be displayed in the label.
	 * @param horizontalAlignment int horizontal aligment of the text in the
	 *        label.
	 */
	public ResizableTextLabel(Icon image, int horizontalAlignment)
	{
		this(null, image, horizontalAlignment);
	}

	/**
	 * Creates a resizable text label with an icon
	 *
	 * @param image Icon image to be displayed in the label.
	 */
	public ResizableTextLabel(Icon image)
	{
		this(null, image, JLabel.LEADING);
	}

	/**
	 * Creates an empty label.
	 */
	public ResizableTextLabel()
	{
		this(" ", null, JLabel.LEADING);
	}

	/**
	 * Sets the number of character columns to be displayed. This setting only
	 * has effect if resizable text font setting is enabled. Then the size of
	 * the font is adjusted to display the specified number of character
	 * columns.
	 *
	 * @param newColumns
	 */
	public void setColumns(int newColumns)
	{
		int oldColumns = columns;

		if (oldColumns == newColumns) {
			return;
		}

		columns = newColumns;
		firePropertyChange("columns", oldColumns, newColumns);

		if (resizable) {
			resize();
		}
	}

	/**
	 * Gets the number of character columns to be displayed.
	 *
	 * @return int
	 */
	public int getColumns()
	{
		return columns;
	}

	/**
	 * Returns the resizable text font setting.
	 *
	 * @return boolean
	 */
	public boolean isResizable()
	{
		return resizable;
	}

	/**
	 * Sets the resizable text font setting.
	 *
	 * @param newResizable
	 */
	public void setResizable(boolean newResizable)
	{
		boolean oldResizable = resizable;

		if (oldResizable == newResizable) {
			return;
		}

		resizable = newResizable;
		firePropertyChange("resizable", oldResizable, newResizable);

		if (resizable) {
			resize();
		} else {
			if (userFont != null) {
				super.setFont(userFont);
			} else {
				super.setFont(FontHelper.getDefaultFont());
			}
		}
	}

	/**
	 * Sets the enhanced mode setting. When true, the label is painted with
	 * anti-aliasing rendering hints.
	 *
	 * @param newEnhanced
	 */
	public void setEnhanced(boolean newEnhanced)
	{
		boolean oldEnhanced = enhanced;

		if (oldEnhanced == newEnhanced) {
			return;
		}

		enhanced = newEnhanced;
		firePropertyChange("enhanced", oldEnhanced, newEnhanced);
		repaint();
	}

	/**
	 * Returns the enhanced mode setting.
	 *
	 * @return boolean
	 */
	public boolean isEnhanced()
	{
		return enhanced;
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @param text to be displayed.
	 *
	 * @see javax.swing.JLabel#setText(String)
	 */
    @Override
	public void setText(String text)
	{
		if (text != null && text.length() < 1) {
			text = " ";
		}

		super.setText(text);

		if (resizable) {
			resize();
		}
	}

	/**
	 * Adjusts the size of the font to the size of the label.
	 */
	protected void resize()
	{
		assert (resizable);

		int newSize = getHeight() * 14 / 20;
		Font font = FontHelper.getFontWithSize(newSize, getFont());
		int length = getFontMetrics(font).stringWidth("m") * columns;

		if (columns == 0 && getText() != null) {
			length = getFontMetrics(font).stringWidth(getText());
		}

		if (length <= getWidth() * 0.9 || length == 0) {
			//Debug.out("label length="+length+", columns="+columns+", size="+newSize);
			super.setFont(font);
		} else {
			//Debug.out("shrinking resizable label");
			super.setFont(FontHelper.getFontWithSize(
			        (int)(newSize * getWidth() * 0.9 / length), font));
		}
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @see javax.swing.JComponent#addNotify()
	 */
    @Override
	public void addNotify()
	{
		super.addNotify();

		if (resizable) {
			resize();
		}
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @return Dimension preferred size of the label
	 *
	 * @see javax.swing.JLabel#getPreferredSize()
	 */
    @Override
	public Dimension getPreferredSize()
	{
		if (preferredSize != null) {
			return preferredSize;
		} else if (resizable) {

		Font font;

		if (userFont == null) {
			font = FontHelper.getDefaultFont();
		} else {
			font = userFont;
		}

		int height = font.getSize() * 20 / 13;
		int width = getFontMetrics(font).stringWidth("m") * 10 / 8 * columns;

		if (columns == 0 && getText() != null) {
			width = getFontMetrics(font).stringWidth(getText()) * 10 / 8;
		}

		if (width == 0) {
			width = 1;
		}

		return new Dimension(width, height);
		} else {
			return super.getPreferredSize();
		}
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @see javax.swing.JComponent#setPreferredSize(Dimension)
	 */
    @Override
	public void setPreferredSize(Dimension newPreferredSize)
	{
		preferredSize = newPreferredSize;
		super.setPreferredSize(newPreferredSize);
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @see java.awt.Component#getMinimumSize()
	 */
    @Override
	public Dimension getMinimumSize()
	{
		if (minimumSize != null) {
			return minimumSize;
		} else if (resizable) {
			return getPreferredSize();
		} else {
			return super.getMinimumSize();
		}
	}

	/**
	 * This method was overriden to implement font resizing.
	 *
	 * @see javax.swing.JComponent#setMinimumSize(Dimension)
	 */
    @Override
	public void setMinimumSize(Dimension newMinimumSize)
	{
		minimumSize = newMinimumSize;
		super.setMinimumSize(newMinimumSize);
	}

	/**
	 * This method hes been overriden to implement the feature  of enhanced
	 * anti-aliasing paint of the label.
	 *
	 * @see javax.swing.JComponent#paintComponent(Graphics)
	 */
    @Override
	protected void paintComponent(Graphics g)
	{
		if (enhanced) {
			((Graphics2D)g).addRenderingHints(PaintHelper.getAntialiasingHints());
		}

		super.paintComponent(g);
	}
}

/* __oOo__ */
