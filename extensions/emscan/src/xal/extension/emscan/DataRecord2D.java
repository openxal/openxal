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
public class DataRecord2D extends DefaultDataRecord {

    DataRecord2D(long millis, double[] data) {
        super(millis, data);
    }

    public double getValue(int index) throws IndexOutOfBoundsException {
        return super.getValue(index + 2);
    }

    public int size() {
        return super.size() - 2;
    }

    public double getX() {
        return super.getValue(0);
    }

    public double getY() {
        return super.getValue(1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(new Date(getMillis())).append(" ").append(getX()).append(", ").append(getY()).append(" ").append(valueToString());
        return sb.toString();
    }
}
