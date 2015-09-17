package xal.app.emittanceanalysis.rawdata;

import java.awt.*;

import xal.extension.widgets.plot.*;

/**
 *  This class implements ColorGenerator interface and provides scaling for the
 *  RainbowColorGenerator instance.
 *
 *@author     A. Shishlo
 *@version    1.0
 */

public class LocalColorGenerator implements ColorGenerator {

    private RainbowColorGenerator rainbowC = RainbowColorGenerator.getColorGenerator();

    private double upperLim = 0.99999;


    /**
     *  Constructor of LocalColorGenerator class.
     */
    public LocalColorGenerator() { }


    /**
     *  Sets the scale factor.
     *
     *@param  upperLim  The new upper limit value
     */
    public void setUpperLimit(double upperLim) {
        if (upperLim < 0.00001) {
            upperLim = 0.00001;
        }
        if (upperLim > 0.99999) {
            upperLim = 0.99999;
        }
        this.upperLim = upperLim;
    }


    /**
     *  Implementation of the ColorGenerator interface. Returns the Color
     *  corresponding the input value. The value should be more than 0. and less
     *  than 1.0
     *
     *@param  value  The input value
     *@return        The color for this input value
     */

    public Color getColor(float value) {
        return rainbowC.getColor(Math.min(value / upperLim, 1.0f));
    }


    /**
     *  Implementation of the ColorGenerator interface. Returns the Color
     *  corresponding the input value. The value should be more than 0. and less
     *  than 1.0
     *
     *@param  value  The input value
     *@return        The color for this input value
     */

    public Color getColor(double value) {
        return rainbowC.getColor(Math.min(value / upperLim, 1.0));
    }
}


