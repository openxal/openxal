package xal.extension.widgets.plot;

import java.awt.*;

/**
 * This interface defines the signatures of two methods to produce the color
 * in accordance with value between 0.0 and 1.0. 
 *
 * @version 1.0
 * @author  A. Shishlo
 */

public interface ColorGenerator{

    /** Returns the Color corresponding the input value.
     * The value should be more than 0. and less than 1.0 */
    public Color getColor(float value);


    /** Returns the Color corresponding the input value.
     * The value should be more than 0. and less than 1.0 */
    public Color getColor(double value);
}


