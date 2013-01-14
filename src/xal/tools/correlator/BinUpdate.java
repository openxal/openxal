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
interface BinUpdate<RecordType> {   
    public void newEvent( final String name, final RecordType record, final double timestamp );
}

