package xal.app.lossviewer.signals;

import java.util.*;

public class TimerSignal extends AbstractSignal {

    private long delay;
    private long period;
    private double amp;

    public void start() {
        t.schedule(new TimerTask() {

            public void run() {
                double v = Math.random() * amp;
                long tst = System.currentTimeMillis();
                //		System.out.println(getName()+" "+getID()+" "+v);
                dispatcher.processNewValue(new ScalarSignalValue(TimerSignal.this, tst, v));
            }
        }, delay, period);
    }

    public void close() {
    // TODO: Implement this method
    }

    public double doubleValue() {
        return 0.0;
    }

    public TimerSignal() {
        this(0, 5, 5.0);
    }
    Timer t;

    public TimerSignal(final int delay, final int period, final double amp) {
        this.delay = delay;
        this.amp = amp;
        this.period = period;
        t = new Timer();
    //	System.out.println(getName()+" "+getID());

    }

    public boolean setValue(double v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  
}
