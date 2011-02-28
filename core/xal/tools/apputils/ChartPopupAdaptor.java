/*
 * ChartPopupAdaptor.java
 *
 * Created on June 10, 2003, 2:42 PM
 */

package xal.tools.apputils;

import java.awt.Component;

/**
 * ChartPopupAdaptor is an interface of adaptors that wrap charts so that different
 * kinds of charts can be manipulated via a common interface that is appropriate for 
 * the SimpleChartPopupMenu and SimpleChartDialog. 
 *
 * @author  tap
 */
public interface ChartPopupAdaptor {
    /**
     * Get the chart component.
     * @return The chart as a component.
     */
    public Component getChartComponent();
    
    
    /**
     * Get the minimum value of x that is visible.
     * @return the minimum value of x that is visible
     */
    public double getMinXLimit();
    
    
    /**
     * Set the minimum value of x that is visible.
     * @param lowerLimit the minimum value of x that is visible
     */
    public void setMinXLimit(double lowerLimit);
    
    
    /**
     * Get the maximum value of x that is visible.
     * @return the maximum value of x that is visible
     */
    public double getMaxXLimit();
    
    
    /**
     * Set the maximum value of x that is visible.
     * @param upperLimit the maximum value of x that is visible
     */
    public void setMaxXLimit(double upperLimit);
    
    
    /**
     * Get the minimum value of y that is visible.
     * @return the minimum value of y that is visible
     */
    public double getMinYLimit();
    
    
    /**
     * Set the minimum value of y that is visible.
     * @param lowerLimit the minimum value of y that is visible
     */
    public void setMinYLimit(double lowerLimit);
    
    
    /**
     * Get the maximum value of y that is visible.
     * @return the maximum value of y that is visible
     */
    public double getMaxYLimit();
    
    
    /**
     * Set the maximum value of y that is visible.
     * @param upperLimit the maximum value of y that is visible
     */
    public void setMaxYLimit(double upperLimit);
    
    
    /**
     * Scale the x and y axes once so all points fit on the chart then keep 
     * the axes' scales fixed.
     */
    public void scaleXandY();
    
    
    /**
     * Get the state of x-axis auto-scaling
     * @return true if the x-axis has auto-scaling enabled; false if not
     */
    public boolean isXAutoScale();
    
    
    /**
     * Set the auto-scale state of the x-axis
     * @param state true to enable x-axis auto-scaling; false to disable auto-scaling
     */
    public void setXAutoScale(boolean state);
    
    
    /**
     * Get the state of y-axis auto-scaling
     * @return true if the y-axis has auto-scaling enabled; false if not
     */
    public boolean isYAutoScale();
    
    
    /**
     * Set the auto-scale state of the y-axis
     * @param state true to enable y-axis auto-scaling; false to disable auto-scaling
     */
    public void setYAutoScale(boolean state);
    
    
    /**
     * Get the number of minor ticks per major step on the x-axis.
     * @return the number of minor ticks
     */
    public int getXNumMinorTicks();
    
    
    /**
     * Set the number of minor ticks on the x-axis.
     * @param count number of minor ticks
     */
    public void setXNumMinorTicks(int count);
    
    
    /**
     * Get the number of major ticks on the x-axis.
     * @return the spacing per minor tick
     */
    public int getXNumMajorTicks();
    
    
    /**
     * Set the number of major ticks on the x-axis.
     * @param count the desired number of major ticks
     */
    public void setXNumMajorTicks(int count);
    
    
    /**
     * Get the number of minor ticks per major step on the y-axis.
     * @return the number of minor ticks
     */
    public int getYNumMinorTicks();
    
    
    /**
     * Set the number of minor ticks on the y-axis.
     * @param count the number of minor ticks
     */
    public void setYNumMinorTicks(int count);
    
    
    /**
     * Get the number of minor ticks on the y-axis.
     * @return the number of major ticks
     */
    public int getYNumMajorTicks();
    
    
    /**
     * Set the number of major ticks on the y-axis.
     * @param count the number of major ticks
     */
    public void setYNumMajorTicks(int count);
        
    
    /**
     * Get the visibility state of the x-axis grid.
     * @return true if the grid is visible
     */
    public boolean isXGridVisible();
    
    
    /**
     * Set the visibility of the x-axis grid.
     * @param visibility true to enable the grid; false to disable the grid
     */
    public void setXGridVisible(boolean visibility);
    
    
    /**
     * Get the visibility state of the y-axis grid.
     * @return true if the grid is visible
     */
    public boolean isYGridVisible();
    
    
    /**
     * Set the visibility of the y-axis grid.
     * @param visibility true to enable the grid; false to disable the grid
     */
    public void setYGridVisible(boolean visibility);
}
