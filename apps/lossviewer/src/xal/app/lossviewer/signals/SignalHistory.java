/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.app.lossviewer.signals;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author az9
 */
public class SignalHistory {

    private ArrayList<SignalValue> data;
    private int size,  nonZeroCounts = 0;
    private double sumValue = 0;
    private double sumValueSquared = 0;

    public SignalHistory(int size) {
        data = new ArrayList<SignalValue>(size + 1);
        this.size = size;
    }

    private SignalHistory(List<SignalValue> data, int size, int nonZeroCounts,
            double sumValue, double sumValueSquared) {
        this(size);
        this.data.addAll(data);
        this.nonZeroCounts = nonZeroCounts;
        this.sumValue = sumValue;
        this.sumValueSquared = sumValueSquared;
    }

    private SignalValue findByTimestamp0(long tst, int index, long tolerance) {
        ScalarSignalValue sv = null;
        if (index == -1) {
            return binarySearch(tst, tolerance);
        } else if (index >= 0 && index < data.size()) {
            sv = (ScalarSignalValue) get(index);
            if (sv != null && sv.getTimestamp() == tst) {
                return sv;
            }
        }
        return null;
    }

    public double findByTimestamp(long tst, int index, long tolerance) {
        ScalarSignalValue sv = null;
        sv = (ScalarSignalValue)findByTimestamp0(tst, index, tolerance);
        if (sv == null) {
            sv = (ScalarSignalValue)findByTimestamp0(tst, index - 1, tolerance);
        }
        if (sv == null) {
            sv = (ScalarSignalValue)findByTimestamp0(tst, index + 1, tolerance);
        }
        if (sv == null) {
            sv = (ScalarSignalValue)findByTimestamp0(tst, -1, tolerance);
        }
        if (sv == null) {
            return 0.0;
        } else {
            return sv.getValue();
        }

    }
//Shamelessly stolen from Sun implementation
    private SignalValue binarySearch(long key, long tolerance) {
        int low = 0;
        int high = size();

        while (low <= high) {
            int mid = (low + high) >>> 1;

            SignalValue sv = get(mid);
            if(sv==null){
                return null;
            }
            long midVal = get(mid).getTimestamp();

            if (midVal < key - tolerance) {
                low = mid + 1;
            } else if (midVal > key + tolerance) {
                high = mid - 1;
            } else {
                return get(mid); // key found
            }
        }
        return null;  // key not found.
    }

    public int getNonZeroCount() {
        return nonZeroCounts;
    }

    public Point2D getRange() {
        double max = Double.NEGATIVE_INFINITY;
        double min = Double.POSITIVE_INFINITY;

        for (SignalValue sv : data) {
            double value = ((ScalarSignalValue) sv).getValue();
            if (max < value) {
                max = value;
            }
            if (min > value) {
                min = value;
            }
        }

        Point2D range = new Point2D.Double(min, max);
        return range;
    }

    public int size() {
        return data.size();
    }

    public synchronized SignalHistory copy() {
        SignalHistory sh = new SignalHistory(data, size, nonZeroCounts, sumValue, sumValueSquared);
//        for(SignalValue sv: data){
//            sh.add(sv);
//        }
        return sh;
    }

    public SignalValue get(int i) {
        if (i < 0 || i >= size()) {
            return null;
        }
        return data.get(i);
    }

    public SignalValue getBackWard(int i) {
        return get(size() - i - 1);
    }

    public double getSum() {
        return sumValue;
    }

    public double getSumSquared() {
        return sumValueSquared;
    }

    public synchronized void addSync(SignalValue sv) {

        add(sv);

    }

    public void add(SignalValue sv) {
        if (sv == null) {
            return;
        }
        double val = ((ScalarSignalValue) sv).getValue();



        data.add(sv);
        if (val > 0.0) {
            sumValue += val;
            sumValueSquared += val * val;
            nonZeroCounts++;
        }
        while (data.size() > size) {
            double oldVal = ((ScalarSignalValue) data.remove(0)).getValue();

            if (oldVal > 0.0) {
                sumValue -= oldVal;
                sumValueSquared -= oldVal * oldVal;
                nonZeroCounts--;
            }
        }
    }
}
