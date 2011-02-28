/*
 * BinUpdate.java
 *
 * Created on June 27, 2002, 10:11 AM
 */

package xal.tools.correlator;

/**
 *
 * @author  tap
 * @version 
 */
interface BinUpdate {   
    public void newEvent(String name, Object record, double timestamp);
}

