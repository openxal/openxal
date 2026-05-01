/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

/**
 *
 * @author sashazhukov
 */
public interface DataRecord {

    long getMillis();

    double getValue(int index) throws IndexOutOfBoundsException;
    
}
