package xal.extension.widgets.plot.barchart;

import java.awt.*;

/**
 *  Description of the Class
 *
 *@author     shishlo
 *created    October 10, 2005
 */
public class BarColumnColor {

  private int number = 10;
  private Color[] colors = null;


  /**
   *  Constructor for the BarColumnColor object
   */
  public BarColumnColor() {
    colors = new Color[number];
    colors[0] = new Color(0, 0, 255);
    colors[1] = new Color(0, 88, 255);
    colors[2] = new Color(121, 0, 255);
    colors[3] = new Color(255, 0, 243);
    colors[4] = new Color(255, 0, 79);
    colors[5] = new Color(255, 100, 0);
    colors[6] = new Color(11, 172, 9);
    colors[7] = new Color(246, 180, 13);
    colors[8] = new Color(160, 32, 240);
    colors[9] = new Color(0, 255, 0);

  }


  /**
   *  Returns color according to an integer index.
   *
   *@param  indIn  The color index
   *@return        The color value
   */
  public Color getColor(int indIn) {
    int ind = indIn % number;
    return colors[ind];
  }


  /**
   *  set the color of index i to your favorite color
   *
   *@param  index  The new color index
   *@param  color  The new color value
   */
  public void setColor(int index, Color color) {
    colors[index] = color;
  }


  /**
   *  Returns the number of colors
   *
   *@return    The size value
   */
  public int getSize() {
    return number;
  }

}

