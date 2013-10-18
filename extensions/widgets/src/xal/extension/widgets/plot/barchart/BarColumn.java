package xal.extension.widgets.plot.barchart;

import java.awt.*;

import xal.extension.widgets.plot.*;

/**
 *  The bar coulumn interface for the Bar Chart Class
 *
 *@author     shishlo
 *created    October 10, 2005
 */

public interface BarColumn {

  /**
   *@return    Returns number of lines inside this bar
   */
  public int size();


  /**
   *  Returns true if user wants to see the line with this index
   *
   *@param  index  The index of the line inside the bar
   *@return        True (or false) if user (does not ) wants to see the line
   *      with this index
   */
  public boolean show(int index);


  /**
   *  Returns true if user wants to see the bar
   *
   *@return    True (or false) if user (does not ) wants to see the column
   */
  public boolean show();


  /**
   *  Returns the value for the line hight inside the bar
   *
   *@param  index  The index of the line inside the bar
   *@return        The value for the line hight inside the bar
   */
  public double value(int index);


  /**
   *  Returns the color for the line inside the bar
   *
   *@param  index  The index of the line inside the bar
   *@return        The value for the line hight inside the bar
   */
  public Color getColor(int index);

  /**
   *  Returns a marker for this bar
   *
   *@return    The string with marker
   */
  public String marker();

}

