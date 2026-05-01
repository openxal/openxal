package xal.extension.widgets.plot;

import java.text.*;
import java.awt.*;

/**
 *  The grid limits class that specifies minimal and maximal values for x and y
 *  variables, numbers of major and minor ticks on the axises, and formats of
 *  the markers
 *
 *@author     shishlo
 *@version    1.0
 */
public class GridLimits {

    private double xMin, yMin, xMax, yMax;
    private boolean ixMin, iyMin, ixMax, iyMax;

    private NumberFormat numberFormatX = new DecimalFormat( "0.00E0" );
    private NumberFormat numberFormatY = new DecimalFormat( "0.00E0" );

    private boolean gridLimitsSwitchOnYes = false;

    private Color gridLineColor = Color.cyan;

    private int nMajorTicksX = 4;
    private int nMajorTicksY = 4;
    private int nMinorTicksX = 4;
    private int nMinorTicksY = 4;

    private boolean majorTicksOnX = false;
    private boolean majorTicksOnY = false;


    /**  Constructor for the GridLimits object */
    public GridLimits() {
        this.initialize();
    }


    /**  Sets the limits by using smart procedure for both x and y-axises */
    public void setSmartLimits() {
        setSmartLimitsX();
        setSmartLimitsY();
    }


    /**
     *  Sets the limits by using smart procedure for x-axis. This method will be
     *  defined in the sub-class. Here it is empty
     */
    public void setSmartLimitsX() {
    }


    /**
     *  Sets the limits by using smart procedure for x-axis This method will be
     *  defined in the sub-class. Here it is empty
     */
    public void setSmartLimitsY() {
    }


    /**
     *  Sets the numberFormatX attribute of the GridLimits object
     *
     *@param  numberFormatX_In  The new format for x-axis
     */
    public synchronized void setNumberFormatX( NumberFormat numberFormatX_In ) {
        numberFormatX = numberFormatX_In;
    }


    /**
     *  Sets the numberFormatY attribute of the GridLimits object
     *
     *@param  numberFormatY_In  The new format for x-axis
     */
    public synchronized void setNumberFormatY( NumberFormat numberFormatY_In ) {
        numberFormatY = numberFormatY_In;
    }


    /**
     *  Returns the format for x-axis
     *
     *@return    The format for x-axis
     */
    public synchronized NumberFormat getNumberFormatX() {
        return numberFormatX;
    }


    /**
     *  Returns the format for y-axis
     *
     *@return    The format for y-axis
     */
    public synchronized NumberFormat getNumberFormatY() {
        return numberFormatY;
    }


    /**
     *  Sets the minimal x-value
     *
     *@param  xMin  The new minimal x-value
     */
    public synchronized void setXmin( double xMin ) {
        if ( xMin < this.xMax ) {
            this.xMin = xMin;
            ixMin = true;
        }
        else {
            this.xMin = this.xMax;
            this.xMax = xMin;
            ixMax = true;
        }
    }


    /**
     *  Sets the minimal y-value
     *
     *@param  yMin  The new minimal y-value
     */
    public synchronized void setYmin( double yMin ) {
        if ( yMin < this.yMax ) {
            this.yMin = yMin;
            iyMin = true;
        }
        else {
            this.yMin = this.yMax;
            this.yMax = yMin;
            iyMax = true;
        }
    }


    /**
     *  Sets the maximal x-value
     *
     *@param  xMax  The new maximal x-value
     */
    public synchronized void setXmax( double xMax ) {
        if ( xMax > this.xMin ) {
            this.xMax = xMax;
            ixMax = true;
        }
        else {
            this.xMax = this.xMin;
            this.xMin = xMax;
            ixMin = true;
        }
    }


    /**
     *  Sets the maximal y-value
     *
     *@param  yMax  The new maximal y-value
     */
    public synchronized void setYmax( double yMax ) {
        if ( yMax > this.yMin ) {
            this.yMax = yMax;
            iyMax = true;
        }
        else {
            this.yMax = this.yMin;
            this.yMin = yMax;
            iyMin = true;
        }
    }


    /**
     *  Sets the boolean value that defines will the internal minimal x-value be
     *  used in the graph panel
     *
     *@param  ixMinIn  The boolean value
     */
    public synchronized void setXminOn( boolean ixMinIn ) {
        ixMin = ixMinIn;
    }


    /**
     *  Sets the boolean value that defines will the internal minimal y-value be
     *  used in the graph panel
     *
     *@param  iyMinIn  The boolean value
     */
    public synchronized void setYminOn( boolean iyMinIn ) {
        iyMin = iyMinIn;
    }


    /**
     *  Sets the boolean value that defines will the internal maximal x-value be
     *  used in the graph panel
     *
     *@param  ixMaxIn  The boolean value
     */
    public synchronized void setXmaxOn( boolean ixMaxIn ) {
        ixMax = ixMaxIn;
    }


    /**
     *  Sets the boolean value that defines will the internal maximal y-value be
     *  used in the graph panel
     *
     *@param  iyMaxIn  The boolean value
     */
    public synchronized void setYmaxOn( boolean iyMaxIn ) {
        iyMax = iyMaxIn;
    }


    /**
     *  Returns the boolean value that defines will the internal minimal x-value
     *  be used in the graph panel
     *
     *@return    The boolean value
     */
    public synchronized boolean isSetXmin() {
        return ixMin;
    }


    /**
     *  Returns the boolean value that defines will the internal minimal y-value
     *  be used in the graph panel
     *
     *@return    The boolean value
     */
    public synchronized boolean isSetYmin() {
        return iyMin;
    }


    /**
     *  Returns the boolean value that defines will the internal maximal x-value
     *  be used in the graph panel
     *
     *@return    The boolean value
     */
    public synchronized boolean isSetXmax() {
        return ixMax;
    }


    /**
     *  Returns the boolean value that defines will the internal maximal y-value
     *  be used in the graph panel
     *
     *@return    The boolean value
     */
    public synchronized boolean isSetYmax() {
        return iyMax;
    }


    /**
     *  Returns the internal minimal x-value
     *
     *@return    The internal minimal x-value
     */
    public synchronized double getMinX() {
        return xMin;
    }


    /**
     *  Returns the internal minimal y-value
     *
     *@return    The internal minimal y-value
     */
    public synchronized double getMinY() {
        return yMin;
    }


    /**
     *  Returns the internal maximal x-value
     *
     *@return    The internal maximal x-value
     */
    public synchronized double getMaxX() {
        return xMax;
    }


    /**
     *  Returns the internal maximal y-value
     *
     *@return    The internal maximal y-value
     */
    public synchronized double getMaxY() {
        return yMax;
    }


    /**  Initializes all internal parameters in the initial state */
    public void initialize() {
        xMax = Double.MAX_VALUE;
        xMin = -Double.MAX_VALUE;
        yMax = Double.MAX_VALUE;
        yMin = -Double.MAX_VALUE;
        ixMin = false;
        iyMin = false;
        ixMax = false;
        iyMax = false;
        majorTicksOnX = false;
        majorTicksOnY = false;
        nMajorTicksX = 4;
        nMajorTicksY = 4;
        nMinorTicksX = 4;
        nMinorTicksY = 4;
    }


    /**  Initializes all internal parameters for x-axis in the initial state */
    public void initializeX() {
        xMax = Double.MAX_VALUE;
        xMin = -Double.MAX_VALUE;
        ixMin = false;
        ixMax = false;
        majorTicksOnX = false;
        nMajorTicksX = 4;
        nMinorTicksX = 4;
    }


    /**  Initializes all internal parameters for y-axis in the initial state */
    public void initializeY() {
        yMax = Double.MAX_VALUE;
        yMin = -Double.MAX_VALUE;
        iyMin = false;
        iyMax = false;
        majorTicksOnY = false;
        nMajorTicksY = 4;
        nMinorTicksY = 4;
    }


    /**
     *  Returns the boolean value that specifies if this GridLimits object will
     *  be used to define marks on axis and minimal and maximal values
     *
     *@return    The boolean value
     */
    public boolean getGridLimitsSwitch() {
        return gridLimitsSwitchOnYes;
    }


    /**
     *  Sets the boolean value that specifies if this GridLimits object will be
     *  used to define marks on axis and minimal and maximal values
     *
     *@param  gridLimitsSwitchOnYesIn  The boolean value
     */
    public void setGridLimitsSwitch( boolean gridLimitsSwitchOnYesIn ) {
        gridLimitsSwitchOnYes = gridLimitsSwitchOnYesIn;
    }


    /**
     *  Returns the color of the grid lines
     *
     *@return    The color of the grid lines
     */
    public Color getColor() {
        return gridLineColor;
    }


    /**
     *  Sets the color color of the grid lines
     *
     *@param  clr  The new color the grid lines
     */
    public void setColor( Color clr ) {
        if ( clr != null ) {
            gridLineColor = clr;
        }
    }


    /**
     *  Sets the sets the number of major ticks on the x-axis
     *
     *@param  nMajorTicksXIn  The number of major ticks on the x-axis
     */
    public void setNumMajorTicksX( int nMajorTicksXIn ) {
        nMajorTicksX = nMajorTicksXIn;
    }


    /**
     *  Returns the number of major ticks on the x-axis
     *
     *@return    The number of major ticks on the x-axis
     */
    public int getNumMajorTicksX() {
        return nMajorTicksX;
    }


    /**
     *  Sets the number of major ticks on the y-axis
     *
     *@param  nMajorTicksYIn  The number of major ticks on the y-axis
     */
    public void setNumMajorTicksY( int nMajorTicksYIn ) {
        nMajorTicksY = nMajorTicksYIn;
    }


    /**
     *  Returns the number of major ticks on the y-axis
     *
     *@return    The number of major ticks on the y-axis
     */
    public int getNumMajorTicksY() {
        return nMajorTicksY;
    }


    /**
     *  Sets the number of minor ticks on the x-axis
     *
     *@param  nMinorTicksXIn  The number of minor ticks on the x-axis
     */
    public void setNumMinorTicksX( int nMinorTicksXIn ) {
        nMinorTicksX = nMinorTicksXIn;
    }


    /**
     *  Returns the number of minor ticks on the x-axis
     *
     *@return    The number of minor ticks on the x-axis
     */
    public int getNumMinorTicksX() {
        return nMinorTicksX;
    }


    /**
     *  Sets the number of minor ticks on the y-axis
     *
     *@param  nMinorTicksYIn  The number of minor ticks on the y-axis
     */
    public void setNumMinorTicksY( int nMinorTicksYIn ) {
        nMinorTicksY = nMinorTicksYIn;
    }


    /**
     *  Returns the number of minor ticks on the y-axis
     *
     *@return    The number of minor ticks on the y-axis
     */
    public int getNumMinorTicksY() {
        return nMinorTicksY;
    }


    /**
     *  Sets the boolean value that defines if the major sticks number parameter
     *  will be used on the graph panel
     *
     *@param  majorTicksOnXIn  The boolean value
     */
    public void setMajorTicksOnX( boolean majorTicksOnXIn ) {
        majorTicksOnX = majorTicksOnXIn;
    }


    /**
     *  Returns the boolean value that defines if the major sticks number
     *  parameter will be used on the graph panel
     *
     *@return    The boolean value
     */
    public boolean getMajorTicksOnX() {
        return majorTicksOnX;
    }


    /**
     *  Sets the boolean value that defines if the major sticks number parameter
     *  will be used on the graph panel
     *
     *@param  majorTicksOnYIn  The boolean value
     */
    public void setMajorTicksOnY( boolean majorTicksOnYIn ) {
        majorTicksOnY = majorTicksOnYIn;
    }


    /**
     *  Returns the boolean value that defines if the major sticks number
     *  parameter will be used on the graph panel
     *
     *@return    The boolean value
     */
    public boolean getMajorTicksOnY() {
        return majorTicksOnY;
    }


    //----------------------------------------
    //convenience methods of GridLimits class
    //----------------------------------------

    /**
     *  Sets the minimal value, step, number of steps, and number of minor ticks
     *  on the x-axis
     *
     *@param  vMin           The new minimal value
     *@param  step           The new step
     *@param  nStep          The new number of steps
     *@param  nMinorTicksIn  The new number of minor ticks
     */
    public void setLimitsAndTicksX( double vMin, double step, int nStep, int nMinorTicksIn ) {
        if ( step == 0. ) {
            return;
        }
        if ( step < 0. ) {
            step = -step;
        }
        majorTicksOnX = true;
        if ( vMin < this.xMax ) {
            setXmin( vMin );
            setXmax( vMin + step * nStep );
        }
        else {
            setXmax( vMin + step * nStep );
            setXmin( vMin );
        }
        nMajorTicksX = nStep + 1;
        nMinorTicksX = nMinorTicksIn;
        gridLimitsSwitchOnYes = true;
    }


    /**
     *  Sets the minimal value, step, and number of steps on the x-axis. The
     *  number of minor ticks will be what is was before
     *
     *@param  vMin   The new minimal value
     *@param  step   The new step
     *@param  nStep  The new number of steps
     */
    public void setLimitsAndTicksX( double vMin, double step, int nStep ) {
        setLimitsAndTicksX( vMin, step, nStep, nMinorTicksX );
    }


    /**
     *  Sets the minimal value, step, number of steps, and number of minor ticks
     *  on the y-axis
     *
     *@param  vMin           The new minimal value
     *@param  step           The new step
     *@param  nStep          The new number of steps
     *@param  nMinorTicksIn  The new number of minor ticks
     */
    public void setLimitsAndTicksY( double vMin, double step, int nStep, int nMinorTicksIn ) {
        if ( step == 0. ) {
            return;
        }
        if ( step < 0. ) {
            step = -step;
        }
        majorTicksOnY = true;
        if ( vMin < this.yMax ) {
            setYmin( vMin );
            setYmax( vMin + step * nStep );
        }
        else {
            setYmax( vMin + step * nStep );
            setYmin( vMin );
        }
        nMajorTicksY = nStep + 1;
        nMinorTicksY = nMinorTicksIn;
        gridLimitsSwitchOnYes = true;
    }


    /**
     *  Sets the minimal value, step, and number of steps on the y-axis. The
     *  number of minor ticks will be what is was before
     *
     *@param  vMin   The new minimal value
     *@param  step   The new step
     *@param  nStep  The new number of steps
     */
    public void setLimitsAndTicksY( double vMin, double step, int nStep ) {
        setLimitsAndTicksY( vMin, step, nStep, nMinorTicksY );
    }


    /**
     *  Sets the minimal value, maximal value, step, and number of minor ticks
     *  on the x-axis
     *
     *@param  vMin           The new new minimal value
     *@param  vMax           The new new maximal value
     *@param  step           The new step
     *@param  nMinorTicksIn  The new number of minor ticks
     */
    public void setLimitsAndTicksX( double vMin, double vMax, double step, int nMinorTicksIn ) {
        if ( step == 0. ) {
            return;
        }
        int nStep = (int) Math.round( ( vMax - vMin ) / step );
        if ( nStep <= 0 ) {
            nStep = 1;
        }
        step = ( vMax - vMin ) / nStep;
        setLimitsAndTicksX( vMin, step, nStep, nMinorTicksIn );
    }


    /**
     *  Sets the minimal value, maximal value, step, and number of minor ticks
     *  on the x-axis. The number of minor ticks will be what is was before
     *
     *@param  vMin  The new minimal value
     *@param  vMax  The new maximal value
     *@param  step  The new step
     */
    public void setLimitsAndTicksX( double vMin, double vMax, double step ) {
        setLimitsAndTicksX( vMin, vMax, step, nMinorTicksX );
    }


    /**
     *  Sets the minimal value, maximal value, step, and number of minor ticks
     *  on the y-axis
     *
     *@param  vMin           The new new minimal value
     *@param  vMax           The new new maximal value
     *@param  step           The new step
     *@param  nMinorTicksIn  The new number of minor ticks
     */
    public void setLimitsAndTicksY( double vMin, double vMax, double step, int nMinorTicksIn ) {
        if ( step == 0. ) {
            return;
        }
        int nStep = (int) Math.round( ( vMax - vMin ) / step );
        if ( nStep <= 0 ) {
            nStep = 1;
        }
        step = ( vMax - vMin ) / nStep;
        setLimitsAndTicksY( vMin, step, nStep, nMinorTicksIn );
    }


    /**
     *  Sets the minimal value, maximal value, step, and number of minor ticks
     *  on the y-axis. The number of minor ticks will be what is was before
     *
     *@param  vMin  The new minimal value
     *@param  vMax  The new maximal value
     *@param  step  The new step
     */
    public void setLimitsAndTicksY( double vMin, double vMax, double step ) {
        setLimitsAndTicksY( vMin, vMax, step, nMinorTicksY );
    }
}
