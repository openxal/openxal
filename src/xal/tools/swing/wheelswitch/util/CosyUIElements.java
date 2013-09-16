/*
 * @@COPYRIGHT@@
 */
package com.cosylab.gui.components.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.plaf.metal.MetalBorders;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import com.cosylab.gui.components.SimpleButton;


/**
 * A class containing user interface and border inner classes,
 * used in rendering some of the Swing components
 *
 * @author <a href="mailto:miha.kadunc@cosylab.com">Miha Kadunc</a>
 * @version $id$
 */
public class CosyUIElements {
    public static final int BOTTOM = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int TOP = 0;
    private static Border panelBorder = null;
    private static Border containerBorder = null;
    private static Border flushBorder = null;
    //private static Border[] lineBorders = new Border[4];

    /**
     *
     */
    public static Border getPlainBorder(boolean raised) {
        if (raised) {
            if (panelBorder == null) {
                panelBorder = new PanelFlushBorder(true, true);
            }

            return panelBorder;
        } else {
            if (containerBorder == null) {
                containerBorder = new PanelFlushBorder(false, true);
            }

            return containerBorder;
        }
    }

    /**
     *
     */
    public static Border getSingleLineBorder(int location, Color color) {
        return new SingleLineBorder(location, color);
    }

    public static Border getFlushBorder() {
        if (flushBorder == null) {
            flushBorder = new MetalBorders.Flush3DBorder();
        }

        return flushBorder;
    }

    public static class ButtonOnRightLayout implements LayoutManager2 {
        private static String BUTTON = "Button";
		// notused private static String OTHER = "Other";
        private Dimension minSize = new Dimension(60, 20);
        private Dimension prefSize = new Dimension(2, 20);
        private Component button = null;
        private Component other = null;

        /**
         * @see java.awt.LayoutManager#addLayoutComponent(String, Component)
         */
        public void addLayoutComponent(String name, Component comp) {
            if (comp instanceof SimpleButton || comp instanceof JButton) {
                button = comp;
            } else {
                other = comp;
            }

            prefSize.width = prefSize.width + comp.getPreferredSize().width;
        }

        /**
         * @see java.awt.LayoutManager#layoutContainer(Container)
         */
        public void layoutContainer(Container parent) {
            int x = parent.getInsets().left;
            int y = parent.getInsets().top;
            int w = parent.getWidth() - x - parent.getInsets().right;
            int h = parent.getHeight() - y - parent.getInsets().bottom;

            if (button != null) {
                button.setBounds((x + w) - h, y, h, h);
                w = w - h;
            }

            if (other != null) {
                other.setBounds(x, y, w, h);
            }
        }

        /**
         * @see java.awt.LayoutManager#minimumLayoutSize(Container)
         */
        public Dimension minimumLayoutSize(Container parent) {
            minSize.width = 2;
            minSize.height = 20;

            if (button != null) {
                minSize.width = minSize.width + button.getMinimumSize().width;
            }

            if (other != null) {
                minSize.width = minSize.width + other.getMinimumSize().width;
                minSize.height = other.getMinimumSize().height;
            }

            return minSize;
        }

        /**
         * @see java.awt.LayoutManager#preferredLayoutSize(Container)
         */
        public Dimension preferredLayoutSize(Container parent) {
            prefSize.width = 0;
            prefSize.height = 20;

            if (button != null) {
                prefSize.width = prefSize.width +
                    button.getPreferredSize().width;
            }

            if (other != null) {
                prefSize.width = prefSize.width +
                    other.getPreferredSize().width;
                prefSize.height = other.getPreferredSize().height;
            }

            return prefSize;
        }

        /**
         * @see java.awt.LayoutManager#removeLayoutComponent(Component)
         */
        public void removeLayoutComponent(Component comp) {
            if (comp.equals(button)) {
                button = null;
            } else if (comp.equals(other)) {
                other = null;
            }
        }

        /**
         * @see java.awt.LayoutManager2#addLayoutComponent(java.awt.Component, java.lang.Object)
         */
        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints == BUTTON) {
                button = comp;
            } else {
                other = comp;
            }

            prefSize.width = prefSize.width + comp.getPreferredSize().width;
        }

        /**
         * @see java.awt.LayoutManager2#getLayoutAlignmentX(java.awt.Container)
         */
        public float getLayoutAlignmentX(Container target) {
            return 0;
        }

        /**
         * @see java.awt.LayoutManager2#getLayoutAlignmentY(java.awt.Container)
         */
        public float getLayoutAlignmentY(Container target) {
            return 0;
        }

        /**
         * @see java.awt.LayoutManager2#invalidateLayout(java.awt.Container)
         */
        public void invalidateLayout(Container target) {
        }

        /**
         * @see java.awt.LayoutManager2#maximumLayoutSize(java.awt.Container)
         */
        public Dimension maximumLayoutSize(Container target) {
            return prefSize;
        }
    }

    /**
     *
     *
     * @version @@VERSION@@
     * @author Miha Kadunc (miha.kadunc@cosylab.com)
     */
    public static class TwinOptimalLayout implements LayoutManager {
        private int horBorder;
        private int vertBorder;
        private int spaceBetween;
        private Component[] comps;

        public TwinOptimalLayout(int horBorder, int vertBorder, int spaceBetween) {
            this.horBorder = horBorder;
            this.vertBorder = vertBorder;
            this.spaceBetween = spaceBetween;
            comps = new Component[2];
        }

        /**
         * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
         */
        public void addLayoutComponent(String name, Component comp) {
            if (comps[0] == null) {
                comps[0] = comp;
            } else {
                comps[1] = comp;
            }
        }

        /**
         * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
         */
        public void layoutContainer(Container parent) {
            int w = parent.getWidth();
            int h = parent.getHeight();
            double ratio = (double) w / (double) h;

            if (ratio > 1) {
                int cw = (w - (2 * horBorder) - spaceBetween) / 2;

                if (comps[0] != null) {
                    comps[0].setBounds(horBorder, vertBorder, cw,
                        h - (2 * vertBorder));
                }

                if (comps[1] != null) {
                    comps[1].setBounds(w - cw - horBorder, vertBorder, cw,
                        h - (2 * vertBorder));
                }
            } else {
                int ch = (h - (2 * vertBorder) - spaceBetween) / 2;

                if (comps[0] != null) {
                    comps[0].setBounds(horBorder, vertBorder,
                        w - (2 * horBorder), ch);
                }

                if (comps[1] != null) {
                    comps[1].setBounds(horBorder, h - ch - vertBorder,
                        w - (2 * horBorder), ch);
                }
            }
        }

        /**
         * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
         */
        public Dimension minimumLayoutSize(Container parent) {
            return new Dimension(10, 12);
        }

        /**
         * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
         */
        public Dimension preferredLayoutSize(Container parent) {
            return new Dimension(30, 20);
        }

        /**
         * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
         */
        public void removeLayoutComponent(Component comp) {
            if (comps[0] == comp) {
                comps[0] = null;
            }

            if (comps[1] == comp) {
                comps[1] = null;
            }
        }
    }

    /**
     * A class implementing the user interface of a <code>JTabbedPane</code>.
     * It assumes that the tabs are situated at the top of the pane,
     * and that the components contained inside the tabbed pane have a
     * <code>PanelFlushBorder</code>.
     */
    public static class FlushTabbedPaneUI extends MetalTabbedPaneUI {
        private static final Insets ZERO_INSETS = new Insets(0, 0, 0, 0);

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getContentBorderInsets(int)
         */
        protected Insets getContentBorderInsets(int tabPlacement) {
            return ZERO_INSETS;
        }

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getTabAreaInsets(int)
         */
        protected Insets getTabAreaInsets(int tabPlacement) {
            return ZERO_INSETS;
        }

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#paintFocusIndicator(Graphics, int, Rectangle[], int, Rectangle, Rectangle, boolean)
         */
        protected void paintFocusIndicator(Graphics g, int tabPlacement,
            Rectangle[] rects, int tabIndex, Rectangle iconRect,
            Rectangle textRect, boolean isSelected) {
            if (tabPane.hasFocus() && isSelected) {
                Rectangle tabRect = rects[tabIndex];
                boolean lastInRun = (tabIndex == (tabPane.getTabCount() - 1));
                g.setColor(focus);
                g.translate(tabRect.x, tabRect.y);

                int right = tabRect.width - 2;

                if (lastInRun) {
                    right = right - 1;
                }

                int bottom = tabRect.height - 1;
                boolean leftToRight = true;

                switch (tabPlacement) {
                case RIGHT:
                    g.drawLine(right - 6, 2, right - 2, 6); // slant
                    g.drawLine(1, 2, right - 6, 2); // top
                    g.drawLine(right - 2, 6, right - 2, bottom); // right
                    g.drawLine(1, 2, 1, bottom); // left
                    g.drawLine(1, bottom, right - 2, bottom); // bottom

                    break;

                case BOTTOM:

                    if (leftToRight) {
                        g.drawLine(2, bottom - 6, 6, bottom - 2); // slant
                        g.drawLine(6, bottom - 2, right, bottom - 2); // bottom
                        g.drawLine(2, 0, 2, bottom - 6); // left
                        g.drawLine(2, 0, right, 0); // top
                        g.drawLine(right, 0, right, bottom - 2); // right
                    } else {
                        g.drawLine(right - 2, bottom - 6, right - 6, bottom -
                            2); // slant
                        g.drawLine(right - 2, 0, right - 2, bottom - 6); // right

                        if (lastInRun) {
                            // last tab in run
                            g.drawLine(2, bottom - 2, right - 6, bottom - 2); // bottom
                            g.drawLine(2, 0, right - 2, 0); // top
                            g.drawLine(2, 0, 2, bottom - 2); // left
                        } else {
                            g.drawLine(1, bottom - 2, right - 6, bottom - 2); // bottom
                            g.drawLine(1, 0, right - 2, 0); // top
                            g.drawLine(1, 0, 1, bottom - 2); // left
                        }
                    }

                    break;

                case LEFT:
                    g.drawLine(2, 2, 2, bottom - 1); // left
                    g.drawLine(2, 2, right, 2); // top
                    g.drawLine(right, 2, right, bottom - 1); // right
                    g.drawLine(2, bottom - 1, right, bottom - 1); // bottom

                    break;

                case TOP:default:

                    if (leftToRight) {
                        g.drawLine(2, 5, 5, 2); //slant
                        g.drawLine(2, 5, 2, bottom - 2); // left
                        g.drawLine(5, 2, right - 4, 2); // top
                        g.drawLine(right - 4, 2, right - 1, 5); //right slant
                        g.drawLine(right - 1, 5, right - 1, bottom - 2); // right
                        g.drawLine(2, bottom - 2, right - 1, bottom - 2); // bottom
                    } else {
                        g.drawLine(right - 2, 6, right - 6, 2); // slant
                        g.drawLine(right - 2, 6, right - 2, bottom - 1); // right

                        if (lastInRun) {
                            // last tab in run
                            g.drawLine(right - 6, 2, 2, 2); // top
                            g.drawLine(2, 2, 2, bottom - 1); // left
                            g.drawLine(right - 2, bottom - 1, 2, bottom - 1); // bottom
                        } else {
                            g.drawLine(right - 6, 2, 1, 2); // top
                            g.drawLine(1, 2, 1, bottom - 1); // left
                            g.drawLine(right - 2, bottom - 1, 1, bottom - 1); // bottom
                        }
                    }
                }

                g.translate(-tabRect.x, -tabRect.y);
            }
        }

        /**
         * @see javax.swing.plaf.metal.MetalTabbedPaneUI#paintTopTabBorder(int, Graphics, int, int, int, int, int, int, boolean)
         */
        protected void paintTopTabBorder(int tabIndex, Graphics g, int x,
            int y, int w, int h, int btm, int rght, boolean isSelected) {
            int currentRun = getRunForTab(tabPane.getTabCount(), tabIndex);
            // notused int lastIndex = lastTabInRun(tabPane.getTabCount(), currentRun);
            int firstIndex = tabRuns[currentRun];
            boolean leftToRight = true;
            int bottom = h - 1;
            int right = w - 1;

            g.translate(x, y);

            //
            // Paint Border
            //
            g.setColor(shadow);

            if (leftToRight) {
                //right slant
                g.drawLine(right - 4, 0, right, 4);
            }

            g.setColor(darkShadow);

            if (leftToRight) {
                /*      // Paint top
                          g.drawLine( 0, 0, right, 0);

                */

                // Paint right
                g.drawLine(right, 4, right, bottom);

                //Paint bottom
                g.drawLine(1, bottom, right - 1, bottom);

                /*      // Paint left
                          g.drawLine( 0, 1, 0, bottom);
                  */
            }

            //
            // Paint Highlight
            //
            g.setColor(isSelected ? selectHighlight : highlight);

            if (leftToRight) {
                //Paint slant
                g.drawLine(0, 4, 4, 0);

                //Paint top     
                g.drawLine(4, 0, right - 4, 0);

                // Paint left
                g.drawLine(0, 5, 0, bottom);

                // paint highlight in the gap on tab behind this one
                // on the left end (where they all line up)
                if ((tabIndex == firstIndex) &&
                        (tabIndex != tabRuns[runCount - 1])) {
                    //  first tab in run but not first tab in last run
                    if (tabPane.getSelectedIndex() == tabRuns[currentRun + 1]) {
                        // tab in front of selected tab
                        g.setColor(selectHighlight);
                    } else {
                        // tab in front of normal tab
                        g.setColor(highlight);
                    }

                    g.drawLine(1, 0, 1, 4);
                }
            }

            g.translate(-x, -y);
        }

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#paintTabBackground(Graphics, int, int, int, int, int, int, boolean)
         */
        protected void paintTabBackground(Graphics g, int tabPlacement,
            int tabIndex, int x, int y, int w, int h, boolean isSelected) {
				// notusedint slantWidth = h / 2;

            if (isSelected) {
                g.setColor(selectColor);
            } else {
                g.setColor(tabPane.getBackgroundAt(tabIndex));
            }

            switch (tabPlacement) {
            case LEFT:
                g.fillRect(x + 2, y + 1, w - 2, h - 1);

                break;

            case BOTTOM:
                g.fillRect(x + 2, y, w - 2, h - 4);
                g.fillRect(x + 5, (y + (h - 1)) - 3, w - 5, 3);

                break;

            case RIGHT:
                g.fillRect(x + 1, y + 1, w - 5, h - 1);
                g.fillRect((x + (w - 1)) - 3, y + 5, 3, h - 5);

                break;

            case TOP:default:
                g.fillRect(x + 1, y + 3, 2, h - 3); //left
                g.fillRect(x + 3, y + 1, w - 6, h - 1); //center
                g.fillRect(x + (w - 3), y + 3, 2, h - 3); //right
            }
        }
    }

    public static final class FillingGridLayout extends GridLayout {
        /**
         * Creates a grid layout with a default of one column per component,
         * in a single row.
         *
         * @since JDK1.1
         */
        public FillingGridLayout() {
            this(1, 0, 0, 0);
        }

        /**
         * Creates a grid layout with the specified number of rows and
         * columns. All components in the layout are given equal size.
         * <p>
         * One, but not both, of <code>rows</code> and <code>cols</code> can
         * be zero, which means that any number of objects can be placed in a
         * row or in a column.
         *
         * @param     rows   the rows, with the value zero meaning
         *                   any number of rows.
         * @param     cols   the columns, with the value zero meaning
         *                   any number of columns.
         */
        public FillingGridLayout(int rows, int cols) {
            this(rows, cols, 0, 0);
        }

        /**
         * Creates a grid layout with the specified number of rows and
         * columns. All components in the layout are given equal size.
         * <p>
         * In addition, the horizontal and vertical gaps are set to the
         * specified values. Horizontal gaps are placed at the left and
         * right edges, and between each of the columns. Vertical gaps are
         * placed at the top and bottom edges, and between each of the rows.
         * <p>
         * One, but not both, of <code>rows</code> and <code>cols</code> can
         * be zero, which means that any number of objects can be placed in a
         * row or in a column.
         * <p>
         * All <code>GridLayout</code> constructors defer to this one.
         *
         * @param     rows   the rows, with the value zero meaning
         *                   any number of rows
         * @param     cols   the columns, with the value zero meaning
         *                   any number of columns
         * @param     hgap   the horizontal gap
         * @param     vgap   the vertical gap
         * @exception   IllegalArgumentException  if the value of both
         *      <code>rows</code> and <code>cols</code> is
         *      set to zero
         */
        public FillingGridLayout(int rows, int cols, int hgap, int vgap) {
            super(rows, cols, hgap, vgap);
        }

        /**
         * Lays out the specified container using this layout.
         * <p>
         * This method reshapes the components in the specified target
         * container in order to satisfy the constraints of the
         * <code>GridLayout</code> object.
         * <p>
         * The grid layout manager determines the size of individual
         * components by dividing the free space in the container into
         * equal-sized portions according to the number of rows and columns
         * in the layout. The container's free space equals the container's
         * size minus any insets and any specified horizontal or vertical
         * gap. All components in a grid layout are given the same size.
         *
         * @param      target   the container in which to do the layout
         * @see        java.awt.Container
         * @see        java.awt.Container#doLayout
         */
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                int ncomponents = parent.getComponentCount();
                int nrows = getRows();
                int ncols = getColumns();
                boolean ltr = parent.getComponentOrientation().isLeftToRight();

                if (ncomponents == 0) {
                    return;
                }

                if (nrows > 0) {
                    ncols = ((ncomponents + nrows) - 1) / nrows;
                } else {
                    nrows = ((ncomponents + ncols) - 1) / ncols;
                }

                double w = parent.getWidth() - (insets.left + insets.right);
                double h = parent.getHeight() - (insets.top + insets.bottom);
                w = (w - (((double) ncols - 1) * (double) getHgap())) / (double) ncols;
                h = (h - (((double) nrows - 1) * (double) getVgap())) / (double) nrows;

                if (ltr) {
                    int oldx = insets.left;
                    double x = oldx;
                    int oldy;
                    double y;

                    for (int c = 0; c < ncols; c++) {
                        x = x + w + (double) getVgap();
                        oldy = insets.top;
                        y = oldy;

                        for (int r = 0; r < nrows; r++) {
                            int i = (r * ncols) + c;
                            y = y + h + (double) getVgap();

                            if (i < ncomponents) {
                                parent.getComponent(i).setBounds((int) oldx,
                                    (int) oldy, (int) (x - oldx),
                                    (int) (y - oldy));
                            }

                            oldy = oldy + (int) (y - oldy);
                        }

                        oldx = oldx + (int) (x - oldx);
                    }
                } else {
                    double x = parent.getWidth() - insets.right - w;
                    double y;

                    for (int c = 0; c < ncols; c++, x -= (w + getHgap())) {
                        y = insets.top;

                        for (int r = 0; r < nrows; r++, y += (h + getVgap())) {
                            int i = (r * ncols) + c;

                            if (i < ncomponents) {
                                parent.getComponent(i).setBounds((int) x,
                                    (int) y, (int) w, (int) h);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A class implementing the user interface of a <code>JTabbedPane</code>.
     * It assumes that the tabs are situated at the top of the pane,
     * and that the components contained inside the tabbed pane have no
     * border.
     */
    public static final class PlainTabbedPaneUI extends FlushTabbedPaneUI {
        private static final Insets CONTENT_INSETS = new Insets(1, 1, 1, 1);

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#getContentBorderInsets(int)
         */
        protected Insets getContentBorderInsets(int tabPlacement) {
            return CONTENT_INSETS;
        }

        /**
         * @see javax.swing.plaf.metal.MetalTabbedPaneUI#paintTopTabBorder(int, Graphics, int, int, int, int, int, int, boolean)
         */
        protected void paintTopTabBorder(int tabIndex, Graphics g, int x,
            int y, int w, int h, int btm, int rght, boolean isSelected) {
            super.paintTopTabBorder(tabIndex, g, x, y, w, h, btm, rght,
                isSelected);

            int bottom = h - 1;
            int right = w - 1;

            g.translate(x, y);

            if (isSelected) {
                g.setColor(selectColor);
            } else {
                g.setColor(tabPane.getBackgroundAt(tabIndex));
            }

            //Paint bottom
            g.drawLine(1, bottom, right - 1, bottom);
            g.translate(-x, -y);
        }

        /**
         * @see javax.swing.plaf.basic.BasicTabbedPaneUI#paintContentBorder(Graphics, int, int)
         */
        protected void paintContentBorder(Graphics g, int tabPlacement,
            int selectedIndex) {
            super.paintContentBorder(g, tabPlacement, selectedIndex);

            Rectangle tab = new Rectangle();
            getTabBounds(selectedIndex, tab);
            PanelFlushBorder.paintBorder(g, 0, tab.y + tab.height,
                g.getClipBounds().width,
                g.getClipBounds().height - tab.height - tab.y);
            g.setColor(selectColor);
            g.drawLine(tab.x + 1, tab.y + tab.height, (tab.x + tab.width) - 2,
                tab.y + tab.height);
            g.setColor(shadow);
            g.drawLine((tab.x + tab.width) - 1, tab.y + tab.height,
                (tab.x + tab.width) - 1, tab.y + tab.height);
        }
    }

    /**
     * A clean border that visually raises the component above its surroundings.
     * Should be used in combination with ContainerFlushBorder.
     *
     * @author  Miha Kadunc
     * @version @@VERSION@@
     */
    public static class PanelFlushBorder implements Border {
        private final Insets insets = new Insets(1, 1, 1, 1);
        private final Insets cloneInsets = new Insets(1, 1, 1, 1);
        private boolean lockInsets = false;
        private boolean raised = true;

        public PanelFlushBorder() {
            this(true);
        }

        public PanelFlushBorder(boolean isRaised) {
            this(isRaised, false);
        }

        private PanelFlushBorder(boolean isRaised, boolean lockInsets) {
            this.lockInsets = lockInsets;
            raised = isRaised;
        }

        /**
         * @see javax.swing.border.Border#paintBorder(Component, Graphics, int, int, int, int)
         */
        public void paintBorder(Component c, Graphics g, int x, int y,
            int width, int height) {
            if (raised) {
                paintBorder(g, x, y, width, height);
            } else {
                paintBorder(g, (x + width) - 1, (y + height) - 1, 2 - width,
                    2 - height);
            }
        }

        public static void paintBorder(Graphics g, int x, int y, int width,
            int height) {
        	((Graphics2D)g).setStroke(new BasicStroke());
            g.setColor(ColorHelper.getControlShadow());
            g.drawLine((x + width) - 1, y, (x + width) - 1, (y + height) - 1);
            g.drawLine(x, (y + height) - 1, (x + width) - 1, (y + height) - 1);

            g.setColor(ColorHelper.getControlLightHighlight());
            g.drawLine(x, y, x, (y + height) - 1);
            g.drawLine(x, y, (x + width) - 1, y);

            g.setColor(ColorHelper.getControl());
            g.drawLine((x + width) - 1, y, (x + width) - 1, y);
            g.drawLine(x, (y + height) - 1, x, (y + height) - 1);
        }

        /**
         * @see javax.swing.border.Border#getBorderInsets(Component)
         */
        public Insets getBorderInsets(Component c) {
            if (!lockInsets) {
                return insets;
            } else {
                cloneInsets.top = insets.top;
                cloneInsets.bottom = insets.bottom;
                cloneInsets.left = insets.left;
                cloneInsets.right = insets.right;

                return cloneInsets;
            }
        }

        /**
         * @see javax.swing.border.Border#isBorderOpaque()
         */
        public boolean isBorderOpaque() {
            return true;
        }
    }

    /**
     * @author  Jernej Kamenik
     */
    private static final class SingleLineBorder implements Border {
        private final Insets insets = new Insets(0, 0, 0, 0);
        private Color color;
        private int orientation;

        public SingleLineBorder(int orientation, Color color) {
            this.orientation = orientation;
            this.color = color;

            switch (orientation) {
            case TOP:
                insets.top = 1;

                break;

            case BOTTOM:
                insets.bottom = 1;

                break;

            case LEFT:
                insets.left = 1;

                break;

            case RIGHT:
                insets.right = 1;

                break;
            }
        }

        /**
         * @see javax.swing.border.Border#paintBorder(Component, Graphics, int, int, int, int)
         */
        public void paintBorder(Component c, Graphics g, int x, int y,
            int width, int height) {
            g.setColor(color);

            switch (orientation) {
            case TOP:
                g.drawLine(x, y, (x + width) - 1, y);

                break;

            case BOTTOM:
                g.drawLine(x, (y + height) - 1, (x + width) - 1,
                    (y + height) - 1);

                break;

            case LEFT:
                g.drawLine(x, y, x, (y + height) - 1);

                break;

            case RIGHT:
                g.drawLine((x + width) - 1, y, (x + width) - 1, (y + height) -
                    1);

                break;
            }
        }

        /**
         * @see javax.swing.border.Border#getBorderInsets(Component)
         */
        public Insets getBorderInsets(Component c) {
            return insets;
        }

        /**
         * @see javax.swing.border.Border#isBorderOpaque()
         */
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
