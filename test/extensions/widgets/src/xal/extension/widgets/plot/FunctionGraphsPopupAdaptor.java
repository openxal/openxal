/*
 * FunctionGraphsPopupAdaptor.java
 *
 * Created on June 10, 2003, 3:11 PM
 */

package xal.extension.widgets.plot;

import xal.tools.apputils.*;

import java.awt.Component;


/**
 * FunctionGraphsPopupAdaptor implements the ChartPopupAdaptor on behalf of the 
 * FunctionGraphsPanel class.
 *
 * @author  tap
 */
public class FunctionGraphsPopupAdaptor implements ChartPopupAdaptor {
    protected FunctionGraphsJPanel chart;
    
    
    /** Creates a new instance of FunctionGraphsAdaptor */
    public FunctionGraphsPopupAdaptor(FunctionGraphsJPanel newChart) {
        chart = newChart;
    }
    
    
    /**
     * Get the chart component.
     * @return The chart as a component.
     */
    public Component getChartComponent() {
        return chart;
    }    
    
    
    /**
     * Get the minimum value of x that is visible.
     * @return the minimum value of x that is visible
     */
    public double getMinXLimit() {
        return ( isXAutoScale() ) ? chart.getInnerMinX() : chart.getCurrentMinX();
    }
    
    
    /**
     * Set the minimum value of x that is visible.
     * @param lowerLimit the minimum value of x that is visible
     */
    public void setMinXLimit(double lowerLimit) {
        chart.getCurrentGL().setXmin(lowerLimit);
    }
    
    
    /**
     * Get the maximum value of x that is visible.
     * @return the maximum value of x that is visible
     */
    public double getMaxXLimit() {
        return ( isXAutoScale() ) ? chart.getInnerMaxX() : chart.getCurrentMaxX();
    }
    
    
    /**
     * Set the maximum value of x that is visible.
     * @param upperLimit the maximum value of x that is visible
     */
    public void setMaxXLimit(double upperLimit) {
        chart.getCurrentGL().setXmax(upperLimit);
    }
    
    
    /**
     * Get the minimum value of y that is visible.
     * @return the minimum value of y that is visible
     */
    public double getMinYLimit() {
        return ( isYAutoScale() ) ? chart.getInnerMinY() : chart.getCurrentMinY();
    }
    
    
    /**
     * Set the minimum value of y that is visible.
     * @param lowerLimit the minimum value of y that is visible
     */
    public void setMinYLimit(double lowerLimit) {
        chart.getCurrentGL().setYmin(lowerLimit);
    }
    
    
    /**
     * Get the maximum value of y that is visible.
     * @return the maximum value of y that is visible
     */
    public double getMaxYLimit() {
        return ( isYAutoScale() ) ? chart.getInnerMaxY() : chart.getCurrentMaxY();
    }
    
    
    /**
     * Set the maximum value of y that is visible.
     * @param upperLimit the maximum value of y that is visible
     */
    public void setMaxYLimit(double upperLimit) {
        chart.getCurrentGL().setYmax(upperLimit);
    }
    
    
    /**
     * Scale the x and y axes once so all points fit on the chart then keep 
     * the axes' scales fixed.
     */
    public void scaleXandY() {
        scaleX();
        scaleY();
    }
    
    
    /**
     * Scale the x-axis once so all points fit on the chart along the x axis then 
     * keep the x-axis scale fixed.  Method intended for internal use.
     */
    protected void scaleX() {
        // auto scale
        chart.getCurrentGL().setXminOn(false);
        chart.getCurrentGL().setXmaxOn(false);
        chart.refreshGraphJPanel();
        
        // set fixed scale to auto scaled limits
        chart.getCurrentGL().setXmin( chart.getInnerMinX() );
        chart.getCurrentGL().setXmax( chart.getInnerMaxX() );
        chart.refreshGraphJPanel();
    }
    
    
    /**
     * Scale the y-axis once so all points fit on the chart along the y axis then 
     * keep the y-axis scale fixed.  Method intended for internal use.
     */
    protected void scaleY() {
        // auto scale
        chart.getCurrentGL().setYminOn(false);
        chart.getCurrentGL().setYmaxOn(false);
        chart.refreshGraphJPanel();
        
        // set fixed scale to auto scaled limits
        chart.getCurrentGL().setYmin( chart.getInnerMinY() );
        chart.getCurrentGL().setYmax( chart.getInnerMaxY() );
        chart.refreshGraphJPanel();
    }
    
    
    /**
     * Get the state of x-axis auto-scaling
     * @return true if the x-axis has auto-scaling enabled; false if not
     */
    public boolean isXAutoScale() {
        return !chart.getCurrentGL().isSetXmin();
    }
    
    
    /**
     * Set the auto-scale state of the x-axis
     * @param autoScale true to enable x-axis auto-scaling; false to disable auto-scaling
     */
    public void setXAutoScale(boolean autoScale) {
        // if we are presently auto scaling and we are changing to a fixed scale
        // then set the fixed scale to be consistent with the existing scale
        if ( !autoScale && isXAutoScale() ) {
            scaleX();
        }
		
		chart.getCurrentGL().setXminOn(!autoScale);
		chart.getCurrentGL().setXmaxOn(!autoScale);
		chart.getCurrentGL().setMajorTicksOnX(!autoScale);
		chart.refreshGraphJPanel();
    }
    
    
    /**
     * Get the state of y-axis auto-scaling
     * @return true if the y-axis has auto-scaling enabled; false if not
     */
    public boolean isYAutoScale() {
        return !chart.getCurrentGL().isSetYmin();
    }
    
    
    /**
     * Set the auto-scale state of the y-axis
     * @param autoScale true to enable y-axis auto-scaling; false to disable auto-scaling
     */
    public void setYAutoScale(boolean autoScale) {
        // if we are presently auto scaling and we are changing to a fixed scale
        // then set the fixed scale to be consistent with the existing scale
        if ( !autoScale && isYAutoScale() ) {
            scaleY();
        }
		
		chart.getCurrentGL().setYminOn(!autoScale);
		chart.getCurrentGL().setYmaxOn(!autoScale);
		chart.getCurrentGL().setMajorTicksOnY(!autoScale);
		chart.refreshGraphJPanel();
    }
	
	
    /**
     * Get the number of minor ticks per major step on the x-axis.
     * @return the number of minor ticks
     */
    public int getXNumMinorTicks() {
		return chart.getCurrentGL().getNumMinorTicksX();
	}
    
    
    /**
     * Set the number of minor ticks on the x-axis.
     * @param count number of minor ticks
     */
    public void setXNumMinorTicks(int count) {
		chart.getCurrentGL().setNumMinorTicksX(count);
	}
    
    
    /**
     * Get the number of major ticks on the x-axis.
     * @return the spacing per minor tick
     */
    public int getXNumMajorTicks() {
		return chart.getCurrentGL().getNumMajorTicksX();
	}
    
    
    /**
     * Set the number of major ticks on the x-axis.
     * @param count number of major ticks
     */
    public void setXNumMajorTicks(int count) {
		chart.getCurrentGL().setNumMajorTicksX(count);
	}
    
    
    /**
     * Get the number of minor ticks per major step on the y-axis.
     * @return the number of minor ticks
     */
    public int getYNumMinorTicks() {
		return chart.getCurrentGL().getNumMinorTicksY();
	}
    
    
    /**
     * Set the number of minor ticks on the y-axis.
     * @param count the number of minor ticks
     */
    public void setYNumMinorTicks(int count) {
		chart.getCurrentGL().setNumMinorTicksY(count);
	}
    
    
    /**
     * Get the number of minor ticks on the y-axis.
     * @return the number of major ticks
     */
    public int getYNumMajorTicks() {
		return chart.getCurrentGL().getNumMajorTicksY();
	}
    
    
    /**
     * Set the number of major ticks on the y-axis.
     * @param count the number of major ticks
     */
    public void setYNumMajorTicks(int count) {
		chart.getCurrentGL().setNumMajorTicksY(count);
	}
        
    
    /**
     * Get the visibility state of the x-axis grid.
     * @return true if the grid is visible
     */
    public boolean isXGridVisible() {
        return chart.getGridLinesVisibleX();
    }
    
    
    /**
     * Set the visibility of the x-axis grid.
     * @param visibility true to enable the grid; false to disable the grid
     */
    public void setXGridVisible(boolean visibility) {
        chart.setGridLinesVisibleX(visibility);
    }
    
    
    /**
     * Get the visibility state of the y-axis grid.
     * @return true if the grid is visible
     */
    public boolean isYGridVisible() {
        return chart.getGridLinesVisibleY();
    }
    
    
    /**
     * Set the visibility of the y-axis grid.
     * @param visibility true to enable the grid; false to disable the grid
     */
    public void setYGridVisible(boolean visibility) {
        chart.setGridLinesVisibleY(visibility);
    }
}
