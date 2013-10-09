/*
 * TimeUnitsHandler.java
 *
 * Created on July 16, 2003, 12:55 PM
 */

package xal.app.scope;


/**
 * TimeUnitsHandler is the interface that different handlers implement to 
 * convert turns to some other unit of time.
 *
 * @author  tap
 */
public interface TimeUnitsHandler {
    /**
     * Get the label of the units.
     * @return The label of the units.
     */
    public String getUnits();
    
    
    /**
     * Convert an array of times in turns to a custom unit of time.  The conversion
     * is in place so that the times array is replaced with times in the new units.
     * @param times The time in turns to convert in place.
     */
    public void convertTurns(double[] times);
    
    
    /**
     * Dispose of any resources.
     */
    public void dispose();
}
