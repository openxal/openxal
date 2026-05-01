/*
 * ScopeDataModel.java
 *
 * Created on December 17, 2002, 2:47 PM
 */

package xal.app.scope;

import java.util.*;

/**
 * Implementation of ChartDataModel to display scope data
 *
 * @author  tap
 */
public class ScopeDataModel {
    protected double[][] traces;
    protected double[][] elementTimes;
    
    
    /** Creates a new instance of ScopeDataModel */
    public ScopeDataModel() {
        traces = null;
        elementTimes = null;
    }
    
    
    /** Replace the records that are the source of series data */
    public void setSeriesData(double[][] newTraces, double[][] newElementTimes) {
        traces = newTraces;
        elementTimes = newElementTimes;
    }
    
    
    /** Implement the ChartDataModel interface */
    public int getNumSeries() {
        if ( traces == null )  return 0;
        return traces.length;
    }
    
    
    /** Implement the ChartDataModel interface */
    public double[] getXSeries(int seriesIndex) {
        if ( elementTimes == null )  return new double[0];
        if ( elementTimes[seriesIndex] == null )  return new double[0];
        return elementTimes[seriesIndex];
    }

    
    /** Implement the ChartDataModel interface */
    public double[] getYSeries(int seriesIndex) {
        if ( traces == null )  return new double[0];
        if ( traces[seriesIndex] == null )  return new double[0];
        return traces[seriesIndex];
    }    
}
