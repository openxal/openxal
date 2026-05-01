/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.io.IOException;

/**
 *
 * @author sashazhukov
 */
public interface TimeData <E extends DataRecord> {
  

    E getDataRecord(int index) throws IndexOutOfBoundsException;

    int size();
    void close() throws IOException;
    
}
