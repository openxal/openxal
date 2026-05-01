/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import java.util.Date;

/**
 *
 * @author sashazhukov
 */
public class DefaultDataRecord implements DataRecord {

    private final long millis;
    private final double[] data;
    private final static int MAXELEMENTTOPRINT = 10;
    
    private double scaling = 1.0;

    public void setScaling(double s){
        scaling = s;
    }
    
    public double getScaling(){
        return scaling;
    }
    
    public DefaultDataRecord() {
        millis = 0;
        data = null;
    }

    DefaultDataRecord(long millis, double[] data) {
        this.millis = millis;
        this.data = data;
    }

    public long getMillis() {
        return millis;
    }

    public double getValue(int index) throws IndexOutOfBoundsException {
        return scaling*data[index];
    }

    public int size() {
        return data.length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date(getMillis())).append(" ").append(valueToString());
        return sb.toString();
    }

    protected String valueToString() {
        StringBuilder sb = new StringBuilder();
        int isize = Math.min(MAXELEMENTTOPRINT, size());
        sb.append("[");
        for (int i = 0; i < isize; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(getValue(i));
        }
        int left = size() - isize;

        if (left > 0) {
            sb.append(" and other ").append(left).append(" values]");
        } else {
            sb.append("]");
        }
        return sb.toString();

    }
}
