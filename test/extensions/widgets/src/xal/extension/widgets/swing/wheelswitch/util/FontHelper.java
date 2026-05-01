/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch.util;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.metal.MetalLookAndFeel;


/**
 * Helper class that provides simple access to Font UI resources
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see javax.swing.plaf.metal.MetalLookAndFeel
 * @see java.awt.Font
 */
public final class FontHelper {
    private static List<Font> fonts = new ArrayList<Font>();

    /**
     * Returns the default Font name
     *
     * @return String name
     * @see javax.swing.plaf.metal.MetalLookAndFeel#getControlTextFont()
     */
    public static String getDefaultFontName() {
        return MetalLookAndFeel.getControlTextFont().getName();
    }

    /**
     * Returns the default Font size
     *
     * @return int size
     * @see javax.swing.plaf.metal.MetalLookAndFeel#getControlTextFont()
     */
    public static int getDefaultFontSize() {
        return MetalLookAndFeel.getControlTextFont().getSize();
    }

    /**
     * Returns the default Font
     *
     * @return Font
     * @see javax.swing.plaf.metal.MetalLookAndFeel#getUserTextFont()
     */
    public static Font getDefaultFont() {
        return MetalLookAndFeel.getUserTextFont();
    }

    /**
     * Returns the specified Font with its style changed
     * to the specified style.
     *
     * @return Font
     * @param int style
     * @param Font inFont
     */
    public static Font getFontWithStyle(int style, Font inFont) {
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).getName().equals(inFont.getName()) &&
                    (fonts.get(i).getSize() == inFont.getSize()) &&
                    (fonts.get(i).getStyle() == style)) {
                return fonts.get(i);
            }
        }

        fonts.add(inFont.deriveFont(style));

        return fonts.get(fonts.size() - 1);
    }

    /**
     * Returns the specified Font with its name changed
     * to the specified name.
     *
     * @return Font
     * @param String name
     * @param Font inFont
     */
    public static Font getFontWithName(String name, Font inFont) {
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).getName().equals(name) &&
                    (fonts.get(i).getSize() == inFont.getSize()) &&
                    (fonts.get(i).getStyle() == inFont.getStyle())) {
                return fonts.get(i);
            }
        }

        fonts.add(new Font(name, inFont.getStyle(), inFont.getSize()));

        return fonts.get(fonts.size() - 1);
    }

    /**
     * Returns the specified Font with its size changed
     * to the specified size.
     *
     * @return Font
     * @param int size
     * @param Font inFont
     */
    public static Font getFontWithSize(int size, Font inFont) {
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).getName().equals(inFont.getName()) &&
                    (fonts.get(i).getSize() == size) &&
                    (fonts.get(i).getStyle() == inFont.getStyle())) {
                return fonts.get(i);
            }
        }

        fonts.add(inFont.deriveFont((float) size));

        return fonts.get(fonts.size() - 1);
    }

    /**
     * Returns a new Font with the specified attributes
     *
     * @return Font
     * @param String name
     * @param int style
     * @param int size
     */
    public static Font getFont(String name, int style, int size) {
        for (int i = 0; i < fonts.size(); i++) {
            if (fonts.get(i).getName().equals(name) &&
                    (fonts.get(i).getSize() == size) &&
                    (fonts.get(i).getStyle() == style)) {
                return fonts.get(i);
            }
        }

        fonts.add(new Font(name, style, size));

        return fonts.get(fonts.size() - 1);
    }
}
