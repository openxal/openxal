/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.app.lossviewer.waterfall;

/**
 *
 * @author az9
 */
public class DataSlice {
    public double[] data;
    public long timestamp;

    public DataSlice(long tst, double[] empty) {
        data=empty;
        timestamp = tst;
    }
}
