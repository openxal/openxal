/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch.util;

import java.awt.Color;

import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;


/**
 * Helper class that provides simple access to most often
 * used Color UI resources
 *
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 *
 * @see javax.swing.plaf.metal.MetalLookAndFeel
 */
public final class ColorHelper {
    public final static String[] COLOR_NAMES = new String[] {
            "Alarm", "AlarmOutline", "Control", "ControlDarkShadow",
            "ControlHighlight", "ControlShadow", "ControlText", "CosyControl",
            "CosyControlDarkShadow", "CosyControlHighlight", "CosyControlShadow",
            "CosyControlText", "CosyErrorText", "CosyInputBackground",
            "CosyOverlay", "Error", "EmergencyOutline", "Emergency", "Focus",
            "Hyperlink", "Text", "TextHighlight", "TimeOut", "Warning",
            "WarningOutline", "WindowBackground"
        };
    public final static String[] JAVA_COLOR_NAMES = new String[] {
            "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY",
            "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW"
        };
    private final static Color COSY_CONTROL = new Color(206, 206, 227);
    private final static Color COSY_CONTROL_SHADOW = new Color(126, 126, 177);
    private final static Color COSY_CONTROL_HIGHLIGHT = new Color(246, 246, 255);
    private final static Color COSY_CONTROL_DARK_SHADOW = new Color(86, 86, 157);
    private final static Color HYPERLINK = new Color(0, 0, 255);
    private final static Color ALARM = new Color(255, 128, 128);
    private final static Color ALARM_OUTLINE = new Color(255, 0, 0);
    private final static Color EMERGENCY = new Color(255, 64, 64);
    private final static Color EMERGENCY_OUTLINE = new Color(255, 0, 0);
    private final static Color WARNING = new Color(250, 230, 6);
    private final static Color WARNING_OUTLINE = new Color(160, 128, 8);
    private final static Color TIMEOUT = new Color(0, 5, 212);
    private final static Color TIMEOUT_OUTLINE = new Color(0, 4, 170);

    /**
     * Returns the default control Color, which should be
    * used to render the background of controls used in
    * applications
    *
     * @return Control color
     */
    public static Color getControl() {
        Color ret = UIManager.getColor("control");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControl();
    }

    /**
     * Returns the default control shadow Color
     *
     * @return Color
     */
    public static Color getControlShadow() {
        Color ret = UIManager.getColor("controlShadow");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControlDarkShadow();
    }

    /**
     * Returns the default control dark shadow Color
     *
     * @return Color
     */
    public static Color getControlDarkShadow() {
        Color ret = UIManager.getColor("controlDkShadow");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControlDarkShadow();
    }

    /**
     * Returns the default control highlight Color
     *
     * @return Color
     */
    public static Color getControlHighlight() {
        Color ret = UIManager.getColor("controlHighlight");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControlHighlight();
    }

    /**
     * Returns the default control highlight Color
     *
     * @return Color
     */
    public static Color getControlLightHighlight() {
        Color ret = UIManager.getColor("controlLtHighlight");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControlHighlight();
    }

    /**
     * Returns the default control text Color
     *
     * @return Color
     */
    public static Color getControlText() {
        Color ret = UIManager.getColor("controlText");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getControlTextColor();
    }

    /**
     * Returns the default window background Color
     *
     * @return Color
     */
    public static Color getWindowBackground() {
        Color ret = UIManager.getColor("window");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getWindowBackground();
    }

    /**
     * Returns the default text Color
     *
     * @return Color
     */
    public static Color getText() {
        Color ret = UIManager.getColor("windowText");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getUserTextColor();
    }

    /**
     * Returns the default text highlight Color
     *
     * @return Color
     */
    public static Color getTextHighlight() {
        Color ret = UIManager.getColor("textHighlight");

        if (ret != null) {
            return ret;
        }

        return MetalLookAndFeel.getTextHighlightColor();
    }

	/**
	 * @return
	 */
	public static Color getTextHighlightText() {
		Color ret = UIManager.getColor("textHighlightText");

		if (ret != null) {
			return ret;
		}

		return MetalLookAndFeel.getUserTextColor();
	}    
    
    /**
     * Returns the default focus Color
     *
     * @return Color
     * @see javax.swing.plaf.metal.MetalLookAndFeel#getFocusColor()
     */
    public static Color getFocus() {
        //return UIManager.getColor("controlShadow");		
        return MetalLookAndFeel.getFocusColor();
    }

    /**
     * Returns the default CosyBeans control Color
     *
     * @return Color
     */
    public static Color getCosyControl() {
        return COSY_CONTROL;
    }

    /**
     * Returns the default CosyBeans control highlight Color
     *
     * @return Color
     */
    public static Color getCosyControlHighlight() {
        return COSY_CONTROL_HIGHLIGHT;
    }

    /**
     * Returns the default CosyBeans control shadow Color
     *
     * @return Color
     */
    public static Color getCosyControlShadow() {
        return COSY_CONTROL_SHADOW;
    }

    /**
     * Returns the default CosyBeans control dark shadow Color
     *
     * @return Color
     */
    public static Color getCosyControlDarkShadow() {
        return COSY_CONTROL_DARK_SHADOW;
    }

    /**
     * Returns the default CosyBenas control text Color
     *
     * @return Color
     */
    public static Color getCosyControlText() {
        return Color.BLACK;
    }

    /**
     * Returns the default CosyBeans input background Color
     *
     * @return Color
     */
    public static Color getCosyInputBackground() {
        return MetalLookAndFeel.getWindowBackground();
    }

    /**
     * Returns the default CosyBeans error text Color
     *
     * @return Color
     */
    public static Color getCosyErrorText() {
        return Color.RED;
    }

    /**
     * Returns the default CosyBeans overlay Color
     *
     * @return Color
     */
    public static Color getCosyOverlay() {
        return Color.GRAY;
    }

    /**
     * Returns the default CosyBeans timeout Color.
     *
     * @return Color
     */
    public static Color getTimeOut() {
        return TIMEOUT;
    }

    public static Color getTimeOutOutline() {
        return TIMEOUT_OUTLINE;
    }

    /**
     * Returns the default CosyBeans warning Color.
     *
     * @return Color
     */
    public static Color getWarning() {
        return WARNING;
    }

    /**
     * Returns the default CosyBeans warning outline Color.
     *
     * @return Color
     */
    public static Color getWarningOutline() {
        return WARNING_OUTLINE;
    }

    /**
     * Returns the default Color used to render errors.
     *
     * @return Color
     */
    public static Color getError() {
        return Color.RED;
    }

    /**
     * Returns the default CosyBeans alarm Color.
     *
     * @return Color that should be used to render alarm states
     */
    public static Color getAlarm() {
        return ALARM;
    }

    /**
     * Returns the default CosyBeans alarm outline Color.
     *
     * @return Color that should be used to render outline of alarm symbols
     */
    public static Color getAlarmOutline() {
        return ALARM_OUTLINE;
    }

    /**
     * Returns the default CosyBeans emergency Color.
     *
     * @return Color
     */
    public static Color getEmergency() {
        return EMERGENCY;
    }

    /**
     * Returns the default CosyBeans emergency outline Color.
     *
     * @return Color
     */
    public static Color getEmergencyOutline() {
        return EMERGENCY_OUTLINE;
    }

    public static Color getHyperlink() {
        return HYPERLINK;
    }

}
