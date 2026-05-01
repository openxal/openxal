/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xal.extension.emscan;

import xal.ca.Channel;
import xal.ca.ChannelTimeRecord;
import xal.ca.ConnectionException;
import xal.ca.IEventSinkValTime;
import xal.ca.MonitorException;



/**
 *
 * @author sashazhukov
 */
public class ScalarSampler {

    private final Channel channel;
    private int numOfMeasurements = 0;
    private boolean measuring;
    private int measurementsDone = 0;
    private double sum = 0;
    private double sumsq = 0;
    private double mean = 0;
    private double sigma = 0;
    private boolean interrupted = false;

    public ScalarSampler(Channel ch) throws ConnectionException, MonitorException {
        this.channel = ch;
        ch.addMonitorValTime(new IEventSinkValTime() {

            @Override
            public void eventValue(ChannelTimeRecord record, Channel chan) {
                processEvents(record, chan);

            }
        }, 1);
    }

    protected synchronized void processEvents(ChannelTimeRecord record, Channel chan) {
        if (measuring) {
            double value = record.doubleValue();
            sum += value;
            sumsq += value * value;
            measurementsDone++;
            if (measurementsDone >= numOfMeasurements) {
                endMeasuring(false);
            }
        }
    }

    private synchronized void endMeasuring(boolean interrupt) {
        measuring = false;
        interrupted = interrupt;
        calculate();
    }

    public synchronized void measure(int measurements) {
        this.numOfMeasurements = measurements;
        measuring = true;
        sum = 0;
        sumsq = 0;
        measurementsDone = 0;
        interrupted=false;
    }

    public synchronized boolean isMeasuringDone() {
        return measuring;
    }

    private synchronized void calculate() {
        this.mean = sum / (double) measurementsDone;
        this.sigma = Math.sqrt(sumsq / (double) measurementsDone - mean * mean);
    }

    public boolean waitMeasurementDone(long millis, boolean interrupt) throws InterruptedException {
        int iterations = (int) (millis / 2 + 1);
        for (int i = 0; i < iterations; i++) {
            if (!isMeasuringDone()) {
                return true;
            }
            Thread.sleep(2);
        }
        if (interrupt) {
            endMeasuring(true);
        }
        return false;

    }

    public double getMean() {
        return mean;
    }

    public double getSigma() {
        return sigma;
    }

    public int getMeasurementDone() {
        return measurementsDone;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(channel.channelName()).append(": ");
        if (measuring) {
            sb.append("Measuring ").append(getMeasurementDone()).append(" not done...");
        } else {
            if (interrupted) {
                sb.append("Measurement interrupted, ");
            }
            else {
                sb.append("Measurement completed, ");
            }
            sb.append(getMeasurementDone()).append(" measurements done, mean = ")
                    .append(getMean()).append(", sigma = ").append(getSigma());
        }

        return sb.toString();
    }
}
