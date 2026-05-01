package xal.extension.emscan;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sashazhukov
 */
public interface DataRecordFactory<E extends DataRecord> {
    E create(long millis, double[] data);
}
